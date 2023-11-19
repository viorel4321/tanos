package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.NpcInstance;

public class MonRaceInfo extends L2GameServerPacket
{
	private int _unknown1;
	private int _unknown2;
	private NpcInstance[] _monsters;
	private int[][] _speeds;

	public MonRaceInfo(final int unknown1, final int unknown2, final NpcInstance[] monsters, final int[][] speeds)
	{
		_unknown1 = unknown1;
		_unknown2 = unknown2;
		_monsters = monsters;
		_speeds = speeds;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(221);
		writeD(_unknown1);
		writeD(_unknown2);
		writeD(8);
		for(int i = 0; i < 8; ++i)
		{
			writeD(_monsters[i].getObjectId());
			writeD(_monsters[i].getTemplate().npcId + 1000000);
			writeD(14107);
			writeD(181875 + 58 * (7 - i));
			writeD(-3566);
			writeD(12080);
			writeD(181875 + 58 * (7 - i));
			writeD(-3566);
			writeF(_monsters[i].getCollisionHeight());
			writeF(_monsters[i].getCollisionRadius());
			writeD(120);
			for(int j = 0; j < 20; ++j)
			{
				if(_unknown1 == 0)
					writeC(_speeds[i][j]);
				else
					writeC(0);
			}
			writeD(0);
		}
	}
}
