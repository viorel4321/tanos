package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.skills.Env;

@SuppressWarnings("unchecked")
public class ConditionTargetNpcClass extends Condition
{
	private final Class<NpcInstance> _npcClass;

	public ConditionTargetNpcClass(final String name)
	{
		Class<NpcInstance> classType = null;
		try
		{
			classType = (Class<NpcInstance>) Class.forName("l2s.gameserver.model.instances." + name + "Instance");
		}
		catch(ClassNotFoundException e)
		{
			classType = (Class<NpcInstance>) Scripts.getInstance().getClasses().get("npc.model." + name + "Instance");
		}
		if(classType == null)
			throw new IllegalArgumentException("Not found type class for type: " + name + ".");
		_npcClass = classType;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.target != null && env.target.getClass() == _npcClass;
	}
}
