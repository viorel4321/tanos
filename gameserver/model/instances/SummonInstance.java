package l2s.gameserver.model.instances;

import java.util.concurrent.Future;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SetSummonRemainTime;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.taskmanager.DecayTaskManager;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SummonInstance extends Servitor
{
	public final int CYCLE = 5000;
	private float _expPenalty;
	private int _itemConsumeIdInTime;
	private int _itemConsumeCountInTime;
	private int _itemConsumeDelay;
	private Future<?> _disappearTask;
	private Future<?> _consumeTask;
	int _aliveTime;
	private int _maxTime;
	private boolean _inOlympiadMode;

	public SummonInstance(final int objectId, final NpcTemplate template, final Player owner, final int lifetime, final int consumeid, final int consumecount, final int consumedelay)
	{
		super(objectId, template, owner);
		_expPenalty = 0.0f;
		_inOlympiadMode = false;
		setName(template.name);
		_maxTime = lifetime;
		_aliveTime = lifetime;
		_itemConsumeIdInTime = consumeid;
		_itemConsumeCountInTime = consumecount;
		_itemConsumeDelay = consumedelay;
		_disappearTask = ThreadPoolManager.getInstance().schedule(new Lifetime(this), 5000L);
		if(_itemConsumeIdInTime > 0 && _itemConsumeCountInTime > 0)
			startConsume();
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<SummonInstance> getRef()
	{
		return (HardReference<SummonInstance>) super.getRef();
	}

	@Override
	public final byte getLevel()
	{
		return getTemplate() != null ? getTemplate().level : 0;
	}

	@Override
	public int getSummonType()
	{
		return 1;
	}

	@Override
	public int getCurrentFed()
	{
		return _aliveTime;
	}

	@Override
	public int getMaxFed()
	{
		return _maxTime;
	}

	public void setExpPenalty(final float expPenalty)
	{
		_expPenalty = expPenalty;
	}

	public float getExpPenalty()
	{
		return _expPenalty;
	}

	@Override
	public void onDeath(final Creature killer)
	{
		super.onDeath(killer);
		if(_disappearTask != null)
		{
			_disappearTask.cancel(false);
			_disappearTask = null;
		}
		stopConsume();
		DecayTaskManager.getInstance().addDecayTask(this);
	}

	public int getItemConsumeIdInTime()
	{
		return _itemConsumeIdInTime;
	}

	public int getItemConsumeCountInTime()
	{
		return _itemConsumeCountInTime;
	}

	public int getItemConsumeDelay()
	{
		return _itemConsumeDelay;
	}

	protected synchronized void stopConsume()
	{
		if(_consumeTask != null)
		{
			_consumeTask.cancel(true);
			_consumeTask = null;
		}
	}

	protected synchronized void stopDisappear()
	{
		if(_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}

	public synchronized void startConsume()
	{
		if(_consumeTask == null && !isDead())
			_consumeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Consume(getPlayer(), this), _itemConsumeDelay, _itemConsumeDelay);
	}

	@Override
	public synchronized void unSummon()
	{
		stopDisappear();
		stopConsume();
		super.unSummon();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!isVisible())
			return;
		sendStUpdate();
		final StatusUpdate su = new StatusUpdate(getObjectId());
		if(needHpUpdate())
			su.addAttribute(9, (int) getCurrentHp());
		if(needMpUpdate())
			su.addAttribute(11, (int) getCurrentMp());
		if(su.hasAttributes())
			broadcastToStatusListeners(su);
	}

	@Override
	public void displayGiveDamageMessage(final Creature target, final boolean crit, final boolean miss, final boolean magic)
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		if(crit)
			owner.sendPacket(Msg.SUMMONED_MONSTERS_CRITICAL_HIT);
		if(miss)
			owner.sendPacket(new SystemMessage(1999).addName(target));
	}

	@Override
	public void displayReceiveDamageMessage(final Creature attacker, final int damage)
	{
		final Player owner = getPlayer();
		if(owner != null)
		{
			final SystemMessage sm = new SystemMessage(1027);
			String name = attacker.getVisibleName(owner);
			if(attacker.isNpc() && name.isEmpty())
				sm.addNpcName(((NpcInstance) attacker).getTemplate().npcId);
			else
				sm.addString(name);
			owner.sendPacket(sm.addNumber(Integer.valueOf(damage)));
		}
	}

	@Override
	public boolean isSummon()
	{
		return true;
	}

	@Override
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
	}

	class Lifetime implements Runnable
	{
		private SummonInstance _summon;
		int _count;

		Lifetime(final SummonInstance summon)
		{
			_summon = summon;
		}

		@Override
		public void run()
		{
			final Player owner = getPlayer();
			if(owner == null)
			{
				_disappearTask = null;
				unSummon();
				return;
			}
			final SummonInstance this$0 = SummonInstance.this;
			this$0._aliveTime -= 5000;
			if(_aliveTime <= 0)
			{
				owner.sendPacket(new SystemMessage(1521));
				_disappearTask = null;
				unSummon();
				return;
			}
			owner.sendPacket(new SetSummonRemainTime(_summon));
			_disappearTask = ThreadPoolManager.getInstance().schedule(new Lifetime(_summon), 5000L);
		}
	}

	static class Consume implements Runnable
	{
		private Player _activeChar;
		private SummonInstance _summon;

		Consume(final Player activeChar, final SummonInstance newpet)
		{
			_activeChar = activeChar;
			_summon = newpet;
		}

		@Override
		public void run()
		{
			if(_activeChar == null || _summon == null)
				return;
			final ItemInstance item = _activeChar.getInventory().findItemByItemId(_summon.getItemConsumeIdInTime());
			if(item != null && item.getCount() >= _summon.getItemConsumeCountInTime())
			{
				final ItemInstance dest = _activeChar.getInventory().destroyItemByItemId(_summon.getItemConsumeIdInTime(), _summon.getItemConsumeCountInTime(), true);
				_activeChar.sendPacket(new SystemMessage(1029).addItemName(Integer.valueOf(dest.getItemId())));
			}
			else
			{
				_activeChar.sendPacket(new SystemMessage(1143));
				_summon.unSummon();
			}
		}
	}
}
