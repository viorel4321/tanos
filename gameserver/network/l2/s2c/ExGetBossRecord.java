package l2s.gameserver.network.l2.s2c;

import java.util.List;

public class ExGetBossRecord extends L2GameServerPacket
{
	private List<BossRecordInfo> _bossRecordInfo;
	private int _ranking;
	private int _totalPoints;

	public ExGetBossRecord(final int ranking, final int totalScore, final List<BossRecordInfo> bossRecordInfo)
	{
		_ranking = ranking;
		_totalPoints = totalScore;
		_bossRecordInfo = bossRecordInfo;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(51);
		writeD(_ranking);
		writeD(_totalPoints);
		writeD(_bossRecordInfo.size());
		for(final BossRecordInfo w : _bossRecordInfo)
		{
			writeD(w._bossId);
			writeD(w._points);
			writeD(w._unk1);
		}
	}

	public static class BossRecordInfo
	{
		public int _bossId;
		public int _points;
		public int _unk1;

		public BossRecordInfo(final int bossId, final int points, final int unk1)
		{
			_bossId = bossId;
			_points = points;
			_unk1 = unk1;
		}
	}
}
