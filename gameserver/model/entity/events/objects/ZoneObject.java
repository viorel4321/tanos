package l2s.gameserver.model.entity.events.objects;

import java.util.List;

import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class ZoneObject implements InitableObject
{
	private String _name;
	private Zone _zone;

	public ZoneObject(final String name)
	{
		_name = name;
	}

	@Override
	public void initObject(final GlobalEvent e)
	{
		_zone = ZoneManager.getInstance().getZone(_name);
	}

	public void setActive(final boolean a)
	{
		_zone.setActive(a);
	}

	public void setActive(final boolean a, final GlobalEvent event)
	{
		this.setActive(a);
	}

	public Zone getZone()
	{
		return _zone;
	}

	public List<Player> getInsidePlayers()
	{
		return _zone.getInsidePlayers();
	}

	public boolean checkIfInZone(final Creature c)
	{
		return _zone.checkIfInZone(c);
	}
}
