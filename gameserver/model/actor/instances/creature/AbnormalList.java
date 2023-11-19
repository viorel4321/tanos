package l2s.gameserver.model.actor.instances.creature;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.gameserver.skills.SkillInfo;
import org.apache.commons.lang3.ArrayUtils;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.skills.funcs.Func;

public class  AbnormalList implements Iterable<Abnormal>
{
	public static final int NONE_SLOT_TYPE = -1;
	public static final int BUFF_SLOT_TYPE = 0;
	public static final int TRIGGER_SLOT_TYPE = 1;
	public static final int DEBUFF_SLOT_TYPE = 2;
	public static final int DEBUFF_LIMIT = 8;
	public static final int TRIGGER_LIMIT = 12;

	private Creature _actor;
	private final Collection<Abnormal> _abnormals = new ConcurrentLinkedQueue<Abnormal>();
	private Lock lock;

	public AbnormalList(Creature owner)
	{
		lock = new ReentrantLock();
		_actor = owner;
	}

	@Override
	public Iterator<Abnormal> iterator()
	{
		return _abnormals.iterator();
	}

	public int getCount(int skill_id)
	{
		if(isEmpty())
			return 0;

		int count = 0;
		for(Abnormal a : _abnormals)
		{
			if(a.getSkill().getId() == skill_id)
				++count;
		}
		return count;
	}

	public Abnormal getEffectByType(EffectType et)
	{
		if(isEmpty())
			return null;

		for(Abnormal a : _abnormals)
		{
			if(a.getEffectType() == et)
				return a;
		}
		return null;
	}

	public List<Abnormal> getEffectsByType(EffectType et)
	{
		if(isEmpty())
			return Collections.emptyList();

		List<Abnormal> list = new ArrayList<Abnormal>();
		for(Abnormal a : _abnormals)
		{
			if(a.getEffectType() == et)
				list.add(a);
		}
		return list;
	}

	public List<Abnormal> getEffectsBySkill(final Skill skill)
	{
		if(skill == null)
			return null;
		return getEffectsBySkillId(skill.getId());
	}

	public List<Abnormal> getEffectsBySkillId(final int skillId)
	{
		if(isEmpty())
			return null;
		final List<Abnormal> list = new ArrayList<Abnormal>(2);
		for(final Abnormal e : _abnormals)
			if(e.getSkill().getId() == skillId)
				list.add(e);
		return list.isEmpty() ? null : list;
	}

	public Abnormal getEffectBySkillId(final int skillId)
	{
		if(isEmpty())
			return null;
		for(final Abnormal e : _abnormals)
			if(e.getSkill().getId() == skillId)
				return e;
		return null;
	}

	public Abnormal getEffectByIndexAndType(final int skillId, final EffectType type)
	{
		if(isEmpty())
			return null;
		for(final Abnormal e : _abnormals)
			if(e.getSkill().getId() == skillId && e.getEffectType() == type)
				return e;
		return null;
	}

	public Abnormal getEffectByStackType(final String type)
	{
		if(isEmpty())
			return null;
		for(final Abnormal e : _abnormals)
			if(e.getStackType().equals(type))
				return e;
		return null;
	}

	public boolean containEffectFromSkills(final int[] skillIds)
	{
		if(isEmpty())
			return false;
		for(final Abnormal e : _abnormals)
		{
			final int skillId = e.getSkill().getId();
			if(ArrayUtils.contains(skillIds, skillId))
				return true;
		}
		return false;
	}

	public List<Abnormal> values()
	{
		if(isEmpty())
			return Collections.emptyList();
		return new ArrayList<>(_abnormals);
	}

	public boolean isEmpty()
	{
		return _abnormals == null || _abnormals.isEmpty();
	}

	public Abnormal[] getAllFirstEffects()
	{
		if(isEmpty())
			return Abnormal.EMPTY_L2EFFECT_ARRAY;
		final TIntObjectHashMap<Abnormal> map = new TIntObjectHashMap<Abnormal>();
		for(final Abnormal e : _abnormals)
			map.put(e.getSkill().getId(), e);
		return map.values(new Abnormal[map.size()]);
	}

	private void checkSlotLimit(final Abnormal newEffect)
	{
		if(_abnormals == null)
			return;
		final int slotType = getSlotType(newEffect);
		if(slotType == -1)
			return;
		int size = 0;
		final TIntArrayList skillIds = new TIntArrayList();
		for(final Abnormal e : _abnormals)
			if(e.isInUse())
			{
				if(e.getSkill().equals(newEffect.getSkill()))
					return;
				if(skillIds.contains(e.getSkill().getId()))
					continue;
				final int subType = getSlotType(e);
				if(subType != slotType)
					continue;
				++size;
				skillIds.add(e.getSkill().getId());
			}
		int limit = 0;
		switch(slotType)
		{
			case 0:
			{
				limit = _actor.getBuffLimit();
				break;
			}
			case 2:
			{
				limit = Config.DEBUFF_LIMIT;
				break;
			}
			case 1:
			{
				limit = Config.TRIGGER_LIMIT;
				break;
			}
		}
		if(size < limit)
			return;
		int skillId = 0;
		for(final Abnormal e2 : _abnormals)
			if(e2.isInUse() && getSlotType(e2) == slotType)
			{
				skillId = e2.getSkill().getId();
				break;
			}
		if(skillId != 0)
			this.stop(skillId);
	}

