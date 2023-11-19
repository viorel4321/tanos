package l2s.gameserver.model.entity.olympiad;

import l2s.gameserver.Config;

public enum CompType
{
	NON_CLASSED(Config.ALT_OLY_NONCLASSED_RITEM_C, 5),
	CLASSED(Config.ALT_OLY_CLASSED_RITEM_C, 3);

	private int _reward;
	private int _div;

	private CompType(final int reward, final int div)
	{
		_reward = reward;
		_div = div;
	}

	public int getReward()
	{
		return _reward;
	}

	public int getDiv()
	{
		return _div;
	}
}
