package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;

public class DeleteObject extends L2GameServerPacket
{
	private int _objectId;

	public DeleteObject(final GameObject obj)
	{
		_objectId = obj.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		if(_objectId == 0)
			return;
		final Player player = (getClient()).getActiveChar();
		if(player == null || player.getObjectId() == _objectId)
			return;
		writeC(18);
		writeD(_objectId);
		writeD(0);
	}

	@Override
	public String getType()
	{
		return super.getType() + " " + GameObjectsStorage.findObject(_objectId) + " (" + _objectId + ")";
	}
}
