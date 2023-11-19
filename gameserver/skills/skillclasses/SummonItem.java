package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.StatsSet;

public class SummonItem extends Skill
{
	private final int _itemId;
	private final int _minId;
	private final int _maxId;
	private final int _minCount;
	private final int _maxCount;
	private int count;

	public SummonItem(final StatsSet set)
	{
		super(set);
		_itemId = set.getInteger("SummonItemId", 0);
		_minId = set.getInteger("SummonMinId", 0);
		_maxId = set.getInteger("SummonMaxId", _minId);
		_minCount = set.getInteger("SummonMinCount");
		_maxCount = set.getInteger("SummonMaxCount", _minCount);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger))
		{
			if(activeChar.isPlayer())
			{
				count = Rnd.get(_minCount, _maxCount);
				final ItemInstance i = ((Player) activeChar).getInventory().getItemByItemId(_itemId);
				if(i != null && i.isStackable() && i.getCount() + count > Integer.MAX_VALUE)
				{
					activeChar.sendPacket(new SystemMessage(129));
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				Inventory inventory;
				if(target.isPlayer())
					inventory = ((Player) target).getInventory();
				else
				{
					if(!target.isPet())
						continue;
					inventory = ((PetInstance) target).getInventory();
				}
				ItemInstance item = ItemTable.getInstance().createItem(_minId > 0 ? Rnd.get(_minId, _maxId) : _itemId);
				item.setCount(count);
				activeChar.sendPacket(SystemMessage.obtainItems(item));
				item = inventory.addItem(item);
				activeChar.sendChanges();
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
			}
	}
}
