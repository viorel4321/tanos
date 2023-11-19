package l2s.gameserver.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.utils.PositionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2s.gameserver.Config;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.conditions.Condition;
import l2s.gameserver.skills.conditions.ConditionForceBuff;
import l2s.gameserver.skills.conditions.ConditionGameTime;
import l2s.gameserver.skills.conditions.ConditionHasSkill;
import l2s.gameserver.skills.conditions.ConditionLogicAnd;
import l2s.gameserver.skills.conditions.ConditionLogicNot;
import l2s.gameserver.skills.conditions.ConditionLogicOr;
import l2s.gameserver.skills.conditions.ConditionPlayer;
import l2s.gameserver.skills.conditions.ConditionPlayerClasses;
import l2s.gameserver.skills.conditions.ConditionPlayerCubic;
import l2s.gameserver.skills.conditions.ConditionPlayerEncumbered;
import l2s.gameserver.skills.conditions.ConditionPlayerHasCastle;
import l2s.gameserver.skills.conditions.ConditionPlayerHasCastleId;
import l2s.gameserver.skills.conditions.ConditionPlayerMaxLevel;
import l2s.gameserver.skills.conditions.ConditionPlayerMaxPK;
import l2s.gameserver.skills.conditions.ConditionPlayerMinHp;
import l2s.gameserver.skills.conditions.ConditionPlayerMinLevel;
import l2s.gameserver.skills.conditions.ConditionPlayerOlympiad;
import l2s.gameserver.skills.conditions.ConditionPlayerPercentHp;
import l2s.gameserver.skills.conditions.ConditionPlayerPercentMp;
import l2s.gameserver.skills.conditions.ConditionPlayerRace;
import l2s.gameserver.skills.conditions.ConditionPlayerRiding;
import l2s.gameserver.skills.conditions.ConditionPlayerState;
import l2s.gameserver.skills.conditions.ConditionPlayerSummonSiegeGolem;
import l2s.gameserver.skills.conditions.ConditionPlayerTeam;
import l2s.gameserver.skills.conditions.ConditionSlotItemId;
import l2s.gameserver.skills.conditions.ConditionTargetAggro;
import l2s.gameserver.skills.conditions.ConditionTargetCastleDoor;
import l2s.gameserver.skills.conditions.ConditionTargetDirection;
import l2s.gameserver.skills.conditions.ConditionTargetHasBuffId;
import l2s.gameserver.skills.conditions.ConditionTargetMob;
import l2s.gameserver.skills.conditions.ConditionTargetMobId;
import l2s.gameserver.skills.conditions.ConditionTargetNpcClass;
import l2s.gameserver.skills.conditions.ConditionTargetPercentHp;
import l2s.gameserver.skills.conditions.ConditionTargetPlayable;
import l2s.gameserver.skills.conditions.ConditionTargetPlayerRace;
import l2s.gameserver.skills.conditions.ConditionTargetRace;
import l2s.gameserver.skills.conditions.ConditionTargetSelf;
import l2s.gameserver.skills.conditions.ConditionUsingArmor;
import l2s.gameserver.skills.conditions.ConditionUsingItemType;
import l2s.gameserver.skills.conditions.ConditionUsingSkill;
import l2s.gameserver.skills.conditions.ConditionZone;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.skills.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.ArmorTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Util;

abstract class DocumentBase
{
	static Logger _log;
	private File file;
	protected HashMap<String, Object[]> tables;

	DocumentBase(final File file)
	{
		this.file = file;
		tables = new HashMap<String, Object[]>();
	}

	Document parse()
	{
		Document doc;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch(Exception e)
		{
			DocumentBase._log.error("Error loading file " + file, e);
			return null;
		}
		try
		{
			parseDocument(doc);
		}
		catch(Exception e)
		{
			DocumentBase._log.error("Error in file " + file, e);
			return null;
		}
		return doc;
	}

	protected abstract void parseDocument(final Document p0);

	protected abstract StatsSet getStatsSet();

