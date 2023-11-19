package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.items.PetInventory;
import l2s.gameserver.network.l2.s2c.PetItemList;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestGiveItemToPet extends L2GameClientPacket
{
	private int _objectId;
	private int _amount;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_amount = readD();
	}

	@Override
	public void runImpl()
	{
		if(_amount < 1)
			return;
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		final PetInstance pet = (PetInstance) activeChar.getServitor();
		if(pet == null || pet.isDead())
		{
			this.sendPacket(new SystemMessage(590));
			return;
		}
		if(activeChar.getPrivateStoreType() != 0)
		{
			this.sendPacket(new SystemMessage(1065));
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(1470));
			return;
		}
		if(_objectId == pet.getControlItemId())
		{
			activeChar.sendActionFailed();
			return;
		}
		final PetInventory petInventory = pet.getInventory();
		final PcInventory playerInventory = activeChar.getInventory();
		final ItemInstance playerItem = playerInventory.getItemByObjectId(_objectId);
		if(playerItem == null || playerItem.getObjectId() == pet.getControlItemId())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(playerItem.isHeroItem())
		{
			activeChar.sendActionFailed();
			return;
		}
		int slots = 0;
		final long weight = playerItem.getTemplate().getWeight() * _amount;
		if(!playerItem.getTemplate().isStackable() || pet.getInventory().getItemByItemId(playerItem.getItemId()) == null)
			slots = 1;
		if(!pet.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(new SystemMessage(546));
			return;
		}
		if(!pet.getInventory().validateCapacity(slots) || pet.getInventory().getItemByItemId(playerItem.getItemId()) != null && pet.getInventory().getItemByItemId(playerItem.getItemId()).getCount() + _amount > Integer.MAX_VALUE || _amount > Integer.MAX_VALUE)
		{
			activeChar.sendPacket(new SystemMessage(545));
			return;
		}
		if(!playerItem.canBeDropped(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}
		if(_amount >= playerItem.getIntegerLimitedCount())
		{
			playerInventory.dropItem(_objectId, playerItem.getIntegerLimitedCount());
			playerItem.setCustomFlags(playerItem.getCustomFlags() | 0x80, true);
			petInventory.addItem(playerItem);
		}
		else
			petInventory.addItem(playerInventory.dropItem(_objectId, _amount));
		pet.sendChanges();
		activeChar.sendPacket(new PetItemList(pet));
		activeChar.sendChanges();
	}
}
