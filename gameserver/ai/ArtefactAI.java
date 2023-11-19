package l2s.gameserver.ai;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.instances.NpcInstance;

public class ArtefactAI extends CharacterAI
{
	public ArtefactAI(final NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAggression(final Creature attacker, final int aggro)
	{
		final Player player;
		final NpcInstance actor;
		if(attacker == null || (player = attacker.getPlayer()) == null || (actor = (NpcInstance) getActor()) == null)
			return;
		final SiegeEvent<?, ?> siegeEvent1 = actor.getEvent(SiegeEvent.class);
		if(siegeEvent1 == null)
			return;
		final SiegeEvent<?, ?> siegeEvent2 = player.getEvent(SiegeEvent.class);
		final SiegeClanObject siegeClan = siegeEvent1.getSiegeClan("attackers", player.getClan());
		if(siegeEvent2 == null || siegeEvent1 == siegeEvent2 && siegeClan != null)
			ThreadPoolManager.getInstance().schedule(new notifyGuard(player), 1000L);
	}

	class notifyGuard implements Runnable
	{
		private HardReference<Player> _playerRef;

		public notifyGuard(final Player attacker)
		{
			_playerRef = attacker.getRef();
		}

		@Override
		public void run()
		{
			final Player attacker = _playerRef.get();
			final NpcInstance actor;
			if(attacker == null || (actor = (NpcInstance) getActor()) == null)
				return;
			for(final NpcInstance npc : actor.getAroundNpc(1500, 200))
				if(npc.isSiegeGuard() && Rnd.chance(20))
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
			if(attacker.getCastingSkill() != null && attacker.getCastingSkill().getTargetType() == Skill.SkillTargetType.TARGET_HOLY)
				ThreadPoolManager.getInstance().schedule(this, 10000L);
		}
	}
}
