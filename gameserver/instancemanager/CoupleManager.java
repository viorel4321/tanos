package l2s.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Couple;
import l2s.gameserver.network.l2.components.CustomMessage;

public class CoupleManager
{
	protected static Logger _log;
	private static CoupleManager _instance;
	private List<Couple> _couples;
	private volatile List<Couple> _deletedCouples;

	public static CoupleManager getInstance()
	{
		if(CoupleManager._instance == null)
			new CoupleManager();
		return CoupleManager._instance;
	}

	public CoupleManager()
	{
		(CoupleManager._instance = this).load();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new StoreTask(), 600000L, 600000L);
	}

	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM couples ORDER BY id");
			rs = statement.executeQuery();
			while(rs.next())
			{
				final Couple c = new Couple(rs.getInt("id"));
				c.setPlayer1Id(rs.getInt("player1Id"));
				c.setPlayer2Id(rs.getInt("player2Id"));
				c.setMaried(rs.getBoolean("maried"));
				c.setAffiancedDate(rs.getLong("affiancedDate"));
				c.setWeddingDate(rs.getLong("weddingDate"));
				getCouples().add(c);
			}
		}
		catch(Exception e)
		{
			CoupleManager._log.error("Exception: CoupleManager.load(): " + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public final Couple getCouple(final int coupleId)
	{
		for(final Couple c : getCouples())
			if(c.getId() == coupleId)
				return c;
		return null;
	}

	public void engage(final Player cha)
	{
		final int chaId = cha.getObjectId();
		for(final Couple cl : getCouples())
			if(cl.getPlayer1Id() == chaId || cl.getPlayer2Id() == chaId)
			{
				if(cl.getMaried())
					cha.setMaried(true);
				cha.setCoupleId(cl.getId());
				if(cl.getPlayer1Id() == chaId)
					cha.setPartnerId(cl.getPlayer2Id());
				else
					cha.setPartnerId(cl.getPlayer1Id());
			}
	}

	public void notifyPartner(final Player cha)
	{
		if(cha.getPartnerId() != 0)
		{
			final Player partner = GameObjectsStorage.getPlayer(cha.getPartnerId());
			if(partner != null)
				partner.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.CoupleManager.PartnerEntered"));
			else if(Config.DEBUG)
				CoupleManager._log.info(cha + " partner not in world.");
		}
	}

	public void createCouple(final Player player1, final Player player2)
	{
		if(player1 != null && player2 != null && player1.getPartnerId() == 0 && player2.getPartnerId() == 0)
			getCouples().add(new Couple(player1, player2));
	}

	public final List<Couple> getCouples()
	{
		if(_couples == null)
			_couples = new ArrayList<Couple>();
		return _couples;
	}

	public List<Couple> getDeletedCouples()
	{
		if(_deletedCouples == null)
			_deletedCouples = new ArrayList<Couple>();
		return _deletedCouples;
	}

	public void store()
	{
		Connection con = null;
		try
		{
			if(_deletedCouples != null && !_deletedCouples.isEmpty())
			{
				con = DatabaseFactory.getInstance().getConnection();
				for(final Couple c : _deletedCouples)
				{
					final PreparedStatement statement = con.prepareStatement("DELETE FROM couples WHERE id = ?");
					statement.setInt(1, c.getId());
					statement.execute();
					statement.close();
				}
				_deletedCouples.clear();
			}
			if(_couples != null && !_couples.isEmpty())
				for(final Couple c : _couples)
					if(c.isChanged())
					{
						if(con == null)
							con = DatabaseFactory.getInstance().getConnection();
						c.store(con);
						c.setChanged(false);
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
	}

	static
	{
		CoupleManager._log = LoggerFactory.getLogger(CoupleManager.class);
	}

	private class StoreTask implements Runnable
	{
		private final SimpleDateFormat formatter;

		private StoreTask()
		{
			formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		}

		@Override
		public void run()
		{
			store();
		}
	}
}
