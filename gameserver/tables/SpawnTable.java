package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.CatacombSpawnManager;
import l2s.gameserver.instancemanager.DayNightSpawnManager;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.AutoSpawnHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.SiegeGuardInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SpawnTable
{
	private static final Logger _log;
	private static SpawnTable _instance;
	private List<Spawn> _spawntable;
	private int _npcSpawnCount;
	private int _spawnCount;

	public static SpawnTable getInstance()
	{
		if(SpawnTable._instance == null)
			new SpawnTable();
		return SpawnTable._instance;
	}

	private SpawnTable()
	{
		SpawnTable._instance = this;
	}

	public List<Spawn> getSpawnTable()
	{
		return _spawntable;
	}

	private void fillSpawnTable()
	{
		_spawntable = new ArrayList<Spawn>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM spawnlist ORDER by npc_templateid");
			rset = statement.executeQuery();
			_npcSpawnCount = 0;
			_spawnCount = 0;
			final boolean delay = SpawnManager.retardation && Config.DELAY_SPAWN_NPC > 0L;
			while(rset.next())
			{
				final NpcTemplate template1 = NpcTable.getTemplate(rset.getInt("npc_templateid"));
				if(template1 != null)
				{
					if(template1.isInstanceOf(SiegeGuardInstance.class))
						continue;
					if(!Config.SPAWN_CLASS_MASTERS && template1.type.equalsIgnoreCase("L2ClassMaster"))
						continue;
					if(!Config.HITMAN_ENABLE && template1.type.equalsIgnoreCase("L2Hitman"))
						continue;
					final Spawn spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count") * (Config.ALT_DOUBLE_SPAWN && !template1.isRaid ? 2 : 1));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"), rset.getInt("respawn_delay_rnd"));
					spawnDat.setLocation(rset.getInt("loc_id"));
					spawnDat.setRespawnTime(0);
					if(template1.isInstanceOf(MonsterInstance.class))
						if(template1.name.contains("Lilim") || template1.name.contains("Lith"))
							CatacombSpawnManager.getInstance().addDawnMob(spawnDat);
						else if(template1.name.contains("Nephilim") || template1.name.contains("Gigant"))
							CatacombSpawnManager.getInstance().addDuskMob(spawnDat);
					if(template1.isRaid)
						RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat);
					switch(rset.getInt("periodOfDay"))
					{
						case 0:
						{
							_npcSpawnCount += spawnDat.init();
							_spawntable.add(spawnDat);
							break;
						}
						case 1:
						{
							DayNightSpawnManager.getInstance().addDayMob(spawnDat);
							break;
						}
						case 2:
						{
							DayNightSpawnManager.getInstance().addNightMob(spawnDat);
							break;
						}
					}
					++_spawnCount;
					if(!delay)
						continue;
					Thread.sleep(Config.DELAY_SPAWN_NPC);
				}
				else
					SpawnTable._log.warn("mob data for id:" + rset.getInt("npc_templateid") + " missing in npc table");
			}
			DayNightSpawnManager.getInstance().notifyChangeMode();
			CatacombSpawnManager.getInstance().notifyChangeMode();
		}
		catch(Exception e1)
		{
			SpawnTable._log.error("spawn couldn't be initialized: ", e1);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		SpawnTable._log.info("SpawnTable: Loaded " + _spawnCount + " Npc Spawn Locs. Total NPCs: " + _npcSpawnCount);
		if(Config.MOBSLOOTERS)
			loadInventory();
	}

	public void deleteSpawn(final Spawn spawn)
	{
		_spawntable.remove(spawn);
	}

	public void reloadAll()
	{
		World.deleteVisibleNpcSpawns();
		fillSpawnTable();
		SpawnManager.getInstance().reloadAll();
		RaidBossSpawnManager.getInstance().reloadBosses();
	}

	public void loadSpawn()
	{
		SpawnManager.retardation = true;
		SpawnTable._log.info("Spawn loading...");
		fillSpawnTable();
		SpawnManager.getInstance().spawnAll();
		RaidBossSpawnManager.getInstance();
		AutoSpawnHandler.getInstance();
		SpawnTable._log.info("Spawn loading is completed.");
		SpawnManager.retardation = false;
		SpawnManager.completed = true;
	}

	private void loadInventory()
	{
		int count = 0;
		List<NpcInstance> temp = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT owner_id, object_id, item_id, count, enchant_level FROM items WHERE loc = 'MONSTER'");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				temp = GameObjectsStorage.getNpcs(false, rset.getInt("owner_id"));
				try
				{
					final ItemInstance item = ItemInstance.restoreFromDb(rset.getInt("object_id"), true);
					if(temp.size() > 0)
					{
						final MonsterInstance monster = (MonsterInstance) temp.toArray()[Rnd.get(temp.size())];
						monster.giveItem(item, false);
					}
					else
						NpcTable.getTemplate(rset.getInt("owner_id")).giveItem(item, false);
				}
				catch(Exception e)
				{
					SpawnTable._log.error("Unable to restore monsters inventory for " + temp.get(0).getNpcId(), e);
				}
			}
		}
		catch(Exception e2)
		{
			SpawnTable._log.error("Can't load monsters inventory: ", e2);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static void unload()
	{
		if(SpawnTable._instance != null)
			SpawnTable._instance = null;
	}

	static
	{
		_log = LoggerFactory.getLogger(SpawnTable.class);
	}
}
