package l2s.gameserver.model.instances;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ItemToDrop;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class BoxInstance extends MonsterInstance
{
	public BoxInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public final boolean isMovementDisabled()
	{
		return true;
	}

	public void onOpen(final Player opener)
	{
		setSpoiled(false, null);
		opener.broadcastPacket(new SocialAction(opener.getObjectId(), 3));
		final double[] xpsp = calculateExpAndSp(opener, opener.getLevel(), getMaxHp(), false);
		opener.addExpAndSp((long) xpsp[0], (long) xpsp[1], true, true);
		if(NpcTable.getTemplate(getNpcId()) != null && NpcTable.getTemplate(getNpcId()).getDropData() != null)
		{
			final List<ItemToDrop> drops = NpcTable.getTemplate(getNpcId()).getDropData().rollDrop(0, this, opener, 1.0);
			for(final ItemToDrop drop : drops)
				this.dropItem(opener, drop.itemId, drop.count);
		}
		if(Config.CUSTOM_BOX_DROP && getLevel() >= 78 && Rnd.chance(30))
		{
			final int count = Rnd.get(1, 5);
			opener.getInventory().addItem(6673, count);
			opener.sendPacket(new ItemList(opener, false));
			opener.sendPacket(SystemMessage.obtainItems(6673, count, 0));
		}
		doDie(null);
	}

	@Override
	public int getTrueId()
	{
		switch(getNpcId())
		{
			case 18287:
			case 18288:
			{
				return 21671;
			}
			case 18289:
			case 18290:
			{
				return 21694;
			}
			case 18291:
			case 18292:
			{
				return 21717;
			}
			case 18293:
			case 18294:
			{
				return 21740;
			}
			case 18295:
			case 18296:
			{
				return 21763;
			}
			case 18297:
			case 18298:
			{
				return 21786;
			}
			default:
			{
				return getNpcId() + 3536;
			}
		}
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean isBox()
	{
		return true;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}
