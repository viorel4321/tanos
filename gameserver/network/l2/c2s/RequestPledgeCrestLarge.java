package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.network.l2.s2c.ExPledgeCrestLarge;

public class RequestPledgeCrestLarge extends L2GameClientPacket
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
			RequestPledgeCrestLarge._log.info("largecrestid " + _crestId + " requested");
		final byte[] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);
		if(data != null)
		{
			final ExPledgeCrestLarge pcl = new ExPledgeCrestLarge(_crestId, data);
			this.sendPacket(pcl);
		}
		else if(Config.DEBUG)
			RequestPledgeCrestLarge._log.info("largecrest file is missing:" + _crestId);
	}

	static
	{
		RequestPledgeCrestLarge._log = LoggerFactory.getLogger(RequestPledgeCrestLarge.class);
	}
}
