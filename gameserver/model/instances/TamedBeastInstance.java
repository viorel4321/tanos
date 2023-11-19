package l2s.gameserver.model.instances;

import java.util.*;
import java.util.concurrent.Future;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.NpcInfo;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public final class TamedBeastInstance extends FeedableBeastInstance
{
	private static final int MAX_DISTANCE_FROM_OWNER = 2000;
	private static final int MAX_DURATION = 1200000;
	private static final int DURATION_CHECK_INTERVAL = 60000;
	private static final int DURATION_INCREASE_INTERVAL = 20000;
	private static final int BUFF_INTERVAL = 5000;
	private int ownerObjectId;
	private int _foodSkillId;
	private int _remainingTime;
	private Location _homeLoc;
	private Future<?> _buffTask;
	private Future<?> _durationCheckTask;

	public TamedBeastInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		ownerObjectId = 0;
		_remainingTime = 1200000;
		_buffTask = null;
		_durationCheckTask = null;
		setHome(this);
	}

	public TamedBeastInstance(final int objectId, final NpcTemplate template, final Player owner, final int foodSkillId, final Location loc)
	{
		super(objectId, template);
		ownerObjectId = 0;
		_remainingTime = 1200000;
		_buffTask = null;
		_durationCheckTask = null;
		setFoodType(foodSkillId);
		setHome(loc);
		setRunning();
		spawnMe(loc);
		setOwner(owner);
	}

	public void onReceiveFood()
	{
		_remainingTime += 20000;
		if(_remainingTime > 1200000)
			_remainingTime = 1200000;
	}

	public Location getHome()
	{
		return _homeLoc;
	}

	public void setHome(final Location loc)
	{
		_homeLoc = loc;
	}

	public void setHome(final Creature c)
	{
		this.setHome(c.getLoc());
	}

	public int getRemainingTime()
	{
		return _remainingTime;
	}

	public void setRemainingTime(final int duration)
	{
		_remainingTime = duration;
	}

	public int getFoodType()
	{
		return _foodSkillId;
	}

	public void setFoodType(final int foodItemId)
	{
		if(foodItemId > 0)
		{
			_foodSkillId = foodItemId;
			if(_durationCheckTask != null)
				_durationCheckTask.cancel(true);
			_durationCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckDuration(this), 60000L, 60000L);
		}
	}

	@Override
	public void onDeath(final Creature killer)
	{
		super.onDeath(killer);
		this.stopMove();
		if(_buffTask != null)
		{
			_buffTask.cancel(true);
			_buffTask = null;
		}
		if(_durationCheckTask != null)
		{
			_durationCheckTask.cancel(true);
			_durationCheckTask = null;
		}
		final Player owner = getPlayer();
		if(owner != null)
			owner.setTrainedBeast(null);
		_foodSkillId = 0;
		_remainingTime = 0;
	}

	@Override
	public Player getPlayer()
	{
		return GameObjectsStorage.getPlayer(ownerObjectId);
	}

	public void setOwner(final Player owner)
	{
		if(owner != null)
		{
			ownerObjectId = owner.getObjectId();
			setTitle(owner.getName());
			owner.setTrainedBeast(this);
			for(final Player player : World.getAroundPlayers(this))
				if(player != null && _objectId != player.getObjectId())
					player.sendPacket(new NpcInfo(this, player));
			setFollowTarget(owner);
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, 100);
			int totalBuffsAvailable = 0;
			for(final Skill skill : getTemplate().getSkills().values())
				if(skill.getSkillType() == Skill.SkillType.BUFF)
					++totalBuffsAvailable;
			if(_buffTask != null)
				_buffTask.cancel(true);
			_buffTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckOwnerBuffs(this, totalBuffsAvailable), 5000L, 5000L);
		}
		else
			doDespawn();
	}

	public void doDespawn()
	{
		this.stopMove();
		_buffTask.cancel(true);
		_durationCheckTask.cancel(true);
		final Player owner = getPlayer();
		if(owner != null)
			owner.setTrainedBeast(null);
		setTarget(null);
		_buffTask = null;
		_durationCheckTask = null;
		_foodSkillId = 0;
		_remainingTime = 0;
		onDecay();
	}

	public void onOwnerGotAttacked(final Creature attacker)
	{
		final Player owner = getPlayer();
		if(owner == null || !owner.isOnline())
		{
			doDespawn();
			return;
		}
		if(!this.isInRange(owner, 2000L))
		{
			setFollowTarget(owner);
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, 100);
			return;
		}
		if(owner.isDead())
			return;
		final double HPRatio = owner.getCurrentHpRatio();
		if(HPRatio >= 0.8 && attacker != null)
		{
			final HashMap<Integer, Skill> skills = getTemplate().getSkills();
			for(final Skill skill : skills.values())
				if(skill.isOffensive() && attacker.getAbnormalList().getEffectsBySkill(skill) == null && Rnd.nextBoolean())
				{
					setTarget(attacker);
					doCast(skill, attacker, true);
				}
		}
		else if(HPRatio < 0.5)
		{
			int chance = 1;
			if(HPRatio < 0.25)
				chance = 2;
			final HashMap<Integer, Skill> skills2 = getTemplate().getSkills();
			for(final Skill skill2 : skills2.values())
				if(!skill2.isOffensive() && owner.getAbnormalList().getEffectsBySkill(skill2) == null && Rnd.chance(chance * 20))
				{
					setTarget(owner);
					doCast(skill2, owner, true);
					return;
				}
		}
		setFollowTarget(owner);
		getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, 100);
	}

	private class CheckDuration implements Runnable
	{
		private TamedBeastInstance _tamedBeast;

		CheckDuration(final TamedBeastInstance tamedBeast)
		{
			_tamedBeast = tamedBeast;
		}

		@Override
		public void run()
		{
			final int foodTypeSkillId = _tamedBeast.getFoodType();
			final Player owner = _tamedBeast.getPlayer();
			_tamedBeast.setRemainingTime(_tamedBeast.getRemainingTime() - 60000);
			ItemInstance item = null;
			if(foodTypeSkillId == 2188)
				item = owner.getInventory().getItemByItemId(6643);
			else if(foodTypeSkillId == 2189)
				item = owner.getInventory().getItemByItemId(6644);
			if(item != null && item.getCount() >= 1L)
			{
				final GameObject oldTarget = owner.getTarget();
				owner.setTarget(_tamedBeast);
				final Set<Creature> targets = new HashSet<Creature>();
				targets.add(_tamedBeast);
				owner.callSkill(_tamedBeast, SkillTable.getInstance().getInfo(foodTypeSkillId, 1), targets, true);
				owner.setTarget(oldTarget);
			}
			else if(_tamedBeast.getRemainingTime() < 900000)
				_tamedBeast.setRemainingTime(-1);
			if(_tamedBeast.getRemainingTime() <= 0)
				_tamedBeast.doDespawn();
		}
	}

	private class CheckOwnerBuffs implements Runnable
	{
		private TamedBeastInstance _tamedBeast;
		private int _numBuffs;

		CheckOwnerBuffs(final TamedBeastInstance tamedBeast, final int numBuffs)
		{
			_tamedBeast = tamedBeast;
			_numBuffs = numBuffs;
		}

		@Override
		public void run()
		{
			final Player owner = _tamedBeast.getPlayer();
			if(owner == null || !owner.isOnline())
			{
				doDespawn();
				return;
			}
			setRunning();
			if(!TamedBeastInstance.this.isInRange(owner, 2000L))
			{
				setFollowTarget(owner);
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, 100);
				return;
			}
			if(owner.isDead())
				return;
			int totalBuffsOnOwner = 0;
			int i = 0;
			final int rand = Rnd.get(_numBuffs);
			Skill buffToGive = null;
			final HashMap<Integer, Skill> skills = _tamedBeast.getTemplate().getSkills();
			for(final Skill skill : skills.values())
				if(skill.getSkillType() == Skill.SkillType.BUFF)
				{
					if(i == rand)
						buffToGive = skill;
					++i;
					if(owner.getAbnormalList().getEffectsBySkill(skill) == null)
						continue;
					++totalBuffsOnOwner;
				}
			if(_numBuffs * 2 / 3 > totalBuffsOnOwner)
			{
				_tamedBeast.setTarget(owner);
				_tamedBeast.doCast(buffToGive, owner, true);
			}
			setFollowTarget(owner);
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, 100);
		}
	}
}
