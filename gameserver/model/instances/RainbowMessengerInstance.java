package l2s.gameserver.model.instances;

import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2s.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.TimeUtils;

public class RainbowMessengerInstance extends NpcInstance
{
	public static final int ITEM_ID = 8034;

	public RainbowMessengerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		final ClanHall clanHall = getClanHall();
		final ClanHallMiniGameEvent miniGameEvent = clanHall.getSiegeEvent();
		if(miniGameEvent == null) {
			// TODO: Message??
			return;
		}
		if(command.equalsIgnoreCase("register"))
		{
			if(miniGameEvent.isRegistrationOver())
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti014.htm", new Object[0]);
				return;
			}
			final Clan clan = player.getClan();
			if(clan == null || clan.getLevel() < 3 || clan.getMembersCount() <= 5)
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm", new Object[0]);
				return;
			}
			if(clan.getLeaderId() != player.getObjectId())
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm", new Object[0]);
				return;
			}
			if(clan.getHasHideout() > 0)
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti012.htm", new Object[0]);
				return;
			}
			if(miniGameEvent.getSiegeClan("attackers", clan) != null)
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti013.htm", new Object[0]);
				return;
			}
			final long count = player.getInventory().getCountOf(8034);
			if(count == 0L)
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti008.htm", new Object[0]);
			else
			{
				if(!player.consumeItem(8034, (int) count))
					return;
				final CMGSiegeClanObject siegeClanObject = new CMGSiegeClanObject("attackers", clan, count);
				miniGameEvent.addObject("attackers", siegeClanObject);
				SiegeClanDAO.getInstance().insert(clanHall, siegeClanObject);
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti009.htm", new Object[0]);
			}
		}
		else if(command.equalsIgnoreCase("cancel"))
		{
			if(miniGameEvent.isRegistrationOver())
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti017.htm", new Object[0]);
				return;
			}
			final Clan clan = player.getClan();
			if(clan == null || clan.getLevel() < 3)
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm", new Object[0]);
				return;
			}
			if(clan.getLeaderId() != player.getObjectId())
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm", new Object[0]);
				return;
			}
			final SiegeClanObject siegeClanObject2 = miniGameEvent.getSiegeClan("attackers", clan);
			if(siegeClanObject2 == null)
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti016.htm", new Object[0]);
			else
			{
				miniGameEvent.removeObject("attackers", siegeClanObject2);
				SiegeClanDAO.getInstance().delete(clanHall, siegeClanObject2);
				Functions.addItem(player, 8034, siegeClanObject2.getParam() / 2L);
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti005.htm", new Object[0]);
			}
		}
		else if(command.equalsIgnoreCase("refund"))
		{
			if(miniGameEvent.isRegistrationOver())
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm", new Object[0]);
				return;
			}
			final Clan clan = player.getClan();
			if(clan == null || clan.getLevel() < 3)
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm", new Object[0]);
				return;
			}
			if(clan.getLeaderId() != player.getObjectId())
			{
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm", new Object[0]);
				return;
			}
			final SiegeClanObject siegeClanObject2 = miniGameEvent.getSiegeClan("refund", clan);
			if(siegeClanObject2 == null)
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti020.htm", new Object[0]);
			else
			{
				miniGameEvent.removeObject("refund", siegeClanObject2);
				SiegeClanDAO.getInstance().delete(clanHall, siegeClanObject2);
				Functions.addItem(player, 8034, siegeClanObject2.getParam());
				this.showChatWindow(player, "residence2/clanhall/messenger_yetti019.htm", new Object[0]);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... arg)
	{
		final ClanHall clanHall = getClanHall();
		final Clan clan = clanHall.getOwner();
		final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
		if(clan != null)
		{
			msg.setFile("residence2/clanhall/messenger_yetti001.htm");
			msg.replace("%owner_name%", clan.getName());
		}
		else
			msg.setFile("residence2/clanhall/messenger_yetti001a.htm");
		msg.replace("%siege_date%", TimeUtils.toSimpleFormat(clanHall.getSiegeDate()));
		player.sendPacket(msg);
	}
}
