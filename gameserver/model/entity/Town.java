package l2s.gameserver.model.entity;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.TownManager;
import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.utils.Location;

public class Town
{
	protected static Logger _log;
	private int _CastleIndex;
	private String _Name;
	private int _RedirectToTownId;
	private int _TownId;
	private Zone _Zone;

	public Town(final int townId)
	{
		_CastleIndex = 0;
		_Name = "";
		_RedirectToTownId = 0;
		_TownId = 0;
		_TownId = townId;
		loadData();
	}

	public boolean checkIfInZone(final GameObject obj)
	{
		return this.checkIfInZone(obj.getX(), obj.getY());
	}

	public boolean checkIfInZone(final int x, final int y)
	{
		return _Zone.checkIfInZone(x, y);
	}

	private void loadData()
	{
		_Zone = ZoneManager.getInstance().getZoneByIndex(Zone.ZoneType.Town, _TownId, true);
		if(_Zone != null)
		{
			_CastleIndex = _Zone.getTaxById();
			_Name = _Zone.getName();
		}
		switch(_TownId)
		{
			case 6:
			{
				_RedirectToTownId = 7;
				break;
			}
			case 8:
			{
				_RedirectToTownId = 8;
				break;
			}
			case 9:
			{
				_RedirectToTownId = 19;
				break;
			}
			case 10:
			{
				_RedirectToTownId = 12;
				break;
			}
			case 11:
			{
				_RedirectToTownId = 10;
				break;
			}
			case 13:
			{
				_RedirectToTownId = 19;
				break;
			}
			case 15:
			{
				_RedirectToTownId = 14;
				break;
			}
			case 14:
			{
				_RedirectToTownId = 16;
				break;
			}
			case 16:
			{
				_RedirectToTownId = 4;
				break;
			}
			default:
			{
				_RedirectToTownId = getTownId();
				break;
			}
		}
	}

	public final Castle getCastle()
	{
		return ResidenceHolder.getInstance().getResidence(Castle.class, _CastleIndex);
	}

	public final int getCastleIndex()
	{
		return _CastleIndex;
	}

	public final String getName()
	{
		return _Name;
	}

	public final Location getSpawn()
	{
		Castle castle = getCastle();
		SiegeEvent<?, ?> siegeEvent = castle != null ? castle.getSiegeEvent() : null;
		if(Config.ALLOW_SEVEN_SIGNS && SevenSigns.getInstance().getSealOwner(3) == 2 && _RedirectToTownId != getTownId() && siegeEvent != null && siegeEvent.isInProgress())
			return TownManager.getInstance().getTown(_RedirectToTownId).getSpawn();
		return _Zone.getSpawn();
	}

	public final Location getPKSpawn()
	{
		Castle castle = getCastle();
		SiegeEvent<?, ?> siegeEvent = castle != null ? castle.getSiegeEvent() : null;
		if(Config.ALLOW_SEVEN_SIGNS && SevenSigns.getInstance().getSealOwner(3) == 2 && _RedirectToTownId != getTownId() && siegeEvent != null && siegeEvent.isInProgress())
			return TownManager.getInstance().getTown(_RedirectToTownId).getPKSpawn();
		return _Zone.getPKSpawn();
	}

	public final int getRedirectToTownId()
	{
		return _RedirectToTownId;
	}

	public final int getTownId()
	{
		return _TownId;
	}

	public final Zone getZone()
	{
		return _Zone;
	}

	static
	{
		Town._log = LoggerFactory.getLogger(Town.class);
	}
}
