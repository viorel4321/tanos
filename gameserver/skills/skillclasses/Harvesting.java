package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class Harvesting extends Skill
{
	public Harvesting(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;
		final Player player = (Player) activeChar;
		for(final Creature target : targets)
			if(target != null)
			{
				if(!target.isMonster())
					continue;
				final MonsterInstance monster = (MonsterInstance) target;
				if(!monster.isSeeded())
					activeChar.sendPacket(Msg.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
				else if(!monster.isSeeded(player))
					activeChar.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
				else
				{
					double SuccessRate = Config.MANOR_HARVESTING_BASIC_SUCCESS;
					final int diffPlayerTarget = Math.abs(activeChar.getLevel() - monster.getLevel());
					if(diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET)
						SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
					if(SuccessRate < 1.0)
						SuccessRate = 1.0;
					if(Config.SKILLS_SHOW_CHANCE && player.getVarBoolean("SkillsChance"))
						player.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Harvesting.Chance").addNumber((int) SuccessRate));
					if(!Rnd.chance(SuccessRate))
					{
						activeChar.sendPacket(Msg.THE_HARVEST_HAS_FAILED);
						monster.takeHarvest();
					}
					else
					{
						ItemInstance item = monster.takeHarvest();
						if(item == null)
							System.out.println("Harvesting :: monster.takeHarvest() == null :: monster == " + monster);
						else
						{
							final long itemCount = item.getCount();
							item = player.getInventory().addItem(item);
							player.sendPacket(new SystemMessage(1137).addString("You").addNumber(Long.valueOf(itemCount)).addItemName(Integer.valueOf(item.getItemId())));
							if(!player.isInParty())
								continue;
							final SystemMessage smsg = new SystemMessage(1137).addString(player.getName()).addNumber(Long.valueOf(itemCount)).addItemName(Integer.valueOf(item.getItemId()));
							player.getParty().broadcastToPartyMembers(player, smsg);
						}
					}
				}
			}
	}
}
