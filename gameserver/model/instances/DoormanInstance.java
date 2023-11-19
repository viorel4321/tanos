package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.tables.DoorTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public abstract class DoormanInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;
	protected static final int COND_OWNER = 0;
	protected static final int COND_SIEGE = 1;
	protected static final int COND_FAIL = 2;
	protected String _siegeDialog;
	protected String _mainDialog;
	protected String _failDialog;
	protected int[] _doors;

	public DoormanInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		setDialogs();
		_doors = template.getAIParams().getIntegerArray("doors");
	}

	public void setDialogs()
	{
		_siegeDialog = getTemplate().getAIParams().getString("siege_dialog");
		_mainDialog = getTemplate().getAIParams().getString("main_dialog");
		_failDialog = getTemplate().getAIParams().getString("fail_dialog");
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
				break;
			}
			case 1:
			{
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
			{
				filename = _mainDialog;
				break;
			}
			case 1:
			{
				filename = _siegeDialog;
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

	protected int getCond(final Player player)
	{
		final Residence residence = getResidence();
		final Clan residenceOwner = residence.getOwner();
		if(residenceOwner == null || player.getClan() != residenceOwner || (player.getClanPrivileges() & getOpenPriv()) != getOpenPriv())
			return 2;

		SiegeEvent<?, ?> siegeEvent = residence.getSiegeEvent();
		if(siegeEvent != null && siegeEvent.isInProgress())
			return 1;
		return 0;
	}

	public abstract int getOpenPriv();

	public abstract Residence getResidence();
}
