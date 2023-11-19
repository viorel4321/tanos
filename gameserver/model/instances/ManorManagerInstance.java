package l2s.gameserver.model.instances;

import java.util.List;
import java.util.StringTokenizer;

import l2s.gameserver.TradeController;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.BuyList;
import l2s.gameserver.network.l2.s2c.BuyListSeed;
import l2s.gameserver.network.l2.s2c.ExShowCropInfo;
import l2s.gameserver.network.l2.s2c.ExShowManorDefaultInfo;
import l2s.gameserver.network.l2.s2c.ExShowProcureCropDetail;
import l2s.gameserver.network.l2.s2c.ExShowSeedInfo;
import l2s.gameserver.network.l2.s2c.ExShowSellCropList;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ManorManagerInstance extends MerchantInstance
{
	public ManorManagerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(this != player.getTarget())
			player.setTarget(this);
		else if(!this.isInRange(player, 150L))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(Msg.ActionFail);
		}
		else
		{
			if(CastleManorManager.getInstance().isDisabled())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("npcdefault.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else if(!player.isGM() && getCastle() != null && getCastle().getId() > 0 && player.getClan() != null && getCastle().getOwnerId() == player.getClanId() && player.isClanLeader())
				showMessageWindow(player, "manager-lord.htm");
			else
				showMessageWindow(player, "manager.htm");
			player.sendPacket(Msg.ActionFail);
		}
	}

	@Override
	protected void showBuyWindow(final Player player, final int listId, final boolean tax)
	{
		player.setLastNpc(this);
		final double taxRate = 0.0;
		player.tempInventoryDisable();
		final TradeController.NpcTradeList list = TradeController.getInstance().getBuyList(listId);
		if(list != null)
		{
			player.setLastNpcId(getNpcId());
			player.setBuyListId(listId);
			final BuyList bl = new BuyList(list, player.getAdena(), taxRate);
			player.sendPacket(bl);
		}
		player.sendPacket(Msg.ActionFail);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		player.setLastNpc(this);
		if(command.startsWith("manor_menu_select"))
		{
			if(CastleManorManager.getInstance().isUnderMaintenance())
			{
				player.sendPacket(Msg.ActionFail);
				player.sendPacket(Msg.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
				return;
			}
			final String params = command.substring(command.indexOf("?") + 1);
			final StringTokenizer st = new StringTokenizer(params, "&");
			final int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			final int state = Integer.parseInt(st.nextToken().split("=")[1]);
			final int time = Integer.parseInt(st.nextToken().split("=")[1]);
			int castleId;
			if(state == -1)
				castleId = getCastle().getId();
			else
				castleId = state;
			switch(ask)
			{
				case 1:
				{
					if(castleId != getCastle().getId())
					{
						player.sendPacket(new SystemMessage(1605));
						break;
					}
					final TradeController.NpcTradeList tradeList = new TradeController.NpcTradeList(0);
					final List<CastleManorManager.SeedProduction> seeds = getCastle().getSeedProduction(0);
					for(final CastleManorManager.SeedProduction s : seeds)
					{
						final TradeItem item = new TradeItem();
						item.setItemId(s.getId());
						item.setOwnersPrice(s.getPrice());
						item.setCount(s.getCanProduce());
						if(item.getCount() > 0 && item.getOwnersPrice() > 0)
							tradeList.addItem(item);
					}
					final BuyListSeed bl = new BuyListSeed(tradeList, castleId, player.getAdena());
					player.sendPacket(bl);
					break;
				}
				case 2:
				{
					player.sendPacket(new ExShowSellCropList(player, castleId, getCastle().getCropProcure(0)));
					break;
				}
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
				case 6:
				{
					showBuyWindow(player, Integer.parseInt("3" + getNpcId()), false);
					break;
				}
				case 9:
				{
					player.sendPacket(new ExShowProcureCropDetail(state));
					break;
				}
			}
		}
		else if(command.startsWith("help"))
		{
			final StringTokenizer st2 = new StringTokenizer(command, " ");
			st2.nextToken();
			final String filename = "manor_client_help00" + st2.nextToken() + ".htm";
			showMessageWindow(player, filename);
		}
		else
			super.onBypassFeedback(player, command);
	}

	public String getHtmlPath()
	{
		return "manormanager/";
	}

	@Override
	public String getHtmlPath(final int npcId, final int val, final Player player)
	{
		return "manormanager/manager.htm";
	}

	private void showMessageWindow(final Player player, final String filename)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(this.getHtmlPath() + filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
