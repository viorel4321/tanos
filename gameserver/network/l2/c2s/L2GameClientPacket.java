package l2s.gameserver.network.l2.c2s;

import java.nio.BufferUnderflowException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.net.nio.impl.ReceivablePacket;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

public abstract class L2GameClientPacket extends ReceivablePacket<GameClient>
{
	private static final Logger _log;

	@Override
	public final boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch(BufferUnderflowException e2)
		{
			L2GameClientPacket._log.error("Client: " + _client + " - Failed reading: " + getType() + ". Buffer underflow!");
		}
		catch(Exception e)
		{
			L2GameClientPacket._log.error("Client: " + _client + " - Failed reading: " + getType(), e);
		}
		_client.onPacketReadFail();
		return false;
	}

	protected abstract void readImpl() throws Exception;

	@Override
	public final void run()
	{
		final GameClient client = this.getClient();
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			L2GameClientPacket._log.error("Client: " + client + " - Failed running: " + getType(), e);
		}
	}

	protected abstract void runImpl() throws Exception;

	protected String readS(final int len)
	{
		final String ret = readS();
		return ret.length() > len ? ret.substring(0, len) : ret;
	}

	protected void sendPacket(final L2GameServerPacket packet)
	{
		getClient().sendPacket(packet);
	}

	protected void sendPacket(final L2GameServerPacket... packets)
	{
		getClient().sendPacket(packets);
	}

	public String getType()
	{
		return "[C] " + this.getClass().getSimpleName();
	}

	static
	{
		_log = LoggerFactory.getLogger(L2GameClientPacket.class);
	}
}
