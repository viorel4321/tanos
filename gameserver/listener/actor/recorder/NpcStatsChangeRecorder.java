package l2s.gameserver.listener.actor.recorder;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.StatusUpdate;

public class NpcStatsChangeRecorder extends CharStatsChangeRecorder<NpcInstance>
{
	public static final int SEND_MAX_HP = 8;
	private int _maxHp;

	public NpcStatsChangeRecorder(final NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void refreshStats()
	{
		_maxHp = this.set(8, _maxHp, _activeChar.getMaxHp());
		super.refreshStats();
	}

	@Override
	protected void onSendChanges()
	{
		if((_changes & 0x1) == 0x1)
			_activeChar.broadcastCharInfo();
		else if((_changes & 0x8) == 0x8)
		{
			_activeChar.broadcastToStatusListeners(new StatusUpdate(_activeChar.getObjectId()).addAttribute(10, _activeChar.getMaxHp()));
			_activeChar.broadcastStatusUpdate();
		}
	}
}
