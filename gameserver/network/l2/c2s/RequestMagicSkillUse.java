package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.ai.PlayableAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.SkillTable;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	public void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}

		if(System.currentTimeMillis() - player.getLastSkillPacket() < Config.SKILL_PACKET_DELAY)
			player.addSkillPacket();

		player.setActive();

		Skill skill = SkillTable.getInstance().getInfo(_magicId, player.getSkillLevel(_magicId));
		if(skill != null)
		{
			if(!skill.isActive() && !skill.isToggle())
				return;

			if(skill.isMagic() && player.getSkillPackets() > Config.MAX_SKILL_PACKETS && System.currentTimeMillis() - player.getLastSkillPacket() < Config.SKILL_USE_DELAY)
			{
				player.sendActionFailed();
				return;
			}

			if(player.getSkillPackets() > Config.MAX_SKILL_PACKETS)
				player.clearSkillPackets();

			player.setLastSkillPacket();
			if(skill.isToggle() && player.getAbnormalList().getEffectsBySkill(skill) != null)
			{
				if(!Config.ALT_TOGGLE)
				{
					if(player.isActionsDisabled())
					{
						if(_magicId != 60)
							player.getAI().setNextAction(PlayableAI.nextAction.CAST, skill, player, _ctrlPressed, _shiftPressed);
						else if(Config.USE_BREAK_FAKEDEATH && player.isFakeDeath())
						{
							player.breakFakeDeath();
							player.updateEffectIcons();
						}
						player.sendActionFailed();
						return;
					}

					if(player.isSitting())
					{
						player.sendPacket(new SystemMessage(31));
						player.sendActionFailed();
						return;
					}

					if(skill.stopActor())
						player.stopMove(false);
				}
				player.getAbnormalList().stop(skill.getId());
				player.sendPacket(new SystemMessage(335).addSkillName(skill.getId(), skill.getDisplayLevel()));
				return;
			}

			Creature target = skill.getAimingTarget(player, player.getTarget());
			player.setGroundSkillLoc(null);
			player.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
		}
		else
			player.sendActionFailed();
	}
}
