package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;

public class RequestHennaUnequip extends L2GameClientPacket
{
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		for(int i = 1; i <= 3; ++i)
		{
			final HennaInstance henna = player.getHenna(i);
			if(henna != null)
				if(henna.getSymbolId() == _symbolId)
				{
					final int price = henna.getPrice() / 5;
					if(player.getAdena() < price)
					{
						player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						break;
					}
					if(price > 0)
						player.reduceAdena(price, false);
					player.removeHenna(i);
					player.sendPacket(Msg.THE_SYMBOL_HAS_BEEN_DELETED);
					break;
				}
		}
	}
}
