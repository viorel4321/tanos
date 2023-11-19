package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExConfirmCancelItem;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestConfirmCancelItem extends L2GameClientPacket
{
	private int _itemId;

	@Override
	public void readImpl()
	{
		_itemId = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		final ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!item.isAugmented())
		{
			activeChar.sendPacket(new SystemMessage(1964));
			return;
		}
		int price = 0;
		switch(item.getTemplate().getItemGrade())
		{
			case C:
			{
				if(item.getTemplate().getCrystalCount() < 1720)
				{
					price = 95000;
					break;
				}
				if(item.getTemplate().getCrystalCount() < 2452)
				{
					price = 150000;
					break;
				}
				price = 210000;
				break;
			}
			case B:
			{
				if(item.getTemplate().getCrystalCount() < 1746)
				{
					price = 240000;
					break;
				}
				price = 270000;
				break;
			}
			case A:
			{
				if(item.getTemplate().getCrystalCount() < 2160)
				{
					price = 330000;
					break;
				}
				if(item.getTemplate().getCrystalCount() < 2824)
				{
					price = 390000;
					break;
				}
				price = 420000;
				break;
			}
			case S:
			{
				price = 480000;
				break;
			}
			default:
			{
				return;
			}
		}
		price *= (int) Config.AUGMENT_CANCEL_PRICE_MOD;
		activeChar.sendPacket(new ExConfirmCancelItem(item, price));
	}
}
