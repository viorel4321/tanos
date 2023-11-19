package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.network.l2.s2c.CastleSiegeInfo;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ClanHallMessengerInstance extends NpcInstance
{
	private String _siegeDialog;
	private String _ownerDialog;

	public ClanHallMessengerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_siegeDialog = template.getAIParams().getString("siege_dialog");
		_ownerDialog = template.getAIParams().getString("owner_dialog");
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... arg)
	{
		final ClanHall clanHall = getClanHall();
		final ClanHallSiegeEvent siegeEvent = clanHall.getSiegeEvent();
		if(clanHall.getOwner() != null && clanHall.getOwner() == player.getClan())
			this.showChatWindow(player, _ownerDialog, new Object[0]);
		else if(siegeEvent != null && siegeEvent.isInProgress())
			this.showChatWindow(player, _siegeDialog, new Object[0]);
		else
			player.sendPacket(new CastleSiegeInfo(clanHall, player));
	}
}
