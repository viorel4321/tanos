package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExConfirmVariationGemstone;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestConfirmGemStone extends L2GameClientPacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private int _gemstoneCount;

	@Override
	public void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount = readD();
	}

	@Override
	public void runImpl()
	{
		if(_gemstoneCount <= 0)
			return;

		final Player activeChar = getClient().getActiveChar();
		final ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		final ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		final ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);
		if(targetItem == null || refinerItem == null || gemstoneItem == null)
			return;

		final int gemstoneItemId = gemstoneItem.getTemplate().getItemId();
		switch(targetItem.getTemplate().getItemGrade())
		{
			case C:
			{
				if(gemstoneItemId != 2130)
				{
					activeChar.sendPacket(new SystemMessage(1960));
					return;
				}
				if(_gemstoneCount < 20)
				{
					activeChar.sendPacket(new SystemMessage(1961));
					return;
				}
				_gemstoneCount = 20;
				break;
			}
			case B:
			{
				if(gemstoneItemId != 2130)
				{
					activeChar.sendPacket(new SystemMessage(1960));
					return;
				}
				if(_gemstoneCount < 30)
				{
					activeChar.sendPacket(new SystemMessage(1961));
					return;
				}
				_gemstoneCount = 30;
				break;
			}
			case A:
			{
				if(gemstoneItemId != 2131)
				{
					activeChar.sendPacket(new SystemMessage(1960));
					return;
				}
				if(_gemstoneCount < 20)
				{
					activeChar.sendPacket(new SystemMessage(1961));
					return;
				}
				_gemstoneCount = 20;
				break;
			}
			case S:
			{
				if(gemstoneItemId != 2131)
				{
					activeChar.sendPacket(new SystemMessage(1960));
					return;
				}
				if(_gemstoneCount < 25)
				{
					activeChar.sendPacket(new SystemMessage(1961));
					return;
				}
				_gemstoneCount = 25;
				break;
			}
			default:
			{
				activeChar.sendPacket(new SystemMessage(1960));
				return;
			}
		}
		activeChar.sendPacket(new ExConfirmVariationGemstone(_gemstoneItemObjId, _gemstoneCount));
		activeChar.sendPacket(new SystemMessage(1984));
	}
}
