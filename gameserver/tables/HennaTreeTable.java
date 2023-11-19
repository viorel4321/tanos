package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.instances.HennaInstance;
import l2s.gameserver.templates.HennaTemplate;

public class HennaTreeTable
{
	private static Logger _log;
	private static final HennaTreeTable _instance;
	private HashMap<ClassId, ArrayList<HennaInstance>> _hennaTrees;
	private boolean _initialized;

	public static HennaTreeTable getInstance()
	{
		return HennaTreeTable._instance;
	}

	private HennaTreeTable()
	{
		_initialized = true;
		_hennaTrees = new HashMap<ClassId, ArrayList<HennaInstance>>();
		int classId = 0;
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet classlist = null;
		ResultSet hennatree = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_name, id, parent_id, parent_id2 FROM class_list ORDER BY id");
			statement2 = con.prepareStatement("SELECT class_id, symbol_id FROM henna_trees where class_id=? ORDER BY symbol_id");
			classlist = statement.executeQuery();
			ArrayList<HennaInstance> list = new ArrayList<HennaInstance>();
			while(classlist.next())
			{
				list = new ArrayList<HennaInstance>();
				classId = classlist.getInt("id");
				statement2.setInt(1, classId);
				hennatree = statement2.executeQuery();
				while(hennatree.next())
				{
					final short id = hennatree.getShort("symbol_id");
					final HennaTemplate template = HennaTable.getInstance().getTemplate(id);
					if(template == null)
						return;
					final HennaInstance temp = new HennaInstance(template);
					temp.setSymbolId(id);
					temp.setItemIdDye(template.getDyeId());
					temp.setAmountDyeRequire(template.getAmountDyeRequire());
					temp.setPrice(template.getPrice());
					temp.setStatINT(template.getStatINT());
					temp.setStatSTR(template.getStatSTR());
					temp.setStatCON(template.getStatCON());
					temp.setStatMEN(template.getStatMEN());
					temp.setStatDEX(template.getStatDEX());
					temp.setStatWIT(template.getStatWIT());
					list.add(temp);
				}
				_hennaTrees.put(ClassId.values()[classId], list);
				count += list.size();
				DbUtils.close(hennatree);
			}
		}
		catch(Exception e)
		{
			HennaTreeTable._log.warn("error while creating henna tree for classId " + classId + "\t" + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(statement2, hennatree);
			DbUtils.closeQuietly(con, statement, classlist);
		}
		HennaTreeTable._log.info("HennaTreeTable: Loaded " + count + " Henna Tree Templates.");
	}

	public HennaInstance[] getAvailableHenna(final ClassId classId)
	{
		final ArrayList<HennaInstance> henna = _hennaTrees.get(classId);
		if(henna == null || henna.size() == 0)
		{
			if(Config.DEBUG)
				HennaTreeTable._log.warn("Hennatree for class " + classId + " is not defined!");
			return new HennaInstance[0];
		}
		return henna.toArray(new HennaInstance[henna.size()]);
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	static
	{
		HennaTreeTable._log = LoggerFactory.getLogger(HennaTreeTable.class);
		_instance = new HennaTreeTable();
	}
}
