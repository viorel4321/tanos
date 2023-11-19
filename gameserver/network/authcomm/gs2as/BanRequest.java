package l2s.gameserver.network.authcomm.gs2as;

import l2s.commons.ban.BanBindType;
import l2s.gameserver.network.authcomm.SendablePacket;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 **/
public class BanRequest extends SendablePacket
{
	private final BanBindType bindType;
	private final String bindValue;
	private final int endTime;
	private final String reason;

	public BanRequest(BanBindType bindType, String bindValue, int endTime, String reason)
	{
		this.bindType = bindType;
		this.bindValue = bindValue;
		this.endTime = endTime;
		this.reason = reason;
	}

	protected void writeImpl()
	{
		writeC(0x13);
		writeC(bindType.ordinal());
		writeS(bindValue);
		writeD(endTime);
		writeS(reason);
	}
}
