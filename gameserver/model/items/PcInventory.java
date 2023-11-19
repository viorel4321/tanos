package l2s.gameserver.model.items;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.taskmanager.DelayedItemsManager;

public class PcInventory extends Inventory
{
	private final Player _owner;

	public PcInventory(final Player owner)
	{
		_owner = owner;
	}

	@Override
	public void restore()
	{
		super.restore();

		DelayedItemsManager.getInstance().loadDelayed(getOwner(), false);
	}

	@Override
	public Player getOwner()
	{
		return _owner;
	}

	@Override
	protected ItemInstance.ItemLocation getBaseLocation()
	{
		return ItemInstance.ItemLocation.INVENTORY;
	}

	@Override
	protected ItemInstance.ItemLocation getEquipLocation()
	{
		return ItemInstance.ItemLocation.PAPERDOLL;
	}

	public int getAdena()
	{
		final ItemInstance _adena = getItemByItemId(57);
		if(_adena == null)
			return 0;
		return _adena.getIntegerLimitedCount();
	}

	public ItemInstance addAdena(final long amount)
	{
		final ItemInstance _adena = this.addItem(57, amount);
		return _adena;
	}

	public ItemInstance reduceAdena(final long adena)
	{
		return destroyItemByItemId(57, adena, true);
	}

	public static int[][] restoreVisibleInventory(final int objectId)
	{
		final int[][] paperdoll = new int[17][3];
		Connection con = null;
		PreparedStatement statement2 = null;
		ResultSet invdata = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement2 = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
			statement2.setInt(1, objectId);
			invdata = statement2.executeQuery();
			while(invdata.next())
			{
				final int slot = invdata.getInt("loc_data");
				paperdoll[slot][0] = invdata.getInt("object_id");
				paperdoll[slot][1] = invdata.getInt("item_id");
				paperdoll[slot][2] = invdata.getInt("enchant_level");
			}
		}
		catch(Exception e)
		{
			Inventory._log.error("Could not restore inventory: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement2, invdata);
		}
		return paperdoll;
	}

	public static int[] getVariationsId(final int object_id)
	{
		if(object_id <= 0)
			return new int[]{ 0, 0 };

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		int variation1Id = 0;
		int variation2Id = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT variation1_id, variation2_id FROM items WHERE object_id=?");
			statement.setInt(1, object_id);
			rs = statement.executeQuery();
			if(rs.next()) {
				variation1Id = rs.getInt(1);
				variation2Id = rs.getInt(1);
			}
		}
		catch(Exception e)
		{
			Inventory._log.error("Could not get attributes for item: " + object_id + " from DB: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
		return new int[]{ variation1Id, variation2Id };
	}

	public boolean validateCapacity(final ItemInstance item)
	{
		ItemInstance it = null;
		final boolean se = item.isStackable() && (it = getItemByItemId(item.getItemId())) != null;
		if(se && it.getCount() + item.getCount() > Integer.MAX_VALUE)
			return false;
		int slots = 0;
		if(!se)
			++slots;
		return this.validateCapacity(slots);
	}

	public boolean validateCapacity(final List<ItemInstance> items)
	{
		int slots = 0;
		for(final ItemInstance item : items)
			if(!item.isStackable() || getItemByItemId(item.getItemId()) == null)
				++slots;
		return this.validateCapacity(slots);
	}

	public boolean validateCapacity(final int slots)
	{
		return getSize() + slots <= _owner.getInventoryLimit();
	}

	public int slotsLeft()
	{
		final Player owner = getOwner();
		if(owner == null)
			return 0;
		final int slots = owner.getInventoryLimit() - getSize();
		return slots > 0 ? slots : 0;
	}

	public boolean validateWeight(final List<ItemInstance> items)
	{
		long weight = 0L;
		for(final ItemInstance item : items)
			weight += item.getTemplate().getWeight() * item.getCount();
		return this.validateWeight(weight);
	}

	public boolean validateWeight(final ItemInstance item)
	{
		final long weight = item.getTemplate().getWeight() * item.getCount();
		return this.validateWeight(weight);
	}

	public boolean validateWeight(final long weight)
	{
		final Player owner = getOwner();
		return owner != null && getTotalWeight() + weight <= owner.getMaxLoad();
	}
}
