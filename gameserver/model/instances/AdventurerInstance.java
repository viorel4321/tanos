package l2s.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.RadarControl;
import l2s.gameserver.templates.npc.NpcTemplate;

public class AdventurerInstance extends NpcInstance
{
	private static Logger _log;

	public AdventurerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		if(command.startsWith("npcfind_byid"))
			try
			{
				final int bossId = Integer.parseInt(command.substring(12).trim());
				switch(RaidBossSpawnManager.getInstance().getRaidBossStatusId(bossId))
				{
					case ALIVE:
					case DEAD:
					{
						final Spawn spawn = RaidBossSpawnManager.getInstance().getSpawnTable().get(bossId);
						player.sendPacket(new RadarControl(2, 2, spawn.getLoc()), new RadarControl(0, 1, spawn.getLoc()));
						break;
					}
					case UNDEFINED:
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.AdventurerInstance.BossNotInGame").addNumber(bossId));
						break;
					}
				}
			}
			catch(NumberFormatException e)
			{
				AdventurerInstance._log.warn("AdventurerInstance: Invalid Bypass to Server command parameter.");
			}
		else if(command.startsWith("raidInfo"))
		{
			final int bossLevel = Integer.parseInt(command.substring(9).trim());
			String filename = "adventurer_guildsman/raid_info/info.htm";
			if(bossLevel != 0)
				filename = "adventurer_guildsman/raid_info/level" + bossLevel + ".htm";
			this.showChatWindow(player, filename, new Object[0]);
		}
		else if(command.equalsIgnoreCase("questlist"))
			player.sendPacket(Msg.ExShowQuestInfo);
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(final int npcId, final int val, final Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "adventurer_guildsman/" + pom + ".htm";
	}

	static
	{
		AdventurerInstance._log = LoggerFactory.getLogger(AdventurerInstance.class);
	}
}
