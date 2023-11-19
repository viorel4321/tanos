package l2s.gameserver.network.l2.s2c;

import java.util.Calendar;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

public class CastleSiegeInfo extends L2GameServerPacket
{
	private long _startTime;
	private int _id;
	private int _ownerObjectId;
	private int _allyId;
	private boolean _isLeader;
	private String _ownerName;
	private String _leaderName;
	private String _allyName;
	private int[] _nextTimeMillis;

	public CastleSiegeInfo(final Castle castle, final Player player) {
		this((Residence) castle, player);
		final CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if (siegeEvent != null) {
			final long siegeTimeMillis = castle.getSiegeDate().getTimeInMillis();
			if (siegeTimeMillis == 0L)
				_nextTimeMillis = siegeEvent.getNextSiegeTimes();
			else
				_startTime = (int) (siegeTimeMillis / 1000L);
		}
	}

	public CastleSiegeInfo(final ClanHall ch, final Player player)
	{
		this((Residence) ch, player);
		_startTime = (int) (ch.getSiegeDate().getTimeInMillis() / 1000L);
	}

	protected CastleSiegeInfo(final Residence residence, final Player player)
	{
		_ownerName = "NPC";
		_leaderName = "";
		_allyName = "";
		_nextTimeMillis = ArrayUtils.EMPTY_INT_ARRAY;
		_id = residence.getId();
		_ownerObjectId = residence.getOwnerId();
		final Clan owner = residence.getOwner();
		if(owner != null)
		{
			_isLeader = player.isGM() || _ownerObjectId == player.getClanId() && player.isClanLeader();
			_ownerName = owner.getName();
			_leaderName = owner.getLeaderName();
			final Alliance ally = owner.getAlliance();
			if(ally != null)
			{
				_allyId = ally.getAllyId();
				_allyName = ally.getAllyName();
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeC(201);
		writeD(_id);
		writeD(_isLeader ? 1 : 0);
		writeD(_ownerObjectId);
		writeS(_ownerName);
		writeS(_leaderName);
		writeD(_allyId);
		writeS(_allyName);
		writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000L));
		writeD((int) _startTime);
		if(_startTime == 0L)
			writeDD(_nextTimeMillis, true);
	}
}
