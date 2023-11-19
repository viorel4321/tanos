package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.entity.residence.Castle;

public class ExShowSeedSetting extends L2GameServerPacket
{
	private int _manorId;
	private int _count;
	private long[] _seedData;

	public ExShowSeedSetting(final int manorId)
	{
		_manorId = manorId;
		final Castle c = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		final List<Integer> seeds = Manor.getInstance().getSeedsForCastle(_manorId);
		_count = seeds.size();
		_seedData = new long[_count * 12];
		int i = 0;
		for(final int s : seeds)
		{
			_seedData[i * 12 + 0] = s;
			_seedData[i * 12 + 1] = Manor.getInstance().getSeedLevel(s);
			_seedData[i * 12 + 2] = Manor.getInstance().getRewardItemBySeed(s, 1);
			_seedData[i * 12 + 3] = Manor.getInstance().getRewardItemBySeed(s, 2);
			_seedData[i * 12 + 4] = Manor.getInstance().getSeedSaleLimit(s);
			_seedData[i * 12 + 5] = Manor.getInstance().getSeedBuyPrice(s);
			_seedData[i * 12 + 6] = Manor.getInstance().getSeedBasicPrice(s) * 60 / 100;
			_seedData[i * 12 + 7] = Manor.getInstance().getSeedBasicPrice(s) * 10;
			CastleManorManager.SeedProduction seedPr = c.getSeed(s, 0);
			if(seedPr != null)
			{
				_seedData[i * 12 + 8] = seedPr.getStartProduce();
				_seedData[i * 12 + 9] = seedPr.getPrice();
			}
			else
			{
				_seedData[i * 12 + 8] = 0L;
				_seedData[i * 12 + 9] = 0L;
			}
			seedPr = c.getSeed(s, 1);
			if(seedPr != null)
			{
				_seedData[i * 12 + 10] = seedPr.getStartProduce();
				_seedData[i * 12 + 11] = seedPr.getPrice();
			}
			else
			{
				_seedData[i * 12 + 10] = 0L;
				_seedData[i * 12 + 11] = 0L;
			}
			++i;
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(254);
		writeH(31);
		writeD(_manorId);
		writeD(_count);
		for(int i = 0; i < _count; ++i)
		{
			writeD((int) _seedData[i * 12 + 0]);
			writeD((int) _seedData[i * 12 + 1]);
			writeC(1);
			writeD((int) _seedData[i * 12 + 2]);
			writeC(1);
			writeD((int) _seedData[i * 12 + 3]);
			writeD((int) _seedData[i * 12 + 4]);
			writeD((int) _seedData[i * 12 + 5]);
			writeD((int) _seedData[i * 12 + 6]);
			writeD((int) _seedData[i * 12 + 7]);
			writeD((int) _seedData[i * 12 + 8]);
			writeD((int) _seedData[i * 12 + 9]);
			writeD((int) _seedData[i * 12 + 10]);
			writeD((int) _seedData[i * 12 + 11]);
		}
	}
}
