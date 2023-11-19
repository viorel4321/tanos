package l2s.gameserver.model.instances;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public class CastleBlacksmithInstance extends NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	public CastleBlacksmithInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(this != player.getTarget())
			player.setTarget(this);
		else if(!this.isInRange(player, 150L))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendActionFailed();
		}
		else
		{
			if(player.isMoving)
				player.stopMove();
			player.turn(this, 3000);
			if(CastleManorManager.getInstance().isDisabled())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("npcdefault.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
				showMessageWindow(player, 0);
			player.sendActionFailed();
		}
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		if(CastleManorManager.getInstance().isDisabled())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("npcdefault.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}
		final int condition = validateCondition(player);
		if(condition <= 0)
			return;
		if(condition == 1)
			return;
		if(condition == 2)
			if(command.startsWith("Chat"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch(IndexOutOfBoundsException ex)
				{}
				catch(NumberFormatException ex2)
				{}
				showMessageWindow(player, val);
			}
			else
				super.onBypassFeedback(player, command);
	}

	private void showMessageWindow(final Player player, final int val)
	{
		player.sendActionFailed();
		String filename = "castle/blacksmith/castleblacksmith-no.htm";
		final int condition = validateCondition(player);
		if(condition > 0)
			if(condition == 1)
				filename = "castle/blacksmith/castleblacksmith-busy.htm";
			else if(condition == 2)
				if(val == 0)
					filename = "castle/blacksmith/castleblacksmith.htm";
				else
					filename = "castle/blacksmith/castleblacksmith-" + val + ".htm";
		final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%castleid%", Integer.toString(getCastle().getId()));
		player.sendPacket(html);
	}

	protected int validateCondition(final Player player)
	{
		if(player.isGM())
			return 2;
		if(getCastle() != null && getCastle().getId() > 0 && player.getClan() != null)
		{
			SiegeEvent<?, ?> siegeEvent = getCastle().getSiegeEvent();
			if(siegeEvent != null && siegeEvent.isInProgress())
				return 1;
			if(getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 0x10000) == 0x10000)
				return 2;
		}
		return 0;
	}
}
