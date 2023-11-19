package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import l2s.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.ItemToDrop;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reward.DropData;
import l2s.gameserver.model.reward.DropGroup;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.MinionList;
import l2s.gameserver.utils.Util;

public class MonsterInstance extends NpcInstance
{
	private static Logger _log = LoggerFactory.getLogger(MonsterInstance.class);

	private boolean _dead;
	private boolean _dying;
	private final ReentrantLock dieLock;
	private final ReentrantLock dyingLock;
	private boolean _sweeped = false;
	private final ReentrantLock sweepLock;
	private final ReentrantLock harvestLock;
	private double _overhitDamage;
	protected MinionList _minionList;
	private ScheduledFuture<?> minionMaintainTask;
	private MonsterInstance _master;
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1500;
	private static final int MONSTER_MAX_LEVEL = 200;
	private List<ItemInstance> _inventory;
	private ItemInstance _harvestItem;
	private ItemTemplate _seeded;
	private int seederObjectId;
	private int spoilerObjectId;
	private int overhitAttackerObjectId;
	private List<Integer> _absorbersList;
	private ItemInstance[] _sweepItems;
	private static final int[] _absorbingMOBS_level4;
	private static final int[] _absorbingMOBS_level8;
	private static final int[] _absorbingMOBS_level10;
	private static final int[] _absorbingMOBS_level12;
	private static final int[] _absorbingMOBS_level13;
	private static final int[] _REDCRYSTALS;
	private static final int[] _GREENCRYSTALS;
	private static final int[] _BLUECRYSTALS;
	private static final short _MAX_CRYSTALS_LEVEL = 13;
	protected static final DropData[] _matdrop;
	protected static final List<DropGroup> _herbs;
	private int _isChampion;
	private boolean _isSpoiled;

