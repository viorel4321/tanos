package l2s.gameserver.templates.item;

/**
 * @author Bonux
 */
public enum ItemGrade
{
	/*0*/NONE(ItemTemplate.CRYSTAL_NONE),
	/*1*/D(ItemTemplate.CRYSTAL_D),
	/*2*/C(ItemTemplate.CRYSTAL_C),
	/*3*/B(ItemTemplate.CRYSTAL_B),
	/*4*/A(ItemTemplate.CRYSTAL_A),
	/*5*/S(ItemTemplate.CRYSTAL_S);

	public static final ItemGrade[] VALUES = values();

	private final int _crystalId;

	ItemGrade(int crystalId)
	{
		_crystalId = crystalId;
	}

	public int getCrystalId()
	{
		return _crystalId;
	}
}