package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.TradeController;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.network.l2.s2c.BuyList;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.SellList;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.WareHouseDepositList;
import l2s.gameserver.network.l2.s2c.WareHouseWithdrawList;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class NpcFriendInstance extends NpcInstance
{
	private static Logger _log;
	private long _lastSocialAction;

	public NpcFriendInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(this != player.getTarget())
		{
			player.setTarget(this);
			if(isAutoAttackable(player))
			{
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(9, (int) getCurrentHp());
				su.addAttribute(10, getMaxHp());
				player.sendPacket(su);
			}
			player.sendActionFailed();
			return;
		}
		if(Events.onAction(player, this, shift))
			return;
		if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}
		if(!this.isInRange(player, 150L))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}
		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
		{
			player.sendActionFailed();
			return;
		}
		if(hasRandomAnimation())
			onRandomAnimation();
		player.sendActionFailed();
		if(player.isMoving)
			player.stopMove();
		player.turn(this, 3000);
		String filename = "";
		if(getNpcId() >= 31370 && getNpcId() <= 31376 && player.getVarka() > 0 || getNpcId() >= 31377 && getNpcId() < 31384 && player.getKetra() > 0)
		{
			filename = "npc_friend/" + getNpcId() + "-nofriend.htm";
			this.showChatWindow(player, filename, new Object[0]);
			return;
		}
		switch(getNpcId())
		{
			case 31370:
			case 31371:
			case 31373:
			case 31377:
			case 31378:
			case 31380:
			case 31553:
			case 31554:
			{
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31372:
			{
				if(player.getKetra() > 2)
				{
					filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31379:
			{
				if(player.getVarka() > 2)
				{
					filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31374:
			{
				if(player.getKetra() > 1)
				{
					filename = "npc_friend/" + getNpcId() + "-warehouse.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31381:
			{
				if(player.getVarka() > 1)
				{
					filename = "npc_friend/" + getNpcId() + "-warehouse.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31375:
			{
				if(player.getKetra() == 3 || player.getKetra() == 4)
				{
					filename = "npc_friend/" + getNpcId() + "-special1.htm";
					break;
				}
				if(player.getKetra() == 5)
				{
					filename = "npc_friend/" + getNpcId() + "-special2.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31382:
			{
				if(player.getVarka() == 3 || player.getVarka() == 4)
				{
					filename = "npc_friend/" + getNpcId() + "-special1.htm";
					break;
				}
				if(player.getVarka() == 5)
				{
					filename = "npc_friend/" + getNpcId() + "-special2.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31376:
			{
				if(player.getKetra() == 4)
				{
					filename = "npc_friend/" + getNpcId() + "-normal.htm";
					break;
				}
				if(player.getKetra() == 5)
				{
					filename = "npc_friend/" + getNpcId() + "-special.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31383:
			{
				if(player.getVarka() == 4)
				{
					filename = "npc_friend/" + getNpcId() + "-normal.htm";
					break;
				}
				if(player.getVarka() == 5)
				{
					filename = "npc_friend/" + getNpcId() + "-special.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31555:
			{
				if(player.getRam() == 1)
				{
					filename = "npc_friend/" + getNpcId() + "-special1.htm";
					break;
				}
				if(player.getRam() == 2)
				{
					filename = "npc_friend/" + getNpcId() + "-special2.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
			case 31556:
			{
				if(player.getRam() == 2)
				{
					filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
					break;
				}
				filename = "npc_friend/" + getNpcId() + ".htm";
				break;
			}
		}
		this.showChatWindow(player, filename, new Object[0]);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		if(actualCommand.equalsIgnoreCase("Buff"))
		{
			if(st.countTokens() < 1)
				return;
			final int val = Integer.parseInt(st.nextToken());
			int item = 0;
			switch(getNpcId())
			{
				case 31372:
				{
					item = 7186;
					break;
				}
				case 31379:
				{
					item = 7187;
					break;
				}
				case 31556:
				{
					item = 7251;
					break;
				}
			}
			int skill = 0;
			int level = 0;
			long count = 0L;
			switch(val)
			{
				case 1:
				{
					skill = 4359;
					level = 2;
					count = 2L;
					break;
				}
				case 2:
				{
					skill = 4360;
					level = 2;
					count = 2L;
					break;
				}
				case 3:
				{
					skill = 4345;
					level = 3;
					count = 3L;
					break;
				}
				case 4:
				{
					skill = 4355;
					level = 2;
					count = 3L;
					break;
				}
				case 5:
				{
					skill = 4352;
					level = 1;
					count = 3L;
					break;
				}
				case 6:
				{
					skill = 4354;
					level = 3;
					count = 3L;
					break;
				}
				case 7:
				{
					skill = 4356;
					level = 1;
					count = 6L;
					break;
				}
				case 8:
				{
					skill = 4357;
					level = 2;
					count = 6L;
					break;
				}
			}
			if(skill != 0 && player.getInventory().getItemByItemId(item) != null && item > 0 && player.getInventory().getItemByItemId(item).getIntegerLimitedCount() >= count)
			{
				if(player.getInventory().destroyItemByItemId(item, count, false) == null)
					NpcFriendInstance._log.info("L2NpcFriendInstance[274]: Item not found!!!");
				player.doCast(SkillTable.getInstance().getInfo(skill, level), player, true);
			}
			else
				this.showChatWindow(player, "npc_friend/" + getNpcId() + "-havenotitems.htm", new Object[0]);
		}
		else if(command.startsWith("Chat"))
		{
			final int val = Integer.parseInt(command.substring(5));
			String fname = "";
			fname = "npc_friend/" + getNpcId() + "-" + val + ".htm";
			if(!fname.equals(""))
				this.showChatWindow(player, fname, new Object[0]);
		}
		else if(command.startsWith("Buy"))
		{
			final int val = Integer.parseInt(command.substring(4));
			showBuyWindow(player, val, false);
		}
		else if(actualCommand.equalsIgnoreCase("Sell"))
			showSellWindow(player);
		else if(command.startsWith("WithdrawP"))
		{
			if(!Config.ALLOW_WAREHOUSE)
			{
				player.sendActionFailed();
				return;
			}
			final int val = Integer.parseInt(command.substring(10));
			if(val == 9)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
				html.setFile("npc-friend/personal.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
				showRetrieveWindow(player, val);
		}
		else if(command.equals("DepositP"))
		{
			if(!Config.ALLOW_WAREHOUSE)
			{
				player.sendActionFailed();
				return;
			}
			showDepositWindow(player);
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void showBuyWindow(final Player player, final int listId, final boolean tax)
	{
		if(!player.getPlayerAccess().UseShop)
			return;
		player.tempInventoryDisable();
		final TradeController.NpcTradeList list = TradeController.getInstance().getBuyList(listId);
		if(list != null && list.getNpcId() == getNpcId())
		{
			player.setLastNpcId(getNpcId());
			player.setBuyListId(listId);
			final BuyList bl = new BuyList(list, player.getAdena(), 0.0);
			player.sendPacket(bl);
		}
		else
		{
			NpcFriendInstance._log.warn("[L2NpcFriendInstance] possible client hacker: " + player.toString() + " attempting to buy from GM shop! < Ban him!");
			NpcFriendInstance._log.warn("buylist id:" + listId + " / list_npc = " + list.getNpcId() + " / npc = " + getNpcId());
		}
	}

	private void showSellWindow(final Player player)
	{
		if(!player.getPlayerAccess().UseShop)
			return;
		player.setLastNpcId(getNpcId());
		final SellList sl = new SellList(player);
		player.sendPacket(sl);
	}

	private void showDepositWindow(final Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		player.setUsingWarehouseType(Warehouse.WarehouseType.PRIVATE);
		player.tempInventoryDisable();
		player.sendPacket(new WareHouseDepositList(player, Warehouse.WarehouseType.PRIVATE));
		player.sendActionFailed();
	}

	private void showRetrieveWindow(final Player player, final int val)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		player.setUsingWarehouseType(Warehouse.WarehouseType.PRIVATE);
		player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.PRIVATE, ItemClass.values()[val]));
	}

	static
	{
		NpcFriendInstance._log = LoggerFactory.getLogger(NpcFriendInstance.class);
	}
}
