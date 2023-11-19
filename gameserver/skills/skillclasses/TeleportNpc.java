package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class TeleportNpc extends Skill
{
	public TeleportNpc(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
				target.abortAttack(true, true);
				target.abortCast(true, false);
				target.stopMove();
				int x = activeChar.getX();
				int y = activeChar.getY();
				final int z = activeChar.getZ();
				final int h = activeChar.getHeading();
				final int range = (int) (activeChar.getCollisionRadius() + target.getCollisionRadius());
				final int hyp = (int) Math.sqrt(range * range / 2);
				if(h < 16384)
				{
					x += hyp;
					y += hyp;
				}
				else if(h > 16384 && h <= 32768)
				{
					x -= hyp;
					y += hyp;
				}
				else if(h < 32768 && h <= 49152)
				{
					x -= hyp;
					y -= hyp;
				}
				else if(h > 49152)
				{
					x += hyp;
					y -= hyp;
				}
				target.setXYZ(x, y, z);
				target.validateLocation(1);
			}
	}
}
