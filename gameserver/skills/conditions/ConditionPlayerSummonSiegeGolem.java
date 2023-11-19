package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Env;

public class ConditionPlayerSummonSiegeGolem extends Condition
{
	@Override
	protected boolean testImpl(final Env env)
	{
		final Player player = env.character.getPlayer();
		if(player == null)
			return false;
		final SiegeEvent<?, ?> event = player.getEvent(SiegeEvent.class);
		if(!(event instanceof CastleSiegeEvent))
		{
			player.sendPacket(new SystemMessage(16));
			return false;
		}
		Zone zone = player.getZone(Zone.ZoneType.RESIDENCE);
		if(zone != null)
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u0437\u043e\u043d\u0435 \u0440\u0435\u0437\u0438\u0434\u0435\u043d\u0446\u0438\u0438." : "You are in the residence zone.");
			return false;
		}
		zone = player.getZone(Zone.ZoneType.Siege);
		if(zone == null)
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432\u043d\u0435 \u0437\u043e\u043d\u044b \u043e\u0441\u0430\u0434\u044b." : "You are out of the siege zone.");
			return false;
		}
		if(zone.getIndex() != event.getId())
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0432 \u0434\u0440\u0443\u0433\u043e\u0439 \u0437\u043e\u043d\u0435 \u043e\u0441\u0430\u0434\u044b." : "You are in another siege zone.");
			return false;
		}
		if(event.getSiegeClan("attackers", player.getClan()) == null)
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u0430\u0448 \u043a\u043b\u0430\u043d \u043d\u0435 \u0432 \u0447\u0438\u0441\u043b\u0435 \u0430\u0442\u0430\u043a\u0443\u044e\u0449\u0438\u0445." : "Your clan not attackers.");
			return false;
		}
		return true;
	}
}
