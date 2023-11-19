package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectDispelDisease extends Abnormal
{
	public EffectDispelDisease(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		if(!_effected.getAbnormalList().isEmpty())
		{
			final int id = (int) calc();
			for(final Abnormal e : _effected.getAbnormalList().values())
				if(e.getSkill().getId() == id || e.getSkill().getId() == 4554)
					e.exit();
		}
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
