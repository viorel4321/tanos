package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.Location;

public class GetItem extends L2GameServerPacket
{
	private int _playerId;
	private int _itemObjId;
	private Location _loc;

	public GetItem(final ItemInstance item, final int playerId)
	{
		_itemObjId = item.getObjectId();
		_loc = item.getLoc();
		_playerId = playerId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(13);
		writeD(_playerId);
		writeD(_itemObjId);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
	}
}
