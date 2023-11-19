package l2s.gameserver.model.instances;

import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;

public class LidiaVonHellmannInstance extends SiegeGuardInstance
{
	public LidiaVonHellmannInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onDeath(final Creature killer)
	{
		final SiegeEvent<?, ?> siegeEvent = this.getEvent(SiegeEvent.class);
		if(siegeEvent == null)
			return;
		siegeEvent.processStep(getMostDamagedClan());
		super.onDeath(killer);
	}

	public Clan getMostDamagedClan()
	{
		final ClanHallSiegeEvent siegeEvent = this.getEvent(ClanHallSiegeEvent.class);
		Player temp = null;
		final Map<Player, Integer> damageMap = new HashMap<Player, Integer>();
		for(final AggroList.HateInfo info : getAggroList().getPlayableMap().values())
		{
			final Playable killer = (Playable) info.attacker;
			final int damage = info.damage;
			if(killer.isSummon())
				temp = killer.getPlayer();
			else if(killer.isPlayer())
				temp = (Player) killer;
			if(temp != null)
			{
				if(siegeEvent.getSiegeClan("attackers", temp.getClan()) == null)
					continue;
				if(!damageMap.containsKey(temp))
					damageMap.put(temp, damage);
				else
				{
					final int dmg = damageMap.get(temp) + damage;
					damageMap.put(temp, dmg);
				}
			}
		}
		int mostDamage = 0;
		Player player = null;
		for(final Map.Entry<Player, Integer> entry : damageMap.entrySet())
		{
			final int damage2 = entry.getValue();
			final Player t = entry.getKey();
			if(damage2 > mostDamage)
			{
				mostDamage = damage2;
				player = t;
			}
		}
		return player == null ? null : player.getClan();
	}

	@Override
	public boolean isEffectImmune()
	{
		return true;
	}
}
