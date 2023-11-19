package l2s.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.communitybbs.CommunityBoard;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.TownManager;
import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Town;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ShowBoard;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Util;

public class TeleportBBSManager extends BaseBBSManager
{
	private static Logger _log;
	private static TeleportBBSManager _Instance;

	public static TeleportBBSManager getInstance()
	{
		if(TeleportBBSManager._Instance == null)
			TeleportBBSManager._Instance = new TeleportBBSManager();
		return TeleportBBSManager._Instance;
	}

	@Override
	public void parsecmd(final String command, final Player player)
	{
		if(command.equals("_bbsteleport;"))
			showPoints(player);
		else if(command.startsWith("_bbsteleport;delete;"))
		{
			final StringTokenizer stDell = new StringTokenizer(command, ";");
			stDell.nextToken();
			stDell.nextToken();
			final int TpNameDell = Integer.parseInt(stDell.nextToken());
			delTp(player, TpNameDell);
			showPoints(player);
		}
		else if(command.startsWith("_bbsteleport;save;"))
		{
			final StringTokenizer stAdd = new StringTokenizer(command, ";");
			stAdd.nextToken();
			stAdd.nextToken();
			String TpNameAdd = null;
			if(stAdd.hasMoreTokens())
				TpNameAdd = stAdd.nextToken().trim();
			if(TpNameAdd != null && !TpNameAdd.equals(""))
				AddTp(player, TpNameAdd);
			else
				player.sendMessage("\u0412\u044b \u043d\u0435 \u0432\u0432\u0435\u043b\u0438 \u0438\u043c\u044f \u0437\u0430\u043a\u043b\u0430\u0434\u043a\u0438.");
			showPoints(player);
		}
		else if(command.startsWith("_bbsteleport;teleport;"))
		{
			final StringTokenizer stGoTp = new StringTokenizer(command, " ");
			stGoTp.nextToken();
			final int xTp = Integer.parseInt(stGoTp.nextToken());
			final int yTp = Integer.parseInt(stGoTp.nextToken());
			final int zTp = Integer.parseInt(stGoTp.nextToken());
			goTp(player, xTp, yTp, zTp, false);
			TopBBSManager.getInstance().parsecmd("_bbstop;50", player);
		}
		else if(command.startsWith("_bbsteleport;stele;"))
		{
			final StringTokenizer stGoTp = new StringTokenizer(command, " ");
			stGoTp.nextToken();
			final int xTp = Integer.parseInt(stGoTp.nextToken());
			final int yTp = Integer.parseInt(stGoTp.nextToken());
			final int zTp = Integer.parseInt(stGoTp.nextToken());
			goTp(player, xTp, yTp, zTp, true);
			TopBBSManager.getInstance().parsecmd("_bbstop;50", player);
		}
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>\u0424\u0443\u043d\u043a\u0446\u0438\u044f: " + command + " \u043f\u043e\u043a\u0430 \u043d\u0435 \u0440\u0435\u0430\u043b\u0438\u0437\u043e\u0432\u0430\u043d\u0430</center><br><br></body></html>", player);
	}

