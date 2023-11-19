package l2s.gameserver.skills.effects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.utils.Util;

public class EffectForce extends Abnormal
{
	private static final Logger _log;
	public int forces;
	private int _range;
	private int _skId;

	public EffectForce(final Env env, final EffectTemplate template)
	{
		super(env, template);
		forces = 0;
		_range = -1;
		_skId = 0;
		forces = getSkill().getLevel();
		_range = getSkill().getCastRange();
		_skId = getSkill().getId();
	}

	@Override
	public boolean onActionTime()
	{
		if(!Util.checkIfInRange(_range, getEffector(), getEffected(), true))
			getEffector().abortCast(true, false);
		return true;
	}

	public void increaseForce()
	{
		if(forces < 3)
		{
			++forces;
			updateBuff();
		}
	}

	public void decreaseForce()
	{
		--forces;
		if(forces < 1)
			exit();
		else
			updateBuff();
	}

	public void updateBuff()
	{
		exit();
		final Skill newSkill = SkillTable.getInstance().getInfo(_skId, Math.min(Math.max(forces, 1), 3));
		newSkill.getEffects(getEffector(), getEffected(), false, false);
	}

	static
	{
		_log = LoggerFactory.getLogger(EffectForce.class);
	}
}
