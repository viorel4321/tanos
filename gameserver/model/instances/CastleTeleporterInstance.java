package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.npc.NpcTemplate;

public class CastleTeleporterInstance extends SiegeGuardInstance
{
	public CastleTeleporterInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
	}
}
