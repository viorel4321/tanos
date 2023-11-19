package l2s.gameserver.model;

import l2s.gameserver.model.instances.NpcInstance;

public interface SpawnListener
{
	void npcSpawned(final NpcInstance p0);

	void npcDeSpawned(final NpcInstance p0);
}
