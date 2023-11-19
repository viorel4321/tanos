package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.SiegeHeadquarterInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.StatsSet;

public class Heal extends Skill
{
	private final boolean _ignoreHpEff;
	private final boolean _staticPower;
	private final boolean _newFormula;

	public Heal(final StatsSet set)
	{
		super(set);
		_ignoreHpEff = set.getBool("ignoreHpEff", false);
		_staticPower = set.getBool("staticPower", isHandler());
		_newFormula = set.getBool("newFormula", false);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		return target != null && !target.isDoor() && !(target instanceof SiegeHeadquarterInstance) && super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		double hp = _power;
		if(_newFormula && !isHandler())
			hp += 0.1 * _power * Math.sqrt(activeChar.getMAtk(null, this) / 333);
		final int sps = isSSPossible() && getHpConsume() == 0 ? activeChar.getChargedSpiritShot() : 0;
		if(sps == 2)
			hp *= 1.5;
		else if(sps == 1)
			hp *= 1.3;
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				if(target != activeChar)
				{
					if(target.isPlayer() && target.isCursedWeaponEquipped())
						continue;
					if(activeChar.isPlayer() && activeChar.isCursedWeaponEquipped())
						continue;
				}
				double addToHp = 0.0;
				if(_staticPower)
					addToHp = _power;
				else
				{
					addToHp = hp * (_ignoreHpEff ? 100.0 : target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, null, null)) / 100.0;
					addToHp = activeChar.calcStat(Stats.HEAL_POWER, addToHp, target, this);
				}
				if(addToHp > 0.0)
				{
					target.setCurrentHp(addToHp + target.getCurrentHp(), false);
					if(getId() == 4051)
						target.sendPacket(new SystemMessage(25));
					if(target.isPlayer())
						if(activeChar != target)
							target.sendPacket(new SystemMessage(1067).addNumber(Integer.valueOf((int) addToHp)).addString(activeChar.getName()));
						else
							target.sendPacket(new SystemMessage(1066).addNumber(Integer.valueOf((int) addToHp)));
				}
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
