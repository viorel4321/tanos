package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;

public class GMHennaInfo extends L2GameServerPacket
{
	private int _count;
	private int _str;
	private int _con;
	private int _dex;
	private int _int;
	private int _wit;
	private int _men;
	private final HennaInstance[] _hennas;

	public GMHennaInfo(final Player cha)
	{
		_hennas = new HennaInstance[3];
		_str = cha.getHennaStatSTR();
		_con = cha.getHennaStatCON();
		_dex = cha.getHennaStatDEX();
		_int = cha.getHennaStatINT();
		_wit = cha.getHennaStatWIT();
		_men = cha.getHennaStatMEN();
		int j = 0;
		for(int i = 0; i < 3; ++i)
		{
			final HennaInstance h = cha.getHenna(i + 1);
			if(h != null)
				_hennas[j++] = h;
		}
		_count = j;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(234);
		writeC(_int);
		writeC(_str);
		writeC(_con);
		writeC(_men);
		writeC(_dex);
		writeC(_wit);
		writeD(3);
		writeD(_count);
		for(int i = 0; i < _count; ++i)
		{
			writeD((int) _hennas[i].getSymbolId());
			writeD((int) _hennas[i].getSymbolId());
		}
	}
}
