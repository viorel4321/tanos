package l2s.gameserver.model.entity.events.actions;

import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.network.l2.s2c.CreatureSay;

public class SayAction implements EventAction
{
	private int _range;
	private int _type;
	private int _how;
	private int _msg;

	protected SayAction(final int range, final int type)
	{
		_range = range;
		_type = type;
	}

	public SayAction(final int range, final int type, final int how, final int msg)
	{
		this(range, type);
		_how = how;
		_msg = msg;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		final List<Player> players = event.broadcastPlayers(_range);
		for(final Player player : players)
			if(player != null)
				player.sendPacket(new CreatureSay(0, _type, _how, _msg));
	}
}
