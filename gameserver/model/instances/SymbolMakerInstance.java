package l2s.gameserver.model.instances;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.HennaEquipList;
import l2s.gameserver.network.l2.s2c.HennaUnequipList;
import l2s.gameserver.tables.HennaTreeTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SymbolMakerInstance extends NpcInstance
{
	public SymbolMakerInstance(final int objectID, final NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(command.equals("Draw"))
		{
			player.turn(this, 3000);
			player.sendPacket(new HennaEquipList(player, HennaTreeTable.getInstance().getAvailableHenna(player.getClassId())));
		}
		else if(command.equals("RemoveList"))
		{
			player.turn(this, 3000);
			boolean hasHennas = false;
			for(int i = 1; i <= 3; ++i)
				if(player.getHenna(i) != null)
					hasHennas = true;
			if(hasHennas)
				player.sendPacket(new HennaUnequipList(player));
			else
				player.sendPacket(Msg.THE_SYMBOL_INFORMATION_CANNOT_BE_FOUND);
		}
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
		return "symbolmaker/" + pom + ".htm";
	}
}
