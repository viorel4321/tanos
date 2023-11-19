package l2s.gameserver.network.l2.s2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.net.nio.impl.SendablePacket;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.IBroadcastPacket;

public abstract class L2GameServerPacket extends SendablePacket<GameClient> implements IBroadcastPacket
{
	private static final Logger _log = LoggerFactory.getLogger(L2GameServerPacket.class);

	protected static final int EXTENDED_PACKET = 0xFE;

	public final boolean write()
	{
		if(!canWrite())
			return false;

		try
		{
			writeImpl();
			return true;
		}
		catch(Exception e)
		{
			L2GameServerPacket._log.error("Client: " + getClient() + " - Failed writing: " + getType(), (Throwable) e);
			return false;
		}
	}

	protected abstract void writeImpl();

	protected boolean canWrite()
	{
		return true;
	}

	protected void writeD(boolean b)
	{
		writeD(b ? 1 : 0);
	}

	protected void writeH(boolean b)
	{
		writeH(b ? 1 : 0);
	}

	protected void writeC(boolean b)
	{
		writeC(b ? 1 : 0);
	}

	protected void writeDD(final int[] values, final boolean sendCount)
	{
		if(sendCount)
			getByteBuffer().putInt(values.length);
		for(final int value : values)
			getByteBuffer().putInt(value);
	}

	protected void writeDD(final int[] values)
	{
		writeDD(values, false);
	}

	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}

	@Override
	public L2GameServerPacket packet(final Player player)
	{
		return this;
	}
}
