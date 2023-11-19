package l2s.gameserver.network.l2.s2c;

public class FriendRecvMsg extends L2GameServerPacket
{
	private String _sender;
	private String _receiver;
	private String _message;

	public FriendRecvMsg(final String sender, final String receiver, final String message)
	{
		_sender = sender;
		_receiver = receiver;
		_message = message;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(253);
		writeD(0);
		writeS((CharSequence) _receiver);
		writeS((CharSequence) _sender);
		writeS((CharSequence) _message);
	}
}
