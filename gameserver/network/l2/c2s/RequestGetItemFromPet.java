package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.items.PetInventory;
import l2s.gameserver.network.l2.s2c.PetItemList;

public class RequestGetItemFromPet extends L2GameClientPacket
{
	private int _objectId;
	private int _amount;
	private int _unknown;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_amount = readD();
		_unknown = readD();
	}

	@Override
	public void runImpl()
	{
		if(_amount < 1)
			return;
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final PetInstance pet = (PetInstance) activeChar.getServitor();
		if(pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		final PetInventory petInventory = pet.getInventory();
		final PcInventory playerInventory = activeChar.getInventory();
		final ItemInstance petItem = petInventory.getItemByObjectId(_objectId);
		if(petItem == null)
			return;
		if(petItem.isEquipped())
		{
			activeChar.sendActionFailed();
			return;
		}
		final long finalLoad = petItem.getTemplate().getWeight() * _amount;
		int slots = 0;
		if(!petItem.getTemplate().isStackable() || activeChar.getInventory().getItemByItemId(petItem.getItemId()) == null)
			slots = 1;
		if(!activeChar.getInventory().validateWeight(finalLoad))
		{
			this.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}
		if(!activeChar.getInventory().validateCapacity(slots) || activeChar.getInventory().getItemByItemId(petItem.getItemId()) != null && activeChar.getInventory().getItemByItemId(petItem.getItemId()).getCount() + _amount > Integer.MAX_VALUE || _amount > Integer.MAX_VALUE)
		{
			this.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}
		final ItemInstance item = petInventory.dropItem(_objectId, _amount);
		item.setCustomFlags(item.getCustomFlags() & 0xFFFFFF7F, true);
		playerInventory.addItem(item);
		pet.sendChanges();
		activeChar.sendPacket(new PetItemList(pet));
		activeChar.sendChanges();
	}
}
