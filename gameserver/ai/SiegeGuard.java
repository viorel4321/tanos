package l2s.gameserver.ai;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

public abstract class SiegeGuard extends DefaultAI
{
	public SiegeGuard(final NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = 1000;
	}

	@Override
	public int getMaxPathfindFails()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxAttackTimeout()
	{
		return 0;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	public boolean canSeeInSilentMove(final Playable target)
	{
		return !target.isSilentMoving() || Rnd.chance(10);
	}

	@Override
	protected boolean isAggressive()
	{
		return true;
	}

	@Override
	protected boolean isGlobalAggro()
	{
		return true;
	}

	@Override
	protected void onEvtAggression(final Creature target, final int aggro)
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return;
		if(target == null || !actor.isAutoAttackable(target))
			return;
		super.onEvtAggression(target, aggro);
	}

	@Override
	protected boolean thinkActive()
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.isActionsDisabled())
			return true;
		if(_def_think)
		{
			if(doTask())
				clearTasks();
			return true;
		}
		final long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > Config.AGGRO_CHECK_INTERVAL)
		{
			_checkAggroTimestamp = now;
			int count = 0;
			final List<Creature> chars = World.getAroundCharacters(actor, Config.AGGRO_CHECK_RADIUS, Config.AGGRO_CHECK_HEIGHT);
			final int size = Math.min(chars.size(), 1000);
			while(!chars.isEmpty())
			{
				if(++count > size)
					break;
				final Creature target = getNearestTarget(chars);
				if(target == null)
					break;
				if(checkAggression(target))
				{
					actor.getAggroList().addDamageHate(target, 0, 2);
					if(target.isSummon())
						actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
					actor.setRunning();
					this.setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					return true;
				}
				chars.remove(target);
			}
		}
		final Location sloc = actor.getSpawnedLoc();
		if(!actor.isInRange(sloc, 250L))
		{
			teleportHome();
			return true;
		}
		return false;
	}

	@Override
	protected Creature prepareTarget()
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return null;
		final List<Creature> hateList = actor.getAggroList().getHateList(MAX_PURSUE_RANGE);
		Creature hated = null;
		for(final Creature cha : hateList)
		{
			if(checkTarget(cha, MAX_PURSUE_RANGE))
			{
				hated = cha;
				break;
			}
			actor.getAggroList().remove(cha, true);
		}
		if(hated != null)
		{
			setAttackTarget(hated);
			return hated;
		}
		return null;
	}

	@Override
	protected boolean canAttackCharacter(final Creature target)
	{
		return getActor().isAutoAttackable(target);
	}
}
