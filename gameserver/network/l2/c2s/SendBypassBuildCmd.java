package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.handler.AdminCommandHandler;
import l2s.gameserver.handler.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;

public class SendBypassBuildCmd extends L2GameClientPacket
{
	public static int GM_MESSAGE;
	public static int ANNOUNCEMENT;
	private String _command;

	@Override
	public void readImpl()
	{
		_command = readS();
		if(_command != null)
			_command = _command.trim();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(!activeChar.isGM() && !activeChar.getPlayerAccess().CanUseGMCommand)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.SendBypassBuildCmd.NoCommandOrAccess").addString(_command));
			return;
		}
		if(activeChar.isKeyBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}
		final GameObject target = activeChar.getTarget();
		if(target != null && target.isPlayer())
			_command = _command.replaceFirst("%target", target.getName());
		String cmd = "admin_" + _command;
		IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(cmd);
		if(ach == null)
		{
			cmd = _command;
			ach = AdminCommandHandler.getInstance().getAdminCommandHandler(cmd);
		}
		if(ach != null)
			try
			{
				ach.useAdminCommand(cmd, activeChar);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		else
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.SendBypassBuildCmd.NoCommandOrAccess").addString(_command));
	}

	static
	{
		SendBypassBuildCmd.GM_MESSAGE = 9;
		SendBypassBuildCmd.ANNOUNCEMENT = 10;
	}
}
