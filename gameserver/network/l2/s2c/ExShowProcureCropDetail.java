package l2s.gameserver.network.l2.s2c;

import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.entity.residence.Castle;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private int _cropId;
	private Map<Integer, CastleManorManager.CropProcure> _castleCrops;

	public ExShowProcureCropDetail(final int cropId)
	{
		_cropId = cropId;
		_castleCrops = new HashMap<Integer, CastleManorManager.CropProcure>();
		for(final Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
		{
			final CastleManorManager.CropProcure cropItem = c.getCrop(_cropId, 0);
			if(cropItem != null && cropItem.getAmount() > 0)
				_castleCrops.put(c.getId(), cropItem);
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(254);
		writeH(34);
		writeD(_cropId);
		writeD(_castleCrops.size());
		for(final int manorId : _castleCrops.keySet())
		{
			final CastleManorManager.CropProcure crop = _castleCrops.get(manorId);
			writeD(manorId);
			writeD(crop.getAmount());
			writeD(crop.getPrice());
			writeC(crop.getReward());
		}
	}
}
