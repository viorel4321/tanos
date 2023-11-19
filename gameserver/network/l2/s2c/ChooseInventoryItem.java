package l2s.gameserver.network.l2.s2c;

public class ChooseInventoryItem extends L2GameServerPacket
{
	private int ItemID;

	public ChooseInventoryItem(final int id)
	{
		ItemID = id;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(111);
		writeD(ItemID);
	}
}
