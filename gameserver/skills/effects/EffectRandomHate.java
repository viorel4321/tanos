package l2s.gameserver.skills.effects;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.skills.Env;

public class EffectRandomHate extends Abnormal
{
	public EffectRandomHate(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffected().isMonster();
	}

	@Override
	public void onStart()
	{
		final MonsterInstance monster = (MonsterInstance) getEffected();
		final Creature mostHated = monster.getAggroList().getMostHated();
		if(mostHated == null)
			return;
		final AggroList.AggroInfo mostAggroInfo = monster.getAggroList().get(mostHated);
		final List<Creature> hateList = monster.getAggroList().getHateList(monster.getAggroRange());
		hateList.remove(mostHated);
		if(!hateList.isEmpty())
		{
			final AggroList.AggroInfo newAggroInfo = monster.getAggroList().get(hateList.get(Rnd.get(hateList.size())));
			final int oldHate = newAggroInfo.hate;
			newAggroInfo.hate = mostAggroInfo.hate;
			mostAggroInfo.hate = oldHate;
		}
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
