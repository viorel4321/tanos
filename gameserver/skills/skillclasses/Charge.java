package l2s.gameserver.skills.skillclasses;

import java.util.Set;
import java.util.concurrent.Future;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.StatsSet;

public class Charge extends Skill
{
	private int _charges;

	public Charge(final StatsSet set)
	{
		super(set);
		_charges = set.getInteger("charges", getLevel());
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!activeChar.isPlayer())
			return false;
		final Player player = (Player) activeChar;
		if(this.getPower() <= 0.0 && getId() != 2165 && player.getIncreasedForce() >= _charges)
		{
			player.sendPacket(new SystemMessage(1196));
			player.sendPacket(new SystemMessage(113).addSkillName(getId(), getDisplayLevel()));
			return false;
		}
		if(getId() == 2165)
			player.sendPacket(new MagicSkillUse(player, player, 2165, 1, 0, 0L));
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;
		final boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss && getTargetType() != SkillTargetType.TARGET_SELF)
			activeChar.unChargeShots(false);
		for(final Creature target : targets)
			if(target != null)
			{
				if(target == activeChar)
					continue;
				if(Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0.0, activeChar, this)))
				{
					activeChar.sendPacket(new SystemMessage(43));
					target.sendPacket(new SystemMessage(42).addName(activeChar));
				}
				else
				{
					final boolean reflected = target.checkReflectSkill(activeChar, this);
					final Creature realTarget = reflected ? activeChar : target;
					final boolean shld = !getShieldIgnore() && Formulas.calcShldUse(activeChar, realTarget);
					double damage;
					if(getId() == 345 || getId() == 346)
						damage = 1.3 * Formulas.calcPhysDam(activeChar, realTarget, this, shld, false, true, ss);
					else
						damage = Formulas.calcPhysDam(activeChar, realTarget, this, shld, false, false, ss);
					realTarget.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, false, true, false, false, true);
				}
			}
		chargePlayer((Player) activeChar, getId());
	}

	private void chargePlayer(final Player player, final int skillId)
	{
		switch(skillId)
		{
			case 8:
			case 50:
			case 345:
			case 346:
			{
				player.setIncreasedForce(player.getIncreasedForce() + 1);
				createRunnable(player);
				break;
			}
			case 2165:
			{
				if(player.getIncreasedForce() < _charges)
				{
					player.setIncreasedForce(player.getIncreasedForce() + 1);
					createRunnable(player);
					break;
				}
				player.sendPacket(new SystemMessage(324));
				break;
			}
		}
	}

	private void createRunnable(final Player player)
	{
		final Future<?> lastChargeRunnable = player._lastChargeRunnable;
		if(lastChargeRunnable != null)
			lastChargeRunnable.cancel(false);
		player._lastChargeRunnable = ThreadPoolManager.getInstance().schedule(new ChargeTimer(player), 600000L);
	}

	private class ChargeTimer implements Runnable
	{
		private HardReference<Player> _playerRef;

		public ChargeTimer(final Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;
			player.setIncreasedForce(0);
		}
	}
}
