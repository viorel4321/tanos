package l2s.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import gnu.trove.impl.sync.TSynchronizedIntList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.ExOlympiadMatchEnd;
import l2s.gameserver.network.l2.s2c.ExOlympiadMode;
import l2s.gameserver.network.l2.s2c.ExOlympiadUserInfo;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Log;

public class OlympiadGame
{
	private static final Logger _log;
	public boolean _aborted = false;
	private int _reason = 0;
	private int _id;
	private CompType _type;
	private Zone _zone;
	private String _playerOneName;
	private String _playerTwoName;
	private int _playerOneID = 0;
	private int _playerTwoID = 0;
	public Player _playerOne;
	public Player _playerTwo;
	private double _playerOneDamage = 0.;
	private double _playerTwoDamage = 0.;
	private List<Player> _players = new ArrayList<>(2);
	private int[] _playerOneLocation = new int[] { 0, 0, 0 };
	private int[] _playerTwoLocation = new int[] { 0, 0, 0 };
	private TIntList _spectators = new TSynchronizedIntList(new TIntArrayList());
	private SystemMessage _sm;
	private int _winner = 0;
	private int _started = 0;
	public boolean validated = false;
	private boolean ended = false;
	private int seconds = 0;
	private ScheduledFuture<?> _checkTask;
	OlympiadGameTask _task;
	ScheduledFuture<?> _shedule;

	public OlympiadGame(int id, CompType type, TIntList opponents)
	{
		_id = id;
		_type = type;

		if(opponents != null && opponents.size() > 1)
		{
			_playerOne = GameObjectsStorage.getPlayer(opponents.get(0));
			_playerTwo = GameObjectsStorage.getPlayer(opponents.get(1));

			if(unchecks())
			{
				_reason = 3;
				_aborted = true;
				return;
			}

			_players.add(_playerOne);
			_players.add(_playerTwo);

			try
			{
				if(_playerOne.inObserverMode())
					if(_playerOne.getOlympiadObserveId() > 0)
						_playerOne.leaveOlympiadObserverMode();
					else
						_playerOne.leaveObserverMode();
				if(_playerTwo.inObserverMode())
					if(_playerTwo.getOlympiadObserveId() > 0)
						_playerTwo.leaveOlympiadObserverMode();
					else
						_playerTwo.leaveObserverMode();
				_playerOne.setOlympiadSide(1);
				_playerTwo.setOlympiadSide(2);
				_playerOne.setOlympiadGameId(_id);
				_playerTwo.setOlympiadGameId(_id);
				_playerOneName = _playerOne.getName();
				_playerTwoName = _playerTwo.getName();
				_playerOneID = _playerOne.getObjectId();
				_playerTwoID = _playerTwo.getObjectId();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				_reason = 3;
				_aborted = true;
				return;
			}
			Log.addLog("Olympiad System: Game - " + id + ": " + _playerOne.getName() + " Vs " + _playerTwo.getName(), "olympiad");
		}
		else
		{
			Log.addLog("Players list is not full for game " + _id, "olympiad");
			_reason = 3;
			_aborted = true;
		}
	}

	private boolean unchecks()
	{
		if(_playerOne == null || _playerTwo == null || _playerOne.getOlympiadGameId() != -1 || _playerTwo.getOlympiadGameId() != -1 || _playerOne.getObjectId() == _playerTwo.getObjectId())
			return true;
		if(_playerOne.isCursedWeaponEquipped())
		{
			final String name = _playerOne.getActiveWeaponInstance().getName();
			_playerOne.sendPacket(new SystemMessage(1857).addString(name));
			_playerTwo.sendPacket(new SystemMessage(1856).addString(name));
			return true;
		}
		if(_playerTwo.isCursedWeaponEquipped())
		{
			final String name = _playerTwo.getActiveWeaponInstance().getName();
			_playerTwo.sendPacket(new SystemMessage(1857).addString(name));
			_playerOne.sendPacket(new SystemMessage(1856).addString(name));
			return true;
		}
		return false;
	}

