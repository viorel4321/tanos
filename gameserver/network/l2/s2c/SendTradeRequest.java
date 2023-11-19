package l2s.gameserver.network.l2.s2c;

public class SendTradeRequest extends L2GameServerPacket
{
	private int _senderID;

	public SendTradeRequest(final int senderID)
	{
		_senderID = senderID;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(94);
		writeD(_senderID);
	}
}
