package l2s.gameserver.tables;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.DocumentFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.model.base.PledgeSkillLearn;

public class SkillTree
{
	private static Logger _log;
	private static Map<ClassId, Map<Integer, SkillLearn>> _skillTrees;
	public static Map<Integer, Integer> _baseLevels;
	private List<SkillLearn> _fishingSkillTrees;
	private List<SkillLearn> _expandDwarvenCraftSkillTrees;
	private List<PledgeSkillLearn> _pledgeSkillTrees;
	private List<EnchantSkillLearn> _enchantSkillTrees;

	public static SkillTree getInstance()
	{
		return SingletonHolder._instance;
	}

	private SkillTree()
	{
		load();
	}

	public void addSkillsToSkillTrees(final List<SkillLearn> skills, final int classId, final int parentId)
	{
		if(skills == null || skills.isEmpty())
			return;
		final Map<Integer, SkillLearn> tmp = new HashMap<Integer, SkillLearn>();
		if(parentId > -1)
		{
			final Map<Integer, SkillLearn> parent = SkillTree._skillTrees.get(ClassId.values()[parentId]);
			if(parent != null)
				for(final SkillLearn skillLearn : parent.values())
					if(skillLearn != null)
						tmp.put(SkillTable.getSkillHashCode(skillLearn.getId(), skillLearn.getLevel()), skillLearn);
		}
		for(final SkillLearn skillLearn2 : skills)
			if(skillLearn2 != null)
				tmp.put(SkillTable.getSkillHashCode(skillLearn2.getId(), skillLearn2.getLevel()), skillLearn2);
		if(!tmp.isEmpty())
			SkillTree._skillTrees.put(ClassId.values()[classId], tmp);
	}

	public EnchantSkillLearn[] getAvailableEnchantSkills(final Player cha)
	{
		final List<EnchantSkillLearn> result = new ArrayList<EnchantSkillLearn>();
		final List<EnchantSkillLearn> skills = new ArrayList<EnchantSkillLearn>();
		skills.addAll(_enchantSkillTrees);
		final Skill[] oldSkills = cha.getAllSkillsArray();
		for(final EnchantSkillLearn e : skills)
			if(76 <= cha.getLevel())
			{
				boolean knownSkill = false;
				for(int j = 0; j < oldSkills.length && !knownSkill; ++j)
					if(oldSkills[j].getId() == e.getId())
					{
						knownSkill = true;
						if(oldSkills[j].getLevel() == e.getMinSkillLevel())
							result.add(e);
					}
			}
		return result.toArray(new EnchantSkillLearn[result.size()]);
	}

	public PledgeSkillLearn[] getAvailablePledgeSkills(final Player cha)
	{
		final List<PledgeSkillLearn> result = new ArrayList<PledgeSkillLearn>();
		final List<PledgeSkillLearn> skills = _pledgeSkillTrees;
		if(skills == null)
		{
			SkillTree._log.warn("No clan skills defined!");
			return new PledgeSkillLearn[0];
		}
		final Skill[] oldSkills = cha.getClan().getAllSkills();
		for(final PledgeSkillLearn temp : skills)
			if(temp.getBaseLevel() <= cha.getClan().getLevel())
			{
				boolean knownSkill = false;
				for(int j = 0; j < oldSkills.length && !knownSkill; ++j)
					if(oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						if(oldSkills[j].getLevel() == temp.getLevel() - 1)
							result.add(temp);
					}
				if(knownSkill || temp.getLevel() != 1)
					continue;
				result.add(temp);
			}
		return result.toArray(new PledgeSkillLearn[result.size()]);
	}

