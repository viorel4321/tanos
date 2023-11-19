package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.StatsSet;

public class ShiftAggression extends Skill
{
	public ShiftAggression(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(activeChar.getPlayer() == null)
			return;
		for(final Creature target : targets)
			if(target != null)
			{
				if(!target.isPlayer())
					continue;
				final Player player = (Player) target;
				for(final NpcInstance npc : World.getAroundNpc(activeChar, getSkillRadius(), getSkillRadius()))
				{
					final AggroList.AggroInfo ai = npc.getAggroList().get(activeChar);
					if(ai == null)
						continue;
					npc.getAggroList().addDamageHate(player, 0, ai.hate);
					npc.getAggroList().remove(activeChar, true);
				}
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
