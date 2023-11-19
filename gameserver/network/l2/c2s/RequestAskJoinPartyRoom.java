package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.PartyRoom;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.s2c.ExAskJoinPartyRoom;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final Player targetPlayer = World.getPlayer(_name);
		if(targetPlayer == null || targetPlayer == player)
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInTransaction())
		{
			player.sendPacket(new SystemMessage(164));
			return;
		}
		if(targetPlayer.isInTransaction())
		{
			player.sendPacket(new SystemMessage(153).addName(targetPlayer));
			return;
		}
		if(targetPlayer.getPartyRoom() != null)
			return;
		final PartyRoom room = player.getPartyRoom();
		if(room == null)
			return;
		if(room.getLeader() != player)
		{
			player.sendPacket(new SystemMessage(1832));
			return;
		}
		if(room.getPlayers().size() >= room.getMaxMembersSize())
		{
			player.sendPacket(new SystemMessage(1834));
			return;
		}
		new Transaction(Transaction.TransactionType.PARTY_ROOM, player, targetPlayer, 10000L);
		targetPlayer.sendPacket(new ExAskJoinPartyRoom(player.getName()));
		player.sendPacket(new SystemMessage(1901).addString(targetPlayer.getName()));
	}
}
