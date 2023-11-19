package l2s.gameserver.network.l2.s2c;

public class ExRegenMax extends L2GameServerPacket
{
	private double _max;
	private int _count;
	private int _time;
	public static final int POTION_HEALING_GREATER = 16457;
	public static final int POTION_HEALING_MEDIUM = 16440;
	public static final int POTION_HEALING_LESSER = 16416;

	public ExRegenMax(final double max, final int count, final int time)
	{
		_max = max * 0.66;
		_count = count;
		_time = time;
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(1);
		writeD(1);
		writeD(_count);
		writeD(_time);
		writeF(_max);
	}
}
