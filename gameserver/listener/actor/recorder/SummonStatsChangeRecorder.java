package l2s.gameserver.listener.actor.recorder;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.network.l2.s2c.StatusUpdate;

public class SummonStatsChangeRecorder extends CharStatsChangeRecorder<Servitor>
{
	public static final int SEND_MAX_HP = 8;
	public static final int SEND_MAX_MP = 16;
	private int _maxHp;
	private int _maxMp;

	public SummonStatsChangeRecorder(final Servitor actor)
	{
		super(actor);
	}

	@Override
	protected void refreshStats()
	{
		_maxHp = this.set(8, _maxHp, _activeChar.getMaxHp());
		if(_activeChar.isSummon())
			_maxMp = this.set(16, _maxMp, _activeChar.getMaxMp());
		super.refreshStats();
	}

	@Override
	protected void onSendChanges()
	{
		if((_changes & 0x1) == 0x1)
		{
			_activeChar.broadcastCharInfo();
			return;
		}
		if((_changes & 0x2) == 0x2)
			_activeChar.sendPetInfo();
		final StatusUpdate su = new StatusUpdate(_activeChar.getObjectId());
		if((_changes & 0x8) == 0x8)
			su.addAttribute(10, _activeChar.getMaxHp());
		if((_changes & 0x10) == 0x10)
			su.addAttribute(12, _activeChar.getMaxMp());
		if(su.hasAttributes())
		{
			_activeChar.broadcastToStatusListeners(su);
			_activeChar.broadcastStatusUpdate();
		}
	}
}
