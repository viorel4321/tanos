package l2s.gameserver.model.instances;

import java.util.List;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2s.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class RainbowCoordinatorInstance extends NpcInstance
{
	public RainbowCoordinatorInstance(final int objectId, final NpcTemplate template)
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
		if(miniGameEvent == null)
			return;
		if(miniGameEvent.isArenaClosed())
		{
			this.showChatWindow(player, "residence2/clanhall/game_manager003.htm", new Object[0]);
			return;
		}
		final List<CMGSiegeClanObject> siegeClans = miniGameEvent.getObjects("attackers");
		final CMGSiegeClanObject siegeClan = miniGameEvent.getSiegeClan("attackers", player.getClan());
		if(siegeClan == null)
		{
			this.showChatWindow(player, "residence2/clanhall/game_manager014.htm", new Object[0]);
			return;
		}
		if(siegeClan.getPlayers().isEmpty())
		{
			final Party party = player.getParty();
			if(party == null)
			{
				this.showChatWindow(player, player.isClanLeader() ? "residence2/clanhall/game_manager005.htm" : "residence2/clanhall/game_manager002.htm", new Object[0]);
				return;
			}
			if(!player.isClanLeader())
			{
				this.showChatWindow(player, "residence2/clanhall/game_manager004.htm", new Object[0]);
				return;
			}
			if(party.getMemberCount() < 5)
			{
				this.showChatWindow(player, "residence2/clanhall/game_manager003.htm", new Object[0]);
				return;
			}
			if(party.getPartyLeader() != player)
			{
				this.showChatWindow(player, "residence2/clanhall/game_manager006.htm", new Object[0]);
				return;
			}
			for(final Player member : party.getPartyMembers())
				if(member.getClan() != player.getClan())
				{
					this.showChatWindow(player, "residence2/clanhall/game_manager007.htm", new Object[0]);
					return;
				}
			final int index = siegeClans.indexOf(siegeClan);
			final SpawnExObject spawnEx = miniGameEvent.getFirstObject("arena_" + index);
			final Location loc = (Location) spawnEx.getSpawns().get(0).getCurrentSpawnRange();
			for(final Player member2 : party.getPartyMembers())
			{
				siegeClan.addPlayer(member2.getObjectId());
				member2.teleToLocation(Location.coordsRandomize(loc, 100, 200));
			}
		}
		else
			this.showChatWindow(player, "residence2/clanhall/game_manager013.htm", new Object[0]);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... arg)
	{
		this.showChatWindow(player, "residence2/clanhall/game_manager001.htm", new Object[0]);
	}
}
