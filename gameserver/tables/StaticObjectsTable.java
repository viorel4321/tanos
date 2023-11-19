package l2s.gameserver.tables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class StaticObjectsTable
{
	private static Logger _log;
	private static StaticObjectsTable _instance;
	private HashMap<Integer, StaticObjectInstance> _staticObjects;

	public static StaticObjectsTable getInstance()
	{
		if(StaticObjectsTable._instance == null)
			StaticObjectsTable._instance = new StaticObjectsTable();
		return StaticObjectsTable._instance;
	}

	public StaticObjectsTable()
	{
		_staticObjects = new HashMap<Integer, StaticObjectInstance>();
		parseData();
		StaticObjectsTable._log.info("StaticObject: Loaded " + _staticObjects.size() + " StaticObject Templates.");
	}

	private void parseData()
	{
		LineNumberReader lnr = null;
		try
		{
			final File doorData = new File(Config.DATAPACK_ROOT, "data/staticobjects.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));
			String line = null;
			while((line = lnr.readLine()) != null)
				if(line.trim().length() != 0)
				{
					if(line.startsWith("#"))
						continue;
					final StaticObjectInstance obj = parse(line);
					_staticObjects.put(obj.getUId(), obj);
				}
		}
		catch(FileNotFoundException e2)
		{
			StaticObjectsTable._log.warn("staticobjects.csv is missing in data folder");
		}
		catch(Exception e)
		{
			StaticObjectsTable._log.warn("error while creating StaticObjects table " + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception ex)
			{}
		}
	}

	public static StaticObjectInstance parse(final String line)
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		st.nextToken();
		final int id = Integer.parseInt(st.nextToken());
		final int x = Integer.parseInt(st.nextToken());
		final int y = Integer.parseInt(st.nextToken());
		final int z = Integer.parseInt(st.nextToken());
		final int type = Integer.parseInt(st.nextToken());
		final String filePath = st.nextToken();
		final int mapX = Integer.parseInt(st.nextToken());
		final int mapY = Integer.parseInt(st.nextToken());
		final StatsSet npcDat = NpcTemplate.getEmptyStatsSet();
		npcDat.set("npcId", id);
		npcDat.set("name", type == 0 ? "Arena" : "");
		npcDat.set("jClass", "static");
		npcDat.set("type", "StaticObject");
		final NpcTemplate template = new NpcTemplate(npcDat);
		final StaticObjectInstance obj = new StaticObjectInstance(IdFactory.getInstance().getNextId(), template);
		obj.setType(type);
		obj.setUId(id);
		obj.setFilePath(filePath);
		obj.setMapX(mapX);
		obj.setMapY(mapY);
		obj.spawnMe(new Location(x, y, z));
		return obj;
	}

	static
	{
		StaticObjectsTable._log = LoggerFactory.getLogger(StaticObjectsTable.class);
	}
}
