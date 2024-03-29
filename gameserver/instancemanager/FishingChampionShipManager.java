package l2s.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;

public class FishingChampionShipManager
{
	private static final Logger _log;
	private static final FishingChampionShipManager _instance;
	private long _enddate;
	private List<String> _playersName;
	private List<String> _fishLength;
	private List<String> _winPlayersName;
	private List<String> _winFishLength;
	private List<Fisher> _tmpPlayers;
	private List<Fisher> _winPlayers;
	private double _minFishLength;
	private boolean _needRefresh;

	public static final FishingChampionShipManager getInstance()
	{
		return FishingChampionShipManager._instance;
	}

	private FishingChampionShipManager()
	{
		_enddate = 0L;
		_playersName = new ArrayList<String>();
		_fishLength = new ArrayList<String>();
		_winPlayersName = new ArrayList<String>();
		_winFishLength = new ArrayList<String>();
		_tmpPlayers = new ArrayList<Fisher>();
		_winPlayers = new ArrayList<Fisher>();
		_minFishLength = 0.0;
		_needRefresh = true;
		restoreData();
		refreshWinResult();
		recalculateMinLength();
		if(_enddate <= System.currentTimeMillis())
		{
			_enddate = System.currentTimeMillis();
			new finishChamp().run();
		}
		else
			ThreadPoolManager.getInstance().schedule(new finishChamp(), _enddate - System.currentTimeMillis());
	}

	private void setEndOfChamp()
	{
		final Calendar finishtime = Calendar.getInstance();
		finishtime.setTimeInMillis(_enddate);
		finishtime.set(12, 0);
		finishtime.set(13, 0);
		finishtime.add(5, 6);
		finishtime.set(7, 3);
		finishtime.set(11, 19);
		_enddate = finishtime.getTimeInMillis();
	}

