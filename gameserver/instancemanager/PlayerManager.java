package l2s.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.ItemTable;

public class PlayerManager
{
	protected static Logger _log;

	public static void saveCharToDisk(final Player cha)
	{
		try
		{
			cha.getInventory().updateDatabase(true);
		}
		catch(Exception e)
		{
			_log.error("Error saving player inventory: ", e);
		}

		try
		{
			cha.store(false);
		}
		catch(Exception e)
		{
			_log.error("Error saving player data: ", e);
		}
	}

	public static void deleteFromClan(final int charId, final int clanId)
	{
		if(clanId == 0)
			return;
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		if(clan != null)
			clan.removeClanMember(charId);
	}

	public static void deleteCharByObjId(final int objid)
	{
		if(objid < 0)
			return;
		if(Config.SERVICES_CHAR_KEY && Config.CHAR_KEYS.containsKey(objid))
			Config.CHAR_KEYS.remove(objid);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE pets FROM pets, items WHERE pets.item_obj_id=items.object_id AND items.owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE char_obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? or friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM heroes_diary WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM buffer_skillsave WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_variables WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM hwid_locks WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM game_bans WHERE (bind_type='player' OR bind_type='chat') AND bind_value=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			PlayerManager._log.error("data error on delete char: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static boolean createDb(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `characters` (account_name, obj_Id, char_name, face, hairStyle, hairColor, sex, karma, pvpkills, pkkills, clanid, createtime, deletetime, title, accesslevel, online, leaveclan, deleteclan, nochannel, pledge_type, pledge_rank, lvl_joined_academy, apprentice) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, player.getObjectId());
			statement.setString(3, player.getName());
			statement.setInt(4, player.getFace());
			statement.setInt(5, player.getHairStyle());
			statement.setInt(6, player.getHairColor());
			statement.setInt(7, player.getSex());
			statement.setInt(8, player.getKarma());
			statement.setInt(9, player.getPvpKills());
			statement.setInt(10, player.getPkKills());
			statement.setInt(11, player.getClanId());
			statement.setLong(12, player.getCreateTime() / 1000L);
			statement.setInt(13, player.getDeleteTimer());
			statement.setString(14, player.getTitle());
			statement.setInt(15, player.getAccessLevel());
			statement.setInt(16, player.isOnline() ? 1 : 0);
			statement.setLong(17, player.getLeaveClanTime() / 1000L);
			statement.setLong(18, player.getDeleteClanTime() / 1000L);
			statement.setLong(19, player.getNoChannel() > 0L ? player.getNoChannel() / 1000L : player.getNoChannel());
			statement.setInt(20, player.getPledgeType());
			statement.setInt(21, player.getPowerGrade());
			statement.setInt(22, player.getLvlJoinedAcademy());
			statement.setInt(23, player.getApprentice());
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getTemplate().classId.getId());
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setDouble(5, player.getTemplate().getBaseHpMax(player.getLevel()));
			statement.setDouble(6, player.getTemplate().getBaseMpMax(player.getLevel()));
			statement.setDouble(7, player.getTemplate().getBaseCpMax(player.getLevel()));
			statement.setDouble(8, player.getTemplate().getBaseHpMax(player.getLevel()));
			statement.setDouble(9, player.getTemplate().getBaseMpMax(player.getLevel()));
			statement.setDouble(10, player.getTemplate().getBaseCpMax(player.getLevel()));
			statement.setInt(11, 1);
			statement.setInt(12, 1);
			statement.setInt(13, 1);
			statement.setInt(14, 0);
			statement.executeUpdate();
			if(Config.POST_CHARBRIEF)
			{
				DbUtils.close(statement);
				TextBuilder text = new TextBuilder();
				text.append(Config.POST_BRIEFTEXT);
				statement = con.prepareStatement("INSERT INTO `z_bbs_mail` (`from`, `to`, `tema`, `text`, `datetime`, `read`, `item_id`, `item_count`, `item_ench`, `variation1_id`, `variation2_id`, `variation_stone_id`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, 555);
				statement.setInt(2, player.getObjectId());
				statement.setString(3, Config.POST_BRIEFTHEME);
				statement.setString(4, text.toString());
				statement.setLong(5, System.currentTimeMillis());
				statement.setInt(6, 0);
				statement.setInt(7, Config.POST_BRIEF_ITEM);
				statement.setInt(8, Config.POST_BRIEF_COUNT);
				statement.setInt(9, 0);
				statement.setInt(10, 0);
				statement.setInt(11, 0);
				statement.setInt(12, 0);
				statement.execute();
				text.clear();
				text = null;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public static int getObjectIdByName(final String name)
	{
		int result = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, name);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getInt(1);
		}
		catch(Exception e)
		{
			PlayerManager._log.error("PlayerManager.getObjectIdByName(String): ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public static String getNameByObjectId(final int objectId)
	{
		String result = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=? LIMIT 1");
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getString(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public static String getAccNameByName(final String n)
	{
		String result = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, n);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getString(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public static int accountCharNumber(final String account)
	{
		int number = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			rset = statement.executeQuery();
			if(rset.next())
				number = rset.getInt(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return number;
	}

	public static int[] getCharWeaponById(final int id)
	{
		int[] result = { 0 };
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id,item_id FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
			statement.setInt(1, id);
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int itemId = rset.getInt("item_id");
				if(ItemTable.getInstance().getTemplate(itemId).isWeapon())
				{
					result = new int[] { rset.getInt("object_id"), itemId };
					break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public static void saveCharNameToDB(final int objId, final String name)
	{
		Connection con = null;
		PreparedStatement st = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ? LIMIT 1");
			st.setString(1, name);
			st.setInt(2, objId);
			st.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

	public static String getLastIPByName(final String n)
	{
		String ip = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT last_ip FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, n);
			rset = statement.executeQuery();
			if(rset.next())
				ip = rset.getString(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return ip != null ? ip : "";
	}

	public static String getLastHWIDByName(final String n)
	{
		String hwid = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT last_hwid FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, n);
			rset = statement.executeQuery();
			if(rset.next())
				hwid = rset.getString(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return hwid != null ? hwid : "";
	}

	public static String getAccVal(final String val, final String account)
	{
		String v = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstanceLogin().getConnection();
			statement = con.prepareStatement("SELECT " + val + " FROM accounts WHERE login=? LIMIT 1");
			statement.setString(1, account);
			rset = statement.executeQuery();
			if(rset.next())
				v = rset.getString(1);
		}
		catch(Exception e)
		{
			PlayerManager._log.warn("Could not get " + val + " for acc: " + account + " " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return v;
	}

	public static void initLocks(final Player player)
	{
		if(Config.SERVICES_LOCK_CHAR_HWID)
		{
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT Lock1,Lock2 FROM hwid_locks WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, player.getObjectId());
				rset = statement.executeQuery();
				if(rset.next())
				{
					player.lockChar1 = rset.getString(1);
					player.lockChar2 = rset.getString(2);
				}
			}
			catch(Exception e)
			{
				PlayerManager._log.warn("Could not init locks for player: " + player.toString() + " " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
		}
	}

	static
	{
		PlayerManager._log = LoggerFactory.getLogger(PlayerManager.class);
	}
}
