package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.model.Player;

public final class RequestPetition extends L2GameClientPacket
{
	private String _content;
	private int _type;

	@Override
	protected void readImpl()
	{
		_content = readS(4096);
		_type = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		PetitionManager.getInstance().handle(player, _type, _content);
	}
}
