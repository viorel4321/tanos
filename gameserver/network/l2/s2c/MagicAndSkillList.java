package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class MagicAndSkillList extends L2GameServerPacket
{
	private int _chaId;
	private int _unk1;
	private int _unk2;

	public MagicAndSkillList(final Creature cha, final int unk1, final int unk2)
	{
		_chaId = cha.getObjectId();
		_unk1 = unk1;
		_unk2 = unk2;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(64);
		writeD(_chaId);
		writeD(_unk1);
	}
}
