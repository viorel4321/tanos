package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.PledgeSkillLearn;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.AcquireSkillInfo;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.tables.Spellbook;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private int _id;
	private int _level;
	private int _skillType;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
	}

	@Override
	public void runImpl()
	{
		if(_id <= 0 || _level <= 0)
			return;
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || SkillTable.getInstance().getInfo(_id, _level) == null)
			return;
		final NpcInstance trainer = activeChar.getLastNpc();
		if((trainer == null || activeChar.getDistance(trainer.getX(), trainer.getY()) > 150.0) && !activeChar.isGM())
			return;
		final Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		boolean canteach = false;
		if(skill == null)
			return;
		if(_skillType == 0)
		{
			if(!trainer.getTemplate().canTeach(activeChar.getSkillLearningClassId()))
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.model.instances.NpcInstance.WrongTeacherClass"));
				return;
			}
			final SkillLearn[] availableSkills;
			final SkillLearn[] skills = availableSkills = SkillTree.getInstance().getAvailableSkills(activeChar, activeChar.getSkillLearningClassId());
			for(final SkillLearn s : availableSkills)
				if(s.getId() == _id && s.getLevel() == _level)
				{
					canteach = true;
					break;
				}
			if(!canteach)
				return;
			final int requiredSp = SkillTree.getInstance().getSkillCost(activeChar, skill);
			final AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);
			int spbId = -1;
			if(skill.getId() == 1405)
				spbId = Spellbook.getInstance().getBookForSkill(skill.getId(), _level);
			else if(skill.getLevel() == 1)
				spbId = Spellbook.getInstance().getBookForSkill(skill.getId(), -1);
			if(spbId > -1)
				asi.addRequirement(99, spbId, 1, 50);
			this.sendPacket(asi);
		}
		else if(_skillType == 2)
		{
			int requiredRep = 0;
			int itemId = 0;
			final PledgeSkillLearn[] availablePledgeSkills;
			final PledgeSkillLearn[] skills2 = availablePledgeSkills = SkillTree.getInstance().getAvailablePledgeSkills(activeChar);
			for(final PledgeSkillLearn s2 : availablePledgeSkills)
				if(s2.getId() == _id && s2.getLevel() == _level)
				{
					canteach = true;
					requiredRep = s2.getRepCost();
					itemId = s2.getItemId();
					break;
				}
			if(!canteach)
				return;
			final AcquireSkillInfo asi2 = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredRep, 2);
			asi2.addRequirement(1, itemId, 1, 0);
			this.sendPacket(asi2);
		}
		else
		{
			int costid = 0;
			int costcount = 0;
			int spcost = 0;
			final SkillLearn[] availableSkills2;
			final SkillLearn[] skillsc = availableSkills2 = SkillTree.getInstance().getAvailableSkills(activeChar);
			for(final SkillLearn s3 : availableSkills2)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s3.getId(), s3.getLevel());
				if(sk != null)
					if(sk == skill)
					{
						canteach = true;
						costid = s3.getIdCost();
						costcount = s3.getCostCount();
						spcost = s3.getSpCost();
					}
			}
			final AcquireSkillInfo asi3 = new AcquireSkillInfo(skill.getId(), skill.getLevel(), spcost, 1);
			asi3.addRequirement(4, costid, costcount, 0);
			this.sendPacket(asi3);
		}
	}
}
