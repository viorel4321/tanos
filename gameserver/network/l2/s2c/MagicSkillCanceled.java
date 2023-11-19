package l2s.gameserver.network.l2.s2c;

public class MagicSkillCanceled extends L2GameServerPacket
{
	private int _objectId;

	public MagicSkillCanceled(final int objectId)
	{
		_objectId = objectId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(73);
		writeD(_objectId);
	}
}
