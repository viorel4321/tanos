package l2s.gameserver.model.items;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.DropItem;
import l2s.gameserver.network.l2.s2c.InventoryUpdate;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SpawnItem;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.skills.funcs.Func;
import l2s.gameserver.skills.funcs.FuncTemplate;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.taskmanager.ItemsAutoDestroy;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Stat;

public final class ItemInstance extends GameObject
{
	private static final long serialVersionUID = 3162753878915133228L;

	private static final Logger _log = LoggerFactory.getLogger(ItemInstance.class);;

	public static final int FLAG_NO_DROP = 1;
	public static final int FLAG_NO_TRADE = 2;
	public static final int FLAG_NO_TRANSFER = 4;
	public static final int FLAG_NO_CRYSTALLIZE = 8;
	public static final int FLAG_NO_ENCHANT = 16;
	public static final int FLAG_NO_DESTROY = 32;
	public static final int FLAG_NO_UNEQUIP = 64;
	public static final int FLAG_PET_EQUIPPED = 128;

	public static final byte CHARGED_NONE = 0;
	public static final byte CHARGED_SOULSHOT = 1;
	public static final byte CHARGED_SPIRITSHOT = 1;
	public static final byte CHARGED_BLESSED_SPIRITSHOT = 2;

	public static final byte UNCHANGED = 0;
	public static final byte ADDED = 1;
	public static final byte REMOVED = 3;
	public static final byte MODIFIED = 2;

	private int ownerObjectId;
	private int itemDropOwnerObjectId;
	ScheduledFuture<?> _itemLifeTimeTask;
	private int _lifeTimeRemaining;
	private long _count;
	private int _itemId;
	private ItemTemplate _itemTemplate;
	private ItemLocation _loc;
	private int _loc_data;
	private int _enchantLevel;
	private int _price_sell;
	private int _count_sell;
	private boolean _wear;
	private int _type1;
	private int _type2;
	private long _dropTime;
	private long _dropTimeOwner;

	private byte _chargedSoulshot;
	private byte _chargedSpiritshot;
	private boolean _chargedFishtshot;

	private byte _lastChange;
	private boolean _existsInDb;
	private boolean _storedInDb;
	private int _customFlags;
	private boolean _engraved;
	private Future<?> _lazyUpdateInDb;
	private int _bodypart;
	private boolean _whflag;
	protected FuncTemplate[] _funcTemplates;

	private int _variationStoneId = 0;
	private int _variation1Id = 0;
	private int _variation2Id = 0;

	public ItemInstance(int objectId, ItemTemplate item, boolean putInStorage)
	{
		super(objectId);

		_chargedSoulshot = 0;
		_chargedSpiritshot = 0;
		_chargedFishtshot = false;
		_lastChange = 2;
		_customFlags = 0;
		_engraved = false;
		_whflag = false;

		if(item == null)
		{
			ItemInstance._log.warn("Not found template for item id: " + _itemId);
			throw new IllegalArgumentException();
		}

		_itemId = item.getItemId();
		_itemTemplate = item;
		_count = 1L;
		_loc = ItemLocation.VOID;
		_dropTime = 0L;

		setItemDropOwner(null, _dropTimeOwner = 0L);

		_lifeTimeRemaining = _itemTemplate.isTemporal() ? (int) (System.currentTimeMillis() / 1000L) + _itemTemplate.getDurability() * 60 : _itemTemplate.getDurability();
		_bodypart = _itemTemplate.getBodyPart();

		if(putInStorage && objectId > 0)
			GameObjectsStorage.put(this);
	}

	public ItemInstance(int objectId, final int itemId)
	{
		this(objectId, ItemTable.getInstance().getTemplate(itemId), true);
	}

	public int getBodyPart()
	{
		return _bodypart;
	}

	public void setBodyPart(final int bodypart)
	{
		_bodypart = bodypart;
	}

	public void setOwnerId(final int ownerId)
	{
		if(getOwnerId() != ownerId)
			_storedInDb = false;
		final Player owner = GameObjectsStorage.getPlayer(ownerId);
		ownerObjectId = ownerId ;
		startTemporalTask(owner);
	}

