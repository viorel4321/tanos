package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private boolean _canWrite = false;
	private int IncreasedForce;
	private int WeightPenalty;
	private int MessageRefusal;
	private int DangerArea;
	private int _expertisePenalty;
	private int CharmOfCourage;
	private int DeathPenaltyLevel;

	public EtcStatusUpdate(final Player player)
	{
		if(player == null)
			return;
		IncreasedForce = player.getIncreasedForce();
		WeightPenalty = player.getWeightPenalty();
		MessageRefusal = player.getMessageRefusal() || player.getNoChannel() != 0L || player.isBlockAll() ? 1 : 0;
		DangerArea = player.isInDangerArea() ? 1 : 0;
		_expertisePenalty = player.getExpertisePenalty();
		CharmOfCourage = player.isCharmOfCourage() ? 1 : 0;
		DeathPenaltyLevel = player.getDeathPenalty() == null ? 0 : player.getDeathPenalty().getLevel();
		_canWrite = true;
	}

	@Override
	protected boolean canWrite()
	{
		return _canWrite;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(243);
		writeD(IncreasedForce);
		writeD(WeightPenalty);
		writeD(MessageRefusal);
		writeD(DangerArea);
		writeD(_expertisePenalty);
		writeD(CharmOfCourage);
		writeD(DeathPenaltyLevel);
	}
}
