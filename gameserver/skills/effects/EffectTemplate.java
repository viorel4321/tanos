package l2s.gameserver.skills.effects;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.conditions.Condition;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.templates.StatsSet;

public final class EffectTemplate extends StatTemplate
{
	static Logger _log = LoggerFactory.getLogger(EffectTemplate.class);

	public static final String NO_STACK = "none".intern();

	public Condition _attachCond;
	public final double _value;
	public final int _counter;
	public final long _period;
	public AbnormalEffect _abnormalEffect;
	public final EffectType _effectType;
	public final String _stackType;
	public final String _stackType2;
	public final double _stackOrder;
	public final int _displayId;
	public final int _displayLevel;
	public final boolean _applyOnCaster;
	public final boolean _isReflectable;
	public final boolean _immuneResists;
	private final Boolean _isOffensive;
	private final int _chance;
	public final int _cubicId;
	public final int _cubicLevel;

	public EffectTemplate(final StatsSet set)
	{
		_value = set.getDouble("value");
		_counter = set.getInteger("count", 1) < 0 ? Integer.MAX_VALUE : set.getInteger("count", 1);
		_period = (long) Math.min(2.147483647E9, 1000.0 * (set.getDouble("time", 1.0) < 0.0 ? 2.147483647E9 : set.getDouble("time", 1.0)));
		_abnormalEffect = set.getEnum("abnormal", AbnormalEffect.class);
		_stackType = set.getString("stackType", EffectTemplate.NO_STACK);
		_stackType2 = set.getString("stackType2", EffectTemplate.NO_STACK);
		_stackOrder = set.getDouble("stackOrder", _stackType.equals(EffectTemplate.NO_STACK) && _stackType2.equals(EffectTemplate.NO_STACK) ? 1.0 : 0.0);
		_applyOnCaster = set.getBool("applyOnCaster", false);
		_displayId = set.getInteger("displayId", 0);
		_displayLevel = set.getInteger("displayLevel", 0);
		_effectType = set.getEnum("name", EffectType.class);
		_isReflectable = set.getBool("isReflectable", true);
		_immuneResists = set.getBool("immuneResists", false);
		_isOffensive = set.isSet("isOffensive") ? set.getBool("isOffensive") : null;
		_chance = set.getInteger("chance", Integer.MAX_VALUE);
		_cubicId = set.getInteger("cubicId", 0);
		_cubicLevel = set.getInteger("cubicLevel", 0);
	}

	public Abnormal getEffect(final Env env)
	{
		if(_attachCond != null && !_attachCond.test(env))
			return null;
		try
		{
			return _effectType.makeEffect(env, this);
		}
		catch(Exception e)
		{
			EffectTemplate._log.error("EffectTemplate.getEffect: ", e);
			return null;
		}
	}

	public void attachCond(final Condition c)
	{
		_attachCond = c;
	}

	public long getPeriod()
	{
		return _period;
	}

	public EffectType getEffectType()
	{
		return _effectType;
	}

	public Abnormal getSameByStackType(final List<Abnormal> list)
	{
		for(final Abnormal ef : list)
			if(ef != null && AbnormalList.checkStackType(ef.getTemplate(), this))
				return ef;
		return null;
	}

	public Abnormal getSameByStackType(final AbnormalList list)
	{
		return this.getSameByStackType(list.values());
	}

	public Abnormal getSameByStackType(final Creature actor)
	{
		return this.getSameByStackType(actor.getAbnormalList().values());
	}

	public int chance(final int val)
	{
		return _chance == Integer.MAX_VALUE ? val : _chance;
	}

	public boolean isOffensive(final boolean def)
	{
		return _isOffensive != null ? _isOffensive : def;
	}
}
