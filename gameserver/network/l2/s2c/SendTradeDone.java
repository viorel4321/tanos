package l2s.gameserver.network.l2.s2c;

public class SendTradeDone extends L2GameServerPacket
{
	public static final SendTradeDone Success;
	public static final SendTradeDone Fail;
	private int _num;

	public SendTradeDone(final int num)
	{
		_num = num;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(34);
		writeD(_num);
	}

	static
	{
		Success = new SendTradeDone(1);
		Fail = new SendTradeDone(0);
	}
}
