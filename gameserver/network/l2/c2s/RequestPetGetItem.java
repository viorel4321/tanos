package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.PetInstance;

public class RequestPetGetItem extends L2GameClientPacket
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
		final ItemInstance item = GameObjectsStorage.getItem(_objectId);
		final Player activeChar = getClient().getActiveChar();
		if(!(activeChar.getServitor() instanceof PetInstance))
		{
			activeChar.sendActionFailed();
			return;
		}
		final PetInstance pet = (PetInstance) activeChar.getServitor();
		if(pet == null || pet.isDead() || pet.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item, null);
	}
}
