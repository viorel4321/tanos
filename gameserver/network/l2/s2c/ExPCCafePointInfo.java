package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExPCCafePointInfo extends L2GameServerPacket
{
	private int _mAddPoint;
	private int _mPeriodType;
	private int _pointType;
	private int _pcBangPoints;
	private int _remainTime;

	public ExPCCafePointInfo(final Player player, final int mAddPoint, final int mPeriodType, final int pointType, final int remainTime)
	{
		_pcBangPoints = player.getPcBangPoints();
		_mAddPoint = mAddPoint;
		_mPeriodType = mPeriodType;
		_pointType = pointType;
		_remainTime = remainTime;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(49);
		writeD(_pcBangPoints);
		writeD(_mAddPoint);
		writeC(_mPeriodType);
		writeD(_remainTime);
		writeC(_pointType);
	}
}
