package l2s.gameserver.model.instances;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public abstract class SiegeToggleNpcInstance extends NpcInstance
{
	private NpcInstance _fakeInstance;
	private int _maxHp;

	public SiegeToggleNpcInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	public void setMaxHp(final int maxHp)
	{
		_maxHp = maxHp;
	}

	public void setZoneList(final Set<String> set)
	{}

	public void register(final Spawner spawn)
	{}

	public void initFake(final int fakeNpcId)
	{
		(_fakeInstance = NpcTable.getTemplate(fakeNpcId).getNewInstance()).setCurrentHpMp(1.0, _fakeInstance.getMaxMp(), false);
		_fakeInstance.setHasChatWindow(false);
	}

	public abstract void onDeathImpl(final Creature p0);

	@Override
	protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp)
	{
		this.setCurrentHp(Math.max(getCurrentHp() - damage, 0.0), false);
		if(getCurrentHp() < 0.5)
		{
			doDie(attacker);
			onDeathImpl(attacker);
			decayMe();
			_fakeInstance.spawnMe(getLoc());
		}
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		if(attacker == null)
			return false;
		final Player player = attacker.getPlayer();
		if(player == null)
			return false;
		final SiegeEvent<?, ?> siegeEvent = this.getEvent(SiegeEvent.class);
		return siegeEvent != null && siegeEvent.isInProgress();
	}

	@Override
	public boolean isAttackable(final Creature attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	public boolean hasRandomAnimation()
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
	public boolean isLethalImmune()
	{
		return true;
	}

	public void decayFake()
	{
		_fakeInstance.decayMe();
	}

	@Override
	public int getMaxHp()
	{
		return _maxHp;
	}

	@Override
	public void onDecay()
	{
		decayMe();
		_spawnAnimation = 2;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}
