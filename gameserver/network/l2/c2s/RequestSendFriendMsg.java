package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.s2c.FriendRecvMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.FriendsTable;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.SpamFilter;

public class RequestSendFriendMsg extends L2GameClientPacket
{
	private String _message;
	private String _receiver;

	@Override
	public void readImpl()
	{
		_message = readS(2048);
		_receiver = readS(Config.CNAME_MAXLEN);
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getNoChannel() != 0L)
		{
			if(activeChar.getNoChannelRemained() > 0L || activeChar.getNoChannel() < 0L)
			{
				activeChar.sendPacket(new SystemMessage(966));
				return;
			}
			activeChar.updateNoChannel(0L);
		}
		final Player targetPlayer = World.getPlayer(_receiver);
		if(targetPlayer == null)
		{
			activeChar.sendPacket(new SystemMessage(145));
			return;
		}
		if(!FriendsTable.getInstance().checkIsFriends(activeChar.getObjectId(), targetPlayer.getObjectId()))
		{
			activeChar.sendPacket(new SystemMessage(171).addString(_receiver));
			return;
		}
		if(targetPlayer.isBlockAll())
		{
			activeChar.sendPacket(new SystemMessage(176));
			return;
		}
		Log.LogChat("FRIENDTELL", activeChar.getName(), _receiver, _message);
		if(SpamFilter.getInstance().checkSpam(activeChar, _message, 12) || activeChar.isSameHWID(targetPlayer.getHWID()))
			targetPlayer.sendPacket(new FriendRecvMsg(activeChar.getName(), _receiver, _message));
	}
}
