package l2s.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.DimensionalRiftManager;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.utils.Location;

public class DimensionalRift
{
	private int _type;
	private Party _party;
	private List<Integer> _completedRooms;
	private static final long seconds_5 = 5000L;
	private static final int MILLISECONDS_IN_MINUTE = 60000;
	private int jumps_current;
	private Future<?> teleporterTask;
	private Future<?> spawnTask;
	private Future<?> killRiftTask;
	private int _choosenRoom;
	private boolean _hasJumped;
	private List<Player> deadPlayers;
	private List<Player> reviedInWaitingRoom;
	private boolean isBossRoom;
	private List<Spawn> _spawns;
	private boolean isCollapseStarted;
	private int _instanceID;
	private NpcInstance _npc;

	public DimensionalRift(final Party party, final int type, final int room)
	{
		_completedRooms = new ArrayList<Integer>();
		jumps_current = 0;
		_choosenRoom = -1;
		_hasJumped = false;
		deadPlayers = new CopyOnWriteArrayList<Player>();
		reviedInWaitingRoom = new CopyOnWriteArrayList<Player>();
		isBossRoom = false;
		_spawns = new ArrayList<Spawn>();
		isCollapseStarted = false;
		_instanceID = 0;
		_type = type;
		_party = party;
		checkBossRoom(_choosenRoom = room);
		for(int i = 300; i < 3000; ++i)
			if(!DimensionalRiftManager.insts.contains(i))
			{
				DimensionalRiftManager.insts.add(i);
				_instanceID = i;
				break;
			}
		party.setDimensionalRift(this);
		final Location coords = getRoomCoord(room);
		_npc = Quest.addSpawnToInstance(31865, coords, 0, _instanceID);
		for(final Player p : party.getPartyMembers())
			DimensionalRiftManager.teleToLocation(p, coords.rnd(50, 100, false), _instanceID);
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}

	public int getType()
	{
		return _type;
	}

	public int getCurrentRoom()
	{
		return _choosenRoom;
	}

	private void createTeleporterTimer(final boolean reasonTP)
	{
		if(teleporterTask != null)
		{
			teleporterTask.cancel(false);
			teleporterTask = null;
		}
		teleporterTask = ThreadPoolManager.getInstance().schedule(() -> {
			if(reasonTP && jumps_current < DimensionalRift.this.getMaxJumps() && _party.getMemberCount() > deadPlayers.size() && _completedRooms.size() < DimensionalRiftManager.getInstance().getRooms(_type).size() - 1)
			{
				jumps_current++;
				_completedRooms.add(_choosenRoom);
				_choosenRoom = -1;
				for(final Player p : _party.getPartyMembers())
					if(!reviedInWaitingRoom.contains(p))
						DimensionalRift.this.teleportToNextRoom(p);
				DimensionalRift.this.createSpawnTimer(_choosenRoom);
				DimensionalRift.this.createTeleporterTimer(true);
			}
			else
			{
				for(final Player p : _party.getPartyMembers())
					if(!reviedInWaitingRoom.contains(p))
						DimensionalRift.this.teleportToWaitingRoom(p);
				DimensionalRift.this.createNewKillRiftTimer();
			}
		}, reasonTP ? calcTimeToNextJump() : 5000L);
	}

