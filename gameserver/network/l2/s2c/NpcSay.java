package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.NpcInstance;

public class NpcSay extends L2GameServerPacket
{
	int _objId;
	int _type;
	int _id;
	String _text;

	public NpcSay(final NpcInstance npc, final int chatType, final String text)
	{
		_objId = npc.getObjectId();
		_type = chatType;
		_text = text;
		_id = npc.getNpcId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(2);
		writeD(_objId);
		writeD(_type);
		writeD(1000000 + _id);
		writeS((CharSequence) _text);
	}
}
