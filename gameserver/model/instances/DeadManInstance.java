package l2s.gameserver.model.instances;

import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.Die;
import l2s.gameserver.templates.npc.NpcTemplate;

public class DeadManInstance extends MonsterInstance
{
	public DeadManInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected CharacterAI initAI()
	{
		return new CharacterAI(this);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		setCurrentHp(0.0, false);
		setDead(true);
		broadcastStatusUpdate();
		broadcastPacket(new Die(this));
		setWalking();
	}

	@Override
	public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final int poleHitCount, final boolean crit, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage)
	{}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isBlocked()
	{
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 0;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}
