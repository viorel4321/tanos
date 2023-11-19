package l2s.gameserver.network.l2.c2s;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.network.l2.s2c.PrivateStoreManageListBuy;

public class RequestPrivateStoreManageBuy extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

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
		if(!activeChar.checksForShop(false))
		{
			activeChar.sendActionFailed();
			return;
		}
		switch(activeChar.getPrivateStoreType())
		{
			case 0:
			{
				if(activeChar.isSitting())
					activeChar.standUp();
				if(activeChar.getTradeList() == null)
					activeChar.setTradeList(new TradeList(0));
				if(activeChar.getBuyList() == null)
					activeChar.setBuyList(new ConcurrentLinkedQueue<TradeItem>());
				activeChar.getTradeList().updateBuyList(activeChar, activeChar.getBuyList());
				activeChar.setPrivateStoreType((short) 0);
				activeChar.sendPacket(new PrivateStoreManageListBuy(activeChar));
				break;
			}
			case 1:
			case 3:
			case 8:
			{
				activeChar.setPrivateStoreType((short) 0);
				activeChar.standUp();
				activeChar.broadcastUserInfo(false);
				activeChar.sendPacket(new PrivateStoreManageListBuy(activeChar));
				break;
			}
		}
	}
}
