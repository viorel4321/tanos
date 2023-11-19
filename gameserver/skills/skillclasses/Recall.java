package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.TownManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.MapRegionTable;
import l2s.gameserver.templates.StatsSet;

public class Recall extends Skill
{
	private final int _townId;
	private final boolean _clanhall;
	private final boolean _castle;

	public Recall(final StatsSet set)
	{
		super(set);
		_townId = set.getInteger("townId", 0);
		_clanhall = set.getBool("clanhall", false);
		_castle = set.getBool("castle", false);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(getHitTime() == 200)
		{
			final Player player = activeChar.getPlayer();
			if(_clanhall)
			{
				if(player.getClan() == null || player.getClan().getHasHideout() == 0)
				{
					activeChar.sendPacket(new SystemMessage(113).addItemName(Integer.valueOf(_itemConsumeId[0])));
					return false;
				}
			}
			else if(_castle && (player.getClan() == null || player.getClan().getHasCastle() == 0))
			{
				activeChar.sendPacket(new SystemMessage(113).addItemName(Integer.valueOf(_itemConsumeId[0])));
				return false;
			}
		}
		if(activeChar.isPlayer())
		{
			final Player p = (Player) activeChar;
			if(p.isInDuel())
			{
				activeChar.sendMessage(new CustomMessage("common.RecallInDuel"));
				return false;
			}
			if(p.getTeam() != 0)
			{
				activeChar.sendMessage("You can't use teleport in event.");
				return false;
			}
			if(p.getVar("jailed") != null)
			{
				activeChar.sendMessage("You can't use teleport in jail.");
				return false;
			}
		}
		if(activeChar.isInZone(Zone.ZoneType.no_escape))
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Recall.Here"));
			return false;
		}
		if(activeChar.getReflectionId() != 0)
		{
			activeChar.sendMessage("You can't use teleport in instance.");
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				final Player pcTarget = target.getPlayer();
				if(pcTarget == null)
					continue;
				if(pcTarget.isFestivalParticipant())
					activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Recall.Festival"));
				else
				{
					if(pcTarget.isInOlympiadMode())
					{
						activeChar.sendPacket(new SystemMessage(1509));
						return;
					}
					if(pcTarget.isInDuel())
					{
						activeChar.sendMessage(new CustomMessage("common.RecallInDuel"));
						return;
					}
					if(pcTarget.getTeam() != 0)
					{
						activeChar.sendMessage("Impossible teleport in event.");
						return;
					}
					if(pcTarget.isFlying())
					{
						activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Recall.Flying"));
						return;
					}
					if(pcTarget.getVar("jailed") != null)
					{
						activeChar.sendMessage(pcTarget.getName() + " is in jail.");
						return;
					}
					if(pcTarget.getReflectionId() != 0)
					{
						activeChar.sendMessage("Impossible teleport in instance.");
						return;
					}
					ThreadPoolManager.getInstance().schedule(new Runnable(){
						@Override
						public void run()
						{
							target.abortAttack(true, true);
							target.abortCast(true, false);
							target.stopMove();
							if(Config.UNSTUCK_TOWN > 0 && (_id == 2099 || _id == 2100) && (!Config.NO_TO_TOWN_PK || pcTarget.getKarma() < 1))
							{
								pcTarget.teleToLocation(TownManager.getInstance().getTown(Config.UNSTUCK_TOWN).getSpawn());
								return;
							}
							if(_isItemHandler)
							{
								if(_itemConsumeId[0] == 7125)
								{
									pcTarget.teleToLocation(17144, 170156, -3502);
									return;
								}
								if(_itemConsumeId[0] == 7127)
								{
									pcTarget.teleToLocation(105918, 109759, -3207);
									return;
								}
								if(_itemConsumeId[0] == 7130)
								{
									pcTarget.teleToLocation(85475, 16087, -3672);
									return;
								}
								if(_itemConsumeId[0] == 7618)
								{
									pcTarget.teleToLocation(149864, -81062, -5618);
									return;
								}
								if(_itemConsumeId[0] == 7619)
								{
									pcTarget.teleToLocation(108275, -53785, -2524);
									return;
								}
							}
							if(_townId > 0)
							{
								pcTarget.teleToLocation(TownManager.getInstance().getTown(_townId).getSpawn());
								return;
							}
							if(_castle)
							{
								pcTarget.teleToLocation(MapRegionTable.getInstance().getTeleToLocation(pcTarget, MapRegionTable.TeleportWhereType.Castle));
								return;
							}
							if(_clanhall)
							{
								pcTarget.teleToLocation(MapRegionTable.getInstance().getTeleToLocation(pcTarget, MapRegionTable.TeleportWhereType.ClanHall));
								return;
							}
							if(target.isInZone(Zone.ZoneType.battle_zone) && target.getZone(Zone.ZoneType.battle_zone).getRestartPoints() != null)
							{
								target.teleToLocation(target.getZone(Zone.ZoneType.battle_zone).getSpawn());
								return;
							}
							if(target.isInZone(Zone.ZoneType.peace_zone) && target.getZone(Zone.ZoneType.peace_zone).getRestartPoints() != null)
							{
								target.teleToLocation(target.getZone(Zone.ZoneType.peace_zone).getSpawn());
								return;
							}
							target.teleToClosestTown();
						}
					}, 100L);
				}
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
