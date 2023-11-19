package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.instances.CTBBossInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class CTBTeamObject implements SpawnableObject
{
	private CTBSiegeClanObject _siegeClan;
	private final NpcTemplate _mobTemplate;
	private final NpcTemplate _flagTemplate;
	private final Location _flagLoc;
	private NpcInstance _flag;
	private CTBBossInstance _mob;

	public CTBTeamObject(final int mobTemplate, final int flagTemplate, final Location flagLoc)
	{
		_mobTemplate = NpcTable.getTemplate(mobTemplate);
		_flagTemplate = NpcTable.getTemplate(flagTemplate);
		_flagLoc = flagLoc;
	}

	@Override
	public void spawnObject(final GlobalEvent event)
	{
		if(_flag == null)
		{
			(_flag = new NpcInstance(IdFactory.getInstance().getNextId(), _flagTemplate)).setCurrentHpMp(_flag.getMaxHp(), _flag.getMaxMp(), true);
			_flag.setHasChatWindow(false);
			_flag.spawnMe(_flagLoc);
		}
		else
		{
			if(_mob != null)
				throw new IllegalArgumentException("Cant spawn twice");
			final NpcTemplate template = _siegeClan == null || _siegeClan.getParam() == 0L ? _mobTemplate : NpcTable.getTemplate((int) _siegeClan.getParam());
			(_mob = (CTBBossInstance) template.getNewInstance()).setCurrentHpMp(_mob.getMaxHp(), _mob.getMaxMp(), true);
			_mob.setMatchTeamObject(this);
			_mob.addEvent(event);
			final int x = (int) (_flagLoc.x + 300.0 * Math.cos(_mob.headingToRadians(_flag.getHeading() - 32768)));
			final int y = (int) (_flagLoc.y + 300.0 * Math.sin(_mob.headingToRadians(_flag.getHeading() - 32768)));
			final Location loc = new Location(x, y, _flag.getZ(), _flag.getHeading());
			_mob.setSpawnedLoc(loc);
			_mob.spawnMe(loc);
		}
	}

	@Override
	public void despawnObject(final GlobalEvent event)
	{
		if(_mob != null)
		{
			_mob.deleteMe();
			_mob = null;
		}
		if(_flag != null)
		{
			_flag.deleteMe();
			_flag = null;
		}
		_siegeClan = null;
	}

	@Override
	public void refreshObject(final GlobalEvent event)
	{}

	public CTBSiegeClanObject getSiegeClan()
	{
		return _siegeClan;
	}

	public void setSiegeClan(final CTBSiegeClanObject siegeClan)
	{
		_siegeClan = siegeClan;
	}

	public boolean isParticle()
	{
		return _flag != null && _mob != null;
	}

	public NpcInstance getFlag()
	{
		return _flag;
	}
}
