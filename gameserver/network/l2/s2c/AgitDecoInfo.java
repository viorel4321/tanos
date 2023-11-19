package l2s.gameserver.network.l2.s2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.entity.residence.ClanHall;

public class AgitDecoInfo extends L2GameServerPacket
{
	private static final Logger _log;
	ClanHall _ch;
	private int[] _buff;
	private int[] _itCr8;
	private int hp_recovery;
	private int mp_recovery;
	private int exp_recovery;
	private int teleport;
	private int curtains;
	private int itemCreate;
	private int support;
	private int platform;

	public AgitDecoInfo(final ClanHall ch)
	{
		_buff = new int[] { 0, 1, 1, 1, 2, 2, 2, 2, 2, 0, 0, 1, 1, 1, 2, 2, 2, 2, 2 };
		_itCr8 = new int[] { 0, 1, 2, 2 };
		if(ch == null)
		{
			AgitDecoInfo._log.warn("Attemp to send decorations for null ClanHall");
			return;
		}
		_ch = ch;
		hp_recovery = getHpRecovery(_ch.isFunctionActive(3) ? _ch.getFunction(3).getLevel() : 0);
		mp_recovery = getMpRecovery(_ch.isFunctionActive(4) ? _ch.getFunction(4).getLevel() : 0);
		exp_recovery = getExpRecovery(_ch.isFunctionActive(5) ? _ch.getFunction(5).getLevel() : 0);
		teleport = _ch.isFunctionActive(1) ? _ch.getFunction(1).getLevel() : 0;
		curtains = _ch.isFunctionActive(7) ? _ch.getFunction(7).getLevel() : 0;
		itemCreate = _ch.isFunctionActive(2) ? _itCr8[_ch.getFunction(2).getLevel()] : 0;
		support = _ch.isFunctionActive(6) ? _buff[_ch.getFunction(6).getLevel()] : 0;
		platform = _ch.isFunctionActive(8) ? _ch.getFunction(8).getLevel() : 0;
	}

	@Override
	protected final void writeImpl()
	{
		if(_ch == null)
			return;
		writeC(247);
		writeD(_ch.getId());
		writeC(hp_recovery);
		writeC(mp_recovery);
		writeC(mp_recovery);
		writeC(exp_recovery);
		writeC(teleport);
		writeC(0);
		writeC(curtains);
		writeC(itemCreate);
		writeC(support);
		writeC(support);
		writeC(platform);
		writeC(itemCreate);
		writeD(0);
		writeD(0);
	}

	private int getHpRecovery(final int percent)
	{
		switch(percent)
		{
			case 0:
			{
				return 0;
			}
			case 20:
			case 40:
			case 80:
			case 120:
			case 140:
			{
				return 1;
			}
			case 160:
			case 180:
			case 200:
			case 220:
			case 240:
			case 260:
			case 280:
			case 300:
			{
				return 2;
			}
			default:
			{
				AgitDecoInfo._log.warn("Unsupported percent " + percent + " in hp recovery");
				return 0;
			}
		}
	}

	private int getMpRecovery(final int percent)
	{
		switch(percent)
		{
			case 0:
			{
				return 0;
			}
			case 5:
			case 10:
			case 15:
			case 20:
			{
				return 1;
			}
			case 25:
			case 30:
			case 35:
			case 40:
			case 45:
			case 50:
			{
				return 2;
			}
			default:
			{
				AgitDecoInfo._log.warn("Unsupported percent " + percent + " in mp recovery");
				return 0;
			}
		}
	}

	private int getExpRecovery(final int percent)
	{
		switch(percent)
		{
			case 0:
			{
				return 0;
			}
			case 5:
			case 10:
			case 15:
			case 20:
			{
				return 1;
			}
			case 25:
			case 30:
			case 35:
			case 40:
			case 45:
			case 50:
			{
				return 2;
			}
			default:
			{
				AgitDecoInfo._log.warn("Unsupported percent " + percent + " in exp recovery");
				return 0;
			}
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(AgitDecoInfo.class);
	}
}
