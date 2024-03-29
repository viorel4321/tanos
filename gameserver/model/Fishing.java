package l2s.gameserver.model;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import l2s.commons.util.Rnd;
import l2s.gameserver.GameTimeController;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.FishingChampionShipManager;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.s2c.ExFishingEnd;
import l2s.gameserver.network.l2.s2c.ExFishingHpRegen;
import l2s.gameserver.network.l2.s2c.ExFishingStart;
import l2s.gameserver.network.l2.s2c.ExFishingStartCombat;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.FishTemplate;
import l2s.gameserver.utils.Location;

public class Fishing
{
	private final Player _fisher;
	public static final int FISHING_NONE = 0;
	public static final int FISHING_STARTED = 1;
	public static final int FISHING_WAITING = 2;
	public static final int FISHING_COMBAT = 3;
	private AtomicInteger _state;
	private int _time;
	private int _stop;
	private int _gooduse;
	private int _anim;
	private int _combatMode;
	private int _deceptiveMode;
	private int _fishCurHP;
	private FishTemplate _fish;
	private int _lureId;
	private Future<?> _fishingTask;
	private Location _fishLoc;

	public Fishing(final Player fisher)
	{
		_combatMode = -1;
		_fishLoc = new Location();
		_fisher = fisher;
		_state = new AtomicInteger(0);
	}

	public void setFish(final FishTemplate fish)
	{
		_fish = fish;
	}

	public void setLureId(final int lureId)
	{
		_lureId = lureId;
	}

	public int getLureId()
	{
		return _lureId;
	}

	public void setFishLoc(final Location loc)
	{
		_fishLoc.x = loc.x;
		_fishLoc.y = loc.y;
		_fishLoc.z = loc.z;
	}

	public Location getFishLoc()
	{
		return _fishLoc;
	}

	public void startFishing()
	{
		if(!_state.compareAndSet(0, 1))
			return;
		_fisher.setFishing(true);
		_fisher.broadcastUserInfo(false);
		_fisher.broadcastPacket(new ExFishingStart(_fisher, _fish.getType(), _fisher.getFishLoc(), isNightLure(_lureId)));
		_fisher.sendPacket(new SystemMessage(1461));
		startLookingForFishTask();
	}

	public void stopFishing()
	{
		if(_state.getAndSet(0) == 0)
			return;
		stopFishingTask();
		_fisher.setFishing(false);
		_fisher.broadcastPacket(new ExFishingEnd(_fisher, false));
		_fisher.broadcastUserInfo(false);
		_fisher.sendPacket(new SystemMessage(1458));
	}

	public void endFishing(final boolean win)
	{
		if(!_state.compareAndSet(3, 0))
			return;
		stopFishingTask();
		_fisher.setFishing(false);
		_fisher.broadcastPacket(new ExFishingEnd(_fisher, win));
		_fisher.broadcastUserInfo(false);
		_fisher.sendPacket(new SystemMessage(1460));
	}

	private void stopFishingTask()
	{
		if(_fishingTask != null)
		{
			_fishingTask.cancel(false);
			_fishingTask = null;
		}
	}

