package l2s.gameserver.model.entity.events;

public class EventTimeTask implements Runnable
{
	private final GlobalEvent _event;
	private final int _time;

	public EventTimeTask(final GlobalEvent event, final int time)
	{
		_event = event;
		_time = time;
	}

	@Override
	public void run()
	{
		_event.timeActions(_time);
	}
}
