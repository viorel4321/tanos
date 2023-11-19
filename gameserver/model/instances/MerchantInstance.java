package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.TradeController;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.BuyList;
import l2s.gameserver.network.l2.s2c.SellList;
import l2s.gameserver.network.l2.s2c.ShopPreviewList;
import l2s.gameserver.templates.npc.NpcTemplate;

public class MerchantInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;
	private static Logger _log;

	public MerchantInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		String temp = "merchant/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;
		temp = "teleporter/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;
		return super.getHtmlPath(npcId, val, player);
	}

	private void showWearWindow(final Player player, final int val)
	{
		if(!player.getPlayerAccess().UseShop)
			return;
		player.tempInventoryDisable();
		final TradeController.NpcTradeList list = TradeController.getInstance().getBuyList(val);
		if(list != null)
		{
			final ShopPreviewList bl = new ShopPreviewList(list, player.getAdena(), player.expertiseIndex);
			player.sendPacket(bl);
		}
		else
		{
			MerchantInstance._log.warn("no buylist with id:" + val);
			player.sendActionFailed();
		}
	}

	protected void showBuyWindow(final Player player, final int listId, final boolean tax)
	{
		if(!player.getPlayerAccess().UseShop)
			return;
		double taxRate = 0.0;
		if(tax && getCastle() != null)
			taxRate = getCastle().getTaxRate();
		player.tempInventoryDisable();
		final TradeController.NpcTradeList list = TradeController.getInstance().getBuyList(listId);
		if(list != null && list.getNpcId() == getNpcId())
		{
			player.setLastNpcId(getNpcId());
			player.setBuyListId(listId);
			final BuyList bl = new BuyList(list, player.getAdena(), taxRate);
			player.sendPacket(bl);
		}
		else
		{
			MerchantInstance._log.warn("[L2MerchantInstance] possible client hacker: " + player.toString() + " attempting to buy from GM shop! < Ban him!");
			MerchantInstance._log.warn("buylist id:" + listId + " / list_npc = " + list.getNpcId() + " / npc = " + getNpcId());
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

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		if(actualCommand.equalsIgnoreCase("Buy"))
		{
			if(st.countTokens() < 1)
				return;
			final int val = Integer.parseInt(st.nextToken());
			showBuyWindow(player, val, true);
		}
		else if(actualCommand.equalsIgnoreCase("Sell"))
			showSellWindow(player);
		else if(actualCommand.equalsIgnoreCase("Wear") && Config.WEAR_TEST_ENABLED)
		{
			if(st.countTokens() < 1)
				return;
			final int val = Integer.parseInt(st.nextToken());
			showWearWindow(player, val);
		}
		else if(actualCommand.equalsIgnoreCase("Multisell"))
		{
			if(st.countTokens() < 1)
				return;
			final int val = Integer.parseInt(st.nextToken());
			player.setLastNpcId(getNpcId());
			MultiSellHolder.getInstance().SeparateAndSend(val, player, getCastle() != null ? getCastle().getTaxRate() : 0.0);
		}
		else
			super.onBypassFeedback(player, command);
	}

	static
	{
		MerchantInstance._log = LoggerFactory.getLogger(MerchantInstance.class);
	}
}
