package l2s.gameserver.model.instances;

import java.util.List;
import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import l2s.gameserver.templates.npc.NpcTemplate;

public class FlameControlTowerInstance extends SiegeToggleNpcInstance
{
	private Set<String> _zoneList;

	public FlameControlTowerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onDeathImpl(final Creature killer)
	{
		final CastleSiegeEvent event = this.getEvent(CastleSiegeEvent.class);
		if(event == null || !event.isInProgress())
			return;
		for(final String s : _zoneList)
		{
			final List<CastleDamageZoneObject> objects = event.getObjects(s);
			for(final CastleDamageZoneObject zone : objects)
				zone.getZone().setActive(false);
		}
	}

	@Override
	public void setZoneList(final Set<String> set)
	{
		_zoneList = set;
	}
}
