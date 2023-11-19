package l2s.gameserver.network.l2.s2c;

public class AutoAttackStop extends L2GameServerPacket
{
	private int _targetId;

	public AutoAttackStop(final int targetId)
	{
		_targetId = targetId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(44);
		writeD(_targetId);
	}
}