	protected abstract Object getTableValue(final String p0);

	protected abstract Object getTableValue(final String p0, final int p1);

	protected void resetTable()
	{
		tables = new HashMap<String, Object[]>();
	}

	protected void setTable(final String name, final Object[] table)
	{
		tables.put(name, table);
	}

	protected void parseTemplate(Node n, StatTemplate template)
	{
		n = n.getFirstChild();
		if(n == null)
			return;
		while(n != null)
		{
			final String nodeName = n.getNodeName();
			if("add".equalsIgnoreCase(nodeName))
				attachFunc(n, template, "Add");
			else if("sub".equalsIgnoreCase(nodeName))
				attachFunc(n, template, "Sub");
			else if("mul".equalsIgnoreCase(nodeName))
				attachFunc(n, template, "Mul");
			else if("div".equalsIgnoreCase(nodeName))
				attachFunc(n, template, "Div");
			else if("set".equalsIgnoreCase(nodeName))
				attachFunc(n, template, "Set");
			else if("enchant".equalsIgnoreCase(nodeName))
				attachFunc(n, template, "Enchant");
			else if("enchantadd".equalsIgnoreCase(nodeName))
				attachFunc(n, template, "EnchantAdd");
			else if("effect".equalsIgnoreCase(nodeName))
			{
				if(template instanceof EffectTemplate)
					throw new RuntimeException("Nested effects");
				attachEffect(n, template);
			}
			else if(template instanceof EffectTemplate)
			{
				final Condition cond = parseCondition(n.getFirstChild(), template);
				if(cond != null)
					((EffectTemplate) template).attachCond(cond);
			}
			n = n.getNextSibling();
		}
	}

	protected void parseTriggers(Node n, StatTemplate triggerable)
	{
		n = n.getFirstChild();
		if(n == null)
			return;
		while(n != null)
		{
			if("trigger".equalsIgnoreCase(n.getNodeName())) {
				int id = parseNumber(n.getAttributes().getNamedItem("id").getNodeValue()).intValue();
				int level = parseNumber(n.getAttributes().getNamedItem("level").getNodeValue()).intValue();

				if (id <= 0 || level <= 0)
					continue;

				TriggerType t = TriggerType.valueOf(parseString(n.getAttributes().getNamedItem("type").getNodeValue()));

				double chance = 100D;
				if (n.getAttributes().getNamedItem("chance") != null)
					chance = parseNumber(n.getAttributes().getNamedItem("chance").getNodeValue()).doubleValue();

				boolean increasing = false;
				if (n.getAttributes().getNamedItem("increasing") != null)
					increasing = parseBoolean(n.getAttributes().getNamedItem("increasing").getNodeValue());

				int delay = 0;
				if (n.getAttributes().getNamedItem("delay") != null)
					delay = parseNumber(n.getAttributes().getNamedItem("delay").getNodeValue()).intValue() * 1000;

				boolean cancel = false;
				if (n.getAttributes().getNamedItem("cancel_effects_on_remove") != null)
					cancel = parseBoolean(n.getAttributes().getNamedItem("cancel_effects_on_remove").getNodeValue());

				String args = "";
				if (n.getAttributes().getNamedItem("args") != null)
					args = n.getAttributes().getNamedItem("args").getNodeValue();

				TriggerInfo trigger = new TriggerInfo(id, level, t, chance, increasing, delay, cancel, args);

				Condition condition = parseCondition(n.getFirstChild(), trigger);
				if (condition != null)
					trigger.addCondition(condition);

				triggerable.addTrigger(trigger);
			}
			n = n.getNextSibling();
		}
	}

