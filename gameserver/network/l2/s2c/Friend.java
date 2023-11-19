package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class Friend extends L2GameServerPacket
{
	boolean _add;
	boolean _online;
	String _name;
	int _object_id;

	public Friend(final Player player, final boolean add)
	{
		_add = add;
		_name = player.getName();
		_object_id = player.getObjectId();
		_online = true;
	}

	public Friend(final String name, final boolean add, final boolean online, final int object_id)
	{
		_name = name;
		_add = add;
		_object_id = object_id;
		_online = online;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(251);
		writeD(_add ? 1 : 3);
		writeD(0);
		writeS(_name);
		writeD(_online ? 1 : 0);
		writeD(_object_id);
	}
}
