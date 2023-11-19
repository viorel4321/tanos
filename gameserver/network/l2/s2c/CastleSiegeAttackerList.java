package l2s.gameserver.network.l2.s2c;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

public class CastleSiegeAttackerList extends L2GameServerPacket
{
	private int _id;
	private int _registrationValid;
	private List<SiegeClanObject> _clans;

	public CastleSiegeAttackerList(final Residence residence)
	{
		_clans = Collections.emptyList();
		_id = residence.getId();
		SiegeEvent<?, ?> siegeEvent = residence.getSiegeEvent();
		if(siegeEvent != null) {
			_registrationValid = siegeEvent.isRegistrationOver() ? 0 : 1;
			_clans = siegeEvent.getObjects("attackers");
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(202);
		writeD(_id);
		writeD(0);
		writeD(_registrationValid);
		writeD(0);
		writeD(_clans.size());
		writeD(_clans.size());
		for(final SiegeClanObject siegeClan : _clans)
		{
			final Clan clan = siegeClan.getClan();
			writeD(clan.getClanId());
			writeS((CharSequence) clan.getName());
			writeS((CharSequence) clan.getLeaderName());
			writeD(clan.getCrestId());
			writeD((int) (siegeClan.getDate() / 1000L));
			final Alliance alliance = clan.getAlliance();
			writeD(clan.getAllyId());
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
}
