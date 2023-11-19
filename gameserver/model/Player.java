package l2s.gameserver.model;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.commons.ban.BanBindType;
import l2s.gameserver.instancemanager.*;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.templates.OptionDataTemplate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.dbcp.DbUtils;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Bonus;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.GameTimeController;
import l2s.gameserver.RecipeController;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.ai.PlayableAI;
import l2s.gameserver.ai.PlayerAI;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.communitybbs.BB.Forum;
import l2s.gameserver.communitybbs.Manager.BuffBBSManager;
import l2s.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2s.gameserver.dao.AccountBonusDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.Status;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.PlayerListenerList;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ScriptAnswerListener;
import l2s.gameserver.listener.actor.player.impl.SummonAnswerListener;
import l2s.gameserver.listener.actor.recorder.PlayerStatsChangeRecorder;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.base.PlayerAccess;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.model.expertise.ArmorExpertise;
import l2s.gameserver.model.expertise.WeaponExpertise;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.instances.GuardInstance;
import l2s.gameserver.model.instances.HennaInstance;
import l2s.gameserver.model.instances.HitmanInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetBabyInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.instances.TamedBeastInstance;
import l2s.gameserver.model.items.PcFreight;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.items.PcWarehouse;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.ChangeAccessLevel;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.c2s.EnterWorld;
import l2s.gameserver.network.l2.s2c.AutoAttackStart;
import l2s.gameserver.network.l2.s2c.CameraMode;
import l2s.gameserver.network.l2.s2c.ChangeWaitType;
import l2s.gameserver.network.l2.s2c.CharInfo;
import l2s.gameserver.network.l2.s2c.CharSit;
import l2s.gameserver.network.l2.s2c.ConfirmDlg;
import l2s.gameserver.network.l2.s2c.EnchantResult;
import l2s.gameserver.network.l2.s2c.EtcStatusUpdate;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.ExDuelUpdateUserInfo;
import l2s.gameserver.network.l2.s2c.ExOlympiadMode;
import l2s.gameserver.network.l2.s2c.ExOlympiadSpelledInfo;
import l2s.gameserver.network.l2.s2c.ExPCCafePointInfo;
import l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCount;
import l2s.gameserver.network.l2.s2c.ExUseSharedGroupItem;
import l2s.gameserver.network.l2.s2c.GetItem;
import l2s.gameserver.network.l2.s2c.HennaInfo;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.MagicEffectIcons;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.MyTargetSelected;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.ObserverEnd;
import l2s.gameserver.network.l2.s2c.ObserverStart;
import l2s.gameserver.network.l2.s2c.PartySmallWindowUpdate;
import l2s.gameserver.network.l2.s2c.PartySpelled;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDelete;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAll;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListUpdate;
import l2s.gameserver.network.l2.s2c.PrivateStoreListBuy;
import l2s.gameserver.network.l2.s2c.PrivateStoreListSell;
import l2s.gameserver.network.l2.s2c.PrivateStoreManageList;
import l2s.gameserver.network.l2.s2c.PrivateStoreManageListBuy;
import l2s.gameserver.network.l2.s2c.PrivateStoreMsgBuy;
import l2s.gameserver.network.l2.s2c.PrivateStoreMsgSell;
import l2s.gameserver.network.l2.s2c.QuestList;
import l2s.gameserver.network.l2.s2c.RecipeShopMsg;
import l2s.gameserver.network.l2.s2c.RecipeShopSellList;
import l2s.gameserver.network.l2.s2c.RelationChanged;
import l2s.gameserver.network.l2.s2c.Ride;
import l2s.gameserver.network.l2.s2c.SendTradeDone;
import l2s.gameserver.network.l2.s2c.SetupGauge;
import l2s.gameserver.network.l2.s2c.ShortBuffStatusUpdate;
import l2s.gameserver.network.l2.s2c.ShortCutInit;
import l2s.gameserver.network.l2.s2c.ShortCutRegister;
import l2s.gameserver.network.l2.s2c.SkillCoolTime;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.Snoop;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.SpecialCamera;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.TargetSelected;
import l2s.gameserver.network.l2.s2c.TargetUnselected;
import l2s.gameserver.network.l2.s2c.TitleUpdate;
import l2s.gameserver.network.l2.s2c.UserInfo;
import l2s.gameserver.network.l2.s2c.ValidateLocation;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.tables.CharTemplateTable;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.HennaTable;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.MapRegionTable;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.taskmanager.AutoSaveManager;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.FishTemplate;
import l2s.gameserver.templates.HennaTemplate;
import l2s.gameserver.templates.item.ArmorTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.utils.EffectsComparator;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.SiegeUtils;
import l2s.gameserver.utils.SqlBatch;
import l2s.gameserver.utils.Stat;
import l2s.gameserver.utils.Util;

public final class Player extends Playable
{

