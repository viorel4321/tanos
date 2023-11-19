package l2s.gameserver.instancemanager;

import l2s.commons.ban.BanBindType;
import l2s.commons.ban.BanInfo;
import l2s.commons.ban.BanManager;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.GameBansDAO;
import l2s.gameserver.database.mysql;
import l2s.gameserver.listener.actor.CharListenerList;
import l2s.gameserver.listener.actor.OnPlayerEnterListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.KickPlayerInGameTask;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 **/
public class GameBanManager extends BanManager {
	private class BanListeners implements OnPlayerEnterListener {
		@Override
		public void onPlayerEnter(Player player) {
			BanInfo banInfo = getBanInfoIfBanned(BanBindType.CHAT, player.getObjectId());
			if(banInfo == null)
				return;

			onBan(BanBindType.CHAT, player.getObjectId(), banInfo, false);
		}
	}

	private static final GameBanManager INSTANCE = new GameBanManager();

	public static GameBanManager getInstance() {
		return INSTANCE;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(GameBanManager.class);

	private final Lock lock = new ReentrantLock();

	private final BanListeners listeners = new BanListeners();

	private ScheduledFuture<?> checkBansTask = null;

	public void init() {
		GameBansDAO.getInstance().cleanUp();
		startCheckBansTask();
		CharListenerList.addGlobal(listeners);
		LOGGER.info("GameBanManager: Initialized.");
	}

	private void startCheckBansTask() {
		if (checkBansTask != null)
			return;

		long interval = TimeUnit.MINUTES.toMillis(Config.CHECK_BANS_INTERVAL);
		checkBansTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> checkBans(), 0, interval);
	}

