package l2s.gameserver.model.entity.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class OlympiadDatabase
{
	private static class MyComparator implements Comparator<String>
	{
		@Override
		public int compare(final String o1, final String o2)
		{
			return o1.compareTo(o2);
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(OlympiadDatabase.class);
	private static MyComparator comp = new MyComparator();

	public static synchronized void loadNobles()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `olympiad_nobles`");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int classId = rset.getInt("class_id");
				if(classId < 88)
					for(int i = 88; i < 119; ++i)
						if(ClassId.values()[i].getParent().getId() == classId)
						{
							classId = i;
							break;
						}
				final StatsSet statDat = new StatsSet();
				final int charId = rset.getInt("char_id");
				statDat.set("class_id", classId);
				statDat.set("char_name", rset.getString("char_name"));
				statDat.set("olympiad_points", rset.getInt("olympiad_points"));
				statDat.set("olympiad_points_past", rset.getInt("olympiad_points_past"));
				statDat.set("olympiad_points_past_static", rset.getInt("olympiad_points_past_static"));
				statDat.set("competitions_done", rset.getInt("competitions_done"));
				statDat.set("competitions_win", rset.getInt("competitions_win"));
				statDat.set("competitions_loose", rset.getInt("competitions_loose"));
				Olympiad._nobles.put(charId, statDat);
			}
		}
		catch(Exception e)
		{
			OlympiadDatabase._log.error("Olympiad System: Error!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static synchronized void cleanupNobles()
	{
		OlympiadDatabase._log.info("Olympiad: Calculating last period...");
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Olympiad.OLYMPIAD_CALCULATE_LAST_PERIOD);
			statement.execute();
		}
		catch(Exception e)
		{
			OlympiadDatabase._log.error("Olympiad System: Couldn't calculate last period!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		OlympiadDatabase._log.info("Olympiad: Clearing nobles table...");
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Olympiad.OLYMPIAD_CLEANUP_NOBLES);
			statement.execute();
		}
		catch(Exception e)
		{
			OlympiadDatabase._log.error("Olympiad System: Couldn't cleanup nobles table!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		for(final Integer nobleId : Olympiad._nobles.keySet())
		{
			final StatsSet nobleInfo = Olympiad._nobles.get(nobleId);
			final int points = nobleInfo.getInteger("olympiad_points");
			final int compDone = nobleInfo.getInteger("competitions_done");
			final int compWin = nobleInfo.getInteger("competitions_win");
			nobleInfo.set("olympiad_points", Config.OLYMPIAD_POINTS_DEFAULT);
			if(compDone >= Config.OLY_COMP_DONE_HERO && compWin >= Config.OLY_COMP_WIN_HERO)
			{
				nobleInfo.set("olympiad_points_past", points);
				nobleInfo.set("olympiad_points_past_static", points);
			}
			else
			{
				nobleInfo.set("olympiad_points_past", 0);
				nobleInfo.set("olympiad_points_past_static", 0);
			}
			nobleInfo.set("competitions_done", 0);
			nobleInfo.set("competitions_win", 0);
			nobleInfo.set("competitions_loose", 0);
		}
	}

	public static List<String> getClassLeaderBoard(final int classId)
	{
		final HashMap<String, Integer> np = new HashMap<String, Integer>();
		final List<String> names = new ArrayList<String>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Config.OLY_RANKING_PAST ? "SELECT `char_name`, `olympiad_points_past_static` FROM `olympiad_nobles` WHERE `class_id` = ? AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC LIMIT 15" : Olympiad.GET_EACH_CLASS_LEADER);
			statement.setInt(1, classId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				names.add(rset.getString("char_name"));
				np.put(rset.getString("char_name"), rset.getInt(Config.OLY_RANKING_PAST ? "olympiad_points_past_static" : "olympiad_points"));
			}
		}
		catch(Exception e)
		{
			OlympiadDatabase._log.error("Olympiad System: Couldn't get class leader from db!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		final List<String> nm = new ArrayList<String>();
		int s = 0;
		int c = 0;
		int p = -1;
		for(final String name : names)
		{
			if(s < 5 && np.get(name) == p)
				++s;
			else
			{
				if(c >= 10)
					break;
				++c;
			}
			p = np.get(name);
			nm.add(name);
		}
		if(Config.OLY_SORT_LIST)
			Collections.sort(nm, OlympiadDatabase.comp);
		return nm;
	}

	public static synchronized void sortHeroesToBe()
	{
		if(Olympiad._period != 1)
			return;
		Olympiad._heroesToBe = new ArrayList<StatsSet>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(final ClassId id : ClassId.values())
				if(id.level() == 3)
				{
					statement = con.prepareStatement(Olympiad.OLYMPIAD_GET_HEROES);
					statement.setInt(1, id.getId());
					rset = statement.executeQuery();
					if(rset.next())
					{
						final StatsSet hero = new StatsSet();
						hero.set("class_id", id.getId());
						hero.set("char_id", rset.getInt("char_id"));
						hero.set("char_name", rset.getString("char_name"));
						Olympiad._heroesToBe.add(hero);
					}
					DbUtils.close(statement, rset);
				}
		}
		catch(Exception e)
		{
			OlympiadDatabase._log.error("Olympiad System: Couldn't sort heroes in db!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static synchronized void saveNobleData(final int nobleId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			final StatsSet nobleInfo = Olympiad._nobles.get(nobleId);
			final int classId = nobleInfo.getInteger("class_id");
			final String charName = nobleInfo.getString("char_name");
			final int points = nobleInfo.getInteger("olympiad_points");
			final int points_past = nobleInfo.getInteger("olympiad_points_past");
			final int points_past_static = nobleInfo.getInteger("olympiad_points_past_static");
			final int compDone = nobleInfo.getInteger("competitions_done");
			final int compWin = nobleInfo.getInteger("competitions_win");
			final int compLoose = nobleInfo.getInteger("competitions_loose");
			statement = con.prepareStatement("REPLACE INTO `olympiad_nobles` (`char_id`, `class_id`, `char_name`, `olympiad_points`, `olympiad_points_past`, `olympiad_points_past_static`, `competitions_done`, `competitions_win`, `competitions_loose`) VALUES (?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, nobleId);
			statement.setInt(2, classId);
			statement.setString(3, charName);
			statement.setInt(4, points);
			statement.setInt(5, points_past);
			statement.setInt(6, points_past_static);
			statement.setInt(7, compDone);
			statement.setInt(8, compWin);
			statement.setInt(9, compLoose);
			statement.execute();
		}
		catch(Exception e)
		{
			OlympiadDatabase._log.error("Olympiad System: Couldn't save noble info in db for player objId: " + nobleId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static synchronized void saveNobleData()
	{
		if(Olympiad._nobles == null)
			return;
		for(final Integer nobleId : Olympiad._nobles.keySet())
			saveNobleData(nobleId);
	}

	public static synchronized void setNewOlympiadEnd()
	{
		Olympiad._olympiadEnd = Config.OLYMIAD_END_PERIOD_TIME.next(System.currentTimeMillis());
		final Calendar nextChange = Calendar.getInstance();
		Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
		Olympiad._isOlympiadEnd = false;
		Announcements.getInstance().announceToAll(new SystemMessage(1639).addNumber(Integer.valueOf(Olympiad._currentCycle)));
	}

	public static void save()
	{
		saveNobleData();
		ServerVariables.set("Olympiad_CurrentCycle", Olympiad._currentCycle);
		ServerVariables.set("Olympiad_Period", Olympiad._period);
		ServerVariables.set("Olympiad_End", Olympiad._olympiadEnd);
		ServerVariables.set("Olympiad_ValdationEnd", Olympiad._validationEnd);
		ServerVariables.set("Olympiad_NextWeeklyChange", Olympiad._nextWeeklyChange);
	}
}
