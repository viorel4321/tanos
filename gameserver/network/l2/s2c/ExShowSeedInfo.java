package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;

public class ExShowSeedInfo extends L2GameServerPacket
{
	private List<CastleManorManager.SeedProduction> _seeds;
	private int _manorId;

	public ExShowSeedInfo(final int manorId, final List<CastleManorManager.SeedProduction> seeds)
	{
		_manorId = manorId;
		_seeds = seeds;
		if(_seeds == null)
			_seeds = new ArrayList<CastleManorManager.SeedProduction>();
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(28);
		writeC(0);
		writeD(_manorId);
		writeD(0);
		writeD(_seeds.size());
		for(final CastleManorManager.SeedProduction seed : _seeds)
		{
			writeD(seed.getId());
			writeD(seed.getCanProduce());
			writeD(seed.getStartProduce());
			writeD(seed.getPrice());
			writeD(Manor.getInstance().getSeedLevel(seed.getId()));
			writeC(1);
			writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 1));
			writeC(1);
			writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 2));
		}
	}
}
