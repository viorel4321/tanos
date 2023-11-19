package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class TitleUpdate extends L2GameServerPacket
{
	private final int objectId;
	private final String title;

	public TitleUpdate(final Creature cha)
	{
		objectId = cha.getObjectId();
		title = cha.getTitle();
	}

	@Override
	protected void writeImpl()
	{
		writeC(204);
		writeD(objectId);
		writeS((CharSequence) title);
	}
}
