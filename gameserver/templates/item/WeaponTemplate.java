package l2s.gameserver.templates.item;

import l2s.gameserver.Config;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.funcs.FuncTemplate;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.StatsSet;

public final class WeaponTemplate extends ItemTemplate
{
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _pDam;
	private final int _rndDam;
	private final int _atkReuse;
	private final int _mpConsume;
	private final int _mDam;
	private final int _aSpd;
	private final int _critical;
	private final double _accmod;
	private final double _evsmod;
	private final int _sDef;
	private final int _rShld;
	private Skill _enchant4Skill;
	private final boolean _sa;
	private final boolean _isMagicWeapon;

	public WeaponTemplate(final WeaponType type, final StatsSet set)
	{
		super(type, set);
		_enchant4Skill = null;
		_soulShotCount = Config.INFINITY_SS ? 1 : set.getInteger("soulshots");
		_spiritShotCount = Config.INFINITY_SS ? 1 : set.getInteger("spiritshots");
		_pDam = set.getInteger("p_dam");
		_rndDam = set.getInteger("rnd_dam");
		_mpConsume = set.getInteger("mp_consume");
		_isMagicWeapon = set.getBool("is_magic_weapon", false);
		_mDam = set.getInteger("m_dam");
		_aSpd = set.getInteger("atk_speed");
		_atkReuse = set.getInteger("atk_reuse", type == WeaponType.BOW ? 1500 : 0);
		_critical = set.getInteger("critical");
		_accmod = set.getDouble("hit_modify");
		_evsmod = set.getDouble("avoid_modify");
		_sDef = set.getInteger("shield_def");
		_rShld = set.getInteger("shield_def_rate");
		_sa = !_icon.endsWith("i00");
		if(type == WeaponType.POLE)
			attachSkill(SkillTable.getInstance().getInfo(3599, 1));
		final int sId = set.getInteger("enchant4_skill_id");
		final int sLv = set.getInteger("enchant4_skill_lvl");
		if(sId > 0 && sLv > 0)
			_enchant4Skill = SkillTable.getInstance().getInfo(sId, sLv);
		if(_pDam != 0)
			attachFunc(new FuncTemplate(null, "Add", Stats.POWER_ATTACK, 16, _pDam));
		if(_mDam != 0)
			attachFunc(new FuncTemplate(null, "Add", Stats.MAGIC_ATTACK, 16, _mDam));
		if(_critical != 0)
			attachFunc(new FuncTemplate(null, "Set", Stats.CRITICAL_BASE, 8, _critical * 10));
		if(_aSpd != 0)
			attachFunc(new FuncTemplate(null, "Set", Stats.ATK_BASE, 8, _aSpd));
		if(_sDef != 0)
			attachFunc(new FuncTemplate(null, "Add", Stats.SHIELD_DEFENCE, 16, _sDef));
		if(_accmod != 0.0)
			attachFunc(new FuncTemplate(null, "Add", Stats.ACCURACY_COMBAT, 16, _accmod));
		if(_evsmod != 0.0)
			attachFunc(new FuncTemplate(null, "Add", Stats.EVASION_RATE, 16, _evsmod));
		if(_rShld != 0)
			attachFunc(new FuncTemplate(null, "Add", Stats.SHIELD_RATE, 16, _rShld));
		if(getItemGrade().ordinal() != 0)
		{
			if(_sDef > 0)
				attachFunc(new FuncTemplate(null, "Enchant", Stats.SHIELD_DEFENCE, 12, 0.0));
			if(_pDam > 0)
				attachFunc(new FuncTemplate(null, "Enchant", Stats.POWER_ATTACK, 12, 0.0));
			if(_mDam > 0)
				attachFunc(new FuncTemplate(null, "Enchant", Stats.MAGIC_ATTACK, 12, 0.0));
		}
	}

	@Override
	public WeaponType getItemType()
	{
		return (WeaponType) type;
	}

	public int getBaseSpeed()
	{
		return _aSpd;
	}

	@Override
	public long getItemMask()
	{
		return getItemType().mask();
	}

	public int getSoulShotCount()
	{
		return _soulShotCount;
	}

	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}

	public int getPDamage()
	{
		return _pDam;
	}

	public int getCritical()
	{
		return _critical;
	}

	public int getRandomDamage()
	{
		return _rndDam;
	}

	public int getAttackReuseDelay()
	{
		return _atkReuse;
	}

	public int getMDamage()
	{
		return _mDam;
	}

	public int getMpConsume()
	{
		return _mpConsume;
	}

	public int getAttackRange()
	{
		switch(getItemType())
		{
			case BOW:
			{
				return 460;
			}
			case POLE:
			{
				return 40;
			}
			default:
			{
				return 0;
			}
		}
	}

	public Skill getEnchant4Skill()
	{
		return _enchant4Skill;
	}

	@Override
	public boolean isSa()
	{
		return _sa;
	}

	@Override
	public WeaponFightType getWeaponFightType()
	{
		if(_isMagicWeapon)
			return WeaponFightType.MAGE;
		return WeaponFightType.WARRIOR;
	}

	public enum WeaponType
	{
		NONE(1, "Shield", (Stats) null),
		SWORD(2, "Sword", Stats.SWORD_WPN_RECEPTIVE),
		BLUNT(3, "Blunt", Stats.BLUNT_WPN_RECEPTIVE),
		DAGGER(4, "Dagger", Stats.DAGGER_WPN_RECEPTIVE),
		BOW(5, "Bow", Stats.BOW_WPN_RECEPTIVE),
		POLE(6, "Pole", Stats.POLE_WPN_RECEPTIVE),
		ETC(7, "Etc", (Stats) null),
		FIST(8, "Fist", Stats.FIST_WPN_RECEPTIVE),
		DUAL(9, "Dual Sword", Stats.DUAL_WPN_RECEPTIVE),
		DUALFIST(10, "Dual Fist", Stats.FIST_WPN_RECEPTIVE),
		BIGSWORD(11, "Big Sword", Stats.SWORD_WPN_RECEPTIVE),
		PET(12, "Pet", Stats.FIST_WPN_RECEPTIVE),
		ROD(13, "Rod", (Stats) null),
		BIGBLUNT(14, "Big Blunt", Stats.BLUNT_WPN_RECEPTIVE);

		public static final WeaponType[] VALUES = values();

		private final int _id;
		private final String _name;
		private final Stats _defence;

		private WeaponType(final int id, final String name, final Stats defence)
		{
			_id = id;
			_name = name;
			_defence = defence;
		}

		public long mask()
		{
			return 1L << _id;
		}

		public Stats getDefence()
		{
			return _defence;
		}

		@Override
		public String toString()
		{
			return _name;
		}
	}
}
