package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class AcquireSkillInfo extends L2GameServerPacket
{
	private List<Req> _reqs;
	private int _id;
	private int _level;
	private int _spCost;
	private int _mode;

	public AcquireSkillInfo(final int id, final int level, final int spCost, final int mode)
	{
		_reqs = new ArrayList<Req>();
		_id = id;
		_level = level;
		_spCost = spCost;
		_mode = mode;
	}

	public void addRequirement(final int type, final int id, final int count, final int unk)
	{
		_reqs.add(new Req(type, id, count, unk));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(139);
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeD(_mode);
		writeD(_reqs.size());
		for(final Req temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.itemId);
			writeD(temp.count);
			writeD(temp.unk);
		}
	}

	private static class Req
	{
		public int itemId;
		public int count;
		public int type;
		public int unk;

		public Req(final int pType, final int pItemId, final int pCount, final int pUnk)
		{
			itemId = pItemId;
			type = pType;
			count = pCount;
			unk = pUnk;
		}
	}
}
