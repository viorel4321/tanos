package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class ClanHallSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject>
{
	public static final String BOSS = "boss";

	public ClanHallSiegeEvent(final MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void startEvent()
	{
		_oldOwner = getResidence().getOwner();
		if(_oldOwner != null)
		{
			getResidence().changeOwner(null);
			addObject("attackers", new SiegeClanObject("attackers", _oldOwner, 0L));
		}
		if(this.getObjects("attackers").size() == 0)
		{
			this.broadcastInZone2(new SystemMessage(846).addString(getResidence().getName()));
			reCalcNextTime(false);
			return;
		}
		SiegeClanDAO.getInstance().delete(getResidence());
		updateParticles(true, "attackers");
		this.broadcastTo(new SystemMessage(844).addString(getResidence().getName()), "attackers");
		super.startEvent();
	}

	@Override
	public void stopEvent(final boolean step)
	{
		final Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			newOwner.broadcastToOnlineMembers(new PlaySound("Siege_Victory"));
			newOwner.incReputation(getResidence().getReputation(), false, toString());
			this.broadcastTo(new SystemMessage(855).addString(newOwner.getName()).addString(getResidence().getName()), "attackers");
			this.broadcastTo(new SystemMessage(843).addString(getResidence().getName()), "attackers");
		}
		else
			this.broadcastTo(new SystemMessage(856).addString(getResidence().getName()), "attackers");
		updateParticles(false, "attackers");
		this.removeObjects("attackers");
		super.stopEvent(step);
		_oldOwner = null;
	}

	@Override
	public void removeState(final int val)
	{
		super.removeState(val);
		if(val == 2)
			this.broadcastTo(new SystemMessage(845).addString(getResidence().getName()), "attackers");
	}

	@Override
	public void processStep(final Clan clan)
	{
		if(clan != null)
			getResidence().changeOwner(clan);
		this.stopEvent(true);
	}

	@Override
	public void loadSiegeClans()
	{
		addObjects("attackers", SiegeClanDAO.getInstance().load(getResidence(), "attackers"));
	}

	@Override
	public int getUserRelation(final Player thisPlayer, final int result)
	{
		return result;
	}

	@Override
	public int getRelation(final Player thisPlayer, final Player targetPlayer, final int result)
	{
		return result;
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
		final ClanHallSiegeEvent siegeEvent = targetPlayer.getEvent(ClanHallSiegeEvent.class);
		if(siegeEvent != this)
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
		final SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("attackers", targetPlayer.getClan());
		if(targetSiegeClan.getFlag() == null)
		{
			if(force)
				targetPlayer.sendPacket(Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
			resurrectPlayer.sendPacket(force ? Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : Msg.INCORRECT_TARGET);
			return false;
		}
		if(force)
			return true;
		resurrectPlayer.sendPacket(Msg.INCORRECT_TARGET);
		return false;
	}
}
