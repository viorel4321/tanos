package l2s.gameserver.model.items;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.listeners.ArmorSetListener;
import l2s.gameserver.model.items.listeners.BowListener;
import l2s.gameserver.model.items.listeners.ChangeRecorder;
import l2s.gameserver.model.items.listeners.ItemAugmentationListener;
import l2s.gameserver.model.items.listeners.ItemSkillsListener;
import l2s.gameserver.model.items.listeners.PaperdollListener;
import l2s.gameserver.model.items.listeners.StatsListener;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.InventoryUpdate;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.PetInventoryUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.Util;

public abstract class Inventory
{
	protected static final Logger _log = LoggerFactory.getLogger(Inventory.class);

	public static final byte PAPERDOLL_UNDER = 0;
	public static final byte PAPERDOLL_LEAR = 1;
	public static final byte PAPERDOLL_REAR = 2;
	public static final byte PAPERDOLL_NECK = 3;
	public static final byte PAPERDOLL_LFINGER = 4;
	public static final byte PAPERDOLL_RFINGER = 5;
	public static final byte PAPERDOLL_HEAD = 6;
	public static final byte PAPERDOLL_RHAND = 7;
	public static final byte PAPERDOLL_LHAND = 8;
	public static final byte PAPERDOLL_GLOVES = 9;
	public static final byte PAPERDOLL_CHEST = 10;
	public static final byte PAPERDOLL_LEGS = 11;
	public static final byte PAPERDOLL_FEET = 12;
	public static final byte PAPERDOLL_BACK = 13;
	public static final byte PAPERDOLL_LRHAND = 14;
	public static final byte PAPERDOLL_HAIR = 15;
	public static final byte PAPERDOLL_DHAIR = 16;
	public static final byte PAPERDOLL_MAX = 17;

	private final ItemInstance[] _paperdoll;
	private final CopyOnWriteArrayList<PaperdollListener> _paperdollListeners;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	private List<ItemInstance> _listenedItems;
	protected final ConcurrentLinkedQueue<ItemInstance> _items;
	private int _totalWeight;
	private boolean _refreshingListeners;
	private long _wearedMask;
	public int _questSlots;
	public int _oldSlots;
	public static final Integer[] _castleCirclets = new Integer[] { 0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183 };
	public static final int FORMAL_WEAR_ID = 6408;
	public static ItemOrderComparator OrderComparator = new ItemOrderComparator();

	protected Inventory()
	{
		_questSlots = 0;
		_oldSlots = 0;
		_paperdoll = new ItemInstance[17];
		_items = new ConcurrentLinkedQueue<ItemInstance>();
		_paperdollListeners = new CopyOnWriteArrayList<PaperdollListener>();
		addPaperdollListener(new BowListener(this));
		addPaperdollListener(new ArmorSetListener(this));
		addPaperdollListener(new StatsListener(this));
		addPaperdollListener(new ItemSkillsListener(this));
		addPaperdollListener(new ItemAugmentationListener(this));
	}

	public abstract Creature getOwner();

	protected abstract ItemInstance.ItemLocation getBaseLocation();

	protected abstract ItemInstance.ItemLocation getEquipLocation();

