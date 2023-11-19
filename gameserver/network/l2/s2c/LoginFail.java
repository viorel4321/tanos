package l2s.gameserver.network.l2.s2c;

public class LoginFail extends L2GameServerPacket
{
	public static final LoginFail NO_TEXT = new LoginFail(0);
	public static final LoginFail SYSTEM_ERROR_LOGIN_LATER = new LoginFail(1);
	public static final LoginFail PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT = new LoginFail(2);
	public static final LoginFail PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT2 = new LoginFail(3);
	public static final LoginFail ACCESS_FAILED_TRY_LATER = new LoginFail(4);
	public static final LoginFail INCORRECT_ACCOUNT_INFO_CONTACT_CUSTOMER_SUPPORT = new LoginFail(5);
	public static final LoginFail ACCESS_FAILED_TRY_LATER2 = new LoginFail(6);
	public static final LoginFail ACOUNT_ALREADY_IN_USE = new LoginFail(7);
	public static final LoginFail ACCESS_FAILED_TRY_LATER3 = new LoginFail(8);
	public static final LoginFail ACCESS_FAILED_TRY_LATER4 = new LoginFail(9);
	public static final LoginFail ACCESS_FAILED_TRY_LATER5 = new LoginFail(10);

	private final int _reason;

	private LoginFail(final int reason)
	{
		_reason = reason;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(20);
		writeD(_reason);
	}
}
