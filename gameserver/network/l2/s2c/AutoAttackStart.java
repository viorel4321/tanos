package l2s.gameserver.network.l2.s2c;

public class AutoAttackStart extends L2GameServerPacket
{
	private int _targetId;

	public AutoAttackStart(final int targetId)
	{
		_targetId = targetId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(43);
		writeD(_targetId);
	}
}
