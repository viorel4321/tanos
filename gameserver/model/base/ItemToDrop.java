package l2s.gameserver.model.base;

public class ItemToDrop
{
	public int itemId;
	public int count;
	public boolean isSpoil;
	public boolean isAdena;

	public ItemToDrop(final int id)
	{
		itemId = id;
	}
}
