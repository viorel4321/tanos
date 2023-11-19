package l2s.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import l2s.commons.math.random.RndSelector;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.StatsSet;

public class Extract extends Skill
{
	private final RndSelector<ExtractGroup> _selector;
	private final boolean _isFish;
	private final boolean _itemOne;

	@SuppressWarnings("unchecked")
	public Extract(final StatsSet set)
	{
		super(set);
		List<ExtractGroup> extractGroupList = (List<ExtractGroup>)set.get("extractlist");
		if(extractGroupList == null)
			extractGroupList = Collections.emptyList();
		_selector = new RndSelector<ExtractGroup>(extractGroupList.size());
		for(final ExtractGroup g : extractGroupList)
			_selector.add(g, (int) (g._chance * 10000.0));
		_isFish = set.getBool("isFish", false);
		_itemOne = set.getBool("itemOne", false);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
		{
			final Player targetPlayer = target.getPlayer();
			if(targetPlayer == null)
				return;
			final ExtractGroup extractGroup = _selector.chance(1000000);
			if(extractGroup != null)
			{
				if(_itemOne)
				{
					final ExtractItem item = extractGroup.get(Rnd.get(extractGroup.size()));
					Functions.addItem(targetPlayer, item._itemId, _isFish ? (long) (item._count * Config.RATE_FISH_DROP_COUNT) : (long) item._count);
				}
				else
					for(final ExtractItem item2 : extractGroup)
						Functions.addItem(targetPlayer, item2._itemId, _isFish ? (long) (item2._count * Config.RATE_FISH_DROP_COUNT) : (long) item2._count);
			}
			else
				targetPlayer.sendPacket(new SystemMessage(1669));
		}
	}

	public static class ExtractItem
	{
		private final int _itemId;
		private final int _count;

		public ExtractItem(final int itemId, final int count)
		{
			_itemId = itemId;
			_count = count;
		}
	}

	public static class ExtractGroup extends ArrayList<ExtractItem>
	{
		private static final long serialVersionUID = -2124531921046325587L;
		private final double _chance;

		public ExtractGroup(final double chance)
		{
			_chance = chance;
		}
	}
}
