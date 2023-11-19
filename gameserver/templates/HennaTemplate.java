package l2s.gameserver.templates;

public class HennaTemplate
{
	public final short symbol_id;
	public final String symbol_name;
	public final short dye;
	public final int price;
	public final byte amount;
	public final byte stat_INT;
	public final byte stat_STR;
	public final byte stat_CON;
	public final byte stat_MEN;
	public final byte stat_DEX;
	public final byte stat_WIT;

	public HennaTemplate(final StatsSet set)
	{
		symbol_id = set.getShort("symbol_id");
		symbol_name = "";
		dye = set.getShort("dye");
		price = set.getInteger("price");
		amount = set.getByte("amount");
		stat_INT = set.getByte("stat_INT");
		stat_STR = set.getByte("stat_STR");
		stat_CON = set.getByte("stat_CON");
		stat_MEN = set.getByte("stat_MEN");
		stat_DEX = set.getByte("stat_DEX");
		stat_WIT = set.getByte("stat_WIT");
	}

	public short getSymbolId()
	{
		return symbol_id;
	}

	public short getDyeId()
	{
		return dye;
	}

	public int getPrice()
	{
		return price;
	}

	public byte getAmountDyeRequire()
	{
		return amount;
	}

	public byte getStatINT()
	{
		return stat_INT;
	}

	public byte getStatSTR()
	{
		return stat_STR;
	}

	public byte getStatCON()
	{
		return stat_CON;
	}

	public byte getStatMEN()
	{
		return stat_MEN;
	}

	public byte getStatDEX()
	{
		return stat_DEX;
	}

	public byte getStatWIT()
	{
		return stat_WIT;
	}
}
