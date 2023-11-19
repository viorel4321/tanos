package l2s.gameserver.network.l2.c2s;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class SetPrivateStoreListSell extends L2GameClientPacket
{
	private int _count;
	private boolean _package;
	private int[] _items;

	@Override
	public void readImpl()
	{
		_package = readD() == 1;
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 3];
		for(int i = 0; i < _count; ++i)
		{
			_items[i * 3] = readD();
			_items[i * 3 + 1] = readD();
			_items[i * 3 + 2] = readD();
			if(_items[i * 3 + 1] < 0)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_items == null || _count <= 0 || activeChar.isMounted())
		{
			TradeList.cancelStore(activeChar);
			return;
		}
		if(!activeChar.checksForShop(false))
		{
			TradeList.cancelStore(activeChar);
			return;
		}
		final int maxSlots = activeChar.getTradeLimit();
		if(_count > maxSlots)
		{
			activeChar.sendPacket(new SystemMessage(1036));
			TradeList.cancelStore(activeChar);
			return;
		}
		final int currecyId = activeChar.getPrivateStoreCurrecy();
		final ConcurrentLinkedQueue<TradeItem> listsell = new ConcurrentLinkedQueue<TradeItem>();
		int count = _count;
		activeChar.getInventory().writeLock();
		try
		{
			for(int x = 0; x < _count; ++x)
			{
				final int objectId = _items[x * 3];
				long cnt = _items[x * 3 + 1];
				final long price = _items[x * 3 + 2];
				final ItemInstance itemToSell = activeChar.getInventory().getItemByObjectId(objectId);
				if(cnt < 1L || itemToSell == null || itemToSell.isWear() || !itemToSell.canBeTraded(activeChar))
				{
					--count;
					continue;
				}

				if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
				{
					if((cnt * price + activeChar.getInventory().getAdena()) > Integer.MAX_VALUE)
					{
						--count;
						continue;
					}
				}
				else
				{
					if((cnt * price + Functions.getItemCount(activeChar, currecyId)) > Integer.MAX_VALUE)
					{
						--count;
						continue;
					}
				}

				if(activeChar.getEnchantScroll() != null && itemToSell.getObjectId() == activeChar.getEnchantScroll().getObjectId())
					activeChar.setEnchantScroll(null);
				if(cnt > itemToSell.getIntegerLimitedCount())
					cnt = itemToSell.getIntegerLimitedCount();
				final TradeItem temp = new TradeItem();
				temp.setObjectId(objectId);
				temp.setCount((int) cnt);
				temp.setOwnersPrice((int) price);
				temp.setItemId(itemToSell.getItemId());
				temp.setEnchantLevel(itemToSell.getEnchantLevel());
				listsell.add(temp);
			}
		}
		catch(Exception ex)
		{
			//
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		if(count != 0)
		{
			activeChar.setSellList(listsell);
			activeChar.setPrivateStoreType(_package ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL);
			activeChar.sitDown(0);
			final int id = activeChar.getObjectId();
			ThreadPoolManager.getInstance().schedule(() -> {
				final Player player = GameObjectsStorage.getPlayer(id);
				if(player == null)
					return;
				player.broadcastPrivateStoreMsg(2);
				player.broadcastUserInfo(true);
			}, 2500L);
		}
		else
			TradeList.cancelStore(activeChar);
	}
}
