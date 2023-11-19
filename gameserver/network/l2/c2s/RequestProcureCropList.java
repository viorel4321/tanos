package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.ManorManagerInstance;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestProcureCropList extends L2GameClientPacket
{
	private int _size;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_size = readD();
		if(_size * 16 > _buf.remaining() || _size > 32767 || _size <= 0)
		{
			_size = 0;
			return;
		}
		_items = new int[_size * 4];
		for(int i = 0; i < _size; ++i)
		{
			final int objId = readD();
			_items[i * 4 + 0] = objId;
			final int itemId = readD();
			_items[i * 4 + 1] = itemId;
			final int manorId = readD();
			_items[i * 4 + 2] = manorId;
			final int count = readD();
			_items[i * 4 + 3] = count;
		}
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(_size < 1)
		{
			player.sendActionFailed();
			return;
		}
		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInStoreMode())
		{
			player.sendPacket(new SystemMessage(1065));
			return;
		}
		if(player.isInTrade())
		{
			player.sendActionFailed();
			return;
		}
		final GameObject target = player.getTarget();
		final ManorManagerInstance manor = target != null && target instanceof ManorManagerInstance ? (ManorManagerInstance) target : null;
		if(manor == null || !player.isInRange(manor, 150L))
		{
			player.sendActionFailed();
			return;
		}
		final int currentManorId = manor == null ? 0 : manor.getCastle().getId();
		int slots = 0;
		int weight = 0;
		for(int i = 0; i < _size; ++i)
		{
			final int itemId = _items[i * 4 + 1];
			final int manorId = _items[i * 4 + 2];
			final int count = _items[i * 4 + 3];
			if(itemId != 0 && manorId != 0)
				if(count != 0)
					if(count >= 1)
					{
						if(count > Integer.MAX_VALUE)
						{
							this.sendPacket(new SystemMessage(1036));
							return;
						}
						final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
						if(castle == null)
							return;
						final CastleManorManager.CropProcure crop = castle.getCrop(itemId, 0);
						if(crop == null || crop.getId() == 0 || crop.getPrice() == 0)
							return;
						try
						{
							final int rewardItemId = Manor.getInstance().getRewardItem(itemId, crop.getReward());
							final ItemTemplate template = ItemTable.getInstance().getTemplate(rewardItemId);
							weight += count * template.getWeight();
							if(!template.isStackable())
								slots += count;
							else if(player.getInventory().getItemByItemId(itemId) == null)
								++slots;
						}
						catch(NullPointerException e)
						{}
					}
		}
		if(!player.getInventory().validateWeight(weight))
		{
			this.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}
		if(!player.getInventory().validateCapacity(slots))
		{
			this.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}
		for(int i = 0; i < _size; ++i)
		{
			final int objId = _items[i * 4 + 0];
			final int cropId = _items[i * 4 + 1];
			final int manorId2 = _items[i * 4 + 2];
			final int count2 = _items[i * 4 + 3];
			if(objId != 0 && cropId != 0 && manorId2 != 0)
				if(count2 != 0)
					if(count2 >= 1)
					{
						final Castle castle2 = ResidenceHolder.getInstance().getResidence(Castle.class, manorId2);
						if(castle2 != null)
						{
							final CastleManorManager.CropProcure crop2 = castle2.getCrop(cropId, 0);
							if(crop2 != null && crop2.getId() != 0)
								if(crop2.getPrice() != 0)
								{
									long fee = 0L;
									final int rewardItem = Manor.getInstance().getRewardItem(cropId, crop2.getReward());
									if(count2 <= crop2.getAmount())
									{
										final long sellPrice = count2 * crop2.getPrice();
										final long rewardPrice = ItemTable.getInstance().getTemplate(rewardItem).getReferencePrice();
										if(rewardPrice != 0L)
										{
											final long rewardItemCount = sellPrice / rewardPrice;
											if(rewardItemCount < 1L)
											{
												final SystemMessage sm = new SystemMessage(1491);
												sm.addItemName(Integer.valueOf(cropId));
												sm.addNumber(Integer.valueOf(count2));
												player.sendPacket(sm);
											}
											else
											{
												if(manorId2 != currentManorId)
													fee = sellPrice * 5L / 100L;
												if(player.getInventory().getAdena() < fee)
												{
													final SystemMessage sm = new SystemMessage(1491);
													sm.addItemName(Integer.valueOf(cropId));
													sm.addNumber(Integer.valueOf(count2));
													player.sendPacket(sm);
													player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
												}
												else
												{
													ItemInstance itemDel = null;
													ItemInstance itemAdd = null;
													if(player.getInventory().getItemByObjectId(objId) != null)
													{
														final ItemInstance item = player.getInventory().getItemByObjectId(objId);
														if(item.getIntegerLimitedCount() >= count2)
														{
															itemDel = player.getInventory().destroyItem(objId, count2, true);
															if(itemDel != null)
															{
																if(fee > 0L)
																	player.getInventory().reduceAdena(fee);
																crop2.setAmount(crop2.getAmount() - count2);
																if(Config.MANOR_SAVE_ALL_ACTIONS)
																	ResidenceHolder.getInstance().getResidence(Castle.class, manorId2).updateCrop(crop2.getId(), crop2.getAmount(), 0);
																itemAdd = player.getInventory().addItem(rewardItem, rewardItemCount);
																if(itemAdd != null)
																{
																	SystemMessage sm2 = new SystemMessage(1490);
																	sm2.addItemName(Integer.valueOf(cropId));
																	sm2.addNumber(Integer.valueOf(count2));
																	player.sendPacket(sm2);
																	if(fee > 0L)
																	{
																		sm2 = new SystemMessage(1607);
																		sm2.addNumber(Long.valueOf(fee));
																		player.sendPacket(sm2);
																	}
																	sm2 = new SystemMessage(301);
																	sm2.addItemName(Integer.valueOf(cropId));
																	sm2.addNumber(Integer.valueOf(count2));
																	player.sendPacket(sm2);
																	if(fee > 0L)
																	{
																		sm2 = new SystemMessage(672);
																		sm2.addNumber(Long.valueOf(fee));
																		player.sendPacket(sm2);
																	}
																	sm2 = new SystemMessage(53);
																	sm2.addItemName(Integer.valueOf(rewardItem));
																	sm2.addNumber(Long.valueOf(rewardItemCount));
																	player.sendPacket(sm2);
																}
															}
														}
													}
												}
											}
										}
									}
								}
						}
					}
		}
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(14, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
