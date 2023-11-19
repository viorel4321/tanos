package l2s.gameserver.model.instances;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.ClanHallTeamBattleEvent;
import l2s.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.CTBTeamObject;
import l2s.gameserver.templates.npc.NpcTemplate;

public abstract class CTBBossInstance extends MonsterInstance
{
	private static final long serialVersionUID = 1L;
	private CTBTeamObject _matchTeamObject;

	public CTBBossInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	@Override
	public boolean isAttackable(final Creature attacker)
	{
		final CTBSiegeClanObject clan = _matchTeamObject == null ? null : _matchTeamObject.getSiegeClan();
		if(clan != null && attacker.isPlayable())
		{
			final Player player = attacker.getPlayer();
			if(player.getClan() == clan.getClan())
				return false;
		}
		return true;
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return isAttackable(attacker);
	}

	@Override
	public void onDeath(final Creature killer)
	{
		final ClanHallTeamBattleEvent event = this.getEvent(ClanHallTeamBattleEvent.class);
		event.processStep(_matchTeamObject);
		super.onDeath(killer);
	}

	@Override
	public String getTitle()
	{
		final CTBSiegeClanObject clan = _matchTeamObject.getSiegeClan();
		return clan == null ? "" : clan.getClan().getName();
	}

	public void setMatchTeamObject(final CTBTeamObject matchTeamObject)
	{
		_matchTeamObject = matchTeamObject;
	}
}
