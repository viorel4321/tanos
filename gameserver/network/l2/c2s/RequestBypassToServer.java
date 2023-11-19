package l2s.gameserver.network.l2.c2s;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import l2s.gameserver.data.xml.holder.MultiSellHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.communitybbs.CommunityBoard;
import l2s.gameserver.handler.AdminCommandHandler;
import l2s.gameserver.handler.IAdminCommandHandler;
import l2s.gameserver.handler.IVoicedCommandHandler;
import l2s.gameserver.handler.VoicedCommandHandler;
import l2s.gameserver.model.BypassManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.utils.Log;

public class RequestBypassToServer extends L2GameClientPacket
{
	private static final Logger _log;
	private BypassManager.DecodedBypass bp;
	private int command_success;

	public RequestBypassToServer()
	{
		bp = null;
	}

	@Override
	protected void readImpl()
	{
		final String bypass = readS();
		if(!bypass.isEmpty())
			bp = getClient().getActiveChar().decodeBypass(bypass);
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || bp == null)
			return;

		if(activeChar.isKeyBlocked() && !bp.bypass.startsWith("scripts_services.RateBonus:keyCheck") && !bp.bypass.startsWith("user_charkeyset") && !bp.bypass.startsWith("user_lang"))
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFrozen() && !bp.bypass.startsWith("user_ag_"))
		{
			activeChar.sendActionFailed();
			return;
		}
		try
		{
			NpcInstance npc = activeChar.getLastNpc();
			final GameObject target = activeChar.getTarget();
			if(target != null && target.isNpc())
				npc = (NpcInstance) target;
			command_success = 0;
			if(bp.bbs)
				CommunityBoard.getInstance().handleCommands(getClient(), bp.bypass);
			else if(bp.bypass.startsWith("admin_"))
			{
				if(!activeChar.getPlayerAccess().IsGM && !activeChar.getPlayerAccess().CanUseGMCommand)
					return;
				final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(bp.bypass);
				if(ach != null)
				{
					try
					{
						command_success = ach.useAdminCommand(bp.bypass, activeChar) ? 1 : 0;
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					Log.LogCommand(activeChar, bp.bypass, command_success);
				}
				else
					RequestBypassToServer._log.warn("No handler registered for bypass '" + bp.bypass + "'");
			}
			else if(bp.bypass.startsWith("player_help "))
				playerHelp(activeChar, bp.bypass.substring(12), npc);
			else if(bp.bypass.startsWith("scripts_"))
			{
				final String command = bp.bypass.substring(8).trim();
				final String[] word = command.split("\\s+");
				final String[] args = command.substring(word[0].length()).trim().split("\\s+");
				final String[] path = word[0].split(":");
				if(path.length != 2)
				{
					RequestBypassToServer._log.warn("Bad Script bypass!");
					return;
				}
				Map<String, Object> variables = null;
				if(npc != null)
				{
					variables = new HashMap<String, Object>(1);
					variables.put("npc", npc.getRef());
				}
				if(word.length == 1)
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], variables);
				else
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], new Object[] { args }, variables);
			}
			else if(bp.bypass.startsWith("user_"))
			{
				final String command = bp.bypass.substring(5).trim();
				final String word2 = command.split("\\s+")[0];
				final String args2 = command.substring(word2.length()).trim();
				final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word2);
				if(vch != null)
					vch.useVoicedCommand(word2, activeChar, args2);
				else
					RequestBypassToServer._log.warn("Unknow voiced command '" + word2 + "'");
			}
			else if(bp.bypass.startsWith("npc_"))
			{
				final int endOfId = bp.bypass.indexOf(95, 5);
				String id;
				if(endOfId > 0)
					id = bp.bypass.substring(4, endOfId);
				else
					id = bp.bypass.substring(4);
				final GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
				if(object != null && object.isNpc() && endOfId > 0 && activeChar.isInRange(object.getLoc(), 150L))
				{
					activeChar.setLastNpc((NpcInstance) object);
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
				}
			}
			else if(bp.bypass.startsWith("oly_"))
			{
				if(!Config.ENABLE_OLYMPIAD_SPECTATING)
					return;
				if(!activeChar.inObserverMode() || activeChar.getOlympiadObserveId() == -1)
					return;
				final int arenaId = Integer.parseInt(bp.bypass.substring(4));
				if(activeChar.getOlympiadObserveId() == arenaId || Olympiad._manager == null || Olympiad._manager.getOlympiadInstance(arenaId) == null)
					return;
				activeChar.switchOlympiadObserverArena(arenaId);
			}
			else if(bp.bypass.startsWith("_diary"))
			{
				final String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				final StringTokenizer st = new StringTokenizer(params, "&");
				final int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				final int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				final int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if(heroid > 0)
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
			}
			else if(bp.bypass.startsWith("manor_menu_select?"))
			{
				final GameObject object2 = activeChar.getTarget();
				if(object2 != null && object2.isNpc())
					((NpcInstance) object2).onBypassFeedback(activeChar, bp.bypass);
			}
			else if(bp.bypass.startsWith("Quest "))
			{
				final String p = bp.bypass.substring(6).trim();
				final int idx = p.indexOf(32);
				try
				{
					if(idx < 0)
						activeChar.processQuestEvent(Integer.parseInt(p.split("_")[1]), "", npc);
					else
						activeChar.processQuestEvent(Integer.parseInt(p.substring(0, idx).split("_")[1]), p.substring(idx).trim(), npc);
				}
				catch(NumberFormatException nfe)
				{
					_log.error("Error while parse NPC bypass command: " + bp.bypass, nfe);
				}
			}
			else if(bp.bypass.startsWith("interface?"))
			{
				String command = bp.bypass.substring(10).trim();
				if(command.equalsIgnoreCase("exp_on") || command.equalsIgnoreCase("exp_off"))
				{
					if(activeChar.getVarInt("NoExp", 0) > 0)
					{
						activeChar.unsetVar("NoExp");
						activeChar.sendPacket(new CustomMessage("interface.NoExp.off"));
					}
					else
					{
						activeChar.setVar("NoExp", "1");
						activeChar.sendPacket(new CustomMessage("interface.NoExp.on"));
					}
				}
			}
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			String st2 = "Bad RequestBypassToServer: " + bp.bypass;
			final GameObject target2 = activeChar.getTarget();
			if(activeChar.getTarget() != null && target2.isNpc())
				st2 = st2 + " via NPC #" + ((NpcInstance) target2).getNpcId();
			RequestBypassToServer._log.error(st2, e2);
		}
	}

	private void playerHelp(final Player activeChar, final String path, final NpcInstance npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(activeChar, npc);
		html.setFile(path);
		activeChar.sendPacket(html);
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestBypassToServer.class);
	}
}
