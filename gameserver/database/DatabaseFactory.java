package l2s.gameserver.database;

import java.sql.Connection;
import java.sql.SQLException;

import l2s.commons.dbcp.BasicDataSource;
import l2s.gameserver.Config;

public class DatabaseFactory extends BasicDataSource
{
	private static final DatabaseFactory _instance;
	private static DatabaseFactory _instanceLogin;

	public static final DatabaseFactory getInstance() throws SQLException
	{
		return DatabaseFactory._instance;
	}

	public DatabaseFactory()
	{
		super(Config.DATABASE_DRIVER, Config.DATABASE_URL, Config.DATABASE_LOGIN, Config.DATABASE_PASSWORD, Config.DATABASE_MAX_CONNECTIONS, Config.DATABASE_MAX_CONNECTIONS, Config.DATABASE_MAX_IDLE_TIMEOUT, Config.DATABASE_IDLE_TEST_PERIOD, false);
	}

	public static DatabaseFactory getInstanceLogin() throws SQLException
	{
		if(DatabaseFactory._instanceLogin == null)
		{
			if(Config.DATABASE_URL.equalsIgnoreCase(Config.ACCOUNTS_URL))
				return getInstance();
			DatabaseFactory._instanceLogin = new DatabaseFactory(Config.ACCOUNTS_DRIVER, Config.ACCOUNTS_URL, Config.ACCOUNTS_LOGIN, Config.ACCOUNTS_PASSWORD);
		}
		return DatabaseFactory._instanceLogin;
	}

	private DatabaseFactory(final String driver, final String url, final String login, final String pass)
	{
		super(driver, url, login, pass, Config.ACCOUNTS_DB_MAX_CONNECTIONS, Config.ACCOUNTS_DB_MAX_CONNECTIONS, 300, 60, false);
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return getConnection((Connection) null);
	}

	static
	{
		_instance = new DatabaseFactory();
	}
}
