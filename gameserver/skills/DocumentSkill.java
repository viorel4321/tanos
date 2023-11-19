package l2s.gameserver.skills;

import java.io.File;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.conditions.Condition;
import l2s.gameserver.skills.skillclasses.Extract;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.templates.StatsSet;

final class DocumentSkill extends DocumentBase
{
	private SkillData currentSkill;
	private HashSet<String> usedTables;
	private List<Skill> skillsInFile;

	DocumentSkill(final File file)
	{
		super(file);
		usedTables = new HashSet<String>();
		skillsInFile = new LinkedList<Skill>();
	}

	@Override
	protected void resetTable()
	{
		if(!usedTables.isEmpty())
			for(final String table : tables.keySet())
				if(!usedTables.contains(table))
					System.out.println("WARNING: Unused table " + table + " for skill " + currentSkill.id);
		usedTables.clear();
		super.resetTable();
	}

	private void setCurrentSkill(final SkillData skill)
	{
		currentSkill = skill;
	}

	@Override
	protected StatsSet getStatsSet()
	{
		return currentSkill.sets[currentSkill.currentLevel];
	}

	protected List<Skill> getSkills()
	{
		return skillsInFile;
	}

	@Override
	protected Object getTableValue(final String name)
	{
		try
		{
			usedTables.add(name);
			final Object[] a = tables.get(name);
			if(a.length - 1 >= currentSkill.currentLevel)
				return a[currentSkill.currentLevel];
			return a[a.length - 1];
		}
		catch(RuntimeException e)
		{
			DocumentBase._log.error("error in table " + name + " of skill Id " + currentSkill.id, e);
			return 0;
		}
	}

	@Override
	protected Object getTableValue(final String name, int idx)
	{
		--idx;
		try
		{
			usedTables.add(name);
			final Object[] a = tables.get(name);
			if(a.length - 1 >= idx)
				return a[idx];
			return a[a.length - 1];
		}
		catch(RuntimeException e)
		{
			DocumentBase._log.error("wrong level count in skill Id " + currentSkill.id + " table " + name + " level " + idx, e);
			return 0;
		}
	}

