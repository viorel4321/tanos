package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class ClanHallNpcSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject>
{
	public ClanHallNpcSiegeEvent(final MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void startEvent()
	{
		_oldOwner = getResidence().getOwner();
		this.broadcastInZone(new SystemMessage(844).addString(getResidence().getName()));
		super.startEvent();
	}

	@Override
	public void stopEvent(final boolean step)
	{
		final Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			if(_oldOwner != newOwner)
			{
				newOwner.broadcastToOnlineMembers(new PlaySound("Siege_Victory"));
				newOwner.incReputation(getResidence().getReputation(), false, toString());
				if(_oldOwner != null && getResidence().getReputationLoser() < 0)
					_oldOwner.incReputation(getResidence().getReputationLoser(), false, toString());
			}
			this.broadcastInZone(new SystemMessage(855).addString(newOwner.getName()).addString(getResidence().getName()));
			this.broadcastInZone(new SystemMessage(843).addString(getResidence().getName()));
		}
		else
			this.broadcastInZone(new SystemMessage(856).addString(getResidence().getName()));
		super.stopEvent(step);
		_oldOwner = null;
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
	{}
}
