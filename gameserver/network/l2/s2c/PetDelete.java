package l2s.gameserver.network.l2.s2c;

public class PetDelete extends L2GameServerPacket
{
	private int _petId;
	private int _petnum;

	public PetDelete(final int petId, final int petnum)
	{
		_petId = petId;
		_petnum = petnum;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(182);
		writeD(_petId);
		writeD(_petnum);
	}
}
