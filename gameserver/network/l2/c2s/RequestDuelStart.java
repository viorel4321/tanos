package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestDuelStart extends L2GameClientPacket
{
	private String _name;
	private int _duelType;

	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
		_duelType = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInTransaction())
		{
			player.sendPacket(new SystemMessage(164));
			return;
		}
		final Player target = World.getPlayer(_name);
		if(target == null || target == player)
		{
			player.sendPacket(new SystemMessage(1926));
			return;
		}
		final DuelEvent duelEvent = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, _duelType);
		if(duelEvent == null)
			return;
		if(!duelEvent.canDuel(player, target, true))
			return;
		if(target.isBusy())
		{
			player.sendPacket(new SystemMessage(153).addName(target));
			return;
		}
		duelEvent.askDuel(player, target);
	}
}
