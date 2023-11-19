package l2s.gameserver.communitybbs.Manager;

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

public class StatBBSManager extends BaseBBSManager
{
	private static StatBBSManager _Instance;

	public static StatBBSManager getInstance()
	{
		if(StatBBSManager._Instance == null)
			StatBBSManager._Instance = new StatBBSManager();
		return StatBBSManager._Instance;
	}

	@Override
	public void parsecmd(final String command, final Player player)
	{
		if(command.equals("_bbsstat;"))
			showPvP(player);
		else if(command.startsWith("_bbsstat;pk"))
			showPK(player);
		else if(command.startsWith("_bbsstat;online"))
			showOnline(player);
		else if(command.startsWith("_bbsstat;clan"))
			showClan(player);
		else if(command.startsWith("_bbsstat;castle"))
			showCastle(player);
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>В bbsstat функция: " + command + " пока не реализована</center><br><br></body></html>", player);
	}

	private void showPvP(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pvpkills DESC LIMIT " + Config.CB_STAT_LIMIT_TOP_PVP + ";");
			rs = statement.executeQuery();
			final StringBuilder html = new StringBuilder();
			html.append("<table width=" + Config.CB_STAT_TABLE_WIDTH + ">");
			while(rs.next())
			{
				final CBStatMan tp = new CBStatMan();
				tp.PlayerId = rs.getInt("obj_Id");
				tp.ChName = rs.getString("char_name");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = Config.CB_STAT_ONLINE ? rs.getInt("online") : 0;
				final String sex = tp.ChSex == 1 ? player.isLangRus() ? "\u0416" : "F" : "M";
				String color = null;
				String OnOff = null;
				if(Config.CB_STAT_ONLINE)
					if(tp.ChOnOff == 1)
					{
						OnOff = player.isLangRus() ? "В игре" : "Online";
						color = "00CC00";
					}
					else
					{
						OnOff = player.isLangRus() ? "Оффлайн" : "Offline";
						color = "D70000";
					}
				html.append("<tr>");
				html.append("<td width=" + Config.CB_STAT_TD_WIDTH + ">" + tp.ChName + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(tp.ChGameTime, player.isLangRus()) + "</td>");
				html.append("<td width=50><font color=00CC00>" + tp.ChPvP + "</font></td>");
				html.append("<td width=50>" + tp.ChPk + "</td>");
				if(Config.CB_STAT_ONLINE)
					html.append("<td width=100><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			String content = HtmCache.getInstance().getHtml("CommunityBoard/81.htm", player);
			content = content.replace("%stats_top_pvp%", html.toString());
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

	private void showPK(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pkkills DESC LIMIT " + Config.CB_STAT_LIMIT_TOP_PK + ";");
			rs = statement.executeQuery();
			final StringBuilder html = new StringBuilder();
			html.append("<table width=" + Config.CB_STAT_TABLE_WIDTH + ">");
			while(rs.next())
			{
				final CBStatMan tp = new CBStatMan();
				tp.PlayerId = rs.getInt("obj_Id");
				tp.ChName = rs.getString("char_name");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = Config.CB_STAT_ONLINE ? rs.getInt("online") : 0;
				final String sex = tp.ChSex == 1 ? player.isLangRus() ? "\u0416" : "F" : "M";
				String color = null;
				String OnOff = null;
				if(Config.CB_STAT_ONLINE)
					if(tp.ChOnOff == 1)
					{
						OnOff = player.isLangRus() ? "\u0412 \u0438\u0433\u0440\u0435" : "Online";
						color = "00CC00";
					}
					else
					{
						OnOff = player.isLangRus() ? "\u041e\u0444\u0444\u043b\u0430\u0439\u043d" : "Offline";
						color = "D70000";
					}
				html.append("<tr>");
				html.append("<td width=" + Config.CB_STAT_TD_WIDTH + ">" + tp.ChName + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(tp.ChGameTime, player.isLangRus()) + "</td>");
				html.append("<td width=50>" + tp.ChPvP + "</td>");
				html.append("<td width=50><font color=00CC00>" + tp.ChPk + "</font></td>");
				if(Config.CB_STAT_ONLINE)
					html.append("<td width=100><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			String content = HtmCache.getInstance().getHtml("CommunityBoard/82.htm", player);
			content = content.replace("%stats_top_pk%", html.toString());
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

	private void showOnline(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY onlinetime DESC LIMIT " + Config.CB_STAT_LIMIT_TOP_ONLINE + ";");
			rs = statement.executeQuery();
			final StringBuilder html = new StringBuilder();
			html.append("<table width=" + Config.CB_STAT_TABLE_WIDTH + ">");
			while(rs.next())
			{
				final CBStatMan tp = new CBStatMan();
				tp.PlayerId = rs.getInt("obj_Id");
				tp.ChName = rs.getString("char_name");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = Config.CB_STAT_ONLINE ? rs.getInt("online") : 0;
				final String sex = tp.ChSex == 1 ? player.isLangRus() ? "\u0416" : "F" : "M";
				String color = null;
				String OnOff = null;
				if(Config.CB_STAT_ONLINE)
					if(tp.ChOnOff == 1)
					{
						OnOff = player.isLangRus() ? "\u0412 \u0438\u0433\u0440\u0435" : "Online";
						color = "00CC00";
					}
					else
					{
						OnOff = player.isLangRus() ? "\u041e\u0444\u0444\u043b\u0430\u0439\u043d" : "Offline";
						color = "D70000";
					}
				html.append("<tr>");
				html.append("<td width=" + Config.CB_STAT_TD_WIDTH + ">" + tp.ChName + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100><font color=00CC00>" + OnlineTime(tp.ChGameTime, player.isLangRus()) + "</font></td>");
				html.append("<td width=50>" + tp.ChPvP + "</td>");
				html.append("<td width=50>" + tp.ChPk + "</td>");
				if(Config.CB_STAT_ONLINE)
					html.append("<td width=100><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			String content = HtmCache.getInstance().getHtml("CommunityBoard/83.htm", player);
			content = content.replace("%stats_online%", html.toString());
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

	private void showCastle(final Player player)
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM castle ORDER BY id");
			rs = statement.executeQuery();
			final StringBuilder html = new StringBuilder();
			html.append("<table width=570>");
			String Owner = null;
			String color = "FFFFFF";
			while(rs.next())
			{
				final int id = rs.getInt("id");
				if(!ArrayUtils.contains(Config.CB_STAT_CASTLES, id))
					continue;
				final CBStatMan tp = new CBStatMan();
				tp.id = id;
				tp.NameCastl = rs.getString("name");
				tp.Percent = rs.getString("tax_percent") + "%";
				tp.siegeDate = sdf.format(new Date(rs.getLong("siege_date")));
				Owner = CastleManorManager.getInstance().getOwner(tp.id);
				if(Owner != null)
					color = "00CC00";
				else
				{
					color = "FFFFFF";
					Owner = player.isLangRus() ? "\u041d\u0435\u0442 \u0432\u043b\u0430\u0434\u0435\u043b\u044c\u0446\u0430" : "No owner";
				}
				html.append("<tr>");
				html.append("<td width=150>" + tp.NameCastl + "</td>");
				html.append("<td width=100>" + tp.Percent + "</td>");
				html.append("<td width=200><font color=" + color + ">" + Owner + "</font></td>");
				html.append("<td width=150>" + tp.siegeDate + "</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			String content = HtmCache.getInstance().getHtml("CommunityBoard/84.htm", player);
			content = content.replace("%stats_castle%", html.toString());
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

	private void showClan(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_data.clan_name, clan_data.clan_id, ally_data.ally_name, clan_data.clan_level, clan_data.reputation_score, clan_data.hasCastle, characters.char_name, ccount FROM `clan_data` LEFT JOIN `characters` ON characters.obj_Id = clan_data.leader_id LEFT JOIN (SELECT clanid, count(0) AS ccount FROM characters WHERE clanid GROUP BY clanid) AS levels ON clan_data.clan_id = levels.clanid LEFT JOIN `ally_data` ON clan_data.ally_id = ally_data.ally_id ORDER BY clan_data.clan_level DESC, clan_data.reputation_score DESC LIMIT " + Config.CB_STAT_LIMIT_TOP_CLANS + ";");
			rs = statement.executeQuery();
			final StringBuilder html = new StringBuilder();
			html.append("<table width=570>");
			while(rs.next())
			{
				final CBStatMan tp = new CBStatMan();
				tp.ClanName = rs.getString("clan_name");
				tp.AllyName = rs.getString("ally_name");
				tp.ReputationClan = rs.getInt("reputation_score");
				tp.ClanLevel = rs.getInt("clan_level");
				tp.hasCastle = rs.getInt("hasCastle");
				String hasCastle = "";
				String castleColor = "D70000";
				switch(tp.hasCastle)
				{
					case 1:
					{
						hasCastle = "Gludio";
						castleColor = "00CC00";
						break;
					}
					case 2:
					{
						hasCastle = "Dion";
						castleColor = "00CC00";
						break;
					}
					case 3:
					{
						hasCastle = "Giran";
						castleColor = "00CC00";
						break;
					}
					case 4:
					{
						hasCastle = "Oren";
						castleColor = "00CC00";
						break;
					}
					case 5:
					{
						hasCastle = "Aden";
						castleColor = "00CC00";
						break;
					}
					case 6:
					{
						hasCastle = "Innadril";
						castleColor = "00CC00";
						break;
					}
					case 7:
					{
						hasCastle = "Goddard";
						castleColor = "00CC00";
						break;
					}
					case 8:
					{
						hasCastle = "Rune";
						castleColor = "00CC00";
						break;
					}
					case 9:
					{
						hasCastle = "Schuttgart";
						castleColor = "00CC00";
						break;
					}
					default:
					{
						hasCastle = player.isLangRus() ? "\u041d\u0435\u0442\u0443" : "No";
						castleColor = "D70000";
						break;
					}
				}
				html.append("<tr>");
				html.append("<td width=150>" + tp.ClanName + "</td>");
				if(tp.AllyName != null)
					html.append("<td width=150>" + tp.AllyName + "</td>");
				else
					html.append("<td width=150>" + (player.isLangRus() ? "\u041d\u0435\u0442 \u0430\u043b\u044c\u044f\u043d\u0441\u0430" : "No alliance") + "</td>");
				html.append("<td width=100>" + tp.ReputationClan + "</td>");
				html.append("<td width=50>" + tp.ClanLevel + "</td>");
				html.append("<td width=100><font color=" + castleColor + ">" + hasCastle + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			String content = HtmCache.getInstance().getHtml("CommunityBoard/85.htm", player);
			content = content.replace("%stats_clan%", html.toString());
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

	private String OnlineTime(final int time, final boolean ru)
	{
		long onlinetimeH;
		if(time / 60 / 60 - 0.5 <= 0.0)
			onlinetimeH = 0L;
		else
			onlinetimeH = Math.round(time / 60 / 60 - 0.5);
		final int onlinetimeM = Math.round(time % 3600 / 60);
		return onlinetimeH + " " + (ru ? "ч" : "h") + " " + onlinetimeM + " " + (ru ? "м" : "m");
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player player)
	{}

	static
	{
		StatBBSManager._Instance = null;
	}

	public class CBStatMan
	{
		public int PlayerId;
		public String ChName;
		public int ChGameTime;
		public int ChPk;
		public int ChPvP;
		public int ChOnOff;
		public int ChSex;
		public String NameCastl;
		public Object siegeDate;
		public String Percent;
		public Object id2;
		public int id;
		public int ClanLevel;
		public int hasCastle;
		public int ReputationClan;
		public String AllyName;
		public String ClanName;
		public String Owner;

		public CBStatMan()
		{
			PlayerId = 0;
			ChName = "";
			ChGameTime = 0;
			ChPk = 0;
			ChPvP = 0;
			ChOnOff = 0;
			ChSex = 0;
		}
	}
}
