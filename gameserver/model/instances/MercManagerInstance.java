package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class MercManagerInstance extends MerchantInstance
{
	private static int COND_ALL_FALSE;
	private static int COND_BUSY_BECAUSE_OF_SIEGE;
	private static int COND_OWNER;

	public MercManagerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		final int condition = validateCondition(player);
		if(condition <= MercManagerInstance.COND_ALL_FALSE || condition == MercManagerInstance.COND_BUSY_BECAUSE_OF_SIEGE)
			return;
		if(condition == MercManagerInstance.COND_OWNER)
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			final String actualCommand = st.nextToken();
			String val = "";
			if(st.countTokens() >= 1)
				val = st.nextToken();
			if(actualCommand.equalsIgnoreCase("hire"))
			{
				if(val.equals(""))
					return;
				showBuyWindow(player, Integer.parseInt(val), false);
			}
			else
				super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		String filename = "castle/mercmanager/mercmanager-no.htm";
		final int condition = validateCondition(player);
		if(condition == MercManagerInstance.COND_BUSY_BECAUSE_OF_SIEGE)
			filename = "castle/mercmanager/mercmanager-busy.htm";
		else if(condition == MercManagerInstance.COND_OWNER)
		{
			if(Config.ALLOW_SEVEN_SIGNS)
			{
				if(SevenSigns.getInstance().getCurrentPeriod() == 3)
				{
					if(SevenSigns.getInstance().getSealOwner(3) == 2)
						filename = "castle/mercmanager/mercmanager_dawn.htm";
					else if(SevenSigns.getInstance().getSealOwner(3) == 1)
						filename = "castle/mercmanager/mercmanager_dusk.htm";
					else
						filename = "castle/mercmanager/mercmanager.htm";
				}
				else
					filename = "castle/mercmanager/mercmanager_nohire.htm";
			}
			else
			{
				filename = "castle/mercmanager/mercmanager.htm";
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	private int validateCondition(final Player player)
	{
		if(player.isGM())
			return MercManagerInstance.COND_OWNER;
		if(getCastle() != null && getCastle().getId() > 0 && player.getClan() != null)
		{
			SiegeEvent<?, ?> siegeEvent = getCastle().getSiegeEvent();
			if(siegeEvent != null && siegeEvent.isInProgress())
				return MercManagerInstance.COND_BUSY_BECAUSE_OF_SIEGE;
			if(getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 0x200000) == 0x200000)
				return MercManagerInstance.COND_OWNER;
		}
		return MercManagerInstance.COND_ALL_FALSE;
	}

	static
	{
		MercManagerInstance.COND_ALL_FALSE = 0;
		MercManagerInstance.COND_BUSY_BECAUSE_OF_SIEGE = 1;
		MercManagerInstance.COND_OWNER = 2;
	}
}
