package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectRemoveTarget extends Abnormal
{
	public EffectRemoveTarget(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		final Creature target = getEffected();
		if(target.getTarget() == null)
			return;
		if(target.getAI() instanceof DefaultAI)
			((DefaultAI) target.getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);
		target.setTarget(null);
		target.abortCast(true, true);
		target.abortAttack(true, true);
		target.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
