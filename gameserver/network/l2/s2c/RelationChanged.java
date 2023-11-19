package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;

public class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PVP_FLAG = 2;
	public static final int RELATION_HAS_KARMA = 4;
	public static final int RELATION_LEADER = 128;
	public static final int RELATION_INSIEGE = 512;
	public static final int RELATION_ATTACKER = 1024;
	public static final int RELATION_ALLY = 2048;
	public static final int RELATION_ENEMY = 4096;
	public static final int RELATION_MUTUAL_WAR = 32768;
	public static final int RELATION_1SIDED_WAR = 65536;
	protected final List<RelationChangedData> _data;

	protected RelationChanged(final int s)
	{
		_data = new ArrayList<RelationChangedData>(s);
	}

	protected void add(final RelationChangedData data)
	{
		_data.add(data);
	}

	@Override
	protected void writeImpl()
	{
		writeC(206);
		for(final RelationChangedData d : _data)
		{
			writeD(d.charObjId);
			writeD(d.relation);
			writeD(d.isAutoAttackable ? 1 : 0);
			writeD(d.karma);
			writeD(d.pvpFlag);
		}
	}

	public static L2GameServerPacket update(final Player sendTo, final Playable targetPlayable, final Player activeChar)
	{
		if(sendTo == null || targetPlayable == null || activeChar == null)
			return null;
		final Player targetPlayer = targetPlayable.getPlayer();
		final int relation = targetPlayer == null ? 0 : targetPlayer.getRelation(activeChar);
		final RelationChanged pkt = new RelationChanged(1);
		pkt.add(new RelationChangedData(targetPlayable, targetPlayable.isAutoAttackable(activeChar), relation));
		return pkt;
	}

	static class RelationChangedData
	{
		public final int charObjId;
		public final boolean isAutoAttackable;
		public final int relation;
		public final int karma;
		public final int pvpFlag;

		public RelationChangedData(final Playable cha, final boolean _isAutoAttackable, final int _relation)
		{
			isAutoAttackable = _isAutoAttackable;
			relation = _relation;
			charObjId = cha.getObjectId();
			karma = cha.getKarma();
			pvpFlag = cha.getPvpFlag();
		}
	}
}
