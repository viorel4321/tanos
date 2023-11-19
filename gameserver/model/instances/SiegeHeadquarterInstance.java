package l2s.gameserver.model.instances;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SiegeHeadquarterInstance extends NpcInstance
{
	private SiegeClanObject _owner;
	private long _lastAnnouncedAttackedTime;

	public SiegeHeadquarterInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_lastAnnouncedAttackedTime = 0L;
		setHasChatWindow(false);
	}

	@Override
	public String getName()
	{
		return _owner.getClan().getName();
	}

	@Override
	public Clan getClan()
	{
		return _owner.getClan();
	}

	@Override
	public String getTitle()
	{
		return "";
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		final Player player = attacker.getPlayer();
		if(player == null || isInvul())
			return false;
		final Clan clan = player.getClan();
		return clan == null || _owner.getClan() != clan;
	}

	@Override
	public boolean isAttackable(final Creature attacker)
	{
		return true;
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new StatusUpdate(getObjectId()).addAttribute(9, (int) getCurrentHp()).addAttribute(10, getMaxHp()));
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else
			player.sendActionFailed();
	}

	@Override
	public void onDeath(final Creature killer)
	{
		_owner.setFlag(null);
		super.onDeath(killer);
	}

	@Override
	protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp)
	{
		if(System.currentTimeMillis() - _lastAnnouncedAttackedTime > 120000L)
		{
			_lastAnnouncedAttackedTime = System.currentTimeMillis();
			_owner.getClan().broadcastToOnlineMembers(Msg.YOUR_BASE_IS_BEING_ATTACKED);
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return _isInvul;
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

	@Override
	public boolean isEffectImmune()
	{
		return true;
	}

	public void setClan(final SiegeClanObject owner)
	{
		_owner = owner;
	}

	@Override
	public boolean isDmg()
	{
		return true;
	}
}
