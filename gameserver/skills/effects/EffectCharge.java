package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectCharge extends Abnormal
{
	public EffectCharge(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;
		if(getEffected().isPlayer())
		{
			final Player player = (Player) getEffected();
			final int val = Math.max(player.getSkillLevel(8), player.getSkillLevel(50));
			final int cur = player.getIncreasedForce();
			if(val > 0 && cur < val)
				player.setIncreasedForce(cur + 1);
		}
		return true;
	}
}
