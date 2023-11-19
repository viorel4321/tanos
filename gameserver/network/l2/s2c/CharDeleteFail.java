package l2s.gameserver.network.l2.s2c;

public class CharDeleteFail extends L2GameServerPacket
{
	public static int REASON_DELETION_FAILED;
	public static int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER;
	public static int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED;
	int _error;

	public CharDeleteFail(final int error)
	{
		_error = error;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(36);
		writeD(_error);
	}

	static
	{
		CharDeleteFail.REASON_DELETION_FAILED = 1;
		CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 2;
		CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 3;
	}
}
