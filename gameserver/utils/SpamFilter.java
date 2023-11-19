package l2s.gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.Player;

public class SpamFilter
{
	private static final SpamFilter _instance;
	private Map<String, SpamAccount> spams;

	public static SpamFilter getInstance()
	{
		return SpamFilter._instance;
	}

	private SpamFilter()
	{
		spams = new ConcurrentHashMap<String, SpamAccount>();
	}

	public boolean checkSpam(final Player player, final String message, final int type)
	{
		SpamAccount sa = spams.get(player.getAccountName());
		if(sa == null)
		{
			sa = new SpamAccount();
			spams.put(player.getAccountName(), sa);
		}
		return sa.checkSpam(player, message, type);
	}

	public boolean isSpamer(final String account)
	{
		final SpamAccount sa = spams.get(account);
		return sa != null && sa.isSpamer();
	}

	public void setBlockTime(final String account, final long n)
	{
		SpamAccount sa = spams.get(account);
		if(sa == null)
		{
			if(n == 0L)
				return;
			sa = new SpamAccount();
			spams.put(account, sa);
		}
		sa.setBlockTime(n);
	}

	public void clear()
	{
		for(final SpamAccount sa : spams.values())
			sa.setBlockTime(0L);
	}

	public void save()
	{
		for(final String acc : spams.keySet())
		{
			final SpamAccount sa = spams.get(acc);
			if(sa.isSpamer())
				mysql.set("REPLACE INTO `account_spamers` (`account`, `expire`) VALUES ('" + acc + "'," + (sa.getBlockTime() < 0L ? -1L : sa.getBlockTime() / 1000L) + ")");
		}
	}

	public void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `account_spamers`");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final String acc = rset.getString("account");
				final int cn = rset.getInt("expire");
				final SpamAccount sa = new SpamAccount();
				sa.setBlockTime(cn < 0 ? -1L : cn * 1000L);
				spams.put(acc, sa);
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

	static
	{
		_instance = new SpamFilter();
	}
}
