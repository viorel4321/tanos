package l2s.gameserver.model.entity.events.impl;

import java.util.List;

import l2s.commons.collections.CollectionUtils;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.dao.SiegePlayerDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.CTBTeamObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.Location;

public class ClanHallTeamBattleEvent extends SiegeEvent<ClanHall, CTBSiegeClanObject>
{
	public static final String TRYOUT_PART = "tryout_part";
	public static final String CHALLENGER_RESTART_POINTS = "challenger_restart_points";
	public static final String FIRST_DOORS = "first_doors";
	public static final String SECOND_DOORS = "second_doors";
	public static final String NEXT_STEP = "next_step";

	public ClanHallTeamBattleEvent(final MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void startEvent()
	{
		_oldOwner = getResidence().getOwner();
		final List<CTBSiegeClanObject> attackers = this.getObjects("attackers");
		if(attackers.isEmpty())
		{
			if(_oldOwner == null)
				this.broadcastInZone2(new SystemMessage(846).addString(getResidence().getName()));
			else
				this.broadcastInZone2(new SystemMessage(295).addString(getResidence().getName()));
			reCalcNextTime(false);
			return;
		}
		if(_oldOwner != null)
			addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0L));
		SiegeClanDAO.getInstance().delete(getResidence());
		SiegePlayerDAO.getInstance().delete(getResidence());
		final List<CTBTeamObject> teams = this.getObjects("tryout_part");
		for(int i = 0; i < 5; ++i)
		{
			final CTBTeamObject team = teams.get(i);
			team.setSiegeClan(CollectionUtils.safeGet(attackers, i));
		}
		this.broadcastTo(new SystemMessage(844).addString(getResidence().getName()), "attackers", "defenders");
		this.broadcastTo(Msg.THE_TRYOUTS_ARE_ABOUT_TO_BEGIN, "attackers");
		super.startEvent();
	}

	public void nextStep()
	{
		this.broadcastTo(Msg.THE_TRYOUTS_HAVE_BEGUN, "attackers", "defenders");
		updateParticles(true, "attackers", "defenders");
	}

	public void processStep(final CTBTeamObject team)
	{
		if(team.getSiegeClan() != null)
		{
			final CTBSiegeClanObject object = team.getSiegeClan();
			object.setEvent(false, this);
			teleportPlayers("spectators");
		}
		team.despawnObject(this);
		final List<CTBTeamObject> teams = this.getObjects("tryout_part");
		boolean hasWinner = false;
		CTBTeamObject winnerTeam = null;
		for(final CTBTeamObject t : teams)
			if(t.isParticle())
			{
				hasWinner = winnerTeam == null;
				winnerTeam = t;
			}
		if(!hasWinner)
			return;
		final SiegeClanObject clan = winnerTeam.getSiegeClan();
		if(clan != null)
			getResidence().changeOwner(clan.getClan());
		this.stopEvent(true);
	}

	@Override
	public void announce(final int val)
	{
		final int minute = val / 60;
		if(minute > 0)
			this.broadcastTo(new SystemMessage("The contest will begin in " + minute + " minute(s)."), "attackers", "defenders");
		else
			this.broadcastTo(new SystemMessage(1881).addNumber(Integer.valueOf(val)), "attackers", "defenders");
	}

	@Override
	public void stopEvent(final boolean step)
	{
		final Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			if(_oldOwner != newOwner)
			{
				newOwner.broadcastToOnlineMembers(new PlaySound("Siege_Victory"));
				newOwner.incReputation(getResidence().getReputation(), false, toString());
			}
			this.broadcastTo(new SystemMessage(855).addString(newOwner.getName()).addString(getResidence().getName()), "attackers", "defenders");
			this.broadcastTo(new SystemMessage(843).addString(getResidence().getName()), "attackers", "defenders");
		}
		else
			this.broadcastTo(new SystemMessage(858).addString(getResidence().getName()), "attackers");
		updateParticles(false, "attackers", "defenders");
		this.removeObjects("defenders");
		this.removeObjects("attackers");
		super.stopEvent(step);
		_oldOwner = null;
	}

	@Override
	public void loadSiegeClans()
	{
		final List<SiegeClanObject> siegeClanObjectList = SiegeClanDAO.getInstance().load(getResidence(), "attackers");
		addObjects("attackers", siegeClanObjectList);
		final List<CTBSiegeClanObject> objects = this.getObjects("attackers");
		for(final CTBSiegeClanObject clan : objects)
			clan.select(getResidence());
	}

	@Override
	public CTBSiegeClanObject newSiegeClan(final String type, final int clanId, final long i, final long date)
	{
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : new CTBSiegeClanObject(type, clan, i, date);
	}

	@Override
	public boolean isParticle(final Player player)
	{
		if(!isInProgress() || player.getClan() == null)
			return false;
		final CTBSiegeClanObject object = getSiegeClan("attackers", player.getClan());
		return object != null && object.getPlayers().contains(player.getObjectId());
	}

	@Override
	public Location getRestartLoc(final Player player, final RestartType type)
	{
		if(!checkIfInZone(player))
			return null;
		final SiegeClanObject attackerClan = getSiegeClan("attackers", player.getClan());
		Location loc = null;
		switch(type)
		{
			case TO_VILLAGE:
			{
				if(attackerClan != null && checkIfInZone(player))
				{
					final List<SiegeClanObject> objectList = this.getObjects("attackers");
					final List<Location> teleportList = this.getObjects("challenger_restart_points");
					final int index = objectList.indexOf(attackerClan);
					loc = teleportList.get(index);
					break;
				}
				break;
			}
		}
		return loc;
	}

	@Override
	public void action(final String name, final boolean start)
	{
		if(name.equalsIgnoreCase("next_step"))
			nextStep();
		else
			super.action(name, start);
	}

	@Override
	public int getUserRelation(final Player thisPlayer, final int result)
	{
		return result;
	}

	@Override
	public int getRelation(final Player thisPlayer, final Player targetPlayer, final int result)
	{
		return result;
	}
}
