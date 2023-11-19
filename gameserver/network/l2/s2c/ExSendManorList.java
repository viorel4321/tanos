package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;

public class ExSendManorList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(27);
		final Collection<Castle> _castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		writeD(_castles.size());
		for(final Residence castle : _castles)
		{
			writeD(castle.getId());
			writeS((CharSequence) castle.getName().toLowerCase());
		}
	}
}
