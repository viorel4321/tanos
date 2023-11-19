package l2s.gameserver.communitybbs;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.communitybbs.Manager.BuffBBSManager;
import l2s.gameserver.communitybbs.Manager.ClanBBSManager;
import l2s.gameserver.communitybbs.Manager.EnchantBBSManager;
import l2s.gameserver.communitybbs.Manager.FailBBSManager;
import l2s.gameserver.communitybbs.Manager.FriendsBBSManager;
import l2s.gameserver.communitybbs.Manager.PostBBSManager;
import l2s.gameserver.communitybbs.Manager.RegionBBSManager;
import l2s.gameserver.communitybbs.Manager.StatBBSManager;
import l2s.gameserver.communitybbs.Manager.TeleportBBSManager;
import l2s.gameserver.communitybbs.Manager.TopBBSManager;
import l2s.gameserver.communitybbs.Manager.TopicBBSManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.handler.IVoicedCommandHandler;
import l2s.gameserver.handler.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.SellList;
import l2s.gameserver.network.l2.s2c.ShowBoard;
import l2s.gameserver.scripts.Scripts;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.communitybbs.CommunityBoard;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ShowBoard;



public class CommunityBoard
{
	private static Logger _log = LoggerFactory.getLogger(CommunityBoard.class);

	private static CommunityBoard _instance;

	public static CommunityBoard getInstance()
	{
		if(CommunityBoard._instance == null)
			CommunityBoard._instance = new CommunityBoard();
		return CommunityBoard._instance;
	}

