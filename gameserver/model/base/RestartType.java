package l2s.gameserver.model.base;

public enum RestartType
{
	TO_VILLAGE,
	TO_CLANHALL,
	TO_CASTLE,
	TO_FLAG,
	FIXED;

	public static final RestartType[] VALUES;

	static
	{
		VALUES = values();
	}
}
