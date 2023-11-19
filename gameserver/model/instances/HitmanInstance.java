package l2s.gameserver.model.instances;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Util;

public class HitmanInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private static final Logger _log = LoggerFactory.getLogger(HitmanInstance.class);

	public static List<Integer> _orderPlayer = new ArrayList<Integer>();
	private static final SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");

	public HitmanInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		String val = "";
		String val2 = "";
		String val3 = "";
		if(st.countTokens() >= 1)
			val = st.nextToken();
		if(st.countTokens() >= 1)
			val2 = st.nextToken();
		if(st.countTokens() >= 1)
			val3 = st.nextToken();
		final boolean en = !player.isLangRus();
		if(actualCommand.equalsIgnoreCase("order"))
		{
			if(val.equals("") || val2.equals("") || val3.equals(""))
				return;
			if(!Util.isMatchingRegexp(val2, "[0-9]{1,10}"))
			{
				if(en)
					player.sendMessage("Wrong price. Try again.");
				else
					player.sendMessage("\u041d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u0430\u044f \u0446\u0435\u043d\u0430. \u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0435\u0449\u0435 \u0440\u0430\u0437.");
				return;
			}
			final int target = getObjId(val);
			final int price = Integer.parseInt(val2);
			final int client = player.getObjectId();
			final int min_item = val3.equalsIgnoreCase(Config.HITMAN_ITEM_NAME2) ? Config.HITMAN_MIN_ITEM2 : Config.HITMAN_MIN_ITEM;
			final String item_name = val3.equalsIgnoreCase(Config.HITMAN_ITEM_NAME2) ? Config.HITMAN_ITEM_NAME2 : Config.HITMAN_ITEM_NAME;
			final int item_id = val3.equalsIgnoreCase(Config.HITMAN_ITEM_NAME2) ? Config.HITMAN_ITEM_ID2 : Config.HITMAN_ITEM_ID;
			if(price < min_item)
			{
				if(en)
					player.sendMessage("Minimum price: " + min_item + " " + item_name);
				else
					player.sendMessage("\u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0446\u0435\u043d\u0430: " + min_item + " " + item_name);
				return;
			}
			if(price > 2100000000)
			{
				if(en)
					player.sendMessage("Maximum price: 2,1 billion");
				else
					player.sendMessage("\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0446\u0435\u043d\u0430: 2,1 \u043c\u043b\u0440\u0434");
				return;
			}
			if(target == 0)
			{
				if(en)
					player.sendMessage("Char with name " + val + " not found.");
				else
					player.sendMessage("\u041f\u0435\u0440\u0441\u043e\u043d\u0430\u0436 \u0441 \u043d\u0438\u043a\u043e\u043c " + val + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d.");
				return;
			}
			if(player.getObjectId() == target)
			{
				if(en)
					player.sendMessage("You can not order yourself.");
				else
					player.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0437\u0430\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u0435\u0431\u044f.");
				return;
			}
			if(Functions.getItemCount(player, item_id) < price)
			{
				if(en)
					player.sendMessage("Not enough " + item_name);
				else
					player.sendMessage("\u0423 \u0432\u0430\u0441 \u043d\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 " + item_name);
				return;
			}
			if(checkObjId(target))
			{
				if(en)
					player.sendMessage("This char has already ordered.");
				else
					player.sendMessage("\u0414\u0430\u043d\u043d\u044b\u0439 \u043f\u0435\u0440\u0441\u043e\u043d\u0430\u0436 \u0443\u0436\u0435 \u0437\u0430\u043a\u0430\u0437\u0430\u043d.");
				return;
			}
			player.getInventory().destroyItemByItemId(item_id, price, true);
			player.getInventory().updateDatabase(false);
			player.sendPacket(new ItemList(player, false));
			final String msg = getName(client) + " \u0437\u0430\u043a\u0430\u0437\u0430\u043b(\u0430) " + getName(target) + " \u0437\u0430 " + price + " " + item_name;
			Announcements.getInstance().announceToAll(msg);
			if(Config.HITMAN_LOGGING_ENABLE)
				RecordLog(msg);
			Connection con = null;
			final long time = Config.HITMAN_ORDER_DAYS * 86400000 + System.currentTimeMillis();
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO orderlist(client, target, price, item, end_time) VALUES (?,?,?,?,?)");
				statement.setInt(1, client);
				statement.setInt(2, target);
				statement.setInt(3, price);
				statement.setInt(4, val3.equalsIgnoreCase(Config.HITMAN_ITEM_NAME2) ? 2 : 1);
				statement.setLong(5, time);
				statement.execute();
				statement.close();
				statement = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con);
			}
			updateOrderPlayer();
		}
		else if(actualCommand.equalsIgnoreCase("setorder"))
		{
			final String htmContent = HtmCache.getInstance().getHtml("default/10050-1.htm", player);
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml(htmContent);
			html.replace("%i%", Config.HITMAN_ITEM_NAME + ";" + Config.HITMAN_ITEM_NAME2);
			html.replace("%nid%", "npc_" + getObjectId() + "_order");
			player.sendPacket(html);
			html = null;
		}
		else if(actualCommand.equalsIgnoreCase("list"))
		{
			final String htmContent = HtmCache.getInstance().getHtml("default/10050-2.htm", player);
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml(htmContent);
			html.replace("%list%", getOrderList(en));
			player.sendPacket(html);
			html = null;
		}
		else if(actualCommand.startsWith("takeorder"))
		{
			if(val.equals(""))
				return;
			final int target = Integer.parseInt(val);
			if(target <= 0)
				return;
			takeOrder(player, target);
		}
		else if(actualCommand.startsWith("myorder"))
		{
			final String htmContent = HtmCache.getInstance().getHtml("default/10050-3.htm", player);
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml(htmContent);
			html.replace("%list%", playerOrderList(player.getObjectId(), en));
			player.sendPacket(html);
			html = null;
		}
		else if(actualCommand.startsWith("deleteorder"))
		{
			if(val.equals(""))
				return;
			final int target = Integer.parseInt(val);
			if(target <= 0)
				return;
			deletePlayerOrder(player.getObjectId(), target);
			if(en)
				player.sendMessage("Order on " + getName(target) + " canceled.");
			else
				player.sendMessage("\u0417\u0430\u043a\u0430\u0437 \u043d\u0430 " + getName(target) + " \u043e\u0442\u043c\u0435\u043d\u0435\u043d.");
		}
		else if(actualCommand.startsWith("orderview"))
		{
			if(val.equals(""))
				return;
			final int targ = Integer.parseInt(val);
			if(targ <= 0)
				return;
			final String ta = en ? "Accept" : "\u0412\u0437\u044f\u0442\u044c\u0441\u044f";
			final String ca = en ? "Cancel" : "\u041e\u0442\u043a\u0430\u0437\u0430\u0442\u044c\u0441\u044f";
			final String htmContent2 = HtmCache.getInstance().getHtml("default/10050-4.htm", player);
			NpcHtmlMessage html2 = new NpcHtmlMessage(1);
			html2.setHtml(htmContent2);
			html2.replace("%client%", "<font color=\"LEVEL\">" + getName(getClientId(targ)) + "</font>");
			html2.replace("%target%", "<font color=\"f52e2e\">" + getName(targ) + "</font>");
			html2.replace("%reward%", "<font color=\"00ff00\">" + getReward(targ) + "</font>");
			html2.replace("%rewardname%", "<font color=\"00ff00\">" + getRewardName(targ) + "</font>");
			html2.replace("%execute%", "<button value=\"" + ta + "\" action=\"bypass -h npc_" + getObjectId() + "_takeorder " + val + "\"width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">");
			html2.replace("%breack%", "<button value=\"" + ca + "\" action=\"bypass -h npc_" + getObjectId() + "_deleteorder " + val + "\"width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">");
			player.sendPacket(html2);
			html2 = null;
		}
		super.onBypassFeedback(player, command);
	}

	private static String getName(final int n)
	{
		String name = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
			statement.setInt(1, n);
			rset = statement.executeQuery();
			if(rset.next())
				name = rset.getString("char_name");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return name;
	}

	private static int getObjId(final String n)
	{
		int id = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE accesslevel=0 AND char_name=?");
			statement.setString(1, n);
			rset = statement.executeQuery();
			if(rset.next())
				id = rset.getInt("obj_Id");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return id;
	}

	private String getOrderList(final boolean en)
	{
		Connection con = null;
		final String sql_name = "SELECT target FROM orderlist";
		final String sql_client = "SELECT client FROM orderlist";
		final String sql_price = "SELECT price FROM orderlist";
		final String sql_item = "SELECT item FROM orderlist";
		final String cl = en ? "Client" : "\u041a\u043b\u0438\u0435\u043d\u0442";
		final String targ = en ? "Target" : "\u0426\u0435\u043b\u044c";
		final String pr = en ? "Price" : "\u0426\u0435\u043d\u0430";
		String result = "<tr><td width=\"30\"><font color=\"LEVEL\">" + cl + "</font></td><td width=\"30\"><font color=\"LEVEL\">" + targ + "</font></td><td width=\"30\"><font color=\"LEVEL\">" + pr + "</font></td></tr>";
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT target FROM orderlist");
			PreparedStatement st2 = con.prepareStatement("SELECT price FROM orderlist");
			PreparedStatement st3 = con.prepareStatement("SELECT client FROM orderlist");
			final PreparedStatement st4 = con.prepareStatement("SELECT item FROM orderlist");
			ResultSet rset = st.executeQuery();
			ResultSet rset2 = st2.executeQuery();
			ResultSet rset3 = st3.executeQuery();
			final ResultSet rset4 = st4.executeQuery();
			while(rset.next())
			{
				rset2.next();
				rset3.next();
				rset4.next();
				final String in = rset4.getInt(1) == 2 ? Config.HITMAN_ITEM_NAME2 : Config.HITMAN_ITEM_NAME;
				result = result + "<tr><td width=\"30\">" + getName(rset3.getInt(1)) + "</td>";
				result = result + "<td width=\"30\"><a action=\"bypass -h npc_" + getObjectId() + "_orderview " + rset.getString(1) + "\">" + getName(rset.getInt(1)) + "</a></td>";
				result = result + "<td width=\"30\">" + rset2.getString(1) + " " + in + "</td></tr>";
			}
			st.close();
			st2.close();
			st3.close();
			rset.close();
			rset2.close();
			rset3.close();
			st = null;
			st2 = null;
			st3 = null;
			rset = null;
			rset2 = null;
			rset3 = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return result;
	}

	private boolean checkObjId(final int id)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		boolean res = true;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM orderlist WHERE target=?");
			statement.setInt(1, id);
			rs = statement.executeQuery();
			res = rs.next();
			statement.close();
			statement = null;
			rs = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res = false;
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return res;
	}

	private static void delete()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT target FROM orderlist WHERE `end_time` < " + System.currentTimeMillis());
			rs = statement.executeQuery();
			while(rs.next())
				orderDelete(rs.getInt("target"));
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

	public static void updateOrderPlayer()
	{
		delete();
		final String sql_query = "SELECT target FROM orderlist";
		Connection con = null;
		_orderPlayer.clear();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT target FROM orderlist");
			ResultSet rs = st.executeQuery();
			while(rs.next())
				_orderPlayer.add(rs.getInt(1));
			st.close();
			rs.close();
			st = null;
			rs = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	public static int getReward(final int id)
	{
		final String sql = "SELECT price FROM orderlist WHERE target=?";
		Connection con = null;
		int price = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT price FROM orderlist WHERE target=?");
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			while(rs.next())
				price = rs.getInt(1);
			st.close();
			rs.close();
			st = null;
			rs = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return price;
	}

	public static String getRewardName(final int id)
	{
		final String sql = "SELECT item FROM orderlist WHERE target=?";
		Connection con = null;
		int item = 1;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT item FROM orderlist WHERE target=?");
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			while(rs.next())
				item = rs.getInt(1);
			st.close();
			rs.close();
			st = null;
			rs = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return item == 2 ? Config.HITMAN_ITEM_NAME2 : Config.HITMAN_ITEM_NAME;
	}

	public static void orderDelete(final int id)
	{
		final String sql = "DELETE FROM orderlist WHERE target=?";
		final String sql2 = "DELETE FROM orderplayer WHERE target=?";
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("DELETE FROM orderlist WHERE target=?");
			PreparedStatement st2 = con.prepareStatement("DELETE FROM orderplayer WHERE target=?");
			st.setInt(1, id);
			st2.setInt(1, id);
			st.execute();
			st2.execute();
			st.close();
			st2.close();
			st = null;
			st2 = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	public static boolean getPlayerOrder(final int killer_id, final int target_id)
	{
		final String sql = "SELECT COUNT(*) FROM orderplayer WHERE killer=? AND target=?";
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM orderplayer WHERE killer=? AND target=?");
			st.setInt(1, killer_id);
			st.setInt(2, target_id);
			ResultSet rs = st.executeQuery();
			while(rs.next())
				if(rs.getInt(1) == 0)
					return false;
			st.close();
			rs.close();
			st = null;
			rs = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return true;
	}

	private int getOrderCount(final int id)
	{
		final String sql = "SELECT COUNT(*) FROM orderplayer where killer=?";
		Connection con = null;
		int count = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM orderplayer where killer=?");
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			while(rs.next())
				count = rs.getInt(1);
			st.close();
			rs.close();
			st = null;
			rs = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return count;
	}

	private void takeOrder(final Player killer, final int target_id)
	{
		final boolean en = !killer.isLangRus();
		if(killer.getObjectId() == target_id)
		{
			if(en)
				killer.sendMessage("You can not take an order on yourself.");
			else
				killer.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0432\u0437\u044f\u0442\u044c \u0437\u0430\u043a\u0430\u0437 \u043d\u0430 \u0441\u0430\u043c\u043e\u0433\u043e \u0441\u0435\u0431\u044f.");
			return;
		}
		if(killer.getObjectId() == getClientId(target_id))
		{
			if(en)
				killer.sendMessage("You can not take your own order.");
			else
				killer.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0432\u0437\u044f\u0442\u044c \u0441\u043e\u0431\u0441\u0442\u0432\u0435\u043d\u043d\u044b\u0439 \u0437\u0430\u043a\u0430\u0437.");
			return;
		}
		boolean ok = false;
		if(Config.HITMAN_ORDER_LIMIT == 0)
			ok = true;
		else if(getOrderCount(killer.getObjectId()) < Config.HITMAN_ORDER_LIMIT)
			ok = true;
		if(ok)
		{
			final String sql = "INSERT INTO orderplayer(killer, target) VALUES (?, ?)";
			final String sql2 = "SELECT target FROM orderplayer WHERE killer=?";
			Connection con = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT target FROM orderplayer WHERE killer=?");
				statement.setInt(1, killer.getObjectId());
				ResultSet rs = statement.executeQuery();
				while(rs.next())
					if(rs.getInt(1) == target_id)
					{
						if(en)
							killer.sendMessage("You already have this order.");
						else
							killer.sendMessage("\u0423 \u0432\u0430\u0441 \u0443\u0436\u0435 \u0435\u0441\u0442\u044c \u044d\u0442\u043e\u0442 \u0437\u0430\u043a\u0430\u0437.");
						return;
					}
				statement.close();
				rs.close();
				statement = null;
				rs = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con);
			}
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO orderplayer(killer, target) VALUES (?, ?)");
				statement.setInt(1, killer.getObjectId());
				statement.setInt(2, target_id);
				statement.execute();
				statement.close();
				statement = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con);
			}
			if(en)
				killer.sendMessage("You took order on " + getName(target_id));
			else
				killer.sendMessage("\u0412\u044b \u0432\u0437\u044f\u043b\u0438 \u0437\u0430\u043a\u0430\u0437 \u043d\u0430 " + getName(target_id));
		}
		else if(en)
			killer.sendMessage("You have maximum orders count.");
		else
			killer.sendMessage("\u0423 \u0432\u0430\u0441 \u043c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u0447\u0438\u0441\u043b\u043e \u0437\u0430\u043a\u0430\u0437\u043e\u0432.");
	}

	private String playerOrderList(final int id, final boolean en)
	{
		final String sql = "SELECT target FROM orderplayer WHERE killer=?";
		Connection con = null;
		String result = "";
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT target FROM orderplayer WHERE killer=?");
			statement.setInt(1, id);
			final ResultSet rs = statement.executeQuery();
			final String det = en ? "Detail" : "\u041f\u043e\u0434\u0440\u043e\u0431\u043d\u0435\u0435";
			while(rs.next())
			{
				result = result + "<tr><td width=\"35\"><font color=\"LEVEL\">" + getName(rs.getInt(1)) + "</font></td>";
				result = result + "<td width=\"35\"><a action=\"bypass -h npc_" + getObjectId() + "_orderview " + rs.getString(1) + "\">" + det + "</a></td></tr>";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		if(result.equalsIgnoreCase(""))
			return en ? "You have no orders" : "\u0423 \u0432\u0430\u0441 \u043d\u0435\u0442 \u0437\u0430\u043a\u0430\u0437\u043e\u0432";
		return result;
	}

	private void deletePlayerOrder(final int killer_id, final int target_id)
	{
		final String sql = "DELETE FROM orderplayer WHERE killer=? AND target=?";
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("DELETE FROM orderplayer WHERE killer=? AND target=?");
			st.setInt(1, killer_id);
			st.setInt(2, target_id);
			st.execute();
			st.close();
			st = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	private static int getClientId(final int target)
	{
		final String sql = "SELECT client FROM orderlist WHERE target=?";
		Connection con = null;
		int result = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			final PreparedStatement st = con.prepareStatement("SELECT client FROM orderlist WHERE target=?");
			st.setInt(1, target);
			final ResultSet rs = st.executeQuery();
			while(rs.next())
				result = rs.getInt(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return result;
	}

	public static boolean isRevenge(final int client_id, final int target_id)
	{
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		boolean ok = false;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT client FROM orderlist WHERE target=?");
			st.setInt(1, target_id);
			rs = st.executeQuery();
			while(rs.next())
				if(rs.getInt(1) == client_id)
					ok = true;
			st.close();
			rs.close();
			st = null;
			rs = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return ok;
	}

	public static void AnnounceStart()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoAnnouncements(), Config.HITMAN_ANNOUNCE_TIME * 60000L, Config.HITMAN_ANNOUNCE_TIME * 60000L);
	}

	public static void RecordLog(final String str)
	{
		final String today = _formatter.format(new Date());
		FileWriter save = null;
		try
		{
			final File file = new File("./log/hitman.txt");
			save = new FileWriter(file, true);
			final String out = "[" + today + "] --> " + str + " \r\n";
			save.write(out);
		}
		catch(IOException e)
		{
			_log.error("Hitman could not be saved: ", e);
		}
		finally
		{
			try
			{
				save.close();
			}
			catch(Exception ex)
			{}
		}
	}

	@Override
	public Clan getClan()
	{
		return null;
	}

	public static class AutoAnnouncements implements Runnable
	{
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll(Config.HITMAN_ANNOUNCE_TEXT);
		}
	}
}
