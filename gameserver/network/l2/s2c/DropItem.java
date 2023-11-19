package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;

public class DropItem extends L2GameServerPacket
{
	private final int playerObjectId;
	private final int itemObjectId;
	private final int itemId;
	private final boolean stackable;
	private final int count;
	private final int x, y, z;

	public DropItem(final ItemInstance item, int playerObjectId)
	{
		this.playerObjectId = playerObjectId;
		this.itemObjectId = item.getObjectId();
		this.itemId = item.getItemId();
		this.x = item.getLoc().getX();
		this.y = item.getLoc().getY();
		this.z = item.getLoc().getZ();
		this.stackable = item.isStackable();
		this.count = item.getIntegerLimitedCount();
	}

	public DropItem(int playerObjectId, int itemObjectId, int itemId, int x, int y, int z, boolean stackable, int count)
	{
		this.playerObjectId = playerObjectId;
		this.itemObjectId = itemObjectId;
		this.itemId = itemId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.stackable = stackable;
		this.count = count;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(12);
		writeD(playerObjectId);
		writeD(itemObjectId);
		writeD(itemId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(stackable);
		writeD(count);
		writeD(1);
	}
}
