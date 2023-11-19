package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private ClanMember _member;
	private String _name;

	public PledgeReceiveMemberInfo(final ClanMember member)
	{
		_member = member;
		final Clan clan = _member != null ? _member.getClan() : null;
		_name = clan != null ? _member.getPledgeType() != 0 ? clan.getSubPledge(_member.getPledgeType()).getName() : clan.getName() : null;
	}

	@Override
	protected final void writeImpl()
	{
		if(_member == null || _name == null)
			return;
		writeC(254);
		writeH(61);
		writeD(_member.getPledgeType());
		writeS((CharSequence) _member.getName());
		writeS((CharSequence) _member.getTitle());
		writeD(_member.getPowerGrade());
		if(_member.getPledgeType() != 0)
			writeS((CharSequence) _member.getClan().getSubPledge(_member.getPledgeType()).getName());
		else
			writeS((CharSequence) _member.getClan().getName());
		writeS((CharSequence) _member.getRelatedName());
	}
}
