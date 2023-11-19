package l2s.gameserver.model.instances;

import l2s.gameserver.Config;
import l2s.gameserver.templates.HennaTemplate;

public class HennaInstance
{
	private HennaTemplate _template;
	private short _symbolId;
	private short _itemIdDye;
	private int _price;
	private byte _statINT;
	private byte _statSTR;
	private byte _statCON;
	private byte _statMEN;
	private byte _statDEX;
	private byte _statWIT;
	private byte _amountDyeRequire;

	public HennaInstance(final HennaTemplate template)
	{
		_template = template;
		_symbolId = _template.symbol_id;
		_itemIdDye = _template.dye;
		_amountDyeRequire = _template.amount;
		_price = (int) (_template.price * Config.HENNA_PRICE_MOD);
		_statINT = _template.stat_INT;
		_statSTR = _template.stat_STR;
		_statCON = _template.stat_CON;
		_statMEN = _template.stat_MEN;
		_statDEX = _template.stat_DEX;
		_statWIT = _template.stat_WIT;
	}

	public String getName()
	{
		String res = "";
		if(_statINT > 0)
			res = res + "INT +" + _statINT;
		else if(_statSTR > 0)
			res = res + "STR +" + _statSTR;
		else if(_statCON > 0)
			res = res + "CON +" + _statCON;
		else if(_statMEN > 0)
			res = res + "MEN +" + _statMEN;
		else if(_statDEX > 0)
			res = res + "DEX +" + _statDEX;
		else if(_statWIT > 0)
			res = res + "WIT +" + _statWIT;
		if(_statINT < 0)
			res = res + ", INT " + _statINT;
		else if(_statSTR < 0)
			res = res + ", STR " + _statSTR;
		else if(_statCON < 0)
			res = res + ", CON " + _statCON;
		else if(_statMEN < 0)
			res = res + ", MEN " + _statMEN;
		else if(_statDEX < 0)
			res = res + ", DEX " + _statDEX;
		else if(_statWIT < 0)
			res = res + ", WIT " + _statWIT;
		return res;
	}

	public HennaTemplate getTemplate()
	{
		return _template;
	}

	public short getSymbolId()
	{
		return _symbolId;
	}

	public void setSymbolId(final short SymbolId)
	{
		_symbolId = SymbolId;
	}

	public short getItemIdDye()
	{
		return _itemIdDye;
	}

	public void setItemIdDye(final short ItemIdDye)
	{
		_itemIdDye = ItemIdDye;
	}

	public byte getAmountDyeRequire()
	{
		return _amountDyeRequire;
	}

	public void setAmountDyeRequire(final byte AmountDyeRequire)
	{
		_amountDyeRequire = AmountDyeRequire;
	}

	public int getPrice()
	{
		return _price;
	}

	public void setPrice(final int Price)
	{
		_price = Price;
	}

	public byte getStatINT()
	{
		return _statINT;
	}

	public void setStatINT(final byte StatINT)
	{
		_statINT = StatINT;
	}

	public byte getStatSTR()
	{
		return _statSTR;
	}

	public void setStatSTR(final byte StatSTR)
	{
		_statSTR = StatSTR;
	}

	public byte getStatCON()
	{
		return _statCON;
	}

	public void setStatCON(final byte StatCON)
	{
		_statCON = StatCON;
	}

	public byte getStatMEN()
	{
		return _statMEN;
	}

	public void setStatMEN(final byte StatMEN)
	{
		_statMEN = StatMEN;
	}

	public byte getStatDEX()
	{
		return _statDEX;
	}

	public void setStatDEX(final byte StatDEX)
	{
		_statDEX = StatDEX;
	}

	public byte getStatWIT()
	{
		return _statWIT;
	}

	public void setStatWIT(final byte StatWIT)
	{
		_statWIT = StatWIT;
	}
}
