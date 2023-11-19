package l2s.gameserver.utils;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectTasks;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class NpcUtils
{
	public static NpcInstance spawnSingle(final int npcId, final Location loc)
	{
		return spawnSingle(npcId, loc, 0L);
	}

	public static NpcInstance spawnSingle(final int npcId, final int x, final int y, final int z)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), 0L);
	}

	public static NpcInstance spawnSingle(final int npcId, final int x, final int y, final int z, final long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), despawnTime);
	}

	public static NpcInstance spawnSingle(final int npcId, final int x, final int y, final int z, final int h, final long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, h), despawnTime);
	}

	public static NpcInstance spawnSingle(final int npcId, final Location loc, final long despawnTime)
	{
		final NpcTemplate template = NpcTable.getTemplate(npcId);
		if(template == null)
			throw new NullPointerException("Npc template id : " + npcId + " not found!");
		final NpcInstance npc = template.getNewInstance();
		npc.setHeading(loc.h < 0 ? Rnd.get(65535) : loc.h);
		npc.setSpawnedLoc(loc);
		npc.spawnMe(npc.getSpawnedLoc());
		if(despawnTime > 0L)
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(npc), despawnTime);
		return npc;
	}
}
