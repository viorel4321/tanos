package l2s.gameserver.model.instances;

import l2s.gameserver.ai.ArtefactAI;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class ArtefactInstance extends NpcInstance
{
	public ArtefactInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	@Override
	public boolean isArtefact()
	{
		return true;
	}

	@Override
	protected CharacterAI initAI()
	{
		return new ArtefactAI(this);
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable(final Creature attacker)
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