	public void createSpawnTimer(final int room)
	{
		if(spawnTask != null)
		{
			spawnTask.cancel(false);
			spawnTask = null;
		}
		clearSpawn();
		final DimensionalRiftManager.DimensionalRiftRoom riftRoom = DimensionalRiftManager.getInstance().getRoom(_type, room);
		spawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			for(final Spawn s : riftRoom.getSpawns())
			{
				final Spawn sp = s.clone();
				sp.setInstanceId(_instanceID);
				_spawns.add(sp);
				if(!isBossRoom)
					sp.startRespawn();
				for(int i = 0; i < sp.getAmount(); ++i)
					sp.doSpawn(true);
			}
		}, Config.RIFT_SPAWN_DELAY);
	}

	public synchronized void createNewKillRiftTimer()
	{
		if(killRiftTask != null)
		{
			killRiftTask.cancel(false);
			killRiftTask = null;
		}
		killRiftTask = ThreadPoolManager.getInstance().schedule(() -> {
			if(isCollapseStarted)
				return;
			isCollapseStarted = true;
			DimensionalRift.this.collapse();
		}, 100L);
	}

	public void partyMemberInvited()
	{
		createTeleporterTimer(false);
	}

	public void partyMemberExited(final Player player)
	{
		if(deadPlayers.contains(player))
			deadPlayers.remove(player);
		if(reviedInWaitingRoom.contains(player))
			reviedInWaitingRoom.remove(player);
		if(_party.getMemberCount() < Config.RIFT_MIN_PARTY_SIZE || _party.getMemberCount() == 1)
		{
			for(final Player p : _party.getPartyMembers())
				teleportToWaitingRoom(p);
			createNewKillRiftTimer();
		}
	}

	public void manualTeleport(final Player player, final NpcInstance npc)
	{
		if(!player.isInParty() || !player.getParty().isInDimensionalRift())
			return;
		if(!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;
		}
		if(jumps_current == getMaxJumps())
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/UsedAllJumps.html", npc);
			return;
		}
		if(_hasJumped)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/AllreadyTeleported.html", npc);
			return;
		}
		if(!Config.ALLOW_JUMP_BOSS)
		{
			final int size = DimensionalRiftManager.getInstance().getRooms(_type).size();
			if(_completedRooms.size() == size - 2 && !_completedRooms.contains(size) && size != _choosenRoom)
			{
				player.sendMessage("You have visited all rooms!");
				return;
			}
		}
		_hasJumped = true;
		_completedRooms.add(_choosenRoom);
		_choosenRoom = Config.ALLOW_JUMP_BOSS ? -1 : -2;
		for(final Player p : _party.getPartyMembers())
			teleportToNextRoom(p);
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}

	public void manualExitRift(final Player player, final NpcInstance npc)
	{
		if(!player.isInParty() || !player.getParty().isInDimensionalRift())
			return;
		if(!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;
		}
		for(final Player p : player.getParty().getPartyMembers())
			teleportToWaitingRoom(p);
		createNewKillRiftTimer();
	}

	private void teleportToNextRoom(final Player player)
	{
		if(_choosenRoom < 0)
		{
			clearSpawn();
			if(_npc != null)
			{
				_npc.deleteMe();
				_npc = null;
			}
			int size = DimensionalRiftManager.getInstance().getRooms(_type).size();
			if(_choosenRoom == -2)
				--size;
			final List<Integer> notCompletedRooms = new ArrayList<Integer>();
			for(int i = 1; i <= size; ++i)
				if(!_completedRooms.contains(i))
					notCompletedRooms.add(i);
			if(Rnd.chance(Config.BOSS_ROOM_CHANCE) && notCompletedRooms.contains(9))
				_choosenRoom = 9;
			else
				_choosenRoom = notCompletedRooms.get(Rnd.get(notCompletedRooms.size()));
			checkBossRoom(_choosenRoom);
			_npc = Quest.addSpawnToInstance(31865, getRoomCoord(_choosenRoom), 0, _instanceID);
		}
		DimensionalRiftManager.teleToLocation(player, getRoomCoord(_choosenRoom).rnd(50, 100, false), _instanceID);
	}

	private void teleportToWaitingRoom(final Player player)
	{
		DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
	}

	public void collapse()
	{
		Future<?> task = teleporterTask;
		if(task != null)
		{
			task.cancel(false);
			teleporterTask = null;
		}
		task = spawnTask;
		if(task != null)
		{
			task.cancel(false);
			spawnTask = null;
		}
		task = killRiftTask;
		if(task != null)
		{
			task.cancel(false);
			killRiftTask = null;
		}
		task = null;
		clearSpawn();
		if(_npc != null)
		{
			_npc.deleteMe();
			_npc = null;
		}
		_completedRooms = null;
		if(_party != null)
			_party.setDimensionalRift(null);
		_party = null;
		reviedInWaitingRoom = null;
		deadPlayers = null;
		if(DimensionalRiftManager.insts.contains(_instanceID))
			for(int size = DimensionalRiftManager.insts.size(), i = 0; i < size; ++i)
				if(DimensionalRiftManager.insts.get(i) == _instanceID)
				{
					DimensionalRiftManager.insts.remove(i);
					break;
				}
	}

	private long calcTimeToNextJump()
	{
		if(isBossRoom)
			return (long) ((Config.RIFT_AUTO_JUMPS_TIME * 60000 + Rnd.get(0, Config.RIFT_AUTO_JUMPS_TIME_RAND)) * Config.RIFT_BOSS_ROOM_TIME_MUTIPLY);
		return Config.RIFT_AUTO_JUMPS_TIME * 60000 + Rnd.get(0, Config.RIFT_AUTO_JUMPS_TIME_RAND);
	}

	public void memberDead(final Player player)
	{
		if(!deadPlayers.contains(player))
			deadPlayers.add(player);
	}

	public void memberRessurected(final Player player)
	{
		if(deadPlayers.contains(player))
			deadPlayers.remove(player);
	}

	public void usedTeleport(final Player player)
	{
		if(!reviedInWaitingRoom.contains(player))
			reviedInWaitingRoom.add(player);
		if(!deadPlayers.contains(player))
			deadPlayers.add(player);
		if(_party.getMemberCount() - reviedInWaitingRoom.size() < Config.RIFT_MIN_PARTY_SIZE)
		{
			for(final Player p : _party.getPartyMembers())
				if(!reviedInWaitingRoom.contains(p))
					teleportToWaitingRoom(p);
			createNewKillRiftTimer();
		}
	}

	public List<Player> getDeadMemberList()
	{
		return deadPlayers;
	}

	public List<Player> getRevivedAtWaitingRoom()
	{
		return reviedInWaitingRoom;
	}

	public void checkBossRoom(final int room)
	{
		isBossRoom = DimensionalRiftManager.getInstance().getRoom(_type, room).isBossRoom();
	}

	public Location getRoomCoord(final int room)
	{
		return DimensionalRiftManager.getInstance().getRoom(_type, room).getTeleportCoords();
	}

	public int getMaxJumps()
	{
		return Math.max(Math.min(Config.RIFT_MAX_JUMPS, 8), 1);
	}

	private void clearSpawn()
	{
		if(_spawns.isEmpty())
			return;
		for(final Spawn s : _spawns)
			s.despawnAll();
		_spawns.clear();
	}
}
