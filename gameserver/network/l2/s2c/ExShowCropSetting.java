package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.entity.residence.Castle;

public class ExShowCropSetting extends L2GameServerPacket
{
	private int _manorId;
	private int _count;
	private long[] _cropData;

	public ExShowCropSetting(final int manorId)
	{
		_manorId = manorId;
		final Castle c = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		final List<Integer> crops = Manor.getInstance().getCropsForCastle(_manorId);
		_count = crops.size();
		_cropData = new long[_count * 14];
		int i = 0;
		for(final int cr : crops)
		{
			_cropData[i * 14 + 0] = cr;
			_cropData[i * 14 + 1] = Manor.getInstance().getSeedLevelByCrop(cr);
			_cropData[i * 14 + 2] = Manor.getInstance().getRewardItem(cr, 1);
			_cropData[i * 14 + 3] = Manor.getInstance().getRewardItem(cr, 2);
			_cropData[i * 14 + 4] = Manor.getInstance().getCropPuchaseLimit(cr);
			_cropData[i * 14 + 5] = 0L;
			_cropData[i * 14 + 6] = Manor.getInstance().getCropBasicPrice(cr) * 60 / 100;
			_cropData[i * 14 + 7] = Manor.getInstance().getCropBasicPrice(cr) * 10;
			CastleManorManager.CropProcure cropPr = c.getCrop(cr, 0);
			if(cropPr != null)
			{
				_cropData[i * 14 + 8] = cropPr.getStartAmount();
				_cropData[i * 14 + 9] = cropPr.getPrice();
				_cropData[i * 14 + 10] = cropPr.getReward();
			}
			else
			{
				_cropData[i * 14 + 8] = 0L;
				_cropData[i * 14 + 9] = 0L;
				_cropData[i * 14 + 10] = 0L;
			}
			cropPr = c.getCrop(cr, 1);
			if(cropPr != null)
			{
				_cropData[i * 14 + 11] = cropPr.getStartAmount();
				_cropData[i * 14 + 12] = cropPr.getPrice();
				_cropData[i * 14 + 13] = cropPr.getReward();
			}
			else
			{
				_cropData[i * 14 + 11] = 0L;
				_cropData[i * 14 + 12] = 0L;
				_cropData[i * 14 + 13] = 0L;
			}
			++i;
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(254);
		writeH(32);
		writeD(_manorId);
		writeD(_count);
		for(int i = 0; i < _count; ++i)
		{
			writeD((int) _cropData[i * 14 + 0]);
			writeD((int) _cropData[i * 14 + 1]);
			writeC(1);
			writeD((int) _cropData[i * 14 + 2]);
			writeC(1);
			writeD((int) _cropData[i * 14 + 3]);
			writeD((int) _cropData[i * 14 + 4]);
			writeD((int) _cropData[i * 14 + 5]);
			writeD((int) _cropData[i * 14 + 6]);
			writeD((int) _cropData[i * 14 + 7]);
			writeD((int) _cropData[i * 14 + 8]);
			writeD((int) _cropData[i * 14 + 9]);
			writeC((int) _cropData[i * 14 + 10]);
			writeD((int) _cropData[i * 14 + 11]);
			writeD((int) _cropData[i * 14 + 12]);
			writeC((int) _cropData[i * 14 + 13]);
		}
	}
}