	@Override
	protected void parseDocument(final Document doc)
	{
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if("skill".equalsIgnoreCase(d.getNodeName()))
					{
						setCurrentSkill(new SkillData());
						parseSkill(d);
						skillsInFile.addAll(currentSkill.skills);
						resetTable();
					}
			}
			else if("skill".equalsIgnoreCase(n.getNodeName()))
			{
				setCurrentSkill(new SkillData());
				parseSkill(n);
				skillsInFile.addAll(currentSkill.skills);
			}
	}

	protected void parseSkill(Node n)
	{
		final NamedNodeMap attrs = n.getAttributes();
		final int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
		final String skillName = attrs.getNamedItem("name").getNodeValue();
		final String levels = attrs.getNamedItem("levels").getNodeValue();
		final int lastLvl = Integer.parseInt(levels);
		currentSkill.id = skillId;
		currentSkill.name = skillName;
		currentSkill.sets = new StatsSet[lastLvl];
		for(int i = 0; i < lastLvl; ++i)
		{
			(currentSkill.sets[i] = new StatsSet()).set("skill_id", currentSkill.id);
			currentSkill.sets[i].set("level", convertLvl(skillId, i) + 1);
			currentSkill.sets[i].set("name", currentSkill.name);
		}
		if(currentSkill.sets.length != lastLvl)
			throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + lastLvl + " levels expected");
		Node first;
		for(first = n = n.getFirstChild(); n != null; n = n.getNextSibling())
			if("table".equalsIgnoreCase(n.getNodeName()))
				parseTable(n, skillId);
		for(int j = 1; j <= lastLvl; ++j)
			for(n = first; n != null; n = n.getNextSibling())
				if("set".equalsIgnoreCase(n.getNodeName()))
					parseBeanSet(n, currentSkill.sets[j - 1], j);
		int level;
		for(n = first; n != null; n = n.getNextSibling())
			if("extractlist".equalsIgnoreCase(n.getNodeName()))
			{
				level = Integer.parseInt(n.getAttributes().getNamedItem("level").getNodeValue());
				parseExtract(n, currentSkill.sets[level - 1]);
			}
		makeSkills();
		for(int j = 0; j < lastLvl; ++j)
		{
			currentSkill.currentLevel = j;
			Condition condition;
			Node msg;
			Node msgAttribute;
			int msgId;
			for(n = first; n != null; n = n.getNextSibling())
			{
				if("cond".equalsIgnoreCase(n.getNodeName()))
				{
					condition = parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(j));
					if(condition != null)
					{
						msg = n.getAttributes().getNamedItem("msg");
						msgAttribute = n.getAttributes().getNamedItem("msgId");
						if(msg != null)
							condition.setMessage(msg.getNodeValue());
						else if(msgAttribute != null)
						{
							msgId = parseNumber(msgAttribute.getNodeValue()).intValue();
							condition.setSystemMsg(msgId);
						}
						currentSkill.currentSkills.get(j).attachFunc(condition);
					}
				}
				else if("for".equalsIgnoreCase(n.getNodeName()))
					parseTemplate(n, currentSkill.currentSkills.get(j));
				else if("triggers".equalsIgnoreCase(n.getNodeName()))
					parseTriggers(n, currentSkill.currentSkills.get(j));
			}
		}
		currentSkill.skills.addAll(currentSkill.currentSkills);
	}

	protected void parseTable(Node n, int skillId)
	{
		final NamedNodeMap attrs = n.getAttributes();
		final String name = attrs.getNamedItem("name").getNodeValue();
		if(name.charAt(0) != '#')
		{
			_log.warn("Error while parse table[" + name + "] value for skill ID[" + skillId + "] (Table name must start with #)!");
			return;
		}

		if(name.lastIndexOf('#') != 0)
		{
			_log.warn("Error while parse table[" + name + "] value for skill ID[" + skillId + "] (Table name should not contain # character, but only start with #)!");
			return;
		}

		if(name.contains(";") || name.contains(":") || name.contains(" ") || name.contains("-"))
		{
			_log.warn("Error while parse table[" + name + "] value for skill ID[" + skillId + "] (Table name should not contain characters: ';' ':' '-' or space)!");
			return;
		}

		final StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
		final ArrayList<String> array = new ArrayList<String>();
		while(data.hasMoreTokens())
			array.add(data.nextToken());
		final Object[] res = new Object[array.size()];
		for(int i = 0; i < array.size(); ++i)
			res[i] = parseNumber(array.get(i));
		setTable(name, res);
	}

	protected void parseExtract(Node n, final StatsSet set)
	{
		final List<Extract.ExtractGroup> list = new ArrayList<Extract.ExtractGroup>();
		NamedNodeMap map;
		double chance;
		Extract.ExtractGroup g;
		Node n2;
		int itemId;
		int count;
		for(n = n.getFirstChild(); n != null; n = n.getNextSibling())
			if("group".equalsIgnoreCase(n.getNodeName()))
			{
				map = n.getAttributes();
				chance = Double.parseDouble(map.getNamedItem("chance").getNodeValue());
				g = new Extract.ExtractGroup(chance);
				list.add(g);
				for(n2 = n.getFirstChild(); n2 != null; n2 = n2.getNextSibling())
					if("extract".equalsIgnoreCase(n2.getNodeName()))
					{
						map = n2.getAttributes();
						itemId = Integer.parseInt(map.getNamedItem("item_id").getNodeValue());
						count = Integer.parseInt(map.getNamedItem("count").getNodeValue());
						g.add(new Extract.ExtractItem(itemId, count));
					}
			}
		set.set("extractlist", list);
	}

	private void makeSkills()
	{
		currentSkill.currentSkills = new ArrayList<Skill>(currentSkill.sets.length);
		for(int i = 0; i < currentSkill.sets.length; ++i)
			currentSkill.currentSkills.add(i, (currentSkill.sets[i].getEnum("skillType", Skill.SkillType.class)).makeSkill(currentSkill.sets[i]));
	}

	private static int convertLvl(final int id, final int lvl)
	{
		if(!SkillTree._baseLevels.containsKey(id))
			return lvl;
		final int diff = lvl - SkillTree._baseLevels.get(id);
		if(diff < 0)
			return lvl;
		return diff < 30 ? diff + 100 : diff + 110;
	}

	public class SkillData
	{
		public int id;
		public String name;
		public StatsSet[] sets;
		public int currentLevel;
		public ArrayList<Skill> skills;
		public ArrayList<Skill> currentSkills;

		public SkillData()
		{
			skills = new ArrayList<Skill>();
			currentSkills = new ArrayList<Skill>();
		}
	}
}
