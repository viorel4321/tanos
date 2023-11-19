package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Util;

public class RequestSocialAction extends L2GameClientPacket
{
	private int _actionId;

	@Override
	public void readImpl()
	{
		_actionId = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(1471));
			return;
		}
		if(_actionId < 2 || _actionId > 13)
		{
			Util.handleIllegalPlayerAction(activeChar, "Character " + activeChar.getName() + " at account " + activeChar.getAccountName() + " requested an internal Social Action " + _actionId, 1);
			return;
		}
		if(activeChar.getPrivateStoreType() == 0 && !activeChar.isInTransaction() && !activeChar.isActionsDisabled() && !activeChar.isSitting())
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), _actionId));
			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600L);
				activeChar.block();
			}
		}
	}

	class SocialTask implements Runnable
	{
		Player _player;

		SocialTask(final Player player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			_player.unblock();
		}
	}
}
