package l2s.gameserver.network.l2.c2s;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.Die;
import l2s.gameserver.network.l2.s2c.Revive;
import l2s.gameserver.tables.MapRegionTable;
import l2s.gameserver.utils.Location;

public class RequestRestartPoint extends L2GameClientPacket
{
	private RestartType _restartType;

	@Override
	protected void readImpl()
	{
		_restartType = ArrayUtils.valid(RestartType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(_restartType == null || activeChar == null)
			return;
		if(activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}
		if(!activeChar.isDead() && !activeChar.isGM() || activeChar.isInOlympiadMode())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFestivalParticipant())
		{
			activeChar.doRevive(true);
			return;
		}
		if(activeChar.inEvent)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0414\u043e\u0436\u0434\u0438\u0442\u0435\u0441\u044c \u0432\u043e\u0441\u043a\u0440\u0435\u0448\u0435\u043d\u0438\u044f \u044d\u0432\u0435\u043d\u0442\u043e\u043c." : "Wait resurrection by event.");
			activeChar.sendActionFailed();
			return;
		}
		switch(_restartType)
		{
			case FIXED:
			{
				if(activeChar.getPlayerAccess().ResurectFixed)
				{
					activeChar.doRevive(100.0);
					break;
				}
				activeChar.sendPacket(Msg.ActionFail, new Die(activeChar));
				break;
			}
			default:
			{
				Location loc = null;
				for(final GlobalEvent e : activeChar.getEvents())
					loc = e.getRestartLoc(activeChar, _restartType);
				if(loc == null)
					loc = defaultLoc(_restartType, activeChar);
				if(loc != null)
				{
					activeChar.broadcastPacket(new Revive(activeChar));
					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(loc);
					break;
				}
				activeChar.sendPacket(Msg.ActionFail, new Die(activeChar));
				break;
			}
		}
	}

	public static Location defaultLoc(final RestartType restartType, final Player activeChar)
	{
		Location loc = null;
		final Clan clan = activeChar.getClan();
		switch(restartType)
		{
			case TO_CLANHALL:
			{
				if(clan != null && clan.getHasHideout() != 0)
				{
					final ClanHall clanHall = activeChar.getClanHall();
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);
					if(clanHall.getFunction(5) != null)
						activeChar.restoreExp(clanHall.getFunction(5).getLevel());
					break;
				}
				break;
			}
			case TO_CASTLE:
			{
				if(clan != null && clan.getHasCastle() != 0)
				{
					final Castle castle = activeChar.getCastle();
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
					if(castle.getFunction(5) != null)
						activeChar.restoreExp(castle.getFunction(5).getLevel());
					break;
				}
				break;
			}
			case TO_FLAG:
			{
				if(Config.ALLOW_PVP_ZONES_MOD && org.apache.commons.lang3.ArrayUtils.contains(Config.PVP_ZONES_MOD, activeChar.getZoneIndex(Zone.ZoneType.battle_zone)))
				{
					final Zone battle = activeChar.getZone(Zone.ZoneType.battle_zone);
					if(battle != null)
						loc = battle.getAdvSpawn();
					break;
				}
				break;
			}
			default:
			{
				loc = MapRegionTable.getTeleToClosestTown(activeChar);
				break;
			}
		}
		return loc;
	}
}
