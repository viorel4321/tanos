package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;

import l2s.gameserver.templates.player.PlayerTemplate;

public class NewCharacterSuccess extends L2GameServerPacket
{
	private ArrayList<PlayerTemplate> _chars;

	public NewCharacterSuccess()
	{
		_chars = new ArrayList<PlayerTemplate>();
	}

	public void addChar(final PlayerTemplate template)
	{
		_chars.add(template);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(23);
		writeD(_chars.size());
		for(final PlayerTemplate temp : _chars)
		{
			writeD(temp.race.ordinal());
			writeD(temp.classId.getId());
			writeD(70);
			writeD(temp.baseSTR);
			writeD(10);
			writeD(70);
			writeD(temp.baseDEX);
			writeD(10);
			writeD(70);
			writeD(temp.baseCON);
			writeD(10);
			writeD(70);
			writeD(temp.baseINT);
			writeD(10);
			writeD(70);
			writeD(temp.baseWIT);
			writeD(10);
			writeD(70);
			writeD(temp.baseMEN);
			writeD(10);
		}
	}
}
