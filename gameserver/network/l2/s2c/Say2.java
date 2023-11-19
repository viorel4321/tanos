package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.ChatType;

public class Say2 extends L2GameServerPacket
{
	private final int _objectId;
	private final ChatType _chatType;
	private final String _text;

	private String _charName;

	public Say2(int objectId, ChatType chatType, String charName, String text)
	{
		_objectId = objectId;
		_chatType = chatType;
		_charName = charName;
		_text = text;
	}

	public void setCharName(String name)
	{
		_charName = name;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(74);
		writeD(_objectId);
		writeD(_chatType.ordinal());
		writeS(_charName);
		writeS(_text);

		Player player = (getClient()).getActiveChar();
		if(player != null)
			player.broadcastSnoop(_chatType.ordinal(), _charName, _text);
	}
}
