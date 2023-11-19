package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class FestivalMonsterInstance extends MonsterInstance
{
	protected int _bonusMultiplier;

	public FestivalMonsterInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_bonusMultiplier = 1;
	}

	public void setOfferingBonus(final int bonusMultiplier)
	{
		_bonusMultiplier = bonusMultiplier;
	}

	@Override
	public void spawnMe()
	{
		super.spawnMe();
		final List<Player> pl = World.getAroundPlayers(this);
		if(pl.isEmpty())
			return;
		final List<Player> alive = new ArrayList<Player>(9);
		for(final Player p : pl)
			if(!p.isDead())
				alive.add(p);
		if(alive.isEmpty())
			return;
		final Player target = alive.get(Rnd.get(alive.size()));
		getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1);
	}

	@Override
	public void doItemDrop(final Creature topDamager, final int lvl)
	{
		super.doItemDrop(topDamager, lvl);
		if(!topDamager.isPlayable())
			return;
		final Player topDamagerPlayer = topDamager.getPlayer();
		final Party associatedParty = topDamagerPlayer.getParty();
		if(associatedParty == null)
			return;
		final Player partyLeader = associatedParty.getPartyLeader();
		if(partyLeader == null)
			return;
		final ItemInstance bloodOfferings = ItemTable.getInstance().createItem(5901);
		bloodOfferings.setCount(_bonusMultiplier);
		partyLeader.getInventory().addItem(bloodOfferings);
		partyLeader.sendPacket(SystemMessage.obtainItems(5901, _bonusMultiplier, 0));
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 1000;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}
