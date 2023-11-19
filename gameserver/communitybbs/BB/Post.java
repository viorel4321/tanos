package l2s.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.communitybbs.Manager.PostBBSManager;
import l2s.gameserver.database.DatabaseFactory;

public class Post
{
	private static Logger _log;
	private List<CPost> _post;

	public Post(final String _PostOwner, final int _PostOwnerID, final long date, final int tid, final int _PostForumID, final String txt)
	{
		_post = new ArrayList<CPost>();
		final CPost cp = new CPost();
		cp._PostID = 0;
		cp._PostOwner = _PostOwner;
		cp._PostOwnerID = _PostOwnerID;
		cp._PostDate = date;
		cp._PostTopicID = tid;
		cp._PostForumID = _PostForumID;
		cp._PostTxt = txt;
		_post.add(cp);
		insertindb(cp);
	}

	public void insertindb(final CPost cp)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)");
			statement.setInt(1, cp._PostID);
			statement.setString(2, cp._PostOwner);
			statement.setInt(3, cp._PostOwnerID);
			statement.setLong(4, cp._PostDate);
			statement.setInt(5, cp._PostTopicID);
			statement.setInt(6, cp._PostForumID);
			statement.setString(7, cp._PostTxt);
			statement.execute();
		}
		catch(Exception e)
		{
			Post._log.warn("error while saving new Post to db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public Post(final Topic t)
	{
		_post = new ArrayList<CPost>();
		load(t);
	}

	public CPost getCPost(final int id)
	{
		int i = 0;
		for(final CPost cp : _post)
		{
			if(i == id)
				return cp;
			++i;
		}
		return null;
	}

	public void deleteme(final Topic t)
	{
		PostBBSManager.getInstance().delPostByTopic(t);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
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

	private void load(final Topic t)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final CPost cp = new CPost();
				cp._PostID = Integer.parseInt(rset.getString("post_id"));
				cp._PostOwner = rset.getString("post_owner_name");
				cp._PostOwnerID = Integer.parseInt(rset.getString("post_ownerid"));
				cp._PostDate = Long.parseLong(rset.getString("post_date"));
				cp._PostTopicID = Integer.parseInt(rset.getString("post_topic_id"));
				cp._PostForumID = Integer.parseInt(rset.getString("post_forum_id"));
				cp._PostTxt = rset.getString("post_txt");
				_post.add(cp);
			}
		}
		catch(Exception e)
		{
			Post._log.warn("data error on Post " + t.getForumID() + "/" + t.getID() + " : " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void updatetxt(final int i)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			final CPost cp = getCPost(i);
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?");
			statement.setString(1, cp._PostTxt);
			statement.setInt(2, cp._PostID);
			statement.setInt(3, cp._PostTopicID);
			statement.setInt(4, cp._PostForumID);
			statement.execute();
		}
		catch(Exception e)
		{
			Post._log.warn("error while saving new Post to db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	static
	{
		Post._log = LoggerFactory.getLogger(Post.class);
	}

	public class CPost
	{
		public int _PostID;
		public String _PostOwner;
		public int _PostOwnerID;
		public long _PostDate;
		public int _PostTopicID;
		public int _PostForumID;
		public String _PostTxt;
	}
}
