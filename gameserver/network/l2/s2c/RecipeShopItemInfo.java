package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;

public class RecipeShopItemInfo extends L2GameServerPacket
{
	private boolean _canWrite = false;
	private int _recipeId;
	private int _shopId;
	private int curMp;
	private int maxMp;
	private int _success;

	public RecipeShopItemInfo(final int shopId, final int recipeId, final int success, final Player activeChar)
	{
		_success = -1;
		_recipeId = recipeId;
		_shopId = shopId;
		_success = success;
		final GameObject manufacturer = activeChar.getVisibleObject(_shopId);
		if(manufacturer == null)
			return;
		if(!manufacturer.isPlayer())
			return;
		curMp = (int) ((Player) manufacturer).getCurrentMp();
		maxMp = ((Player) manufacturer).getMaxMp();
		_canWrite = true;
	}

	@Override
	protected boolean canWrite()
	{
		return _canWrite;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(218);
		writeD(_shopId);
		writeD(_recipeId);
		writeD(curMp);
		writeD(maxMp);
		writeD(_success);
	}
}
