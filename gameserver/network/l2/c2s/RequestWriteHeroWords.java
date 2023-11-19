package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;

public class RequestWriteHeroWords extends L2GameClientPacket
{
	private String _heroWords;

	@Override
	public void readImpl()
	{
		_heroWords = readS();
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null || !player.isHero())
			return;
		if(_heroWords == null || _heroWords.length() > 300)
			return;
		Hero.getInstance().setHeroMessage(player.getObjectId(), _heroWords);
	}
}
