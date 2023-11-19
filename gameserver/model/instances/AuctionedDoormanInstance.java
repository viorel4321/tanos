package l2s.gameserver.model.instances;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.tables.DoorTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;

public class AuctionedDoormanInstance extends NpcInstance
{
	private int[] _doors;
	private boolean _elite;

	public AuctionedDoormanInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
		_elite = template.getAIParams().getBool("elite", false);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		final ClanHall clanHall = getClanHall();
		if(command.equalsIgnoreCase("openDoors"))
		{
			if((player.getClanPrivileges() & 0x400) == 0x400 && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(final int d : _doors)
					DoorTable.getInstance().getDoor(d).openMe();
				this.showChatWindow(player, "residence2/clanhall/agitafterdooropen.htm", new Object[0]);
			}
			else
				this.showChatWindow(player, "residence2/clanhall/noAuthority.htm", new Object[0]);
		}
		else if(command.equalsIgnoreCase("closeDoors"))
		{
			if((player.getClanPrivileges() & 0x400) == 0x400 && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(final int d : _doors)
					DoorTable.getInstance().getDoor(d).closeMe();
				this.showChatWindow(player, "residence2/clanhall/agitafterdoorclose.htm", new Object[0]);
			}
			else
				this.showChatWindow(player, "residence2/clanhall/noAuthority.htm", new Object[0]);
		}
		else if(command.equalsIgnoreCase("banish"))
		{
			if((player.getClanPrivileges() & 0x2000) == 0x2000)
			{
				clanHall.banishForeigner();
				this.showChatWindow(player, "residence2/clanhall/agitafterbanish.htm", new Object[0]);
			}
			else
				this.showChatWindow(player, "residence2/clanhall/noAuthority.htm", new Object[0]);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		final ClanHall clanHall = getClanHall();
		if(clanHall != null)
		{
			final Clan playerClan = player.getClan();
			if(playerClan != null && playerClan.getHasHideout() == clanHall.getId())
				this.showChatWindow(player, _elite ? "residence2/clanhall/WyvernAgitJanitorHi.htm" : "residence2/clanhall/AgitJanitorHi.htm", "%owner%", playerClan.getName());
			else if(playerClan != null && playerClan.getHasCastle() > 0)
			{
				final Castle castle = ResidenceHolder.getInstance().getResidence(playerClan.getHasCastle());
				final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("merchant/territorystatus.htm");
				html.replace("%npcname%", getName());
				html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
				html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));
				html.replace("%clanname%", playerClan.getName());
				html.replace("%clanleadername%", playerClan.getLeaderName());
				player.sendPacket(html);
			}
			else
				this.showChatWindow(player, "residence2/clanhall/noAgitInfo.htm", new Object[0]);
		}
		else
			this.showChatWindow(player, "residence2/clanhall/noAgitInfo.htm", new Object[0]);
	}
}
