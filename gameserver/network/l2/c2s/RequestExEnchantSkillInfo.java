package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillInfo;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;

public class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private static final Logger _log;
	private int _skillId;
	private int _skillLvl;

	@Override
	public void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	public void runImpl()
	{
		if(_skillId <= 0 || _skillLvl <= 0)
			return;
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getLevel() < 76)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0414\u043b\u044f \u0437\u0430\u0442\u043e\u0447\u043a\u0438 \u0443\u043c\u0435\u043d\u0438\u0439 \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c 76+ \u0443\u0440\u043e\u0432\u0435\u043d\u044c." : "Need 76+ level.");
			return;
		}
		if(activeChar.getClassId().getLevel() < 4)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0414\u043b\u044f \u0437\u0430\u0442\u043e\u0447\u043a\u0438 \u0443\u043c\u0435\u043d\u0438\u0439 \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u0430 \u0442\u0440\u0435\u0442\u044c\u044f \u043f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u044f." : "Need third class.");
			return;
		}
		final Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if(skill == null || skill.getId() != _skillId)
		{
			RequestExEnchantSkillInfo._log.warn("RequestExEnchantSkillInfo: skillId " + _skillId + " level " + _skillLvl + " not found in Datapack.");
			activeChar.sendPacket(new SystemMessage(1438));
			return;
		}
		final NpcInstance trainer = activeChar.getLastNpc();
		if(trainer == null || activeChar.getDistance(trainer) > 150.0 && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(trainer.getNpcId() != Config.ALLOW_ESL && !trainer.getTemplate().canTeach(activeChar.getClassId()) && !trainer.getTemplate().canTeach(activeChar.getClassId().getParent()))
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0412\u0430\u043c \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u043d\u0430\u0439\u0442\u0438 \u043c\u0430\u0441\u0442\u0435\u0440\u0430 \u0434\u043b\u044f \u0432\u0430\u0448\u0435\u0439 \u0442\u0435\u043a\u0443\u0449\u0435\u0439 \u043f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u0438." : "You need to find the master for your current class.");
			return;
		}
		boolean canteach = false;
		final EnchantSkillLearn[] availableEnchantSkills;
		final EnchantSkillLearn[] skills = availableEnchantSkills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
		for(final EnchantSkillLearn s : availableEnchantSkills)
			if(s.getId() == _skillId && s.getLevel() == _skillLvl)
			{
				canteach = true;
				break;
			}
		if(!canteach)
		{
			this.sendPacket(new SystemMessage(1438));
			return;
		}
		final int requiredSp = SkillTree.getInstance().getSkillSpCost(activeChar, skill);
		final int requiredExp = SkillTree.getInstance().getSkillExpCost(activeChar, skill);
		final int rate = SkillTree.getInstance().getSkillRate(activeChar, skill);
		final ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);
		if(_skillLvl == 101 || _skillLvl == 141)
			asi.addRequirement(4, 6622, 1, 0);
		activeChar.sendPacket(asi);
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestExEnchantSkillInfo.class);
	}
}
