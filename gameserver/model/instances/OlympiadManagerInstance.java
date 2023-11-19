package l2s.gameserver.model.instances;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2s.gameserver.network.l2.s2c.ExHeroList;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public class OlympiadManagerInstance extends NpcInstance
{
	private static Logger _log;
	private static final short _gatePass = 6651;
	private static final int[] _monuments;

	public OlympiadManagerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!Config.ENABLE_OLYMPIAD)
			return;
		player.setLastNpc(this);
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		if(command.startsWith("OlympiadDesc"))
		{
			final int val = Integer.parseInt(command.substring(13, 14));
			final String suffix = command.substring(14);
			this.showChatWindow(player, val, suffix);
		}
		else if(command.startsWith("OlympiadNoble"))
		{
			final int classId = player.getClassId().getId();
			if(!Config.ENABLE_OLYMPIAD || !player.isNoble() || classId < 88 || classId > 118)
				return;
			final int val2 = Integer.parseInt(command.substring(14));
			switch(val2)
			{
				case 1:
				{
					Olympiad.unRegisterNoble(player, false);
					break;
				}
				case 2:
				{
					int classed = 0;
					int nonClassed = 0;
					final int[] array = Olympiad.getWaitingList();
					if(array != null)
					{
						classed = array[0];
						nonClassed = array[1];
					}
					final NpcHtmlMessage reply = new NpcHtmlMessage(_objectId);
					final StringBuffer replyMSG = new StringBuffer("<html><body>");
					replyMSG.append("The number of people on the waiting list for Grand Olympiad<center><img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3><table width=270 border=0 bgcolor=\"000000\"><tr><td align=\"left\">General</td><td align=\"right\">" + classed + "</td></tr><tr><td align=\"left\">Not class-defined</td><td align=\"right\">" + nonClassed + "</td></tr></table><img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3><button value=\"Back\" action=\"bypass -h npc_%objectId%_OlympiadDesc 2a\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
					replyMSG.append("</body></html>");
					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				}
				case 3:
				{
					final NpcHtmlMessage reply = new NpcHtmlMessage(_objectId);
					final StringBuffer replyMSG = new StringBuffer("<html><body>");
					replyMSG.append("There are " + Olympiad.getNoblePoints(player.getObjectId()) + " Grand Olympiad points granted for this event.<br><br><a action=\"bypass -h npc_%objectId%_OlympiadDesc 2a\">Return</a>");
					replyMSG.append("</body></html>");
					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				}
				case 4:
				{
					Olympiad.registerParticipant(player, false);
					break;
				}
				case 5:
				{
					Olympiad.registerParticipant(player, true);
					break;
				}
				case 6:
				{
					final int passes = Olympiad.getNoblessePasses(player.getObjectId());
					if(passes > 0)
					{
						player.getInventory().addItem(6651, passes);
						player.sendPacket(new SystemMessage(53).addNumber(Integer.valueOf(passes)).addItemName(Short.valueOf((short) 6651)));
						break;
					}
					final NpcHtmlMessage reply = new NpcHtmlMessage(_objectId);
					final StringBuffer replyMSG = new StringBuffer("<html><body>");
					replyMSG.append("Grand Olympiad Manager:<br>");
					replyMSG.append("Sorry, you don't have enough points for a Noblesse Gate Pass. Better luck next time.<br>");
					replyMSG.append("<a action=\"bypass -h npc_%objectId%_OlympiadDesc 4a\">Return</a>");
					replyMSG.append("</body></html>");
					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				}
				case 7:
				{
					player.setLastNpcId(getNpcId());
					MultiSellHolder.getInstance().SeparateAndSend(102, player, 0.0);
					break;
				}
				case 8:
				{
					final NpcHtmlMessage reply = new NpcHtmlMessage(_objectId);
					final StringBuffer replyMSG = new StringBuffer("<html><body>");
					replyMSG.append("Your Grand Olympiad Score from previous period is " + Olympiad.getNoblePointsPast(player.getObjectId()) + " point(s).<br>");
					replyMSG.append("<a action=\"bypass -h npc_%objectId%_OlympiadDesc 4a\">Return</a>");
					replyMSG.append("</body></html>");
					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				}
				default:
				{
					OlympiadManagerInstance._log.warn("Olympiad System: Couldn't send packet for request " + val2);
					break;
				}
			}
		}
		else if(command.startsWith("Olympiad"))
		{
			if(!Config.ENABLE_OLYMPIAD)
				return;
			final int val = Integer.parseInt(command.substring(9, 10));
			NpcHtmlMessage reply2 = new NpcHtmlMessage(player, this);
			switch(val)
			{
				case 1:
				{
					final String[] matches = Olympiad.getAllTitles();
					reply2 = new NpcHtmlMessage(_objectId);
					final StringBuffer replyMSG2 = new StringBuffer("<html><body><br>");
					replyMSG2.append("Grand Olympiad Games Overview<br>* Caution: Please note, if you watch an Olympiad game, the summoning of your Servitors or Pets will be cancelled. Be careful.<br><br>");
					for(int i = 0; i < matches.length; ++i)
						replyMSG2.append("<br1><a action=\"bypass -h npc_%objectId%_Olympiad 3_" + i + "\">" + matches[i] + "</a>");
					replyMSG2.append("<br><img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");
					replyMSG2.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat 0\" back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\" width=67 height=19></center>");
					replyMSG2.append("</body></html>");
					reply2.setHtml(replyMSG2.toString());
					player.sendPacket(reply2);
					break;
				}
				case 2:
				{
					final int classId2 = Integer.parseInt(command.substring(11));
					final boolean ru = player.isLangRus();
					final String head = ru ? "\u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430 \u0412\u0435\u043b\u0438\u043a\u043e\u0439 \u041e\u043b\u0438\u043c\u043f\u0438\u0430\u0434\u044b" : "Grand Olympiad Ranking";
					final String back = ru ? "\u041d\u0430\u0437\u0430\u0434" : "Back";
					if(classId2 >= 88)
					{
						reply2 = new NpcHtmlMessage(_objectId);
						final StringBuffer replyMSG2 = new StringBuffer("<html><body>");
						replyMSG2.append("<br><center>" + head);
						replyMSG2.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");
						final List<String> names = OlympiadDatabase.getClassLeaderBoard(classId2);
						if(names.size() != 0)
						{
							replyMSG2.append("<table width=300 border=0 bgcolor=\"000000\"><tr><td height=255>");
							replyMSG2.append("<table width=300>");
							int index = 1;
							for(final String name : names)
							{
								replyMSG2.append("<tr>");
								replyMSG2.append("<td><center>" + index + "</center></td>");
								replyMSG2.append("<td><center>" + name + "</center></td>");
								replyMSG2.append("</tr>");
								++index;
							}
							replyMSG2.append("<tr><td><br><br><br></td><td></td></tr>");
							replyMSG2.append("</table>");
							replyMSG2.append("</td></tr></table>");
						}
						else
							replyMSG2.append("<table><tr><td height=255></td></tr></table>");
						replyMSG2.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");
						replyMSG2.append("<button value=\"" + back + "\" action=\"bypass -h npc_%objectId%_OlympiadDesc 3a\" back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\" width=67 height=19>");
						replyMSG2.append("</center>");
						replyMSG2.append("</body></html>");
						reply2.setHtml(replyMSG2.toString());
						player.sendPacket(reply2);
						break;
					}
					break;
				}
				case 3:
				{
					if(!Config.ENABLE_OLYMPIAD_SPECTATING)
						break;
					Olympiad.addSpectator(Integer.parseInt(command.substring(11)), player, true);
					break;
				}
				case 4:
				{
					player.sendPacket(new ExHeroList());
					break;
				}
				default:
				{
					OlympiadManagerInstance._log.warn("Olympiad System: Couldn't send packet for request " + val);
					break;
				}
			}
		}
		else if(command.equalsIgnoreCase("becomeHero") && ArrayUtils.contains(OlympiadManagerInstance._monuments, getNpcId()))
		{
			if(player.isHero())
				return;
			if(!Hero.isNotActiveHero(player.getObjectId()))
			{
				OlympiadManagerInstance._log.warn("Player " + player.toString() + " tried to become a hero!");
				return;
			}
			Hero.getInstance().becomeHero(player);
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void showChatWindow(final Player player, final int val, final String suffix)
	{
		String filename = "olympiad/";
		filename = filename + "noble_desc" + val;
		filename += suffix != null ? suffix + ".htm" : ".htm";
		if(filename.equals("olympiad/noble_desc0.htm"))
			filename = "olympiad/noble_main.htm";
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		final int npcId = getTemplate().npcId;
		String filename = getHtmlPath(npcId, val, player);
		boolean main = false;
		switch(npcId)
		{
			case 31688:
			{
				if(player.isNoble())
				{
					filename = "olympiad/noble_main.htm";
					break;
				}
				break;
			}
			case 31690:
			case 31769:
			case 31770:
			case 31771:
			case 31772:
			{
				if(player.isRealHero() || Hero.isNotActiveHero(player.getObjectId()))
				{
					filename = "olympiad/hero_main.htm";
					main = true;
					break;
				}
				filename = "olympiad/hero_main2.htm";
				break;
			}
		}
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this, filename, val);
		if(main)
		{
			String hiddenText = "";
			if(Hero.isNotActiveHero(player.getObjectId()))
				hiddenText = "<a action=\"bypass -h npc_" + getObjectId() + "_OlympiadDesc 0hero\">\"" + (player.isLangRus() ? "\u042f \u0445\u043e\u0447\u0443 \u0431\u044b\u0442\u044c \u0413\u0435\u0440\u043e\u0435\u043c" : "I want to be a Hero") + ".\"</a><br>";
			html.replace("%hero%", hiddenText);
		}
		player.sendPacket(html);
	}

	static
	{
		OlympiadManagerInstance._log = LoggerFactory.getLogger(OlympiadManagerInstance.class);
		_monuments = new int[] { 31690, 31769, 31770, 31771, 31772 };
	}
}