	public int getOwnerId()
	{
		return ownerObjectId;
	}

	public void setLocation(final ItemLocation loc)
	{
		this.setLocation(loc, 0);
	}

	public void setLocation(final ItemLocation loc, final int loc_data)
	{
		if(loc == _loc && loc_data == _loc_data)
			return;
		_loc = loc;
		_loc_data = loc_data;
		_storedInDb = false;
	}

	public ItemLocation getLocation()
	{
		return _loc;
	}

	public int getIntegerLimitedCount()
	{
		return (int) Math.min(_count, Integer.MAX_VALUE);
	}

	public long getCount()
	{
		return _count;
	}

	public void setCount(long count)
	{
		if(count < 0L)
			count = 0L;
		if(!isStackable() && count > 1L)
		{
			_count = 1L;
			return;
		}
		if(_count == count)
			return;
		_count = count;
		_storedInDb = false;
	}

	public boolean isEquipable()
	{
		return _itemTemplate.isEquipable();
	}

	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL;
	}

	public int getEquipSlot()
	{
		return _loc_data;
	}

	public ItemTemplate getTemplate()
	{
		return _itemTemplate;
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public void setCustomType1(final int newtype)
	{
		_type1 = newtype;
	}

	public void setCustomType2(final int newtype)
	{
		_type2 = newtype;
	}

	public void setDropTime(final long time)
	{
		_dropTime = time;
	}

	public long getDropTime()
	{
		return _dropTime;
	}

	public long getDropTimeOwner()
	{
		return _dropTimeOwner;
	}

	public void setItemDropOwner(final Player owner, final long time)
	{
		itemDropOwnerObjectId = owner != null ? owner.getObjectId() : 0;
		_dropTimeOwner = owner != null ? System.currentTimeMillis() + time : 0L;
	}

	public Player getItemDropOwner()
	{
		return GameObjectsStorage.getPlayer(itemDropOwnerObjectId);
	}

	public boolean isWear()
	{
		return _wear;
	}

	public void setWear(final boolean newwear)
	{
		_wear = newwear;
	}

	public Enum<?> getItemType()
	{
		return _itemTemplate.getItemType();
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getReferencePrice()
	{
		return _itemTemplate.getReferencePrice();
	}

	public int getPriceToSell()
	{
		return _price_sell;
	}

	public void setPriceToSell(final int price)
	{
		_price_sell = price;
	}

	public void setCountToSell(final int count)
	{
		_count_sell = count;
	}

	public int getCountToSell()
	{
		return _count_sell;
	}

	public int getLastChange()
	{
		return _lastChange;
	}

	public void setLastChange(final byte lastChange)
	{
		_lastChange = lastChange;
	}

	public boolean isStackable()
	{
		return _itemTemplate.isStackable();
	}

	public boolean isMercTicket()
	{
		return _itemTemplate.isMercTicket();
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;
		if(player.isCursedWeaponEquipped() && isCursed())
		{
			player.sendActionFailed();
			return;
		}
		if(isMercTicket())
		{
			final Castle castle = player.getCastle();
			if(castle != null && (player.getClanPrivileges() & 0x200000) == 0x200000)
			{
				SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
				if(!castle.getSpawnMerchantTickets().contains(this))
					player.sendPacket(new SystemMessage(658));
				else if(siegeEvent != null && siegeEvent.isInProgress())
					player.sendPacket(new SystemMessage(1194));
				else if(player.isInParty())
					player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.ItemInstance.NoMercInParty"));
				else
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
			}
			else
				player.sendPacket(new SystemMessage(654));
			player.setTarget(this);
			player.sendActionFailed();
		}
		else
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
	}

	public boolean isAugmented()
	{
		return getVariation1Id() != 0 || getVariation2Id() != 0;
	}

	public int getVariation1Id()
	{
		return _variation1Id;
	}

	public void setVariation1Id(int val)
	{
		_variation1Id = val;
	}

	public int getVariation2Id()
	{
		return _variation2Id;
	}

	public void setVariation2Id(int val)
	{
		_variation2Id = val;
	}

	public void setVariationStoneId(int id)
	{
		_variationStoneId = id;
	}

	public int getVariationStoneId()
	{
		return _variationStoneId;
	}

	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	public int getEnchantLevel2()
	{
		if(isEquipped())
			return Math.min(isWeapon() ? Config.OLY_ENCHANT_LIMIT_WEAPON : Config.OLY_ENCHANT_LIMIT_ARMOR, _enchantLevel);
		return _enchantLevel;
	}

	public void setEnchantLevel(final int enchantLevel)
	{
		if(_enchantLevel == enchantLevel)
			return;
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return false;
	}

	public byte getChargedSoulshot()
	{
		return _chargedSoulshot;
	}

	public byte getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}

	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}

	public void setChargedSoulshot(final byte type)
	{
		_chargedSoulshot = type;
	}

	public void setChargedSpiritshot(final byte type)
	{
		_chargedSpiritshot = type;
	}

	public void setChargedFishshot(final boolean type)
	{
		_chargedFishtshot = type;
	}

	public void attachFunction(final FuncTemplate f)
	{
		if(_funcTemplates == null)
			_funcTemplates = new FuncTemplate[] { f };
		else
		{
			final int len = _funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	public Func[] getStatFuncs()
	{
		final List<Func> funcs = new ArrayList<Func>();
		if(_itemTemplate.getAttachedFuncs() != null)
			for(final FuncTemplate t : _itemTemplate.getAttachedFuncs())
			{
				final Func f = t.getFunc(this);
				if(f != null)
					funcs.add(f);
			}
		if(_funcTemplates != null)
			for(final FuncTemplate t : _funcTemplates)
			{
				final Func f = t.getFunc(this);
				if(f != null)
					funcs.add(f);
			}
		if(funcs.size() == 0)
			return new Func[0];
		return (Func[]) funcs.toArray((Object[]) new Func[funcs.size()]);
	}

	public boolean canBeAugmented(Player player)
	{
		if(!getTemplate().isAugmentable())
			return false;

		if(isAugmented())
			return false;

		if(isHeroItem())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		return true;
	}
	public void updateDatabase()
	{
		this.updateDatabase(false);
	}

	public synchronized void updateDatabase(final boolean commit)
	{
		if(_existsInDb)
		{
			if(_loc == ItemLocation.VOID || _count == 0L || getOwnerId() == 0)
				this.removeFromDb();
			else if(Config.LAZY_ITEM_UPDATE && (isStackable() || Config.LAZY_ITEM_UPDATE_ALL))
			{
				if(commit)
				{
					if(stopLazyUpdateTask(true))
					{
						insertIntoDb();
						return;
					}
					updateInDb();
					Stat.increaseUpdateItemCount();
				}
				else
				{
					final Future<?> lazyUpdateInDb = _lazyUpdateInDb;
					if(lazyUpdateInDb == null || lazyUpdateInDb.isDone())
					{
						_lazyUpdateInDb = ThreadPoolManager.getInstance().schedule(new LazyUpdateInDb(this), isStackable() ? (long) Config.LAZY_ITEM_UPDATE_TIME : (long) Config.LAZY_ITEM_UPDATE_ALL_TIME);
						Stat.increaseLazyUpdateItem();
					}
				}
			}
			else
			{
				updateInDb();
				Stat.increaseUpdateItemCount();
			}
		}
		else
		{
			if(_count == 0L || _loc == ItemLocation.VOID || getOwnerId() == 0)
				return;
			insertIntoDb();
		}
	}

	public boolean stopLazyUpdateTask(boolean interrupt)
	{
		boolean ret = false;
		if(_lazyUpdateInDb != null)
		{
			ret = _lazyUpdateInDb.cancel(interrupt);
			_lazyUpdateInDb = null;
		}
		return ret;
	}

	public static synchronized ItemInstance restoreFromDb(int objectId, boolean putInStorage)
	{
		ItemInstance inst = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet item_rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM items WHERE object_id=? LIMIT 1");
			statement.setInt(1, objectId);
			item_rset = statement.executeQuery();
			if(item_rset.next())
				inst = restoreFromDb(item_rset, con, putInStorage);
			else
				ItemInstance._log.error("Item object_id=" + objectId + " not found");
		}
		catch(Exception e)
		{
			ItemInstance._log.error("Could not restore item " + objectId + " from DB: " + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, item_rset);
		}
		return inst;
	}

	public static synchronized ItemInstance restoreFromDb(ResultSet item_rset, Connection con, boolean putInStorage)
	{
		if(item_rset == null)
			return null;

		int objectId = 0;
		try
		{
			objectId = item_rset.getInt("object_id");

			ItemTemplate item = ItemTable.getInstance().getTemplate(item_rset.getInt("item_id"));
			if(item == null)
			{
				ItemInstance._log.error("Item item_id=" + item_rset.getInt("item_id") + " not known, object_id=" + objectId);
				return null;
			}

			if(item.isTemporal() && item_rset.getInt("shadow_life_time") <= System.currentTimeMillis() / 1000L && item_rset.getInt("shadow_life_time") > 1262304000)
			{
				removeFromDb(objectId);
				return null;
			}

			ItemInstance inst = new ItemInstance(objectId, item, putInStorage);
			inst._existsInDb = true;
			inst._storedInDb = true;
			inst._lifeTimeRemaining = item_rset.getInt("shadow_life_time");
			inst.setOwnerId(item_rset.getInt("owner_id"));
			inst._count = item_rset.getLong("count");
			inst._enchantLevel = item_rset.getInt("enchant_level");
			inst._type1 = item_rset.getInt("custom_type1");
			inst._type2 = item_rset.getInt("custom_type2");
			inst._loc = ItemLocation.valueOf(item_rset.getString("loc"));
			inst._loc_data = item_rset.getInt("loc_data");
			inst._customFlags = item_rset.getInt("flags");
			inst.setVariationStoneId(item_rset.getInt("variation_stone_id"));
			inst.setVariation1Id(item_rset.getInt("variation1_id"));
			inst.setVariation2Id(item_rset.getInt("variation2_id"));

			if(Config.ENGRAVE_SYSTEM)
				inst._engraved = item_rset.getInt("engraved") == 1;

			return inst;
		}
		catch(Exception e)
		{
			ItemInstance._log.error("Could not restore(2) item " + objectId + " from DB: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public synchronized void updateInDb()
	{
		if(isWear())
			return;

		if(_storedInDb)
			return;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,shadow_life_time=?,item_id=?,flags=?,variation_stone_id=?,variation1_id=?,variation2_id=? WHERE object_id = ? LIMIT 1");
			statement.setInt(1, getOwnerId());
			statement.setLong(2, _count);
			statement.setString(3, _loc.name());
			statement.setInt(4, _loc_data);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, _lifeTimeRemaining);
			statement.setInt(7, getItemId());
			statement.setInt(8, _customFlags);
			statement.setInt(9, getVariationStoneId());
			statement.setInt(10, getVariation1Id());
			statement.setInt(11, getVariation2Id());
			statement.setInt(12, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}
		catch(Exception e)
		{
			ItemInstance._log.error("Could not update item " + getObjectId() + " itemID " + _itemId + " count " + getCount() + " owner " + getOwnerId() + " in DB:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private synchronized void insertIntoDb()
	{
		if(isWear())
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,shadow_life_time,name,flags,variation_stone_id,variation1_id,variation2_id" + (Config.ENGRAVE_SYSTEM ? ",engraved" : "") + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" + (Config.ENGRAVE_SYSTEM ? ",?" : "") + ")");
			statement.setInt(1, getOwnerId());
			statement.setInt(2, _itemId);
			statement.setLong(3, _count);
			statement.setString(4, _loc.name());
			statement.setInt(5, _loc_data);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, getObjectId());
			statement.setInt(8, _type1);
			statement.setInt(9, _type2);
			statement.setInt(10, _lifeTimeRemaining);
			statement.setString(11, getName());
			statement.setInt(12, _customFlags);
			statement.setInt(13, getVariationStoneId());
			statement.setInt(14, getVariation1Id());
			statement.setInt(15, getVariation2Id());
			if(Config.ENGRAVE_SYSTEM)
				statement.setInt(16, _engraved ? 1 : 0);
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			Stat.increaseInsertItemCount();
		}
		catch(Exception e)
		{
			ItemInstance._log.error("Could not insert item " + getObjectId() + "; itemID=" + _itemId + "; count=" + getCount() + "; owner=" + getOwnerId(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public synchronized void removeFromDb()
	{
		if(isWear() || !_existsInDb)
			return;

		stopLazyUpdateTask(true);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM items WHERE object_id = ? LIMIT 1");
			statement.setInt(1, _objectId);
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
			Stat.increaseDeleteItemCount();
		}
		catch(Exception e)
		{
			ItemInstance._log.error("Could not delete item " + _objectId + " in DB:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static synchronized void removeFromDb(final int _objectIds)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM items WHERE object_id = ? LIMIT 1");
			statement.setInt(1, _objectIds);
			statement.executeUpdate();
			Stat.increaseDeleteItemCount();
		}
		catch(Exception e)
		{
			ItemInstance._log.error("Could not delete item " + _objectIds + " in DB:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isBlessedEnchantScroll()
	{
		switch(_itemId)
		{
			case 6569:
			case 6570:
			case 6571:
			case 6572:
			case 6573:
			case 6574:
			case 6575:
			case 6576:
			case 6577:
			case 6578:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean isCrystallEnchantScroll()
	{
		switch(_itemId)
		{
			case 731:
			case 732:
			case 949:
			case 950:
			case 953:
			case 954:
			case 957:
			case 958:
			case 961:
			case 962:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public short getEnchantCrystalId(final ItemInstance scroll)
	{
		final int scrollItemId = scroll.getItemId();
		short crystalId = 0;
		switch(_itemTemplate.getItemGrade().ordinal())
		{
			case 4:
			{
				crystalId = 1461;
				break;
			}
			case 3:
			{
				crystalId = 1460;
				break;
			}
			case 2:
			{
				crystalId = 1459;
				break;
			}
			case 1:
			{
				crystalId = 1458;
				break;
			}
			case 5:
			{
				crystalId = 1462;
				break;
			}
		}
		for(final short scrollId : getEnchantScrollId())
			if(scrollItemId == scrollId)
				return crystalId;
		return 0;
	}

	public short[] getEnchantScrollId()
	{
		if(_itemTemplate.getType2() == 0)
			switch(_itemTemplate.getItemGrade().ordinal())
			{
				case 4:
				{
					return new short[] { 729, 6569, 731 };
				}
				case 3:
				{
					return new short[] { 947, 6571, 949 };
				}
				case 2:
				{
					return new short[] { 951, 6573, 953 };
				}
				case 1:
				{
					return new short[] { 955, 6575, 957 };
				}
				case 5:
				{
					return new short[] { 959, 6577, 961 };
				}
			}
		else if(_itemTemplate.getType2() == 1 || _itemTemplate.getType2() == 2)
			switch(_itemTemplate.getItemGrade().ordinal())
			{
				case 4:
				{
					return new short[] { 730, 6570, 732 };
				}
				case 3:
				{
					return new short[] { 948, 6572, 950 };
				}
				case 2:
				{
					return new short[] { 952, 6574, 954 };
				}
				case 1:
				{
					return new short[] { 956, 6576, 958 };
				}
				case 5:
				{
					return new short[] { 960, 6578, 962 };
				}
			}
		return new short[] { 0, 0, 0 };
	}

	public boolean isHeroItem()
	{
		return _itemTemplate.isHeroItem();
	}

	public boolean isHeroWeapon()
	{
		return _itemTemplate.isHeroWeapon();
	}

	public boolean isClanApellaItem()
	{
		final int myid = _itemId;
		return myid >= 7860 && myid <= 7879;
	}

	public boolean isStriderItem()
	{
		final int myid = _itemId;
		return myid >= 4422 && myid <= 4424;
	}

	public boolean canBeDestroyed(final Player player)
	{
		return (_customFlags & 0x20) != 0x20 && !isHeroItem() && (!isStriderItem() || !player.isMounted()) && (player.getServitor() == null || getObjectId() != player.getServitor().getControlItemId()) && !isCursed() && !isWear() && isDestroyable();
	}

	public boolean canBeDropped(final Player player)
	{
		return (_customFlags & 0x1) != 0x1 && !isHeroItem() && !isShadowItem() && !isEngraved() && !isTemporalItem() && !isAugmented() && _itemTemplate.getType2() != 3 && (!isStriderItem() || !player.isMounted()) && (player.getServitor() == null || getObjectId() != player.getServitor().getControlItemId()) && !isCursed() && !isWear() && getTemplate().getType2() != 3 && player.getEnchantScroll() != this && _itemTemplate.isDropable();
	}

	public boolean canBeTraded(final Player owner)
	{
		return (_customFlags & 0x2) != 0x2 && !isHeroItem() && !isShadowItem() && !isEngraved() && !isTemporalItem() && (!PetDataTable.isPetControlItem(this) || !owner.isMounted()) && (owner.getServitor() == null || getObjectId() != owner.getServitor().getControlItemId()) && !isAugmented() && !isCursed() && !isEquipped() && !isWear() && getTemplate().getType2() != 3 && owner.getEnchantScroll() != this && _itemTemplate.isTradeable();
	}

	public boolean canBeStored(final Player player, final boolean privatewh)
	{
		return (_customFlags & 0x4) != 0x4 && !isHeroItem() && (privatewh || !isShadowItem() && !isTemporalItem()) && (!PetDataTable.isPetControlItem(this) || !player.isMounted()) && (player.getServitor() == null || getObjectId() != player.getServitor().getControlItemId()) && (privatewh || !isAugmented()) && !isCursed() && !isEquipped() && !isWear() && getTemplate().getType2() != 3 && player.getEnchantScroll() != this && (privatewh || _itemTemplate.isTradeable());
	}

	public boolean canBeCrystallized(final Player player, final boolean msg)
	{
		if((_customFlags & 0x8) == 0x8)
			return false;
		if(isHeroItem())
			return false;
		if(isShadowItem())
			return false;
		if(isTemporalItem())
			return false;
		final int level = player.getSkillLevel(248);
		if(level < 1 || _itemTemplate.getItemGrade().ordinal() > level)
		{
			if(msg)
			{
				player.sendPacket(new SystemMessage(562));
				player.sendActionFailed();
			}
			return false;
		}
		return (!PetDataTable.isPetControlItem(this) || !player.isMounted()) && (player.getServitor() == null || getObjectId() != player.getServitor().getControlItemId()) && !isCursed() && !isEquipped() && !isWear() && getTemplate().getType2() != 3 && _itemTemplate.isCrystallizable();
	}

	public boolean canBeEnchanted()
	{
		return (_customFlags & 0x10) != 0x10 && !isWear() && _itemTemplate.canBeEnchanted();
	}

	public boolean isRaidAccessory()
	{
		return _itemTemplate.isRaidAccessory();
	}

	public boolean isArrow()
	{
		return _itemTemplate.isArrow();
	}

	public boolean isEnchantScroll()
	{
		return _itemTemplate.isEnchantScroll();
	}

	public boolean isEquipment()
	{
		return _itemTemplate.isEquipment();
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(getObjectId());
		sb.append(']');
		sb.append('(');
		sb.append(getItemId());
		sb.append(')');
		sb.append(' ');
		if(getEnchantLevel() > 0)
		{
			sb.append('+');
			sb.append(getEnchantLevel());
			sb.append(' ');
		}
		sb.append(getName());
		if(!_itemTemplate.getAdditionalName().isEmpty())
		{
			sb.append(' ');
			sb.append('<').append(_itemTemplate.getAdditionalName()).append('>');
		}
		sb.append(' ');
		sb.append(':');
		sb.append(getCount());
		sb.append(':');
		return sb.toString();
	}

	public boolean isNightLure()
	{
		return _itemId >= 8505 && _itemId <= 8513 || _itemId == 8485;
	}

	public void shadowNotify(final boolean equipped)
	{
		if(!isShadowItem())
			return;
		if(!equipped)
		{
			if(_itemLifeTimeTask != null)
				_itemLifeTimeTask.cancel(false);
			_itemLifeTimeTask = null;
			return;
		}
		if(_itemLifeTimeTask != null && !_itemLifeTimeTask.isDone())
			return;
		final Player owner = getOwner();
		if(owner == null)
			return;
		setLifeTimeRemaining(owner, getLifeTimeRemaining() - 1);
		if(!checkDestruction(owner))
			_itemLifeTimeTask = ThreadPoolManager.getInstance().schedule(new LifeTimeTask(), 60000L);
	}

	private void startTemporalTask(final Player owner)
	{
		if(!isTemporalItem() || owner == null)
			return;
		if(_itemLifeTimeTask != null && !_itemLifeTimeTask.isDone())
			return;
		if(!checkDestruction(owner))
			_itemLifeTimeTask = ThreadPoolManager.getInstance().schedule(new LifeTimeTask(), 60000L);
	}

	public boolean isShadowItem()
	{
		return _itemTemplate.isShadowItem();
	}

	public boolean isTemporalItem()
	{
		return _itemTemplate.isTemporal();
	}

	public boolean isAltSeed()
	{
		return _itemTemplate.isAltSeed();
	}

	public boolean isCursed()
	{
		return _itemTemplate.isCursed();
	}

	public Player getOwner()
	{
		return GameObjectsStorage.getPlayer(ownerObjectId);
	}

	private boolean checkDestruction(final Player owner)
	{
		if(!isShadowItem() && !isTemporalItem())
			return true;
		int left = getLifeTimeRemaining();
		if(isTemporalItem())
			left /= 60;
		if(left == 10 || left == 5 || left == 1 || left <= 0)
		{
			if(isShadowItem())
			{
				SystemMessage sm;
				if(left == 10)
					sm = new SystemMessage(1979);
				else if(left == 5)
					sm = new SystemMessage(1980);
				else if(left == 1)
					sm = new SystemMessage(1981);
				else
					sm = new SystemMessage(1982);
				sm.addItemName(Integer.valueOf(getItemId()));
				owner.sendPacket(sm);
			}
			if(left <= 0)
			{
				owner.getInventory().unEquipItem(this);
				owner.getInventory().destroyItem(this, getCount(), true);
				if(isTemporalItem())
					owner.sendMessage(owner.isLangRus() ? "\u0412\u0440\u0435\u043c\u0435\u043d\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0443\u0434\u0430\u043b\u0435\u043d." : "The limited time item has been deleted.");
				owner.sendPacket(new ItemList(owner, false));
				owner.broadcastUserInfo(true);
				return true;
			}
		}
		return false;
	}

	public int getLifeTimeRemaining()
	{
		if(isTemporalItem())
			return _lifeTimeRemaining - (int) (System.currentTimeMillis() / 1000L);
		return _lifeTimeRemaining;
	}

	private void setLifeTimeRemaining(final Player owner, final int lt)
	{
		assert !isTemporalItem();
		_lifeTimeRemaining = lt;
		_storedInDb = false;
		owner.sendPacket(new InventoryUpdate().addModifiedItem(this));
	}

	public void dropToTheGround(final Player lastAttacker, final NpcInstance dropper)
	{
		if(dropper == null)
		{
			final Location dropPos = Location.findAroundPosition(lastAttacker.getLoc(), 0, 70, lastAttacker.getGeoIndex());
			dropMe(lastAttacker, dropPos);
			if(Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed())
				ItemsAutoDestroy.getInstance().addItem(this);
			return;
		}
		final Location dropPos = Location.findAroundPosition(dropper.getLoc(), 0, 70, dropper.getGeoIndex());
		dropMe(dropper, dropPos);
		if(isHerb())
			ItemsAutoDestroy.getInstance().addHerb(this);
		else if(Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed())
			ItemsAutoDestroy.getInstance().addItem(this);
		if(lastAttacker != null)
			setItemDropOwner(lastAttacker, dropper.isRaid() ? (long) Config.NONOWNER_ITEM_PICKUP_DELAY_BOSS : (long) Config.NONOWNER_ITEM_PICKUP_DELAY);
	}

	public void dropToTheGround(final Creature dropper, final Location dropPos)
	{
		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
			dropMe(dropper, dropPos);
		else
			dropMe(dropper, dropper.getLoc());
		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance().addItem(this);
	}

	public boolean isDestroyable()
	{
		return _itemTemplate.isDestroyable();
	}

	public void setWhFlag(final boolean whflag)
	{
		_whflag = whflag;
	}

	public ItemClass getItemClass()
	{
		return _itemTemplate.getItemClass();
	}

	public void setItemId(final int id)
	{
		_itemId = id;
		_itemTemplate = ItemTable.getInstance().getTemplate(id);
		_storedInDb = false;
	}

	public boolean isArmor()
	{
		return getTemplate().isArmor();
	}

	public boolean isAccessory()
	{
		return getTemplate().isAccessory();
	}

	public boolean isHerb()
	{
		return getTemplate().isHerb();
	}

	public ItemGrade getItemGrade()
	{
		return _itemTemplate.getItemGrade();
	}

	public void setCustomFlags(final int i, final boolean updateDb)
	{
		if(_customFlags != i)
		{
			_customFlags = i;
			if(updateDb)
				this.updateDatabase();
			else
				_storedInDb = false;
		}
	}

	public int getCustomFlags()
	{
		return _customFlags;
	}

	public void setEngrave(final boolean val)
	{
		_engraved = val;
	}

	public boolean isEngraved()
	{
		return _engraved;
	}

	@Override
	public String getName()
	{
		return getTemplate().getName();
	}

	public boolean isWeapon()
	{
		return getTemplate().isWeapon();
	}

	public static void deleteItems(final int id)
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			if(player != null)
			{
				final ItemInstance item = player.getInventory().findItemByItemId(id);
				if(item == null)
					continue;
				player.getInventory().destroyItem(item);
			}
		for(final ItemInstance item2 : GameObjectsStorage.getItems())
			if(item2 != null && item2.getItemId() == id)
				item2.deleteMe();
		mysql.set("DELETE FROM `items` WHERE `item_id`='" + id + "'");
	}

	@Override
	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		L2GameServerPacket packet = null;
		if(dropper != null)
			packet = new DropItem(this, dropper.getObjectId());
		else
			packet = new SpawnItem(this);
		return Collections.singletonList(packet);
	}

	@Override
	public boolean isItem()
	{
		return true;
	}

	public enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		FREIGHT,
		MONSTER,
		DUMMY;
	}

	private class LazyUpdateInDb implements Runnable
	{
		private final int itemObjectId;

		public LazyUpdateInDb(final ItemInstance item)
		{
			itemObjectId = item.getObjectId();
		}

		@Override
		public void run()
		{
			final ItemInstance _item = GameObjectsStorage.getItem(itemObjectId);
			if(_item == null)
				return;
			try
			{
				_item.updateInDb();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				_item.stopLazyUpdateTask(false);
			}
		}
	}

	public class LifeTimeTask implements Runnable
	{
		@Override
		public void run()
		{
			final Player owner = getOwner();
			if(owner == null || !owner.isOnline())
				return;
			if(isShadowItem())
			{
				if(!isEquipped())
					return;
				setLifeTimeRemaining(owner, getLifeTimeRemaining() - 1);
			}
			if(checkDestruction(owner))
				return;
			_itemLifeTimeTask = ThreadPoolManager.getInstance().schedule(this, 60000L);
		}
	}
}
