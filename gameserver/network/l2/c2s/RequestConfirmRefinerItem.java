package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExConfirmVariationRefiner;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestConfirmRefinerItem extends L2GameClientPacket
{
	private static final int GEMSTONE_D = 2130;
	private static final int GEMSTONE_C = 2131;
	private int _targetItemObjId;
	private int _refinerItemObjId;

	@Override
	public void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		final ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		final ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if(targetItem == null || refinerItem == null)
			return;

		final int refinerItemId = refinerItem.getTemplate().getItemId();
		if(refinerItemId < 8723 || refinerItemId > 8762)
		{
			activeChar.sendPacket(new SystemMessage(1960));
			return;
		}

		int gemstoneCount = 0;
		int gemstoneItemId = 0;
		final SystemMessage sm = new SystemMessage(1959);
		switch(targetItem.getTemplate().getItemGrade())
		{
			case C:
			{
				gemstoneCount = 20;
				gemstoneItemId = 2130;
				sm.addNumber(Integer.valueOf(gemstoneCount));
				sm.addString("Gemstone D");
				break;
			}
			case B:
			{
				gemstoneCount = 30;
				gemstoneItemId = 2130;
				sm.addNumber(Integer.valueOf(gemstoneCount));
				sm.addString("Gemstone D");
				break;
			}
			case A:
			{
				gemstoneCount = 20;
				gemstoneItemId = 2131;
				sm.addNumber(Integer.valueOf(gemstoneCount));
				sm.addString("Gemstone C");
				break;
			}
			case S:
			{
				gemstoneCount = 25;
				gemstoneItemId = 2131;
				sm.addNumber(Integer.valueOf(gemstoneCount));
				sm.addString("Gemstone C");
				break;
			}
		}
		activeChar.sendPacket(new ExConfirmVariationRefiner(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount));
		activeChar.sendPacket(sm);
	}

	private int getLifeStoneGrade(int itemId)
	{
		itemId -= 8723;
		if(itemId < 10)
			return 0;
		if(itemId < 20)
			return 1;
		if(itemId < 30)
			return 2;
		return 3;
	}

	private int getLifeStoneLevel(int itemId)
	{
		itemId -= 10 * getLifeStoneGrade(itemId);
		itemId -= 8722;
		return itemId;
	}
}
