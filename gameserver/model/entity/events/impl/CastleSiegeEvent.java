package l2s.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.TreeIntSet;

import gnu.trove.map.hash.TIntIntHashMap;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.CastleDamageZoneDAO;
import l2s.gameserver.dao.CastleDoorUpgradeDAO;
import l2s.gameserver.dao.CastleHiredGuardDAO;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.SiegeToggleNpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.MerchantGuard;
import l2s.gameserver.utils.Location;

public class CastleSiegeEvent extends SiegeEvent<Castle, SiegeClanObject>
{
	public static final int MAX_SIEGE_CLANS = 20;
	public static final long DAY_IN_MILISECONDS = 86400000L;
	public static final String DEFENDERS_WAITING = "defenders_waiting";
	public static final String DEFENDERS_REFUSED = "defenders_refused";
	public static final String CONTROL_TOWERS = "control_towers";
	public static final String FLAME_TOWERS = "flame_towers";
	public static final String BOUGHT_ZONES = "bought_zones";
	public static final String GUARDS = "guards";
	public static final String HIRED_GUARDS = "hired_guards";
	private IntSet _nextSiegeTimes;
	private Future<?> _nextSiegeDateSetTask;
	private boolean _firstStep;
	private Calendar siegeLast;
	private TIntIntHashMap _engrave;

	public CastleSiegeEvent(final MultiValueSet<String> set)
	{
		super(set);
		_nextSiegeTimes = Containers.EMPTY_INT_SET;
		_nextSiegeDateSetTask = null;
		_firstStep = false;
		_engrave = new TIntIntHashMap();
	}

	@Override
	public void initEvent()
	{
		super.initEvent();
		final List<DoorObject> doorObjects = this.getObjects("doors");
		addObjects("bought_zones", CastleDamageZoneDAO.getInstance().load(getResidence()));
		for(final DoorObject doorObject : doorObjects)
		{
			doorObject.setUpgradeValue(this, CastleDoorUpgradeDAO.getInstance().load(doorObject.getUId()));
			doorObject.getDoor().addListener(_doorDeathListener);
		}
	}

	public synchronized void engrave(final Player player, final int id)
	{
		if(!getResidence().getArtefacts().contains(id))
			return;
		_engrave.put(id, player.getClan().getClanId());
		int c = 0;
		if(_engrave.size() == 1)
			++c;
		else
			for(final int art : getResidence().getArtefacts())
				if(_engrave.containsKey(art) && _engrave.get(art) == player.getClan().getClanId())
					++c;
		if(c < getResidence().getArtefacts().size())
		{
			player.sendMessage(player.isLangRus() ? "\u041f\u0435\u0440\u0432\u044b\u0439 \u0430\u043b\u0442\u0430\u0440\u044c \u043f\u043e\u0434\u0447\u0438\u043d\u0438\u043b\u0441\u044f \u0412\u0430\u043c. \u041d\u0430\u043b\u043e\u0436\u0438\u0442\u0435 \u043f\u0435\u0447\u0430\u0442\u044c \u043d\u0430 \u0432\u0442\u043e\u0440\u043e\u0439." : "First altar obeyed you. Place the seal on the second.");
			this.broadcastTo(new SystemMessage("\u041a\u043b\u0430\u043d\u0443 " + player.getClan().getName() + " \u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u043b\u043e\u0436\u0438\u0442\u044c \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u044e\u044e \u043f\u0435\u0447\u0430\u0442\u044c!"), "attackers", "defenders");
		}
		else
		{
			_engrave.clear();
			this.broadcastTo(new SystemMessage(285).addString(player.getClan().getName()), "attackers", "defenders");
			processStep(player.getClan());
		}
	}

	public synchronized boolean isActiveArtefact(final int clanId, final int id)
	{
		return !_engrave.containsKey(id) || _engrave.get(id) != clanId;
	}

