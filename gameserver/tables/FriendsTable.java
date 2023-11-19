package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExMailArrived;
import l2s.gameserver.network.l2.s2c.Friend;
import l2s.gameserver.network.l2.s2c.FriendAddRequest;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class FriendsTable
{
	private static final Logger _log;
	private static FriendsTable _instance;
	private HashMap<Integer, List<Integer>> _friends;

	public static synchronized FriendsTable getInstance()
	{
		if(FriendsTable._instance == null)
			FriendsTable._instance = new FriendsTable();
		return FriendsTable._instance;
	}

	private FriendsTable()
	{
		_friends = new HashMap<Integer, List<Integer>>();
		RestoreFriendsData();
	}

	private void RestoreFriendsData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet friendsdata = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_id, friend_id FROM character_friends");
			friendsdata = statement.executeQuery();
			int i = 0;
			while(friendsdata.next())
			{
				add(friendsdata.getInt("char_id"), friendsdata.getInt("friend_id"));
				++i;
			}
			if(i > 0)
				FriendsTable._log.info("FriendsTable: Loaded " + i + " friends.");
		}
		catch(Exception e)
		{
			FriendsTable._log.error("Error while loading friends table!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, friendsdata);
		}
	}

	private void add(final int char_id, final int friend_id)
	{
		List<Integer> friends = _friends.get(char_id);
		if(friends == null)
		{
			friends = new ArrayList<Integer>(1);
			_friends.put(char_id, friends);
		}
		friends.add(friend_id);
	}

	public void addFriend(final Player player1, final Player player2)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_friends (char_id,friend_id) VALUES(?,?)");
			statement.setInt(1, player1.getObjectId());
			statement.setInt(2, player2.getObjectId());
			statement.execute();
			DbUtils.closeQuietly(statement);
			statement = con.prepareStatement("REPLACE INTO character_friends (char_id,friend_id) VALUES(?,?)");
			statement.setInt(1, player2.getObjectId());
			statement.setInt(2, player1.getObjectId());
			statement.execute();
			add(player1.getObjectId(), player2.getObjectId());
			add(player2.getObjectId(), player1.getObjectId());
			player1.sendPacket(Msg.YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND, new SystemMessage(132).addString(player2.getName()), new Friend(player2, true));
			player2.sendPacket(new SystemMessage(479).addString(player1.getName()), new Friend(player1, true));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean TryFriendDelete(final Player activeChar, String delFriend)
	{
		if(activeChar == null || delFriend == null || delFriend.isEmpty())
			return false;
		delFriend = delFriend.trim();
		final Player friendChar = World.getPlayer(delFriend);
		if(friendChar != null)
			delFriend = friendChar.getName();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, delFriend);
			rset = statement.executeQuery();
			if(!rset.next())
			{
				System.out.println("FriendsTable: not found char to delete: " + delFriend);
				activeChar.sendPacket(new SystemMessage(171).addString(delFriend));
				return false;
			}
			final int friendId = rset.getInt("obj_Id");
			if(!checkIsFriends(activeChar.getObjectId(), friendId))
			{
				System.out.println("FriendsTable: not in friend list: " + activeChar.getObjectId() + ", " + delFriend);
				activeChar.sendPacket(new SystemMessage(171).addString(delFriend));
				return false;
			}
			DbUtils.closeQuietly(statement, rset);
			rset = null;
			statement = con.prepareStatement("DELETE FROM character_friends WHERE (char_id=? AND friend_id=?) OR (char_id=? AND friend_id=?)");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, friendId);
			statement.setInt(3, friendId);
			statement.setInt(4, activeChar.getObjectId());
			statement.execute();
			List<Integer> friends = _friends.get(activeChar.getObjectId());
			if(friends != null)
				friends.remove(new Integer(friendId));
			friends = _friends.get(friendId);
			if(friends != null)
				friends.remove(new Integer(activeChar.getObjectId()));
			activeChar.sendPacket(new SystemMessage(133).addString(delFriend), new Friend(delFriend, false, friendChar != null, friendId));
			if(friendChar != null)
				friendChar.sendPacket(new SystemMessage(481).addString(activeChar.getName()), new Friend(activeChar, false));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return true;
	}

	public boolean TryFriendInvite(final Player activeChar, final String addFriend)
	{
		if(activeChar == null || addFriend == null || addFriend.isEmpty())
			return false;
		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY);
			return false;
		}
		if(activeChar.getName().equalsIgnoreCase(addFriend))
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST);
			return false;
		}
		final Player friendChar = World.getPlayer(addFriend);
		if(friendChar == null)
		{
			activeChar.sendPacket(Msg.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
			return false;
		}
		if(friendChar.isBlockAll() || friendChar.isInBlockList(activeChar) || friendChar.getMessageRefusal())
		{
			activeChar.sendPacket(Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE);
			return false;
		}
		if(friendChar.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(153).addName(friendChar));
			return false;
		}
		if(getInstance().checkIsFriends(activeChar.getObjectId(), activeChar.getObjectId()))
		{
			activeChar.sendPacket(new SystemMessage(167).addString(friendChar.getName()));
			return false;
		}
		new Transaction(Transaction.TransactionType.FRIEND, activeChar, friendChar, 10000L);
		friendChar.sendPacket(new SystemMessage(168).addString(activeChar.getName()), new FriendAddRequest(activeChar.getName()));
		return true;
	}

	public List<Integer> getFriendsList(final int char_id)
	{
		List<Integer> friends = _friends.get(char_id);
		if(friends == null)
			friends = new ArrayList<Integer>(0);
		return friends;
	}

	public boolean checkIsFriends(final int char_id, final int friend_id)
	{
		for(final Integer obj_id : getFriendsList(char_id))
			if(obj_id != null && obj_id.equals(friend_id))
				return true;
		for(final Integer obj_id : getFriendsList(friend_id))
			if(obj_id != null && obj_id.equals(char_id))
			{
				System.out.println("FriendsTable: corrupted friends table! " + char_id + "," + friend_id);
				return true;
			}
		return false;
	}

	public static void checkMail(final Player player)
	{
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT id FROM `z_bbs_mail` WHERE `to` = ? AND `read` = ? LIMIT 1");
			st.setInt(1, player.getObjectId());
			st.setInt(2, 0);
			rs = st.executeQuery();
			if(rs.next())
			{
				player.sendPacket(new ExMailArrived());
				player.sendMessage(new CustomMessage("l2s.NewMail"));
			}
		}
		catch(Exception e)
		{
			FriendsTable._log.error("EnterWorld: checkMail() error: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, st, rs);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(FriendsTable.class);
	}
}
