package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.network.l2.s2c.AllianceCrest;

public class RequestAllyCrest extends L2GameClientPacket
{
	private static Logger _log;
	private int _crestId;

	@Override
	public void readImpl()
	{
		_crestId = readD();
	}

	@Override
	public void runImpl()
	{
		if(_crestId == 0)
			return;
		if(Config.DEBUG)
			RequestAllyCrest._log.info("allycrestid " + _crestId + " requested");
		final byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);
		if(data != null)
		{
			final AllianceCrest ac = new AllianceCrest(_crestId, data);
			this.sendPacket(ac);
		}
		else if(Config.DEBUG)
			RequestAllyCrest._log.info("allycrest is missing:" + _crestId);
	}

	static
	{
		RequestAllyCrest._log = LoggerFactory.getLogger(RequestAllyCrest.class);
	}
}
