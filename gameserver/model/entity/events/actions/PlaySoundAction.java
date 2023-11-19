package l2s.gameserver.model.entity.events.actions;

import java.util.List;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.network.l2.s2c.PlaySound;

public class PlaySoundAction implements EventAction
{
	private int _range;
	private String _sound;
	private PlaySound.Type _type;

	public PlaySoundAction(final int range, final String s, final PlaySound.Type type)
	{
		_range = range;
		_sound = s;
		_type = type;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		final GameObject object = event.getCenterObject();
		PlaySound packet = null;
		if(object != null)
			packet = new PlaySound(_type, _sound, 1, object.getObjectId(), object.getLoc());
		else
			packet = new PlaySound(_type, _sound, 0, 0, null);
		final List<Player> players = event.broadcastPlayers(_range);
		for(final Player player : players)
			if(player != null)
				player.sendPacket(packet);
	}
}
