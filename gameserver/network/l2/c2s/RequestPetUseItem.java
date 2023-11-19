package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestPetUseItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_THAT_WHILE_FISHING));
			return;
		}

		activeChar.setActive();

		PetInstance pet = (PetInstance) activeChar.getServitor();
		if(pet == null)
			return;

		ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		if(item == null)
			return;

		pet.useItem(item, false, true);
	}
}
