package l2s.gameserver.templates.npc;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.MinionData;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.instances.BoxInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.reward.Drop;
import l2s.gameserver.model.reward.DropData;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.StatsSet;

public final class NpcTemplate extends CreatureTemplate
{
	private static Logger _log;
	private static final HashMap<Integer, Skill> _emptySkills;
	private static final Skill[] _emptySkillArray;
	public final int npcId;
	public String type;
	public String ai_type;
	public String name;
	public String title;
	public final byte level;
	public final int revardExp;
	public final int revardSp;
	public final double expRate;
	public final short aggroRange;
	public int rhand;
	public int lhand;
	public final String factionId;
	public final short factionRange;
	public final double physHitMod;
	public final int physAvoidMod;
	public final int soulshotCount;
	public final int spiritshotCount;
	public final String jClass;
	public int displayId = 0;
	public boolean isDropHerbs = false;
	public ShotsType shots;
	public boolean isRaid;
	public boolean isEpicBoss;
	public boolean isBox;
	private StatsSet _AIParams = null;
	private int race = 0;
	public double rateHp = 1.;
	private Drop _drop = null;
	public int killscount = 0;
	private final List<MinionData> _minions = new ArrayList<MinionData>(0);
	private List<ClassId> _teachInfo = null;
	private Map<QuestEventType, Quest[]> _questEvents;
	private Class<NpcInstance> this_class;
	private HashMap<Integer, Skill> _skills;
	private HashMap<Skill.SkillType, Skill[]> _skillsByType;
	private Skill[] _dam_skills;
	private Skill[] _dot_skills;
	private Skill[] _debuff_skills;
	private Skill[] _buff_skills;
	private Skill[] _stun_skills;
	private Skill[] _heal_skills;
	private List<ItemInstance> _inventory;

	public NpcTemplate(final StatsSet set, final StatsSet AIParams)
	{
		super(set);
		npcId = set.getInteger("npcId");
		displayId = set.getInteger("displayId");
		type = set.getString("type");
		ai_type = set.getString("ai_type");
		name = set.getString("name");
		title = set.getString("title");
		level = (byte) Math.max(1, set.getByte("level"));
		revardExp = set.getInteger("revardExp");
		revardSp = set.getInteger("revardSp");
		expRate = revardExp / level * level;
		aggroRange = set.getShort("aggroRange");
		rhand = set.getInteger("rhand");
		lhand = set.getInteger("lhand");
		jClass = set.getString("jClass", (String) null);
		final String f = set.getString("factionId", (String) null);
		factionId = f == null ? "" : f.intern();
		factionRange = set.getShort("factionRange");
		physHitMod = set.getDouble("physHitMod");
		physAvoidMod = set.getInteger("physAvoidMod");
		soulshotCount = set.getInteger("soulshotCount");
		spiritshotCount = set.getInteger("spiritshotCount");
		isDropHerbs = set.getBool("isDropHerbs");
		shots = set.getEnum("shots", ShotsType.class, ShotsType.NONE);
		_AIParams = AIParams;
		setInstance(type);
	}

	public Class<NpcInstance> getInstanceClass()
	{
		return this_class;
	}

	public Constructor<?> getInstanceConstructor()
	{
		return this_class == null ? null : this_class.getConstructors()[0];
	}

	public boolean isInstanceOf(final Class<?> _class)
	{
		return this_class != null && _class.isAssignableFrom(this_class);
	}

