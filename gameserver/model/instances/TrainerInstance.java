package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class TrainerInstance extends NpcInstance
{
	public TrainerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(final int npcId, final int val, final Player player)
	{
		String pom = "";
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "trainer/" + pom + ".htm";
	}
}
