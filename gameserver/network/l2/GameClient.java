package l2s.gameserver.network.l2;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import l2s.commons.ban.BanBindType;
import l2s.gameserver.instancemanager.GameBanManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.net.nio.impl.MMOClient;
import l2s.commons.net.nio.impl.MMOConnection;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.AccountBonusDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.CharSelectInfo;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.KickPlayerInGameTask;
import l2s.gameserver.network.authcomm.SessionKey;
import l2s.gameserver.network.authcomm.gs2as.PlayerLogout;
import l2s.gameserver.network.l2.PacketFloodProtector.ActionType;
import l2s.gameserver.network.l2.PacketFloodProtector.PacketData;
import l2s.gameserver.network.l2.s2c.ActionFail;
import l2s.gameserver.network.l2.s2c.CharSelected;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.utils.AutoBan;
import l2s.gameserver.utils.Language;

public final class GameClient extends MMOClient<MMOConnection<GameClient>>
{
	protected static Logger _log;
	private static final String NO_IP = "?.?.?.?";
	public GameCrypt _crypt;
	private float _bonus;
	private int _bonusExpire;
	public GameClientState _state;
	private boolean _protocol;
	public IExReader _reader;
	private String _hwid;
	private String _allowedHwid;
	private String _allowedHwidSecond;
	private String _loginName;
	private Player _activeChar;
	private SessionKey _sessionId;
	private String _ip;
	private int revision;
	private CharSelectInfo[] _csi;
	private Map<Integer, Long> _packets;
	private int _failedPackets;
	private int _unknownPackets;
	private Language _language = Config.DEFAULT_LANG;

	public GameClient(final MMOConnection<GameClient> con)
	{
		super(con);
		_crypt = null;
		_hwid = null;
		_allowedHwid = "";
		_allowedHwidSecond = "";
		_ip = "?.?.?.?";
		revision = 0;
		_csi = new CharSelectInfo[7];
		_failedPackets = 0;
		_unknownPackets = 0;
		_state = GameClientState.CONNECTED;
		_crypt = new GameCrypt();
		_ip = con.getSocket().getInetAddress().getHostAddress();
		_packets = new HashMap<Integer, Long>();
	}

	@Override
	protected void onDisconnection()
	{
		setState(GameClientState.DISCONNECTED);
		final Player player = getActiveChar();
		setActiveChar(null);
		if(player != null)
		{
			player.setNetConnection(null);
			player.logout();
		}
		if(getSessionKey() != null)
			if(isAuthed())
			{
				AuthServerCommunication.getInstance().removeAuthedClient(getLogin());
				AuthServerCommunication.getInstance().sendPacket(new PlayerLogout(getLogin()));
			}
			else
				AuthServerCommunication.getInstance().removeWaitingClient(getLogin());
	}

	@Override
	protected void onForcedDisconnection()
	{}

