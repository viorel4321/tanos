package l2s.gameserver.model.instances;

import java.util.List;
import java.util.StringTokenizer;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.TeleportLocation;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.WareHouseDepositList;
import l2s.gameserver.network.l2.s2c.WareHouseWithdrawList;
import l2s.gameserver.tables.DoorTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.TimeUtils;

public abstract class ResidenceManager extends MerchantInstance
{
	private static final long serialVersionUID = 1L;
	protected static final int COND_FAIL = 0;
	protected static final int COND_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	protected String _siegeDialog;
	protected String _mainDialog;
	protected String _failDialog;
	protected int[] _doors;

	public ResidenceManager(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		setDialogs();
		_doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
	}

	protected void setDialogs()
	{
		_siegeDialog = getTemplate().getAIParams().getString("siege_dialog", "npcdefault.htm");
		_mainDialog = getTemplate().getAIParams().getString("main_dialog", "npcdefault.htm");
		_failDialog = getTemplate().getAIParams().getString("fail_dialog", "npcdefault.htm");
	}

	protected abstract Residence getResidence();

	protected abstract L2GameServerPacket decoPacket();

	protected abstract int getPrivUseFunctions();

	protected abstract int getPrivSetFunctions();

	protected abstract int getPrivDismiss();

	protected abstract int getPrivDoors();

	public void broadcastDecoInfo()
	{
		final L2GameServerPacket decoPacket = decoPacket();
		if(decoPacket == null)
			return;
		for(final Player player : World.getAroundPlayers(this))
			player.sendPacket(decoPacket);
	}