	public int getOwnerId()
	{
		final Creature owner = getOwner();
		return owner == null ? 0 : owner.getObjectId();
	}

	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}

	public int getSize()
	{
		return _oldSlots;
	}

	public ItemInstance[] getItems()
	{
		return _items.toArray(new ItemInstance[_items.size()]);
	}

	public ConcurrentLinkedQueue<ItemInstance> getItemsList()
	{
		return _items;
	}

	public ItemInstance addItem(final int id, final long count)
	{
		final ItemInstance newItem = ItemTable.getInstance().createItem(id);
		newItem.setCount(count);
		return this.addItem(newItem);
	}

	public ItemInstance addItem(final ItemInstance newItem)
	{
		return this.addItem(newItem, true, true);
	}

	private ItemInstance addItem(final ItemInstance newItem, final boolean dbUpdate, final boolean log)
	{
		final Creature owner = getOwner();
		if(owner == null || newItem == null)
			return null;
		if(newItem.isHerb() && !owner.getPlayer().isGM())
		{
			Util.handleIllegalPlayerAction(owner.getPlayer(), "tried to pickup herb into inventory", 1);
			return null;
		}
		if(newItem.getCount() <= 0L)
			return null;
		ItemInstance result = newItem;
		boolean stackableFound = false;
		if(log)
			Log.LogItem(owner, "Get", result);
		if(newItem.isStackable())
		{
			final int itemId = newItem.getItemId();
			final ItemInstance old = getItemByItemId(itemId);
			if(Config.DON_LOG && log && itemId == Config.DON_ITEM_LOG && !Config.gmlist.containsKey(owner.getPlayer().getObjectId()))
			{
				long count;
				final long cn = count = newItem.getCount();
				if(old != null)
					count += old.getCount();
				if(count >= Config.DON_MIN_COUNT_LOG)
				{
					String ms = owner.toString() + ": " + cn + " | loc: " + owner.getX() + " " + owner.getY() + " " + owner.getZ();
					final GameObject target = owner.getTarget();
					if(target != null && target.getObjectId() != owner.getObjectId())
						ms = ms + " | target: " + target.getName() + "[" + target.getObjectId() + "] | targetLoc: " + target.getX() + " " + target.getY() + " " + target.getZ();
					ms = ms + " | total: " + count;
					Log.addLog(ms, "don");
				}
			}
			if(old != null)
			{
				old.setCount(old.getCount() + newItem.getCount());
				newItem.setCount(0L);
				newItem.setOwnerId(0);
				newItem.setLocation(ItemInstance.ItemLocation.VOID);
				newItem.removeFromDb();
				newItem.deleteMe();
				stackableFound = true;
				sendModifyItem(old);
				old.updateDatabase();
				result = old;
			}
		}
		if(!stackableFound)
		{
			if(getItemByObjectId(newItem.getObjectId()) == null)
			{
				getItemsList().add(newItem);
				if(!newItem.isEquipable() && newItem.getTemplate() instanceof EtcItemTemplate && !newItem.isStackable() && (newItem.getStatFuncs().length > 0 || newItem.getTemplate().getFirstSkill() != null ))
				{
					if(_listenedItems == null)
						_listenedItems = new CopyOnWriteArrayList<ItemInstance>();
					_listenedItems.add(newItem);
					for(final PaperdollListener listener : _paperdollListeners)
						listener.notifyEquipped(-1, newItem);
				}
			}
			else if(log)
				Log.LogItem(owner, "DoubleLink", newItem);
			if(newItem.getOwnerId() != owner.getPlayer().getObjectId() || dbUpdate)
			{
				newItem.setOwnerId(owner.getPlayer().getObjectId());
				newItem.setLocation(getBaseLocation(), findSlot(0));
				sendNewItem(newItem);
			}
			if(newItem.getTemplate().isQuest())
				++_questSlots;
			else
				++_oldSlots;
			if(dbUpdate)
				newItem.updateDatabase();
		}
		if(dbUpdate && result.isCursed() && owner.isPlayer())
			CursedWeaponsManager.getInstance().checkPlayer((Player) owner, result);
		if(newItem.isArrow() && owner.isPlayer() && owner.getActiveWeaponItem() != null && owner.getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.BOW)
		{
			final ItemInstance arrow = findArrowForBow(owner.getActiveWeaponItem());
			if(arrow != null)
				setPaperdollItem(8, arrow);
		}
		refreshWeight();
		return result;
	}

	public void restoreCursedWeapon()
	{
		final Creature owner = getOwner();
		if(owner == null || !owner.isPlayer())
			return;
		for(final ItemInstance i : getItemsList())
			if(i.isCursed())
			{
				CursedWeaponsManager.getInstance().checkPlayer((Player) owner, i);
				Inventory._log.info("Restored CursedWeapon [" + i + "] for: " + owner);
				break;
			}
	}

	public int findSlot(int slot)
	{
		for(final ItemInstance i : _items)
			if(!i.isEquipped())
			{
				if(i.getTemplate().getType2() == 3)
					continue;
				if(i.getEquipSlot() == slot)
					return findSlot(++slot);
				continue;
			}
		return slot;
	}

	public ItemInstance getPaperdollItem(final int slot)
	{
		return _paperdoll[slot];
	}

	public ItemInstance[] getPaperdollItems()
	{
		return _paperdoll;
	}

	public int getPaperdollItemId(final int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null)
			return item.getItemId();
		if(slot == 15)
		{
			item = _paperdoll[16];
			if(item != null)
				return item.getItemId();
		}
		return 0;
	}

	public int getPaperdollObjectId(final int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null)
			return item.getObjectId();
		if(slot == 15)
		{
			item = _paperdoll[16];
			if(item != null)
				return item.getObjectId();
		}
		return 0;
	}

	public synchronized void addPaperdollListener(final PaperdollListener listener)
	{
		_paperdollListeners.add(listener);
	}

	public synchronized void removePaperdollListener(final PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}

	public ItemInstance setPaperdollItem(final int slot, final ItemInstance item)
	{
		writeLock();
		try
		{
			ItemInstance old = _paperdoll[slot];
			if(old != item)
			{
				if(old != null)
				{
					_paperdoll[slot] = null;
					old.setLocation(getBaseLocation(), findSlot(0));
					sendModifyItem(old);
					long mask = 0L;
					for(int i = 0; i < 17; ++i)
					{
						final ItemInstance pi = _paperdoll[i];
						if(pi != null)
							mask |= pi.getTemplate().getItemMask();
					}
					_wearedMask = mask;
					old.updateDatabase();
					for(final PaperdollListener listener : _paperdollListeners)
						listener.notifyUnequipped(slot, old);
					old.shadowNotify(false);
				}
				if(item != null)
				{
					(_paperdoll[slot] = item).setLocation(getEquipLocation(), slot);
					sendModifyItem(item);
					_wearedMask |= item.getTemplate().getItemMask();
					item.updateDatabase();
					for(final PaperdollListener listener2 : _paperdollListeners)
						listener2.notifyEquipped(slot, item);
					item.shadowNotify(true);
				}
			}
			return old;
		}
		finally
		{
			writeUnlock();
		}
	}

	public long getWearedMask()
	{
		return _wearedMask;
	}

	public void unEquipItem(final ItemInstance item)
	{
		if(item.isEquipped())
			unEquipItemInBodySlot(item.getBodyPart(), item);
	}

	public ItemInstance[] unEquipItemInBodySlotAndRecord(final int slot, final ItemInstance item)
	{
		final ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInBodySlot(slot, item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	public ItemInstance[] unEquipItemInSlotAndRecord(final int slot)
	{
		final ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInSlot(slot);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	public void unEquipItemInBodySlotAndNotify(final int slot, final ItemInstance item)
	{
		final Player cha = getOwner().getPlayer();
		if(cha == null)
			return;
		final ItemInstance[] unequipped = unEquipItemInBodySlotAndRecord(slot, item);
		if(unequipped == null || unequipped.length == 0)
			return;
		final ItemInstance weapon = cha.getActiveWeaponInstance();
		for(final ItemInstance uneq : unequipped)
			if(uneq != null)
				if(!uneq.isWear())
				{
					cha.sendDisarmMessage(uneq);
					if(weapon != null && uneq == weapon)
					{
						uneq.setChargedSpiritshot((byte) 0);
						uneq.setChargedSoulshot((byte) 0);
						cha.abortAttack(true, true);
						cha.abortCast(true, false);
					}
				}
		if(item != null)
			cha.refreshExpertisePenalty();
		cha.broadcastUserInfo(true);
	}

	public ItemInstance unEquipItemInSlot(final int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}

	public void unEquipItemInBodySlot(final int slot, final ItemInstance item)
	{
		byte pdollSlot = -1;
		switch(slot)
		{
			case 8:
			{
				pdollSlot = 3;
				break;
			}
			case 4:
			{
				pdollSlot = 1;
				break;
			}
			case 2:
			{
				pdollSlot = 2;
				break;
			}
			case 32:
			{
				pdollSlot = 4;
				break;
			}
			case 16:
			{
				pdollSlot = 5;
				break;
			}
			case 65536:
			{
				pdollSlot = 15;
				break;
			}
			case 262144:
			{
				pdollSlot = 16;
				break;
			}
			case 524288:
			{
				setPaperdollItem(15, null);
				setPaperdollItem(16, null);
				pdollSlot = 15;
				break;
			}
			case 64:
			{
				pdollSlot = 6;
				break;
			}
			case 128:
			{
				pdollSlot = 7;
				break;
			}
			case 256:
			{
				pdollSlot = 8;
				break;
			}
			case 512:
			{
				pdollSlot = 9;
				break;
			}
			case 2048:
			{
				pdollSlot = 11;
				break;
			}
			case 1024:
			case 32768:
			case 131072:
			{
				pdollSlot = 10;
				break;
			}
			case 8192:
			{
				pdollSlot = 13;
				break;
			}
			case 4096:
			{
				pdollSlot = 12;
				break;
			}
			case 1:
			{
				pdollSlot = 0;
				break;
			}
			case 16384:
			{
				setPaperdollItem(8, null);
				setPaperdollItem(7, null);
				pdollSlot = 7;
				break;
			}
			default:
			{
				final String name = getOwner() == null ? "null" : getOwner().getPlayer().toString();
				Inventory._log.warn("Requested invalid body slot: " + slot + ", Item: " + item + ", owner: '" + name + "'");
				Thread.dumpStack();
				break;
			}
		}
		if(pdollSlot >= 0)
			setPaperdollItem(pdollSlot, null);
	}

	public synchronized void equipItem(final ItemInstance item, final boolean checks)
	{
		if(checks)
		{
			final Creature owner = getOwner();
			if(owner.isPlayer() && owner.getName() != null)
			{
				final SystemMessage msg = checkConditions(item);
				if(msg != null)
				{
					owner.sendPacket(msg);
					return;
				}
				if(item.isWeapon() && owner.getActiveWeaponItem() != null && owner.getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.BOW && owner.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				{
					owner.abortAttack(true, false);
					if(owner.getAI().getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
						owner.getAI().changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				}
			}
		}
		final int targetSlot = item.getTemplate().getBodyPart();
		double mp = 0.0;
		switch(targetSlot)
		{
			case 16384:
			{
				setPaperdollItem(8, null);
				setPaperdollItem(7, null);
				setPaperdollItem(7, item);
				break;
			}
			case 256:
			{
				final ItemInstance slot = getPaperdollItem(7);
				final ItemTemplate oldItem = slot == null ? null : slot.getTemplate();
				final ItemTemplate newItem = item.getTemplate();
				if(oldItem != null && newItem.getItemType() == EtcItemTemplate.EtcItemType.ARROW && oldItem.getItemType() == WeaponTemplate.WeaponType.BOW && oldItem.getItemGrade().ordinal() != newItem.getItemGrade().ordinal())
					return;
				if(newItem.getItemType() != EtcItemTemplate.EtcItemType.ARROW && newItem.getItemType() != EtcItemTemplate.EtcItemType.BAIT)
				{
					if(oldItem != null && oldItem.getBodyPart() == 16384)
					{
						setPaperdollItem(7, null);
						setPaperdollItem(8, null);
					}
					else
						setPaperdollItem(8, null);
					setPaperdollItem(8, item);
					break;
				}
				if(oldItem == null || (newItem.getItemType() != EtcItemTemplate.EtcItemType.ARROW || oldItem.getItemType() != WeaponTemplate.WeaponType.BOW) && (newItem.getItemType() != EtcItemTemplate.EtcItemType.BAIT || oldItem.getItemType() != WeaponTemplate.WeaponType.ROD))
					break;
				setPaperdollItem(8, item);
				if(newItem.getItemType() == EtcItemTemplate.EtcItemType.BAIT && getOwner().isPlayer())
				{
					final Player owner2 = (Player) getOwner();
					owner2.setVar("LastLure", String.valueOf(item.getObjectId()));
					break;
				}
				break;
			}
			case 128:
			{
				setPaperdollItem(7, item);
				break;
			}
			case 2:
			case 4:
			case 6:
			{
				boolean itemList;
				final boolean info = itemList = Config.USER_INFO_INTERVAL > 0;
				if(_paperdoll[1] == null)
				{
					item.setBodyPart(4);
					setPaperdollItem(1, item);
					itemList = true;
				}
				else if(_paperdoll[2] == null)
				{
					item.setBodyPart(2);
					setPaperdollItem(2, item);
					itemList = true;
				}
				else if(_paperdoll[1].getItemId() == item.getItemId() || _paperdoll[1].getItemId() == _paperdoll[2].getItemId())
				{
					item.setBodyPart(2);
					setPaperdollItem(2, null);
					setPaperdollItem(2, item);
				}
				else
				{
					item.setBodyPart(4);
					setPaperdollItem(1, null);
					setPaperdollItem(1, item);
				}
				if(info)
					((Player) getOwner()).sendUserInfo(true);
				if(itemList)
				{
					getOwner().sendPacket(new ItemList((Player) getOwner(), false));
					break;
				}
				break;
			}
			case 16:
			case 32:
			case 48:
			{
				boolean itemList;
				final boolean info = itemList = Config.USER_INFO_INTERVAL > 0;
				if(_paperdoll[4] == null)
				{
					item.setBodyPart(32);
					setPaperdollItem(4, item);
					itemList = true;
				}
				else if(_paperdoll[5] == null)
				{
					item.setBodyPart(16);
					setPaperdollItem(5, item);
					itemList = true;
				}
				else if(_paperdoll[4].getItemId() == item.getItemId() || _paperdoll[4].getItemId() == _paperdoll[5].getItemId())
				{
					item.setBodyPart(16);
					setPaperdollItem(5, null);
					setPaperdollItem(5, item);
				}
				else
				{
					item.setBodyPart(32);
					setPaperdollItem(4, null);
					setPaperdollItem(4, item);
				}
				if(info)
					((Player) getOwner()).sendUserInfo(true);
				if(itemList)
				{
					getOwner().sendPacket(new ItemList((Player) getOwner(), false));
					break;
				}
				break;
			}
			case 8:
			{
				setPaperdollItem(3, item);
				break;
			}
			case 32768:
			{
				if(getOwner() != null)
					mp = getOwner().getCurrentMp();
				setPaperdollItem(10, null);
				setPaperdollItem(11, null);
				setPaperdollItem(10, item);
				if(mp > getOwner().getCurrentMp())
				{
					getOwner().setCurrentMp(mp);
					break;
				}
				break;
			}
			case 1024:
			{
				if(getOwner() != null)
					mp = getOwner().getCurrentMp();
				setPaperdollItem(10, item);
				if(mp > getOwner().getCurrentMp())
				{
					getOwner().setCurrentMp(mp);
					break;
				}
				break;
			}
			case 2048:
			{
				final ItemInstance chest = getPaperdollItem(10);
				if(chest != null && chest.getBodyPart() == 32768)
					setPaperdollItem(10, null);
				if(getPaperdollItemId(10) == 6408)
					setPaperdollItem(10, null);
				if(getOwner() != null)
					mp = getOwner().getCurrentMp();
				setPaperdollItem(11, null);
				setPaperdollItem(11, item);
				if(mp > getOwner().getCurrentMp())
				{
					getOwner().setCurrentMp(mp);
					break;
				}
				break;
			}
			case 4096:
			{
				if(getPaperdollItemId(10) == 6408)
					setPaperdollItem(10, null);
				setPaperdollItem(12, item);
				break;
			}
			case 512:
			{
				if(getPaperdollItemId(10) == 6408)
					setPaperdollItem(10, null);
				setPaperdollItem(9, item);
				break;
			}
			case 64:
			{
				if(getPaperdollItemId(10) == 6408)
					setPaperdollItem(10, null);
				setPaperdollItem(6, item);
				break;
			}
			case 65536:
			{
				final ItemInstance slot = getPaperdollItem(16);
				if(slot != null && slot.getTemplate().getBodyPart() == 524288)
				{
					setPaperdollItem(15, null);
					setPaperdollItem(16, null);
				}
				setPaperdollItem(15, item);
				break;
			}
			case 262144:
			{
				final ItemInstance slot2 = getPaperdollItem(16);
				if(slot2 != null && slot2.getTemplate().getBodyPart() == 524288)
				{
					setPaperdollItem(15, null);
					setPaperdollItem(16, null);
				}
				setPaperdollItem(16, item);
				break;
			}
			case 524288:
			{
				setPaperdollItem(15, null);
				setPaperdollItem(16, null);
				setPaperdollItem(16, item);
				break;
			}
			case 1:
			{
				setPaperdollItem(0, item);
				break;
			}
			case 8192:
			{
				setPaperdollItem(13, item);
				break;
			}
			case 131072:
			{
				setPaperdollItem(11, null);
				setPaperdollItem(10, null);
				setPaperdollItem(6, null);
				setPaperdollItem(12, null);
				setPaperdollItem(9, null);
				setPaperdollItem(10, item);
				break;
			}
			default:
			{
				Inventory._log.warn("unknown body slot: " + targetSlot + " for item id: " + item.getItemId());
				break;
			}
		}
		if(getOwner().isPlayer())
		{
			final Player pr = getOwner().getPlayer();
			pr.autoShot();
			if(pr.recording && !ArrayUtils.contains(Config.BOTS_RT_EQUIP, item.getItemId()))
				pr.recBot(2, item.getItemId(), item.getEnchantLevel(), checks ? 1 : 0, 0, 0, 0);
		}
	}

	public ItemInstance getItemByItemId(final int itemId)
	{
		for(final ItemInstance temp : getItemsList())
			if(temp.getItemId() == itemId)
				return temp;
		return null;
	}

	public ItemInstance findItemByItemId(final int itemId)
	{
		return getItemByItemId(itemId);
	}

	public ItemInstance[] getAllItemsById(final int itemId)
	{
		final List<ItemInstance> ar = new ArrayList<ItemInstance>();
		for(final ItemInstance i : getItemsList())
			if(i.getItemId() == itemId)
				ar.add(i);
		return (ItemInstance[]) ar.toArray((Object[]) new ItemInstance[ar.size()]);
	}

	public int getPaperdollVariation1Id(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null && item.isAugmented())
			return item.getVariation1Id();
		return 0;
	}

	public int getPaperdollVariation2Id(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null && item.isAugmented())
			return item.getVariation2Id();
		return 0;
	}

	public ItemInstance getItemByObjectId(final Integer objectId)
	{
		for(final ItemInstance temp : getItemsList())
			if(temp.getObjectId() == objectId)
				return temp;
		return null;
	}

	public ItemInstance destroyItem(final int objectId, final long count, final boolean toLog)
	{
		final ItemInstance item = getItemByObjectId(objectId);
		return this.destroyItem(item, count, toLog);
	}

	public ItemInstance destroyItem(final ItemInstance item, final long count, final boolean toLog)
	{
		if(getOwner() == null || item == null)
			return null;
		if(count < 0L)
		{
			Inventory._log.warn("DestroyItem: count < 0 owner:" + getOwner().getName());
			Thread.dumpStack();
			return null;
		}
		if(toLog)
			Log.LogItem(getOwner(), "Delete", item, count);
		if(item.getCount() <= count)
		{
			removeItemFromInventory(item, true);
			if(PetDataTable.isPetControlItem(item))
				PetDataTable.deletePet(item, getOwner());
		}
		else
		{
			item.setCount(item.getCount() - count);
			sendModifyItem(item);
			item.updateDatabase();
		}
		refreshWeight();
		return item;
	}

	public ItemInstance destroyItem(final ItemInstance item)
	{
		if(getOwner() == null)
			return null;
		if(item == null)
			return null;
		removeItemFromInventory(item, true);
		if(PetDataTable.isPetControlItem(item))
			PetDataTable.deletePet(item, getOwner());
		refreshWeight();
		return item;
	}

	private void sendModifyItem(final ItemInstance item)
	{
		if(getOwner().isPet())
			getOwner().getPlayer().sendPacket(new PetInventoryUpdate().addModifiedItem(item));
		else
			getOwner().sendPacket(new InventoryUpdate().addModifiedItem(item));
	}

	private void sendRemoveItem(final ItemInstance item)
	{
		if(getOwner().isPet())
			getOwner().getPlayer().sendPacket(new PetInventoryUpdate().addRemovedItem(item));
		else
			getOwner().sendPacket(new InventoryUpdate().addRemovedItem(item));
	}

	private void sendNewItem(final ItemInstance item)
	{
		if(getOwner().isPet())
			getOwner().getPlayer().sendPacket(new PetInventoryUpdate().addNewItem(item));
		else
			getOwner().sendPacket(new InventoryUpdate().addNewItem(item));
	}

	public ItemInstance destroyItemByItemId(final int itemId, final long count, final boolean toLog)
	{
		final ItemInstance item = getItemByItemId(itemId);
		return this.destroyItem(item, count, toLog);
	}

	private void removeItemFromInventory(final ItemInstance item, final boolean clearCount)
	{
		if(getOwner() == null)
			return;
		if(getOwner().isPlayer())
		{
			final Player player = (Player) getOwner();
			player.removeItemFromShortCut(item.getObjectId());
			if(item.isEquipped())
				unEquipItem(item);
		}
		getItemsList().remove(item);
		item.shadowNotify(false);
		if(!item.isEquipable() && item.getTemplate() instanceof EtcItemTemplate && !item.isStackable() && (item.getStatFuncs().length > 0 || item.getTemplate().getFirstSkill() != null))
		{
			if(_listenedItems != null)
			{
				_listenedItems.remove(item);
				if(_listenedItems.isEmpty())
					_listenedItems = null;
			}
			for(final PaperdollListener listener : _paperdollListeners)
				listener.notifyUnequipped(-1, item);
		}
		if(clearCount)
			item.setCount(0L);
		if(item.getTemplate().isQuest())
		{
			--_questSlots;
			if(_questSlots < 0)
				_questSlots = 0;
		}
		else
		{
			--_oldSlots;
			if(_oldSlots < 0)
				_oldSlots = 0;
		}
		item.setOwnerId(0);
		item.setLocation(ItemInstance.ItemLocation.VOID);
		sendRemoveItem(item);
		item.updateDatabase(true);
		item.deleteMe();
	}

	public ItemInstance dropItem(final int objectId, final long count)
	{
		final ItemInstance item = getItemByObjectId(objectId);
		if(item == null)
		{
			Inventory._log.warn("DropItem: item objectId: " + objectId + " does not exist in inventory");
			Thread.dumpStack();
			return null;
		}
		return this.dropItem(item, count);
	}

	public ItemInstance dropItem(final ItemInstance item, final long count, final boolean whflag)
	{
		item.setWhFlag(whflag);
		return this.dropItem(item, count);
	}

	public ItemInstance dropItem(final ItemInstance oldItem, final long count)
	{
		if(getOwner() == null)
			return null;
		if(getOwner().isPlayer() && ((Player) getOwner()).getPlayerAccess() != null && ((Player) getOwner()).getPlayerAccess().BlockInventory)
			return null;
		if(count <= 0L)
		{
			Inventory._log.warn("DropItem: count <= 0 owner: " + getOwner().toString());
			return null;
		}
		if(oldItem == null)
		{
			Inventory._log.warn("DropItem: item id does not exist in inventory");
			return null;
		}
		Log.LogItem(getOwner(), "Drop", oldItem, count);
		if(oldItem.getCount() <= count || oldItem.getCount() <= 1L)
		{
			removeItemFromInventory(oldItem, false);
			refreshWeight();
			if(PetDataTable.isPetControlItem(oldItem))
				PetDataTable.unSummonPet(oldItem, getOwner());
			return oldItem;
		}
		oldItem.setCount(oldItem.getCount() - count);
		sendModifyItem(oldItem);
		final ItemInstance newItem = ItemTable.getInstance().createItem(oldItem.getItemId());
		newItem.setCount(count);
		oldItem.updateDatabase();
		refreshWeight();
		return newItem;
	}

	private void refreshWeight()
	{
		int weight = 0;
		for(final ItemInstance element : getItemsList())
			weight += (int) (element.getTemplate().getWeight() * element.getCount());
		_totalWeight = weight;
		if(getOwner().isPlayer())
			((Player) getOwner()).refreshOverloaded();
	}

	public int getTotalWeight()
	{
		return _totalWeight;
	}

	public ItemInstance findArrowForBow(final ItemTemplate bow)
	{
		return findItemByItemId(findArrowId(bow));
	}

	public int findArrowId(final ItemTemplate bow)
	{
		int arrowsId = 0;
		switch(bow.getItemGrade())
		{
			default:
			{
				arrowsId = 17;
				break;
			}
			case D:
			{
				arrowsId = 1341;
				break;
			}
			case C:
			{
				arrowsId = 1342;
				break;
			}
			case B:
			{
				arrowsId = 1343;
				break;
			}
			case A:
			{
				arrowsId = 1344;
				break;
			}
			case S:
			{
				arrowsId = 1345;
				break;
			}
		}
		return arrowsId;
	}

	public ItemInstance findEquippedLure()
	{
		ItemInstance res = null;
		int last_lure = 0;
		if(getOwner() != null && getOwner().isPlayer())
			try
			{
				final Player owner = (Player) getOwner();
				final String LastLure = owner.getVar("LastLure");
				if(LastLure != null && !LastLure.isEmpty())
					last_lure = Integer.valueOf(LastLure);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		for(final ItemInstance temp : getItemsList())
			if(temp.getItemType() == EtcItemTemplate.EtcItemType.BAIT)
			{
				if(temp.getLocation() == ItemInstance.ItemLocation.PAPERDOLL && temp.getEquipSlot() == 8)
					return temp;
				if(last_lure <= 0 || res != null || temp.getObjectId() != last_lure)
					continue;
				res = temp;
			}
		return res;
	}

	public synchronized void deleteMe()
	{
		for(final ItemInstance inst : getItemsList())
		{
			inst.updateInDb();
			inst.deleteMe();
		}
		getItemsList().clear();
	}

	public void updateDatabase(final boolean commit)
	{
		this.updateDatabase(getItemsList(), commit);
	}

	private void updateDatabase(final ConcurrentLinkedQueue<ItemInstance> items, final boolean commit)
	{
		if(getOwner() != null)
			for(final ItemInstance inst : items)
				inst.updateDatabase(commit);
	}

	public void validateItems()
	{
		for(final ItemInstance item : getItemsList())
		{
			if(!getOwner().isPlayer())
				continue;
			final Player player = getOwner().getPlayer();
			if(item.isClanApellaItem() && player.getPledgeClass() < 5)
				unEquipItem(item);
			else if(item.getItemId() >= 7850 && item.getItemId() <= 7859 && player.getLvlJoinedAcademy() == 0)
				unEquipItem(item);
			else
			{
				if(!item.isHeroItem() || getOwner().isHero())
					continue;
				unEquipItem(item);
				if(item.getItemId() == 6842)
					continue;
				this.destroyItem(item, 1L, false);
			}
		}
	}

	public void restore()
	{
		final int OWNER = getOwner().getObjectId();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY object_id DESC");
			statement.setInt(1, OWNER);
			statement.setString(2, getBaseLocation().name());
			statement.setString(3, getEquipLocation().name());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final ItemInstance item;
				if((item = ItemInstance.restoreFromDb(rset, con, true)) == null)
					continue;
				final ItemInstance newItem = this.addItem(item, false, false);
				if(newItem == null)
					continue;
				if(!item.isEquipped())
					continue;
				equipItem(item, false);
			}
		}
		catch(Exception e)
		{
			Inventory._log.error("could not restore inventory for player " + getOwner().toString() + ":", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void refreshListeners()
	{
		setRefreshingListeners(true);
		for(int i = 0; i < _paperdoll.length; ++i)
		{
			final ItemInstance item = getPaperdollItem(i);
			if(item != null)
				for(final PaperdollListener listener : _paperdollListeners)
				{
					listener.notifyUnequipped(i, item);
					listener.notifyEquipped(i, item);
				}
		}
		if(_listenedItems != null)
			for(final ItemInstance item : _listenedItems)
				for(final PaperdollListener listener : _paperdollListeners)
				{
					listener.notifyUnequipped(-1, item);
					listener.notifyEquipped(-1, item);
				}
		setRefreshingListeners(false);
	}

	public boolean isRefreshingListeners()
	{
		return _refreshingListeners;
	}

	public void setRefreshingListeners(final boolean refreshingListeners)
	{
		_refreshingListeners = refreshingListeners;
	}

	public long getCountOf(final int itemId)
	{
		long result = 0L;
		for(final ItemInstance item : getItemsList())
			if(item.getItemId() == itemId)
				result += item.getCount();
		return result;
	}

	public void checkAllConditions()
	{
		for(final ItemInstance item : _paperdoll)
			if(item != null && checkConditions(item) != null)
			{
				unEquipItem(item);
				getOwner().getPlayer().sendDisarmMessage(item);
			}
	}

	private SystemMessage checkConditions(final ItemInstance item)
	{
		final Player owner = getOwner().getPlayer();
		final int itemId = item.getItemId();
		final int targetSlot = item.getTemplate().getBodyPart();
		final Clan ownersClan = owner.getClan();
		if(item.isHeroItem() && !owner.isHero() && !owner.isGM())
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		if(itemId >= 7850 && itemId <= 7859 && owner.getLvlJoinedAcademy() == 0)
			return Msg.THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY;
		if(item.isClanApellaItem() && owner.getPledgeClass() < 5)
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		if(Arrays.asList(Inventory._castleCirclets).contains(itemId) && (ownersClan == null || itemId != Inventory._castleCirclets[ownersClan.getHasCastle()]))
			return new SystemMessage(new CustomMessage("l2s.gameserver.model.Inventory.CircletWorn").addString(ResidenceHolder.getInstance().getResidence(Castle.class, Arrays.asList(Inventory._castleCirclets).indexOf(itemId)).getName()));
		if(itemId == 6841 && (ownersClan == null || !owner.isClanLeader() || ownersClan.getHasCastle() == 0))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		if(targetSlot == 16384 || targetSlot == 256 || targetSlot == 128)
		{
			if(itemId != getPaperdollItemId(7) && CursedWeaponsManager.getInstance().isCursed(getPaperdollItemId(7)))
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
			if(owner.isCursedWeaponEquipped() && itemId != owner.getCursedWeaponEquippedId())
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		}
		if(Config.ENABLE_FORBIDDEN_BOW_CLASSES && !owner.isInOlympiadMode() && item.getItemType() == WeaponTemplate.WeaponType.BOW && ArrayUtils.contains(Config.FORBIDDEN_BOW_CLASSES, owner.getActiveClassId()))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		return null;
	}

	public final void writeLock()
	{
		writeLock.lock();
	}

	public final void writeUnlock()
	{
		writeLock.unlock();
	}

	public final void readLock()
	{
		readLock.lock();
	}

	public final void readUnlock()
	{
		readLock.unlock();
	}

	private static class ItemOrderComparator implements Comparator<ItemInstance>
	{
		@Override
		public int compare(final ItemInstance o1, final ItemInstance o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o1.getEquipSlot() - o2.getEquipSlot();
		}
	}
}
