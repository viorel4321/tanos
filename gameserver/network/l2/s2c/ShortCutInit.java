package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.ShortCut;

public class ShortCutInit extends L2GameServerPacket
{
	private Collection<ShortCut> _shortCuts;

	public ShortCutInit(final Player pl)
	{
		_shortCuts = pl.getAllShortCuts();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(69);
		writeD(_shortCuts.size());
		for(final ShortCut sc : _shortCuts)
		{
			writeD(sc.type);
			writeD(sc.slot + sc.page * 12);
			switch(sc.type)
			{
				case 1:
				{
					writeD(sc.id);
					writeD(1);
					writeD(-1);
					writeD(0);
					writeD(0);
					writeH(0);
					writeH(0);
					continue;
				}
				case 2:
				{
					writeD(sc.id);
					writeD(sc.level);
					writeC(0);
					writeD(1);
					continue;
				}
				default:
				{
					writeD(sc.id);
					writeD(1);
					continue;
				}
			}
		}
	}
}
