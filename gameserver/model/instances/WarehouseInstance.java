package l2s.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.PackageToList;
import l2s.gameserver.network.l2.s2c.WareHouseDepositList;
import l2s.gameserver.network.l2.s2c.WareHouseWithdrawList;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class WarehouseInstance extends NpcInstance
{
	private static Logger _log;

	public WarehouseInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(final int npcId, final int val, final Player player)
	{
		String pom = "";
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "warehouse/" + pom + ".htm";
	}

	private void showRetrieveWindow(final Player player, final int val)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		if(Config.DEBUG)
			WarehouseInstance._log.info("Showing stored items");
		player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.PRIVATE, ItemClass.values()[val]));
	}

	private void showDepositWindow(final Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		player.tempInventoryDisable();
		if(Config.DEBUG)
			WarehouseInstance._log.info("Showing items to deposit");
		player.sendPacket(new WareHouseDepositList(player, Warehouse.WarehouseType.PRIVATE));
		player.sendActionFailed();
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
			player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
			player.sendActionFailed();
			return;
		}
		player.tempInventoryDisable();
		if(Config.DEBUG)
			WarehouseInstance._log.info("Showing items to deposit - clan");
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
		final Clan _clan = player.getClan();
		if(_clan.getLevel() == 0)
		{
			player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
			player.sendActionFailed();
			return;
		}
		if((player.getClanPrivileges() & 0x8) == 0x8)
		{
			player.tempInventoryDisable();
			player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.CLAN, ItemClass.values()[val]));
		}
		else
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
			player.sendActionFailed();
		}
	}

	private void showWithdrawWindowFreight(final Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		if(Config.DEBUG)
			WarehouseInstance._log.info("Showing freightened items");
		final Warehouse list = player.getFreight();
		if(list != null)
			player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.FREIGHT, ItemClass.ALL));
		else if(Config.DEBUG)
			WarehouseInstance._log.info("no items freightened");
		player.sendActionFailed();
	}

	private void showDepositWindowFreight(final Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		if(Config.DEBUG)
			WarehouseInstance._log.info("Showing destination chars to freight - char src: " + player.getName());
		player.sendPacket(new PackageToList(player));
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		player.closeEnchant();
		if(command.startsWith("WithdrawP"))
		{
			if(!Config.ALLOW_WAREHOUSE)
			{
				player.sendActionFailed();
				return;
			}
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
		{
			if(!Config.ALLOW_WAREHOUSE)
			{
				player.sendActionFailed();
				return;
			}
			showDepositWindow(player);
		}
		else if(command.startsWith("WithdrawC"))
		{
			if(!Config.ALLOW_WAREHOUSE)
			{
				player.sendActionFailed();
				return;
			}
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
		{
			if(!Config.ALLOW_WAREHOUSE)
			{
				player.sendActionFailed();
				return;
			}
			showDepositWindowClan(player);
		}
		else if(command.startsWith("WithdrawF"))
		{
			if(!Config.ALLOW_FREIGHT)
			{
				player.sendActionFailed();
				return;
			}
			showWithdrawWindowFreight(player);
		}
		else if(command.startsWith("DepositF"))
		{
			if(!Config.ALLOW_FREIGHT)
			{
				player.sendActionFailed();
				return;
			}
			showDepositWindowFreight(player);
		}
		else
			super.onBypassFeedback(player, command);
	}

	static
	{
		WarehouseInstance._log = LoggerFactory.getLogger(WarehouseInstance.class);
	}
}
