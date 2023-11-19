package l2s.gameserver.model.instances;

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SubClass;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.PlayerClass;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.CharTemplateTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Util;

public final class SubclassMasterInstance extends NpcInstance
{
	private static Logger _log;

	public SubclassMasterInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		if(command.startsWith("Subclass"))
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
					if(playerClassList.size() < 1)
					{
						content.append("You can't change subclasses when you don't have a subclass to begin with.<br><a action=\"bypass -h npc_" + getObjectId() + "_Subclass 1\">Add subclass.</a>");
						break;
					}
					content.append("Please choose a subclass to change to. If the one you are looking for is not here, please seek out the appropriate master for that class.<br>");
					content.append("Base class: <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(baseClassId) + "</font><br><br>");
					for(final SubClass subClass2 : playerClassList.values())
					{
						final int subClassId = subClass2.getClassId();
						content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + subClassId + "\">" + CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
					}
					break;
				}
				case 3:
				{
					content.append("Addition of a sub-class:<br>Which of the following sub-classes would you like to change?<br>");
					for(final SubClass sub : playerClassList.values())
					{
						content.append("<br>");
						if(!sub.isBase())
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 " + sub.getClassId() + "\">" + CharTemplateTable.getClassNameById(sub.getClassId()) + "</a><br>");
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
		return "custom/" + pom + ".htm";
	}

	private Set<PlayerClass> getAvailableSubClasses(final Player player)
	{
		final int charClassId = player.getBaseClassId();
		final PlayerClass currClass = PlayerClass.values()[charClassId];
		final Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return null;
		availSubs.remove(currClass);
		for(final PlayerClass availSub : availSubs)
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

	static
	{
		SubclassMasterInstance._log = LoggerFactory.getLogger(SubclassMasterInstance.class);
	}
}
