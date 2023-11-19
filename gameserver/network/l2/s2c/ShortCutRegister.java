package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.ShortCut;

public class ShortCutRegister extends L2GameServerPacket
{
	private ShortCut sc;

	public ShortCutRegister(final ShortCut _sc)
	{
		sc = _sc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(68);
		writeD(sc.type);
		writeD(sc.slot + sc.page * 12);
		switch(sc.type)
		{
			case 1:
			{
				writeD(sc.id);
				writeD(1);
				writeD(-1);
				break;
			}
			case 2:
			{
				writeD(sc.id);
				writeD(sc.level);
				writeC(0);
				writeD(1);
				break;
			}
			default:
			{
				writeD(sc.id);
				writeD(1);
				break;
			}
		}
	}
}
