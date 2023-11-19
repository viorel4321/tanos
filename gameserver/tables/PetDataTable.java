package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.PetData;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.items.ItemInstance;

public class PetDataTable
{
	private static final Logger _log;
	private static PetDataTable _instance;
	private HashMap<Integer, PetData> _pets;
	public static final int[] _itemControlIds;

	public static PetDataTable getInstance()
	{
		return PetDataTable._instance;
	}

	public void reload()
	{
		PetDataTable._instance = new PetDataTable();
	}

	private PetDataTable()
	{
		_pets = new HashMap<Integer, PetData>(1200, 0.95f);
		FillPetDataTable();
	}

	public PetData getInfo(final int petNpcId, final int level)
	{
		return _pets.get(petNpcId * 100 + level);
	}

	private void FillPetDataTable()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id, level, exp, hp, mp, patk, pdef, matk, mdef, acc, evasion, crit, speed, atk_speed, cast_speed, max_meal, battle_meal, normal_meal, loadMax, hpregen, mpregen FROM pet_data");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final PetData petData = new PetData();
				petData.setID(rset.getInt("id"));
				petData.setLevel(rset.getInt("level"));
				petData.setExp(rset.getInt("exp"));
				petData.setHP(rset.getInt("hp"));
				petData.setMP(rset.getInt("mp"));
				petData.setPAtk(rset.getInt("patk"));
				petData.setPDef(rset.getInt("pdef"));
				petData.setMAtk(rset.getInt("matk"));
				petData.setMDef(rset.getInt("mdef"));
				petData.setAccuracy(rset.getInt("acc"));
				petData.setEvasion(rset.getInt("evasion"));
				petData.setCritical(rset.getInt("crit"));
				petData.setSpeed(rset.getInt("speed"));
				petData.setAtkSpeed(rset.getInt("atk_speed"));
				petData.setCastSpeed(rset.getInt("cast_speed"));
				petData.setFeedMax(rset.getInt("max_meal"));
				petData.setFeedBattle(rset.getInt("battle_meal"));
				petData.setFeedNormal(rset.getInt("normal_meal"));
				petData.setMaxLoad(rset.getInt("loadMax"));
				petData.setHpRegen(rset.getInt("hpregen"));
				petData.setMpRegen(rset.getInt("mpregen"));
				_pets.put(petData.getID() * 100 + petData.getLevel(), petData);
			}
		}
		catch(Exception e)
		{
			PetDataTable._log.error("Cannot fill up PetDataTable:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		PetDataTable._log.info("PetDataTable: Loaded " + _pets.size() + " pets.");
	}

	public static boolean isPetControlItem(final ItemInstance item)
	{
		for(final int id : PetDataTable._itemControlIds)
			if(item.getItemId() == id)
				return true;
		return false;
	}

	public static void deletePet(final ItemInstance item, final Creature owner)
	{
		int petObjectId = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT objId FROM pets WHERE item_obj_id=?");
			statement.setInt(1, item.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
				petObjectId = rset.getInt("objId");
			DbUtils.closeQuietly(statement, rset);
			final Servitor summon = owner.getServitor();
			if(summon != null && summon.getObjectId() == petObjectId)
				summon.unSummon();
			final Player player = owner.getPlayer();
			if(player != null && player.isMounted() && player.getMountObjId() == petObjectId)
				player.setMount(0, 0, 0);
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, item.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			PetDataTable._log.error("could not delete pet:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static void unSummonPet(final ItemInstance oldItem, final Creature owner)
	{
		int petObjectId = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT objId FROM pets WHERE item_obj_id=?");
			statement.setInt(1, oldItem.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
				petObjectId = rset.getInt("objId");
			if(owner == null)
				return;
			final Servitor summon = owner.getServitor();
			if(summon != null && summon.getObjectId() == petObjectId)
				summon.unSummon();
			final Player player = owner.getPlayer();
			if(player != null && player.isMounted() && player.getMountObjId() == petObjectId)
				player.setMount(0, 0, 0);
		}
		catch(Exception e)
		{
			PetDataTable._log.error("could not unsummon pet:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static boolean petNameExist(final String n)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT name FROM pets WHERE item_obj_id>0");
			rset = statement.executeQuery();
			while(rset.next())
				if(n.equalsIgnoreCase(rset.getString("name")))
					return true;
		}
		catch(Exception e)
		{
			PetDataTable._log.error("could not check pet name:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return false;
	}

	public static int getSummonId(final ItemInstance item)
	{
		int npcId = 0;
		switch(item.getItemId())
		{
			case 2375:
			{
				npcId = 12077;
				break;
			}
			case 3500:
			{
				npcId = Config.ADDON ? 0 : 12311;
				break;
			}
			case 3501:
			{
				npcId = Config.ADDON ? 0 : 12312;
				break;
			}
			case 3502:
			{
				npcId = 12313;
				break;
			}
			case 4422:
			{
				npcId = 12526;
				break;
			}
			case 4423:
			{
				npcId = 12527;
				break;
			}
			case 4424:
			{
				npcId = 12528;
				break;
			}
			case 4425:
			{
				npcId = 12564;
				break;
			}
			case 6648:
			{
				npcId = 12780;
				break;
			}
			case 6650:
			{
				npcId = 12781;
				break;
			}
			case 6649:
			{
				npcId = 12782;
				break;
			}
		}
		return npcId;
	}

	static
	{
		_log = LoggerFactory.getLogger(PetDataTable.class);
		PetDataTable._instance = new PetDataTable();
		_itemControlIds = new int[] { 2375, 3500, 3501, 3502, 4422, 4423, 4424, 4425, 6648, 6649, 6650 };
	}
}
