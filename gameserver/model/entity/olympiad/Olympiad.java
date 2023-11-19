package l2s.gameserver.model.entity.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import gnu.trove.impl.sync.TSynchronizedIntList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.MultiValueIntegerMap;

public class Olympiad
{
	private static class SaveTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!Config.ENABLE_OLYMPIAD)
				return;

			try
			{
				OlympiadDatabase.save();
			}
			catch(Exception e)
			{
				_log.error("Olympiad System: Failed to save Olympiad configuration: ", e);
			}
		}
	}

	protected static class CompEndTask implements Runnable
	{
		@Override
		public void run()
		{
			endLock.lock();
			try
			{
				doCompEnd();
			}
			finally
			{
				endLock.unlock();
			}
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(Olympiad.class);
	public static Map<Integer, StatsSet> _nobles;
	public static List<StatsSet> _heroesToBe;
	public static TIntList _nonClassBasedRegisters = new TSynchronizedIntList(new TIntArrayList());
	public static MultiValueIntegerMap _classBasedRegisters = new MultiValueIntegerMap();

	public static final String OLYMPIAD_HTML_FILE = "olympiad/";

	public static final String OLYMPIAD_LOAD_NOBLES = "SELECT * FROM `olympiad_nobles`";
	public static final String OLYMPIAD_SAVE_NOBLES = "REPLACE INTO `olympiad_nobles` (`char_id`, `class_id`, `char_name`, `olympiad_points`, `olympiad_points_past`, `olympiad_points_past_static`, `competitions_done`, `competitions_win`, `competitions_loose`) VALUES (?,?,?,?,?,?,?,?,?)";
	public static final String OLYMPIAD_GET_HEROES = "SELECT `char_id`, `char_name` FROM `olympiad_nobles` WHERE `class_id` = ? AND `competitions_done` >= " + Config.OLY_COMP_DONE_HERO + " AND `competitions_win` >= " + Config.OLY_COMP_WIN_HERO + " ORDER BY `olympiad_points` DESC, `competitions_done` DESC, `competitions_win` DESC";
	public static final String GET_EACH_CLASS_LEADER = "SELECT `char_name`, `olympiad_points` FROM `olympiad_nobles` WHERE `class_id` = ? AND `competitions_done` >= " + Config.OLY_COMP_DONE_HERO + " AND `competitions_win` >= " + Config.OLY_COMP_WIN_HERO + " ORDER BY `olympiad_points` DESC, `competitions_done` DESC, `competitions_win` DESC LIMIT 15";
	public static final String GET_EACH_CLASS_LEADER_PAST = "SELECT `char_name`, `olympiad_points_past_static` FROM `olympiad_nobles` WHERE `class_id` = ? AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC LIMIT 15";
	public static final String OLYMPIAD_CALCULATE_LAST_PERIOD = "UPDATE `olympiad_nobles` SET `olympiad_points_past` = `olympiad_points`, `olympiad_points_past_static` = `olympiad_points` WHERE `competitions_done` >= " + Config.OLY_COMP_DONE_HERO + " AND `competitions_win` >= " + Config.OLY_COMP_WIN_HERO;
	public static final String OLYMPIAD_CLEANUP_NOBLES = "UPDATE `olympiad_nobles` SET `olympiad_points` = " + Config.OLYMPIAD_POINTS_DEFAULT + ", `competitions_done` = 0, `competitions_win` = 0, `competitions_loose` = 0";

	public static final String CHAR_ID = "char_id";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String POINTS_PAST = "olympiad_points_past";
	public static final String POINTS_PAST_STATIC = "olympiad_points_past_static";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WIN = "competitions_win";
	public static final String COMP_LOOSE = "competitions_loose";

	public static long _olympiadEnd;
	public static long _validationEnd;
	public static int _period;
	public static long _nextWeeklyChange;
	public static int _currentCycle;
	protected static long _compEnd;

	private static final Calendar _compStart = Calendar.getInstance();

	public static boolean _inCompPeriod;
	public static boolean _isOlympiadEnd;

	protected static ScheduledFuture<?> _scheduledOlympiadEnd;
	private static ScheduledFuture<?> _scheduledWeeklyTask;
	protected static ScheduledFuture<?> _scheduledValdationTask;
	private static ScheduledFuture<?> _scheduledCompStartTask;
	protected static ScheduledFuture<?> _scheduledCompEndTask;
	private static ScheduledFuture<?> _saveTask;

	public static final ReentrantLock endLock = new ReentrantLock();

	public static final Stadia[] STADIUMS = new Stadia[22];
	public static final int[][][] COORDS = new int[][][] {
			{ { -20853, -21058 }, { -21453, -21058 }, { -20254, -21058 } },
			{ { -109909, -218674 }, { -110508, -218674 }, { -109311, -218674 } },
			{ { -120328, -225041 }, { -120939, -225041 }, { -119718, -225041 } },
			{ { -126638, -218275 }, { -127235, -218275 }, { -126041, -218275 } },
			{ { -114565, -213201 }, { -115162, -213201 }, { -113968, -213201 } },
			{ { -120294, -207398 }, { -120870, -207398 }, { -119718, -207398 } },
			{ { -102529, -209047 }, { -103125, -209047 }, { -101933, -209047 } },
			{ { -109629, -201243 }, { -110237, -201243 }, { -109022, -201243 } },
			{ { -87595, -257844 }, { -88209, -257844 }, { -86981, -257844 } },
			{ { -93889, -251053 }, { -94489, -251053 }, { -93289, -251053 } },
			{ { -77162, -251509 }, { -77780, -251509 }, { -76544, -251509 } },
			{ { -81810, -246024 }, { -82415, -246024 }, { -81206, -246024 } },
			{ { -87557, -240165 }, { -88165, -240165 }, { -86949, -240165 } },
			{ { -69780, -241845 }, { -70382, -241831 }, { -69178, -241858 } },
			{ { -76869, -234008 }, { -77470, -234008 }, { -76268, -234008 } },
			{ { -87567, -225058 }, { -88267, -225058 }, { -86867, -225058 } },
			{ { -93852, -218294 }, { -94466, -218294 }, { -93239, -218294 } },
			{ { -77151, -218734 }, { -77777, -218734 }, { -76525, -218734 } },
			{ { -81787, -213232 }, { -82459, -213232 }, { -81115, -213232 } },
			{ { -87542, -207433 }, { -88166, -207433 }, { -86918, -207433 } },
			{ { -76869, -201268 }, { -77493, -201268 }, { -76245, -201268 } },
			{ { -69784, -209137 }, { -70392, -209137 }, { -69176, -209137 } } };

	public static OlympiadManager _manager;

	public static void load()
	{
		_nobles = new ConcurrentHashMap<Integer, StatsSet>();
		OlympiadDatabase.loadNobles();
		if(!Config.ENABLE_OLYMPIAD)
			return;
		_currentCycle = ServerVariables.getInt("Olympiad_CurrentCycle", 1);
		_period = ServerVariables.getInt("Olympiad_Period", 0);
		_olympiadEnd = ServerVariables.getLong("Olympiad_End", 0L);
		_validationEnd = ServerVariables.getLong("Olympiad_ValdationEnd", 0L);
		_nextWeeklyChange = ServerVariables.getLong("Olympiad_NextWeeklyChange", 0L);
		initStadiums();
		switch(_period)
		{
			case 0:
			{
				if(_olympiadEnd <= 0L)
				{
					OlympiadDatabase.setNewOlympiadEnd();
					break;
				}
				if(_olympiadEnd < Calendar.getInstance().getTimeInMillis())
				{
					doEnd();
					if(_validationEnd > Calendar.getInstance().getTimeInMillis())
						_scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), getMillisToValidationEnd());
					else
						doValidate();
					OlympiadDatabase.save();
					break;
				}
				_isOlympiadEnd = false;
				break;
			}
			case 1:
			{
				if(_validationEnd > Calendar.getInstance().getTimeInMillis())
				{
					_isOlympiadEnd = true;
					_scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), getMillisToValidationEnd());
					break;
				}
				doValidate();
				OlympiadDatabase.save();
				break;
			}
			default:
			{
				_log.warn("Olympiad System: something went wrong in loading! Period = " + _period);
				return;
			}
		}
		_log.info("Olympiad System: Loading Olympiad System....");
		if(_period == 0)
			_log.info("Olympiad System: Currently in Olympiad Period");
		else
			_log.info("Olympiad System: Currently in Validation Period");
		_log.info("Olympiad System: Period Ends....");
		long milliToEnd;
		if(_period == 0)
			milliToEnd = getMillisToOlympiadEnd();
		else
			milliToEnd = getMillisToValidationEnd();
		final double numSecs = milliToEnd / 1000L % 60L;
		double countDown = (milliToEnd / 1000L - numSecs) / 60.0;
		final int numMins = (int) Math.floor(countDown % 60.0);
		countDown = (countDown - numMins) / 60.0;
		final int numHours = (int) Math.floor(countDown % 24.0);
		final int numDays = (int) Math.floor((countDown - numHours) / 24.0);
		_log.info("Olympiad System: In " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		if(_period == 0)
		{
			_log.info("Olympiad System: Next Weekly Change is in...");
			milliToEnd = getMillisToWeekChange();
			final double numSecs2 = milliToEnd / 1000L % 60L;
			double countDown2 = (milliToEnd / 1000L - numSecs2) / 60.0;
			final int numMins2 = (int) Math.floor(countDown2 % 60.0);
			countDown2 = (countDown2 - numMins2) / 60.0;
			final int numHours2 = (int) Math.floor(countDown2 % 24.0);
			final int numDays2 = (int) Math.floor((countDown2 - numHours2) / 24.0);
			_log.info("Olympiad System: In " + numDays2 + " days, " + numHours2 + " hours and " + numMins2 + " mins.");
		}
		_log.info("Olympiad System: Loaded " + _nobles.size() + " Noblesses");
		if(_period == 0)
			init();
		if(Config.OLY_SAVE_DELAY > 0)
		{
			if(_saveTask != null)
				_saveTask.cancel(false);
			_saveTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveTask(), Config.OLY_SAVE_DELAY * 60000L, Config.OLY_SAVE_DELAY * 60000L);
		}
	}

	public static void doEnd()
	{
		_isOlympiadEnd = true;
		if(_scheduledWeeklyTask != null)
			_scheduledWeeklyTask.cancel(false);
		_validationEnd = _olympiadEnd + Config.ALT_OLY_VPERIOD;
		OlympiadDatabase.saveNobleData();
		_period = 1;
		Hero.getInstance().clearHeroes();
	}

	public static void doValidate()
	{
		OlympiadDatabase.sortHeroesToBe();
		giveHeroBonus();
		OlympiadDatabase.saveNobleData();
		if(Hero.getInstance().computeNewHeroes(_heroesToBe))
			_log.warn("Olympiad: No heroes while computing new heroes!");
		_period = 0;
		++_currentCycle;
		OlympiadDatabase.cleanupNobles();
		OlympiadDatabase.setNewOlympiadEnd();
	}

	public static void doCompEnd()
	{
		if(isOlympiadEnd())
			return;
		_inCompPeriod = false;
		try
		{
			if(_manager != null)
			{
				if(!_manager.getOlympiadGames().isEmpty())
				{
					_scheduledCompEndTask = ThreadPoolManager.getInstance().schedule(new CompEndTask(), 60000L);
					return;
				}
				_manager.disable();
				_manager = null;
			}
			Announcements.getInstance().announceToAll(new SystemMessage(1642));
			_log.info("Olympiad System: Olympiad Game Ended.");
			OlympiadDatabase.save();
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Failed to save Olympiad configuration:", e);
		}
		init();
	}

	private static void initStadiums()
	{
		for(int i = 0; i < STADIUMS.length; ++i)
			if(STADIUMS[i] == null)
				STADIUMS[i] = new Stadia();
	}

	public static void init()
	{
		if(isValidationPeriod())
			return;

		_compStart.setTimeInMillis(Config.OLYMPIAD_START_TIME.next(System.currentTimeMillis() - Config.ALT_OLY_CPERIOD));
		_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
		if(_compEnd < System.currentTimeMillis())
		{
			_compStart.setTimeInMillis(Config.OLYMPIAD_START_TIME.next(System.currentTimeMillis()));
			_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
		}

		if(_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(false);
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().schedule(new OlympiadEndTask(), getMillisToOlympiadEnd());

		updateCompStatus();

		if(_scheduledWeeklyTask != null)
			_scheduledWeeklyTask.cancel(false);
		_scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WeeklyTask(), getMillisToWeekChange(), Config.ALT_OLY_WPERIOD);
	}

	public static boolean setOlyEnd(final long time)
	{
		if(isValidationPeriod())
			return false;

		_olympiadEnd = time;
		if(_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(false);
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().schedule(new OlympiadEndTask(), getMillisToOlympiadEnd());
		ServerVariables.set("Olympiad_End", _olympiadEnd);
		return true;
	}

	public static boolean isClassedBattlesAllowed()
	{
		return ArrayUtils.contains(Config.OLY_CLASSED_GAMES_DAYS, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
	}

	public static synchronized boolean registerParticipant(final Player noble, final boolean classBased)
	{
		if(noble.getClassId().getLevel() != 4)
			return false;
		if(!_inCompPeriod || _isOlympiadEnd)
		{
			final SystemMessage sm = new SystemMessage(1651);
			noble.sendPacket(sm);
			return false;
		}
		if(getMillisToCompEnd() <= 600000L)
		{
			final SystemMessage sm = new SystemMessage(1803);
			noble.sendPacket(sm);
			return false;
		}
		if(classBased && !isClassedBattlesAllowed())
		{
			noble.sendMessage(new CustomMessage("l2s.gameserver.model.entity.Olympiad.ClassedInDays"));
			return false;
		}
		if(noble.isCursedWeaponEquipped())
		{
			noble.sendMessage(new CustomMessage("l2s.gameserver.model.entity.Olympiad.Cursed"));
			return false;
		}
		if(!noble.isNoble())
		{
			final SystemMessage sm = new SystemMessage(1501);
			noble.sendPacket(sm);
			return false;
		}
		if(noble.getBaseClassId() != noble.getClassId().getId())
		{
			final SystemMessage sm = new SystemMessage(1500);
			noble.sendPacket(sm);
			return false;
		}
		addNoble(noble);
		if(noble.getOlympiadGameId() > -1)
		{
			final OlympiadGame game = getOlympiadGame(noble.getOlympiadGameId());
			if(game != null)
			{
				SystemMessage sm;
				if(game.getType() == CompType.CLASSED)
					sm = new SystemMessage(1689);
				else
					sm = new SystemMessage(1690);
				noble.sendPacket(sm);
			}
			return false;
		}
		if(noble.getTeam() != 0)
		{
			noble.sendMessage("You are event participant!");
			return false;
		}
		if(getNoblePoints(noble.getObjectId()) < Config.OLY_MIN_REG_POINTS)
		{
			noble.sendMessage(new CustomMessage("l2s.gameserver.model.entity.Olympiad.LessPoints").addNumber(Config.OLY_MIN_REG_POINTS));
			return false;
		}
		if(_nonClassBasedRegisters.contains(noble.getObjectId()))
		{
			noble.sendPacket(new SystemMessage(1690));
			return false;
		}
		if(_classBasedRegisters.containsValue(noble.getObjectId()))
		{
			noble.sendPacket(new SystemMessage(1689));
			return false;
		}
		if(classBased)
		{
			final StatsSet nobleInfo = _nobles.get(noble.getObjectId());
			final int classId = nobleInfo.getInteger("class_id");
			if(Config.OLY_NO_SAME_IP || Config.OLY_NO_SAME_PC)
			{
				TIntList list = _classBasedRegisters.get(classId);
				if(list != null)
					for(int id : list.toArray())
					{
						final Player p = GameObjectsStorage.getPlayer(id);
						if(p != null)
						{
							if(Config.OLY_NO_SAME_IP && p.getIP().equals(noble.getIP()))
							{
								noble.sendMessage(noble.isLangRus() ? "\u0414\u0430\u043d\u043d\u044b\u0439 IP \u0443\u0436\u0435 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u044f." : "This IP is already on the waiting list.");
								return false;
							}
							if(Config.OLY_NO_SAME_PC && p.isSameHWID(noble.getHWID()))
							{
								noble.sendMessage(noble.isLangRus() ? "\u0414\u0430\u043d\u043d\u044b\u0439 \u041f\u041a \u0443\u0436\u0435 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u044f." : "This PC is already on the waiting list.");
								return false;
							}
							continue;
						}
					}
			}
			_classBasedRegisters.put(classId, noble.getObjectId());
			noble.sendPacket(new SystemMessage(1503));
		}
		else
		{
			if((Config.OLY_NO_SAME_IP || Config.OLY_NO_SAME_PC) && !_nonClassBasedRegisters.isEmpty())
				for(int id2 : _nonClassBasedRegisters.toArray())
				{
					Player p2 = GameObjectsStorage.getPlayer(id2);
					if(p2 != null)
					{
						if(Config.OLY_NO_SAME_IP && p2.getIP().equals(noble.getIP()))
						{
							noble.sendMessage(noble.isLangRus() ? "\u0414\u0430\u043d\u043d\u044b\u0439 IP \u0443\u0436\u0435 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u044f." : "This IP is already on the waiting list.");
							return false;
						}

						if(Config.OLY_NO_SAME_PC && p2.isSameHWID(noble.getHWID()))
						{
							noble.sendMessage(noble.isLangRus() ? "\u0414\u0430\u043d\u043d\u044b\u0439 \u041f\u041a \u0443\u0436\u0435 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u044f." : "This PC is already on the waiting list.");
							return false;
						}
						continue;
					}
				}
			_nonClassBasedRegisters.add(noble.getObjectId());
			noble.sendPacket(new SystemMessage(1504));
		}
		if(Config.ALLOW_OLY_HENNA)
			noble.sendMessage("\u0421\u043e\u0432\u0435\u0442\u0443\u0435\u043c \u043f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0443 \u0441\u0432\u043e\u0438\u0445 \u043a\u0440\u0430\u0441\u043e\u043a \u0434\u043b\u044f \u041e\u043b\u0438\u043c\u043f\u0438\u0430\u0434\u044b ( \u043a\u043e\u043c\u0430\u043d\u0434\u0430 .olyhenna )");
		return true;
	}

	public static synchronized boolean unRegisterNoble(final Player noble, final boolean disconnect)
	{
		if(!disconnect)
		{
			if(!_inCompPeriod || _isOlympiadEnd)
			{
				final SystemMessage sm = new SystemMessage(1651);
				noble.sendPacket(sm);
				return false;
			}
			if(!noble.isNoble())
			{
				final SystemMessage sm = new SystemMessage(1501);
				noble.sendPacket(sm);
				return false;
			}
			if(!isRegistered(noble))
			{
				final SystemMessage sm = new SystemMessage(1506);
				noble.sendPacket(sm);
				return false;
			}
		}
		final int gameId = noble.getOlympiadGameId();
		if(gameId != -1)
		{
			final OlympiadGame game = getOlympiadGame(gameId);
			if(game != null)
				try
				{
					if(!game.validated)
						game.endGame(5000L, noble.getOlympiadSide());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		}
		_classBasedRegisters.removeValue(noble.getObjectId());
		_nonClassBasedRegisters.remove(new Integer(noble.getObjectId()));
		if(!disconnect)
			noble.sendPacket(new SystemMessage(1505));
		return true;
	}

	public static synchronized boolean unRegisterCursed(final Player noble, final String name)
	{
		SystemMessage sm = new SystemMessage(1857).addString(name);
		noble.sendPacket(sm);
		final int gameId = noble.getOlympiadGameId();
		if(gameId != -1)
		{
			final OlympiadGame game = getOlympiadGame(gameId);
			if(game != null)
				try
				{
					sm = new SystemMessage(1856).addString(name);
					if(game._playerOne != null && game._playerOne.getObjectId() != noble.getObjectId())
						game._playerOne.sendPacket(sm);
					else if(game._playerTwo != null && game._playerTwo.getObjectId() != noble.getObjectId())
						game._playerTwo.sendPacket(sm);
					if(!game.validated)
						game.endGame(1000L, 3);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		}
		_classBasedRegisters.removeValue(noble.getObjectId());
		_nonClassBasedRegisters.remove(new Integer(noble.getObjectId()));
		return true;
	}

	private static synchronized void updateCompStatus()
	{
		final long milliToStart = getMillisToCompBegin();
		final double numSecs = milliToStart / 1000L % 60L;
		double countDown = (milliToStart / 1000L - numSecs) / 60.0;
		final int numMins = (int) Math.floor(countDown % 60.0);
		countDown = (countDown - numMins) / 60.0;
		final int numHours = (int) Math.floor(countDown % 24.0);
		final int numDays = (int) Math.floor((countDown - numHours) / 24.0);
		_log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		_log.info("Olympiad System: Event starts/started: " + _compStart.getTime());
		if(_scheduledCompStartTask != null)
			_scheduledCompStartTask.cancel(false);
		_scheduledCompStartTask = ThreadPoolManager.getInstance().schedule(new CompStartTask(), getMillisToCompBegin());
	}

	private static long getMillisToOlympiadEnd()
	{
		return _olympiadEnd - System.currentTimeMillis();
	}

	static long getMillisToValidationEnd()
	{
		if(_validationEnd > System.currentTimeMillis())
			return _validationEnd - System.currentTimeMillis();
		return 10L;
	}

	public static boolean isValidationPeriod()
	{
		return _period == 1;
	}

	public static boolean isOlympiadEnd()
	{
		return _isOlympiadEnd;
	}

	public static boolean inCompPeriod()
	{
		return _inCompPeriod;
	}

	private static long getMillisToCompBegin()
	{
		if(_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && _compEnd > Calendar.getInstance().getTimeInMillis())
			return 10L;
		if(_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
			return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		return setNewCompBegin();
	}

	private static long setNewCompBegin()
	{
		_compStart.setTimeInMillis(Config.OLYMPIAD_START_TIME.next(System.currentTimeMillis() - Config.ALT_OLY_CPERIOD));
		_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
		if(_compEnd < System.currentTimeMillis())
		{
			_compStart.setTimeInMillis(Config.OLYMPIAD_START_TIME.next(System.currentTimeMillis()));
			_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
		}

		_log.info("Olympiad System: New Schedule @ " + _compStart.getTime());

		return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	public static long getMillisToCompEnd()
	{
		return _compEnd - Calendar.getInstance().getTimeInMillis();
	}

	private static long getMillisToWeekChange()
	{
		if(_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
			return _nextWeeklyChange - Calendar.getInstance().getTimeInMillis();
		return 10L;
	}

	public static synchronized void addWeeklyPoints()
	{
		if(isValidationPeriod())
			return;

		for(final Integer nobleId : _nobles.keySet())
		{
			final StatsSet nobleInfo = _nobles.get(nobleId);
			if(nobleInfo != null)
				nobleInfo.set("olympiad_points", Math.min(nobleInfo.getInteger("olympiad_points") + Config.OLYMPIAD_POINTS_WEEKLY, Config.OLY_POINTS_MAX));
		}
	}

	public static String[] getAllTitles()
	{
		final String[] msg = new String[STADIUMS.length];
		for(int i = 0; i < STADIUMS.length; ++i)
			if(_manager != null && _manager.getOlympiadInstance(i) != null && _manager.getOlympiadInstance(i).getStarted() > 0)
				msg[i] = i + 1 + "_In Progress_" + _manager.getOlympiadInstance(i).getTitle();
			else
				msg[i] = i + 1 + "_Initial State";
		return msg;
	}

	public static int getCurrentCycle()
	{
		return _currentCycle;
	}

	public static synchronized void addSpectator(final int id, final Player spectator, final boolean enter)
	{
		if(spectator.getOlympiadGameId() != -1 || isRegistered(spectator))
		{
			spectator.sendPacket(new SystemMessage(1693));
			return;
		}
		if(_manager == null || _manager.getOlympiadInstance(id) == null)
		{
			spectator.sendPacket(new SystemMessage(1651));
			return;
		}
		if(enter && spectator.getTeam() != 0)
		{
			spectator.sendMessage("You are some team member!");
			return;
		}
		final Location loc = new Location(COORDS[id][0][0], COORDS[id][0][1], id == 0 ? -3024 : -3328);
		if(enter)
			spectator.enterOlympiadObserverMode(loc, id);
		else
			spectator.teleToLocation(loc);
		final OlympiadGame game = _manager.getOlympiadInstance(id);
		if(game != null)
		{
			game.addSpectator(spectator.getObjectId());
			if(game.getStarted() == 2)
				spectator.sendPacket(new SystemMessage(283));
			game.broadcastInfo(false, true, true);
		}
	}

	public static synchronized void removeSpectator(final int id, final Player spectator)
	{
		if(_manager == null)
			return;
		final OlympiadGame game = _manager.getOlympiadInstance(id);
		if(game == null)
			return;
		game.removeSpectator(spectator.getObjectId());
		if(game.getStarted() == 2)
			spectator.sendPacket(new SystemMessage(284));
	}

	public static OlympiadGame getOlympiadGame(final int gameId)
	{
		if(_manager == null || gameId < 0)
			return null;
		return _manager.getOlympiadGames().get(gameId);
	}

	public static synchronized int[] getWaitingList()
	{
		if(!inCompPeriod())
			return null;
		final int[] array = { _classBasedRegisters.totalSize(), _nonClassBasedRegisters.size() };
		return array;
	}

	public static synchronized void giveHeroBonus()
	{
		if(_heroesToBe == null || _heroesToBe.isEmpty())
			return;
		for(final StatsSet hero : _heroesToBe)
		{
			final StatsSet noble = _nobles.get(hero.getInteger("char_id"));
			if(noble != null)
				noble.set("olympiad_points", Math.min(noble.getInteger("olympiad_points") + Config.OLY_POINTS_HERO, Config.OLY_POINTS_MAX));
		}
	}

	public static synchronized int getNoblessePasses(final int objId)
	{
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		final int points = noble.getInteger("olympiad_points_past");
		if(points <= 50)
			return 0;
		noble.set("olympiad_points_past", 0);
		OlympiadDatabase.saveNobleData(objId);
		return points * 1000;
	}

	public static synchronized boolean isRegistered(final Player noble)
	{
		return _classBasedRegisters.containsValue(noble.getObjectId()) || _nonClassBasedRegisters.contains(noble.getObjectId());
	}

	public static synchronized int getNoblePoints(final int objId)
	{
		if(_nobles.size() == 0)
			return 0;
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger("olympiad_points");
	}

	public static synchronized int getNoblePointsByName(final String name)
	{
		if(_nobles.size() == 0)
			return -1;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, name);
			rset = statement.executeQuery();
			int objId = 0;
			if(rset.next())
				objId = rset.getInt("obj_Id");
			if(objId > 0)
			{
				final StatsSet noble = _nobles.get(objId);
				if(noble == null)
					return -1;
				return noble.getInteger("olympiad_points");
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return -1;
	}

	public static void changeNobleName(final int objId, final String newName)
	{
		if(_nobles.size() == 0)
			return;
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return;
		noble.set("char_name", newName);
		OlympiadDatabase.saveNobleData(objId);
	}

	public static String getNobleName(final int objId)
	{
		if(_nobles.size() == 0)
			return null;
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return null;
		return noble.getString("char_name");
	}

	public static synchronized int getNoblePointsPast(final int objId)
	{
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger("olympiad_points_past");
	}

	public static synchronized int getCompetitionDone(final int objId)
	{
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger("competitions_done");
	}

	public static synchronized int getCompetitionWin(final int objId)
	{
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger("competitions_win");
	}

	public static synchronized int getCompetitionLoose(final int objId)
	{
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger("competitions_loose");
	}

	public static boolean manualStartOlympiad()
	{
		if(_inCompPeriod)
			return false;
		if(_manager != null)
			_manager.disable();
		_inCompPeriod = true;
		_manager = new OlympiadManager();
		new Thread(_manager).start();
		if(_scheduledCompStartTask != null)
		{
			_scheduledCompStartTask.cancel(false);
			_scheduledCompStartTask = null;
		}
		if(_scheduledCompEndTask != null)
			_scheduledCompEndTask.cancel(false);
		_scheduledCompEndTask = ThreadPoolManager.getInstance().schedule(new CompEndTask(), Config.ALT_OLY_CPERIOD);
		Announcements.getInstance().announceToAll(new SystemMessage(1641));
		_log.info("Olympiad System: Olympiad Game Started by Admin.");
		return true;
	}

	public static boolean manualStopOlympiad()
	{
		if(!_inCompPeriod)
			return false;
		_inCompPeriod = false;
		if(_scheduledCompEndTask != null)
		{
			_scheduledCompEndTask.cancel(false);
			_scheduledCompEndTask = null;
		}
		Announcements.getInstance().announceToAll(new SystemMessage(1642));
		_log.info("Olympiad System: Olympiad Game Ended by Admin.");
		OlympiadDatabase.save();
		if(_manager != null)
			_manager.disable();
		return true;
	}

	public static void manualSelectHeroes()
	{
		Announcements.getInstance().announceToAll(new SystemMessage(1640).addNumber(Integer.valueOf(_currentCycle)));
		Announcements.getInstance().announceToAll("Olympiad Validation Period has began");

		_isOlympiadEnd = true;

		if(_scheduledWeeklyTask != null)
			_scheduledWeeklyTask.cancel(false);

		if(_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(false);

		OlympiadDatabase.saveNobleData();

		_period = 1;

		Hero.getInstance().clearHeroes();
		OlympiadDatabase.sortHeroesToBe();
		giveHeroBonus();
		OlympiadDatabase.saveNobleData();

		_log.info("Olympiad: Sorted " + _heroesToBe.size() + " new heroes.");

		if(Hero.getInstance().computeNewHeroes(_heroesToBe))
			_log.warn("Olympiad: No heroes while computing new heroes!");

		Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");

		_period = 0;
		_currentCycle++;

		OlympiadDatabase.cleanupNobles();
		OlympiadDatabase.setNewOlympiadEnd();

		init();

		OlympiadDatabase.save();
	}

	public static void manualSetNoblePoints(final int objId, final int points)
	{
		final StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return;

		noble.set("olympiad_points", Math.min(points, Config.OLY_POINTS_MAX));
		OlympiadDatabase.saveNobleData(objId);
	}

	public static synchronized void addNoble(final Player noble)
	{
		if(!_nobles.containsKey(noble.getObjectId()))
		{
			int classId = noble.getBaseClassId();
			if(classId < 88)
				for(final ClassId id : ClassId.values())
					if(id.level() == 3 && id.getParent().getId() == classId)
					{
						classId = id.getId();
						break;
					}
			final StatsSet statDat = new StatsSet();
			statDat.set("class_id", classId);
			statDat.set("char_name", noble.getName());
			statDat.set("olympiad_points", Config.OLYMPIAD_POINTS_DEFAULT);
			statDat.set("olympiad_points_past", 0);
			statDat.set("olympiad_points_past_static", 0);
			statDat.set("competitions_done", 0);
			statDat.set("competitions_win", 0);
			statDat.set("competitions_loose", 0);
			_nobles.put(noble.getObjectId(), statDat);
			OlympiadDatabase.saveNobleData(noble.getObjectId());
		}
	}

	public static synchronized void removeNoble(final Player noble)
	{
		_nobles.remove(noble.getObjectId());
		OlympiadDatabase.saveNobleData();
	}
}
