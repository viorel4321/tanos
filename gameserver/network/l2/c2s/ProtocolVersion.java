package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.KeyPacket;

public class ProtocolVersion extends L2GameClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(ProtocolVersion.class);
	private int _version;
	private byte[] _data;
	private byte[] _check;

	@Override
	protected void readImpl()
	{
		_version = readD();
	}

	@Override
	protected void runImpl()
	{
		final GameClient client = this.getClient();
		if(_version == -2)
			client.closeNow(false);
		else if(_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION)
		{
			ProtocolVersion._log.info("Wrong protocol revision: " + _version + ", client IP: " + client.getIpAddr());
			client.close(new KeyPacket(null));
		}
		else
		{
			client.setProtocolOk(true);
			client.setRevision(_version);
			this.sendPacket(new KeyPacket(client.enableCrypt()));
		}
	}
}
