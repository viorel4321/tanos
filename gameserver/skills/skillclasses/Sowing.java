package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.StatsSet;

public class Sowing extends Skill
{
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	public Sowing(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;
		final Player player = (Player) activeChar;
		final int seed_id = player.getUseSeed();
		final ItemInstance seedItem = player.getInventory().getItemByItemId(seed_id);
		if(seedItem != null)
		{
			player.sendPacket(SystemMessage.removeItems(seed_id, 1L));
			player.getInventory().destroyItem(seedItem, 1L, true);
			for(final Creature target : targets)
				if(target != null)
				{
					final MonsterInstance monster = (MonsterInstance) target;
					if(monster.isSeeded())
						continue;
					double SuccessRate = Config.MANOR_SOWING_BASIC_SUCCESS;
					final double diffPlayerTarget = Math.abs(activeChar.getLevel() - target.getLevel());
					final double diffSeedTarget = Math.abs(Manor.getInstance().getSeedLevel(seed_id) - target.getLevel());
					if(diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET)
						SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
					if(diffSeedTarget > Config.MANOR_DIFF_SEED_TARGET)
						SuccessRate -= (diffSeedTarget - Config.MANOR_DIFF_SEED_TARGET) * Config.MANOR_DIFF_SEED_TARGET_PENALTY;
					if(ItemTable.getInstance().getTemplate(seed_id).isAltSeed())
						SuccessRate *= Config.MANOR_SOWING_ALT_BASIC_SUCCESS / Config.MANOR_SOWING_BASIC_SUCCESS;
					if(SuccessRate < 1.0)
						SuccessRate = 1.0;
					if(Config.SKILLS_SHOW_CHANCE && player.getVarBoolean("SkillsChance"))
						player.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Sowing.Chance").addNumber((int) SuccessRate));
					if(Rnd.chance(SuccessRate))
					{
						monster.setSeeded(seedItem.getTemplate(), player);
						activeChar.sendPacket(new SystemMessage(889));
					}
					else
						activeChar.sendPacket(new SystemMessage(890));
				}
			return;
		}
		activeChar.sendPacket(Msg.SYSTEM_ERROR);
	}
}
