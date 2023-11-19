package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

public class CastleSiegeDefenderList extends L2GameServerPacket
{
	public static int OWNER;
	public static int WAITING;
	public static int ACCEPTED;
	public static int REFUSE;
	private int _id;
	private int _registrationValid;
	private List<DefenderClan> _defenderClans;

	public CastleSiegeDefenderList(final Castle castle)
	{
		_defenderClans = Collections.emptyList();
		_id = castle.getId();

		SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
		if(siegeEvent == null)
			return;

		_registrationValid = !siegeEvent.isRegistrationOver() && castle.getOwner() != null ? 1 : 0;

		final List<SiegeClanObject> defenders = siegeEvent.getObjects("defenders");
		final List<SiegeClanObject> defendersWaiting = siegeEvent.getObjects("defenders_waiting");
		final List<SiegeClanObject> defendersRefused = siegeEvent.getObjects("defenders_refused");
		_defenderClans = new ArrayList<DefenderClan>(defenders.size() + defendersWaiting.size() + defendersRefused.size());
		if(castle.getOwner() != null)
			_defenderClans.add(new DefenderClan(castle.getOwner(), CastleSiegeDefenderList.OWNER, 0));
		for(final SiegeClanObject siegeClan : defenders)
			_defenderClans.add(new DefenderClan(siegeClan.getClan(), CastleSiegeDefenderList.ACCEPTED, (int) (siegeClan.getDate() / 1000L)));
		for(final SiegeClanObject siegeClan : defendersWaiting)
			_defenderClans.add(new DefenderClan(siegeClan.getClan(), CastleSiegeDefenderList.WAITING, (int) (siegeClan.getDate() / 1000L)));
		for(final SiegeClanObject siegeClan : defendersRefused)
			_defenderClans.add(new DefenderClan(siegeClan.getClan(), CastleSiegeDefenderList.REFUSE, (int) (siegeClan.getDate() / 1000L)));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(203);
		writeD(_id);
		writeD(0);
		writeD(_registrationValid);
		writeD(0);
		writeD(_defenderClans.size());
		writeD(_defenderClans.size());
		for(final DefenderClan defenderClan : _defenderClans)
		{
			final Clan clan = defenderClan._clan;
			writeD(clan.getClanId());
			writeS((CharSequence) clan.getName());
			writeS((CharSequence) clan.getLeaderName());
			writeD(clan.getCrestId());
			writeD(defenderClan._time);
			writeD(defenderClan._type);
			writeD(clan.getAllyId());
			final Alliance alliance = clan.getAlliance();
			if(alliance != null)
			{
				writeS((CharSequence) alliance.getAllyName());
				writeS((CharSequence) alliance.getAllyLeaderName());
				writeD(alliance.getAllyCrestId());
			}
			else
			{
				writeS((CharSequence) "");
				writeS((CharSequence) "");
				writeD(0);
			}
		}
	}

	static
	{
		CastleSiegeDefenderList.OWNER = 1;
		CastleSiegeDefenderList.WAITING = 2;
		CastleSiegeDefenderList.ACCEPTED = 3;
		CastleSiegeDefenderList.REFUSE = 4;
	}

	private static class DefenderClan
	{
		private Clan _clan;
		private int _type;
		private int _time;

		public DefenderClan(final Clan clan, final int type, final int time)
		{
			_clan = clan;
			_type = type;
			_time = time;
		}
	}
}
