package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.ChangeWaitType;
import l2s.gameserver.network.l2.s2c.Revive;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;

public final class EffectFakeDeath extends Abnormal
{
	public EffectFakeDeath(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		final Player player = (Player) getEffected();
		player.setFakeDeath(true);
		player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
		player.broadcastPacket(new ChangeWaitType(player, 2));
		player.broadcastUserInfo(false);
		player.abortCast(true, false);
		player.abortAttack(true, false);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		final Player player = (Player) getEffected();
		player.setNonAggroTime(System.currentTimeMillis() + 5000L);
		player.setFakeDeath(false);
		player.broadcastPacket(new ChangeWaitType(player, 3));
		player.broadcastPacket(new Revive(player));
		player.broadcastUserInfo(false);
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;
		double manaDam = calc();
		if(getSkill().isMagic())
			manaDam = getEffected().calcStat(Stats.MP_MAGIC_SKILL_CONSUME, manaDam, null, getSkill());
		else
			manaDam = getEffected().calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, manaDam, null, getSkill());
		if(manaDam > getEffected().getCurrentMp() && getSkill().isToggle())
		{
			getEffected().sendPacket(Msg.SKILL_WAS_REMOVED_DUE_TO_LACK_OF_MP);
			return false;
		}
		getEffected().reduceCurrentMp(manaDam, null);
		return true;
	}
}
