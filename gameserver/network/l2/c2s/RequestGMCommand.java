package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.s2c.GMHennaInfo;
import l2s.gameserver.network.l2.s2c.GMViewCharacterInfo;
import l2s.gameserver.network.l2.s2c.GMViewItemList;
import l2s.gameserver.network.l2.s2c.GMViewPledgeInfo;
import l2s.gameserver.network.l2.s2c.GMViewQuestInfo;
import l2s.gameserver.network.l2.s2c.GMViewSkillInfo;
import l2s.gameserver.network.l2.s2c.GMViewWarehouseWithdrawList;
import l2s.gameserver.tables.ClanTable;

public class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	public void readImpl()
	{
		_targetName = readS();
		_command = readD();
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final Player target = World.getPlayer(_targetName);
		Clan clan = null;
		if(target == null && (_command != 6 || (clan = ClanTable.getInstance().getClanByName(_targetName)) == null))
			return;
		if(!player.getPlayerAccess().CanViewChar || player.isKeyBlocked())
			return;
		switch(_command)
		{
			case 1:
			{
				this.sendPacket(new GMViewCharacterInfo(target));
				this.sendPacket(new GMHennaInfo(target));
				break;
			}
			case 2:
			{
				if(target.getClan() != null)
				{
					this.sendPacket(new GMViewPledgeInfo(target.getClan(), target));
					break;
				}
				break;
			}
			case 3:
			{
				this.sendPacket(new GMViewSkillInfo(target));
				break;
			}
			case 4:
			{
				this.sendPacket(new GMViewQuestInfo(target));
				break;
			}
			case 5:
			{
				this.sendPacket(new GMViewItemList(target));
				this.sendPacket(new GMHennaInfo(target));
				break;
			}
			case 6:
			{
				if(target != null)
				{
					this.sendPacket(new GMViewWarehouseWithdrawList(target));
					break;
				}
				this.sendPacket(new GMViewWarehouseWithdrawList(clan));
				break;
			}
		}
	}
}
