package l2s.gameserver.skills.funcs;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;

public class FuncEnchantAdd extends Func
{
	public FuncEnchantAdd(final Stats stat, final int order, final Object owner, final double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public void calc(final Env env)
	{
		if(cond != null && !cond.test(env))
			return;
		final ItemInstance item = (ItemInstance) owner;
		env.value += value * item.getEnchantLevel();
	}
}
