package l2s.gameserver.skills.skillclasses;

import java.util.List;
import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.StatsSet;

public class Cancel extends Skill
{
	private final int type;
	private final int _cancelRate;
	private final int _negateMinCount;
	private final int _negateCount;
	private final boolean _staticRate;
	private final boolean _groupRate;

	public Cancel(final StatsSet set)
	{
		super(set);
		final String dispelType = set.getString("dispelType", "");
		type = dispelType.contains("positive") ? 1 : dispelType.contains("negative") ? 2 : 0;
		_cancelRate = set.getInteger("cancelRate", -1);
		_negateMinCount = set.getInteger("negateMinCount", 1);
		_negateCount = set.getInteger("negateCount", 5);
		_staticRate = set.getBool("staticRate", false);
		_groupRate = set.getBool("groupRate", false);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null && !target.isDoor() && !target.isInvul())
			{
				final boolean reflected = type == 2 && target.checkReflectSkill(activeChar, this);
				final Creature realTarget = reflected ? activeChar : target;
				final double chance = _staticRate ? _cancelRate == -1 ? 100 : _cancelRate : Formulas.cancelChance(activeChar, realTarget, this, _cancelRate);
				if(_groupRate && !Rnd.chance(chance))
				{
					if(showActivate())
						activeChar.sendPacket(new SystemMessage(1597).addSkillName(getDisplayId(), getDisplayLevel()));
				}
				else if(chance > 0.0)
				{
					int counter = 0;
					final int negateCount = Rnd.get(_negateMinCount, _negateCount);
					final List<Abnormal> eff = realTarget.getAbnormalList().values();
					if(_groupRate && showActivate())
						activeChar.sendPacket(new SystemMessage(1595).addSkillName(getDisplayId(), getDisplayLevel()));
					while(counter < negateCount && eff.size() > 0)
					{
						final Abnormal e = eff.remove(Rnd.get(eff.size()));
						final Skill skill = e.getSkill();
						if((_groupRate || Rnd.chance(chance)) && skill.isCancelable() && !e.isCubic() && (type == 0 || type == 1 && !e.isOffensive() || type == 2 && e.isOffensive()))
						{
							e.restore(type == 1);
							++counter;
							realTarget.sendPacket(new SystemMessage(749).addSkillName(skill.getId(), skill.getDisplayLevel()));
						}
					}
				}
				this.getEffects(activeChar, target, false, false, reflected);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	@Override
	public boolean isOffensive()
	{
		return type != 2;
	}
}
