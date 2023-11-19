package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.HennaTable;
import l2s.gameserver.tables.HennaTreeTable;
import l2s.gameserver.templates.HennaTemplate;

public class RequestHennaEquip extends L2GameClientPacket
{
	private static final Logger _log;
	private int _symbolId;

	@Override
	public void readImpl()
	{
		_symbolId = readD();
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
		final HennaTemplate template = HennaTable.getInstance().getTemplate(_symbolId);
		if(template == null)
			return;
		final HennaInstance temp = new HennaInstance(template);
		boolean cheater = true;
		for(final HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId()))
			if(h.getSymbolId() == temp.getSymbolId())
			{
				cheater = false;
				break;
			}
		if(cheater)
		{
			activeChar.sendPacket(new SystemMessage(899));
			return;
		}
		final PcInventory inventory = activeChar.getInventory();
		final ItemInstance item = inventory.getItemByItemId(temp.getItemIdDye());
		if(item != null && item.getIntegerLimitedCount() >= temp.getAmountDyeRequire() && activeChar.getAdena() >= temp.getPrice() && activeChar.addHenna(temp))
		{
			activeChar.sendPacket(new SystemMessage(302).addString(temp.getName()));
			activeChar.sendPacket(new SystemMessage(877));
			inventory.reduceAdena(temp.getPrice());
			if(inventory.destroyItemByItemId(temp.getItemIdDye(), temp.getAmountDyeRequire(), true) == null)
				RequestHennaEquip._log.info("RequestHennaEquip[50]: Item not found!!!");
		}
		else
			activeChar.sendPacket(new SystemMessage(899));
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestHennaEquip.class);
	}
}
