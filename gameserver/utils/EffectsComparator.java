package l2s.gameserver.utils;

import java.util.Comparator;

import l2s.gameserver.model.actor.instances.creature.Abnormal;

public class EffectsComparator implements Comparator<Abnormal>
{
	private static final EffectsComparator instance;

	public static final EffectsComparator getInstance()
	{
		return EffectsComparator.instance;
	}

	@Override
	public int compare(final Abnormal e1, final Abnormal e2)
	{
		if(e1 == null || e2 == null)
			return 0;
		final boolean toggle1 = e1.getSkill().isToggle();
		final boolean toggle2 = e2.getSkill().isToggle();
		if(toggle1 && toggle2)
			return compareStartTime(e1, e2);
		if(toggle1 || toggle2)
		{
			if(toggle1)
				return 1;
			return -1;
		}
		else
		{
			final boolean offensive1 = e1.isOffensive();
			final boolean offensive2 = e2.isOffensive();
			if(offensive1 && offensive2)
				return compareStartTime(e1, e2);
			if(offensive1 || offensive2)
			{
				if(offensive1)
					return 1;
				return -1;
			}
			else
			{
				final boolean trigger1 = e1.getSkill().isTrigger();
				final boolean trigger2 = e2.getSkill().isTrigger();
				if(trigger1 && trigger2)
					return compareStartTime(e1, e2);
				if(!trigger1 && !trigger2)
					return compareStartTime(e1, e2);
				if(trigger1)
					return 1;
				return -1;
			}
		}
	}

	private int compareStartTime(final Abnormal o1, final Abnormal o2)
	{
		if(o1.getStartTime() > o2.getStartTime())
			return 1;
		if(o1.getStartTime() < o2.getStartTime())
			return -1;
		return 0;
	}

	static
	{
		instance = new EffectsComparator();
	}
}
