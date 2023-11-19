package l2s.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.GameTimeController;
import l2s.gameserver.model.Spawn;

public class DayNightSpawnManager
{
	private static DayNightSpawnManager _instance;
	private static List<Spawn> _dayMobs;
	private static List<Spawn> _nightMobs;

	public static DayNightSpawnManager getInstance()
	{
		if(_instance == null)
			_instance = new DayNightSpawnManager();
		return _instance;
	}

	public void addDayMob(final Spawn spawnDat)
	{
		_dayMobs.add(spawnDat);
	}

	public void addNightMob(final Spawn spawnDat)
	{
		_nightMobs.add(spawnDat);
	}

	public void deleteMobs(final List<Spawn> mobsSpawnsList)
	{
		for(final Spawn spawnDat : mobsSpawnsList)
			spawnDat.despawnAll();
	}

	public void spawnMobs(final List<Spawn> mobsSpawnsList)
	{
		for(final Spawn spawnDat : mobsSpawnsList)
			spawnDat.init();
	}

	public void changeMode(final int mode)
	{
		switch(mode)
		{
			case 1:
			{
				deleteMobs(_nightMobs);
				deleteMobs(_dayMobs);
				spawnMobs(_dayMobs);
				break;
			}
			case 2:
			{
				deleteMobs(_nightMobs);
				deleteMobs(_dayMobs);
				spawnMobs(_nightMobs);
				break;
			}
		}
	}

	public void notifyChangeMode()
	{
		if(GameTimeController.getInstance().isNowNight())
			changeMode(2);
		else
			changeMode(1);
	}

	public void cleanUp()
	{
		deleteMobs(_nightMobs);
		deleteMobs(_dayMobs);
		_nightMobs.clear();
		_dayMobs.clear();
	}

	static
	{
		_dayMobs = new ArrayList<Spawn>();
		_nightMobs = new ArrayList<Spawn>();
	}
}
