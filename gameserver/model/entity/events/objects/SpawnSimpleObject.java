package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;

public class SpawnSimpleObject implements SpawnableObject
{
	private int _npcId;
	private Location _loc;
	private NpcInstance _npc;

	public SpawnSimpleObject(final int npcId, final Location loc)
	{
		_npcId = npcId;
		_loc = loc;
	}

	@Override
	public void spawnObject(final GlobalEvent event)
	{
		(_npc = NpcUtils.spawnSingle(_npcId, _loc)).addEvent(event);
	}

	@Override
	public void despawnObject(final GlobalEvent event)
	{
		_npc.removeEvent(event);
		_npc.deleteMe();
	}

	@Override
	public void refreshObject(final GlobalEvent event)
	{}
}
