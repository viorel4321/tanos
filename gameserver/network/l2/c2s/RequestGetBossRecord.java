package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExGetBossRecord;

public class RequestGetBossRecord extends L2GameClientPacket
{
	private int _bossID;

	@Override
	public void readImpl()
	{
		_bossID = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		int totalPoints = 0;
		int ranking = 0;
		if(activeChar == null)
			return;
		final List<ExGetBossRecord.BossRecordInfo> list = new ArrayList<ExGetBossRecord.BossRecordInfo>();
		final Map<Integer, Integer> points = RaidBossSpawnManager.getInstance().getPointsForOwnerId(activeChar.getObjectId());
		if(points != null && !points.isEmpty())
			for(final Map.Entry<Integer, Integer> e : points.entrySet())
				switch(e.getKey())
				{
					case -1:
					{
						ranking = e.getValue();
						continue;
					}
					case 0:
					{
						totalPoints = e.getValue();
						continue;
					}
					default:
					{
						list.add(new ExGetBossRecord.BossRecordInfo(e.getKey(), e.getValue(), 0));
						continue;
					}
				}
		activeChar.sendPacket(new ExGetBossRecord(ranking, totalPoints, list));
	}
}
