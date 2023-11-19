package l2s.gameserver.network.l2.s2c;

public class VehicleStart extends L2GameServerPacket
{
	private int _objId;
	private int _state;

	public VehicleStart(final int objId, final int state)
	{
		_objId = objId;
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeC(186);
		writeD(_objId);
		writeD(_state);
	}
}
