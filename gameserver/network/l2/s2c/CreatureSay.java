package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class CreatureSay extends L2GameServerPacket
{
	private final int _objectId;
	private final int _textType;
	private String _charName;
	private int _charId;
	private String _text;
	private int _npcString;
	private List<String> _parameters;

	public CreatureSay(final int objectId, final int messageType, final String charName, final String text)
	{
		_charName = null;
		_charId = 0;
		_text = null;
		_npcString = -1;
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}

	public CreatureSay(final int objectId, final int messageType, final int charId, final int id)
	{
		_charName = null;
		_charId = 0;
		_text = null;
		_npcString = -1;
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = id;
	}

	public void addStringParameter(final String text)
	{
		if(_parameters == null)
			_parameters = new ArrayList<String>();
		_parameters.add(text);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(74);
		writeD(_objectId);
		writeD(_textType);
		if(_charName != null)
			writeS(_charName);
		else
			writeD(_charId);
		writeD(_npcString);
		if(_text != null)
			writeS(_text);
		else if(_parameters != null)
		{
			for(final String s : _parameters)
				writeS(s);
		}
	}
}
