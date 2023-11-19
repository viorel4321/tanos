package l2s.gameserver.network.l2.s2c;

public class ShowCalculator extends L2GameServerPacket
{
	private int _calculatorId;

	public ShowCalculator(final int calculatorId)
	{
		_calculatorId = calculatorId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(220);
		writeD(_calculatorId);
	}
}
