package l2s.gameserver.network.l2.s2c;

public class ShowTownMap extends L2GameServerPacket
{
	String _texture;
	int _x;
	int _y;

	public ShowTownMap(final String texture, final int x, final int y)
	{
		_texture = texture;
		_x = x;
		_y = y;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(222);
		writeS((CharSequence) _texture);
		writeD(_x);
		writeD(_y);
	}
}
