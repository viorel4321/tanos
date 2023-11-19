package l2s.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.utils.Location;

public abstract class SiegeEvent<R extends Residence, S extends SiegeClanObject> extends GlobalEvent
{
	public static final String OWNER = "owner";
	public static final String OLD_OWNER = "old_owner";
	public static final String ATTACKERS = "attackers";
	public static final String DEFENDERS = "defenders";
	public static final String SPECTATORS = "spectators";
	public static final String SIEGE_ZONES = "siege_zones";
	public static final String FLAG_ZONES = "flag_zones";
	public static final String DAY_OF_WEEK = "day_of_week";
	public static final String HOUR_OF_DAY = "hour_of_day";
	public static final String REGISTRATION = "registration";
	public static final String DOORS = "doors";
	public static final int PROGRESS_STATE = 1;
	public static final int REGISTRATION_STATE = 2;
	protected R _residence;
	private int _state;
	protected int _dayOfWeek;
	protected int _hourOfDay;
	protected Clan _oldOwner;
	protected OnDeathListener _doorDeathListener = new DoorDeathListener();
	protected List<Integer> _siegeSummons = new CopyOnWriteArrayList<Integer>();

	public SiegeEvent(final MultiValueSet<String> set)
	{
		super(set);
		_dayOfWeek = set.getInteger("day_of_week", 2);
		_hourOfDay = set.getInteger("hour_of_day", 15);
	}

	@Override
	public void startEvent()
	{
		addState(1);
		super.startEvent();
	}

	@Override
	public final void stopEvent()
	{
		stopEvent(false);
	}

	public void stopEvent(final boolean step)
	{
		removeState(1);
		despawnSiegeSummons();
		reCalcNextTime(false);
		super.stopEvent();
	}

	public void processStep(final Clan clan)
	{}

	@Override
	public void reCalcNextTime(final boolean onInit)
	{
		clearActions();
		final Calendar startSiegeDate = getResidence().getSiegeDate();
		if(onInit)
		{
			if(startSiegeDate.getTimeInMillis() <= System.currentTimeMillis())
			{
				startSiegeDate.set(7, _dayOfWeek);
				startSiegeDate.set(11, _hourOfDay);
				validateSiegeDate(startSiegeDate, 2);
				getResidence().setJdbcState(JdbcEntityState.UPDATED);
			}
		}
		else
		{
			startSiegeDate.add(3, 2);
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
		}
		registerActions();
		getResidence().update();
	}

	protected void validateSiegeDate(final Calendar calendar, final int add)
	{
		calendar.set(12, 0);
		calendar.set(13, 0);
		calendar.set(14, 0);
		while(calendar.getTimeInMillis() < System.currentTimeMillis())
			calendar.add(3, add);
	}

	@Override
	protected long startTimeMillis()
	{
		return getResidence().getSiegeDate().getTimeInMillis();
	}

