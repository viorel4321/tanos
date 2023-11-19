package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.StringUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.SummonAI;
import l2s.gameserver.listener.actor.recorder.SummonStatsChangeRecorder;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.PetInventory;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.AutoAttackStart;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.NpcInfo;
import l2s.gameserver.network.l2.s2c.PartySpelled;
import l2s.gameserver.network.l2.s2c.PetDelete;
import l2s.gameserver.network.l2.s2c.PetInfo;
import l2s.gameserver.network.l2.s2c.PetItemList;
import l2s.gameserver.network.l2.s2c.PetStatusShow;
import l2s.gameserver.network.l2.s2c.PetStatusUpdate;
import l2s.gameserver.network.l2.s2c.RelationChanged;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.taskmanager.DecayTaskManager;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public abstract class Servitor extends Playable
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

	private class PetInfoTask implements Runnable
	{
		@Override
		public void run()
		{
			sendPetInfoImpl();
			_petInfoTask = null;
		}
	}

	private static final long serialVersionUID = 1L;

	public static final String TITLE_BY_OWNER_NAME = "%OWNER_NAME%";

	public static final int SIEGE_GOLEM_ID = 14737;
	public static final int SIEGE_CANNON_ID = 14768;
	public static final int SWOOP_CANNON_ID = 14839;
	private static final int SUMMON_DISAPPEAR_RANGE = 2500;

	protected long _exp;
	protected int _sp;
	private Creature _lastTarget;
	private int _spawnAnimation;
	private int _maxLoad;
	private int _spsCharged;
	private int _attackRange;
	private boolean _follow;
	private boolean _depressed;
	private boolean _ssCharged;
	private Location _lastFollowPosition;
	private final String _ownerName;
	private int _ownerObjectId;

	private ScheduledFuture<?> _broadcastCharInfoTask;
	private Future<?> _petInfoTask;

	public Servitor(final int objectId, final NpcTemplate template, final Player owner)
	{
		super(objectId, template);
		_exp = 0L;
		_sp = 0;
		_lastTarget = null;
		_spawnAnimation = 2;
		_spsCharged = 0;
		_attackRange = 36;
		_follow = true;
		_depressed = false;
		_ssCharged = false;
		_lastFollowPosition = null;
		_ownerName = owner.getName();
		_ownerObjectId = owner.getObjectId();
		this.setXYZInvisible(owner.getX() + Rnd.get(-100, 100), owner.getY() + Rnd.get(-100, 100), owner.getZ());
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_spawnAnimation = 0;
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	protected CharacterAI initAI()
	{
		return new SummonAI(this);
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	public abstract int getSummonType();

	@Override
	public void updateStats()
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		for(final Player player : World.getAroundPlayers(this))
			if(player != null)
				if(player == owner)
					player.sendPacket(new PetInfo(this, 1));
				else
					player.sendPacket(new NpcInfo(this, player, 1));
	}

	public boolean isMountable()
	{
		return false;
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		final Player owner = getPlayer();
		if(owner == null)
		{
			player.sendActionFailed();
			return;
		}
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this && (isSummon() || player == owner))
				player.sendPacket(new StatusUpdate(getObjectId()).addAttribute(9, (int) getCurrentHp()).addAttribute(10, getMaxHp()).addAttribute(11, (int) getCurrentMp()).addAttribute(12, getMaxMp()));
			else
				player.sendActionFailed();
		}
		else if(Events.onAction(player, this, shift) || player.isConfused() || player.isBlocked())
			player.sendActionFailed();
		else if(player == owner)
		{
			if(this.isInRange(player, 150L))
				player.turn(this, 3000);
			player.sendPacket(new PetInfo(this));
			if(!player.isActionsDisabled())
				player.sendPacket(new PetStatusShow(this), new PartySpelled(this, true));
			player.sendActionFailed();
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else
		{
			if(this.isInRange(player, 150L))
				player.turn(this, 3000);
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, 100);
				else
					player.sendActionFailed();
			}
			else
				player.sendActionFailed();
		}
	}

	public Creature getLastTarget()
	{
		return _lastTarget;
	}

	public void setLastTarget(final Creature lastTarget)
	{
		_lastTarget = lastTarget;
	}

	public long getExpForThisLevel()
	{
		if(getLevel() >= Experience.LEVEL.length)
			return 0L;
		return Experience.LEVEL[getLevel()];
	}

	public long getExpForNextLevel()
	{
		if(getLevel() + 1 >= Experience.LEVEL.length)
			return 0L;
		return Experience.LEVEL[getLevel() + 1];
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	public final long getExp()
	{
		return _exp;
	}

	public final void setExp(final long exp)
	{
		_exp = exp;
	}

	public final int getSp()
	{
		return _sp;
	}

	public void setSp(final int sp)
	{
		_sp = sp;
	}

	public int getMaxLoad()
	{
		return _maxLoad;
	}

	public void setMaxLoad(final int maxLoad)
	{
		_maxLoad = maxLoad;
	}

	public abstract int getCurrentFed();

	public abstract int getMaxFed();

	@Override
	protected synchronized void onDeath(Creature killer)
	{
		super.onDeath(killer);
		final Player owner = getPlayer();
		if(owner == null)
			return;
		if(killer == null || killer == owner || killer.getObjectId() == _objectId || isInZoneBattle() || killer.isInZoneBattle())
			return;
		if(killer.isSummon())
			killer = killer.getPlayer();
		if(killer == null)
			return;
		if(killer.isPlayer())
		{
			final Player pk = (Player) killer;
			if(this.isInZone(Zone.ZoneType.Siege))
				return;
			final DuelEvent duelEvent = this.getEvent(DuelEvent.class);
			if(owner.getPvpFlag() > 0 || owner.atMutualWarWith(pk))
				pk.setPvpKills(pk.getPvpKills() + 1);
			else if((duelEvent == null || duelEvent != pk.getEvent(DuelEvent.class)) && getKarma() <= 0 && !owner.isDead())
			{
				final int pkCountMulti = Math.max(pk.getPkKills() / 2, 1);
				pk.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
			}
			pk.sendChanges();
		}
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	@Override
	public void onDecay()
	{
		deleteMe();
	}

	public void endDecayTask()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
		onDecay();
	}

	public void sendStUpdate()
	{
		final Player owner = getPlayer();
		if(owner != null)
			owner.sendPacket(new PetStatusUpdate(this));
	}

	@Override
	public void deleteMe()
	{
		final Player owner = getPlayer();
		if(owner != null)
		{
			owner.sendPacket(new PetDelete(getObjectId(), 2));
			owner.setServitor(null);
		}
		_resEffs = null;
		_ownerObjectId = 0;
		super.deleteMe();
	}

	public void unSummon()
	{
		deleteMe();
	}

	public int getAttackRange()
	{
		return _attackRange;
	}

	public void setAttackRange(int range)
	{
		if(range < 36)
			range = 36;
		_attackRange = range;
	}

	@Override
	public void setFollowStatus(final boolean state, final boolean changeIntention)
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		_follow = state;
		if(changeIntention)
			if(_follow)
			{
				setLastFollowPosition(null);
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, 100);
			}
			else
			{
				setLastFollowPosition(getLoc());
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			}
	}

	public boolean isFollow()
	{
		return _follow;
	}

	public void setLastFollowPosition(final Location loc)
	{
		_lastFollowPosition = loc;
	}

	public Location getLastFollowPosition()
	{
		return _lastFollowPosition;
	}

	@Override
	public void updateEffectIcons()
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		for(final Player player : World.getAroundPlayers(this))
		{
			if(player == null)
				continue;
			if(player != owner)
				player.sendPacket(new NpcInfo(this, player, 1));
			else
				player.sendPacket(new PetInfo(this, 1));
		}
		final PartySpelled ps = new PartySpelled(this, true);
		if(owner.getParty() != null)
			owner.getParty().broadCast(ps);
		else
			owner.sendPacket(ps);
	}

	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	public int getControlItemId()
	{
		return 0;
	}

	public WeaponTemplate getActiveWeapon()
	{
		return null;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	@Override
	public void doPickupItem(final GameObject object)
	{}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		return null;
	}

	public Party getParty()
	{
		final Player owner = getPlayer();
		return owner != null ? owner.getParty() : null;
	}

	public boolean isInParty()
	{
		final Player owner = getPlayer();
		return owner != null && owner.getParty() != null;
	}

	@Override
	public abstract void displayGiveDamageMessage(final Creature p0, final boolean p1, final boolean p2, final boolean p3);

	@Override
	public abstract void displayReceiveDamageMessage(final Creature p0, final int p1);

	@Override
	public boolean unChargeShots(final boolean spirit)
	{
		final Player owner = getPlayer();
		if(owner == null)
			return false;
		if(spirit)
		{
			if(_spsCharged != 0)
			{
				_spsCharged = 0;
				owner.autoShot();
				return true;
			}
		}
		else if(_ssCharged)
		{
			_ssCharged = false;
			owner.autoShot();
			return true;
		}
		return false;
	}

	@Override
	public boolean getChargedSoulShot()
	{
		return _ssCharged;
	}

	@Override
	public int getChargedSpiritShot()
	{
		return _spsCharged;
	}

	public void chargeSoulShot()
	{
		_ssCharged = true;
	}

	public void chargeSpiritShot(final int state)
	{
		_spsCharged = state;
	}

	public int getSoulshotConsumeCount()
	{
		return getTemplate().soulshotCount;
	}

	public int getSpiritshotConsumeCount()
	{
		return getTemplate().spiritshotCount;
	}

	public boolean isDepressed()
	{
		return _depressed;
	}

	public void setDepressed(final boolean depressed)
	{
		_depressed = depressed;
	}

	public boolean isInRange()
	{
		final Player owner = getPlayer();
		return owner != null && this.getDistance(owner) < 2500.0;
	}

	public void teleportToOwner()
	{
		if(isDead())
			return;
		final Player owner = getPlayer();
		if(owner == null)
			return;
		if(owner.isInOlympiadMode())
			this.setLoc(owner.getLoc());
		else
			this.setLoc(Location.findAroundPosition(owner.getLoc(), 50, 150, owner.getGeoIndex()));
		if(_follow)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, 100);
		updateEffectIcons();
	}

	public void broadcastCharInfo()
	{
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
		final Player owner = getPlayer();
		for(final Player player : World.getAroundPlayers(this))
			if(player == owner)
				player.sendPacket(new PetInfo(this, 1));
			else
				player.sendPacket(new NpcInfo(this, player, 1));
	}

	private void sendPetInfoImpl()
	{
		final Player owner = getPlayer();
		if(owner != null)
			owner.sendPacket(new PetInfo(this, 1));
	}

	public void sendPetInfo()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_petInfoTask != null)
			{
				_petInfoTask.cancel(false);
				_petInfoTask = null;
			}
			sendPetInfoImpl();
			return;
		}
		if(_petInfoTask != null)
			return;
		_petInfoTask = ThreadPoolManager.getInstance().schedule(new PetInfoTask(), Config.USER_INFO_INTERVAL);
	}

	public void broadcastPetInfo()
	{
		updateEffectIcons();
	}

	@Override
	public void startPvPFlag(final Creature target)
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		owner.startPvPFlag(target);
	}

	@Override
	public int getPvpFlag()
	{
		final Player owner = getPlayer();
		return owner == null ? 0 : owner.getPvpFlag();
	}

	@Override
	public int getKarma()
	{
		final Player owner = getPlayer();
		return owner == null ? 0 : owner.getKarma();
	}

	@Override
	public int getTeam()
	{
		final Player owner = getPlayer();
		return owner == null ? 0 : owner.getTeam();
	}

	@Override
	public Player getPlayer()
	{
		return GameObjectsStorage.getPlayer(_ownerObjectId);
	}

	@Override
	public SummonStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new SummonStatsChangeRecorder(this);
			}
		return (SummonStatsChangeRecorder) _statsRecorder;
	}

	public boolean isSiegeWeapon()
	{
		return getNpcId() == 14737 || getNpcId() == 14768 || getNpcId() == 14839;
	}

	@Override
	public void broadcastUserInfo(final boolean force)
	{
		broadcastPetInfo();
	}

	public int getBaseRunSpd()
	{
		return getTemplate().baseRunSpd;
	}

	@Override
	public boolean isSummon()
	{
		return true;
	}

	@Override
	public Clan getClan()
	{
		final Player owner = getPlayer();
		return owner != null ? owner.getClan() : null;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		final List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		final Player owner = getPlayer();
		if(owner == forPlayer)
		{
			list.add(new PetInfo(this, 2));
			list.add(new PartySpelled(this, true));
			if(isPet())
				list.add(new PetItemList((PetInstance) this));
		}
		else
		{
			final Party party = forPlayer.getParty();
			list.add(new NpcInfo(this, forPlayer, 2));
			if(owner != null && party != null && party == owner.getParty())
				list.add(new PartySpelled(this, true));
			list.add(RelationChanged.update(forPlayer, this, forPlayer));
		}
		if(isInCombat())
			list.add(new AutoAttackStart(getObjectId()));
		if(isMoving || isFollow)
			list.add(movePacket());
		return list;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		final Player player = getPlayer();
		if(player != null)
			player.startAttackStanceTask0();
	}

	@Override
	public <E extends GlobalEvent> E getEvent(final Class<E> eventClass)
	{
		final Player player = getPlayer();
		if(player != null)
			return player.getEvent(eventClass);
		return super.getEvent(eventClass);
	}

	@Override
	public Set<GlobalEvent> getEvents()
	{
		final Player player = getPlayer();
		if(player != null)
			return player.getEvents();
		return super.getEvents();
	}

	@Override
	public boolean isServitor()
	{
		return true;
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
		return title;
	}
}
