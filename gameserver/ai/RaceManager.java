package l2s.gameserver.ai;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RaceManagerInstance;
import l2s.gameserver.network.l2.s2c.MonRaceInfo;

import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.CArrayIntSet;

public class RaceManager extends DefaultAI
{
	private boolean thinking = false;
	private IntSet _knownPlayers = new CArrayIntSet();

	public RaceManager(final NpcInstance actor)
	{
		super(actor);
		AI_TASK_ATTACK_DELAY = 5000L;
	}

	@Override
	public void run()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtThink()
	{
		final RaceManagerInstance actor = getActor();
		if(actor == null)
			return;

		final MonRaceInfo packet = actor.getPacket();
		if(packet == null)
			return;

		synchronized(this)
		{
			if(thinking)
				return;
			thinking = true;
		}

		try
		{
			final IntSet newPlayers = new CArrayIntSet();
			for(Player player : World.getAroundPlayers(actor, 1200, 200))
			{
				newPlayers.add(player.getObjectId());
				if(!_knownPlayers.contains(player.getObjectId()))
					player.sendPacket(packet);
				_knownPlayers.remove(player.getObjectId());
			}

			for(int playerObjectId : _knownPlayers.toArray())
			{
				final Player player2;
				if((player2 = GameObjectsStorage.getPlayer(playerObjectId)) != null)
					actor.removeKnownPlayer(player2);
			}
			_knownPlayers = newPlayers;
		}
		finally
		{
			thinking = false;
		}
	}

	@Override
	public RaceManagerInstance getActor()
	{
		return (RaceManagerInstance) super.getActor();
	}
}
