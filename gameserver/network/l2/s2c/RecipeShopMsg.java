package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class RecipeShopMsg extends L2GameServerPacket
{
	private int _chaObjectId;
	private String _chaStoreName;

	public RecipeShopMsg(final Player player, final boolean check)
	{
		if(player.getCreateList() == null || player.getCreateList().getStoreName() == null)
			return;
		_chaObjectId = player.getObjectId();
		_chaStoreName = check && player.getCreateList().isSpam() ? "" : player.getCreateList().getStoreName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(219);
		writeD(_chaObjectId);
		writeS((CharSequence) _chaStoreName);
	}
}
