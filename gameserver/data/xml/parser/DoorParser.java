package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Polygon;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.DoorHolder;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public final class DoorParser extends AbstractParser<DoorHolder>
{
	private static final DoorParser _instance = new DoorParser();

	public static DoorParser getInstance()
	{
		return _instance;
	}

	protected DoorParser()
	{
		super(DoorHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/doors/");
	}

	@Override
	public String getDTDFileName()
	{
		return "doors.dtd";
	}

	private StatsSet initBaseStats()
	{
		StatsSet baseDat = new StatsSet();
		//baseDat.set("level", 0);
		baseDat.set("str", 0);
		baseDat.set("con", 0);
		baseDat.set("dex", 0);
		baseDat.set("int", 0);
		baseDat.set("wit", 0);
		baseDat.set("men", 0);
		baseDat.set("baseShldDef", 0);
		baseDat.set("baseShldRate", 0);
		baseDat.set("crit", 0);
		//baseDat.set("baseMCritRate", 0);
		baseDat.set("attackRange", 0);
		baseDat.set("mp", 0);
		//baseDat.set("baseCpMax", 0);
		baseDat.set("pAtk", 0);
		baseDat.set("mAtk", 0);
		baseDat.set("atkSpd", 0);
		baseDat.set("baseMAtkSpd", 0);
		baseDat.set("walkSpd", 0);
		baseDat.set("runSpd", 0);
		baseDat.set("hpRegen", 0);
		//baseDat.set("baseCpReg", 0);
		baseDat.set("mpRegen", 0);

		return baseDat;
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element doorElement = iterator.next();

			if("door".equals(doorElement.getName()))
			{
				StatsSet doorSet = initBaseStats();
				StatsSet aiParams = null;

				doorSet.set("door_type", doorElement.attributeValue("type"));

				Element posElement = doorElement.element("pos");
				Location doorPos;
				int x = Integer.parseInt(posElement.attributeValue("x"));
				int y = Integer.parseInt(posElement.attributeValue("y"));
				int z = Integer.parseInt(posElement.attributeValue("z"));
				doorSet.set("pos", doorPos = new Location(x, y, z));

				Polygon shape = new Polygon();
				int minz = 0, maxz = 0;

				Element shapeElement = doorElement.element("shape");
				minz = Integer.parseInt(shapeElement.attributeValue("minz"));
				maxz = Integer.parseInt(shapeElement.attributeValue("maxz"));
				shape.add(Integer.parseInt(shapeElement.attributeValue("ax")), Integer.parseInt(shapeElement.attributeValue("ay")));
				shape.add(Integer.parseInt(shapeElement.attributeValue("bx")), Integer.parseInt(shapeElement.attributeValue("by")));
				shape.add(Integer.parseInt(shapeElement.attributeValue("cx")), Integer.parseInt(shapeElement.attributeValue("cy")));
				shape.add(Integer.parseInt(shapeElement.attributeValue("dx")), Integer.parseInt(shapeElement.attributeValue("dy")));
				shape.setZmin(minz);
				shape.setZmax(maxz);
				doorSet.set("shape", shape);

				doorPos.setZ(minz + 32); //фактическая координата двери в мире

				for(Iterator<Element> i = doorElement.elementIterator(); i.hasNext();)
				{
					Element n = i.next();
					if("set".equals(n.getName()))
						doorSet.set(n.attributeValue("name"), n.attributeValue("value"));
					else if("ai_params".equals(n.getName()))
					{
						if(aiParams == null)
						{
							aiParams = new StatsSet();
							doorSet.set("ai_params", aiParams);
						}

						for(Iterator<Element> aiParamsIterator = n.elementIterator(); aiParamsIterator.hasNext();)
						{
							Element aiParamElement = aiParamsIterator.next();

							aiParams.set(aiParamElement.attributeValue("name"), aiParamElement.attributeValue("value"));
						}
					}
				}

				doorSet.set("uid", doorElement.attributeValue("id"));
				doorSet.set("name", doorElement.attributeValue("name"));
				doorSet.set("hp", doorElement.attributeValue("hp"));
				doorSet.set("pDef", doorElement.attributeValue("pdef"));
				doorSet.set("mDef", doorElement.attributeValue("mdef"));

				doorSet.set("height", (maxz - minz) & 0xfff0);
				doorSet.set("radius", Math.max(50, shape.getRadius()));

				DoorTemplate template = new DoorTemplate(doorSet);
				getHolder().addTemplate(template);
			}
		}
	}
}
