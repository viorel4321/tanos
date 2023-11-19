package l2s.gameserver.taskmanager;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.items.ItemInstance;

public class ItemsAutoDestroy
{
	private static ItemsAutoDestroy _instance;
	private ConcurrentLinkedQueue<ItemInstance> _items;
	private ConcurrentLinkedQueue<ItemInstance> _herbs;

	private ItemsAutoDestroy()
	{
		_items = null;
		_herbs = null;
		_herbs = new ConcurrentLinkedQueue<ItemInstance>();
		if(Config.AUTODESTROY_ITEM_AFTER > 0)
		{
			_items = new ConcurrentLinkedQueue<ItemInstance>();
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckItemsForDestroy(), 60000L, 60000L);
		}
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckHerbsForDestroy(), 1000L, 1000L);
	}

	public static ItemsAutoDestroy getInstance()
	{
		if(ItemsAutoDestroy._instance == null)
			ItemsAutoDestroy._instance = new ItemsAutoDestroy();
		return ItemsAutoDestroy._instance;
	}

	public void addItem(final ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}

	public void addHerb(final ItemInstance herb)
	{
		herb.setDropTime(System.currentTimeMillis());
		_herbs.add(herb);
	}

	public class CheckItemsForDestroy implements Runnable
	{
		@Override
		public void run()
		{
			final long _sleep = Config.AUTODESTROY_ITEM_AFTER * 1000L;
			try
			{
				final long curtime = System.currentTimeMillis();
				for(final ItemInstance item : _items)
					if(item == null || item.getDropTime() == 0L || item.getLocation() != ItemInstance.ItemLocation.VOID)
						_items.remove(item);
					else
					{
						if(item.getDropTime() + _sleep >= curtime)
							continue;
						item.deleteMe();
						_items.remove(item);
					}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public class CheckHerbsForDestroy implements Runnable
	{
		static final long _sleep = 15000L;

		@Override
		public void run()
		{
			try
			{
				final long curtime = System.currentTimeMillis();
				for(final ItemInstance item : _herbs)
					if(item == null || item.getDropTime() == 0L || item.getLocation() != ItemInstance.ItemLocation.VOID)
						_herbs.remove(item);
					else
					{
						if(item.getDropTime() + 15000L >= curtime)
							continue;
						item.deleteMe();
						_herbs.remove(item);
					}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
