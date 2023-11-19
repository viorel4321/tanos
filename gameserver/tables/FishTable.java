package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.reward.DropData;
import l2s.gameserver.templates.FishTemplate;

public class FishTable
{
	private static final Logger _log;
	private static final FishTable _instance;
	private TIntObjectHashMap<List<FishTemplate>> _fishes;
	private TIntObjectHashMap<List<DropData>> _fishRewards;

	public static final FishTable getInstance()
	{
		return FishTable._instance;
	}

	private FishTable()
	{
		load();
	}

	public void reload()
	{
		load();
	}

	private void load()
	{
		_fishes = new TIntObjectHashMap<List<FishTemplate>>();
		_fishRewards = new TIntObjectHashMap<List<DropData>>();
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id, level, name, hp, hpregen, fish_type, fish_group, fish_guts, guts_check_time, wait_time, combat_time FROM fish ORDER BY id");
			resultSet = statement.executeQuery();
			while(resultSet.next())
			{
				final int id = resultSet.getInt("id");
				final int lvl = resultSet.getInt("level");
				final String name = resultSet.getString("name");
				final int hp = resultSet.getInt("hp");
				final int hpreg = resultSet.getInt("hpregen");
				final int type = resultSet.getInt("fish_type");
				final int group = resultSet.getInt("fish_group");
				final int fish_guts = resultSet.getInt("fish_guts");
				final int guts_check_time = resultSet.getInt("guts_check_time");
				final int wait_time = resultSet.getInt("wait_time");
				final int combat_time = resultSet.getInt("combat_time");
				final FishTemplate fish = new FishTemplate(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time);
				List<FishTemplate> fishes;
				if((fishes = _fishes.get(group)) == null)
					_fishes.put(group, (fishes = new ArrayList<FishTemplate>()));
				fishes.add(fish);
				++count;
			}
			DbUtils.close(statement, resultSet);
			FishTable._log.info("FishTable: Loaded " + count + " fishes.");
			count = 0;
			statement = con.prepareStatement("SELECT fishid, rewardid, min, max, chance FROM fishreward ORDER BY fishid");
			resultSet = statement.executeQuery();
			while(resultSet.next())
			{
				final int fishid = resultSet.getInt("fishid");
				final int rewardid = resultSet.getInt("rewardid");
				final int mindrop = resultSet.getInt("min");
				final int maxdrop = resultSet.getInt("max");
				final int chance = resultSet.getInt("chance");
				final DropData reward = new DropData(rewardid, mindrop, maxdrop, chance);
				List<DropData> rewards;
				if((rewards = _fishRewards.get(fishid)) == null)
					_fishRewards.put(fishid, (rewards = new ArrayList<DropData>()));
				rewards.add(reward);
				++count;
			}
			FishTable._log.info("FishTable: Loaded " + count + " fish rewards.");
		}
		catch(Exception e)
		{
			FishTable._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, resultSet);
		}
	}

	public int[] getFishIds()
	{
		return _fishRewards.keys();
	}

	public List<FishTemplate> getFish(final int group, final int type, final int lvl)
	{
		final List<FishTemplate> result = new ArrayList<FishTemplate>();
		final List<FishTemplate> fishs = _fishes.get(group);
		if(fishs == null)
		{
			FishTable._log.warn("No fishes defined for group : " + group + "!");
			return null;
		}
		for(final FishTemplate f : fishs)
		{
			if(f.getType() != type)
				continue;
			if(f.getLevel() != lvl)
				continue;
			result.add(f);
		}
		if(result.isEmpty())
			FishTable._log.warn("No fishes for group : " + group + " type: " + type + " level: " + lvl + "!");
		return result;
	}

	public List<DropData> getFishReward(final int fishid)
	{
		final List<DropData> result = _fishRewards.get(fishid);
		if(_fishRewards == null)
		{
			FishTable._log.warn("No fish rewards defined for fish id: " + fishid + "!");
			return null;
		}
		if(result.isEmpty())
			FishTable._log.warn("No fish rewards for fish id: " + fishid + "!");
		return result;
	}

	static
	{
		_log = LoggerFactory.getLogger(FishTable.class);
		_instance = new FishTable();
	}
}
