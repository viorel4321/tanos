package l2s.gameserver.model.items;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;

public abstract class Warehouse
{
	private static final Logger _log;
	private static final String query = "SELECT * FROM items WHERE owner_id=? AND loc=? ORDER BY name ASC LIMIT 200";
	private static final String query_class = "SELECT * FROM items WHERE owner_id=? AND loc=? AND class=? ORDER BY name ASC LIMIT 200";

	public abstract int getOwnerId();

	public abstract ItemInstance.ItemLocation getLocationType();

	public ItemInstance[] listItems(final ItemClass clss)
	{
		final List<ItemInstance> items = new ArrayList<ItemInstance>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(clss == ItemClass.ALL ? "SELECT * FROM items WHERE owner_id=? AND loc=? ORDER BY name ASC LIMIT 200" : "SELECT * FROM items WHERE owner_id=? AND loc=? AND class=? ORDER BY name ASC LIMIT 200");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getLocationType().name());
			if(clss != ItemClass.ALL)
				statement.setString(3, clss.name());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final ItemInstance item;
				if((item = ItemInstance.restoreFromDb(rset, con, false)) != null)
					items.add(item);
			}
		}
		catch(Exception e)
		{
			Warehouse._log.error("could not restore warehouse:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return items.toArray(new ItemInstance[items.size()]);
	}

	public int countItems()
	{
		return mysql.simple_get_int("COUNT(object_id)", "items", "owner_id=" + getOwnerId() + " AND loc=" + getLocationType().name());
	}

	public synchronized void addItem(final int id, final long count)
	{
		final ItemInstance i = ItemTable.getInstance().createItem(id);
		i.setCount(count);
		this.addItem(i);
	}

	public synchronized void addItem(final ItemInstance newItem)
	{
		final ItemInstance item;
		if(newItem.isStackable() && (item = findItemId(newItem.getItemId())) != null)
		{
			item.setCount(item.getCount() + newItem.getCount());
			item.updateDatabase(true);
		}
		else
		{
			newItem.setOwnerId(getOwnerId());
			newItem.setLocation(getLocationType(), 0);
			newItem.updateDatabase(true);
		}
		newItem.deleteMe();
	}

	public synchronized ItemInstance takeItemByObj(final int objectId, final long count)
	{
		final ItemInstance item = ItemInstance.restoreFromDb(objectId, true);
		if(item == null)
			return null;
		if(item.getLocation() != ItemInstance.ItemLocation.CLANWH && item.getLocation() != ItemInstance.ItemLocation.WAREHOUSE && item.getLocation() != ItemInstance.ItemLocation.FREIGHT)
		{
			item.deleteMe();
			Warehouse._log.warn("WARNING get item not in WAREHOUSE via WAREHOUSE: item objid=" + item.getObjectId() + " ownerid=" + item.getOwnerId());
			return null;
		}
		if(item.getCount() <= count)
		{
			item.setLocation(ItemInstance.ItemLocation.VOID, 0);
			item.setWhFlag(true);
			item.updateDatabase(true);
			return item;
		}
		item.setCount(item.getCount() - count);
		item.updateDatabase(true);
		final ItemInstance Newitem = ItemTable.getInstance().createItem(item.getTemplate().getItemId());
		Newitem.setCount(count);
		return Newitem;
	}

	public synchronized void destroyItem(final int itemId, long count)
	{
		final ItemInstance item = findItemId(itemId);
		if(item == null)
			return;
		if(item.getCount() < count)
			count = item.getCount();
		if(item.getCount() == count)
		{
			item.setCount(0L);
			item.removeFromDb();
		}
		else
		{
			item.setCount(item.getCount() - count);
			item.updateDatabase(true);
		}
	}

	public ItemInstance findItemId(final int itemId)
	{
		ItemInstance foundItem = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND loc=? AND item_id=?");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getLocationType().name());
			statement.setInt(3, itemId);
			rset = statement.executeQuery();
			if(rset.next())
				foundItem = ItemInstance.restoreFromDb(rset.getInt(1), false);
		}
		catch(Exception e)
		{
			Warehouse._log.error("could not list warehouse: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return foundItem;
	}

	public long countOf(final int itemId)
	{
		final ItemInstance foundItem = findItemId(itemId);
		return foundItem == null ? 0L : foundItem.getCount();
	}

	public long getAdenaCount()
	{
		return countOf(57);
	}

	static
	{
		_log = LoggerFactory.getLogger(Warehouse.class);
	}

	public enum WarehouseType
	{
		PRIVATE(1),
		CLAN(2),
		CASTLE(3),
		FREIGHT(4);

		private final int _type;

		private WarehouseType(final int type)
		{
			_type = type;
		}

		public int getPacketValue()
		{
			return _type;
		}
	}
}
