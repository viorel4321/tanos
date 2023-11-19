package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Player;
import l2s.gameserver.skills.Env;
import l2s.gameserver.templates.item.ArmorTemplate;

public class ConditionUsingArmor extends Condition
{
	private final ArmorTemplate.ArmorType _armor;

	public ConditionUsingArmor(final ArmorTemplate.ArmorType armor)
	{
		_armor = armor;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.isPlayer() && ((Player) env.character).isWearingArmor(_armor);
	}
}
