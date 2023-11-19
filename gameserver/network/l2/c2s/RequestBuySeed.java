package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.ManorManagerInstance;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestBuySeed extends L2GameClientPacket
{
	private int _count;
	private int _manorId;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if(_count > 32767 || _count <= 0 || _count * 8 < _buf.remaining())
		{
			_count = 0;
			return;
		}
		_items = new int[_count * 2];
		for(int i = 0; i < _count; ++i)
		{
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
		long totalPrice = 0L;
		int slots = 0;
		int totalWeight = 0;
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
		if(player.isFishing())
		{
			player.sendPacket(new SystemMessage(1470));
			return;
		}
		final GameObject target = player.getTarget();
		final ManorManagerInstance manor = target != null && target instanceof ManorManagerInstance ? (ManorManagerInstance) target : null;
		if(manor == null || !player.isInRange(manor, 150L))
		{
			player.sendActionFailed();
			return;
		}
		final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(castle == null)
			return;
		for(int i = 0; i < _count; ++i)
		{
			final int seedId = _items[i * 2 + 0];
			final long count = _items[i * 2 + 1];
			long price = 0L;
			long residual = 0L;
			final CastleManorManager.SeedProduction seed = castle.getSeed(seedId, 0);
			price = seed.getPrice();
			residual = seed.getCanProduce();
			if(price <= 0L)
				return;
			if(residual < count)
				return;
			totalPrice += count * price;
			final ItemTemplate template = ItemTable.getInstance().getTemplate(seedId);
			totalWeight += (int) (count * template.getWeight());
			if(!template.isStackable())
				slots += (int) count;
			else if(player.getInventory().getItemByItemId(seedId) == null)
				++slots;
		}
		if(totalPrice > Integer.MAX_VALUE)
			return;
		if(!player.getInventory().validateWeight(totalWeight))
		{
			this.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}
		if(!player.getInventory().validateCapacity(slots))
		{
			this.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}
		if(totalPrice < 0L || player.getAdena() < totalPrice)
		{
			this.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena(totalPrice, true);
		castle.addToTreasuryNoTax((int) totalPrice, false, true);
		for(int i = 0; i < _count; ++i)
		{
			final int seedId = _items[i * 2 + 0];
			int count2 = _items[i * 2 + 1];
			if(count2 < 0)
				count2 = 0;
			final CastleManorManager.SeedProduction seed2 = castle.getSeed(seedId, 0);
			seed2.setCanProduce(seed2.getCanProduce() - count2);
			if(Config.MANOR_SAVE_ALL_ACTIONS)
				castle.updateSeed(seed2.getId(), seed2.getCanProduce(), 0);
			player.getInventory().addItem(seedId, count2);
			SystemMessage sm = null;
			sm = new SystemMessage(53);
			sm.addItemName(Integer.valueOf(seedId));
			sm.addNumber(Integer.valueOf(count2));
			player.sendPacket(sm);
		}
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(14, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
