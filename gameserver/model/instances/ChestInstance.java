package l2s.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ChestInstance extends MonsterInstance
{
	private static final Logger _log;

	public ChestInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	public void chestTrap(final Player player)
	{
		int trapSkillId = 0;
		final int rnd = Rnd.get(120);
		if(getLevel() >= 61)
		{
			if(rnd >= 90)
				trapSkillId = 4139;
			else if(rnd >= 50)
				trapSkillId = 4118;
			else if(rnd >= 20)
				trapSkillId = 1167;
			else
				trapSkillId = 223;
		}
		else if(getLevel() >= 41)
		{
			if(rnd >= 90)
				trapSkillId = 4139;
			else if(rnd >= 60)
				trapSkillId = 96;
			else if(rnd >= 20)
				trapSkillId = 1167;
			else
				trapSkillId = 4118;
		}
		else if(getLevel() >= 21)
		{
			if(rnd >= 80)
				trapSkillId = 4139;
			else if(rnd >= 50)
				trapSkillId = 96;
			else if(rnd >= 20)
				trapSkillId = 1167;
			else
				trapSkillId = 129;
		}
		else if(rnd >= 80)
			trapSkillId = 4139;
		else if(rnd >= 50)
			trapSkillId = 96;
		else
			trapSkillId = 129;
		player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.ChestInstance.isTrap"));
		handleCast(player, trapSkillId);
	}

	private void handleCast(final Player player, final int skillId)
	{
		int skillLevel = 1;
		final int lvl = getLevel();
		if(lvl > 20 && lvl <= 40)
			skillLevel = 3;
		else if(lvl > 40 && lvl <= 60)
			skillLevel = 5;
		else if(lvl > 60)
			skillLevel = 6;
		if(player.isDead() || !player.isVisible() || !player.isInRange(player.getTarget(), 150L))
			return;
		final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if(skill == null)
		{
			ChestInstance._log.warn("L2Chest skillId: " + skillId + " level: " + skillLevel + " not found!");
			return;
		}
		if(player.getAbnormalList().getEffectsBySkill(skill) == null)
		{
			this.broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skillLevel, skill.getHitTime(), 0L));
			skill.getEffects(this, player, true, false);
		}
	}

	public void onOpen(final Player opener)
	{
		opener.broadcastPacket(new SocialAction(opener.getObjectId(), 13));
		getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, opener, null, 100);
	}

	@Override
	public int getTrueId()
	{
		switch(getNpcId())
		{
			case 21671:
			{
				return Rnd.get(18287, 18288);
			}
			case 21694:
			{
				return Rnd.get(18289, 18290);
			}
			case 21717:
			{
				return Rnd.get(18291, 18292);
			}
			case 21740:
			{
				return Rnd.get(18293, 18294);
			}
			case 21763:
			{
				return Rnd.get(18295, 18296);
			}
			case 21786:
			{
				return Rnd.get(18297, 18298);
			}
			default:
			{
				return getNpcId() - 3536;
			}
		}
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean isChest()
	{
		return true;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}

	static
	{
		_log = LoggerFactory.getLogger(ChestInstance.class);
	}
}
