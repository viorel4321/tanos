package l2s.gameserver.network.l2.s2c;

import java.util.Map;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;

public class PackageToList extends L2GameServerPacket
{
	private Map<Integer, String> characters;

	public PackageToList(final Player player)
	{
		characters = player.getAccountChars();
		if(characters.size() < 1)
		{
			characters = null;
			player.sendPacket(Msg.THAT_CHARACTER_DOES_NOT_EXIST);
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(characters == null)
			return;
		writeC(194);
		writeD(characters.size());
		for(final Integer char_id : characters.keySet())
		{
			writeD((int) char_id);
			writeS((CharSequence) characters.get(char_id));
		}
	}
}