	protected void attachFunc(final Node n, final Object template, final String name)
	{
		final Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
		final String order = n.getAttributes().getNamedItem("order").getNodeValue();
		final int ord = parseNumber(order).intValue();
		final Condition applyCond = parseCondition(n.getFirstChild(), template);
		double val = 0.0;
		if(n.getAttributes().getNamedItem("val") != null)
			val = parseNumber(n.getAttributes().getNamedItem("val").getNodeValue()).doubleValue();
		else if(n.getAttributes().getNamedItem("value") != null)
			val = parseNumber(n.getAttributes().getNamedItem("value").getNodeValue()).doubleValue();
		final FuncTemplate ft = new FuncTemplate(applyCond, name, stat, ord, val);
		if(template instanceof StatTemplate)
			((StatTemplate) template).attachFunc(ft);
	}

	protected void attachEffect(final Node n, final Object template)
	{
		final NamedNodeMap attrs = n.getAttributes();
		final StatsSet set = new StatsSet();
		set.set("name", attrs.getNamedItem("name").getNodeValue());
		if(attrs.getNamedItem("count") != null)
			set.set("count", parseNumber(attrs.getNamedItem("count").getNodeValue()).intValue());
		if(attrs.getNamedItem("time") != null)
		{
			double times = parseNumber(attrs.getNamedItem("time").getNodeValue()).doubleValue();
			if(Config.ENABLE_MODIFY_SKILL_DURATION && times > 0.0 && Config.SKILL_DURATION_LIST.containsKey(((Skill) template).getId()))
				if(((Skill) template).getLevel() < 101 || ((Skill) template).getLevel() > 140)
					times = Config.SKILL_DURATION_LIST.get(((Skill) template).getId());
				else
					times += Config.SKILL_DURATION_LIST.get(((Skill) template).getId());
			set.set("time", times);
		}
		set.set("value", attrs.getNamedItem("val") != null ? parseNumber(attrs.getNamedItem("val").getNodeValue()).doubleValue() : 0.0);
		set.set("abnormal", AbnormalEffect.NULL);
		if(attrs.getNamedItem("abnormal") != null)
			set.set("abnormal", AbnormalEffect.getByName(attrs.getNamedItem("abnormal").getNodeValue()));
		if(attrs.getNamedItem("stackType") != null)
			set.set("stackType", attrs.getNamedItem("stackType").getNodeValue());
		if(attrs.getNamedItem("stackType2") != null)
			set.set("stackType2", attrs.getNamedItem("stackType2").getNodeValue());
		if(attrs.getNamedItem("stackOrder") != null)
			set.set("stackOrder", parseNumber(attrs.getNamedItem("stackOrder").getNodeValue()).doubleValue());
		if(attrs.getNamedItem("applyOnCaster") != null)
			set.set("applyOnCaster", Boolean.valueOf(attrs.getNamedItem("applyOnCaster").getNodeValue()));
		if(attrs.getNamedItem("displayId") != null)
			set.set("displayId", parseNumber(attrs.getNamedItem("displayId").getNodeValue()).intValue());
		if(attrs.getNamedItem("displayLevel") != null)
			set.set("displayLevel", parseNumber(attrs.getNamedItem("displayLevel").getNodeValue()).intValue());
		if(attrs.getNamedItem("chance") != null)
			set.set("chance", parseNumber(attrs.getNamedItem("chance").getNodeValue()).intValue());
		if(attrs.getNamedItem("isReflectable") != null)
			set.set("isReflectable", Boolean.valueOf(attrs.getNamedItem("isReflectable").getNodeValue()));
		if(attrs.getNamedItem("immuneResists") != null)
			set.set("immuneResists", Boolean.valueOf(attrs.getNamedItem("immuneResists").getNodeValue()));
		if(attrs.getNamedItem("isOffensive") != null)
			set.set("isOffensive", Boolean.valueOf(attrs.getNamedItem("isOffensive").getNodeValue()));
		if(attrs.getNamedItem("cubicId") != null)
			set.set("cubicId", parseNumber(attrs.getNamedItem("cubicId").getNodeValue()).intValue());
		if(attrs.getNamedItem("cubicLevel") != null)
			set.set("cubicLevel", parseNumber(attrs.getNamedItem("cubicLevel").getNodeValue()).intValue());
		final EffectTemplate lt = new EffectTemplate(set);
		parseTemplate(n, lt);
		if(template instanceof Skill)
			((Skill) template).attachFunc(lt);
	}

