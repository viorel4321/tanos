package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.StatsSet;

public class DeleteHateOfMe extends Skill
{
	private final boolean _cancelSelfTarget;

	public DeleteHateOfMe(final StatsSet set)
	{
		super(set);
		_cancelSelfTarget = set.getBool("cancelSelfTarget", false);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isNpc() && Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					final NpcInstance npc = (NpcInstance) target;
					npc.getAggroList().remove(activeChar, true);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				}
				if(_cancelSelfTarget)
					activeChar.setTarget(null);
				this.getEffects(activeChar, target, true, false);
			}
	}
}
