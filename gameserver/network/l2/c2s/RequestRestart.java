package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfo;
import l2s.gameserver.network.l2.s2c.RestartResponse;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestRestart extends L2GameClientPacket
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
			sendPacket(RestartResponse.valueOf(false));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(142));
			sendPacket(RestartResponse.valueOf(false));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestRestart.Olympiad"));
			sendPacket(RestartResponse.valueOf(false));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.inObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestRestart.Observer"));
			sendPacket(RestartResponse.valueOf(false));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInCombat() && !activeChar.isGM())
		{
			activeChar.sendPacket(new SystemMessage(102));
			sendPacket(RestartResponse.valueOf(false));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(1471));
			sendPacket(RestartResponse.valueOf(false));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isBlocked() && !activeChar.isFlying())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestRestart.OutOfControl"));
			sendPacket(RestartResponse.valueOf(false));
			activeChar.sendActionFailed();
			return;
		}
		if(Config.ALLOW_SEVEN_SIGNS)
		{
			if(activeChar.isFestivalParticipant())
			{
				if(SevenSignsFestival.getInstance().isFestivalInitialized())
				{
					activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestRestart.Festival"));
					sendPacket(RestartResponse.valueOf(false));
					activeChar.sendActionFailed();
					return;
				}
				final Party playerParty = activeChar.getParty();
				if(playerParty != null)
					playerParty.broadcastMessageToPartyMembers(activeChar.getName() + " has been removed from the upcoming festival.");
			}
		}
		if(getClient() != null)
			getClient().setState(GameClient.GameClientState.AUTHED);
		activeChar.client_request = true;
		activeChar.restart();
		sendPacket(RestartResponse.valueOf(true));
		final CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
}
