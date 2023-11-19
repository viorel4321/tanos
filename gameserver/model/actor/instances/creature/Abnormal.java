package l2s.gameserver.model.actor.instances.creature;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import l2s.gameserver.stats.triggers.TriggerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.ExOlympiadSpelledInfo;
import l2s.gameserver.network.l2.s2c.MagicEffectIcons;
import l2s.gameserver.network.l2.s2c.PartySpelled;
import l2s.gameserver.network.l2.s2c.ShortBuffStatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.skills.funcs.Func;
import l2s.gameserver.skills.funcs.FuncOwner;
import l2s.gameserver.skills.funcs.FuncTemplate;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.taskmanager.EffectTaskManager;

public abstract class Abnormal implements Comparable<Abnormal>, FuncOwner, Runnable
{
	protected static final Logger _log;
	public static final Abnormal[] EMPTY_L2EFFECT_ARRAY;
	private static final Func[] _emptyFunctionSet;
	public static int SUSPENDED;
	public static int STARTING;
	public static int STARTED;
	public static int ACTING;
	public static int FINISHING;
	public static int FINISHED;
	protected final Creature _effector;
	protected final Creature _effected;
	protected final Skill _skill;
	protected final int _displayId;
	protected final int _displayLevel;
	private final double _value;
	private final AtomicInteger _state;
	private int _count;
	private AbnormalEffect _abnormalEffect;
	private long _period;
	private long _startTimeMillis;
	private long _duration;
	private boolean _inUse;
	private Abnormal _next;
	private boolean _active;
	protected final EffectTemplate _template;
	private Future<?> _effectTask;
	private Future<?> _restoreTask;

	protected Abnormal(final Env env, final EffectTemplate template)
	{
		_inUse = false;
		_next = null;
		_active = false;
		_skill = env.skill;
		_effector = env.character;
		_effected = env.target;
		_template = template;
		_value = template._value;
		_count = template._counter;
		_period = template.getPeriod();
		_duration = _period * _count;
		_displayId = template._displayId != 0 ? template._displayId : _skill.getDisplayId();
		_displayLevel = template._displayLevel != 0 ? template._displayLevel : _skill.getDisplayLevel();
		_abnormalEffect = template._abnormalEffect;
		_state = new AtomicInteger(Abnormal.STARTING);
	}

	public long getPeriod()
	{
		return _period;
	}

	public void setPeriod(final long time)
	{
		_period = time;
		_duration = _period * _count;
	}

	public int getCount()
	{
		return _count;
	}

	public void setCount(final int count)
	{
		_count = count;
		_duration = _period * _count;
	}

	public boolean isOneTime()
	{
		return _period == 0L;
	}

	public long getStartTime()
	{
		if(_startTimeMillis == 0L)
			return System.currentTimeMillis();
		return _startTimeMillis;
	}

	public void setStartTime(final long val)
	{
		_startTimeMillis = val;
	}

	public long getTime()
	{
		return System.currentTimeMillis() - getStartTime();
	}

	public long getDuration()
	{
		return _duration;
	}

	public long getTimeLeft()
	{
		return getDuration() - getTime();
	}

	public boolean isTimeLeft()
	{
		return getDuration() - getTime() > 0L;
	}

	public boolean isInUse()
	{
		return _inUse;
	}

	public void setInUse(final boolean inUse)
	{
		_inUse = inUse;
	}

	public boolean isActive()
	{
		return _active;
	}

	public void setActive(final boolean set)
	{
		_active = set;
	}

	public EffectTemplate getTemplate()
	{
		return _template;
	}

	public String getStackType()
	{
		return getTemplate()._stackType;
	}

	public String getStackType2()
	{
		return getTemplate()._stackType2;
	}

	public boolean checkStackType(final String param)
	{
		return getStackType().equalsIgnoreCase(param) || getStackType2().equalsIgnoreCase(param);
	}

