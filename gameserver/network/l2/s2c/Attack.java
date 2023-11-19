package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;

public class Attack extends L2GameServerPacket
{
	public final int _attackerId;
	public final boolean _soulshot;
	private final int _grade;
	private int _x;
	private int _y;
	private int _z;
	private Hit[] hits;

	public Attack(final Creature attacker, final Creature target, final boolean ss, final int grade)
	{
		_attackerId = attacker.getObjectId();
		_soulshot = ss;
		_grade = grade;
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
		hits = new Hit[0];
	}

	public void addHit(final GameObject target, final int damage, final boolean miss, final boolean crit, final boolean shld)
	{
		final int pos = hits.length;
		final Hit[] tmp = new Hit[pos + 1];
		for(int i = 0; i < hits.length; ++i)
			tmp[i] = hits[i];
		tmp[pos] = new Hit(target, damage, miss, crit, shld);
		hits = tmp;
	}

	public boolean hasHits()
	{
		return hits.length > 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(5);
		writeD(_attackerId);
		writeD(hits[0]._targetId);
		writeD(hits[0]._damage);
		writeC(hits[0]._flags);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(hits.length - 1);
		for(int i = 1; i < hits.length; ++i)
		{
			writeD(hits[i]._targetId);
			writeD(hits[i]._damage);
			writeC(hits[i]._flags);
		}
	}

	private class Hit
	{
		int _targetId;
		int _damage;
		int _flags;

		Hit(final GameObject target, final int damage, final boolean miss, final boolean crit, final boolean shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;
			if(_soulshot)
				_flags |= 0x10 | _grade;
			if(crit)
				_flags |= 0x20;
			if(shld)
				_flags |= 0x40;
			if(miss)
				_flags |= 0x80;
		}
	}
}
