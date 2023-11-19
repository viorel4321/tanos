package l2s.gameserver.model.instances;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import l2s.gameserver.model.items.ItemInstance;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.DimensionalRiftManager;
import l2s.gameserver.instancemanager.QuestManager;
import l2s.gameserver.instancemanager.TownManager;
import l2s.gameserver.listener.actor.recorder.NpcStatsChangeRecorder;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.TeleportLoc;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.model.base.PledgeSkillLearn;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.Town;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.AcquireSkillList;
import l2s.gameserver.network.l2.s2c.AutoAttackStart;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillList;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.NpcInfo;
import l2s.gameserver.network.l2.s2c.RadarControl;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.tables.TeleportLocTable;
import l2s.gameserver.tables.TeleportTable;
import l2s.gameserver.taskmanager.DecayTaskManager;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.MinionList;
import l2s.gameserver.utils.HtmlUtils;

public class NpcInstance extends Creature
{
	public class BroadcastCharInfoTask implements Runnable
	{
		@Override
		public void run()
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	protected class doBuff implements Runnable
	{
		Creature _actor;
		Skill _skill;
		Player _target;

		doBuff(final Creature actor, final Skill skill, final Player target)
		{
			_actor = actor;
			_skill = skill;
			_target = target;
		}

		@Override
		public void run()
		{
			_actor.doCast(_skill, _target, true);
		}
	}

	private boolean hasChatWindow;
	protected long _dieTime;
	private int _personalAggroRange;
	public List<Integer> questis;
	private Castle _nearestCastle;
	private ClanHall _nearestClanHall;
	private int _currentLHandId;
	private int _currentRHandId;
	private double _currentCollisionRadius;
	private double _currentCollisionHeight;
	protected int _spawnAnimation;
	protected boolean _hasRandomAnimation;
	protected boolean _hasRandomWalk;
	private Future<?> _animationTask;
	private AggroList _aggroList;
	private boolean _noLethal;
	private static final Logger _log = LoggerFactory.getLogger(NpcInstance.class);
	private final ClassId[] _classesToTeach;
	private long _attack_timeout;
	private Location _spawnedLoc;
	private static Map<String, Constructor<?>> _ai_constructors = new HashMap<String, Constructor<?>>();
	private final ReentrantLock getAiLock;
	private final ReentrantLock decayLock;
	protected boolean _unAggred;
	private Spawn _spawn;
	private Spawner _spawn2;
	private SpawnRange _spawnRange;
	private boolean _isDecayed;
	private ScheduledFuture<?> _broadcastCharInfoTask;
	protected long _lastSocialAction;
	private boolean _isBusy;
	private String _busyMessage;
	private boolean _isUnderground;

	private String _ownerName = StringUtils.EMPTY;
	private HardReference<Player> _ownerRef = HardReferences.emptyRef();

	private boolean _geoControlEnabled;

	private final int _geoRadius;
	private final int _geoHeight;

