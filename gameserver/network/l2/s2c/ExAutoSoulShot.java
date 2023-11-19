package l2s.gameserver.network.l2.s2c;

public class ExAutoSoulShot extends L2GameServerPacket
{
	private final int _itemId;
	private final boolean _type;

	public ExAutoSoulShot(final int itemId, final boolean type)
	{
		_itemId = itemId;
		_type = type;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(18);
		writeD(_itemId);
		writeD(_type ? 1 : 0);
	}
}
