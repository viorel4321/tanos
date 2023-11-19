package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.utils.Location;

public class ValidatePosition extends L2GameClientPacket
{
	private final Location _loc;
	private int _boatObjectId;
	private Location _lastClientPosition;
	private Location _lastServerPosition;

	public ValidatePosition()
	{
		_loc = new Location();
	}

	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
		_boatObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.isTeleporting() || player.inObserverMode())
			return;
		_lastClientPosition = player.getLastClientPosition();
		_lastServerPosition = player.getLastServerPosition();
		if(_lastClientPosition == null)
			_lastClientPosition = player.getLoc();
		if(_lastServerPosition == null)
			_lastServerPosition = player.getLoc();
		if(player.getX() == 0 && player.getY() == 0 && player.getZ() == 0)
		{
			if(Config.VALID_TELEPORT)
				player.teleToClosestTown();
			else
				correctPosition(player);
			return;
		}
		final Vehicle boat = player.getVehicle();
		if(boat != null)
		{
			if(boat.getObjectId() == _boatObjectId)
			{
				final Location boatLoc = player.getInVehiclePosition();
				if(boatLoc != null && (boatLoc.distance(_loc) > 1024.0 || Math.abs(_loc.z - boatLoc.z) > 256))
					player.sendPacket(boat.validateLocationPacket(player));
			}
			return;
		}
		if(player.isFalling())
		{
			player.setLastClientPosition(null);
			player.setLastServerPosition(null);
			return;
		}
		final double diff = player.getDistance(_loc.x, _loc.y);
		final int dz = Math.abs(_loc.z - player.getZ());
		if(dz >= (player.isFlying() ? 1024 : 512))
		{
			if(player.getIncorrectValidateCount() >= 3 || Config.VALID_TELEPORT)
				player.teleToClosestTown();
			else
			{
				player.teleToLocation(player.getLoc(), player.getReflectionId());
				player.setIncorrectValidateCount(player.getIncorrectValidateCount() + 1);
			}
		}
		else if(dz >= 256)
			player.validateLocation(0);
		else if(_loc.z < -15000 || _loc.z > 15000)
		{
			if(player.getIncorrectValidateCount() >= 3 || Config.VALID_TELEPORT)
				player.teleToClosestTown();
			else
			{
				correctPosition(player);
				player.setIncorrectValidateCount(player.getIncorrectValidateCount() + 1);
			}
		}
		else if(diff > 1024.0)
		{
			if(player.getIncorrectValidateCount() >= 3 || Config.VALID_TELEPORT)
				player.teleToClosestTown();
			else
			{
				player.teleToLocation(player.getLoc(), player.getReflectionId());
				player.setIncorrectValidateCount(player.getIncorrectValidateCount() + 1);
			}
		}
		else if(diff > 512.0)
			player.validateLocation(1);
		else
			player.setIncorrectValidateCount(0);
		player.setLastClientPosition(_loc.setH(player.getHeading()));
		player.setLastServerPosition(player.getLoc());
	}

	private void correctPosition(final Player player)
	{
		if(player.isGM())
		{
			player.sendMessage("Server loc: " + player.getLoc());
			player.sendMessage("Correcting position...");
		}
		if(_lastServerPosition.x != 0 && _lastServerPosition.y != 0 && _lastServerPosition.z != 0)
		{
			if(GeoEngine.getLowerNSWE(_lastServerPosition.x, _lastServerPosition.y, _lastServerPosition.z, player.getGeoIndex()) == 15)
				player.teleToLocation(_lastServerPosition, player.getReflectionId());
			else
				player.teleToClosestTown();
		}
		else if(_lastClientPosition.x != 0 && _lastClientPosition.y != 0 && _lastClientPosition.z != 0)
		{
			if(GeoEngine.getLowerNSWE(_lastClientPosition.x, _lastClientPosition.y, _lastClientPosition.z, player.getGeoIndex()) == 15)
				player.teleToLocation(_lastClientPosition, player.getReflectionId());
			else
				player.teleToClosestTown();
		}
		else
			player.teleToClosestTown();
	}
}
