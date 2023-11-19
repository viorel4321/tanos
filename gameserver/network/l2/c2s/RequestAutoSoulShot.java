package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestAutoSoulShot extends L2GameClientPacket
{
	private int _itemId;
	private boolean _type;

	@Override
	public void readImpl()
	{
		_itemId = readD();
		_type = readD() == 1;
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.getPrivateStoreType() != 0 || player.isDead())
			return;

		final ItemInstance item = player.getInventory().findItemByItemId(_itemId);
		if(item == null)
			return;

		if(!_type)
		{
			player.removeAutoSoulShot(_itemId);
			player.sendPacket(new ExAutoSoulShot(_itemId, false));
			player.sendPacket(new SystemMessage(1434).addString(item.getTemplate().getName()));
			if(player.recording)
				player.recBot(10, 0, _itemId, 0, 0, 0, 0);
			return;
		}

		if(player.isInTrade())
		{
			player.sendPacket(new SystemMessage(149));
			return;
		}

		if(_itemId >= 6645 && _itemId <= 6647 && player.getServitor() == null)
		{
			player.sendPacket(new SystemMessage(1676));
			return;
		}

		final IItemHandler handler = item.getTemplate().getHandler();
		if(handler == null)
			return;

		if(handler.useItem(player, item, false))
		{
			player.addAutoSoulShot(_itemId);
			player.sendPacket(new ExAutoSoulShot(_itemId, true));
			player.sendPacket(new SystemMessage(1433).addItemName(Integer.valueOf(_itemId)));
			if(player.recording)
				player.recBot(10, 1, _itemId, 0, 0, 0, 0);
		}
	}
}
