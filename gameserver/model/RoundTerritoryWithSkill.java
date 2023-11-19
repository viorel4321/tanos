package l2s.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoundTerritoryWithSkill extends RoundTerritory
{
	private static final Logger _log;
	private final Creature _effector;
	private final Skill _skill;

	public RoundTerritoryWithSkill(final int id, final int centerX, final int centerY, final int radius, final int zMin, final int zMax, final Creature effector, final Skill skill)
	{
		super(id, centerX, centerY, radius, zMin, zMax);
		_effector = effector;
		_skill = skill;
		if(_skill == null)
			RoundTerritoryWithSkill._log.error("L2RoundTerritoryWithSkill with null skill actor: " + effector);
	}

	@Override
	public void doEnter(final GameObject obj)
	{
		super.doEnter(obj);
		if(_effector == null || obj == null || _skill == null || !this.isInside(obj.getX(), obj.getY(), obj.getZ()))
			return;
		if(obj.isCreature() && obj.getReflectionId() == _effector.getReflectionId())
		{
			final Creature effected = (Creature) obj;
			if(_skill.checkTarget(_effector, effected, null, false, false, false) == null)
				_skill.getEffects(_effector, effected, false, false);
		}
	}

	@Override
	public void doLeave(final GameObject obj, final boolean notify)
	{
		super.doLeave(obj, notify);
		if(_effector == null || obj == null)
			return;
		if(obj.isCreature() && obj.getReflectionId() == _effector.getReflectionId())
			((Creature) obj).getAbnormalList().stop(_skill.getId());
	}

	static
	{
		_log = LoggerFactory.getLogger(RoundTerritoryWithSkill.class);
	}
}
