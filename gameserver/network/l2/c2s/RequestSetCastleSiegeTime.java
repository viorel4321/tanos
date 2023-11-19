package l2s.gameserver.network.l2.c2s;

import java.nio.BufferUnderflowException;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.CastleSiegeInfo;

public class RequestSetCastleSiegeTime extends L2GameClientPacket
{
	private int _id;
	private int _time;
	private boolean _fail;

	@Override
	protected void readImpl()
	{
		try
		{
			_id = readD();
			_time = readD();
		}
		catch(BufferUnderflowException e)
		{
			_fail = true;
		}
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(_fail)
		{
			player.sendActionFailed();
			return;
		}
		final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _id);
		if(castle == null)
			return;
		if(player.getClan().getHasCastle() != castle.getId())
			return;
		if((player.getClanPrivileges() & 0x20000) != 0x20000)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME);
			return;
		}

		final CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if(siegeEvent != null)
			siegeEvent.setNextSiegeByOwner(_time);

		player.sendPacket(new CastleSiegeInfo(castle, player));
	}
}
