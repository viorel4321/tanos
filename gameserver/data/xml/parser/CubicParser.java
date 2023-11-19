package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import gnu.trove.map.hash.TIntIntHashMap;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.CubicHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.CubicTemplate;

public final class CubicParser extends AbstractParser<CubicHolder>
{
	private static CubicParser _instance;

	public static CubicParser getInstance()
	{
		return CubicParser._instance;
	}

	protected CubicParser()
	{
		super(CubicHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/pts/cubics.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "cubics.dtd";
	}

	@Override
	protected void readData(final Element rootElement) throws Exception
	{
		final Iterator<?> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			final Element cubicElement = (Element) iterator.next();
			final int id = Integer.parseInt(cubicElement.attributeValue("id"));
			final int level = Integer.parseInt(cubicElement.attributeValue("level"));
			final int delay = Integer.parseInt(cubicElement.attributeValue("delay"));
			final CubicTemplate template = new CubicTemplate(id, level, delay);
			getHolder().addCubicTemplate(template);
			final Iterator<?> skillsIterator = cubicElement.elementIterator();
			while(skillsIterator.hasNext())
			{
				final Element skillsElement = (Element) skillsIterator.next();
				final int chance = Integer.parseInt(skillsElement.attributeValue("chance"));
				final List<CubicTemplate.SkillInfo> skills = new ArrayList<CubicTemplate.SkillInfo>(1);
				final Iterator<?> skillIterator = skillsElement.elementIterator();
				while(skillIterator.hasNext())
				{
					final Element skillElement = (Element) skillIterator.next();
					final int id2 = Integer.parseInt(skillElement.attributeValue("id"));
					final int level2 = Integer.parseInt(skillElement.attributeValue("level"));
					final int chance2 = skillElement.attributeValue("chance") == null ? 0 : Integer.parseInt(skillElement.attributeValue("chance"));
					final boolean canAttackDoor = Boolean.parseBoolean(skillElement.attributeValue("can_attack_door"));
					final CubicTemplate.ActionType type = CubicTemplate.ActionType.valueOf(skillElement.attributeValue("action_type"));
					final TIntIntHashMap set = new TIntIntHashMap();
					final Iterator<?> chanceIterator = skillElement.elementIterator();
					while(chanceIterator.hasNext())
					{
						final Element chanceElement = (Element) chanceIterator.next();
						final int min = Integer.parseInt(chanceElement.attributeValue("min"));
						final int max = Integer.parseInt(chanceElement.attributeValue("max"));
						final int value = Integer.parseInt(chanceElement.attributeValue("value"));
						for(int i = min; i <= max; ++i)
							set.put(i, value);
					}
					if(chance2 == 0 && set.isEmpty())
						this.warn("Wrong skill chance. Cubic: " + id + "/" + level);
					final Skill skill = SkillTable.getInstance().getInfo(id2, level2);
					if(skill != null)
					{
						skill.setCubicSkill(true);
						skills.add(new CubicTemplate.SkillInfo(skill, chance2, type, canAttackDoor, set));
					}
				}
				template.putSkills(chance, skills);
			}
		}
	}

	static
	{
		CubicParser._instance = new CubicParser();
	}
}
