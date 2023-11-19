package l2s.gameserver.model.instances;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.templates.npc.NpcTemplate;

public class BossInstance extends RaidBossInstance
{
	private boolean _teleportedToNest;
	private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

	public BossInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isBoss()
	{
		return true;
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return 10000;
	}

	private int getFixResp(SchedulingPattern schedulingPattern)
	{
		if(schedulingPattern != null) {
			/*if(getDeadTime() <= 0L){
				System.out.println("fixresp");
				return 0;}*/
			return (int) (schedulingPattern.next(System.currentTimeMillis()) / 1000);
		}
		return 0;
	}

	@Override
	public void onDeath(final Creature killer)
	{
		switch(getNpcId())
		{
			case 29001:
			{
				getSpawn().setFixResp(getFixResp(Config.QUEEN_ANT_FIXRESP));
				break;
			}
			case 29006:
			{
				getSpawn().setFixResp(getFixResp(Config.CORE_FIXRESP));
				break;
			}
			case 29014:
			{
				getSpawn().setFixResp(getFixResp(Config.ORFEN_FIXRESP));
				break;
			}
			case 29022:
			{
				getSpawn().setFixResp(getFixResp(Config.ZAKEN_FIXRESP));
				break;
			}
		}
		if(killer.isPlayable())
		{
			final Player player = killer.getPlayer();
			if(player.isInParty())
			{
				for(final Player member : player.getParty().getPartyMembers())
					if(member.isNoble())
						Hero.getInstance().addHeroDiary(member.getObjectId(), 1, getNpcId());
			}
			else if(player.isNoble())
				Hero.getInstance().addHeroDiary(player.getObjectId(), 1, getNpcId());
		}
		super.onDeath(killer);
	}

	public void setTeleported(final boolean flag)
	{
		_teleportedToNest = flag;
	}

	public boolean isTeleported()
	{
		return _teleportedToNest;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}
}
