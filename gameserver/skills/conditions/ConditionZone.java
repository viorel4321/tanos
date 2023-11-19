package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Zone;
import l2s.gameserver.skills.Env;

public class ConditionZone extends Condition
{
	private final Zone.ZoneType _zoneType;

	public ConditionZone(final String zoneType)
	{
		_zoneType = Zone.ZoneType.valueOf(zoneType);
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.isPlayer() && env.character.isInZone(_zoneType);
	}
}
