package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.StartRotation;
import l2s.gameserver.network.l2.s2c.StopRotation;
import l2s.gameserver.skills.Env;

public class EffectTurner extends Abnormal
{
	public EffectTurner(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		return (!_effected.isNpc() || _effected.isMonster()) && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		_effected.broadcastPacket(new StartRotation(_effected.getObjectId(), _effected.getHeading(), 1, 65535));
		_effected.broadcastPacket(new StopRotation(_effected.getObjectId(), _effector.getHeading(), 65535));
		_effected.setHeading(_effector.getHeading());
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
