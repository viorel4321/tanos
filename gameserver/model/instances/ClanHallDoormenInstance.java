package l2s.gameserver.model.instances;

import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ClanHallDoormenInstance extends DoormanInstance
{
	public ClanHallDoormenInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getOpenPriv()
	{
		return 1024;
	}

	@Override
	public Residence getResidence()
	{
		return getClanHall();
	}
}