	@Override
	public void processStep(final Clan newOwnerClan)
	{
		final Clan oldOwnerClan = getResidence().getOwner();
		getResidence().changeOwner(newOwnerClan);
		if(oldOwnerClan != null)
		{
			final SiegeClanObject ownerSiegeClan = getSiegeClan("defenders", oldOwnerClan);
			removeObject("defenders", ownerSiegeClan);
			ownerSiegeClan.setType("attackers");
			addObject("attackers", ownerSiegeClan);
		}
		else
		{
			if(this.getObjects("attackers").size() == 1)
			{
				this.stopEvent();
				return;
			}
			final int allianceObjectId = newOwnerClan.getAllyId();
			if(allianceObjectId > 0)
			{
				final List<SiegeClanObject> attackers = this.getObjects("attackers");
				boolean sameAlliance = true;
				for(final SiegeClanObject sc : attackers)
					if(sc != null && sc.getClan().getAllyId() != allianceObjectId)
						sameAlliance = false;
				if(sameAlliance)
				{
					this.stopEvent();
					return;
				}
			}
		}
		final SiegeClanObject newOwnerSiegeClan = getSiegeClan("attackers", newOwnerClan);
		newOwnerSiegeClan.deleteFlag();
		newOwnerSiegeClan.setType("defenders");
		removeObject("attackers", newOwnerSiegeClan);
		final List<SiegeClanObject> defenders = this.removeObjects("defenders");
		for(final SiegeClanObject siegeClan : defenders)
			siegeClan.setType("attackers");
		addObject("defenders", newOwnerSiegeClan);
		addObjects("attackers", defenders);
		updateParticles(true, "attackers", "defenders");
		teleportPlayers("attackers");
		teleportPlayers("spectators");
		if(!_firstStep)
		{
			_firstStep = true;
			this.broadcastTo(Msg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED, "attackers", "defenders");
			if(_oldOwner != null)
			{
				spawnAction("hired_guards", false);
				damageZoneAction(false);
				this.removeObjects("hired_guards");
				this.removeObjects("bought_zones");
				CastleDamageZoneDAO.getInstance().delete(getResidence());
			}
			else
				spawnAction("guards", false);
			final List<DoorObject> doorObjects = this.getObjects("doors");
			for(final DoorObject doorObject : doorObjects)
			{
				doorObject.setWeak(true);
				doorObject.setUpgradeValue(this, 0);
				CastleDoorUpgradeDAO.getInstance().delete(doorObject.getUId());
			}
		}
		spawnAction("doors", true);
		despawnSiegeSummons();
	}

