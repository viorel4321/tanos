package l2s.gameserver.network.l2.s2c;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;

public class FriendList extends L2GameServerPacket
{
	private static Logger _log;
	private List<FriendInfo> friends;
	private Player _cha;
	private boolean _message;
	private boolean _packet;

	public FriendList(final Player cha)
	{
		friends = new ArrayList<FriendInfo>();
		_message = false;
		_packet = false;
		_cha = cha;
		_message = true;
		_packet = false;
		common();
	}

	public FriendList(final Player cha, final boolean sendMessage)
	{
		friends = new ArrayList<FriendInfo>();
		_message = false;
		_packet = false;
		_cha = cha;
		_message = sendMessage;
		_packet = true;
		common();
	}

	private void common()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT friend_id, char_name FROM character_friends LEFT JOIN characters ON ( character_friends.friend_id = characters.obj_Id ) WHERE char_id=?");
			statement.setInt(1, _cha.getObjectId());
			rset = statement.executeQuery();
			if(_message)
				_cha.sendPacket(new SystemMessage(487));
			while(rset.next())
			{
				final String name = rset.getString("char_name");
				if(name == null)
					FriendList._log.warn("Can not find friend name for char: " + _cha.toString());
				else
				{
					final FriendInfo friendinfo = new FriendInfo(name);
					final Player friend = World.getPlayer(name);
					if(friend == null)
					{
						if(_message)
							_cha.sendPacket(new SystemMessage(489).addString(friendinfo.name));
					}
					else
					{
						if(_message)
							_cha.sendPacket(new SystemMessage(488).addString(friendinfo.name));
						friendinfo.id = friend.getObjectId();
					}
					friends.add(friendinfo);
				}
			}
			if(_message)
				_cha.sendPacket(new SystemMessage(490));
		}
		catch(Exception e)
		{
			FriendList._log.error("Error in friendlist ", e);
			_packet = false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(!_packet)
			return;
		writeC(250);
		writeD(friends.size());
		for(final FriendInfo friend : friends)
		{
			writeD(0);
			writeS(friend.name);
			writeD(friend.id > 0 ? 1 : 0);
			writeD(friend.id);
		}
	}

	static
	{
		FriendList._log = LoggerFactory.getLogger(FriendList.class);
	}

	private class FriendInfo
	{
		public final String name;
		public int id;

		public FriendInfo(final String _name)
		{
			id = 0;
			name = _name;
		}
	}
}
