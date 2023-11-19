package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.ban.BanBindType;
import l2s.commons.ban.BanInfo;
import l2s.gameserver.instancemanager.GameBanManager;
import l2s.gameserver.utils.TimeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.IVoicedCommandHandler;
import l2s.gameserver.handler.VoicedCommandHandler;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.WorldRegion;
import l2s.gameserver.model.PartyRoom;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.Say2;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.tables.MapRegionTable;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.SpamFilter;
import l2s.gameserver.utils.Strings;

public class Say2C extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Say2C.class);

	private String _text;
	private ChatType _type;
	private String _target;

	@Override
	public void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = l2s.commons.lang.ArrayUtils.valid(ChatType.VALUES, readD());
		_target = _type == ChatType.TELL ? readS(Config.CNAME_MAXLEN) : null;
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isKeyBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(_text == null || _text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		_text = _text.replaceAll("\u2116", "");
		_text = _text.replaceAll("\\\\n", "\n");
		if(_text.contains("\n"))
		{
			final String[] lines = _text.split("\n");
			_text = "";
			for(int i = 0; i < lines.length; ++i)
			{
				lines[i] = lines[i].trim();
				if(lines[i].length() != 0)
				{
					if(_text.length() > 0)
						_text += "\n  >";
					_text += lines[i];
				}
			}
		}

		if(_text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(Config.VIKTORINA_ENABLED && Functions.isEventStarted("events.Viktorina.Viktorina"))
		{
			final String answer = _text.trim();
			if(answer.length() > 0)
			{
				final Object[] objects = { answer, activeChar };
				Functions.callScripts("events.Viktorina.Viktorina", "checkAnswer", objects);
			}
		}

		if(_text.startsWith("."))
		{
			if(Config.ALLOW_VOICED_COMMANDS)
			{
				String fullcmd = _text.substring(1).trim();
				String command = fullcmd.split("\\s+")[0];
				String args = fullcmd.substring(command.length()).trim();

				if(command.length() > 0)
				{
					// then check for VoicedCommands
					IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					if(vch != null)
					{
						vch.useVoicedCommand(command, activeChar, args);
						return;
					}
				}
				activeChar.sendMessage(new CustomMessage("common.command404"));
				return;
			}
		}

		final boolean globalchat = _type != ChatType.ALLIANCE && _type != ChatType.CLAN && _type != ChatType.PARTY;
		if((globalchat || ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type.ordinal()))) {
			final BanInfo banInfo = GameBanManager.getInstance().getBanInfoIfBanned(BanBindType.CHAT, activeChar.getObjectId());
			if (banInfo != null) {
				if (banInfo.getEndTime() == - 1)
					activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently"));
				else
					activeChar.sendMessage(new CustomMessage("common.ChatBanned").addString(TimeUtils.toSimpleFormat(banInfo.getEndTime() * 1000L)));
				activeChar.sendActionFailed();
				return;
			}

			if (activeChar.getNoChannel() != 0L) {
				if (activeChar.getNoChannelRemained() > 0L || activeChar.getNoChannel() < 0L) {
					if (activeChar.getNoChannel() > 0L)
						activeChar.sendMessage(new CustomMessage("common.ChatBanned").addString(TimeUtils.toSimpleFormat(System.currentTimeMillis() + activeChar.getNoChannelRemained())));
					else
						activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently"));
					activeChar.sendActionFailed();
					return;
				}
				activeChar.updateNoChannel(0L);
			}
		}

		final boolean noSpam = SpamFilter.getInstance().checkSpam(activeChar, _text, _type.ordinal());
		if(globalchat)
		{
			if(Config.MAT_REPLACE)
			{
				if(Config.containsAbuseWord(_text))
					_text = Config.MAT_REPLACE_STRING;
			}
			else if(Config.MAT_BANCHAT && Config.containsAbuseWord(_text))
			{
				activeChar.sendMessage("You are banned in all chats. Time to unban: " + Config.UNCHATBANTIME * 60 + " sec.");
				Log.addLog(activeChar + ": " + _text, "abuse");
				activeChar.updateNoChannel(Config.UNCHATBANTIME * 60000);
				activeChar.sendActionFailed();
				return;
			}
		}

		final String translit = activeChar.getVar("translit");
		if(translit != null)
			_text = Strings.fromTranslit(_text, translit.equals("tl") ? 1 : 2);
		Say2 cs = new Say2(activeChar.getObjectId(), _type, activeChar.getName(), _text);
		final int mapregion = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
		final long curTime = System.currentTimeMillis();
		switch(_type)
		{
			case TELL:
			{
				if(activeChar.getLevel() < Config.TELL_CHAT_MIN_LVL)
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u041f\u0440\u0438\u0432\u0430\u0442\u043d\u044b\u0439 \u0447\u0430\u0442 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u0441 " + Config.TELL_CHAT_MIN_LVL + "-\u0433\u043e \u0443\u0440\u043e\u0432\u043d\u044f.");
					else
						activeChar.sendMessage("Tell chat is allowed from " + Config.TELL_CHAT_MIN_LVL + " level only.");
					return;
				}
				if(Config.NO_TELL_JAILED && activeChar.getVar("jailed") != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "\u041f\u0440\u0438\u0432\u0430\u0442\u043d\u044b\u0439 \u0447\u0430\u0442 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d, \u043f\u043e\u043a\u0430 \u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u0442\u044e\u0440\u044c\u043c\u0435." : "Tell chat can not be used in jail.");
					return;
				}
				final Player receiver = World.getPlayer(_target);
				if(receiver == null)
				{
					activeChar.sendPacket(new SystemMessage(3).addString(_target), Msg.ActionFail);
					break;
				}
				if(receiver.isInOfflineMode())
				{
					activeChar.sendMessage("The person is in offline trade mode.");
					activeChar.sendActionFailed();
					break;
				}
				if(receiver.isBlockAll() || receiver.isInBlockList(activeChar))
				{
					activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED, Msg.ActionFail);
					break;
				}
				final Long lastTellTime = (Long) activeChar.getProperty("Say2.TellChatLaunched");
				if(lastTellTime != null && lastTellTime + (activeChar.getLevel() >= Config.TELL_DELAY_LEVEL ? 1000L : Config.TELL_DELAY_TIME * 1000L) > curTime)
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u041f\u0440\u0438\u0432\u0430\u0442\u043d\u044b\u0439 \u0447\u0430\u0442 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0440\u0430\u0437 \u0432 " + (activeChar.getLevel() >= Config.TELL_DELAY_LEVEL ? "\u0441\u0435\u043a\u0443\u043d\u0434\u0443." : Config.TELL_DELAY_TIME + " \u0441\u0435\u043a \u0434\u043e " + Config.TELL_DELAY_LEVEL + "-\u0433\u043e \u0443\u0440\u043e\u0432\u043d\u044f."));
					else
						activeChar.sendMessage("Tell chat is allowed once per " + (activeChar.getLevel() >= Config.TELL_DELAY_LEVEL ? "1 second." : Config.TELL_DELAY_TIME + " seconds before " + Config.TELL_DELAY_LEVEL + " level."));
					return;
				}
				activeChar.addProperty("Say2.TellChatLaunched", curTime);
				if(!receiver.getMessageRefusal())
				{
					if(noSpam || activeChar.isSameHWID(receiver.getHWID()))
						receiver.sendPacket(cs);
					cs = new Say2(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text);
					activeChar.sendPacket(cs);
				}
				else
					activeChar.sendPacket(new SystemMessage(176));
				break;
			}
			case SHOUT:
			{
				if(Config.NO_TS_JAILED && activeChar.getVar("jailed") != null)
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u041a\u0440\u0438\u043a \u0438 \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u0432 \u0447\u0430\u0442\u0435 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b, \u043f\u043e\u043a\u0430 \u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u0442\u044e\u0440\u044c\u043c\u0435.");
					else
						activeChar.sendMessage("Shout and trade chatting can not be used in jail.");
					return;
				}
				if(activeChar.getLevel() < Config.SHOUT_CHAT_MIN_LVL)
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u041a\u0440\u0438\u043a \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u0441 " + Config.SHOUT_CHAT_MIN_LVL + "-\u0433\u043e \u0443\u0440\u043e\u0432\u043d\u044f.");
					else
						activeChar.sendMessage("Shout chat is allowed from " + Config.SHOUT_CHAT_MIN_LVL + " level only.");
					return;
				}
				if(activeChar.isCursedWeaponEquipped())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u041a\u0440\u0438\u043a \u0438 \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u0432 \u0447\u0430\u0442\u0435 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b, \u043f\u043e\u043a\u0430 \u0443 \u0412\u0430\u0441 \u0435\u0441\u0442\u044c \u043f\u0440\u043e\u043a\u043b\u044f\u0442\u043e\u0435 \u043e\u0440\u0443\u0436\u0438\u0435.");
					else
						activeChar.sendMessage("Shout and trade chatting can not be used while possessing a cursed weapon.");
					return;
				}
				if(activeChar.inObserverMode())
				{
					activeChar.sendPacket(new SystemMessage(932));
					return;
				}
				final Long lastShoutTime = (Long) activeChar.getProperty("Say2.ShoutChatLaunched");
				if(lastShoutTime != null && lastShoutTime + Config.SHOUT_CHAT_DELAY * 1000L > curTime)
				{
					activeChar.sendMessage("Shout chat is allowed once per " + Config.SHOUT_CHAT_DELAY + " seconds.");
					return;
				}
				activeChar.addProperty("Say2.ShoutChatLaunched", curTime);
				if(activeChar.getLevel() >= Config.GLOBAL_CHAT || activeChar.isGM())
				{
					for(final Player player : GameObjectsStorage.getPlayers())
					{
						if(!player.isBlockAll() && player != activeChar && (noSpam || activeChar.isSameHWID(player.getHWID())))
							player.sendPacket(cs);
					}
				}
				else if(Config.SHOUT_CHAT_MODE == 1)
				{
					for(final Player player : World.getAroundPlayers(activeChar, Config.CHAT_RANGE_FIRST_MODE, 5000))
					{
						if(!player.isBlockAll() && player != activeChar && (noSpam || activeChar.isSameHWID(player.getHWID())))
							player.sendPacket(cs);
					}
				}
				else
				{
					for(final Player player : GameObjectsStorage.getPlayers())
					{
						if(!player.isBlockAll() && player != activeChar && MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()) == mapregion && (noSpam || activeChar.isSameHWID(player.getHWID())))
							player.sendPacket(cs);
					}
				}
				activeChar.sendPacket(cs);
				break;
			}
			case TRADE:
			{
				if(Config.NO_TS_JAILED && activeChar.getVar("jailed") != null)
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u041a\u0440\u0438\u043a \u0438 \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u0432 \u0447\u0430\u0442\u0435 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b, \u043f\u043e\u043a\u0430 \u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u0442\u044e\u0440\u044c\u043c\u0435.");
					else
						activeChar.sendMessage("Shout and trade chatting can not be used in jail.");
					return;
				}
				if(activeChar.getLevel() < Config.TRADE_CHAT_MIN_LVL)
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u0422\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u0432 \u0447\u0430\u0442\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0441 " + Config.TRADE_CHAT_MIN_LVL + "-\u0433\u043e \u0443\u0440\u043e\u0432\u043d\u044f.");
					else
						activeChar.sendMessage("Trade chat is allowed from " + Config.TRADE_CHAT_MIN_LVL + " level only.");
					return;
				}
				if(activeChar.isCursedWeaponEquipped())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u041a\u0440\u0438\u043a \u0438 \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u0432 \u0447\u0430\u0442\u0435 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b, \u043f\u043e\u043a\u0430 \u0443 \u0412\u0430\u0441 \u0435\u0441\u0442\u044c \u043f\u0440\u043e\u043a\u043b\u044f\u0442\u043e\u0435 \u043e\u0440\u0443\u0436\u0438\u0435.");
					else
						activeChar.sendMessage("Shout and trade chatting can not be used while possessing a cursed weapon.");
					return;
				}
				if(activeChar.inObserverMode())
				{
					activeChar.sendPacket(new SystemMessage(932));
					return;
				}
				final Long lastTradeTime = (Long) activeChar.getProperty("Say2.TradeChatLaunched");
				if(lastTradeTime != null && lastTradeTime + Config.TRADE_CHAT_DELAY * 1000L > curTime)
				{
					activeChar.sendMessage("Trade chat is allowed once per " + Config.TRADE_CHAT_DELAY + " seconds.");
					return;
				}
				activeChar.addProperty("Say2.TradeChatLaunched", curTime);
				if(activeChar.getLevel() >= Config.GLOBAL_TRADE_CHAT || activeChar.isGM())
				{
					for(final Player player2 : GameObjectsStorage.getPlayers())
					{
						if(!player2.isBlockAll() && player2 != activeChar && (noSpam || activeChar.isSameHWID(player2.getHWID())))
							player2.sendPacket(cs);
					}
				}
				else if(Config.TRADE_CHAT_MODE == 1)
				{
					for(final Player player2 : World.getAroundPlayers(activeChar, Config.CHAT_RANGE_FIRST_MODE, 5000))
					{
						if(!player2.isBlockAll() && player2 != activeChar && (noSpam || activeChar.isSameHWID(player2.getHWID())))
							player2.sendPacket(cs);
					}
				}
				else
				{
					for(final Player player2 : GameObjectsStorage.getPlayers())
					{
						if(!player2.isBlockAll() && player2 != activeChar && MapRegionTable.getInstance().getMapRegion(player2.getX(), player2.getY()) == mapregion && (noSpam || activeChar.isSameHWID(player2.getHWID())))
							player2.sendPacket(cs);
					}
				}
				activeChar.sendPacket(cs);
				break;
			}
			case ALL:
			{
				if(activeChar.getLevel() < Config.ALL_CHAT_MIN_LVL)
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u0411\u0435\u043b\u044b\u0439 \u0447\u0430\u0442 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u0441 " + Config.ALL_CHAT_MIN_LVL + "-\u0433\u043e \u0443\u0440\u043e\u0432\u043d\u044f.");
					else
						activeChar.sendMessage("White chat is allowed from " + Config.ALL_CHAT_MIN_LVL + " level only.");
					return;
				}
				if(activeChar.inObserverMode() && activeChar.getObservNeighbor() != null)
				{
					final List<Player> result = new ArrayList<Player>(50);
					for(final WorldRegion neighbor : activeChar.getObservNeighbor().getNeighbors())
						neighbor.getPlayersList(result, activeChar.getObjectId(), activeChar.getReflectionId(), activeChar.getX(), activeChar.getY(), activeChar.getZ(), Config.ALL_CHAT_RANGE * Config.ALL_CHAT_RANGE, 1000);
					for(final Player player3 : result)
					{
						if(!player3.isBlockAll() && player3 != activeChar && (noSpam || activeChar.isSameHWID(player3.getHWID())))
						{
							cs.setCharName(activeChar.getVisibleName(player3));
							player3.sendPacket(cs);
						}
					}
				}
				else
				{
					for(final Player player2 : World.getAroundPlayers(activeChar, Config.ALL_CHAT_RANGE, 1000))
					{
						if(!player2.isBlockAll() && player2 != activeChar && (noSpam || activeChar.isSameHWID(player2.getHWID())))
						{
							cs.setCharName(activeChar.getVisibleName(player2));
							player2.sendPacket(cs);
						}
					}
				}
				cs.setCharName(activeChar.getVisibleName(activeChar));
				activeChar.sendPacket(cs);
				break;
			}
			case CLAN:
			{
				if(activeChar.getClan() != null)
				{
					activeChar.getClan().broadcastToOnlineMembers(cs);
					break;
				}
				activeChar.sendActionFailed();
				break;
			}
			case ALLIANCE:
			{
				if(activeChar.getClan() != null && activeChar.getClan().getAlliance() != null)
				{
					activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
					break;
				}
				activeChar.sendActionFailed();
				break;
			}
			case PARTY:
			{
				if(activeChar.isInParty())
				{
					activeChar.getParty().broadCast(cs);
					break;
				}
				activeChar.sendActionFailed();
				break;
			}
			case PARTY_ROOM:
			{
				final PartyRoom r = activeChar.getPartyRoom();
				if(r != null)
				{
					r.broadCast(cs);
					break;
				}
				break;
			}
			case COMMANDCHANNEL_ALL:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(new SystemMessage(1617));
					return;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
				{
					activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs);
					break;
				}
				activeChar.sendPacket(new SystemMessage(1603));
				break;
			}
			case COMMANDCHANNEL_COMMANDER:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(new SystemMessage(1617));
					return;
				}
				if(activeChar.getParty().isLeader(activeChar))
				{
					activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
					break;
				}
				activeChar.sendPacket(new SystemMessage(1602));
				break;
			}
			case HERO_VOICE:
			{
				if(!activeChar.isHero() && !activeChar.getPlayerAccess().CanAnnounce)
					break;
				if(activeChar.getVar("jailed") != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "\u0413\u0435\u0440\u043e\u0439\u0441\u043a\u0438\u0439 \u0447\u0430\u0442 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d, \u043f\u043e\u043a\u0430 \u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u0442\u044e\u0440\u044c\u043c\u0435." : "Hero chat can not be used in jail.");
					return;
				}
				if(!activeChar.getPlayerAccess().CanAnnounce)
				{
					final Long lastTime = (Long) activeChar.getProperty("Say2.HeroChatLaunched");
					if(lastTime != null && lastTime + Config.HERO_CHAT_DELAY * 1000L > curTime)
					{
						activeChar.sendMessage("Hero chat is allowed once per " + Config.HERO_CHAT_DELAY + " seconds.");
						return;
					}
					activeChar.addProperty("Say2.HeroChatLaunched", curTime);
				}
				for(final Player player3 : GameObjectsStorage.getPlayers())
				{
					if(!player3.isBlockAll() && player3 != activeChar && (noSpam || activeChar.isSameHWID(player3.getHWID())))
						player3.sendPacket(cs);
				}
				activeChar.sendPacket(cs);
				break;
			}
			case PETITION_PLAYER:
			case PETITION_GM:
			{
				if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage(745));
					return;
				}
				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			}
			default:
			{
				_log.warn("Player " + activeChar.toString() + " used unknown chat type: " + _type);
				break;
			}
		}
		Log.LogChat(_type.toString(), activeChar.getName(), _target, _text);
	}
}
