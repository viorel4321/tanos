package l2s.gameserver.templates;

import org.napile.primitive.sets.IntSet;

import l2s.gameserver.Config;
import l2s.gameserver.model.entity.SevenSigns;

public class MerchantGuard
{
	private int _itemId;
	private int _npcId;
	private int _max;
	private IntSet _ssq;

	public MerchantGuard(final int itemId, final int npcId, final int max, final IntSet ssq)
	{
		_itemId = itemId;
		_npcId = npcId;
		_max = max;
		_ssq = ssq;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public int getMax()
	{
		return _max;
	}

	public boolean isValidSSQPeriod()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return true;
		return SevenSigns.getInstance().getCurrentPeriod() == 3 && _ssq.contains(SevenSigns.getInstance().getSealOwner(3));
	}
}
