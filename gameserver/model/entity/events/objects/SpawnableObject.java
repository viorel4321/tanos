package l2s.gameserver.model.entity.events.objects;

import java.io.Serializable;

import l2s.gameserver.model.entity.events.GlobalEvent;

public interface SpawnableObject extends Serializable
{
	void spawnObject(final GlobalEvent p0);

	void despawnObject(final GlobalEvent p0);

	void refreshObject(final GlobalEvent p0);
}
