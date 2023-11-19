package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;

public class PetStatusShow extends L2GameServerPacket
{
	private int _summonType;

	public PetStatusShow(final Servitor summon)
	{
		_summonType = summon.getSummonType();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(176);
		writeD(_summonType);
	}
}