	public HashMap<Integer, Integer> getPledgeSkillsMax(final Player cha)
	{
		final HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		final List<PledgeSkillLearn> skills = _pledgeSkillTrees;
		if(skills == null)
		{
			SkillTree._log.warn("No clan skills defined!");
			return result;
		}
		final Skill[] oldSkills = cha.getClan().getAllSkills();
		for(final PledgeSkillLearn s : skills)
		{
			boolean knownSkill = false;
			for(int j = 0; j < oldSkills.length && !knownSkill; ++j)
				if(oldSkills[j].getId() == s.getId())
				{
					knownSkill = true;
					if(oldSkills[j].getLevel() < s.getLevel())
						if(!result.containsKey(s.getId()))
							result.put(s.getId(), s.getLevel());
						else if(result.get(s.getId()) < s.getLevel())
						{
							result.remove(s.getId());
							result.put(s.getId(), s.getLevel());
						}
				}
			if(!knownSkill)
				if(!result.containsKey(s.getId()))
					result.put(s.getId(), s.getLevel());
				else
				{
					if(result.get(s.getId()) >= s.getLevel())
						continue;
					result.remove(s.getId());
					result.put(s.getId(), s.getLevel());
				}
		}
		return result;
	}

	public SkillLearn[] getAvailableSkills(final Player cha)
	{
		final List<SkillLearn> result = new ArrayList<SkillLearn>();
		final List<SkillLearn> skills = new ArrayList<SkillLearn>();
		skills.addAll(_fishingSkillTrees);
		if(cha.getSkillLevel(172) >= 1)
			skills.addAll(_expandDwarvenCraftSkillTrees);
		final Skill[] oldSkills = cha.getAllSkillsArray();
		for(final SkillLearn temp : skills)
			if(temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;
				for(int j = 0; j < oldSkills.length && !knownSkill; ++j)
					if(oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						if(oldSkills[j].getLevel() == temp.getLevel() - 1)
							result.add(temp);
					}
				if(knownSkill || temp.getLevel() != 1)
					continue;
				result.add(temp);
			}
		return result.toArray(new SkillLearn[result.size()]);
	}

	public SkillLearn[] getAvailableSkills(final Player cha, final ClassId classId)
	{
		final List<SkillLearn> result = new ArrayList<SkillLearn>();
		final Collection<SkillLearn> skills = SkillTree._skillTrees.get(classId).values();
		if(skills == null)
		{
			SkillTree._log.warn("Skilltree for class " + classId + " is not defined!");
			return new SkillLearn[0];
		}
		final Skill[] oldSkills = cha.getAllSkillsArray();
		for(final SkillLearn temp : skills)
			if(temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;
				for(int j = 0; j < oldSkills.length && !knownSkill; ++j)
					if(oldSkills[j] != null && oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						if(oldSkills[j].getLevel() == temp.getLevel() - 1)
							result.add(temp);
					}
				if(knownSkill || temp.getLevel() != 1)
					continue;
				result.add(temp);
			}
		return result.toArray(new SkillLearn[result.size()]);
	}

	public int getMinLevelForNewSkill(final Player cha, final ClassId classId)
	{
		int minLevel = 0;
		final Collection<SkillLearn> skills = SkillTree._skillTrees.get(classId).values();
		if(skills == null)
		{
			SkillTree._log.warn("Skilltree for class " + classId + " is not defined !");
			return minLevel;
		}
		for(final SkillLearn temp : skills)
			if(temp.getMinLevel() > cha.getLevel() && temp.getSpCost() != 0 && (minLevel == 0 || temp.getMinLevel() < minLevel))
				minLevel = temp.getMinLevel();
		return minLevel;
	}