	private void restoreData()
	{
		_enddate = ServerVariables.getLong("fishChampionshipEnd", 0L);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `PlayerName`, `fishLength`, `rewarded` FROM fishing_championship");
			final ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				final int rewarded = rs.getInt("rewarded");
				if(rewarded == 0)
					_tmpPlayers.add(new Fisher(rs.getString("PlayerName"), rs.getDouble("fishLength"), 0));
				if(rewarded > 0)
					_winPlayers.add(new Fisher(rs.getString("PlayerName"), rs.getDouble("fishLength"), rewarded));
			}
			rs.close();
		}
		catch(SQLException e)
		{
			FishingChampionShipManager._log.warn("Exception: can't get fishing championship info: " + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public synchronized void newFish(final Player pl, final int lureId)
	{
		if(!Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			return;
		double p1 = Rnd.get(60, 80);
		if(p1 < 90.0 && lureId > 8484 && lureId < 8486)
		{
			final long diff = Math.round(90.0 - p1);
			if(diff > 1L)
				p1 += Rnd.get(1L, diff);
		}
		final double len = Rnd.get(100, 999) / 1000.0 + p1;
		if(_tmpPlayers.size() < 5)
		{
			for(final Fisher fisher : _tmpPlayers)
				if(fisher.getName().equalsIgnoreCase(pl.getName()))
				{
					if(fisher.getLength() < len)
					{
						fisher.setLength(len);
						pl.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.FishingChampionShipManager.ResultImproveOn"));
						recalculateMinLength();
					}
					return;
				}
			_tmpPlayers.add(new Fisher(pl.getName(), len, 0));
			pl.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.FishingChampionShipManager.YouInAPrizeList"));
			recalculateMinLength();
		}
		else if(_minFishLength < len)
		{
			for(final Fisher fisher : _tmpPlayers)
				if(fisher.getName().equalsIgnoreCase(pl.getName()))
				{
					if(fisher.getLength() < len)
					{
						fisher.setLength(len);
						pl.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.FishingChampionShipManager.ResultImproveOn"));
						recalculateMinLength();
					}
					return;
				}
			Fisher minFisher = null;
			double minLen = 99999.0;
			for(final Fisher fisher2 : _tmpPlayers)
				if(fisher2.getLength() < minLen)
				{
					minFisher = fisher2;
					minLen = minFisher.getLength();
				}
			_tmpPlayers.remove(minFisher);
			_tmpPlayers.add(new Fisher(pl.getName(), len, 0));
			pl.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.FishingChampionShipManager.YouInAPrizeList"));
			recalculateMinLength();
		}
	}

	private void recalculateMinLength()
	{
		double minLen = 99999.0;
		for(final Fisher fisher : _tmpPlayers)
			if(fisher.getLength() < minLen)
				minLen = fisher.getLength();
		_minFishLength = minLen;
	}

	public long getTimeRemaining()
	{
		return (_enddate - System.currentTimeMillis()) / 60000L;
	}

	public String getWinnerName(final int par)
	{
		if(_winPlayersName.size() >= par)
			return _winPlayersName.get(par - 1);
		return "\u2014";
	}

	public String getCurrentName(final int par)
	{
		if(_playersName.size() >= par)
			return _playersName.get(par - 1);
		return "\u2014";
	}

	public String getFishLength(final int par)
	{
		if(_winFishLength.size() >= par)
			return _winFishLength.get(par - 1);
		return "0";
	}

	public String getCurrentFishLength(final int par)
	{
		if(_fishLength.size() >= par)
			return _fishLength.get(par - 1);
		return "0";
	}

	public void getReward(final Player pl)
	{
		final String filename = "fisherman/championship/getReward.htm";
		final NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
		html.setFile(filename);
		pl.sendPacket(html);
		for(final Fisher fisher : _winPlayers)
			if(fisher._name.equalsIgnoreCase(pl.getName()) && fisher.getRewardType() != 2)
			{
				int rewardCnt = 0;
				for(int x = 0; x < _winPlayersName.size(); ++x)
					if(_winPlayersName.get(x).equalsIgnoreCase(pl.getName()))
						switch(x)
						{
							case 0:
							{
								rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_1;
								break;
							}
							case 1:
							{
								rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_2;
								break;
							}
							case 2:
							{
								rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_3;
								break;
							}
							case 3:
							{
								rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_4;
								break;
							}
							case 4:
							{
								rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_5;
								break;
							}
						}
				fisher.setRewardType(2);
				if(rewardCnt <= 0)
					continue;
				pl.sendPacket(SystemMessage.obtainItems(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM, rewardCnt, 0));
				pl.getInventory().addItem(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM, rewardCnt);
				pl.sendPacket(new ItemList(pl, false));
			}
	}

	public void showMidResult(final Player pl)
	{
		if(_needRefresh)
		{
			refreshResult();
			ThreadPoolManager.getInstance().schedule(new needRefresh(), 60000L);
		}
		final NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
		final String filename = "fisherman/championship/MidResult.htm";
		html.setFile(filename);
		String str = null;
		for(int x = 1; x <= 5; ++x)
		{
			str = str + "<tr><td width=70 align=center>" + x + (pl.isLangRus() ? " \u041c\u0435\u0441\u0442\u043e:" : " Position:") + "</td>";
			str = str + "<td width=110 align=center>" + getCurrentName(x) + "</td>";
			str = str + "<td width=80 align=center>" + getCurrentFishLength(x) + "</td></tr>";
		}
		html.replace("%TABLE%", str);
		html.replace("%prizeItem%", ItemTable.getInstance().getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).getName());
		html.replace("%prizeFirst%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_1));
		html.replace("%prizeTwo%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_2));
		html.replace("%prizeThree%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_3));
		html.replace("%prizeFour%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_4));
		html.replace("%prizeFive%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_5));
		pl.sendPacket(html);
	}

	public void showChampScreen(final Player pl, final NpcInstance npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
		final String filename = "fisherman/championship/champScreen.htm";
		html.setFile(filename);
		String str = null;
		for(int x = 1; x <= 5; ++x)
		{
			str = str + "<tr><td width=70 align=center>" + x + (pl.isLangRus() ? " \u041c\u0435\u0441\u0442\u043e:" : " Position:") + "</td>";
			str = str + "<td width=110 align=center>" + getWinnerName(x) + "</td>";
			str = str + "<td width=80 align=center>" + getFishLength(x) + "</td></tr>";
		}
		html.replace("%TABLE%", str);
		html.replace("%prizeItem%", ItemTable.getInstance().getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).getName());
		html.replace("%prizeFirst%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_1));
		html.replace("%prizeTwo%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_2));
		html.replace("%prizeThree%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_3));
		html.replace("%prizeFour%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_4));
		html.replace("%prizeFive%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_5));
		html.replace("%refresh%", String.valueOf(getTimeRemaining()));
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		pl.sendPacket(html);
	}

	public void shutdown()
	{
		ServerVariables.set("fishChampionshipEnd", _enddate);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM fishing_championship");
			statement.execute();
			statement.close();
			for(final Fisher fisher : _winPlayers)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher.getName());
				statement.setDouble(2, fisher.getLength());
				statement.setInt(3, fisher.getRewardType());
				statement.execute();
				statement.close();
			}
			for(final Fisher fisher : _tmpPlayers)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher.getName());
				statement.setDouble(2, fisher.getLength());
				statement.setInt(3, 0);
				statement.execute();
				statement.close();
			}
		}
		catch(SQLException e)
		{
			FishingChampionShipManager._log.warn("Exception: can't update player vitality: " + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private synchronized void refreshResult()
	{
		_needRefresh = false;
		_playersName.clear();
		_fishLength.clear();
		Fisher fisher1 = null;
		Fisher fisher2 = null;
		for(int x = 0; x <= _tmpPlayers.size() - 1; ++x)
			for(int y = 0; y <= _tmpPlayers.size() - 2; ++y)
			{
				fisher1 = _tmpPlayers.get(y);
				fisher2 = _tmpPlayers.get(y + 1);
				if(fisher1.getLength() < fisher2.getLength())
				{
					_tmpPlayers.set(y, fisher2);
					_tmpPlayers.set(y + 1, fisher1);
				}
			}
		for(int x = 0; x <= _tmpPlayers.size() - 1; ++x)
		{
			_playersName.add(_tmpPlayers.get(x)._name);
			_fishLength.add(String.valueOf(_tmpPlayers.get(x).getLength()));
		}
	}

	private void refreshWinResult()
	{
		_winPlayersName.clear();
		_winFishLength.clear();
		Fisher fisher1 = null;
		Fisher fisher2 = null;
		for(int x = 0; x <= _winPlayers.size() - 1; ++x)
			for(int y = 0; y <= _winPlayers.size() - 2; ++y)
			{
				fisher1 = _winPlayers.get(y);
				fisher2 = _winPlayers.get(y + 1);
				if(fisher1.getLength() < fisher2.getLength())
				{
					_winPlayers.set(y, fisher2);
					_winPlayers.set(y + 1, fisher1);
				}
			}
		for(int x = 0; x <= _winPlayers.size() - 1; ++x)
		{
			_winPlayersName.add(_winPlayers.get(x)._name);
			_winFishLength.add(String.valueOf(_winPlayers.get(x).getLength()));
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(FishingChampionShipManager.class);
		_instance = new FishingChampionShipManager();
	}

	private class finishChamp implements Runnable
	{
		@Override
		public void run()
		{
			_winPlayers.clear();
			for(final Fisher fisher : _tmpPlayers)
			{
				fisher.setRewardType(1);
				_winPlayers.add(fisher);
			}
			_tmpPlayers.clear();
			refreshWinResult();
			setEndOfChamp();
			shutdown();
			_log.info("Fishing Championship Manager : start new event period.");
			ThreadPoolManager.getInstance().schedule(new finishChamp(), _enddate - System.currentTimeMillis());
		}
	}

	private class needRefresh implements Runnable
	{
		@Override
		public void run()
		{
			_needRefresh = true;
		}
	}

	private class Fisher
	{
		private double _length;
		private String _name;
		private int _reward;

		public Fisher(final String name, final double length, final int rewardType)
		{
			_length = 0.0;
			_reward = 0;
			setName(name);
			setLength(length);
			setRewardType(rewardType);
		}

		public void setLength(final double value)
		{
			_length = value;
		}

		public void setName(final String value)
		{
			_name = value;
		}

		public void setRewardType(final int value)
		{
			_reward = value;
		}

		public String getName()
		{
			return _name;
		}

		public int getRewardType()
		{
			return _reward;
		}

		public double getLength()
		{
			return _length;
		}
	}
}
