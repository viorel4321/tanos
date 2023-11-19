package l2s.gameserver.network.l2.s2c;

public class Snoop extends L2GameServerPacket
{
	private int _convoID;
	private String _name;
	private int _type;
	private String _speaker;
	private String _msg;

	public Snoop(final int id, final String name, final int type, final String speaker, final String msg)
	{
		_convoID = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_msg = msg;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(213);
		writeD(_convoID);
		writeS((CharSequence) _name);
		writeD(0);
		writeD(_type);
		writeS((CharSequence) _speaker);
		writeS((CharSequence) _msg);
	}
}
