package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class PrivateStoreMsgSell extends L2GameServerPacket
{
	private int char_obj_id;
	private String store_name;

	public PrivateStoreMsgSell(final Player player, final boolean check)
	{
		char_obj_id = player.getObjectId();
		store_name = player.getTradeList() == null || check && player.getTradeList().isSpamSell() ? "" : player.getTradeList().getSellStoreName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(156);
		writeD(char_obj_id);
		writeS((CharSequence) store_name);
	}
}
