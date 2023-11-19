package l2s.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.InventoryUpdate;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Log;

public class EnchantBBSManager extends BaseBBSManager
{
	private static EnchantBBSManager _Instance;

	public static EnchantBBSManager getInstance()
	{
		if(EnchantBBSManager._Instance == null)
			EnchantBBSManager._Instance = new EnchantBBSManager();
		return EnchantBBSManager._Instance;
	}

	@Override
	public void parsecmd(final String command, final Player activeChar)
	{
		if(command.equals("_bbsechant"))
		{
			final String name = ItemTable.getInstance().getTemplate(Config.CB_ENCH_ITEM).getName();
			final StringBuilder sb = new StringBuilder("");
			sb.append("<table width=400>");
			final ItemInstance[] arr = activeChar.getInventory().getItems();
			final int len = arr.length;
			final boolean ru = activeChar.isLangRus();
			for(final ItemInstance _item : arr)
				if(!noEnch(_item))
				{
					sb.append(new StringBuilder("<tr><td height=" + (_item.getEnchantLevel() <= 0 ? "65" : "45") + "><img src=" + _item.getTemplate().getIcon() + " width=32 height=32></td><td height=65>"));
					sb.append(new StringBuilder("<font color=\"ffff00\">" + _item.getTemplate().getName() + " " + (_item.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font><br1><font color=3293F3>" + (ru ? "\u0417\u0430\u0442\u043e\u0447\u0435\u043d\u043e \u043d\u0430" : "Enchanted") + ": +" + _item.getEnchantLevel())) + "</font><br1>"));
					sb.append(new StringBuilder((ru ? "\u0417\u0430\u0442\u043e\u0447\u043a\u0430 \u0437\u0430" : "Consume") + ": <font color=\"ff3300\">" + name + "</font>"));
					sb.append("<img src=\"l2ui.squaregray\" width=\"170\" height=\"1\">");
					sb.append("</td><td>");
					sb.append(new StringBuilder("<button value=\"" + (ru ? "\u0412\u044b\u0431\u0440\u0430\u0442\u044c" : "Select") + "\" action=\"bypass _bbsechantlist:" + _item.getObjectId() + ";\" width=75 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\">"));
					sb.append("</td></tr>");
				}
			sb.append("</table>");
			String content = HtmCache.getInstance().getHtml("CommunityBoard/enchant.htm", activeChar);
			content = content.replace("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		else if(command.startsWith("_bbsechantlist"))
		{
			final StringTokenizer st2 = new StringTokenizer(command, ";");
			final String[] mBypass = st2.nextToken().split(":");
			final int ItemForEchantObjID = Integer.parseInt(mBypass[1]);
			final String name2 = ItemTable.getInstance().getTemplate(Config.CB_ENCH_ITEM).getName();
			final ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			if(EhchantItem == null)
			{
				parsecmd("_bbsechant", activeChar);
				return;
			}
			final boolean ru2 = activeChar.isLangRus();
			final StringBuilder sb2 = new StringBuilder("");
			sb2.append((ru2 ? "\u0414\u043b\u044f \u0437\u0430\u0442\u043e\u0447\u043a\u0438 \u0432\u044b\u0431\u0440\u0430\u043d\u0430 \u0432\u0435\u0449\u044c" : "Selected item for enchant") + ":<br1><table width=300>");
			sb2.append(new StringBuilder("<tr><td width=32><img src=" + EhchantItem.getTemplate().getIcon() + " width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>"));
			sb2.append(new StringBuilder("<font color=\"ffff00\">" + EhchantItem.getTemplate().getName() + " " + (EhchantItem.getEnchantLevel() <= 0 ? "" : new StringBuilder("</font><br1><font color=3293F3>" + (ru2 ? "\u0417\u0430\u0442\u043e\u0447\u0435\u043d\u043e \u043d\u0430" : "Enchanted") + ": +" + EhchantItem.getEnchantLevel())) + "</font><br1>"));
			sb2.append(new StringBuilder((ru2 ? "\u0417\u0430\u0442\u043e\u0447\u043a\u0430 \u043f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0441\u044f \u0437\u0430" : "Enchant consume") + ": <font color=\"ff3300\">" + name2 + "</font>"));
			sb2.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb2.append(new StringBuilder("<td width=32><img src=" + EhchantItem.getTemplate().getIcon() + " width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>"));
			sb2.append("</tr>");
			sb2.append("</table>");
			sb2.append("<br>");
			sb2.append("<br>");
			final int[] enchant_level = EhchantItem.isWeapon() ? Config.CB_ENCHANT_LVL_WEAPON : Config.CB_ENCHANT_LVL_ARMOR;
			for(int j = 0; j < enchant_level.length; ++j)
				if(enchant_level[j] > EhchantItem.getEnchantLevel())
					sb2.append(new StringBuilder("<button value=\"" + (ru2 ? "\u041d\u0430" : "") + " +" + enchant_level[j] + "   " + (ru2 ? "\u0426\u0435\u043d\u0430" : "Price") + ": " + (EhchantItem.isWeapon() ? Config.CB_ENCHANT_PRICE_WEAPON[j] : Config.CB_ENCHANT_PRICE_ARMOR[j]) + "\" action=\"bypass _bbsechantgo:" + j + ":" + ItemForEchantObjID + ";\" width=135 height=21 back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\">"));
			sb2.append("<br><button value=\"" + (ru2 ? "\u041d\u0430\u0437\u0430\u0434" : "Back") + "\" action=\"bypass _bbsechant\" width=67 height=19 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">");
			String content2 = HtmCache.getInstance().getHtml("CommunityBoard/enchant.htm", activeChar);
			content2 = content2.replace("%enchanter%", sb2.toString());
			separateAndSend(content2, activeChar);
		}
		else if(command.startsWith("_bbsechantgo"))
		{
			final StringTokenizer st2 = new StringTokenizer(command, ";");
			final String[] mBypass = st2.nextToken().split(":");
			final int k = Integer.parseInt(mBypass[1]);
			final int EchantObjID = Integer.parseInt(mBypass[2]);
			final ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(EchantObjID);
			if(noEnch(EhchantItem) || k >= (EhchantItem.isWeapon() ? Config.CB_ENCHANT_LVL_WEAPON.length : Config.CB_ENCHANT_LVL_ARMOR.length))
			{
				activeChar.sendMessage(activeChar.isLangRus() ? "\u0417\u0430\u0442\u043e\u0447\u043a\u0430 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u0430!" : "Enchant impossible!");
				parsecmd("_bbsechant", activeChar);
				return;
			}
			final int EchantVal = EhchantItem.isWeapon() ? Config.CB_ENCHANT_LVL_WEAPON[k] : Config.CB_ENCHANT_LVL_ARMOR[k];
			final int EchantPrice = EhchantItem.isWeapon() ? Config.CB_ENCHANT_PRICE_WEAPON[k] : Config.CB_ENCHANT_PRICE_ARMOR[k];
			final ItemTemplate item = ItemTable.getInstance().getTemplate(Config.CB_ENCH_ITEM);
			final ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if(pay != null && pay.getCount() >= EchantPrice)
			{
				activeChar.getInventory().destroyItem(pay, EchantPrice, true);
				EhchantItem.setEnchantLevel(EchantVal);
				activeChar.getInventory().equipItem(EhchantItem, false);
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(EhchantItem));
				activeChar.broadcastUserInfo(true);
				activeChar.sendMessage(EhchantItem.getTemplate().getName() + (activeChar.isLangRus() ? " \u0437\u0430\u0442\u043e\u0447\u0435\u043d\u043e \u043d\u0430" : " enchanted") + " +" + EchantVal);
				Log.addLog(activeChar.toString() + " enchant item in community: " + EhchantItem.getTemplate().getName() + " val: " + EchantVal, "services");
			}
			else
				activeChar.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			parsecmd("_bbsechant", activeChar);
		}
	}

	private static boolean noEnch(final ItemInstance item)
	{
		if(item == null || !item.isEquipped() || !item.canBeEnchanted())
			return true;
		final int e = item.getEnchantLevel();
		final int[] array;
		final int[] list = array = item.isWeapon() ? Config.CB_ENCHANT_LVL_WEAPON : Config.CB_ENCHANT_LVL_ARMOR;
		for(final int i : array)
			if(i > e)
				return false;
		return true;
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player activeChar)
	{}

	static
	{
		EnchantBBSManager._Instance = null;
	}
}
