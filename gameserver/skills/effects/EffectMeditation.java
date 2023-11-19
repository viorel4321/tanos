package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectMeditation extends Abnormal
{
	public EffectMeditation(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.block();
		_effected.setMeditated(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.unblock();
		_effected.setMeditated(false);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;
		_effected.setCurrentMp(_effected.getCurrentMp() + calc());
		if(!getSkill().isMagic())
			_effected.setCurrentHp(getEffected().getCurrentHp() + 100.0, false);
		return true;
	}
}
