package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.actor.instances.creature.Abnormal;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
	int _skillId;
	int _skillLevel;
	int _skillDuration;

	public ShortBuffStatusUpdate(final Abnormal effect)
	{
		_skillId = effect.getSkill().getId();
		_skillLevel = effect.getSkill().getLevel();
		_skillDuration = (int) effect.getTimeLeft() / 1000;
	}

	public ShortBuffStatusUpdate()
	{
		_skillId = 0;
		_skillLevel = 0;
		_skillDuration = 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(244);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_skillDuration);
	}
}
