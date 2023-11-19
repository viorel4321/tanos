package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Bonus;
import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.PlayerManager;
import l2s.gameserver.instancemanager.QuestManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.ShortCut;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharCreateFail;
import l2s.gameserver.network.l2.s2c.CharCreateOk;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfo;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterCreate extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterCreate.class);

	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	public void readImpl()
	{
		_name = readS();
		readD();
		_sex = readD();
		_classId = readD();
		readD();
		readD();
		readD();
		readD();
		readD();
		readD();
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}

	@Override
	public void runImpl()
	{
		for(final ClassId cid : ClassId.values())
		{
			if(cid.getId() == _classId && cid.getLevel() != 1)
				return;
		}

		if(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0 && PlayerManager.accountCharNumber(getClient().getLogin()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT)
		{
			sendPacket(CharCreateFail.REASON_TOO_MANY_CHARACTERS);
			return;
		}

		if(Config.OFF_NAME_LENGTH && (_name.length() < 3 || _name.length() > 16))
		{
			sendPacket(CharCreateFail.REASON_16_ENG_CHARS);
			return;
		}

		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE) || Config.CNAME_DENY_PATTERN.matcher(_name).matches())
		{
			sendPacket(CharCreateFail.REASON_INCORRECT_NAME);
			return;
		}

		if(PlayerManager.getObjectIdByName(_name) > 0 || Config.BOTS_USED_NAMES && Player.bots_names.contains(_name))
		{
			sendPacket(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}

		if(_face > 2 || _face < 0)
		{
			_log.warn("Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharCreateFail.REASON_CREATION_FAILED);
			return;
		}

		if(_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6))
		{
			_log.warn("Character Creation Failure: Character hair style " + _hairStyle + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharCreateFail.REASON_CREATION_FAILED);
			return;
		}

		if(_hairColor > 3 || _hairColor < 0)
		{
			_log.warn("Character Creation Failure: Character hair color " + _hairColor + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharCreateFail.REASON_CREATION_FAILED);
			return;
		}

		final Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
			return;

		sendPacket(CharCreateOk.STATIC_PACKET);
		initNewChar(getClient(), newChar);
	}

	private void initNewChar(final GameClient client, final Player newChar)
	{
		final PlayerTemplate template = newChar.getTemplate();
		Player.restoreCharSubClasses(newChar);
		if(Config.STARTING_ADENA > 0)
			newChar.addAdena(Config.STARTING_ADENA);
		if(Config.START_XYZ.length >= 15)
		{
			if(newChar.getRace().ordinal() == 0)
				newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[0], Config.START_XYZ[1], Config.START_XYZ[2], 0, 200, newChar.getGeoIndex()));
			else if(newChar.getRace().ordinal() == 1)
				newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[3], Config.START_XYZ[4], Config.START_XYZ[5], 0, 200, newChar.getGeoIndex()));
			else if(newChar.getRace().ordinal() == 2)
				newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[6], Config.START_XYZ[7], Config.START_XYZ[8], 0, 200, newChar.getGeoIndex()));
			else if(newChar.getRace().ordinal() == 3)
				newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[9], Config.START_XYZ[10], Config.START_XYZ[11], 0, 200, newChar.getGeoIndex()));
			else
				newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[12], Config.START_XYZ[13], Config.START_XYZ[14], 0, 200, newChar.getGeoIndex()));
		}
		else if(Config.START_XYZ.length < 3)
			newChar.setXYZInvisible(template.getStartLoc());
		else
			newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[0], Config.START_XYZ[1], Config.START_XYZ[2], 0, 200, newChar.getGeoIndex()));
		newChar.setTitle(Config.CHAR_TITLE);
		final ItemTable itemTable = ItemTable.getInstance();
		for(final ItemTemplate i : template.getItems())
		{
			final ItemInstance item = itemTable.createItem(i.getItemId());
			newChar.getInventory().addItem(item);
			if(item.getItemId() == 5588)
				newChar.registerShortCut(new ShortCut(11, 0, 1, item.getObjectId(), -1));
			if(item.isEquipable() && !item.isArrow() && (newChar.getActiveWeaponItem() == null || item.getTemplate().getType2() != 0))
				newChar.getInventory().equipItem(item, false);
		}
		if(Config.ALLOW_START_ITEMS)
		{
			if(newChar.isMageClass())
			{
				for(int j = 0; j < Config.START_ITEMS_MAGE.length; j += 2)
				{
					final ItemInstance item2 = itemTable.createItem(Config.START_ITEMS_MAGE[j]);
					item2.setCount(Config.START_ITEMS_MAGE[j + 1]);
					newChar.getInventory().addItem(item2);
					if(item2.isEquipable() && !item2.isArrow())
						newChar.getInventory().equipItem(item2, false);
				}
			}
			else
			{
				for(int j = 0; j < Config.START_ITEMS_FIGHTER.length; j += 2)
				{
					final ItemInstance item2 = itemTable.createItem(Config.START_ITEMS_FIGHTER[j]);
					item2.setCount(Config.START_ITEMS_FIGHTER[j + 1]);
					newChar.getInventory().addItem(item2);
					if(item2.isEquipable() && !item2.isArrow())
						newChar.getInventory().equipItem(item2, false);
				}
			}
		}

		if(Config.ALLOW_START_ITEMS_ENCHANT)
		{
			for(final ItemInstance item2 : newChar.getInventory().getItemsList())
			{
				if(item2.canBeEnchanted())
					item2.setEnchantLevel(item2.isWeapon() ? Config.START_ITEMS_ENCHANT_WEAPON : Config.START_ITEMS_ENCHANT_ARMOR);
			}
		}

		if(Config.ALLOW_START_BUFFS)
		{
			int n = 0;
			if(newChar.isMageClass())
			{
				for(int k = 0; k < Config.START_BUFFS_MAGE.length; k += 2) {
					Skill skill = SkillTable.getInstance().getInfo(Config.START_BUFFS_MAGE[k], Config.START_BUFFS_MAGE[k + 1]);
					if (skill == null) {
						_log.warn("Cannot found skill ID[" + Config.START_BUFFS_MAGE[k] + "] LEVEL[" + Config.START_BUFFS_MAGE[k + 1] + "] for mage start buffs!");
						continue;
					}
					OlympiadGame.giveBuff(newChar, skill, n++);
				}
			}
			else
			{
				for(int k = 0; k < Config.START_BUFFS_FIGHTER.length; k += 2) {
					Skill skill = SkillTable.getInstance().getInfo(Config.START_BUFFS_FIGHTER[k], Config.START_BUFFS_FIGHTER[k + 1]);
					if (skill == null) {
						_log.warn("Cannot found skill ID[" + Config.START_BUFFS_MAGE[k] + "] LEVEL[" + Config.START_BUFFS_MAGE[k + 1] + "] for fighter start buffs!");
						continue;
					}
					OlympiadGame.giveBuff(newChar, skill, n++);
				}
			}
		}

		if(Config.START_PA > 0)
			Bonus.newbieBonus(newChar.getObjectId(), Config.START_RATE_PA, Config.START_PA, client);

		final SkillLearn[] availableSkills;
		final SkillLearn[] skills = availableSkills = SkillTree.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		for(final SkillLearn s : availableSkills)
			newChar.addSkill(SkillTable.getInstance().getInfo(s.getId(), s.getLevel()), true);
		if(newChar.getSkillLevel(1001) > 0)
			newChar.registerShortCut(new ShortCut(1, 0, 2, 1001, 1));
		if(newChar.getSkillLevel(1177) > 0)
			newChar.registerShortCut(new ShortCut(1, 0, 2, 1177, 1));
		if(newChar.getSkillLevel(1216) > 0)
			newChar.registerShortCut(new ShortCut(2, 0, 2, 1216, 1));
		newChar.registerShortCut(new ShortCut(0, 0, 3, 2, -1));
		newChar.registerShortCut(new ShortCut(3, 0, 3, 5, -1));
		newChar.registerShortCut(new ShortCut(10, 0, 3, 0, -1));

		if(Config.STARTING_LEVEL > 1)
		{
			newChar.addExpAndSp((Experience.LEVEL[Config.STARTING_LEVEL + 1] - Experience.LEVEL[Config.STARTING_LEVEL]) / 2L + Experience.LEVEL[Config.STARTING_LEVEL], 0L, false, false);
			newChar.getSubClasses().get(newChar.getActiveClassId()).setExp(newChar.getExp());
			newChar.getSubClasses().get(newChar.getActiveClassId()).setLevel(newChar.getLevel());
		}

		if(Config.STARTING_SP > 0)
			newChar.getSubClasses().get(newChar.getActiveClassId()).setSp(Config.STARTING_SP);

		startTutorialQuest(newChar);

		if(Config.START_QUESTS_COMPLETED.length > 0)
		{
			for(final int id : Config.START_QUESTS_COMPLETED)
			{
				final Quest q = QuestManager.getQuest(id);
				if(q != null)
					q.newQuestState(newChar, 3);
			}
		}

		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp(), false);
		newChar.setCurrentCp(0.0);
		newChar.setOnlineStatus(false);
		newChar.storeLastIpAndHWID(client.getIpAddr(), client.getHWID());
		PlayerManager.saveCharToDisk(newChar);
		newChar.deleteMe();
		final CharacterSelectionInfo CSInfo = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		client.sendPacket(CSInfo);
		client.setCharSelection(CSInfo.getCharInfo());
	}

	private static void startTutorialQuest(final Player player)
	{
		final Quest q = QuestManager.getQuest(255);
		if(q != null)
			q.newQuestState(player, 1);
	}
}
