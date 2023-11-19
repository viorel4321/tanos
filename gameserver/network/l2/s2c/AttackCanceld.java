package l2s.gameserver.network.l2.s2c;

@Deprecated
public class AttackCanceld extends L2GameServerPacket
{
	private int _objectId;

	public AttackCanceld(final int objectId)
	{
		_objectId = objectId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(10);
		writeD(_objectId);
	}
}
