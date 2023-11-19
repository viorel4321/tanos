package l2s.gameserver.model.items;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Log;

public class ClanWarehousePool
{
	private static final Logger _log;
	private static ClanWarehousePool _instance;
	private List<ClanWarehouseWork> _works;
	private boolean inWork;

	public static ClanWarehousePool getInstance()
	{
		if(ClanWarehousePool._instance == null)
			ClanWarehousePool._instance = new ClanWarehousePool();
		return ClanWarehousePool._instance;
	}

	public ClanWarehousePool()
	{
		_works = new ArrayList<ClanWarehouseWork>();
		inWork = false;
	}

	public void AddWork(final Player _activeChar, final int[] _items, final int[] _counts)
	{
		final ClanWarehouseWork cww = new ClanWarehouseWork(_activeChar, _items, _counts);
		_works.add(cww);
		if(Config.DEBUG)
			ClanWarehousePool._log.info("ClanWarehousePool: add work, work count " + _works.size());
		RunWorks();
	}

	private void RunWorks()
	{
		if(inWork)
		{
			if(Config.DEBUG)
				ClanWarehousePool._log.info("ClanWarehousePool: work in progress, work count " + _works.size());
			return;
		}
		inWork = true;
		try
		{
			if(_works.size() > 0)
			{
				final ClanWarehouseWork cww = _works.get(0);
				if(!cww.complete)
				{
					if(Config.DEBUG)
						ClanWarehousePool._log.info("ClanWarehousePool: run work, work count " + _works.size());
					cww.RunWork();
				}
				_works.remove(0);
			}
		}
		catch(Exception e)
		{
			ClanWarehousePool._log.error("Error ClanWarehousePool: " + e);
		}
		inWork = false;
		if(!_works.isEmpty())
			RunWorks();
	}

	static
	{
		_log = LoggerFactory.getLogger(ClanWarehousePool.class);
	}

	private class ClanWarehouseWork
	{
		private Player activeChar;
		private int[] items;
		private int[] counts;
		public boolean complete;

		public ClanWarehouseWork(final Player _activeChar, final int[] _items, final int[] _counts)
		{
			activeChar = _activeChar;
			items = _items;
			counts = _counts;
			complete = false;
		}

		public synchronized void RunWork()
		{
			if(activeChar.getClan() == null)
				return;
			Warehouse warehouse2 = null;
			warehouse2 = activeChar.getClan().getWarehouse();
			for(int i = 0; i < items.length; ++i)
			{
				if(counts[i] < 0)
				{
					ClanWarehousePool._log.warn("Warning char:" + activeChar.toString() + " get Item from ClanWarhouse count < 0: objid=" + items[i]);
					return;
				}
				final ItemInstance TransferItem;
				if((TransferItem = warehouse2.takeItemByObj(items[i], counts[i])) == null)
					ClanWarehousePool._log.warn("Warning char:" + activeChar.toString() + " get null Item from ClanWarhouse: objid=" + items[i]);
				else
				{
					activeChar.getInventory().addItem(TransferItem);
					Log.LogItem(activeChar, "ClanWarehouseWithdraw", TransferItem);
				}
			}
			activeChar.sendChanges();
			complete = true;
		}
	}
}