	protected boolean portPlayersToArena()
	{
		if(!checkContinue(false))
			return false;
		if(_playerOne.isTeleporting())
		{
			_playerOne.sendPacket(new SystemMessage(2029));
			endGame(1000L, 1);
			return false;
		}
		if(_playerTwo.isTeleporting())
		{
			_playerTwo.sendPacket(new SystemMessage(2029));
			endGame(1000L, 2);
			return false;
		}
		try
		{
			DuelEvent duel = _playerOne.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(_playerOne);
			duel = _playerTwo.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(_playerTwo);
			_playerOne.sendPacket(new ExOlympiadMode(1));
			_playerTwo.sendPacket(new ExOlympiadMode(2));
			_playerOneLocation[0] = _playerOne.getX();
			_playerOneLocation[1] = _playerOne.getY();
			_playerOneLocation[2] = _playerOne.getZ();
			_playerTwoLocation[0] = _playerTwo.getX();
			_playerTwoLocation[1] = _playerTwo.getY();
			_playerTwoLocation[2] = _playerTwo.getZ();
			if(_playerOne.isDead())
				_playerOne.setPendingRevive(true);
			if(_playerTwo.isDead())
				_playerTwo.setPendingRevive(true);
			if(_playerOne.isSitting())
				_playerOne.standUp();
			if(_playerTwo.isSitting())
				_playerTwo.standUp();
			_playerOne.setTarget(null);
			_playerTwo.setTarget(null);
			_playerOne.setIsInOlympiadMode(true);
			_playerTwo.setIsInOlympiadMode(true);
			_playerOne.closeEnchant();
			_playerTwo.closeEnchant();
			checkWeapon(_playerOne);
			checkWeapon(_playerTwo);
			if(Config.OLY_RESET_CHARGES)
			{
				_playerOne.setIncreasedForce(0);
				_playerTwo.setIncreasedForce(0);
			}
			if(Config.ALLOW_OLY_HENNA)
			{
				_playerOne.olyHenna(true);
				_playerTwo.olyHenna(true);
			}
			if(Config.OLY_RESTRICTED_ITEMS.length > 0)
			{
				for(final ItemInstance item : _playerOne.getInventory().getPaperdollItems())
					if(item != null && ArrayUtils.contains(Config.OLY_RESTRICTED_ITEMS, item.getItemId()))
						_playerOne.getInventory().unEquipItem(item);
				for(final ItemInstance item : _playerTwo.getInventory().getPaperdollItems())
					if(item != null && ArrayUtils.contains(Config.OLY_RESTRICTED_ITEMS, item.getItemId()))
						_playerTwo.getInventory().unEquipItem(item);
			}
			_zone = ZoneManager.getInstance().getZoneById(Zone.ZoneType.OlympiadStadia, 3001 + _id, false);
			if(_zone == null)
			{
				OlympiadGame._log.warn("Olympiad zone null!!!");
				endGame(1000L, 3);
				return false;
			}
			final int z = _id == 0 ? -3024 : -3328;
			_playerOne.setHeading(0);
			_playerOne.teleToLocation(Olympiad.COORDS[_id][1][0], Olympiad.COORDS[_id][1][1], z);
			_playerTwo.setHeading(32768);
			_playerTwo.teleToLocation(Olympiad.COORDS[_id][2][0], Olympiad.COORDS[_id][2][1], z);
		}
		catch(Exception e)
		{
			Log.addLog("Olympiad Result: " + _playerOneName + " vs " + _playerTwoName + " ... aborted due to crash before teleport to arena", "olympiad");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected void preparation()
	{
		if(!_players.isEmpty())
			for(final Player player : _players)
				if(player != null)
					try
					{
						player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), false);
						player.setCurrentCp(player.getMaxCp());
						player.setTeam(0, false);
						if(player.getClan() != null)
							for(final Skill skill : player.getClan().getAllSkills())
								player.addUnActiveSkill(skill);
						Hero.unActivateHeroSkills(player);
						if(Config.OLY_RESTRICTED_SKILLS.length > 0)
							for(final Skill skill2 : player.getAllSkills())
								if(ArrayUtils.contains(Config.OLY_RESTRICTED_SKILLS, skill2.getId()))
									player.addUnActiveSkill(skill2);
						if(player.isInParty())
							player.getParty().removePartyMember(player, false);
						final Servitor summon = player.getServitor();
						if(summon != null)
						{
							summon.getAbnormalList().stopAll();
							if(summon.isPet() || Config.OLY_RESTRICTED_SUMMONS.length > 0 && ArrayUtils.contains(Config.OLY_RESTRICTED_SUMMONS, summon.getNpcId()))
								summon.unSummon();
						}
						if(player.isCastingNow())
							player.abortCast(true, false);
						player._resEffs = null;
						for(final Abnormal e : player.getAbnormalList().values())
							if(e != null && (!e.getSkill().isToggle() || ArrayUtils.contains(Config.OLY_RESTRICTED_SKILLS, e.getSkill().getId())) && (e.getEffectType() != EffectType.Cubic || player.getSkillLevel(e.getSkill().getId()) <= 0))
								e.exit();
						if(Config.OLY_RENEWAL_BEGIN)
							player.resetReuse(true);
						player.sendPacket(new SkillList(player));
						checkWeapon(player);
						for(final int itemId : player.getAutoSoulShot())
						{
							player.removeAutoSoulShot(itemId);
							player.sendPacket(new ExAutoSoulShot(itemId, false));
						}
						final ItemInstance weapon = player.getActiveWeaponInstance();
						if(weapon != null)
						{
							weapon.setChargedSpiritshot((byte) 0);
							weapon.setChargedSoulshot((byte) 0);
						}
						player.broadcastUserInfo(true);
					}
					catch(Exception e2)
					{
						e2.printStackTrace();
					}
		if(Config.OLY_ZONE_CHECK > 0)
			_checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckTask(), Config.OLY_ZONE_CHECK * 1000L, Config.OLY_ZONE_CHECK * 1000L);
	}

	private static void checkWeapon(final Player player)
	{
		final ItemInstance wpn = player.getInventory().getPaperdollItem(7);
		if(wpn != null && wpn.isHeroWeapon())
		{
			player.getInventory().unEquipItem(wpn);
			player.abortAttack(true, false);
			player.refreshExpertisePenalty();
		}
	}

	protected void sendMessageToPlayers(final boolean toBattleBegin, final int nsecond)
	{
		if(toBattleBegin)
			_sm = new SystemMessage(1495);
		else
			_sm = new SystemMessage(1492);
		_sm.addNumber(Integer.valueOf(nsecond));
		try
		{
			if(!_players.isEmpty())
				for(final Player player : _players)
					if(player != null)
						player.sendPacket(_sm);
			if(toBattleBegin && !_spectators.isEmpty())
				for(final Player s : getSpectators())
					if(s != null && s.getOlympiadObserveId() == _id)
						s.sendPacket(_sm);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void portPlayersBack()
	{
		if(!_players.isEmpty())
			for(final Player player : _players)
				if(player != null)
					try
					{
						player.setIsInOlympiadMode(false);
						player.setOlympiadSide(-1);
						player.setOlympiadGameId(-1);
						player._resEffs = null;
						for(final Abnormal e : player.getAbnormalList().values())
							if(e != null && !e.getSkill().isToggle() && (e.getEffectType() != EffectType.Cubic || player.getSkillLevel(e.getSkill().getId()) <= 0))
								e.exit();
						player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), false);
						player.setCurrentCp(player.getMaxCp());
						if(player.getClan() != null && player.getClan().getReputationScore() >= 0)
							for(final Skill skill : player.getClan().getAllSkills())
								if(skill.getMinPledgeClass() <= player.getPledgeClass())
									player.removeUnActiveSkill(skill);
						Hero.activateHeroSkills(player);
						if(Config.OLY_RESTRICTED_SKILLS.length > 0)
							for(final Skill skill2 : player.getAllSkills())
								if(ArrayUtils.contains(Config.OLY_RESTRICTED_SKILLS, skill2.getId()))
									player.removeUnActiveSkill(skill2);
						if(Config.OLY_RENEWAL_END)
							player.resetReuse(true);
						if(player.getServitor() != null)
							player.getServitor().getAbnormalList().stopAll();
						player.sendPacket(new SkillList(player));
						player.sendPacket(new ExOlympiadMode(0));
						if(!Config.ALLOW_OLY_HENNA)
							continue;
						player.olyHenna(false);
					}
					catch(Exception e2)
					{
						e2.printStackTrace();
					}
		try
		{
			if(_playerOne != null && _playerOne.isInZoneOlympiad())
				if(_playerOneLocation[0] != 0)
					_playerOne.teleToLocation(_playerOneLocation[0], _playerOneLocation[1], _playerOneLocation[2]);
				else
					_playerOne.teleToClosestTown();
			if(_playerTwo != null && _playerTwo.isInZoneOlympiad())
				if(_playerTwoLocation[0] != 0)
					_playerTwo.teleToLocation(_playerTwoLocation[0], _playerTwoLocation[1], _playerTwoLocation[2]);
				else
					_playerTwo.teleToClosestTown();
		}
		catch(Exception e3)
		{
			e3.printStackTrace();
		}
		_zone = null;
	}

	public void validateWinner() throws Exception
	{
		if(validated)
			return;
		validated = true;
		_started = 0;
		matchEnd();
		if(_checkTask != null)
		{
			_checkTask.cancel(false);
			_checkTask = null;
		}
		if(_reason > 0)
		{
			if(_playerOne != null || _playerTwo != null)
			{
				if(_reason < 3)
					crash();
				if(_playerOne != null && _playerOne.isInOlympiadMode() || _playerTwo != null && _playerTwo.isInOlympiadMode())
				{
					broadcastMessage(new SystemMessage(284), true, false);
					if(seconds > 1)
					{
						(_sm = new SystemMessage(1499)).addNumber(Integer.valueOf(seconds));
						broadcastMessage(_sm, false, false);
					}
				}
			}
			return;
		}
		final StatsSet playerOneStat = Olympiad._nobles.get(_playerOneID);
		final StatsSet playerTwoStat = Olympiad._nobles.get(_playerTwoID);
		if(playerOneStat == null || playerTwoStat == null)
			return;
		final int playerOneWin = playerOneStat.getInteger("competitions_win");
		final int playerTwoWin = playerTwoStat.getInteger("competitions_win");
		final int playerOneLoose = playerOneStat.getInteger("competitions_loose");
		final int playerTwoLoose = playerTwoStat.getInteger("competitions_loose");
		final int playerOnePlayed = playerOneStat.getInteger("competitions_done");
		final int playerTwoPlayed = playerTwoStat.getInteger("competitions_done");
		final int playerOnePoints = playerOneStat.getInteger("olympiad_points");
		final int playerTwoPoints = playerTwoStat.getInteger("olympiad_points");
		_playerOne = GameObjectsStorage.getPlayer(_playerOneID);
		_playerTwo = GameObjectsStorage.getPlayer(_playerTwoID);
		if(_playerOne == null && _playerTwo == null)
		{
			Log.addLog("Olympiad Result: " + _playerOneName + " vs " + _playerTwoName + " ... aborted/tie due to crashes! (Players check)", "olympiad");
			return;
		}
		double playerOneHp = 0.0;
		try
		{
			if(_playerOne != null)
				playerOneHp = _playerOne.getCurrentHp() + _playerOne.getCurrentCp();
			else
			{
				if(_winner == 0)
					_winner = 1;
				_playerOne = null;
				playerOneHp = 0.0;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(_winner == 0)
				_winner = 1;
			_playerOne = null;
			playerOneHp = 0.0;
		}
		double playerTwoHp = 0.0;
		try
		{
			if(_playerTwo != null)
				playerTwoHp = _playerTwo.getCurrentHp() + _playerTwo.getCurrentCp();
			else
			{
				if(_winner == 0)
					_winner = 2;
				_playerTwo = null;
				playerTwoHp = 0.0;
			}
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			if(_winner == 0)
				_winner = 2;
			_playerTwo = null;
			playerTwoHp = 0.0;
		}
		_sm = new SystemMessage(1497);
		final SystemMessage _sm2 = new SystemMessage(1657);
		final SystemMessage _sm3 = new SystemMessage(1658);
		String result = "";
		if(_playerOne == null && _playerTwo == null)
		{
			Log.addLog("Olympiad Result: " + _playerOneName + " vs " + _playerTwoName + " ... aborted/tie due to crashes! (Connection check)", "olympiad");
			_playerOne = null;
			_playerTwo = null;
			return;
		}
		if(_winner == 0)
			if(_playerTwo == null)
				_winner = 2;
			else if(_playerOne == null)
				_winner = 1;
		if(_winner == 0)
			if(_playerOneDamage < _playerTwoDamage)
				_winner = 2;
			else if(_playerOneDamage > _playerTwoDamage)
				_winner = 1;
		String[] announce = null;
		if(_winner == 2)
		{
			if(Config.OLY_COMP_WIN_ANNOUNCE > 0 && playerOneWin + 1 >= Config.OLY_COMP_WIN_ANNOUNCE)
				announce = new String[] { _playerOneName, String.valueOf(playerOneWin + 1) };
			final int ngp = _type.getReward();
			final int pointDiff = Math.max(Math.min(playerOnePoints, playerTwoPoints) / _type.getDiv(), 1);
			playerOneStat.set("olympiad_points", Math.min(playerOnePoints + pointDiff, Config.OLY_POINTS_MAX));
			playerTwoStat.set("olympiad_points", Math.min(playerTwoPoints - pointDiff, Config.OLY_POINTS_MAX));
			playerOneStat.set("competitions_win", playerOneWin + 1);
			playerTwoStat.set("competitions_loose", playerTwoLoose + 1);
			playerOneStat.set("competitions_done", playerOnePlayed + 1);
			playerTwoStat.set("competitions_done", playerTwoPlayed + 1);
			_sm.addString(_playerOneName);
			broadcastMessage(_sm, true, true);
			_sm2.addString(_playerOneName);
			_sm2.addNumber(Integer.valueOf(pointDiff));
			broadcastMessage(_sm2, false, false);
			_sm3.addString(_playerTwoName);
			_sm3.addNumber(Integer.valueOf(pointDiff));
			broadcastMessage(_sm3, false, false);
			try
			{
				result = "(" + (int) playerOneHp + "hp vs " + (int) playerTwoHp + "hp - " + (int) _playerOneDamage + " vs " + (int) _playerTwoDamage + ") " + _playerOneName + " win " + pointDiff + " points";
				_playerOne.getInventory().addItem(6651, ngp);
				final SystemMessage sm = new SystemMessage(53);
				sm.addItemName(Integer.valueOf(6651));
				sm.addNumber(Integer.valueOf(ngp));
				_playerOne.sendPacket(sm);
				if(Config.OLY_MATCH_REWARD.length > 3)
					for(int i = 0; i < Config.OLY_MATCH_REWARD.length; i += 4)
						if(Rnd.chance(Config.OLY_MATCH_REWARD[i + 3]))
						{
							final int cnt = Rnd.get(Config.OLY_MATCH_REWARD[i + 1], Config.OLY_MATCH_REWARD[i + 2]);
							_playerOne.getInventory().addItem(Config.OLY_MATCH_REWARD[i], cnt);
							_playerOne.sendPacket(SystemMessage.obtainItems(Config.OLY_MATCH_REWARD[i], cnt, 0));
						}
			}
			catch(Exception e3)
			{
				e3.printStackTrace();
			}
		}
		else if(_winner == 1)
		{
			if(Config.OLY_COMP_WIN_ANNOUNCE > 0 && playerTwoWin + 1 >= Config.OLY_COMP_WIN_ANNOUNCE)
				announce = new String[] { _playerTwoName, String.valueOf(playerTwoWin + 1) };
			final int ngp = _type.getReward();
			final int pointDiff = Math.max(Math.min(playerOnePoints, playerTwoPoints) / _type.getDiv(), 1);
			playerTwoStat.set("olympiad_points", Math.min(playerTwoPoints + pointDiff, Config.OLY_POINTS_MAX));
			playerOneStat.set("olympiad_points", Math.min(playerOnePoints - pointDiff, Config.OLY_POINTS_MAX));
			playerOneStat.set("competitions_loose", playerOneLoose + 1);
			playerTwoStat.set("competitions_win", playerTwoWin + 1);
			playerOneStat.set("competitions_done", playerOnePlayed + 1);
			playerTwoStat.set("competitions_done", playerTwoPlayed + 1);
			_sm.addString(_playerTwoName);
			broadcastMessage(_sm, true, true);
			_sm2.addString(_playerTwoName);
			_sm2.addNumber(Integer.valueOf(pointDiff));
			broadcastMessage(_sm2, false, false);
			_sm3.addString(_playerOneName);
			_sm3.addNumber(Integer.valueOf(pointDiff));
			broadcastMessage(_sm3, false, false);
			try
			{
				result = "(" + (int) playerOneHp + "hp vs " + (int) playerTwoHp + "hp - " + (int) _playerOneDamage + " vs " + (int) _playerTwoDamage + ") " + _playerTwoName + " win " + pointDiff + " points";
				_playerTwo.getInventory().addItem(6651, ngp);
				final SystemMessage sm = new SystemMessage(53);
				sm.addItemName(Integer.valueOf(6651));
				sm.addNumber(Integer.valueOf(ngp));
				_playerTwo.sendPacket(sm);
				if(Config.OLY_MATCH_REWARD.length > 3)
					for(int i = 0; i < Config.OLY_MATCH_REWARD.length; i += 4)
						if(Rnd.chance(Config.OLY_MATCH_REWARD[i + 3]))
						{
							final int cnt = Rnd.get(Config.OLY_MATCH_REWARD[i + 1], Config.OLY_MATCH_REWARD[i + 2]);
							_playerTwo.getInventory().addItem(Config.OLY_MATCH_REWARD[i], cnt);
							_playerTwo.sendPacket(SystemMessage.obtainItems(Config.OLY_MATCH_REWARD[i], cnt, 0));
						}
			}
			catch(Exception e3)
			{
				e3.printStackTrace();
			}
		}
		else
		{
			result = "tie";
			broadcastMessage(_sm = new SystemMessage(1498), true, true);
		}
		Log.addLog("Olympiad Result: " + _playerOneName + " vs " + _playerTwoName + " ... " + result, "olympiad");
		if(!result.equals("tie"))
		{
			Olympiad._nobles.remove(_playerOneID);
			Olympiad._nobles.remove(_playerTwoID);
			Olympiad._nobles.put(_playerOneID, playerOneStat);
			Olympiad._nobles.put(_playerTwoID, playerTwoStat);
			OlympiadDatabase.saveNobleData(_playerOneID);
			OlympiadDatabase.saveNobleData(_playerTwoID);
		}
		broadcastMessage(new SystemMessage(284), true, false);
		(_sm = new SystemMessage(1499)).addNumber(Integer.valueOf(Config.OLY_RETURN_TIME));
		broadcastMessage(_sm, false, false);
		if(announce != null)
			Announcements.getInstance().announceByCustomMessage("l2s.OlympiadCompWinAnnounce", announce);
	}

	private boolean playerNot(final int playerID, final boolean zone)
	{
		if(playerID <= 0)
			return true;
		final Player player = GameObjectsStorage.getPlayer(playerID);
		return player == null || player.isLogoutStarted() || player.getOlympiadGameId() == -1 || player.inObserverMode() || zone && !player.isInZone(_zone);
	}

	private void crash()
	{
		broadcastMessage(new SystemMessage(1493), true, false);
		try
		{
			final StatsSet playerOneStat = Olympiad._nobles.get(_playerOneID);
			final StatsSet playerTwoStat = Olympiad._nobles.get(_playerTwoID);
			if(playerOneStat == null || playerTwoStat == null)
				return;
			final int playerOnePoints = playerOneStat.getInteger("olympiad_points");
			final int playerTwoPoints = playerTwoStat.getInteger("olympiad_points");
			final SystemMessage _sm1 = new SystemMessage(1657);
			final SystemMessage _sm2 = new SystemMessage(1658);
			boolean save = false;
			if(_reason == 1)
			{
				final boolean arena = _playerTwo != null && _playerTwo.isInOlympiadMode();
				final int pointsPart = arena && _type == CompType.CLASSED ? Math.max(Math.min(playerOnePoints, playerTwoPoints) / 3, 1) : Math.max(Math.min(playerOnePoints, playerTwoPoints) / 5, 1);
				playerOneStat.set("olympiad_points", Math.min(playerOnePoints - pointsPart, Config.OLY_POINTS_MAX));
				save = true;
				if(arena)
				{
					playerTwoStat.set("olympiad_points", Math.min(playerTwoPoints + pointsPart, Config.OLY_POINTS_MAX));
					playerOneStat.set("competitions_loose", playerOneStat.getInteger("competitions_loose") + 1);
					playerTwoStat.set("competitions_win", playerTwoStat.getInteger("competitions_win") + 1);
					playerOneStat.set("competitions_done", playerOneStat.getInteger("competitions_done") + 1);
					playerTwoStat.set("competitions_done", playerTwoStat.getInteger("competitions_done") + 1);
					_sm1.addString(_playerTwoName);
					_sm1.addNumber(Integer.valueOf(pointsPart));
					broadcastMessage(_sm1, false, false);
				}
				_sm2.addString(_playerOneName);
				_sm2.addNumber(Integer.valueOf(pointsPart));
				broadcastMessage(_sm2, false, false);
				Log.addLog("Olympiad Result: " + _playerOneName + " vs " + _playerTwoName + " ... " + _playerOneName + " lost " + pointsPart + " points by check", "olympiad");
			}
			else if(_reason == 2)
			{
				final boolean arena = _playerOne != null && _playerOne.isInOlympiadMode();
				final int pointsPart = arena && _type == CompType.CLASSED ? Math.max(Math.min(playerOnePoints, playerTwoPoints) / 3, 1) : Math.max(Math.min(playerOnePoints, playerTwoPoints) / 5, 1);
				playerTwoStat.set("olympiad_points", Math.min(playerTwoPoints - pointsPart, Config.OLY_POINTS_MAX));
				save = true;
				if(arena)
				{
					playerOneStat.set("olympiad_points", Math.min(playerOnePoints + pointsPart, Config.OLY_POINTS_MAX));
					playerTwoStat.set("competitions_loose", playerTwoStat.getInteger("competitions_loose") + 1);
					playerOneStat.set("competitions_win", playerOneStat.getInteger("competitions_win") + 1);
					playerOneStat.set("competitions_done", playerOneStat.getInteger("competitions_done") + 1);
					playerTwoStat.set("competitions_done", playerTwoStat.getInteger("competitions_done") + 1);
					_sm1.addString(_playerOneName);
					_sm1.addNumber(Integer.valueOf(pointsPart));
					broadcastMessage(_sm1, false, false);
				}
				_sm2.addString(_playerTwoName);
				_sm2.addNumber(Integer.valueOf(pointsPart));
				broadcastMessage(_sm2, false, false);
				Log.addLog("Olympiad Result: " + _playerOneName + " vs " + _playerTwoName + " ... " + _playerTwoName + " lost " + pointsPart + " points by check", "olympiad");
			}
			if(save)
			{
				Olympiad._nobles.remove(_playerOneID);
				Olympiad._nobles.remove(_playerTwoID);
				Olympiad._nobles.put(_playerOneID, playerOneStat);
				Olympiad._nobles.put(_playerTwoID, playerTwoStat);
				OlympiadDatabase.saveNobleData(_playerOneID);
				OlympiadDatabase.saveNobleData(_playerTwoID);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	protected boolean checkContinue(final boolean zone)
	{
		if(validated)
		{
			OlympiadGame._log.info("Olympiad checkContinue in validated mode");
			return true;
		}
		if(_reason > 0)
			return true;
		final boolean pn1 = playerNot(_playerOneID, zone);
		final boolean pn2 = playerNot(_playerTwoID, zone);
		final int reason = pn1 && pn2 ? 3 : pn1 ? 1 : pn2 ? 2 : 0;
		if(reason > 0)
		{
			endGame(5000L, reason);
			return false;
		}
		return true;
	}

	protected void healPlusBuffs()
	{
		if(!_players.isEmpty())
			for(final Player player : _players)
				if(player != null)
					try
					{
						player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), false);
						player.setCurrentCp(player.getMaxCp());
						if(Config.OLY_BUFFS_PTS)
						{
							final int classId = player.getClassId().getId();
							switch(classId)
							{
								case 94:
								case 95:
								case 96:
								case 97:
								case 98:
								case 103:
								case 104:
								case 105:
								case 110:
								case 111:
								case 112:
								case 115:
								case 116:
								{
									giveBuff(player, SkillTable.getInstance().getInfo(1204, 2), 0);
									continue;
								}
								default:
								{
									giveBuff(player, SkillTable.getInstance().getInfo(1086, 2), 0);
									giveBuff(player, SkillTable.getInstance().getInfo(1204, 1), 1);
									continue;
								}
							}
						}
						else
						{
							giveBuff(player, SkillTable.getInstance().getInfo(1204, 2), 0);
							if(player.isMageClass())
								giveBuff(player, SkillTable.getInstance().getInfo(1085, 1), 1);
							else
								giveBuff(player, SkillTable.getInstance().getInfo(1086, 1), 1);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
	}

	public static void giveBuff(final Creature cha, final Skill skill, final int i)
	{
		EffectTemplate[] templates = skill.getEffectTemplates();
		if(templates == null) {
			_log.warn("Cannot give buff from skill ID[" + skill.getId() + "] LEVEL[" + skill.getLevel() + "]! Skill dont have effects.");
			return;
		}

		for(final EffectTemplate et : templates)
		{
			final Env env = new Env(cha, cha, skill);
			final Abnormal effect = et.getEffect(env);
			effect.setStartTime(System.currentTimeMillis() + i);
			cha.getAbnormalList().add(effect);
		}
	}

	private void matchEnd()
	{
		if(!_players.isEmpty())
			for(final Player player : _players)
				if(player != null)
					try
					{
						if(!player.isInOlympiadMode())
							continue;
						final Servitor summon = player.getServitor();
						if(summon != null)
						{
							if(summon.isCastingNow())
								summon.abortCast(true, false);
							summon.abortAttack(true, false);
						}
						player.sendPacket(new ExOlympiadMode(player.getOlympiadSide()));
						player.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
		if(!_spectators.isEmpty())
			for(final Player pc : getSpectators())
				try
				{
					if(pc == null || !pc.inObserverMode() || pc.getOlympiadObserveId() != _id)
						continue;
					pc.sendPacket(new ExOlympiadMode(3));
					pc.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
	}

	protected boolean makeCompetitionStart()
	{
		_sm = new SystemMessage(1496);
		try
		{
			broadcastMessage(_sm, true, true);
			broadcastMessage(new SystemMessage(283), true, false);
			broadcastInfo(true, true, true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected int getId()
	{
		return _id;
	}

	protected String getTitle()
	{
		return _playerOneName + " : " + _playerTwoName;
	}

	public List<Player> getSpectators()
	{
		List<Player> result = new ArrayList<Player>();
		for(int objectId : _spectators.toArray())
		{
			Player player = GameObjectsStorage.getPlayer(objectId);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	public void addSpectator(int spec)
	{
		_spectators.add(spec);
	}

	protected void removeSpectator(int spec)
	{
		_spectators.remove(spec);
	}

	public void clearSpectators()
	{
		if(!_spectators.isEmpty())
		{
			for(final Player pc : getSpectators())
				try
				{
					if(pc == null || !pc.inObserverMode())
						continue;
					pc.leaveOlympiadObserverMode();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			_spectators.clear();
		}
	}

	public void clearPlayers()
	{
		if(!_players.isEmpty())
			_players.clear();
		_playerOne = null;
		_playerTwo = null;
		_playerOneName = "";
		_playerTwoName = "";
		_playerOneID = 0;
		_playerTwoID = 0;
	}

	public void broadcastInfo(final boolean toPlayers, final boolean toSpectators, final boolean first)
	{
		if(_started != 2 || _playerOne == null || _playerTwo == null)
			return;
		if(toPlayers)
		{
			_playerOne.sendPacket(new ExOlympiadUserInfo(_playerTwo, 2));
			_playerTwo.sendPacket(new ExOlympiadUserInfo(_playerOne, 1));
		}
		if(toSpectators && !_spectators.isEmpty())
			for(final Player sp : getSpectators())
				if(sp != null && sp.getOlympiadObserveId() == _id)
				{
					sp.sendPacket(new ExOlympiadUserInfo(_playerOne, 1));
					sp.sendPacket(new ExOlympiadUserInfo(_playerTwo, 2));
				}
		if(first)
		{
			_playerOne.updateEffectIcons();
			_playerTwo.updateEffectIcons();
		}
	}

	protected void broadcastMessage(final SystemMessage sm, final boolean toAll, final boolean upd)
	{
		if(_playerOne != null)
			_playerOne.sendPacket(sm);
		if(_playerTwo != null)
			_playerTwo.sendPacket(sm);
		if(toAll && !_spectators.isEmpty())
			for(final Player spec : getSpectators())
				try
				{
					if(spec == null || spec.getOlympiadObserveId() != _id)
						continue;
					spec.sendPacket(sm);
					if(!upd)
						continue;
					spec.sendPacket(new ExOlympiadMode(0), new ExOlympiadMode(3));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
	}

	public void setWinner(final int val)
	{
		_winner = val;
	}

	public void setStarted(final int val)
	{
		_started = val;
	}

	public int getStarted()
	{
		return _started;
	}

	public void addDamage(final int side, final double damage)
	{
		if(side == 1)
			_playerOneDamage += damage;
		else
			_playerTwoDamage += damage;
	}

	public CompType getType()
	{
		return _type;
	}

	public synchronized void sheduleTask(final OlympiadGameTask task)
	{
		if(_shedule != null)
			_shedule.cancel(false);
		_task = task;
		_shedule = task.shedule();
	}

	public OlympiadGameTask getTask()
	{
		return _task;
	}

	@Override
	public String toString()
	{
		return "[" + _id + "] " + _playerOneName + " vs " + _playerTwoName;
	}

	public void endGame(final long time, final int reason)
	{
		if(ended)
			return;
		ended = true;
		if((_reason = reason) > 0)
			seconds = (int) (time / 1000L);
		try
		{
			validateWinner();
		}
		catch(Exception e)
		{
			OlympiadGame._log.error("", e);
		}
		sheduleTask(new OlympiadGameTask(this, BattleStatus.Ending, 0, time));
	}

	static
	{
		_log = LoggerFactory.getLogger(OlympiadGame.class);
	}

	private class CheckTask implements Runnable
	{
		@Override
		public void run()
		{
			for(final Player p : _zone.getInsidePlayers())
				if(p != null && !p.inObserverMode() && !p.isGM() && (!p.isInOlympiadMode() || p.getOlympiadGameId() != _id || p.getObjectId() != _playerOneID && p.getObjectId() != _playerTwoID))
				{
					p.teleToLocation(43825, -47950, -790);
					p.sendMessage("You have been teleported by olympiad protect.");
				}
		}
	}
}
