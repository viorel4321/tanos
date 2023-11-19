package l2s.gameserver.templates.item.support;

import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;

public class EnchantScroll extends EnchantItem
{
	private final FailResultType _resultType;
	private final int _minEncVisEff, _maxEncVisEff;
	private boolean _showFailEffect;

	public EnchantScroll(int itemId, int chance, int magicChance, int maxEnchant, EnchantType type, ItemGrade grade, FailResultType resultType, int minEncVisEff, int maxEncVisEff, boolean showFailEffect, boolean isAltFormula, int safeLevel, int safeLevelFull)
	{
		super(itemId, chance, magicChance, maxEnchant, type, grade, isAltFormula, safeLevel, safeLevelFull);
		_resultType = resultType;
		_minEncVisEff = minEncVisEff;
		_maxEncVisEff = maxEncVisEff;
		_showFailEffect = showFailEffect;
	}

	public FailResultType getResultType()
	{
		return _resultType;
	}

	public boolean showSuccessEffect(int enchant)
	{
		if(enchant >= _minEncVisEff && enchant <= _maxEncVisEff)
			return true;
		return false;
	}

	public boolean showFailEffect()
	{
		return _showFailEffect;
	}
}
