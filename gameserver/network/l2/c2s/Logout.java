package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class Logout extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getEnchantScroll() != null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(142));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInCombat() && !activeChar.isGM())
		{
			activeChar.sendPacket(new SystemMessage(101));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(1471));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isBlocked())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.Logout.OutOfControl"));
			activeChar.sendActionFailed();
			return;
		}
		if(Config.ALLOW_SEVEN_SIGNS)
		{
			if(activeChar.isFestivalParticipant())
			{
				if(SevenSignsFestival.getInstance().isFestivalInitialized())
				{
					activeChar.sendMessage("You cannot log out while you are a participant in a festival.");
					return;
				}
				Party playerParty = activeChar.getParty();
				if(playerParty != null)
					playerParty.broadcastMessageToPartyMembers(activeChar.getName() + " has been removed from the upcoming festival.");
			}
		}
		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.Logout.Olympiad"));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.inObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.Logout.Observer"));
			activeChar.sendActionFailed();
			return;
		}
		activeChar.client_request = true;
		activeChar.logout();
	}
}
