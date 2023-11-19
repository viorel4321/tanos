package l2s.gameserver.skills.skillclasses;

import java.util.List;
import java.util.Set;

import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.instances.SiegeHeadquarterInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.funcs.FuncMul;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public class SiegeFlag extends Skill
{
	private final double _advancedMult;

	public SiegeFlag(final StatsSet set)
	{
		super(set);
		_advancedMult = set.getDouble("advancedMultiplier", 1.0);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!activeChar.isPlayer())
			return false;
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger))
			return false;
		final Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
			return false;
		if(player.isInZone(Zone.ZoneType.RESIDENCE))
		{
			player.sendPacket(new SystemMessage(290), new SystemMessage(113).addSkillName(_id, getDisplayLevel()));
			return false;
		}
		final SiegeEvent<?, ?> siegeEvent = activeChar.getEvent(SiegeEvent.class);
		if(siegeEvent == null)
		{
			player.sendPacket(new SystemMessage(290), new SystemMessage(113).addSkillName(_id, getDisplayLevel()));
			return false;
		}
		boolean inZone = false;
		final List<ZoneObject> zones = siegeEvent.getObjects("flag_zones");
		for(final ZoneObject zone : zones)
			if(player.isInZone(zone.getZone()))
				inZone = true;
		if(!inZone)
		{
			player.sendPacket(new SystemMessage(290), new SystemMessage(113).addSkillName(_id, getDisplayLevel()));
			return false;
		}
		final SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", player.getClan());
		if(siegeClan == null)
		{
			player.sendPacket(new SystemMessage(294), new SystemMessage(113).addSkillName(_id, getDisplayLevel()));
			return false;
		}
		if(siegeClan.getFlag() != null)
		{
			player.sendPacket(new SystemMessage(289), new SystemMessage(113).addSkillName(_id, getDisplayLevel()));
			return false;
		}
		return true;
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		final Player player = (Player) activeChar;
		final Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader())
			return;
		final SiegeEvent<?, ?> siegeEvent = activeChar.getEvent(SiegeEvent.class);
		if(siegeEvent == null)
			return;
		final SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
		if(siegeClan == null)
			return;
		if(siegeClan.getFlag() != null)
			return;
		final SiegeHeadquarterInstance flag = new SiegeHeadquarterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(35062));
		flag.setClan(siegeClan);
		flag.addEvent(siegeEvent);
		if(_id == 326)
			flag.addStatFunc(new FuncMul(Stats.MAX_HP, 80, flag, _advancedMult));
		flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
		flag.setHeading(player.getHeading());
		final int x = (int) (player.getX() + 100.0 * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
		final int y = (int) (player.getY() + 100.0 * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
		Location loc = GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, 0);
		if(loc == null)
			loc = Location.findAroundPosition(player.getLoc(), 100, player.getGeoIndex());
		flag.spawnMe(loc);
		siegeClan.setFlag(flag);
	}
}
