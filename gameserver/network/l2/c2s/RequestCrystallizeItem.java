package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.utils.Log;

public class RequestCrystallizeItem extends L2GameClientPacket
{
	public static short[] _crystalId;
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(new SystemMessage(1065));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(1470));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		final ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null || !item.canBeCrystallized(activeChar, true))
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.getInventory().destroyItem(item, 1L, true);
		final int crystalAmount = item.getTemplate().getCrystalCount();
		final short crystalId = RequestCrystallizeItem._crystalId[item.getTemplate().getItemGrade().ordinal()];
		final ItemInstance createditem = ItemTable.getInstance().createItem(crystalId);
		createditem.setCount(crystalAmount);
		final ItemInstance addedItem = activeChar.getInventory().addItem(createditem);
		activeChar.sendPacket(new SystemMessage(1198));
		activeChar.sendPacket(new SystemMessage(29).addItemName(Short.valueOf(crystalId)).addNumber(Integer.valueOf(crystalAmount)));
		Log.LogItem(activeChar, "Crystalize", item);
		activeChar.updateStats();
	}

	static
	{
		RequestCrystallizeItem._crystalId = new short[] { 0, 1458, 1459, 1460, 1461, 1462 };
	}
}
