package l2s.gameserver.model.instances;

import l2s.gameserver.model.Creature;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.npc.NpcTemplate;

public abstract class _34BossMinionInstance extends SiegeGuardInstance implements _34SiegeGuard
{
	private static final long serialVersionUID = 1L;

	public _34BossMinionInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onDeath(final Creature killer)
	{
		this.setCurrentHp(1.0, false);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		Functions.npcShout(this, spawnChatSay(), 0);
	}

	public abstract String spawnChatSay();

	@Override
	public abstract String teleChatSay();
}
