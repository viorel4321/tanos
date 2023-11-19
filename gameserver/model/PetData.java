package l2s.gameserver.model;

public class PetData
{
	private int _id;
	private int _level;
	private int _feedMax;
	private int _feedBattle;
	private int _feedNormal;
	private int _pAtk;
	private int _pDef;
	private int _mAtk;
	private int _mDef;
	private int _hp;
	private int _mp;
	private int _hpRegen;
	private int _mpRegen;
	private int _exp;
	private int _accuracy;
	private int _evasion;
	private int _critical;
	private int _speed;
	private int _atkSpeed;
	private int _castSpeed;
	private int _maxLoad;

	public int getFeedBattle()
	{
		return _feedBattle;
	}

	public void setFeedBattle(final int feedBattle)
	{
		_feedBattle = feedBattle;
	}

	public int getFeedNormal()
	{
		return _feedNormal;
	}

	public void setFeedNormal(final int feedNormal)
	{
		_feedNormal = feedNormal;
	}

	public int getHP()
	{
		return _hp;
	}

	public void setHP(final int petHP)
	{
		_hp = petHP;
	}

	public int getID()
	{
		return _id;
	}

	public void setID(final int petID)
	{
		_id = petID;
	}

	public int getLevel()
	{
		return _level;
	}

	public void setLevel(final int petLevel)
	{
		_level = petLevel;
	}

	public int getMAtk()
	{
		return _mAtk;
	}

	public void setMAtk(final int mAtk)
	{
		_mAtk = mAtk;
	}

	public int getFeedMax()
	{
		return _feedMax;
	}

	public void setFeedMax(final int feedMax)
	{
		_feedMax = feedMax;
	}

	public int getMDef()
	{
		return _mDef;
	}

	public void setMDef(final int mDef)
	{
		_mDef = mDef;
	}

	public int getExp()
	{
		return _exp;
	}

	public void setExp(final int exp)
	{
		_exp = exp;
	}

	public int getMP()
	{
		return _mp;
	}

	public void setMP(final int mp)
	{
		_mp = mp;
	}

	public int getPAtk()
	{
		return _pAtk;
	}

	public void setPAtk(final int pAtk)
	{
		_pAtk = pAtk;
	}

	public int getPDef()
	{
		return _pDef;
	}

	public int getAccuracy()
	{
		return _accuracy;
	}

	public int getEvasion()
	{
		return _evasion;
	}

	public int getCritical()
	{
		return _critical;
	}

	public int getSpeed()
	{
		return _speed;
	}

	public int getAtkSpeed()
	{
		return _atkSpeed;
	}

	public int getCastSpeed()
	{
		return _castSpeed;
	}

	public int getMaxLoad()
	{
		return _maxLoad != 0 ? _maxLoad : _level * 300;
	}

	public void setPDef(final int pDef)
	{
		_pDef = pDef;
	}

	public int getHpRegen()
	{
		return _hpRegen;
	}

	public void setHpRegen(final int hpRegen)
	{
		_hpRegen = hpRegen;
	}

	public int getMpRegen()
	{
		return _mpRegen;
	}

	public void setMpRegen(final int mpRegen)
	{
		_mpRegen = mpRegen;
	}

	public void setAccuracy(final int accuracy)
	{
		_accuracy = accuracy;
	}

	public void setEvasion(final int evasion)
	{
		_evasion = evasion;
	}

	public void setCritical(final int critical)
	{
		_critical = critical;
	}

	public void setSpeed(final int speed)
	{
		_speed = speed;
	}

	public void setAtkSpeed(final int atkSpeed)
	{
		_atkSpeed = atkSpeed;
	}

	public void setCastSpeed(final int castSpeed)
	{
		_castSpeed = castSpeed;
	}

	public void setMaxLoad(final int maxLoad)
	{
		_maxLoad = maxLoad;
	}

	public static boolean isBaby(final int npcId)
	{
		return npcId > 12779 && npcId < 12783;
	}
}
