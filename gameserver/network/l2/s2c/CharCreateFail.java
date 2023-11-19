package l2s.gameserver.network.l2.s2c;

public class CharCreateFail extends L2GameServerPacket
{
	public static final L2GameServerPacket REASON_CREATION_FAILED = new CharCreateFail(0x00); // "Your character creation has failed."
	public static final L2GameServerPacket REASON_TOO_MANY_CHARACTERS = new CharCreateFail(0x01); // "You cannot create another character. Please delete the existing character and try again." Removes all settings that were selected (race, class, etc).
	public static final L2GameServerPacket REASON_NAME_ALREADY_EXISTS = new CharCreateFail(0x02); // "This name already exists."
	public static final L2GameServerPacket REASON_16_ENG_CHARS = new CharCreateFail(0x03); // "Your title cannot exceed 16 characters in length. Please try again."
	public static final L2GameServerPacket REASON_INCORRECT_NAME = new CharCreateFail(0x04); // "Incorrect name. Please try again."
	public static final L2GameServerPacket REASON_CREATE_NOT_ALLOWED = new CharCreateFail(0x05); // "Characters cannot be created from this server."
	public static final L2GameServerPacket REASON_CHOOSE_ANOTHER_SVR = new CharCreateFail(0x06); // "Unable to create character. You are unable to create a new character on the selected server. A restriction is in place which restricts users from creating characters on different servers where no previous character exists. Please choose another server."

	private int _error;

	public CharCreateFail(final int errorCode)
	{
		_error = errorCode;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(26);
		writeD(_error);
	}
}
