package l2s.gameserver.model;

import l2s.gameserver.Config;
import l2s.gameserver.model.base.Experience;

public class SubClass
{
	private int _class;
	private long _exp;
	private long maxExp;
	private int _sp;
	private byte _level;
	private double _Hp;
	private double _Mp;
	private double _Cp;
	private boolean _active;
	private boolean _isBase;
	private Player _player;
	private DeathPenalty _dp;

	public SubClass()
	{
		_class = 0;
		_exp = Experience.LEVEL[Config.SUBCLASS_LEVEL];
		maxExp = Experience.LEVEL[Experience.LEVEL.length - 1];
		_sp = 0;
		_level = Config.SUBCLASS_LEVEL;
		_Hp = 1.0;
		_Mp = 1.0;
		_Cp = 1.0;
		_active = false;
		_isBase = false;
	}

	public int getClassId()
	{
		return _class;
	}

	public long getExp()
	{
		return _exp;
	}

	public void addExp(final long val)
	{
		setExp(_exp + val);
	}

	public int getSp()
	{
		return _sp;
	}

	public void addSp(final long val)
	{
		setSp(_sp + val);
	}

	public byte getLevel()
	{
		return _level;
	}

	public void setClassId(final int classId)
	{
		_class = classId;
	}

	public void setExp(final long val)
	{
		if(val < 0L)
			_exp = 0L;
		else if(val <= maxExp)
			_exp = _isBase ? val : Math.max(val, Experience.LEVEL[40]);
	}

	public void setSp(long spValue)
	{
		spValue = Math.max(spValue, 0L);
		spValue = Math.min(spValue, Integer.MAX_VALUE);
		_sp = (int) spValue;
	}

	public void setHp(final double hpValue)
	{
		_Hp = hpValue;
	}

	public double getHp()
	{
		return _Hp;
	}

	public void setMp(final double mpValue)
	{
		_Mp = mpValue;
	}

	public double getMp()
	{
		return _Mp;
	}

	public void setCp(final double cpValue)
	{
		_Cp = cpValue;
	}

	public double getCp()
	{
		return _Cp;
	}

	public void setLevel(byte levelValue)
	{
		if(levelValue > Experience.LEVEL[80])
			levelValue = (byte) Experience.LEVEL[80];
		else if(levelValue < 40 && !_isBase)
			levelValue = 40;
		_level = levelValue;
	}

	public void incLevel()
	{
		if(_level > Experience.LEVEL[80])
			return;
		++_level;
	}

	public void decLevel()
	{
		if(_level == 40 && !_isBase)
			return;
		--_level;
	}

	public void setActive(final boolean active)
	{
		_active = active;
	}

	public boolean isActive()
	{
		return _active;
	}

	public void setBase(final boolean base)
	{
		_isBase = base;
	}

	public boolean isBase()
	{
		return _isBase;
	}

	public DeathPenalty getDeathPenalty()
	{
		if(_dp == null)
			_dp = new DeathPenalty(_player, (byte) 0);
		return _dp;
	}

	public void setDeathPenalty(final DeathPenalty dp)
	{
		_dp = dp;
	}

	public void setPlayer(final Player player)
	{
		_player = player;
	}
}
