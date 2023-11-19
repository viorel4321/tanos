package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class Ride extends Skill
{
	public Ride(final StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!activeChar.isPlayer())
			return false;
		final Player player = (Player) activeChar;
		if(getNpcId() != 0)
		{
			if(player.isInDuel())
			{
				player.sendMessage("You cannot mount a steed while in a duel.");
				return false;
			}
			if(player.isInCombat())
			{
				player.sendMessage("You cannot mount a steed while in battle.");
				return false;
			}
			if(player.isFishing())
			{
				player.sendMessage("You cannot mount a steed while fishing.");
				return false;
			}
			if(player.isSitting())
			{
				player.sendMessage("You cannot mount a steed while sitting.");
				return false;
			}
			if(player.isCursedWeaponEquipped())
			{
				player.sendMessage("You cannot mount a steed while a cursed weapon is equipped.");
				return false;
			}
			if(player.getServitor() != null)
			{
				player.sendMessage("You cannot mount a steed while a pet or a servitor is summoned.");
				return false;
			}
			if(player.isMounted())
			{
				player.sendMessage("You have already mounted another steed.");
				return false;
			}
			if(player.isInVehicle())
			{
				player.sendMessage("You cannot mount in a boat.");
				return false;
			}
		}
		else if(getNpcId() == 0 && !player.isMounted())
			return false;
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature caster, final Set<Creature> targets)
	{
		if(!caster.isPlayer())
			return;
		final Player activeChar = (Player) caster;
		activeChar.setMount(getNpcId(), 0, 0);
	}
}
