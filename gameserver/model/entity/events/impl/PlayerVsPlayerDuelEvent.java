package l2s.gameserver.model.entity.events.impl;

import java.util.List;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.model.entity.events.objects.DuelSnapshotObject;
import l2s.gameserver.network.l2.s2c.ExDuelAskStart;
import l2s.gameserver.network.l2.s2c.ExDuelEnd;
import l2s.gameserver.network.l2.s2c.ExDuelReady;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class PlayerVsPlayerDuelEvent extends DuelEvent
{
	public PlayerVsPlayerDuelEvent(final MultiValueSet<String> set)
	{
		super(set);
	}

	protected PlayerVsPlayerDuelEvent(final int id, final String name)
	{
		super(id, name);
	}

	@Override
	public boolean canDuel(final Player player, final Player target, final boolean first)
	{
		IBroadcastPacket sm = canDuel0(player, target);
		if(sm != null)
		{
			player.sendPacket(sm);
			return false;
		}
		sm = canDuel0(target, player);
		if(sm != null)
		{
			player.sendPacket(Msg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return false;
		}
		return true;
	}

	@Override
	public void askDuel(final Player player, final Player target)
	{
		final Transaction transaction = new Transaction(Transaction.TransactionType.DUEL, player, target, 10000L);
		player.setTransaction(transaction);
		target.setTransaction(transaction);
		player.sendPacket(new SystemMessage(1927).addName(target));
		target.sendPacket(new SystemMessage(1938).addName(player), new ExDuelAskStart(player.getName(), 0));
	}

	@Override
	public void createDuel(final Player player, final Player target)
	{
		final PlayerVsPlayerDuelEvent duelEvent = new PlayerVsPlayerDuelEvent(getDuelType(), player.getObjectId() + "_" + target.getObjectId() + "_duel");
		cloneTo(duelEvent);
		duelEvent.addObject("BLUE", new DuelSnapshotObject(player, 1));
		duelEvent.addObject("RED", new DuelSnapshotObject(target, 2));
		duelEvent.sendPacket(new ExDuelReady(getDuelType()));
		duelEvent.reCalcNextTime(false);
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
				if(!winners.isEmpty())
					this.sendPacket(new SystemMessage(1950).addName(winners.get(0).getPlayer()));
				for(final DuelSnapshotObject d2 : lossers)
					d2.getPlayer().broadcastPacket(new SocialAction(d2.getPlayer().getObjectId(), 7));
				break;
			}
		}
		this.removeObjects("RED");
		this.removeObjects("BLUE");
	}

	@Override
	public void onDie(final Player player)
	{
		final int team = player.getTeam();
		if(team == 0 || _aborted)
			return;
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
		return 0;
	}

	@Override
	public void playerExit(final Player player)
	{
		if(_winner != 0 || _aborted)
			return;
		_winner = player.getTeam() == 1 ? 2 : player.getTeam() == 2 ? 1 : 0;
		_aborted = false;
		stopEvent();
	}

	@Override
	public void packetSurrender(final Player player)
	{
		playerExit(player);
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 5000L;
	}
}
