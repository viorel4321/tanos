package l2s.gameserver.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;

public class CrestCache
{
	public static final int ALLY_CREST_SIZE = 192;
	public static final int CREST_SIZE = 256;
	public static final int LARGE_CREST_SIZE = 2176;
	private static final Logger _log;
	private static final CrestCache _instance;
	private final TIntIntHashMap _pledgeCrestId;
	private final TIntIntHashMap _pledgeCrestLargeId;
	private final TIntIntHashMap _allyCrestId;
	private final TIntObjectHashMap<byte[]> _pledgeCrest;
	private final TIntObjectHashMap<byte[]> _pledgeCrestLarge;
	private final TIntObjectHashMap<byte[]> _allyCrest;
	private final ReentrantReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;

	private static int getCrestId(final int pledgeId, final byte[] crest)
	{
		return Math.abs(new HashCodeBuilder(15, 87).append(pledgeId).append(crest).toHashCode());
	}

	public static final CrestCache getInstance()
	{
		return CrestCache._instance;
	}

	private CrestCache()
	{
		_pledgeCrestId = new TIntIntHashMap();
		_pledgeCrestLargeId = new TIntIntHashMap();
		_allyCrestId = new TIntIntHashMap();
		_pledgeCrest = new TIntObjectHashMap<byte[]>();
		_pledgeCrestLarge = new TIntObjectHashMap<byte[]>();
		_allyCrest = new TIntObjectHashMap<byte[]>();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
		load();
	}

	public byte[] getAllyCrest(final int crestId)
	{
		byte[] crest = null;
		readLock.lock();
		try
		{
			crest = _allyCrest.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}

	public int getAllyCrestId(final int pledgeId)
	{
		int crestId = 0;
		readLock.lock();
		try
		{
			crestId = _allyCrestId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}

	public byte[] getPledgeCrest(final int crestId)
	{
		byte[] crest = null;
		readLock.lock();
		try
		{
			crest = _pledgeCrest.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}

	public int getPledgeCrestId(final int pledgeId)
	{
		int crestId = 0;
		readLock.lock();
		try
		{
			crestId = _pledgeCrestId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}

	public byte[] getPledgeCrestLarge(final int crestId)
	{
		byte[] crest = null;
		readLock.lock();
		try
		{
			crest = _pledgeCrestLarge.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}

	public int getPledgeCrestLargeId(final int pledgeId)
	{
		int crestId = 0;
		readLock.lock();
		try
		{
			crestId = _pledgeCrestLargeId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}

	public void load()
	{
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id, crest FROM clan_data WHERE crest IS NOT NULL");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				final int pledgeId = rset.getInt("clan_id");
				final byte[] crest = rset.getBytes("crest");
				final int crestId = getCrestId(pledgeId, crest);
				_pledgeCrestId.put(pledgeId, crestId);
				_pledgeCrest.put(crestId, crest);
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT clan_id, largecrest FROM clan_data WHERE largecrest IS NOT NULL");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				final int pledgeId = rset.getInt("clan_id");
				final byte[] crest = rset.getBytes("largecrest");
				final int crestId = getCrestId(pledgeId, crest);
				_pledgeCrestLargeId.put(pledgeId, crestId);
				_pledgeCrestLarge.put(crestId, crest);
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT ally_id, crest FROM ally_data WHERE crest IS NOT NULL");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				final int pledgeId = rset.getInt("ally_id");
				final byte[] crest = rset.getBytes("crest");
				final int crestId = getCrestId(pledgeId, crest);
				_allyCrestId.put(pledgeId, crestId);
				_allyCrest.put(crestId, crest);
			}
		}
		catch(Exception e)
		{
			CrestCache._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		CrestCache._log.info("CrestCache: Loaded " + count + " crests");
	}

	public void removeAllyCrest(final int pledgeId)
	{
		writeLock.lock();
		try
		{
			_allyCrest.remove(_allyCrestId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
			statement.setNull(1, -3);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			CrestCache._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void removePledgeCrest(final int pledgeId)
	{
		writeLock.lock();
		try
		{
			_pledgeCrest.remove(_pledgeCrestId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
			statement.setNull(1, -3);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			CrestCache._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void removePledgeCrestLarge(final int pledgeId)
	{
		writeLock.lock();
		try
		{
			_pledgeCrestLarge.remove(_pledgeCrestLargeId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET largecrest=? WHERE clan_id=?");
			statement.setNull(1, -3);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			CrestCache._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int saveAllyCrest(final int pledgeId, final byte[] crest)
	{
		final int crestId = getCrestId(pledgeId, crest);
		writeLock.lock();
		try
		{
			_allyCrestId.put(pledgeId, crestId);
			_allyCrest.put(crestId, crest);
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
			statement.setBytes(1, crest);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			CrestCache._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return crestId;
	}

	public int savePledgeCrest(final int pledgeId, final byte[] crest)
	{
		final int crestId = getCrestId(pledgeId, crest);
		writeLock.lock();
		try
		{
			_pledgeCrestId.put(pledgeId, crestId);
			_pledgeCrest.put(crestId, crest);
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
			statement.setBytes(1, crest);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			CrestCache._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return crestId;
	}

	public int savePledgeCrestLarge(final int pledgeId, final byte[] crest)
	{
		final int crestId = getCrestId(pledgeId, crest);
		writeLock.lock();
		try
		{
			_pledgeCrestLargeId.put(pledgeId, crestId);
			_pledgeCrestLarge.put(crestId, crest);
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET largecrest=? WHERE clan_id=?");
			statement.setBytes(1, crest);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			CrestCache._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return crestId;
	}

	static
	{
		_log = LoggerFactory.getLogger(CrestCache.class);
		_instance = new CrestCache();
	}
}
