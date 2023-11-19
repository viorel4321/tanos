package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.StatsSet;

public class Aggression extends Skill
{
	private final boolean _unaggring;
	private final boolean _silent;
	private final boolean _attack;

	public Aggression(final StatsSet set)
	{
		super(set);
		_unaggring = set.getBool("unaggroing", false);
		_silent = set.getBool("silent", false);
		_attack = set.getBool("attack", false);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		int effect = _effectPoint;
		if(isSSPossible() && (activeChar.getChargedSoulShot() || activeChar.getChargedSpiritShot() > 0))
			effect *= 2;
		for(final Creature target : targets)
			if(target != null)
			{
				if(!target.isAutoAttackable(activeChar))
					continue;
				if(target.isNpc())
				{
					if(_unaggring)
					{
						if(activeChar.isPlayable())
							((NpcInstance) target).getAggroList().addDamageHate(activeChar, 0, -effect);
					}
					else
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, effect);
						if(!_silent)
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, this, 0);
					}
				}
				else if(target.isPlayable() && !target.isDebuffImmune() && !target.isInvul())
				{
					target.setTarget(activeChar);
					if(_attack)
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
				}
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
