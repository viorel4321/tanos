package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;

public class MyTargetSelected extends L2GameServerPacket
{
	private final int _objectId;
	private final int _color;

	public MyTargetSelected(final Player player, final Creature target)
	{
		_objectId = target.getObjectId();
		_color = target.isSummon() || target.isNpc() && !(target instanceof StaticObjectInstance) && !(target instanceof DecoyInstance) ? player.getLevel() - target.getLevel() : 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(166);
		writeD(_objectId);
		writeH(_color);
	}
}
