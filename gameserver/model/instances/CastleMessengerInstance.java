package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.CastleSiegeInfo;
import l2s.gameserver.templates.npc.NpcTemplate;

public class CastleMessengerInstance extends NpcInstance
{
	public CastleMessengerInstance(final int objectID, final NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		final Castle castle = getCastle();
		final SiegeEvent<?, ?> siegeEvent = castle != null ? castle.getSiegeEvent() : null;
		if(player.isCastleLord(castle.getId()))
		{
			if(siegeEvent != null && siegeEvent.isInProgress())
				this.showChatWindow(player, "residence2/castle/sir_tyron021.htm", new Object[0]);
			else
				this.showChatWindow(player, "residence2/castle/sir_tyron007.htm", new Object[0]);
		}
		else if(siegeEvent != null && siegeEvent.isInProgress())
			this.showChatWindow(player, "residence2/castle/sir_tyron021.htm", new Object[0]);
		else
			player.sendPacket(new CastleSiegeInfo(castle, player));
	}
}
