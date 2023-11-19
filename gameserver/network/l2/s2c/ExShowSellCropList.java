package l2s.gameserver.network.l2.s2c;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class ExShowSellCropList extends L2GameServerPacket
{
	private int _manorId;
	private Map<Integer, ItemInstance> _cropsItems;
	private Map<Integer, CastleManorManager.CropProcure> _castleCrops;

	public ExShowSellCropList(final Player player, final int manorId, final List<CastleManorManager.CropProcure> crops)
	{
		_manorId = 1;
		_manorId = manorId;
		_castleCrops = new HashMap<Integer, CastleManorManager.CropProcure>();
		_cropsItems = new HashMap<Integer, ItemInstance>();
		final List<Integer> allCrops = Manor.getInstance().getAllCrops();
		for(final int cropId : allCrops)
		{
			final ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if(item != null)
				_cropsItems.put(cropId, item);
		}
		for(final CastleManorManager.CropProcure crop : crops)
			if(_cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0)
				_castleCrops.put(crop.getId(), crop);
	}

	@Override
	public void writeImpl()
	{
		writeC(254);
		writeH(33);
		writeD(_manorId);
		writeD(_cropsItems.size());
		for(final ItemInstance item : _cropsItems.values())
		{
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(Manor.getInstance().getSeedLevelByCrop(item.getItemId()));
			writeC(1);
			writeD(Manor.getInstance().getRewardItem(item.getItemId(), 1));
			writeC(1);
			writeD(Manor.getInstance().getRewardItem(item.getItemId(), 2));
			if(_castleCrops.containsKey(item.getItemId()))
			{
				final CastleManorManager.CropProcure crop = _castleCrops.get(item.getItemId());
				writeD(_manorId);
				writeD(crop.getAmount());
				writeD(crop.getPrice());
				writeC(crop.getReward());
			}
			else
			{
				writeD(-1);
				writeD(0);
				writeD(0);
				writeC(0);
			}
			writeD(item.getIntegerLimitedCount());
		}
	}
}
