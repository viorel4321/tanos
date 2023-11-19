package l2s.gameserver.model.instances;

import java.util.List;
import java.util.concurrent.Future;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class CastleMassTeleporterInstance extends NpcInstance
{
	private Future<?> _teleportTask;
	private Location _teleportLoc;

	public CastleMassTeleporterInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_teleportTask = null;
		_teleportLoc = Location.parseLoc(template.getAIParams().getString("teleport_loc"));
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		if(_teleportTask != null)
		{
			this.showChatWindow(player, "residence2/castle/CastleTeleportDelayed.htm", new Object[0]);
			return;
		}
		_teleportTask = ThreadPoolManager.getInstance().schedule(new TeleportTask(), isAllTowersDead() ? 480000L : 30000L);
		this.showChatWindow(player, "residence2/castle/CastleTeleportDelayed.htm", new Object[0]);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		if(_teleportTask != null)
			this.showChatWindow(player, "residence2/castle/CastleTeleportDelayed.htm", new Object[0]);
		else if(isAllTowersDead())
			this.showChatWindow(player, "residence2/castle/gludio_mass_teleporter002.htm", new Object[0]);
		else
			this.showChatWindow(player, "residence2/castle/gludio_mass_teleporter001.htm", new Object[0]);
	}

	private boolean isAllTowersDead()
	{
		final SiegeEvent<?, ?> siegeEvent = this.getEvent(SiegeEvent.class);
		if(siegeEvent == null || !siegeEvent.isInProgress())
			return false;
		final List<SiegeToggleNpcObject> towers = siegeEvent.getObjects("control_towers");
		for(final SiegeToggleNpcObject t : towers)
			if(t.isAlive())
				return false;
		return true;
	}

	private class TeleportTask implements Runnable
	{
		@Override
		public void run()
		{
			Functions.npcShout(CastleMassTeleporterInstance.this, "The defenders of " + getCastle().getName() + " castle will be teleported to the inner castle.", 3000);
			for(final Player p : World.getAroundPlayers(CastleMassTeleporterInstance.this, 600, 50))
				p.teleToLocation(Location.findAroundPosition(_teleportLoc, 10, 100, p.getGeoIndex()));
			_teleportTask = null;
		}
	}
}
