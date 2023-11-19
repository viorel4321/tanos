package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectNegateMusic extends Abnormal
{
	public EffectNegateMusic(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		for(final Abnormal e : _effected.getAbnormalList().values())
			if(e.getSkill().isMusic())
				e.exit();
		return false;
	}
}
