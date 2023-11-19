package l2s.gameserver.model.instances;

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SubClass;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassType;
import l2s.gameserver.model.base.PledgeSkillLearn;
import l2s.gameserver.model.base.PlayerClass;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.AcquireSkillList;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.PledgeReceiveSubPledgeCreated;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListAll;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListUpdate;
import l2s.gameserver.network.l2.s2c.PledgeStatusChanged;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.CharTemplateTable;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.SiegeUtils;
import l2s.gameserver.utils.Util;

public final class VillageMasterInstance extends NpcInstance
{
	private static Logger _log;

	public VillageMasterInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		if(command.startsWith("create_clan") && command.length() > 12)
		{
			final String val = command.substring(12);
			createClan(player, val);
		}
		else if(command.startsWith("create_academy") && command.length() > 15)
		{
			final String sub = command.substring(15, command.length());
			createSubPledge(player, sub, -1, 5, "");
		}
		else if(command.startsWith("create_royal") && command.length() > 15)
		{
			final String[] sub2 = command.substring(13, command.length()).split(" ", 2);
			if(sub2.length == 2)
				createSubPledge(player, sub2[1], 100, 6, sub2[0]);
		}
		else if(command.startsWith("create_knight") && command.length() > 16)
		{
			final String[] sub2 = command.substring(14, command.length()).split(" ", 2);
			if(sub2.length == 2)
				createSubPledge(player, sub2[1], 1001, 7, sub2[0]);
		}
		else if(command.startsWith("assign_subpl_leader") && command.length() > 22)
		{
			final String[] sub2 = command.substring(20, command.length()).split(" ", 2);
			if(sub2.length == 2)
				assignSubPledgeLeader(player, sub2[1], sub2[0]);
		}
		else if(command.startsWith("assign_new_clan_leader") && command.length() > 23)
		{
			final String val = command.substring(23);
			setLeader(player, val);
			if(Config.DEBUG)
				VillageMasterInstance._log.info("Clan " + player.getClan() + " assign new clan leader: " + val + ".");
		}
		if(command.startsWith("create_ally") && command.length() > 12)
		{
			final String val = command.substring(12);
			createAlly(player, val);
		}
		else if(command.startsWith("dissolve_ally"))
			dissolveAlly(player);
		else if(command.startsWith("dissolve_clan"))
			ClanTable.getInstance().dissolveClan(player);
		else if(command.startsWith("increase_clan_level"))
			levelUpClan(player);
		else if(command.startsWith("learn_clan_skills"))
			showClanSkillWindow(player);
		else if(command.startsWith("Subclass"))
		{
			if(player.getServitor() != null)
			{
				player.sendPacket(Msg.A_SUB_CLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED);
				return;
			}
			if(player.isActionsDisabled())
			{
				if(player.isCastingNow())
					player.sendPacket(Msg.SUB_CLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
				return;
			}
			if(player.getSittingTask())
			{
				player.sendMessage("You can't do it while sitting task.");
				return;
			}
			if(player.getWeightPenalty() >= 3)
			{
				player.sendPacket(Msg.A_SUB_CLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
				return;
			}
			if(player.getInventoryLimit() * 0.8 < player.getInventory().getSize())
			{
				player.sendPacket(Msg.A_SUB_CLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
				return;
			}
			StringBuffer content = new StringBuffer("<html><body>");
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			final HashMap<Integer, SubClass> playerClassList = player.getSubClasses();
			if(player.getLevel() < 40)
			{
				content.append("You must be level 40 or more to operate with your sub-classes.");
				content.append("</body></html>");
				html.setHtml(content.toString());
				player.sendPacket(html);
				return;
			}
			if(player.getClassId().getLevel() < 3)
			{
				content.append("You must have 2nd class.");
				content.append("</body></html>");
				html.setHtml(content.toString());
				player.sendPacket(html);
				return;
			}
			int classId = 0;
			int newClassId = 0;
			int intVal = 0;
			try
			{
				for(final String id : command.substring(9, command.length()).split(" "))
					if(intVal == 0)
						intVal = Integer.parseInt(id);
					else if(classId > 0)
						newClassId = Integer.parseInt(id);
					else
						classId = Integer.parseInt(id);
			}
			catch(Exception ex)
			{}
			switch(intVal)
			{
				case 1:
				{
					final Set<PlayerClass> subsAvailable = getAvailableSubClasses(player);
					if(subsAvailable != null && !subsAvailable.isEmpty())
					{
						content.append("Add a subclass:<br>Which subclass do you wish to add?<br>");
						for(final PlayerClass subClass : subsAvailable)
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 4 " + subClass.ordinal() + "\" msg=\"" + (player.isLangRus() ? "\u0412\u044b \u0445\u043e\u0442\u0438\u0442\u0435 \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c " + CharTemplateTable.getClassNameById(subClass.ordinal()) + " \u043a\u043b\u0430\u0441\u0441, \u043a\u0430\u043a \u0432\u0430\u0448 \u0441\u0430\u0431\u043a\u043b\u0430\u0441\u0441?" : "Do you wish to add " + CharTemplateTable.getClassNameById(classId) + " class as your sub class?") + "\">" + formatClassForDisplay(subClass) + "</a><br>");
						break;
					}
					player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.NoSubAtThisTime"));
					return;
				}
				case 2:
				{
					content.append("Change Subclass:<br>");
					final int baseClassId = player.getBaseClassId();
					HashMap<Integer, SubClass> pClassList = new HashMap<Integer, SubClass>();
					if(Config.ANY_SUBCLASS_MASTER)
						pClassList = playerClassList;
					else
						for(final SubClass subClass2 : playerClassList.values())
							if(isAvailableClass(PlayerClass.values()[subClass2.getClassId()]))
								pClassList.put(subClass2.getClassId(), subClass2);
					if(pClassList.size() < 1)
					{
						content.append("You can't change subclasses when you don't have a subclass to begin with.<br><a action=\"bypass -h npc_" + getObjectId() + "_Subclass 1\">Add subclass.</a>");
						break;
					}
					content.append("Please choose a subclass to change to. If the one you are looking for is not here, please seek out the appropriate master for that class.<br>");
					content.append("Base class: <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(baseClassId) + "</font><br><br>");
					for(final SubClass subClass2 : pClassList.values())
					{
						final int subClassId = subClass2.getClassId();
						content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + subClassId + "\">" + CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
					}
					break;
				}
				case 3:
				{
					content.append("Addition of a sub-class:<br>Which of the following sub-classes would you like to change?<br>");
					for(final SubClass sub3 : playerClassList.values())
					{
						content.append("<br>");
						if(!sub3.isBase())
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 " + sub3.getClassId() + "\">" + CharTemplateTable.getClassNameById(sub3.getClassId()) + "</a><br>");
					}
					content.append("<br>If you change a sub-class, you'll start at level " + Config.SUBCLASS_LEVEL + " after the 2nd class transfer.");
					break;
				}
				case 4:
				{
					boolean allowAddition = true;
					if(player.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.NoSubBeforeLevel").addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
						allowAddition = false;
					}
					if(!playerClassList.isEmpty())
						for(final SubClass subClass3 : playerClassList.values())
							if(subClass3.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
							{
								if(allowAddition)
									player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.NoSubBeforeLevel").addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
								allowAddition = false;
								break;
							}
					if(!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS && !playerClassList.isEmpty() && playerClassList.size() < 2 + Config.ALT_GAME_SUB_ADD)
					{
						QuestState qs = player.getQuestState(234);
						allowAddition = qs != null && qs.isCompleted();
						if(allowAddition)
						{
							qs = player.getQuestState(235);
							allowAddition = qs != null && qs.isCompleted();
							if(!allowAddition)
								player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.QuestMimirsElixir"));
						}
						else
							player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.QuestFatesWhisper"));
					}
					if(!allowAddition)
					{
						html.setFile("villagemaster/SubClass_Fail.htm");
						break;
					}
					if(System.currentTimeMillis() - player.getLastClassChange() < Config.CLASS_CHANGE_DELAY * 1000L)
					{
						player.sendMessage(player.isLangRus() ? "\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0440\u0430\u0437 \u0432 " + Config.CLASS_CHANGE_DELAY + " " + Util.secondFormat(true, String.valueOf(Config.CLASS_CHANGE_DELAY)) + "." : "Available once per " + Config.CLASS_CHANGE_DELAY + " " + Util.secondFormat(false, String.valueOf(Config.CLASS_CHANGE_DELAY)) + ".");
						return;
					}
					player.setLastClassChange();
					if(!player.addSubClass(classId, true))
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.SubclassCouldNotBeAdded"));
						return;
					}
					if(Config.ENABLE_OLYMPIAD && (Olympiad.isRegistered(player) || player.getOlympiadGameId() > -1))
					{
						player.sendPacket(new SystemMessage(1692));
						Olympiad.unRegisterNoble(player, true);
					}
					content.append("Add a subclass:<br>The subclass of <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(classId) + "</font> has been added.");
					if(Config.SUBCLASS_LEVEL >= 76)
						content.append("<br><br><br><center><button value=\"" + (player.isLangRus() ? "\u041f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u044f" : "Profession") + "\" action=\"bypass -h scripts_services.ClassMaster:list\" width=115 height=30 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></center>");
					player.sendPacket(new SystemMessage(1269));
					if(player.recording)
					{
						player.recBot(7, 2, classId, 0, getNpcId(), getX(), getY());
						break;
					}
					break;
				}
				case 5:
				{
					if(classId == player.getActiveClassId())
					{
						content.append("Change Subclass:<br>");
						content.append("Um, I don't think so. That is your current subclass... Select another subclass.<br>");
						content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 2\">Change subclasses.</a><br>");
						break;
					}
					if(System.currentTimeMillis() - player.getLastClassChange() < Config.CLASS_CHANGE_DELAY * 1000L)
					{
						player.sendMessage(player.isLangRus() ? "\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0440\u0430\u0437 \u0432 " + Config.CLASS_CHANGE_DELAY + " " + Util.secondFormat(true, String.valueOf(Config.CLASS_CHANGE_DELAY)) + "." : "Available once per " + Config.CLASS_CHANGE_DELAY + " " + Util.secondFormat(false, String.valueOf(Config.CLASS_CHANGE_DELAY)) + ".");
						return;
					}
					player.setLastClassChange();
					player.setActiveSubClass(classId, true);
					if(Config.ENABLE_OLYMPIAD && (Olympiad.isRegistered(player) || player.getOlympiadGameId() > -1))
					{
						player.sendPacket(new SystemMessage(1692));
						Olympiad.unRegisterNoble(player, true);
					}
					content = null;
					player.sendPacket(new SystemMessage(1270));
					if(player.recording)
					{
						player.recBot(7, 1, classId, player.getLevel(), getNpcId(), getX(), getY());
						break;
					}
					break;
				}
				case 6:
				{
					content.append("Addition of a Sub-Class:<br>Which of the following sub-classes would you like to add as your sub-class?<br>");
					final Set<PlayerClass> subsAvailable = getAvailableSubClasses(player);
					if(subsAvailable != null && !subsAvailable.isEmpty())
					{
						for(final PlayerClass subClass4 : subsAvailable)
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 7 " + classId + " " + subClass4.ordinal() + "\" msg=\"" + (player.isLangRus() ? "\u0412\u0430\u0448 \u043f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0438\u0439 \u0441\u0430\u0431\u043a\u043b\u0430\u0441\u0441 \u043f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u0438 \u0431\u0443\u0434\u0435\u0442 \u0443\u0434\u0430\u043b\u0435\u043d \u0438 \u043f\u0440\u0438 \u0432\u044b\u0431\u043e\u0440\u0435 \u043d\u043e\u0432\u043e\u0433\u043e \u0441\u0430\u0431\u043a\u043b\u0430\u0441\u0441\u0430 \u0432\u0430\u043c \u0431\u0443\u0434\u0435\u0442 \u043f\u0440\u0438\u0441\u0432\u043e\u0435\u043d " + Config.SUBCLASS_LEVEL + "-\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c. \u0412\u044b \u0436\u0435\u043b\u0430\u0435\u0442\u0435 \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c \u043d\u043e\u0432\u044b\u0439 \u0441\u0430\u0431\u043a\u043b\u0430\u0441\u0441?" : "Your previous subclass will be removed and replaced with the new subclass at level " + Config.SUBCLASS_LEVEL + ".  Do you wish to continue?") + "\">" + formatClassForDisplay(subClass4) + "</a><br>");
						break;
					}
					player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.NoSubAtThisTime"));
					return;
				}
				case 7:
				{
					if(System.currentTimeMillis() - player.getLastClassChange() < Config.CLASS_CHANGE_DELAY * 1000L)
					{
						player.sendMessage(player.isLangRus() ? "\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0440\u0430\u0437 \u0432 " + Config.CLASS_CHANGE_DELAY + " " + Util.secondFormat(true, String.valueOf(Config.CLASS_CHANGE_DELAY)) + "." : "Available once per " + Config.CLASS_CHANGE_DELAY + " " + Util.secondFormat(false, String.valueOf(Config.CLASS_CHANGE_DELAY)) + ".");
						return;
					}
					player.setLastClassChange();
					if(!player.modifySubClass(classId, newClassId))
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.SubclassCouldNotBeAdded"));
						return;
					}
					if(Config.ENABLE_OLYMPIAD && (Olympiad.isRegistered(player) || player.getOlympiadGameId() > -1))
					{
						player.sendPacket(new SystemMessage(1692));
						Olympiad.unRegisterNoble(player, true);
					}
					content.append("Change Subclass:<br>You've changed subclasses. Come see me if you wish to change subclasses again.");
					if(Config.SUBCLASS_LEVEL >= 76)
						content.append("<br><br><br><center><button value=\"" + (player.isLangRus() ? "\u041f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u044f" : "Profession") + "\" action=\"bypass -h scripts_services.ClassMaster:list\" width=115 height=30 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"></center>");
					player.sendPacket(new SystemMessage(1269));
					if(player.recording)
					{
						player.recBot(7, 3, classId, newClassId, getNpcId(), getX(), getY());
						break;
					}
					break;
				}
			}
			if(content == null)
				return;
			content.append("</body></html>");
			if(content.length() > 26)
				html.setHtml(content.toString());
			player.sendPacket(html);
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
		return "villagemaster/" + pom + ".htm";
	}

	public void createClan(final Player player, final String clanName)
	{
		if(Config.DEBUG)
			VillageMasterInstance._log.info(player.getObjectId() + "(" + player.getName() + ") requested clan creation from " + getObjectId() + "(" + getName() + ")");
		if(player.getLevel() < Config.MinLevelToCreatePledge)
		{
			player.sendPacket(new SystemMessage(229));
			return;
		}
		if(player.getClanId() != 0)
		{
			player.sendPacket(new SystemMessage(190));
			return;
		}
		if(!player.canCreateClan())
		{
			player.sendPacket(new SystemMessage(230));
			return;
		}
		if(clanName.length() > 16)
		{
			player.sendPacket(new SystemMessage(262));
			return;
		}
		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
		{
			player.sendPacket(new SystemMessage(261));
			return;
		}
		final Clan clan = ClanTable.getInstance().createClan(player, clanName);
		if(clan == null)
		{
			player.sendPacket(new SystemMessage(79));
			return;
		}
		player.sendPacket(new PledgeShowInfoUpdate(clan), new PledgeShowMemberListAll(clan, player), new SystemMessage(189));
		player.updatePledgeClass();
		player.broadcastUserInfo(true);
	}

	public void setLeader(final Player player, final String name)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(236));
			return;
		}
		final Clan clan = player.getClan();
		if(clan == null)
		{
			player.sendPacket(new SystemMessage(190));
			return;
		}
		final ClanMember member = clan.getClanMember(name);
		if(member == null)
		{
			player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.S1IsNotMemberOfTheClan").addString(name));
			this.showChatWindow(player, "villagemaster/clan-20.htm", new Object[0]);
			return;
		}
		clan.setLeader(member);
		clan.updateClanInDB();
		member.setLeader(true);
		member.setPowerGrade(6);
		final Player mem = member.getPlayer();
		if(mem != null)
		{
			if(clan.getLevel() > 3)
				SiegeUtils.addSiegeSkills(mem);
			mem.updatePledgeClass();
		}
		final ClanMember pm = clan.getClanMember(player.getName());
		pm.setLeader(false);
		player.updatePledgeClass();
		pm.setPowerGrade(clan.getAffiliationRank(player.getPledgeType()));
		if(clan.getLevel() > 3)
			SiegeUtils.removeSiegeSkills(player);
		clan.broadcastClanStatus(true, true, false);
		player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.ClanLeaderWillBeChangedFromS1ToS2").addString(player.getName()).addString(name));
	}

	public void createSubPledge(final Player player, final String clanName, int pledgeType, final int minClanLvl, final String leaderName)
	{
		if(Config.DISABLE_ACADEMY && pledgeType == -1)
		{
			player.sendMessage(player.isLangRus() ? "\u0410\u043a\u0430\u0434\u0435\u043c\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "Academy disabled.");
			return;
		}
		if(Config.DISABLE_ROYAL && pledgeType == 100)
		{
			player.sendMessage(player.isLangRus() ? "\u0413\u0432\u0430\u0440\u0434\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "Royal disabled.");
			return;
		}
		if(Config.DISABLE_KNIGHT && pledgeType == 1001)
		{
			player.sendMessage(player.isLangRus() ? "\u0413\u0432\u0430\u0440\u0434\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "Knight disabled.");
			return;
		}
		int subLeaderId = 0;
		ClanMember subLeader = null;
		final Clan clan = player.getClan();
		if(clan == null)
		{
			final SystemMessage sm = new SystemMessage(190);
			player.sendPacket(sm);
			return;
		}
		if(!player.isClanLeader())
		{
			final SystemMessage sm = new SystemMessage(190);
			player.sendPacket(sm);
			return;
		}
		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
		{
			final SystemMessage sm = new SystemMessage(261);
			player.sendPacket(sm);
			return;
		}
		final Clan.SubPledge[] allSubPledges;
		final Clan.SubPledge[] subPledge = allSubPledges = clan.getAllSubPledges();
		for(final Clan.SubPledge element : allSubPledges)
			if(element.getName().equals(clanName))
			{
				final SystemMessage sm2 = new SystemMessage(1855);
				player.sendPacket(sm2);
				return;
			}
		if(ClanTable.getInstance().getClanByName(clanName) != null)
		{
			final SystemMessage sm3 = new SystemMessage(1855);
			player.sendPacket(sm3);
			return;
		}
		if(clan.getLevel() < minClanLvl)
		{
			final SystemMessage sm3 = new SystemMessage(1791);
			player.sendPacket(sm3);
			return;
		}
		if(pledgeType != -1)
		{
			subLeader = clan.getClanMember(leaderName);
			if(subLeader == null || subLeader.getPledgeType() != 0)
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader"));
				return;
			}
			if(subLeader.isClanLeader())
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.YouCantBeASubUnitLeader"));
				return;
			}
			subLeaderId = subLeader.getObjectId();
		}
		pledgeType = clan.createSubPledge(player, pledgeType, subLeaderId, clanName);
		if(pledgeType == 0)
			return;
		clan.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(clan.getSubPledge(pledgeType)));
		SystemMessage sm3;
		if(pledgeType == -1)
		{
			sm3 = new SystemMessage(1741);
			sm3.addString(player.getClan().getName());
		}
		else if(pledgeType >= 1001)
		{
			sm3 = new SystemMessage(1794);
			sm3.addString(player.getClan().getName());
		}
		else if(pledgeType >= 100)
		{
			sm3 = new SystemMessage(1795);
			sm3.addString(player.getClan().getName());
		}
		else
			sm3 = new SystemMessage(189);
		player.sendPacket(sm3);
		if(subLeader != null)
		{
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subLeader));
			if(subLeader.isOnline())
			{
				subLeader.getPlayer().updatePledgeClass();
				subLeader.getPlayer().broadcastUserInfo(true);
			}
		}
	}

	public void assignSubPledgeLeader(final Player player, final String clanName, final String leaderName)
	{
		final Clan clan = player.getClan();
		if(clan == null)
		{
			player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.ClanDoesntExist"));
			return;
		}
		if(!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(236));
			return;
		}
		final Clan.SubPledge[] subPledge = clan.getAllSubPledges();
		int match = -1;
		for(int i = 0; i < subPledge.length; ++i)
			if(subPledge[i].getName().equals(clanName))
			{
				match = i;
				break;
			}
		if(match < 0)
		{
			player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.SubUnitNotFound"));
			return;
		}
		final ClanMember subLeader = clan.getClanMember(leaderName);
		if(subLeader == null || subLeader.getPledgeType() != 0)
		{
			player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader"));
			return;
		}
		if(subLeader.isClanLeader())
		{
			player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.YouCantBeASubUnitLeader"));
			return;
		}
		subPledge[match].setLeaderId(subLeader.getObjectId());
		clan.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge[match]));
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subLeader));
		if(subLeader.isOnline())
		{
			subLeader.getPlayer().updatePledgeClass();
			subLeader.getPlayer().broadcastUserInfo(true);
		}
		player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.VillageMasterInstance.NewSubUnitLeaderHasBeenAssigned"));
	}

	public void levelUpClan(final Player player)
	{
		final Clan clan = player.getClan();
		if(clan == null)
			return;
		if(!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(236));
			return;
		}
		boolean increaseClanLevel = false;
		switch(clan.getLevel())
		{
			case 0:
			{
				if(player.getSp() >= 35000 && player.getAdena() >= Config.ADENA_FOR_LEVEL_1)
				{
					player.setSp(player.getSp() - 35000);
					player.reduceAdena(Config.ADENA_FOR_LEVEL_1, true);
					increaseClanLevel = true;
					break;
				}
				break;
			}
			case 1:
			{
				if(player.getSp() >= 150000 && player.getAdena() >= Config.ADENA_FOR_LEVEL_2)
				{
					player.setSp(player.getSp() - 150000);
					player.reduceAdena(Config.ADENA_FOR_LEVEL_2, true);
					increaseClanLevel = true;
					break;
				}
				break;
			}
			case 2:
			{
				if(player.getSp() >= 500000 && player.getInventory().findItemByItemId(1419) != null)
				{
					player.setSp(player.getSp() - 500000);
					player.getInventory().destroyItemByItemId(1419, 1L, true);
					increaseClanLevel = true;
					break;
				}
				break;
			}
			case 3:
			{
				if(player.getSp() >= 1400000 && player.getInventory().findItemByItemId(3874) != null)
				{
					player.setSp(player.getSp() - 1400000);
					player.getInventory().destroyItemByItemId(3874, 1L, true);
					increaseClanLevel = true;
					break;
				}
				break;
			}
			case 4:
			{
				if(player.getSp() >= 3500000 && player.getInventory().findItemByItemId(3870) != null)
				{
					player.setSp(player.getSp() - 3500000);
					player.getInventory().destroyItemByItemId(3870, 1L, true);
					increaseClanLevel = true;
					break;
				}
				break;
			}
			case 5:
			{
				if(clan.getReputationScore() >= Config.REP_FOR_LEVEL_6 && clan.getMembersCount() >= Config.MEMBERS_FOR_LEVEL_6)
				{
					clan.incReputation(-Config.REP_FOR_LEVEL_6, false, "LvlUpClan");
					increaseClanLevel = true;
					break;
				}
				break;
			}
			case 6:
			{
				if(clan.getReputationScore() >= Config.REP_FOR_LEVEL_7 && clan.getMembersCount() >= Config.MEMBERS_FOR_LEVEL_7)
				{
					clan.incReputation(-Config.REP_FOR_LEVEL_7, false, "LvlUpClan");
					increaseClanLevel = true;
					break;
				}
				break;
			}
			case 7:
			{
				if(clan.getReputationScore() >= Config.REP_FOR_LEVEL_8 && clan.getMembersCount() >= Config.MEMBERS_FOR_LEVEL_8)
				{
					clan.incReputation(-Config.REP_FOR_LEVEL_8, false, "LvlUpClan");
					increaseClanLevel = true;
					break;
				}
				break;
			}
		}
		if(increaseClanLevel)
		{
			player.sendChanges();
			clan.setLevel((byte) (clan.getLevel() + 1));
			clan.updateClanInDB();
			doCast(SkillTable.getInstance().getInfo(5103, 1), player, true);
			if(clan.getLevel() > 3)
				SiegeUtils.addSiegeSkills(player);
			if(clan.getLevel() == 5)
				player.sendPacket(new SystemMessage(1771));
			final PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
			final PledgeStatusChanged ps = new PledgeStatusChanged(clan);
			for(final ClanMember mbr : clan.getMembers())
				if(mbr.isOnline())
				{
					mbr.getPlayer().updatePledgeClass();
					mbr.getPlayer().sendPacket(Msg.CLANS_SKILL_LEVEL_HAS_INCREASED, pu, ps);
					mbr.getPlayer().broadcastUserInfo(true);
				}
		}
		else
			player.sendPacket(new SystemMessage(275));
	}

	public void createAlly(final Player player, final String allyName)
	{
		if(Config.DEBUG)
			VillageMasterInstance._log.info(player.getObjectId() + "(" + player.getName() + ") requested ally creation from " + getObjectId() + "(" + getName() + ")");
		if(!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(504));
			return;
		}
		if(player.getClan().getAllyId() != 0)
		{
			player.sendPacket(new SystemMessage(502));
			return;
		}
		if(allyName.length() > 16)
		{
			player.sendPacket(new SystemMessage(507));
			return;
		}
		if(!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
		{
			player.sendPacket(new SystemMessage(506));
			return;
		}
		if(player.getClan().getLevel() < 5)
		{
			player.sendPacket(new SystemMessage(549));
			return;
		}
		if(ClanTable.getInstance().getAllyByName(allyName) != null)
		{
			player.sendPacket(new SystemMessage(508));
			return;
		}
		if(!player.getClan().canCreateAlly())
		{
			player.sendPacket(new SystemMessage(505));
			return;
		}
		final Alliance alliance = ClanTable.getInstance().createAlliance(player, allyName);
		if(alliance == null)
			return;
		player.broadcastUserInfo(true);
		player.sendMessage("Alliance " + allyName + " has been created.");
	}

	private void dissolveAlly(final Player player)
	{
		if(player == null || player.getAlliance() == null)
			return;
		if(!player.isAllyLeader())
		{
			player.sendPacket(new SystemMessage(464));
			return;
		}
		if(player.getAlliance().getMembersCount() > 1)
		{
			player.sendPacket(new SystemMessage(524));
			return;
		}
		ClanTable.getInstance().dissolveAlly(player);
	}

	private boolean isAvailableClass(final PlayerClass c)
	{
		final Race npcRace = getVillageMasterRace();
		final ClassType npcTeachType = getVillageMasterTeachType();
		return npcRace == Race.darkelf && c.isOfRace(Race.darkelf) || npcRace == Race.orc && c.isOfRace(Race.orc) || npcRace == Race.dwarf && c.isOfRace(Race.dwarf) || c.isOfType(npcTeachType) && npcRace == Race.human && (c.isOfRace(Race.human) || c.isOfRace(Race.elf));
	}

	private Set<PlayerClass> getAvailableSubClasses(final Player player)
	{
		final int charClassId = player.getBaseClassId();
		final Race npcRace = getVillageMasterRace();
		final ClassType npcTeachType = getVillageMasterTeachType();
		final PlayerClass currClass = PlayerClass.values()[charClassId];
		final Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return null;
		availSubs.remove(currClass);
		for(final PlayerClass availSub : availSubs)
		{
			for(final SubClass subClass : player.getSubClasses().values())
				if(availSub.ordinal() == subClass.getClassId())
					availSubs.remove(availSub);
				else
				{
					final ClassId parent = ClassId.values()[availSub.ordinal()].getParent();
					if(parent != null && parent.getId() == subClass.getClassId())
						availSubs.remove(availSub);
					else
					{
						final ClassId subParent = ClassId.values()[subClass.getClassId()].getParent();
						if(subParent == null || subParent.getId() != availSub.ordinal())
							continue;
						availSubs.remove(availSub);
					}
				}
			if(Config.ANY_SUBCLASS_MASTER)
			{
				if(!availSub.isOfRace(Race.human) && !availSub.isOfRace(Race.elf))
				{
					if(availSub.isOfRace(npcRace))
						continue;
					availSubs.remove(availSub);
				}
				else
				{
					if(availSub.isOfType(npcTeachType))
						continue;
					availSubs.remove(availSub);
				}
			}
			else
			{
				if(isAvailableClass(availSub))
					continue;
				availSubs.remove(availSub);
			}
		}
		return availSubs;
	}

	private String formatClassForDisplay(final PlayerClass className)
	{
		String classNameStr = className.toString();
		final char[] charArray = classNameStr.toCharArray();
		for(int i = 1; i < charArray.length; ++i)
			if(Character.isUpperCase(charArray[i]))
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);
		return classNameStr;
	}

	private Race getVillageMasterRace()
	{
		switch(getTemplate().getRace())
		{
			case 14:
			{
				return Race.human;
			}
			case 15:
			{
				return Race.elf;
			}
			case 16:
			{
				return Race.darkelf;
			}
			case 17:
			{
				return Race.orc;
			}
			case 18:
			{
				return Race.dwarf;
			}
			default:
			{
				return null;
			}
		}
	}

	private ClassType getVillageMasterTeachType()
	{
		switch(getNpcId())
		{
			case 30031:
			case 30037:
			case 30070:
			case 30120:
			case 30141:
			case 30191:
			case 30289:
			case 30305:
			case 30358:
			case 30359:
			case 30857:
			case 30905:
			case 31279:
			case 31336:
			case 32095:
			{
				return ClassType.Priest;
			}
			case 30115:
			case 30154:
			case 30174:
			case 30175:
			case 30176:
			case 30694:
			case 30854:
			case 31285:
			case 31288:
			case 31326:
			case 31331:
			case 31755:
			case 31977:
			case 31996:
			case 32098:
			{
				return ClassType.Mystic;
			}
			default:
			{
				return ClassType.Fighter;
			}
		}
	}

	public void showClanSkillWindow(final Player player)
	{
		if(player == null || player.getClan() == null)
			return;
		if(!Config.ALLOW_CLANSKILLS)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("Not available now, try later.<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Clan);
		int counts = 0;
		final PledgeSkillLearn[] availablePledgeSkills;
		final PledgeSkillLearn[] skills = availablePledgeSkills = SkillTree.getInstance().getAvailablePledgeSkills(player);
		for(final PledgeSkillLearn s : availablePledgeSkills)
			if(s != null)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk != null)
				{
					final int cost = s.getRepCost();
					asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
					++counts;
				}
			}
		if(counts == 0)
		{
			final NpcHtmlMessage html2 = new NpcHtmlMessage(getObjectId());
			if(player.getClan().getLevel() < 8)
			{
				final SystemMessage sm = new SystemMessage(607);
				if(player.getClan().getLevel() < 5)
					sm.addNumber(Integer.valueOf(5));
				else
					sm.addNumber(Integer.valueOf(player.getClan().getLevel() + 1));
				player.sendPacket(sm);
			}
			else
			{
				final TextBuilder sb2 = new TextBuilder();
				sb2.append("<html><head><body>");
				sb2.append("You've learned all skills available for your Clan.<br>");
				sb2.append("</body></html>");
				html2.setHtml(sb2.toString());
				player.sendPacket(html2);
			}
		}
		else
			player.sendPacket(asl);
		player.sendActionFailed();
	}

	static
	{
		VillageMasterInstance._log = LoggerFactory.getLogger(VillageMasterInstance.class);
	}
}
