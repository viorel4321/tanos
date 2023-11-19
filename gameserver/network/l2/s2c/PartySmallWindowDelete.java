package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class PartySmallWindowDelete extends L2GameServerPacket
{
	private int member_obj_id;
	private String member_name;

	public PartySmallWindowDelete(final Player member)
	{
		member_obj_id = member.getObjectId();
		member_name = member.getName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(81);
		writeD(member_obj_id);
		writeS((CharSequence) member_name);
	}
}
