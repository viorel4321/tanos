package l2s.gameserver.model.base;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;

public final class EnchantSkillLearn
{
	private final int _id;
	private final int _level;
	private final String _name;
	private final int _spCost;
	private final int _baseLvl;
	private final int _minSkillLevel;
	private final int _exp;
	private final byte _rate76;
	private final byte _rate77;
	private final byte _rate78;

	public EnchantSkillLearn(final int id, final int lvl, final int minSkillLvl, final int baseLvl, final String name, final int cost, final int exp, final byte rate76, final byte rate77, final byte rate78)
	{
		_id = id;
		_level = lvl;
		_baseLvl = baseLvl;
		_minSkillLevel = minSkillLvl;
		_name = name.intern();
		_spCost = cost;
		_exp = exp;
		_rate76 = rate76;
		_rate77 = rate77;
		_rate78 = rate78;
	}

	public int getBaseLevel()
	{
		return _baseLvl;
	}

	public int getExp()
	{
		return _exp;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMinSkillLevel()
	{
		return _minSkillLevel;
	}

	public String getName()
	{
		return _name;
	}

	public int getRate(final Player ply)
	{
		int result = 0;
		switch(ply.getLevel())
		{
			case 76:
			{
				result = _rate76;
				break;
			}
			case 77:
			{
				result = _rate77;
				break;
			}
			case 78:
			{
				result = _rate78;
				break;
			}
			default:
			{
				result = _rate78;
				break;
			}
		}
		if(Config.ALLOW_ES_BONUS)
		{
			final String v = ply.getVar("BonusES");
			if(v != null)
				if(Long.parseLong(v) < System.currentTimeMillis())
				{
					ply.sendMessage(ply.isLangRus() ? "\u0412\u0430\u0448 \u0431\u043e\u043d\u0443\u0441 \u0437\u0430\u0442\u043e\u0447\u043a\u0438 \u0441\u043a\u0438\u043b\u043e\u0432 \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0441\u044f." : "Your enchant skill bonus is over.");
					ply.unsetVar("BonusES");
				}
				else
					result += Config.ES_BONUS_CHANCE;
		}
		return Math.min(result, 100);
	}

	public int getSpCost()
	{
		return _spCost;
	}
}