	public static int getSlotType(final Abnormal e)
	{
		if(e.isHidden() || e.getSkill().isPassive() || e.getSkill().isToggle() || e.getStackType().equalsIgnoreCase("HpRecoverCast") || e.getStackType().equalsIgnoreCase("MpRecoverCast") || e.getEffectType() == EffectType.Cubic || e.getEffectType() == EffectType.CharmOfCourage)
			return -1;
		if(e.isOffensive())
			return 2;
		if(e.getSkill().isTrigger())
			return 1;
		return 0;
	}

	public static boolean checkStackType(final EffectTemplate ef1, final EffectTemplate ef2)
	{
		return !ef1._stackType.equals(EffectTemplate.NO_STACK) && ef1._stackType.equalsIgnoreCase(ef2._stackType) || !ef1._stackType.equals(EffectTemplate.NO_STACK) && ef1._stackType.equalsIgnoreCase(ef2._stackType2) || !ef1._stackType2.equals(EffectTemplate.NO_STACK) && ef1._stackType2.equalsIgnoreCase(ef2._stackType) || !ef1._stackType2.equals(EffectTemplate.NO_STACK) && ef1._stackType2.equalsIgnoreCase(ef2._stackType2);
	}

	public void add(final Abnormal effect)
	{
		final double hp = _actor.getCurrentHp();
		final double mp = _actor.getCurrentMp();
		final double cp = _actor.getCurrentCp();
		final String stackType = effect.getStackType();
		boolean add = false;
		lock.lock();
		try
		{
			if(stackType.equals(EffectTemplate.NO_STACK))
				for(final Abnormal e : _abnormals)
				{
					if(!e.isInUse())
						continue;
					if(!e.getStackType().equals(EffectTemplate.NO_STACK) || e.getSkill().getId() != effect.getSkill().getId() || e.getEffectType() != effect.getEffectType())
						continue;
					if(effect.getTimeLeft() <= e.getTimeLeft())
						return;
					e.exit();
				}
			else
				for(final Abnormal e : _abnormals)
				{
					if(!e.isInUse())
						continue;
					if(!checkStackType(e.getTemplate(), effect.getTemplate()))
						continue;
					if(e.getSkill().getId() == effect.getSkill().getId() && e.getEffectType() != effect.getEffectType())
						break;
					if(e.getStackOrder() == -1.0)
						return;
					if(!e.maybeScheduleNext(effect))
						return;
				}
			checkSlotLimit(effect);
			if(add = _abnormals.add(effect))
				effect.setInUse(true);
		}
		finally
		{
			lock.unlock();
		}
		if(!add)
			return;
		effect.start();
		for(final Func ft : effect.getStatFuncs())
			if(ft.stat == Stats.MAX_HP)
				_actor.setCurrentHp(hp, false);
			else if(ft.stat == Stats.MAX_MP)
				_actor.setCurrentMp(mp);
			else if(ft.stat == Stats.MAX_CP)
				_actor.setCurrentCp(cp);
		_actor.updateStats();
		_actor.updateEffectIcons();
	}

	public void remove(final Abnormal effect)
	{
		if(effect == null)
			return;
		boolean remove = false;
		lock.lock();
		try
		{
			if(_abnormals == null)
				return;
			if(!(remove = _abnormals.remove(effect)))
				return;
		}
		finally
		{
			lock.unlock();
		}
		if(!remove)
			return;
		_actor.updateStats();
		_actor.updateEffectIcons();
	}

	public void stopAll()
	{
		if(Config.RESTORE_CANCEL_BUFFS > 0 && _actor.isPlayable())
			((Playable) _actor)._resEffs = null;
		if(isEmpty())
			return;
		lock.lock();
		try
		{
			for(final Abnormal e : _abnormals)
				e.exit();
		}
		finally
		{
			lock.unlock();
		}
		_actor.updateStats();
		_actor.updateEffectIcons();
	}

	public void stop()
	{
		if(Config.RESTORE_CANCEL_BUFFS > 0 && _actor.isPlayable())
			((Playable) _actor)._resEffs = null;

		if(isEmpty())
			return;

		lock.lock();
		try
		{
			for(final Abnormal e : _abnormals)
			{
				if(!e.getSkill().isToggle())
					e.exit();
			}
		}
		finally
		{
			lock.unlock();
		}

		_actor.updateStats();
		_actor.updateEffectIcons();
	}

	public int stop(int skillId, int skillLvl)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(final Abnormal e : _abnormals)
		{
			if(e.getSkill().getId() == skillId || e.getSkill().getLevel() == skillLvl)
				e.exit();
		}
		return removed;
	}

	public int stop(int skillId)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(final Abnormal e : _abnormals)
		{
			if(e.getSkill().getId() == skillId)
				e.exit();
		}
		return removed;
	}

	public int stop(SkillInfo skillInfo)
	{
		if(skillInfo == null)
			return 0;
		return stop(skillInfo.getId());
	}

	public int stopByDisplayId(int skillId)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(final Abnormal e : _abnormals) {
			if (e.getSkill().getDisplayId() == skillId)
				e.exit();
		}
		return removed;
	}

	public int stop(EffectType type)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(final Abnormal e : _abnormals) {
			if (e.getEffectType() == type)
				e.exit();
		}
		return removed;
	}

	public int stop(SkillInfo skillInfo, boolean checkLevel)
	{
		if(skillInfo == null)
			return 0;

		if(checkLevel)
			return stop(skillInfo.getId(), skillInfo.getLevel());
		return stop(skillInfo.getId());
	}

	public void stopAll(EffectType type)
	{
		if(isEmpty())
			return;

		final TIntHashSet skillIds = new TIntHashSet();
		for(final Abnormal e : _abnormals)
			if(e.getEffectType() == type)
				skillIds.add(e.getSkill().getId());
		for(final int skillId : skillIds.toArray())
			this.stop(skillId);
	}
}
