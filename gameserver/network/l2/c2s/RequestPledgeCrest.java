package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.network.l2.s2c.PledgeCrest;

public class RequestPledgeCrest extends L2GameClientPacket
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
			RequestPledgeCrest._log.info("crestid " + _crestId + " requested");
		final byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);
		if(data != null)
		{
			final PledgeCrest pc = new PledgeCrest(_crestId, data);
			this.sendPacket(pc);
		}
		else if(Config.DEBUG)
			RequestPledgeCrest._log.info("crest is missing:" + _crestId);
	}

	static
	{
		RequestPledgeCrest._log = LoggerFactory.getLogger(RequestPledgeCrest.class);
	}
}