	private void startLookingForFishTask()
	{
		if(!_state.compareAndSet(1, 2))
			return;
		long checkDelay = 10000L;
		switch(_fish.getGroup())
		{
			case 0:
			{
				checkDelay = Math.round(_fish.getGutsCheckTime() * 1.33);
				break;
			}
			case 1:
			{
				checkDelay = _fish.getGutsCheckTime();
				break;
			}
			case 2:
			{
				checkDelay = Math.round(_fish.getGutsCheckTime() * 0.66);
				break;
			}
		}
		_fishingTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new LookingForFishTask(), 10000L, checkDelay);
	}

	public boolean isInCombat()
	{
		return _state.get() == 3;
	}

	private void startFishCombat()
	{
		if(!_state.compareAndSet(2, 3))
			return;
		_stop = 0;
		_gooduse = 0;
		_anim = 0;
		_time = _fish.getCombatTime() / 1000;
		_fishCurHP = _fish.getHP();
		_combatMode = Rnd.chance(20) ? 1 : 0;
		switch(getLureGrade(_lureId))
		{
			case 0:
			case 1:
			{
				_deceptiveMode = 0;
				break;
			}
			case 2:
			{
				_deceptiveMode = Rnd.chance(10) ? 1 : 0;
				break;
			}
		}
		final ExFishingStartCombat efsc = new ExFishingStartCombat(_fisher, _time, _fish.getHP(), _combatMode, _fish.getGroup(), _deceptiveMode);
		_fisher.broadcastPacket(efsc);
		_fisher.sendPacket(new SystemMessage(1449));
		_fishingTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FishCombatTask(), 1000L, 1000L);
	}

	private void changeHp(final int hp, final int pen)
	{
		_fishCurHP -= hp;
		if(_fishCurHP < 0)
			_fishCurHP = 0;
		_fisher.broadcastPacket(new ExFishingHpRegen(_fisher, _time, _fishCurHP, _combatMode, _gooduse, _anim, pen, _deceptiveMode));
		_gooduse = 0;
		_anim = 0;
		if(_fishCurHP > _fish.getHP() * 2)
		{
			_fishCurHP = _fish.getHP() * 2;
			doDie(false);
		}
		else if(_fishCurHP == 0)
			doDie(true);
	}

	private void doDie(boolean win)
	{
		stopFishingTask();
		if(win)
			if(!_fisher.isInPeaceZone() && Rnd.chance(5))
			{
				win = false;
				_fisher.sendPacket(new SystemMessage(1655));
				spawnPenaltyMonster(_fisher);
			}
			else
			{
				_fisher.sendPacket(new SystemMessage(1469));
				final ItemInstance item = ItemTable.getInstance().createItem(_fish.getId());
				if(!_fisher.getInventory().validateCapacity(item))
				{
					_fisher.sendPacket(new SystemMessage(129));
					item.deleteMe();
				}
				else
				{
					_fisher.getInventory().addItem(item);
					_fisher.sendPacket(new SystemMessage(30).addItemName(Integer.valueOf(item.getItemId())));
					_fisher.sendPacket(new ItemList(_fisher, false));
				}
				FishingChampionShipManager.getInstance().newFish(_fisher, _lureId);
			}
		endFishing(win);
	}

	public void useFishingSkill(final int dmg, final int pen, final Skill.SkillType skillType)
	{
		if(!isInCombat())
			return;
		int mode;
		if(skillType == Skill.SkillType.REELING && !GameTimeController.getInstance().isNowNight())
			mode = 1;
		else if(skillType == Skill.SkillType.PUMPING && GameTimeController.getInstance().isNowNight())
			mode = 1;
		else
			mode = 0;
		_anim = mode + 1;
		if(Rnd.chance(10))
		{
			_fisher.sendPacket(new SystemMessage(1464));
			changeHp(_gooduse = 0, pen);
			return;
		}
		if(_combatMode == mode)
		{
			if(_deceptiveMode == 0)
			{
				showMessage(_fisher, dmg, pen, skillType, 1);
				_gooduse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				showMessage(_fisher, dmg, pen, skillType, 2);
				_gooduse = 2;
				changeHp(-dmg, pen);
			}
		}
		else if(_deceptiveMode == 0)
		{
			showMessage(_fisher, dmg, pen, skillType, 2);
			_gooduse = 2;
			changeHp(-dmg, pen);
		}
		else
		{
			showMessage(_fisher, dmg, pen, skillType, 3);
			_gooduse = 1;
			changeHp(dmg, pen);
		}
	}

	private static void showMessage(final Player fisher, final int dmg, final int pen, final Skill.SkillType skillType, final int messageId)
	{
		switch(messageId)
		{
			case 1:
			{
				if(skillType == Skill.SkillType.PUMPING)
				{
					fisher.sendPacket(new SystemMessage(1465).addNumber(Integer.valueOf(dmg)));
					if(pen == 50)
					{
						fisher.sendPacket(new SystemMessage(1670));
						fisher.sendPacket(new SystemMessage(1672).addNumber(Integer.valueOf(pen)));
						break;
					}
					break;
				}
				else
				{
					fisher.sendPacket(new SystemMessage(1467).addNumber(Integer.valueOf(dmg)));
					if(pen == 50)
					{
						fisher.sendPacket(new SystemMessage(1670));
						fisher.sendPacket(new SystemMessage(1671).addNumber(Integer.valueOf(pen)));
						break;
					}
					break;
				}
			}
			case 2:
			{
				if(skillType == Skill.SkillType.PUMPING)
				{
					fisher.sendPacket(new SystemMessage(1466).addNumber(Integer.valueOf(dmg)));
					break;
				}
				fisher.sendPacket(new SystemMessage(1468).addNumber(Integer.valueOf(dmg)));
				break;
			}
			case 3:
			{
				if(skillType == Skill.SkillType.PUMPING)
				{
					fisher.sendPacket(new SystemMessage(1465).addNumber(Integer.valueOf(dmg)));
					if(pen == 50)
					{
						fisher.sendPacket(new SystemMessage(1670));
						fisher.sendPacket(new SystemMessage(1672).addNumber(Integer.valueOf(pen)));
						break;
					}
					break;
				}
				else
				{
					fisher.sendPacket(new SystemMessage(1467).addNumber(Integer.valueOf(dmg)));
					if(pen == 50)
					{
						fisher.sendPacket(new SystemMessage(1670));
						fisher.sendPacket(new SystemMessage(1671).addNumber(Integer.valueOf(pen)));
						break;
					}
					break;
				}
			}
		}
	}

	public static void spawnPenaltyMonster(final Player fisher)
	{
		final int npcId = 18319 + Math.min(fisher.getLevel() / 11, 7);
		final MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(npcId));
		npc.setSpawnedLoc(Location.findPointToStay(fisher.getLoc(), 100, 120, fisher.getGeoIndex()));
		npc.setHeading(fisher.getHeading() - 32768);
		npc.spawnMe(npc.getSpawnedLoc());
		npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, fisher, Rnd.get(1, 100));
	}

	public static int getRandomFishType(final int lureId, final int fishLvl, final int dist)
	{
		final int check = Rnd.get(100);
		int type = 0;
		switch(lureId)
		{
			case 7807:
			{
				if(check <= 54)
				{
					type = 5;
					break;
				}
				if(check <= 77)
				{
					type = 4;
					break;
				}
				type = 6;
				break;
			}
			case 7808:
			{
				if(check <= 54)
				{
					type = 4;
					break;
				}
				if(check <= 77)
				{
					type = 6;
					break;
				}
				type = 5;
				break;
			}
			case 7809:
			{
				if(check <= 54)
				{
					type = 6;
					break;
				}
				if(check <= 77)
				{
					type = 5;
					break;
				}
				type = 4;
				break;
			}
			case 8486:
			{
				if(check <= 33)
				{
					type = 4;
					break;
				}
				if(check <= 66)
				{
					type = 5;
					break;
				}
				type = 6;
				break;
			}
			case 7610:
			case 7611:
			case 7612:
			case 7613:
			case 8496:
			case 8497:
			case 8498:
			case 8499:
			case 8500:
			case 8501:
			case 8502:
			case 8503:
			case 8504:
			{
				type = 3;
				break;
			}
			case 8548:
			{
				if(check <= 32)
				{
					type = 1;
					break;
				}
				if(check <= 64)
				{
					type = 2;
					break;
				}
				if(check <= 96)
				{
					type = 0;
					break;
				}
				if(dist == 4 && fishLvl > 19)
				{
					type = 10;
					break;
				}
				type = 0;
				break;
			}
			case 6519:
			case 6520:
			case 6521:
			case 8505:
			case 8507:
			{
				if(check <= 54)
				{
					type = 1;
					break;
				}
				if(check <= 74)
				{
					type = 0;
					break;
				}
				if(check <= 94)
				{
					type = 2;
					break;
				}
				type = 3;
				break;
			}
			case 6522:
			case 6523:
			case 6524:
			case 8508:
			case 8510:
			{
				if(check <= 54)
				{
					type = 0;
					break;
				}
				if(check <= 74)
				{
					type = 1;
					break;
				}
				if(check <= 94)
				{
					type = 2;
					break;
				}
				type = 3;
				break;
			}
			case 6525:
			case 6526:
			case 6527:
			case 8511:
			case 8513:
			{
				if(check <= 55)
				{
					type = 2;
					break;
				}
				if(check <= 74)
				{
					type = 1;
					break;
				}
				if(check <= 94)
				{
					type = 0;
					break;
				}
				type = 3;
				break;
			}
			case 8484:
			{
				if(check <= 33)
				{
					type = 0;
					break;
				}
				if(check <= 66)
				{
					type = 1;
					break;
				}
				type = 2;
				break;
			}
			case 8506:
			{
				if(check <= 54)
				{
					type = 8;
					break;
				}
				if(check <= 77)
				{
					type = 7;
					break;
				}
				type = 9;
				break;
			}
			case 8509:
			{
				if(check <= 54)
				{
					type = 7;
					break;
				}
				if(check <= 77)
				{
					type = 9;
					break;
				}
				type = 8;
				break;
			}
			case 8512:
			{
				if(check <= 54)
				{
					type = 9;
					break;
				}
				if(check <= 77)
				{
					type = 8;
					break;
				}
				type = 7;
				break;
			}
			case 8485:
			{
				if(check <= 33)
				{
					type = 7;
					break;
				}
				if(check <= 66)
				{
					type = 8;
					break;
				}
				type = 9;
				break;
			}
			default:
			{
				type = 1;
				break;
			}
		}
		return type;
	}

	public static int getRandomFishLvl(final Player player)
	{
		int skilllvl = 0;
		final Abnormal effect = player.getAbnormalList().getEffectByStackType("fishPot");
		if(effect != null)
			skilllvl = (int) effect.getSkill().getPower();
		else
			skilllvl = player.getSkillLevel(1315);
		if(skilllvl <= 0)
			return 1;
		final int check = Rnd.get(100);
		int randomlvl;
		if(check < 50)
			randomlvl = skilllvl;
		else if(check <= 85)
		{
			randomlvl = skilllvl - 1;
			if(randomlvl <= 0)
				randomlvl = 1;
		}
		else
			randomlvl = skilllvl + 1;
		randomlvl = Math.min(27, Math.max(1, randomlvl));
		return randomlvl;
	}

	public static int getFishGroup(final int lureId)
	{
		switch(lureId)
		{
			case 7807:
			case 7808:
			case 7809:
			case 8486:
			{
				return 0;
			}
			case 8485:
			case 8506:
			case 8509:
			case 8512:
			{
				return 2;
			}
			default:
			{
				return 1;
			}
		}
	}

	public static int getLureGrade(final int lureId)
	{
		switch(lureId)
		{
			case 6519:
			case 6522:
			case 6525:
			case 8505:
			case 8508:
			case 8511:
			{
				return 0;
			}
			case 6520:
			case 6523:
			case 6526:
			case 7610:
			case 7611:
			case 7612:
			case 7613:
			case 7807:
			case 7808:
			case 7809:
			case 8484:
			case 8485:
			case 8486:
			case 8496:
			case 8497:
			case 8498:
			case 8499:
			case 8500:
			case 8501:
			case 8502:
			case 8503:
			case 8504:
			case 8506:
			case 8509:
			case 8512:
			case 8548:
			{
				return 1;
			}
			case 6521:
			case 6524:
			case 6527:
			case 8507:
			case 8510:
			case 8513:
			{
				return 2;
			}
			default:
			{
				return -1;
			}
		}
	}

	public static boolean isNightLure(final int lureId)
	{
		switch(lureId)
		{
			case 8505:
			case 8508:
			case 8511:
			{
				return true;
			}
			case 8496:
			case 8497:
			case 8498:
			case 8499:
			case 8500:
			case 8501:
			case 8502:
			case 8503:
			case 8504:
			{
				return true;
			}
			case 8506:
			case 8509:
			case 8512:
			{
				return true;
			}
			case 8510:
			case 8513:
			{
				return true;
			}
			case 8485:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	protected class LookingForFishTask implements Runnable
	{
		private long _endTaskTime;

		protected LookingForFishTask()
		{
			_endTaskTime = System.currentTimeMillis() + _fish.getWaitTime() + 10000L;
		}

		@Override
		public void run()
		{
			if(System.currentTimeMillis() >= _endTaskTime)
			{
				_fisher.sendPacket(new SystemMessage(1452));
				stopFishingTask();
				endFishing(false);
				return;
			}
			if(!GameTimeController.getInstance().isNowNight() && Fishing.isNightLure(_lureId))
			{
				_fisher.sendPacket(new SystemMessage(1452));
				stopFishingTask();
				endFishing(false);
				return;
			}
			final int check = Rnd.get(1000);
			if(_fish.getFishGuts() > check)
			{
				stopFishingTask();
				startFishCombat();
			}
		}
	}

	private class FishCombatTask implements Runnable
	{
		@Override
		public void run()
		{
			if(_fishCurHP >= _fish.getHP() * 2)
			{
				_fisher.sendPacket(new SystemMessage(1451));
				doDie(false);
			}
			else if(_time <= 0)
			{
				_fisher.sendPacket(new SystemMessage(1450));
				doDie(false);
			}
			else
			{
				_time--;
				if(_combatMode == 1 && _deceptiveMode == 0 || _combatMode == 0 && _deceptiveMode == 1)
					_fishCurHP += _fish.getHpRegen();
				if(_stop == 0)
				{
					_stop = 1;
					if(Rnd.chance(30))
						_combatMode = _combatMode == 0 ? 1 : 0;
					if(_fish.getGroup() == 2 && Rnd.chance(10))
						_deceptiveMode = _deceptiveMode == 0 ? 1 : 0;
				}
				else
					_stop--;
				final ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHP, _combatMode, 0, _anim, 0, _deceptiveMode);
				if(_anim != 0)
					_fisher.broadcastPacket(efhr);
				else
					_fisher.sendPacket(efhr);
			}
		}
	}
}
