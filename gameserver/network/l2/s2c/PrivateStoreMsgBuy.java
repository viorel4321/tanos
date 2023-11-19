package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class PrivateStoreMsgBuy extends L2GameServerPacket
{
	private int char_obj_id;
	private String store_name;

	public PrivateStoreMsgBuy(final Player player, final boolean check)
	{
		char_obj_id = player.getObjectId();
		store_name = player.getTradeList() == null || check && player.getTradeList().isSpamBuy() ? "" : player.getTradeList().getBuyStoreName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(185);
		writeD(char_obj_id);
		writeS((CharSequence) store_name);
	}
}
