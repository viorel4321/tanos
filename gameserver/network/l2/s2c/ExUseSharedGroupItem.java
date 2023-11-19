package l2s.gameserver.network.l2.s2c;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private int _itemId;
	private int _grpId;
	private int _remainedTime;
	private int _totalTime;

	public ExUseSharedGroupItem(final int itemId, final int grpId, final int remainedTime, final int totalTime)
	{
		_itemId = itemId;
		_grpId = grpId;
		_remainedTime = remainedTime / 1000;
		_totalTime = totalTime / 1000;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(73);
		writeD(_itemId);
		writeD(_grpId);
		writeD(_remainedTime);
		writeD(_totalTime);
	}
}