	public boolean checkStackType(final Abnormal param)
	{
		return this.checkStackType(param.getStackType()) || this.checkStackType(param.getStackType2());
	}

	public double getStackOrder()
	{
		return getTemplate()._stackOrder;
	}

	public Skill getSkill()
	{
		return _skill;
	}

	public Creature getEffector()
	{
		return _effector;
	}

	public Creature getEffected()
	{
		return _effected;
	}

	public double calc()
	{
		return _value;
	}

	public boolean isEnded()
	{
		return isFinished() || isFinishing();
	}

	public boolean isFinishing()
	{
		return getState() == Abnormal.FINISHING;
	}

	public boolean isFinished()
	{
		return getState() == Abnormal.FINISHED;
	}

	private int getState()
	{
		return _state.get();
	}

	private boolean setState(final int oldState, final int newState)
	{
		return _state.compareAndSet(oldState, newState);
	}

	public boolean checkCondition()
	{
		return true;
	}

	public void onStart()
	{
		getEffected().addStatFuncs(getStatFuncs());
		getEffected().addTriggers(getTemplate());
		if(_abnormalEffect != AbnormalEffect.NULL)
			getEffected().startAbnormalEffect(_abnormalEffect);
		else if(getEffectType().getAbnormal() != null)
			getEffected().startAbnormalEffect(getEffectType().getAbnormal());
		if(!getSkill().isHideStartMessage() && getEffected().getAbnormalList().getCount(getSkill().getId()) == 1 && !getSkill().isToggle())
			getEffected().sendPacket(new SystemMessage(110).addSkillName(_skill.getDisplayId(), _skill.getDisplayLevel()));

		//tigger on start
		getEffected().useTriggers(getEffected(), TriggerType.ON_START_EFFECT, null, getSkill(), getTemplate(), 0);
	}

	public abstract boolean onActionTime();

	public void onExit()
	{
		getEffected().removeStatsOwner(this);
		getEffected().removeStatFuncs(getStatFuncs());
		getEffected().removeTriggers(getTemplate());
		if(_abnormalEffect != AbnormalEffect.NULL)
			getEffected().stopAbnormalEffect(_abnormalEffect);
		else if(getEffectType().getAbnormal() != null)
			getEffected().stopAbnormalEffect(getEffectType().getAbnormal());
		if(getEffected().isPlayer() && getStackType().equalsIgnoreCase("HpRecoverCast"))
			getEffected().sendPacket(new ShortBuffStatusUpdate());

		//tigger on exit
		getEffected().useTriggers(getEffected(), TriggerType.ON_EXIT_EFFECT, null, getSkill(), getTemplate(), 0);
	}

	private void stopEffectTask()
	{
		if(_effectTask != null)
			_effectTask.cancel(false);
	}

	private void startEffectTask()
	{
		if(_effectTask == null)
		{
			if(_startTimeMillis == 0L)
				_startTimeMillis = System.currentTimeMillis();
			_effectTask = EffectTaskManager.getInstance().scheduleAtFixedRate(this, _period, _period);
		}
	}

	public final void schedule()
	{
		final Creature effected = getEffected();
		if(effected == null)
			return;
		if(!checkCondition())
			return;
		getEffected().getAbnormalList().add(this);
	}

	private final void suspend()
	{
		if(setState(Abnormal.STARTING, Abnormal.SUSPENDED))
			startEffectTask();
		else if(setState(Abnormal.STARTED, Abnormal.SUSPENDED) || setState(Abnormal.ACTING, Abnormal.SUSPENDED))
		{
			synchronized (this)
			{
				if(isInUse())
				{
					setInUse(false);
					setActive(false);
					onExit();
				}
			}
			getEffected().getAbnormalList().remove(this);
		}
	}

	public final void start()
	{
		if(setState(Abnormal.STARTING, Abnormal.STARTED))
			synchronized (this)
			{
				if(isInUse())
				{
					setActive(true);
					onStart();
					startEffectTask();
				}
			}
		run();
	}

