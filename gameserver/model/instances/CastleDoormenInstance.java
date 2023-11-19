package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.tables.DoorTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class CastleDoormenInstance extends DoormanInstance
{
	private Location[] _locs;

	public CastleDoormenInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_locs = new Location[2];
		for(int i = 0; i < _locs.length; ++i)
		{
			final String loc = template.getAIParams().getString(("tele_loc" + i), (String) null);
			if(loc != null)
				_locs[i] = Location.parseLoc(loc);
		}
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		final int cond = getCond(player);
		switch(cond)
		{
			case 0:
			{
				if(command.equalsIgnoreCase("openDoors"))
				{
					for(final int i : _doors)
						DoorTable.getInstance().getDoor(i).openMe();
					break;
				}
				if(command.equalsIgnoreCase("closeDoors"))
				{
					for(final int i : _doors)
						DoorTable.getInstance().getDoor(i).closeMe();
					break;
				}
				if(command.startsWith("tele"))
				{
					final int id = Integer.parseInt(command.substring(4, 5));
					final Location loc = _locs[id];
					if(loc != null)
						player.teleToLocation(loc);
					break;
				}
				break;
			}
			case 1:
			{
				if(command.startsWith("tele"))
				{
					final int id = Integer.parseInt(command.substring(4, 5));
					final Location loc = _locs[id];
					if(loc != null)
						player.teleToLocation(loc);
					break;
				}
				player.sendPacket(new NpcHtmlMessage(player, this, _siegeDialog, 0));
				break;
			}
			case 2:
			{
				player.sendPacket(new NpcHtmlMessage(player, this, _failDialog, 0));
				break;
			}
		}
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		String filename = null;
		final int cond = getCond(player);
		switch(cond)
		{
			case 0:
			case 1:
			{
				filename = _mainDialog;
				break;
			}
			case 2:
			{
				filename = _failDialog;
				break;
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	@Override
	protected int getCond(final Player player)
	{
		final Castle residence = getCastle();
		final Clan residenceOwner = residence.getOwner();
		if(residenceOwner == null || player.getClan() != residenceOwner || (player.getClanPrivileges() & getOpenPriv()) != getOpenPriv())
			return 2;

		SiegeEvent<?, ?> siegeEvent = residence.getSiegeEvent();
		if(siegeEvent != null && siegeEvent.isInProgress())
			return 1;
		return 0;
	}

	@Override
	public int getOpenPriv()
	{
		return 32768;
	}

	@Override
	public Residence getResidence()
	{
		return getCastle();
	}
}
