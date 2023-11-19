package l2s.gameserver.handler.items;

import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemHandler
{
	private static final Logger _log = LoggerFactory.getLogger(ItemHandler.class);

	private static final ItemHandler _instance = new ItemHandler();

	public static ItemHandler getInstance()
	{
		return _instance;
	}

	private ItemHandler()
	{
		//
	}

	public void registerItemHandler(IItemHandler handler)
	{
		int[] ids = handler.getItemIds();
		for(int itemId : ids)
		{
			ItemTemplate template = ItemTable.getInstance().getTemplate(itemId);
			if(template == null)
				_log.warn("Item not found: " + itemId + " handler: " + handler.getClass().getSimpleName());
			else if(template.getHandler() != IItemHandler.NULL)
			{
				//_log.warn("Duplicate handler for item: " + itemId + "(" + template.getHandler().getClass().getSimpleName() + "," + handler.getClass().getSimpleName() + ")");
			}
			else
				template.setHandler(handler);
		}
	}
}
