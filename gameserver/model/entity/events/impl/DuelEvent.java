package l2s.gameserver.model.entity.events.impl;

import java.util.Iterator;
import java.util.List;

import l2s.commons.collections.JoinedIterator;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.listener.actor.OnPlayerExitListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.events.objects.DuelSnapshotObject;
import l2s.gameserver.network.l2.s2c.ExDuelStart;
import l2s.gameserver.network.l2.s2c.ExDuelUpdateUserInfo;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public abstract class DuelEvent extends GlobalEvent implements Iterable<DuelSnapshotObject>
{
	public static final String RED_TEAM = "RED";
	public static final String BLUE_TEAM = "BLUE";
	protected OnPlayerExitListener _playerExitListener;
	protected int _winner;
	protected boolean _aborted;

	public DuelEvent(final MultiValueSet<String> set)
	{
		super(set);
		_playerExitListener = new OnPlayerExitListenerImpl();
		_winner = 0;
	}

	protected DuelEvent(final int id, final String name)
	{
		super(id, name);
		_playerExitListener = new OnPlayerExitListenerImpl();
		_winner = 0;
	}

	@Override
	public void initEvent()
	{}

	public abstract boolean canDuel(final Player p0, final Player p1, final boolean p2);

	public abstract void askDuel(final Player p0, final Player p1);

	public abstract void createDuel(final Player p0, final Player p1);

	public abstract void playerExit(final Player p0);

	public abstract void packetSurrender(final Player p0);

	public abstract void onDie(final Player p0);

	public abstract int getDuelType();

	@Override
	public void startEvent()
	{
		updatePlayers(true, false);
		sendPackets(new ExDuelStart(getDuelType()), new PlaySound("B04_S01"), Msg.LET_THE_DUEL_BEGIN);
		for(final DuelSnapshotObject player : this)
		{
			checkPlayerIsInPiace();
			this.sendPacket(new ExDuelUpdateUserInfo(player.getPlayer()), player.getTeam() == 1 ? "RED" : "BLUE");
		}
	}

	public void sendPacket(final IBroadcastPacket packet, final String... ar)
	{
		for(final String a : ar)
		{
			final List<DuelSnapshotObject> objs = this.getObjects(a);
			for(final DuelSnapshotObject obj : objs)
				obj.getPlayer().sendPacket(packet);
		}
	}

	public void sendPacket(final IBroadcastPacket packet)
	{
		sendPackets(packet);
	}

	public void sendPackets(final IBroadcastPacket... packet)
	{
		for(final DuelSnapshotObject d : this)
			d.getPlayer().sendPacket(packet);
	}

	public void abortDuel(final Player player)
	{
		_aborted = true;
		_winner = 0;
		stopEvent();
	}

	protected IBroadcastPacket canDuel0(final Player requestor, final Player target)
	{
		IBroadcastPacket packet = null;
		if(target.isInCombat())
			packet = new SystemMessage(2021).addName(target);
		else if(target.isAlikeDead() || target.getCurrentHpPercents() < 50.0 || target.getCurrentMpPercents() < 50.0)
			packet = new SystemMessage(2019).addName(target);
		else if(target.getEvent(DuelEvent.class) != null)
			packet = new SystemMessage(2022).addName(target);
		else if(target.getEvent(ClanHallSiegeEvent.class) != null || target.getEvent(ClanHallNpcSiegeEvent.class) != null)
			packet = new SystemMessage(2025).addName(target);
		else if(target.getEvent(SiegeEvent.class) != null)
			packet = new SystemMessage(2026).addName(target);
		else if(target.isInOlympiadMode())
			packet = new SystemMessage(2024).addName(target);
		else if(target.isCursedWeaponEquipped() || target.getKarma() > 0 || target.getPvpFlag() > 0)
			packet = new SystemMessage(2023).addName(target);
		else if(target.isInStoreMode())
			packet = new SystemMessage(2017).addName(target);
		else if(target.isMounted() || target.isInVehicle())
			packet = new SystemMessage(2027).addName(target);
		else if(target.isFishing())
			packet = new SystemMessage(2018).addName(target);
		else if(target.isInCombatZone() || target.isInPeaceZone() || target.isInWater() || target.isInZone(Zone.ZoneType.no_restart))
			packet = new SystemMessage(2020).addName(target);
		else if(!requestor.isInRangeZ(target, 1200L))
			packet = new SystemMessage(2028).addName(target);
		return packet;
	}

	protected void updatePlayers(final boolean start, final boolean teleport)
	{
		for(final DuelSnapshotObject player : this)
			if(teleport)
				player.teleport();
			else if(start)
			{
				player.getPlayer().addEvent(this);
				player.getPlayer().setTeam(player.getTeam(), false);
			}
			else
			{
				player.getPlayer().abortAttack(true, false);
				if(player.getPlayer().getAI() != null)
					player.getPlayer().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				player.getPlayer().removeEvent(this);
				player.restore(_aborted);
				player.getPlayer().setCanUseSelectedSub(false);
				player.getPlayer().setTeam(0, false);
			}
	}

	@Override
	public SystemMessage checkForAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force)
	{
		if(target.getTeam() == 0 || attacker.getTeam() == 0 || target.getTeam() == attacker.getTeam())
			return Msg.INCORRECT_TARGET;
		final DuelEvent duelEvent = target.getEvent(DuelEvent.class);
		if(duelEvent == null || duelEvent != this)
			return Msg.INCORRECT_TARGET;
		return null;
	}

	@Override
	public boolean canAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force, final boolean nextAttackCheck)
	{
		if(target.getTeam() == 0 || attacker.getTeam() == 0 || target.getTeam() == attacker.getTeam())
			return false;
		final DuelEvent duelEvent = target.getEvent(DuelEvent.class);
		return duelEvent != null && duelEvent == this;
	}

	@Override
	public void onAddEvent(final GameObject o)
	{
		if(o.isPlayer())
			o.getPlayer().addListener(_playerExitListener);
	}

	@Override
	public void onRemoveEvent(final GameObject o)
	{
		if(o.isPlayer())
			o.getPlayer().removeListener(_playerExitListener);
	}

	@Override
	public Iterator<DuelSnapshotObject> iterator()
	{
		final List<DuelSnapshotObject> blue = this.getObjects("BLUE");
		final List<DuelSnapshotObject> red = this.getObjects("RED");
		return new JoinedIterator<DuelSnapshotObject>(blue.iterator(), red.iterator());
	}

	@Override
	public void reCalcNextTime(final boolean onInit)
	{
		registerActions();
	}

	@Override
	public void announce(final int i)
	{
		checkPlayerIsInPiace();
		this.sendPacket(new SystemMessage(1945).addNumber(Integer.valueOf(i)));
	}

	private void checkPlayerIsInPiace()
	{
		for(final DuelSnapshotObject player : this)
			if(player.getPlayer().isInPeaceZone())
				abortDuel(player.getPlayer());
	}

	private class OnPlayerExitListenerImpl implements OnPlayerExitListener
	{
		@Override
		public void onPlayerExit(final Player player)
		{
			playerExit(player);
		}
	}
}