	@Override
	public void teleportPlayers(final String t)
	{
		List<Player> players = new ArrayList<Player>();
		final Clan ownerClan = getResidence().getOwner();
		if(t.equalsIgnoreCase("owner"))
		{
			if(ownerClan != null)
				for(final Player player : getPlayersInZone())
					if(player.getClan() == ownerClan)
						players.add(player);
		}
		else if(t.equalsIgnoreCase("attackers"))
			for(final Player player : getPlayersInZone())
			{
				final S siegeClan = getSiegeClan("attackers", player.getClan());
				if(siegeClan != null && siegeClan.isParticle(player))
					players.add(player);
			}
		else if(t.equalsIgnoreCase("defenders"))
			for(final Player player : getPlayersInZone())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan)
					continue;
				final S siegeClan = getSiegeClan("defenders", player.getClan());
				if(siegeClan == null || !siegeClan.isParticle(player))
					continue;
				players.add(player);
			}
		else if(t.equalsIgnoreCase("spectators"))
			for(final Player player : getPlayersInZone())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan)
					continue;
				if(player.getClan() != null && (getSiegeClan("attackers", player.getClan()) != null || getSiegeClan("defenders", player.getClan()) != null))
					continue;
				players.add(player);
			}
		else
			players = getPlayersInZone();
		for(final Player player : players)
		{
			Location loc;
			if(t.equalsIgnoreCase("owner") || t.equalsIgnoreCase("defenders"))
				loc = getResidence().getOwnerRestartPoint();
			else
				loc = getResidence().getNotOwnerRestartPoint(player);
			player.teleToLocation(loc);
		}
	}

	public List<Player> getPlayersInZone()
	{
		final List<ZoneObject> zones = getObjects("siege_zones");
		final List<Player> result = new LazyArrayList<Player>();
		for(final ZoneObject zone : zones)
			result.addAll(zone.getInsidePlayers());
		return result;
	}

	public void broadcastInZone(final L2GameServerPacket... packet)
	{
		for(final Player player : getPlayersInZone())
			player.sendPacket(packet);
	}

	public void broadcastInZone(final IBroadcastPacket... packet)
	{
		for(final Player player : getPlayersInZone())
			player.sendPacket(packet);
	}

	public boolean checkIfInZone(final Creature character)
	{
		final List<ZoneObject> zones = getObjects("siege_zones");
		for(final ZoneObject zone : zones)
			if(zone.checkIfInZone(character))
				return true;
		return false;
	}

	public void broadcastInZone2(final IBroadcastPacket... packet)
	{
		for(final Player player : getResidence().getZone().getInsidePlayers())
			player.sendPacket(packet);
	}

	public void broadcastInZone2(final L2GameServerPacket... packet)
	{
		for(final Player player : getResidence().getZone().getInsidePlayers())
			player.sendPacket(packet);
	}

	public void loadSiegeClans()
	{
		addObjects("attackers", SiegeClanDAO.getInstance().load(getResidence(), "attackers"));
		addObjects("defenders", SiegeClanDAO.getInstance().load(getResidence(), "defenders"));
	}

	@SuppressWarnings("unchecked")
	public S newSiegeClan(String type, int clanId, long param, long date)
	{
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : (S) new SiegeClanObject(type, clan, param, date);
	}

	public void updateParticles(final boolean start, final String... arg)
	{
		for(final String a : arg)
		{
			final List<SiegeClanObject> siegeClans = getObjects(a);
			for(final SiegeClanObject s : siegeClans)
				s.setEvent(start, this);
		}
	}

	public S getSiegeClan(final String name, final Clan clan)
	{
		if(clan == null)
			return null;
		return getSiegeClan(name, clan.getClanId());
	}

	@SuppressWarnings("unchecked")
	public S getSiegeClan(String name, int objectId)
	{
		List<SiegeClanObject> siegeClanList = getObjects(name);
		if(siegeClanList.isEmpty())
			return null;
		for(int i = 0; i < siegeClanList.size(); i++)
		{
			SiegeClanObject siegeClan = siegeClanList.get(i);
			if(siegeClan.getObjectId() == objectId)
				return (S) siegeClan;
		}
		return null;
	}

	public void broadcastTo(final IBroadcastPacket packet, final String... types)
	{
		for(final String type : types)
		{
			final List<SiegeClanObject> siegeClans = getObjects(type);
			for(final SiegeClanObject siegeClan : siegeClans)
				siegeClan.broadcast(new IBroadcastPacket[] { packet });
		}
	}

	public void broadcastTo(final L2GameServerPacket packet, final String... types)
	{
		for(final String type : types)
		{
			final List<SiegeClanObject> siegeClans = getObjects(type);
			for(final SiegeClanObject siegeClan : siegeClans)
				siegeClan.broadcast(packet);
		}
	}

	@Override
	public void initEvent()
	{
		_residence = ResidenceHolder.getInstance().getResidence(getId());
		loadSiegeClans();
		clearActions();
		super.initEvent();
	}

	@Override
	public boolean ifVar(final String name)
	{
		if(name.equals("owner"))
			return getResidence().getOwner() != null;
		return name.equals("old_owner") && _oldOwner != null;
	}

	@Override
	public boolean isParticle(final Player player)
	{
		return isInProgress() && player.getClan() != null && (getSiegeClan("attackers", player.getClan()) != null || getSiegeClan("defenders", player.getClan()) != null);
	}

	@Override
	public void checkRestartLocs(final Player player, final Map<RestartType, Boolean> r)
	{
		if(getObjects("flag_zones").isEmpty())
			return;
		final S clan = getSiegeClan("attackers", player.getClan());
		if(clan != null && clan.getFlag() != null)
			r.put(RestartType.TO_FLAG, Boolean.TRUE);
	}

	@Override
	public Location getRestartLoc(final Player player, final RestartType type)
	{
		final S attackerClan = getSiegeClan("attackers", player.getClan());
		Location loc = null;
		switch(type)
		{
			case TO_FLAG:
			{
				if(!getObjects("flag_zones").isEmpty() && attackerClan != null && attackerClan.getFlag() != null)
				{
					loc = Location.findPointToStay(attackerClan.getFlag().getLoc(), 50, 75, player.getGeoIndex());
					break;
				}
				player.sendPacket(Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				break;
			}
		}
		return loc;
	}

	@Override
	public int getRelation(final Player thisPlayer, final Player targetPlayer, int result)
	{
		final Clan clan1 = thisPlayer.getClan();
		final Clan clan2 = targetPlayer.getClan();
		if(!Config.VISIBLE_SIEGE_ICONS && (clan1 == null || clan2 == null))
			return result;
		final SiegeEvent<?, ?> siegeEvent2 = targetPlayer.getEvent(SiegeEvent.class);
		result |= 0x200;
		final SiegeClanObject siegeClan1 = getSiegeClan("attackers", clan1);
		final SiegeClanObject siegeClan2 = getSiegeClan("attackers", clan2);
		if(siegeClan1 == null && siegeClan2 == null || siegeClan1 != null && siegeClan2 != null && isAttackersInAlly())
			result |= 0x800;
		else
			result |= 0x1000;
		if(siegeClan1 != null)
			result |= 0x400;
		return result;
	}

	@Override
	public int getUserRelation(final Player thisPlayer, int oldRelation)
	{
		final SiegeClanObject siegeClan = getSiegeClan("attackers", thisPlayer.getClan());
		if(siegeClan != null)
			oldRelation |= 0x180;
		else
			oldRelation |= 0x80;
		return oldRelation;
	}

	@Override
	public SystemMessage checkForAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force)
	{
		if(!checkIfInZone(target) || !checkIfInZone(attacker))
			return null;
		final SiegeEvent<?, ?> siegeEvent = target.getEvent(SiegeEvent.class);
		if(this != siegeEvent)
			return null;
		final Player player = target.getPlayer();
		if(player == null)
			return null;
		final SiegeClanObject siegeClan1 = getSiegeClan("attackers", player.getClan());
		if(siegeClan1 == null && attacker.isSiegeGuard())
			return Msg.INCORRECT_TARGET;
		final Player playerAttacker = attacker.getPlayer();
		if(playerAttacker == null)
			return Msg.INCORRECT_TARGET;
		final SiegeClanObject siegeClan2 = getSiegeClan("attackers", playerAttacker.getClan());
		if(siegeClan1 != null && siegeClan2 != null && isAttackersInAlly())
			return new SystemMessage("Force attack is impossible against a temporary allied member during a siege.");
		if(siegeClan1 == null && siegeClan2 == null)
			return Msg.INCORRECT_TARGET;
		return null;
	}

	@Override
	public boolean isInProgress()
	{
		return hasState(1);
	}

	@Override
	public void action(final String name, final boolean start)
	{
		if(name.equalsIgnoreCase("registration"))
		{
			if(start)
				addState(2);
			else
				removeState(2);
		}
		else
			super.action(name, start);
	}

	public boolean isAttackersInAlly()
	{
		return false;
	}

	@Override
	public List<Player> broadcastPlayers(final int range)
	{
		return itemObtainPlayers();
	}

	@Override
	public List<Player> itemObtainPlayers()
	{
		final List<Player> playersInZone = getPlayersInZone();
		final List<Player> list = new LazyArrayList<Player>(playersInZone.size());
		for(final Player player : getPlayersInZone())
			if(player.getEvent(getClass()) == this)
				list.add(player);
		return list;
	}

	public Location getEnterLoc(final Player player)
	{
		final S siegeClan = getSiegeClan("attackers", player.getClan());
		if(siegeClan == null)
			return getResidence().getOwnerRestartPoint();
		if(siegeClan.getFlag() != null)
			return Location.findAroundPosition(siegeClan.getFlag().getLoc(), 50, 75, player.getGeoIndex());
		return getResidence().getNotOwnerRestartPoint(player);
	}

	public R getResidence()
	{
		return _residence;
	}

	public void addState(final int b)
	{
		_state |= b;
	}

	public void removeState(final int b)
	{
		_state &= ~b;
	}

	public boolean hasState(final int val)
	{
		return (_state & val) == val;
	}

	public boolean isRegistrationOver()
	{
		return !hasState(2);
	}

	public void addSiegeSummon(SummonInstance summon)
	{
		_siegeSummons.add(summon.getObjectId());
	}

	public boolean containsSiegeSummon(SummonInstance cha)
	{
		return _siegeSummons.contains(cha.getObjectId());
	}

	public void despawnSiegeSummons()
	{
		for(Integer id : _siegeSummons)
		{
			GameObject summon = GameObjectsStorage.findObject(id);
			if(summon != null && summon.isSummon())
				((SummonInstance) summon).unSummon();
		}
		_siegeSummons.clear();
	}

	public class DoorDeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(final Creature actor, final Creature killer)
		{
			if(!isInProgress())
				return;

			DoorInstance door = (DoorInstance)actor;
			if(door.getDoorType() == DoorTemplate.DoorType.WALL)
				return;

			broadcastTo(Msg.THE_CASTLE_GATE_HAS_BEEN_BROKEN_DOWN, SiegeEvent.ATTACKERS, SiegeEvent.DEFENDERS);
		}
	}
}
