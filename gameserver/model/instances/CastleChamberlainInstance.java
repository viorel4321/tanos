package l2s.gameserver.model.instances;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.CastleDamageZoneDAO;
import l2s.gameserver.dao.CastleDoorUpgradeDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.CastleSiegeInfo;
import l2s.gameserver.network.l2.s2c.ExShowCropInfo;
import l2s.gameserver.network.l2.s2c.ExShowCropSetting;
import l2s.gameserver.network.l2.s2c.ExShowManorDefaultInfo;
import l2s.gameserver.network.l2.s2c.ExShowSeedInfo;
import l2s.gameserver.network.l2.s2c.ExShowSeedSetting;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.tables.DoorTable;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class CastleChamberlainInstance extends ResidenceManager
{
	public CastleChamberlainInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void setDialogs()
	{
		_mainDialog = "castle/chamberlain/chamberlain.htm";
		_failDialog = "castle/chamberlain/chamberlain-notlord.htm";
		_siegeDialog = _mainDialog;
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		final int condition = getCond(player);
		if(condition != 2)
			return;
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		String val = "";
		if(st.countTokens() >= 1)
			val = st.nextToken();
		final Castle castle = getCastle();
		if(actualCommand.equalsIgnoreCase("viewSiegeInfo"))
		{
			if(!isHaveRigths(player, 131072))
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			player.sendPacket(new CastleSiegeInfo(castle, player));
		}
		else if(actualCommand.equalsIgnoreCase("ManageTreasure"))
		{
			if(!player.isClanLeader())
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
			html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
			html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
			html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("TakeTreasure"))
		{
			if(!player.isClanLeader())
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(!val.equals(""))
			{
				final int treasure = Integer.parseInt(val);
				if(castle.getTreasury() < treasure)
				{
					final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
					html2.setFile("castle/chamberlain/chamberlain-havenottreasure.htm");
					html2.replace("%Treasure%", String.valueOf(castle.getTreasury()));
					html2.replace("%Requested%", String.valueOf(treasure));
					player.sendPacket(html2);
					return;
				}
				if(treasure > 0)
				{
					castle.addToTreasuryNoTax(-treasure, false, false);
					player.addAdena(treasure);
				}
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
			html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
			html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
			html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("PutTreasure"))
		{
			if(!val.equals(""))
			{
				final int treasure = Integer.parseInt(val);
				if(treasure > player.getAdena())
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
				if(treasure > 0)
				{
					castle.addToTreasuryNoTax(treasure, false, false);
					player.reduceAdena(treasure, true);
				}
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
			html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
			html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
			html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("manor"))
		{
			if(!isHaveRigths(player, 65536))
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			String filename = "";
			if(CastleManorManager.getInstance().isDisabled())
				filename = "npcdefault.htm";
			else
			{
				final int cmd = Integer.parseInt(val);
				switch(cmd)
				{
					case 0:
					{
						filename = "castle/chamberlain/manor/manor.htm";
						break;
					}
					case 4:
					{
						filename = "castle/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
						break;
					}
					default:
					{
						filename = "castle/chamberlain/chamberlain-no.htm";
						break;
					}
				}
			}
			if(filename.length() > 0)
			{
				final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
				html2.setFile(filename);
				player.sendPacket(html2);
			}
		}
		else if(actualCommand.startsWith("manor_menu_select"))
		{
			if(!isHaveRigths(player, 65536))
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(CastleManorManager.getInstance().isUnderMaintenance())
			{
				player.sendPacket(Msg.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
				player.sendActionFailed();
				return;
			}
			final String params = actualCommand.substring(actualCommand.indexOf("?") + 1);
			final StringTokenizer str = new StringTokenizer(params, "&");
			final int ask = Integer.parseInt(str.nextToken().split("=")[1]);
			final int state = Integer.parseInt(str.nextToken().split("=")[1]);
			final int time = Integer.parseInt(str.nextToken().split("=")[1]);
			int castleId;
			if(state == -1)
				castleId = castle.getId();
			else
				castleId = state;
			switch(ask)
			{
				case 3:
				{
					if(time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowSeedInfo(castleId, null));
						break;
					}
					player.sendPacket(new ExShowSeedInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getSeedProduction(time)));
					break;
				}
				case 4:
				{
					if(time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowCropInfo(castleId, null));
						break;
					}
					player.sendPacket(new ExShowCropInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getCropProcure(time)));
					break;
				}
				case 5:
				{
					player.sendPacket(new ExShowManorDefaultInfo());
					break;
				}
				case 7:
				{
					if(castle.isNextPeriodApproved())
					{
						player.sendPacket(Msg.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
						break;
					}
					player.sendPacket(new ExShowSeedSetting(castle.getId()));
					break;
				}
				case 8:
				{
					if(castle.isNextPeriodApproved())
					{
						player.sendPacket(Msg.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
						break;
					}
					player.sendPacket(new ExShowCropSetting(castle.getId()));
					break;
				}
			}
		}
		else if(actualCommand.equalsIgnoreCase("operate_door"))
		{
			if(!isHaveRigths(player, 32768))
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}

			SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
			if(siegeEvent == null) {
				// TODO: Message??
				return;
			}

			if(siegeEvent.isInProgress())
			{
				this.showChatWindow(player, "residence2/castle/chamberlain_saius021.htm", new Object[0]);
				return;
			}

			if(!val.equals(""))
			{
				final boolean open = Integer.parseInt(val) == 1;
				while(st.hasMoreTokens())
				{
					final DoorInstance door = DoorTable.getInstance().getDoor(Integer.parseInt(st.nextToken()));
					if(open)
						door.openMe();
					else
						door.closeMe();
				}
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/" + getTemplate().npcId + "-d.htm");
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("tax_set"))
		{
			if(!isHaveRigths(player, 1048576))
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(!val.equals(""))
			{
				int maxTax = 15;
				if(Config.ALLOW_SEVEN_SIGNS)
				{
					if(SevenSigns.getInstance().getSealOwner(3) == 1)
						maxTax = 5;
					else if(SevenSigns.getInstance().getSealOwner(3) == 2)
						maxTax = 25;
				}

				final int tax = Integer.parseInt(val);
				if(tax < 0 || tax > maxTax)
				{
					final NpcHtmlMessage html3 = new NpcHtmlMessage(player, this);
					html3.setFile("castle/chamberlain/chamberlain-hightax.htm");
					html3.replace("%CurrentTax%", String.valueOf(castle.getTaxPercent()));
					player.sendPacket(html3);
					return;
				}
				castle.setTaxPercent(player, tax);
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-settax.htm");
			html.replace("%CurrentTax%", String.valueOf(castle.getTaxPercent()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("upgrade_castle"))
		{
			if(!checkSiegeFunctions(player))
				return;
			this.showChatWindow(player, "castle/chamberlain/chamberlain-upgrades.htm", new Object[0]);
		}
		else if(actualCommand.equalsIgnoreCase("reinforce"))
		{
			if(!checkSiegeFunctions(player))
				return;
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/doorStrengthen-" + castle.getName() + ".htm");
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("trap_select"))
		{
			if(!checkSiegeFunctions(player))
				return;
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/trap_select-" + castle.getName() + ".htm");
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("buy_trap"))
		{
			if(!checkSiegeFunctions(player))
				return;

			SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
			if(siegeEvent == null) {
				// TODO: Message??
				return;
			}

			if(siegeEvent.getObjects("bought_zones").contains(val))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("castle/chamberlain/trapAlready.htm");
				player.sendPacket(html);
				return;
			}
			final List<CastleDamageZoneObject> objects = siegeEvent.getObjects(val);
			long price = 0L;
			for(final CastleDamageZoneObject o : objects)
				price += o.getPrice();
			price = modifyPrice(price);
			if(player.getClan().getAdenaCount() < price)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			player.getClan().getWarehouse().destroyItem(57, price);
			siegeEvent.addObject("bought_zones", val);
			CastleDamageZoneDAO.getInstance().insert(castle, val);
			final NpcHtmlMessage html4 = new NpcHtmlMessage(player, this);
			html4.setFile("castle/chamberlain/trapSuccess.htm");
			player.sendPacket(html4);
		}
		else if(actualCommand.equalsIgnoreCase("door_manage"))
		{
			if(!isHaveRigths(player, 32768))
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}

			SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
			if(siegeEvent == null) {
				// TODO: Message??
				return;
			}

			if(siegeEvent.isInProgress())
			{
				this.showChatWindow(player, "residence2/castle/chamberlain_saius021.htm", new Object[0]);
				return;
			}

			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/doorManage.htm");
			html.replace("%id%", val);
			html.replace("%type%", st.nextToken());
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("upgrade_door_confirm"))
		{
			if(!isHaveRigths(player, 131072))
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			final int id = Integer.parseInt(val);
			final int type = Integer.parseInt(st.nextToken());
			final int level = Integer.parseInt(st.nextToken());
			final long price2 = getDoorCost(type, level);
			final NpcHtmlMessage html5 = new NpcHtmlMessage(player, this);
			html5.setFile("castle/chamberlain/doorConfirm.htm");
			html5.replace("%id%", String.valueOf(id));
			html5.replace("%level%", String.valueOf(level));
			html5.replace("%type%", String.valueOf(type));
			html5.replace("%price%", String.valueOf(price2));
			player.sendPacket(html5);
		}
		else if(actualCommand.equalsIgnoreCase("upgrade_door"))
		{
			if(!checkSiegeFunctions(player))
				return;

			final SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
			if(siegeEvent == null) {
				// TODO: Message??
				return;
			}

			final int id = Integer.parseInt(val);
			final int type = Integer.parseInt(st.nextToken());
			final int level = Integer.parseInt(st.nextToken());
			final long price2 = getDoorCost(type, level);
			final List<DoorObject> doorObjects = siegeEvent.getObjects("doors");

			DoorObject targetDoorObject = null;
			for(final DoorObject o2 : doorObjects)
				if(o2.getUId() == id)
				{
					targetDoorObject = o2;
					break;
				}

			final DoorInstance door2 = targetDoorObject.getDoor();
			final int upgradeHp = (door2.getMaxHp() - door2.getUpgradeHp()) * level - door2.getMaxHp();
			if(price2 == 0L || upgradeHp < 0)
			{
				player.sendMessage(new CustomMessage("common.Error"));
				return;
			}

			if(door2.getUpgradeHp() >= upgradeHp)
			{
				final int oldLevel = door2.getUpgradeHp() / (door2.getMaxHp() - door2.getUpgradeHp()) + 1;
				final NpcHtmlMessage html6 = new NpcHtmlMessage(player, this);
				html6.setFile("castle/chamberlain/doorAlready.htm");
				html6.replace("%level%", String.valueOf(oldLevel));
				player.sendPacket(html6);
				return;
			}

			if(player.getClan().getAdenaCount() < price2)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}

			player.getClan().getWarehouse().destroyItem(57, price2);
			targetDoorObject.setUpgradeValue(castle.getSiegeEvent(), upgradeHp);
			CastleDoorUpgradeDAO.getInstance().insert(door2.getDoorId(), upgradeHp);
		}
		else if(actualCommand.equalsIgnoreCase("report"))
		{
			if(!isHaveRigths(player, 262144))
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}

			String ssq_period;
			if(Config.ALLOW_SEVEN_SIGNS)
			{
				if(SevenSigns.getInstance().getCurrentPeriod() == 1)
					ssq_period = "Competition";
				else if(SevenSigns.getInstance().getCurrentPeriod() == 3)
					ssq_period = "Effective sealing";
				else
					ssq_period = "Ready";
			}
			else
				ssq_period = "Disabled";

			final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
			html2.setFile("castle/chamberlain/chamberlain-report.htm");
			html2.replace("%FeudName%", castle.getName());
			html2.replace("%CharClan%", player.getClan().getName());
			html2.replace("%CharName%", player.getName());
			html2.replace("%SSPeriod%", ssq_period);
			html2.replace("%Avarice%", getSealOwner(1));
			html2.replace("%Revelation%", getSealOwner(2));
			html2.replace("%Strife%", getSealOwner(3));
			player.sendPacket(html2);
		}
		else if(actualCommand.equalsIgnoreCase("Crown"))
		{
			if(!player.isClanLeader())
			{
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(player.getInventory().getItemByItemId(6841) == null)
			{
				final ItemInstance CrownItem = ItemTable.getInstance().createItem(6841);
				player.getInventory().addItem(CrownItem);
				final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
				html2.setFile("castle/chamberlain/chamberlain-givecrown.htm");
				html2.replace("%CharName%", player.getName());
				html2.replace("%FeudName%", castle.getName());
				player.sendPacket(html2);
			}
			else
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("castle/chamberlain/alreadyhavecrown.htm");
				player.sendPacket(html);
			}
		}
		else if(actualCommand.equalsIgnoreCase("manageFunctions"))
		{
			if((player.getClanPrivileges() & 0x400000) != 0x400000)
				this.showChatWindow(player, "residence2/castle/chamberlain_saius063.htm", new Object[0]);
			else
				this.showChatWindow(player, "residence2/castle/chamberlain_saius065.htm", new Object[0]);
		}
		else if(actualCommand.equalsIgnoreCase("manageSiegeFunctions"))
		{
			if((player.getClanPrivileges() & 0x400000) != 0x400000)
				this.showChatWindow(player, "residence2/castle/chamberlain_saius063.htm", new Object[0]);
			else if(Config.ALLOW_SEVEN_SIGNS && SevenSigns.getInstance().getCurrentPeriod() != 3)
				this.showChatWindow(player, "residence2/castle/chamberlain_saius068.htm", new Object[0]);
			else
				this.showChatWindow(player, "residence2/castle/chamberlain_saius052.htm", new Object[0]);
		}
		else if(actualCommand.equalsIgnoreCase("items"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("residence2/castle/chamberlain_saius064.htm");
			html.replace("%npcId%", String.valueOf(getNpcId()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("default"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain.htm");
			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	protected int getCond(final Player player)
	{
		if(player.isGM())
			return 2;
		final Residence castle = getCastle();
		if(castle != null && castle.getId() > 0 && player.getClan() != null)
		{
			SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
			if(siegeEvent != null && siegeEvent.isInProgress())
				return 1;

			if(castle.getOwnerId() == player.getClanId())
			{
				if(player.isClanLeader())
					return 2;
				if(isHaveRigths(player, 32768) || isHaveRigths(player, 65536) || isHaveRigths(player, 131072) || isHaveRigths(player, 262144) || isHaveRigths(player, 524288) || isHaveRigths(player, 1048576) || isHaveRigths(player, 2097152) || isHaveRigths(player, 4194304))
					return 2;
			}
		}
		return 0;
	}

	private String getSealOwner(final int seal)
	{
		if(Config.ALLOW_SEVEN_SIGNS)
		{
			switch(SevenSigns.getInstance().getSealOwner(seal))
			{
				case 1:
					return "Evening";
				case 2:
					return "Dawn";
				default:
					return "None belongs";
			}
		}
		else
			return "Disabled";
	}

	private long getDoorCost(final int type, final int level)
	{
		int price = 0;
		switch(type)
		{
			case 1:
			{
				switch(level)
				{
					case 2:
					{
						price = 3000000;
						break;
					}
					case 3:
					{
						price = 4000000;
						break;
					}
					case 5:
					{
						price = 5000000;
						break;
					}
				}
				break;
			}
			case 2:
			{
				switch(level)
				{
					case 2:
					{
						price = 750000;
						break;
					}
					case 3:
					{
						price = 900000;
						break;
					}
					case 5:
					{
						price = 1000000;
						break;
					}
				}
				break;
			}
			case 3:
			{
				switch(level)
				{
					case 2:
					{
						price = 1600000;
						break;
					}
					case 3:
					{
						price = 1800000;
						break;
					}
					case 5:
					{
						price = 2000000;
						break;
					}
				}
				break;
			}
		}
		return modifyPrice(price);
	}

	private static long modifyPrice(long price)
	{
		if(Config.ALLOW_SEVEN_SIGNS)
		{
			final int SSQ_DawnFactor_door = 80;
			final int SSQ_DrawFactor_door = 100;
			final int SSQ_DuskFactor_door = 300;
			switch(SevenSigns.getInstance().getSealOwner(3))
			{
				case 1:
				{
					price = price * SSQ_DuskFactor_door / 100L;
					break;
				}
				case 2:
				{
					price = price * SSQ_DawnFactor_door / 100L;
					break;
				}
				default:
				{
					price = price * SSQ_DrawFactor_door / 100L;
					break;
				}
			}
		}
		return price;
	}

	@Override
	protected Residence getResidence()
	{
		return getCastle();
	}

	@Override
	public L2GameServerPacket decoPacket()
	{
		return null;
	}

	@Override
	protected int getPrivUseFunctions()
	{
		return 262144;
	}

	@Override
	protected int getPrivSetFunctions()
	{
		return 4194304;
	}

	@Override
	protected int getPrivDismiss()
	{
		return 524288;
	}

	@Override
	protected int getPrivDoors()
	{
		return 32768;
	}

	private boolean checkSiegeFunctions(final Player player)
	{
		final Castle castle = getCastle();
		if((player.getClanPrivileges() & 0x20000) != 0x20000)
		{
			player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}

		SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
		if(siegeEvent != null && siegeEvent.isInProgress())
		{
			this.showChatWindow(player, "residence2/castle/chamberlain_saius021.htm", new Object[0]);
			return false;
		}
		return true;
	}
}