	private void goTp(final Player player, final int xTp, final int yTp, final int zTp, final boolean save)
	{
		if(Config.PVP_BBS_TELEPORT_LVL > player.getLevel())
		{
			player.sendMessage("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0441 " + Config.PVP_BBS_TELEPORT_LVL + "-\u0433\u043e \u0443\u0440\u043e\u0432\u043d\u044f.");
			return;
		}
		if(player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.getTeam() != 0 || player.isFlying() || player.inObserverMode())
		{
			player.sendMessage("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u0432 \u0432\u0430\u0448\u0435\u043c \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0438 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u0430.");
			return;
		}
		if(player.isInZone(Zone.ZoneType.no_escape) || player.isInZone(Zone.ZoneType.Siege))
		{
			player.sendMessage("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u0441 \u044d\u0442\u043e\u0439 \u043c\u0435\u0441\u0442\u043d\u043e\u0441\u0442\u0438 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u0430.");
			return;
		}
		if(player.getVar("jailed") != null)
		{
			player.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u0438\u0437 \u0442\u044e\u0440\u044c\u043c\u044b.");
			return;
		}
		if(player.getTeam() != 0)
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u0432 \u0430\u0443\u0440\u0435." : "You can't do it in aura.");
			return;
		}
		if(Config.PVP_BBS_TELEPORT_PEACE && !player.isInZone(Zone.ZoneType.peace_zone))
		{
			player.sendMessage("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0441 \u043c\u0438\u0440\u043d\u043e\u0439 \u0437\u043e\u043d\u044b.");
			return;
		}
		if(Config.NO_PVP_BBS_TELEPORT_EPIC && player.isInZone(Zone.ZoneType.epic))
		{
			player.sendMessage("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u0432 \u044d\u043f\u0438\u043a \u0437\u043e\u043d\u0430\u0445 \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u0430.");
			return;
		}
		if(!Config.PVP_BBS_TELEPORT_KARMA && player.getKarma() > 0)
		{
			player.sendMessage("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u0441 \u043a\u0430\u0440\u043c\u043e\u0439 \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u0430.");
			return;
		}
		if(!save && Config.TELEPORT_FILTER && !Config.TELEPORT_LIST_FILTER.contains(xTp + "," + yTp + "," + zTp))
		{
			player.sendMessage("\u0417\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043d\u0430\u044f \u0442\u043e\u0447\u043a\u0430 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u0438!");
			return;
		}
		if(save && Config.BBS_TELEPORT_PEACE_SAVE && !ZoneManager.getInstance().checkIfInZone(Zone.ZoneType.peace_zone, xTp, yTp, zTp))
		{
			player.sendMessage("\u0422\u043e\u0447\u043a\u0438 \u0432\u043e\u0437\u0432\u0440\u0430\u0442\u0430 \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u044b \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043c\u0438\u0440\u043d\u0443\u044e \u0437\u043e\u043d\u0443.");
			return;
		}
		if(!Config.PVP_BBS_TELEPORT_SIEGE)
		{
			final Town town = TownManager.getInstance().getClosestTown(xTp, yTp);
			if(town != null)
			{
				final Castle castle = town.getCastle();
				final SiegeEvent<?, ?> siegeEvent = castle != null ? castle.getSiegeEvent() : null;
				if(siegeEvent != null && siegeEvent.isInProgress())
				{
					player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
					return;
				}
			}
		}
		final int id = save ? Config.PVP_BBS_TELEPORT_ITEM : Config.PVP_BBS_TELE_ITEM;
		final int price = save ? Config.PVP_BBS_TELEPORT_PRICE : Config.PVP_BBS_TELE_PRICE;
		if(price > 0)
			if(id == 57)
			{
				if(player.getAdena() < price)
				{
					player.sendPacket(new SystemMessage(279));
					return;
				}
				player.reduceAdena(price, true);
			}
			else
			{
				final ItemInstance pay = player.getInventory().getItemByItemId(id);
				if(pay == null || pay.getCount() < price)
				{
					player.sendPacket(new SystemMessage(351));
					return;
				}
				player.getInventory().destroyItem(pay, price, true);
				player.sendPacket(SystemMessage.removeItems(id, price));
			}
		player.teleToLocation(xTp, yTp, zTp);
	}

	private void showPoints(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM comteleport WHERE charId=?;");
			statement.setLong(1, player.getObjectId());
			rs = statement.executeQuery();
			final TextBuilder html = new TextBuilder();
			html.append("<table width=220>");
			while(rs.next())
			{
				final CBteleport tp = new CBteleport();
				tp.TpId = rs.getInt("TpId");
				tp.TpName = rs.getString("name");
				tp.PlayerId = rs.getInt("charId");
				tp.xC = rs.getInt("xPos");
				tp.yC = rs.getInt("yPos");
				tp.zC = rs.getInt("zPos");
				html.append("<tr><td>");
				html.append("<button value=\"").append(tp.TpName).append("\" action=\"bypass _bbsteleport;stele; ").append(String.valueOf(tp.xC)).append(" ").append(String.valueOf(tp.yC)).append(" ").append(String.valueOf(tp.zC)).append("\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
				html.append("</td><td>");
				html.append("<button value=\"" + (player.isLangRus() ? "\u0423\u0434\u0430\u043b\u0438\u0442\u044c" : "Delete") + "\" action=\"bypass _bbsteleport;delete;").append(String.valueOf(tp.TpId)).append("\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
				html.append("</td></tr>");
			}
			html.append("</table>");
			String content = HtmCache.getInstance().getHtml("CommunityBoard/50.htm", player); // было 49 в место 50
			content = content.replace("%tp%", html.toString());
			content = CommunityBoard.htmlAll(content, player);
			separateAndSend(content, player);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	private void delTp(final Player player, final int TpNameDell)
	{
		Connection conDel = null;
		PreparedStatement statementDel = null;
		try
		{
			conDel = DatabaseFactory.getInstance().getConnection();
			statementDel = conDel.prepareStatement("DELETE FROM comteleport WHERE charId=? AND TpId=?;");
			statementDel.setInt(1, player.getObjectId());
			statementDel.setInt(2, TpNameDell);
			statementDel.execute();
		}
		catch(Exception e)
		{
			TeleportBBSManager._log.warn("data error on Delete Teleport: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conDel, statementDel);
		}
	}

	private void AddTp(final Player player, final String TpNameAdd)
	{
		if(!Config.ALLOW_BBS_TELEPORT_SAVE)
		{
			player.sendMessage("\u0424\u0443\u043d\u043a\u0446\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430.");
			return;
		}
		if(!Util.isMatchingRegexp(TpNameAdd, Config.BBS_TELEPORT_SAVE_NAME))
		{
			player.sendMessage(new CustomMessage("scripts.services.Rename.incorrectinput"));
			return;
		}
		if(player.isAlikeDead() || player.isCastingNow() || player.isAttackingNow() || player.isInOlympiadMode() || player.inObserverMode())
		{
			player.sendMessage("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0437\u0430\u043a\u043b\u0430\u0434\u043a\u0443 \u0432 \u0432\u0430\u0448\u0435\u043c \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0438 \u043d\u0435\u043b\u044c\u0437\u044f.");
			return;
		}
		if(player.getVar("jailed") != null)
		{
			player.sendMessage("\u041d\u0435\u043b\u044c\u0437\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0437\u0430\u043a\u043b\u0430\u0434\u043a\u0443 \u043f\u043e\u043a\u0430 \u0412\u044b \u0432 \u0442\u044e\u0440\u044c\u043c\u0435.");
			return;
		}
		if(Config.BBS_TELEPORT_PEACE_SAVE && !player.isInZone(Zone.ZoneType.peace_zone))
		{
			player.sendMessage("\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0442\u044c \u0437\u0430\u043a\u043b\u0430\u0434\u043a\u0438 \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u043e \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043c\u0438\u0440\u043d\u043e\u0439 \u0437\u043e\u043d\u0435.");
			return;
		}
		if(Config.PVP_BBS_TELEPORT_ADDITIONAL_RULES && (player.isInZoneBattle() || player.isInZone(Zone.ZoneType.no_restart) || player.isInZone(Zone.ZoneType.no_escape) || player.isInZone(Zone.ZoneType.no_summon) || player.isInZone(Zone.ZoneType.epic)))
		{
			player.sendMessage("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0437\u0430\u043a\u043b\u0430\u0434\u043a\u0443 \u0432 \u044d\u0442\u043e\u0439 \u043c\u0435\u0441\u0442\u043d\u043e\u0441\u0442\u0438 \u043d\u0435\u043b\u044c\u0437\u044f.");
			return;
		}
		if(player.isInCombat())
		{
			player.sendMessage("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0437\u0430\u043a\u043b\u0430\u0434\u043a\u0443 \u0432 \u0440\u0435\u0436\u0438\u043c\u0435 \u0431\u043e\u044f \u043d\u0435\u043b\u044c\u0437\u044f.");
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(*) FROM comteleport WHERE charId=?;");
			statement.setLong(1, player.getObjectId());
			rs = statement.executeQuery();
			rs.next();
			if(rs.getInt(1) < Config.PVP_BBS_TELEPORT_SAVE_COUNT)
			{
				statement = con.prepareStatement("SELECT COUNT(*) FROM comteleport WHERE charId=? AND name=?;");
				statement.setLong(1, player.getObjectId());
				statement.setString(2, TpNameAdd);
				rs = statement.executeQuery();
				rs.next();
				if(rs.getInt(1) == 0)
				{
					statement = con.prepareStatement("INSERT INTO comteleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)");
					statement.setInt(1, player.getObjectId());
					statement.setInt(2, player.getX());
					statement.setInt(3, player.getY());
					statement.setInt(4, player.getZ());
					statement.setString(5, TpNameAdd);
					statement.execute();
				}
				else
				{
					statement = con.prepareStatement("UPDATE comteleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
					statement.setInt(1, player.getObjectId());
					statement.setInt(2, player.getX());
					statement.setInt(3, player.getY());
					statement.setInt(4, player.getZ());
					statement.setString(5, TpNameAdd);
					statement.execute();
				}
			}
			else
				player.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0431\u043e\u043b\u0435\u0435 " + Config.PVP_BBS_TELEPORT_SAVE_COUNT + " \u0437\u0430\u043a\u043b\u0430\u0434\u043e\u043a.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player player)
	{}

	static
	{
		TeleportBBSManager._log = LoggerFactory.getLogger(TeleportBBSManager.class);
		TeleportBBSManager._Instance = null;
	}

	public class CBteleport
	{
		public int TpId;
		public String TpName;
		public int PlayerId;
		public int xC;
		public int yC;
		public int zC;

		public CBteleport()
		{
			TpId = 0;
			TpName = "";
			PlayerId = 0;
			xC = 0;
			yC = 0;
			zC = 0;
		}
	}
}
