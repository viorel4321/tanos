package l2s.gameserver.model;

public class ShortCut
{
	public static final int TYPE_ITEM = 1;
	public static final int TYPE_SKILL = 2;
	public static final int TYPE_ACTION = 3;
	public static final int TYPE_MACRO = 4;
	public static final int TYPE_RECIPE = 5;
	public final int slot;
	public final int page;
	public final int type;
	public final int id;
	public final int level;

	public ShortCut(final int slot, final int page, final int type, final int id, final int level)
	{
		this.slot = slot;
		this.page = page;
		this.type = type;
		this.id = id;
		this.level = level;
	}

	@Override
	public String toString()
	{
		return "ShortCut: " + slot + "/" + page + " ( " + type + "," + id + "," + level + ")";
	}

	public int getId()
	{
		return id;
	}

	public int getLevel()
	{
		return level;
	}

	public int getPage()
	{
		return page;
	}

	public int getSlot()
	{
		return slot;
	}

	public int getType()
	{
		return type;
	}
}
