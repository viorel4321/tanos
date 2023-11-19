package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExStorageMaxCount extends L2GameServerPacket
{
	private int _inventory;
	private int _warehouse;
	private int _freight;
	private int _privateSell;
	private int _privateBuy;
	private int _recipeDwarven;
	private int _recipeCommon;

	public ExStorageMaxCount(final Player player)
	{
		_inventory = player.getInventoryLimit();
		_warehouse = player.getWarehouseLimit();
		_freight = player.getFreightLimit();
		final int tradeLimit = player.getTradeLimit();
		_privateSell = tradeLimit;
		_privateBuy = tradeLimit;
		_recipeDwarven = player.getDwarvenRecipeLimit();
		_recipeCommon = player.getCommonRecipeLimit();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(46);
		writeD(_inventory);
		writeD(_warehouse);
		writeD(_freight);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_recipeDwarven);
		writeD(_recipeCommon);
	}
}
