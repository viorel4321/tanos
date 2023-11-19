package l2s.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.communitybbs.Manager.TopicBBSManager;
import l2s.gameserver.database.DatabaseFactory;

public class Topic
{
	private static Logger _log;
	public static final int MORMAL = 0;
	public static final int MEMO = 1;
	private int _ID;
	private int _ForumID;
	private String _TopicName;
	private long _date;
	private String _OwnerName;
	private int _OwnerID;
	private int _type;
	private int _Creply;

	public Topic(final ConstructorType ct, final int id, final int fid, final String name, final long date, final String oname, final int oid, final int type, final int Creply)
	{
		_ID = id;
		_ForumID = fid;
		_TopicName = name;
		_date = date;
		_OwnerName = oname;
		_OwnerID = oid;
		_type = type;
		_Creply = Creply;
		TopicBBSManager.getInstance().addTopic(this);
		if(ct == ConstructorType.CREATE)
			insertindb();
	}

	public void insertindb()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)");
			statement.setInt(1, _ID);
			statement.setInt(2, _ForumID);
			statement.setString(3, _TopicName);
			statement.setLong(4, _date);
			statement.setString(5, _OwnerName);
			statement.setInt(6, _OwnerID);
			statement.setInt(7, _type);
			statement.setInt(8, _Creply);
			statement.execute();
		}
		catch(Exception e)
		{
			Topic._log.warn("error while saving new Topic to db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int getID()
	{
		return _ID;
	}

	public int getForumID()
	{
		return _ForumID;
	}

	public String getName()
	{
		return _TopicName;
	}

	public String getOwnerName()
	{
		return _OwnerName;
	}

	public void deleteme(final Forum f)
	{
		TopicBBSManager.getInstance().delTopic(this);
		f.RmTopicByID(getID());
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?");
			statement.setInt(1, getID());
			statement.setInt(2, f.getID());
			statement.execute();
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

	public long getDate()
	{
		return _date;
	}

	static
	{
		Topic._log = LoggerFactory.getLogger(Topic.class);
	}

	public enum ConstructorType
	{
		RESTORE,
		CREATE;
	}
}
