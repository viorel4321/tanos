package l2s.gameserver.model.instances;

import java.util.List;
import java.util.StringTokenizer;

import l2s.commons.collections.CollectionUtils;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.dao.SiegePlayerDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.ClanHallTeamBattleEvent;
import l2s.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.TimeUtils;

public class FarmMessengerInstance extends NpcInstance
{
	public FarmMessengerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		final ClanHall clanHall = getClanHall();
		final ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
		if(siegeEvent == null) {
			// TODO: Message??
			return;
		}

		final Clan clan = player.getClan();
		if(command.equalsIgnoreCase("registrationMenu"))
		{
			if(!checkCond(player, true))
				return;
			this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_1.htm", new Object[0]);
		}
		else if(command.equalsIgnoreCase("registerAsClan"))
		{
			if(!checkCond(player, false))
				return;
			final List<CTBSiegeClanObject> siegeClans = siegeEvent.getObjects("attackers");
			final CTBSiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
			if(siegeClan != null)
			{
				showFlagInfo(player, siegeClans.indexOf(siegeClan));
				return;
			}
			final QuestState questState = player.getQuestState(655);
			if(questState == null || questState.getQuestItemsCount(8293) != 1L)
			{
				this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_27.htm", new Object[0]);
				return;
			}
			questState.exitCurrentQuest(true);
			register(player);
		}
		else if(command.equalsIgnoreCase("registerAsMember"))
		{
			final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan2 == null)
			{
				this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm", new Object[0]);
				return;
			}
			if(siegeClan2.getClan().getLeaderId() == player.getObjectId())
			{
				this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_5.htm", new Object[0]);
				return;
			}
			if(siegeClan2.getPlayers().contains(player.getObjectId()))
				this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_9.htm", new Object[0]);
			else
			{
				if(siegeClan2.getPlayers().size() >= 18)
				{
					this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_8.htm", new Object[0]);
					return;
				}
				siegeClan2.getPlayers().add(player.getObjectId());
				SiegePlayerDAO.getInstance().insert(clanHall, clan.getClanId(), player.getObjectId());
				this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_9.htm", new Object[0]);
			}
		}
		else if(command.startsWith("formAlliance"))
		{
			final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan2 == null)
			{
				this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm", new Object[0]);
				return;
			}
			if(siegeClan2.getClan().getLeaderId() != player.getObjectId())
			{
				this.showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_10.htm", new Object[0]);
				return;
			}
			if(siegeClan2.getParam() > 0L)
				return;
			final StringTokenizer t = new StringTokenizer(command);
			t.nextToken();
			final int npcId = Integer.parseInt(t.nextToken());
			siegeClan2.setParam(npcId);
			SiegeClanDAO.getInstance().update(clanHall, siegeClan2);
			this.showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_9.htm", new Object[0]);
		}
		else if(command.equalsIgnoreCase("setNpc"))
		{
			final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan2 == null)
			{
				this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm", new Object[0]);
				return;
			}
			if(siegeClan2.getClan().getLeaderId() != player.getObjectId())
			{
				this.showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_10.htm", new Object[0]);
				return;
			}
			this.showChatWindow(player, npcDialog(siegeClan2), new Object[0]);
		}
		else if(command.equalsIgnoreCase("viewNpc"))
		{
			final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
			if(siegeClan2 == null)
			{
				this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm", new Object[0]);
				return;
			}
			String file;
			if(siegeClan2.getParam() == 0L)
				file = "residence2/clanhall/agit_oel_mahum_messeger_10.htm";
			else
				file = npcDialog(siegeClan2);
			this.showChatWindow(player, file, new Object[0]);
		}
		else if(command.equalsIgnoreCase("listClans"))
		{
			final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/farm_messenger003.htm");
			final List<CTBSiegeClanObject> siegeClans2 = siegeEvent.getObjects("attackers");
			for(int i = 0; i < 5; ++i)
			{
				final CTBSiegeClanObject siegeClan3 = CollectionUtils.safeGet(siegeClans2, i);
				if(siegeClan3 != null)
					msg.replace("%clan_" + i + "%", siegeClan3.getClan().getName());
				else
					msg.replace("%clan_" + i + "%", "**unregistered**");
				msg.replace("%clan_count_" + i + "%", siegeClan3 == null ? "" : String.valueOf(siegeClan3.getPlayers().size()));
			}
			player.sendPacket(msg);
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void register(final Player player)
	{
		final ClanHall clanHall = getClanHall();
		final ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
		if(siegeEvent == null)
			return;

		final Clan clan = player.getClan();
		final CTBSiegeClanObject siegeClan = new CTBSiegeClanObject("attackers", clan, 0L);
		siegeClan.getPlayers().add(player.getObjectId());
		siegeEvent.addObject("attackers", siegeClan);
		SiegeClanDAO.getInstance().insert(clanHall, siegeClan);
		SiegePlayerDAO.getInstance().insert(clanHall, clan.getClanId(), player.getObjectId());
		final List<CTBSiegeClanObject> siegeClans = siegeEvent.getObjects("attackers");
		showFlagInfo(player, siegeClans.indexOf(siegeClan));
	}

	private void showFlagInfo(final Player player, final int index)
	{
		String file = null;
		switch(index)
		{
			case 0:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4a.htm";
				break;
			}
			case 1:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4b.htm";
				break;
			}
			case 2:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4c.htm";
				break;
			}
			case 3:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4d.htm";
				break;
			}
			case 4:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_4e.htm";
				break;
			}
			default:
			{
				return;
			}
		}
		this.showChatWindow(player, file, new Object[0]);
	}

	private String npcDialog(final SiegeClanObject siegeClanObject)
	{
		String file = null;
		switch((int) siegeClanObject.getParam())
		{
			case 0:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_6.htm";
				break;
			}
			case 35618:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_17.htm";
				break;
			}
			case 35619:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_18.htm";
				break;
			}
			case 35620:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_19.htm";
				break;
			}
			case 35621:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_20.htm";
				break;
			}
			case 35622:
			{
				file = "residence2/clanhall/farm_kel_mahum_messenger_23.htm";
				break;
			}
		}
		return file;
	}

	private boolean checkCond(final Player player, final boolean regMenu)
	{
		final ClanHall clanHall = getClanHall();
		final ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
		if(siegeEvent == null) {
			// TODO: Message??
			return false;
		}
		final Clan clan = player.getClan();
		final List<CTBSiegeClanObject> siegeClans = siegeEvent.getObjects("attackers");
		final SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
		if(siegeEvent.isRegistrationOver())
		{
			this.showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_11.htm", "%siege_time%", TimeUtils.toSimpleFormat(clanHall.getSiegeDate()));
			return false;
		}
		if(regMenu && siegeClan != null)
			return true;
		if(clan == null || player.getObjectId() != clan.getLeaderId())
		{
			this.showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_03.htm", new Object[0]);
			return false;
		}
		if(player.getObjectId() == clan.getLeaderId() && clan.getLevel() < 4)
		{
			this.showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_05.htm", new Object[0]);
			return false;
		}
		if(clan.getHasHideout() == clanHall.getId())
		{
			this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_22.htm", new Object[0]);
			return false;
		}
		if(clan.getHasHideout() > 0)
		{
			this.showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_04.htm", new Object[0]);
			return false;
		}
		if(siegeClans.size() >= 5)
		{
			this.showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_21.htm", new Object[0]);
			return false;
		}
		return true;
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... arg)
	{
		final Clan clan = getClanHall().getOwner();
		if(clan != null)
			this.showChatWindow(player, "residence2/clanhall/farm_messenger001.htm", "%owner_name%", clan.getName());
		else
			this.showChatWindow(player, "residence2/clanhall/farm_messenger002.htm", new Object[0]);
	}
}
