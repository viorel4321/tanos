package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;

public class HennaInfo extends L2GameServerPacket
{
	private final HennaInstance[] _hennas;
	private int _count;
	private int _str;
	private int _con;
	private int _dex;
	private int _int;
	private int _wit;
	private int _men;

	public HennaInfo(final Player player)
	{
		_hennas = new HennaInstance[3];
		int j = 0;
		for(int i = 0; i < 3; ++i)
		{
			final HennaInstance h = player.getHenna(i + 1);
			if(h != null)
				_hennas[j++] = h;
		}
		_count = j;
		_str = player.getHennaStatSTR();
		_con = player.getHennaStatCON();
		_dex = player.getHennaStatDEX();
		_int = player.getHennaStatINT();
		_wit = player.getHennaStatWIT();
		_men = player.getHennaStatMEN();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(228);
		writeC(_int);
		writeC(_str);
		writeC(_con);
		writeC(_men);
		writeC(_dex);
		writeC(_wit);
		writeD(3);
		writeD(_count);
		for(int i = 0; i < _count; ++i)
			if(_hennas[i] != null)
			{
				writeD((int) _hennas[i].getSymbolId());
				writeD((int) _hennas[i].getSymbolId());
			}
	}
}
