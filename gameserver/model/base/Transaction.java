package l2s.gameserver.model.base;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.s2c.SendTradeDone;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Log;

public class Transaction
{
	private static Logger _log;
	private int _player1;
	private int _player2;
	private long _timeout;
	private TransactionType _type;
	private ConcurrentLinkedQueue<TradeItem> _exchangeList1;
	private ConcurrentLinkedQueue<TradeItem> _exchangeList2;
	private boolean _confirmed1;
	private boolean _confirmed2;

	public Transaction(final TransactionType type, final Player player1, final Player player2, final long timeout)
	{
		_player1 = player1.getObjectId();
		_player2 = player2.getObjectId();
		_timeout = timeout > 0L ? System.currentTimeMillis() + timeout : -1L;
		_type = type;
		player1.setTransaction(this);
		player2.setTransaction(this);
	}

	public Transaction(final TransactionType type, final Player player1, final Player player2)
	{
		this(type, player1, player2, 0L);
	}

	public void cancel()
	{
		Player player = GameObjectsStorage.getPlayer(_player1);
		if(player != null && player.getTransaction() == this)
		{
			player.setTransaction(null);
			if(_type == TransactionType.TRADE)
				player.sendPacket(SendTradeDone.Fail);
		}
		player = GameObjectsStorage.getPlayer(_player2);
		if(player != null && player.getTransaction() == this)
		{
			player.setTransaction(null);
			if(_type == TransactionType.TRADE)
				player.sendPacket(SendTradeDone.Fail);
		}
	}

	public boolean isParticipant(final Player player)
	{
		return player.getObjectId() == _player1 || player.getObjectId() == _player2;
	}

	public boolean isInProgress()
	{
		if(_timeout < 0L)
			return true;
		if(_timeout > System.currentTimeMillis())
			return true;
		cancel();
		return false;
	}

	public boolean isTypeOf(final TransactionType type)
	{
		return _type == type;
	}

	public void confirm(final Player player)
	{
		if(player.getObjectId() == _player1)
			_confirmed1 = true;
		else if(player.getObjectId() == _player2)
			_confirmed2 = true;
	}

	public boolean isConfirmed(final Player player)
	{
		if(player.getObjectId() == _player1)
			return _confirmed1;
		return player.getObjectId() == _player2 && _confirmed2;
	}

	public boolean isValid()
	{
		Player player = GameObjectsStorage.getPlayer(_player1);
		if(player == null || player.getTransaction() != this)
			return false;
		player = GameObjectsStorage.getPlayer(_player2);
		return player != null && player.getTransaction() == this;
	}

	public Player getOtherPlayer(final Player player)
	{
		if(player.getObjectId() == _player1)
			return GameObjectsStorage.getPlayer(_player2);
		if(player.getObjectId() == _player2)
			return GameObjectsStorage.getPlayer(_player1);
		return null;
	}

	public ConcurrentLinkedQueue<TradeItem> getExchangeList(final Player player)
	{
		if(_exchangeList1 == null)
			_exchangeList1 = new ConcurrentLinkedQueue<TradeItem>();
		if(_exchangeList2 == null)
			_exchangeList2 = new ConcurrentLinkedQueue<TradeItem>();
		if(player.getObjectId() == _player1)
			return _exchangeList1;
		if(player.getObjectId() == _player2)
			return _exchangeList2;
		return null;
	}

	public void tradeItems()
	{
		final Player player1 = GameObjectsStorage.getPlayer(_player1);
		final Player player2 = GameObjectsStorage.getPlayer(_player2);
		if(player1 == null || player2 == null)
			return;
		if(Config.TRADE_LOG_MOD)
			Functions.callScripts("services.TradeLog", "log", new Object[] { player1 });
		this.tradeItems(player1, player2);
		this.tradeItems(player2, player1);
	}

	private void tradeItems(final Player player, final Player receiver)
	{
		final ConcurrentLinkedQueue<TradeItem> exchangeList = getExchangeList(player);
		final Inventory playersInv = player.getInventory();
		final Inventory receiverInv = receiver.getInventory();
		for(final TradeItem temp : exchangeList)
			if(temp.getObjectId() == 0)
				Transaction._log.warn("Warning: null object id item, player " + player);
			else if(temp.getCount() <= 0)
				Transaction._log.warn("Warning: null item count, player " + player);
			else
			{
				if(player.getEnchantScroll() != null && temp.getObjectId() == player.getEnchantScroll().getObjectId())
					player.setEnchantScroll(null);
				final ItemInstance TransferItem = playersInv.dropItem(temp.getObjectId(), temp.getCount());
				if(TransferItem == null)
					Transaction._log.warn("Warning: null trade item, player " + player);
				else
				{
					if(Config.DON_LOG && TransferItem.getItemId() == Config.DON_ITEM_LOG && !player.isGM() && !receiver.isGM())
						Log.addLog(player.toString() + " to " + receiver.toString() + ": " + TransferItem.getCount() + " | loc: " + player.getX() + " " + player.getY() + " " + player.getZ(), "don_trade");
					final ItemInstance receiverItem = receiverInv.addItem(TransferItem);
					Log.LogItem(player, "TradeSell", TransferItem);
					Log.LogItem(receiver, "TradeBuy", receiverItem);
				}
			}
		player.sendChanges();
		receiver.sendChanges();
	}

	static
	{
		Transaction._log = LoggerFactory.getLogger(Transaction.class);
	}

	public enum TransactionType
	{
		NONE,
		PARTY,
		PARTY_ROOM,
		CLAN,
		ALLY,
		TRADE,
		TRADE_REQUEST,
		FRIEND,
		CHANNEL,
		DUEL;
	}
}
