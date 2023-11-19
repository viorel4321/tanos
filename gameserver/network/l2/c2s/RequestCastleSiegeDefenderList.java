package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.CastleSiegeDefenderList;

public class RequestCastleSiegeDefenderList extends L2GameClientPacket
{
	private int _unitId;

	@Override
	protected void readImpl()
	{
		_unitId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _unitId);
		if(castle == null || castle.getOwner() == null)
			return;
		player.sendPacket(new CastleSiegeDefenderList(castle));
	}
}