	@Override
	protected CharacterAI initAI()
	{
		CharacterAI ai = null;
		getAiLock.lock();
		try
		{
			final Constructor<?> ai_constructor = NpcInstance._ai_constructors.get(getTemplate().ai_type);
			if(ai_constructor != null)
				try
				{
					ai = (CharacterAI) ai_constructor.newInstance(this);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			if(ai == null)
				ai = new CharacterAI(this);
		}
		finally
		{
			getAiLock.unlock();
		}
		return ai;
	}

	public void setAttackTimeout(final long time)
	{
		_attack_timeout = time;
	}

	public long getAttackTimeout()
	{
		return _attack_timeout;
	}

	public Location getSpawnedLoc()
	{
		return _spawnedLoc;
	}

	public void setSpawnedLoc(final Location loc)
	{
		_spawnedLoc = loc;
	}

	public NpcInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		hasChatWindow = true;
		_dieTime = 0L;
		_personalAggroRange = -1;
		questis = new ArrayList<Integer>();
		_spawnAnimation = 2;
		_spawnedLoc = new Location();
		getAiLock = new ReentrantLock();
		decayLock = new ReentrantLock();
		_unAggred = false;
		_isDecayed = false;
		_busyMessage = "";
		_isUnderground = false;
		if(template == null)
		{
			NpcInstance._log.warn("No template for Npc. Please check your datapack is setup correctly.");
			throw new IllegalArgumentException();
		}
		_hasRandomAnimation = !getParameter("randomAnimationDisabled", false) && Config.MAX_NPC_ANIMATION > 0;
		_hasRandomWalk = !getParameter("noRandomWalk", false);
		_noLethal = getParameter("noLethal", false);
		_classesToTeach = template.getTeachInfo();
		setName(template.name);
		setTitle(template.title);
		final String implementationName = template.ai_type;
		Constructor<?> ai_constructor = NpcInstance._ai_constructors.get(implementationName);
		if(ai_constructor == null)
		{
			try
			{
				if(!implementationName.equalsIgnoreCase("npc"))
					ai_constructor = Class.forName("l2s.gameserver.ai." + implementationName).getConstructors()[0];
			}
			catch(Exception e2)
			{
				try
				{
					final Class<?> classAI = Scripts.getInstance().getClasses().get("ai." + implementationName);
					ai_constructor = classAI.getConstructors()[0];
				}
				catch(Exception e1)
				{
					NpcInstance._log.warn("AI type " + template.ai_type + " not found!");
					e1.printStackTrace();
				}
			}
			if(ai_constructor != null)
				NpcInstance._ai_constructors.put(implementationName, ai_constructor);
		}
		_currentLHandId = getTemplate().lhand;
		_currentRHandId = getTemplate().rhand;
		_currentCollisionHeight = getTemplate().collisionHeight;
		_currentCollisionRadius = getTemplate().collisionRadius;
		_aggroList = new AggroList(this);

		_geoControlEnabled = getParameter("geodata_enabled", false);
		_geoRadius = getParameter("geodata_radius", (int) getCollisionRadius());
		_geoHeight = getParameter("geodata_height", (int) getCollisionHeight());
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<NpcInstance> getRef()
	{
		return (HardReference<NpcInstance>) super.getRef();
	}

	public int getRightHandItem()
	{
		return _currentRHandId;
	}

	public int getLeftHandItem()
	{
		return _currentLHandId;
	}

	public void setLHandId(final int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}

	public void setRHandId(final int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}

	@Override
	public double getCurrentCollisionHeight()
	{
		return _currentCollisionHeight;
	}

	public void setCollisionHeight(final double offset)
	{
		_currentCollisionHeight = offset;
	}

	@Override
	public double getCurrentCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	public void setCollisionRadius(final double collisionRadius)
	{
		_currentCollisionRadius = collisionRadius;
	}

	@Override
	protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp)
	{
		if(attacker.isPlayable())
			getAggroList().addDamageHate(attacker, (int) damage, 0);
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	@Override
	public void onDeath(final Creature killer)
	{
		_dieTime = System.currentTimeMillis();
		setDecayed(false);
		if(isMonster() && (((MonsterInstance) this).isSeeded() || ((MonsterInstance) this).isSpoiled()))
			DecayTaskManager.getInstance().addDecayTask(this, 20000L);
		else
			DecayTaskManager.getInstance().addDecayTask(this);
		_currentLHandId = getTemplate().lhand;
		_currentRHandId = getTemplate().rhand;
		_currentCollisionHeight = getTemplate().collisionHeight;
		_currentCollisionRadius = getTemplate().collisionRadius;
		getAI().stopAITask();
		stopRandomAnimation();
		super.onDeath(killer);
	}

	public long getDeadTime()
	{
		if(_dieTime <= 0L)
			return 0L;
		return System.currentTimeMillis() - _dieTime;
	}

	public AggroList getAggroList()
	{
		return _aggroList;
	}

	public MinionList getMinionList()
	{
		return null;
	}

	public boolean hasMinions()
	{
		return false;
	}

	public void dropItem(final Player lastAttacker, final int itemId, final long itemCount)
	{
		if(itemCount == 0L || lastAttacker == null)
			return;
		if(Config.DROP_COUNTER)
			lastAttacker.incrementDropCounter(itemId, itemCount);
		for(long i = 0L; i < itemCount; ++i)
		{
			final ItemInstance item = ItemTable.getInstance().createItem(itemId);
			if(Config.CHAMPION_DROP_ONLY_ADENA && isChampion() && item.getItemId() != 57)
			{
				item.deleteMe();
				return;
			}
			if(item.isStackable())
			{
				i = itemCount;
				if(isChampion() && item.getItemId() == 57)
					item.setCount((long) (itemCount * Config.RATE_CHAMPION_DROP_ADENA));
				else
					item.setCount(itemCount);
			}
			if(isRaid())
			{
				for(Player player : World.getAroundPlayers(this))
				{
					SystemMessage sm;
					if(itemId == 57)
					{
						sm = new SystemMessage(1246);
						String name = getVisibleName(player);
						if(name.isEmpty())
							sm.addNpcName(getTemplate().npcId);
						else
							sm.addString(name);
						sm.addNumber(Long.valueOf(item.getCount()));
					}
					else
					{
						sm = new SystemMessage(1208);
						String name = getVisibleName(player);
						if(name.isEmpty())
							sm.addNpcName(getTemplate().npcId);
						else
							sm.addString(name);
						sm.addItemName(Integer.valueOf(itemId));
						sm.addNumber(Long.valueOf(item.getCount()));
					}
					player.sendPacket(sm);
				}
			}
			lastAttacker.doAutoLootOrDrop(item, this);
		}
	}

	public void dropItem(final Player lastAttacker, final ItemInstance item)
	{
		if(item.getCount() == 0L)
			return;
		if(isRaid())
		{
			for(Player player : World.getAroundPlayers(this))
			{
				SystemMessage sm;
				if(item.getItemId() == 57)
				{
					sm = new SystemMessage(1246);
					String name = getVisibleName(player);
					if(name.isEmpty())
						sm.addNpcName(getTemplate().npcId);
					else
						sm.addString(name);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(1208);
					String name = getVisibleName(player);
					if(name.isEmpty())
						sm.addNpcName(getTemplate().npcId);
					else
						sm.addString(name);
					sm.addItemName(item.getItemId());
					sm.addNumber(item.getCount());
				}
				player.sendPacket(sm);
			}
		}
		lastAttacker.doAutoLootOrDrop(item, this);
	}

	public ItemInstance getActiveWeapon()
	{
		return null;
	}

	@Override
	public boolean isAttackable(final Creature attacker)
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return false;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setDecayed(false);
		_dieTime = 0L;
	}

	public void onRes()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
		getAggroList().clear();
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setDecayed(false);
		_dieTime = 0L;
		if(!getAI().isActive())
			enableAI();
	}

	@Override
	protected void onDespawn()
	{
		getAggroList().clear();
		disableAI();
		getAI().notifyEvent(CtrlEvent.EVT_DESPAWN);
		questis.clear();
		super.onDespawn();
	}

