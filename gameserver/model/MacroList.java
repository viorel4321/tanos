package l2s.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.network.l2.s2c.SendMacroList;
import l2s.gameserver.utils.Strings;

public class MacroList
{
	private static Logger _log = LoggerFactory.getLogger(MacroList.class);

	private final Player _owner;
	private final HashMap<Integer, Macro> _macroses = new HashMap<Integer, Macro>();

	private int _revision;
	private int _macroId;

	public MacroList(Player owner)
	{
		_owner = owner;
		_revision = 1;
		_macroId = 1000;
	}

	public int getRevision()
	{
		return _revision;
	}

	public Macro[] getAllMacroses()
	{
		return _macroses.values().toArray(new Macro[_macroses.size()]);
	}

	public Macro getMacro(final int id)
	{
		return _macroses.get(id - 1);
	}

	public void registerMacro(final Macro macro)
	{
		if(macro.id == 0)
		{
			macro.id = _macroId++;
			while(_macroses.get(macro.id) != null)
				macro.id = _macroId++;
			_macroses.put(macro.id, macro);
			registerMacroInDb(macro);
		}
		else
		{
			final Macro old = _macroses.put(macro.id, macro);
			if(old != null)
				deleteMacroFromDb(old);
			registerMacroInDb(macro);
		}
		sendUpdate();
	}

	public void deleteMacro(final int id)
	{
		final Macro toRemove = _macroses.get(id);
		if(toRemove != null)
			deleteMacroFromDb(toRemove);
		_macroses.remove(id);
		sendUpdate();
	}

	public void sendUpdate()
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		++_revision;
		final Macro[] all = getAllMacroses();
		if(all.length == 0)
			player.sendPacket(new SendMacroList(_revision, all.length, null));
		else
			for(final Macro m : all)
				player.sendPacket(new SendMacroList(_revision, all.length, m));
	}

	private void registerMacroInDb(final Macro macro)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, macro.id);
			statement.setInt(3, macro.icon);
			statement.setString(4, macro.name);
			statement.setString(5, macro.descr);
			statement.setString(6, macro.acronym);
			final StringBuffer sb = new StringBuffer();
			for(final Macro.L2MacroCmd cmd : macro.commands)
			{
				sb.append(cmd.type).append(',');
				sb.append(cmd.d1).append(',');
				sb.append(cmd.d2);
				if(cmd.cmd != null && cmd.cmd.length() > 0)
					sb.append(',').append(cmd.cmd);
				sb.append(';');
			}
			statement.setString(7, sb.toString());
			statement.execute();
		}
		catch(Exception e)
		{
			MacroList._log.error("could not store macro: " + macro.toString(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void deleteMacroFromDb(final Macro macro)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, macro.id);
			statement.execute();
		}
		catch(Exception e)
		{
			MacroList._log.error("could not delete macro:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restore()
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		_macroses.clear();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
				try
				{
					final int id = rset.getInt("id");
					final int icon = rset.getInt("icon");
					final String name = Strings.stripSlashes(rset.getString("name"));
					final String descr = Strings.stripSlashes(rset.getString("descr"));
					final String acronym = Strings.stripSlashes(rset.getString("acronym"));
					final List<Macro.L2MacroCmd> commands = new ArrayList<Macro.L2MacroCmd>();
					final StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
					while(st1.hasMoreTokens())
					{
						final StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
						final int type = Integer.parseInt(st2.nextToken());
						final int d1 = Integer.parseInt(st2.nextToken());
						final int d2 = Integer.parseInt(st2.nextToken());
						String cmd = "";
						if(st2.hasMoreTokens())
							cmd = st2.nextToken();
						final Macro.L2MacroCmd mcmd = new Macro.L2MacroCmd(commands.size(), type, d1, d2, cmd);
						commands.add(mcmd);
					}
					final Macro m = new Macro(id, icon, name, descr, acronym, commands.toArray(new Macro.L2MacroCmd[commands.size()]));
					_macroses.put(m.id, m);
				}
				catch(NoSuchElementException e)
				{
					MacroList._log.warn(player.getName() + "/" + player.getObjectId() + ": bad macros parsing (NoSuchElementException) - check database manualy");
					e.printStackTrace();
				}
				catch(NumberFormatException e2)
				{
					MacroList._log.warn(player.getName() + "/" + player.getObjectId() + ": bad macros parsing (NumberFormatException) - check database manualy");
					e2.printStackTrace();
				}
		}
		catch(Exception e3)
		{
			MacroList._log.error("could not restore shortcuts:", e3);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public Player getPlayer()
	{
		return _owner;
	}
}
