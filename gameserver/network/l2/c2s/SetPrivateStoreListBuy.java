package l2s.gameserver.network.l2.c2s;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private int _count;
	private int[] _items;

	@Override
	public void readImpl()
	{
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 4];
		for(int i = 0; i < _count; ++i)
		{
			_items[i * 4 + 0] = readD();
			_items[i * 4 + 3] = readH();
			readH();
			_items[i * 4 + 1] = readD();
			_items[i * 4 + 2] = readD();
			if(_items[i * 4 + 1] < 0)
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
		if(_items == null)
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
		final ConcurrentLinkedQueue<TradeItem> listbuy = new ConcurrentLinkedQueue<TradeItem>();
		int totalCost = 0;
		for(int x = 0; x < _count; ++x)
		{
			if(_items[x * 4 + 1] < 1)
				--_count;
			else
			{
				final TradeItem temp = new TradeItem();
				temp.setItemId(_items[x * 4 + 0]);
				temp.setCount(_items[x * 4 + 1]);
				temp.setOwnersPrice(_items[x * 4 + 2]);
				temp.setEnchantLevel(_items[x * 4 + 3]);
				totalCost += temp.getOwnersPrice() * temp.getCount();
				if(temp.getOwnersPrice() < 0 || temp.getCount() < 0)
				{
					TradeList.cancelStore(activeChar);
					return;
				}
				if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
				{
					if(temp.getOwnersPrice() > activeChar.getAdena())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE));
						TradeList.cancelStore(activeChar);
						return;
					}
				}
				else
				{
					if(temp.getOwnersPrice() > Functions.getItemCount(activeChar, currecyId))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE));
						TradeList.cancelStore(activeChar);
						return;
					}
				}
				listbuy.add(temp);
			}
		}
		if(_count > 0)
		{
			activeChar.setBuyList(listbuy);
			activeChar.setPrivateStoreType((short) 3);
			activeChar.sitDown(0);
			final int id = activeChar.getObjectId();
			ThreadPoolManager.getInstance().schedule(new Runnable(){
				@Override
				public void run()
				{
					final Player player = GameObjectsStorage.getPlayer(id);
					if(player == null)
						return;
					player.broadcastPrivateStoreMsg(1);
					player.broadcastUserInfo(true);
				}
			}, 2500L);
		}
		else
			TradeList.cancelStore(activeChar);
	}
}
