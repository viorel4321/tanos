package l2s.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.CursedWeapon;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.FeedableBeastInstance;
import l2s.gameserver.model.instances.FestivalMonsterInstance;
import l2s.gameserver.model.instances.GuardInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RiftInvaderInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.utils.Location;

public class CursedWeaponsManager
{
	private static final Logger _log;
	private static CursedWeaponsManager _instance;
	Map<Integer, CursedWeapon> _cursedWeapons;
	private ScheduledFuture<?> _removeTask;
	private static final int CURSEDWEAPONS_MAINTENANCE_INTERVAL = 300000;

	public static CursedWeaponsManager getInstance()
	{
		if(CursedWeaponsManager._instance == null)
			CursedWeaponsManager._instance = new CursedWeaponsManager();
		return CursedWeaponsManager._instance;
	}

	public CursedWeaponsManager()
	{
		_cursedWeapons = new HashMap<Integer, CursedWeapon>();
		if(!Config.ALLOW_CURSED_WEAPONS)
			return;
		CursedWeaponsManager._log.info("CursedWeaponsManager: Initializing");
		load();
		restore();
		checkConditions();
		cancelTask();
		_removeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RemoveTask(), 300000L, 300000L);
		CursedWeaponsManager._log.info("CursedWeaponsManager: Loaded " + _cursedWeapons.size() + " cursed weapon(s).");
	}

	public final void reload()
	{
		CursedWeaponsManager._instance = new CursedWeaponsManager();
	}

	private void load()
	{
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			final File file = new File(Config.DATAPACK_ROOT, "data/cursed_weapons.xml");
			if(!file.exists())
				return;
			final Document doc = factory.newDocumentBuilder().parse(file);
			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if("list".equalsIgnoreCase(n.getNodeName()))
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							final Integer skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
							String name = "Unknown cursed weapon";
							if(attrs.getNamedItem("name") != null)
								name = attrs.getNamedItem("name").getNodeValue();
							else if(ItemTable.getInstance().getTemplate(id) != null)
								name = ItemTable.getInstance().getTemplate(id).getName();
							if(id != 0)
							{
								final CursedWeapon cw = new CursedWeapon(id, skillId, name);
								for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
									if("dropRate".equalsIgnoreCase(cd.getNodeName()))
										cw.setDropRate(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
									else if("duration".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										cw.setDurationMin(Integer.parseInt(attrs.getNamedItem("min").getNodeValue()));
										cw.setDurationMax(Integer.parseInt(attrs.getNamedItem("max").getNodeValue()));
									}
									else if("durationLost".equalsIgnoreCase(cd.getNodeName()))
										cw.setDurationLost(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
									else if("disapearChance".equalsIgnoreCase(cd.getNodeName()))
										cw.setDisapearChance(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
									else if("stageKills".equalsIgnoreCase(cd.getNodeName()))
										cw.setStageKills(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
								_cursedWeapons.put(id, cw);
							}
						}
		}
		catch(Exception e)
		{
			CursedWeaponsManager._log.error("CursedWeaponsManager: Error parsing cursed_weapons file. " + e);
		}
	}

	private void restore()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM cursed_weapons");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int itemId = rset.getInt("item_id");
				final CursedWeapon cw = _cursedWeapons.get(itemId);
				if(cw != null)
				{
					cw.setPlayerId(rset.getInt("player_id"));
					cw.setPlayerKarma(rset.getInt("player_karma"));
					cw.setPlayerPkKills(rset.getInt("player_pkkills"));
					cw.setNbKills(rset.getInt("nb_kills"));
					cw.setLoc(new Location(rset.getInt("x"), rset.getInt("y"), rset.getInt("z")));
					cw.setEndTime(rset.getLong("end_time") * 1000L);
					if(cw.reActivate())
						continue;
					endOfLife(cw);
				}
				else
				{
					removeFromDb(itemId);
					CursedWeaponsManager._log.warn("CursedWeaponsManager: Unknown cursed weapon " + itemId + ", deleted");
				}
			}
		}
		catch(Exception e)
		{
			CursedWeaponsManager._log.warn("CursedWeaponsManager: Could not restore cursed_weapons data: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private void checkConditions()
	{
		Connection con = null;
		PreparedStatement statement1 = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement1 = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=?");
			statement2 = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
			for(final CursedWeapon cw : _cursedWeapons.values())
			{
				final int itemId = cw.getItemId();
				final int skillId = cw.getSkillId();
				boolean foundedInItems = false;
				statement1.setInt(1, skillId);
				statement1.executeUpdate();
				statement2.setInt(1, itemId);
				rset = statement2.executeQuery();
				while(rset.next())
				{
					final int playerId = rset.getInt("owner_id");
					if(!foundedInItems)
					{
						if(playerId != cw.getPlayerId() || cw.getPlayerId() == 0)
						{
							emptyPlayerCursedWeapon(playerId, itemId, cw);
							CursedWeaponsManager._log.info("CursedWeaponsManager[254]: Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
						}
						else
							foundedInItems = true;
					}
					else
					{
						emptyPlayerCursedWeapon(playerId, itemId, cw);
						CursedWeaponsManager._log.info("CursedWeaponsManager[262]: Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
					}
				}
				if(!foundedInItems && cw.getPlayerId() != 0)
				{
					removeFromDb(cw.getItemId());
					CursedWeaponsManager._log.info("CursedWeaponsManager: Unownered weapon, removing from table...");
				}
			}
		}
		catch(Exception e)
		{
			CursedWeaponsManager._log.warn("CursedWeaponsManager: Could not check cursed_weapons data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(statement1);
			DbUtils.closeQuietly(con, statement2, rset);
		}
	}

	private void emptyPlayerCursedWeapon(final int playerId, final int itemId, final CursedWeapon cw)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
			statement.setInt(1, playerId);
			statement.setInt(2, itemId);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
			statement.setInt(1, cw.getPlayerKarma());
			statement.setInt(2, cw.getPlayerPkKills());
			statement.setInt(3, playerId);
			if(statement.executeUpdate() != 1)
				CursedWeaponsManager._log.warn("Error while updating karma & pkkills for userId " + cw.getPlayerId());
			removeFromDb(itemId);
		}
		catch(SQLException ex)
		{}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void removeFromDb(final int itemId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE item_id = ?");
			statement.setInt(1, itemId);
			statement.executeUpdate();
			if(getCursedWeapon(itemId) != null)
				getCursedWeapon(itemId).initWeapon();
		}
		catch(SQLException e)
		{
			CursedWeaponsManager._log.error("CursedWeaponsManager: Failed to remove data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void cancelTask()
	{
		if(_removeTask != null)
		{
			_removeTask.cancel(true);
			_removeTask = null;
		}
	}

	public void endOfLife(final CursedWeapon cw)
	{
		if(cw.isActivated())
		{
			final Player player = cw.getOnlineOwner();
			if(player != null)
			{
				CursedWeaponsManager._log.info("CursedWeaponsManager: " + cw.getName() + " being removed online from " + player + ".");
				player.abortAttack(true, false);
				player.setKarma(cw.getPlayerKarma());
				player.setPkKills(cw.getPlayerPkKills());
				player.setCursedWeaponEquippedId(0);
				player.removeSkill(SkillTable.getInstance().getInfo(cw.getSkillId(), player.getSkillLevel(cw.getSkillId())), false);
				player.getInventory().unEquipItemInBodySlot(16384, null);
				player.store(false);
				if(player.getInventory().destroyItemByItemId(cw.getItemId(), 1L, false) == null)
					CursedWeaponsManager._log.info("CursedWeaponsManager[395]: Error! Cursed weapon not found!!!");
				player.broadcastUserInfo(true);
			}
			else
			{
				CursedWeaponsManager._log.info("CursedWeaponsManager: " + cw.getName() + " being removed offline.");
				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
					statement.setInt(1, cw.getPlayerId());
					statement.setInt(2, cw.getItemId());
					statement.executeUpdate();
					DbUtils.close(statement);
					statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND skill_id=?");
					statement.setInt(1, cw.getPlayerId());
					statement.setInt(2, cw.getSkillId());
					statement.executeUpdate();
					DbUtils.close(statement);
					statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_Id=?");
					statement.setInt(1, cw.getPlayerKarma());
					statement.setInt(2, cw.getPlayerPkKills());
					statement.setInt(3, cw.getPlayerId());
					statement.executeUpdate();
				}
				catch(SQLException e)
				{
					CursedWeaponsManager._log.warn("CursedWeaponsManager: Could not delete : " + e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
			}
		}
		else if(cw.getPlayer() != null && cw.getPlayer().getInventory().getItemByItemId(cw.getItemId()) != null)
		{
			final Player player = cw.getPlayer();
			if(cw.getPlayer().getInventory().destroyItemByItemId(cw.getItemId(), 1L, false) == null)
				CursedWeaponsManager._log.info("CursedWeaponsManager[453]: Error! Cursed weapon not found!!!");
			player.sendChanges();
			player.broadcastUserInfo(true);
		}
		else if(cw.getItem() != null)
		{
			cw.getItem().removeFromDb();
			cw.getItem().deleteMe();
			CursedWeaponsManager._log.info("CursedWeaponsManager: " + cw.getName() + " item has been removed from World.");
		}
		cw.initWeapon();
		removeFromDb(cw.getItemId());
		announce(new SystemMessage(1818).addString(cw.getName()));
	}

	public void saveData(final CursedWeapon cw)
	{
		Connection con = null;
		PreparedStatement statement = null;
		synchronized (cw)
		{
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE item_id = ?");
				statement.setInt(1, cw.getItemId());
				statement.executeUpdate();
				DbUtils.close(statement);
				statement = null;
				if(cw.isActive())
				{
					statement = con.prepareStatement("REPLACE INTO cursed_weapons (item_id, player_id, player_karma, player_pkkills, nb_kills, x, y, z, end_time) VALUES (?,?,?,?,?,?,?,?,?)");
					statement.setInt(1, cw.getItemId());
					statement.setInt(2, cw.getPlayerId());
					statement.setInt(3, cw.getPlayerKarma());
					statement.setInt(4, cw.getPlayerPkKills());
					statement.setInt(5, cw.getNbKills());
					statement.setInt(6, cw.getLoc().x);
					statement.setInt(7, cw.getLoc().y);
					statement.setInt(8, cw.getLoc().z);
					statement.setLong(9, cw.getEndTime() / 1000L);
					statement.executeUpdate();
				}
			}
			catch(SQLException e)
			{
				CursedWeaponsManager._log.error("CursedWeapon: Failed to save data: " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}

	public void saveData()
	{
		for(final CursedWeapon cw : _cursedWeapons.values())
			this.saveData(cw);
	}

	public void checkPlayer(final Player player, final ItemInstance item)
	{
		if(player == null || item == null || player.isInOlympiadMode())
			return;
		final CursedWeapon cw = _cursedWeapons.get(item.getItemId());
		if(cw == null)
			return;
		if(player.getObjectId() == cw.getPlayerId() || cw.getPlayerId() == 0 || cw.isDropped())
		{
			activate(player, item);
			this.showUsageTime(player, cw);
			if(Config.ENABLE_OLYMPIAD && (Olympiad.isRegistered(player) || player.getOlympiadGameId() > -1))
				Olympiad.unRegisterCursed(player, item.getName());
		}
		else
		{
			CursedWeaponsManager._log.warn("CursedWeaponsManager: " + player + " tried to obtain " + item + " in wrong way");
			player.getInventory().destroyItem(item, item.getCount(), true);
		}
	}

	public void activate(final Player player, final ItemInstance item)
	{
		if(player == null || player.isInOlympiadMode())
			return;
		final CursedWeapon cw = _cursedWeapons.get(item.getItemId());
		if(cw == null)
			return;
		if(player.isCursedWeaponEquipped())
		{
			if(player.getCursedWeaponEquippedId() != item.getItemId())
			{
				final CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquippedId());
				cw2.setNbKills(cw2.getStageKills() - 1);
				cw2.increaseKills();
			}
			endOfLife(cw);
			player.getInventory().destroyItem(item, 1L, true);
		}
		else if(cw.getTimeLeft() > 0L)
		{
			cw.activate(player, item);
			this.saveData(cw);
			announce(new SystemMessage(1816).addZoneName(player.getX(), player.getY(), player.getZ()).addString(cw.getName()));
		}
		else
		{
			endOfLife(cw);
			player.getInventory().destroyItem(item, 1L, true);
		}
	}

	public void doLogout(final Player player)
	{
		for(final CursedWeapon cw : _cursedWeapons.values())
			if(player.getInventory().getItemByItemId(cw.getItemId()) != null)
				if(Config.DROP_CURSED_WEAPONS_ON_KICK)
				{
					player.setPvpFlag(0);
					dropPlayer(player);
				}
				else
				{
					cw.setPlayer(null);
					cw.setItem(null);
				}
	}

	public void dropAttackable(final NpcInstance attackable, final Player killer)
	{
		if(killer.isInOlympiadMode() || killer.isCursedWeaponEquipped() || _cursedWeapons.isEmpty())
			return;
		if(attackable.isBoss() || attackable instanceof RiftInvaderInstance || attackable instanceof FestivalMonsterInstance || attackable instanceof GuardInstance || attackable instanceof FeedableBeastInstance)
			return;
		synchronized (_cursedWeapons)
		{
			int num = 0;
			short count = 0;
			byte breakFlag = 0;
			while(breakFlag == 0)
			{
				num = _cursedWeapons.keySet().toArray(new Integer[_cursedWeapons.size()])[Rnd.get(_cursedWeapons.size())];
				++count;
				if(_cursedWeapons.get(num) != null && !_cursedWeapons.get(num).isActive())
					breakFlag = 1;
				else
				{
					if(count < getCursedWeapons().size())
						continue;
					breakFlag = 2;
				}
			}
			if(breakFlag == 1)
				_cursedWeapons.get(num).create(attackable, killer, false);
		}
	}

	public void dropPlayer(final Player player)
	{
		final CursedWeapon cw = _cursedWeapons.get(player.getCursedWeaponEquippedId());
		if(cw == null)
			return;
		if(cw.dropIt(player))
		{
			this.saveData(cw);
			announce(new SystemMessage(1815).addZoneName(player.getX(), player.getY(), player.getZ()).addString(cw.getName()));
		}
		else
			endOfLife(cw);
	}

	public void increaseKills(final int itemId)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		if(cw != null)
		{
			cw.increaseKills();
			this.saveData(cw);
		}
	}

	public int getLevel(final int itemId)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		return cw != null ? cw.getLevel() : 0;
	}

	public void announce(final SystemMessage sm)
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(sm);
	}

	public void showUsageTime(final Player player, final int itemId)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		if(cw != null)
			this.showUsageTime(player, cw);
	}

	public void showUsageTime(final Player player, final CursedWeapon cw)
	{
		final SystemMessage sm = new SystemMessage(1814);
		sm.addString(cw.getName());
		sm.addNumber((int) (cw.getTimeLeft() / 60000));
		player.sendPacket(sm);
	}

	public boolean isCursed(final int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}

	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}

	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}

	public CursedWeapon getCursedWeapon(final int itemId)
	{
		return _cursedWeapons.get(itemId);
	}

	static
	{
		_log = LoggerFactory.getLogger(CursedWeaponsManager.class);
	}

	private class RemoveTask implements Runnable
	{
		@Override
		public void run()
		{
			for(final CursedWeapon cw : _cursedWeapons.values())
				if(cw.isActive() && cw.getTimeLeft() <= 0L)
					endOfLife(cw);
		}
	}
}
