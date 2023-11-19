package l2s.gameserver.model.entity.events;

public enum EventType
{
	SIEGE_EVENT,
	PVP_EVENT,
	BOAT_EVENT,
	FUN_EVENT;

	private int _step;

	private EventType()
	{
		_step = ordinal() * 1000;
	}

	public int step()
	{
		return _step;
	}
}
