package l2s.gameserver.model.instances;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.WareHouseDepositList;
import l2s.gameserver.network.l2.s2c.WareHouseWithdrawList;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.templates.npc.NpcTemplate;

public class CastleWarehouseInstance extends NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	public CastleWarehouseInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	private void showRetrieveWindow(final Player player, final int val)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.PRIVATE, ItemClass.values()[val]), Msg.ActionFail);
	}

	private void showDepositWindow(final Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		player.tempInventoryDisable();
		player.sendPacket(new WareHouseDepositList(player, Warehouse.WarehouseType.PRIVATE), Msg.ActionFail);
	}

	private void showDepositWindowClan(final Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		if(player.getClan() == null)
		{
			player.sendActionFailed();
			return;
		}
		if(player.getClan().getLevel() == 0)
		{
			player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE, Msg.ActionFail);
			return;
		}
		player.tempInventoryDisable();
		if(!player.isClanLeader() && (!Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || (player.getClanPrivileges() & 0x8) != 0x8))
			player.sendPacket(Msg.ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE);
		player.sendPacket(new WareHouseDepositList(player, Warehouse.WarehouseType.CLAN));
	}

	private void showWithdrawWindowClan(final Player player, final int val)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		if(player.getClan() == null)
		{
			player.sendActionFailed();
			return;
		}
		if(player.getClan().getLevel() == 0)
		{
			player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE, Msg.ActionFail);
			return;
		}
		if((player.getClanPrivileges() & 0x8) == 0x8)
		{
			player.tempInventoryDisable();
			player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.CLAN, ItemClass.values()[val]));
		}
		else
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE, Msg.ActionFail);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		if(!Config.ALLOW_WAREHOUSE)
		{
			player.sendActionFailed();
			return;
		}
		if((player.getClanPrivileges() & 0x40000) != 0x40000)
		{
			player.sendMessage("You don't have rights to do that.");
			return;
		}
		player.closeEnchant();
		if(command.startsWith("WithdrawP"))
		{
			final int val = Integer.parseInt(command.substring(10));
			if(val == 9)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("warehouse/personal.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
				showRetrieveWindow(player, val);
		}
		else if(command.equals("DepositP"))
			showDepositWindow(player);
		else if(command.startsWith("WithdrawC"))
		{
			final int val = Integer.parseInt(command.substring(10));
			if(val == 9)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("warehouse/clan.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
				showWithdrawWindowClan(player, val);
		}
		else if(command.equals("DepositC"))
			showDepositWindowClan(player);
		else if(command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch(IndexOutOfBoundsException ex)
			{}
			catch(NumberFormatException ex2)
			{}
			this.showChatWindow(player, val, new Object[0]);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		player.sendActionFailed();
		String filename = "castle/warehouse/castlewarehouse-no.htm";
		final int condition = validateCondition(player);
		if(condition > 0)
			if(condition == 1)
				filename = "castle/warehouse/castlewarehouse-busy.htm";
			else if(condition == 2)
				if(val == 0)
					filename = "castle/warehouse/castlewarehouse.htm";
				else
					filename = "castle/warehouse/castlewarehouse-" + val + ".htm";
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	protected int validateCondition(final Player player)
	{
		if(player.isGM())
			return 2;
		if(getCastle() != null && getCastle().getId() > 0 && player.getClan() != null)
		{
			SiegeEvent<?, ?> siegeEvent = getCastle().getSiegeEvent();
			if(siegeEvent != null && siegeEvent.isInProgress())
				return 1;
			if(getCastle().getOwnerId() == player.getClanId())
				return 2;
		}
		return 0;
	}
}
