package l2s.gameserver.network.l2.c2s;

import java.nio.BufferUnderflowException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class UseItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(UseItem.class);

	private int _objectId;
	private boolean _ctrlPressed;

	@Override
	public void readImpl()
	{
		try
		{
			_objectId = readD();
			_ctrlPressed = readD() == 1;
		}
		catch(BufferUnderflowException e)
		{
			_log.warn(e.getMessage());
			_log.info("Attention! Possible cheater found! Login: " + getClient().getLogin());
		}
	}

	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setActive();

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getVar("jailed") != null)
		{
			activeChar.sendMessage("You cannot use items in Jail.");
			return;
		}

		synchronized(activeChar.getInventory())
		{
			ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
			if(item == null || item.isArrow())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(!item.isArmor())
			{
				if(System.currentTimeMillis() - activeChar.getLastItemPacket() < Config.ITEM_PACKET_DELAY)
				{
					activeChar.addItemPacket();
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.getItemPackets() > Config.MAX_ITEM_PACKETS && System.currentTimeMillis() - activeChar.getLastItemPacket() < Config.ITEM_USE_DELAY)
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.getItemPackets() > Config.MAX_ITEM_PACKETS)
					activeChar.clearItemPackets();

				activeChar.setLastItemPacket();
			}

			activeChar.useItem(item, _ctrlPressed, true);
		}
	}
}
