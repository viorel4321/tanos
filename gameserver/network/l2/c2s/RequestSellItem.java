package l2s.gameserver.network.l2.c2s;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.CastleChamberlainInstance;
import l2s.gameserver.model.instances.ClanHallManagerInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MercManagerInstance;
import l2s.gameserver.model.instances.MerchantInstance;
import l2s.gameserver.model.instances.NpcFriendInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.Util;

public class RequestSellItem extends L2GameClientPacket
{
	private int _listId;
	private int _count;
	private int[] _items;

	@Override
	public void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 3];
		for(int i = 0; i < _count; ++i)
		{
			_items[i * 3 + 0] = readD();
			_items[i * 3 + 1] = readD();
			_items[i * 3 + 2] = readD();
			if(_items[i * 3 + 1] < 0)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_items == null || _count <= 0)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		final boolean bbs = activeChar.getLastNpcId() == -1;
		final NpcInstance npc = activeChar.getLastNpc();
		if(!bbs && (!NpcInstance.canBypassCheck(activeChar, npc) || !activeChar.checkLastNpc()))
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && !bbs && activeChar.getKarma() > 0 && !activeChar.isGM() && !ArrayUtils.contains(Config.ALT_GAME_KARMA_NPC, npc.getNpcId()))
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALLOW_PVPCB_SHOP_KARMA && activeChar.getKarma() > 0 && bbs && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(bbs)
			activeChar.setLastNpcId(-3);
		else if(!activeChar.isGM())
		{
			final boolean isValidMerchant = npc instanceof ClanHallManagerInstance || npc instanceof MerchantInstance || npc instanceof MercManagerInstance || npc instanceof CastleChamberlainInstance || npc instanceof NpcFriendInstance;
			if(!isValidMerchant)
			{
				activeChar.sendActionFailed();
				return;
			}
		}
		if(!bbs)
			activeChar.turn(npc, 3000);
		for(int i = 0; i < _count; ++i)
		{
			final int objectId = _items[i * 3 + 0];
			final int itemId = _items[i * 3 + 1];
			final int cnt = _items[i * 3 + 2];
			if(cnt >= 1)
			{
				final ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
				if(item == null || !item.canBeTraded(activeChar) || !item.getTemplate().isSellable())
				{
					activeChar.sendPacket(new SystemMessage(1801));
					return;
				}
				if(item.getItemId() != itemId)
					Util.handleIllegalPlayerAction(activeChar, "RequestSellItem[115] Fake packet", 0);
				else if(item.getIntegerLimitedCount() < cnt)
					Util.handleIllegalPlayerAction(activeChar, "RequestSellItem[121] Incorrect item count", 0);
				else
				{
					int price = 0;
					if(Config.DIVIDER_SELL == -1)
						price = 0;
					else if(Config.DIVIDER_SELL > 0)
						price = item.getReferencePrice() * cnt / Config.DIVIDER_SELL;
					if(price > 0)
						activeChar.addAdena(price);

					Log.LogItem(activeChar, "Sell", item);
					if(activeChar.getEnchantScroll() != null && item.getObjectId() == activeChar.getEnchantScroll().getObjectId())
						activeChar.setEnchantScroll(null);
					activeChar.getInventory().destroyItem(item, cnt, true);
				}
			}
		}
		activeChar.updateStats();
		activeChar.sendPacket(new ItemList(activeChar, true));
	}
}