	private void checkBans() {
		lock.lock();
		try {
			for (BanBindType bindType : BanBindType.VALUES) {
				if(!bindType.isGame())
					continue;

				Map<String, BanInfo> bans = new HashMap<>();
				GameBansDAO.getInstance().select(bans, bindType);

				for(Map.Entry<String, BanInfo> entry : bans.entrySet())
					onBan(bindType, entry.getKey(), entry.getValue(), false);

				getCachedBans().put(bindType, bans);
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean giveBan(BanBindType bindType, String bindValue, int endTime, String reason) {
		if(!bindType.isGame())
			return false;

		if (StringUtils.isEmpty(bindValue))
			return false;

		if (endTime != -1 && endTime < (System.currentTimeMillis() / 1000))
			return false;

		lock.lock();
		try {
			BanInfo banInfo = new BanInfo(endTime, reason);
			if (!GameBansDAO.getInstance().insert(bindType, bindValue, banInfo))
				return false;

			getCachedBans().computeIfAbsent(bindType, (b) -> new HashMap<>()).put(bindValue, banInfo);
			onBan(bindType, bindValue, banInfo, false);
		} finally {
			lock.unlock();
		}
		return true;
	}

	public boolean removeBan(BanBindType bindType, String bindValue) {
		if(!bindType.isGame())
			return false;

		if (StringUtils.isEmpty(bindValue))
			return false;

		lock.lock();
		try {
			Map<String, BanInfo> bans = getCachedBans().get(bindType);
			if (bans == null)
				return false;

			if (!GameBansDAO.getInstance().delete(bindType, bindValue))
				return false;

			bans.remove(bindValue);
			onUnban(bindType, bindValue, false);
			return true;
		} finally {
			lock.unlock();
		}
	}

	public static void onBan(BanBindType bindType, Object bindValueObj, BanInfo banInfo, boolean auth) {
		if(auth && !bindType.isAuth() || !auth && !bindType.isGame())
			return;

		int endTime = banInfo.getEndTime();
		if (endTime != -1 && endTime < (System.currentTimeMillis() / 1000))
			return;

		String bindValue = String.valueOf(bindValueObj);

		List<GameClient> gameClients = new ArrayList<>();
		if (bindType == BanBindType.LOGIN) {
			GameClient client = AuthServerCommunication.getInstance().getWaitingClient(bindValue);
			if (client != null)
				gameClients.add(client);

			client = AuthServerCommunication.getInstance().getAuthedClient(bindValue);
			if (client != null)
				gameClients.add(client);
		} else if (bindType == BanBindType.IP) {
			gameClients.addAll(AuthServerCommunication.getInstance().getWaitingClientsByIP(bindValue));
			gameClients.addAll(AuthServerCommunication.getInstance().getAuthedClientsByIP(bindValue));
		} else if (bindType == BanBindType.HWID) {
			gameClients.addAll(AuthServerCommunication.getInstance().getWaitingClientsByHWID(bindValue));
			gameClients.addAll(AuthServerCommunication.getInstance().getAuthedClientsByHWID(bindValue));
		} else if (bindType == BanBindType.PLAYER) {
			Player player = GameObjectsStorage.getPlayer(Integer.parseInt(bindValue));
			if(player == null)
				return;

			GameClient gameClient = player.getNetConnection();
			if(gameClient == null)
				return;

			gameClients.add(gameClient);
		} else if (bindType == BanBindType.CHAT) {
			Player player = GameObjectsStorage.getPlayer(Integer.parseInt(bindValue));
			if(player == null)
				return;

			if(!player.startBanEndTask(bindType, endTime))
				return;

			CustomMessage banMsg = new CustomMessage("l2s.gameserver.instancemanager.GameBanManager.game.banned.chat");
			CustomMessage endTimeMsg = new CustomMessage("l2s.gameserver.instancemanager.GameBanManager.endtime");
			CustomMessage reasonMsg = null;

			if(endTime != -1)
				endTimeMsg = endTimeMsg.addString(TimeUtils.toSimpleFormat(endTime * 1000L));
			else
				endTimeMsg = endTimeMsg.addCustomMessage(new CustomMessage("l2s.gameserver.instancemanager.GameBanManager.never"));

			String reason = banInfo.getReason();
			if (!StringUtils.isEmpty(reason))
				reasonMsg = new CustomMessage("l2s.gameserver.instancemanager.GameBanManager.reason").addString(reason);

			player.sendPacket(banMsg);
			if (reasonMsg != null)
				player.sendPacket(reasonMsg);
			if (endTimeMsg != null)
				player.sendPacket(endTimeMsg);
			return;
		}

		if(gameClients.isEmpty())
			return;

		CustomMessage banMsg = new CustomMessage(String.format("l2s.gameserver.instancemanager.GameBanManager.%s.banned.%s", (auth ? "auth" : "game"), bindType.toString().toLowerCase()));
		CustomMessage endTimeMsg = new CustomMessage("l2s.gameserver.instancemanager.GameBanManager.endtime");
		CustomMessage reasonMsg = null;

		if(endTime != -1)
			endTimeMsg = endTimeMsg.addString(TimeUtils.toSimpleFormat(endTime * 1000L));
		else
			endTimeMsg = endTimeMsg.addCustomMessage(new CustomMessage("l2s.gameserver.instancemanager.GameBanManager.never"));

		String reason = banInfo.getReason();
		if (!StringUtils.isEmpty(reason))
			reasonMsg = new CustomMessage("l2s.gameserver.instancemanager.GameBanManager.reason").addString(reason);

		for (GameClient gameClient : gameClients) {
			AuthServerCommunication.getInstance().removeClient(gameClient);

			final Player activeChar = gameClient.getActiveChar();
			if (activeChar != null) {
				activeChar.sendPacket(banMsg);
				if (reasonMsg != null)
					activeChar.sendPacket(reasonMsg);
				if (endTimeMsg != null)
					activeChar.sendPacket(endTimeMsg);
				ThreadPoolManager.getInstance().schedule(new KickPlayerInGameTask(gameClient), 500L);
			} else
				gameClient.close(Msg.ServerClose);
		}
	}

	public static void onUnban(BanBindType bindType, Object bindValueObj, boolean auth) {
		if(auth && !bindType.isAuth() || !auth && !bindType.isGame())
			return;

		String bindValue = String.valueOf(bindValueObj);

		if (bindType == BanBindType.CHAT) {
			Player player = GameObjectsStorage.getPlayer(Integer.parseInt(bindValue));
			if(player != null) {
				player.stopBanEndTask(bindType);
				player.sendPacket(new CustomMessage("l2s.gameserver.instancemanager.GameBanManager.game.unbanned.chat"));
			}
		}
	}
}
