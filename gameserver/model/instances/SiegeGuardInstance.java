package l2s.gameserver.model.instances;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SiegeGuardInstance extends NpcInstance
{
	public SiegeGuardInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	@Override
	public int getAggroRange()
	{
		return 1200;
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		final Player player = attacker.getPlayer();
		if(player == null)
			return false;
		final SiegeEvent<?, ?> siegeEvent = this.getEvent(SiegeEvent.class);
		final SiegeEvent<?, ?> siegeEvent2 = attacker.getEvent(SiegeEvent.class);
		final Clan clan = player.getClan();
		return siegeEvent != null && (clan == null || siegeEvent != siegeEvent2 || siegeEvent.getSiegeClan("defenders", clan) == null);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
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
	public Clan getClan()
	{
		return null;
	}

	@Override
	public boolean isSiegeGuard()
	{
		return true;
	}

	@Override
	public boolean isDmg()
	{
		return true;
	}
}