	public MonsterInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_dead = false;
		_dying = false;
		dieLock = new ReentrantLock();
		dyingLock = new ReentrantLock();
		sweepLock = new ReentrantLock();
		harvestLock = new ReentrantLock();
		_master = null;
	}

	@Override
	public boolean isLethalImmune()
	{
		return getMaxHp() >= Config.LETHAL_IMMUNE_HP || super.isLethalImmune();
	}

	@Override
	public boolean isFearImmune()
	{
		return Config.CHAMPION_FEAR_IMMUNE && _isChampion > 0 || isRaidFighter() || super.isFearImmune();
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return Config.CHAMPION_PARALYZE_IMMUNE && _isChampion > 0 || super.isParalyzeImmune();
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return !attacker.isMonster();
	}

	public int getChampion()
	{
		return _isChampion;
	}

	@Override
	public boolean isChampion()
	{
		return getChampion() > 0;
	}

	public void setChampion()
	{
		if(canChampion())
		{
			if(Rnd.chance(Config.CHAMPION_CHANCE2))
				this.setChampion(2);
			else if(Rnd.chance(Config.CHAMPION_CHANCE1))
				this.setChampion(1);
			else
				this.setChampion(0);
		}
		else
			this.setChampion(0);
	}

	public void setChampion(final int level)
	{
		if(level == 0)
		{
			removeSkillById(4407);
			_isChampion = 0;
		}
		else
		{
			addSkill(SkillTable.getInstance().getInfo(4407, level));
			_isChampion = level;
			setCurrentHpMp(getMaxHp(), getMaxMp(), false);
		}
	}

	public boolean canChampion()
	{
		return !isMinion() && getTemplate().revardExp > 0 && getTemplate().level <= Config.CHAMPION_TOP_LEVEL && getTemplate().level >= Config.CHAMPION_MIN_LEVEL;
	}

	@Override
	public int getTeam()
	{
		return getChampion();
	}

	@Override
	public void onSpawn()
	{
		_dead = false;
		_dying = false;
		overhitAttackerObjectId = 0;
		super.onSpawn();
		spawnMinions();
		setSpoiled(false, null);
		_sweepItems = null;
		_absorbersList = null;
		_seeded = null;
		seederObjectId = 0;
		spoilerObjectId = 0;
	}

	@Override
	public void onRes()
	{
		_dead = false;
		_dying = false;
		overhitAttackerObjectId = 0;
		super.onRes();
		setSpoiled(false, null);
		_sweepItems = null;
		_absorbersList = null;
		_seeded = null;
		seederObjectId = 0;
		spoilerObjectId = 0;
	}

	protected int getMaintenanceInterval()
	{
		return 1500;
	}

	@Override
	public MinionList getMinionList()
	{
		return _minionList;
	}

	public void setNewMinionList()
	{
		_minionList = new MinionList(this);
	}

	public void spawnMinions()
	{
		if(getTemplate().getMinionData().size() > 0)
		{
			if(minionMaintainTask != null)
			{
				minionMaintainTask.cancel(true);
				minionMaintainTask = null;
			}
			minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MinionMaintainTask(), getMaintenanceInterval());
		}
	}

	public void setDead(final boolean dead)
	{
		_dead = dead;
	}

	public void removeMinions()
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(true);
			minionMaintainTask = null;
		}
		if(_minionList != null)
			_minionList.maintainLonelyMinions();
		_minionList = null;
	}

	public int getTotalSpawnedMinionsInstances()
	{
		return _minionList == null ? 0 : _minionList.countSpawnedMinions();
	}

	public void notifyMinionDied(final MonsterInstance minion)
	{
		if(_minionList != null)
			_minionList.removeSpawnedMinion(minion);
	}

	@Override
	public boolean hasMinions()
	{
		return _minionList != null && _minionList.hasMinions();
	}

	public void setLeader(final MonsterInstance leader)
	{
		_master = leader;
	}

	public MonsterInstance getLeader()
	{
		return _master;
	}

	@Override
	public boolean isRaidFighter()
	{
		return getLeader() != null && getLeader().isRaid();
	}

	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}

	@Override
	public Location getSpawnedLoc()
	{
		return getLeader() != null ? getLeader().getLoc() : super.getSpawnedLoc();
	}

	public Location getMinionPosition()
	{
		return Location.findPointToStay(getLoc(), (int) getCollisionRadius() + 30, (int) getCollisionRadius() + 50, getGeoIndex());
	}

	@Override
	public void deleteMe()
	{
		removeMinions();
		if(_inventory != null)
			synchronized (_inventory)
			{
				for(final ItemInstance item : _inventory)
					getTemplate().giveItem(item, false);
				_inventory = null;
			}
		super.deleteMe();
	}

	@Override
	public void onDeath(final Creature killer)
	{
		if(getLeader() != null)
			getLeader().notifyMinionDied(this);
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(true);
			minionMaintainTask = null;
		}
		if(_dead)
			return;
		dieLock.lock();
		try
		{
			if(_dead)
				return;
			_dieTime = System.currentTimeMillis();
			_dead = true;
			if(isBox())
			{
				super.onDeath(killer);
				return;
			}
			try
			{
				dyingLock.lock();
				_dying = true;
				calculateRewards(killer);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				_dying = false;
				dyingLock.unlock();
			}
		}
		finally
		{
			dieLock.unlock();
		}
		super.onDeath(killer);
	}

	@Override
	protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp)
	{
		if(skill != null && skill.isOverhit())
		{
			final double overhitDmg = (getCurrentHp() - damage) * -1.0;
			if(overhitDmg <= 0.0)
			{
				setOverhitDamage(0.0);
				setOverhitAttacker(null);
			}
			else
			{
				setOverhitDamage(overhitDmg);
				setOverhitAttacker(attacker);
			}
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	public void calculateRewards(Creature lastAttacker)
	{
		Creature topDamager = getAggroList().getTopDamager();
		if(lastAttacker == null || !lastAttacker.isPlayable())
			lastAttacker = topDamager;
		if(lastAttacker == null || !lastAttacker.isPlayable())
			return;
		final Player killer = lastAttacker.getPlayer();
		if(killer == null)
			return;
		if(Config.DROP_LASTHIT)
			topDamager = killer;
		final Map<Playable, AggroList.HateInfo> aggroMap = getAggroList().getPlayableMap();
		try
		{
			if(Config.KILL_COUNTER)
				killer.incrementKillsCounter(getNpcId());
			final NpcTemplate template = getTemplate();
			++template.killscount;
			if(getLevel() >= Config.MOBS_CR_MIN_LVL)
			{
				final Player l2Player = killer;
				++l2Player.mobs_count;
			}
			if(Config.CHAMPION_REWARD && isChampion() && killer.getLevel() - getLevel() <= Config.CHAMPION_REWARD_DIFF)
			{
				final int[] rew = getChampion() == 1 ? Config.CHAMPION_REWARD_LIST1 : Config.CHAMPION_REWARD_LIST2;
				if(rew.length > 3)
					for(int i = 0; i < rew.length; i += 4)
						if(Rnd.chance(rew[i + 3]))
						{
							final int count = Rnd.get(rew[i + 1], rew[i + 2]);
							killer.getInventory().addItem(rew[i], count);
							killer.sendPacket(SystemMessage.obtainItems(rew[i], count, 0));
						}
			}
			final Quest[] quests = getTemplate().getEventQuests(QuestEventType.MOBKILLED);
			if(quests != null && quests.length > 0)
			{
				List<Player> players = null;
				if(isRaid() && Config.ALT_NO_LASTHIT)
				{
					players = new ArrayList<Player>();
					for(final Playable pl : aggroMap.keySet())
						if(pl.isPlayer() && (Config.QUEST_KILL_REWARD_DEAD || !pl.isDead()) && (pl.isInRange(this, Config.PARTY_QUEST_ITEMS_RANGE) || pl.isInRange(killer, Config.PARTY_QUEST_ITEMS_RANGE)) && Math.abs(pl.getZ() - getZ()) < Config.PARTY_QUEST_ITEMS_RANGE_Z && !players.contains(pl.getPlayer()))
							players.add((Player) pl);
				}
				else if(killer.getParty() != null)
				{
					players = new ArrayList<Player>(killer.getParty().getMemberCount());
					for(final Player pl2 : killer.getParty().getPartyMembers())
						if((Config.QUEST_KILL_REWARD_DEAD || !pl2.isDead()) && (pl2.isInRange(this, Config.PARTY_QUEST_ITEMS_RANGE) || pl2.isInRange(killer, Config.PARTY_QUEST_ITEMS_RANGE)) && Math.abs(pl2.getZ() - getZ()) < Config.PARTY_QUEST_ITEMS_RANGE_Z)
							players.add(pl2);
				}
				for(final Quest quest : quests)
				{
					Player toReward = killer;
					if(quest.getParty() != 0 && players != null)
					{
						if(isRaid() || quest.getParty() == 2)
						{
							for(final Player pl3 : players)
							{
								final QuestState qs = pl3.getQuestState(quest.getId());
								if(qs != null && !qs.isCompleted())
									quest.notifyKill(getObjectId(), pl3.getObjectId());
							}
							toReward = null;
						}
						else
						{
							final List<Player> interested = new ArrayList<Player>(players.size());
							for(final Player pl4 : players)
							{
								final QuestState qs2 = pl4.getQuestState(quest.getId());
								if(qs2 != null && !qs2.isCompleted())
									interested.add(pl4);
							}
							if(interested.isEmpty())
								break;
							toReward = interested.get(Rnd.get(interested.size()));
							if(toReward == null)
								toReward = killer;
						}
					}
					if(toReward != null)
					{
						final QuestState qs3 = toReward.getQuestState(quest.getId());
						if(qs3 != null && !qs3.isCompleted())
							quest.notifyKill(getObjectId(), toReward.getObjectId());
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.error("ERROR in MonsterInstance.calculateRewards npcId: " + getNpcId(), e);
		}
		final Map<Playable, RewardInfo> rewards = new HashMap<Playable, RewardInfo>();
		for(final AggroList.HateInfo info : aggroMap.values())
		{
			if(info.damage <= 1)
				continue;
			final Playable attacker = (Playable) info.attacker;
			final RewardInfo reward = rewards.get(attacker);
			if(reward == null)
				rewards.put(attacker, new RewardInfo(attacker, info.damage));
			else
				reward.addDamage(info.damage);
		}
		if(topDamager == null)
			topDamager = killer;
		final int lvl = topDamager.getLevel();
		final Playable[] attackers = rewards.keySet().toArray(new Playable[rewards.size()]);
		double[] xpsp = new double[2];
		for(final Playable attacker2 : attackers)
			if(!attacker2.isDead())
			{
				final RewardInfo reward2 = rewards.get(attacker2);
				if(reward2 != null)
				{
					final Player player = attacker2.getPlayer();
					if(player != null)
					{
						final boolean sn = Config.SUMMON_EXP_SP_PARTY && attacker2.isSummon();
						final Party party = attacker2.isPlayer() || sn ? player.getParty() : null;
						final int maxHp = getMaxHp();
						xpsp[1] = xpsp[0] = 0.0;
						if(party == null)
						{
							final int damage = Math.min(reward2._dmg, maxHp);
							if(damage > 0)
							{
								xpsp = calculateExpAndSp(player, player.getLevel(), damage, false);
								xpsp[0] = applyOverhit(killer, xpsp[0]);
								if(attacker2.isPet())
									attacker2.addExpAndSp((long) xpsp[0], (long) xpsp[1]);
								else
									player.addExpAndSp((long) xpsp[0], (long) xpsp[1], true, true);
							}
							rewards.remove(attacker2);
						}
						else
						{
							int partyDmg = 0;
							int partylevel = 1;
							final List<Player> rewardedMembers = new ArrayList<Player>();
							for(final Player partyMember : party.getPartyMembers())
							{
								final RewardInfo ai = rewards.remove(partyMember);
								if(!partyMember.isDead())
								{
									if(!this.isInRangeZ(partyMember, Config.ALT_PARTY_DISTRIBUTION_RANGE))
										continue;
									if(ai != null)
										partyDmg += ai._dmg;
									rewardedMembers.add(partyMember);
									if(partyMember.getLevel() <= partylevel)
										continue;
									partylevel = partyMember.getLevel();
								}
							}
							if(sn)
							{
								final RewardInfo ai2 = rewards.remove(attacker2);
								if(ai2 != null)
									partyDmg += ai2._dmg;
							}
							partyDmg = Math.min(partyDmg, maxHp);
							if(partyDmg > 0)
							{
								xpsp = calculateExpAndSp(player, partylevel, partyDmg, true);
								final double partyMul = partyDmg / maxHp;
								final double[] array3 = xpsp;
								final int n = 0;
								array3[n] *= partyMul;
								final double[] array4 = xpsp;
								final int n2 = 1;
								array4[n2] *= partyMul;
								party.distributeXpAndSp(xpsp[0] = applyOverhit(killer, xpsp[0]), xpsp[1], rewardedMembers, lastAttacker, this);
							}
						}
					}
				}
			}
		CursedWeaponsManager.getInstance().dropAttackable(this, killer);
		doItemDrop(topDamager, lvl);
		if(this.isSpoiled())
			doSweepDrop(lastAttacker, lvl);
		if(!isRaid())
		{
			final double chancemod = getTemplate().rateHp * Experience.penaltyModifier(calculateLevelDiffForDrop(lvl), 9.0);
			if(Config.ALT_GAME_MATHERIALSDROP && chancemod > 0.0 && (!this.isSeeded() || _seeded.isAltSeed()))
				for(final DropData d : MonsterInstance._matdrop)
					if(getLevel() >= d.getMinLevel())
					{
						final long count2 = Util.rollDrop(d.getMinDrop(), d.getMaxDrop(), d.getChance() * chancemod * Config.RATE_DROP_ITEMS * killer.getRateItems(), true);
						if(count2 > 0L)
							this.dropItem(killer, d.getItemId(), count2);
					}
			if(getTemplate().isDropHerbs && chancemod > 0.0)
				for(final DropGroup h : MonsterInstance._herbs)
				{
					final Collection<ItemToDrop> itdl = h.rollFixedQty(0, this, killer, chancemod);
					if(itdl != null)
						for(final ItemToDrop itd : itdl)
							this.dropItem(killer, itd.itemId, 1L);
				}
		}
		try
		{
			levelSoulCrystals(killer, aggroMap);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
		}
	}

	public boolean isDying()
	{
		return _dying;
	}

	public void giveItem(final ItemInstance item, final boolean store)
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
							i.updateDatabase(true);
						return;
					}
			_inventory.add(item);
			if(store)
			{
				item.setOwnerId(getNpcId());
				item.setLocation(ItemInstance.ItemLocation.MONSTER);
				item.updateDatabase();
			}
		}
	}

	@Override
	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000L)
		{
			this.broadcastPacket(new SocialAction(getObjectId(), 1));
			_lastSocialAction = System.currentTimeMillis();
		}
	}

	@Override
	public void startRandomAnimation()
	{}

	@Override
	public int getKarma()
	{
		return 0;
	}

	public void addAbsorber(final Player attacker)
	{
		if(attacker == null)
			return;
		if(getMaxLevelCrystal() == 0 || getCurrentHpPercents() > 50.0)
			return;
		if(_absorbersList == null)
			_absorbersList = new ArrayList<Integer>();
		if(!_absorbersList.contains(attacker.getObjectId()))
			_absorbersList.add(attacker.getObjectId());
	}

	private int getMinLevelCrystal()
	{
		final int mobId = getNpcId();
		for(final int id : MonsterInstance._absorbingMOBS_level4)
			if(id == mobId)
				return 0;
		for(final int id : MonsterInstance._absorbingMOBS_level8)
			if(id == mobId)
				return 0;
		for(final int id : MonsterInstance._absorbingMOBS_level10)
			if(id == mobId)
				return 0;
		for(final int id : MonsterInstance._absorbingMOBS_level12)
			if(id == mobId)
				return 10;
		for(final int id : MonsterInstance._absorbingMOBS_level13)
			if(id == mobId)
				return 12;
		return 0;
	}

	private int getMaxLevelCrystal()
	{
		final int mobId = getNpcId();
		for(final int id : MonsterInstance._absorbingMOBS_level13)
			if(id == mobId)
				return 13;
		for(final int id : MonsterInstance._absorbingMOBS_level12)
			if(id == mobId)
				return 12;
		for(final int id : MonsterInstance._absorbingMOBS_level10)
			if(id == mobId)
				return 10;
		for(final int id : MonsterInstance._absorbingMOBS_level8)
			if(id == mobId)
				return 8;
		for(final int id : MonsterInstance._absorbingMOBS_level4)
			if(id == mobId)
				return 4;
		return 0;
	}

	private void levelSoulCrystals(final Creature attacker, final Map<Playable, AggroList.HateInfo> aggroList)
	{
		if(attacker == null || !attacker.isPlayable())
		{
			_absorbersList = null;
			return;
		}
		boolean levelPartyCrystals = false;
		final Player killer = attacker.getPlayer();
		final int minCrystalLevel = getMinLevelCrystal();
		final int maxCrystalLevel = getMaxLevelCrystal();
		if(minCrystalLevel >= 10 && maxCrystalLevel > 10)
			levelPartyCrystals = true;
		else if(maxCrystalLevel == 0 || _absorbersList == null || !_absorbersList.contains(killer.getObjectId()))
		{
			_absorbersList = null;
			return;
		}
		_absorbersList = null;
		List<Player> players = null;
		if(levelPartyCrystals)
			if(Config.ALT_NO_LASTHIT)
			{
				players = new ArrayList<Player>();
				for(final Playable p : aggroList.keySet())
					if(p.isPlayer())
						players.add((Player) p);
			}
			else if(killer.isInParty())
				players = killer.getParty().getPartyMembers();
		if(players == null)
		{
			players = new ArrayList<Player>();
			players.add(killer);
		}
		for(final int id : Config.SC_RANDOM_LEVELING)
			if(getNpcId() == id)
			{
				levelPartyCrystals = false;
				break;
			}
		for(final Player player : players)
		{
			if(player.getQuestState(350) == null)
				continue;
			if(!player.isInRange(this, Config.ALT_PARTY_DISTRIBUTION_RANGE) && !player.isInRange(killer, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			int oldCrystalId = 0;
			int newCrystalId = 0;
			int crystalsCount = 0;
			int crystalLevel = 0;
			boolean canIncreaseCrystal = false;
			boolean resonated = false;
			for(final ItemInstance item : player.getInventory().getItems())
			{
				final int itemId = item.getItemId();
				if(isSoulCrystal(itemId))
				{
					if(++crystalsCount > 1)
					{
						resonated = true;
						break;
					}
					if(crystalsCount >= 1)
						if((newCrystalId = getNextLevelCrystalId(itemId)) != 0)
						{
							crystalLevel = getCrystalLevel(itemId);
							canIncreaseCrystal = crystalLevel >= minCrystalLevel && crystalLevel < maxCrystalLevel;
							oldCrystalId = itemId;
						}
				}
			}
			if(resonated)
			{
				if(levelPartyCrystals)
					continue;
				player.sendPacket(new SystemMessage(977));
			}
			else if(!canIncreaseCrystal)
			{
				if(levelPartyCrystals)
					continue;
				player.sendPacket(new SystemMessage(978));
			}
			else if(levelPartyCrystals || Rnd.chance(Config.SC_LEVEL_CHANCE))
			{
				final ItemInstance oldCrystal = player.getInventory().getItemByItemId(oldCrystalId);
				if(oldCrystal == null)
					continue;
				player.sendPacket(SystemMessage.removeItems(oldCrystal.getItemId(), 1L), new SystemMessage(974), SystemMessage.obtainItems(newCrystalId, 1L, 0));
				player.getInventory().destroyItem(oldCrystal, 1L, true);
				player.getInventory().addItem(ItemTable.getInstance().createItem(newCrystalId));
				if(player.isGM() && player.isInvisible())
					continue;
				final String newCrystalColor = getCrystalColor(newCrystalId);
				if(newCrystalColor == null)
					continue;
				final int newCrystalLvl = getCrystalLevel(newCrystalId);
				final CustomMessage cm = new CustomMessage("l2s.gameserver.model.instances.MonsterInstance.levelSoulCrystals");
				cm.addString(player.getName()).addString(newCrystalColor).addNumber(newCrystalLvl);
				player.broadcastPacketToOthers(new SystemMessage(cm));
			}
			else
				player.sendPacket(new SystemMessage(978));
		}
	}

	private static String getCrystalColor(final int crystalId)
	{
		for(final int id : MonsterInstance._REDCRYSTALS)
			if(id == crystalId)
				return "red";
		for(final int id : MonsterInstance._BLUECRYSTALS)
			if(id == crystalId)
				return "blue";
		for(final int id : MonsterInstance._GREENCRYSTALS)
			if(id == crystalId)
				return "green";
		return null;
	}

	private static int getCrystalLevel(final int crystalId)
	{
		for(int i = 0; i < MonsterInstance._REDCRYSTALS.length; ++i)
			if(MonsterInstance._REDCRYSTALS[i] == crystalId)
				return i;
		for(int i = 0; i < MonsterInstance._BLUECRYSTALS.length; ++i)
			if(MonsterInstance._BLUECRYSTALS[i] == crystalId)
				return i;
		for(int i = 0; i < MonsterInstance._GREENCRYSTALS.length; ++i)
			if(MonsterInstance._GREENCRYSTALS[i] == crystalId)
				return i;
		return Integer.MAX_VALUE;
	}

	private static int getNextLevelCrystalId(final int crystalId)
	{
		for(int i = 0; i < MonsterInstance._REDCRYSTALS.length; ++i)
			if(MonsterInstance._REDCRYSTALS[i] == crystalId)
				return i >= 13 ? MonsterInstance._REDCRYSTALS[13] : MonsterInstance._REDCRYSTALS[i + 1];
		for(int i = 0; i < MonsterInstance._GREENCRYSTALS.length; ++i)
			if(MonsterInstance._GREENCRYSTALS[i] == crystalId)
				return i >= 13 ? MonsterInstance._GREENCRYSTALS[13] : MonsterInstance._GREENCRYSTALS[i + 1];
		for(int i = 0; i < MonsterInstance._BLUECRYSTALS.length; ++i)
			if(MonsterInstance._BLUECRYSTALS[i] == crystalId)
				return i >= 13 ? MonsterInstance._BLUECRYSTALS[13] : MonsterInstance._BLUECRYSTALS[i + 1];
		return 0;
	}

	private static boolean isSoulCrystal(final int crystalId)
	{
		for(final int id : MonsterInstance._REDCRYSTALS)
			if(id == crystalId)
				return true;
		for(final int id : MonsterInstance._BLUECRYSTALS)
			if(id == crystalId)
				return true;
		for(final int id : MonsterInstance._GREENCRYSTALS)
			if(id == crystalId)
				return true;
		return false;
	}

	public ItemInstance takeHarvest()
	{
		harvestLock.lock();
		final ItemInstance harvest = _harvestItem;
		_harvestItem = null;
		_seeded = null;
		seederObjectId = 0;
		harvestLock.unlock();
		return harvest;
	}

	public void setSeeded(final ItemTemplate seed, final Player player)
	{
		if(player == null)
			return;
		harvestLock.lock();
		try
		{
			_seeded = seed;
			seederObjectId = player.getObjectId();
			_harvestItem = ItemTable.getInstance().createItem(Manor.getInstance().getCropType(seed.getItemId()));
			if(getTemplate().rateHp <= 1.0)
				_harvestItem.setCount(1L);
			else
				_harvestItem.setCount(Rnd.get(Math.round(getTemplate().rateHp), Math.round(1.5 * getTemplate().rateHp)));
		}
		finally
		{
			harvestLock.unlock();
		}
	}

	public boolean isSeeded(final Player seeder)
	{
		if(seederObjectId == 0)
			return false;
		final Player _seeder = GameObjectsStorage.getPlayer(seederObjectId);
		return seeder != null && seeder == _seeder || _dieTime + 10000L < System.currentTimeMillis();
	}

	public boolean isSeeded()
	{
		return _seeded != null;
	}

	public boolean isSpoiled()
	{
		return _isSpoiled;
	}

	public boolean isSpoiled(final Player spoiler)
	{
		sweepLock.lock();
		Player this_spoiler;
		try
		{
			if(!_isSpoiled)
				return false;
			this_spoiler = GameObjectsStorage.getPlayer(spoilerObjectId);
		}
		finally
		{
			sweepLock.unlock();
		}
		return this_spoiler == null || spoiler.getObjectId() == this_spoiler.getObjectId() || _dieTime + 10000L < System.currentTimeMillis() || this.getDistance(this_spoiler) > Config.ALT_PARTY_DISTRIBUTION_RANGE || spoiler.getParty() != null && spoiler.getParty().containsMember(this_spoiler);
	}

	public void setSpoiled(final boolean isSpoiled, final Player spoiler)
	{
		sweepLock.lock();
		try
		{
			_isSpoiled = isSpoiled;
			spoilerObjectId = spoiler != null ? spoiler.getObjectId() : 0;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	public void setSweeped(boolean value) {
		_sweeped = value;
	}

	public boolean isSweeped()
	{
		return _sweeped;
	}

	public void doItemDrop(final Creature topDamager, final int lvl)
	{
		final Player player = topDamager.getPlayer();
		if(player == null)
			return;
		final double mod = this.calcStat(Stats.DROP, 1.0, topDamager, null);
		if(getTemplate().getDropData() != null)
		{
			final List<ItemToDrop> drops = getTemplate().getDropData().rollDrop(calculateLevelDiffForDrop(lvl), this, player, mod);
			for(final ItemToDrop drop : drops)
			{
				if(_seeded != null && !_seeded.isAltSeed() && drop.itemId != 57 && drop.itemId != 6360 && drop.itemId != 6361 && drop.itemId != 6362)
					continue;
				this.dropItem(player, drop.itemId, drop.count);
			}
		}
		if(_inventory != null)
			synchronized (_inventory)
			{
				for(final ItemInstance drop2 : _inventory)
					if(drop2 != null)
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.MonsterInstance.ItemBelongedToOther").addString(drop2.getName()));
						this.dropItem(player, drop2);
					}
				if(_inventory != null)
					_inventory.clear();
				_inventory = null;
			}
		final List<ItemInstance> templateInv = getTemplate().takeInventory();
		if(templateInv != null)
		{
			for(final ItemInstance drop2 : templateInv)
				if(drop2 != null)
				{
					player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.MonsterInstance.ItemBelongedToOther").addString(drop2.getName()));
					this.dropItem(player, drop2);
				}
			if(_inventory != null)
				_inventory.clear();
			_inventory = null;
		}
	}

	private void doSweepDrop(final Creature lastAttacker, final int lvl)
	{
		final Player player = lastAttacker.getPlayer();
		if(player == null)
			return;
		final int levelDiff = calculateLevelDiffForDrop(lvl);
		final List<ItemInstance> spoiled = new ArrayList<ItemInstance>();
		if(getTemplate().getDropData() != null)
		{
			final double mod = this.calcStat(Stats.SPOIL, 1.0, lastAttacker, null);
			final List<ItemToDrop> spoils = getTemplate().getDropData().rollSpoil(levelDiff, this, player, mod);
			for(final ItemToDrop spoil : spoils)
			{
				final ItemInstance dropit = ItemTable.getInstance().createItem(spoil.itemId);
				dropit.setCount(spoil.count);
				spoiled.add(dropit);
			}
		}
		if(spoiled.size() > 0)
			_sweepItems = (ItemInstance[]) spoiled.toArray((Object[]) new ItemInstance[spoiled.size()]);
	}

	protected double[] calculateExpAndSp(final Player attacker, final int level, final long damage, final boolean party)
	{
		if(!this.isInRange(attacker, Config.ALT_PARTY_DISTRIBUTION_RANGE))
			return new double[] { 0.0, 0.0 };
		final int diff = level - getLevel();
		double xp = getExpReward() * damage / getMaxHp();
		double sp = getSpReward() * damage / getMaxHp();
		if(diff > 5)
		{
			final double mod = Math.pow(0.83, diff - 5);
			xp *= mod;
			sp *= mod;
		}
		xp = !party && attacker.getVarBoolean("NoExp") ? 0.0 : Math.max(0.0, xp);
		sp = Math.max(0.0, sp);
		return new double[] { xp, sp };
	}

	private double applyOverhit(final Player killer, double xp)
	{
		if(xp > 0.0 && getOverhitAttacker() != null && killer == getOverhitAttacker())
		{
			final int overHitExp = calculateOverhitExp(xp);
			killer.sendPacket(new SystemMessage(361), new SystemMessage(362).addNumber(Integer.valueOf((int) (overHitExp * Config.RATE_XP * killer.getRateExp()))));
			xp += overHitExp;
		}
		return xp;
	}

	public Creature getOverhitAttacker()
	{
		GameObject attacker = GameObjectsStorage.findObject(overhitAttackerObjectId);
		if(attacker == null || !attacker.isCreature())
			return null;
		return (Creature) attacker;
	}

	@Override
	public void setOverhitAttacker(final Creature overhitAttacker)
	{
		overhitAttackerObjectId = overhitAttacker == null ? 0 : overhitAttacker.getObjectId();
	}

	public double getOverhitDamage()
	{
		return _overhitDamage;
	}

	@Override
	public void setOverhitDamage(final double damage)
	{
		_overhitDamage = damage;
	}

	public int calculateOverhitExp(final double normalExp)
	{
		double overhitPercentage = getOverhitDamage() * 100.0 / getMaxHp();
		if(overhitPercentage > 25.0)
			overhitPercentage = 25.0;
		final double overhitExp = overhitPercentage / 100.0 * normalExp;
		overhitAttackerObjectId = 0;
		setOverhitDamage(0.0);
		return (int) Math.round(overhitExp);
	}

	public boolean isSweepActive()
	{
		dyingLock.lock();
		try
		{
			return _sweepItems != null && _sweepItems.length > 0;
		}
		finally
		{
			dyingLock.unlock();
		}
	}

	public ItemInstance[] takeSweep()
	{
		sweepLock.lock();
		ItemInstance[] sweep;
		try
		{
			sweep = _sweepItems == null || _sweepItems.length == 0 ? null : _sweepItems.clone();
			_sweepItems = null;
		}
		finally
		{
			sweepLock.unlock();
		}
		return sweep;
	}

	@Override
	public boolean isMonster()
	{
		return true;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}

	@Override
	public boolean isDmg()
	{
		return true;
	}

	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}

	@Override
	public boolean isAggressive()
	{
		return (Config.CHAMPION_CAN_BE_AGGRO || getChampion() == 0) && super.isAggressive();
	}

	@Override
	public String getFactionId()
	{
		return Config.CHAMPION_CAN_BE_SOCIAL || getChampion() == 0 ? super.getFactionId() : "";
	}

	@Override
	public String toString()
	{
		return "Mob " + getName() + " [" + getNpcId() + "] / " + getObjectId();
	}

	static
	{
		_absorbingMOBS_level4 = new int[] {
				20583,
				20584,
				20585,
				20586,
				20587,
				20588,
				20636,
				20637,
				20638,
				20639,
				20640,
				20641,
				20642,
				20643,
				20644,
				20645 };
		_absorbingMOBS_level8 = new int[] { 20646, 20647, 20648, 20649, 20650, 21006, 21007, 21008 };
		_absorbingMOBS_level10 = new int[] {
				20627,
				20628,
				20629,
				20674,
				20761,
				20762,
				20821,
				20823,
				20826,
				20827,
				20828,
				20829,
				20830,
				20831,
				20858,
				20859,
				20860,
				21009,
				21010,
				21062,
				21063,
				21068,
				21070 };
		_absorbingMOBS_level12 = new int[] { 29022, 25163, 25269, 25453, 25328, 25109, 25202, 29020, 25283, 25286, 22215, 22216, 22217, 29065 };
		_absorbingMOBS_level13 = new int[] { 25338, 29019, 29066, 29067, 29068, 25319, 29028, 29046, 29047 };
		_REDCRYSTALS = new int[] { 4629, 4630, 4631, 4632, 4633, 4634, 4635, 4636, 4637, 4638, 4639, 5577, 5580, 5908 };
		_GREENCRYSTALS = new int[] { 4640, 4641, 4642, 4643, 4644, 4645, 4646, 4647, 4648, 4649, 4650, 5578, 5581, 5911 };
		_BLUECRYSTALS = new int[] { 4651, 4652, 4653, 4654, 4655, 4656, 4657, 4658, 4659, 4660, 4661, 5579, 5582, 5914 };
		_matdrop = new DropData[] {
				new DropData(1864, 1, 1, 50000.0, 1),
				new DropData(1865, 1, 1, 25000.0, 1),
				new DropData(1866, 1, 1, 16666.0, 1),
				new DropData(1867, 1, 1, 33333.0, 1),
				new DropData(1868, 1, 1, 50000.0, 1),
				new DropData(1869, 1, 1, 25000.0, 1),
				new DropData(1870, 1, 1, 25000.0, 1),
				new DropData(1871, 1, 1, 25000.0, 1),
				new DropData(1872, 1, 1, 50000.0, 1),
				new DropData(1873, 1, 1, 10000.0, 1),
				new DropData(1874, 1, 1, 1666.0, 20),
				new DropData(1875, 1, 1, 1666.0, 20),
				new DropData(1876, 1, 1, 5000.0, 20),
				new DropData(1877, 1, 1, 1000.0, 20),
				new DropData(4039, 1, 1, 833.0, 40),
				new DropData(4040, 1, 1, 500.0, 40),
				new DropData(4041, 1, 1, 217.0, 40),
				new DropData(4042, 1, 1, 417.0, 40),
				new DropData(4043, 1, 1, 833.0, 40),
				new DropData(4044, 1, 1, 833.0, 40) };
		_herbs = new ArrayList<DropGroup>(3);
		DropGroup d = new DropGroup(0);
		d.addDropItem(new DropData(8600, 1, 1, 120000.0, 1));
		d.addDropItem(new DropData(8603, 1, 1, 120000.0, 1));
		d.addDropItem(new DropData(8601, 1, 1, 40000.0, 1));
		d.addDropItem(new DropData(8604, 1, 1, 40000.0, 1));
		d.addDropItem(new DropData(8602, 1, 1, 12000.0, 1));
		d.addDropItem(new DropData(8605, 1, 1, 12000.0, 1));
		d.addDropItem(new DropData(8614, 1, 1, 3000.0, 1));
		MonsterInstance._herbs.add(d);
		d = new DropGroup(0);
		d.addDropItem(new DropData(8611, 1, 1, 50000.0, 1));
		d.addDropItem(new DropData(8606, 1, 1, 50000.0, 1));
		d.addDropItem(new DropData(8608, 1, 1, 50000.0, 1));
		d.addDropItem(new DropData(8610, 1, 1, 50000.0, 1));
		d.addDropItem(new DropData(8607, 1, 1, 50000.0, 1));
		d.addDropItem(new DropData(8609, 1, 1, 50000.0, 1));
		d.addDropItem(new DropData(8612, 1, 1, 10000.0, 1));
		d.addDropItem(new DropData(8613, 1, 1, 10000.0, 1));
		MonsterInstance._herbs.add(d);
	}

	protected static final class RewardInfo
	{
		protected Creature _attacker;
		protected int _dmg;

		public RewardInfo(final Creature attacker, final int dmg)
		{
			_dmg = 0;
			_attacker = attacker;
			_dmg = dmg;
		}

		public void addDamage(int dmg)
		{
			if(dmg < 0)
				dmg = 0;
			_dmg += dmg;
		}

		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}

	public class MinionMaintainTask implements Runnable
	{
		@Override
		public void run()
		{
			if(MonsterInstance.this == null || isDead())
				return;
			try
			{
				if(_minionList == null)
					setNewMinionList();
				_minionList.maintainMinions();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
}
