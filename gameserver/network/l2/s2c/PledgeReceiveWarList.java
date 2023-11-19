package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.pledge.Clan;

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private List<WarInfo> infos;
	private static int _updateType;
	private static int _page;

	public PledgeReceiveWarList(final Clan clan, final int type, final int page)
	{
		infos = new ArrayList<WarInfo>();
		PledgeReceiveWarList._updateType = type;
		PledgeReceiveWarList._page = page;
		infos.clear();
		if(PledgeReceiveWarList._updateType == 1)
			for(final Clan _clan : clan.getAttackerClans())
			{
				if(_clan == null)
					continue;
				infos.add(new WarInfo(_clan.getName(), 1, 0));
			}
		else if(PledgeReceiveWarList._updateType == 0)
			for(final Clan _clan : clan.getEnemyClans())
			{
				if(_clan == null)
					continue;
				infos.add(new WarInfo(_clan.getName(), 1, 0));
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(62);
		writeD(PledgeReceiveWarList._updateType);
		writeD(0);
		writeD(infos.size());
		for(final WarInfo _info : infos)
		{
			writeS(_info.clan_name);
			writeD(_info.unk1);
			writeD(_info.unk2);
		}
	}

	static class WarInfo
	{
		public String clan_name;
		public int unk1;
		public int unk2;

		public WarInfo(final String _clan_name, final int _unk1, final int _unk2)
		{
			clan_name = _clan_name;
			unk1 = _unk1;
			unk2 = _unk2;
		}
	}
}
