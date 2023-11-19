package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExConfirmVariationItem;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.item.ItemGrade;

public class RequestConfirmTargetItem extends L2GameClientPacket
{
	private int _itemObjId;

	@Override
	public void readImpl()
	{
		_itemObjId = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		final ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
		if(item == null)
			return;

		if(activeChar.getLevel() < 46)
		{
			activeChar.sendMessage("You have to be level 46 in order to augment an item");
			return;
		}
		if(item.isAugmented())
		{
			activeChar.sendPacket(new SystemMessage(1970));
			return;
		}

		final ItemGrade itemGrade = item.getTemplate().getItemGrade();
		final int itemType = item.getTemplate().getType2();
		if(itemGrade.ordinal() < ItemGrade.C.ordinal() || itemType != 0 || item.isHeroItem() || item.isCursed() || item.isShadowItem() || item.isTemporalItem())
		{
			activeChar.sendPacket(new SystemMessage(1960));
			return;
		}
		if(activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(1972));
			return;
		}
		if(activeChar.isDead())
		{
			activeChar.sendPacket(new SystemMessage(1974));
			return;
		}
		if(activeChar.isParalyzed())
		{
			activeChar.sendPacket(new SystemMessage(1976));
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(1977));
			return;
		}
		if(activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(1978));
			return;
		}
		activeChar.sendPacket(new ExConfirmVariationItem(_itemObjId));
		activeChar.sendPacket(new SystemMessage(1958));
	}
}