	@Override
	public void startEvent()
	{
		siegeLast = (Calendar) getResidence().getSiegeDate().clone();
		_oldOwner = getResidence().getOwner();
		if(_oldOwner != null)
		{
			addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0L));
			if(getResidence().getSpawnMerchantTickets().size() > 0)
			{
				for(final ItemInstance item : getResidence().getSpawnMerchantTickets())
				{
					final MerchantGuard guard = getResidence().getMerchantGuard(item.getItemId());
					addObject("hired_guards", new SpawnSimpleObject(guard.getNpcId(), item.getLoc()));
					item.deleteMe();
				}
				CastleHiredGuardDAO.getInstance().delete(getResidence());
				spawnAction("hired_guards", true);
			}
		}
		final List<SiegeClanObject> attackers = this.getObjects("attackers");
		if(attackers.isEmpty())
		{
			if(_oldOwner == null)
				this.broadcastToWorld(new SystemMessage(846).addString(getResidence().getName()));
			else
			{
				this.broadcastToWorld(new SystemMessage(295).addString(getResidence().getName()));
				getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
				getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
				getResidence().update();
			}
			reCalcNextTime(false);
			return;
		}
		SiegeClanDAO.getInstance().delete(getResidence());
		updateParticles(true, "attackers", "defenders");
		this.broadcastTo(Msg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT_IT_WILL_BE_DISSOLVED_WHEN_THE_CASTLE_LORD_IS_REPLACED, "attackers");
		this.broadcastTo(new SystemMessage(711).addString(getResidence().getName()), "attackers", "defenders");
		super.startEvent();
		if(_oldOwner == null)
			initControlTowers(0); // TODO: Use geoIndex
		else
			damageZoneAction(true);
	}

	@Override
	public void stopEvent(final boolean step)
	{
		final List<DoorObject> doorObjects = this.getObjects("doors");
		for(final DoorObject doorObject : doorObjects)
		{
			doorObject.setWeak(false);
			doorObject.res();
		}
		damageZoneAction(false);
		updateParticles(false, "attackers", "defenders");
		final List<SiegeClanObject> attackers = this.removeObjects("attackers");
		for(final SiegeClanObject siegeClan : attackers)
			siegeClan.deleteFlag();
		this.broadcastToWorld(new SystemMessage(843).addString(getResidence().getName()));
		this.removeObjects("defenders");
		this.removeObjects("defenders_waiting");
		this.removeObjects("defenders_refused");
		final Clan ownerClan = getResidence().getOwner();
		if(ownerClan != null)
		{
			if(_oldOwner == ownerClan)
			{
				getResidence().setRewardCount(getResidence().getRewardCount() + 1);
				ownerClan.broadcastToOnlineMembers(new SystemMessage(1773).addNumber(Integer.valueOf(ownerClan.incReputation(getResidence().getReputationOwner(), false, toString()))));
			}
			else
			{
				this.broadcastToWorld(new SystemMessage(291).addString(ownerClan.getName()).addString(getResidence().getName()));
				ownerClan.broadcastToOnlineMembers(new SystemMessage(1773).addNumber(Integer.valueOf(ownerClan.incReputation(getResidence().getReputation(), false, toString()))));
				if(_oldOwner != null && getResidence().getReputationLoser() < 0)
				{
					_oldOwner.incReputation(getResidence().getReputationLoser(), false, toString());
					_oldOwner.broadcastToOnlineMembers(new SystemMessage(1784).addNumber(Integer.valueOf(Math.abs(getResidence().getReputationLoser()))));
				}
				for(final ClanMember member : ownerClan.getMembers())
				{
					final Player player = member.getPlayer();
					if(player != null)
					{
						player.sendPacket(new PlaySound("Siege_Victory"));
						if(player.isOnline() && player.isNoble())
							Hero.getInstance().addHeroDiary(player.getObjectId(), 3, getResidence().getId());
					}
				}
			}
			getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
			getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
			giveReward(ownerClan, getResidence().getId());
		}
		else
		{
			this.broadcastToWorld(new SystemMessage(856).addString(getResidence().getName()));
			getResidence().getOwnDate().setTimeInMillis(0L);
			getResidence().getLastSiegeDate().setTimeInMillis(0L);
		}
		despawnSiegeSummons();
		if(_oldOwner != null)
		{
			spawnAction("hired_guards", false);
			this.removeObjects("hired_guards");
		}
		super.stopEvent(step);
	}

	private static void giveReward(final Clan clan, final int id)
	{
		switch(id)
		{
			case 1:
			{
				if(Config.GLUDIO_REWARD.length > 1)
				{
					for(int i = 0; i < Config.GLUDIO_REWARD.length; i += 2)
						if(Config.GLUDIO_REWARD[i] == -200)
							clan.incReputation(Config.GLUDIO_REWARD[i + 1], false, "GludioReward");
						else
							clan.getWarehouse().addItem(Config.GLUDIO_REWARD[i], Config.GLUDIO_REWARD[i + 1]);
					break;
				}
				break;
			}
			case 2:
			{
				if(Config.DION_REWARD.length > 1)
				{
					for(int i = 0; i < Config.DION_REWARD.length; i += 2)
						if(Config.DION_REWARD[i] == -200)
							clan.incReputation(Config.DION_REWARD[i + 1], false, "DionReward");
						else
							clan.getWarehouse().addItem(Config.DION_REWARD[i], Config.DION_REWARD[i + 1]);
					break;
				}
				break;
			}
			case 3:
			{
				if(Config.GIRAN_REWARD.length > 1)
				{
					for(int i = 0; i < Config.GIRAN_REWARD.length; i += 2)
						if(Config.GIRAN_REWARD[i] == -200)
							clan.incReputation(Config.GIRAN_REWARD[i + 1], false, "GiranReward");
						else
							clan.getWarehouse().addItem(Config.GIRAN_REWARD[i], Config.GIRAN_REWARD[i + 1]);
					break;
				}
				break;
			}
			case 4:
			{
				if(Config.OREN_REWARD.length > 1)
				{
					for(int i = 0; i < Config.OREN_REWARD.length; i += 2)
						if(Config.OREN_REWARD[i] == -200)
							clan.incReputation(Config.OREN_REWARD[i + 1], false, "OrenReward");
						else
							clan.getWarehouse().addItem(Config.OREN_REWARD[i], Config.OREN_REWARD[i + 1]);
					break;
				}
				break;
			}
			case 5:
			{
				if(Config.ADEN_REWARD.length > 1)
				{
					for(int i = 0; i < Config.ADEN_REWARD.length; i += 2)
						if(Config.ADEN_REWARD[i] == -200)
							clan.incReputation(Config.ADEN_REWARD[i + 1], false, "AdenReward");
						else
							clan.getWarehouse().addItem(Config.ADEN_REWARD[i], Config.ADEN_REWARD[i + 1]);
					break;
				}
				break;
			}
			case 6:
			{
				if(Config.INNADRIL_REWARD.length > 1)
				{
					for(int i = 0; i < Config.INNADRIL_REWARD.length; i += 2)
						if(Config.INNADRIL_REWARD[i] == -200)
							clan.incReputation(Config.INNADRIL_REWARD[i + 1], false, "InnadrilReward");
						else
							clan.getWarehouse().addItem(Config.INNADRIL_REWARD[i], Config.INNADRIL_REWARD[i + 1]);
					break;
				}
				break;
			}
			case 7:
			{
				if(Config.GODDARD_REWARD.length > 1)
				{
					for(int i = 0; i < Config.GODDARD_REWARD.length; i += 2)
						if(Config.GODDARD_REWARD[i] == -200)
							clan.incReputation(Config.GODDARD_REWARD[i + 1], false, "GoddardReward");
						else
							clan.getWarehouse().addItem(Config.GODDARD_REWARD[i], Config.GODDARD_REWARD[i + 1]);
					break;
				}
				break;
			}
			case 8:
			{
				if(Config.RUNE_REWARD.length > 1)
				{
					for(int i = 0; i < Config.RUNE_REWARD.length; i += 2)
						if(Config.RUNE_REWARD[i] == -200)
							clan.incReputation(Config.RUNE_REWARD[i + 1], false, "RuneReward");
						else
							clan.getWarehouse().addItem(Config.RUNE_REWARD[i], Config.RUNE_REWARD[i + 1]);
					break;
				}
				break;
			}
			case 9:
			{
				if(Config.SCHUTTGART_REWARD.length > 1)
				{
					for(int i = 0; i < Config.SCHUTTGART_REWARD.length; i += 2)
						if(Config.SCHUTTGART_REWARD[i] == -200)
							clan.incReputation(Config.SCHUTTGART_REWARD[i + 1], false, "SchuttgartReward");
						else
							clan.getWarehouse().addItem(Config.SCHUTTGART_REWARD[i], Config.SCHUTTGART_REWARD[i + 1]);
					break;
				}
				break;
			}
		}
	}

	@Override
	public void reCalcNextTime(final boolean onInit)
	{
		clearActions();
		final long currentTimeMillis = System.currentTimeMillis();
		final Calendar startSiegeDate = getResidence().getSiegeDate();
		final Calendar ownSiegeDate = getResidence().getOwnDate();
		if(onInit)
		{
			if(startSiegeDate.getTimeInMillis() > currentTimeMillis)
			{
				addState(2);
				registerActions();
			}
			else if(startSiegeDate.getTimeInMillis() == 0L)
			{
				if(currentTimeMillis - ownSiegeDate.getTimeInMillis() > 86400000L)
					setNextSiegeTimeNow(getResidence().getOwnDate().getTimeInMillis() != 0L);
				else
					generateNextSiegeDates();
			}
			else if(startSiegeDate.getTimeInMillis() <= currentTimeMillis)
				setNextSiegeTimeNow(false);
		}
		else if(getResidence().getOwner() != null)
		{
			if(Config.CASTLE_SELECT_HOURS.length > 0)
			{
				getResidence().getSiegeDate().setTimeInMillis(0L);
				getResidence().setJdbcState(JdbcEntityState.UPDATED);
				getResidence().update();
				generateNextSiegeDates();
			}
			else
				setNextSiegeTimeNow(true);
		}
		else
			setNextSiegeTimeNow(false);
	}

	@Override
	public void loadSiegeClans()
	{
		super.loadSiegeClans();
		addObjects("defenders_waiting", SiegeClanDAO.getInstance().load(getResidence(), "defenders_waiting"));
		addObjects("defenders_refused", SiegeClanDAO.getInstance().load(getResidence(), "defenders_refused"));
	}

	@Override
	public void removeState(final int val)
	{
		super.removeState(val);
		if(val == 2)
			this.broadcastToWorld(new SystemMessage(293).addString(getResidence().getName()));
	}

	@Override
	public void announce(final int val)
	{
		final int min = val / 60;
		final int hour = min / 60;
		SystemMessage msg;
		if(hour > 0)
			msg = new SystemMessage(358).addNumber(Integer.valueOf(hour));
		else if(min > 0)
			msg = new SystemMessage(359).addNumber(Integer.valueOf(min));
		else
			msg = new SystemMessage(360).addNumber(Integer.valueOf(val));
		this.broadcastTo(msg, "attackers", "defenders");
	}

	private void initControlTowers(int geoIndex)
	{
		final List<SpawnExObject> objects = this.getObjects("guards");
		final List<Spawner> spawns = new ArrayList<Spawner>();
		for(final SpawnExObject o : objects)
			spawns.addAll(o.getSpawns());
		final List<SiegeToggleNpcObject> ct = this.getObjects("control_towers");
		for(final Spawner spawn : spawns)
		{
			final Location spawnLoc = spawn.getCurrentSpawnRange().getRandomLoc(geoIndex);
			SiegeToggleNpcInstance closestCt = null;
			double distanceClosest = 0.0;
			for(final SiegeToggleNpcObject c : ct)
			{
				final SiegeToggleNpcInstance npcTower = c.getToggleNpc();
				final double distance = npcTower.getDistance(spawnLoc);
				if(closestCt == null || distance < distanceClosest)
				{
					closestCt = npcTower;
					distanceClosest = distance;
				}
				closestCt.register(spawn);
			}
		}
	}

	private void damageZoneAction(final boolean active)
	{
		zoneAction("bought_zones", active);
	}

	public void generateNextSiegeDates()
	{
		if(getResidence().getSiegeDate().getTimeInMillis() != 0L)
			return;
		final Calendar calendar = (Calendar) (siegeLast != null ? siegeLast.clone() : Calendar.getInstance());
		calendar.set(7, _dayOfWeek);
		calendar.set(11, _hourOfDay);
		validateSiegeDate(calendar, Config.CASTLE_SIEGE_WEEKS);
		_nextSiegeTimes = new TreeIntSet();
		for(final int h : Config.CASTLE_SELECT_HOURS)
		{
			calendar.set(11, h);
			_nextSiegeTimes.add((int) (calendar.getTimeInMillis() / 1000L));
		}
		final long diff = getResidence().getOwnDate().getTimeInMillis() + 86400000L - System.currentTimeMillis();
		_nextSiegeDateSetTask = ThreadPoolManager.getInstance().schedule(new NextSiegeDateSet(), diff);
	}

	public void setNextSiegeByOwner(final int id)
	{
		if(!_nextSiegeTimes.contains(id) || _nextSiegeDateSetTask == null)
			return;
		_nextSiegeTimes = Containers.EMPTY_INT_SET;
		_nextSiegeDateSetTask.cancel(false);
		_nextSiegeDateSetTask = null;
		setNextSiegeTime(id * 1000L);
	}

	private void setNextSiegeTimeNow(final boolean own)
	{
		final Calendar calendar = (Calendar) (own ? getResidence().getSiegeDate().clone() : siegeLast != null ? siegeLast.clone() : Calendar.getInstance());
		calendar.set(7, _dayOfWeek);
		calendar.set(11, _hourOfDay);
		validateSiegeDate(calendar, Config.CASTLE_SIEGE_WEEKS);
		if(Config.CASTLE_GENERATE_TIME_ALTERNATIVE)
		{
			if(ArrayUtils.contains(Config.CASTLE_HIGH_LIST, getId()))
				setNextSiegeTime(calendar.getTimeInMillis() + Config.CASTLE_GENERATE_TIME_HIGH);
			else
				setNextSiegeTime(calendar.getTimeInMillis() + Config.CASTLE_GENERATE_TIME_LOW);
		}
		else
			setNextSiegeTime(calendar.getTimeInMillis());
	}

	private void setNextSiegeTime(final long g)
	{
		this.broadcastToWorld(new SystemMessage(292).addString(getResidence().getName()));
		clearActions();
		getResidence().getSiegeDate().setTimeInMillis(g);
		getResidence().setJdbcState(JdbcEntityState.UPDATED);
		getResidence().update();
		registerActions();
		addState(2);
	}

	@Override
	public boolean isAttackersInAlly()
	{
		return !_firstStep && Config.ATTACKERS_ALLY_FIRST_STEP_SIEGE;
	}

	public int[] getNextSiegeTimes()
	{
		return _nextSiegeTimes.toArray();
	}

	@Override
	public boolean canResurrect(final Player resurrectPlayer, final Creature target, final boolean force)
	{
		final Player targetPlayer = target.getPlayer();
		if(!resurrectPlayer.isOnSiegeField() && !targetPlayer.isOnSiegeField())
			return true;
		if(!targetPlayer.isOnSiegeField())
			return true;
		if(Config.NO_RES_SIEGE)
			return false;
		if(resurrectPlayer.getClan() == null)
			return false;
		if(!resurrectPlayer.isInSiege())
			return false;
		final CastleSiegeEvent siegeEvent = target.getEvent(CastleSiegeEvent.class);
		final CastleSiegeEvent siegeEventatt = resurrectPlayer.getEvent(CastleSiegeEvent.class);
		if(siegeEvent == null || siegeEventatt == null)
		{
			resurrectPlayer.sendPacket(Msg.INCORRECT_TARGET);
			return false;
		}
		if(siegeEvent != this || siegeEventatt != this)
		{
			if(force)
				targetPlayer.sendPacket(Msg.IT_IS_IMPOSSIBLE_TO_BE_RESSURECTED_IN_BATTLEFIELDS_WHERE_SIEGE_WARS_ARE_IN_PROCESS);
			resurrectPlayer.sendPacket(force ? Msg.IT_IS_IMPOSSIBLE_TO_BE_RESSURECTED_IN_BATTLEFIELDS_WHERE_SIEGE_WARS_ARE_IN_PROCESS : Msg.INCORRECT_TARGET);
			return false;
		}
		if(resurrectPlayer.getSiegeFieldId() != getId())
		{
			resurrectPlayer.sendMessage("You are not in your siege area.");
			return false;
		}
		SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("attackers", targetPlayer.getClan());
		if(targetSiegeClan == null)
			targetSiegeClan = siegeEvent.getSiegeClan("defenders", targetPlayer.getClan());
		if(targetSiegeClan == null)
			return false;
		if(targetSiegeClan.getType().equals("attackers"))
		{
			if(resurrectPlayer.getClan() != targetSiegeClan.getClan() && resurrectPlayer.getAlliance() != null && resurrectPlayer.getAlliance() != targetPlayer.getAlliance())
			{
				if(force)
					targetPlayer.sendPacket(Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				resurrectPlayer.sendPacket(force ? Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : Msg.INCORRECT_TARGET);
				return false;
			}
			if(targetSiegeClan.getFlag() == null)
			{
				if(force)
					targetPlayer.sendPacket(Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				resurrectPlayer.sendPacket(force ? Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : Msg.INCORRECT_TARGET);
				return false;
			}
			return true;
		}
		else
		{
			if(targetSiegeClan.getType().equals("defenders"))
			{
				final List<SiegeToggleNpcObject> towers = this.getObjects("control_towers");
				final boolean canRes = true;
				final Iterator<SiegeToggleNpcObject> iterator = towers.iterator();
				if(iterator.hasNext())
				{
					final SiegeToggleNpcObject t = iterator.next();
					if(resurrectPlayer.getClan() != targetSiegeClan.getClan() && resurrectPlayer.getAlliance() != null && resurrectPlayer.getAlliance() != targetPlayer.getAlliance())
					{
						if(force)
							targetPlayer.sendPacket(Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
						resurrectPlayer.sendPacket(force ? Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : Msg.INCORRECT_TARGET);
						return false;
					}
					if(!t.isAlive())
					{
						if(force)
							targetPlayer.sendPacket(Msg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE);
						resurrectPlayer.sendPacket(force ? Msg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE : Msg.INCORRECT_TARGET);
						return false;
					}
					return true;
				}
			}
			if(force)
				return true;
			resurrectPlayer.sendPacket(Msg.INCORRECT_TARGET);
			return false;
		}
	}

	@Override
	public Location getRestartLoc(final Player player, final RestartType type)
	{
		Location loc = null;
		final SiegeClanObject attackerClan = getSiegeClan("attackers", player.getClan());
		switch(type)
		{
			case TO_VILLAGE:
			{
				if(Config.ALLOW_SEVEN_SIGNS && SevenSigns.getInstance().getSealOwner(3) == 2)
				{
					loc = _residence.getNotOwnerRestartPoint(player);
					break;
				}
				break;
			}
			case TO_FLAG:
			{
				if(attackerClan != null && attackerClan.getFlag() != null)
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

	private class NextSiegeDateSet implements Runnable
	{
		@Override
		public void run()
		{
			setNextSiegeTimeNow(false);
		}
	}
}
