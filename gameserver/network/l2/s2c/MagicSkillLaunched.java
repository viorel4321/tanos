package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collection;

import l2s.gameserver.model.Creature;

public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _casterId;
	private final int _skillId;
	private final int _skillLevel;
	private final Collection<Creature> _targets;

	public MagicSkillLaunched(final int casterId, final int skillId, final int skillLevel, final Creature target)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		(_targets = new ArrayList<Creature>()).add(target);
	}

	public MagicSkillLaunched(final int casterId, final int skillId, final int skillLevel, final Collection<Creature> targets)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(118);
		writeD(_casterId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.size());
		for(final Creature target : _targets)
			if(target != null)
				writeD(target.getObjectId());
	}
}