	protected int getCond(final Player player)
	{
		final Residence residence = getResidence();
		final Clan residenceOwner = residence.getOwner();
		if(residenceOwner == null || player.getClan() != residenceOwner)
			return 0;

		SiegeEvent<?, ?> siegeEvent = residence.getSiegeEvent();
		if(siegeEvent != null && siegeEvent.isInProgress())
			return 1;
		return 2;
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		String filename = null;
		final int cond = getCond(player);
		switch(cond)
		{
			case 2:
			{
				filename = _mainDialog;
				break;
			}
			case 1:
			{
				filename = _siegeDialog;
				break;
			}
			case 0:
			{
				filename = _failDialog;
				break;
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		String val = "";
		if(st.countTokens() >= 1)
			val = st.nextToken();
		final int cond = getCond(player);
		switch(cond)
		{
			case 1:
			{
				this.showChatWindow(player, _siegeDialog, new Object[0]);
				break;
			}
			case 0:
			{
				this.showChatWindow(player, _failDialog, new Object[0]);
				break;
			}
			default:
			{
				if(actualCommand.equalsIgnoreCase("banish"))
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("residence/Banish.htm");
					sendHtmlMessage(player, html);
				}
				else if(actualCommand.equalsIgnoreCase("banish_foreigner"))
				{
					if(!isHaveRigths(player, getPrivDismiss()))
					{
						player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
						return;
					}
					getResidence().banishForeigner();
					return;
				}
				else if(actualCommand.equalsIgnoreCase("Buy"))
				{
					if(val.equals(""))
						return;
					showBuyWindow(player, Integer.valueOf(val), true);
				}
				else if(actualCommand.equalsIgnoreCase("manage_vault"))
				{
					if(!Config.ALLOW_WAREHOUSE)
					{
						player.sendActionFailed();
						return;
					}
					if(val.equalsIgnoreCase("deposit"))
						showDepositWindowClan(player);
					else if(val.equalsIgnoreCase("withdraw"))
					{
						final int value = Integer.valueOf(st.nextToken());
						if(value == 99)
						{
							final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
							html2.setFile("residence/clan.htm");
							html2.replace("%npcname%", getName());
							player.sendPacket(html2);
						}
						else
							showWithdrawWindowClan(player, value);
					}
					else
					{
						final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/vault.htm");
						sendHtmlMessage(player, html);
					}
					return;
				}
				else if(actualCommand.equalsIgnoreCase("door"))
					this.showChatWindow(player, "residence/door.htm", new Object[0]);
				else if(actualCommand.equalsIgnoreCase("openDoors"))
				{
					if(isHaveRigths(player, getPrivDoors()))
					{
						for(final int i : _doors)
							DoorTable.getInstance().getDoor(i).openMe();
						this.showChatWindow(player, "residence/door.htm", new Object[0]);
					}
					else
						player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				}
				else if(actualCommand.equalsIgnoreCase("closeDoors"))
				{
					if(isHaveRigths(player, getPrivDoors()))
					{
						for(final int i : _doors)
							DoorTable.getInstance().getDoor(i).closeMe();
						this.showChatWindow(player, "residence/door.htm", new Object[0]);
					}
					else
						player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				}
				else if(actualCommand.equalsIgnoreCase("functions"))
				{
					if(!isHaveRigths(player, getPrivUseFunctions()))
					{
						player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
						return;
					}
					if(val.equalsIgnoreCase("tele"))
					{
						if(!getResidence().isFunctionActive(1))
						{
							final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							html.setFile("residence/teleportNotActive.htm");
							sendHtmlMessage(player, html);
							return;
						}
						final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/teleport.htm");
						final String template = "<a action=\"bypass -h scripts_Util:RGK %loc% %price% @811;%name%\">%name% - %price% Adena</a><br1>";
						String teleport_list = "";
						for(final TeleportLocation loc : getResidence().getFunction(1).getTeleports())
							teleport_list += template.replaceAll("%loc%", loc.getX() + " " + loc.getY() + " " + loc.getZ()).replaceAll("%price%", String.valueOf(loc.getPrice())).replaceAll("%name%", loc.getName());
						html.replace("%teleList%", teleport_list);
						sendHtmlMessage(player, html);
					}
					else if(val.equalsIgnoreCase("item_creation"))
					{
						if(!getResidence().isFunctionActive(2))
						{
							final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							html.setFile("residence/itemNotActive.htm");
							sendHtmlMessage(player, html);
							return;
						}
						final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/item.htm");
						String template = "<button value=\"Buy Item\" action=\"bypass -h npc_%objectId%_Buy %id%\" width=95 height=21 back=\"L2UI_ch3.bigbutton_down\" fore=\"L2UI_ch3.bigbutton\">";
						template = template.replaceAll("%id%", String.valueOf(getResidence().getFunction(2).getBuylist()[1])).replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%itemList%", template);
						sendHtmlMessage(player, html);
					}
					else if(val.equalsIgnoreCase("support"))
					{
						if(!getResidence().isFunctionActive(6))
						{
							final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							html.setFile("residence/supportNotActive.htm");
							sendHtmlMessage(player, html);
							return;
						}
						final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/support.htm");
						final Object[][] allBuffs = getResidence().getFunction(6).getBuffs();
						final StringBuilder support_list = new StringBuilder(allBuffs.length * 50);
						int i = 0;
						for(final Object[] buff : allBuffs)
						{
							final Skill s = (Skill) buff[0];
							support_list.append("<a action=\"bypass -h npc_%objectId%_support ");
							support_list.append(String.valueOf(s.getId()));
							support_list.append(" ");
							support_list.append(String.valueOf(s.getLevel()));
							support_list.append("\">");
							support_list.append(s.getName());
							support_list.append(" Lv.");
							support_list.append(String.valueOf(s.getDisplayLevel()));
							support_list.append("</a><br1>");
							if(++i % 5 == 0)
								support_list.append("<br>");
						}
						html.replace("%magicList%", support_list.toString());
						html.replace("%mp%", String.valueOf(Math.round(getCurrentMp())));
						html.replace("%all%", Config.ALT_CH_ALL_BUFFS ? "<a action=\"bypass -h npc_%objectId%_support all\">Give all</a><br1><a action=\"bypass -h npc_%objectId%_support allW\">Give warrior</a><br1><a action=\"bypass -h npc_%objectId%_support allM\">Give mystic</a><br>" : "");
						sendHtmlMessage(player, html);
					}
					else if(val.equalsIgnoreCase("back"))
						this.showChatWindow(player, 0, new Object[0]);
					else
					{
						final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/functions.htm");
						if(getResidence().isFunctionActive(5))
							html.replace("%xp_regen%", String.valueOf(getResidence().getFunction(5).getLevel()) + "%");
						else
							html.replace("%xp_regen%", "0%");
						if(getResidence().isFunctionActive(3))
							html.replace("%hp_regen%", String.valueOf(getResidence().getFunction(3).getLevel()) + "%");
						else
							html.replace("%hp_regen%", "0%");
						if(getResidence().isFunctionActive(4))
							html.replace("%mp_regen%", String.valueOf(getResidence().getFunction(4).getLevel()) + "%");
						else
							html.replace("%mp_regen%", "0%");
						sendHtmlMessage(player, html);
					}
				}
				else if(actualCommand.equalsIgnoreCase("manage"))
				{
					if(!isHaveRigths(player, getPrivSetFunctions()))
					{
						player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
						return;
					}
					if(val.equalsIgnoreCase("recovery"))
					{
						if(st.countTokens() >= 1)
						{
							val = st.nextToken();
							boolean success = true;
							if(val.equalsIgnoreCase("hp"))
								success = getResidence().updateFunctions(3, Integer.valueOf(st.nextToken()));
							else if(val.equalsIgnoreCase("mp"))
								success = getResidence().updateFunctions(4, Integer.valueOf(st.nextToken()));
							else if(val.equalsIgnoreCase("exp"))
								success = getResidence().updateFunctions(5, Integer.valueOf(st.nextToken()));
							if(!success)
								player.sendPacket(Msg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
							else
								broadcastDecoInfo();
						}
						showManageRecovery(player);
					}
					else if(val.equalsIgnoreCase("other"))
					{
						if(st.countTokens() >= 1)
						{
							val = st.nextToken();
							boolean success = true;
							if(val.equalsIgnoreCase("item"))
								success = getResidence().updateFunctions(2, Integer.valueOf(st.nextToken()));
							else if(val.equalsIgnoreCase("tele"))
								success = getResidence().updateFunctions(1, Integer.valueOf(st.nextToken()));
							else if(val.equalsIgnoreCase("support"))
								success = getResidence().updateFunctions(6, Integer.valueOf(st.nextToken()));
							if(!success)
								player.sendPacket(Msg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
							else
								broadcastDecoInfo();
						}
						showManageOther(player);
					}
					else if(val.equalsIgnoreCase("deco"))
					{
						if(st.countTokens() >= 1)
						{
							val = st.nextToken();
							boolean success = true;
							if(val.equalsIgnoreCase("platform"))
								success = getResidence().updateFunctions(8, Integer.valueOf(st.nextToken()));
							else if(val.equalsIgnoreCase("curtain"))
								success = getResidence().updateFunctions(7, Integer.valueOf(st.nextToken()));
							if(!success)
								player.sendPacket(Msg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
							else
								broadcastDecoInfo();
						}
						showManageDeco(player);
					}
					else if(val.equalsIgnoreCase("back"))
						this.showChatWindow(player, 0, new Object[0]);
					else
					{
						final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/manage.htm");
						sendHtmlMessage(player, html);
					}
					return;
				}
				else if(actualCommand.equalsIgnoreCase("support"))
				{
					if(!isHaveRigths(player, getPrivUseFunctions()))
					{
						player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
						return;
					}
					setTarget(player);
					if(val.equals(""))
						return;
					if(!getResidence().isFunctionActive(6))
						return;
					if(val.startsWith("all"))
					{
						for(final Object[] buff2 : getResidence().getFunction(6).getBuffs())
							if(!val.equals("allM") || buff2[1] != "W")
								if(!val.equals("allW") || buff2[1] != "M")
								{
									final Skill s2 = (Skill) buff2[0];
									if(!useSkill(s2.getId(), s2.getLevel(), player))
										break;
								}
					}
					else
					{
						final int skill_id = Integer.parseInt(val);
						int skill_lvl = 0;
						if(st.countTokens() >= 1)
							skill_lvl = Integer.parseInt(st.nextToken());
						useSkill(skill_id, skill_lvl, player);
					}
					onBypassFeedback(player, "functions support");
					return;
				}
				super.onBypassFeedback(player, command);
				break;
			}
		}
	}

	private boolean useSkill(final int id, final int level, final Player player)
	{
		final Skill skill = SkillTable.getInstance().getInfo(id, level);
		if(skill == null)
		{
			player.sendMessage("Invalid skill " + id);
			return true;
		}
		if(skill.getMpConsume() > getCurrentMp())
		{
			if(Config.RESIDENCE_BUFFS_COST_MP)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("residence/NeedCoolTime.htm");
				html.replace("%mp%", String.valueOf(Math.round(getCurrentMp())));
				sendHtmlMessage(player, html);
				return false;
			}
			this.setCurrentMp(getMaxMp());
		}
		altUseSkill(skill, player);
		return true;
	}

	private void sendHtmlMessage(final Player player, final NpcHtmlMessage html)
	{
		html.replace("%npcname%", getName());
		player.sendPacket(html);
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
		final Clan _clan = player.getClan();
		if(_clan.getLevel() == 0)
		{
			player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE, Msg.ActionFail);
			return;
		}
		if(isHaveRigths(player, 8))
		{
			player.tempInventoryDisable();
			player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.CLAN, ItemClass.values()[val]));
		}
		else
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE, Msg.ActionFail);
	}

	private void replace(final NpcHtmlMessage html, final int type, final String replace1, final String replace2)
	{
		final boolean proc = type == 3 || type == 4 || type == 5;
		if(getResidence().isFunctionActive(type))
		{
			html.replace("%" + replace1 + "%", String.valueOf(getResidence().getFunction(type).getLevel()) + (proc ? "%" : ""));
			html.replace("%" + replace1 + "Price%", String.valueOf(getResidence().getFunction(type).getLease()));
			html.replace("%" + replace1 + "Date%", TimeUtils.toSimpleFormat(getResidence().getFunction(type).getEndTimeInMillis()));
		}
		else
		{
			html.replace("%" + replace1 + "%", "0");
			html.replace("%" + replace1 + "Price%", "0");
			html.replace("%" + replace1 + "Date%", "0");
		}
		if(getResidence().getFunction(type) != null && getResidence().getFunction(type).getLevels().size() > 0)
		{
			String out = "[<a action=\"bypass -h npc_%objectId%_manage " + replace2 + " " + replace1 + " 0\">Stop</a>]";
			for(final int level : getResidence().getFunction(type).getLevels())
				out = out + "[<a action=\"bypass -h npc_%objectId%_manage " + replace2 + " " + replace1 + " " + level + "\">" + level + (proc ? "%" : "") + "</a>]";
			html.replace("%" + replace1 + "Manage%", out);
		}
		else
			html.replace("%" + replace1 + "Manage%", "Not Available");
	}

	private void showManageRecovery(final Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("residence/edit_recovery.htm");
		replace(html, 5, "exp", "recovery");
		replace(html, 3, "hp", "recovery");
		replace(html, 4, "mp", "recovery");
		sendHtmlMessage(player, html);
	}

	private void showManageOther(final Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("residence/edit_other.htm");
		replace(html, 1, "tele", "other");
		replace(html, 6, "support", "other");
		replace(html, 2, "item", "other");
		sendHtmlMessage(player, html);
	}

	private void showManageDeco(final Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("residence/edit_deco.htm");
		replace(html, 7, "curtain", "deco");
		replace(html, 8, "platform", "deco");
		sendHtmlMessage(player, html);
	}

	protected boolean isHaveRigths(final Player player, final int rigthsToCheck)
	{
		return player.getClan() != null && (player.getClanPrivileges() & rigthsToCheck) == rigthsToCheck;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		final List<L2GameServerPacket> list = super.addPacketList(forPlayer, dropper);
		final L2GameServerPacket p = decoPacket();
		if(p != null)
			list.add(p);
		return list;
	}
}