	protected Condition parseCondition(Node n, final Object template)
	{
		while(n != null && n.getNodeType() != 1)
			n = n.getNextSibling();
		if(n == null)
			return null;
		if("and".equalsIgnoreCase(n.getNodeName()))
			return parseLogicAnd(n, template);
		if("or".equalsIgnoreCase(n.getNodeName()))
			return parseLogicOr(n, template);
		if("not".equalsIgnoreCase(n.getNodeName()))
			return parseLogicNot(n, template);
		if("player".equalsIgnoreCase(n.getNodeName()))
			return parsePlayerCondition(n);
		if("target".equalsIgnoreCase(n.getNodeName()))
			return parseTargetCondition(n);
		if("has".equalsIgnoreCase(n.getNodeName()))
			return parseHasCondition(n);
		if("using".equalsIgnoreCase(n.getNodeName()))
			return parseUsingCondition(n);
		if("game".equalsIgnoreCase(n.getNodeName()))
			return parseGameCondition(n);
		if("zone".equalsIgnoreCase(n.getNodeName()))
			return parseZoneCondition(n);
		return null;
	}

	protected Condition parseLogicAnd(Node n, final Object template)
	{
		final ConditionLogicAnd cond = new ConditionLogicAnd();
		for(n = n.getFirstChild(); n != null; n = n.getNextSibling())
			if(n.getNodeType() == 1)
				cond.add(parseCondition(n, template));
		if(cond._conditions == null || cond._conditions.length == 0)
			DocumentBase._log.error("Empty <and> condition in " + file);
		return cond;
	}

	protected Condition parseLogicOr(Node n, final Object template)
	{
		final ConditionLogicOr cond = new ConditionLogicOr();
		for(n = n.getFirstChild(); n != null; n = n.getNextSibling())
			if(n.getNodeType() == 1)
				cond.add(parseCondition(n, template));
		if(cond._conditions == null || cond._conditions.length == 0)
			DocumentBase._log.error("Empty <or> condition in " + file);
		return cond;
	}

	protected Condition parseLogicNot(Node n, final Object template)
	{
		for(n = n.getFirstChild(); n != null; n = n.getNextSibling())
			if(n.getNodeType() == 1)
				return new ConditionLogicNot(parseCondition(n, template));
		DocumentBase._log.error("Empty <not> condition in " + file);
		return null;
	}

