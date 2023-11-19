package l2s.gameserver.network.l2.s2c;

public class EnchantResult extends L2GameServerPacket
{
	public static final EnchantResult SUCCESS;
	public static final EnchantResult FAILED;
	public static final EnchantResult CANCELLED;
	public static final EnchantResult BLESSED_FAILED;
	public static final EnchantResult FAILED_NO_CRYSTALS;
	private final int _result;

	private EnchantResult(final int result)
	{
		_result = result;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(129);
		writeD(_result);
	}

	static
	{
		SUCCESS = new EnchantResult(0);
		FAILED = new EnchantResult(1);
		CANCELLED = new EnchantResult(2);
		BLESSED_FAILED = new EnchantResult(3);
		FAILED_NO_CRYSTALS = new EnchantResult(4);
	}
}