	public static int getMinSkillLevel(final int skillId, final ClassId classId, final int skillLvl)
	{
		final Map<Integer, SkillLearn> map = SkillTree._skillTrees.get(classId);
		final int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);
		if(map.containsKey(skillHashCode))
			return map.get(skillHashCode).getMinLevel();
		return 0;
	}

	public int getMinSkillLevel(final int skillId, final int skillLvl)
	{
		final int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);
		for(final Map<Integer, SkillLearn> map : SkillTree._skillTrees.values())
			if(map.containsKey(skillHashCode))
				return map.get(skillHashCode).getMinLevel();
		return 0;
	}

	public int getSkillCost(final Player player, final Skill skill)
	{
		int skillCost = 100000000;
		final ClassId classId = player.getSkillLearningClassId();
		final int skillHashCode = SkillTable.getSkillHashCode(skill);
		if(SkillTree._skillTrees.get(classId).containsKey(skillHashCode))
		{
			final SkillLearn skillLearn = SkillTree._skillTrees.get(classId).get(skillHashCode);
			if(skillLearn.getMinLevel() <= player.getLevel())
			{
				skillCost = skillLearn.getSpCost();
				if(!player.getClassId().equalsOrChildOf(classId))
					return skillCost;
			}
		}
		return skillCost;
	}

	public int getSkillExpCost(final Player player, final Skill skill)
	{
		final EnchantSkillLearn[] availableEnchantSkills;
		final EnchantSkillLearn[] enchantSkillLearnList = availableEnchantSkills = getAvailableEnchantSkills(player);
		for(final EnchantSkillLearn enchantSkillLearn : availableEnchantSkills)
			if(enchantSkillLearn.getId() == skill.getId())
				if(enchantSkillLearn.getLevel() == skill.getLevel())
					return enchantSkillLearn.getExp();
		return 1000000000;
	}

	public int getSkillRate(final Player player, final Skill skill)
	{
		final EnchantSkillLearn[] availableEnchantSkills;
		final EnchantSkillLearn[] enchantSkillLearnList = availableEnchantSkills = getAvailableEnchantSkills(player);
		for(final EnchantSkillLearn enchantSkillLearn : availableEnchantSkills)
			if(enchantSkillLearn.getId() == skill.getId())
				if(enchantSkillLearn.getLevel() == skill.getLevel())
					return enchantSkillLearn.getRate(player);
		return 0;
	}

	public int getSkillSpCost(final Player player, final Skill skill)
	{
		final EnchantSkillLearn[] availableEnchantSkills;
		final EnchantSkillLearn[] enchantSkillLearnList = availableEnchantSkills = getAvailableEnchantSkills(player);
		for(final EnchantSkillLearn enchantSkillLearn : availableEnchantSkills)
			if(enchantSkillLearn.getId() == skill.getId())
				if(enchantSkillLearn.getLevel() == skill.getLevel())
					return enchantSkillLearn.getSpCost();
		return 100000000;
	}

	public static Map<ClassId, Map<Integer, SkillLearn>> getSkillTrees()
	{
		return SkillTree._skillTrees;
	}

	public boolean isSkillPossible(final Player player, final int skillid, final int level)
	{
		final Map<Integer, SkillLearn> skills = SkillTree._skillTrees.get(ClassId.values()[player.getActiveClassId()]);
		for(final SkillLearn skilllearn : skills.values())
			if(skilllearn.getId() == skillid && skilllearn.getLevel() <= level)
				return true;
		return false;
	}

	private void load()
	{
		try
		{
			_fishingSkillTrees = new ArrayList<SkillLearn>();
			_expandDwarvenCraftSkillTrees = new ArrayList<SkillLearn>();
			final File f = new File(Config.DATAPACK_ROOT, "data/pts/skillstrees/fishing_skills_tree.xml");
			final Document doc = DocumentFactory.getInstance().loadDocument(f);
			final Node n = doc.getFirstChild();
			for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				if("skill".equalsIgnoreCase(d.getNodeName()))
				{
					final NamedNodeMap attrs = d.getAttributes();
					final int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					final int skillLvl = Integer.parseInt(attrs.getNamedItem("lvl").getNodeValue());
					final int minLvl = Integer.parseInt(attrs.getNamedItem("minLvl").getNodeValue());
					final int itemId = Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue());
					final int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
					final boolean isDwarf = Boolean.parseBoolean(attrs.getNamedItem("isDwarf").getNodeValue());
					final SkillLearn skill = new SkillLearn(skillId, skillLvl, minLvl, 0, itemId, count);
					if(isDwarf)
						_expandDwarvenCraftSkillTrees.add(skill);
					else
						_fishingSkillTrees.add(skill);
				}
		}
		catch(Exception e)
		{
			SkillTree._log.warn("FishingTable: Error while loading fishing skills: " + e);
		}
		try
		{
			_enchantSkillTrees = new ArrayList<EnchantSkillLearn>();
			final File f = new File(Config.DATAPACK_ROOT, "data/pts/skillstrees/enchant_skills_tree.xml");
			final Document doc = DocumentFactory.getInstance().loadDocument(f);
			final Node n = doc.getFirstChild();
			for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				if("enchant".equalsIgnoreCase(d.getNodeName()))
				{
					int minSkillLvl = 0;
					final int id = Integer.valueOf(d.getAttributes().getNamedItem("id").getNodeValue());
					final String name = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
					final int baseLvl = Integer.valueOf(d.getAttributes().getNamedItem("base_lvl").getNodeValue());
					for(Node t = d.getFirstChild(); t != null; t = t.getNextSibling())
						if("data".equalsIgnoreCase(t.getNodeName()))
						{
							final NamedNodeMap attrs2 = t.getAttributes();
							final int lvl = Integer.valueOf(attrs2.getNamedItem("level").getNodeValue());
							final int sp = Integer.valueOf(attrs2.getNamedItem("sp").getNodeValue());
							final int exp = Integer.valueOf(attrs2.getNamedItem("exp").getNodeValue());
							final byte rate76 = Byte.valueOf(attrs2.getNamedItem("rate_76").getNodeValue());
							final byte rate77 = Byte.valueOf(attrs2.getNamedItem("rate_77").getNodeValue());
							final byte rate78 = Byte.valueOf(attrs2.getNamedItem("rate_78").getNodeValue());
							if(lvl == 101 || lvl == 141)
								minSkillLvl = baseLvl;
							else
								minSkillLvl = lvl - 1;
							final EnchantSkillLearn skill2 = new EnchantSkillLearn(id, lvl, minSkillLvl, baseLvl, name, sp, exp, rate76, rate77, rate78);
							_enchantSkillTrees.add(skill2);
						}
					if(!SkillTree._baseLevels.containsKey(id))
						SkillTree._baseLevels.put(id, baseLvl);
				}
		}
		catch(Exception e)
		{
			SkillTree._log.warn("EnchantSkillTable: Error while loading enchant skills tree: " + e);
		}
		try
		{
			_pledgeSkillTrees = new ArrayList<PledgeSkillLearn>();
			final File f = new File(Config.DATAPACK_ROOT, "data/pts/skillstrees/pledge_skills_tree.xml");
			final Document doc = DocumentFactory.getInstance().loadDocument(f);
			final Node n = doc.getFirstChild();
			for(Node nb = n.getFirstChild(); nb != null; nb = nb.getNextSibling())
				if("clan".equals(nb.getNodeName()))
				{
					final NamedNodeMap nbAttr = nb.getAttributes();
					final int clanLvl = Integer.parseInt(nbAttr.getNamedItem("lvl").getNodeValue());
					for(Node nc = nb.getFirstChild(); nc != null; nc = nc.getNextSibling())
						if("skill".equals(nc.getNodeName()))
						{
							final NamedNodeMap attr = nc.getAttributes();
							final int skillId2 = Integer.parseInt(attr.getNamedItem("id").getNodeValue());
							final int skillLvl2 = Integer.parseInt(attr.getNamedItem("lvl").getNodeValue());
							final int repCost = Integer.parseInt(attr.getNamedItem("repCost").getNodeValue());
							final int itemId2 = Integer.parseInt(attr.getNamedItem("itemId").getNodeValue());
							final PledgeSkillLearn skill3 = new PledgeSkillLearn(skillId2, skillLvl2, clanLvl, repCost, itemId2);
							_pledgeSkillTrees.add(skill3);
						}
				}
		}
		catch(Exception e)
		{
			SkillTree._log.warn("PledgeTable: Error while loading pledge skills: " + e);
		}
		SkillTree._log.info("FishingSkillTree: Loaded " + _fishingSkillTrees.size() + " general skills.");
		SkillTree._log.info("DwarvenCraftSkillTree: Loaded " + _expandDwarvenCraftSkillTrees.size() + " dwarven skills.");
		SkillTree._log.info("EnchantSkillTree: Loaded " + _enchantSkillTrees.size() + " enchant skills.");
		SkillTree._log.info("PledgeSkillTree: Loaded " + _pledgeSkillTrees.size() + " pledge skills.");
	}

	static
	{
		SkillTree._log = LoggerFactory.getLogger(SkillTree.class);
		SkillTree._skillTrees = new HashMap<ClassId, Map<Integer, SkillLearn>>();
		SkillTree._baseLevels = new HashMap<Integer, Integer>();
	}

	private static class SingletonHolder
	{
		protected static final SkillTree _instance = new SkillTree();
	}
}
