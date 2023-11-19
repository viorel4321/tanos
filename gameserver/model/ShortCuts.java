package l2s.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.ShortCutInit;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.item.EtcItemTemplate;

public class ShortCuts
{
	private static Logger _log = LoggerFactory.getLogger(ShortCuts.class);

	private final Player _owner;
	private final ConcurrentHashMap<Integer, ShortCut> _shortCuts = new ConcurrentHashMap<Integer, ShortCut>();

	public ShortCuts(Player owner)
	{
		_owner = owner;
	}

	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.values();
	}

	public ShortCut getShortCut(final int slot, final int page)
	{
		final Player player = getPlayer();
		if(player == null)
			return null;
		ShortCut sc = _shortCuts.get(slot + page * 12);
		if(sc != null && sc.type == 1 && player.getInventory().getItemByObjectId(sc.id) == null)
		{
			player.sendPacket(new SystemMessage(137));
			deleteShortCut(sc.slot, sc.page);
			sc = null;
		}
		return sc;
	}

	public void registerShortCut(final ShortCut shortcut)
	{
		final ShortCut oldShortCut = _shortCuts.put(shortcut.slot + 12 * shortcut.page, shortcut);
		registerShortCutInDb(shortcut, oldShortCut);
	}

	private synchronized void registerShortCutInDb(final ShortCut shortcut, final ShortCut oldShortCut)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		if(oldShortCut != null)
			deleteShortCutFromDb(oldShortCut);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_shortcuts SET char_obj_id=?,slot=?,page=?,type=?,shortcut_id=?,level=?,class_index=?");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, shortcut.slot);
			statement.setInt(3, shortcut.page);
			statement.setInt(4, shortcut.type);
			statement.setInt(5, shortcut.id);
			statement.setInt(6, shortcut.level);
			statement.setInt(7, player.getActiveClassId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("could not store shortcuts:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void deleteShortCutFromDb(final ShortCut shortcut)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, shortcut.slot);
			statement.setInt(3, shortcut.page);
			statement.setInt(4, player.getActiveClassId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("could not delete shortcuts:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deleteShortCut(final int slot, final int page)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		final ShortCut old = _shortCuts.remove(slot + page * 12);
		if(old == null)
			return;
		deleteShortCutFromDb(old);
		if(old.type == 2)
		{
			player.sendPacket(new ShortCutInit(player));
			for(final int shotId : player.getAutoSoulShot())
				player.sendPacket(new ExAutoSoulShot(shotId, true));
		}
		if(old.type == 1)
		{
			final ItemInstance item = player.getInventory().getItemByObjectId(old.id);
			if(item != null && item.getItemType() == EtcItemTemplate.EtcItemType.SHOT)
			{
				player.removeAutoSoulShot(item.getItemId());
				player.sendPacket(new ExAutoSoulShot(item.getItemId(), false));
				for(final int shotId2 : player.getAutoSoulShot())
					player.sendPacket(new ExAutoSoulShot(shotId2, true));
			}
		}
	}

	public void deleteShortCutByObjectId(final int objectId)
	{
		for(final ShortCut shortcut : _shortCuts.values())
			if(shortcut != null && shortcut.type == 1 && shortcut.id == objectId)
				deleteShortCut(shortcut.slot, shortcut.page);
	}

	public void deleteShortCutBySkillId(final int skillId)
	{
		for(final ShortCut shortcut : _shortCuts.values())
			if(shortcut != null && shortcut.type == 2 && shortcut.id == skillId)
				deleteShortCut(shortcut.slot, shortcut.page);
	}

	public void restore()
	{
		final Player player = getPlayer();
		if(player == null)
			return;

		_shortCuts.clear();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_obj_id, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getActiveClassId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int slot = rset.getInt("slot");
				final int page = rset.getInt("page");
				final int type = rset.getInt("type");
				final int id = rset.getInt("shortcut_id");
				final int level = rset.getInt("level");
				final ShortCut sc = new ShortCut(slot, page, type, id, level);
				_shortCuts.put(slot + page * 12, sc);
			}
		}
		catch(Exception e)
		{
			_log.error("could not store shortcuts:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		for(final ShortCut sc2 : _shortCuts.values())
		{
			if(sc2.type == 1)
			{
				if(player.getInventory().getItemByObjectId(sc2.id) == null)
					deleteShortCut(sc2.slot, sc2.page);
				else
				{
					if(sc2.type != 2)
						continue;

					final Skill s = SkillTable.getInstance().getInfo(sc2.id, sc2.level);
					if(s == null)
						continue;

					if(!s.isValidateable())
						continue;

					if(player._skills.containsKey(sc2.id))
						continue;

					deleteShortCut(sc2.slot, sc2.page);
				}
			}
		}
	}

	public Player getPlayer()
	{
		return _owner;
	}
}
