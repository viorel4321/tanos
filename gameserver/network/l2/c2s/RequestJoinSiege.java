package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.CastleSiegeAttackerList;
import l2s.gameserver.network.l2.s2c.CastleSiegeDefenderList;

public class RequestJoinSiege extends L2GameClientPacket
{
	private int _id;
	private boolean _isAttacker;
	private boolean _isJoining;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_isAttacker = readD() == 1;
		_isJoining = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}
		if(player.getClan() == null || (player.getClanPrivileges() & 0x20000) != 0x20000)
		{
			player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		final Residence residence = ResidenceHolder.getInstance().getResidence(_id);
		if(residence.getType() == ResidenceType.Castle)
			registerAtCastle(player, (Castle) residence, _isAttacker, _isJoining, false);
		else if(residence.getType() == ResidenceType.ClanHall && _isAttacker)
			registerAtClanHall(player, (ClanHall) residence, _isJoining, false);
	}

	public static void registerAtCastle(final Player player, final Castle castle, final boolean attacker, final boolean join, final boolean gm)
	{
		final CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		if(siegeEvent == null) {
			// TODO: Message??
			return;
		}

		final Clan playerClan = player.getClan();
		SiegeClanObject siegeClan = null;
		if(attacker)
			siegeClan = siegeEvent.getSiegeClan("attackers", playerClan);
		else
		{
			siegeClan = siegeEvent.getSiegeClan("defenders", playerClan);
			if(siegeClan == null)
				siegeClan = siegeEvent.getSiegeClan("defenders_waiting", playerClan);
		}
		if(join)
		{
			Residence registeredCastle = null;
			for(final Residence residence : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			{
				SiegeEvent<?, ?> siegeEvent1 = residence.getSiegeEvent();
				if(siegeEvent1 == null)
					 continue;

				SiegeClanObject tempCastle = siegeEvent1.getSiegeClan("attackers", playerClan);
				if(tempCastle == null)
					tempCastle = siegeEvent1.getSiegeClan("defenders", playerClan);
				if(tempCastle == null)
					tempCastle = siegeEvent1.getSiegeClan("defenders_waiting", playerClan);
				if(tempCastle != null)
					registeredCastle = residence;
			}
			if(attacker)
			{
				if(castle.getOwnerId() == playerClan.getClanId())
				{
					player.sendPacket(Msg.THE_CLAN_THAT_OWNS_THE_CASTLE_IS_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
					return;
				}
				final Alliance alliance = playerClan.getAlliance();
				if(alliance != null)
					for(final Clan clan : alliance.getMembers())
						if(clan.getHasCastle() == castle.getId())
						{
							player.sendPacket(Msg.YOU_CANNOT_REGISTER_ON_THE_ATTACKING_SIDE_BECAUSE_YOU_ARE_PART_OF_AN_ALLIANCE_WITH_THE_CLAN_THAT_OWNS_THE_CASTLE);
							return;
						}
				if(playerClan.getHasCastle() > 0)
				{
					player.sendPacket(Msg.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
					return;
				}
				if(siegeClan != null)
				{
					player.sendPacket(Msg.YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
					return;
				}
				if(playerClan.getLevel() < 4)
				{
					player.sendPacket(Msg.ONLY_CLANS_WITH_LEVEL_4_AND_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
					return;
				}
				if(registeredCastle != null)
				{
					player.sendPacket(Msg.YOU_HAVE_ALREADY_REQUESTED_A_SIEGE_BATTLE);
					return;
				}
				if(!gm && siegeEvent.isRegistrationOver())
				{
					player.sendPacket(Msg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
					return;
				}
				if(castle.getSiegeDate().getTimeInMillis() == 0L)
				{
					player.sendPacket(Msg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
					return;
				}
				final int allSize = siegeEvent.getObjects("attackers").size();
				if(allSize >= 20)
				{
					player.sendPacket(Msg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
					return;
				}
				siegeClan = new SiegeClanObject("attackers", playerClan, 0L);
				siegeEvent.addObject("attackers", siegeClan);
				SiegeClanDAO.getInstance().insert(castle, siegeClan);
				player.sendPacket(new CastleSiegeAttackerList(castle));
			}
			else
			{
				if(castle.getOwnerId() == 0)
					return;
				if(castle.getOwnerId() == playerClan.getClanId())
				{
					player.sendPacket(Msg.THE_CLAN_THAT_OWNS_THE_CASTLE_IS_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
					return;
				}
				if(playerClan.getHasCastle() > 0)
				{
					player.sendPacket(Msg.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
					return;
				}
				if(siegeClan != null)
				{
					player.sendPacket(Msg.YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
					return;
				}
				if(playerClan.getLevel() < 4)
				{
					player.sendPacket(Msg.ONLY_CLANS_WITH_LEVEL_4_AND_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
					return;
				}
				if(registeredCastle != null)
				{
					player.sendPacket(Msg.YOU_HAVE_ALREADY_REQUESTED_A_SIEGE_BATTLE);
					return;
				}
				if(castle.getSiegeDate().getTimeInMillis() == 0L)
				{
					player.sendPacket(Msg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
					return;
				}
				if(!gm && siegeEvent.isRegistrationOver())
				{
					player.sendPacket(Msg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
					return;
				}
				siegeClan = new SiegeClanObject("defenders_waiting", playerClan, 0L);
				siegeEvent.addObject("defenders_waiting", siegeClan);
				SiegeClanDAO.getInstance().insert(castle, siegeClan);
				player.sendPacket(new CastleSiegeDefenderList(castle));
			}
		}
		else
		{
			if(siegeClan == null)
				siegeClan = siegeEvent.getSiegeClan("defenders_refused", playerClan);
			if(siegeClan == null)
			{
				player.sendPacket(Msg.YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE);
				return;
			}
			if(!gm && siegeEvent.isRegistrationOver())
			{
				player.sendPacket(Msg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}
			siegeEvent.removeObject(siegeClan.getType(), siegeClan);
			SiegeClanDAO.getInstance().delete(castle, siegeClan);
			if(siegeClan.getType() == "attackers")
				player.sendPacket(new CastleSiegeAttackerList(castle));
			else
				player.sendPacket(new CastleSiegeDefenderList(castle));
		}
	}

	public static void registerAtClanHall(final Player player, final ClanHall clanHall, final boolean join, final boolean gm)
	{
		final ClanHallSiegeEvent siegeEvent = clanHall.getSiegeEvent();
		if(siegeEvent == null) {
			// TODO: Message??
			return;
		}

		final Clan playerClan = player.getClan();
		SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", playerClan);
		if(join)
		{
			if(playerClan.getHasHideout() > 0)
			{
				player.sendPacket(Msg.A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE);
				return;
			}
			if(siegeClan != null)
			{
				player.sendPacket(Msg.YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
				return;
			}
			if(playerClan.getLevel() < 4)
			{
				player.sendMessage("Only clans who are level 4 or above can register for battle at Devastated Castle and Fortress of the Dead.");
				return;
			}
			if(!gm && siegeEvent.isRegistrationOver())
			{
				player.sendPacket(Msg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}
			final int allSize = siegeEvent.getObjects("attackers").size();
			if(allSize >= 20)
			{
				player.sendPacket(Msg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
				return;
			}
			siegeClan = new SiegeClanObject("attackers", playerClan, 0L);
			siegeEvent.addObject("attackers", siegeClan);
			SiegeClanDAO.getInstance().insert(clanHall, siegeClan);
		}
		else
		{
			if(siegeClan == null)
			{
				player.sendPacket(Msg.YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE);
				return;
			}
			if(!gm && siegeEvent.isRegistrationOver())
			{
				player.sendPacket(Msg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}
			siegeEvent.removeObject(siegeClan.getType(), siegeClan);
			SiegeClanDAO.getInstance().delete(clanHall, siegeClan);
		}
		player.sendPacket(new CastleSiegeAttackerList(clanHall));
	}
}
