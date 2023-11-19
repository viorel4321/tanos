package l2s.gameserver.network.l2.c2s;

import java.nio.BufferUnderflowException;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.StopMove;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public class MoveBackwardToLocation extends L2GameClientPacket
{
	private Location _targetLoc;
	private Location _originLoc;
	private int _moveMovement;

	public MoveBackwardToLocation()
	{
		_targetLoc = new Location();
		_originLoc = new Location();
	}

	@Override
	public void readImpl()
	{
		_targetLoc.x = readD();
		_targetLoc.y = readD();
		_targetLoc.z = readD();
		_originLoc.x = readD();
		_originLoc.y = readD();
		_originLoc.z = readD();
		try
		{
			_moveMovement = readD();
		}
		catch(BufferUnderflowException e)
		{
			if(Config.L2WALKER_PROTECTION)
			{
				final Player player = getClient().getActiveChar();
				if(player != null)
					Util.handleIllegalPlayerAction(player, "trying to use L2Walker", 1);
			}
		}
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(_moveMovement == 0 && !Config.ALLOW_KEYBOARD_MOVE)
		{
			player.sendActionFailed();
			return;
		}
		player.setActive();
		if(System.currentTimeMillis() - player.getLastMovePacket() < Config.MOVE_PACKET_DELAY)
		{
			player.sendActionFailed();
			return;
		}
		player.setLastMovePacket();
		if(player.isInVehicle() && (player.getDistance(_targetLoc.x, _targetLoc.y) > 500.0 || Math.abs(_targetLoc.z - player.getZ()) > 200))
		{
			player.sendPacket(new StopMove(player));
			player.sendActionFailed();
			return;
		}
		if(player.isTeleporting())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isBlocked())
		{
			player.sendPacket(new SystemMessage(687));
			player.sendActionFailed();
			return;
		}
		if((player.inObserverMode() || player.isOutOfControl()) && player.getOlympiadObserveId() == -1)
		{
			player.sendActionFailed();
			return;
		}
		player.closeEnchant();
		if(player.getTeleMode() > 0)
		{
			if(player.getTeleMode() == 1)
				player.setTeleMode(0);
			player.sendActionFailed();
			player.teleToLocation(_targetLoc);
			return;
		}
		final double dx = _targetLoc.x - player.getX();
		final double dy = _targetLoc.y - player.getY();
		if(dx * dx + dy * dy > 9.801E7)
		{
			player.sendActionFailed();
			return;
		}
		player.moveToLocation(_targetLoc, 0, _moveMovement != 0 && !player.getVarBoolean("no_pf"));
	}
}
