package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.utils.Util;

public class RequestChangePetName extends L2GameClientPacket
{
	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS();
	}

	@Override
	public void runImpl()
	{
		final Player cha = getClient().getActiveChar();
		final Servitor pet = cha.getServitor();
		if(pet != null && (pet.getName() == null || pet.getName().isEmpty() || pet.getName().equalsIgnoreCase(pet.getTemplate().name)))
		{
			if(PetDataTable.petNameExist(_name))
			{
				cha.sendPacket(new SystemMessage(584));
				return;
			}
			if(_name.length() > 8)
			{
				cha.sendPacket(new SystemMessage(548));
				return;
			}
			if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
			{
				cha.sendPacket(new SystemMessage(591));
				return;
			}
			pet.setName(_name);
			pet.broadcastPetInfo();
			if(pet.isPet())
			{
				final PetInstance _pet = (PetInstance) pet;
				final ItemInstance controlItem = _pet.getControlItem();
				if(controlItem != null)
				{
					controlItem.setCustomType2(1);
					controlItem.setPriceToSell(0);
					controlItem.updateDatabase();
					_pet.InventoryUpdateControlItem();
				}
			}
		}
		else
			cha.sendPacket(new SystemMessage(695));
	}
}
