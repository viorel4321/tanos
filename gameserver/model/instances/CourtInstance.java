package l2s.gameserver.model.instances;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.skills.skillclasses.Call;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class CourtInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	public CourtInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		final int condition = validateCondition(player);
		if(condition <= 0)
			return;
		if(condition == 1)
			return;
		if((player.getClanPrivileges() & 0x40000) != 0x40000)
		{
			player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if(condition == 2)
		{
			if(command.startsWith("Chat"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch(IndexOutOfBoundsException ex)
				{}
				catch(NumberFormatException ex2)
				{}
				this.showChatWindow(player, val, new Object[0]);
				return;
			}
			if(command.startsWith("gotoleader"))
			{
				if(player.getClan() != null)
				{
					final Player clanLeader = player.getClan().getLeader().getPlayer();
					if(clanLeader == null)
						return;
					if(clanLeader.getAbnormalList().getEffectsBySkillId(3632) != null)
					{
						if(Call.canSummonHere(clanLeader) != null)
							return;
						if(Call.canBeSummoned(player, clanLeader.isIn7sDungeon()) == null)
							player.teleToLocation(Location.findAroundPosition(clanLeader.getLoc(), 0, 100, player.getGeoIndex()));
					}
					else
						this.showChatWindow(player, "castle/CourtMagician/CourtMagician-nogate.htm", new Object[0]);
				}
			}
			else
				super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... arg)
	{
		player.sendActionFailed();
		String filename = "castle/CourtMagician/CourtMagician-no.htm";
		final int condition = validateCondition(player);
		if(condition > 0)
			if(condition == 1)
				filename = "castle/CourtMagician/CourtMagician-busy.htm";
			else if(condition == 2)
				if(val == 0)
					filename = "castle/CourtMagician/CourtMagician.htm";
				else
					filename = "castle/CourtMagician/CourtMagician-" + val + ".htm";
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		player.sendPacket(html);
	}

	protected int validateCondition(final Player player)
	{
		if(player.isGM())
			return 2;
		final Castle castle = getCastle();
		if(castle != null && castle.getId() > 0 && player.getClan() != null)
		{
			SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
			if(siegeEvent != null && siegeEvent.isInProgress())
				return 1;
			if(castle.getOwnerId() == player.getClanId())
				return 2;
		}
		return 0;
	}
}
