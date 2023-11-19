package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.CastleChamberlainInstance;
import l2s.gameserver.model.instances.ClanHallManagerInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MercManagerInstance;
import l2s.gameserver.model.instances.MerchantInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;

public class RequestPreviewItem extends L2GameClientPacket
{
	protected static Logger _log;
	protected Future<?> _removeWearItemsTask;
	private int _unknow;
	private int _listId;
	private int _count;
	private short[] _items;
	protected Player _cha;

	@Override
	public void readImpl()
	{
		_cha = getClient().getActiveChar();
		_unknow = readD();
		_listId = readD();
		_count = readD();
		if(_count * 4 > _buf.remaining() || _count > 32767 || _count <= 0)
		{
			_count = 0;
			return;
		}
		_items = new short[_count];
		for(int i = 0; i < _count; ++i)
			_items[i] = (short) readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = _cha;
		if(!Config.WEAR_TEST_ENABLED)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPreviewItem.Disabled"));
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		final NpcInstance npc = activeChar.getLastNpc();
		final boolean isValidMerchant = npc instanceof ClanHallManagerInstance || npc instanceof MerchantInstance || npc instanceof MercManagerInstance || npc instanceof CastleChamberlainInstance;
		if(!activeChar.isGM() && (npc == null || !isValidMerchant || !activeChar.isInRange(npc.getLoc(), 150L)))
		{
			activeChar.sendActionFailed();
			return;
		}
		if(_count < 1)
		{
			activeChar.sendActionFailed();
			return;
		}
		final ArrayList<ItemInstance> items = new ArrayList<ItemInstance>(_count);
		for(int i = 0; i < _count; ++i)
		{
			final short itemId = _items[i];
			final int cnt = 1;
			final ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(cnt);
			items.add(inst);
		}
		int neededMoney = 0;
		int finalLoad = 0;
		int finalCount = 0;
		int needsSpace = 2;
		int weight = 0;
		final int currentMoney = activeChar.getAdena();
		for(final ItemInstance item : items)
		{
			final int itemId2 = item.getItemId();
			final int cnt2 = item.getIntegerLimitedCount();
			if(item.getTemplate().isStackable())
			{
				needsSpace = 1;
				if(activeChar.getInventory().getItemByItemId(itemId2) != null)
					needsSpace = 0;
			}
			final int price = 10;
			weight = item.getTemplate().getWeight();
			neededMoney += cnt2 * price;
			if(neededMoney > Integer.MAX_VALUE)
			{
				RequestPreviewItem._log.warn("Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.");
				activeChar.sendActionFailed();
				return;
			}
			finalLoad += cnt2 * weight;
			if(needsSpace == 2)
				finalCount += cnt2;
			else
			{
				if(needsSpace != 1)
					continue;
				++finalCount;
			}
		}
		if(neededMoney > currentMoney || neededMoney < 0 || currentMoney <= 0)
		{
			this.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			for(final ItemInstance j : items)
				j.deleteMe();
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.getInventory().validateWeight(finalLoad))
		{
			this.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			for(final ItemInstance j : items)
				j.deleteMe();
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.getInventory().validateCapacity(finalCount))
		{
			this.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			for(final ItemInstance j : items)
				j.deleteMe();
			activeChar.sendActionFailed();
			return;
		}
		activeChar.reduceAdena(neededMoney, true);
		activeChar.sendPacket(new SystemMessage(672).addNumber(Integer.valueOf(neededMoney)));
		for(final ItemInstance item : items)
		{
			item.setWear(true);
			activeChar.getInventory().addItem(item);
			activeChar.getInventory().equipItem(item, true);
		}
		activeChar.broadcastUserInfo(true);
		this.sendPacket(new ItemList(activeChar, false));
		if(_removeWearItemsTask == null)
			_removeWearItemsTask = ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(), 10000L);
	}

	static
	{
		RequestPreviewItem._log = LoggerFactory.getLogger(RequestPreviewItem.class);
	}

	class RemoveWearItemsTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				final ItemInstance[] items2;
				final ItemInstance[] items = items2 = _cha.getInventory().getItems();
				for(final ItemInstance i : items2)
					if(i.isWear())
						_cha.getInventory().destroyItem(i.getObjectId(), 1L, true);
				_cha.broadcastUserInfo(true);
				_cha.sendPacket(new SystemMessage(1306));
				RequestPreviewItem.this.sendPacket(new ItemList(_cha, false));
			}
			catch(Throwable e)
			{
				RequestPreviewItem._log.error("", e);
			}
		}
	}
}
