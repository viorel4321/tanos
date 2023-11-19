package l2s.gameserver.communitybbs.Manager;

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
import l2s.gameserver.communitybbs.BB.Forum;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;

public class ForumsBBSManager extends BaseBBSManager
{
	private static Logger _log;
	private Map<Integer, Forum> _root;
	private List<Forum> _table;
	private static ForumsBBSManager _Instance;
	private int lastid;

	public static ForumsBBSManager getInstance()
	{
		if(ForumsBBSManager._Instance == null)
			(ForumsBBSManager._Instance = new ForumsBBSManager()).load();
		return ForumsBBSManager._Instance;
	}

	public ForumsBBSManager()
	{
		lastid = 1;
		_root = new HashMap<Integer, Forum>();
		_table = new ArrayList<Forum>();
	}

	public void addForum(final Forum ff)
	{
		_table.add(ff);
		if(ff.getID() > lastid)
			lastid = ff.getID();
	}

	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final Forum f = new Forum(Integer.parseInt(rset.getString("forum_id")), null);
				_root.put(Integer.parseInt(rset.getString("forum_id")), f);
			}
		}
		catch(Exception e)
		{
			ForumsBBSManager._log.warn("data error on Forum (root): " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	@Override
	public void parsecmd(final String command, final Player activeChar)
	{}

	public Forum getForumByName(final String Name)
	{
		for(final Forum f : _table)
			if(f.getName().equals(Name))
				return f;
		return null;
	}

	public Forum CreateNewForum(final String name, final Forum parent, final int type, final int perm, final int oid)
	{
		final Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertindb();
		return forum;
	}

	public int GetANewID()
	{
		return ++lastid;
	}

	public Forum getForumByID(final int idf)
	{
		for(final Forum f : _table)
			if(f.getID() == idf)
				return f;
		return null;
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player activeChar)
	{}

	static
	{
		ForumsBBSManager._log = LoggerFactory.getLogger(ForumsBBSManager.class);
	}
}
