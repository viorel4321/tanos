package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;

public class ExShowCropInfo extends L2GameServerPacket
{
	private List<CastleManorManager.CropProcure> _crops;
	private int _manorId;

	public ExShowCropInfo(final int manorId, final List<CastleManorManager.CropProcure> crops)
	{
		_manorId = manorId;
		_crops = crops;
		if(_crops == null)
			_crops = new ArrayList<CastleManorManager.CropProcure>();
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(29);
		writeC(0);
		writeD(_manorId);
		writeD(0);
		writeD(_crops.size());
		for(final CastleManorManager.CropProcure crop : _crops)
		{
			writeD(crop.getId());
			writeD(crop.getAmount());
			writeD(crop.getStartAmount());
			writeD(crop.getPrice());
			writeC(crop.getReward());
			writeD(Manor.getInstance().getSeedLevelByCrop(crop.getId()));
			writeC(1);
			writeD(Manor.getInstance().getRewardItem(crop.getId(), 1));
			writeC(1);
			writeD(Manor.getInstance().getRewardItem(crop.getId(), 2));
		}
	}
}
