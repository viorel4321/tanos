package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestUnEquipItem extends L2GameClientPacket
{
	private int _slot;

	@Override
	public void readImpl()
	{
		_slot = readD();
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if((_slot == 128 || _slot == 256 || _slot == 16384) && (player.isCursedWeaponEquipped() || player.isFlagEquipped()))
			return;
		if(player.isCastingNow())
		{
			player.sendPacket(new SystemMessage(104));
			return;
		}
		if(_slot == 256)
		{
			final ItemInstance item = player.getInventory().getPaperdollItem(8);
			if(item != null && item.isArrow())
				return;
		}
		if(player.inTvT && Config.TvT_CustomItems)
			return;
		player.getInventory().unEquipItemInBodySlotAndNotify(_slot, null);
		if(player.recording)
			player.recBot(3, _slot, 0, 0, 0, 0, 0);
	}
}