	@Override
	public void spawnMe()
	{
		super.spawnMe();
		_spawnAnimation = 0;
		getAI().notifyEvent(CtrlEvent.EVT_SPAWN);
		if(getAI().isGlobalAI() || getCurrentRegion() != null && !getCurrentRegion().areNeighborsEmpty())
			enableAI();
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	public void setUnAggred(final boolean state)
	{
		_unAggred = state;
	}

	public boolean isAggressive()
	{
		return getAggroRange() > 0;
	}

	public int getAggroRange()
	{
		if(_unAggred)
			return 0;
		if(_personalAggroRange >= 0)
			return _personalAggroRange;
		return getTemplate().aggroRange;
	}

	public void setAggroRange(final int aggroRange)
	{
		_personalAggroRange = aggroRange;
	}

	public int getFactionRange()
	{
		return getTemplate().factionRange;
	}

	public String getFactionId()
	{
		return getTemplate().factionId;
	}

	public long getExpReward()
	{
		return (long) calcStat(Stats.EXP, getTemplate().revardExp, null, null);
	}

	public long getSpReward()
	{
		return (long) calcStat(Stats.SP, getTemplate().revardSp, null, null);
	}

	@Override
	public void deleteMe()
	{
		super.deleteMe();
		if(_spawn != null)
			_spawn.stopRespawn();
		else if(_spawn2 != null)
			_spawn2.stopRespawn();
		setSpawn(null);
		setSpawn2(null);
	}

	public Spawn getSpawn()
	{
		return _spawn;
	}

	public void setSpawn(final Spawn spawn)
	{
		_spawn = spawn;
	}

	@Override
	public void onDecay()
	{
		decayLock.lock();
		try
		{
			if(isDecayed())
				return;
			setDecayed(true);
			super.onDecay();
			_spawnAnimation = 2;
			if(_spawn != null)
				_spawn.decreaseCount(this);
			else if(_spawn2 != null)
				_spawn2.decreaseCount(this);
			else
				deleteMe();
		}
		finally
		{
			decayLock.unlock();
		}
	}

	public SpawnRange getSpawnRange()
	{
		return _spawnRange;
	}

	public void setSpawnRange(final SpawnRange spawnRange)
	{
		_spawnRange = spawnRange;
	}

	public Spawner getSpawn2()
	{
		return _spawn2;
	}

	public void setSpawn2(final Spawner spawn)
	{
		_spawn2 = spawn;
	}

	public final void setDecayed(final boolean mode)
	{
		_isDecayed = mode;
	}

	public final boolean isDecayed()
	{
		return _isDecayed;
	}

	public void endDecayTask()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
		onDecay();
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	@Override
	public byte getLevel()
	{
		return getTemplate().level;
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		final int weaponId = getTemplate().rhand;
		if(weaponId < 1)
			return null;
		final ItemTemplate item = ItemTable.getInstance().getTemplate(getTemplate().rhand);
		if(!(item instanceof WeaponTemplate))
			return null;
		return (WeaponTemplate) item;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		final int weaponId = getTemplate().lhand;
		if(weaponId < 1)
			return null;
		final ItemTemplate item = ItemTable.getInstance().getTemplate(getTemplate().lhand);
		if(!(item instanceof WeaponTemplate))
			return null;
		return (WeaponTemplate) item;
	}

	@Override
	public void sendChanges()
	{
		if(isFlying())
			return;
		super.sendChanges();
	}

	public void broadcastCharInfo()
	{
		if(!isVisible())
			return;
		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0)
		{
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl();
			return;
		}
		if(_broadcastCharInfoTask != null)
			return;
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	public void broadcastCharInfoImpl()
	{
		for(final Player player : World.getAroundPlayers(this))
			player.sendPacket(new NpcInfo(this, player));
	}

	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000L)
		{
			broadcastPacket(new SocialAction(getObjectId(), 2));
			_lastSocialAction = System.currentTimeMillis();
		}
	}

	public void startRandomAnimation()
	{
		if(!hasRandomAnimation())
			return;
		if(_animationTask != null)
			_animationTask.cancel(false);
		_animationTask = LazyPrecisionTaskManager.getInstance().addNpcAnimationTask(this);
	}

	public void stopRandomAnimation()
	{
		if(_animationTask != null)
		{
			_animationTask.cancel(false);
			_animationTask = null;
		}
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return _hasRandomAnimation;
	}

	public boolean hasRandomWalk()
	{
		return _hasRandomWalk;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	public Castle getCastle()
	{
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && isInZone(Zone.ZoneType.offshore))
			return null;
		if(_nearestCastle == null)
		{
			final Town town = TownManager.getInstance().getClosestTown(this);
			if(town != null)
				_nearestCastle = town.getCastle();
		}
		return _nearestCastle;
	}

	public ClanHall getClanHall()
	{
		if(_nearestClanHall == null)
			_nearestClanHall = ResidenceHolder.getInstance().findNearestResidence(ClanHall.class, getX(), getY(), getZ(), 32768);
		return _nearestClanHall;
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(this != player.getTarget())
		{
			player.setTarget(this);
			if(isAutoAttackable(player))
			{
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(9, (int) getCurrentHp());
				su.addAttribute(10, getMaxHp());
				player.sendPacket(su);
			}
			return;
		}
		if(Events.onAction(player, this, shift))
		{
			player.sendActionFailed();
			return;
		}
		if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}
		if(!isInRange(player, 150L))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}
		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0 && !(this instanceof WarehouseInstance) && !(this instanceof ResidenceManager) && !(this instanceof ClanHallDoormenInstance) && !(this instanceof CastleDoormenInstance) && !ArrayUtils.contains(Config.ALT_GAME_KARMA_NPC, getNpcId()))
		{
			player.sendActionFailed();
			return;
		}
		if(hasRandomAnimation())
			onRandomAnimation();
		player.sendActionFailed();
		if(player.isMoving)
			player.stopMove();
		player.turn(this, 3000);
		if(_isBusy)
			showBusyWindow(player);
		else if(isHasChatWindow())
		{
			boolean flag = false;
			final Quest[] qlst = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
			if(qlst != null && qlst.length > 0)
				for(final Quest element : qlst)
				{
					final QuestState qs = player.getQuestState(element.getId());
					if((qs == null || !qs.isCompleted()) && element.notifyFirstTalk(this, player))
						flag = true;
				}
			if(!flag)
				showChatWindow(player, 0, new Object[0]);
		}
	}

	public void showQuestWindow(final Player player, final int questId)
	{
		if(!player.isQuestContinuationPossible(true))
			return;

		try
		{
			QuestState qs = player.getQuestState(questId);
			if(qs != null)
			{
				if(qs.isCompleted())
				{
					Functions.show(new CustomMessage("quests.QuestAlreadyCompleted"), player);
					return;
				}
				if(qs.getQuest().notifyTalk(this, qs))
					return;
			}
			else
			{
				final Quest q = QuestManager.getQuest(questId);
				if(q != null)
				{
					final Quest[] qlst = getTemplate().getEventQuests(QuestEventType.QUEST_START);
					if(qlst != null && qlst.length > 0)
					{
						final Quest[] array = qlst;
						final int length = array.length;
						int i = 0;
						while(i < length)
						{
							final Quest element = array[i];
							if(element == q)
							{
								qs = q.newQuestState(player, 1);
								if(qs.getQuest().notifyTalk(this, qs))
									return;
								break;
							}
							else
								++i;
						}
					}
				}
			}
			showChatWindow(player, "no-quest.htm", new Object[0]);
		}
		catch(Exception e)
		{
			NpcInstance._log.warn("problem with npc text " + e);
			e.printStackTrace();
		}
		player.sendActionFailed();
	}

	public static boolean canBypassCheck(final Player player, final NpcInstance npc)
	{
		if(npc == null || player.getTarget() != npc || player.isActionsDisabled() || !Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || !npc.isInRange(player, 150L))
		{
			player.sendActionFailed();
			return false;
		}
		return true;
	}

	public void onBypassFeedback(final Player player, final String command)
	{
		if(!canBypassCheck(player, this))
			return;
		try
		{
			if(command.equalsIgnoreCase("TerritoryStatus"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
				html.setFile("merchant/territorystatus.htm");
				html.replace("%npcname%", getName());
				if(getCastle() != null && getCastle().getId() > 0)
				{
					html.replace("%castlename%", getCastle().getName());
					html.replace("%taxpercent%", String.valueOf(getCastle().getTaxPercent()));
					if(getCastle().getOwnerId() > 0)
					{
						final Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
						if(clan != null)
						{
							html.replace("%clanname%", clan.getName());
							html.replace("%clanleadername%", clan.getLeaderName());
						}
						else
						{
							html.replace("%clanname%", "unexistant clan");
							html.replace("%clanleadername%", "None");
						}
					}
					else
					{
						html.replace("%clanname%", "NPC");
						html.replace("%clanleadername%", "None");
					}
				}
				else
				{
					html.replace("%castlename%", "Open");
					html.replace("%taxpercent%", "0");
					html.replace("%clanname%", "No");
					html.replace("%clanleadername%", getName());
				}
				player.sendPacket(html);
			}
			else if(command.equalsIgnoreCase("Exchange"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
				html.setFile("merchant/exchange.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else if(command.startsWith("Quest"))
			{
				String quest = command.substring(5).trim();
				if(quest.length() == 0)
					showQuestWindow(player);
				else
				{
					try
					{
						showQuestWindow(player, Integer.parseInt(quest.split("_")[1]));
					}
					catch(NumberFormatException nfe)
					{
						_log.error("Error while parse NPC bypass command: " + command, nfe);
					}
				}
			}
			else if(command.startsWith("Chat"))
			{
				try
				{
					final int val = Integer.parseInt(command.substring(5));
					showChatWindow(player, val, new Object[0]);
				}
				catch(NumberFormatException nfe)
				{
					final String filename = command.substring(5).trim();
					if(filename.length() == 0)
						showChatWindow(player, "npcdefault.htm", new Object[0]);
					else
						showChatWindow(player, filename, new Object[0]);
				}
			}
			else if(command.startsWith("Loto"))
			{
				if(!Config.ALLOW_LOTO)
				{
					player.sendMessage(player.isLangRus() ? "\u041b\u043e\u0442\u0435\u0440\u0435\u044f \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "Lottery disabled.");
					return;
				}
				try
				{
					final int val = Integer.parseInt(command.substring(5));
					if(val == 0)
						for(int i = 0; i < 5; ++i)
							player.setLoto(i, 0);
					showLotoWindow(player, val);
				}
				catch(NumberFormatException e)
				{
					Log.debug("L2LotteryManagerInstance: bypass: " + command + "; player: " + player, e);
				}
			}
			else if(command.startsWith("CPRecovery"))
				makeCPRecovery(player);
			else if(command.startsWith("NpcLocationInfo"))
			{
				final int val = Integer.parseInt(command.substring(16));
				List<NpcInstance> npcs = GameObjectsStorage.getNpcs(true, val);
				if(!npcs.isEmpty())
				{
					player.sendPacket(new RadarControl(2, 2, npcs.get(0).getLoc()));
					player.sendPacket(new RadarControl(0, 1, npcs.get(0).getLoc()));
				}
			}
			else if(command.startsWith("SupportMagic"))
			{
				if(Config.ADV_NEWBIE_BUFF)
					makeSupportMagicAdv(player);
				else
					makeSupportMagic(player);
			}
			else if(command.startsWith("ProtectionBlessing"))
			{
				if(player.getKarma() > 0)
					return;
				if(player.getLevel() > 39 || player.getClassId().getLevel() >= 2)
				{
					final String content = "<html><body>Blessing of protection not available for characters whose level more than 39 or completed second class transfer.</body></html>";
					final NpcHtmlMessage html2 = new NpcHtmlMessage(_objectId);
					html2.setHtml(content);
					player.sendPacket(html2);
					return;
				}
				doCast(SkillTable.getInstance().getInfo(5182, 1), player, true);
			}
			else if(command.startsWith("Multisell") || command.startsWith("multisell"))
			{
				final String listId = command.substring(9).trim();
				player.setLastNpcId(getNpcId());
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(listId), player, getCastle() != null ? getCastle().getTaxRate() : 0.0);
			}
			else if(command.startsWith("EnterRift"))
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				final Byte b1 = Byte.parseByte(st.nextToken());
				DimensionalRiftManager.getInstance().start(player, b1, this);
			}
			else if(command.startsWith("ChangeRiftRoom"))
			{
				if(player.isInParty() && player.getParty().isInDimensionalRift())
					player.getParty().getDimensionalRift().manualTeleport(player, this);
				else
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
			}
			else if(command.startsWith("ExitRift"))
			{
				if(player.isInParty() && player.getParty().isInDimensionalRift())
					player.getParty().getDimensionalRift().manualExitRift(player, this);
				else
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
			}
			else if(command.equalsIgnoreCase("SkillList"))
			{
				player.setSkillLearningClassId(player.getClassId());
				showSkillList(player);
			}
			else if(command.equalsIgnoreCase("ClanSkillList"))
				showClanSkillList(player);
			else if(command.equalsIgnoreCase("EnchantSkillList"))
				showEnchantSkillList(player);
			else if(command.startsWith("Augment"))
			{
				final int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				if(cmdChoice == 1)
					player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, Msg.ExShowVariationMakeWindow);
				else if(cmdChoice == 2)
					player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, Msg.ExShowVariationCancelWindow);
			}
			else if(command.startsWith("Link"))
				showChatWindow(player, "" + command.substring(5), new Object[0]);
			else if(command.startsWith("Teleport"))
			{
				final int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
				final TeleportTable.TeleportLocation[] list = TeleportTable.getInstance().getTeleportLocationList(getNpcId(), cmdChoice);
				if(list != null)
				{
					player.teleList = cmdChoice;
					showTeleportList(player, list);
				}
				else
					player.sendMessage("\u0421\u0441\u044b\u043b\u043a\u0430 \u043d\u0435\u0438\u0441\u043f\u0440\u0430\u0432\u043d\u0430, \u0441\u043e\u043e\u0431\u0449\u0438\u0442\u0435 \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u0443: " + getNpcId() + " " + cmdChoice);
			}
			else if(command.startsWith("goto"))
				doTeleport(player, Integer.parseInt(command.substring(5).trim()));
		}
		catch(StringIndexOutOfBoundsException sioobe)
		{
			if(!isMonster())
				NpcInstance._log.info("Incorrect htm bypass! npcId=" + getTemplate().npcId + " command=[" + command + "]");
		}
		catch(NumberFormatException nfe)
		{
			if(!isMonster())
				NpcInstance._log.info("Invalid bypass to Server command parameter! npcId=" + getTemplate().npcId + " command=[" + command + "]");
		}
	}

	private void showTeleportList(final Player player, final TeleportTable.TeleportLocation[] list)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("!Gatekeeper ").append(_name).append(":<br>\n");
		if(list != null)
		{
			float pricemod = player.getLevel() < Config.GATEKEEPER_FREE ? 0.0f : Config.GATEKEEPER_MODIFIER;
			if(pricemod > 0.0f)
			{
				final int day = Calendar.getInstance().get(7);
				final int hour = Calendar.getInstance().get(11);
				if(day != 1 && day != 7 && (hour <= 8 || hour >= 24))
					pricemod /= 2.0f;
			}
			player.teleMod = pricemod;
			for(final TeleportTable.TeleportLocation tl : list)
				if(tl._item.getItemId() == 57)
				{
					sb.append("[scripts_Util:SGK ").append(tl._target).append(" ").append((int) (tl._price * pricemod)).append(" @811;").append(tl._name).append("|").append(tl._text);
					if(tl._price > 0)
						sb.append(" - ").append((int) (tl._price * pricemod)).append(" Adena");
					sb.append("]<br1>\n");
				}
				else
					sb.append("[scripts_Util:SGK ").append(tl._target).append(" ").append(tl._price).append(" ").append(tl._item.getItemId()).append(" @811;").append(tl._name).append("|").append(tl._text).append(" - ").append(tl._price).append(" ").append(tl._item.getName()).append("]<br1>\n");
		}
		else
			sb.append("No teleports available.");
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(HtmlUtils.bbParse(sb.toString()));
		player.sendPacket(html);
	}

	private void doTeleport(final Player player, final int val)
	{
		final TeleportLoc list = TeleportLocTable.getInstance().getTemplate(val);
		if(list != null)
		{
			if(player.getKarma() > 0 && !ArrayUtils.contains(Config.ALT_GAME_KARMA_NPC, getNpcId()))
			{
				player.sendMessage(new CustomMessage("l2s.TeleportKarma"));
				return;
			}
			if(player.getMountType() == 2)
			{
				player.sendMessage(new CustomMessage("l2s.TeleportWyvern"));
				return;
			}
			if(list.isForNoble() && !player.isNoble())
			{
				final String filename = "scripts/nobleteleporter-no.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			if(player.isAlikeDead())
				return;
			if(!Config.GATEKEEPER_TELEPORT_SIEGE)
			{
				final Town town = TownManager.getInstance().getClosestTown(list.getLocX(), list.getLocY());
				if(town != null)
				{
					final Castle castle = town.getCastle();
					if(castle != null)
					{
						SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
						if(siegeEvent != null && siegeEvent.isInProgress())
						{
							player.sendPacket(new SystemMessage(707));
							return;
						}
					}
				}
			}
			if(!list.isForNoble())
			{
				int price = (int) (list.getPrice() * (player.getLevel() < Config.GATEKEEPER_FREE ? 0.0f : Config.GATEKEEPER_MODIFIER));
				if(price > 0)
				{
					final Calendar cal = Calendar.getInstance();
					if(cal.get(11) >= 20 && cal.get(11) <= 23 && (cal.get(7) == 1 || cal.get(7) == 7))
						price /= 2;
				}
				if(player.getAdena() >= price)
				{
					if(price > 0)
						player.reduceAdena(price, true);
					player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
				}
				else
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			else
			{
				final ItemInstance pay = player.getInventory().getItemByItemId(13722);
				if(pay != null && pay.getCount() >= list.getPrice())
				{
					player.getInventory().destroyItem(pay, list.getPrice(), false);
					player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
				}
				else
					player.sendPacket(new SystemMessage(351));
			}
		}
		else
			NpcInstance._log.warn("No teleport destination with id: " + val);
		player.sendActionFailed();
	}

	public void showQuestWindow(final Player player)
	{
		final List<Quest> options = new ArrayList<Quest>();

		final List<QuestState> awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
		if(awaits != null)
		{
			for(final QuestState x : awaits)
			{
				if(!options.contains(x.getQuest()) && x.getQuest().isVisible(player))
					options.add(x.getQuest());
			}
		}

		final Quest[] starts = getTemplate().getEventQuests(QuestEventType.QUEST_START);
		if(starts != null)
		{
			for(final Quest x2 : starts)
			{
				if(!options.contains(x2) && x2.isVisible(player))
					options.add(x2);
			}
		}

		if(options.size() > 1)
			showQuestChooseWindow(player, (Quest[]) options.toArray((Object[]) new Quest[options.size()]));
		else if(options.size() == 1)
			showQuestWindow(player, options.get(0).getId());
		else
			showQuestWindow(player, 0);
	}

	public void showQuestChooseWindow(final Player player, final Quest[] quests)
	{
		final StringBuffer sb = new StringBuffer();
		if(player.isLangRus())
			sb.append("<html><body><title>Поговорить о:</title><br>");
		else
			sb.append("<html><body><title>Talk about:</title><br>");
		for(final Quest q : quests)
		{
			if(!q.isVisible(player))
				continue;

			if(player.getQuestState(q.getId()) == null)
				sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr()).append("]</a><br>");
			else if(player.getQuestState(q.getId()).isCompleted())
				sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr()).append(player.isLangRus() ? " - завершен" : " - completed").append("]</a><br>");
			else
				sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr()).append(player.isLangRus() ? " - в процессе" : " - in progress").append("]</a><br>");
		}
		sb.append("</body></html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}

	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		if(getParameter("chatWindowDisabled", false))
			return;

		int npcId = getTemplate().npcId;

		String filename;
		if(Config.ALLOW_SEVEN_SIGNS)
		{
			filename = "seven_signs/";
			final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
			final int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(2);
			final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
			final boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
			final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
			switch(npcId)
			{
				case 31095:
				case 31096:
				case 31097:
				case 31098:
				case 31099:
				case 31100:
				case 31101:
				case 31102:
				{
					if(isSealValidationPeriod)
					{
						if(sealAvariceOwner != compWinner)
						{
							player.sendMessage("Seal of avarice wasn't taken!");
							filename += "necro_no.htm";
							break;
						}
						if(playerCabal != compWinner)
						{
							switch(compWinner)
							{
								case 2:
								{
									player.sendPacket(new SystemMessage(1301));
									filename += "necro_no.htm";
									break;
								}
								case 1:
								{
									player.sendPacket(new SystemMessage(1302));
									filename += "necro_no.htm";
									break;
								}
								case 0:
								{
									filename = getHtmlPath(npcId, val, player);
									break;
								}
							}
							break;
						}
						filename = getHtmlPath(npcId, val, player);
						break;
					}
					else
					{
						if(playerCabal == 0)
						{
							filename += "necro_no.htm";
							break;
						}
						filename = getHtmlPath(npcId, val, player);
						break;
					}
				}
				case 31114:
				case 31115:
				case 31116:
				case 31117:
				case 31118:
				case 31119:
				{
					if(isSealValidationPeriod)
					{
						if(sealGnosisOwner != compWinner)
						{
							player.sendMessage("Seal of gnosis wasn't taken!");
							filename += "cata_no.htm";
							break;
						}
						if(playerCabal != compWinner)
						{
							switch(compWinner)
							{
								case 2:
								{
									player.sendPacket(new SystemMessage(1301));
									filename += "cata_no.htm";
									break;
								}
								case 1:
								{
									player.sendPacket(new SystemMessage(1302));
									filename += "cata_no.htm";
									break;
								}
								case 0:
								{
									filename = getHtmlPath(npcId, val, player);
									break;
								}
							}
							break;
						}
						filename = getHtmlPath(npcId, val, player);
						break;
					}
					else
					{
						if(playerCabal == 0)
						{
							filename += "cata_no.htm";
							break;
						}
						filename = getHtmlPath(npcId, val, player);
						break;
					}
				}
				case 31111:
				{
					if(playerCabal == sealAvariceOwner && playerCabal == compWinner)
					{
						switch(sealAvariceOwner)
						{
							case 2:
							{
								filename += "spirit_dawn.htm";
								break;
							}
							case 1:
							{
								filename += "spirit_dusk.htm";
								break;
							}
							case 0:
							{
								filename += "spirit_null.htm";
								break;
							}
						}
						break;
					}
					filename += "spirit_null.htm";
					break;
				}
				case 31112:
				{
					filename += "spirit_exit.htm";
					break;
				}
				default:
				{
					if(npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
						return;
					filename = getHtmlPath(npcId, val, player);
					break;
				}
			}
		}
		else
		{
			if(npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
				return;
			filename = getHtmlPath(npcId, val, player);
		}

		NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, val);
		if(replace.length % 2 == 0)
		{
			for(int i = 0; i < replace.length; i += 2)
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
		}
		player.sendPacket(packet);
	}

	public void showChatWindow(final Player player, final String filename, final Object... replace)
	{
		final NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, 0);
		if(replace.length % 2 == 0)
			for(int i = 0; i < replace.length; i += 2)
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
		player.sendPacket(packet);
	}

	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		String temp = "default/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;
		temp = "trainer/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;
		return "npcdefault.htm";
	}

	public void showLotoWindow(final Player player, final int val)
	{
		final int npcId = getTemplate().npcId;
		final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
		if(val == 0)
		{
			final String filename = getHtmlPath(npcId, 1, player);
			html.setFile(filename);
		}
		else if(val != 21)
		{
			final String filename = getHtmlPath(npcId, 5, player);
			html.setFile(filename);
			int count = 0;
			int found = 0;
			for(int i = 0; i < 5; ++i)
				if(player.getLoto(i) == val)
				{
					player.setLoto(i, 0);
					found = 1;
				}
				else if(player.getLoto(i) > 0)
					++count;
			if(count < 5 && found == 0 && val < 21)
				for(int i = 0; i < 5; ++i)
					if(player.getLoto(i) == 0)
					{
						player.setLoto(i, val);
						break;
					}
			count = 0;
			for(int i = 0; i < 5; ++i)
				if(player.getLoto(i) > 0)
				{
					++count;
					String button = String.valueOf(player.getLoto(i));
					if(player.getLoto(i) < 10)
						button = "0" + button;
					final String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					final String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}
			if(count == 5)
			{
				final String search2 = "0\">Return";
				final String replace2 = "21\">The winner selected the numbers above.";
				html.replace(search2, replace2);
			}
		}
		else
		{
			final int price = 2000;
			final int lotonumber = 1;
			int enchant = 0;
			int type2 = 0;
			for(int j = 0; j < 5; ++j)
			{
				if(player.getLoto(j) == 0)
					return;
				if(player.getLoto(j) < 17)
					enchant += (int) Math.pow(2.0, player.getLoto(j) - 1);
				else
					type2 += (int) Math.pow(2.0, player.getLoto(j) - 17);
			}
			if(player.getAdena() < price)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			player.reduceAdena(price, true);
			final SystemMessage sm = new SystemMessage(371);
			sm.addNumber(Integer.valueOf(lotonumber));
			sm.addItemName(Integer.valueOf(4442));
			player.sendPacket(sm);
			final ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 4442);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.getInventory().addItem(item);
			player.getInventory().findItemByItemId(57);
			html.setHtml("<html><body>Lottery Ticket Seller:<br>Thank you for playing the lottery<br>The winners will be announced at ??? 19<br><center><a action=\"bypass -h npc_%objectId%_Chat 0\">Back</a></center></body></html>");
		}
		player.sendPacket(html);
		player.sendActionFailed();
	}

	public void makeCPRecovery(final Player player)
	{
		if(getNpcId() != 31225 && getNpcId() != 31226)
			return;
		final int neededmoney = 100;
		final int currentmoney = player.getAdena();
		if(neededmoney > currentmoney)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena(neededmoney, true);
		player.setCurrentCp(getCurrentCp() + 5000.0);
		player.sendPacket(new SystemMessage(1405).addString(player.getName()));
	}

	public void makeSupportMagic(final Player player)
	{
		if(player.isCursedWeaponEquipped())
			return;
		final int lvl = player.getLevel();
		if(lvl < 8)
		{
			showChatWindow(player, "default/newbie_nosupport8.htm", new Object[0]);
			return;
		}
		if(lvl > 39)
		{
			showChatWindow(player, "default/newbie_nosupport39.htm", new Object[0]);
			return;
		}
		int time = 0;
		if(lvl >= 8 && lvl <= 39)
		{
			final Skill skill = SkillTable.getInstance().getInfo(4322, 1);
			time += 1000;
			ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
		}
		if(lvl >= 11 && lvl <= 38)
		{
			final Skill skill = SkillTable.getInstance().getInfo(4323, 1);
			time += 1000;
			ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
		}
		if(lvl >= 16 && lvl <= 34)
		{
			final Skill skill = SkillTable.getInstance().getInfo(4338, 1);
			time += 1000;
			ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
		}
		if(!player.isMageClass())
		{
			if(lvl >= 12 && lvl <= 37)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4324, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 13 && lvl <= 36)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4325, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 14 && lvl <= 35)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4326, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 15 && lvl <= 34)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4327, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
		}
		else
		{
			if(lvl >= 12 && lvl <= 37)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4328, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 13 && lvl <= 36)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4329, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 14 && lvl <= 35)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4330, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 15 && lvl <= 34)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4331, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
		}
	}

	private void makeSupportMagicAdv(final Player player)
	{
		if(player.isCursedWeaponEquipped())
			return;
		final int lvl = player.getLevel();
		if(lvl < 8)
		{
			showChatWindow(player, "default/newbie_nosupport8.htm", new Object[0]);
			return;
		}
		if(lvl > 62)
		{
			showChatWindow(player, "default/newbie_nosupport39.htm", new Object[0]);
			return;
		}
		int time = 0;
		if(lvl >= 8 && lvl <= 62)
		{
			final Skill skill = SkillTable.getInstance().getInfo(4322, 1);
			time += 1000;
			ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
		}
		if(lvl >= 11 && lvl <= 62)
		{
			final Skill skill = SkillTable.getInstance().getInfo(4323, 1);
			time += 1000;
			ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
		}
		if(lvl >= 6 && lvl <= 39)
		{
			final Skill skill = SkillTable.getInstance().getInfo(4338, 1);
			time += 1000;
			ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
		}
		if(!player.isMageClass())
		{
			if(lvl >= 12 && lvl <= 62)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4324, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 13 && lvl <= 62)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4325, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 14 && lvl <= 62)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4326, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 15 && lvl <= 62)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4327, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
		}
		else
		{
			if(lvl >= 12 && lvl <= 62)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4328, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 13 && lvl <= 62)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4329, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 14 && lvl <= 62)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4330, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
			if(lvl >= 15 && lvl <= 62)
			{
				final Skill skill = SkillTable.getInstance().getInfo(4331, 1);
				time += 1000;
				ThreadPoolManager.getInstance().schedule(new doBuff(this, skill, player), time);
			}
		}
	}

	public final boolean isBusy()
	{
		return _isBusy;
	}

	public void setBusy(final boolean isBusy)
	{
		_isBusy = isBusy;
	}

	public final String getBusyMessage()
	{
		return _busyMessage;
	}

	public void setBusyMessage(final String message)
	{
		_busyMessage = message;
	}

	public void showBusyWindow(final Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
		html.setFile("npcbusy.htm");
		html.replace("%npcname%", getName());
		html.replace("%playername%", player.getName());
		html.replace("%busymessage%", _busyMessage);
		player.sendPacket(html);
	}

	public void showSkillList(final Player player)
	{
		final ClassId classId = player.getClassId();
		if(classId == null)
			return;
		final int npcId = getTemplate().npcId;
		if(_classesToTeach == null)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("I cannot teach you. My class list is empty.<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		if(!getTemplate().canTeach(classId) && !getTemplate().canTeach(classId.getParent()))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2s.gameserver.model.instances.NpcInstance.WrongTeacherClass").toString(player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;
		final SkillLearn[] availableSkills;
		final SkillLearn[] skills = availableSkills = SkillTree.getInstance().getAvailableSkills(player, classId);
		for(final SkillLearn s : availableSkills)
		{
			final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk != null && sk.getCanLearn(player.getClassId()))
				if(sk.canTeachBy(npcId))
				{
					final int cost = SkillTree.getInstance().getSkillCost(player, sk);
					++counts;
					asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
				}
		}
		if(counts == 0)
		{
			final int minlevel = SkillTree.getInstance().getMinLevelForNewSkill(player, classId);
			if(minlevel > 0)
			{
				final SystemMessage sm = new SystemMessage(607);
				sm.addNumber(Integer.valueOf(minlevel));
				player.sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(750);
				player.sendPacket(sm);
			}
		}
		else
			player.sendPacket(asl);
		player.sendActionFailed();
	}

	public void showEnchantSkillList(final Player player)
	{
		if(!enchantChecks(player))
			return;
		final EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(player);
		final ExEnchantSkillList esl = new ExEnchantSkillList();
		int counts = 0;
		for(final EnchantSkillLearn s : skills)
		{
			final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk != null)
			{
				++counts;
				esl.addSkill(s.getId(), s.getLevel(), s.getSpCost(), s.getExp());
			}
		}
		if(counts == 0)
			player.sendPacket(Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
		else
			player.sendPacket(esl);
	}

	private boolean enchantChecks(final Player player)
	{
		if(getNpcId() != Config.ALLOW_ESL && _classesToTeach == null)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("I cannot teach you. My class list is empty.<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return false;
		}
		if(getNpcId() != Config.ALLOW_ESL && !getTemplate().canTeach(player.getClassId()) && !getTemplate().canTeach(player.getClassId().getParent()))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2s.gameserver.model.instances.NpcInstance.WrongTeacherClass").toString(player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return false;
		}
		if(player.getClassId().getLevel() < 4)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(player.isLangRus() ? "\u0412\u0430\u043c \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u0430 \u0442\u0440\u0435\u0442\u044c\u044f \u043f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u044f." : "You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return false;
		}
		if(player.getLevel() < 76)
		{
			player.sendPacket(Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			return false;
		}
		return true;
	}

	public void showFishingSkillList(final Player player)
	{
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Fishing);
		int counts = 0;
		final SkillLearn[] availableSkills;
		final SkillLearn[] skills = availableSkills = SkillTree.getInstance().getAvailableSkills(player);
		for(final SkillLearn s : availableSkills)
		{
			final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk != null)
			{
				++counts;
				asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), 0, 0);
			}
		}
		if(counts == 0)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(player.isLangRus() ? "\u0412\u044b \u0432\u044b\u0443\u0447\u0438\u043b\u0438 \u0432\u0441\u0435 \u0443\u043c\u0435\u043d\u0438\u044f." : "You've learned all skills.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
		}
		else
			player.sendPacket(asl);
		player.sendActionFailed();
	}

	public void showClanSkillList(final Player player)
	{
		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(236));
			player.sendActionFailed();
			return;
		}
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Clan);
		int counts = 0;
		final PledgeSkillLearn[] availablePledgeSkills;
		final PledgeSkillLearn[] skills = availablePledgeSkills = SkillTree.getInstance().getAvailablePledgeSkills(player);
		for(final PledgeSkillLearn s : availablePledgeSkills)
		{
			final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk != null)
			{
				final int cost = s.getRepCost();
				++counts;
				asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
			}
		}
		if(counts == 0)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml("<html><head><body>" + (player.isLangRus() ? "\u0412\u044b \u0432\u044b\u0443\u0447\u0438\u043b\u0438 \u0432\u0441\u0435 \u0443\u043c\u0435\u043d\u0438\u044f" : "You've learned all skills") + ".</body></html>");
			player.sendPacket(html);
		}
		else
			player.sendPacket(asl);
		player.sendActionFailed();
	}

	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	@Override
	public boolean getChargedSoulShot()
	{
		switch(getTemplate().shots)
		{
			case SOUL:
			case SOUL_SPIRIT:
			case SOUL_BSPIRIT:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	@Override
	public int getChargedSpiritShot()
	{
		switch(getTemplate().shots)
		{
			case SOUL_SPIRIT:
			case SPIRIT:
			{
				return 1;
			}
			case SOUL_BSPIRIT:
			case BSPIRIT:
			{
				return 2;
			}
			default:
			{
				return 0;
			}
		}
	}

	@Override
	public boolean unChargeShots(final boolean spirit)
	{
		broadcastPacket(new MagicSkillUse(this, spirit ? 2061 : 2039, 1, 0, 0L));
		return true;
	}

	@Override
	public double getCollisionRadius()
	{
		return getCurrentCollisionRadius();
	}

	@Override
	public double getCollisionHeight()
	{
		return getCurrentCollisionHeight();
	}

	public int calculateLevelDiffForDrop(final int charLevel)
	{
		if(!Config.DEEPBLUE_DROP_RULES)
			return 0;
		final int mobLevel = getLevel();
		final int deepblue_maxdiff = isRB() ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;
		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}

	public boolean isSevenSignsMonster()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return false;
		return getName().startsWith("Lilim ") || getName().startsWith("Nephilim ") || getName().startsWith("Lith ") || getName().startsWith("Gigant ");
	}

	@Override
	public String toString()
	{
		return getNpcId() + " " + getName();
	}

	public void refreshID()
	{
		GameObjectsStorage.remove(this);

		_objectId = IdFactory.getInstance().getNextId();

		GameObjectsStorage.put(this);
	}

	public void setUnderground(final boolean b)
	{
		_isUnderground = b;
	}

	public boolean isUnderground()
	{
		return _isUnderground;
	}

	public void animationShots()
	{
		switch(getTemplate().shots)
		{
			case SOUL:
			{
				unChargeShots(false);
				break;
			}
			case SPIRIT:
			case BSPIRIT:
			{
				unChargeShots(true);
				break;
			}
			case SOUL_SPIRIT:
			case SOUL_BSPIRIT:
			{
				unChargeShots(false);
				unChargeShots(true);
				break;
			}
		}
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if(isRunning())
			return getRunSpeed() * 1.0f / getTemplate().baseRunSpd;
		return getWalkSpeed() * 1.0f / getTemplate().baseWalkSpd;
	}

	@Override
	public NpcStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new NpcStatsChangeRecorder(this);
			}
		return (NpcStatsChangeRecorder) _statsRecorder;
	}

	public boolean isHasChatWindow()
	{
		return hasChatWindow;
	}

	public void setHasChatWindow(final boolean hcw)
	{
		hasChatWindow = hcw;
	}

	@Override
	public boolean isLethalImmune()
	{
		return _noLethal;
	}

	@Override
	public boolean isImmobilized()
	{
		return getTemplate().immobilized || super.isImmobilized();
	}

	public int getTrueId()
	{
		return 0;
	}

	public int getParameter(final String str, final int val)
	{
		return getTemplate().getAIParams().getInteger(str, val);
	}

	public long getParameter(final String str, final long val)
	{
		return getTemplate().getAIParams().getLong(str, val);
	}

	public boolean getParameter(final String str, final boolean val)
	{
		return getTemplate().getAIParams().getBool(str, val);
	}

	@Override
	public void enableAI()
	{
		getAI().startAITask();
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		startRandomAnimation();
	}

	@Override
	public void disableAI()
	{
		stopRandomAnimation();
		getAI().stopAITask();
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		final List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>(3);
		list.add(new NpcInfo(this, forPlayer));
		if(isInCombat())
			list.add(new AutoAttackStart(getObjectId()));
		if(isMoving || isFollow)
			list.add(movePacket());
		if(getAI().isNulled())
		{
			if(_animationTask == null)
				startRandomAnimation();
		}
		else if(!getAI().isActive())
			enableAI();
		return list;
	}

	@Override
	public boolean isNpc()
	{
		return true;
	}

	@Override
	public int getGeoZ(int x, int y, int z)
	{
		int geoZ = super.getGeoZ(x, y, z);

		Location spawnedLoc = getSpawnedLoc();
		if(spawnedLoc.equals(x, y, z))
		{
			// Заглушка для точечного спавна. Некоторые НПС заспавнены в местах, где нет геодаты.
			if(Math.abs(geoZ - z) > Config.MIN_LAYER_HEIGHT)
				return z;
		}
		return geoZ;
	}

	@Override
	public Clan getClan()
	{
		final Castle castle = getCastle();
		if(castle == null)
			return null;
		return castle.getOwner() == null ? null : castle.getOwner();
	}

	@Override
	public boolean isEpicBoss()
	{
		return getTemplate().isEpicBoss;
	}

	public void setOwner(Player owner)
	{
		_ownerName = owner == null ? StringUtils.EMPTY : owner.getName();
		_ownerRef = owner == null ? HardReferences.<Player> emptyRef() : owner.getRef();
	}

	@Override
	public Player getPlayer()
	{
		return _ownerRef.get();
	}

	@Override
	public final String getVisibleName(Player receiver)
	{
		String name = getName();
		if(name.equals(getTemplate().name))
			name = StringUtils.EMPTY;
		return name;
	}

	@Override
	public final String getVisibleTitle(Player receiver)
	{
		String title = getTitle();
		if(title.equals(Servitor.TITLE_BY_OWNER_NAME))
		{
			Player player = getPlayer();
			if(player != null)
				title = player.getVisibleName(receiver);
			else
				title = _ownerName;
		}
		else if(title.equals(getTemplate().title))
			title = StringUtils.EMPTY;
		return title;
	}

	@Override
	protected Shape makeGeoShape()
	{
		int x = getX();
		int y = getY();
		int z = getZ();
		Circle circle = new Circle(x, y, _geoRadius);
		circle.setZmin(z - Config.MAX_Z_DIFF);
		circle.setZmax(z + _geoHeight);
		return circle;
	}

	@Override
	protected boolean isGeoControlEnabled()
	{
		return _geoControlEnabled;
	}

	public void setGeoControlEnabled(boolean value)
	{
		_geoControlEnabled = value;
	}
}
