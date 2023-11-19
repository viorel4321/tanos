package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.DimensionalRiftManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public class Call extends Skill
{
	final boolean _party;

	public Call(final StatsSet set)
	{
		super(set);
		_party = set.getBool("party", false);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(activeChar.isPlayer())
		{
			if(_party && ((Player) activeChar).getParty() == null)
				return false;
			if(DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getLoc(), false))
			{
				activeChar.sendPacket(new SystemMessage(650));
				return false;
			}
			SystemMessage msg = canSummonHere((Player) activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}
			if(!_party)
			{
				if(activeChar == target)
					return false;
				msg = canBeSummoned(target, ((Player) activeChar).isIn7sDungeon());
				if(msg != null)
				{
					activeChar.sendPacket(msg);
					return false;
				}
			}
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;
		final SystemMessage msg = canSummonHere((Player) activeChar);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			return;
		}
		final boolean ss = ((Player) activeChar).isIn7sDungeon();
		if(_party)
		{
			if(((Player) activeChar).getParty() != null)
				for(final Player target : ((Player) activeChar).getParty().getPartyMembers())
					if(target.getObjectId() != activeChar.getObjectId() && canBeSummoned(target, ss) == null)
					{
						target.stopMove();
						target.teleToLocation(Location.findAroundPosition(activeChar.getLoc(), 100, 150, activeChar.getGeoIndex()));
						this.getEffects(activeChar, target, getActivateRate() > 0, false);
						if(activeChar.isGM())
							continue;
						target.sendMessage("\u0412\u0430\u0441 \u043f\u0440\u0438\u0437\u0432\u0430\u043b \u0438\u0433\u0440\u043e\u043a " + activeChar.getName() + " \u0433\u0440\u0443\u043f\u043f\u043e\u0432\u044b\u043c \u043f\u0440\u0438\u0437\u044b\u0432\u043e\u043c \u0438 \u0432\u043e\u0432\u0441\u0435 \u041d\u0415 \u0410\u0434\u043c\u0438\u043d \u0438 \u041d\u0415 \u0413\u041c!");
					}
			if(isSSPossible())
				activeChar.unChargeShots(isMagic());
			return;
		}
		for(final Creature target2 : targets)
			if(target2 != null)
			{
				if(canBeSummoned(target2, ss) != null)
					continue;
				((Player) target2).summonCharacterRequest(activeChar, Location.findAroundPosition(activeChar.getLoc(), 100, 150, activeChar.getGeoIndex()), getId() == 1403 || getId() == 1404 ? 1 : 0);
				this.getEffects(activeChar, target2, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	public static SystemMessage canSummonHere(final Player activeChar)
	{
		if(activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || activeChar.isFlying() || activeChar.isFestivalParticipant())
			return new SystemMessage(61);
		if(activeChar.isInZoneBattle() || activeChar.isInZone(Zone.ZoneType.Siege) || activeChar.isInZone(Zone.ZoneType.no_restart) || activeChar.isInZone(Zone.ZoneType.no_summon) || activeChar.isInZone(Zone.ZoneType.epic) || activeChar.isInVehicle() || activeChar.getReflectionId() != 0)
			return new SystemMessage(650);
		if(activeChar.isInStoreMode() || activeChar.isInTransaction())
			return new SystemMessage(577);
		if(activeChar.getVar("jailed") != null)
			return new SystemMessage("You can not summon in jail.");
		return null;
	}

	public static SystemMessage canBeSummoned(final Creature target, final boolean ss)
	{
		if(target == null || !target.isPlayer() || target.isFlying() || target.inObserverMode() || target.getPlayer().isFestivalParticipant())
			return new SystemMessage(109);
		if(target.isInOlympiadMode())
			return new SystemMessage(1911);
		if(target.isInZoneBattle() || target.isInZone(Zone.ZoneType.Siege) || target.isInZone(Zone.ZoneType.no_restart) || target.isInZone(Zone.ZoneType.no_summon) || target.isInVehicle() || target.getReflectionId() != 0)
			return new SystemMessage(1899);
		if(target.isAlikeDead())
			return new SystemMessage(1844).addString(target.getName());
		if(target.getPvpFlag() != 0 || target.isInCombat())
			return new SystemMessage(1843).addString(target.getName());
		final Player pTarget = (Player) target;
		if(pTarget.isInStoreMode() || pTarget.isInTransaction())
			return new SystemMessage(1898).addString(target.getName());
		if(pTarget.getVar("jailed") != null)
			return new SystemMessage(pTarget.getName() + " is in jail and can not be summoned.");
		if(ss)
		{
			if(Config.ALLOW_SEVEN_SIGNS)
			{
				final int targetCabal = SevenSigns.getInstance().getPlayerCabal(pTarget);
				if(SevenSigns.getInstance().isSealValidationPeriod())
				{
					if(targetCabal != SevenSigns.getInstance().getCabalHighestScore())
						return new SystemMessage(1899);
				}
				else if(targetCabal == 0)
					return new SystemMessage(1899);
			}
		}
		return null;
	}
}
