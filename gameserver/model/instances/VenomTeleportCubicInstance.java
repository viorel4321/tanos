package l2s.gameserver.model.instances;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class VenomTeleportCubicInstance extends NpcInstance
{
	public static final Location[] LOCS;

	public VenomTeleportCubicInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		player.teleToLocation(VenomTeleportCubicInstance.LOCS[Rnd.get(VenomTeleportCubicInstance.LOCS.length)]);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		this.showChatWindow(player, "residence2/castle/teleport_cube_benom001.htm", new Object[0]);
	}

	@Override
	public Clan getClan()
	{
		return null;
	}

	static
	{
		LOCS = new Location[] { new Location(11913, -48851, -1088), new Location(11918, -49447, -1088) };
	}
}
