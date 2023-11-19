package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;

public class HennaUnequipInfo extends L2GameServerPacket
{
	private int _str;
	private int _con;
	private int _dex;
	private int _int;
	private int _wit;
	private int _men;
	private int _adena;
	private HennaInstance _henna;

	public HennaUnequipInfo(final HennaInstance henna, final Player player)
	{
		_henna = henna;
		_adena = player.getAdena();
		_str = player.getSTR();
		_dex = player.getDEX();
		_con = player.getCON();
		_int = player.getINT();
		_wit = player.getWIT();
		_men = player.getMEN();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(230);
		writeD((int) _henna.getSymbolId());
		writeD((int) _henna.getItemIdDye());
		writeD(_henna.getAmountDyeRequire() / 2);
		writeD(_henna.getPrice() / 5);
		writeD(1);
		writeD(_adena);
		writeD(_int);
		writeC(_int - _henna.getStatINT());
		writeD(_str);
		writeC(_str - _henna.getStatSTR());
		writeD(_con);
		writeC(_con - _henna.getStatCON());
		writeD(_men);
		writeC(_men - _henna.getStatMEN());
		writeD(_dex);
		writeC(_dex - _henna.getStatDEX());
		writeD(_wit);
		writeC(_wit - _henna.getStatWIT());
	}
}
