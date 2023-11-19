package l2s.gameserver.model.instances;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Couple;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public class WeddingManagerInstance extends NpcInstance
{
	public WeddingManagerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		player.setLastNpc(this);
		final String filename = "wedding/start.htm";
		final String repl = "";
		final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
		html.setFile(filename);
		html.replace("%replace%", repl);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		String filename = "wedding/start.htm";
		String replace = "";
		if(player.getPartnerId() == 0)
		{
			filename = "wedding/nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		final Player ptarget = GameObjectsStorage.getPlayer(player.getPartnerId());
		if(ptarget == null || !ptarget.isOnline())
		{
			filename = "wedding/notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		if(player.isMaried())
		{
			filename = "wedding/already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		if(command.startsWith("AcceptWedding"))
		{
			player.setMaryAccepted(true);
			final Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
			couple.marry();
			player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.WeddingManagerMessage"));
			player.setMaried(true);
			player.setMaryRequest(false);
			ptarget.sendMessage(new CustomMessage("l2s.gameserver.model.instances.WeddingManagerMessage"));
			ptarget.setMaried(true);
			ptarget.setMaryRequest(false);
			if(Config.WEDDING_CUPID_BOW)
			{
				player.getInventory().addItem(9140, 1L);
				ptarget.getInventory().addItem(9140, 1L);
			}
			player.broadcastPacket(new MagicSkillUse(player, player, 2230, 1, 1, 0L));
			ptarget.broadcastPacket(new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0L));
			player.broadcastPacket(new MagicSkillUse(player, player, 2025, 1, 1, 0L));
			ptarget.broadcastPacket(new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0L));
			Announcements.getInstance().announceToAll("Gratulations, " + player.getName() + " and " + ptarget.getName() + " has married.");
			filename = "wedding/accepted.htm";
			replace = ptarget.getName();
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		if(player.isMaryRequest())
		{
			if(Config.WEDDING_FORMALWEAR && !isWearingFormalWear(player))
			{
				filename = "wedding/noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			filename = "wedding/ask.htm";
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
		}
		else if(command.startsWith("AskWedding"))
		{
			if(Config.WEDDING_FORMALWEAR && !isWearingFormalWear(player))
			{
				filename = "wedding/noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			if(Config.WEDDING_PRICE > 0)
				if(Config.WEDDING_ITEM == 57)
				{
					if(player.getAdena() < Config.WEDDING_PRICE)
					{
						player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						return;
					}
					player.reduceAdena(Config.WEDDING_PRICE, true);
				}
				else
				{
					final ItemInstance pay = player.getInventory().getItemByItemId(Config.WEDDING_ITEM);
					if(pay == null || pay.getCount() < Config.WEDDING_PRICE)
					{
						player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
						return;
					}
					player.getInventory().destroyItem(pay, Config.WEDDING_PRICE, true);
				}
			player.setMaryAccepted(true);
			ptarget.setMaryRequest(true);
			replace = ptarget.getName();
			filename = "wedding/requested.htm";
			sendHtmlMessage(player, filename, replace);
		}
		else
		{
			if(command.startsWith("DeclineWedding"))
			{
				player.setMaryRequest(false);
				ptarget.setMaryRequest(false);
				player.setMaryAccepted(false);
				ptarget.setMaryAccepted(false);
				player.sendMessage("You declined");
				ptarget.sendMessage("Your partner declined");
				replace = ptarget.getName();
				filename = "wedding/declined.htm";
				sendHtmlMessage(ptarget, filename, replace);
				return;
			}
			if(player.isMaryAccepted())
			{
				filename = "wedding/waitforpartner.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			sendHtmlMessage(player, filename, replace);
		}
	}

	private static boolean isWearingFormalWear(final Player player)
	{
		return player != null && player.getInventory() != null && player.getInventory().getPaperdollItemId(10) == 6408;
	}

	private void sendHtmlMessage(final Player player, final String filename, final String replace)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
		html.setFile(filename);
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}
