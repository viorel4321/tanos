package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.handler.IUserCommandHandler;
import l2s.gameserver.handler.UserCommandHandler;
import l2s.gameserver.model.Player;

public class RequestUserCommand extends L2GameClientPacket
{
	static Logger _log;
	private int _command;

	@Override
	public void readImpl()
	{
		_command = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);
		if(handler == null)
			activeChar.sendMessage("user commandID " + _command + " not implemented yet");
		else
			handler.useUserCommand(_command, getClient().getActiveChar());
	}

	static
	{
		RequestUserCommand._log = LoggerFactory.getLogger(RequestUserCommand.class);
	}
}
