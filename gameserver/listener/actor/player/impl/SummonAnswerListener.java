package l2s.gameserver.listener.actor.player.impl;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.Config;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.skillclasses.Call;
import l2s.gameserver.utils.Location;

public class SummonAnswerListener implements OnAnswerListener
{
	private final HardReference<Player> _playerRef;
	private final Location _location;
	private final long _count;

	public SummonAnswerListener(Player player, Location loc, long count)
	{
		_playerRef = player.getRef();
		_location = loc;
		_count = count;
	}

	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;

		if(Config.NO_SUMMON_KARMA && player.getKarma() > 0)
		{
			player.sendMessage(player.isLangRus() ? "\u041d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0432 \u043a\u0430\u0440\u043c\u0435." : "Impossible with karma.");
			return;
		}
		if(Call.canBeSummoned(player, false) != null)
		{
			player.sendMessage(player.isLangRus() ? "\u041d\u0435\u043f\u043e\u0434\u0445\u043e\u0434\u044f\u0449\u0438\u0435 \u0443\u0441\u043b\u043e\u0432\u0438\u044f." : "Unsuitable conditions.");
			return;
		}
		player.abortAttack(true, false);
		player.abortCast(true, false);
		player.stopMove();
		if(_count > 0L)
		{
			ItemInstance item = player.getInventory().getItemByItemId(8615);
			if(item != null && item.getCount() >= _count)
			{
				player.getInventory().destroyItem(item, _count, false);
				player.sendPacket(SystemMessage.removeItems(8615, _count));
				player.teleToLocation(_location);
			}
			else
				player.sendPacket(new SystemMessage(351));
		}
		else
			player.teleToLocation(_location);
	}

	@Override
	public void sayNo()
	{
		//
	}
}
