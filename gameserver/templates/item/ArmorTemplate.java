package l2s.gameserver.templates.item;

import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public final class ArmorTemplate extends ItemTemplate
{
	public static final double EMPTY_RING = 5.0;
	public static final double EMPTY_EARRING = 9.0;
	public static final double EMPTY_NECKLACE = 13.0;
	public static final double EMPTY_HELMET = 12.0;
	public static final double EMPTY_BODY_FIGHTER = 31.0;
	public static final double EMPTY_LEGS_FIGHTER = 18.0;
	public static final double EMPTY_BODY_MYSTIC = 15.0;
	public static final double EMPTY_LEGS_MYSTIC = 8.0;
	public static final double EMPTY_GLOVES = 8.0;
	public static final double EMPTY_BOOTS = 7.0;
	private final int _pDef;
	private final int _mDef;
	private final int _mpBonus;
	private final double _evsmod;

	public ArmorTemplate(final ArmorType type, final StatsSet set)
	{
		super(type, set);
		_pDef = set.getInteger("p_def");
		_mDef = set.getInteger("m_def");
		_mpBonus = set.getInteger("mp_bonus");
		_evsmod = set.getDouble("avoid_modify");
		if(_pDef > 0)
			attachFunc(new FuncTemplate(null, "Add", Stats.POWER_DEFENCE, 16, _pDef));
		if(_mDef > 0)
			attachFunc(new FuncTemplate(null, "Add", Stats.MAGIC_DEFENCE, 16, _mDef));
		if(_mpBonus > 0)
			attachFunc(new FuncTemplate(null, "Add", Stats.MAX_MP, 16, _mpBonus));
		if(_evsmod != 0.0)
			attachFunc(new FuncTemplate(null, "Add", Stats.EVASION_RATE, 16, _evsmod));
		if(getItemGrade().ordinal() != 0)
		{
			if(_pDef > 0)
				attachFunc(new FuncTemplate(null, "Enchant", Stats.POWER_DEFENCE, 12, 0.0));
			if(_mDef > 0)
				attachFunc(new FuncTemplate(null, "Enchant", Stats.MAGIC_DEFENCE, 12, 0.0));
		}
	}

	@Override
	public ArmorType getItemType()
	{
		return (ArmorType) super.type;
	}

	@Override
	public final long getItemMask()
	{
		return getItemType().mask();
	}

	public enum ArmorType
	{
		NONE(1, "None"),
		LIGHT(2, "Light"),
		HEAVY(3, "Heavy"),
		MAGIC(4, "Magic"),
		PET(5, "Pet");

		public static final ArmorType[] VALUES = values();

		final int _id;
		final String _name;

		private ArmorType(final int id, final String name)
		{
			_id = id;
			_name = name;
		}

		public long mask()
		{
			return 1L << _id + WeaponTemplate.WeaponType.values().length;
		}

		@Override
		public String toString()
		{
			return _name;
		}
	}
}
