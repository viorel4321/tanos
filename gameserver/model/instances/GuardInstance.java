package l2s.gameserver.model.instances;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class GuardInstance extends NpcInstance
{
	public GuardInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return attacker.isMonster() && ((NpcInstance) attacker).isAggressive() || attacker.isPlayable() && attacker.getKarma() > 0;
	}

	@Override
	public String getHtmlPath(final int npcId, final int val, final Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "guard/" + pom + ".htm";
	}

	@Override
	public boolean isInvul()
	{
		return false;
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
	public boolean isGuard()
	{
		return true;
	}

	@Override
	public boolean isDmg()
	{
		return true;
	}

	@Override
	protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp)
	{
		getAggroList().addDamageHate(attacker, (int) damage, 0);
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
}
