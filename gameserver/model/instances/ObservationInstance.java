package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public final class ObservationInstance extends NpcInstance
{
	public ObservationInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		super.onBypassFeedback(player, command);
		if(player.getOlympiadGameId() != -1 || Olympiad.isRegistered(player))
		{
			player.sendMessage(player.isLangRus() ? "\u041d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0434\u043b\u044f \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u043e\u0432 \u041e\u043b\u0438\u043c\u043f\u0438\u0430\u0434\u044b." : "You are olympiad participant!");
			return;
		}
		if(command.startsWith("observeSiege"))
		{
			final String val = command.substring(13);
			final StringTokenizer st = new StringTokenizer(val);
			st.nextToken();
			if(ZoneManager.getInstance().checkIfInZone(Zone.ZoneType.Siege, Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())))
				doObserve(player, val);
			else
				player.sendPacket(Msg.OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE);
		}
		else if(command.startsWith("observe"))
			doObserve(player, command.substring(8));
	}

	@Override
	public String getHtmlPath(final int npcId, final int val, final Player player)
	{
		String pom = "";
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "observation/" + pom + ".htm";
	}

	private void doObserve(final Player player, final String val)
	{
		if(player.getTeam() != 0 || player.inEvent)
		{
			player.sendMessage("You are event participant!");
			return;
		}
		final StringTokenizer st = new StringTokenizer(val);
		final int cost = Integer.parseInt(st.nextToken());
		if(player.getAdena() < cost)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else if(player.enterObserverMode(new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()))))
			player.reduceAdena(cost, true);
		player.sendActionFailed();
	}
}