	@Override
	public final void run()
	{
		if(setState(Abnormal.STARTED, Abnormal.ACTING))
			return;
		if(getState() == Abnormal.SUSPENDED)
		{
			if(isTimeLeft())
			{
				--_count;
				if(isTimeLeft())
					return;
			}
			exit();
			return;
		}
		if(getState() == Abnormal.ACTING && isTimeLeft())
		{
			--_count;
			if((!isActive() || onActionTime()) && isTimeLeft())
				return;
		}
		if(setState(Abnormal.ACTING, Abnormal.FINISHING))
			setInUse(false);
		if(setState(Abnormal.FINISHING, Abnormal.FINISHED))
		{
			synchronized (this)
			{
				setActive(false);
				stopEffectTask();
				onExit();
			}
			final Abnormal next = getNext();
			if(next != null && next.setState(Abnormal.SUSPENDED, Abnormal.STARTING))
				next.schedule();
			if(getSkill().getDelayedEffect() > 0)
				SkillTable.getInstance().getInfo(getSkill().getDelayedEffect(), 1).getEffects(_effector, _effected, false, false);
			final boolean msg = !isHidden() && getEffected().getAbnormalList().getCount(getSkill().getId()) == 1;
			getEffected().getAbnormalList().remove(this);
			if(msg)
				getEffected().sendPacket(new SystemMessage(92).addSkillName(_skill.getId(), _skill.getLevel()));

			getEffected().useTriggers(getEffected(), TriggerType.ON_FINISH_EFFECT, null, getSkill(), getTemplate(), 0);
		}
	}

	public final void exit()
	{
		if(_restoreTask != null)
			_restoreTask.cancel(false);
		final Abnormal next = getNext();
		if(next != null)
			next.exit();
		removeNext();
		if(setState(Abnormal.STARTING, Abnormal.FINISHED))
			getEffected().getAbnormalList().remove(this);
		else if(setState(Abnormal.SUSPENDED, Abnormal.FINISHED))
			stopEffectTask();
		else if(setState(Abnormal.STARTED, Abnormal.FINISHED) || setState(Abnormal.ACTING, Abnormal.FINISHED))
		{
			synchronized (this)
			{
				if(isInUse())
				{
					setInUse(false);
					setActive(false);
					stopEffectTask();
					onExit();
				}
			}
			getEffected().getAbnormalList().remove(this);
		}
	}

	private boolean scheduleNext(final Abnormal e)
	{
		if(e == null || e.isEnded())
			return false;
		final Abnormal next = getNext();
		if(next != null && !next.maybeScheduleNext(e))
			return false;
		_next = e;
		return true;
	}

	public Abnormal getNext()
	{
		return _next;
	}

	private void removeNext()
	{
		_next = null;
	}

	public boolean maybeScheduleNext(final Abnormal newEffect)
	{
		if(newEffect.getStackOrder() < getStackOrder())
		{
			if(newEffect.getTimeLeft() > getTimeLeft())
			{
				newEffect.suspend();
				scheduleNext(newEffect);
			}
			return false;
		}
		if(newEffect.getTimeLeft() >= getTimeLeft() || newEffect.getStackOrder() >= getStackOrder() && getStackType().equals("augment") || newEffect.getStackOrder() > getStackOrder() && !isOffensive() && newEffect.isOffensive())
		{
			if(getNext() != null && getNext().getTimeLeft() > newEffect.getTimeLeft())
			{
				newEffect.scheduleNext(getNext());
				removeNext();
			}
			exit();
		}
		else
		{
			suspend();
			newEffect.scheduleNext(this);
		}
		return true;
	}

	public Func[] getStatFuncs()
	{
		return getTemplate().getStatFuncs(this);
	}

	public void addIcon(final MagicEffectIcons mi)
	{
		if(!isActive() || isHidden())
			return;
		final int duration = _skill.isToggle() ? -1 : (int) (getTimeLeft() / 1000L);
		mi.addEffect(_displayId, _displayLevel, duration);
	}

