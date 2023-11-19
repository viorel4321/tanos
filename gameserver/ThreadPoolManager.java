package l2s.gameserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import l2s.commons.threading.PriorityThreadFactory;
import l2s.commons.threading.RejectedExecutionHandlerImpl;
import l2s.commons.threading.RunnableWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolManager
{
	private static final Logger _log = LoggerFactory.getLogger(ThreadPoolManager.class);

	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;

	private static final ThreadPoolManager _instance = new ThreadPoolManager();

	public static ThreadPoolManager getInstance()
	{
		return _instance;
	}

	private final ScheduledThreadPoolExecutor _scheduledExecutor;
	private final ThreadPoolExecutor _executor;

	private boolean _shutdown;

	private ThreadPoolManager()
	{
		_scheduledExecutor = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_SIZE, new PriorityThreadFactory("ScheduledThreadPool", Thread.NORM_PRIORITY), new ThreadPoolExecutor.CallerRunsPolicy());
		_scheduledExecutor.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
		_scheduledExecutor.prestartAllCoreThreads();

		_executor = new ThreadPoolExecutor(Config.EXECUTOR_THREAD_POOL_SIZE, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("ThreadPoolExecutor", Thread.NORM_PRIORITY), new ThreadPoolExecutor.CallerRunsPolicy());
		_executor.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
		_executor.prestartAllCoreThreads();

		//Очистка каждые 5 минут
		scheduleAtFixedRate(() ->
		{
			_scheduledExecutor.purge();
			_executor.purge();
		}, 5, 5, TimeUnit.MINUTES);
	}

	private long validate(long delay, TimeUnit timeUnit)
	{
		long delayInMilliseconds = timeUnit.toMillis(delay);
		long validatedDelay = Math.max(0, Math.min(MAX_DELAY, delayInMilliseconds));
		if(delayInMilliseconds > validatedDelay)
			return -1L;

		return timeUnit.convert(delayInMilliseconds, TimeUnit.MILLISECONDS);
	}

	public boolean isShutdown()
	{
		return _shutdown;
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit timeUnit)
	{
		delay = validate(delay, timeUnit);
		if(delay == -1L)
			return null;

		return _scheduledExecutor.schedule(new RunnableWrapper(r), delay, timeUnit);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return schedule(r, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay, TimeUnit timeUnit)
	{
		initial = validate(initial, timeUnit);
		if(initial == -1L)
			return null;

		delay = validate(delay, timeUnit);
		if(delay == -1L)
			return _scheduledExecutor.schedule(new RunnableWrapper(r), initial, timeUnit);

		return _scheduledExecutor.scheduleAtFixedRate(new RunnableWrapper(r), initial, delay, timeUnit);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay, TimeUnit timeUnit)
	{
		initial = validate(initial, timeUnit);
		if(initial == -1L)
			return null;

		delay = validate(delay, timeUnit);
		if(delay == -1L)
			return _scheduledExecutor.schedule(new RunnableWrapper(r), initial, timeUnit);

		return _scheduledExecutor.scheduleWithFixedDelay(new RunnableWrapper(r), initial, delay, timeUnit);
	}

	public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay)
	{
		return scheduleAtFixedDelay(r, initial, delay, TimeUnit.MILLISECONDS);
	}

	public void execute(Runnable r)
	{
		_executor.execute(new RunnableWrapper(r));
	}

	public void shutdown() throws InterruptedException
	{
		_shutdown = true;
		try
		{
			_scheduledExecutor.shutdown();
			_scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
		}
		finally
		{
			_executor.shutdown();
			_executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}

	public CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();

		list.append("ScheduledThreadPool\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_scheduledExecutor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_scheduledExecutor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_scheduledExecutor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_scheduledExecutor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_scheduledExecutor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_scheduledExecutor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_scheduledExecutor.getTaskCount()).append("\n");
		list.append("ThreadPoolExecutor\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_executor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_executor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_executor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_executor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_executor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_executor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_executor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_executor.getTaskCount()).append("\n");

		return list;
	}
}
