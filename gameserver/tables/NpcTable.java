package l2s.gameserver.tables;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.cache.InfoCache;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.CatacombSpawnManager;
import l2s.gameserver.model.MinionData;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.reward.DropData;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.DropList;
import l2s.gameserver.utils.Log;

public class NpcTable
{
	private static final Logger _log = LoggerFactory.getLogger(NpcTable.class);
	private static NpcTable _instance;
	private static NpcTemplate[] _npcs;
	private static HashMap<Integer, StatsSet> ai_params;
	private static final TIntObjectMap<List<NpcTemplate>> _npcsByLevel = new TIntObjectHashMap<List<NpcTemplate>>();
	private static HashMap<String, NpcTemplate> _npcsNames;
	private static boolean _initialized = false;
	private final double[] hprateskill;

	public static NpcTable getInstance()
	{
		if(_instance == null)
			_instance = new NpcTable();
		return _instance;
	}

	private NpcTable()
	{
		hprateskill = new double[] { 0.0, 1.0, 1.2, 1.3, 2.0, 2.0, 4.0, 4.0, 0.25, 0.5, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0 };
		_npcsNames = new HashMap<String, NpcTemplate>();
		ai_params = new HashMap<Integer, StatsSet>();
		RestoreNpcData(false);
	}

	private void RestoreNpcData(final boolean reload)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			try
			{
				statement = con.prepareStatement("SELECT * FROM ai_params");
				rs = statement.executeQuery();
				LoadAIParams(rs);
			}
			catch(Exception ex)
			{}
			finally
			{
				DbUtils.closeQuietly(statement, rs);
			}
			try
			{
				statement = con.prepareStatement("SELECT * FROM npc WHERE ai_type IS NOT NULL");
				rs = statement.executeQuery();
				fillNpcTable(rs, reload);
			}
			catch(Exception e)
			{
				_log.error("error while creating npc table ", e);
			}
			finally
			{
				DbUtils.closeQuietly(statement, rs);
			}
			try
			{
				statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
				rs = statement.executeQuery();
				final List<Integer> unimpl = new ArrayList<Integer>();
				int counter = 0;
				while(rs.next())
				{
					final int mobId = rs.getInt("npcid");
					final NpcTemplate npcDat = _npcs[mobId];
					if(npcDat == null)
						continue;
					final int skillId = rs.getInt("skillid");
					final int level = rs.getInt("level");
					if(skillId == 4416)
						npcDat.setRace(level);
					if(skillId >= 4290 && skillId <= 4302)
						_log.info("Warning! Skill " + skillId + " not used, use 4416 instead.");
					else
					{
						if(skillId == 4408)
							if(CatacombSpawnManager._monsters.contains(mobId))
								npcDat.setRateHp(hprateskill[12]);
							else
								npcDat.setRateHp(hprateskill[level]);
						final Skill npcSkill = SkillTable.getInstance().getInfo(skillId, level);
						if(npcSkill == null || npcSkill.getSkillType() == Skill.SkillType.NOTDONE)
							unimpl.add(skillId);
						if(npcSkill == null)
							continue;
						npcDat.addSkill(npcSkill);
						++counter;
					}
				}
				new File("log/unimplemented_npc_skills.txt").delete();
				for(final Integer i : unimpl)
					Log.addLog("[" + i + "] " + SkillTable.getInstance().getInfo(i, 1), "unimplemented_npc_skills");
				_log.info("Loaded " + counter + " npc skills.");
			}
			catch(Exception e)
			{
				_log.error("error while reading npcskills table ", e);
			}
			finally
			{
				DbUtils.closeQuietly(statement, rs);
			}
			try
			{
				statement = con.prepareStatement("SELECT mobId, itemId, min, max, category, chance FROM droplist ORDER BY mobId, category, chance DESC");
				rs = statement.executeQuery();
				DropData dropDat = null;
				NpcTemplate npcDat2 = null;
				while(rs.next())
				{
					final int mobId2 = rs.getInt("mobId");
					npcDat2 = _npcs[mobId2];
					if(npcDat2 != null)
					{
						dropDat = new DropData();
						final int id = rs.getShort("itemId");
						dropDat.setItemId(id);
						if(dropDat.getItem() == null)
							_log.warn("Drop item null for mobId,itemId: " + mobId2 + "," + id);
						else
						{
							dropDat.setChance(rs.getInt("chance"));
							dropDat.setMinDrop(rs.getInt("min"));
							dropDat.setMaxDrop(rs.getInt("max"));
							final int category = rs.getInt("category");
							dropDat.setSweep(category < 0);
							if(dropDat.getItem().isArrow() || dropDat.getItemId() == 1419)
								dropDat.setGroupId(127);
							else
								dropDat.setGroupId(category);
							npcDat2.addDropData(dropDat);
						}
					}
				}
				if(!Config.ALT_GAME_GEN_DROPLIST_ON_DEMAND)
					FillDropList();
				if(Config.KILL_COUNTER_PRELOAD && Config.KILL_COUNTER)
					loadKillCount();
			}
			catch(Exception e)
			{
				_log.error("error reading npc drops ", e);
			}
			finally
			{
				DbUtils.closeQuietly(statement, rs);
			}
			try
			{
				statement = con.prepareStatement("SELECT boss_id, minion_id, amount_min, amount_max FROM minions");
				rs = statement.executeQuery();
				MinionData minionDat = null;
				NpcTemplate npcDat2 = null;
				int cnt = 0;
				while(rs.next())
				{
					final int raidId = rs.getInt("boss_id");
					npcDat2 = _npcs[raidId];
					if(npcDat2 == null)
						_log.warn("Not found boss_id " + raidId + " from minions in npc table!");
					else
					{
						minionDat = new MinionData();
						minionDat.setMinionId(rs.getInt("minion_id"));
						minionDat.setAmount(rs.getInt("amount_min"), rs.getInt("amount_max"));
						npcDat2.addRaidData(minionDat);
						++cnt;
					}
				}
				_log.info("NpcTable: Loaded " + cnt + " Minions.");
			}
			catch(Exception e)
			{
				_log.error("error loading minions", e);
			}
			finally
			{
				DbUtils.closeQuietly(statement, rs);
			}
			try
			{
				statement = con.prepareStatement("SELECT npc_id, class_id FROM skill_learn");
				rs = statement.executeQuery();
				NpcTemplate npcDat = null;
				int cnt2 = 0;
				while(rs.next())
				{
					npcDat = _npcs[rs.getInt(1)];
					npcDat.addTeachInfo(ClassId.values()[rs.getInt(2)]);
					++cnt2;
				}
				_log.info("NpcTable: Loaded " + cnt2 + " SkillLearn entrys.");
			}
			catch(Exception e)
			{
				_log.error("error loading skilllearn", e);
			}
			finally
			{
				DbUtils.closeQuietly(statement, rs);
			}
		}
		catch(Exception e)
		{
			_log.error("Cannot find connection to database");
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		_initialized = true;
	}

	private static void LoadAIParams(final ResultSet AIData) throws Exception
	{
		int ai_params_counter = 0;
		StatsSet set = null;
		while(AIData.next())
		{
			final int npc_id = AIData.getInt("npc_id");
			final String param = AIData.getString("param");
			final String value = AIData.getString("value");
			if(ai_params.containsKey(npc_id))
				set = ai_params.get(npc_id);
			else
			{
				set = new StatsSet();
				ai_params.put(npc_id, set);
			}
			set.set(param, value);
			++ai_params_counter;
		}
		_log.info("NpcTable: Loaded " + ai_params_counter + " AI params for " + ai_params.size() + " NPCs.");
	}

	private static StatsSet fillNpcTable(final ResultSet NpcData, final boolean reload) throws Exception
	{
		StatsSet npcDat = null;
		final List<NpcTemplate> temp = new ArrayList<NpcTemplate>(10000);
		int maxId = 0;
		while(NpcData.next())
		{
			npcDat = new StatsSet();
			final int id = NpcData.getInt("id");
			final int level = NpcData.getByte("level");
			if(maxId < id)
				maxId = id;
			npcDat.set("npcId", id);
			npcDat.set("displayId", NpcData.getInt("displayId"));
			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));
			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("radius", NpcData.getDouble("collision_radius"));
			npcDat.set("height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			npcDat.set("type", NpcData.getString("type"));
			npcDat.set("ai_type", NpcData.getString("ai_type"));
			npcDat.set("attackRange", NpcData.getInt("attackrange"));
			npcDat.set("revardExp", NpcData.getInt("exp"));
			npcDat.set("revardSp", NpcData.getInt("sp"));
			npcDat.set("atkSpd", NpcData.getInt("atkspd"));
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
			npcDat.set("aggroRange", NpcData.getShort("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("walkSpd", NpcData.getInt("walkspd"));
			npcDat.set("runSpd", NpcData.getInt("runspd"));
			npcDat.set("hpRegen", NpcData.getDouble("base_hp_regen"));
			npcDat.set("mpRegen", NpcData.getDouble("base_mp_regen"));
			npcDat.set("str", NpcData.getInt("str"));
			npcDat.set("con", NpcData.getInt("con"));
			npcDat.set("dex", NpcData.getInt("dex"));
			npcDat.set("int", NpcData.getInt("int"));
			npcDat.set("wit", NpcData.getInt("wit"));
			npcDat.set("men", NpcData.getInt("men"));
			npcDat.set("hp", NpcData.getInt("hp"));
			npcDat.set("mp", NpcData.getInt("mp"));
			npcDat.set("pAtk", NpcData.getInt("patk"));
			npcDat.set("pDef", NpcData.getInt("pdef"));
			npcDat.set("mAtk", NpcData.getInt("matk"));
			npcDat.set("mDef", NpcData.getInt("mdef"));
			npcDat.set("baseShldDef", NpcData.getInt("shield_defense"));
			npcDat.set("baseShldRate", NpcData.getInt("shield_defense_rate"));
			npcDat.set("crit", NpcData.getInt("base_critical") * 10);
			npcDat.set("physHitMod", NpcData.getDouble("physical_hit_modify"));
			npcDat.set("physAvoidMod", NpcData.getInt("physical_avoid_modify"));
			final String factionId = NpcData.getString("faction_id");
			if(factionId != null)
				factionId.trim();
			npcDat.set("factionId", factionId);
			npcDat.set("factionRange", factionId == null || factionId.equals("") ? 0 : NpcData.getShort("faction_range"));
			npcDat.set("soulshotCount", NpcData.getInt("soulshot_count"));
			npcDat.set("spiritshotCount", NpcData.getInt("spiritshot_count"));
			npcDat.set("isDropHerbs", NpcData.getBoolean("isDropHerbs"));
			npcDat.set("shots", NpcData.getString("shots"));
			final NpcTemplate template = new NpcTemplate(npcDat, ai_params.containsKey(id) ? ai_params.get(id) : null);
			if(reload && _npcs[template.npcId] != null)
				template.setEventQuests(_npcs[template.npcId].getEventQuests());
			temp.add(template);

			List<NpcTemplate> npcsByLevel = _npcsByLevel.get(level);
			if(npcsByLevel == null)
			{
				npcsByLevel = new ArrayList<NpcTemplate>();
				_npcsByLevel.put(level, npcsByLevel);
			}
			npcsByLevel.add(template);

			_npcsNames.put(NpcData.getString("name").toLowerCase(), template);
		}
		_npcs = new NpcTemplate[maxId + 1];
		for(final NpcTemplate template2 : temp)
			_npcs[template2.npcId] = template2;
		_log.info("NpcTable: Loaded " + temp.size() + " Npc Templates.");
		return npcDat;
	}

	public static void reloadNpc(final int id)
	{
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			final NpcTemplate old = getTemplate(id);
			final HashMap<Integer, Skill> skills = new HashMap<Integer, Skill>();
			if(old.getSkills() != null)
				skills.putAll(old.getSkills());
			ClassId[] classIds = null;
			if(old.getTeachInfo() != null)
				classIds = old.getTeachInfo().clone();
			final List<MinionData> minions = new ArrayList<MinionData>();
			minions.addAll(old.getMinionData());
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT * FROM npc WHERE id=?");
			st.setInt(1, id);
			rs = st.executeQuery();
			fillNpcTable(rs, false);
			final NpcTemplate created = getTemplate(id);
			for(final Skill skill : skills.values())
				created.addSkill(skill);
			if(classIds != null)
				for(final ClassId classId : classIds)
					created.addTeachInfo(classId);
			for(final MinionData minion : minions)
				created.addRaidData(minion);
		}
		catch(Exception e)
		{
			_log.warn("cannot reload npc " + id + ": " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, st, rs);
		}
	}

	public static StatsSet getNpcStatsSet(final int id)
	{
		StatsSet dat = null;
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT * FROM npc WHERE id=?");
			st.setInt(1, id);
			rs = st.executeQuery();
			dat = fillNpcTable(rs, false);
		}
		catch(Exception e)
		{
			_log.warn("cannot load npc stats for " + id + ": " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, st, rs);
		}
		return dat;
	}

	@SuppressWarnings("rawtypes")
	public void reloadAllNpc()
	{
		_npcsByLevel.clear();
		_npcsNames = new HashMap<String, NpcTemplate>();
		ai_params = new HashMap<Integer, StatsSet>();
		for(final NpcTemplate npc : _npcs)
		{
			if(npc != null)
				npc.clean();
		}
		RestoreNpcData(true);
	}

	public static boolean isInitialized()
	{
		return _initialized;
	}

	public static void replaceTemplate(final NpcTemplate npc)
	{
		_npcs[npc.npcId] = npc;
		_npcsNames.put(npc.name.toLowerCase(), npc);
	}

	public static NpcTemplate getTemplate(final int id)
	{
		return _npcs[id];
	}

	public static NpcTemplate getTemplateByName(final String name)
	{
		return _npcsNames.get(name.toLowerCase());
	}

	public static List<NpcTemplate> getAllOfLevel(final int lvl)
	{
		List<NpcTemplate> npcsByLevel = _npcsByLevel.get(lvl);
		if(npcsByLevel == null)
			return Collections.emptyList();
		return npcsByLevel;
	}

	public static NpcTemplate[] getAll()
	{
		return _npcs;
	}

	public void FillDropList()
	{
		for(final NpcTemplate npc : _npcs)
			if(npc != null)
				InfoCache.addToDroplistCache(npc.npcId, DropList.generateDroplist(npc, null, 1.0, null));
		_log.info("Players droplist was cached");
	}

	public static void storeKillsCount()
	{
		Connection con = null;
		Statement fs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			fs = con.createStatement();
			for(final NpcTemplate t : getAll())
			{
				if(t != null && t.killscount > 0)
				{
					StringBuilder sb = new StringBuilder();
					fs.addBatch(sb.append("REPLACE INTO `killcount` SET `npc_id`=").append(t.npcId).append(", `count`=").append(t.killscount).append(", `char_id`=-1").toString());
				}
			}
			fs.executeBatch();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, fs);
		}
	}

	private void loadKillCount()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet list = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `killcount` WHERE `char_id`=-1");
			list = statement.executeQuery();
			while(list.next())
			{
				final NpcTemplate t = getTemplate(list.getInt("npc_id"));
				if(t != null)
					t.killscount = list.getInt("count");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, list);
		}
	}
}