	//TODO: Вынести таски в отдельный файл.
	private class UpdateEffectIcons implements Runnable
	{
		@Override
		public void run()
		{
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

	private class DeleteTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isConnected())
				deleteMe();
		}
	}

	private class FallTask implements Runnable
	{
		@Override
		public void run()
		{
			final int safeHeight = (int) calcStat(Stats.FALL_SAFE, getBaseTemplate().baseSafeFallHeight, null, null);
			final int dz = _fallZ - getZ();
			if(!isFlying() && !isInZone(Zone.ZoneType.water) && dz > safeHeight)
				falling(dz, safeHeight);
			_fallTask = null;
		}
	}

	private class MortalTask implements Runnable
	{
		@Override
		public void run()
		{
			setActive();
		}
	}

	public static class DeleteBotTask implements Runnable
	{
		private int _objId;

		public DeleteBotTask(final int objId)
		{
			_objId = objId;
		}

		@Override
		public void run()
		{}
	}

	public static final int DEFAULT_NAME_COLOR = 0xFFFFFF;
	public static final int DEFAULT_TITLE_COLOR = 0xFFFF77;

	private Bonus _bonus = new Bonus();
	private Future<?> _bonusExpiration;
	private boolean _premium;

	private static final int SCPPHC = SkillTable.getSkillHashCode(1324, 1);
	private static final String key_protect_path = "scripts/commands/voiced/charKey/char_key_protect.html";
	private static final String key_forced_path = "scripts/commands/voiced/charKey/char_key_forced.html";
	private static final int reon = 55501730;
	private static final boolean _dds = true;
	public static final int saver = 55501730;

	public HashMap<Integer, SubClass> _classlist = new HashMap<Integer, SubClass>(4 + Config.ALT_GAME_SUB_ADD);

	public static final short STORE_PRIVATE_NONE = 0;
	public static final short STORE_PRIVATE_SELL = 1;
	public static final short STORE_PRIVATE_BUY = 3;
	public static final short STORE_PRIVATE_MANUFACTURE = 5;
	public static final short STORE_OBSERVING_GAMES = 7;
	public static final short STORE_PRIVATE_SELL_PACKAGE = 8;

	public static final int RANK_VAGABOND = 0;
	public static final int RANK_VASSAL = 1;
	public static final int RANK_HEIR = 2;
	public static final int RANK_KNIGHT = 3;
	public static final int RANK_WISEMAN = 4;
	public static final int RANK_BARON = 5;
	public static final int RANK_VISCOUNT = 6;
	public static final int RANK_COUNT = 7;
	public static final int RANK_MARQUIS = 8;

	public static final int[] EXPERTISE_LEVELS = new int[] { 0, 20, 40, 52, 61, 76, Integer.MAX_VALUE };

	static final Logger _log = LoggerFactory.getLogger(Player.class);
	private GameClient _connection;
	private int _charId = 199546;
	private int _karma;
	private int _pvpKills;
	private int _pkKills;

	private int _nameColor = DEFAULT_NAME_COLOR;
	private int _titlecolor = DEFAULT_TITLE_COLOR;

	private boolean _overloaded;
	private int _recomHave;
	private int _recomLeft;
	private final List<Integer> _recomChars = new ArrayList<Integer>();
	private int _curWeightPenalty = 0;
	private int _deleteTimer;
	private final PcInventory _inventory = new PcInventory(this);
	private PcWarehouse _warehouse = new PcWarehouse(this);
	private PcFreight _freight = new PcFreight(this);
	boolean sittingTaskLaunched;
	private boolean AutoLootAdena = Config.AUTO_LOOT_ADENA;
	private boolean AutoLootItems = Config.AUTO_LOOT_ITEMS;
	private boolean AutoLootHerbs = Config.AUTO_LOOT_HERBS;
	private boolean AutoLootList = true;
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	private final IntObjectMap<QuestState> _quests = new HashIntObjectMap<QuestState>();
	private ShortCuts _shortCuts = new ShortCuts(this);
	private MacroList _macroses = new MacroList(this);
	private TradeList _tradeList;
	private ManufactureList _createList;
	private ConcurrentLinkedQueue<TradeItem> _sellList;
	private ConcurrentLinkedQueue<TradeItem> _buyList;
	private short _privatestore;
	private ClassId _skillLearningClassId;
	private final HennaInstance[] _henna = new HennaInstance[3];
	private short _hennaSTR;
	private short _hennaINT;
	private short _hennaDEX;
	private short _hennaMEN;
	private short _hennaWIT;
	private short _hennaCON;
	private Servitor _summon = null;
	private DecoyInstance _decoy = null;
	private Map<Integer, EffectCubic> _cubics = null;
	private Transaction _transaction;
	public Radar radar;
	public WeaponExpertise wepex;
	public ArmorExpertise armex;
	private Party _party;
	private Clan _clan;
	private int _pledgeClass = 0;
	private int _pledgeType = 0;
	private int _powerGrade = 0;
	private int _lvlJoinedAcademy = 0;
	private int _apprentice = 0;
	private long _createTime;
	private long _onlineTime;
	private long _leaveClanTime;
	private long _deleteClanTime;
	private long _NoChannel;
	private long _NoChannelBegin;
	private int _accessLevel;
	private PlayerAccess _playerAccess = new PlayerAccess();
	private boolean _messageRefusal = false;
	private boolean _tradeRefusal = false;
	private boolean _exchangeRefusal = false;
	public boolean _exploring = false;
	private ItemInstance _arrowItem;
	private WeaponTemplate _fistsWeaponItem;
	private long _uptime;
	private String _accountName;
	private HashMap<Integer, String> _chars = new HashMap<Integer, String>(8);
	public byte updateKnownCounter = 0;
	private Map<Integer, RecipeList> _recipebook = new TreeMap<Integer, RecipeList>();
	private Map<Integer, RecipeList> _commonrecipebook = new TreeMap<Integer, RecipeList>();
	private int _usedInventoryPercents = 0;
	private int _weightPercents = 0;
	public int expertiseIndex = 0;
	public int armorExpertise = 0;
	public int weaponExpertise = 0;
	private ItemInstance _enchantScroll = null;
	private Warehouse.WarehouseType _usingWHType;
	private boolean _isOnline = false;
	private boolean _isDeleting = false;
	protected boolean _inventoryDisable = false;
	private HardReference<NpcInstance> _lastNpc = HardReferences.emptyRef();
	private MultiSellHolder.MultiSellListContainer _multisell = null;
	private Set<Integer> _activeSoulShots = new CopyOnWriteArraySet<Integer>();
	private boolean _invisible = false;
	private Location _obsLoc = new Location();
	private WorldRegion _observNeighbor;
	private byte _observerMode = 0;
	public int _telemode = 0;
	public Location _stablePoint = null;
	public int[] _loto = new int[5];
	public int[] _race = new int[2];
	private final Map<Integer, String> _blockList = new ConcurrentSkipListMap<Integer, String>();
	private boolean _blockAll = false;
	private boolean _hero = false;

	private int _team = 0;
	private boolean _checksForTeam = false;
	private long _lastAccess;
	private Vehicle _vehicle;
	private Location _inVehiclePosition;
	protected int _baseClass = -1;
	protected SubClass _activeClass = null;
	private boolean _isSitting = false;
	private int _sittingObject = 0;
	private boolean _noble = false;
	private boolean _inOlympiadMode = false;
	private int _olympiadGameId = -1;
	private int _olympiadSide = -1;
	private int _olympiadObserveId = -1;
	private int _varka = 0;
	private int _ketra = 0;
	private int _ram = 0;
	private byte[] _keyBindings;
	public ScheduledFuture<?> _taskWater;
	private Future<?> _autoSaveTask;
	protected HashMap<Integer, Long> _StatKills;
	protected HashMap<Integer, Long> _StatDrop;
	protected HashMap<Integer, Long> _StatCraft;
	private Forum _forumMemo;
	private int _cursedWeaponEquippedId = 0;
	private final Fishing _fishing = new Fishing(this);
	private boolean _isFishing;
	private Future<?> _kickTask;
	private Future<?> _pcCafePointsTask;
	private boolean _isInCombatZone;
	private boolean _isOnSiegeField;
	private int _siegeFieldId;
	private boolean _isInPeaceZone;
	private boolean _isInSSZone;
	private boolean _offline = false;
	private int _pcBangPoints;
	private int _expandInventory = 0;
	private int _incMaxLoad = 0;
	private int _lastCpBarUpdate = -1;
	public boolean escLoc = false;
	protected final ConcurrentHashMap<Integer, List<Skill>> _gskills = new ConcurrentHashMap<Integer, List<Skill>>();
	private List<String> bypasses = null;
	private List<String> bypasses_bbs = null;
	private Pair<Integer, OnAnswerListener> _askDialog = null;
	private static final int[] _classIdprefetch = new int[] { 0, 10, 18, 25, 31, 38, 44, 49, 53 };
	private Future<?> _updateEffectIconsTask;
	private boolean _logoutStarted = false;
	public ScheduledFuture<?> _broadcastCharInfoTask;
	public Future<?> _userInfoTask;
	public boolean entering = true;
	private Future<?> _deleteTask;

	public String lockChar1 = "";
	public String lockChar2 = "";
	public Future<?> _unjailTask;

	private final Lock _storeLock = new ReentrantLock();

	private int _mountNpcId;
	private int _mountObjId;
	private int _mountLevel;
	private int _lastNpcId = -3;
	private final Map<String, String> user_variables = new ConcurrentHashMap<String, String>();
	private final Map<String, Integer> user_hero = new ConcurrentHashMap<String, Integer>();
	private final Object _subLock = new Object();
	private boolean _keyBlocked;
	private boolean _keyForced;
	private boolean _maried = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _engagerequest = false;
	private int _engageid = 0;
	private boolean _maryrequest = false;
	private boolean _maryaccepted = false;
	private List<Player> _snoopListener = new ArrayList<Player>();
	private List<Player> _snoopedPlayer = new ArrayList<Player>();
	public long lastDiceThrown = 0L;
	private boolean _charmOfCourage = false;
	private int _increasedForce = 0;
	public Future<?> _lastChargeRunnable = null;
	private int _fallZ;
	private ScheduledFuture<?> _fallTask = null;
	private boolean _isInDangerArea;
	private ResidenceType _inResidence = ResidenceType.None;
	private Location _lastClientPosition;
	private Location _lastServerPosition;
	private int _useSeed = 0;
	protected int _pvpFlag;
	private Future<?> _PvPRegTask;
	private long _lastPvpAttack;
	private TamedBeastInstance _tamedBeast = null;
	private long _lastAttackPacket = 0L;
	private long _lastMovePacket = 0L;
	private long _lastSkillPacket = 0L;
	private int _skillPackets = 0;
	private long _lastItemPacket = 0L;
	private int _itemPackets = 0;
	private long _lastClassChange = 0L;
	private long _lastValidPacket = 0L;
	private Location _groundSkillLoc;
	private int _buyListId;
	private int _movieId = 0;
	private boolean _isInMovie;
	private final int _incorrectValidateCount = 0;
	private Future<?> _mortalTask;
	private boolean isProtect;
	private ItemInstance _petControlItem = null;
	private PartyRoom _partyRoom;
	private boolean _canUseSelectedSub = false;
	public boolean noHeroAura;
	public HashMap<String, String> schemesB;
	protected ScheduledFuture<?> _useCP;
	protected ScheduledFuture<?> _useHP;
	protected ScheduledFuture<?> _useMP;
	protected Skill skillCP;
	protected Skill skillHP;
	protected Skill skillMP;
	public int percentCP = 0;
	public int percentHP = 0;
	public int percentMP = 0;
	public int percentECP = 0;
	public int percentEHP = 0;
	protected Map<Integer, Long> _lastRewPvP = Config.ALLOW_PVP_REWARD ? new ConcurrentSkipListMap<Integer, Long>() : null;
	protected Map<Integer, Long> _lastRewPK = Config.ALLOW_PK_REWARD ? new ConcurrentSkipListMap<Integer, Long>() : null;
	public int eventKills = 0;
	public boolean eventAct = false;
	public boolean client_request;
	protected long lastExit;
	public boolean inEvent;
	public boolean inTvT;
	public boolean inLH;
	public int GvG_ID;
	public int teleList = 1;
	public float teleMod = 1.0f;
	public int CK_FAIL;
	public int mobs_count = 0;
	public long online_count = 0L;
	private boolean _vip = false;
	private int _bbsMailItem = 0;
	private String _bbsMailSender = "n.a";
	private String _bbsMailTheme = "n.a";
	public static List<String> bots_names;
	public boolean isFashion = false;
	public boolean recording = false;
	public ScheduledFuture<?> stopBot;

	private PlayerTemplate _template;

	private Language _language = Config.DEFAULT_LANG;

	private int _privateStoreCurrecy = 0;

	private final IntObjectMap<OptionDataTemplate> _options = new CTreeIntObjectMap<OptionDataTemplate>();

	private Player(final int objectId, final PlayerTemplate template, final String accountName)
	{
		super(objectId, template);

		_accountName = accountName;
		_template = template;
		_baseClass = getClassId().getId();
	}

	private Player(final int objectId, final PlayerTemplate template)
	{
		this(objectId, template, null);
		getInventory().restore();
		_ai = new PlayerAI(this);
		radar = new Radar(this);
		wepex = new WeaponExpertise(this);
		armex = new ArmorExpertise(this);
		if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			setPlayerAccess(Config.gmlist.get(objectId));
		else
			setPlayerAccess(Config.gmlist.get(0));
		_macroses.restore();
	}

	public static Player create(final int classId, final int sex, final String accountName, final String name, final int hairStyle, final int hairColor, final int face)
	{
		try
		{
			if(Status.started)
				return null;
		}
		catch(Exception e)
		{
			return null;
		}
		final PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, sex != 0);
		final Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);
		player.setName(name);
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());
		if(!PlayerManager.createDb(player))
			return null;
		return player;
	}

	@Override
	protected CharacterAI initAI()
	{
		return new PlayerAI(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<Player> getRef()
	{
		return (HardReference<Player>) super.getRef();
	}

	public String getAccountName()
	{
		if(_connection == null)
			return _accountName;
		return _connection.getLogin();
	}

	public String getIP()
	{
		if(_connection == null)
			return "<not connected>";
		return _connection.getIpAddr();
	}

	public HashMap<Integer, String> getAccountChars()
	{
		return _chars;
	}

	@Override
	public final PlayerTemplate getTemplate()
	{
		return _template;
	}

	public final PlayerTemplate getBaseTemplate()
	{
		return (PlayerTemplate) super.getTemplate();
	}

	public void changeSex()
	{
		boolean male = true;
		if(getSex() == 1)
			male = false;
		_template = CharTemplateTable.getInstance().getTemplate(getClassId(), !male);
	}

	@Override
	public PlayerAI getAI()
	{
		return (PlayerAI) _ai;
	}

	@Override
	public void doCast(final Skill skill, final Creature target, final boolean forceUse)
	{
		if(isFlagEquipped())
		{
			sendActionFailed();
			return;
		}
		super.doCast(skill, target, forceUse);
	}

	@Override
	public void updateEffectIcons()
	{
		if(entering || isLogoutStarted())
			return;
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}
		if(_updateEffectIconsTask != null)
			return;
		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	public void updateEffectIconsImpl()
	{
		final Abnormal[] effects = getAbnormalList().getAllFirstEffects();
		Arrays.sort(effects, EffectsComparator.getInstance());
		PartySpelled ps = null;
		if(_party != null)
			ps = new PartySpelled(this, false);
		final MagicEffectIcons mi = new MagicEffectIcons();
		for(final Abnormal effect : effects)
			if(effect.isInUse())
			{
				if(effect.getStackType().equalsIgnoreCase("HpRecoverCast") && effect.getTimeLeft() >= effect.getDuration() - 300L)
					sendPacket(new ShortBuffStatusUpdate(effect));
				else if(!effect.getStackType().equalsIgnoreCase("HpRecoverCast"))
					effect.addIcon(mi);
				if(ps != null)
					effect.addPartySpelledIcon(ps);
			}
		sendPacket(mi);
		if(ps != null)
			_party.broadCast(ps);
		if(Config.ENABLE_OLYMPIAD && isInOlympiadMode() && isOlympiadCompStart())
		{
			final OlympiadGame olymp_game = Olympiad.getOlympiadGame(getOlympiadGameId());
			if(olymp_game != null)
			{
				final ExOlympiadSpelledInfo os = new ExOlympiadSpelledInfo();
				for(final Abnormal effect2 : effects)
					if(effect2 != null && effect2.isInUse())
						effect2.addOlympiadSpelledIcon(this, os);
				try
				{
					for(final Player spectator : olymp_game.getSpectators())
						if(spectator != null)
							spectator.sendPacket(os);
				}
				catch(Exception ex)
				{}
			}
		}
	}

	@Override
	public void sendChanges()
	{
		if(entering || isLogoutStarted())
			return;
		super.sendChanges();
	}

	@Override
	public final byte getLevel()
	{
		return _activeClass == null ? 1 : _activeClass.getLevel();
	}

	public final boolean setLevel(final int lvl)
	{
		if(_activeClass != null)
			_activeClass.setLevel((byte) lvl);
		return lvl == getLevel();
	}

	public int getSex()
	{
		return getTemplate().isMale ? 0 : 1;
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(final int face)
	{
		_face = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(final int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(final int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	@Override
	public boolean isMovementDisabled()
	{
		return isInStoreMode() || super.isMovementDisabled();
	}

	public boolean isInStoreMode()
	{
		return _privatestore != 0;
	}

	public void offline()
	{
		if(recording)
			writeBot(false);
		BuffBBSManager.storeSchemes(this);
		CursedWeaponsManager.getInstance().doLogout(this);
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(Msg.LeaveWorld);
			setNetConnection(null);
		}
		if(Config.SERVICES_ALLOW_OFFLINE_TRADE_NAME_COLOR)
			setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR, false);
		setOnlineTime(getOnlineTime());
		setUptime(0L);
		setOfflineMode(true);
		setVar("offline", String.valueOf(System.currentTimeMillis() / 1000L));
		if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L)
			startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L, false);
		if(_party != null)
		{
			if(isFestivalParticipant())
				_party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
			leaveParty();
		}
		if(getServitor() != null)
			getServitor().unSummon();
		if(Config.ENABLE_OLYMPIAD && (Olympiad.isRegistered(this) || getOlympiadGameId() > -1))
			Olympiad.unRegisterNoble(this, true);
		broadcastUserInfo(true);
		stopWaterTask();
		stopBonusTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		_resEffs = null;
		PlayerManager.saveCharToDisk(this);
	}

	public void kick(final boolean sc)
	{
		try
		{
			if(_connection != null)
			{
				_connection.close(sc ? Msg.ServerClose : Msg.LeaveWorld);
				setNetConnection(null);
			}
			deleteMe();
		}
		catch(Exception ex)
		{}
	}

	public void restart()
	{
		try
		{
			if(_connection != null)
			{
				_connection.setActiveChar(null);
				setNetConnection(null);
			}
			if(!Config.SERVICES_ENABLE_NO_CARRIER || client_request || inObserverMode())
				deleteMe();
			else
				scheduleDelete();
		}
		catch(Exception ex)
		{}
	}

	public void logout()
	{
		try
		{
			if(_connection != null)
			{
				_connection.close(Msg.LeaveWorld);
				setNetConnection(null);
			}
			if(!Config.SERVICES_ENABLE_NO_CARRIER || client_request || inObserverMode())
				deleteMe();
			else
				scheduleDelete();
		}
		catch(Exception ex)
		{}
	}

	private void prepareToLogout()
	{
		BuffBBSManager.storeSchemes(this);
		CursedWeaponsManager.getInstance().doLogout(this);

		if(recording)
			writeBot(false);

		setNetConnection(null);
		setIsOnline(false);
		getListeners().onExit();

		Object[] script_args = { this };
		for(final Scripts.ScriptClassAndMethod handler : Scripts.onPlayerExit)
			Scripts.getInstance().callScripts(this, handler.className, handler.methodName, script_args);

		if(isFlying() && !checkLandingState())
			setLoc(MapRegionTable.getTeleToClosestTown(this));

		if(isCastingNow())
			abortCast(true, false);

		DuelEvent duel = getEvent(DuelEvent.class);
		if(duel != null)
			duel.abortDuel(this);

		if(_party != null && isFestivalParticipant())
			_party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");

		if(inObserverMode()) {
			if (_olympiadObserveId == -1)
				leaveObserverMode();
			else
				leaveOlympiadObserverMode();
		}

		if(Config.ENABLE_OLYMPIAD && (Olympiad.isRegistered(this) || getOlympiadGameId() > -1))
			Olympiad.unRegisterNoble(this, true);

		stopFishing();

		if(_stablePoint != null)
		{
			teleToLocation(_stablePoint);
			addAdena(_stablePoint.h);
		}

		if(_recomChars.isEmpty())
			unsetVar("recomChars");
		else
		{
			String recomList = Integer.toHexString(_recomChars.get(0));
			for(int i = 1; i < _recomChars.size(); ++i)
				recomList = recomList + "," + Integer.toHexString(_recomChars.get(i));
			setVar("recomChars", recomList);
		}

		if(getServitor() != null)
			getServitor().unSummon();

		if(_forceBuff != null)
			_forceBuff.delete();

		if(_party != null)
		{
			for(final Player member : _party.getPartyMembers())
				if(member.getForceBuff() != null && member.getForceBuff().getTarget() == this)
					member.getForceBuff().delete();
			leaveParty();
		}

		_resEffs = null;

		if(isInTransaction())
			getTransaction().cancel();

		Clan clan = getClan();
		if(clan != null)
		{
			ClanMember clanMember = clan.getClanMember(getObjectId());
			int sponsor = clanMember.getSponsor();
			int apprentice = getApprentice();
			PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
			for(Player member : clan.getOnlineMembers(getObjectId())) {
				if (member.getObjectId() != getObjectId()) {
					member.sendPacket(memberUpdate);
					if (member.getObjectId() == sponsor)
						member.sendPacket(new SystemMessage(1757).addString(getName()));
					else if (member.getObjectId() == apprentice)
						member.sendPacket(new SystemMessage(1759).addString(getName()));
				}
			}
			clanMember.setPlayerInstance(null);
		}

		if(CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null)
			CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);

		final PartyRoom room = getPartyRoom();
		if(room != null) {
			if (room.getLeader() == this)
				room.disband();
			else
				room.removeMember(this, false);
		}

		setPartyRoom(null);
		PartyRoomManager.getInstance().removeFromWaitingList(this);

		for(final Player player : _snoopedPlayer)
			player.removeSnooper(this);

		for(final Player player : _snoopListener)
			player.removeSnooped(this);

		if(_decoy != null)
			_decoy.deleteMe();

		stopPvPFlag();
		PlayerManager.saveCharToDisk(this);
	}

	public boolean isLogoutStarted()
	{
		return _logoutStarted;
	}

	public void setLogoutStarted(final boolean logoutStarted)
	{
		_logoutStarted = logoutStarted;
	}

	public Collection<RecipeList> getDwarvenRecipeBook()
	{
		return _recipebook.values();
	}

	public Collection<RecipeList> getCommonRecipeBook()
	{
		return _commonrecipebook.values();
	}

	public int recipesCount()
	{
		return _commonrecipebook.size() + _recipebook.size();
	}

	public boolean findRecipe(final RecipeList id)
	{
		return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
	}

	public boolean findRecipe(final int id)
	{
		return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
	}

	public void registerRecipe(final RecipeList recipe, final boolean saveDB)
	{
		if(recipe.isDwarvenRecipe())
			_recipebook.put(recipe.getId(), recipe);
		else
			_commonrecipebook.put(recipe.getId(), recipe);
		if(saveDB)
			mysql.set("REPLACE INTO character_recipebook (char_id, id) values(" + getObjectId() + "," + recipe.getId() + ")");
	}

	public void unregisterRecipe(final int RecipeID)
	{
		if(_recipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=" + getObjectId() + " AND `id`=" + RecipeID + " LIMIT 1");
			_recipebook.remove(RecipeID);
		}
		else if(_commonrecipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=" + getObjectId() + " AND `id`=" + RecipeID + " LIMIT 1");
			_commonrecipebook.remove(RecipeID);
		}
		else
			_log.warn("Attempted to remove unknown RecipeList" + RecipeID);
	}

	public QuestState getQuestState(int id)
	{
		return _quests.get(id);
	}

	public boolean isQuestCompleted(int id)
	{
		final QuestState q = getQuestState(id);
		return q != null && q.isCompleted();
	}

	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuest().getId(), qs);
	}

	public void delQuestState(int id)
	{
		_quests.remove(id);
	}

	public Quest[] getAllActiveQuests()
	{
		final List<Quest> quests = new ArrayList<Quest>();
		for(final QuestState qs : _quests.valueCollection())
		{
			if(qs.isStarted())
				quests.add(qs.getQuest());
		}
		return quests.toArray(new Quest[quests.size()]);
	}

	public QuestState[] getAllQuestsStates()
	{
		return _quests.values(new QuestState[_quests.size()]);
	}

	public List<QuestState> getQuestsForEvent(final NpcInstance npc, final QuestEventType event)
	{
		final List<QuestState> states = new ArrayList<QuestState>();
		final Quest[] quests = npc.getTemplate().getEventQuests(event);
		if(quests != null)
		{
			for(final Quest quest : quests)
			{
				QuestState qs = getQuestState(quest.getId());
				if(qs != null && !qs.isCompleted())
					states.add(qs);
			}
		}
		return states;
	}

	public void processQuestEvent(int questId, String event, final NpcInstance npc)
	{
		if(event == null)
			event = "";
		QuestState qs = getQuestState(questId);
		if(qs == null)
		{
			final Quest q = QuestManager.getQuest(questId);
			if(q == null)
			{
				System.out.println("Quest ID[" + questId + "] not found!!!");
				return;
			}
			qs = q.newQuestState(this, 1);
		}

		if(qs.isCompleted())
			return;

		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestList(this));
	}

	public boolean isQuestContinuationPossible(final boolean msg)
	{
		if(getInventory().getSize() <= getInventoryLimit() * 0.8)
			return true;
		if(msg)
			sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
		return false;
	}

	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}

	public ShortCut getShortCut(final int slot, final int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}

	public void registerShortCut(final ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	public void deleteShortCut(final int slot, final int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}

	public void registerMacro(final Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	public void deleteMacro(final int id)
	{
		_macroses.deleteMacro(id);
	}

	public MacroList getMacroses()
	{
		return _macroses;
	}

	public boolean isCastleLord(final int castleId)
	{
		return _clan != null && isClanLeader() && _clan.getHasCastle() == castleId;
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public void setPkKills(final int pkKills)
	{
		_pkKills = pkKills;
	}

	public long getCreateTime()
	{
		return _createTime;
	}

	public void setCreateTime(final long createTime)
	{
		_createTime = createTime;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(final int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(final long value)
	{
		_lastAccess = value;
	}

	public int getRecomHave()
	{
		return _recomHave;
	}

	public void setRecomHave(final int value)
	{
		if(value > 255)
			_recomHave = 255;
		else if(value < 0)
			_recomHave = 0;
		else
			_recomHave = value;
	}

	public int getRecomLeft()
	{
		return _recomLeft;
	}

	public void setRecomLeft(final int value)
	{
		_recomLeft = value;
	}

	public void giveRecom(final Player target)
	{
		final int targetRecom = target.getRecomHave();
		if(targetRecom < 255)
			target.setRecomHave(targetRecom + 1);
		if(_recomLeft > 0)
			--_recomLeft;
		_recomChars.add(target.getObjectId());
	}

	public boolean canRecom(final Player target)
	{
		return !_recomChars.contains(target.getObjectId());
	}

	@Override
	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(int karma)
	{
		if(karma < 0)
			karma = 0;
		if(_karma == karma)
			return;
		_karma = karma;
		sendChanges();
		if(getServitor() != null)
			getServitor().broadcastCharInfo();
	}

	public int getMaxLoad()
	{
		final int con = getCON();
		if(con < 1)
			return (int) (31000.0f * Config.MAXLOAD_MODIFIER + getIncMaxLoad());
		if(con > 59)
			return (int) (176000.0f * Config.MAXLOAD_MODIFIER + getIncMaxLoad());
		return (int) (calcStat(Stats.MAX_LOAD, Math.pow(1.029993928, con) * 30495.627366 * Config.MAXLOAD_MODIFIER, this, null) + getIncMaxLoad());
	}

	public int getExpertisePenalty(final ItemInstance item)
	{
		if(item.getTemplate().getType2() == 0)
			return getWeaponsExpertisePenalty();
		if(item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2)
			return getArmorsExpertisePenalty();
		return 0;
	}

	public int getExpertisePenalty()
	{
		if(weaponExpertise > armorExpertise)
			return weaponExpertise;
		if(armorExpertise > weaponExpertise)
			return armorExpertise;
		if(armorExpertise > 0 && weaponExpertise > 0 && armorExpertise == weaponExpertise)
			return weaponExpertise;
		return 0;
	}

	public int getWeaponsExpertisePenalty()
	{
		return weaponExpertise;
	}

	public int getArmorsExpertisePenalty()
	{
		return armorExpertise;
	}

	public void refreshExpertisePenalty()
	{
		if(Config.DISABLE_EXPERTISE_PENALTY || isLogoutStarted())
			return;
		int level;
		int i;
		for(level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null), i = 0, i = 0; i < EXPERTISE_LEVELS.length && level >= EXPERTISE_LEVELS[i + 1]; ++i)
		{}
		boolean skillUpdate = false;
		if(expertiseIndex != i)
		{
			expertiseIndex = i;
			if(expertiseIndex > 0)
			{
				addSkill(SkillTable.getInstance().getInfo(239, expertiseIndex), false);
				skillUpdate = true;
			}
		}
		int newWeaponPenalty = 0;
		int newArmorPenalty = 0;
		final ItemInstance[] paperdollItems;
		final ItemInstance[] items = paperdollItems = getInventory().getPaperdollItems();
		for(final ItemInstance item : paperdollItems)
			if(item != null)
			{
				final int crystaltype = item.getTemplate().getItemGrade().ordinal();
				if(item.getTemplate().getType2() == 0)
				{
					if(crystaltype > newWeaponPenalty)
						newWeaponPenalty = crystaltype;
				}
				else if(crystaltype > newArmorPenalty && (item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2))
					newArmorPenalty = crystaltype;
			}
		newWeaponPenalty -= expertiseIndex;
		if(newWeaponPenalty <= 0)
			newWeaponPenalty = 0;
		else if(newWeaponPenalty >= 4)
			newWeaponPenalty = 4;
		newArmorPenalty -= expertiseIndex;
		if(newArmorPenalty <= 0)
			newArmorPenalty = 0;
		else if(newArmorPenalty >= 4)
			newArmorPenalty = 4;
		if(weaponExpertise != newWeaponPenalty)
		{
			if((weaponExpertise = newWeaponPenalty) > 0)
			{
				getWepEx().remove();
				getWepEx().add(weaponExpertise);
			}
			else
				getWepEx().remove();
			skillUpdate = true;
		}
		if(armorExpertise != newArmorPenalty)
		{
			if((armorExpertise = newArmorPenalty) > 0)
			{
				getArmEx().remove();
				getArmEx().add(armorExpertise);
			}
			else
				getArmEx().remove();
			skillUpdate = true;
		}
		if(armorExpertise > 0 || weaponExpertise > 0)
			super.addSkill(SkillTable.getInstance().getInfo(4267, 1));
		else
			super.removeSkill(getKnownSkill(4267));
		if(skillUpdate)
		{
			sendPacket(new SkillList(this));
			sendEtcStatusUpdate();
		}
	}

	public WeaponExpertise getWepEx()
	{
		return wepex;
	}

	public ArmorExpertise getArmEx()
	{
		return armex;
	}

	public void refreshOverloaded()
	{
		if(isLogoutStarted())
			return;
		final int maxLoad = getMaxLoad();
		if(maxLoad <= 0)
			return;
		int inventoryLimit = getInventoryLimit();
		if(inventoryLimit <= 0)
			inventoryLimit = 1;
		_usedInventoryPercents = (int) (100.0 * getInventory().getSize() / inventoryLimit);
		int weightPercents = (int) (100.0 * getCurrentLoad() / maxLoad);
		if(weightPercents < 0)
			weightPercents = 0;
		_weightPercents = weightPercents;
		setOverloaded(weightPercents > 100);
		int newWeightPenalty = 0;
		if(weightPercents < 50)
			newWeightPenalty = 0;
		else if(weightPercents < 66.6)
			newWeightPenalty = 1;
		else if(weightPercents < 80)
			newWeightPenalty = 2;
		else if(weightPercents < 100)
			newWeightPenalty = 3;
		else
			newWeightPenalty = 4;
		if(_curWeightPenalty == newWeightPenalty)
			return;
		_curWeightPenalty = newWeightPenalty;
		if(_curWeightPenalty > 0)
			super.addSkill(SkillTable.getInstance().getInfo(4270, _curWeightPenalty));
		else
			super.removeSkill(getKnownSkill(4270));
		sendEtcStatusUpdate();
	}

	public int getUsedInventoryPercents()
	{
		return _usedInventoryPercents;
	}

	public int getWeightPercents()
	{
		return _weightPercents;
	}

	public int getWeightPenalty()
	{
		return _curWeightPenalty;
	}

	public int getPvpKills()
	{
		return _pvpKills;
	}

	public void setPvpKills(final int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	public ClassId getClassId()
	{
		return getTemplate().classId;
	}

	public void addClanPointsOnProfession(final int id)
	{
		if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5)
		{
			final int jobLvl = ClassId.values()[id].getLevel();
			if(jobLvl == 2)
				_clan.incReputation((int) (100.0 * Config.RATE_CLAN_POINTS_ACADEMY1), true, "Academy");
			else if(jobLvl > 2 && getLevel() >= Config.ACADEMY_LEAVE_LVL)
				leaveAcademy();
		}
	}

	private void leaveAcademy()
	{
		int earnedPoints = 0;
		if(getLvlJoinedAcademy() <= 16)
			earnedPoints = 400;
		else if(getLvlJoinedAcademy() >= 39)
			earnedPoints = 170;
		else
			earnedPoints = 400 - (getLvlJoinedAcademy() - 16) * 10;
		earnedPoints *= (int) Config.RATE_CLAN_POINTS_ACADEMY2;
		_clan.removeClanMember(getObjectId());
		final SystemMessage sm = new SystemMessage(1748);
		sm.addString(getName());
		sm.addNumber(Integer.valueOf(_clan.incReputation(earnedPoints, true, "Academy")));
		_clan.broadcastToOnlineMembers(sm);
		_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDelete(getName()), this);
		setLvlJoinedAcademy(0);
		setClan(null);
		setTitle("");
		sendPacket(Msg.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
		setLeaveClanTime(0L);
		broadcastRelationChanged();
		sendPacket(new PledgeShowMemberListDeleteAll());
		final ItemInstance academyCirclet = ItemTable.getInstance().createItem(8181);
		getInventory().addItem(academyCirclet);
		sendPacket(new SystemMessage(53).addString("Academy Reward").addNumber(Integer.valueOf(1)));
	}

	public synchronized void setClassId(final int id, final boolean noban, final boolean fromQuest)
	{
		if(!noban && !ClassId.values()[id].equalsOrChildOf(ClassId.values()[getActiveClassId()]) && !getPlayerAccess().CanChangeClass && !Config.EVERYBODY_HAS_ADMIN_RIGHTS && (!Config.EVER_BASE_CLASS || isSubClassActive()))
		{
			Util.handleIllegalPlayerAction(this, "Player[1535] tried to change class " + getActiveClassId() + " to " + id, 1);
			return;
		}
		final boolean newClass = !getSubClasses().containsKey(id);
		if(newClass)
		{
			final SubClass cclass = getActiveClass();
			getSubClasses().remove(getActiveClassId());
			changeClassInDb(cclass.getClassId(), id);
			if(cclass.isBase())
			{
				setBaseClass(id);
				addClanPointsOnProfession(id);
				ItemInstance coupons = null;
				if(ClassId.values()[id].getLevel() == 2)
				{
					if((Config.COUPONS_COUNT > 0 || fromQuest) && Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemTable.getInstance().createItem(8869);
					unsetVar("p1q2");
					unsetVar("p1q3");
					unsetVar("p1q4");
					unsetVar("prof1");
				}
				else if(ClassId.values()[id].getLevel() == 3)
				{
					if((Config.COUPONS_COUNT > 0 || fromQuest) && Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemTable.getInstance().createItem(8870);
					unsetVar("dd1");
					unsetVar("dd2");
					unsetVar("dd3");
					unsetVar("prof2.1");
					unsetVar("prof2.2");
					unsetVar("prof2.3");
				}
				if(coupons != null)
				{
					coupons.setCount(Config.COUPONS_COUNT);
					getInventory().addItem(coupons);
					sendPacket(SystemMessage.obtainItems(coupons));
				}
			}
			cclass.setClassId(id);
			getSubClasses().put(id, cclass);
			storeCharSubClasses();
			if(Config.EVER_NOBL && cclass.isBase() && ClassId.values()[id].getLevel() == 4)
				setNoble();
			if(fromQuest || Config.GET_CLASS_SOC)
			{
				broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0L));
				sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
			}
			broadcastUserInfo(true);
		}

		final PlayerTemplate t = CharTemplateTable.getInstance().getTemplate(id, getSex() == 1);
		if(t == null)
		{
			_log.error("Missing template for classId: " + id);
			return;
		}

		_template = t;

		if(newClass)
		{
			rewardSkills();
			getInventory().checkAllConditions();
		}
		if(Config.BS_MOD && isVisible())
			Functions.callScripts("services.BonusStats", "sub", new Object[] { this });
		if(isInParty())
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		if(getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		if(_partyRoom != null)
			_partyRoom.broadcastPlayerUpdate(this);
		if(recording && newClass)
		{
			final NpcInstance n = getLastNpc();
			recBot(7, 0, id, fromQuest ? 1 : 0, n != null ? n.getNpcId() : 0, n != null ? n.getX() : 0, n != null ? n.getY() : 0);
		}
	}

	public long getExp()
	{
		return _activeClass == null ? 0L : _activeClass.getExp();
	}

	public void addExp(final long val)
	{
		if(_activeClass != null)
			_activeClass.addExp(val);
	}

	public void setEnchantScroll(final ItemInstance scroll)
	{
		_enchantScroll = scroll;
	}

	public ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}

	public void setFistsWeaponItem(final WeaponTemplate weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	public WeaponTemplate getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	public WeaponTemplate findFistsWeaponItem(final int classId)
	{
		if(classId >= 0 && classId <= 9)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(246);
		if(classId >= 10 && classId <= 17)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(251);
		if(classId >= 18 && classId <= 24)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(244);
		if(classId >= 25 && classId <= 30)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(249);
		if(classId >= 31 && classId <= 37)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(245);
		if(classId >= 38 && classId <= 43)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(250);
		if(classId >= 44 && classId <= 48)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(248);
		if(classId >= 49 && classId <= 52)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(252);
		if(classId >= 53 && classId <= 57)
			return (WeaponTemplate) ItemTable.getInstance().getTemplate(247);
		return null;
	}

	@Override
	public void addExpAndSp(final long addToExp, final long addToSp)
	{
		addExpAndSp(addToExp, addToSp, true, true);
	}

	public void addExpAndSp(long addToExp, long addToSp, final boolean applyBonus, final boolean appyToPet)
	{
		if(applyBonus)
		{
			addToExp *= (long) (Config.RATE_XP * getRateExp());
			addToSp *= (long) (Config.RATE_SP * getRateSp());
		}
		if(addToExp > 0L)
		{
			if(appyToPet && getServitor() != null && !getServitor().isDead())
				if(getServitor().getNpcId() == 12564)
				{
					getServitor().addExpAndSp(addToExp, 0L);
					addToExp = 0L;
				}
				else if(getServitor().isPet())
				{
					getServitor().addExpAndSp((long) (addToExp * 0.1), 0L);
					addToExp *= (long) 0.9;
				}
			if(!isDead() && !isCursedWeaponEquipped() && addToExp > 0L && _karma > 0)
			{
				long expGained = Math.abs(addToExp);
				expGained /= Config.KARMA_XP_DIVIDER;
				final int karmaLost = (int) Math.min(Math.max(Math.min(expGained, Integer.MAX_VALUE), 0L), _karma);
				if(karmaLost > 0)
					_karma -= karmaLost;
			}
		}
		final long max_xp = Experience.LEVEL[81];
		addToExp = Math.min(addToExp, max_xp - getExp());
		if(Config.CLASS_EXP && addToExp < 0L)
		{
			final int jobLvl = getClassId().getLevel();
			if(jobLvl > 1)
				addToExp = Math.max(addToExp, -(getExp() - Experience.LEVEL[jobLvl == 4 ? 76 : jobLvl == 3 ? 40 : 20]));
		}
		addExp(addToExp);
		addSp(addToSp);
		if(addToSp > 0L && addToExp <= 0L)
			sendPacket(new SystemMessage(331).addNumber(Integer.valueOf((int) addToSp)));
		else if(addToSp > 0L || addToExp > 0L)
			sendPacket(new SystemMessage(95).addNumber(Integer.valueOf((int) addToExp)).addNumber(Integer.valueOf((int) addToSp)));
		int level;
		int old_level;
		for(old_level = level = getLevel(); getExp() >= Experience.LEVEL[level + 1] && level < 80; ++level)
		{}
		while(getExp() < Experience.LEVEL[level] && level > 1)
			--level;
		setLevel(level);
		if(old_level > level)
		{
			decreaseLevel(false);
			if(recording)
				recBot(8, level, 0, 0, 0, 0, 0);
		}
		else if(old_level < level)
		{
			sendPacket(Msg.YOU_HAVE_INCREASED_YOUR_LEVEL);
			if(!entering)
				broadcastPacket(new SocialAction(getObjectId(), 15));
			if(Config.ACADEMY_LEAVE_LVL > 40 && getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && level >= Config.ACADEMY_LEAVE_LVL && getClassId().getLevel() > 2)
				leaveAcademy();
			setCurrentHpMp(getMaxHp(), getMaxMp(), false);
			setCurrentCp(getMaxCp());
			if(isInParty())
				getParty().recalculatePartyData();
			if(_clan != null)
			{
				final PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
				for(final Player clanMember : _clan.getOnlineMembers(0))
					clanMember.sendPacket(memberUpdate);
			}
			if(_partyRoom != null)
				_partyRoom.broadcastPlayerUpdate(this);
			rewardSkills();
			final Quest q = QuestManager.getQuest(255);
			if(q != null)
				processQuestEvent(q.getId(), "CE40", null);
			classWindow();
			if(recording)
				recBot(8, level, 0, 0, 0, 0, 0);
			if(level == 80 && getClassId().getLevel() == 4 && !ServerVariables.getBool("class"+getClassId().ordinal(), false))
			{
				ServerVariables.set("class"+getClassId().ordinal(), true);
				setTitleColor(Integer.decode("0x0000FF"));
				broadcastUserInfo(true);
			}
		}
		updateStats();
	}

	public void rewardSkills()
	{
		boolean update = false;
		if(Config.AUTO_LEARN_SKILLS)
		{
			int unLearnable = 0;
			for(SkillLearn[] skills = SkillTree.getInstance().getAvailableSkills(this, getClassId()); skills.length > unLearnable; skills = SkillTree.getInstance().getAvailableSkills(this, getClassId()))
			{
				unLearnable = 0;
				for(final SkillLearn s : skills)
				{
					final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if(sk == null || !sk.getCanLearn(getClassId()))
						++unLearnable;
					else
					{
						addSkill(sk, true);
						if(getAllShortCuts().size() > 0 && sk.getLevel() > 1)
						{
							for(final ShortCut sc : getAllShortCuts())
							{
								if(sc.getId() == sk.getId() && sc.getType() == 2)
								{
									final ShortCut newsc = new ShortCut(sc.slot, sc.page, sc.type, sc.id, sk.getLevel());
									sendPacket(new ShortCutRegister(newsc));
									registerShortCut(newsc);
								}
							}
						}
					}
				}
			}
			update = true;
		}
		else
		{
			final SkillLearn[] availableSkills;
			final SkillLearn[] skills2 = availableSkills = SkillTree.getInstance().getAvailableSkills(this, getClassId());
			for(final SkillLearn s2 : availableSkills)
				if(s2.getSpCost() == 0 && s2.getId() != 1405)
				{
					final Skill sk2 = SkillTable.getInstance().getInfo(s2.getId(), s2.getLevel());
					addSkill(sk2, true);
					if(getAllShortCuts().size() > 0 && sk2.getLevel() > 1)
					{
						for(final ShortCut sc2 : getAllShortCuts())
						{
							if(sc2.getId() == sk2.getId() && sc2.getType() == 2)
							{
								final ShortCut newsc2 = new ShortCut(sc2.slot, sc2.page, sc2.type, sc2.id, sk2.getLevel());
								sendPacket(new ShortCutRegister(newsc2));
								registerShortCut(newsc2);
							}
						}
					}
					update = true;
				}
		}
		if(update)
			sendPacket(new SkillList(this));
		updateStats();
	}

	public Race getRace()
	{
		return getBaseTemplate().race;
	}

	public int getSp()
	{
		return _activeClass == null ? 0 : _activeClass.getSp();
	}

	public void setSp(final int sp)
	{
		if(_activeClass != null)
			_activeClass.setSp(sp);
	}

	public void addSp(final long val)
	{
		if(_activeClass != null)
			_activeClass.addSp(val);
	}

	@Override
	public int getClanId()
	{
		return _clan == null ? 0 : _clan.getClanId();
	}

	@Override
	public int getClanCrestId()
	{
		return _clan == null ? 0 : _clan.getCrestId();
	}

	@Override
	public int getClanCrestLargeId()
	{
		return _clan == null ? 0 : _clan.getCrestLargeId();
	}

	public long getLeaveClanTime()
	{
		return _leaveClanTime;
	}

	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}

	public void setLeaveClanTime(final long time)
	{
		_leaveClanTime = time;
	}

	public void setDeleteClanTime(final long time)
	{
		_deleteClanTime = time;
	}

	public void setOnlineTime(final long time)
	{
		_onlineTime = time;
	}

	public void setNoChannel(final long time)
	{
		_NoChannel = time;
		if(_NoChannel > 2145909600000L || _NoChannel < 0L)
			_NoChannel = -1L;
		if(_NoChannel > 0L)
			_NoChannelBegin = System.currentTimeMillis();
		else
			_NoChannelBegin = 0L;
		sendEtcStatusUpdate();
	}

	public long getNoChannel()
	{
		return _NoChannel;
	}

	public long getNoChannelRemained()
	{
		if(_NoChannel == 0L)
			return 0L;
		if(_NoChannel < 0L)
			return -1L;
		final long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
		if(remained < 0L)
			return 0L;
		return remained;
	}

	public void setLeaveClanCurTime()
	{
		_leaveClanTime = System.currentTimeMillis();
	}

	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}

	public boolean canJoinClan()
	{
		if(_leaveClanTime == 0L)
			return true;
		if(System.currentTimeMillis() - _leaveClanTime >= Config.HoursBeforeJoinAClan * 3600000L)
		{
			_leaveClanTime = 0L;
			return true;
		}
		return false;
	}

	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0L)
			return true;
		if(System.currentTimeMillis() - _deleteClanTime >= Config.HoursBeforeCreateClan * 3600000L)
		{
			_deleteClanTime = 0L;
			return true;
		}
		return false;
	}

	public SystemMessage canJoinParty(final Player inviter)
	{
		final Transaction transaction = getTransaction();
		if(transaction != null && transaction.isInProgress() && transaction.getOtherPlayer(this) != inviter)
			return Msg.WAITING_FOR_ANOTHER_REPLY;
		if(isBlockAll() || getMessageRefusal())
			return Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE;
		if(isInParty())
			return new SystemMessage(160).addString(getName());
		if(isCursedWeaponEquipped() || inviter.isCursedWeaponEquipped())
			return Msg.INCORRECT_TARGET;
		if(inviter.isInOlympiadMode() || isInOlympiadMode())
			return Msg.INCORRECT_TARGET;
		if(!inviter.getPlayerAccess().CanJoinParty || !getPlayerAccess().CanJoinParty)
			return Msg.INCORRECT_TARGET;
		if(isInDuel())
			return Msg.INCORRECT_TARGET;
		return null;
	}

	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}

	public void removeItemFromShortCut(final int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}

	public void removeSkillFromShortCut(final int skillId)
	{
		_shortCuts.deleteShortCutBySkillId(skillId);
	}

	@Override
	public boolean isSitting()
	{
		return _isSitting;
	}

	public void setSitting(final boolean val)
	{
		_isSitting = val;
	}

	public boolean getSittingTask()
	{
		return sittingTaskLaunched;
	}

	@Override
	public void sitDown(final int throne)
	{
		if(isSitting() || sittingTaskLaunched || isAlikeDead())
			return;
		if(isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isMoving)
		{
			getAI().setNextAction(PlayableAI.nextAction.REST, null, null, false, false);
			return;
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		if(throne == 0)
			broadcastPacket(new ChangeWaitType(this, 0));
		else
			broadcastPacket(new CharSit(this, throne));
		_sittingObject = throne;
		setSitting(true);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndSitDownTask(this), 2500L);
	}

	@Override
	public void standUp()
	{
		if(!isSitting() || sittingTaskLaunched || isInStoreMode() || isAlikeDead())
			return;
		getAbnormalList().stopAll(EffectType.Relax);
		getAI().clearNextAction();
		broadcastPacket(new ChangeWaitType(this, 1));
		_sittingObject = 0;
		setSitting(false);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndStandUpTask(this), 2500L);
	}

	public Warehouse getWarehouse()
	{
		return _warehouse;
	}

	public Warehouse getFreight()
	{
		return _freight;
	}

	public int getCharId()
	{
		return _charId;
	}

	public void setCharId(final int charId)
	{
		_charId = charId;
	}

	public int getAdena()
	{
		return _inventory.getAdena();
	}

	public ItemInstance reduceAdena(final long adena, final boolean notify)
	{
		if(notify && adena > 0L)
			sendPacket(new SystemMessage(672).addNumber(Long.valueOf(adena)));
		return _inventory.reduceAdena(adena);
	}

	public ItemInstance addAdena(final long adena)
	{
		return _inventory.addAdena(adena);
	}

	public GameClient getNetConnection()
	{
		return _connection;
	}

	public void setNetConnection(final GameClient connection)
	{
		_connection = connection;
	}

	public boolean isConnected()
	{
		return _connection != null && _connection.isConnected();
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() != this)
				player.sendActionFailed();
		}
		else if(Events.onAction(player, this, shift))
			player.sendActionFailed();
		else if(getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			if(getDistance(player) > 150.0 && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				else
					player.sendActionFailed();
			}
			else
			{
				player.doInteract(this);
				if(isInRange(player, 150L))
					player.turn(this, 3000);
			}
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else if(player != this)
		{
			if(isInRange(player, 150L))
				player.turn(this, 3000);
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, 80);
				else
					player.sendActionFailed();
			}
			else
				player.sendActionFailed();
		}
		else
			player.sendActionFailed();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!isVisible())
			return;
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(33, (int) getCurrentCp());
		su.addAttribute(9, (int) getCurrentHp());
		su.addAttribute(11, (int) getCurrentMp());
		sendPacket(su);
		final boolean needCpUpdate = needCpUpdate();
		final boolean needHpUpdate = needHpUpdate();
		if(isInParty() && (needCpUpdate || needHpUpdate || needMpUpdate()))
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		final DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && (needCpUpdate || needHpUpdate))
			duelEvent.sendPacket(new ExDuelUpdateUserInfo(this), getTeam() == 1 ? "RED" : "BLUE");
		if(isInOlympiadMode() && isOlympiadCompStart() && (needCpUpdate || needHpUpdate))
		{
			final OlympiadGame game = Olympiad.getOlympiadGame(getOlympiadGameId());
			if(game != null)
				game.broadcastInfo(true, true, false);
		}
	}

	private boolean needCpUpdate()
	{
		final int bar = (int) (getCurrentCp() * 352.0 / getMaxCp());
		if(bar == 0 || bar != _lastCpBarUpdate)
		{
			_lastCpBarUpdate = bar;
			return true;
		}
		return false;
	}

	@Override
	public void broadcastUserInfo(final boolean force)
	{
		sendUserInfo(force);
		if(!isVisible() || isInvisible())
			return;
		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0 || force)
		{
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl();
			return;
		}
		if(_broadcastCharInfoTask != null)
			return;
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.BroadcastCharInfoTask(this), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	protected void broadcastCharInfoImpl()
	{
		if(!isVisible() || isInvisible())
			return;

		for(final Player player : World.getAroundPlayers(this))
		{
			if(player != null && _objectId != player.getObjectId())
			{
				player.sendPacket(new CharInfo(this, player));
				player.sendPacket(RelationChanged.update(player, this, player));
			}
		}
	}

	public final void broadcastTitleInfo()
	{
		sendPacket(new UserInfo(this));
		broadcastPacket(new TitleUpdate(this));
	}

	public void broadcastRelationChanged()
	{
		if(isInvisible() || isInOfflineMode())
			return;

		for(final Player player : World.getAroundPlayers(this))
		{
			if(player != null && _objectId != player.getObjectId())
			{
				player.sendPacket(new CharInfo(this, player));
				player.sendPacket(RelationChanged.update(player, this, player));
			}
		}
	}

	public void sendEtcStatusUpdate()
	{
		if(!isVisible())
			return;
		sendPacket(new EtcStatusUpdate(this));
	}

	public void sendUserInfo(final boolean force)
	{
		if(!isVisible() || entering || isLogoutStarted())
			return;
		if(Config.USER_INFO_INTERVAL == 0 || force)
		{
			if(_userInfoTask != null)
			{
				_userInfoTask.cancel(false);
				_userInfoTask = null;
			}
			sendPacket(new UserInfo(this));
			return;
		}
		if(_userInfoTask != null)
			return;
		_userInfoTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.UserInfoTask(this), Config.USER_INFO_INTERVAL);
	}

	@Override
	public StatusUpdate makeStatusUpdate(final int... fields)
	{
		final StatusUpdate su = new StatusUpdate(getObjectId());
		for(final int field : fields)
			switch(field)
			{
				case 14:
				{
					su.addAttribute(field, getCurrentLoad());
					break;
				}
				case 15:
				{
					su.addAttribute(field, getMaxLoad());
					break;
				}
				case 26:
				{
					su.addAttribute(field, _pvpFlag);
					break;
				}
				case 27:
				{
					su.addAttribute(field, getKarma());
					break;
				}
			}
		return su;
	}

	public void sendStatusUpdate(final boolean broadCast, final boolean withPet, final int... fields)
	{
		if(fields.length == 0 || entering && !broadCast)
			return;
		final StatusUpdate su = makeStatusUpdate(fields);
		if(!su.hasAttributes())
			return;
		final List<L2GameServerPacket> packets = new ArrayList<L2GameServerPacket>(withPet ? 2 : 1);
		if(withPet && getServitor() != null)
			packets.add(getServitor().makeStatusUpdate(fields));
		packets.add(su);
		if(!broadCast)
			sendPacket(packets);
		else if(entering)
			broadcastPacketToOthers(packets);
		else
			broadcastPacket(packets);
	}

	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public int getAllyCrestId()
	{
		return getAlliance() == null ? 0 : getAlliance().getAllyCrestId();
	}

	@Override
	public void sendPacket(final IBroadcastPacket p)
	{
		if(!isConnected())
			return;
		if(p.packet(this) != null)
			_connection.sendPacket(p.packet(this));
	}

	@Override
	public void sendPacket(final IBroadcastPacket... packets)
	{
		if(!isConnected())
			return;
		for(final IBroadcastPacket p : packets)
			if(p != null)
				_connection.sendPacket(p.packet(this));
	}

	@Override
	public void sendPacket(final List<? extends IBroadcastPacket> packets)
	{
		if(!isConnected() || packets == null || packets.isEmpty())
			return;
		for(final IBroadcastPacket p : packets)
			if(p != null)
				_connection.sendPacket(p.packet(this));
	}

	public void doInteract(final GameObject target)
	{
		if(target == null || isActionsDisabled())
		{
			sendActionFailed();
			return;
		}
		if(target.isPlayer())
		{
			if(target.getDistance(this) <= 150.0)
			{
				final Player temp = (Player) target;
				if(temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				{
					sendPacket(new PrivateStoreListSell(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				{
					sendPacket(new PrivateStoreListBuy(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				{
					sendPacket(new RecipeShopSellList(this, temp));
					sendActionFailed();
				}
				sendActionFailed();
			}
			else if(getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
		}
		else
			target.onAction(this, false);
	}

	public void doAutoLootOrDrop(final ItemInstance item, final NpcInstance fromNpc)
	{
		if(fromNpc.isRaid() && !Config.AUTO_LOOT_FROM_RAIDS && !item.isHerb())
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		if(ArrayUtils.contains(Config.DROP_ITEMS_GR, item.getItemId()))
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		if(!AutoLootList && ArrayUtils.contains(Config.AUTO_LOOT_LIST, item.getItemId()))
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		if(item.isHerb())
		{
			if(fromNpc.isChampion() && !Config.CHAMPION_DROP_HERBS)
			{
				item.deleteMe();
				return;
			}
			if(!AutoLootHerbs)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			final Skill[] skills = item.getTemplate().getAttachedSkills();
			if(skills != null && skills.length > 0)
				for(final Skill skill : skills)
				{
					altUseSkill(skill, this);
					if(getServitor() != null && getServitor().isSummon() && !getServitor().isDead() && (item.getItemId() <= 8605 || item.getItemId() == 8614))
						getServitor().altUseSkill(skill, getServitor());
				}
			item.deleteMe();
			broadcastPacket(new GetItem(item, getObjectId()));
		}
		else
		{
			if(!AutoLootAdena && item.getItemId() == 57)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			if(!AutoLootItems && item.getItemId() != 57 && !item.isHerb() && !ArrayUtils.contains(Config.AUTO_LOOT_LIST, item.getItemId()))
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			if(!isInParty())
			{
				if(!getInventory().validateWeight(item))
				{
					sendActionFailed();
					sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
					item.dropToTheGround(this, fromNpc);
					return;
				}
				if(!getInventory().validateCapacity(item))
				{
					sendActionFailed();
					sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
					item.dropToTheGround(this, fromNpc);
					return;
				}
				Log.LogItem(this, "Pickup", item);
				sendPacket(SystemMessage.obtainItems(item));
				getInventory().addItem(item);
				sendChanges();
			}
			else if(item.getItemId() == 57)
				getParty().distributeAdena(item, fromNpc, this);
			else
				getParty().distributeItem(this, item, fromNpc);
			broadcastPickUpMsg(item);
		}
	}

	@Override
	public void doPickupItem(final GameObject object)
	{
		if(!object.isItem())
		{
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}
		if(isInTrade())
		{
			sendPacket(new SystemMessage(149));
			return;
		}
		sendActionFailed();
		stopMove();
		final ItemInstance item = (ItemInstance) object;
		synchronized (item)
		{
			if(item.getDropTimeOwner() > 0L && item.getItemDropOwner() != null && item.getDropTimeOwner() > System.currentTimeMillis() && this != item.getItemDropOwner() && (!isInParty() || isInParty() && item.getItemDropOwner().isInParty() && getParty() != item.getItemDropOwner().getParty()))
			{
				SystemMessage sm;
				if(item.getItemId() == 57)
				{
					sm = new SystemMessage(55);
					sm.addNumber(Long.valueOf(item.getCount()));
				}
				else
				{
					sm = new SystemMessage(56);
					sm.addItemName(Integer.valueOf(item.getItemId()));
				}
				sendPacket(sm);
				return;
			}
			if(!item.isVisible())
				return;
			if(item.isHerb())
			{
				final Skill[] skills = item.getTemplate().getAttachedSkills();
				if(skills != null && skills.length > 0)
					for(final Skill skill : skills)
					{
						altUseSkill(skill, this);
						if(getServitor() != null && getServitor().isSummon() && !getServitor().isDead() && (item.getItemId() <= 8605 || item.getItemId() == 8614))
							getServitor().altUseSkill(skill, getServitor());
					}
				item.deleteMe();
				broadcastPacket(new GetItem(item, getObjectId()));
				return;
			}
			if(!isInParty() || item.isCursed())
			{
				if(!getInventory().validateWeight(item))
				{
					sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
					return;
				}
				if(!getInventory().validateCapacity(item))
				{
					sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
					return;
				}
				item.pickupMe(this);
				Log.LogItem(this, "Pickup", item);
				sendPacket(SystemMessage.obtainItems(item));
				getInventory().addItem(item);
				sendChanges();
			}
			else if(item.getItemId() == 57)
			{
				item.pickupMe(this);
				getParty().distributeAdena(item, null, this);
			}
			else
			{
				item.pickupMe(null);
				getParty().distributeItem(this, item);
			}
			broadcastPacket(new GetItem(item, getObjectId()));
			broadcastPickUpMsg(item);
		}
	}

	public boolean itemLimM(final MultiSellIngredient item, final long amount)
	{
		final long count = item.getItemCount() * amount;
		if(!item.isStackable() && getInventoryLimit() - getInventory().getSize() < count)
			return true;
		if(count > Integer.MAX_VALUE)
			return true;
		final ItemInstance ii = getInventory().getItemByItemId(item.getItemId());
		return ii != null && ii.getCount() + count > Integer.MAX_VALUE;
	}

	@Override
	public void setTarget(GameObject newTarget)
	{
		if(newTarget != null && !newTarget.isVisible())
			newTarget = null;
		final Party party = getParty();
		if(party != null && party.isInDimensionalRift())
		{
			final int riftType = party.getDimensionalRift().getType();
			final int riftRoom = party.getDimensionalRift().getCurrentRoom();
			if(newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getLoc()))
				newTarget = null;
		}
		final GameObject oldTarget = getTarget();
		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
			{
				if(newTarget != null && newTarget.isCreature() && newTarget.getObjectId() != getObjectId())
					sendPacket(new ValidateLocation((Creature) newTarget));
				return;
			}
			if(oldTarget.isCreature())
				((Creature) oldTarget).removeStatusListener(this);
			broadcastPacket(new TargetUnselected(this));
		}
		if(newTarget != null)
		{
			if(newTarget.isCreature())
			{
				final Creature target = (Creature) newTarget;
				if(newTarget.getObjectId() != getObjectId())
					sendPacket(new ValidateLocation(target));
				sendPacket(new MyTargetSelected(this, target));
				target.addStatusListener(this);
			}
			broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getLoc()));
		}
		super.setTarget(newTarget);
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(7);
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return getFistsWeaponItem();
		return (WeaponTemplate) weapon.getTemplate();
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(8);
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		final ItemInstance weapon = getSecondaryWeaponInstance();
		if(weapon == null)
			return getFistsWeaponItem();
		final ItemTemplate item = weapon.getTemplate();
		if(item instanceof WeaponTemplate)
			return (WeaponTemplate) item;
		return null;
	}

	public boolean isWearingArmor(final ArmorTemplate.ArmorType armorType)
	{
		final ItemInstance chest = getInventory().getPaperdollItem(10);
		if(chest == null)
			return armorType == ArmorTemplate.ArmorType.NONE;
		if(chest.getItemType() != armorType)
			return false;
		if(chest.getBodyPart() == 32768)
			return true;
		final ItemInstance legs = getInventory().getPaperdollItem(11);
		return legs == null ? armorType == ArmorTemplate.ArmorType.NONE : legs.getItemType() == armorType;
	}

	@Override
	public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final int poleHitCount, final boolean crit, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage)
	{
		if(attacker == null || isDead() || attacker.isDead() && !isDot)
			return;
		if(attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			if(attacker.getKarma() > 0 && getAbnormalList().getEffectsBySkillId(5182) != null && !isInZone(Zone.ZoneType.Siege))
				return;
			if(getKarma() > 0 && attacker.getAbnormalList().getEffectsBySkillId(5182) != null && !attacker.isInZone(Zone.ZoneType.Siege))
				return;
		}
		super.reduceCurrentHp(damage, attacker, skill, poleHitCount, crit, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	protected void onReduceCurrentHp(double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp)
	{
		if(standUp)
		{
			standUp();
			if(isFakeDeath() && attacker instanceof Playable)
				breakFakeDeath();
		}
		final int i = (int) damage;
		if(attacker.isPlayable() && !directHp && getCurrentCp() > 0.0)
		{
			double cp = getCurrentCp();
			if(cp >= damage)
			{
				cp -= damage;
				damage = 0.0;
			}
			else
			{
				damage -= cp;
				cp = 0.0;
			}
			setCurrentCp(cp);
		}
		final double hp = getCurrentHp();
		final DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && hp <= damage)
		{
			setCurrentHp(1.0, true);
			duelEvent.onDie(this);
			return;
		}
		if(isInOlympiadMode())
		{
			final OlympiadGame game = Olympiad.getOlympiadGame(getOlympiadGameId());
			if(game != null)
			{
				if(hp <= damage)
				{
					setCurrentHp(1.0, true);
					game.setWinner(getOlympiadSide());
					game.endGame(Config.OLY_RETURN_TIME * 1000, 0);
					attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					attacker.sendActionFailed();
					return;
				}
				if(this != attacker)
					game.addDamage(getOlympiadSide(), i);
			}
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
		if(getLevel() < 6 && getCurrentHpPercents() < 25.0)
		{
			final Quest q = QuestManager.getQuest(255);
			if(q != null)
				processQuestEvent(q.getId(), "CE45", null);
		}
	}

	private void altDeathPenalty(final Creature killer)
	{
		if(!Config.ALT_GAME_DELEVEL)
			return;
		if(isInZoneBattle() && killer.isPlayable())
			return;
		if(killer.isPlayable())
			deathPenalty(atWarWith(killer.getPlayer()));
		else
			deathPenalty(false);
	}

	public final boolean atWarWith(final Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId());
	}

	public boolean atMutualWarWith(final Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId()) && player.getClan().isAtWarWith(_clan.getClanId());
	}

	public final void doPurePk(final Player killer)
	{
		final int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);
		killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
		killer.setPkKills(killer.getPkKills() + 1);
		if(Config.ALLOW_PK_REWARD && killer.lastExit + Config.PK_REWARD_TIME < System.currentTimeMillis() && killer.getLevel() - getLevel() <= Config.PK_REWARD_LVL_DIFF && _pvpFlag <= 0 && (!killer._lastRewPK.containsKey(getObjectId()) || killer._lastRewPK.get(getObjectId()) + Config.PK_REWARD_TIME < System.currentTimeMillis()))
		{
			for(int i = 0; i < Config.PK_REWARD.length; i += 2)
			{
				killer.getInventory().addItem(Config.PK_REWARD[i], Config.PK_REWARD[i + 1]);
				killer.sendPacket(SystemMessage.obtainItems(Config.PK_REWARD[i], Config.PK_REWARD[i + 1], 0));
			}
			killer._lastRewPK.put(getObjectId(), System.currentTimeMillis());
		}
	}

	public final void doKillInPeace(final Player killer)
	{
		if(_karma <= 0)
			doPurePk(killer);
		else
		{
			killer.setPvpKills(killer.getPvpKills() + 1);
			pvpReward(killer);
		}
	}

	private void pvpReward(final Player killer)
	{
		if(Config.ALLOW_PVP_REWARD && killer.lastExit + Config.PVP_REWARD_TIME < System.currentTimeMillis() && killer.getLevel() - getLevel() <= Config.PVP_REWARD_LVL_DIFF && (!killer._lastRewPvP.containsKey(getObjectId()) || killer._lastRewPvP.get(getObjectId()) + Config.PVP_REWARD_TIME < System.currentTimeMillis()))
		{
			for(int i = 0; i < Config.PVP_REWARD.length; i += 2)
			{
				killer.getInventory().addItem(Config.PVP_REWARD[i], Config.PVP_REWARD[i + 1]);
				killer.sendPacket(SystemMessage.obtainItems(Config.PVP_REWARD[i], Config.PVP_REWARD[i + 1], 0));
			}
			killer._lastRewPvP.put(getObjectId(), System.currentTimeMillis());
		}
	}

	public void checkAddItemToDrop(final List<ItemInstance> array, final List<ItemInstance> items, final int maxCount)
	{
		for(int i = 0; i < maxCount && !items.isEmpty(); ++i)
			array.add(items.remove(Rnd.get(items.size())));
	}

	protected void doPKPVPManage(final Creature killer)
	{
		if(killer == null || killer == _summon || killer.getObjectId() == _objectId)
			return;

		final Player pk = killer.getPlayer();

		boolean protectedPvP = pk != null && (Config.PVP_HWID && isSameHWID(pk.getHWID()) || Config.PVP_IP && getIP().equals(pk.getIP()));

		final boolean battle = isInZoneBattle();
		if(battle || killer.isInZoneBattle())
		{
			if(!protectedPvP && Config.PVP_KILLS && battle && pk != null && ArrayUtils.contains(Config.PVP_KILLS_ZONES, getZoneIndex(Zone.ZoneType.battle_zone)))
			{
				pk.setPvpKills(pk.getPvpKills() + 1);
				pvpReward(pk);
			}
			return;
		}

		if(pk != null)
		{
			final int repValue = getLevel() - pk.getLevel() >= 20 ? Config.CLAN_WAR_POINTS_MORE : Config.CLAN_WAR_POINTS;
			final boolean war = atMutualWarWith(pk);
			if(!protectedPvP && _clan != null && _clan.getReputationScore() >= Config.CLAN_WAR_POINTS_MIN)
			{
				if(war)
				{
					if(_clan.getLevel() >= 5)
						_clan.broadcastToOtherOnlineMembers(new SystemMessage(1782).addString(getName()).addNumber(Integer.valueOf(-_clan.incReputation(-repValue, true, "ClanWar"))), this);
					if(pk.getClan().getLevel() >= 5)
						pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1783).addNumber(Integer.valueOf(pk.getClan().incReputation(repValue, true, "ClanWar"))), pk);
				}
				else
				{
					final CastleSiegeEvent siegeEvent = getEvent(CastleSiegeEvent.class);
					final CastleSiegeEvent siegeEventPk = pk.getEvent(CastleSiegeEvent.class);
					if(siegeEvent != null && siegeEvent == siegeEventPk && (siegeEventPk.getSiegeClan("defenders", pk.getClan()) != siegeEvent.getSiegeClan("attackers", getClan()) || siegeEventPk.getSiegeClan("attackers", pk.getClan()) != siegeEvent.getSiegeClan("defenders", getClan())))
					{
						if(_clan.getLevel() >= 5)
							_clan.broadcastToOtherOnlineMembers(new SystemMessage(1782).addString(getName()).addNumber(Integer.valueOf(-_clan.incReputation(-repValue, true, "ClanWar"))), this);
						if(pk.getClan().getLevel() >= 5)
							pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1783).addNumber(Integer.valueOf(pk.getClan().incReputation(repValue, true, "ClanWar"))), pk);
					}
					else
					{
						final ClanHallSiegeEvent chsiegeEvent = getEvent(ClanHallSiegeEvent.class);
						final ClanHallSiegeEvent chsiegeEventPk = pk.getEvent(ClanHallSiegeEvent.class);
						if(chsiegeEvent != null && chsiegeEvent == chsiegeEventPk && (chsiegeEventPk.getSiegeClan("defenders", pk.getClan()) != chsiegeEvent.getSiegeClan("attackers", getClan()) || chsiegeEventPk.getSiegeClan("attackers", pk.getClan()) != chsiegeEvent.getSiegeClan("defenders", getClan())))
						{
							if(_clan.getLevel() >= 5)
								_clan.broadcastToOtherOnlineMembers(new SystemMessage(1782).addString(getName()).addNumber(Integer.valueOf(-_clan.incReputation(-repValue, true, "ClanWar"))), this);
							if(pk.getClan().getLevel() >= 5)
								pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1783).addNumber(Integer.valueOf(pk.getClan().incReputation(repValue, true, "ClanWar"))), pk);
						}
					}
				}
			}

			if(isOnSiegeField())
				return;

			if(!protectedPvP && (_pvpFlag > 0 || war))
			{
				pk.setPvpKills(pk.getPvpKills() + 1);
				pvpReward(pk);
			}
			else
				doKillInPeace(pk);

			pk.sendChanges();
		}
		final int karma = _karma;
		decreaseKarma(Config.KARMA_LOST_BASE);
		final boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;
		if(killer.isMonster() && !Config.DROP_ITEMS_ON_DIE || isPvP && (_pkKills < Config.MIN_PK_TO_ITEMS_DROP || karma == 0 && Config.KARMA_NEEDED_TO_DROP) || isFestivalParticipant() || !killer.isMonster() && !isPvP)
			return;
		if(!Config.KARMA_DROP_GM && isGM() || getVarBoolean("NoDropPK") || Config.SERVICES_RATE_BONUS_NO_DROP_PK && isPremium())
			return;
		final int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;
		double dropRate;
		if(isPvP)
			dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
		else
			dropRate = Config.NORMAL_DROPCHANCE_BASE;
		int dropEquipCount = 0;
		int dropWeaponCount = 0;
		int dropItemCount = 0;
		for(int i = 0; i < Math.ceil(dropRate / 100.0) && i < max_drop_count; ++i)
			if(Rnd.chance(dropRate))
			{
				final int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
				if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
					++dropItemCount;
				else if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
					++dropEquipCount;
				else
					++dropWeaponCount;
			}
		final List<ItemInstance> drop = new LazyArrayList<ItemInstance>();
		final List<ItemInstance> dropItem = new LazyArrayList<ItemInstance>();
		final List<ItemInstance> dropEquip = new LazyArrayList<ItemInstance>();
		final List<ItemInstance> dropWeapon = new LazyArrayList<ItemInstance>();
		getInventory().writeLock();
		try
		{
			for(final ItemInstance item : getInventory().getItems())
				if(item.canBeDropped(this))
					if(!Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
						if(item.getTemplate().getType2() == 0)
							dropWeapon.add(item);
						else if(item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2)
							dropEquip.add(item);
						else if(item.getTemplate().getType2() == 5)
							dropItem.add(item);
			checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
			checkAddItemToDrop(drop, dropEquip, dropEquipCount);
			checkAddItemToDrop(drop, dropItem, dropItemCount);
			if(drop.isEmpty())
				return;
			for(ItemInstance item2 : drop)
			{
				if(item2.isEquipped())
					getInventory().unEquipItemInSlot(item2.getEquipSlot());
				item2 = getInventory().dropItem(item2, item2.getCount(), false);
				Log.LogItem(this, "PvPDrop", item2);
				boolean msg = false;
				if(Config.MOBSLOOTERS && killer.isMonster() && !item2.isCursed())
				{
					if(killer.isMinion() && !((MonsterInstance) killer).getLeader().isDead())
						((MonsterInstance) killer).getLeader().giveItem(item2, true);
					else
						((MonsterInstance) killer).giveItem(item2, true);
				}
				else if(Config.AUTO_LOOT_PK && pk != null)
				{
					pk.getInventory().addItem(item2);
					Log.LogItem(pk, "Pickup", item2);
					if(!item2.isCursed())
						msg = true;
				}
				else
				{
					item2 = getInventory().dropItem(item2, item2.getCount(), false);
					item2.dropMe(this, Location.findAroundPosition(getLoc(), 0, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT, getGeoIndex()));
				}
				if(item2.getEnchantLevel() > 0)
					sendPacket(new SystemMessage(375).addNumber(Integer.valueOf(item2.getEnchantLevel())).addItemName(Integer.valueOf(item2.getItemId())));
				else
					sendPacket(new SystemMessage(298).addItemName(Integer.valueOf(item2.getItemId())));
				if(msg)
					pk.sendPacket(SystemMessage.obtainItems(item2));
			}
		}
		finally
		{
			getInventory().writeUnlock();
		}
		refreshOverloaded();
	}

	@Override
	public final void doDie(final Creature killer)
	{
		if(isInOlympiadMode())
			return;
		super.doDie(killer);
	}

	@Override
	protected void onDeath(final Creature killer)
	{
		getDeathPenalty().checkCharmOfLuck();
		final TradeList tl = getTradeList();
		if(tl != null)
		{
			tl.removeAll();
			setTradeList(null);
		}
		if(isInTransaction())
		{
			if(getTransaction().isTypeOf(Transaction.TransactionType.TRADE))
				sendPacket(new SendTradeDone(0));
			getTransaction().cancel();
		}
		setPrivateStoreType((short) 0);
		if(_summon != null && (_summon.isPet() || _summon.isSiegeWeapon()))
			_summon.unSummon();
		if(_cubics != null)
			getAbnormalList().stopAll(EffectType.Cubic);
		if(Config.LOG_KILLS)
		{
			final String coords = " at (" + getX() + "," + getY() + "," + getZ() + ")";
			if(killer.isNpc())
				Log.add("" + this + " karma " + _karma + " killed by mob " + killer.getNpcId() + coords, "kills");
			else if(killer.isSummon() && killer.getPlayer() != null)
				Log.add("" + this + " karma " + _karma + " killed by summon of " + killer.getPlayer() + coords, "kills");
			else
				Log.add("" + this + " karma " + _karma + " killed by " + killer + coords, "kills");
		}
		if(Config.HITMAN_ENABLE && killer.isPlayer())
		{
			final Player pk = (Player) killer;
			if(HitmanInstance._orderPlayer.contains(_objectId) && HitmanInstance.getPlayerOrder(pk.getObjectId(), _objectId) && !isSameHWID(pk.getHWID()) && (Config.HITMAN_EXECUTE_CLAN || pk.getClanId() == 0 || pk.getClanId() != getClanId()) && (!Config.HITMAN_EXECUTE_PVP || getPvpFlag() > 0 || getKarma() > 0))
			{
				final int reward = HitmanInstance.getReward(_objectId);
				final String rewardName = HitmanInstance.getRewardName(_objectId);
				final String str = pk.getName() + " \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u043b(\u0430) \u0437\u0430\u043a\u0430\u0437 \u043d\u0430 " + getName();
				Announcements.getInstance().announceToAll(str);
				if(Config.HITMAN_LOGGING_ENABLE)
					HitmanInstance.RecordLog(str);
				pk.getInventory().addItem(rewardName.equals(Config.HITMAN_ITEM_NAME2) ? Config.HITMAN_ITEM_ID2 : Config.HITMAN_ITEM_ID, reward);
				pk.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 " + reward + " " + rewardName);
				HitmanInstance.orderDelete(_objectId);
				HitmanInstance.updateOrderPlayer();
			}
			if(Config.HITMAN_REVENGE_ENABLE && HitmanInstance.isRevenge(_objectId, pk.getObjectId()))
			{
				final String str2 = pk.getName() + " \u043f\u043e\u043a\u0430\u0440\u0430\u043b(\u0430) \u0441\u0432\u043e\u0435\u0433\u043e \u0437\u0430\u043a\u0430\u0437\u0447\u0438\u043a\u0430: " + getName();
				Announcements.getInstance().announceToAll(str2);
				if(Config.HITMAN_LOGGING_ENABLE)
					HitmanInstance.RecordLog(str2);
				if(Config.HITMAN_REVENGE_PERCENT != 0)
				{
					final int price = HitmanInstance.getReward(pk.getObjectId()) / 100 * Config.HITMAN_REVENGE_PERCENT;
					final String i = HitmanInstance.getRewardName(pk.getObjectId());
					getInventory().addItem(i.equals(Config.HITMAN_ITEM_NAME2) ? Config.HITMAN_ITEM_ID2 : Config.HITMAN_ITEM_ID, price);
					sendMessage("\u0412\u0430\u043c \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0435\u043d\u043e " + Config.HITMAN_REVENGE_PERCENT + "% \u043e\u0442 \u0432\u0430\u0448\u0435\u0433\u043e \u0437\u0430\u043a\u0430\u0437\u0430. \u0417\u0430\u043a\u0430\u0437 \u043e\u0442\u043c\u0435\u043d\u0435\u043d.");
				}
				HitmanInstance.orderDelete(pk.getObjectId());
				HitmanInstance.updateOrderPlayer();
			}
		}

		boolean checkPvp = true;
		if(Config.ALLOW_CURSED_WEAPONS)
		{
			if(isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().dropPlayer(this);
				checkPvp = false;
			}
			else if(killer != null && killer.isPlayer() && killer.isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().increaseKills(((Player) killer).getCursedWeaponEquippedId());
				checkPvp = false;
			}
		}

		final Object[] script_args = { this, killer };
		for(final Scripts.ScriptClassAndMethod handler : Scripts.onDie)
			Scripts.getInstance().callScripts(this, handler.className, handler.methodName, script_args);

		if(checkPvp)
		{
			doPKPVPManage(killer);
			altDeathPenalty(killer);
		}

		getDeathPenalty().notifyDead(killer);
		setIncreasedForce(0);

		if(isInParty() && getParty().isInDimensionalRift())
			getParty().getDimensionalRift().getDeadMemberList().add(this);

		stopWaterTask();

		if(!isSalvation() && isOnSiegeField() && isCharmOfCourage())
			setCharmOfCourage(false);

		if(getLevel() < 6)
		{
			final Quest q = QuestManager.getQuest(255);
			if(q != null)
				processQuestEvent(q.getId(), "CE30", null);
		}
		super.onDeath(killer);
	}

	public void restoreExp()
	{
		restoreExp(100.0);
	}

	public void restoreExp(final double percent)
	{
		if(percent == 0.0)
			return;
		int lostexp = 0;
		final String lostexps = getVar("lostexp");
		if(lostexps != null)
		{
			lostexp = Integer.parseInt(lostexps);
			unsetVar("lostexp");
		}
		if(lostexp != 0)
			addExpAndSp((long) (lostexp * percent / 100.0), 0L, false, false);
	}

	public void deathPenalty(final boolean atwar)
	{
		if(Config.DEATH_PENALTY_LOW_EXP > 0L && getExp() >= Config.DEATH_PENALTY_LOW_EXP)
			return;
		double deathPenaltyBonus = getDeathPenalty().getLevel() * Config.ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
		if(deathPenaltyBonus < 2.0)
			deathPenaltyBonus = 1.0;
		else
			deathPenaltyBonus /= 2.0;
		double percentLost = 8.0;
		final int lvl = getLevel();
		if(lvl >= 79)
			percentLost = 1.0;
		else if(lvl >= 78)
			percentLost = 1.5;
		else if(lvl >= 76)
			percentLost = 2.0;
		else if(lvl >= 40)
			percentLost = 4.0;
		if(Config.ALT_DEATH_PENALTY)
			percentLost = percentLost * Config.RATE_XP + _pkKills * Config.ALT_PK_DEATH_RATE;
		if(isFestivalParticipant() || atwar)
			percentLost /= 4.0;
		int lostexp = (int) Math.round((Experience.LEVEL[lvl + 1] - Experience.LEVEL[lvl]) * percentLost / 100.0);
		lostexp *= (int) deathPenaltyBonus;
		lostexp = (int) calcStat(Stats.EXP_LOST, lostexp, null, null);
		if(isOnSiegeField() && getEvent(SiegeEvent.class) != null)
			if(isCharmOfCourage())
				lostexp = 0;
			else
				lostexp /= 4;
		setVar("lostexp", String.valueOf(lostexp), -1L);
		addExpAndSp(-1 * lostexp, 0L, false, false);
	}

	public void setTransaction(final Transaction transaction)
	{
		_transaction = transaction;
	}

	public Transaction getTransaction()
	{
		return _transaction;
	}

	public boolean isInTransaction()
	{
		return _transaction != null && _transaction.isInProgress();
	}

	public boolean isBusy()
	{
		return isInTransaction() || isOutOfControl() || isInOlympiadMode() || getTeam() != 0 || isInStoreMode() || isInDuel() || getMessageRefusal() || isBlockAll() || isInvisible();
	}

	public boolean isInTrade()
	{
		return isInTransaction() && getTransaction().isTypeOf(Transaction.TransactionType.TRADE);
	}

	public List<L2GameServerPacket> addVisibleObject(final GameObject object, final Creature dropper)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible())
			return Collections.emptyList();
		return object.addPacketList(this, dropper);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		if(isInvisible() && forPlayer.getObjectId() != getObjectId())
			return Collections.emptyList();
		if(isInStoreMode() && forPlayer.getVarBoolean("notraders"))
			return Collections.emptyList();
		if(inObserverMode() && getOlympiadObserveId() == -1 && getCurrentRegion() != getObservNeighbor() && getObservNeighbor() == forPlayer.getCurrentRegion())
			return Collections.emptyList();
		final List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		if(forPlayer.getObjectId() != getObjectId())
			list.add(new CharInfo(this, forPlayer));
		if(isMounted())
			list.add(new Ride(this));
		if(isSitting() && _sittingObject > 0)
			list.add(new CharSit(this, _sittingObject));
		if(isInStoreMode())
		{
			if(getPrivateStoreType() == STORE_PRIVATE_BUY)
				list.add(new PrivateStoreMsgBuy(this, !isSameHWID(forPlayer.getHWID())));
			else if(getPrivateStoreType() == STORE_PRIVATE_SELL || getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				list.add(new PrivateStoreMsgSell(this, !isSameHWID(forPlayer.getHWID())));
			else if(getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				list.add(new RecipeShopMsg(this, !isSameHWID(forPlayer.getHWID())));
			if(forPlayer.isInZonePeace())
				return list;
		}
		if(isCastingNow())
		{
			final Creature castingTarget = getCastingTarget();
			final Skill castingSkill = getCastingSkill();
			final long animationEndTime = getAnimationEndTime();
			if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && getAnimationEndTime() > 0L)
				list.add(new MagicSkillUse(this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0L));
		}
		if(isInCombat())
			list.add(new AutoAttackStart(getObjectId()));
		list.add(RelationChanged.update(forPlayer, this, forPlayer));
		if(isInVehicle())
			list.add(getVehicle().getOnPacket(this, getInVehiclePosition()));
		else if(isMoving || isFollow)
			list.add(movePacket());
		return list;
	}

	public List<L2GameServerPacket> removeVisibleObject(final GameObject object, final List<L2GameServerPacket> list, final boolean deactivateAI)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId())
			return null;
		if(isInVehicle() && getVehicle() == object)
			return null;
		if(deactivateAI && object.isNpc())
		{
			final WorldRegion region = object.getCurrentRegion();
			final CharacterAI ai = object.getAI();
			if(ai instanceof DefaultAI && ai.isActive() && !ai.isGlobalAI() && (region == null || region.areNeighborsEmpty()))
				object.disableAI();
		}
		final List<L2GameServerPacket> result = list == null ? object.deletePacketList() : list;
		getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		return result;
	}

	public void decreaseLevel(final boolean dec)
	{
		if(dec)
		{
			if(getLevel() == 1)
				return;
			setLevel(getLevel() - 1);
		}
		if(isInParty())
			getParty().recalculatePartyData();
		if(_clan != null)
		{
			final PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
			for(final Player clanMember : _clan.getOnlineMembers(_objectId))
				if(!clanMember.equals(this))
					clanMember.sendPacket(memberUpdate);
		}
		if(_partyRoom != null)
			_partyRoom.broadcastPlayerUpdate(this);
		if(Config.REMOVE_SK_ON_DELEVEL > 0)
			checkSkills(Config.REMOVE_SK_ON_DELEVEL);
		stopToggle();
	}

	public void stopToggle()
	{
		for(final Abnormal e : getAbnormalList().values())
			if(e != null && e.getSkill().isToggle())
				e.exit();
	}

	public void checkSkills(final int maxDiff)
	{
		boolean upd = false;
		for(final Skill sk : getAllSkillsArray())
		{
			int level = sk.getLevel();
			if(level > 100)
				level = SkillTree._baseLevels.get(sk.getId());
			if(SkillTree.getMinSkillLevel(sk.getId(), getClassId(), level) >= getLevel() + maxDiff)
			{
				upd = true;
				final int id = sk.getId();
				removeSkill(sk, true);
				if(level > 1)
				{
					final Skill skill = SkillTable.getInstance().getInfo(id, level - 1);
					addSkill(skill, true);
				}
			}
		}
		if(upd)
			sendPacket(new SkillList(this));
	}

	private void stopAllTimers()
	{
		stopWaterTask();
		stopBonusTask();
		stopKickTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRegeneration();
		stopDeleteTask();
		if(_fallTask != null)
		{
			_fallTask.cancel(true);
			_fallTask = null;
		}
		stopBanEndTasks();
	}

	@Override
	protected void onDespawn()
	{
		setTarget(null);
		stopMove();
		stopAttackStanceTask();
		clearStatusListeners();
	}

	@Override
	public Servitor getServitor()
	{
		return _summon;
	}

	public void setServitor(final Servitor servitor)
	{
		boolean isPet = false;
		if(_summon != null && _summon.isPet())
			isPet = true;
		unsetVar("pet");
		_summon = servitor;
		autoShot();
		if(servitor == null)
		{
			if(isPet)
			{
				if(isLogoutStarted() && getPetControlItem() != null)
					setVar("pet", String.valueOf(getPetControlItem().getObjectId()), -1L);
				setPetControlItem(null);
			}
			if(getAbnormalList().getEffectsBySkillId(4140) != null)
				getAbnormalList().stop(4140);
		}
	}

	public void stopDeleteTask()
	{
		if(_deleteTask != null)
			_deleteTask.cancel(false);
		_deleteTask = null;
	}

	private void scheduleDelete()
	{
		if(isLogoutStarted())
			return;

		int time = 0;
		if(Config.SERVICES_ENABLE_NO_CARRIER)
			time = NumberUtils.toInt(getVar("noCarrier"), Config.SERVICES_NO_CARRIER_DEFAULT_TIME);

		if(time <= 0)
		{
			deleteMe();
			return;
		}

		PlayerManager.saveCharToDisk(this);

		client_request = false;

		stopToggle();

		if(recording)
			writeBot(false);

		if(isLogoutStarted() || isInOfflineMode())
			return;

		broadcastUserInfo(false);

		if(_deleteTask != null)
			_deleteTask.cancel(false);
		_deleteTask = ThreadPoolManager.getInstance().schedule(new DeleteTask(), time * 1000L);
	}

	@Override
	public void deleteMe()
	{
		if(isLogoutStarted())
			return;

		setLogoutStarted(true);
		prepareToLogout();
		Quest.pauseQuestTimes(this);
		super.deleteMe();

		_isDeleting = true;

		EnterWorld.notifyFriends(this, false);

		try
		{
			setOnlineStatus(false);
		}
		catch(Throwable t)
		{
			_log.error("deletedMe()", t);
		}
		try
		{
			stopAllTimers();
		}
		catch(Throwable t)
		{
			_log.error("deletedMe()", t);
		}
		try
		{
			setTarget(null);
		}
		catch(Throwable t)
		{
			_log.error("deletedMe()", t);
		}
		try
		{
			getInventory().deleteMe();
		}
		catch(Throwable t)
		{
			_log.error("deletedMe()", t);
		}

		_ai = null;
		_summon = null;
		_warehouse = null;
		_freight = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_chars = null;
		_enchantScroll = null;
		_lastNpc = HardReferences.emptyRef();
		_obsLoc = null;
		_observNeighbor = null;
		_askDialog = null;
		_lastRewPvP = null;
		_lastRewPK = null;
	}

	public void setTradeList(final TradeList x)
	{
		_tradeList = x;
	}

	public TradeList getTradeList()
	{
		return _tradeList;
	}

	public void setSellList(final ConcurrentLinkedQueue<TradeItem> x)
	{
		_sellList = x;
		saveTradeList();
	}

	public ConcurrentLinkedQueue<TradeItem> getSellList()
	{
		return _sellList != null ? _sellList : new ConcurrentLinkedQueue<TradeItem>();
	}

	public ManufactureList getCreateList()
	{
		return _createList;
	}

	public void setCreateList(final ManufactureList x)
	{
		_createList = x;
		saveTradeList();
	}

	public void setBuyList(final ConcurrentLinkedQueue<TradeItem> x)
	{
		_buyList = x;
		saveTradeList();
	}

	public ConcurrentLinkedQueue<TradeItem> getBuyList()
	{
		return _buyList != null ? _buyList : new ConcurrentLinkedQueue<TradeItem>();
	}

	public void setPrivateStoreType(final short type)
	{
		_privatestore = type;
		if(type != 0)
		{
			setVar("storemode", String.valueOf(type));
			if(recording)
				writeBot(false);
		}
		else
			unsetVar("storemode");
	}

	public short getPrivateStoreType()
	{
		return _olympiadObserveId != -1 ? STORE_OBSERVING_GAMES : _privatestore;
	}

	public void setSkillLearningClassId(final ClassId classId)
	{
		_skillLearningClassId = classId;
	}

	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}

	public void setClan(final Clan clan)
	{
		final Clan oldClan = _clan;
		if(oldClan != null && clan == null)
			for(final Skill skill : oldClan.getAllSkills())
				removeSkill(skill, false);
		if((_clan = clan) == null)
		{
			_pledgeType = 0;
			_pledgeClass = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			if(_activeClass != null)
				getInventory().checkAllConditions();
			return;
		}
		if(!clan.isMember(getObjectId()))
			setClan(null);
		setTitle("");
	}

	@Override
	public Clan getClan()
	{
		return _clan;
	}

	public ClanHall getClanHall()
	{
		final int id = _clan != null ? _clan.getHasHideout() : 0;
		return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
	}

	public Castle getCastle()
	{
		final int id = _clan != null ? _clan.getHasCastle() : 0;
		return ResidenceHolder.getInstance().getResidence(Castle.class, id);
	}

	public Alliance getAlliance()
	{
		return _clan == null ? null : _clan.getAlliance();
	}

	public boolean isClanLeader()
	{
		return _clan != null && _objectId == _clan.getLeaderId();
	}

	public boolean isAllyLeader()
	{
		return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
	}

	@Override
	public void reduceArrowCount()
	{
		if(Config.INFINITY_ARROW)
			return;
		final ItemInstance arrows = getInventory().destroyItem(getInventory().getPaperdollObjectId(8), 1L, false);
		if(arrows == null || arrows.getCount() == 0L)
		{
			getInventory().unEquipItemInSlot(8);
			_arrowItem = null;
		}
	}

	protected boolean checkAndEquipArrows()
	{
		if(getInventory().getPaperdollItem(8) == null)
		{
			if(getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.BOW)
				_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			if(_arrowItem != null)
				getInventory().setPaperdollItem(8, _arrowItem);
		}
		else
			_arrowItem = getInventory().getPaperdollItem(8);
		return _arrowItem != null;
	}

	public long getOnlineTime()
	{
		return _onlineTime + getUptime();// + getUptime();
	}

	public void setUptime(final long time)
	{
		_uptime = time;
	}

	public long getUptime()
	{
		return _uptime == 0L ? 0L : System.currentTimeMillis() - _uptime;
	}

	public boolean isInParty()
	{
		return _party != null;
	}

	public void setParty(final Party party)
	{
		_party = party;
	}

	public void joinParty(Party party, boolean force)
	{
		if(party != null)
		{
			(_party = party).addPartyMember(this, force);
			party.broadcastToPartyMembers(this, new PartySpelled(this, true));
			for(final Player member : party.getPartyMembers())
				sendPacket(new PartySpelled(member, true));
		}
	}

	public void leaveParty()
	{
		if(isInParty())
		{
			_party.oustPartyMember(this);
			_party = null;
		}
	}

	public Party getParty()
	{
		return _party;
	}

	@Override
	public boolean isGM()
	{
		return _playerAccess != null && _playerAccess.IsGM;
	}

	public void setAccessLevel(final int level)
	{
		_accessLevel = level;
	}

	@Override
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public void setPlayerAccess(final PlayerAccess pa)
	{
		if(pa != null)
			_playerAccess = pa;
		else
			_playerAccess = new PlayerAccess();
		setAccessLevel(isGM() || _playerAccess.Menu ? 100 : 0);
	}

	public PlayerAccess getPlayerAccess()
	{
		return _playerAccess;
	}

	public void setAccountAccesslevel(final int level, final int banTime)
	{
		AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(getAccountName(), level, banTime));
	}

	@Override
	public double getLevelMod()
	{
		return (89.0 + getLevel()) / 100.0;
	}

	@Override
	public void updateStats()
	{
		if(entering || isLogoutStarted())
			return;
		refreshOverloaded();
		refreshExpertisePenalty();
		super.updateStats();
	}

	public void updateKarma(final boolean flagChanged)
	{
		sendStatusUpdate(true, true, 27);
		if(flagChanged)
			broadcastRelationChanged();
	}

	public String getHWID()
	{
		if(getNetConnection() != null)
			return getNetConnection().getHWID();
		return null;
	}

	public boolean isSameHWID(String HWID)
	{
		if(StringUtils.isEmpty(HWID))
			return false;

		String hwid = getHWID();
		if(StringUtils.isEmpty(hwid))
			return false;

		return hwid.equalsIgnoreCase(HWID);
	}

	public void storeLastIpAndHWID(String ip, String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if(StringUtils.isEmpty(hwid)) {
				statement = con.prepareStatement("UPDATE characters SET last_ip=? WHERE obj_Id=? LIMIT 1");
				statement.setString(1, ip);
				statement.setInt(2, getObjectId());
			} else {
				statement = con.prepareStatement("UPDATE characters SET last_ip=?, last_hwid=? WHERE obj_Id=? LIMIT 1");
				statement.setString(1, ip);
				statement.setString(2, hwid);
				statement.setInt(3, getObjectId());
			}
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Could not store " + toString() + " IP and HWID: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void increaseKarma(final long add_karma)
	{
		final boolean flagChanged = _karma == 0;
		long new_karma = _karma + add_karma;
		if(new_karma > Integer.MAX_VALUE)
			new_karma = Integer.MAX_VALUE;
		if(_karma == 0 && new_karma > 0L)
		{
			if(_pvpFlag > 0)
			{
				_pvpFlag = 0;
				if(_PvPRegTask != null)
				{
					_PvPRegTask.cancel(true);
					_PvPRegTask = null;
				}
				sendStatusUpdate(true, true, 26);
			}
			_karma = (int) new_karma;
		}
		else
			_karma = (int) new_karma;
		updateKarma(flagChanged);
	}

	public void decreaseKarma(final int i)
	{
		final boolean flagChanged = _karma > 0;
		_karma -= i;
		if(_karma <= 0)
		{
			_karma = 0;
			updateKarma(flagChanged);
		}
		else
			updateKarma(false);
	}

	public static Player restore(final int objectId)
	{
		Player player = null;
		Connection con = null;
		Statement statement = null;
		Statement statement2 = null;
		ResultSet pl_rset = null;
		ResultSet ps_rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement2 = con.createStatement();
			pl_rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`='" + objectId + "' LIMIT 1");
			ps_rset = statement2.executeQuery("SELECT `class_id` FROM `character_subclasses` WHERE `char_obj_id`='" + objectId + "' AND `isBase`='1' LIMIT 1");
			if(pl_rset.next() && ps_rset.next())
			{
				final short classId = ps_rset.getShort("class_id");
				final boolean female = pl_rset.getInt("sex") == 1;
				final PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, female);
				player = GameObjectsStorage.getPlayer(objectId);
				if(player != null)
					player.kick(true);
				player = new Player(objectId, template);
				player.loadVariables();
				player.loadRealHero();
				player.setBaseClass(classId);
				player._accountName = pl_rset.getString("account_name");
				player.setName(pl_rset.getString("char_name"));
				player.setFace(pl_rset.getByte("face"));
				player.setHairStyle(pl_rset.getByte("hairStyle"));
				player.setHairColor(pl_rset.getByte("hairColor"));
				player.setHeading(pl_rset.getInt("heading"));
				player.setKarma(pl_rset.getInt("karma"));
				player.setPvpKills(pl_rset.getInt("pvpkills"));
				player.setPkKills(pl_rset.getInt("pkkills"));
				player.setLeaveClanTime(pl_rset.getLong("leaveclan") * 1000L);
				if(player.getLeaveClanTime() > 0L && player.canJoinClan())
					player.setLeaveClanTime(0L);
				player.setDeleteClanTime(pl_rset.getLong("deleteclan") * 1000L);
				if(player.getDeleteClanTime() > 0L && player.canCreateClan())
					player.setDeleteClanTime(0L);
				player.setOnlineTime(pl_rset.getLong("onlinetime") * 1000L);
				player.setNoble(pl_rset.getBoolean("noble"));
				player.updateKetraVarka();
				player.updateRam();
				final int clanId = pl_rset.getInt("clanid");
				if(clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
					player.setPledgeType(pl_rset.getInt("pledge_type"));
					player.setPowerGrade(pl_rset.getInt("pledge_rank"));
					player.setLvlJoinedAcademy(pl_rset.getInt("lvl_joined_academy"));
					player.setApprentice(pl_rset.getInt("apprentice"));
				}
				player.setCreateTime(pl_rset.getLong("createtime") * 1000L);
				player.setDeleteTimer(pl_rset.getInt("deletetime"));
				player.setTitle(pl_rset.getString("title"));
				if(player.getVar("namecolor") == null)
				{
					if(player.getPlayerAccess().IsGM)
						player.setNameColor(Config.GM_NAME_COLOUR, false);
					else if(player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId())
						player.setNameColor(Config.CLANLEADER_NAME_COLOUR, false);
					else
						player.setNameColor(Config.NORMAL_NAME_COLOUR, false);
				}
				else
					player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")), true);
				if(player.getVar("titlecolor") == null)
					player.setTitleColor(Integer.decode("0xFFFF77"));
				else
					player.setTitleColor(Integer.decode("0x" + player.getVar("titlecolor")));
				if(Config.AUTO_LOOT_INDIVIDUAL_ADENA)
					player.AutoLootAdena = player.getVarBoolean("AutoLootAdena", Config.AUTO_LOOT_ADENA);
				if(Config.AUTO_LOOT_INDIVIDUAL_ITEMS)
					player.AutoLootItems = player.getVarBoolean("AutoLootItems", Config.AUTO_LOOT_ITEMS);
				if(Config.AUTO_LOOT_INDIVIDUAL_HERBS)
					player.AutoLootHerbs = player.getVarBoolean("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
				if(Config.AUTO_LOOT_INDIVIDUAL_LIST)
					player.AutoLootList = player.getVarBoolean("AutoLootList", false);

				if(Config.SERVICES_LOCK_ACC_HWID || Config.SERVICES_LOCK_CHAR_HWID)
					PlayerManager.initLocks(player);

				player.noHeroAura = player.getVarBoolean("NoHeroAura", false);
				final String recomList = player.getVar("recomChars");
				if(recomList != null && !recomList.isEmpty())
					for(final String recom : recomList.split(","))
						if(!recom.isEmpty())
							player._recomChars.add(Integer.decode("0x" + recom));
				player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
				player.setLastAccess(pl_rset.getLong("lastAccess"));
				player.lastExit = player.getLastAccess() * 1000L;
				player.setRecomHave(pl_rset.getInt("rec_have"));
				player.setRecomLeft(pl_rset.getInt("rec_left"));

				if(!Config.USE_CLIENT_LANG)
					player.setLanguage(player.getVar(Language.LANG_VAR));

				player.setKeyBindings(pl_rset.getBytes("key_bindings"));
				player.setPcBangPoints(pl_rset.getInt("pcBangPoints"));
				player.restoreRecipeBook();
				player.restoreTradeList();
				if(player.getVar("storemode") != null)
				{
					player.setPrivateStoreType(Short.parseShort(player.getVar("storemode")));
					player.setSitting(true);
				}
				if(player.getVar("HeroStatus") != null && Long.parseLong(player.getVar("HeroStatus")) > System.currentTimeMillis() || Config.ENABLE_OLYMPIAD && Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(player.getObjectId()))
					player.setHero(true);
				player.updatePledgeClass();
				EventHolder.getInstance().findEvent(player);
				Quest.playerEnter(player);
				player._hidden = true;
				restoreCharSubClasses(player);
				player._hidden = false;
				try
				{
					final String var = player.getVar("ExpandInventory");
					if(var != null)
						player.setExpandInventory(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					final String var = player.getVar("IncMaxLoad");
					if(var != null)
						player.setIncMaxLoad(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					final String var = player.getVar("pet");
					if(var != null)
						player.setPetControlItem(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.warn("" + e);
				}
				player.setNonAggroTime(System.currentTimeMillis() + 15000L);
				if(player.getVar("jailed") != null && System.currentTimeMillis() / 1000L < Integer.parseInt(player.getVar("jailed")) - 10)
				{
					player.setXYZInvisible(-114648, -249384, -2984);
					final String[] re = player.getVar("jailedFrom").split(";");
					final Location loc = new Location(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
					player._unjailTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.TeleportTask(player, loc), Integer.parseInt(player.getVar("jailed")) * 1000L - System.currentTimeMillis());
				}
				else
				{
					player.unsetVar("jailed");
					player.unsetVar("jailedFrom");
					player.setXYZInvisible(pl_rset.getInt("x"), pl_rset.getInt("y"), pl_rset.getInt("z"));
				}
				player.setNoChannel(pl_rset.getLong("nochannel") * 1000L);
				if(player.getNoChannel() > 0L && player.getNoChannelRemained() < 0L)
					player.updateNoChannel(0L);
				PreparedStatement stmt = null;
				ResultSet chars = null;
				try
				{
					stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
					stmt.setString(1, player._accountName);
					stmt.setInt(2, objectId);
					chars = stmt.executeQuery();
					while(chars.next())
					{
						final Integer charId = chars.getInt("obj_Id");
						final String charName = chars.getString("char_name");
						player._chars.put(charId, charName);
					}
				}
				catch(Exception e2)
				{
					e2.printStackTrace();
				}
				finally
				{
					DbUtils.closeQuietly(stmt, chars);
				}
				if(Config.KILL_COUNTER)
				{
					Statement stt = null;
					ResultSet rstkills = null;
					try
					{
						stt = con.createStatement();
						rstkills = stt.executeQuery("SELECT `npc_id`, `count` FROM `killcount` WHERE `char_id`=" + objectId);
						player._StatKills = new HashMap<Integer, Long>(128);
						while(rstkills.next())
							player._StatKills.put(rstkills.getInt("npc_id"), rstkills.getLong("count"));
					}
					catch(Exception e3)
					{
						e3.printStackTrace();
					}
					finally
					{
						DbUtils.closeQuietly(stt, rstkills);
					}
				}
				if(Config.CRAFT_COUNTER)
				{
					Statement stcraft = null;
					ResultSet rstcraft = null;
					try
					{
						stcraft = con.createStatement();
						rstcraft = stcraft.executeQuery("SELECT `item_id`, `count` FROM `craftcount` WHERE `char_id`=" + objectId);
						player._StatCraft = new HashMap<Integer, Long>(32);
						while(rstcraft.next())
							player._StatCraft.put(rstcraft.getInt("item_id"), rstcraft.getLong("count"));
					}
					catch(Exception e3)
					{
						e3.printStackTrace();
					}
					finally
					{
						DbUtils.closeQuietly(stcraft, rstcraft);
					}
				}
				if(Config.DROP_COUNTER)
				{
					Statement stdrop = null;
					ResultSet rstdrop = null;
					try
					{
						stdrop = con.createStatement();
						rstdrop = stdrop.executeQuery("SELECT `item_id`, `count` FROM `dropcount` WHERE `char_id`=" + objectId);
						player._StatDrop = new HashMap<Integer, Long>(128);
						while(rstdrop.next())
							player._StatDrop.put(rstdrop.getInt("item_id"), rstdrop.getLong("count"));
					}
					catch(Exception e3)
					{
						e3.printStackTrace();
					}
					finally
					{
						DbUtils.closeQuietly(stdrop, rstdrop);
					}
				}

				if(!World.validCoords(player.getX(), player.getY()) || player.getX() == 0 && player.getY() == 0)
					player.setXYZInvisible(MapRegionTable.getTeleToClosestTown(player));

				player.updateTerritories();

				if(Config.ENABLE_OLYMPIAD && player.isInZoneOlympiad())
				{
					player.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.EnterWorld.TeleportedReasonOlympiad"));
					player.setXYZInvisible(43825, -47950, -790);
				}
				else if(player.isInZone(Zone.ZoneType.no_restart) && System.currentTimeMillis() / 1000L - player.getLastAccess() > player.getZone(Zone.ZoneType.no_restart).getRestartTime())
				{
					player.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.EnterWorld.TeleportedReasonNoRestart"));
					player.setXYZInvisible(MapRegionTable.getTeleToClosestTown(player));
				}
				else if(!player.isGM() && player.isInZone(Zone.ZoneType.Siege))
				{
					final SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
					if(siegeEvent != null)
						player.setXYZInvisible(siegeEvent.getEnterLoc(player));
					else
						player.setXYZInvisible(MapRegionTable.getTeleToClosestTown(player));
				}
				else if(DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getLoc(), false))
					player.setXYZInvisible(DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords());
	
				if(!player.isGM())
					player.getInventory().validateItems();

				player.revalidatePenalties();
				player.restoreBlockList();

				GameObjectsStorage.put(player);
			}
		}
		catch(Exception e4)
		{
			_log.error("restore: could not restore char data!", e4);
		}
		finally
		{
			DbUtils.closeQuietly(statement2, ps_rset);
			DbUtils.closeQuietly(con, statement, pl_rset);
		}
		return player;
	}

	public void incrementKillsCounter(final Integer Id)
	{
		final Long tmp = _StatKills.containsKey(Id) ? _StatKills.get(Id) + 1L : 1L;
		_StatKills.put(Id, tmp);
		sendMessage(new CustomMessage("l2s.gameserver.model.Player.KillsCounter").addString(tmp.toString()));
	}

	public void incrementDropCounter(final Integer Id, final Long qty)
	{
		_StatDrop.put(Id, _StatDrop.containsKey(Id) ? _StatDrop.get(Id) + qty : (long) qty);
	}

	public void incrementCraftCounter(final Integer Id, final Integer qty)
	{
		final Long tmp = _StatCraft.containsKey(Id) ? _StatCraft.get(Id) + qty : qty;
		_StatCraft.put(Id, tmp);
		sendMessage(new CustomMessage("l2s.gameserver.model.Player.CraftCounter").addString(tmp.toString()));
	}

	public void store(final boolean fast)
	{
		if(!_storeLock.tryLock())
			return;

		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			Statement fs = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,deletetime=?,title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?,onlinetime=?,noble=?,ketra=?,varka=?,ram=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=? WHERE obj_Id=?");
				statement.setInt(1, getFace());
				statement.setInt(2, getHairStyle());
				statement.setInt(3, getHairColor());
				statement.setInt(4, getHeading());
				if(_stablePoint == null)
				{
					statement.setInt(5, getX());
					statement.setInt(6, getY());
					statement.setInt(7, getZ());
				}
				else
				{
					statement.setInt(5, _stablePoint.getX());
					statement.setInt(6, _stablePoint.getY());
					statement.setInt(7, _stablePoint.getZ());
				}
				statement.setInt(8, getKarma());
				statement.setInt(9, getPvpKills());
				statement.setInt(10, getPkKills());
				statement.setInt(11, getRecomHave());
				statement.setInt(12, getRecomLeft());
				statement.setInt(13, getClanId());
				statement.setInt(14, getDeleteTimer());
				statement.setString(15, getTitle());
				statement.setInt(16, _accessLevel);
				statement.setInt(17, isOnline() && !isInOfflineMode() || isInOfflineMode() && Config.SHOW_OFFLINE_MODE_IN_ONLINE ? 1 : 0);
				statement.setLong(18, getLeaveClanTime() / 1000L);
				statement.setLong(19, getDeleteClanTime() / 1000L);
				statement.setLong(20, _NoChannel > 0L ? getNoChannelRemained() / 1000L : _NoChannel);
				statement.setInt(21, (int) (getOnlineTime() / 1000L));
				statement.setInt(22, isNoble() ? 1 : 0);
				statement.setInt(23, getKetra());
				statement.setInt(24, getVarka());
				statement.setInt(25, getRam());
				statement.setInt(26, getPledgeType());
				statement.setInt(27, getPowerGrade());
				statement.setInt(28, getLvlJoinedAcademy());
				statement.setInt(29, getApprentice());
				statement.setBytes(30, getKeyBindings());
				statement.setInt(31, getPcBangPoints());
				statement.setString(32, getName());
				statement.setInt(33, getObjectId());
				statement.execute();
				DbUtils.closeQuietly(statement);
				statement = null;
				Stat.increaseUpdatePlayerBase();
				try
				{
					if(!fast && Config.KILL_COUNTER && _StatKills != null)
					{
						fs = con.createStatement();
						for(final Map.Entry<Integer, Long> tmp : _StatKills.entrySet())
						{
							StringBuilder sb = new StringBuilder();
							fs.addBatch(sb.append("REPLACE DELAYED INTO `killcount` SET `npc_id`=").append(tmp.getKey()).append(", `count`=").append(tmp.getValue()).append(", `char_id`=").append(_objectId).toString());
						}
						fs.executeBatch();
						DbUtils.closeQuietly(fs);
					}
					if(!fast && Config.CRAFT_COUNTER && _StatCraft != null)
					{
						fs = con.createStatement();
						for(final Map.Entry<Integer, Long> tmp : _StatCraft.entrySet())
						{
							StringBuilder sb = new StringBuilder();
							fs.addBatch(sb.append("REPLACE DELAYED INTO `craftcount` SET `item_id`=").append(tmp.getKey()).append(", `count`=").append(tmp.getValue()).append(", `char_id`=").append(_objectId).toString());
						}
						fs.executeBatch();
						DbUtils.closeQuietly(fs);
					}
					if(!fast && Config.DROP_COUNTER && _StatDrop != null)
					{
						fs = con.createStatement();
						for(final Map.Entry<Integer, Long> tmp : _StatDrop.entrySet())
						{
							StringBuilder sb = new StringBuilder();
							fs.addBatch(sb.append("REPLACE DELAYED INTO `craftcount` SET `item_id`=").append(tmp.getKey()).append(", `count`=").append(tmp.getValue()).append(", `char_id`=").append(_objectId).toString());
						}
						fs.executeBatch();
						DbUtils.closeQuietly(fs);
					}
				}
				catch(ConcurrentModificationException ex)
				{}
				if(!fast)
				{
					storeEffects();
					storeDisableSkills();
					storeBlockList();
				}
				storeCharSubClasses();
			}
			catch(Exception e)
			{
				_log.warn("store: could not store char data: " + e);
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		finally
		{
			_storeLock.unlock();
		}
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public void setIsOnline(final boolean isOnline)
	{
		_isOnline = isOnline;
	}

	public void setOnlineStatus(final boolean isOnline)
	{
		_isOnline = isOnline;
		updateOnlineStatus();
	}

	public void updateOnlineStatus()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if(isInOfflineMode())
			{
				statement = con.prepareStatement("UPDATE characters SET online=? WHERE obj_id=?");
				statement.setInt(1, Config.SHOW_OFFLINE_MODE_IN_ONLINE ? 1 : 0);
				statement.setInt(2, getObjectId());
			}
			else
			{
				statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
				statement.setInt(1, isOnline() ? 1 : 0);
				statement.setLong(2, System.currentTimeMillis() / 1000L);
				statement.setInt(3, getObjectId());
			}
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("Could not set char online status: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public Skill addSkill(final Skill newSkill, final boolean store)
	{
		if(newSkill == null)
			return null;
		final Skill oldSkill = super.addSkill(newSkill);
		if(newSkill.equals(oldSkill))
			return oldSkill;
		if(store)
			storeSkill(newSkill, oldSkill);
		return oldSkill;
	}

	public Skill removeSkill(final Skill skill, final boolean fromDB)
	{
		if(skill == null)
			return null;
		return removeSkill(skill.getId(), fromDB, false);
	}

	public Skill removeSkill(final int id, final boolean fromDB, final boolean needDB)
	{
		final Skill oldSkill = super.removeSkillById(id);
		if(!fromDB)
			return oldSkill;
		if(oldSkill != null || needDB)
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
				statement.setInt(1, id);
				statement.setInt(2, getObjectId());
				statement.setInt(3, getActiveClassId());
				statement.execute();
			}
			catch(Exception e)
			{
				_log.error("Could not delete skill!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		return oldSkill;
	}

	private void storeSkill(final Skill newSkill, final Skill oldSkill)
	{
		if(newSkill == null)
		{
			_log.warn("could not store new skill. its NULL");
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) values(?,?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newSkill.getId());
			statement.setInt(3, newSkill.getLevel());
			statement.setString(4, newSkill.getName());
			statement.setInt(5, getActiveClassId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Could not store skills!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void restoreSkills(final boolean needDB)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int id = rset.getInt("skill_id");
				final int level = rset.getInt("skill_level");
				final Skill skill = SkillTable.getInstance().getInfo(id, level);
				if(skill == null)
					continue;

				if(!isGM() && !skill.isCommon() && !SkillTree.getInstance().isSkillPossible(this, skill.getId(), skill.getLevel()) && !ArrayUtils.contains(Config.VipSkillsList, skill.getId()))
				{
					removeSkill(id, true, needDB);
					removeSkillFromShortCut(skill.getId());
				}
				else
					super.addSkill(skill);
			}

			if(isNoble())
				updateNobleSkills();

			if(Config.BLESS_NOBL)
				super.addSkill(SkillTable.getInstance().getInfo(1323, 1));

			if(isHero())
				Hero.addSkills(this);

			if(_clan != null)
			{
				if(_clan.getLeaderId() == getObjectId() && _clan.getLevel() > 3)
					SiegeUtils.addSiegeSkills(this);
				_clan.addAndShowSkillsToPlayer(this);
			}

			if(getActiveClassId() >= 53 && getActiveClassId() <= 57 || getActiveClassId() == 117 || getActiveClassId() == 118)
				super.addSkill(SkillTable.getInstance().getInfo(1321, 1));

			super.addSkill(SkillTable.getInstance().getInfo(1322, 1));

			if(Config.UNSTUCK_SKILL && getSkillLevel(1050) < 0)
				super.addSkill(SkillTable.getInstance().getInfo(2099, 1));

			for(OptionDataTemplate optionData : _options.valueCollection())
			{
				for(Skill skill : optionData.getSkills())
					addSkill(skill);
			}
		}
		catch(Exception e)
		{
			_log.warn("Could not not restore skills: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void storeDisableSkills()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());
			if(_skillReuses.isEmpty())
				return;
			final SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`skill_level`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
			synchronized (_skillReuses)
			{
				for(final TimeStamp timeStamp : _skillReuses.valueCollection())
					if(timeStamp.hasNotPassed())
					{
						final StringBuilder sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(timeStamp.getId()).append(",");
						sb.append(timeStamp.getLevel()).append(",");
						sb.append(getActiveClassId()).append(",");
						sb.append(timeStamp.getEndTime()).append(",");
						sb.append(timeStamp.getReuseBasic()).append(")");
						b.write(sb.toString());
					}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.warn("Could not store disable skills data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restoreDisableSkills(final boolean sc)
	{
		if(sc)
		{
			final TimeStamp sts = _skillReuses.get(SCPPHC);
			_skillReuses.clear();
			if(sts != null)
				_skillReuses.put(SCPPHC, sts);
		}
		else
			_skillReuses.clear();
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT skill_id,skill_level,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=" + getObjectId() + " AND class_index=" + getActiveClassId());
			while(rset.next())
			{
				final int skillId = rset.getInt("skill_id");
				if(ArrayUtils.contains(Config.RESTART_SKILLS, skillId))
					continue;
				final int skillLevel = rset.getInt("skill_level");
				final long endTime = rset.getLong("end_time");
				final long rDelayOrg = rset.getLong("reuse_delay_org");
				final long curTime = System.currentTimeMillis();
				final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				if(skill == null || endTime - curTime <= 500L)
					continue;
				_skillReuses.put(skill.reuseCode(), new TimeStamp(skill, endTime, rDelayOrg));
				disableItem(skill, rDelayOrg, endTime - curTime);
			}
			DbUtils.close(statement);
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());
		}
		catch(Exception e)
		{
			_log.warn("Could not restore active skills data! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void storeEffects()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_effects_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId());
			final List<Abnormal> effects = getAbnormalList().values();
			if(effects == null || effects.isEmpty())
				return;
			final Abnormal[] effs = effects.toArray(new Abnormal[effects.size()]);
			Arrays.sort(effs, EffectsComparator.getInstance());
			int order = 0;
			final SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_effects_save` (`char_obj_id`,`skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration`,`order`,`class_index`) VALUES");
			for(Abnormal effect : effs)
				if(effect != null && effect.isInUse() && !effect.getSkill().isToggle() && (!Config.DEL_AUGMENT_BUFFS || !effect.getSkill().isItemSkill()) && effect.getEffectType() != EffectType.HealOverTime && effect.getEffectType() != EffectType.CombatPointHealOverTime)
				{
					if(effect.isSaveable())
					{
						final StringBuilder sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(effect.getSkill().getId()).append(",");
						sb.append(effect.getSkill().getLevel()).append(",");
						sb.append(effect.getCount()).append(",");
						sb.append(effect.getTime()).append(",");
						sb.append(effect.getPeriod()).append(",");
						sb.append(order).append(",");
						sb.append(getActiveClassId()).append(")");
						b.write(sb.toString());
					}
					while((effect = effect.getNext()) != null && effect.isSaveable())
					{
						final StringBuilder sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(effect.getSkill().getId()).append(",");
						sb.append(effect.getSkill().getLevel()).append(",");
						sb.append(effect.getCount()).append(",");
						sb.append(effect.getTime()).append(",");
						sb.append(effect.getPeriod()).append(",");
						sb.append(order).append(",");
						sb.append(getActiveClassId()).append(")");
						b.write(sb.toString());
					}
					++order;
				}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.error("Could not store active effects data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		updateEffectIcons();
	}

	public void restoreEffects()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration` FROM `character_effects_save` WHERE `char_obj_id`=? AND `class_index`=? ORDER BY `order` ASC");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();
			int i = 0;
			while(rset.next())
			{
				final int skillId = rset.getInt("skill_id");
				final int skillLvl = rset.getInt("skill_level");
				final int effectCount = rset.getInt("effect_count");
				final long effectCurTime = rset.getLong("effect_cur_time");
				final long duration = rset.getLong("duration");
				final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if(skill == null)
				{
					_log.warn("Can't restore Effect\tskill: " + skillId + ":" + skillLvl + " " + toFullString());
					Thread.dumpStack();
				}
				else if(skill.getEffectTemplates() == null)
				{
					_log.warn("Can't restore Effect, EffectTemplates is NULL\tskill: " + skillId + ":" + skillLvl + " " + toFullString());
					Thread.dumpStack();
				}
				else
					for(final EffectTemplate et : skill.getEffectTemplates())
						if(et != null)
							if(et._counter != 1 || et.getPeriod() != 0L)
							{
								final Env env = new Env(this, this, skill);
								final Abnormal abnormal = et.getEffect(env);
								if(abnormal != null)
								{
									abnormal.setCount(effectCount);
									abnormal.setPeriod(effectCount == 1 ? duration - effectCurTime : duration);
									abnormal.setStartTime(System.currentTimeMillis() + i);
									getAbnormalList().add(abnormal);
									++i;
								}
							}
			}
			DbUtils.closeQuietly(statement, rset);
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE char_obj_id = ? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.warn("Could not restore active effects data [charId: " + getObjectId() + "; ActiveClassId: " + getActiveClassId() + "]: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		updateEffectIcons();
		broadcastUserInfo(true);
	}

	@Override
	public void disableItem(final Skill handler, final long timeTotal, final long timeLeft)
	{
		if(handler.isHandler() && timeLeft > 1000L)
			if(handler.getReuseGroupId() > 0)
				for(final int id : handler.getReuseGroup())
					sendPacket(new ExUseSharedGroupItem(id, handler.getReuseGroupId(), (int) timeLeft, (int) timeTotal));
			else
				sendPacket(new ExUseSharedGroupItem(handler._itemConsumeId[0], handler._itemConsumeId[0], (int) timeTotal, (int) timeLeft));
	}

	private void restoreHenna()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();
			for(int i = 0; i < 3; ++i)
				_henna[i] = null;
			while(rset.next())
			{
				final int slot = rset.getInt("slot");
				if(slot >= 1)
				{
					if(slot > 3)
						continue;
					final int symbol_id = rset.getInt("symbol_id");
					if(symbol_id == 0)
						continue;
					final HennaTemplate tpl = HennaTable.getInstance().getTemplate(symbol_id);
					if(tpl == null)
						continue;
					final HennaInstance sym = new HennaInstance(tpl);
					_henna[slot - 1] = sym;
				}
			}
		}
		catch(Exception e)
		{
			_log.warn("could not restore henna: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		recalcHennaStats();
	}

	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + getClassId().level();
		for(int i = 0; i < 3; ++i)
			if(_henna[i] != null)
				--totalSlots;
		if(totalSlots <= 0)
			return 0;
		return totalSlots;
	}

	public boolean removeHenna(int slot)
	{
		if(slot < 1 || slot > 3)
			return false;
		--slot;
		if(_henna[slot] == null)
			return false;
		final HennaInstance henna = _henna[slot];
		final short dyeID = henna.getItemIdDye();
		final ItemInstance hennaDyes = ItemTable.getInstance().createItem(dyeID);
		hennaDyes.setCount(henna.getAmountDyeRequire() / 2);
		_henna[slot] = null;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getActiveClassId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could not remove char henna: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		recalcHennaStats();
		sendPacket(new HennaInfo(this));
		sendPacket(new UserInfo(this));
		getInventory().addItem(hennaDyes);
		final SystemMessage sm = new SystemMessage(53);
		sm.addItemName(Short.valueOf(henna.getItemIdDye()));
		sm.addNumber(Integer.valueOf(henna.getAmountDyeRequire() / 2));
		sendPacket(sm);
		return true;
	}

	public boolean addHenna(final HennaInstance henna)
	{
		if(getHennaEmptySlots() == 0)
		{
			sendPacket(Msg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			return false;
		}
		for(int i = 0; i < 3; ++i)
			if(_henna[i] == null)
			{
				_henna[i] = henna;
				recalcHennaStats();
				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getActiveClassId());
					statement.execute();
				}
				catch(Exception e)
				{
					_log.warn("could not save char henna: " + e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
				final HennaInfo hi = new HennaInfo(this);
				sendPacket(hi);
				sendPacket(new UserInfo(this));
				return true;
			}
		return false;
	}

	public void olyHenna(final boolean enable)
	{
		if(enable)
		{
			for(int i = 0; i < 3; ++i)
				_henna[i] = null;
			if(getVar("OlyHenna") != null)
			{
				final String[] ids = getVar("OlyHenna").split(";");
				int c = 0;
				for(final String id : ids)
					if(!id.isEmpty())
					{
						_henna[c] = new HennaInstance(HennaTable.getInstance().getTemplate(Integer.parseInt(id)));
						++c;
					}
			}
			recalcHennaStats();
		}
		else
			restoreHenna();
		sendPacket(new HennaInfo(this));
		sendPacket(new UserInfo(this));
	}

	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		for(int i = 0; i < 3; ++i)
			if(_henna[i] != null)
			{
				_hennaINT += _henna[i].getStatINT();
				_hennaSTR += _henna[i].getStatSTR();
				_hennaMEN += _henna[i].getStatMEN();
				_hennaCON += _henna[i].getStatCON();
				_hennaWIT += _henna[i].getStatWIT();
				_hennaDEX += _henna[i].getStatDEX();
			}
		if(_hennaINT > Config.LIM_HENNA_STAT)
			_hennaINT = Config.LIM_HENNA_STAT;
		if(_hennaSTR > Config.LIM_HENNA_STAT)
			_hennaSTR = Config.LIM_HENNA_STAT;
		if(_hennaMEN > Config.LIM_HENNA_STAT)
			_hennaMEN = Config.LIM_HENNA_STAT;
		if(_hennaCON > Config.LIM_HENNA_STAT)
			_hennaCON = Config.LIM_HENNA_STAT;
		if(_hennaWIT > Config.LIM_HENNA_STAT)
			_hennaWIT = Config.LIM_HENNA_STAT;
		if(_hennaDEX > Config.LIM_HENNA_STAT)
			_hennaDEX = Config.LIM_HENNA_STAT;
	}

	public HennaInstance getHenna(final int slot)
	{
		if(slot < 1 || slot > 3)
			return null;
		return _henna[slot - 1];
	}

	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}

	@Override
	public boolean consumeItem(final int itemConsumeId, final int itemCount)
	{
		final ItemInstance item = getInventory().findItemByItemId(itemConsumeId);
		return item != null && item.getCount() >= itemCount && getInventory().destroyItem(item, itemCount, false) != null;
	}

	@Override
	public boolean isMageClass()
	{
		return getClassId().getType().isMagician();
	}

	public boolean isMounted()
	{
		return _mountNpcId > 0;
	}

	public boolean checkLandingState()
	{
		if(isInZone(Zone.ZoneType.no_landing))
			return false;
		final SiegeEvent<?, ?> siege = getEvent(SiegeEvent.class);
		if(siege != null)
		{
			final Residence unit = siege.getResidence();
			return unit != null && getClan() != null && isClanLeader() && getClan().getHasCastle() == unit.getId();
		}
		return true;
	}

	public void setMount(final int npcId, final int obj_id, final int level)
	{
		if(isCursedWeaponEquipped() || isFlagEquipped())
			return;
		switch(npcId)
		{
			case 0:
			{
				setFlying(false);
				setRiding(false);
				removeSkillById(325);
				removeSkillById(4289);
				getAbnormalList().stop(4258);
				break;
			}
			case 12526:
			case 12527:
			case 12528:
			{
				setRiding(true);
				if(isNoble())
				{
					addSkill(SkillTable.getInstance().getInfo(325, 1), false);
					break;
				}
				break;
			}
			case 12621:
			{
				setFlying(true);
				setLoc(getLoc().changeZ(32));
				addSkill(SkillTable.getInstance().getInfo(4289, 1), false);
				break;
			}
		}
		if(npcId > 0)
			unEquipWeapon();
		_mountNpcId = npcId;
		_mountObjId = obj_id;
		_mountLevel = level;
		broadcastUserInfo(true);
		broadcastPacket(new Ride(this));
		broadcastUserInfo(true);
		sendPacket(new SkillList(this));
		sendPacket(new SetupGauge(3, 0));
	}

	private void unEquipWeapon()
	{
		ItemInstance wpn = getSecondaryWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}
		wpn = getActiveWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}
		abortAttack(true, false);
		abortCast(true, false);
	}

	public void sendDisarmMessage(final ItemInstance wpn)
	{
		if(wpn.isArrow())
			return;
		if(wpn.getEnchantLevel() > 0)
		{
			final SystemMessage sm = new SystemMessage(1064);
			sm.addNumber(Integer.valueOf(wpn.getEnchantLevel()));
			sm.addItemName(Integer.valueOf(wpn.getItemId()));
			sendPacket(sm);
		}
		else
		{
			final SystemMessage sm = new SystemMessage(417);
			sm.addItemName(Integer.valueOf(wpn.getItemId()));
			sendPacket(sm);
		}
	}

	public void setMountNpcId(final int id)
	{
		_mountNpcId = id;
	}

	public int getMountNpcId()
	{
		return _mountNpcId;
	}

	public void setMountObjId(final int id)
	{
		_mountObjId = id;
	}

	public int getMountObjId()
	{
		return _mountObjId;
	}

	public int getMountLevel()
	{
		return _mountLevel;
	}

	public void tempInventoryDisable()
	{
		_inventoryDisable = true;
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.InventoryEnableTask(this), 1500L);
	}

	public boolean isInventoryDisabled()
	{
		return _inventoryDisable;
	}

	public void setUsingWarehouseType(final Warehouse.WarehouseType type)
	{
		_usingWHType = type;
	}

	public Warehouse.WarehouseType getUsingWarehouseType()
	{
		return _usingWHType;
	}

	public Collection<EffectCubic> getCubics()
	{
		return _cubics == null ? Collections.emptyList() : _cubics.values();
	}

	public void addCubic(final EffectCubic cubic)
	{
		if(_cubics == null)
			_cubics = new ConcurrentHashMap<Integer, EffectCubic>(3);
		_cubics.put(cubic.getId(), cubic);
	}

	public void removeCubic(final int id)
	{
		if(_cubics != null)
			_cubics.remove(id);
	}

	public EffectCubic getCubic(final int id)
	{
		return _cubics == null ? null : _cubics.get(id);
	}

	@Override
	public String toString()
	{
		return getName() + "[" + getObjectId() + "]";
	}

	public int getEnchantEffect()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return Math.min(127, wpn.getEnchantLevel());
	}

	public int getVariation1Id()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return wpn.getVariation1Id();
	}

	public int getVariation2Id()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return wpn.getVariation2Id();
	}

	public int getEnchantEffect2()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;
		int e = wpn.getEnchantLevel();
		if(isInOlympiadMode() && Config.OLY_ENCHANT_LIMIT)
			e = Math.min(Config.OLY_ENCHANT_LIMIT_WEAPON, e);
		return Math.min(127, e);
	}

	public void setLastNpc(final NpcInstance npc)
	{
		if(npc == null)
		{
			teleList = 1;
			teleMod = 1.0f;
			_lastNpc = HardReferences.emptyRef();
		}
		else
		{
			if(_lastNpc != npc.getRef())
			{
				teleList = 1;
				teleMod = 1.0f;
			}
			_lastNpc = npc.getRef();
		}
	}

	public NpcInstance getLastNpc()
	{
		return _lastNpc.get();
	}

	public void setLastNpcId(final int id)
	{
		_lastNpcId = id;
	}

	public boolean checkLastNpc()
	{
		final NpcInstance npc = _lastNpc.get();
		return npc != null && npc.getNpcId() == _lastNpcId;
	}

	public int getLastNpcId()
	{
		return _lastNpcId;
	}

	public void setMultisell(final MultiSellHolder.MultiSellListContainer multisell)
	{
		_multisell = multisell;
	}

	public MultiSellHolder.MultiSellListContainer getMultisell()
	{
		return _multisell;
	}

	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}

	@Override
	public boolean unChargeShots(final boolean spirit)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		if(spirit)
			weapon.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
		else
			weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);

		autoShot();
		return true;
	}

	public boolean unChargeFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;
		weapon.setChargedFishshot(false);
		autoShot();
		return true;
	}

	public void autoShot()
	{
		for(Integer shotId : _activeSoulShots)
		{
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item == null)
			{
				removeAutoSoulShot(shotId);
				continue;
			}
			IItemHandler handler = item.getTemplate().getHandler();
			if(handler == null)
				continue;
			handler.useItem(this, item, false);
		}
	}

	public boolean getChargedFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedFishshot();
	}

	@Override
	public boolean getChargedSoulShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedSoulshot() == 1;
	}

	@Override
	public int getChargedSpiritShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return 0;
		return weapon.getChargedSpiritshot();
	}

	public void addAutoSoulShot(final Integer itemId)
	{
		_activeSoulShots.add(itemId);
	}

	public void removeAutoSoulShot(final Integer itemId)
	{
		_activeSoulShots.remove(itemId);
	}

	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}

	public void setInvisible(final boolean vis)
	{
		_invisible = vis;
	}

	@Override
	public boolean isInvisible()
	{
		return _invisible;
	}

	public int getClanPrivileges()
	{
		if(_clan == null)
			return 0;
		if(isClanLeader())
			return 8388606;
		if(_powerGrade < 1 || _powerGrade > 9)
			return 0;
		final Clan.RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if(privs != null)
			return privs.getPrivs();
		return 0;
	}

	public boolean isIn7sDungeon()
	{
		return getVarBoolean("isIn7sDungeon") && getZ() < -4700;
	}

	@Override
	public void sendMessage(CustomMessage message)
	{
		sendPacket(message);
	}

	@Override
	public void teleToLocation(final int x, final int y, final int z, final int instanceId)
	{
		if(isLogoutStarted())
			return;
		final Vehicle boat = getVehicle();
		if(boat != null && !boat.isTeleporting())
			boat.oustPlayer(this, getLoc(), false);
		super.teleToLocation(x, y, z, instanceId);
		if(getServitor() != null)
			getServitor().teleportToOwner();
	}

	public void onTeleported()
	{
		if(isFakeDeath())
			breakFakeDeath();
		if(isInVehicle())
			setLoc(getVehicle().getLoc());
		if(Config.TELEPORT_PROTECT > 0)
		{
			setNonAggroTime(System.currentTimeMillis() + Config.TELEPORT_PROTECT * 1000L);
			if(_mortalTask != null)
				_mortalTask.cancel(false);
			isProtect = true;
			_mortalTask = ThreadPoolManager.getInstance().schedule(new MortalTask(), Config.TELEPORT_PROTECT * 1000L);
		}
		spawnMe();
		if(escLoc)
		{
			escLoc = false;
			return;
		}
		setLastClientPosition(getLoc());
		setLastServerPosition(getLoc());
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		setIsTeleporting(false);
		if(isPendingRevive())
			doRevive(false);
		if(getTrainedBeast() != null)
			getTrainedBeast().setXYZ(getX() + Rnd.get(-100, 100), getY() + Rnd.get(-100, 100), getZ());
		checkWaterState();
		if(getVarBoolean("isIn7sDungeon") && getZ() >= -4700)
			unsetVar("isIn7sDungeon");
		sendActionFailed();
		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);
		sendUserInfo(true);
	}

	public boolean enterObserverMode(final Location loc)
	{
		_obsLoc = getLoc();
		_observNeighbor = World.getRegion(loc);
		if(_observNeighbor == null)
			return false;
		setInvisible(true);
		sendUserInfo(true);
		if(getCurrentRegion() != null)
			for(final WorldRegion neighbor : getCurrentRegion().getNeighbors())
				neighbor.removePlayerFromOtherPlayers(this);
		setTarget(null);
		stopMove();
		sitDown(0);
		block();
		_observerMode = 1;
		sendPacket(new ObserverStart(loc.x, loc.y, loc.z));
		return true;
	}

	public void appearObserverMode()
	{
		final WorldRegion currentRegion = getCurrentRegion();
		if(_observNeighbor == null || currentRegion == null)
		{
			if(_olympiadObserveId == -1)
				leaveObserverMode();
			else
				leaveOlympiadObserverMode();
			return;
		}
		_observerMode = 3;
		if(!_observNeighbor.equals(currentRegion))
			_observNeighbor.addObject(this);
		for(final WorldRegion neighbor : _observNeighbor.getNeighbors())
			neighbor.showObjectsToPlayer(this);
	}

	public void returnFromObserverMode()
	{
		_observerMode = 0;
		_observNeighbor = null;
		_olympiadObserveId = -1;
		setIsInvul(false);
		if(!isGM())
			setInvisible(false);
		final WorldRegion currentRegion = getCurrentRegion();
		if(currentRegion != null)
			for(final WorldRegion neighbor : currentRegion.getNeighbors())
				neighbor.showObjectsToPlayer(this);
		broadcastUserInfo(true);
	}

	public void leaveObserverMode()
	{
		final WorldRegion observNeighbor = _observNeighbor;
		if(observNeighbor != null)
			for(final WorldRegion neighbor : observNeighbor.getNeighbors())
			{
				neighbor.removeObjectsFromPlayer(this);
				neighbor.removeObject(this, false);
			}
		setLastClientPosition(null);
		setLastServerPosition(null);
		_observNeighbor = null;
		_observerMode = 2;
		setTarget(null);
		setIsInvul(false);
		unblock();
		standUp();
		sendPacket(new ObserverEnd(this));
		if(isLogoutStarted())
			_observerMode = 0;
	}

	public synchronized void enterOlympiadObserverMode(final Location loc, final int id)
	{
		stopMove();
		if(getServitor() != null)
			getServitor().unSummon();
		_olympiadObserveId = id;
		_obsLoc = getLoc();
		_observerMode = 1;
		_decoy = isGM() || !Config.ENABLE_DECOY ? null : new DecoyInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(31688), this);
		if(_decoy != null)
		{
			_decoy.setCurrentHp(_decoy.getMaxHp(), false);
			_decoy.setCurrentMp(_decoy.getMaxMp());
			_decoy.setHeading(getHeading());
			_decoy.spawnMe(_obsLoc);
		}
		setInvisible(true);
		sendUserInfo(true);
		if(getCurrentRegion() != null)
			for(final WorldRegion neighbor : getCurrentRegion().getNeighbors())
				neighbor.removePlayerFromOtherPlayers(this);
		setTarget(null);
		teleToLocation(loc);
		sendPacket(new ExOlympiadMode(3));
		if(_decoy != null)
			_decoy.sitDown();
	}

	public synchronized void switchOlympiadObserverArena(final int id)
	{
		setTarget(null);
		Olympiad.removeSpectator(_olympiadObserveId, this);
		_olympiadObserveId = id;
		sendPacket(new ExOlympiadMode(3));
		Olympiad.addSpectator(id, this, false);
	}

	public synchronized void leaveOlympiadObserverMode()
	{
		if(_decoy != null)
			_decoy.standUp();
		setTarget(null);
		teleToLocation(_obsLoc);
		if(_decoy != null)
			_decoy.deleteMe();
		sendPacket(new ExOlympiadMode(0));
		_observerMode = 0;
		Olympiad.removeSpectator(_olympiadObserveId, this);
		_olympiadObserveId = -1;
		if(!isGM())
			setInvisible(false);
		broadcastUserInfo(true);
		updateEffectIcons();
	}

	public void setOlympiadSide(final int i)
	{
		_olympiadSide = i;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	public void setOlympiadGameId(final int id)
	{
		_olympiadGameId = id;
	}

	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}

	@Override
	public int getOlympiadObserveId()
	{
		return _olympiadObserveId;
	}

	public Location getObsLoc()
	{
		return _obsLoc;
	}

	@Override
	public boolean inObserverMode()
	{
		return _observerMode > 0;
	}

	public byte getObserverMode()
	{
		return _observerMode;
	}

	public void setObserverMode(final byte mode)
	{
		_observerMode = mode;
	}

	public WorldRegion getObservNeighbor()
	{
		return _observNeighbor;
	}

	public void setObservNeighbor(final WorldRegion region)
	{
		_observNeighbor = region;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(final int mode)
	{
		_telemode = mode;
	}

	public void setLoto(final int i, final int val)
	{
		_loto[i] = val;
	}

	public int getLoto(final int i)
	{
		return _loto[i];
	}

	public void setRace(final int i, final int val)
	{
		_race[i] = val;
	}

	public int getRace(final int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(final boolean mode)
	{
		_messageRefusal = mode;
		sendEtcStatusUpdate();
	}

	public void setTradeRefusal(final boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void setExchangeRefusal(final boolean mode)
	{
		_exchangeRefusal = mode;
	}

	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}

	public void addToBlockList(final String charName)
	{
		if(charName == null || charName.equalsIgnoreCase(getName()) || isInBlockList(charName))
		{
			sendPacket(new SystemMessage(615));
			return;
		}
		final Player block_target = World.getPlayer(charName);
		if(block_target != null)
		{
			if(block_target.isGM())
			{
				sendPacket(new SystemMessage(827));
				return;
			}
			_blockList.put(block_target.getObjectId(), block_target.getName());
			sendPacket(new SystemMessage(617).addString(block_target.getName()));
			block_target.sendPacket(new SystemMessage(619).addString(getName()));
		}
		else
		{
			final int charId = PlayerManager.getObjectIdByName(charName);
			if(charId == 0)
			{
				sendPacket(new SystemMessage(615));
				return;
			}
			if(Config.gmlist.containsKey(charId) && Config.gmlist.get(charId).IsGM)
			{
				sendPacket(new SystemMessage(827));
				return;
			}
			_blockList.put(charId, charName);
			sendPacket(new SystemMessage(617).addString(charName));
		}
	}

	public void removeFromBlockList(final String charName)
	{
		int charId = 0;
		for(final int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
			{
				charId = blockId;
				break;
			}
		if(charId == 0)
		{
			sendPacket(new SystemMessage(306));
			return;
		}
		sendPacket(new SystemMessage(618).addString(_blockList.remove(charId)));
		final Player block_target = GameObjectsStorage.getPlayer(charId);
		if(block_target != null)
			block_target.sendMessage(getName() + " has removed you from his/her Ignore List.");
	}

	public boolean isInBlockList(final Player player)
	{
		return isInBlockList(player.getObjectId());
	}

	public boolean isInBlockList(final int charId)
	{
		return _blockList != null && _blockList.containsKey(charId);
	}

	public boolean isInBlockList(final String charName)
	{
		for(final int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
				return true;
		return false;
	}

	private void restoreBlockList()
	{
		_blockList.clear();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT target_Id, target_Name FROM character_blocklist WHERE obj_Id = ?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				final int targetId = rs.getInt("target_Id");
				final String name = rs.getString("target_Name");
				if(name == null)
					continue;
				_blockList.put(targetId, name);
			}
		}
		catch(SQLException e)
		{
			_log.warn("Can't restore player blocklist " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	private void storeBlockList()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_blocklist WHERE obj_Id=" + getObjectId());
			if(_blockList.isEmpty())
				return;
			final SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_blocklist` (`obj_Id`,`target_Id`,`target_Name`) VALUES");
			synchronized (_blockList)
			{
				for(final Map.Entry<Integer, String> e : _blockList.entrySet())
				{
					final StringBuilder sb = new StringBuilder("(");
					sb.append(getObjectId()).append(",");
					sb.append(e.getKey()).append(",'");
					sb.append(e.getValue().replace("'", "")).append("')");
					b.write(sb.toString());
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e2)
		{
			_log.error("Can't store player blocklist:", e2);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isBlockAll()
	{
		return _blockAll;
	}

	public void setBlockAll(final boolean state)
	{
		_blockAll = state;
	}

	public Collection<String> getBlockList()
	{
		return _blockList.values();
	}

	public void setHero(final boolean hero)
	{
		_hero = hero;
		if(recording && hero && Config.BOTS_NO_WRITE_HERO)
			writeBot(false);
	}

	@Override
	public boolean isHero()
	{
		return _hero;
	}

	public boolean isRealHero(){
		final Integer v = this.getRealHero(getName());
		if(v == null)
			return false;
		else{
			return true;
		}
	}

	public boolean isFakeHero()
	{
		final String v = this.getVar("HeroStatus");
		if(v == null || System.currentTimeMillis() > Long.parseLong(v))
			return false;
		else{
			return true;
		}
	}

	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
		if(_summon != null && _summon.isSummon())
			((SummonInstance) _summon).setIsInOlympiadMode(b);
	}

	@Override
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public boolean isOlympiadGameStart()
	{
		final int id = _olympiadGameId;
		if(id < 0)
			return false;
		final OlympiadGame og = Olympiad.getOlympiadGame(id);
		return og != null && og.getStarted() == 1;
	}

	public boolean isOlympiadCompStart()
	{
		final int id = _olympiadGameId;
		if(id < 0)
			return false;
		final OlympiadGame og = Olympiad.getOlympiadGame(id);
		return og != null && og.getStarted() == 2;
	}

	public void updateNobleSkills()
	{
		if(isNoble())
		{
			super.addSkill(SkillTable.getInstance().getInfo(1323, 1));
			super.addSkill(SkillTable.getInstance().getInfo(325, 1));
			super.addSkill(SkillTable.getInstance().getInfo(326, 1));
			super.addSkill(SkillTable.getInstance().getInfo(327, 1));
			super.addSkill(SkillTable.getInstance().getInfo(1324, 1));
			super.addSkill(SkillTable.getInstance().getInfo(1325, 1));
			super.addSkill(SkillTable.getInstance().getInfo(1326, 1));
			super.addSkill(SkillTable.getInstance().getInfo(1327, 1));
		}
		else
		{
			super.removeSkillById(1323);
			super.removeSkillById(325);
			super.removeSkillById(326);
			super.removeSkillById(327);
			super.removeSkillById(1324);
			super.removeSkillById(1325);
			super.removeSkillById(1326);
			super.removeSkillById(1327);
		}
	}

	public void setNoble(final boolean noble)
	{
		_noble = noble;
	}

	public void setNoble()
	{
		if(_noble)
			return;
		Quest q = QuestManager.getQuest(234);
		QuestState qs = getQuestState(q.getId());
		if(qs != null)
			qs.exitCurrentQuest(true);
		q.newQuestState(this, 3);
		q = QuestManager.getQuest(235);
		qs = getQuestState(q.getId());
		if(qs != null)
			qs.exitCurrentQuest(true);
		q.newQuestState(this, 3);
		setNoble(true);
		Olympiad.addNoble(this);
		updatePledgeClass();
		updateNobleSkills();
		sendPacket(new SkillList(this));
		broadcastUserInfo(true);
	}

	@Override
	public boolean isNoble()
	{
		return _noble;
	}

	public int getSubLevel()
	{
		return isSubClassActive() ? getLevel() : 0;
	}

	public int getBaseLevel()
	{
		int lvl = 0;
		if(_classlist.size() < 2)
			lvl = getLevel();
		else
			for(final SubClass sub : _classlist.values())
				if(sub.isBase())
				{
					lvl = sub.getLevel();
					break;
				}
		return lvl;
	}

	public void updateKetraVarka()
	{
		if(Functions.getItemCount(this, 7215) > 0L)
			_ketra = 5;
		else if(Functions.getItemCount(this, 7214) > 0L)
			_ketra = 4;
		else if(Functions.getItemCount(this, 7213) > 0L)
			_ketra = 3;
		else if(Functions.getItemCount(this, 7212) > 0L)
			_ketra = 2;
		else if(Functions.getItemCount(this, 7211) > 0L)
			_ketra = 1;
		else if(Functions.getItemCount(this, 7225) > 0L)
			_varka = 5;
		else if(Functions.getItemCount(this, 7224) > 0L)
			_varka = 4;
		else if(Functions.getItemCount(this, 7223) > 0L)
			_varka = 3;
		else if(Functions.getItemCount(this, 7222) > 0L)
			_varka = 2;
		else if(Functions.getItemCount(this, 7221) > 0L)
			_varka = 1;
		else
		{
			_varka = 0;
			_ketra = 0;
		}
	}

	public int getVarka()
	{
		return _varka;
	}

	public int getKetra()
	{
		return _ketra;
	}

	public void updateRam()
	{
		if(Functions.getItemCount(this, 7247) > 0L)
			_ram = 2;
		else if(Functions.getItemCount(this, 7246) > 0L)
			_ram = 1;
		else
			_ram = 0;
	}

	public int getRam()
	{
		return _ram;
	}

	public void setPledgeType(final int typeId)
	{
		_pledgeType = typeId;
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

	public void setLvlJoinedAcademy(final int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public void setPledgeClass(final int classId)
	{
		_pledgeClass = classId;
	}

	public int getPledgeClass()
	{
		return _pledgeClass;
	}

	public void updatePledgeClass()
	{
		final byte CLAN_LEVEL = _clan == null ? -1 : _clan.getLevel();
		final boolean CLAN_LEADER = _clan != null && _clan.getLeaderId() == _objectId;
		final boolean IN_ACADEMY = _clan != null && _clan.isAcademy(_pledgeType);
		final boolean IS_GUARD = _clan != null && _clan.isRoyalGuard(_pledgeType);
		final boolean IS_KNIGHT = _clan != null && _clan.isOrderOfKnights(_pledgeType);
		boolean IS_GUARD_CAPTAIN = false;
		boolean IS_KNIGHT_BANNERET = false;
		if(_clan != null && _pledgeType == 0)
		{
			final int leaderOf = _clan.getClanMember(Integer.valueOf(_objectId)).isSubLeader();
			if(_clan.isRoyalGuard(leaderOf))
				IS_GUARD_CAPTAIN = true;
			else if(_clan.isOrderOfKnights(leaderOf))
				IS_KNIGHT_BANNERET = true;
		}
		switch(CLAN_LEVEL)
		{
			case -1:
			{
				_pledgeClass = 0;
				break;
			}
			case 0:
			case 1:
			case 2:
			case 3:
			{
				if(CLAN_LEADER)
				{
					_pledgeClass = 2;
					break;
				}
				_pledgeClass = 1;
				break;
			}
			case 4:
			{
				if(CLAN_LEADER)
				{
					_pledgeClass = 3;
					break;
				}
				_pledgeClass = 1;
				break;
			}
			case 5:
			{
				if(CLAN_LEADER)
				{
					_pledgeClass = 4;
					break;
				}
				_pledgeClass = 2;
				break;
			}
			case 6:
			{
				if(CLAN_LEADER)
				{
					_pledgeClass = 5;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeClass = 1;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = 4;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeClass = 2;
					break;
				}
				_pledgeClass = 3;
				break;
			}
			case 7:
			{
				if(CLAN_LEADER)
				{
					_pledgeClass = 7;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeClass = 1;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = 6;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeClass = 3;
					break;
				}
				if(IS_KNIGHT_BANNERET)
				{
					_pledgeClass = 5;
					break;
				}
				if(IS_KNIGHT)
				{
					_pledgeClass = 2;
					break;
				}
				_pledgeClass = 4;
				break;
			}
			case 8:
			{
				if(CLAN_LEADER)
				{
					_pledgeClass = 8;
					break;
				}
				if(IN_ACADEMY)
				{
					_pledgeClass = 1;
					break;
				}
				if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = 7;
					break;
				}
				if(IS_GUARD)
				{
					_pledgeClass = 4;
					break;
				}
				if(IS_KNIGHT_BANNERET)
				{
					_pledgeClass = 6;
					break;
				}
				if(IS_KNIGHT)
				{
					_pledgeClass = 3;
					break;
				}
				_pledgeClass = 5;
				break;
			}
		}
		if(_hero)
			_pledgeClass = 8;
		else if(_noble && _pledgeClass < 5)
			_pledgeClass = 5;
	}

	public void setPowerGrade(final int grade)
	{
		_powerGrade = grade;
	}

	public int getPowerGrade()
	{
		return _powerGrade;
	}

	public void setApprentice(final int apprentice)
	{
		_apprentice = apprentice;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public int getSponsor()
	{
		return _clan == null ? 0 : _clan.getClanMember(Integer.valueOf(getObjectId())).getSponsor();
	}

	public void setTeam(final int team, final boolean checksForTeam)
	{
		_checksForTeam = checksForTeam;
		if(_team != team)
		{
			_team = team;
			sendChanges();
			if(getServitor() != null)
				getServitor().broadcastPetInfo();
		}
	}

	@Override
	public int getTeam()
	{
		return _team;
	}

	public boolean isChecksForTeam()
	{
		return _checksForTeam;
	}

	public int getNameColor()
	{
		if(inObserverMode())
			return Color.black.getRGB();

		return _nameColor;
	}

	public void setCodeNameColor(final int nameColor)
	{
		_nameColor = nameColor;
	}

	public void setNameColor(final int nameColor, final boolean save)
	{
		if(save || nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.GM_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(nameColor));
		else if(nameColor == Config.NORMAL_NAME_COLOUR)
			unsetVar("namecolor");
		_nameColor = nameColor;
	}

	public void setNameColor(final int red, final int green, final int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
		if(_nameColor != Config.NORMAL_NAME_COLOUR && _nameColor != Config.CLANLEADER_NAME_COLOUR && _nameColor != Config.GM_NAME_COLOUR && _nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(_nameColor));
		else
			unsetVar("namecolor");
	}

	public int getTitleColor()
	{
		return _titlecolor;
	}

	public void setTitleColor(final int titlecolor)
	{
		if(titlecolor != Integer.decode("0xFFFF77"))
			setVar("titlecolor", Integer.toHexString(titlecolor));
		else if(titlecolor == Integer.decode("0xFFFF77"))
			unsetVar("titlecolor");
		_titlecolor = titlecolor;
	}

	public void setCodeTitleColor(final int titlecolor)
	{
		_titlecolor = titlecolor;
	}

	public void setTitleColor(final int red, final int green, final int blue)
	{
		_titlecolor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
		if(_titlecolor != Integer.decode("0xFFFF77"))
			setVar("titlecolor", Integer.toHexString(_titlecolor));
		else
			unsetVar("titlecolor");
	}

	public final String toFullString()
	{
		final StringBuffer sb = new StringBuffer(160);
		sb.append("Player '").append(getName()).append("' [oid=").append(_objectId).append(", account='").append(getAccountName()).append(", ip=").append(_connection != null ? _connection.getIpAddr() : "0.0.0.0").append("']");
		return sb.toString();
	}

	public void setVar(String name, Object value, long expirationTime)
	{
		user_variables.put(name, String.valueOf(value));
		mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,?)", getObjectId(), name, String.valueOf(value), expirationTime);
	}

	public void setVar(String name, Object value)
	{
		setVar(name, value, -1);
	}

	public void unsetVar(final String name)
	{
		if(name == null)
			return;
		if(user_variables.remove(name) != null)
			mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", _objectId, name);
	}

	public String getVar(final String name)
	{
		return user_variables.get(name);
	}
	public Integer getRealHero(final String name)
	{
		return user_hero.get(name);
	}


	public boolean getVarBoolean(final String name, final boolean defaultVal)
	{
		final String var = user_variables.get(name);
		if(var == null)
			return defaultVal;
		return !var.equals("0") && !var.equalsIgnoreCase("false");
	}

	public boolean getVarBoolean(final String name)
	{
		final String var = user_variables.get(name);
		return var != null && !var.equals("0") && !var.equalsIgnoreCase("false");
	}

	public int getVarInt(final String name, final int defaultVal)
	{
		try
		{
			final int var = Integer.parseInt(user_variables.get(name));
			return var;
		}
		catch(Exception e)
		{
			return defaultVal;
		}
	}

	private void loadVariables()
	{
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();
			while(rs.next())
			{
				final String name = rs.getString("name");
				final String value = rs.getString("value");
				user_variables.put(name, value);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}
	private void loadRealHero()
	{
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM heroes WHERE char_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();
			while(rs.next())
			{
				final String name = rs.getString("char_name");
				final Integer value = rs.getInt("count");
				user_hero.put(name, value);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}

	public void setLanguage(String val)
	{
		_language = Language.getLanguage(val);
		setVar(Language.LANG_VAR, _language.getShortName(), -1);
	}

	public Language getLanguage()
	{
		if(Config.USE_CLIENT_LANG && getNetConnection() != null)
			return getNetConnection().getLanguage();
		return _language;
	}

	public int getLocationId()
	{
		if(getNetConnection() != null)
			return getNetConnection().getLanguage().getId();
		return -1;
	}

	public boolean isLangRus()
	{
		return getLanguage() == Language.RUSSIAN || getLanguage().getSecondLanguage() == Language.RUSSIAN;
	}

	public int isAtWarWith(final Integer id)
	{
		return _clan != null && _clan.isAtWarWith(id) ? 1 : 0;
	}

	public int isAtWar()
	{
		return _clan != null && _clan.isAtWarOrUnderAttack() > 0 ? 1 : 0;
	}

	public void stopWaterTask()
	{
		if(_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(2, 0));
			sendChanges();
		}
	}

	public void startWaterTask()
	{
		if(isDead())
			stopWaterTask();
		else if(Config.ALLOW_WATER && _taskWater == null)
		{
			final int timeinwater = (int) (calcStat(Stats.BREATH, 86.0, null, null) * 1000.0);
			sendPacket(new SetupGauge(2, timeinwater));
			_taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.WaterTask(this), timeinwater, 1000L);
			sendChanges();
		}
	}

	public void checkWaterState()
	{
		if(isInWater())
			startWaterTask();
		else
			stopWaterTask();
	}

	public void doRevive(final double percent)
	{
		restoreExp(percent);
		doRevive(true);
	}

	@Override
	public void doRevive(final boolean absolute)
	{
		super.doRevive(absolute);
		unsetVar("lostexp");
		updateEffectIcons();
		autoShot();
		if(isInParty() && getParty().isInDimensionalRift() && !DimensionalRiftManager.getInstance().checkIfInPeaceZone(getLoc()))
			getParty().getDimensionalRift().memberRessurected(this);
	}

	public void reviveRequest(final Player reviver, final double percent, final boolean pet, final boolean salva)
	{
		final ReviveAnswerListener reviveAsk = _askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) _askDialog.getValue() : null;
		if(reviveAsk != null)
		{
			if(reviveAsk.isForPet() == pet && reviveAsk.getPower() >= percent)
			{
				reviver.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
				return;
			}
			if(pet && !reviveAsk.isForPet())
			{
				reviver.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
				return;
			}
			if(pet && isDead())
			{
				reviver.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
				return;
			}
		}
		if(pet && getServitor() != null && getServitor().isDead() || !pet && isDead())
		{
			final ConfirmDlg pkt = new ConfirmDlg(1510, Config.REVIVE_TIME * 1000);
			pkt.addString(reviver.getName());
			ask(pkt, new ReviveAnswerListener(this, percent, pet, salva, reviver.getName()));
		}
	}

	public void summonCharacterRequest(final Creature summoner, final Location loc, final int summonConsumeCrystal)
	{
		final ConfirmDlg cd = new ConfirmDlg(1842, 60000);
		cd.addString(summoner.getName()).addZoneName(loc.x, loc.y, loc.z);
		ask(cd, new SummonAnswerListener(this, loc, summonConsumeCrystal));
	}

	public void scriptRequest(final String text, final String scriptName, final Object[] args)
	{
		ask(new ConfirmDlg(2010, 30000).addString(text), new ScriptAnswerListener(this, scriptName, args));
	}

	public void updateNoChannel(final long time)
	{
		Connection con = null;
		PreparedStatement statement = null;
		setNoChannel(time);
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET nochannel = ? WHERE obj_Id=? LIMIT 1");
			statement.setLong(1, _NoChannel > 0L ? _NoChannel / 1000L : _NoChannel);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.warn("Could not activate nochannel:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void checkRecom()
	{
		final Calendar temp = Calendar.getInstance();
		temp.set(11, 13);
		temp.set(12, 0);
		temp.set(13, 0);
		long count = Math.round((System.currentTimeMillis() / 1000L - _lastAccess) / 86400L);
		if(count == 0L && _lastAccess < temp.getTimeInMillis() / 1000L && System.currentTimeMillis() > temp.getTimeInMillis())
			++count;
		for(int i = 1; i < count; ++i)
			if(_recomHave < 200)
				_recomHave -= 2;
			else
				_recomHave -= 3;
		if(_recomHave < 0)
			_recomHave = 0;
		if(getLevel() < 10)
			return;
		if(count > 0L)
			restartRecom();
	}

	public void restartRecom()
	{
		try
		{
			_recomChars.clear();
			if(getLevel() < 20)
				_recomLeft = 3;
			else if(getLevel() < 40)
				_recomLeft = 6;
			else
				_recomLeft = 9;
			if(_recomHave < 200)
				_recomHave -= 2;
			else
				_recomHave -= 3;
			if(_recomHave < 0)
				_recomHave = 0;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean isInVehicle()
	{
		return _vehicle != null;
	}

	public Vehicle getVehicle()
	{
		return _vehicle;
	}

	public void setVehicle(final Vehicle boat)
	{
		_vehicle = boat;
	}

	public Location getInVehiclePosition()
	{
		return _inVehiclePosition;
	}

	public void setInVehiclePosition(final Location loc)
	{
		_inVehiclePosition = loc;
	}

	public HashMap<Integer, SubClass> getSubClasses()
	{
		return _classlist;
	}

	public void setBaseClass(final int baseClass)
	{
		_baseClass = baseClass;
	}

	public int getBaseClassId()
	{
		return _baseClass;
	}

	public void setActiveClass(final SubClass activeClass)
	{
		_activeClass = activeClass;
	}

	public SubClass getActiveClass()
	{
		return _activeClass;
	}

	public int getActiveClassId()
	{
		return _activeClass == null ? 0 : _activeClass.getClassId();
	}

	public synchronized void changeClassInDb(final int oldclass, final int newclass)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_effects_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
		}
		catch(SQLException e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void storeCharSubClasses()
	{
		synchronized (_subLock)
		{
			final SubClass main = getActiveClass();
			if(main != null)
			{
				main.setCp(getCurrentCp());
				main.setHp(getCurrentHp());
				main.setMp(getCurrentMp());
				main.setActive(true);
				getSubClasses().put(getActiveClassId(), main);
			}
			else
				_log.warn("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);
			Connection con = null;
			Statement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.createStatement();
				boolean active = true;
				for(final SubClass subClass : _classlist.values())
				{
					final StringBuilder sb = new StringBuilder("UPDATE character_subclasses SET ");
					sb.append("exp=").append(subClass.getExp()).append(",");
					sb.append("sp=").append(subClass.getSp()).append(",");
					sb.append("curHp=").append(subClass.getHp()).append(",");
					sb.append("curMp=").append(subClass.getMp()).append(",");
					sb.append("curCp=").append(subClass.getCp()).append(",");
					sb.append("level=").append(subClass.getLevel()).append(",");
					if(subClass.isActive() && active)
					{
						active = false;
						sb.append("active=1,");
					}
					else
						sb.append("active=0,");
					sb.append("isBase=").append(subClass.isBase() ? 1 : 0).append(",");
					sb.append("death_penalty=").append(subClass.getDeathPenalty().getLevelOnSaveDB());
					sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND class_id=").append(subClass.getClassId()).append(" LIMIT 1");
					statement.executeUpdate(sb.toString());
				}
				final StringBuilder sb = new StringBuilder("UPDATE character_subclasses SET ");
				sb.append("maxHp=").append(getMaxHp()).append(",");
				sb.append("maxMp=").append(getMaxMp()).append(",");
				sb.append("maxCp=").append(getMaxCp());
				sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND active=1 LIMIT 1");
				statement.executeUpdate(sb.toString());
			}
			catch(Exception e)
			{
				_log.error("Could not store char sub data for player: " + toString(), e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}

	public static void restoreCharSubClasses(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id,exp,sp,level,curHp,curCp,curMp,active,isBase,death_penalty FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final SubClass subClass = new SubClass();
				subClass.setBase(rset.getInt("isBase") != 0);
				subClass.setClassId(rset.getShort("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setHp(rset.getDouble("curHp"));
				subClass.setMp(rset.getDouble("curMp"));
				subClass.setCp(rset.getDouble("curCp"));
				subClass.setActive(rset.getInt("active") != 0);
				subClass.setDeathPenalty(new DeathPenalty(player, rset.getByte("death_penalty")));
				subClass.setPlayer(player);
				player.getSubClasses().put(subClass.getClassId(), subClass);
			}
			if(player.getSubClasses().size() == 0)
				throw new Exception("There are no one subclass for player: " + player);
			final int BaseClassId = player.getBaseClassId();
			if(BaseClassId == -1)
				throw new Exception("There are no base subclass for player: " + player);
			for(final SubClass subClass2 : player.getSubClasses().values())
				if(subClass2.isActive())
				{
					player.setActiveSubClass(subClass2.getClassId(), false);
					break;
				}
			if(player.getActiveClass() == null)
			{
				final SubClass subClass3 = player.getSubClasses().get(BaseClassId);
				subClass3.setActive(true);
				player.setActiveSubClass(subClass3.getClassId(), false);
			}
		}
		catch(Exception e)
		{
			_log.warn("Could not restore char sub-classes for player: " + player.toString() + " " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public boolean addSubClass(final int classId, final boolean storeOld)
	{
		if(_classlist.size() >= 4 + Config.ALT_GAME_SUB_ADD)
			return false;
		final ClassId newId = ClassId.values()[classId];
		final SubClass newClass = new SubClass();
		if(newId.getRace() == null)
			return false;
		newClass.setClassId(classId);
		newClass.setPlayer(this);
		_classlist.put(classId, newClass);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, Experience.LEVEL[Config.SUBCLASS_LEVEL]);
			statement.setInt(4, 0);
			statement.setDouble(5, getCurrentHp());
			statement.setDouble(6, getCurrentMp());
			statement.setDouble(7, getCurrentCp());
			statement.setDouble(8, getCurrentHp());
			statement.setDouble(9, getCurrentMp());
			statement.setDouble(10, getCurrentCp());
			statement.setInt(11, Config.SUBCLASS_LEVEL);
			statement.setInt(12, 0);
			statement.setInt(13, 0);
			statement.setInt(14, 0);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("Could not add character sub-class: " + e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		setActiveSubClass(classId, storeOld);
		boolean countUnlearnable = true;
		int unLearnable = 0;
		int numSkillsAdded = 0;
		for(SkillLearn[] skills = SkillTree.getInstance().getAvailableSkills(this, newId); skills.length > unLearnable; skills = SkillTree.getInstance().getAvailableSkills(this, newId))
		{
			for(final SkillLearn s : skills)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || !sk.getCanLearn(newId))
				{
					if(countUnlearnable)
						++unLearnable;
				}
				else
				{
					addSkill(sk, true);
					++numSkillsAdded;
				}
			}
			countUnlearnable = false;
		}
		restoreSkills(false);
		rewardSkills();
		sendPacket(new SkillList(this));
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());
		if(Config.DEBUG)
			_log.info(numSkillsAdded + " skills added for " + getName() + "'s sub-class.");
		return true;
	}

	public boolean modifySubClass(final int oldClassId, final int newClassId)
	{
		final SubClass originalClass = _classlist.get(oldClassId);
		if(originalClass.isBase())
			return false;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("Could not delete char sub-class: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		if(newClassId > 0 && oldClassId != getActiveClassId())
		{
			final SubClass oldsub = getActiveClass();
			oldsub.setCp(getCurrentCp());
			oldsub.setExp(getExp());
			oldsub.setLevel(getLevel());
			oldsub.setSp(getSp());
			oldsub.setHp(getCurrentHp());
			oldsub.setMp(getCurrentMp());
			oldsub.setActive(false);
			getSubClasses().put(getActiveClassId(), oldsub);
		}
		_classlist.remove(oldClassId);
		return newClassId <= 0 || addSubClass(newClassId, false);
	}

	public void setActiveSubClass(final int subId, final boolean store)
	{
		abortAttack(true, false);
		abortCast(true, false);

		final SubClass sub = getSubClasses().get(subId);
		if(sub == null)
		{
			System.out.print("WARNING! setActiveSubClass<?> :: sub == null :: subId == " + subId);
			Thread.dumpStack();
			return;
		}

		if(isInDuel() || isCanUseSelectedSub())
		{
			sendMessage(isLangRus() ? "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u0432\u043e \u0432\u0440\u0435\u043c\u044f \u0434\u0443\u044d\u043b\u0438!" : "Unable to perform during a duel!");
			return;
		}

		if(subId == getBaseClassId())
			sub.setBase(true);
		else
			sub.setBase(false);

		final boolean enter = _activeClass == null;
		if(!enter)
		{
			getAbnormalList().stopAll();
			storeEffects();
			storeDisableSkills();
			final QuestState qs = getQuestState(422);
			if(qs != null)
				qs.exitCurrentQuest(true);
		}
		if(store)
		{
			final SubClass oldsub = getActiveClass();
			oldsub.setCp(getCurrentCp());
			oldsub.setHp(getCurrentHp());
			oldsub.setMp(getCurrentMp());
			oldsub.setActive(false);
			getSubClasses().put(getActiveClassId(), oldsub);
		}
		sub.setActive(true);
		setActiveClass(sub);
		getSubClasses().put(getActiveClassId(), sub);
		setClassId(subId, false, false);
		removeAllSkills();
		if(!enter)
		{
			if(getAI() != null)
				getAI().clearNextAction();
			abortCast(true, false);
			getAbnormalList().stopAll();
		}
		if(getServitor() != null && getServitor().isSummon())
			getServitor().unSummon();
		checkRecom();
		restoreSkills(enter);
		rewardSkills();
		sendPacket(new ExStorageMaxCount(this));
		sendPacket(new SkillList(this));
		getInventory().refreshListeners();
		getInventory().checkAllConditions();
		for(int i = 0; i < 3; ++i)
			_henna[i] = null;
		restoreHenna();
		sendPacket(new HennaInfo(this));

		if(enter)
			restoreEffects();

		if(isVisible())
			restoreDisableSkills(true);

		setCurrentHpMp(sub.getHp(), sub.getMp(), false);
		setCurrentCp(sub.getCp());

		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));

		for(final int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));

		sendPacket(new SkillCoolTime(this));

		if(isInParty())
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));

		broadcastPacket(new SocialAction(getObjectId(), 15));
		getInventory().restoreCursedWeapon();
		getDeathPenalty().restore();
		setIncreasedForce(0);
		broadcastUserInfo(false);
		updateEffectIcons();
		updateStats();
	}

	public void startKickTask(final long delay, final boolean sc)
	{
		if(_kickTask != null)
			stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.KickTask(this, sc), delay);
	}

	public void stopKickTask()
	{
		if(_kickTask != null)
		{
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public void startBonusTask()
	{
		if(Config.SERVICES_RATE_TYPE > 0)
		{
			final int bonusExpire = getNetConnection().getBonusExpire();
			final float bonus = getNetConnection().getBonus();
			final long time = bonusExpire * 1000L - System.currentTimeMillis();
			if(time > 1000L)
			{
				_premium = true;
				if(_bonusExpiration != null)
					_bonusExpiration.cancel(false);
				_bonusExpiration = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.BonusTask(this), time);
			}
			else
			{
				_premium = false;
				if(bonus > 0.0f && Config.SERVICES_RATE_TYPE > 1)
					AccountBonusDAO.getInstance().delete(Config.SERVICES_RATE_TYPE == 3 ? String.valueOf(getObjectId()) : getAccountName());
			}
		}
	}

	public void stopBonusTask()
	{
		if(_bonusExpiration != null)
		{
			_bonusExpiration.cancel(false);
			_bonusExpiration = null;
		}
	}

	public boolean isPremium()
	{
		return _premium;
	}

	public int getInventoryLimit()
	{
		return (int) calcStat(Stats.INVENTORY_LIMIT, 0.0, null, null);
	}

	public int getWarehouseLimit()
	{
		return (int) calcStat(Stats.STORAGE_LIMIT, 0.0, null, null);
	}

	public int getFreightLimit()
	{
		return getWarehouseLimit();
	}

	public int getTradeLimit()
	{
		return (int) calcStat(Stats.TRADE_LIMIT, 0.0, null, null);
	}

	public int getDwarvenRecipeLimit()
	{
		return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50.0, null, null) + Config.ALT_ADD_RECIPES;
	}

	public int getCommonRecipeLimit()
	{
		return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50.0, null, null) + Config.ALT_ADD_RECIPES;
	}

	@Override
	public int getNpcId()
	{
		return -2;
	}

	public GameObject getVisibleObject(final int id)
	{
		if(getObjectId() == id)
			return this;
		GameObject target = null;
		if(getTargetId() == id)
			target = getTarget();
		if(target == null && _party != null)
			for(final Player p : _party.getPartyMembers())
				if(p != null && p.getObjectId() == id)
				{
					target = p;
					break;
				}
		if(target == null)
			target = World.getAroundObjectById(this, id);
		return target == null || target.isInvisible() ? null : target;
	}

	@Override
	public int getPAtk(final Creature target)
	{
		final double init = getActiveWeaponInstance() == null ? isMageClass() ? 3 : 4 : 0.0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getMAtk(final Creature target, final Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		final double init = getActiveWeaponInstance() == null ? 6.0 : 0.0;
		return (int) calcStat(Stats.MAGIC_ATTACK, init, target, skill);
	}

	@Override
	public int getPDef(final Creature target)
	{
		double init = 4.0;
		final ItemInstance chest = _inventory.getPaperdollItem(10);
		if(chest == null)
			init += isMageClass() ? 15.0 : 31.0;
		if(_inventory.getPaperdollItem(11) == null && (chest == null || chest.getBodyPart() != 32768))
			init += isMageClass() ? 8.0 : 18.0;
		if(_inventory.getPaperdollItem(6) == null)
			init += 12.0;
		if(_inventory.getPaperdollItem(9) == null)
			init += 8.0;
		if(_inventory.getPaperdollItem(12) == null)
			init += 7.0;
		return (int) calcStat(Stats.POWER_DEFENCE, init, target, null);
	}

	@Override
	public int getMDef(final Creature target, final Skill skill)
	{
		double init = 0.0;
		if(_inventory.getPaperdollItem(1) == null)
			init += 9.0;
		if(_inventory.getPaperdollItem(2) == null)
			init += 9.0;
		if(_inventory.getPaperdollItem(3) == null)
			init += 13.0;
		if(_inventory.getPaperdollItem(4) == null)
			init += 5.0;
		if(_inventory.getPaperdollItem(5) == null)
			init += 5.0;
		return (int) calcStat(Stats.MAGIC_DEFENCE, init, null, skill);
	}

	@Override
	public final int getMaxCp()
	{
		return (int) calcStat(Stats.MAX_CP, getTemplate().getBaseCpMax(getLevel()), null, null);
	}

	@Override
	public final int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, getTemplate().getBaseHpMax(getLevel()), null, null);
	}

	@Override
	public final int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, getTemplate().getBaseMpMax(getLevel()), null, null);
	}

	public boolean isSubClassActive()
	{
		return getBaseClassId() != getActiveClassId();
	}

	public Forum getMemo()
	{
		if(_forumMemo == null)
		{
			if(ForumsBBSManager.getInstance().getForumByName("MemoRoot") == null)
				return null;
			if(ForumsBBSManager.getInstance().getForumByName("MemoRoot").GetChildByName(_accountName) == null)
				ForumsBBSManager.getInstance().CreateNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), 3, 3, getObjectId());
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").GetChildByName(_accountName));
		}
		return _forumMemo;
	}

	public void setMemo(final Forum forum)
	{
		_forumMemo = forum;
	}

	@Override
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}

	public void setCursedWeaponEquippedId(final int value)
	{
		_cursedWeaponEquippedId = value;
	}

	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}

	public final String getCursedWeaponName(Player activeChar)
	{
		if(isCursedWeaponEquipped())
			return new CustomMessage("cursed_weapon_name." + _cursedWeaponEquippedId).toString(activeChar);
		return null;
	}

	@Override
	public boolean isImmobilized()
	{
		return super.isImmobilized() || isOverloaded() || isSitting() || isFishing();
	}

	@Override
	public boolean isBlocked()
	{
		return super.isBlocked() || isKeyBlocked() || isInMovie() || isTeleporting() || isLogoutStarted();
	}

	public boolean isFrozen()
	{
		return super.isBlocked();
	}

	public boolean isKeyBlocked()
	{
		if(_keyBlocked || _keyForced)
		{
			Functions.show(HtmCache.getInstance().getHtml(_keyBlocked ? "scripts/commands/voiced/charKey/char_key_protect.html" : "scripts/commands/voiced/charKey/char_key_forced.html", this), this);
			return true;
		}
		return false;
	}

	public boolean isKeyForced()
	{
		return _keyForced;
	}

	public void setKeyBlocked(final boolean v)
	{
		_keyBlocked = v;
	}

	public void setKeyForced(final boolean v)
	{
		_keyForced = v;
	}

	public void checkKey()
	{
		if(!Config.SERVICES_CHAR_KEY)
			return;
		if(Config.CHAR_KEYS.containsKey(getObjectId()))
			_keyBlocked = true;
		else if(Config.SERVICES_CHAR_FORCED_KEY)
			_keyForced = true;
		CK_FAIL = 0;
	}

	@Override
	public boolean isInvul()
	{
		return super.isInvul() || isInMovie() || isProtect;
	}

	public void setOverloaded(final boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public boolean isOverloaded()
	{
		return _overloaded;
	}

	public boolean isFishing()
	{
		return _isFishing;
	}

	public Fishing getFishing()
	{
		return _fishing;
	}

	public void setFishing(final boolean value)
	{
		_isFishing = value;
	}

	public void startFishing(final FishTemplate fish, final int lureId)
	{
		_fishing.setFish(fish);
		_fishing.setLureId(lureId);
		_fishing.startFishing();
	}

	public void stopFishing()
	{
		_fishing.stopFishing();
	}

	public Location getFishLoc()
	{
		return _fishing.getFishLoc();
	}

	public Bonus getBonus()
	{
		return _bonus;
	}

	public void restoreBonus()
	{
		getBonus().restore(this);
	}

	@Override
	public float getRateAdena()
	{
		if(!Config.ALT_RATE_ADENA)
			return _party == null ? _bonus.RATE_DROP_ADENA : _party._rateAdena;
		if(_party != null)
			return _party._rateAdena;
		float rate = 1.0f;
		final int lvl = getLevel();
		if(lvl > 75)
			rate *= Config.ALT_RATE_ADENA_S;
		else if(lvl >= 61)
			rate *= Config.ALT_RATE_ADENA_A;
		else if(lvl >= 52)
			rate *= Config.ALT_RATE_ADENA_B;
		else if(lvl >= 40)
			rate *= Config.ALT_RATE_ADENA_C;
		else if(lvl >= 20)
			rate *= Config.ALT_RATE_ADENA_D;
		else
			rate *= Config.ALT_RATE_ADENA_NG;
		rate *= _bonus.RATE_DROP_ADENA;
		return rate;
	}

	@Override
	public float getRateItems()
	{
		return _party == null ? _bonus.RATE_DROP_ITEMS : _party._rateDrop;
	}

	@Override
	public double getRateExp()
	{
		return calcStat(Stats.EXP, _bonus.RATE_XP, null, null);
	}

	@Override
	public double getRateSp()
	{
		return calcStat(Stats.SP, _bonus.RATE_SP, null, null);
	}

	@Override
	public float getRateSpoil()
	{
		return _party == null ? _bonus.RATE_DROP_SPOIL : _party._rateSpoil;
	}

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(final boolean state)
	{
		_maried = state;
	}

	public boolean isEngageRequest()
	{
		return _engagerequest;
	}

	public void setEngageRequest(final boolean state, final int playerid)
	{
		_engagerequest = state;
		_engageid = playerid;
	}

	public void setMaryRequest(final boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMaryRequest()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(final boolean state)
	{
		_maryaccepted = state;
	}

	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}

	public int getEngageId()
	{
		return _engageid;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(final int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(final int coupleId)
	{
		_coupleId = coupleId;
	}

	public void engageAnswer(final int answer)
	{
		if(!_engagerequest || _engageid == 0)
			return;
		final Player ptarget = GameObjectsStorage.getPlayer(_engageid);
		setEngageRequest(false, 0);
		if(ptarget != null)
			if(answer == 1)
			{
				CoupleManager.getInstance().createCouple(ptarget, this);
				ptarget.sendMessage(new CustomMessage("l2s.gameserver.model.Player.EngageAnswerYes"));
			}
			else
				ptarget.sendMessage(new CustomMessage("l2s.gameserver.model.Player.EngageAnswerNo"));
	}

	public void broadcastSnoop(final int type, final String name, final String _text)
	{
		if(_snoopListener.size() > 0)
		{
			final Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);
			for(final Player pci : _snoopListener)
				if(pci != null)
					pci.sendPacket(sn);
		}
	}

	public void addSnooper(final Player pci)
	{
		if(!_snoopListener.contains(pci))
			_snoopListener.add(pci);
	}

	public void removeSnooper(final Player pci)
	{
		_snoopListener.remove(pci);
	}

	public void addSnooped(final Player pci)
	{
		if(!_snoopedPlayer.contains(pci))
			_snoopedPlayer.add(pci);
	}

	public void removeSnooped(final Player pci)
	{
		_snoopedPlayer.remove(pci);
	}

	public void resetReuse(final boolean sc)
	{
		if(sc)
		{
			final TimeStamp sts = _skillReuses.get(SCPPHC);
			_skillReuses.clear();
			if(sts != null)
				_skillReuses.put(SCPPHC, sts);
		}
		else
			_skillReuses.clear();
		sendPacket(new SkillCoolTime(this));
	}

	public ScheduledFuture<?> getWaterTask()
	{
		return _taskWater;
	}

	public DeathPenalty getDeathPenalty()
	{
		if(_activeClass == null)
			return new DeathPenalty(this, (byte) 0);
		return getActiveClass().getDeathPenalty();
	}

	public boolean isCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(final boolean val)
	{
		if(!(_charmOfCourage = val))
			getAbnormalList().stop(5041);
		sendEtcStatusUpdate();
	}

	private void revalidatePenalties()
	{
		_curWeightPenalty = 0;
		armorExpertise = 0;
		weaponExpertise = 0;
		refreshOverloaded();
		refreshExpertisePenalty();
	}

	@Override
	public int getIncreasedForce()
	{
		return _increasedForce;
	}

	@Override
	public void setIncreasedForce(int i)
	{
		i = Math.min(i, 7);
		i = Math.max(i, 0);
		if(i == 0)
		{
			final Future<?> lastChargeRunnable = _lastChargeRunnable;
			if(lastChargeRunnable != null)
				lastChargeRunnable.cancel(false);
			_lastChargeRunnable = null;
		}
		else if(i > _increasedForce)
			sendPacket(new SystemMessage(323).addNumber(Integer.valueOf(i)));
		_increasedForce = i;
		sendEtcStatusUpdate();
	}

	public boolean isFalling()
	{
		return _fallTask != null;
	}

	public void falling(final int height, final int safeHeight)
	{
		if(/*!Config.DAMAGE_FROM_FALLING || */inObserverMode() || isDead() || isFlying())
			return;
		final int curHp = (int) getCurrentHp();
		final int damage = (int) calcStat(Stats.FALL_DAMAGE, height - safeHeight, null, null);
		if(damage >= 1)
		{
			if(curHp - damage < 1)
				setCurrentHp(1.0, false);
			else
				setCurrentHp(curHp - damage, false);
			sendPacket(new SystemMessage(296).addNumber(Integer.valueOf(damage)));
		}
	}

	@Override
	public boolean setXYZ(final int x, final int y, final int z, final boolean move)
	{
		final int currZ = getZ();
		boolean result = super.setXYZ(x, y, z, move);
		if(Config.ALLOW_FALL_FROM_WALLS && move && !isInVehicle() && !isFlying() && !isInZone(Zone.ZoneType.water) && currZ - getZ() > 64 && _fallTask == null)
		{
			_fallZ = currZ;
			_fallTask = ThreadPoolManager.getInstance().schedule(new FallTask(), 2000L);
		}
		return result;
	}

	@Override
	public void checkHpMessages(final double curHp, final double newHp)
	{
		final int[] _hp = { 30, 30 };
		final int[] skills = { 290, 291 };
		final double percent = getMaxHp() / 100;
		final double _curHpPercent = curHp / percent;
		final double _newHpPercent = newHp / percent;
		boolean needsUpdate = false;
		for(int i = 0; i < skills.length; ++i)
		{
			final int level = getSkillLevel(skills[i]);
			if(level > 0)
				if(_curHpPercent > _hp[i] && _newHpPercent <= _hp[i])
				{
					sendPacket(new SystemMessage(1133).addSkillName(skills[i], level));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _hp[i] && _newHpPercent > _hp[i])
				{
					sendPacket(new SystemMessage(1134).addSkillName(skills[i], level));
					needsUpdate = true;
				}
		}
		if(needsUpdate)
			sendChanges();
	}

	public void checkDayNightMessages()
	{
		final int level = getSkillLevel(294);
		if(level > 0)
			if(GameTimeController.getInstance().isNowNight())
				sendPacket(new SystemMessage(1131).addSkillName(294, level));
			else
				sendPacket(new SystemMessage(1132).addSkillName(294, level));
		sendChanges();
	}

	public boolean isInDangerArea()
	{
		return _isInDangerArea;
	}

	public void setInDangerArea(final boolean value)
	{
		_isInDangerArea = value;
	}

	public void setInCombatZone(final boolean flag)
	{
		_isInCombatZone = flag;
	}

	public void setOnSiegeField(final boolean flag)
	{
		_isOnSiegeField = flag;
	}

	public void setSiegeFieldId(final int val)
	{
		_siegeFieldId = val;
	}

	public boolean isInPeaceZone()
	{
		return _isInPeaceZone;
	}

	public void setInPeaceZone(final boolean b)
	{
		_isInPeaceZone = b;
	}

	public boolean isInSSZone()
	{
		return _isInSSZone;
	}

	public void setInSSZone(final boolean b)
	{
		_isInSSZone = b;
	}

	public boolean isInCombatZone()
	{
		return _isInCombatZone;
	}

	public boolean isOnSiegeField()
	{
		return _isOnSiegeField;
	}

	public int getSiegeFieldId()
	{
		return _siegeFieldId;
	}

	public void doZoneCheck(final int messageNumber)
	{
		final boolean oldIsInDangerArea = isInDangerArea();
		final boolean oldIsInCombatZone = isInCombatZone();
		final boolean oldIsOnSiegeField = isOnSiegeField();
		final boolean oldIsInPeaceZone = isInPeaceZone();
		final boolean oldSSQZone = isInSSZone();
		setInDangerArea(isInZone(Zone.ZoneType.poison) || isInZone(Zone.ZoneType.instant_skill) || isInZone(Zone.ZoneType.swamp) || isInZone(Zone.ZoneType.damage));
		setInCombatZone(isInZone(Zone.ZoneType.battle_zone));
		final Zone zone = getZone(Zone.ZoneType.Siege);
		setOnSiegeField(zone != null);
		if(isOnSiegeField())
			setSiegeFieldId(zone.getIndex());
		setInPeaceZone(isInZone(Zone.ZoneType.peace_zone));
		setInSSZone(isInZone(Zone.ZoneType.ssq_zone));
		if(oldIsInDangerArea != isInDangerArea() || oldIsInCombatZone != isInCombatZone() || oldIsOnSiegeField != isOnSiegeField() || oldIsInPeaceZone != isInPeaceZone() || oldSSQZone != isInSSZone())
		{
			sendPacket(new ExSetCompassZoneCode(this));
			sendPacket(new EtcStatusUpdate(this));
			if(messageNumber != 0)
				sendPacket(new SystemMessage(messageNumber));
		}
		if(oldIsInCombatZone != isInCombatZone())
			broadcastRelationChanged();
		if(oldIsOnSiegeField != isOnSiegeField())
		{
			broadcastRelationChanged();
			if(isOnSiegeField())
				sendPacket(Msg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			else
				sendPacket(Msg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
		}
		if(oldIsOnSiegeField != isOnSiegeField() && !isOnSiegeField() && !isTeleporting() && getPvpFlag() == 0)
			startPvPFlag(null);
		if(oldIsInPeaceZone != isInPeaceZone() && isInPeaceZone())
		{
			final DuelEvent duelEvent = getEvent(DuelEvent.class);
			if(duelEvent != null)
				duelEvent.abortDuel(this);
		}
		revalidateInResidence();
		checkWaterState();
		if(Config.WRITE_BOTS_AI)
			if(!recording && !oldIsInPeaceZone && isInPeaceZone())
				writeBot(true);
			else if(recording && (oldIsInPeaceZone && !isInPeaceZone() || isInZone(Zone.ZoneType.RESIDENCE)) && !isTeleporting())
				writeBot(false);
	}

	public void startAutoSaveTask()
	{
		if(_autoSaveTask == null)
			_autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
	}

	public void stopAutoSaveTask()
	{
		if(_autoSaveTask != null)
			_autoSaveTask.cancel(false);
		_autoSaveTask = null;
	}

	public void startPcBangPointsTask()
	{
		if(!Config.PCBANG_POINTS_ENABLED || Config.PCBANG_POINTS_DELAY <= 0)
			return;
		if(_pcCafePointsTask == null)
			_pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
	}

	public void stopPcBangPointsTask()
	{
		if(_pcCafePointsTask != null)
			_pcCafePointsTask.cancel(false);
		_pcCafePointsTask = null;
	}

	public void revalidateInResidence()
	{
		final Clan clan = _clan;
		if(clan == null)
			return;
		if(clan.getHasHideout() != 0)
		{
			final ClanHall clansHall = ResidenceHolder.getInstance().getResidenceByObject(ClanHall.class, this);
			if(clansHall != null && clansHall.checkIfInZone(getX(), getY(), getZ()))
			{
				setInResidence(ResidenceType.ClanHall);
				return;
			}
		}
		if(clan.getHasCastle() != 0)
		{
			final Castle castle = ResidenceHolder.getInstance().getResidenceByObject(Castle.class, this);
			if(castle != null && castle.checkIfInZone(getX(), getY(), getZ()))
			{
				setInResidence(ResidenceType.Castle);
				return;
			}
		}
		setInResidence(ResidenceType.None);
	}

	public ResidenceType getInResidence()
	{
		return _inResidence;
	}

	public void setInResidence(final ResidenceType inResidence)
	{
		_inResidence = inResidence;
	}

	@Override
	public void sendMessage(final String message)
	{
		sendPacket(new SystemMessage(message));
	}

	@Override
	public void setLastClientPosition(final Location position)
	{
		_lastClientPosition = position;
	}

	public Location getLastClientPosition()
	{
		return _lastClientPosition;
	}

	@Override
	public void setLastServerPosition(final Location position)
	{
		_lastServerPosition = position;
	}

	public Location getLastServerPosition()
	{
		return _lastServerPosition;
	}

	public void setUseSeed(final int id)
	{
		_useSeed = id;
	}

	public int getUseSeed()
	{
		return _useSeed;
	}

	public int getRelation(final Player target)
	{
		int result = 0;
		if(_pvpFlag != 0)
			result |= 0x2;
		if(_karma > 0)
			result |= 0x4;
		if(isClanLeader())
			result |= 0x80;
		for(final GlobalEvent e : getEvents())
			result = e.getRelation(this, target, result);
		final Clan clan1 = getClan();
		final Clan clan2 = target.getClan();
		if(clan1 != null && clan2 != null && target.getPledgeType() != -1 && getPledgeType() != -1 && clan2.isAtWarWith(clan1.getClanId()))
		{
			result |= 0x10000;
			if(clan1.isAtWarWith(clan2.getClanId()))
				result |= 0x8000;
		}
		return result;
	}

	public void setlastPvpAttack(final long time)
	{
		_lastPvpAttack = time;
	}

	public long getlastPvpAttack()
	{
		return _lastPvpAttack;
	}

	@Override
	public void startPvPFlag(final Creature target)
	{
		if(target != null && !PvPFlagDead && target.isDead())
			return;
		PvPFlagDead = false;
		long startTime = System.currentTimeMillis();
		if(target != null && target.getPvpFlag() != 0)
			startTime -= Config.PVP_TIME / 2;
		if(_pvpFlag != 0 && _lastPvpAttack > startTime)
			return;
		_lastPvpAttack = startTime;
		updatePvPFlag(1);
		if(_PvPRegTask == null)
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.PvPFlagTask(this), 1000L, 1000L);
	}

	public void stopPvPFlag()
	{
		if(_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
			_PvPRegTask = null;
		}
		updatePvPFlag(0);
	}

	public void updatePvPFlag(final int value)
	{
		if(_pvpFlag == value)
			return;
		setPvpFlag(value);
		sendStatusUpdate(true, true, 26);
		broadcastRelationChanged();
	}

	public void setPvpFlag(final int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	@Override
	public int getPvpFlag()
	{
		return _pvpFlag;
	}

	public boolean isInDuel()
	{
		return getEvent(DuelEvent.class) != null;
	}

	public boolean isInSiege()
	{
		return getEvent(SiegeEvent.class) != null;
	}

	public TamedBeastInstance getTrainedBeast()
	{
		return _tamedBeast;
	}

	public void setTrainedBeast(final TamedBeastInstance tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}

	public long getLastAttackPacket()
	{
		return _lastAttackPacket;
	}

	public void setLastAttackPacket()
	{
		_lastAttackPacket = System.currentTimeMillis();
	}

	public long getLastMovePacket()
	{
		return _lastMovePacket;
	}

	public void setLastMovePacket()
	{
		_lastMovePacket = System.currentTimeMillis();
	}

	public long getLastSkillPacket()
	{
		return _lastSkillPacket;
	}

	public void setLastSkillPacket()
	{
		_lastSkillPacket = System.currentTimeMillis();
	}

	public int getSkillPackets()
	{
		return _skillPackets;
	}

	public void addSkillPacket()
	{
		++_skillPackets;
	}

	public void clearSkillPackets()
	{
		_skillPackets = 0;
	}

	public long getLastItemPacket()
	{
		return _lastItemPacket;
	}

	public void setLastItemPacket()
	{
		_lastItemPacket = System.currentTimeMillis();
	}

	public int getItemPackets()
	{
		return _itemPackets;
	}

	public void addItemPacket()
	{
		++_itemPackets;
	}

	public void clearItemPackets()
	{
		_itemPackets = 0;
	}

	public long getLastClassChange()
	{
		return _lastClassChange;
	}

	public void setLastClassChange()
	{
		_lastClassChange = System.currentTimeMillis();
	}

	public long getLastValidPacket()
	{
		return _lastValidPacket;
	}

	public void setLastValidPacket()
	{
		_lastValidPacket = System.currentTimeMillis();
	}

	public byte[] getKeyBindings()
	{
		return _keyBindings;
	}

	public void setKeyBindings(byte[] keyBindings)
	{
		if(keyBindings == null)
			keyBindings = new byte[0];
		_keyBindings = keyBindings;
	}

	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}

	public void setPcBangPoints(final int val)
	{
		_pcBangPoints = val;
	}

	public void addPcBangPoints(int count, final boolean doublePoints)
	{
		if(doublePoints)
			count *= 2;
		_pcBangPoints += count;
		sendPacket(new SystemMessage(doublePoints ? 1708 : 1707).addNumber(Integer.valueOf(count)));
		sendPacket(new ExPCCafePointInfo(this, count, 1, 2, 12));
	}

	public boolean reducePcBangPoints(final int count)
	{
		if(_pcBangPoints < count)
			return false;
		_pcBangPoints -= count;
		sendPacket(new SystemMessage(1709).addNumber(Integer.valueOf(count)));
		sendPacket(new ExPCCafePointInfo(this, 0, 1, 2, 12));
		return true;
	}

	public void setGroundSkillLoc(final Location location)
	{
		_groundSkillLoc = location;
	}

	public Location getGroundSkillLoc()
	{
		return _groundSkillLoc;
	}

	public boolean isDeleting()
	{
		return _isDeleting;
	}

	public void setOfflineMode(final boolean val)
	{
		if(!val)
			unsetVar("offline");
		_offline = val;
	}

	public boolean isInOfflineMode()
	{
		return _offline;
	}

	public void saveTradeList()
	{
		String val = "";
		if(_sellList == null || _sellList.isEmpty())
			unsetVar("selllist");
		else
		{
			for(final TradeItem i : _sellList)
				val = val + i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("selllist", val);
			val = "";
			if(_tradeList != null && _tradeList.getSellStoreName() != null)
				setVar("sellstorename", _tradeList.getSellStoreName());
		}
		if(_buyList == null || _buyList.isEmpty())
			unsetVar("buylist");
		else
		{
			for(final TradeItem i : _buyList)
			{
				val = val + i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice();
				if(i.getEnchantLevel() > 0)
					val = val + ";" + i.getEnchantLevel();
				val += ":";
			}
			setVar("buylist", val);
			val = "";
			if(_tradeList != null && _tradeList.getBuyStoreName() != null)
				setVar("buystorename", _tradeList.getBuyStoreName());
		}
		if(_createList == null || _createList.getList().isEmpty())
			unsetVar("createlist");
		else
		{
			for(final ManufactureItem j : _createList.getList())
				val = val + j.getRecipeId() + ";" + j.getCost() + ":";
			setVar("createlist", val);
			if(_createList.getStoreName() != null)
				setVar("manufacturename", _createList.getStoreName());
		}
	}

	public void restoreTradeList()
	{
		if(getVar("selllist") != null)
		{
			_sellList = new ConcurrentLinkedQueue<TradeItem>();
			final String[] split;
			final String[] items = split = getVar("selllist").split(":");
			for(final String item : split)
				if(!item.isEmpty())
				{
					final String[] values = item.split(";");
					if(values.length >= 3)
					{
						final TradeItem i = new TradeItem();
						final int oId = Integer.parseInt(values[0]);
						int count = Integer.parseInt(values[1]);
						final int price = Integer.parseInt(values[2]);
						i.setObjectId(oId);
						final ItemInstance itemToSell = getInventory().getItemByObjectId(oId);
						if(count >= 1)
							if(itemToSell != null)
							{
								if(count > itemToSell.getIntegerLimitedCount())
									count = itemToSell.getIntegerLimitedCount();
								i.setCount(count);
								i.setOwnersPrice(price);
								i.setItemId(itemToSell.getItemId());
								i.setEnchantLevel(itemToSell.getEnchantLevel());
								_sellList.add(i);
							}
					}
				}
			if(_tradeList == null)
				_tradeList = new TradeList();
			if(getVar("sellstorename") != null)
				_tradeList.setSellStoreName(getVar("sellstorename"));
		}
		if(getVar("buylist") != null)
		{
			_buyList = new ConcurrentLinkedQueue<TradeItem>();
			final String[] split2;
			final String[] items = split2 = getVar("buylist").split(":");
			for(final String item : split2)
				if(!item.isEmpty())
				{
					final String[] values = item.split(";");
					if(values.length >= 3)
					{
						final TradeItem i = new TradeItem();
						i.setItemId(Integer.parseInt(values[0]));
						i.setCount(Integer.parseInt(values[1]));
						i.setOwnersPrice(Integer.parseInt(values[2]));
						if(values.length > 3)
							i.setEnchantLevel(Integer.parseInt(values[3]));
						_buyList.add(i);
					}
				}
			if(_tradeList == null)
				_tradeList = new TradeList();
			if(getVar("buystorename") != null)
				_tradeList.setBuyStoreName(getVar("buystorename"));
		}
		if(getVar("createlist") != null)
		{
			_createList = new ManufactureList();
			final String[] split3;
			final String[] items = split3 = getVar("createlist").split(":");
			for(final String item : split3)
				if(!item.isEmpty())
				{
					final String[] values = item.split(";");
					if(values.length >= 2)
						_createList.add(new ManufactureItem(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
				}
			if(getVar("manufacturename") != null)
				_createList.setStoreName(getVar("manufacturename"));
		}
	}

	public void restoreRecipeBook()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int id = rset.getInt("id");
				final RecipeList recipe = RecipeController.getInstance().getRecipeList(id);
				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			_log.warn("count not recipe skills:" + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void setDecoy(final DecoyInstance decoy)
	{
		_decoy = decoy;
	}

	public int getMountType()
	{
		switch(getMountNpcId())
		{
			case 12526:
			case 12527:
			case 12528:
			{
				return 1;
			}
			case 12621:
			{
				return 2;
			}
			default:
			{
				return 0;
			}
		}
	}

	@Override
	public int getRunSpeed()
	{
		if(isMounted())
		{
			if(!isInWater())
				return (int) calcStat(Stats.RUN_SPEED, getMountBaseSpeed(), null, null);
			getSwimSpeed();
		}
		return super.getRunSpeed();
	}

	@Override
	public int getWalkSpeed()
	{
		return getRunSpeed() * 70 / 100;
	}

	public int getMountBaseSpeed()
	{
		final PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
		int speed = petData != null ? petData.getSpeed() : 187;
		if(_mountLevel - getLevel() > 10)
			speed /= 2;
		return speed;
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if(isMounted())
			return getRunSpeed() * 1.0f / getMountBaseSpeed();
		return super.getMovementSpeedMultiplier();
	}

	@Override
	public double getCollisionRadius()
	{
		if(isMounted() && NpcTable.getTemplate(getMountNpcId()) != null)
			return NpcTable.getTemplate(getMountNpcId()).collisionRadius;
		return getBaseTemplate().collisionRadius;
	}

	@Override
	public double getCollisionHeight()
	{
		if(isMounted() && NpcTable.getTemplate(getMountNpcId()) != null)
			return NpcTable.getTemplate(getMountNpcId()).collisionHeight + getBaseTemplate().collisionHeight;
		return getBaseTemplate().collisionHeight;
	}

	public boolean isFlagEquipped()
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getTemplate().isFlag();
	}

	@Override
	public void setReflectionId(final int i)
	{
		if(getReflectionId() == i)
			return;
		super.setReflectionId(i);
		if(_summon != null && !_summon.isDead())
			_summon.setReflectionId(i);
	}

	public void setBuyListId(final int listId)
	{
		_buyListId = listId;
	}

	public int getBuyListId()
	{
		return _buyListId;
	}

	public boolean checksForShop(final boolean RequestManufacture)
	{
		if(!Config.ALLOW_PRIVATE_STORE && !RequestManufacture)
		{
			sendMessage(isLangRus() ? "\u041f\u0440\u0438\u0432\u0430\u0442\u043d\u0430\u044f \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "Private store disabled.");
			return false;
		}
		if(RequestManufacture && !Config.ALLOW_MANUFACTURE)
		{
			sendMessage(isLangRus() ? "\u041e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u043e!" : "Disabled!");
			return false;
		}
		if(!getPlayerAccess().UseTrade)
		{
			sendMessage("You can't use private store.");
			return false;
		}
		final String tradeBan = getVar("tradeBan");
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			sendMessage("Your trade is banned! Expires: " + (tradeBan.equals("-1") ? "never" : Util.formatTime((Long.parseLong(tradeBan) - System.currentTimeMillis()) / 1000L)) + ".");
			return false;
		}
		stopMove();
		final String BLOCK_ZONE = RequestManufacture ? "private workshop" : "private store";
		if(isActionBlocked(BLOCK_ZONE) && !isInStoreMode() && (!Config.SERVICES_NO_TRADE_ONLY_OFFLINE || Config.SERVICES_NO_TRADE_ONLY_OFFLINE && isInOfflineMode()))
		{
			sendPacket(RequestManufacture ? new SystemMessage(1297) : Msg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_IN_THIS_AREA);
			return false;
		}
		if(isCastingNow())
		{
			sendPacket(Msg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			return false;
		}
		if(isInCombat())
		{
			sendPacket(Msg.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return false;
		}
		if(isOutOfControl() || isActionsDisabled() || isMounted() || isInOlympiadMode() || isInDuel() || isAlikeDead() || isInTransaction())
			return false;
		if(!isInZonePeace())
		{
			sendMessage(isLangRus() ? "\u0422\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043c\u0438\u0440\u043d\u043e\u0439 \u0437\u043e\u043d\u0435." : "Trade allowed in peace zone only.");
			return false;
		}
		if(Config.SERVICES_TRADE_ONLY_FAR && !isInStoreMode())
		{
			boolean tradenear = false;
			for(final Player player : World.getAroundPlayers(this, Config.SERVICES_TRADE_RADIUS, 200))
				if(player.isInStoreMode())
				{
					tradenear = true;
					break;
				}
			if(!tradenear && World.getAroundNpc(this, Config.SERVICES_TRADE_RADIUS_NPC, 200).size() > 0)
				tradenear = true;
			if(tradenear)
			{
				sendMessage(new CustomMessage("trade.OtherTradersNear"));
				return false;
			}
		}
		if(Config.TRADE_ONLY_TOWNS > 0 && (!isInZone(Zone.ZoneType.Town) || TownManager.getInstance().getClosestTown(this).getTownId() != Config.TRADE_ONLY_TOWNS))
		{
			final String n = TownManager.getInstance().getTown(Config.TRADE_ONLY_TOWNS).getName();
			if(!isLangRus())
				sendMessage("Trade allowed only in " + n + ".");
			else
				sendMessage("\u0422\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0432 " + n + ".");
			return false;
		}
		if(Config.TRADE_ONLY_GH && !ZoneManager.getInstance().checkInZoneAndIndex(Zone.ZoneType.offshore, 1, getX(), getY(), getZ()))
		{
			if(!isLangRus())
				sendMessage("Trade allowed only in Giran Harbor.");
			else
				sendMessage("\u0422\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0432 Giran Harbor.");
			return false;
		}
		return true;
	}

	public void enterMovieMode()
	{
		if(isInMovie())
			return;
		setTarget(null);
		stopMove();
		setIsInMovie(true);
		sendPacket(new CameraMode(1));
	}

	public void leaveMovieMode()
	{
		if(!isInMovie())
			return;
		setIsInMovie(false);
		sendPacket(new CameraMode(0));
		sendActionFailed();
		broadcastUserInfo(true);
	}

	public void specialCamera(final GameObject target, final int dist, final int yaw, final int pitch, final int time, final int duration)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	public void setMovieId(final int id)
	{
		_movieId = id;
	}

	public int getMovieId()
	{
		return _movieId;
	}

	public boolean isInMovie()
	{
		return _isInMovie;
	}

	public void setIsInMovie(final boolean state)
	{
		_isInMovie = state;
	}

	public int getIncorrectValidateCount()
	{
		return 0;
	}

	public int setIncorrectValidateCount(final int count)
	{
		return 0;
	}

	public int getExpandInventory()
	{
		return _expandInventory;
	}

	public void setExpandInventory(final int inventory)
	{
		_expandInventory = inventory;
	}

	public int getIncMaxLoad()
	{
		return _incMaxLoad;
	}

	public void setIncMaxLoad(final int val)
	{
		_incMaxLoad = val;
	}

	public void setAutoLootAdena(final boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL_ADENA)
		{
			AutoLootAdena = enable;
			setVar("AutoLootAdena", String.valueOf(enable));
		}
	}

	public void setAutoLootItems(final boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL_ITEMS)
		{
			AutoLootItems = enable;
			setVar("AutoLootItems", String.valueOf(enable));
		}
	}

	public void setAutoLootHerbs(final boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL_HERBS)
		{
			AutoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable));
		}
	}

	public void setAutoLootList(final boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL_LIST)
		{
			AutoLootList = enable;
			setVar("AutoLootList", String.valueOf(enable));
		}
	}

	public boolean isAutoLootAdenaEnabled()
	{
		return AutoLootAdena;
	}

	public boolean isAutoLootItemsEnabled()
	{
		return AutoLootItems;
	}

	public boolean isAutoLootHerbsEnabled()
	{
		return AutoLootHerbs;
	}

	public boolean isAutoLootListEnabled()
	{
		return AutoLootList;
	}

	public final void reName(final String name, final boolean saveToDB)
	{
		setName(name);
		if(saveToDB)
			saveNameToDB();
		Olympiad.changeNobleName(getObjectId(), name);
		Hero.changeHeroName(getObjectId(), name);
		broadcastUserInfo(true);
	}

	public final void reName(final String name)
	{
		reName(name, false);
	}

	public final void saveNameToDB()
	{
		Connection con = null;
		PreparedStatement st = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
			st.setString(1, getName());
			st.setInt(2, getObjectId());
			st.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

	@Override
	public Player getPlayer()
	{
		return this;
	}

	private List<String> getStoredBypasses(final boolean bbs)
	{
		if(bbs)
		{
			if(bypasses_bbs == null)
				bypasses_bbs = new ArrayList<String>();
			return bypasses_bbs;
		}
		if(bypasses == null)
			bypasses = new ArrayList<String>();
		return bypasses;
	}

	public void cleanBypasses(final boolean bbs)
	{
		final List<String> bypassStorage = getStoredBypasses(bbs);
		synchronized (bypassStorage)
		{
			bypassStorage.clear();
		}
	}

	public String encodeBypasses(final String htmlCode, final boolean bbs)
	{
		final List<String> bypassStorage = getStoredBypasses(bbs);
		synchronized (bypassStorage)
		{
			return BypassManager.encode(htmlCode, bypassStorage, bbs);
		}
	}

	public BypassManager.DecodedBypass decodeBypass(final String bypass)
	{
		final BypassManager.BypassType bpType = BypassManager.getBypassType(bypass);
		final boolean bbs = bpType == BypassManager.BypassType.ENCODED_BBS || bpType == BypassManager.BypassType.SIMPLE_BBS;
		final List<String> bypassStorage = getStoredBypasses(bbs);
		if(bpType == BypassManager.BypassType.ENCODED || bpType == BypassManager.BypassType.ENCODED_BBS)
			return BypassManager.decode(bypass, bypassStorage, bbs, this);
		if(bpType == BypassManager.BypassType.SIMPLE)
			return new BypassManager.DecodedBypass(bypass, false).trim();
		if(bpType == BypassManager.BypassType.SIMPLE_BBS && Config.ALLOW_COMMUNITYBOARD)
			return new BypassManager.DecodedBypass(bypass, true).trim();
		if(bypass.startsWith("_bbs"))
			sendPacket(Msg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
		else
			Log.addLog("Direct access to bypass: " + bypass + " / Player: " + toString(), "bypass");
		return null;
	}

	public void setActive()
	{
		eventAct = true;
		setNonAggroTime(0L);
		if(!isProtect)
			return;
		isProtect = false;
		if(_mortalTask != null)
		{
			_mortalTask.cancel(false);
			_mortalTask = null;
		}
	}

	public void setPetControlItem(final int itemObjId)
	{
		setPetControlItem(getInventory().getItemByObjectId(itemObjId));
	}

	public void setPetControlItem(final ItemInstance item)
	{
		_petControlItem = item;
	}

	public ItemInstance getPetControlItem()
	{
		return _petControlItem;
	}

	public void summonPet()
	{
		if(getServitor() != null)
			return;
		final ItemInstance controlItem = getPetControlItem();
		if(controlItem == null)
			return;
		final int npcId = PetDataTable.getSummonId(controlItem);
		if(npcId == 0)
			return;
		final NpcTemplate petTemplate = NpcTable.getTemplate(npcId);
		if(petTemplate == null)
			return;
		PetInstance pet;
		if(PetData.isBaby(npcId))
			pet = PetBabyInstance.spawnPet(petTemplate, this, controlItem);
		else
			pet = PetInstance.spawnPet(petTemplate, this, controlItem);
		if(pet == null)
			return;
		setServitor(pet);
		pet.setTitle(Servitor.TITLE_BY_OWNER_NAME);
		if(!pet.isRespawned())
			try
			{
				pet.setCurrentHp(pet.getMaxHp(), false);
				pet.setCurrentMp(pet.getMaxMp());
				pet.setExp(pet.getExpForThisLevel());
				pet.setCurrentFed(pet.getMaxFed());
				pet.store();
			}
			catch(NullPointerException e)
			{
				_log.warn("PetSummon: failed set stats for summon " + npcId + ".");
				return;
			}
		pet.setReflectionId(getReflectionId());
		pet.spawnMe(Location.findPointToStay(getLoc(), 50, 70, getGeoIndex()));
		pet.broadcastPetInfo();
		pet.startFeed(false);
		pet.setRunning();
		pet.setFollowStatus(true, true);
		if(pet instanceof PetBabyInstance)
			((PetBabyInstance) pet).startHealTask();
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		final Servitor summon = getServitor();
		if(summon != null)
			summon.startAttackStanceTask0();
	}

	@Override
	public void displayGiveDamageMessage(final Creature target, final boolean crit, final boolean miss, final boolean magic)
	{
		super.displayGiveDamageMessage(target, crit, miss, magic);
		if(miss)
			sendPacket(Msg.MISSED_TARGET);
		else if(crit)
			if(magic)
				sendPacket(new SystemMessage(1280));
			else
				sendPacket(Msg.CRITICAL_HIT);
	}

	@Override
	public void displayReceiveDamageMessage(final Creature attacker, final int damage)
	{
		if(attacker != null && attacker != this)
		{
			final SystemMessage smsg = new SystemMessage(36);
			String name = attacker.getVisibleName(this);
			if(attacker.isNpc() && name.isEmpty())
				smsg.addNpcName(((NpcInstance) attacker).getTemplate().npcId);
			else
				smsg.addString(name);
			smsg.addNumber(Integer.valueOf(damage));
			sendPacket(smsg);
		}
	}

	public void closeEnchant()
	{
		if(getEnchantScroll() != null)
		{
			setEnchantScroll(null);
			sendPacket(EnchantResult.SUCCESS);
		}
	}

	public boolean classWindow()
	{
		if(!Config.ALLOW_REMOTE_CLASS_MASTERS)
			return false;

		final int c = getClassId().getLevel();
		final int lvl = getLevel();
		if(lvl >= 20 && c == 1 || lvl >= 40 && c == 2 || lvl >= 76 && c == 3)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("custom/31860.htm");
			sendPacket(html);
			return true;
		}
		return false;
	}

	@Override
	public PlayerListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new PlayerListenerList(this);
			}
		return (PlayerListenerList) listeners;
	}

	@Override
	public PlayerStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new PlayerStatsChangeRecorder(this);
			}
		return (PlayerStatsChangeRecorder) _statsRecorder;
	}

	public void ask(final ConfirmDlg dlg, final OnAnswerListener listener)
	{
		if(_askDialog != null)
			return;
		final int rnd = Rnd.nextInt();
		_askDialog = new ImmutablePair<Integer, OnAnswerListener>(rnd, listener);
		dlg.setRequestId(rnd);
		sendPacket(dlg);
	}

	public Pair<Integer, OnAnswerListener> getAskListener(final boolean clear)
	{
		if(!clear)
			return _askDialog;
		final Pair<Integer, OnAnswerListener> ask = _askDialog;
		_askDialog = null;
		return ask;
	}

	@Override
	public boolean isDead()
	{
		return !isInOlympiadMode() && super.isDead();
	}

	public PartyRoom getPartyRoom()
	{
		return _partyRoom;
	}

	public void setPartyRoom(final PartyRoom room)
	{
		_partyRoom = room;
	}

	public boolean isCanUseSelectedSub()
	{
		return _canUseSelectedSub;
	}

	public void setCanUseSelectedSub(final boolean is)
	{
		_canUseSelectedSub = is;
	}

	public void tryOpenPrivateStore(final boolean sell, final boolean sellPackage)
	{
		if(getSittingTask())
		{
			sendActionFailed();
			return;
		}
		if(isInStoreMode())
		{
			setPrivateStoreType((short) 0);
			standUp();
			broadcastUserInfo(false);
		}
		if(isInTransaction())
			getTransaction().cancel();
		if(getTradeList() != null)
		{
			getTradeList().removeAll();
			sendPacket(new SendTradeDone(0));
		}
		else
			setTradeList(new TradeList(0));
		if(sell)
			getTradeList().updateSellList(this, getSellList());
		else
			getTradeList().updateBuyList(this, getBuyList());
		if(!checksForShop(false))
		{
			sendActionFailed();
			return;
		}
		if(sell)
			sendPacket(new PrivateStoreManageList(this, sellPackage));
		else
			sendPacket(new PrivateStoreManageListBuy(this));
	}

	public void broadcastPrivateStoreMsg(final int type)
	{
		if(!isVisible())
			return;
		final List<Player> players = World.getAroundPlayers(this);
		if(type == 1)
		{
			sendPacket(new PrivateStoreMsgBuy(this, false));
			for(int i = 0; i < players.size(); ++i)
			{
				final Player target = players.get(i);
				target.sendPacket(new PrivateStoreMsgBuy(this, !isSameHWID(target.getHWID())));
			}
		}
		else if(type == 2)
		{
			sendPacket(new PrivateStoreMsgSell(this, false));
			for(int i = 0; i < players.size(); ++i)
			{
				final Player target = players.get(i);
				target.sendPacket(new PrivateStoreMsgSell(this, !isSameHWID(target.getHWID())));
			}
		}
		else
		{
			sendPacket(new RecipeShopMsg(this, false));
			for(int i = 0; i < players.size(); ++i)
			{
				final Player target = players.get(i);
				target.sendPacket(new RecipeShopMsg(this, !isSameHWID(target.getHWID())));
			}
		}
	}

	@Override
	public void disableSkill(final Skill skill, final long delay)
	{
		super.disableSkill(skill, delay);
		if(skill.getReuseGroupId() > 0 && !skill.isHandler())
			sendPacket(new SkillCoolTime(this));
	}

	public List<Skill> getGSkills(final int id)
	{
		return _gskills.containsKey(id) ? _gskills.get(id) : Collections.emptyList();
	}

	private int getElixCP()
	{
		final int lvl = getLevel();
		if(lvl > 75)
			return 8639;
		if(lvl >= 61)
			return 8638;
		if(lvl >= 52)
			return 8637;
		if(lvl >= 40)
			return 8636;
		if(lvl >= 20)
			return 8635;
		return 8634;
	}

	private int getElixHP()
	{
		final int lvl = getLevel();
		if(lvl > 75)
			return 8627;
		if(lvl >= 61)
			return 8626;
		if(lvl >= 52)
			return 8625;
		if(lvl >= 40)
			return 8624;
		if(lvl >= 20)
			return 8623;
		return 8622;
	}

	private int getElixLvl()
	{
		final int lvl = getLevel();
		if(lvl > 75)
			return 6;
		if(lvl >= 61)
			return 5;
		if(lvl >= 52)
			return 4;
		if(lvl >= 40)
			return 3;
		if(lvl >= 20)
			return 2;
		return 1;
	}

	public boolean inEvent()
	{
		return inEvent || inLH;
	}

	public void setVIP(final boolean f)
	{
		_vip = f;
	}

	@Override
	public boolean isVIP()
	{
		return _vip;
	}

	public int getBriefItem()
	{
		return _bbsMailItem;
	}

	public void setBriefItem(final int obj)
	{
		_bbsMailItem = obj;
	}

	public void setBriefSender(final String sender)
	{
		_bbsMailSender = sender;
	}

	public String getBriefSender()
	{
		return _bbsMailSender;
	}

	public void setMailTheme(final String sender)
	{
		_bbsMailTheme = sender;
	}

	public String getMailTheme()
	{
		return _bbsMailTheme;
	}

	public void setPrivateStoreCurrecy(int value)
	{
		_privateStoreCurrecy = value;
	}

	public int getPrivateStoreCurrecy()
	{
		return _privateStoreCurrecy;
	}

	public static void loadBots()
	{}

	public static void startSpawnBots(final long time)
	{}

	public static void stopSpawnBots()
	{}

	public static void clearBotsAi()
	{}

	public static void parseBotsAi()
	{}

	public static void parseBotsFile(final int v)
	{}

	public void writeBot(final boolean start)
	{}

	public void recBot(final int sn, final int s1, final int s2, final int s3, final int s4, final int s5, final int s6)
	{}

	@Override
	public final String getVisibleName(Player receiver)
	{
		if(isCursedWeaponEquipped())
		{
			String cursedName = getCursedWeaponName(receiver);
			if(cursedName == null || cursedName.isEmpty())
				return getName();

			return cursedName;
		}
		return getName();
	}

	@Override
	public final String getVisibleTitle(Player receiver)
	{
		if(isCursedWeaponEquipped())
			return "";

		if(getPrivateStoreCurrecy() > 0)
		{
			switch(getPrivateStoreType())
			{
				case STORE_PRIVATE_SELL:
				case STORE_PRIVATE_SELL_PACKAGE:
				case STORE_PRIVATE_BUY:
					return new CustomMessage("l2s.gameserver.model.Player.private_store_currecy").addItemName(getPrivateStoreCurrecy()).toString(receiver);
			}
		}
		if(inTvT && Config.TvT_ShowKills)
			return "Убийств: " + eventKills;
		return getTitle();
	}

	public final int getVisibleNameColor(Player receiver)
	{
		if(isInStoreMode())
		{
			if(isInOfflineMode())
				return Config.SERVICES_OFFLINE_TRADE_NAME_COLOR;
		}
		return getNameColor();
	}

	public final int getVisibleTitleColor(Player receiver)
	{
		if(getPrivateStoreCurrecy() > 0)
		{
			switch(getPrivateStoreType())
			{
				case STORE_PRIVATE_SELL:
					return Integer.decode("0xF5A2EA");
				case STORE_PRIVATE_SELL_PACKAGE:
					return Integer.decode("0x7573FC");
				case STORE_PRIVATE_BUY:
					return Integer.decode("0xA5FAFD");
			}
		}
		return getTitleColor();
	}


	public final boolean isPledgeVisible(Player receiver)
	{
		if(isCursedWeaponEquipped())
			return false;

		return true;
	}

	@Override
	public boolean useItem(ItemInstance item, boolean ctrl, boolean sendMsg)
	{
		if(!ItemFunctions.checkUseItem(this, item, sendMsg))
			return false;

		if(item.getTemplate().useItem(this, item, ctrl))
			return true;

		return false;
	}

	public OptionDataTemplate addOptionData(OptionDataTemplate optionData)
	{
		if(optionData == null)
			return null;

		OptionDataTemplate oldOptionData = _options.get(optionData.getId());
		if(optionData == oldOptionData)
			return oldOptionData;

		_options.put(optionData.getId(), optionData);

		addTriggers(optionData);
		addStatFuncs(optionData.getStatFuncs(optionData));

		for(Skill skill : optionData.getSkills())
			addSkill(skill);

		return oldOptionData;
	}

	public OptionDataTemplate removeOptionData(int id)
	{
		OptionDataTemplate oldOptionData = _options.remove(id);
		if(oldOptionData != null)
		{
			removeTriggers(oldOptionData);
			removeStatsOwner(oldOptionData);

			for(Skill skill : oldOptionData.getSkills())
				removeSkill(skill);
		}
		return oldOptionData;
	}

	private final Map<BanBindType, Pair<Integer, Future<?>>> banTasks = new HashMap<>();

	public boolean startBanEndTask(BanBindType bindType, int endTime) {
		Pair<Integer, Future<?>> taskInfo = banTasks.get(bindType);
		if(taskInfo != null) {
			if(taskInfo.getKey() == endTime)
				return false;

			Future<?> task = taskInfo.getValue();
			if(task != null)
				task.cancel(false);
		}

		Future<?> task = null;
		if(endTime != -1) {
			long delay = (endTime * 1000L) - System.currentTimeMillis();
			if (delay <= 0)
				return false;

			if (bindType == BanBindType.CHAT)
				task = ThreadPoolManager.getInstance().schedule(() -> GameBanManager.onUnban(bindType, getObjectId(), false), delay);

			if (task == null)
				return false;
		}
		banTasks.put(bindType, Pair.of(endTime, task));
		return true;
	}

	public boolean stopBanEndTask(BanBindType bindType) {
		Pair<Integer, Future<?>> taskInfo = banTasks.remove(bindType);
		if(taskInfo == null)
			return false;

		Future<?> task = taskInfo.getValue();
		if(task == null)
			return false;

		task.cancel(false);
		return true;
	}

	public void stopBanEndTasks() {
		for(Pair<Integer, Future<?>> taskInfo : banTasks.values()) {
			Future<?> task = taskInfo.getValue();
			if(task != null)
				task.cancel(false);
		}
		banTasks.clear();
	}

	//tw
	private boolean _isInTownWar = false;

	public void setIsInTownWarEvent(boolean val)
	{
		_isInTownWar = val;
	}

	public boolean isInTownWarEvent()
	{
		return _isInTownWar;
	}
}