	protected Condition parsePlayerCondition(final Node n)
	{
		final int[] forces = new int[2];
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for(int i = 0; i < attrs.getLength(); ++i)
		{
			final Node a = attrs.item(i);
			final String nodeName = a.getNodeName();
			if("race".equalsIgnoreCase(nodeName))
				cond = joinAnd(cond, new ConditionPlayerRace(a.getNodeValue()));
			else if("minLevel".equalsIgnoreCase(nodeName))
			{
				final int lvl = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerMinLevel(lvl));
			}
			else if("summon_siege_golem".equalsIgnoreCase(nodeName))
				cond = joinAnd(cond, new ConditionPlayerSummonSiegeGolem());
			else if("maxLevel".equalsIgnoreCase(nodeName))
			{
				final int lvl = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerMaxLevel(lvl));
			}
			else if("maxPK".equalsIgnoreCase(nodeName))
			{
				final int pk = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerMaxPK(pk));
			}
			else if("resting".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, val));
			}
			else if("moving".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.MOVING, val));
			}
			else if("running".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, val));
			}
			else if("standing".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.STANDING, val));
			}
			else if("flying".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FLYING, val));
			}
			else if("olympiad".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerOlympiad(val));
			}
			else if("percentHP".equalsIgnoreCase(nodeName))
			{
				final int percentHP = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerPercentHp(percentHP));
			}
			else if("minHP".equalsIgnoreCase(nodeName))
			{
				final int minHP = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerMinHp(minHP));
			}
			else if("percentMP".equalsIgnoreCase(nodeName))
			{
				final int percentMP = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerPercentMp(percentMP));
			}
			else if("team".equalsIgnoreCase(nodeName))
			{
				final int team = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerTeam(team));
			}
			else if("classes".equalsIgnoreCase(nodeName))
			{
				final int[] classes = Util.parseCommaSeparatedIntegerArray(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerClasses(classes));
			}
			else if("hasCastle".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerHasCastle(val));
			}
			else if("hasCastleId".equalsIgnoreCase(nodeName))
			{
				final int id = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerHasCastleId(id));
			}
			else if("cubic".equalsIgnoreCase(nodeName))
			{
				final int cubicId = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerCubic(cubicId));
			}
			else if("riding".equalsIgnoreCase(nodeName))
			{
				final String riding = a.getNodeValue();
				if("strider".equalsIgnoreCase(riding))
					cond = joinAnd(cond, new ConditionPlayerRiding(ConditionPlayerRiding.CheckPlayerRiding.STRIDER));
				else if("wyvern".equalsIgnoreCase(riding))
					cond = joinAnd(cond, new ConditionPlayerRiding(ConditionPlayerRiding.CheckPlayerRiding.WYVERN));
				else if("none".equalsIgnoreCase(riding))
					cond = joinAnd(cond, new ConditionPlayerRiding(ConditionPlayerRiding.CheckPlayerRiding.NONE));
			}
			else if("encumbered".equalsIgnoreCase(nodeName))
			{
				final String[] st = a.getNodeValue().split(";");
				cond = joinAnd(cond, new ConditionPlayerEncumbered(Integer.parseInt(st[0]), Integer.parseInt(st[1])));
			}
			else if("battle_force".equalsIgnoreCase(a.getNodeName()))
				forces[0] = parseNumber(a.getNodeValue()).intValue();
			else if("spell_force".equalsIgnoreCase(a.getNodeName()))
				forces[1] = parseNumber(a.getNodeValue()).intValue();
		}
		if(forces[0] + forces[1] > 0)
			cond = joinAnd(cond, new ConditionForceBuff(forces));
		if(cond == null)
			DocumentBase._log.error("Unrecognized <player> condition in " + file);
		return cond;
	}

	protected Condition parseTargetCondition(final Node n)
	{
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for(int i = 0; i < attrs.getLength(); ++i)
		{
			final Node a = attrs.item(i);
			final String nodeName = a.getNodeName();
			if("aggro".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetAggro(val));
			}
			else if("pvp".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetPlayable(val));
			}
			else if("self".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetSelf(val));
			}
			else if("mob".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetMob(val));
			}
			else if("mobId".equalsIgnoreCase(nodeName))
				cond = joinAnd(cond, new ConditionTargetMobId(Integer.parseInt(a.getNodeValue())));
			else if("race".equalsIgnoreCase(nodeName))
				cond = joinAnd(cond, new ConditionTargetRace(a.getNodeValue()));
			else if("npc_class".equalsIgnoreCase(nodeName))
				cond = joinAnd(cond, new ConditionTargetNpcClass(a.getNodeValue()));
			else if("playerRace".equalsIgnoreCase(nodeName))
				cond = joinAnd(cond, new ConditionTargetPlayerRace(a.getNodeValue()));
			else if("castledoor".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetCastleDoor(val));
			}
			else if("direction".equalsIgnoreCase(nodeName))
			{
				final String val2 = a.getNodeValue();
				if("behind".equalsIgnoreCase(val2))
					cond = joinAnd(cond, new ConditionTargetDirection(PositionUtils.TargetDirection.BEHIND));
				if("front".equalsIgnoreCase(val2))
					cond = joinAnd(cond, new ConditionTargetDirection(PositionUtils.TargetDirection.FRONT));
				if("side".equalsIgnoreCase(val2))
					cond = joinAnd(cond, new ConditionTargetDirection(PositionUtils.TargetDirection.SIDE));
			}
			else if("percentHP".equalsIgnoreCase(nodeName))
				cond = joinAnd(cond, new ConditionTargetPercentHp(parseNumber(a.getNodeValue()).intValue()));
			else if("hasBuffId".equalsIgnoreCase(nodeName))
			{
				int id = parseNumber(a.getNodeValue().trim()).intValue();
				cond = joinAnd(cond, new ConditionTargetHasBuffId(id, -1));
			}
		}
		if(cond == null)
			DocumentBase._log.error("Unrecognized <target> condition in " + file);
		return cond;
	}

	protected Condition parseUsingCondition(final Node n)
	{
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for(int i = 0; i < attrs.getLength(); ++i)
		{
			final Node a = attrs.item(i);
			final String nodeName = a.getNodeName();
			final String nodeValue = a.getNodeValue();
			if("kind".equalsIgnoreCase(nodeName))
			{
				long mask = 0L;
				final StringTokenizer st = new StringTokenizer(nodeValue, ",");
				loop: while(st.hasMoreTokens())
				{
					final String item = st.nextToken().trim();
					for(final WeaponTemplate.WeaponType wt : WeaponTemplate.WeaponType.values())
					{
						if(wt.toString().equalsIgnoreCase(item))
						{
							mask |= wt.mask();
							continue loop;
						}
					}
					for(final ArmorTemplate.ArmorType at : ArmorTemplate.ArmorType.values())
					{
						if(at.toString().equalsIgnoreCase(item))
						{
							mask |= at.mask();
							continue loop;
						}
					}
					new IllegalArgumentException("Invalid item kind: " + item).printStackTrace();
				}
				if(mask > 0L)
					cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
			else if("armor".equalsIgnoreCase(nodeName))
			{
				final ArmorTemplate.ArmorType armor = ArmorTemplate.ArmorType.valueOf(nodeValue.toUpperCase());
				cond = joinAnd(cond, new ConditionUsingArmor(armor));
			}
			else if("skill".equalsIgnoreCase(nodeName))
			{
				final int id = Integer.parseInt(nodeValue);
				cond = joinAnd(cond, new ConditionUsingSkill(id));
			}
			else if("slotitem".equalsIgnoreCase(nodeName))
			{
				final StringTokenizer st2 = new StringTokenizer(nodeValue, ";");
				final int id2 = Integer.parseInt(st2.nextToken().trim());
				final short slot = Short.parseShort(st2.nextToken().trim());
				int enchant = 0;
				if(st2.hasMoreTokens())
					enchant = Integer.parseInt(st2.nextToken().trim());
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id2, enchant));
			}
			else if("direction".equalsIgnoreCase(nodeName))
			{
				final PositionUtils.TargetDirection Direction = PositionUtils.TargetDirection.valueOf(nodeValue.toUpperCase());
				cond = joinAnd(cond, new ConditionTargetDirection(Direction));
			}
			else if("player".equalsIgnoreCase(nodeName))
			{
				final boolean val = Boolean.valueOf(nodeValue);
				cond = joinAnd(cond, new ConditionPlayer(val));
			}
		}
		if(cond == null)
			DocumentBase._log.error("Unrecognized <using> condition in " + file);
		return cond;
	}

	protected Condition parseHasCondition(final Node n)
	{
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for(int i = 0; i < attrs.getLength(); ++i)
		{
			final Node a = attrs.item(i);
			final String nodeName = a.getNodeName();
			final String nodeValue = a.getNodeValue();
			if("skill".equalsIgnoreCase(nodeName))
			{
				final StringTokenizer st = new StringTokenizer(nodeValue, ";");
				final Integer id = parseNumber(st.nextToken().trim()).intValue();
				final short level = parseNumber(st.nextToken().trim()).shortValue();
				cond = joinAnd(cond, new ConditionHasSkill(id, level));
			}
		}
		if(cond == null)
			DocumentBase._log.error("Unrecognized <has> condition in " + file);
		return cond;
	}

	protected Condition parseGameCondition(final Node n)
	{
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for(int i = 0; i < attrs.getLength(); ++i)
		{
			final Node a = attrs.item(i);
			if("night".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameTime(ConditionGameTime.CheckGameTime.NIGHT, val));
			}
		}
		if(cond == null)
			DocumentBase._log.error("Unrecognized <game> condition in " + file);
		return cond;
	}

	protected Condition parseZoneCondition(final Node n)
	{
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for(int i = 0; i < attrs.getLength(); ++i)
		{
			final Node a = attrs.item(i);
			if("type".equalsIgnoreCase(a.getNodeName()))
				cond = joinAnd(cond, new ConditionZone(a.getNodeValue()));
		}
		if(cond == null)
			DocumentBase._log.error("Unrecognized <zone> condition in " + file);
		return cond;
	}

	protected void parseBeanSet(final Node n, final StatsSet set, final int level)
	{
		try
		{
			final String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
			String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
			final char ch = value.length() == 0 ? ' ' : value.charAt(0);
			if(value.contains("#") && ch != '#')
				for(final String str : value.split("[;: ]+"))
					if(str.charAt(0) == '#')
						value = value.replace(str, String.valueOf(this.getTableValue(str, level)));
			if(ch == '#')
			{
				final Object tableVal = this.getTableValue(value, level);
				final Number parsedVal = parseNumber(tableVal.toString());
				set.set(name, parsedVal == null ? tableVal : String.valueOf(parsedVal));
			}
			else if((Character.isDigit(ch) || ch == '-') && !value.contains(" ") && !value.contains(";"))
				set.set(name, String.valueOf(parseNumber(value)));
			else
				set.set(name, value);
		}
		catch(Exception e)
		{
			System.out.println(n.getAttributes().getNamedItem("name") + " " + set.get("skill_id"));
			e.printStackTrace();
		}
	}

	private final static Pattern TABLE_PATTERN = Pattern.compile("((?!;|:| |-).*?)((;|:| |-)|$)", Pattern.DOTALL);

	protected Object parseValue(Object object)
	{
		if(object == null)
			return null;

		String value = String.valueOf(object);
		if(value.isEmpty())
			return object;

		if(value.contains("#"))
		{
			String temp;
			StringBuilder sb = new StringBuilder();
			Matcher m = TABLE_PATTERN.matcher(value);
			while(m.find())
			{
				temp = m.group(1);
				if(temp == null || temp.isEmpty())
					continue;

				if(temp.charAt(0) == '#')
					sb.append(getTableValue(temp));
				else
					sb.append(temp);

				temp = m.group(2);
				if(temp == null || temp.isEmpty())
					continue;

				sb.append(temp);
			}
			return sb.toString();
		}

		return object;
	}

	protected final String parseString(Object object)
	{
		object = parseValue(object);
		return String.valueOf(object);
	}

	protected final boolean parseBoolean(Object object)
	{
		return Boolean.parseBoolean(parseString(object));
	}

	protected final Number parseNumber(String value, int... arg)
	{
		value = parseString(value);

		try
		{
			if(value.equalsIgnoreCase("max"))
				return Double.POSITIVE_INFINITY;

			if(value.equalsIgnoreCase("min"))
				return Double.NEGATIVE_INFINITY;

			if(value.indexOf('.') == -1)
			{
				int radix = 10;
				if(value.length() > 2 && value.substring(0, 2).equalsIgnoreCase("0x"))
				{
					value = value.substring(2);
					radix = 16;
				}
				return Integer.valueOf(value, radix);
			}
			return Double.valueOf(value);
		}
		catch(NumberFormatException e)
		{
			_log.warn("Error while parsing number: " + value, e);
			return null;
		}
	}

	protected Condition joinAnd(final Condition cond, final Condition c)
	{
		if(cond == null)
			return c;
		if(cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		final ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}

	static
	{
		DocumentBase._log = LoggerFactory.getLogger(DocumentBase.class);
	}
}
