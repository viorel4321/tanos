package l2s.gameserver.network.l2.c2s;

import java.util.Collection;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.ShortCut;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ShortCutRegister;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;

public class RequestExEnchantSkill extends L2GameClientPacket
{
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
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
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
		if(activeChar.getSkillLevel(_skillId) >= _skillLvl)
		{
			this.sendPacket(new SystemMessage(1438));
			return;
		}
		final Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if(skill == null)
		{
			this.sendPacket(new SystemMessage(1438));
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
		int counts = 0;
		int _requiredSp = 10000000;
		int _requiredExp = 100000;
		int _rate = 0;
		int _baseLvl = 1;
		final EnchantSkillLearn[] availableEnchantSkills;
		final EnchantSkillLearn[] skills = availableEnchantSkills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
		for(final EnchantSkillLearn s : availableEnchantSkills)
		{
			final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk != null)
				if(sk == skill)
				{
					++counts;
					_requiredSp = s.getSpCost();
					_requiredExp = s.getExp();
					_rate = s.getRate(activeChar);
					_baseLvl = s.getBaseLevel();
				}
		}
		if(counts == 0)
		{
			this.sendPacket(new SystemMessage(1438));
			return;
		}
		if(activeChar.getSp() < _requiredSp)
		{
			this.sendPacket(new SystemMessage(1443));
			return;
		}
		if(activeChar.getExp() - Experience.LEVEL[76] >= _requiredExp)
		{
			if(_skillLvl == 101 || _skillLvl == 141)
			{
				final ItemInstance spb = activeChar.getInventory().getItemByItemId(6622);
				if(spb == null)
				{
					this.sendPacket(new SystemMessage(1439));
					return;
				}
				activeChar.getInventory().destroyItem(spb, 1L, true);
			}
			boolean ok = true;
			if(Rnd.chance(_rate))
			{
				activeChar.addSkill(skill, true);
				activeChar.setSp(activeChar.getSp() - _requiredSp);
				activeChar.addExp(-_requiredExp);
				if(activeChar.getExp() < Experience.LEVEL[activeChar.getLevel()])
					activeChar.decreaseLevel(true);
				activeChar.updateStats();
				activeChar.sendPacket(new SystemMessage(539).addNumber(Integer.valueOf(_requiredExp)));
				activeChar.sendPacket(new SystemMessage(538).addNumber(Integer.valueOf(_requiredSp)));
				activeChar.sendPacket(new SystemMessage(1440).addSkillName(_skillId, _skillLvl));
			}
			else
			{
				activeChar.addSkill(SkillTable.getInstance().getInfo(_skillId, _baseLvl), true);
				activeChar.sendPacket(new SystemMessage(1441).addSkillName(_skillId, _skillLvl));
				ok = false;
			}
			trainer.showEnchantSkillList(activeChar);
			this.sendPacket(new SkillList(activeChar));
			updateSkillShortcuts(activeChar, ok ? _skillLvl : _baseLvl);
			return;
		}
		this.sendPacket(new SystemMessage(1444));
	}

	private void updateSkillShortcuts(final Player player, final int lvl)
	{
		final Collection<ShortCut> allShortCuts = player.getAllShortCuts();
		for(final ShortCut sc : allShortCuts)
			if(sc.id == _skillId && sc.type == 2)
			{
				final ShortCut newsc = new ShortCut(sc.slot, sc.page, sc.type, sc.id, lvl);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
	}
}
