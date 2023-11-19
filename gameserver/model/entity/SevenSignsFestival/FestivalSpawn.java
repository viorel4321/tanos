package l2s.gameserver.model.entity.SevenSignsFestival;

import l2s.commons.util.Rnd;
import l2s.gameserver.utils.Location;

public class FestivalSpawn
{
	public Location loc;
	public int npcId;

	FestivalSpawn(final Location loc)
	{
		this.loc = loc;
		this.loc.h = loc.h < 0 ? Rnd.get(65536) : loc.h;
		npcId = -1;
	}

	FestivalSpawn(final int[] spawnData)
	{
		loc = new Location(spawnData[0], spawnData[1], spawnData[2], spawnData[3] < 0 ? Rnd.get(65536) : spawnData[3]);
		if(spawnData.length > 4)
			npcId = spawnData[4];
		else
			npcId = -1;
	}
}
