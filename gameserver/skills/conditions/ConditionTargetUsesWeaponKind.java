package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;
import l2s.gameserver.templates.item.WeaponTemplate;

public class ConditionTargetUsesWeaponKind extends Condition
{
	private final int _weaponMask;

	public ConditionTargetUsesWeaponKind(final int weaponMask)
	{
		_weaponMask = weaponMask;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		if(env.target == null)
			return false;
		final WeaponTemplate item = env.target.getActiveWeaponItem();
		return item != null && (item.getItemType().mask() & _weaponMask) != 0x0L;
	}
}
