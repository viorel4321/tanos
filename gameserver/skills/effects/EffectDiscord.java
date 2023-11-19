package l2s.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectDiscord extends Abnormal
{
	public EffectDiscord(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		final int skilldiff = _effected.getLevel() - _skill.getMagicLevel();
		final int lvldiff = _effected.getLevel() - _effector.getLevel();
		if(skilldiff > 10 || skilldiff > 5 && Rnd.chance(30) || Rnd.chance(Math.abs(lvldiff) * 2))
			return false;
		final boolean multitargets = _skill.isAoE();
		if(!_effected.isMonster())
		{
			if(!multitargets)
				getEffector().sendPacket(Msg.TARGET_IS_INCORRECT);
			return false;
		}
		if(_effected.isFearImmune() || _effected.isRaid())
		{
			if(!multitargets)
				getEffector().sendPacket(Msg.TARGET_IS_INCORRECT);
			return false;
		}
		if(_effected.isSummon() && ((Servitor) _effected).isSiegeWeapon())
		{
			if(!multitargets)
				getEffector().sendPacket(Msg.TARGET_IS_INCORRECT);
			return false;
		}
		if(_effected.isInZonePeace())
		{
			if(!multitargets)
				getEffector().sendPacket(Msg.YOU_CANNOT_ATTACK_IN_THE_PEACE_ZONE);
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startConfused();
		onActionTime();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopConfused();
		_effected.setWalking();
		_effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public boolean onActionTime()
	{
		final List<Creature> targetList = new ArrayList<Creature>();
		for(final Creature character : _effected.getAroundCharacters(900, 200))
			if(character.isNpc() && character != getEffected())
				targetList.add(character);
		if(targetList.size() == 0)
			return true;
		final Creature target = targetList.get(Rnd.get(targetList.size()));
		_effected.setRunning();
		_effected.getAI().Attack(target, true, false);
		return false;
	}
}
