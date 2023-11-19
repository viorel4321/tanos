package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.GameTimeController;
import l2s.gameserver.model.Player;

public class CharSelected extends L2GameServerPacket
{
	private final Player _cha;
	private final int _sessionId;

	public CharSelected(final Player cha, final int sessionId)
	{
		_cha = cha;
		_sessionId = sessionId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(21);
		writeS((CharSequence) _cha.getName());
		writeD(_cha.getCharId());
		writeS((CharSequence) _cha.getTitle());
		writeD(_sessionId);
		writeD(_cha.getClanId());
		writeD(0);
		writeD(_cha.getSex());
		writeD(_cha.getRace().ordinal());
		writeD(_cha.getClassId().getId());
		writeD(1);
		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());
		writeF(_cha.getCurrentHp());
		writeF(_cha.getCurrentMp());
		writeD(_cha.getSp());
		writeQ(_cha.getExp());
		writeD((int) _cha.getLevel());
		writeD(_cha.getKarma());
		writeD(_cha.getPkKills());
		writeD((int) _cha.getINT());
		writeD((int) _cha.getSTR());
		writeD((int) _cha.getCON());
		writeD((int) _cha.getMEN());
		writeD((int) _cha.getDEX());
		writeD((int) _cha.getWIT());
		for(int i = 0; i < 30; ++i)
			writeD(0);
		writeD(0);
		writeD(0);
		writeD(GameTimeController.getInstance().getGameTime());
		writeD(0);
		writeD(_cha.getClassId().getId());
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
	}
}
