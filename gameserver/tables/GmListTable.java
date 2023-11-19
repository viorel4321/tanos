package l2s.gameserver.tables;

import java.util.ArrayList;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class GmListTable
{
	public static ArrayList<Player> getAllGMs()
	{
		final ArrayList<Player> gmList = new ArrayList<Player>();
		for(final Player player : GameObjectsStorage.getPlayers())
			if(player.isGM())
				gmList.add(player);
		return gmList;
	}

	public static ArrayList<Player> getAllVisibleGMs()
	{
		final ArrayList<Player> gmList = new ArrayList<Player>();
		for(final Player player : GameObjectsStorage.getPlayers())
			if(player.isGM() && !player.getVarBoolean("NoGMList"))
				gmList.add(player);
		return gmList;
	}

	public static void sendListToPlayer(final Player player)
	{
		ArrayList<Player> gmList = new ArrayList<Player>();
		if(player.isGM())
			gmList = getAllGMs();
		else
			gmList = getAllVisibleGMs();
		if(gmList.isEmpty())
		{
			player.sendPacket(new SystemMessage(702));
			return;
		}
		player.sendPacket(new SystemMessage(703));
		for(final Player gm : gmList)
			player.sendPacket(new SystemMessage(704).addString(gm.getName()));
	}

	public static void broadcastToGMs(final L2GameServerPacket packet)
	{
		for(final Player gm : getAllGMs())
			gm.sendPacket(packet);
	}

	public static void broadcastMessageToGMs(final String message)
	{
		for(final Player gm : getAllGMs())
			gm.sendMessage(message);
	}
}
