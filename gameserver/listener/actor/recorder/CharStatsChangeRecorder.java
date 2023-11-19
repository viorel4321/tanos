package l2s.gameserver.listener.actor.recorder;

import l2s.gameserver.model.Creature;

public class CharStatsChangeRecorder<T extends Creature>
{
	public static final int BROADCAST_CHAR_INFO = 1;
	public static final int SEND_CHAR_INFO = 2;
	public static final int SEND_STATUS_INFO = 4;
	protected final T _activeChar;
	protected int _level;
	protected int _accuracy;
	protected int _attackSpeed;
	protected int _castSpeed;
	protected int _criticalHit;
	protected int _evasion;
	protected int _magicAttack;
	protected int _magicDefence;
	protected int _physicAttack;
	protected int _physicDefence;
	protected int _runSpeed;
	protected int _abnormalEffects;
	protected int _team;
	protected int _changes;

	public CharStatsChangeRecorder(final T actor)
	{
		this._activeChar = actor;
	}

	protected int set(final int flag, final int oldValue, final int newValue)
	{
		if(oldValue != newValue)
			this._changes |= flag;
		return newValue;
	}

	protected long set(final int flag, final long oldValue, final long newValue)
	{
		if(oldValue != newValue)
			this._changes |= flag;
		return newValue;
	}

	protected String set(final int flag, final String oldValue, final String newValue)
	{
		if(!oldValue.equals(newValue))
			this._changes |= flag;
		return newValue;
	}

	protected <E extends Enum<E>> E set(final int flag, final E oldValue, final E newValue)
	{
		if(oldValue != newValue)
			this._changes |= flag;
		return newValue;
	}

	protected void refreshStats()
	{
		this._accuracy = this.set(2, this._accuracy, this._activeChar.getAccuracy());
		this._attackSpeed = this.set(1, this._attackSpeed, this._activeChar.getPAtkSpd());
		this._castSpeed = this.set(1, this._castSpeed, this._activeChar.getMAtkSpd());
		this._criticalHit = this.set(2, this._criticalHit, this._activeChar.getCriticalHit(null, null));
		this._evasion = this.set(2, this._evasion, this._activeChar.getEvasionRate(null));
		this._runSpeed = this.set(1, this._runSpeed, this._activeChar.getMoveSpeed());
		this._physicAttack = this.set(2, this._physicAttack, this._activeChar.getPAtk(null));
		this._physicDefence = this.set(2, this._physicDefence, this._activeChar.getPDef(null));
		this._magicAttack = this.set(2, this._magicAttack, this._activeChar.getMAtk(null, null));
		this._magicDefence = this.set(2, this._magicDefence, this._activeChar.getMDef(null, null));
		this._level = this.set(2, this._level, this._activeChar.getLevel());
		this._abnormalEffects = this.set(1, this._abnormalEffects, this._activeChar.getAbnormalEffect());
		this._team = this.set(1, this._team, this._activeChar.getTeam());
	}

	public final void sendChanges()
	{
		this.refreshStats();
		this.onSendChanges();
		this._changes = 0;
	}

	protected void onSendChanges()
	{
		if((this._changes & 0x4) == 0x4)
			this._activeChar.broadcastStatusUpdate();
	}
}