	public void markDeleteCharByObjId(final int charId, final boolean delete)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
			statement.setLong(1, delete ? (long) (int) (System.currentTimeMillis() / 1000L) : 0L);
			statement.setInt(2, charId);
			statement.execute();
		}
		catch(Exception e)
		{
			GameClient._log.error("markDeleteCharByObjId(int,boolean): data error on update deletime char: " + charId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int getObjectIdByIndex(final int charslot)
	{
		if(charslot < 0 || charslot >= _csi.length)
		{
			GameClient._log.warn(getLogin() + " tried to modify Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		final CharSelectInfo p = _csi[charslot];
		return p == null ? 0 : p.getObjectId();
	}

	public Player getActiveChar()
	{
		return _activeChar;
	}

	public SessionKey getSessionKey()
	{
		return _sessionId;
	}

	public String getLogin()
	{
		return _loginName;
	}

	public void setLoginName(final String loginName)
	{
		_loginName = loginName;
	}

	public void setActiveChar(final Player player)
	{
		_activeChar = player;
		if(player != null)
		{
			player.setNetConnection(this);
			if(_reader != null)
				_reader.checkChar(_activeChar);
		}
	}

	public void setSessionId(final SessionKey sessionKey)
	{
		_sessionId = sessionKey;
	}

	public void setCharSelection(final CharSelectInfo[] chars)
	{
		_csi = chars;
	}

	public int getRevision()
	{
		return revision;
	}

	public void setRevision(int revision)
	{
		revision = revision;
	}

	public void playerSelected(final int index)
	{
		final int objId = getObjectIdByIndex(index);
		if(objId <= 0 || getActiveChar() != null)
		{
			sendPacket(ActionFail.STATIC);
			return;
		}
		if(!isAuthed())
		{
			GameClient._log.info("Not authed client from IP: " + getIpAddr() + " Acc: " + getLogin());
			close(Msg.ServerClose);
			return;
		}
		if(GameBanManager.getInstance().isBanned(BanBindType.PLAYER, objId) || AutoBan.isBanned(objId))
		{
			GameClient._log.info("Try enter banned char[" + objId + "] from IP: " + getIpAddr() + " Acc: " + getLogin());
			close(Msg.ServerClose);
			return;
		}
		if(Config.SERVICES_LOCK_CHAR_HWID && charLockHWID(objId))
		{
			close(Msg.LeaveWorld);
			return;
		}
		final CharSelectInfo info = _csi[index];
		if(info == null)
			return;
		Player character = null;
		final CharSelectInfo[] array = getCharacters();
		for(int i = 0; i < array.length; ++i)
		{
			final CharSelectInfo p = array[i];
			final Player player = p != null ? GameObjectsStorage.getPlayer(p.getObjectId()) : null;
			if(player != null)
				if(player.isInOfflineMode() || player.isLogoutStarted())
				{
					if(index == i)
					{
						player.setOfflineMode(false);
						player.kick(false);
					}
				}
				else
				{
					player.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
					if(index == i)
					{
						character = player;
						final GameClient oldClient = player.getNetConnection();
						if(oldClient != null)
						{
							oldClient.setActiveChar(null);
							oldClient.close(Msg.ServerClose);
						}
					}
					else
						player.kick(false);
				}
		}
		final Player selectedPlayer = character == null ? Player.restore(objId) : character;
		if(selectedPlayer == null)
		{
			sendPacket(ActionFail.STATIC);
			return;
		}
		if(Config.SERVICES_RATE_TYPE == 3)
		{
			final double[] bonuses = AccountBonusDAO.getInstance().select(String.valueOf(objId));
			setBonus((float) bonuses[0]);
			setBonusExpire((int) bonuses[1]);
		}
		if(selectedPlayer.getAccessLevel() < 0)
			selectedPlayer.setAccessLevel(0);
		selectedPlayer.setOnlineStatus(true);
		setActiveChar(selectedPlayer);
		selectedPlayer.checkKey();
		selectedPlayer.restoreBonus();
		selectedPlayer.storeLastIpAndHWID(getIpAddr(), getHWID());
		setState(GameClientState.ENTER_GAME);
		sendPacket(new CharSelected(selectedPlayer, getSessionKey().playOkID1));
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	@Override
	public boolean decrypt(final ByteBuffer buf, final int size)
	{
		final boolean ret = _crypt.decrypt(buf.array(), buf.position(), size);
		return ret;
	}

	public void sendPacket(final L2GameServerPacket gsp)
	{
		if(isConnected())
			getConnection().sendPacket(gsp);
	}

	public void sendPacket(final L2GameServerPacket... gsp)
	{
		if(isConnected())
			getConnection().sendPacket(gsp);
	}

	public void close(final L2GameServerPacket gsp)
	{
		if(isConnected())
			getConnection().close(gsp);
	}

	public String getIpAddr()
	{
		return _ip;
	}

	public byte[] enableCrypt()
	{
		final byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}

	public float getBonus()
	{
		return _bonus;
	}

	public int getBonusExpire()
	{
		return _bonusExpire;
	}

	public void setBonus(final float bonus)
	{
		_bonus = bonus;
	}

	public void setBonusExpire(final int bonusExpire)
	{
		_bonusExpire = bonusExpire;
	}

	public Language getLanguage()
	{
		return _language;
	}

	public void setLanguage(Language language)
	{
		_language = language;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public void setState(final GameClientState state)
	{
		_state = state;
	}

	public boolean isProtocolOk()
	{
		return _protocol;
	}

	public void setProtocolOk(final boolean b)
	{
		_protocol = b;
	}

	public void onPacketReadFail()
	{
		if(_failedPackets++ >= Config.MAX_FAILED_PACKETS)
			if(_activeChar != null)
				_activeChar.kick(true);
			else
				closeNow(true);
	}

	public void onUnknownPacket()
	{
		if(_unknownPackets++ >= Config.MAX_UNKNOWN_PACKETS)
			if(_activeChar != null)
				_activeChar.kick(true);
			else
				closeNow(true);
	}

	public PacketFloodProtector.ActionType checkPacket(final int packetId)
	{
		final PacketFloodProtector.PacketData pd = PacketFloodProtector.getInstance().getDataByPacketId(packetId);
		if(pd == null)
			return PacketFloodProtector.ActionType.none;
		if(!_packets.containsKey(packetId))
		{
			_packets.put(packetId, System.currentTimeMillis());
			return PacketFloodProtector.ActionType.none;
		}
		if(pd.getDelay() > System.currentTimeMillis() - _packets.get(packetId))
		{
			_packets.put(packetId, System.currentTimeMillis());
			return pd.getAction();
		}
		_packets.put(packetId, System.currentTimeMillis());
		return PacketFloodProtector.ActionType.none;
	}

	public CharSelectInfo[] getCharacters()
	{
		return _csi;
	}

	@Override
	public String toString()
	{
		return _state + " IP: " + getIpAddr() + (_loginName == null ? "" : " Account: " + _loginName) + (_activeChar == null ? "" : " Player: " + _activeChar);
	}

	public boolean isHWIDBanned()
	{
		boolean result = false;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM hwid_bans WHERE HWID=? LIMIT 1");
			statement.setString(1, _hwid);
			rs = statement.executeQuery();
			if(rs.next())
			{
				final long time = rs.getLong("end_date");
				if(time <= 0L || time > System.currentTimeMillis())
					result = true;
				else
				{
					DbUtils.closeQuietly(statement, rs);
					statement = con.prepareStatement("DELETE FROM hwid_bans WHERE HWID=?");
					statement.setString(1, _hwid);
					statement.execute();
				}
			}
		}
		catch(Exception ex)
		{}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
		return result;
	}

	public boolean accLockHWID()
	{
		final boolean empty = _allowedHwid.isEmpty() && _allowedHwidSecond.isEmpty();
		return !empty && !_allowedHwid.equals(_hwid) && !_allowedHwidSecond.equals(_hwid);
	}

	public boolean charLockHWID(final int objId)
	{
		int i = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT Lock1,Lock2 FROM hwid_locks WHERE obj_Id=? LIMIT 1");
			statement.setInt(1, objId);
			rs = statement.executeQuery();
			if(rs.next())
			{
				String h = rs.getString(1);
				if(!h.isEmpty())
					i = h.equals(_hwid) ? 2 : 1;
				if(i != 2)
				{
					h = rs.getString(2);
					if(!h.isEmpty())
						i = h.equals(_hwid) ? 0 : 1;
				}
			}
		}
		catch(Exception ex)
		{}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
		return i == 1;
	}

	public void kick()
	{
		setAuthed(false);
		if(_activeChar != null)
		{
			sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
			ThreadPoolManager.getInstance().schedule(new KickPlayerInGameTask(this), 500L);
		}
		else
			close(Msg.ServerClose);
	}

	public String getHWID()
	{
		return _hwid;
	}

	public void setHWID(final String h)
	{
		_hwid = h;
	}

	public String getAllowedHwid()
	{
		return _allowedHwid;
	}

	public void setAllowedHwid(final String allowedHwid)
	{
		_allowedHwid = allowedHwid;
	}

	public String getAllowedHwidSecond()
	{
		return _allowedHwidSecond;
	}

	public void setAllowedHwidSecond(final String allowedHwid)
	{
		_allowedHwidSecond = allowedHwid;
	}

	static
	{
		GameClient._log = LoggerFactory.getLogger(GameClient.class);
	}

	public enum GameClientState
	{
		CONNECTED,
		AUTHED,
		ENTER_GAME,
		IN_GAME,
		DISCONNECTED;
	}

	public interface IExReader
	{
		void checkChar(final Player p0);

		int read(final ByteBuffer p0);
	}
}
