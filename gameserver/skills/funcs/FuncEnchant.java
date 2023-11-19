package l2s.gameserver.skills.funcs;

import l2s.gameserver.Config;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.item.WeaponTemplate;

public class FuncEnchant extends Func
{
	private final Stats _stat;

	public FuncEnchant(final Stats stat, final int order, final Object owner, final double value)
	{
		super(stat, order, owner);
		_stat = stat;
	}

	@Override
	public void calc(final Env env)
	{
		if(cond != null && !cond.test(env))
			return;
		final ItemInstance item = (ItemInstance) owner;
		int enchant = item.getEnchantLevel();
		if(enchant <= 0)
			return;
		if(Config.OLY_ENCHANT_LIMIT && env.character != null && env.character.isPlayer() && env.character.isInOlympiadMode())
			enchant = Math.min(item.isWeapon() ? Config.OLY_ENCHANT_LIMIT_WEAPON : Config.OLY_ENCHANT_LIMIT_ARMOR, enchant);
		int overenchant = 0;
		if(enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}
		if(_stat == Stats.MAGIC_DEFENCE || _stat == Stats.POWER_DEFENCE)
		{
			env.value += enchant + 3 * overenchant;
			return;
		}
		if(_stat == Stats.SHIELD_DEFENCE)
		{
			env.value += enchant + 2 * overenchant;
			return;
		}
		if(_stat == Stats.MAGIC_ATTACK)
		{
			switch(item.getTemplate().getItemGrade().ordinal())
			{
				case 5:
				{
					env.value += 4 * enchant + 8 * overenchant;
					break;
				}
				case 2:
				case 3:
				case 4:
				{
					env.value += 3 * enchant + 6 * overenchant;
					break;
				}
				case 0:
				case 1:
				{
					env.value += 2 * enchant + 4 * overenchant;
					break;
				}
			}
			return;
		}
		final Enum<?> itemType = item.getItemType();
		final boolean isBow = itemType == WeaponTemplate.WeaponType.BOW;
		final boolean isSword = (itemType == WeaponTemplate.WeaponType.DUALFIST || itemType == WeaponTemplate.WeaponType.DUAL || itemType == WeaponTemplate.WeaponType.BIGSWORD || itemType == WeaponTemplate.WeaponType.SWORD) && item.getTemplate().getBodyPart() == 16384;
		switch(item.getTemplate().getItemGrade().ordinal())
		{
			case 5:
			{
				if(isBow)
				{
					env.value += 10 * enchant + 20 * overenchant;
					break;
				}
				if(isSword)
				{
					env.value += 6 * enchant + 12 * overenchant;
					break;
				}
				env.value += 5 * enchant + 10 * overenchant;
				break;
			}
			case 4:
			{
				if(isBow)
				{
					env.value += 8 * enchant + 16 * overenchant;
					break;
				}
				if(isSword)
				{
					env.value += 5 * enchant + 10 * overenchant;
					break;
				}
				env.value += 4 * enchant + 8 * overenchant;
				break;
			}
			case 2:
			case 3:
			{
				if(isBow)
				{
					env.value += 6 * enchant + 12 * overenchant;
					break;
				}
				if(isSword)
				{
					env.value += 4 * enchant + 8 * overenchant;
					break;
				}
				env.value += 3 * enchant + 6 * overenchant;
				break;
			}
			case 0:
			case 1:
			{
				if(isBow)
				{
					env.value += 4 * enchant + 8 * overenchant;
					break;
				}
				env.value += 2 * enchant + 4 * overenchant;
				break;
			}
		}
	}
}
