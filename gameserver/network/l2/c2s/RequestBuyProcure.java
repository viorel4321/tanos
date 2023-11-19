package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.ManorManagerInstance;
import l2s.gameserver.network.l2.s2c.InventoryUpdate;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestBuyProcure extends L2GameClientPacket
{
	private int _listId;
	private int _count;
	private int[] _items;
	private List<CastleManorManager.CropProcure> _procureList;

	public RequestBuyProcure()
	{
		_procureList = new ArrayList<CastleManorManager.CropProcure>();
	}

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count > 500 || _count < 0)
		{
			_count = 0;
			return;
		}
		_items = new int[_count * 2];
		for(int i = 0; i < _count; ++i)
		{
			final long servise = readD();
			final int itemId = readD();
			_items[i * 2 + 0] = itemId;
			final long cnt = readD();
			if(cnt > Integer.MAX_VALUE || cnt < 1L)
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[i * 2 + 1] = (int) cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return;
		final GameObject target = player.getTarget();
		if(_count < 1)
		{
			player.sendActionFailed();
			return;
		}
		final long subTotal = 0L;
		final int tax = 0;
		int slots = 0;
		int weight = 0;
		final ManorManagerInstance manor = target != null && target instanceof ManorManagerInstance ? (ManorManagerInstance) target : null;
		for(int i = 0; i < _count; ++i)
		{
			final int itemId = _items[i * 2 + 0];
			final int count = _items[i * 2 + 1];
			final int price = 0;
			if(count < 0 || count > Integer.MAX_VALUE)
			{
				this.sendPacket(new SystemMessage(351));
				return;
			}
			final ItemTemplate template = ItemTable.getInstance().getTemplate(Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, 0).getReward()));
			weight += count * template.getWeight();
			if(!template.isStackable())
				slots += count;
			else if(player.getInventory().getItemByItemId(itemId) == null)
				++slots;
		}
		if(!player.getInventory().validateWeight(weight))
		{
			this.sendPacket(new SystemMessage(422));
			return;
		}
		if(!player.getInventory().validateCapacity(slots))
		{
			this.sendPacket(new SystemMessage(129));
			return;
		}
		final InventoryUpdate playerIU = new InventoryUpdate();
		_procureList = manor.getCastle().getCropProcure(0);
		for(int j = 0; j < _count; ++j)
		{
			final int itemId2 = _items[j * 2 + 0];
			int count2 = _items[j * 2 + 1];
			if(count2 < 0)
				count2 = 0;
			final int rewradItemId = Manor.getInstance().getRewardItem(itemId2, manor.getCastle().getCrop(itemId2, 0).getReward());
			int rewradItemCount = 1;
			rewradItemCount = count2 / rewradItemCount;
			final ItemInstance item = player.getInventory().addItem(rewradItemId, rewradItemCount);
			final ItemInstance iteme = player.getInventory().destroyItemByItemId(itemId2, count2, true);
			if(item != null)
				if(iteme != null)
				{
					playerIU.addRemovedItem(iteme);
					if(item.getCount() > rewradItemCount)
						playerIU.addModifiedItem(item);
					else
						playerIU.addNewItem(item);
					SystemMessage sm = new SystemMessage(53);
					sm.addItemName(Integer.valueOf(rewradItemId));
					sm.addNumber(Integer.valueOf(rewradItemCount));
					player.sendPacket(sm);
					sm = null;
				}
		}
		player.sendPacket(playerIU);
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(14, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
