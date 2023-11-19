package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Polygon;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.model.Territory;
import l2s.gameserver.tables.TerritoryTable;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.spawn.PeriodOfDay;
import l2s.gameserver.templates.spawn.SpawnNpcInfo;
import l2s.gameserver.templates.spawn.SpawnTemplate;
import l2s.gameserver.utils.Location;

public final class SpawnParser extends AbstractParser<SpawnHolder>
{
	private static final SpawnParser _instance;

	public static SpawnParser getInstance()
	{
		return SpawnParser._instance;
	}

	protected SpawnParser()
	{
		super(SpawnHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/spawn/");
	}

	@Override
	public boolean isIgnored(final File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "spawn.dtd";
	}

	@Override
	protected void readData(final Element rootElement) throws Exception
	{
		final Map<String, Territory> territories = new HashMap<String, Territory>();
		final Iterator<Element> spawnIterator = rootElement.elementIterator();
		while(spawnIterator.hasNext())
		{
			final Element spawnElement = spawnIterator.next();
			if(spawnElement.getName().equalsIgnoreCase("territory"))
			{
				final String terName = spawnElement.attributeValue("name");
				final Territory territory = parseTerritory(terName, spawnElement);
				territories.put(terName, territory);
			}
			else
			{
				if(!spawnElement.getName().equalsIgnoreCase("spawn"))
					continue;
				String group = spawnElement.attributeValue("group");
				String name = spawnElement.attributeValue("name") == null ? (group == null ? "" : group) : spawnElement.attributeValue("name");
				int respawn = spawnElement.attributeValue("respawn") == null ? 60 : Integer.parseInt(spawnElement.attributeValue("respawn"));
				int respawnRandom = spawnElement.attributeValue("respawn_random") == null ? 0 : Integer.parseInt(spawnElement.attributeValue("respawn_random"));
				String respawnPattern = spawnElement.attributeValue("respawn_pattern");
				int count = spawnElement.attributeValue("count") == null ? 1 : Integer.parseInt(spawnElement.attributeValue("count"));
				PeriodOfDay periodOfDay = spawnElement.attributeValue("period_of_day") == null ? PeriodOfDay.NONE : PeriodOfDay.valueOf(spawnElement.attributeValue("period_of_day").toUpperCase());
				if(group == null)
					group = periodOfDay.name();
				SpawnTemplate template = new SpawnTemplate(name, periodOfDay, count, respawn, respawnRandom, respawnPattern);
				final Iterator<Element> subIterator = spawnElement.elementIterator();
				while(subIterator.hasNext())
				{
					final Element subElement = subIterator.next();
					if(subElement.getName().equalsIgnoreCase("point"))
					{
						final int x = Integer.parseInt(subElement.attributeValue("x"));
						final int y = Integer.parseInt(subElement.attributeValue("y"));
						final int z = Integer.parseInt(subElement.attributeValue("z"));
						final int h = subElement.attributeValue("h") == null ? -1 : Integer.parseInt(subElement.attributeValue("h"));
						template.addSpawnRange(new Location(x, y, z, h));
					}
					else if(subElement.getName().equalsIgnoreCase("territory"))
					{
						final String terName2 = subElement.attributeValue("name");
						if(terName2 != null)
						{
							final Territory g = territories.get(terName2);
							if(g == null)
								this.error("Invalid territory name: " + terName2 + "; " + getCurrentFileName());
							else
								template.addSpawnRange(g);
						}
						else
						{
							final Territory temp = parseTerritory(null, subElement);
							template.addSpawnRange(temp);
						}
					}
					else
					{
						if(!subElement.getName().equalsIgnoreCase("npc"))
							continue;
						final int npcId = Integer.parseInt(subElement.attributeValue("id"));
						final int max = subElement.attributeValue("max") == null ? 0 : Integer.parseInt(subElement.attributeValue("max"));
						MultiValueSet<String> parameters = StatsSet.EMPTY;
						for(final Element e : subElement.elements())
						{
							if(parameters.isEmpty())
								parameters = new MultiValueSet<String>();
							parameters.set(e.attributeValue("name"), e.attributeValue("value"));
						}
						template.addNpc(new SpawnNpcInfo(npcId, max, parameters));
					}
				}
				if(template.getNpcSize() == 0)
					this.warn("Npc id is zero! File: " + getCurrentFileName());
				else if(template.getSpawnRangeSize() == 0)
					this.warn("No points to spawn! File: " + getCurrentFileName());
				else
					getHolder().addSpawn(group, template);
			}
		}
	}

	private Territory parseTerritory(final String name, final Element e)
	{
		final Territory t = new Territory(TerritoryTable.locId++);
		t.add(parsePolygon0(name, e));
		final Iterator<Element> iterator = e.elementIterator("banned_territory");
		while(iterator.hasNext())
			t.addBanned(parsePolygon0(name, iterator.next()));
		return t;
	}

	private Polygon parsePolygon0(final String name, final Element e)
	{
		final Polygon temp = new Polygon();
		final Iterator<Element> addIterator = e.elementIterator("add");
		while(addIterator.hasNext())
		{
			final Element addElement = addIterator.next();
			final int x = Integer.parseInt(addElement.attributeValue("x"));
			final int y = Integer.parseInt(addElement.attributeValue("y"));
			final int zmin = Integer.parseInt(addElement.attributeValue("zmin"));
			final int zmax = Integer.parseInt(addElement.attributeValue("zmax"));
			temp.add(x, y).setZmin(zmin).setZmax(zmax);
		}
		if(!temp.validate())
			this.error("Invalid polygon: " + name + "{" + temp + "}. File: " + getCurrentFileName());
		return temp;
	}

	static
	{
		_instance = new SpawnParser();
	}
}
