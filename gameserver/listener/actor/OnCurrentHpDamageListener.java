package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

public interface OnCurrentHpDamageListener extends CharListener
{
	void onCurrentHpDamage(final Creature p0, final double p1, final Creature p2, final Skill p3);
}
