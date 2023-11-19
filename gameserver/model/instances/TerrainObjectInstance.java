package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;

public class TerrainObjectInstance extends NpcInstance
{
	public TerrainObjectInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		player.sendActionFailed();
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}
