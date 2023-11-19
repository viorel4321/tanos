package l2s.gameserver.network.l2.c2s;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.ShortCut;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.PledgeSkillLearn;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.VillageMasterInstance;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCount;
import l2s.gameserver.network.l2.s2c.ShortCutRegister;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.tables.Spellbook;

public class RequestAquireSkill extends L2GameClientPacket
{
	private static final Logger _log;
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
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final NpcInstance trainer = player.getLastNpc();
		if((trainer == null || player.getDistance(trainer.getX(), trainer.getY()) > 150.0) && !player.isGM())
			return;
		final int npcid = trainer.getNpcId();
		player.setSkillLearningClassId(player.getClassId());
		if(Math.max(player.getSkillLevel(_id), 0) + 1 != _level)
			return;
		final Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		if(skill == null || !skill.isCommon() && !SkillTree.getInstance().isSkillPossible(player, _id, _level))
			return;
		int counts = 0;
		int _requiredSp = 100000000;
		if(_skillType == 0)
		{
			final SkillLearn[] availableSkills;
			final SkillLearn[] skills = availableSkills = SkillTree.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());
			for(final SkillLearn s : availableSkills)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk != null && sk == skill && sk.getCanLearn(player.getSkillLearningClassId()))
					if(sk.canTeachBy(npcid))
					{
						_requiredSp = SkillTree.getInstance().getSkillCost(player, skill);
						++counts;
					}
			}
			if(counts == 0)
				return;
			if(player.getSp() < _requiredSp)
			{
				SystemMessage sm = new SystemMessage(278);
				player.sendPacket(sm);
				sm = null;
				return;
			}
			if(!Config.ALT_DISABLE_SPELLBOOKS)
			{
				int spbId = -1;
				if(skill.getId() == 1405)
					spbId = Spellbook.getInstance().getBookForSkill(skill.getId(), _level);
				else
					spbId = Spellbook.getInstance().getBookForSkill(skill.getId(), -1);
				if(skill.getId() == 1405 || skill.getLevel() == 1 && spbId > -1)
				{
					final ItemInstance spb = player.getInventory().getItemByItemId(spbId);
					if(spb == null)
					{
						player.sendPacket(new SystemMessage(276));
						return;
					}
					final ItemInstance ri = player.getInventory().destroyItem(spb.getObjectId(), 1L, true);
					player.sendPacket(SystemMessage.removeItems(ri.getItemId(), 1L));
				}
			}
		}
		else if(_skillType == 1)
		{
			int costid = 0;
			int costcount = 0;
			final SkillLearn[] availableSkills2;
			final SkillLearn[] skillsc = availableSkills2 = SkillTree.getInstance().getAvailableSkills(player);
			for(final SkillLearn s2 : availableSkills2)
			{
				final Skill sk2 = SkillTable.getInstance().getInfo(s2.getId(), s2.getLevel());
				if(sk2 != null)
					if(sk2 == skill)
					{
						++counts;
						costid = s2.getIdCost();
						costcount = s2.getCostCount();
						_requiredSp = s2.getSpCost();
					}
			}
			if(counts == 0)
				return;
			if(player.getSp() < _requiredSp)
			{
				SystemMessage sm2 = new SystemMessage(278);
				player.sendPacket(sm2);
				sm2 = null;
				return;
			}
			final ItemInstance spb2 = player.getInventory().getItemByItemId(costid);
			if(spb2 == null || spb2.getCount() < costcount)
			{
				player.sendPacket(new SystemMessage(276));
				return;
			}
			final ItemInstance ri2 = player.getInventory().destroyItem(spb2, costcount, true);
			player.sendPacket(SystemMessage.removeItems(ri2.getItemId(), costcount));
		}
		else
		{
			if(_skillType != 2)
			{
				RequestAquireSkill._log.warn("Recived Wrong Packet Data in Aquired Skill - unk1: " + _skillType);
				return;
			}
			if(!player.isClanLeader())
			{
				player.sendPacket(new SystemMessage(236));
				return;
			}
			int itemId = 0;
			int repCost = 100000000;
			final PledgeSkillLearn[] availablePledgeSkills;
			final PledgeSkillLearn[] skills2 = availablePledgeSkills = SkillTree.getInstance().getAvailablePledgeSkills(player);
			for(final PledgeSkillLearn s3 : availablePledgeSkills)
			{
				final Skill sk2 = SkillTable.getInstance().getInfo(s3.getId(), s3.getLevel());
				if(sk2 != null)
					if(sk2 == skill)
					{
						++counts;
						itemId = s3.getItemId();
						repCost = s3.getRepCost();
					}
			}
			if(counts == 0)
				return;
			if(player.getClan().getReputationScore() >= repCost)
			{
				if(!Config.ALT_DISABLE_EGGS)
				{
					final ItemInstance spb2 = player.getInventory().getItemByItemId(itemId);
					if(spb2 == null)
					{
						player.sendPacket(new SystemMessage(276));
						return;
					}
					final ItemInstance ri2 = player.getInventory().destroyItem(spb2, 1L, true);
					player.sendPacket(SystemMessage.removeItems(ri2.getItemId(), 1L));
				}
				player.getClan().incReputation(-repCost, false, "AquireSkill");
				player.getClan().addNewSkill(skill, true);
				player.getClan().addAndShowSkillsToPlayer(player);
				player.getClan().broadcastToOnlineMembers(new SystemMessage(1788).addSkillName(_id, _level));
				((VillageMasterInstance) trainer).showClanSkillWindow(player);
				return;
			}
			final SystemMessage sm2 = new SystemMessage(1852);
			player.sendPacket(sm2);
			return;
		}
		player.addSkill(skill, true);
		player.sendPacket(new SkillList(player));
		player.setSp(player.getSp() - _requiredSp);
		player.updateStats();
		player.sendPacket(new SystemMessage(538).addNumber(Integer.valueOf(_requiredSp)));
		player.sendPacket(new SystemMessage(277).addSkillName(_id, _level));
		if(_level > 1)
		{
			final Collection<ShortCut> allShortCuts = player.getAllShortCuts();
			for(final ShortCut sc : allShortCuts)
				if(sc.id == _id && sc.type == 2)
				{
					final ShortCut newsc = new ShortCut(sc.slot, sc.page, sc.type, sc.id, _level);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
		}
		if(_id >= 1368 && _id <= 1372)
			player.sendPacket(new ExStorageMaxCount(player));
		if(trainer != null)
			if(_skillType == 0)
				trainer.showSkillList(player);
			else if(_skillType == 1)
				trainer.showFishingSkillList(player);
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestAquireSkill.class);
	}
}
