package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.ManufactureItem;
import l2s.gameserver.model.ManufactureList;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestRecipeShopListSet extends L2GameClientPacket
{
	private int _count;
	ManufactureList createList;

	public RequestRecipeShopListSet()
	{
		createList = new ManufactureList();
	}

	@Override
	public void readImpl()
	{
		_count = readD();
		if(_count * 8 > _buf.remaining() || _count > 32767 || _count < 0)
		{
			_count = 0;
			return;
		}
		for(int x = 0; x < _count; ++x)
			createList.add(new ManufactureItem(readD(), readD()));
		_count = createList.size();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getSittingTask())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.checksForShop(true))
		{
			TradeList.cancelStore(activeChar);
			return;
		}
		if(activeChar.getNoChannel() != 0L)
		{
			activeChar.sendPacket(new SystemMessage(1329));
			TradeList.cancelStore(activeChar);
			return;
		}
		if(_count == 0 || activeChar.getCreateList() == null)
		{
			TradeList.cancelStore(activeChar);
			return;
		}
		if(_count > Config.MAX_PVTCRAFT_SLOTS)
		{
			this.sendPacket(new SystemMessage(1036));
			TradeList.cancelStore(activeChar);
			return;
		}
		createList.setStoreName(activeChar.getCreateList().getStoreName());
		activeChar.setCreateList(createList);
		activeChar.setPrivateStoreType((short) 5);
		activeChar.sitDown(0);
		final int id = activeChar.getObjectId();
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				final Player player = GameObjectsStorage.getPlayer(id);
				if(player == null)
					return;
				player.broadcastPrivateStoreMsg(3);
				player.broadcastUserInfo(true);
			}
		}, 2500L);
	}
}
