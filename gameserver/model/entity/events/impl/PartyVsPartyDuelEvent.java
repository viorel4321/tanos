package l2s.gameserver.model.entity.events.impl;

import java.util.List;

import l2s.commons.collections.CollectionUtils;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.model.entity.events.objects.DuelSnapshotObject;
import l2s.gameserver.network.l2.s2c.ExDuelAskStart;
import l2s.gameserver.network.l2.s2c.ExDuelEnd;
import l2s.gameserver.network.l2.s2c.ExDuelReady;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class PartyVsPartyDuelEvent extends DuelEvent
{
	public PartyVsPartyDuelEvent(final MultiValueSet<String> set)
	{
		super(set);
	}

	protected PartyVsPartyDuelEvent(final int id, final String name)
	{
		super(id, name);
	}

	@Override
	public void stopEvent()
	{
		clearActions();
		updatePlayers(false, false);
		for(final DuelSnapshotObject d : this)
		{
			d.getPlayer().sendPacket(new ExDuelEnd(getDuelType()));
			final GameObject target = d.getPlayer().getTarget();
			if(target != null)
				d.getPlayer().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, target);
		}
		switch(_winner)
		{
			case 0:
			{
				this.sendPacket(Msg.THE_DUEL_HAS_ENDED_IN_A_TIE);
				break;
			}
			case 1:
			case 2:
			{
				final List<DuelSnapshotObject> winners = this.getObjects(_winner == 1 ? "BLUE" : "RED");
				final List<DuelSnapshotObject> lossers = this.getObjects(_winner == 1 ? "RED" : "BLUE");
				final DuelSnapshotObject winner = CollectionUtils.safeGet(winners, 0);
				if(winner != null)
				{
					this.sendPacket(new SystemMessage(1951).addName(winners.get(0).getPlayer()));
					for(final DuelSnapshotObject d2 : lossers)
						d2.getPlayer().broadcastPacket(new SocialAction(d2.getPlayer().getObjectId(), 7));
					break;
				}
				this.sendPacket(Msg.THE_DUEL_HAS_ENDED_IN_A_TIE);
				break;
			}
		}
		updatePlayers(false, true);
		this.removeObjects("RED");
		this.removeObjects("BLUE");
	}

	@Override
	public void teleportPlayers(final String name)
	{
		final int x = -102495;
		final int y = -209023;
		final int z = -3326;
		int offset = 0;
		List<DuelSnapshotObject> team = this.getObjects("BLUE");
		for(int i = 0; i < team.size(); ++i)
		{
			final DuelSnapshotObject $member = team.get(i);
			$member.getPlayer().addEvent(this);
			$member.getPlayer()._stablePoint = $member.getLoc();
			$member.getPlayer().teleToLocation(x + offset - 180, y - 150, z);
			offset += 40;
		}
		team = this.getObjects("RED");
		offset = 0;
		for(int i = 0; i < team.size(); ++i)
		{
			final DuelSnapshotObject $member = team.get(i);
			$member.getPlayer().addEvent(this);
			$member.getPlayer()._stablePoint = $member.getLoc();
			$member.getPlayer().teleToLocation(x + offset - 180, y + 150, z);
			offset += 40;
		}
	}

	@Override
	public boolean canDuel(final Player player, final Player target, final boolean first)
	{
		if(player.getParty() == null)
		{
			player.sendPacket(Msg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return false;
		}
		if(target.getParty() == null)
		{
			player.sendPacket(Msg.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
			return false;
		}
		final Party party1 = player.getParty();
		final Party party2 = target.getParty();
		if(player != party1.getPartyLeader() || target != party2.getPartyLeader())
		{
			player.sendPacket(Msg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return false;
		}
		for(final Player $member : party1.getPartyMembers())
		{
			IBroadcastPacket packet = null;
			if((packet = canDuel0(player, $member)) != null)
			{
				player.sendPacket(packet);
				target.sendPacket(packet);
				return false;
			}
		}
		for(final Player $member : party2.getPartyMembers())
		{
			IBroadcastPacket packet = null;
			if((packet = canDuel0(player, $member)) != null)
			{
				player.sendPacket(packet);
				target.sendPacket(packet);
				return false;
			}
		}
		return true;
	}

	@Override
	public void askDuel(final Player player, final Player target)
	{
		final Transaction transaction = new Transaction(Transaction.TransactionType.DUEL, player, target, 10000L);
		player.setTransaction(transaction);
		target.setTransaction(transaction);
		player.sendPacket(new SystemMessage(1928).addName(target));
		target.sendPacket(new SystemMessage(1939).addName(player), new ExDuelAskStart(player.getName(), 1));
	}

	@Override
	public void createDuel(final Player player, final Player target)
	{
		final PartyVsPartyDuelEvent duelEvent = new PartyVsPartyDuelEvent(getDuelType(), player.getObjectId() + "_" + target.getObjectId() + "_duel");
		cloneTo(duelEvent);
		for(final Player $member : player.getParty().getPartyMembers())
			duelEvent.addObject("BLUE", new DuelSnapshotObject($member, 1));
		for(final Player $member : target.getParty().getPartyMembers())
			duelEvent.addObject("RED", new DuelSnapshotObject($member, 2));
		duelEvent.sendPacket(new ExDuelReady(getDuelType()));
		duelEvent.reCalcNextTime(false);
	}

	@Override
	public void playerExit(final Player player)
	{
		for(final DuelSnapshotObject $snapshot : this)
		{
			if($snapshot.getPlayer() == player)
				removeObject($snapshot.getTeam() == 1 ? "BLUE" : "RED", $snapshot);
			final List<DuelSnapshotObject> objects = this.getObjects($snapshot.getTeam() == 1 ? "BLUE" : "RED");
			if(objects.isEmpty())
			{
				_winner = $snapshot.getTeam() == 1 ? 2 : $snapshot.getTeam() == 2 ? 1 : 0;
				stopEvent();
			}
		}
	}

	@Override
	public void packetSurrender(final Player player)
	{}

	@Override
	public void onDie(final Player player)
	{
		final int team = player.getTeam();
		if(team == 0 || _aborted)
			return;
		this.sendPacket(Msg.THE_OTHER_PARTY_IS_FROZEN_PLEASE_WAIT_A_MOMENT, team == 2 ? "BLUE" : "RED");
		player.stopAttackStanceTask();
		player.block();
		player.setTeam(0, false);
		for(final Player $player : World.getAroundPlayers(player))
		{
			$player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, player);
			if(player.getServitor() != null)
				$player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, player.getServitor());
		}
		player.sendChanges();
		boolean allDead = true;
		final List<DuelSnapshotObject> objs = this.getObjects(team == 1 ? "BLUE" : "RED");
		for(final DuelSnapshotObject obj : objs)
		{
			if(obj.getPlayer() == player)
				obj.setDead();
			if(!obj.isDead())
				allDead = false;
		}
		if(allDead)
		{
			_winner = team == 1 ? 2 : team == 2 ? 1 : 0;
			stopEvent();
		}
	}

	@Override
	public int getDuelType()
	{
		return 1;
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 30000L;
	}
}
