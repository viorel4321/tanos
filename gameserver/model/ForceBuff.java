package l2s.gameserver.model;

import java.util.concurrent.Future;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.effects.EffectForce;
import l2s.gameserver.tables.SkillTable;

public class ForceBuff
{
	private Creature _caster;
	private Creature _target;
	private Skill _skill;
	private Skill _force;
	private Future<?> _task;

	public Creature getCaster()
	{
		return _caster;
	}

	public Creature getTarget()
	{
		return _target;
	}

	public Skill getSkill()
	{
		return _skill;
	}

	public Skill getForce()
	{
		return _force;
	}

	public ForceBuff(final Creature caster, final Creature target, final Skill skill)
	{
		_caster = caster;
		_target = target;
		_skill = skill;
		_force = SkillTable.getInstance().getInfo(skill.getForceId(), 1);
		for(final Abnormal e : getTarget().getAbnormalList().values())
			if(e.getSkill().getId() == getForce().getId())
			{
				final EffectForce ef = (EffectForce) e;
				if(ef.forces >= 3)
					return;
				continue;
			}
		final Runnable r = new Runnable(){
			@Override
			public void run()
			{
				final int forceId = ForceBuff.this.getForce().getId();
				boolean create = true;
				for(final Abnormal e : ForceBuff.this.getTarget().getAbnormalList().values())
					if(e.getSkill().getId() == forceId)
					{
						((EffectForce) e).increaseForce();
						create = false;
						break;
					}
				if(create)
					ForceBuff.this.getForce().getEffects(_caster, ForceBuff.this.getTarget(), false, false);
			}
		};
		_task = ThreadPoolManager.getInstance().schedule(r, Config.START_FORCE_EFFECT);
	}

	public void delete()
	{
		if(_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
		final int toDeleteId = getForce().getId();
		for(final Abnormal e : _target.getAbnormalList().values())
			if(e.getSkill().getId() == toDeleteId)
			{
				((EffectForce) e).decreaseForce();
				break;
			}
		_caster.setForceBuff(null);
		_caster.abortCast(true, false);
	}
}
