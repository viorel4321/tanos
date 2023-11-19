package l2s.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.taskmanager.tasks.TaskEndHero;
import l2s.gameserver.taskmanager.tasks.TaskRecom;

public final class TaskManager
{
	private static final Logger _log;
	private static TaskManager _instance;
	static final String[] SQL_STATEMENTS;
	private final Map<Integer, Task> _tasks;
	final List<ExecutedTask> _currentTasks;

	public static TaskManager getInstance()
	{
		if(TaskManager._instance == null)
			TaskManager._instance = new TaskManager();
		return TaskManager._instance;
	}

	public TaskManager()
	{
		_tasks = new ConcurrentHashMap<Integer, Task>();
		_currentTasks = new ArrayList<ExecutedTask>();
		initializate();
		startAllTasks();
	}

	private void initializate()
	{
		registerTask(new TaskEndHero());
		registerTask(new TaskRecom());
	}

	public void registerTask(final Task task)
	{
		final int key = task.getName().hashCode();
		if(!_tasks.containsKey(key))
		{
			_tasks.put(key, task);
			task.initializate();
		}
	}

	private void startAllTasks()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(TaskManager.SQL_STATEMENTS[0]);
			rset = statement.executeQuery();
			while(rset.next())
			{
				final Task task = _tasks.get(rset.getString("task").trim().toLowerCase().hashCode());
				if(task == null)
					continue;
				final TaskTypes type = TaskTypes.valueOf(rset.getString("type"));
				if(type == TaskTypes.TYPE_NONE)
					continue;
				final ExecutedTask current = new ExecutedTask(task, type, rset);
				if(!launchTask(current))
					continue;
				_currentTasks.add(current);
			}
		}
		catch(Exception e)
		{
			TaskManager._log.error("error while loading Global Task table: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private boolean launchTask(final ExecutedTask task)
	{
		final ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
		final TaskTypes type = task.getType();
		if(type == TaskTypes.TYPE_STARTUP)
		{
			task.run();
			return false;
		}
		if(type == TaskTypes.TYPE_SHEDULED)
		{
			final long delay = Long.valueOf(task.getParams()[0]);
			task._scheduled = scheduler.schedule(task, delay);
			return true;
		}
		if(type == TaskTypes.TYPE_FIXED_SHEDULED)
		{
			final long delay = Long.valueOf(task.getParams()[0]);
			final long interval = Long.valueOf(task.getParams()[1]);
			task._scheduled = scheduler.scheduleAtFixedRate(task, delay, interval);
			return true;
		}
		if(type == TaskTypes.TYPE_TIME)
			try
			{
				final Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
				final long diff = desired.getTime() - System.currentTimeMillis();
				if(diff >= 0L)
				{
					task._scheduled = scheduler.schedule(task, diff);
					return true;
				}
				TaskManager._log.info("Task " + task.getId() + " is obsoleted.");
			}
			catch(Exception ex)
			{}
		else if(type == TaskTypes.TYPE_SPECIAL)
		{
			final ScheduledFuture<?> result = task.getTask().launchSpecial(task);
			if(result != null)
			{
				task._scheduled = result;
				return true;
			}
		}
		else if(type == TaskTypes.TYPE_GLOBAL_TASK)
		{
			final long interval2 = Long.valueOf(task.getParams()[0]) * 86400000L;
			final String[] hour = task.getParams()[1].split(":");
			if(hour.length != 3)
			{
				TaskManager._log.warn("Task " + task.getId() + " has incorrect parameters");
				return false;
			}
			final Calendar check = Calendar.getInstance();
			check.setTimeInMillis(task.getLastActivation() + interval2);
			final Calendar min = Calendar.getInstance();
			try
			{
				min.set(11, Integer.valueOf(hour[0]));
				min.set(12, Integer.valueOf(hour[1]));
				min.set(13, Integer.valueOf(hour[2]));
			}
			catch(Exception e)
			{
				TaskManager._log.warn("Bad parameter on task " + task.getId() + ": " + e.getMessage());
				return false;
			}
			long delay2 = min.getTimeInMillis() - System.currentTimeMillis();
			if(check.after(min) || delay2 < 0L)
				delay2 += interval2;
			task._scheduled = scheduler.scheduleAtFixedRate(task, delay2, interval2);
			return true;
		}
		return false;
	}

	public static boolean addUniqueTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3)
	{
		return addUniqueTask(task, type, param1, param2, param3, 0L);
	}

	public static boolean addUniqueTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3, final long lastActivation)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(TaskManager.SQL_STATEMENTS[2]);
			statement.setString(1, task);
			rset = statement.executeQuery();
			final boolean exists = rset.next();
			DbUtils.close(statement, rset);
			if(!exists)
			{
				statement = con.prepareStatement(TaskManager.SQL_STATEMENTS[3]);
				statement.setString(1, task);
				statement.setString(2, type.toString());
				statement.setLong(3, lastActivation / 1000L);
				statement.setString(4, param1);
				statement.setString(5, param2);
				statement.setString(6, param3);
				statement.execute();
			}
			return true;
		}
		catch(SQLException e)
		{
			TaskManager._log.warn("cannot add the unique task: " + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return false;
	}

	public static boolean addTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3)
	{
		return addTask(task, type, param1, param2, param3, 0L);
	}

	public static boolean addTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3, final long lastActivation)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(TaskManager.SQL_STATEMENTS[3]);
			statement.setString(1, task);
			statement.setString(2, type.toString());
			statement.setLong(3, lastActivation / 1000L);
			statement.setString(4, param1);
			statement.setString(5, param2);
			statement.setString(6, param3);
			statement.execute();
			return true;
		}
		catch(SQLException e)
		{
			TaskManager._log.warn("cannot add the task:\t" + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return false;
	}

	static
	{
		_log = LoggerFactory.getLogger(TaskManager.class);
		SQL_STATEMENTS = new String[] {
				"SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks",
				"UPDATE global_tasks SET last_activation=? WHERE id=?",
				"SELECT id FROM global_tasks WHERE task=?",
				"INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)" };
	}

	public class ExecutedTask implements Runnable
	{
		int _id;
		long _lastActivation;
		Task _task;
		TaskTypes _type;
		String[] _params;
		ScheduledFuture<?> _scheduled;

		public ExecutedTask(final Task task, final TaskTypes type, final ResultSet rset) throws SQLException
		{
			_task = task;
			_type = type;
			_id = rset.getInt("id");
			_lastActivation = rset.getLong("last_activation") * 1000L;
			_params = new String[] { rset.getString("param1"), rset.getString("param2"), rset.getString("param3") };
		}

		@Override
		public void run()
		{
			_task.onTimeElapsed(this);
			_lastActivation = System.currentTimeMillis();
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(TaskManager.SQL_STATEMENTS[1]);
				statement.setLong(1, _lastActivation / 1000L);
				statement.setInt(2, _id);
				statement.executeUpdate();
			}
			catch(SQLException e)
			{
				_log.warn("cannot updated the Global Task " + _id + ": " + e.getMessage());
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
			if(_type == TaskTypes.TYPE_SHEDULED || _type == TaskTypes.TYPE_TIME)
				stopTask();
		}

		@Override
		public boolean equals(final Object object)
		{
			return _id == ((ExecutedTask) object)._id;
		}

		@Override     
		public int hashCode()
		{
			return 7 * _id + 13210;
		}

		public Task getTask()
		{
			return _task;
		}

		public TaskTypes getType()
		{
			return _type;
		}

		public int getId()
		{
			return _id;
		}

		public String[] getParams()
		{
			return _params;
		}

		public long getLastActivation()
		{
			return _lastActivation;
		}

		public void stopTask()
		{
			_task.onDestroy();
			if(_scheduled != null)
				_scheduled.cancel(false);
			_currentTasks.remove(this);
		}
	}
}
