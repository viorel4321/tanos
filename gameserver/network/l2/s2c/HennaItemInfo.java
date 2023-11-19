package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;

public class HennaItemInfo extends L2GameServerPacket
{
	private int char_adena;
	private int _str;
	private int _con;
	private int _dex;
	private int _int;
	private int _wit;
	private int _men;
	private HennaInstance _henna;

	public HennaItemInfo(final HennaInstance henna, final Player player)
	{
		_henna = henna;
		char_adena = player.getAdena();
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
		writeC(227);
		writeD((int) _henna.getSymbolId());
		writeD((int) _henna.getItemIdDye());
		writeD((int) _henna.getAmountDyeRequire());
		writeD(_henna.getPrice());
		writeD(1);
		writeD(char_adena);
		writeD(_int);
		writeC(_int + _henna.getStatINT());
		writeD(_str);
		writeC(_str + _henna.getStatSTR());
		writeD(_con);
		writeC(_con + _henna.getStatCON());
		writeD(_men);
		writeC(_men + _henna.getStatMEN());
		writeD(_dex);
		writeC(_dex + _henna.getStatDEX());
		writeD(_wit);
		writeC(_wit + _henna.getStatWIT());
	}
}
