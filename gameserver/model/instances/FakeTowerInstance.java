package l2s.gameserver.model.instances;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.npc.NpcTemplate;

public class FakeTowerInstance extends NpcInstance
{
	public FakeTowerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAutoAttackable(final Creature player)
	{
		return false;
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{}

	@Override
	public void showChatWindow(final Player player, final String filename, final Object... replace)
	{}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
}
