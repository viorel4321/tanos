package l2s.gameserver.network.l2.s2c;

public class ShowXMasSeal extends L2GameServerPacket
{
	private int _item;

	public ShowXMasSeal(final int item)
	{
		_item = item;
	}

	@Override
	protected void writeImpl()
	{
		writeC(242);
		writeD(_item);
	}
}