	public void handleCommands(final GameClient client, final String command)
	{
		final Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;
		if(!Config.ALLOW_COMMUNITYBOARD)
		{
			activeChar.sendPacket(Msg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}
		if(!Config.ALLOW_PVPCB_ABNORMAL && (activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isInDuel() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isInVehicle() || activeChar.isFlying() || activeChar.inObserverMode() || activeChar.isSitting()))
		{
			TopBBSManager.getInstance().parsecmd("_bbstop;10", activeChar);
			return;
		}
		if(Config.PVPCB_ONLY_PEACE && !activeChar.isInZonePeace())
		{
			TopBBSManager.getInstance().parsecmd("_bbstop;10", activeChar);
			return;
		}
		if(command.startsWith("_bbsclan"))
			ClanBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsmemo"))
			TopicBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbstopics"))
			TopicBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsposts"))
			PostBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbstop"))
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbshome"))
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsloc"))
			RegionBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_friend") || command.startsWith("_block"))
			FriendsBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsgetfav"))
			ShowBoard.separateAndSend("<html><body><br><br><center>\u0417\u0430\u043a\u043b\u0430\u0434\u043a\u0438 \u043f\u043e\u043a\u0430 \u043d\u0435 \u0440\u0435\u0430\u043b\u0438\u0437\u043e\u0432\u0430\u043d\u044b.</center><br><br></body></html>", activeChar);
		else if(command.startsWith("_bbsmail") || command.startsWith("_mail"))
		{
			if(!Config.ALLOW_MAIL)
				FailBBSManager.getInstance().parsecmd(command, activeChar);
			else
				Scripts.getInstance().callScripts(activeChar, "services.MailBBSManager", "parsecmd", new Object[] { command });
		}
		else if(command.startsWith("_bbsteleport;"))
		{
			if(!Config.ALLOW_PVPCB_TELEPORT)
				//FailBBSManager.getInstance().parsecmd(command, activeChar); отменил запрем АЛЬТ + Б
			TeleportBBSManager.getInstance().parsecmd(command, activeChar);
			else
				TeleportBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if(command.startsWith("_bbsechant"))
		{
			if(!Config.ALLOW_CB_ENCHANT)
				FailBBSManager.getInstance().parsecmd(command, activeChar);
			else
				EnchantBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if(command.startsWith("_bbsbuff;"))
			BuffBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsstat;"))
		{
			if(Config.ALLOW_PVPCB_STAT)
				StatBBSManager.getInstance().parsecmd(command, activeChar);
			else
				FailBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else if(command.startsWith("_bbsmultisell;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
			if(!Config.ALLOW_PVPCB_SHOP)
				activeChar.sendMessage(activeChar.isLangRus() ? "\u042d\u0442\u0430 \u0444\u0443\u043d\u043a\u0446\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "This function disabled.");
			else if(Config.ALLOW_PVPCB_SHOP_KARMA || activeChar.getKarma() <= 0)
			{
				activeChar.setLastNpcId(-1);
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(st.nextToken()), activeChar, 0.0);
			}
			else
				activeChar.sendMessage(activeChar.isLangRus() ? "\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u043c\u0430\u0433\u0430\u0437\u0438\u043d \u0441 \u043a\u0430\u0440\u043c\u043e\u0439." : "You can't use shop with karma.");
		}
		else if(command.startsWith("_bbssell;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
			if(!Config.ALLOW_PVPCB_SHOP)
				activeChar.sendMessage(activeChar.isLangRus() ? "\u042d\u0442\u0430 \u0444\u0443\u043d\u043a\u0446\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "This function disabled.");
			else
			{
				activeChar.setLastNpcId(-1);
				activeChar.sendPacket(new SellList(activeChar));
			}
		}
		else if(command.startsWith("_bbsuser;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
			final String cmd = st.nextToken().trim();
			final String word = cmd.split("\\s+")[0];
			final String args = cmd.substring(word.length()).trim();
			final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);
			if(vch != null)
				vch.useVoicedCommand(word, activeChar, args);
		}
		else if(command.startsWith("_bbsaugment;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
			final String type = st.nextToken();
			if(type.equals("1"))
				activeChar.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, Msg.ExShowVariationMakeWindow);
			else
				activeChar.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, Msg.ExShowVariationCancelWindow);
		}
		else if(command.startsWith("_bbsscripts;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			final String page = st.nextToken();
			if(page.equals("70"))
				BuffBBSManager.getInstance().showBuffList(activeChar);
			else
				TopBBSManager.getInstance().parsecmd("_bbstop;" + page, activeChar);
			final String com = st.nextToken();
			final String[] word1 = com.split("\\s+");
			final String[] args1 = com.substring(word1[0].length()).trim().split("\\s+");
			final String[] path = word1[0].split(":");
			if(path.length != 2)
			{
				_log.warn("Bad Script community bypass! " + command);
				return;
			}
		   activeChar.setLastNpc(null);
		   HashMap<String, Object> variables = new HashMap<String, Object>();
		   variables.put("npc", HardReferences.emptyRef());
		   Scripts.getInstance().callScripts(activeChar, path[0], path[1], word1.length == 1 ? new Object[0] : new Object[]{ args1 }, variables);
		}
	}

	public void handleWriteCommands(final GameClient client, final String url, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5)
	{
		final Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;
		if(!Config.ALLOW_COMMUNITYBOARD)
		{
			activeChar.sendPacket(Msg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}
		if(url.equals("Topic"))
			TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if(url.equals("Post"))
			PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if(url.equals("Region"))
			RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if(url.equals("Notice"))
		{
			if(arg4.length() > 512)
			{
				ShowBoard.separateAndSend("<html><body><br><br><center>\u0412\u044b \u0432\u0432\u0435\u043b\u0438 \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u0434\u043b\u0438\u043d\u043d\u043e\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435, \u043e\u043d\u043e \u0431\u0443\u0434\u0435\u0442 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043e \u043d\u0435 \u043f\u043e\u043b\u043d\u043e\u0441\u0442\u044c\u044e.</center><br><br></body></html>", activeChar);
				return;
			}
			ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>\u0424\u0443\u043d\u043a\u0446\u0438\u044f: " + url + " \u043f\u043e\u043a\u0430 \u043d\u0435 \u0440\u0435\u0430\u043b\u0438\u0437\u043e\u0432\u0430\u043d\u0430.</center><br><br></body></html>", activeChar);
	}

	public static String htmlAll(final String htm, final Player player)
	{
		String html_all = HtmCache.getInstance().getHtml(Config.COMMUNITYBOARD_HTML_ROOT + "block/pages.htm", player);
		final String html_menu = HtmCache.getInstance().getHtml(Config.COMMUNITYBOARD_HTML_ROOT + "block/menu.htm", player);
		html_all = html_all.replace("%main_menu%", html_menu);
		html_all = html_all.replace("%body_page%", htm);
		html_all = html_all.replace("%copy%", "©");
		html_all = html_all.replace("%reg%", "®");
		html_all = html_all.replace("%name%", player.getName());
		html_all = html_all.replace("%level%", String.valueOf(player.getLevel()));



		long playedTime = player.getOnlineTime() / 1000;// / 60;
		long sec = playedTime % 60;
		long minutes = playedTime % 3600 / 60;
		long hours = playedTime % 86400 / 3600;
		long days = playedTime / 86400;
		html_all = html_all.replace("%played_day%", String.valueOf(days));
		html_all = html_all.replace("%played_hour%", String.valueOf(hours));
		html_all = html_all.replace("%played_minute%", String.valueOf(minutes));

		html_all = html_all.replace("%onlinetime%", String.valueOf(player.getOnlineTime()));
		if(player.getClan() == null)
		{
		html_all = html_all.replace("%clan%", "No");
		html_all = html_all.replace("%ally%", "No");

		}
		if(player.getClan() != null && player.getClan().getAlliance() == null)
		{
			html_all = html_all.replace("%clan%", player.getClan().getName());
			html_all = html_all.replace("%ally%", "No");

		}

		if(player.getClan() != null && player.getClan().getAlliance() != null)
		{
			html_all = html_all.replace("%clan%", player.getClan().getName());
			html_all = html_all.replace("%ally%", player.getClan().getAlliance().getAllyName());
		}

		if(player.getClassId().getId() == 0)
		{
			html_all = html_all.replace("%class%", "Fighter");
		}
		if(player.getClassId().getId() == 1)
		{
			html_all = html_all.replace("%class%", "Warrior");
		}
		if(player.getClassId().getId() == 2)
		{
			html_all = html_all.replace("%class%", "Gladiator");
		}
		if(player.getClassId().getId() == 3)
		{
			html_all = html_all.replace("%class%", "Warlord");
		}
		if(player.getClassId().getId() == 4)
		{
			html_all = html_all.replace("%class%", "Knight");
		}
		if(player.getClassId().getId() == 5)
		{
			html_all = html_all.replace("%class%", "Paladin");
		}
		if(player.getClassId().getId() == 6)
		{
			html_all = html_all.replace("%class%", "DarkAvenger");
		}
		if(player.getClassId().getId() == 7)
		{
			html_all = html_all.replace("%class%", "Rogue");
		}
		if(player.getClassId().getId() == 8)
		{
			html_all = html_all.replace("%class%", "TreasureHunter");
		}
		if(player.getClassId().getId() == 9)
		{
			html_all = html_all.replace("%class%", "Hawkeye");
		}
		if(player.getClassId().getId() == 10)
		{
			html_all = html_all.replace("%class%", "Mage");
		}
		if(player.getClassId().getId() == 11)
		{
			html_all = html_all.replace("%class%", "Wizard");
		}
		if(player.getClassId().getId() == 12)
		{
			html_all = html_all.replace("%class%", "Sorceror");
		}
		if(player.getClassId().getId() == 13)
		{
			html_all = html_all.replace("%class%", "Necromancer");
		}
		if(player.getClassId().getId() == 14)
		{
			html_all = html_all.replace("%class%", "Warlock");
		}
		if(player.getClassId().getId() == 15)
		{
			html_all = html_all.replace("%class%", "Cleric");
		}
		if(player.getClassId().getId() == 16)
		{
			html_all = html_all.replace("%class%", "Bishop");
		}
		if(player.getClassId().getId() == 17)
		{
			html_all = html_all.replace("%class%", "Prophet");
		}
		if(player.getClassId().getId() == 18)
		{
			html_all = html_all.replace("%class%", "ElvenFighter");
		}
		if(player.getClassId().getId() == 19)
		{
			html_all = html_all.replace("%class%", "ElvenKnight");
		}
		if(player.getClassId().getId() == 20)
		{
			html_all = html_all.replace("%class%", "TempleKnight");
		}
		if(player.getClassId().getId() == 21)
		{
			html_all = html_all.replace("%class%", "SwordSinger");
		}
		if(player.getClassId().getId() == 22)
		{
			html_all = html_all.replace("%class%", "ElvenScout");
		}
		if(player.getClassId().getId() == 23)
		{
			html_all = html_all.replace("%class%", "PlainsWalker");
		}
		if(player.getClassId().getId() == 24)
		{
			html_all = html_all.replace("%class%", "SilverRanger");
		}
		if(player.getClassId().getId() == 25)
		{
			html_all = html_all.replace("%class%", "ElvenMage");
		}
		if(player.getClassId().getId() == 26)
		{
			html_all = html_all.replace("%class%", "ElvenWizard");
		}
		if(player.getClassId().getId() == 27)
		{
			html_all = html_all.replace("%class%", "Spellsinger");
		}
		if(player.getClassId().getId() == 28)
		{
			html_all = html_all.replace("%class%", "ElementalSummoner");
		}
		if(player.getClassId().getId() == 29)
		{
			html_all = html_all.replace("%class%", "Oracle");
		}
		if(player.getClassId().getId() == 30)
		{
			html_all = html_all.replace("%class%", "Elder");
		}
		if(player.getClassId().getId() == 31)
		{
			html_all = html_all.replace("%class%", "DarkFighter");
		}
		if(player.getClassId().getId() == 32)
		{
			html_all = html_all.replace("%class%", "PalusKnight");
		}
		if(player.getClassId().getId() == 33)
		{
			html_all = html_all.replace("%class%", "ShillienKnight");
		}
		if(player.getClassId().getId() == 34)
		{
			html_all = html_all.replace("%class%", "Bladedancer");
		}
		if(player.getClassId().getId() == 35)
		{
			html_all = html_all.replace("%class%", "Assassin");
		}
		if(player.getClassId().getId() == 36)
		{
			html_all = html_all.replace("%class%", "AbyssWalker");
		}
		if(player.getClassId().getId() == 37)
		{
			html_all = html_all.replace("%class%", "PhantomRanger");
		}
		if(player.getClassId().getId() == 38)
		{
			html_all = html_all.replace("%class%", "DarkMage");
		}
		if(player.getClassId().getId() == 39)
		{
			html_all = html_all.replace("%class%", "DarkWizard");
		}
		if(player.getClassId().getId() == 40)
		{
			html_all = html_all.replace("%class%", "Spellhowler");
		}
		if(player.getClassId().getId() == 41)
		{
			html_all = html_all.replace("%class%", "PhantomSummoner");
		}
		if(player.getClassId().getId() == 42)
		{
			html_all = html_all.replace("%class%", "ShillienOracle");
		}
		if(player.getClassId().getId() == 43)
		{
			html_all = html_all.replace("%class%", "ShillienElder");
		}
		if(player.getClassId().getId() == 44)
		{
			html_all = html_all.replace("%class%", "OrcFighter");
		}
		if(player.getClassId().getId() == 45)
		{
			html_all = html_all.replace("%class%", "OrcRaider");
		}
		if(player.getClassId().getId() == 46)
		{
			html_all = html_all.replace("%class%", "Destroyer");
		}
		if(player.getClassId().getId() == 47)
		{
			html_all = html_all.replace("%class%", "OrcMonk");
		}
		if(player.getClassId().getId() == 48)
		{
			html_all = html_all.replace("%class%", "Tyrant");
		}
		if(player.getClassId().getId() == 49)
		{
			html_all = html_all.replace("%class%", "OrcMage");
		}
		if(player.getClassId().getId() == 50)
		{
			html_all = html_all.replace("%class%", "OrcShaman");
		}
		if(player.getClassId().getId() == 51)
		{
			html_all = html_all.replace("%class%", "Overlord");
		}
		if(player.getClassId().getId() == 52)
		{
			html_all = html_all.replace("%class%", "Warcryer");
		}
		if(player.getClassId().getId() == 53)
		{
			html_all = html_all.replace("%class%", "DwarvenFighter");
		}
		if(player.getClassId().getId() == 54)
		{
			html_all = html_all.replace("%class%", "Scavenger");
		}
		if(player.getClassId().getId() == 55)
		{
			html_all = html_all.replace("%class%", "BountyHunter");
		}
		if(player.getClassId().getId() == 56)
		{
			html_all = html_all.replace("%class%", "Artisan");
		}
		if(player.getClassId().getId() == 57)
		{
			html_all = html_all.replace("%class%", "Warsmith");
		}
		if(player.getClassId().getId() == 88)
		{
			html_all = html_all.replace("%class%", "Duelist");
		}
		if(player.getClassId().getId() == 89)
		{
			html_all = html_all.replace("%class%", "Dreadnought");
		}
		if(player.getClassId().getId() == 90)
		{
			html_all = html_all.replace("%class%", "PhoenixKnight");
		}
		if(player.getClassId().getId() == 91)
		{
			html_all = html_all.replace("%class%", "HellKnight");
		}
		if(player.getClassId().getId() == 92)
		{
			html_all = html_all.replace("%class%", "Sagittarius");
		}
		if(player.getClassId().getId() == 93)
		{
			html_all = html_all.replace("%class%", "Adventurer");
		}
		if(player.getClassId().getId() == 94)
		{
			html_all = html_all.replace("%class%", "Archmage");
		}
		if(player.getClassId().getId() == 95)
		{
			html_all = html_all.replace("%class%", "Soultaker");
		}
		if(player.getClassId().getId() == 96)
		{
			html_all = html_all.replace("%class%", "ArcanaLord");
		}
		if(player.getClassId().getId() == 97)
		{
			html_all = html_all.replace("%class%", "Cardinal");
		}
		if(player.getClassId().getId() == 98)
		{
			html_all = html_all.replace("%class%", "Hierophant");
		}
		if(player.getClassId().getId() == 99)
		{
			html_all = html_all.replace("%class%", "EvaTemplar");
		}
		if(player.getClassId().getId() == 100)
		{
			html_all = html_all.replace("%class%", "SwordMuse");
		}
		if(player.getClassId().getId() == 101)
		{
			html_all = html_all.replace("%class%", "WindRider");
		}
		if(player.getClassId().getId() == 102)
		{
			html_all = html_all.replace("%class%", "MoonlightSentinel");
		}
		if(player.getClassId().getId() == 103)
		{
			html_all = html_all.replace("%class%", "MysticMuse");
		}
		if(player.getClassId().getId() == 104)
		{
			html_all = html_all.replace("%class%", "ElementalMaster");
		}
		if(player.getClassId().getId() == 105)
		{
			html_all = html_all.replace("%class%", "EvaSaint");
		}
		if(player.getClassId().getId() == 106)
		{
			html_all = html_all.replace("%class%", "ShillienTemplar");
		}
		if(player.getClassId().getId() == 107)
		{
			html_all = html_all.replace("%class%", "SpectralDancer");
		}
		if(player.getClassId().getId() == 108)
		{
			html_all = html_all.replace("%class%", "GhostHunter");
		}
		if(player.getClassId().getId() == 109)
		{
			html_all = html_all.replace("%class%", "GhostSentinel");
		}
		if(player.getClassId().getId() == 110)
		{
			html_all = html_all.replace("%class%", "StormScreamer");
		}
		if(player.getClassId().getId() == 111)
		{
			html_all = html_all.replace("%class%", "SpectralMaster");
		}
		if(player.getClassId().getId() == 112)
		{
			html_all = html_all.replace("%class%", "ShillienSaint");
		}
		if(player.getClassId().getId() == 113)
		{
			html_all = html_all.replace("%class%", "Titan");
		}
		if(player.getClassId().getId() == 114)
		{
			html_all = html_all.replace("%class%", "GrandKhauatari");
		}
		if(player.getClassId().getId() == 115)
		{
			html_all = html_all.replace("%class%", "Dominator");
		}
		if(player.getClassId().getId() == 116)
		{
			html_all = html_all.replace("%class%", "Doomcryer");
		}
		if(player.getClassId().getId() == 117)
		{
			html_all = html_all.replace("%class%", "FortuneSeeker");
		}
		if(player.getClassId().getId() == 118)
		{
			html_all = html_all.replace("%class%", "Maestro");
		}





		return html_all;
	}

}
