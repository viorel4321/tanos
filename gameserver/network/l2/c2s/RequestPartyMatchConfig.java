package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.instancemanager.PartyRoomManager;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ListPartyWaiting;

public class RequestPartyMatchConfig extends L2GameClientPacket
{
	private int _page;
	private int _region;
	private int _allLevels;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_region = readD();
		_allLevels = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final Party party = player.getParty();
		final CommandChannel channel = party != null ? party.getCommandChannel() : null;
		if(channel != null && !channel.getParties().contains(party))
			player.sendMessage("The Command Channel affiliated party's party member cannot use the matching screen.");
		else if(party != null && !party.isLeader(player))
			player.sendPacket(Msg.THE_LIST_OF_PARTY_ROOMS_CAN_BE_VIEWED_BY_A_PERSON_WHO_HAS_NOT_JOINED_A_PARTY_OR_WHO_IS_A_PARTY_LEADER);
		else
		{
			PartyRoomManager.getInstance().addToWaitingList(player);
			player.sendPacket(new ListPartyWaiting(_region, _allLevels == 1, _page, player));
		}
	}
}
