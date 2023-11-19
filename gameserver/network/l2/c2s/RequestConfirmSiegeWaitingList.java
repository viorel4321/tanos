package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.CastleSiegeDefenderList;

public class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
	private boolean _approved;
	private int _unitId;
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_unitId = readD();
		_clanId = readD();
		_approved = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.getClan() == null)
			return;
		final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _unitId);
		if(castle == null || player.getClan().getHasCastle() != castle.getId())
		{
			player.sendActionFailed();
			return;
		}
		final CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if(siegeEvent == null) {
			player.sendActionFailed();
			return;
		}
		SiegeClanObject siegeClan = siegeEvent.getSiegeClan("defenders_waiting", _clanId);
		if(siegeClan == null)
			siegeClan = siegeEvent.getSiegeClan("defenders", _clanId);
		if(siegeClan == null)
			return;
		if((player.getClanPrivileges() & 0x20000) != 0x20000)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST);
			return;
		}
		if(siegeEvent.isRegistrationOver())
		{
			player.sendPacket(Msg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED);
			return;
		}
		final int allSize = siegeEvent.getObjects("defenders").size();
		if(allSize >= 20)
		{
			player.sendPacket(Msg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE);
			return;
		}
		siegeEvent.removeObject(siegeClan.getType(), siegeClan);
		if(_approved)
			siegeClan.setType("defenders");
		else
			siegeClan.setType("defenders_refused");
		siegeEvent.addObject(siegeClan.getType(), siegeClan);
		SiegeClanDAO.getInstance().update(castle, siegeClan);
		player.sendPacket(new CastleSiegeDefenderList(castle));
	}
}
