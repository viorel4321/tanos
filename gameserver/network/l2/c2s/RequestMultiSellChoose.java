package l2s.gameserver.network.l2.c2s;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.lists.IntList;
import org.napile.primitive.lists.impl.ArrayIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Util;

public class RequestMultiSellChoose extends L2GameClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(RequestMultiSellChoose.class);
	private int _listId;
	private int _entryId;
	private int _amount;
	private int _enchant;
	private boolean _keepenchant;
	private boolean _extra;
	private boolean _notax;
	private MultiSellHolder.MultiSellListContainer _list;
	private List<ItemData> _items;
	private String query;

	public RequestMultiSellChoose()
	{
		_enchant = 0;
		_keepenchant = false;
		_extra = false;
		_notax = false;
		_list = null;
		_items = new ArrayList<ItemData>();
		query = "INSERT INTO `multisell_log` VALUES (?,?,?,?,?,?,?);";
	}

	@Override
	public void readImpl()
	{
		try
		{
			_listId = readD();
			_entryId = readD();
			_amount = readD();
		}
		catch(BufferUnderflowException ex)
		{}
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _amount < 1)
			return;
		_amount = Math.min(_amount, Config.MULTISELL_MAX_AMOUNT);
		final boolean bbs = activeChar.getLastNpcId() == -1;
		final NpcInstance npc = activeChar.getLastNpc();
		if(!bbs && (!NpcInstance.canBypassCheck(activeChar, npc) || !activeChar.checkLastNpc()))
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0412 \u043f\u043e\u0434\u043e\u0431\u043d\u044b\u0445 \u0443\u0441\u043b\u043e\u0432\u0438\u044f\u0445 \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e!" : "Impossible under such conditions!");
			activeChar.sendActionFailed();
			return;
		}
		if(bbs && !ArrayUtils.contains(Config.CB_MULTISELLS, _listId))
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0417\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043d\u044b\u0439 \u043c\u0443\u043b\u044c\u0442\u0438\u0441\u0435\u043b\u043b!" : "Forbidden meltisell!");
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && !bbs && activeChar.getKarma() > 0 && !ArrayUtils.contains(Config.ALT_GAME_KARMA_NPC, npc.getNpcId()))
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0418\u0437\u0431\u0430\u0432\u044c\u0442\u0435\u0441\u044c \u043e\u0442 \u043a\u0430\u0440\u043c\u044b!" : "You have a karma!");
			activeChar.sendActionFailed();
			return;
		}
		if(bbs && !Config.ALLOW_PVPCB_SHOP_KARMA && activeChar.getKarma() > 0)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0421 \u043a\u0430\u0440\u043c\u043e\u0439 \u044d\u0442\u043e \u043d\u0435 \u043a\u0430\u043d\u0430\u0435\u0442!" : "You can't do it with karma!");
			activeChar.sendActionFailed();
			return;
		}
		_list = activeChar.getMultisell();
		if(_list == null)
		{
			activeChar.sendActionFailed();
			activeChar.setMultisell(null);
			return;
		}
		if(activeChar.getMultisell().getListId() != _listId)
		{
			Util.handleIllegalPlayerAction(activeChar, "RequestMultiSellChoose[110] Tried to buy from multisell: " + _listId, 1);
			activeChar.sendActionFailed();
			activeChar.setMultisell(null);
			return;
		}
		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u041d\u0430 \u043e\u043b\u0438\u043c\u043f\u0438\u0430\u0434\u0435 \u044d\u0442\u043e \u043d\u0435 \u043a\u0430\u043d\u0430\u0435\u0442!" : "At the Olympics to use the exchange is forbidden!");
			activeChar.sendActionFailed();
			activeChar.setMultisell(null);
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		_keepenchant = _list.isKeepEnchant();
		_extra = _list.isExtra();
		_notax = _list.isNoTax();
		for(final MultiSellEntry entry : _list.getEntries())
			if(entry.getEntryId() == _entryId)
			{
				doExchange(activeChar, entry);
				break;
			}
	}

	private void doExchange(final Player activeChar, final MultiSellEntry entry)
	{
		final PcInventory inv = activeChar.getInventory();
		int totalAdenaCost = 0;
		final NpcInstance merchant = activeChar.getLastNpc();
		final Castle castle = merchant != null ? merchant.getCastle() : null;
		final List<MultiSellIngredient> productId = entry.getProduction();
		if(_keepenchant)
			for(final MultiSellIngredient p : productId)
				_enchant = Math.max(_enchant, p.getItemEnchant());
		String itemIds = "";
		String counts = "";
		String dItemId = "";
		String dCount = "";
		inv.writeLock();
		try
		{
			final int tax = SafeMath.mulAndCheck(entry.getTax(), _amount);
			final int slots = inv.slotsLeft();
			if(slots == 0)
			{
				activeChar.sendPacket(new SystemMessage(1139));
				return;
			}
			int req = 0;
			long totalLoad = 0L;
			for(final MultiSellIngredient i : productId)
			{
				if(i.getItemId() <= 0)
					continue;
				if(activeChar.itemLimM(i, _amount))
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("\u041c\u043d\u043e\u0433\u043e \u0431\u0435\u0440\u0435\u0442\u0435! \u041d\u0435\u0442 \u043c\u0435\u0441\u0442\u0430!");
					else
						activeChar.sendMessage("Take a lot! No space!");
					return;
				}
				totalLoad += ItemTable.getInstance().getTemplate(i.getItemId()).getWeight() * _amount;
				if(!ItemTable.getInstance().getTemplate(i.getItemId()).isStackable())
					req += _amount;
				else
					++req;
			}
			if(req > slots || !inv.validateWeight(totalLoad))
			{
				activeChar.sendPacket(new SystemMessage(1139));
				return;
			}
			if(entry.getIngredients().size() == 0)
			{
				RequestMultiSellChoose._log.warn("Ingredients list = 0 multisell id=:" + _listId + " player: " + activeChar.toString());
				activeChar.sendActionFailed();
				activeChar.setMultisell(null);
				return;
			}
			final List<Augmentation> augmentations = new ArrayList<Augmentation>();
			final IntList enchants = _extra ? new ArrayIntList() : null;
			for(final MultiSellIngredient ingridient : entry.getIngredients())
			{
				final int ingridientItemId = ingridient.getItemId();
				final long ingridientItemCount = ingridient.getItemCount();
				final long total_amount = ingridientItemCount * _amount;
				if(total_amount <= 0L || total_amount > Integer.MAX_VALUE)
				{
					activeChar.sendActionFailed();
					return;
				}
				if(ingridientItemId > 0 && !ItemTable.getInstance().getTemplate(ingridientItemId).isStackable()) {
					for (int j = 0; j < ingridientItemCount * _amount; ++j) {
						final ItemInstance[] list = inv.getAllItemsById(ingridientItemId);
						if (_keepenchant) {
							ItemInstance itemToTake = null;
							for (final ItemInstance itm : list)
								if ((itm.getEnchantLevel() == _enchant || itm.getTemplate().getType2() > 2 || _extra && itm.getTemplate().getType2() < 3) && !_items.contains(new ItemData(itm.getItemId(), itm.getCount(), itm)) && !itm.isShadowItem() && !itm.isTemporalItem() && (itm.getCustomFlags() & 0x2) != 0x2) {
									if (_extra && itm.getTemplate().getType2() < 3)
										enchants.add(itm.getEnchantLevel());
									itemToTake = itm;
									break;
								}
							if (itemToTake == null) {
								activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
								return;
							}
							if (!checkItem(itemToTake, activeChar)) {
								activeChar.sendActionFailed();
								return;
							}
							if (itemToTake.isAugmented()) {
								itemToTake.setWhFlag(true);
								augmentations.add(new Augmentation(itemToTake));
							}
							_items.add(new ItemData(itemToTake.getItemId(), 1L, itemToTake));
						} else {
							ItemInstance itemToTake = null;
							for (final ItemInstance itm : list)
								if (!_items.contains(new ItemData(itm.getItemId(), itm.getCount(), itm)) && (itemToTake == null || itm.getEnchantLevel() < itemToTake.getEnchantLevel()) && !itm.isShadowItem() && !itm.isTemporalItem() && (itm.getCustomFlags() & 0x2) != 0x2 && checkItem(itm, activeChar)) {
									itemToTake = itm;
									if (itemToTake.getEnchantLevel() == 0)
										break;
								}
							if (itemToTake == null) {
								activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
								return;
							}
							if (itemToTake.isAugmented()) {
								itemToTake.setWhFlag(true);
								augmentations.add(new Augmentation(itemToTake));
							}
							_items.add(new ItemData(itemToTake.getItemId(), 1L, itemToTake));
						}
					}
				} else if(ingridientItemId == -200) {
					if(activeChar.getClan() == null)
					{
						activeChar.sendPacket(new SystemMessage(212));
						return;
					}
					if(activeChar.getClan().getReputationScore() < total_amount)
					{
						activeChar.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
						return;
					}
					if(activeChar.getClan().getLeaderId() != activeChar.getObjectId())
					{
						activeChar.sendPacket(new SystemMessage(9).addString(activeChar.getName()));
						return;
					}
					_items.add(new ItemData(ingridientItemId, total_amount, null));
				} else if(ingridientItemId == -100) {
					if(activeChar.getPcBangPoints() < total_amount)
					{
						activeChar.sendPacket(new SystemMessage(1710));
						return;
					}
					_items.add(new ItemData(ingridientItemId, total_amount, null));
				} else {
					if(ingridientItemId == 57)
						totalAdenaCost += (int) (ingridientItemCount * _amount);
					final ItemInstance item = inv.getItemByItemId(ingridientItemId);
					if(item == null || item.getCount() < total_amount)
					{
						activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
						return;
					}
					_items.add(new ItemData(item.getItemId(), total_amount, item));
				}

				if(activeChar.getAdena() < totalAdenaCost)
				{
					activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
			}
			for(final ItemData id : _items)
			{
				final long count = id.getCount();
				if(count > 0L)
				{
					final ItemInstance item2 = id.getItem();
					if(item2 != null)
					{
						activeChar.sendPacket(SystemMessage.removeItems(item2.getItemId(), count));
						inv.destroyItem(item2, count, true);
					}
					else if(id.getId() == -200)
					{
						activeChar.getClan().incReputation((int) -count, false, "MultiSell" + _listId);
						activeChar.sendPacket(new SystemMessage(1787).addNumber(Long.valueOf(count)));
					}
					else if(id.getId() == -100)
						activeChar.reducePcBangPoints((int) count);
					dItemId = dItemId + id.getId() + ";";
					dCount = dCount + id.getCount() + ";";
				}
			}
			if(tax > 0 && !_notax && castle != null)
			{
				activeChar.sendMessage("Tax: " + tax);
				if(merchant != null)
					castle.addToTreasury(tax, true, false);
			}
			for(final MultiSellIngredient in : productId)
			{
				if(in.getItemId() <= 0)
				{
					if(in.getItemId() == -200)
					{
						activeChar.getClan().incReputation(in.getItemCount() * _amount, false, "MultiSell" + _listId);
						activeChar.sendPacket(new SystemMessage(1781).addNumber(Integer.valueOf(in.getItemCount() * _amount)));
					}
					else if(in.getItemId() == -100)
						activeChar.addPcBangPoints(in.getItemCount() * _amount, false);
				}
				else if(ItemTable.getInstance().getTemplate(in.getItemId()).isStackable())
				{
					final ItemInstance product = ItemTable.getInstance().createItem(in.getItemId());
					final double total = in.getItemCount() * _amount;
					if(total < 0.0 || total > 2.147483647E9)
					{
						activeChar.sendActionFailed();
						return;
					}
					product.setCount((long) total);
					activeChar.sendPacket(SystemMessage.obtainItems(product));
					inv.addItem(product);
				}
				else
					for(int cnt = _amount * in.getItemCount(), k = 0; k < cnt; ++k)
					{
						final ItemInstance product2 = ItemTable.getInstance().createItem(in.getItemId());
						if(_keepenchant)
						{
							if(_extra)
							{
								if(!enchants.isEmpty() && product2.getTemplate().getType2() < 3)
									product2.setEnchantLevel(enchants.removeByIndex(0));
							}
							else
								product2.setEnchantLevel(_enchant);
						}
						else if(product2.getTemplate().getType2() < 3)
							product2.setEnchantLevel(in.getItemEnchant());
						if(!augmentations.isEmpty() && product2.getTemplate() instanceof WeaponTemplate && product2.canBeEnchanted())
						{
							Augmentation augmentation = augmentations.remove(0);
							product2.setVariation1Id(augmentation.getVariation1Id());
							product2.setVariation2Id(augmentation.getVariation2Id());
							product2.setVariationStoneId(augmentation.getVariationStoneId());
							product2.updateDatabase();
						}
						activeChar.sendPacket(SystemMessage.obtainItems(product2));
						inv.addItem(product2);
					}
				itemIds = itemIds + in.getItemId() + ";";
				counts = counts + in.getItemCount() + ";";
			}
		}
		catch(ArithmeticException ae)
		{
			activeChar.sendPacket(new SystemMessage(1036));
			return;
		}
		finally
		{
			inv.writeUnlock();
		}
		activeChar.sendPacket(new StatusUpdate(activeChar.getObjectId()).addAttribute(14, activeChar.getCurrentLoad()));
		if(_list == null || !_list.isShowAll())
			MultiSellHolder.getInstance().SeparateAndSend(_listId, activeChar, castle == null ? 0.0 : castle.getTaxRate());
	}

	private boolean checkItem(final ItemInstance temp, final Player activeChar)
	{
		if(temp == null)
		{
			activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return false;
		}
		if(temp.isHeroItem())
			return false;
		if(temp.isShadowItem())
			return false;
		if(temp.isTemporalItem())
			return false;
		if(PetDataTable.isPetControlItem(temp) && activeChar.isMounted())
			return false;
		if(activeChar.getServitor() != null && temp.getObjectId() == activeChar.getServitor().getControlItemId())
			return false;
		if(temp.isEquipped())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u041f\u0435\u0440\u0435\u0434 \u043e\u0431\u043c\u0435\u043d\u043e\u043c, \u043d\u0443\u0436\u043d\u043e \u0441\u043d\u044f\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442!" : "You must unequip item before exchange!");
			return false;
		}
		if(temp.isWear())
			return false;
		if(activeChar.getEnchantScroll() == temp)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u041f\u0435\u0440\u0435\u0434 \u043e\u0431\u043c\u0435\u043d\u043e\u043c, \u043d\u0443\u0436\u043d\u043e \u0437\u0430\u043a\u0440\u044b\u0442\u044c \u0437\u0430\u0442\u043e\u0447\u043a\u0443!" : "You must close enchant before exchange!");
			return false;
		}
		return true;
	}

	private class ItemData
	{
		private final int _id;
		private final long _count;
		private final ItemInstance _item;

		public ItemData(final int id, final long count, final ItemInstance item)
		{
			_id = id;
			_count = count;
			_item = item;
		}

		public int getId()
		{
			return _id;
		}

		public long getCount()
		{
			return _count;
		}

		public ItemInstance getItem()
		{
			return _item;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if(!(obj instanceof ItemData))
				return false;
			final ItemData i = (ItemData) obj;
			return _id == i._id && _count == i._count && _item == i._item;
		}

		@Override     
		public int hashCode()
		{
			int hash = _item.hashCode();
			hash = 76 * hash + _id;
			hash = 76 * hash + (int) (_count / 1757);
			return hash;
		}
	}

	private class Augmentation {
		private final int variation1Id;
		private final int variation2Id;
		private final int variationStoneId;

		public Augmentation(ItemInstance item) {
			this.variation1Id = item.getVariation1Id();
			this.variation2Id = item.getVariation2Id();
			this.variationStoneId = item.getVariationStoneId();
		}

		public int getVariation1Id() {
			return variation1Id;
		}

		public int getVariation2Id() {
			return variation2Id;
		}

		public int getVariationStoneId() {
			return variationStoneId;
		}
	}
}
