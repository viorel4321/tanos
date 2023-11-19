package l2s.gameserver.model.instances;

import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.network.l2.s2c.AgitDecoInfo;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ClanHallManagerInstance extends ResidenceManager
{
	public ClanHallManagerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected Residence getResidence()
	{
		return getClanHall();
	}

	@Override
	public L2GameServerPacket decoPacket()
	{
		final ClanHall clanHall = getClanHall();
		if(clanHall != null)
			return new AgitDecoInfo(clanHall);
		return null;
	}

	@Override
	protected int getPrivUseFunctions()
	{
		return 2048;
	}

	@Override
	protected int getPrivSetFunctions()
	{
		return 16384;
	}

	@Override
	protected int getPrivDismiss()
	{
		return 8192;
	}

	@Override
	protected int getPrivDoors()
	{
		return 1024;
	}
}
