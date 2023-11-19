package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.tables.ClanTable;

public class AllianceInfo extends L2GameServerPacket
{
	private final Player _cha;

	public AllianceInfo(final Player cha)
	{
		_cha = cha;
		if(_cha.getAlliance() == null)
			return;
		_cha.sendPacket(new SystemMessage(491));
		_cha.sendPacket(new SystemMessage(492).addString(_cha.getClan().getAlliance().getAllyName()));
		int clancount = 0;
		final Clan leaderclan = _cha.getAlliance().getLeader();
		clancount = ClanTable.getInstance().getAlliance(leaderclan.getAllyId()).getMembers().length;
		final int[] online = new int[clancount + 1];
		final int[] count = new int[clancount + 1];
		final Clan[] clans = _cha.getAlliance().getMembers();
		for(int i = 0; i < clancount; ++i)
		{
			online[i + 1] = clans[i].getOnlineMembers(0).length;
			count[i + 1] = clans[i].getMembers().length;
			final int[] array = online;
			final int n = 0;
			array[n] += online[i + 1];
			final int[] array2 = count;
			final int n2 = 0;
			array2[n2] += count[i + 1];
		}
		SystemMessage sm = new SystemMessage(493);
		sm.addNumber(Integer.valueOf(online[0]));
		sm.addNumber(Integer.valueOf(count[0]));
		_cha.sendPacket(sm);
		sm = new SystemMessage(494);
		sm.addString(leaderclan.getName());
		sm.addString(leaderclan.getLeaderName());
		_cha.sendPacket(sm);
		_cha.sendPacket(new SystemMessage(495).addNumber(Integer.valueOf(clancount)));
		_cha.sendPacket(new SystemMessage(496));
		for(int i = 0; i < clancount; ++i)
		{
			_cha.sendPacket(new SystemMessage(497).addString(clans[i].getName()));
			_cha.sendPacket(new SystemMessage(498).addString(clans[i].getLeaderName()));
			_cha.sendPacket(new SystemMessage(499).addNumber(Byte.valueOf(clans[i].getLevel())));
			sm = new SystemMessage(493);
			sm.addNumber(Integer.valueOf(online[i + 1]));
			sm.addNumber(Integer.valueOf(count[i + 1]));
			_cha.sendPacket(sm);
			_cha.sendPacket(new SystemMessage(500));
		}
		_cha.sendPacket(new SystemMessage(490));
	}

	@Override
	protected final void writeImpl()
	{}
}
