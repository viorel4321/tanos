package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class WyvernManagerInstance extends NpcInstance
{
	private static Logger _log;

	public WyvernManagerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		final boolean condition = validateCondition(player);
		if(actualCommand.equalsIgnoreCase("RideHelp"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("wyvern/help_ride.htm");
			html.replace("%npcname%", "Wyvern Manager " + getName());
			player.sendPacket(html);
			player.sendActionFailed();
		}
		if(condition)
		{
			if(actualCommand.equalsIgnoreCase("RideWyvern") && player.isClanLeader())
				if(!player.isRiding() || player.getMountNpcId() == 12621)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/not_ready.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else if(player.getInventory().getItemByItemId(1460) == null || player.getInventory().getItemByItemId(1460).getCount() < 25L)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/havenot_cry.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else if(Config.ALLOW_SEVEN_SIGNS && SevenSigns.getInstance().getCurrentPeriod() == 3 && SevenSigns.getInstance().getCabalHighestScore() == 3)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/no_ride_dusk.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else
				{
					if(player.getInventory().destroyItemByItemId(1460, 25L, false) == null)
						WyvernManagerInstance._log.info("L2WyvernManagerInstance[72]: Item not found!!!");
					player.setMount(12621, player.getMountObjId(), player.getMountLevel());
					final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/after_ride.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		if(!validateCondition(player))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("wyvern/lord_only.htm");
			html.replace("%npcname%", "Wyvern Manager " + getName());
			player.sendPacket(html);
			player.sendActionFailed();
			return;
		}
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("wyvern/lord_here.htm");
		html.replace("%Char_name%", String.valueOf(player.getName()));
		html.replace("%npcname%", "Wyvern Manager " + getName());
		player.sendPacket(html);
		player.sendActionFailed();
	}

	private boolean validateCondition(final Player player)
	{
		Residence residence = getCastle();
		if(residence != null && residence.getId() > 0 && player.getClan() != null && residence.getOwnerId() == player.getClanId() && player.isClanLeader())
			return true;
		residence = getClanHall();
		return residence != null && residence.getId() > 0 && player.getClan() != null && residence.getOwnerId() == player.getClanId() && player.isClanLeader();
	}

	static
	{
		WyvernManagerInstance._log = LoggerFactory.getLogger(WyvernManagerInstance.class);
	}
}
