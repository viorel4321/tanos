package l2s.gameserver.model.instances;

import java.util.List;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2s.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;

public class RainbowGourdInstance extends NpcInstance
{
	private CMGSiegeClanObject _winner;

	public RainbowGourdInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	public void doDecrease(final Creature character)
	{
		if(isDead())
			return;
		reduceCurrentHp(getMaxHp() * 0.2, character, null, 0, false, false, false, false, false, false, false, false);
	}

	public void doHeal()
	{
		if(isDead())
			return;
		this.setCurrentHp(getCurrentHp() + getMaxHp() * 0.2, false);
	}

	public void doSwitch(final RainbowGourdInstance npc)
	{
		if(isDead() || npc.isDead())
			return;
		final double currentHp = getCurrentHp();
		this.setCurrentHp(npc.getCurrentHp(), false);
		npc.setCurrentHp(currentHp, false);
	}

	@Override
	public void onDeath(final Creature killer)
	{
		super.onDeath(killer);
		final ClanHallMiniGameEvent miniGameEvent = this.getEvent(ClanHallMiniGameEvent.class);
		if(miniGameEvent == null)
			return;
		final Player player = killer.getPlayer();
		final CMGSiegeClanObject siegeClanObject = miniGameEvent.getSiegeClan("attackers", player.getClan());
		if(siegeClanObject == null)
			return;
		_winner = siegeClanObject;
		final List<CMGSiegeClanObject> attackers = miniGameEvent.getObjects("attackers");
		for(int i = 0; i < attackers.size(); ++i)
			if(attackers.get(i) != siegeClanObject)
			{
				final String arenaName = "arena_" + i;
				final SpawnExObject spawnEx = miniGameEvent.getFirstObject(arenaName);
				final RainbowYetiInstance yetiInstance = (RainbowYetiInstance) spawnEx.getSpawns().get(0).getFirstSpawned();
				yetiInstance.teleportFromArena();
				miniGameEvent.spawnAction(arenaName, false);
			}
	}

	@Override
	public void onDecay()
	{
		super.onDecay();
		final ClanHallMiniGameEvent miniGameEvent = this.getEvent(ClanHallMiniGameEvent.class);
		if(miniGameEvent == null)
			return;
		if(_winner == null)
			return;
		final List<CMGSiegeClanObject> attackers = miniGameEvent.getObjects("attackers");
		final int index = attackers.indexOf(_winner);
		final String arenaName = "arena_" + index;
		miniGameEvent.spawnAction(arenaName, false);
		final SpawnExObject spawnEx = miniGameEvent.getFirstObject(arenaName);
		final Spawner spawner = spawnEx.getSpawns().get(0);
		final Location loc = (Location) spawner.getCurrentSpawnRange();
		miniGameEvent.removeBanishItems();
		final NpcInstance npc = NpcUtils.spawnSingle(35600, loc.x, loc.y, loc.z, 0L);
		ThreadPoolManager.getInstance().schedule(() -> {
			final List<Player> around = World.getAroundPlayers(npc, 750, 100);
			npc.deleteMe();
			for(final Player player : around)
				player.teleToLocation(miniGameEvent.getResidence().getOwnerRestartPoint());
			miniGameEvent.processStep(_winner.getClan());
		}, 10000L);
	}

	@Override
	public boolean isAttackable(final Creature c)
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(final Creature c)
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}
}
