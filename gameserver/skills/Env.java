package l2s.gameserver.skills;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public final class Env
{
	public Creature character;
	public Creature target;
	public ItemInstance item;
	public Skill skill;
	public double value;

	public Env()
	{}

	public Env(final Creature cha, final Creature tar, final Skill sk)
	{
		character = cha;
		target = tar;
		skill = sk;
	}
}
