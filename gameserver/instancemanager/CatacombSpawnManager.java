package l2s.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.entity.SevenSigns;

public class CatacombSpawnManager
{
	private static Logger _log = LoggerFactory.getLogger(CatacombSpawnManager.class);

	private static CatacombSpawnManager _instance;

	private static List<Spawn> _dawnMobs = new ArrayList<Spawn>();
	private static List<Spawn> _duskMobs = new ArrayList<Spawn>();
	private static int _currentState = 0;
	public static final List<Integer> _monsters = Arrays.asList(21139, 21140, 21141, 21142, 21143, 21144, 21145, 21146, 21147, 21148, 21149, 21150, 21151, 21152, 21153, 21154, 21155, 21156, 21157, 21158, 21159, 21160, 21161, 21162, 21163, 21164, 21165, 21166, 21167, 21168, 21169, 21170, 21171, 21172, 21173, 21174, 21175, 21176, 21177, 21178, 21179, 21180, 21181, 21182, 21183, 21184, 21185, 21186, 21187, 21188, 21189, 21190, 21191, 21192, 21193, 21194, 21195, 21196, 21197, 21198, 21199, 21200, 21201, 21202, 21203, 21204, 21205, 21206, 21207, 21208, 21209, 21210, 21211, 21213, 21214, 21215, 21217, 21218, 21219, 21221, 21222, 21223, 21224, 21225, 21226, 21227, 21228, 21229, 21230, 21231, 21236, 21237, 21238, 21239, 21240, 21241, 21242, 21243, 21244, 21245, 21246, 21247, 21248, 21249, 21250, 21251, 21252, 21253, 21254, 21255);

	public static CatacombSpawnManager getInstance()
	{
		if(_instance == null)
			_instance = new CatacombSpawnManager();
		return _instance;
	}

	public void addDawnMob(final Spawn spawnDat)
	{
		_dawnMobs.add(spawnDat);
	}

	public void addDuskMob(final Spawn spawnDat)
	{
		_duskMobs.add(spawnDat);
	}

	public void changeMode(final int mode)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;
		if(_currentState == mode)
			return;
		switch(_currentState = mode)
		{
			case 0:
			{
				deleteMobs(_dawnMobs);
				deleteMobs(_duskMobs);
				spawnMobs(_duskMobs);
				spawnMobs(_dawnMobs);
				break;
			}
			case 1:
			{
				deleteMobs(_dawnMobs);
				deleteMobs(_duskMobs);
				spawnMobs(_duskMobs);
				break;
			}
			case 2:
			{
				deleteMobs(_dawnMobs);
				deleteMobs(_duskMobs);
				spawnMobs(_dawnMobs);
				break;
			}
			default:
			{
				_log.warn("CatacombSpawnManager: Wrong mode sent");
				break;
			}
		}
	}

	public void notifyChangeMode()
	{
		if(Config.ALLOW_SEVEN_SIGNS && SevenSigns.getInstance().getCurrentPeriod() == 3)
			changeMode(SevenSigns.getInstance().getCabalHighestScore());
		else
			changeMode(0);
	}

	public void cleanUp()
	{
		deleteMobs(_duskMobs);
		deleteMobs(_dawnMobs);
		_duskMobs.clear();
		_dawnMobs.clear();
	}

	public void spawnMobs(final List<Spawn> mobsSpawnsList)
	{
		for(final Spawn spawnDat : mobsSpawnsList)
		{
			if(_currentState == 0)
				spawnDat.restoreAmount();
			else
				spawnDat.setAmount(spawnDat.getAmount() * 2);
			spawnDat.init();
		}
	}

	public static void deleteMobs(final List<Spawn> mobsSpawnsList)
	{
		for(final Spawn spawnDat : mobsSpawnsList)
			spawnDat.despawnAll();
	}
}
