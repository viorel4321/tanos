package l2s.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2s.gameserver.communitybbs.Manager.TopicBBSManager;
import l2s.gameserver.database.DatabaseFactory;

public class Forum
{
	public static final int ROOT = 0;
	public static final int NORMAL = 1;
	public static final int CLAN = 2;
	public static final int MEMO = 3;
	public static final int MAIL = 4;
	public static final int INVISIBLE = 0;
	public static final int ALL = 1;
	public static final int CLANMEMBERONLY = 2;
	public static final int OWNERONLY = 3;
	private static Logger _log;
	private List<Forum> _children;
	private Map<Integer, Topic> _topic;
	private int _ForumId;
	private String _ForumName;
	private int _ForumParent;
	private int _ForumType;
	private int _ForumPost;
	private int _ForumPerm;
	private Forum _FParent;
	private int _OwnerID;
	private boolean loaded;

	public Forum(final int Forumid, final Forum FParent)
	{
		loaded = false;
		_ForumId = Forumid;
		_FParent = FParent;
		_children = new ArrayList<Forum>();
		_topic = new HashMap<Integer, Topic>();
		ForumsBBSManager.getInstance().addForum(this);
	}

	public Forum(final String name, final Forum parent, final int type, final int perm, final int OwnerID)
	{
		loaded = false;
		_ForumName = name;
		_ForumId = ForumsBBSManager.getInstance().GetANewID();
		_ForumParent = parent.getID();
		_ForumType = type;
		_ForumPost = 0;
		_ForumPerm = perm;
		_FParent = parent;
		_OwnerID = OwnerID;
		_children = new ArrayList<Forum>();
		_topic = new HashMap<Integer, Topic>();
		parent._children.add(this);
		ForumsBBSManager.getInstance().addForum(this);
		loaded = true;
	}

	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			try
			{
				statement = con.prepareStatement("SELECT * FROM forums WHERE forum_id=?");
				statement.setInt(1, _ForumId);
				rset = statement.executeQuery();
				if(rset.next())
				{
					_ForumName = rset.getString("forum_name");
					_ForumParent = Integer.parseInt(rset.getString("forum_parent"));
					_ForumPost = Integer.parseInt(rset.getString("forum_post"));
					_ForumType = Integer.parseInt(rset.getString("forum_type"));
					_ForumPerm = Integer.parseInt(rset.getString("forum_perm"));
					_OwnerID = Integer.parseInt(rset.getString("forum_owner_id"));
				}
			}
			catch(Exception e)
			{
				Forum._log.warn("data error on Forum " + _ForumId + " : " + e);
				throw e;
			}
			DbUtils.closeQuietly(statement, rset);
			try
			{
				statement = con.prepareStatement("SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC");
				statement.setInt(1, _ForumId);
				rset = statement.executeQuery();
				while(rset.next())
				{
					final Topic t = new Topic(Topic.ConstructorType.RESTORE, Integer.parseInt(rset.getString("topic_id")), Integer.parseInt(rset.getString("topic_forum_id")), rset.getString("topic_name"), Long.parseLong(rset.getString("topic_date")), rset.getString("topic_ownername"), Integer.parseInt(rset.getString("topic_ownerid")), Integer.parseInt(rset.getString("topic_type")), Integer.parseInt(rset.getString("topic_reply")));
					_topic.put(t.getID(), t);
					if(t.getID() > TopicBBSManager.getInstance().getMaxID(this))
						TopicBBSManager.getInstance().setMaxID(t.getID(), this);
				}
			}
			catch(Exception e)
			{
				Forum._log.warn("data error on Forum " + _ForumId + " : " + e);
				throw e;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private void getChildren()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_parent=?");
			statement.setInt(1, _ForumId);
			rset = statement.executeQuery();
			while(rset.next())
				_children.add(new Forum(Integer.parseInt(rset.getString("forum_id")), this));
		}
		catch(Exception e)
		{
			Forum._log.warn("data error on Forum (children): " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public int getTopicSize()
	{
		if(!loaded)
		{
			load();
			getChildren();
			loaded = true;
		}
		return _topic.size();
	}

	public Topic gettopic(final int j)
	{
		if(!loaded)
		{
			load();
			getChildren();
			loaded = true;
		}
		return _topic.get(j);
	}

	public void addtopic(final Topic t)
	{
		if(!loaded)
		{
			load();
			getChildren();
			loaded = true;
		}
		_topic.put(t.getID(), t);
	}

	public int getID()
	{
		return _ForumId;
	}

	public String getName()
	{
		if(!loaded)
		{
			load();
			getChildren();
			loaded = true;
		}
		return _ForumName;
	}

	public int getType()
	{
		if(!loaded)
		{
			load();
			getChildren();
			loaded = true;
		}
		return _ForumType;
	}

	public Forum GetChildByName(final String name)
	{
		if(!loaded)
		{
			load();
			getChildren();
			loaded = true;
		}
		for(final Forum f : _children)
			if(f.getName().equals(name))
				return f;
		return null;
	}

	public void RmTopicByID(final int id)
	{
		_topic.remove(id);
	}

	public void insertindb()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)");
			statement.setInt(1, _ForumId);
			statement.setString(2, _ForumName);
			statement.setInt(3, _FParent.getID());
			statement.setInt(4, _ForumPost);
			statement.setInt(5, _ForumType);
			statement.setInt(6, _ForumPerm);
			statement.setInt(7, _OwnerID);
			statement.execute();
		}
		catch(Exception e)
		{
			Forum._log.warn("error while saving new Forum to db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void vload()
	{
		if(!loaded)
		{
			load();
			getChildren();
			loaded = true;
		}
	}

	static
	{
		Forum._log = LoggerFactory.getLogger(Forum.class);
	}
}
