package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.ManorManagerInstance;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestProcureCrop extends L2GameClientPacket
{
	private int _listId;
	private int _count;
	private int[] _items;
	private List<CastleManorManager.CropProcure> _procureList;

	public RequestProcureCrop()
	{
		_procureList = new ArrayList<CastleManorManager.CropProcure>();
	}

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count <= 0)
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
		if(_count < 1)
		{
			player.sendActionFailed();
			return;
		}
		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInStoreMode())
		{
			player.sendPacket(new SystemMessage(1065));
			return;
		}
		if(player.isInTrade())
		{
			player.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
		{
			player.sendActionFailed();
			return;
		}
		final GameObject target = player.getTarget();
		final ManorManagerInstance manor = target != null && target instanceof ManorManagerInstance ? (ManorManagerInstance) target : null;
		if(manor == null || !player.isInRange(manor, 150L))
		{
			player.sendActionFailed();
			return;
		}
		final long subTotal = 0L;
		final int tax = 0;
		int slots = 0;
		int weight = 0;
		for(int i = 0; i < _count; ++i)
		{
			final int itemId = _items[i * 2 + 0];
			final long count = _items[i * 2 + 1];
			final int price = 0;
			if(count < 0L || count > Integer.MAX_VALUE)
			{
				this.sendPacket(Msg.INCORRECT_ITEM_COUNT);
				return;
			}
			final ItemTemplate template = ItemTable.getInstance().getTemplate(Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, 0).getReward()));
			weight += (int) (count * template.getWeight());
			if(!template.isStackable())
				slots += (int) count;
			else if(player.getInventory().getItemByItemId(itemId) == null)
				++slots;
		}
		if(!player.getInventory().validateWeight(weight))
		{
			this.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}
		if(!player.getInventory().validateCapacity(slots))
		{
			this.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}
		_procureList = manor.getCastle().getCropProcure(0);
		for(int i = 0; i < _count; ++i)
		{
			final int itemId = _items[i * 2 + 0];
			long count = _items[i * 2 + 1];
			if(count < 0L)
				count = 0L;
			final int rewradItemId = Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, 0).getReward());
			long rewradItemCount = Manor.getInstance().getRewardAmountPerCrop(manor.getCastle().getId(), itemId, manor.getCastle().getCropRewardType(itemId));
			rewradItemCount *= count;
			final ItemInstance item = player.getInventory().addItem(rewradItemId, rewradItemCount);
			final ItemInstance iteme = player.getInventory().destroyItemByItemId(itemId, count, true);
			if(item != null)
				if(iteme != null)
				{
					SystemMessage sm = new SystemMessage(53);
					sm.addItemName(Integer.valueOf(rewradItemId));
					sm.addNumber(Long.valueOf(rewradItemCount));
					player.sendPacket(sm);
					sm = null;
				}
		}
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(14, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