	public void addPartySpelledIcon(final PartySpelled ps)
	{
		if(!isActive() || isHidden())
			return;
		final int duration = _skill.isToggle() ? -1 : (int) (getTimeLeft() / 1000L);
		ps.addPartySpelledEffect(_displayId, _displayLevel, duration);
	}

	public void addOlympiadSpelledIcon(final Player player, final ExOlympiadSpelledInfo os)
	{
		if(!isActive() || isHidden())
			return;
		final int duration = _skill.isToggle() ? -1 : (int) (getTimeLeft() / 1000L);
		os.addSpellRecivedPlayer(player);
		os.addEffect(_displayId, _displayLevel, duration);
	}

	protected int getLevel()
	{
		return _skill.getLevel();
	}

	public boolean containsStat(final Stats stat)
	{
		for(FuncTemplate func : getTemplate().getAttachedFuncs()) {
			if (func != null && func._stat == stat)
				return true;
		}
		return false;
	}

	public EffectType getEffectType()
	{
		return getTemplate()._effectType;
	}

	public boolean isHidden()
	{
		return _displayId < 0;
	}

	@Override
	public int compareTo(final Abnormal obj)
	{
		if(obj.equals(this))
			return 0;
		return 1;
	}

	public AbnormalEffect getAbnormalEffect()
	{
		return _abnormalEffect;
	}

	public boolean isSaveable()
	{
		return getTimeLeft() >= 15000L && getSkill().isSaveable();
	}

	public int getDisplayId()
	{
		return _displayId;
	}

	public int getDisplayLevel()
	{
		return _displayLevel;
	}

	@Override
	public String toString()
	{
		return "Skill: " + _skill + ", state: " + getState() + ", inUse: " + _inUse + ", active : " + _active;
	}

	@Override
	public boolean isFuncEnabled()
	{
		return isInUse();
	}

	@Override
	public boolean overrideLimits()
	{
		return false;
	}

	public boolean isOffensive()
	{
		return _template.isOffensive(getSkill().isOffensive());
	}

	public boolean isCubic()
	{
		return getEffectType() == EffectType.Cubic;
	}

	public String code()
	{
		return getSkill().getId() + getEffectType().toString() + getSkill().getLevel();
	}

	public final void restore(final boolean res)
	{
		if(res && Config.RESTORE_CANCEL_BUFFS > 0 && getTimeLeft() / 1000L - Config.RESTORE_CANCEL_BUFFS > 1L && getEffected() != null && getEffected().isPlayable())
		{
			final Playable p = (Playable) getEffected();
			if(p._resEffs == null)
				p._resEffs = new ArrayList<String>();
			final String code = code();
			if(!p._resEffs.contains(code))
			{
				suspend();
				p._resEffs.add(code);
				if(_restoreTask != null)
					_restoreTask.cancel(false);
				_restoreTask = ThreadPoolManager.getInstance().schedule(() -> {
					final Playable pe = (Playable) Abnormal.this.getEffected();
					if(pe != null && pe._resEffs != null && pe._resEffs.contains(code))
					{
						pe._resEffs.remove(code);
						if(Abnormal.this.setState(Abnormal.SUSPENDED, Abnormal.STARTING))
						{
							Abnormal.this.schedule();
							return;
						}
					}
					Abnormal.this.exit();
				}, Config.RESTORE_CANCEL_BUFFS * 1000L);
				return;
			}
		}
		exit();
	}

	static
	{
		_log = LoggerFactory.getLogger(Abnormal.class);
		EMPTY_L2EFFECT_ARRAY = new Abnormal[0];
		_emptyFunctionSet = new Func[0];
		Abnormal.SUSPENDED = -1;
		Abnormal.STARTING = 0;
		Abnormal.STARTED = 1;
		Abnormal.ACTING = 2;
		Abnormal.FINISHING = 3;
		Abnormal.FINISHED = 4;
	}
}
