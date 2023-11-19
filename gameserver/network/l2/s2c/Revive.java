package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.GameObject;

public class Revive extends L2GameServerPacket
{
	private int _objectId;

	public Revive(final GameObject obj)
	{
		_objectId = obj.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(7);
		writeD(_objectId);
	}
}
