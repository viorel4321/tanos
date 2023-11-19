package l2s.gameserver.model;

import l2s.gameserver.model.items.PcInventory;

public class CharSelectInfo
{
	private String _name;
	private int _objectId;
	private int _charId;
	private long _exp;
	private int _sp;
	private int _clanId;
	private int _race;
	private int _classId;
	private int _baseClassId;
	private int _deleteTimer;
	private long _lastAccess;
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	private int _sex;
	private int _level;
	private int _karma;
	private int _maxHp;
	private double _currentHp;
	private int _maxMp;
	private double _currentMp;
	private int[][] _paperdoll;
	private int _accesslevel;

	public CharSelectInfo(final int objectId, final String name)
	{
		_objectId = 0;
		_charId = 199546;
		_exp = 0L;
		_sp = 0;
		_clanId = 0;
		_race = 0;
		_classId = 0;
		_baseClassId = 0;
		_deleteTimer = 0;
		_lastAccess = 0L;
		_face = 0;
		_hairStyle = 0;
		_hairColor = 0;
		_sex = 0;
		_level = 1;
		_karma = 0;
		_maxHp = 0;
		_currentHp = 0.0;
		_maxMp = 0;
		_currentMp = 0.0;
		_accesslevel = 0;
		setObjectId(objectId);
		_name = name;
		_paperdoll = PcInventory.restoreVisibleInventory(objectId);
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public void setObjectId(final int objectId)
	{
		_objectId = objectId;
	}

	public int getCharId()
	{
		return _charId;
	}

	public void setCharId(final int charId)
	{
		_charId = charId;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public void setClanId(final int clanId)
	{
		_clanId = clanId;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getBaseClassId()
	{
		return _baseClassId;
	}

	public void setBaseClassId(final int baseClassId)
	{
		_baseClassId = baseClassId;
	}

	public void setClassId(final int classId)
	{
		_classId = classId;
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public void setCurrentHp(final double currentHp)
	{
		_currentHp = currentHp;
	}

	public double getCurrentMp()
	{
		return _currentMp;
	}

	public void setCurrentMp(final double currentMp)
	{
		_currentMp = currentMp;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(final int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(final long lastAccess)
	{
		_lastAccess = lastAccess;
	}

	public long getExp()
	{
		return _exp;
	}

	public void setExp(final long exp)
	{
		_exp = exp;
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(final int face)
	{
		_face = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(final int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(final int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public int getPaperdollObjectId(final int slot)
	{
		return _paperdoll[slot][0];
	}

	public int[] getPaperdollVariationsId(final int slot)
	{
		return PcInventory.getVariationsId(_paperdoll[slot][0]);
	}

	public int getPaperdollItemId(final int slot)
	{
		return _paperdoll[slot][1];
	}

	public int getLevel()
	{
		return _level;
	}

	public void setLevel(final int level)
	{
		_level = level;
	}

	public int getMaxHp()
	{
		return _maxHp;
	}

	public void setMaxHp(final int maxHp)
	{
		_maxHp = maxHp;
	}

	public int getMaxMp()
	{
		return _maxMp;
	}

	public void setMaxMp(final int maxMp)
	{
		_maxMp = maxMp;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(final String name)
	{
		_name = name;
	}

	public int getRace()
	{
		return _race;
	}

	public void setRace(final int race)
	{
		_race = race;
	}

	public int getSex()
	{
		return _sex;
	}

	public void setSex(final int sex)
	{
		_sex = sex;
	}

	public int getSp()
	{
		return _sp;
	}

	public void setSp(final int sp)
	{
		_sp = sp;
	}

	public int getEnchantEffect()
	{
		return _paperdoll[7][2];
	}

	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(final int karma)
	{
		_karma = karma;
	}

	public int getAccessLevel()
	{
		return _accesslevel;
	}

	public void setAccessLevel(final int accesslevel)
	{
		_accesslevel = accesslevel;
	}
}
