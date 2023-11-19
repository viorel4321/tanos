package l2s.gameserver.model.instances;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectTasks;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class GustavInstance extends SiegeGuardInstance implements _34SiegeGuard
{
	private AtomicBoolean _canDead;
	private Future<?> _teleportTask;

	public GustavInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_canDead = new AtomicBoolean();
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_canDead.set(false);
		Functions.npcShout(this, "Prepare to die, foreign invaders! I am Gustav, the eternal ruler of this fortress and I have taken up my sword to repel thee!", 0);
	}

	@Override
	public void onDeath(final Creature killer)
	{
		if(!_canDead.get())
		{
			_canDead.set(true);
			this.setCurrentHp(1.0, false);
			for(final Creature cha : World.getAroundCharacters(this))
				ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(cha, CtrlEvent.EVT_FORGET_OBJECT, this, null));
			final ClanHallSiegeEvent siegeEvent = this.getEvent(ClanHallSiegeEvent.class);
			if(siegeEvent == null)
				return;
			final SpawnExObject obj = siegeEvent.getFirstObject("boss");
			for(int i = 0; i < 3; ++i)
			{
				final NpcInstance npc = obj.getSpawns().get(i).getFirstSpawned();
				Functions.npcSay(npc, ((_34SiegeGuard) npc).teleChatSay());
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 4235, 1, 10000, 0L));
				_teleportTask = ThreadPoolManager.getInstance().schedule(() -> {
					final Location loc = Location.findAroundPosition(177134, -18807, -2256, 50, 100, npc.getGeoIndex());
					npc.teleToLocation(loc);
					if(npc == GustavInstance.this)
						npc.onDeath(null);
				}, 10000L);
			}
		}
		else
		{
			if(_teleportTask != null)
			{
				_teleportTask.cancel(false);
				_teleportTask = null;
			}
			final SiegeEvent<?, ?> siegeEvent2 = this.getEvent(SiegeEvent.class);
			if(siegeEvent2 == null)
				return;
			siegeEvent2.processStep(getMostDamagedClan());
			super.onDeath(killer);
		}
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
	public String teleChatSay()
	{
		return "This is unbelievable! Have I really been defeated? I shall return and take your head!";
	}

	@Override
	public boolean isEffectImmune()
	{
		return true;
	}
}
