package l2s.gameserver.ai;

import l2s.gameserver.Config;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;

public class Guard extends Fighter
{
	public Guard(final NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean canAttackCharacter(final Creature target)
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return false;
		if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
		{
			final AggroList.AggroInfo ai = actor.getAggroList().get(target);
			return ai != null && ai.hate > 0;
		}
		return target.isMonster() || target.isPlayable();
	}

	@Override
	public boolean checkAggression(final Creature target)
	{
		final NpcInstance actor = getActor();
		return actor != null && getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && isGlobalAggro() && (!target.isPlayable() || target.getKarma() != 0) && (!target.isMonster() || Config.GUARD_ATTACK_AGGRO_MOB && ((MonsterInstance) target).isAggressive()) && super.checkAggression(target);
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
	public boolean canSeeInSilentMove(final Playable target)
	{
		return true;
	}
}