	public NpcInstance getNewInstance()
	{
		try
		{
			return (NpcInstance) getInstanceConstructor().newInstance(IdFactory.getInstance().getNextId(), this);
		}
		catch(Exception e)
		{
			NpcTemplate._log.error("Unable to create instance of NPC " + npcId);
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void setInstance(final String type)
	{
		Class<NpcInstance> classType = null;
		try
		{
			classType = (Class<NpcInstance>) Class.forName("l2s.gameserver.model.instances." + type + "Instance");
		}
		catch(ClassNotFoundException e)
		{
			classType = (Class<NpcInstance>) Scripts.getInstance().getClasses().get("npc.model." + type + "Instance");
		}
		if(classType == null)
			NpcTemplate._log.error("Not found type class for type: " + type + ". NpcId: " + npcId);
		else
			this_class = classType;
		if(this_class.isAnnotationPresent(Deprecated.class))
			NpcTemplate._log.error("Npc type: " + type + ", is deprecated. NpcId: " + npcId);
		isEpicBoss = ArrayUtils.contains(Config.EPICBOSS_IDS, npcId);
		isRaid = isEpicBoss || isInstanceOf(RaidBossInstance.class);
		isBox = isInstanceOf(BoxInstance.class);
	}

	public NpcTemplate(final StatsSet set)
	{
		this(set, null);
	}

	public void addTeachInfo(final ClassId classId)
	{
		if(_teachInfo == null)
			_teachInfo = new ArrayList<ClassId>();
		_teachInfo.add(classId);
	}

	public ClassId[] getTeachInfo()
	{
		if(_teachInfo == null)
			return null;
		return _teachInfo.toArray(new ClassId[_teachInfo.size()]);
	}

	public boolean canTeach(final ClassId classId)
	{
		return _teachInfo != null && _teachInfo.contains(classId);
	}

	public void addDropData(final DropData drop)
	{
		if(_drop == null)
			_drop = new Drop();
		_drop.addData(drop);
	}

	public void addRaidData(final MinionData minion)
	{
		_minions.add(minion);
	}

	public void addSkill(final Skill skill)
	{
		if(_skills == null)
			_skills = new HashMap<Integer, Skill>();
		if(_skillsByType == null)
			_skillsByType = new HashMap<Skill.SkillType, Skill[]>();
		_skills.put(skill.getId(), skill);
		if(skill.isNotUsedByAI() || skill.getTargetType() == Skill.SkillTargetType.TARGET_NONE || skill.getSkillType() == Skill.SkillType.NOTDONE || !skill.isActive())
			return;
		Skill[] skilllist;
		if(_skillsByType.get(skill.getSkillType()) != null)
		{
			skilllist = new Skill[_skillsByType.get(skill.getSkillType()).length + 1];
			System.arraycopy(_skillsByType.get(skill.getSkillType()), 0, skilllist, 0, _skillsByType.get(skill.getSkillType()).length);
		}
		else
			skilllist = new Skill[] { null };
		skilllist[skilllist.length - 1] = skill;
		_skillsByType.put(skill.getSkillType(), skilllist);
	}

	public Skill[] getSkillsByType(final Skill.SkillType type)
	{
		if(_skillsByType == null)
			return NpcTemplate._emptySkillArray;
		return _skillsByType.containsKey(type) ? _skillsByType.get(type) : NpcTemplate._emptySkillArray;
	}

	public synchronized Skill[] getDamageSkills()
	{
		if(_dam_skills == null)
			_dam_skills = summ(new Skill[][] {
					getSkillsByType(Skill.SkillType.PDAM),
					getSkillsByType(Skill.SkillType.FATALBLOW),
					getSkillsByType(Skill.SkillType.MANADAM),
					getSkillsByType(Skill.SkillType.MDAM),
					getSkillsByType(Skill.SkillType.DRAIN),
					getSkillsByType(Skill.SkillType.DRAIN_SOUL) });
		return _dam_skills;
	}

	public synchronized Skill[] getDotSkills()
	{
		if(_dot_skills == null)
			_dot_skills = summ(new Skill[][] {
					getSkillsByType(Skill.SkillType.DOT),
					getSkillsByType(Skill.SkillType.MDOT),
					getSkillsByType(Skill.SkillType.POISON),
					getSkillsByType(Skill.SkillType.BLEED) });
		return _dot_skills;
	}

	public synchronized Skill[] getDebuffSkills()
	{
		if(_debuff_skills == null)
			_debuff_skills = summ(new Skill[][] {
					getSkillsByType(Skill.SkillType.DEBUFF),
					getSkillsByType(Skill.SkillType.CANCEL),
					getSkillsByType(Skill.SkillType.SLEEP),
					getSkillsByType(Skill.SkillType.ROOT),
					getSkillsByType(Skill.SkillType.PARALYZE),
					getSkillsByType(Skill.SkillType.MUTE),
					getSkillsByType(Skill.SkillType.TELEPORT_NPC),
					getSkillsByType(Skill.SkillType.AGGRESSION) });
		return _debuff_skills;
	}

	public synchronized Skill[] getBuffSkills()
	{
		if(_buff_skills == null)
			_buff_skills = getSkillsByType(Skill.SkillType.BUFF);
		return _buff_skills;
	}

	public synchronized Skill[] getStunSkills()
	{
		if(_stun_skills == null)
			_stun_skills = getSkillsByType(Skill.SkillType.STUN);
		return _stun_skills;
	}

	public synchronized Skill[] getHealSkills()
	{
		if(_heal_skills == null)
			_heal_skills = summ(new Skill[][] {
					getSkillsByType(Skill.SkillType.HEAL),
					getSkillsByType(Skill.SkillType.HEAL_PERCENT),
					getSkillsByType(Skill.SkillType.HOT) });
		return _heal_skills;
	}

	private static final Skill[] summ(final Skill[]... skills2d)
	{
		int i = 0;
		for(final Skill[] skills : skills2d)
			i += skills.length;
		if(i == 0)
			return NpcTemplate._emptySkillArray;
		final Skill[] result = new Skill[i];
		i = 0;
		for(final Skill[] skills2 : skills2d)
		{
			System.arraycopy(skills2, 0, result, i, skills2.length);
			i += skills2.length;
		}
		return result;
	}

	public Drop getDropData()
	{
		return _drop;
	}

	public void clearDropData()
	{
		_drop = null;
	}

	public List<MinionData> getMinionData()
	{
		return _minions;
	}

	public HashMap<Integer, Skill> getSkills()
	{
		return _skills == null ? NpcTemplate._emptySkills : _skills;
	}

	public void addQuestEvent(final QuestEventType EventType, final Quest q)
	{
		if(_questEvents == null)
			_questEvents = new HashMap<QuestEventType, Quest[]>();
		if(_questEvents.get(EventType) == null)
			_questEvents.put(EventType, new Quest[] { q });
		else
		{
			final Quest[] _quests = _questEvents.get(EventType);
			final int len = _quests.length;
			final Quest[] tmp = new Quest[len + 1];
			for(int i = 0; i < len; ++i)
			{
				if(_quests[i].getName().equals(q.getName()))
				{
					_quests[i] = q;
					return;
				}
				tmp[i] = _quests[i];
			}
			tmp[len] = q;
			_questEvents.put(EventType, tmp);
		}
	}

	public Quest[] getEventQuests(final QuestEventType EventType)
	{
		if(_questEvents == null)
			return null;
		return _questEvents.get(EventType);
	}

	public boolean hasQuestEvents()
	{
		return _questEvents != null && !_questEvents.isEmpty();
	}

	public Map<QuestEventType, Quest[]> getEventQuests()
	{
		return _questEvents;
	}

	public void setEventQuests(final Map<QuestEventType, Quest[]> qes)
	{
		_questEvents = qes;
	}

	public int getRace()
	{
		return race;
	}

	public void setRace(final int newrace)
	{
		race = newrace;
	}

	public boolean isUndead()
	{
		return race == 1;
	}

	public void setRateHp(final double newrate)
	{
		rateHp = newrate;
	}

	@Override
	public String toString()
	{
		return "Npc template " + name + "[" + npcId + "]";
	}

	@Override
	public int getId()
	{
		return npcId;
	}

	public final String getJClass()
	{
		return jClass;
	}

	public final StatsSet getAIParams()
	{
		return _AIParams == null ? new StatsSet() : _AIParams;
	}

	public synchronized void giveItem(final ItemInstance item, final boolean store)
	{
		if(_inventory == null)
			_inventory = new ArrayList<ItemInstance>();
		synchronized (_inventory)
		{
			if(item.isStackable())
				for(final ItemInstance i : _inventory)
					if(i.getItemId() == item.getItemId())
					{
						i.setCount(item.getCount() + i.getCount());
						if(store)
							i.updateDatabase();
						return;
					}
			_inventory.add(item);
			if(store)
			{
				item.setOwnerId(getId());
				item.setLocation(ItemInstance.ItemLocation.MONSTER);
				item.updateDatabase();
			}
		}
	}

	public List<ItemInstance> takeInventory()
	{
		if(_inventory != null)
			synchronized (_inventory)
			{
				final List<ItemInstance> ret = _inventory;
				_inventory = null;
				return ret;
			}
		return null;
	}

	public static StatsSet getEmptyStatsSet()
	{
		final StatsSet npcDat = CreatureTemplate.getEmptyStatsSet();
		npcDat.set("npcId", 0);
		npcDat.set("displayId", 0);
		npcDat.set("level", 0);
		npcDat.set("name", "");
		npcDat.set("title", "");
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("ai_type", "npc");
		npcDat.set("revardExp", 0);
		npcDat.set("revardSp", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("factionId", "");
		npcDat.set("factionRange", 0);
		npcDat.set("physHitMod", 0);
		npcDat.set("physAvoidMod", 0);
		npcDat.set("soulshotCount", 0);
		npcDat.set("spiritshotCount", 0);
		npcDat.set("isDropHerbs", false);
		return npcDat;
	}

	public void clean()
	{
		_skills = null;
		_skillsByType = null;
		_dam_skills = null;
		_dot_skills = null;
		_debuff_skills = null;
		_buff_skills = null;
		_stun_skills = null;
		_heal_skills = null;
		_drop = null;
		_minions.clear();
		_teachInfo = null;
	}

	static
	{
		NpcTemplate._log = LoggerFactory.getLogger(NpcTemplate.class);
		_emptySkills = new HashMap<Integer, Skill>(0);
		_emptySkillArray = new Skill[0];
	}

	public enum ShotsType
	{
		NONE,
		SOUL,
		SPIRIT,
		BSPIRIT,
		SOUL_SPIRIT,
		SOUL_BSPIRIT;
	}
}
