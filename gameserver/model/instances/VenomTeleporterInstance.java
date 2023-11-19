package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;

public class VenomTeleporterInstance extends NpcInstance
{
	public VenomTeleporterInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		final Castle castle = getCastle();
		final SiegeEvent<?, ?> siegeEvent = castle != null ? castle.getSiegeEvent() : null;
		if(siegeEvent != null && siegeEvent.isInProgress())
			this.showChatWindow(player, "residence2/castle/rune_massymore_teleporter002.htm", new Object[0]);
		else
			player.teleToLocation(12589, -49044, -3008);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		this.showChatWindow(player, "residence2/castle/rune_massymore_teleporter001.htm", new Object[0]);
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}
