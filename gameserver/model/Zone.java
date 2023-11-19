package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.engine.DefaultListenerEngine;
import l2s.gameserver.listener.engine.ListenerEngine;
import l2s.gameserver.listener.events.Zone.ZoneEnterLeaveEvent;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.EventTrigger;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.funcs.FuncAdd;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public class Zone
{
	public enum ZoneType
	{
		Town,
		OlympiadStadia,
		Siege,
		RESIDENCE,
		HEADQUARTER,
		FISHING,
		water,
		battle_zone,
		damage,
		instant_skill,
		mother_tree,
		peace_zone,
		poison,
		ssq_zone,
		swamp,
		no_escape,
		no_landing,
		no_restart,
		no_spawn,
		dummy,
		offshore,
		unblock_actions,
		epic,
		no_summon;
	}

	private static class ZoneSkillTimer implements Runnable
	{
		private boolean canceled;
		private Creature _target;
		private Skill _skill;
		private int _skillProb;
		private long _unitTick;

		public ZoneSkillTimer(final Creature target, final Skill skill, final int skillProb, final long unitTick)
		{
			canceled = false;
			_target = target;
			_skill = skill;
			_skillProb = skillProb;
			_unitTick = unitTick;
		}

		@Override
		public void run()
		{
			if(canceled)
				return;
			if(Rnd.chance(_skillProb) && !_target.isDead() && !_target.isEffectImmune() && !_target.isInvul() && (!_target.isBuffImmune() || _skill.isOffensive()) && (!_target.isDebuffImmune() || !_skill.isOffensive()))
				OlympiadGame.giveBuff(_target, _skill, 0);
			ThreadPoolManager.getInstance().schedule(this, _unitTick);
		}

		public void setCanceled(final boolean val)
		{
			canceled = val;
		}
	}

	public enum ZoneTarget
	{
		pc,
		npc,
		only_pc;
	}

	private class DamageTask implements Runnable
	{
		@Override
		public void run()
		{
			if(damageTask != this || !isActive())
				return;
			synchronized (_objects)
			{
				for(final GameObject object : _objects)
				{
					if(_target != null && !checkTarget(object))
						continue;
					if(_target == null && !object.isPlayable())
						continue;
					final Creature target = (Creature) object;
					final int hp = getDamageOnHP();
					final int mp = getDamageOnMP();
					final int message = getMessageNumber();
					if(hp > 0)
					{
						target.reduceCurrentHp(hp, null, null, 0, false, true, true, true, false, false, false, true);
						if(message > 0)
							target.sendPacket(new SystemMessage(message).addNumber(Integer.valueOf(hp)));
					}
					if(mp <= 0)
						continue;
					target.reduceCurrentMp(mp, null);
					if(message <= 0)
						continue;
					target.sendPacket(new SystemMessage(message).addNumber(Integer.valueOf(mp)));
				}
			}
			ThreadPoolManager.getInstance().schedule(this, 1000L);
		}

		public void start()
		{
			damageTask = this;
			if(_initialDelay == 0L)
			{
				run();
				return;
			}
			ThreadPoolManager.getInstance().schedule(this, _initialDelay);
		}
	}

	public static final String BLOCKED_ACTION_PRIVATE_STORE = "private store";
	public static final String BLOCKED_ACTION_PRIVATE_WORKSHOP = "private workshop";
	public static final int ZONE_STATS_ORDER = 64;

	private static Logger _log = LoggerFactory.getLogger(Zone.class);

	private final int _id;
	ZoneType _type;
	private String _name;
	private Territory _loc;
	private List<Location> _restartPoints;
	private List<Location> _PKrestartPoints;
	private List<Location> _ADVrestartPoints;
	private int _entering_message_no;
	private int _leaving_message_no;
	private String _entering_message;
	private String _leaving_message;
	private boolean _active;
	Skill _skill;
	int _skillProb;
	long _unitTick;
	private long _initialDelay;
	private ZoneTarget _target;
	private int _index;
	private int _taxById;
	private long _restartTime;
	private String _blockedActions;
	private String _event;
	private int _eventId;
	private List<Integer> _restrictSkills;
	private List<Integer> _restrictEquip;
	private ListenerEngine<Zone> listenerEngine;
	private DamageTask damageTask;
	private final List<GameObject> _objects;
	private final HashMap<Creature, ZoneSkillTimer> _skillTimers;
	private MultiValueSet<String> _params;
	private int messageNumber;
	private int damageOnHP;
	private int damageOnMP;
	private int moveBonus;
	private int regenBonusHP;
	private int regenBonusMP;
	private String affectRace;
	private int _privateStoreCurrecy;

	public Zone(final int id)
	{
		_eventId = 0;
		_objects = new ArrayList<GameObject>(0);
		_skillTimers = new HashMap<Creature, ZoneSkillTimer>(0);
		_params = new MultiValueSet<String>();
		_id = id;
	}

	public final int getId()
	{
		return _id;
	}

	@Override
	public final String toString()
	{
		return "zone '" + _id + "'";
	}

	public ZoneType getType()
	{
		return _type;
	}

	public void setType(final ZoneType type)
	{
		_type = type;
	}

	public void setLoc(final Territory t)
	{
		_loc = t;
	}

	public void setRestartPoints(final List<Location> t)
	{
		_restartPoints = t;
	}

	public void setPKRestartPoints(final List<Location> t)
	{
		_PKrestartPoints = t;
	}

	public void setAdvRestartPoints(final List<Location> t)
	{
		_ADVrestartPoints = t;
	}

	public Territory getLoc()
	{
		return _loc;
	}

	public List<Location> getRestartPoints()
	{
		return _restartPoints;
	}

	public List<Location> getPKRestartPoints()
	{
		return _PKrestartPoints;
	}

	public Location getSpawn()
	{
		if(_restartPoints == null)
		{
			_log.warn("Zone.getSpawn(): restartPoint not found for " + toString());
			return new Location(17817, 170079, -3530);
		}
		final Location loc = _restartPoints.get(Rnd.get(_restartPoints.size()));
		return loc.clone();
	}

	public Location getPKSpawn()
	{
		if(_PKrestartPoints == null)
			return getSpawn();
		final Location loc = _PKrestartPoints.get(Rnd.get(_PKrestartPoints.size()));
		return loc.clone();
	}

	public Location getAdvSpawn()
	{
		if(_ADVrestartPoints == null)
			return getSpawn();
		final Location loc = _ADVrestartPoints.get(Rnd.get(_ADVrestartPoints.size()));
		return loc.clone();
	}

	public boolean checkIfInZone(final int x, final int y)
	{
		return _loc != null && _loc.isInside(x, y);
	}

	public boolean checkIfInZone(final int x, final int y, final int z)
	{
		return _loc != null && _loc.isInside(x, y, z);
	}

	public boolean checkIfInZone(final GameObject obj)
	{
		return _loc != null && _loc.isInside(obj.getX(), obj.getY(), obj.getZ());
	}

	public final double findDistanceToZone(final GameObject obj, final boolean includeZAxis)
	{
		return this.findDistanceToZone(obj.getX(), obj.getY(), obj.getZ(), includeZAxis);
	}

	public final double findDistanceToZone(final int x, final int y, final int z, final boolean includeZAxis)
	{
		if(_loc == null)
			return 9.9999999E7;
		return Util.calculateDistance(x, y, z, (_loc.getXmax() + _loc.getXmin()) / 2, (_loc.getYmax() + _loc.getYmin()) / 2, (_loc.getZmax() + _loc.getZmin()) / 2, includeZAxis);
	}

	public void doEnter(final GameObject object)
	{
		synchronized (_objects)
		{
			if(!_objects.contains(object))
				_objects.add(object);
			else if(Config.DEBUG)
				_log.warn("Attempt of double object add to zone " + _name + " id " + _id);
		}
		if(!_active)
			return;
		onZoneEnter(object);
	}

	public void doLeave(final GameObject object, final boolean notify)
	{
		synchronized (_objects)
		{
			final boolean remove = _objects.remove(object);
			if(!remove && Config.DEBUG)
				_log.warn("Attempt remove object from zone " + _name + " id " + _id + " where it's absent");
		}
		if(!_active)
			return;
		if(notify)
			onZoneLeave(object);
	}

	private void onZoneEnter(final GameObject object)
	{
		object.addZone(this);
		getListenerEngine().fireMethodInvoked(new ZoneEnterLeaveEvent("Zone.onZoneEnter", this, new GameObject[] { object }));
		checkEffects(object, true);
		addZoneStats((Creature) object);
		if(object.isPlayer())
		{
			Player player = (Player) object;
			player.doZoneCheck(_entering_message_no);
			if(_entering_message != null)
				player.sendMessage(_entering_message);
			if(_eventId != 0)
				player.sendPacket(new EventTrigger(_eventId, true));
			if(Config.ZONE_EQUIP && _restrictEquip != null)
			{
				for(int i = 0; i < 17; ++i)
				{
					final ItemInstance item = player.getInventory().getPaperdollItem(i);
					if(item != null && _restrictEquip.contains(item.getItemId()))
						player.getInventory().unEquipItemInBodySlotAndNotify(item.getBodyPart(), item);
				}
			}
			if(getPrivateStoreCurrecy() > 0)
			{
				String msg = new CustomMessage("l2s.gameserver.model.Zone.enter_changed_private_store_currecy").addItemName(getPrivateStoreCurrecy()).toString(player);
				player.sendPacket(new ExShowScreenMessage(msg, 10000, ScreenMessageAlign.MIDDLE_CENTER, true));
				player.setPrivateStoreCurrecy(getPrivateStoreCurrecy());
			}
		}
	}

	private void onZoneLeave(final GameObject object)
	{
		object.removeZone(this);
		getListenerEngine().fireMethodInvoked(new ZoneEnterLeaveEvent("Zone.onZoneLeave", this, new GameObject[] { object }));
		checkEffects(object, false);
		removeZoneStats((Creature) object);
		if(object.isPlayer())
		{
			Player player = (Player) object;
			player.doZoneCheck(_leaving_message_no);
			if(_leaving_message != null)
				player.sendMessage(_leaving_message);
			if(_eventId != 0)
				player.sendPacket(new EventTrigger(_eventId, false));
			if(getPrivateStoreCurrecy() > 0)
			{
				String msg = new CustomMessage("l2s.gameserver.model.Zone.exit_changed_private_store_currecy").addItemName(getPrivateStoreCurrecy()).toString(player);
				player.sendPacket(new ExShowScreenMessage(msg, 10000, ScreenMessageAlign.MIDDLE_CENTER, true));
				player.setPrivateStoreCurrecy(0);
			}
		}
	}

	private void addZoneStats(final Creature object)
	{
		if(_target != null && !checkTarget(object))
			return;
		if(moveBonus != 0 && object.isPlayable())
		{
			object.addStatFunc(new FuncAdd(Stats.RUN_SPEED, 64, this, getMoveBonus()));
			object.sendChanges();
		}
		if(affectRace != null && !object.isPlayer())
			return;
		if(affectRace != null && !affectRace.equalsIgnoreCase("all"))
		{
			final Player player = object.getPlayer();
			if(!player.getRace().toString().equalsIgnoreCase(affectRace))
				return;
		}
		if(regenBonusHP != 0)
			object.addStatFunc(new FuncAdd(Stats.REGENERATE_HP_RATE, 64, this, getRegenBonusHP()));
		if(regenBonusMP != 0)
			object.addStatFunc(new FuncAdd(Stats.REGENERATE_MP_RATE, 64, this, getRegenBonusMP()));
	}

	private void removeZoneStats(final Creature object)
	{
		if(regenBonusHP == 0 && regenBonusMP == 0 && moveBonus == 0)
			return;
		object.removeStatsOwner(this);
		if(moveBonus > 0)
			object.sendChanges();
	}

	private void checkEffects(final GameObject object, final boolean enter)
	{
		if(_event != null && _event.startsWith("script"))
		{
			final String[] param = _event.split(";");
			Functions.callScripts(param[1], param[2], new Object[] { this, object, enter, Integer.parseInt(param[3]), Location.parseLoc(param[4]) });
		}
		if(_skill == null || _target == null)
			return;
		if(!checkTarget(object))
			return;
		final Creature p = (Creature) object;
		final ZoneSkillTimer rem = _skillTimers.remove(p);
		if(rem != null)
			rem.setCanceled(true);
		if(enter)
		{
			final ZoneSkillTimer t = new ZoneSkillTimer(p, _skill, _skillProb, _unitTick);
			_skillTimers.put(p, t);
			if(_initialDelay > 0L)
				ThreadPoolManager.getInstance().schedule(t, _initialDelay);
			else
				t.run();
		}
	}

	private boolean checkTarget(final GameObject object)
	{
		switch(_target)
		{
			case pc:
			{
				return object.isPlayable();
			}
			case only_pc:
			{
				return object.isPlayer();
			}
			case npc:
			{
				return object.isNpc();
			}
			default:
			{
				return false;
			}
		}
	}

	public GameObject[] getObjects()
	{
		synchronized (_objects)
		{
			return (GameObject[]) _objects.toArray((Object[]) new GameObject[_objects.size()]);
		}
	}

	public List<GameObject> getInsideObjectsIncludeZ()
	{
		final GameObject[] all_objects = getObjects();
		final List<GameObject> result = new ArrayList<GameObject>();
		for(final GameObject obj : all_objects)
			if(obj != null && _loc.isInside(obj.getX(), obj.getY(), obj.getZ()))
				result.add(obj);
		return result;
	}

	public List<Player> getInsidePlayers()
	{
		final GameObject[] all_objects = getObjects();
		final List<Player> result = new ArrayList<Player>();
		for(final GameObject obj : all_objects)
			if(obj != null && obj.isPlayer())
				result.add((Player) obj);
		return result;
	}

	public List<Playable> getInsidePlayables()
	{
		final GameObject[] all_objects = getObjects();
		final List<Playable> result = new ArrayList<Playable>();
		for(final GameObject obj : all_objects)
			if(obj != null && obj.isPlayable())
				result.add((Playable) obj);
		return result;
	}

	public List<Player> getInsidePlayersIncludeZ()
	{
		final GameObject[] all_objects = getObjects();
		final List<Player> result = new ArrayList<Player>();
		for(final GameObject obj : all_objects)
			if(obj != null && obj.isPlayer() && _loc.isInside(obj.getX(), obj.getY(), obj.getZ()))
				result.add((Player) obj);
		return result;
	}

	public void setActive(final boolean value)
	{
		if(_active == value)
			return;
		synchronized (_objects)
		{
			_active = value;
			for(final GameObject obj : _objects)
				if(_active)
					onZoneEnter(obj);
				else
					onZoneLeave(obj);
		}
		setDamageTaskActive(value);
	}

	public void setDamageTaskActive(final boolean value)
	{
		if(value && (damageOnHP > 0 || damageOnMP > 0))
			new DamageTask().start();
		else
			damageTask = null;
	}

	public boolean isActive()
	{
		return _active;
	}

	public final String getName()
	{
		return _name;
	}

	public final void setName(final String name)
	{
		_name = name;
	}

	public final int getEnteringMessageId()
	{
		return _entering_message_no;
	}

	public final void setEnteringMessageId(final int id)
	{
		_entering_message_no = id;
	}

	public final int getLeavingMessageId()
	{
		return _leaving_message_no;
	}

	public final void setLeavingMessageId(final int id)
	{
		_leaving_message_no = id;
	}

	public final void setEnteringMessage(final String msg)
	{
		_entering_message = msg;
	}

	public final void setLeavingMessage(final String msg)
	{
		_leaving_message = msg;
	}

	public final void setIndex(final int index)
	{
		_index = index;
	}

	public final int getIndex()
	{
		return _index;
	}

	public final void setTaxById(final int id)
	{
		_taxById = id;
	}

	public final Integer getTaxById()
	{
		return _taxById;
	}

	public void setSkill(final String skill)
	{
		if(skill == null || skill.equalsIgnoreCase("null"))
			return;
		final String[] sk = skill.split(";");
		_skill = SkillTable.getInstance().getInfo(Short.parseShort(sk[0]), Short.parseShort(sk[1]));
	}

	public void setSkill(final Skill skill)
	{
		_skill = skill;
	}

	public void setSkillProb(final String chance)
	{
		if(chance == null)
		{
			_skillProb = -1;
			return;
		}
		_skillProb = Integer.parseInt(chance);
	}

	public void setUnitTick(final String delay)
	{
		if(delay == null)
		{
			_unitTick = 1000L;
			return;
		}
		_unitTick = Integer.parseInt(delay) * 666L;
	}

	public void setInitialDelay(final String delay)
	{
		if(delay == null)
		{
			_initialDelay = 0L;
			return;
		}
		_initialDelay = Integer.parseInt(delay) * 1000L;
	}

	public void setTarget(final String target)
	{
		if(target == null)
			return;
		_target = ZoneTarget.valueOf(target);
	}

	public long getRestartTime()
	{
		return _restartTime;
	}

	public Skill getZoneSkill()
	{
		return _skill;
	}

	public ZoneTarget getZoneTarget()
	{
		return _target;
	}

	public void setRestartTime(final long restartTime)
	{
		_restartTime = restartTime;
	}

	public void setBlockedActions(final String blockedActions)
	{
		_blockedActions = blockedActions;
	}

	public boolean isActionBlocked(final String action)
	{
		return _blockedActions != null && _blockedActions.contains(action);
	}

	public ListenerEngine<Zone> getListenerEngine()
	{
		if(listenerEngine == null)
			listenerEngine = new DefaultListenerEngine<Zone>(this);
		return listenerEngine;
	}

	public int getMessageNumber()
	{
		return messageNumber;
	}

	public void setMessageNumber(final int messageNumber)
	{
		this.messageNumber = messageNumber;
	}

	public void setMessageNumber(final String number)
	{
		messageNumber = number == null ? 0 : Integer.parseInt(number);
	}

	public int getDamageOnHP()
	{
		return damageOnHP;
	}

	public void setDamageOnHP(final int damageOnHP)
	{
		this.damageOnHP = damageOnHP;
	}

	public void setDamageOnHP(final String damage)
	{
		damageOnHP = damage == null ? 0 : Integer.parseInt(damage);
	}

	public int getDamageOnMP()
	{
		return damageOnMP;
	}

	public void setDamageOnMP(final int damageOnMP)
	{
		this.damageOnMP = damageOnMP;
	}

	public void setDamageOn(final String damage)
	{
		damageOnMP = damage == null ? 0 : Integer.parseInt(damage);
	}

	public int getMoveBonus()
	{
		return moveBonus;
	}

	public void setMoveBonus(final int moveBonus)
	{
		this.moveBonus = moveBonus;
	}

	public void setMoveBonus(final String moveBonus)
	{
		this.moveBonus = moveBonus == null ? 0 : Integer.parseInt(moveBonus);
	}

	public int getRegenBonusHP()
	{
		return regenBonusHP;
	}

	public void setRegenBonusHP(final String bonus)
	{
		regenBonusHP = bonus == null ? 0 : Integer.parseInt(bonus);
	}

	public void setRegenBonusHP(final int regenBonusHP)
	{
		this.regenBonusHP = regenBonusHP;
	}

	public int getRegenBonusMP()
	{
		return regenBonusMP;
	}

	public void setRegenBonusMP(final int regenBonusMP)
	{
		this.regenBonusMP = regenBonusMP;
	}

	public void setRegenBonusMP(final String bonus)
	{
		regenBonusMP = bonus == null ? 0 : Integer.parseInt(bonus);
	}

	public String getAffectRace()
	{
		return affectRace;
	}

	public void setAffectRace(String affectRace)
	{
		if(affectRace != null)
			affectRace = affectRace.toLowerCase().replace(" ", "");
		this.affectRace = affectRace;
	}

	public String getEvent()
	{
		return _event;
	}

	public void setEvent(final String event)
	{
		_event = event;
	}

	public void setEventId(final int id)
	{
		_eventId = id;
	}

	public int getMaxZ(final int x, final int y, final int z)
	{
		if(this.checkIfInZone(x, y))
			return _loc.getZmax();
		return z;
	}

	public int getMinZ(final int x, final int y, final int z)
	{
		if(this.checkIfInZone(x, y))
			return _loc.getZmin();
		return z;
	}

	public boolean restrictSkill(final int id)
	{
		return _restrictSkills != null && _restrictSkills.contains(id);
	}

	public void setRestrictSkills(final int[] ids)
	{
		if(ids.length > 0)
		{
			_restrictSkills = new ArrayList<Integer>(ids.length);
			for(final int i : ids)
				_restrictSkills.add(i);
		}
	}

	public boolean restrictEquip(final int id)
	{
		return _restrictEquip != null && _restrictEquip.contains(id);
	}

	public void setRestrictEquip(final int[] ids)
	{
		if(ids.length > 0)
		{
			_restrictEquip = new ArrayList<Integer>(ids.length);
			for(final int i : ids)
				_restrictEquip.add(i);
			Config.ZONE_EQUIP = true;
		}
	}

	public void setParam(final String name, final String value)
	{
		_params.put(name, value);
	}

	public MultiValueSet<String> getParams()
	{
		return _params;
	}

	public void setPrivateStoreCurrecy(int value)
	{
		_privateStoreCurrecy = value;
	}

	public int getPrivateStoreCurrecy()
	{
		return _privateStoreCurrecy;
	}
}
