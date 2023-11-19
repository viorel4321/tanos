package l2s.gameserver.skills.effects;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Env;

public class EffectRelax extends Abnormal
{
	private boolean _isWereSitting;

	public EffectRelax(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		final Player player = _effected.getPlayer();
		if(player == null)
			return false;
		if(player.isMounted())
		{
			player.sendPacket(new SystemMessage(113).addSkillName(_skill.getId(), _skill.getLevel()));
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		final Player player = _effected.getPlayer();
		if(player.isMoving)
			player.stopMove();
		_isWereSitting = player.isSitting();
		player.sitDown(0);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(!_isWereSitting)
			_effected.getPlayer().standUp();
	}

	@Override
	public boolean onActionTime()
	{
		final Player player = _effected.getPlayer();
		if(player.isAlikeDead() || !player.isSitting())
			return false;
		if(player.isCurrentHpFull() && getSkill().isToggle())
		{
			getEffected().sendPacket(new SystemMessage(175));
			return false;
		}
		final double manaDam = calc();
		if(manaDam > _effected.getCurrentMp() && getSkill().isToggle())
		{
			player.sendPacket(Msg.SKILL_WAS_REMOVED_DUE_TO_LACK_OF_MP);
			return false;
		}
		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}
