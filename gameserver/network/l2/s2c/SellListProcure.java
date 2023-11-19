package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;

public class SellListProcure extends L2GameServerPacket
{
	private int _money;
	private Map<ItemInstance, Integer> _sellList;
	private List<CastleManorManager.CropProcure> _procureList;
	private int _castle;

	public SellListProcure(final Player player, final int castleId)
	{
		_sellList = new HashMap<ItemInstance, Integer>();
		_procureList = new ArrayList<CastleManorManager.CropProcure>();
		_money = player.getAdena();
		_castle = castleId;
		_procureList = ResidenceHolder.getInstance().getResidence(Castle.class, _castle).getCropProcure(0);
		for(final CastleManorManager.CropProcure c : _procureList)
		{
			final ItemInstance item = player.getInventory().getItemByItemId(c.getId());
			if(item != null && c.getAmount() > 0)
				_sellList.put(item, c.getAmount());
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(233);
		writeD(_money);
		writeD(0);
		writeH(_sellList.size());
		for(final ItemInstance item : _sellList.keySet())
		{
			writeH(item.getTemplate().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(_sellList.get(item));
			writeH(item.getTemplate().getType2());
			writeH(0);
			writeD(0);
		}
	}
}
