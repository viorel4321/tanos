package l2s.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import l2s.commons.time.cron.SchedulingPattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2s.commons.configuration.ExProperties;
import l2s.commons.net.nio.impl.SelectorConfig;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.PlayerAccess;
import l2s.gameserver.network.authcomm.ServerType;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.utils.AddonsConfig;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Util;
import l2s.gameserver.utils.velocity.VelocityVariable;

public class Config
{
	protected static Logger _log = LoggerFactory.getLogger(Config.class);

	public static final int NCPUS;
	public static boolean DEBUG;
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static boolean DATABASE_AUTOUPDATE;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIMEOUT;
	public static int DATABASE_IDLE_TEST_PERIOD;
	public static String ACCOUNTS_DRIVER;
	public static String ACCOUNTS_URL;
	public static String ACCOUNTS_LOGIN;
	public static String ACCOUNTS_PASSWORD;
	public static int ACCOUNTS_DB_MAX_CONNECTIONS;
	public static boolean LAZY_ITEM_UPDATE;
	public static boolean LAZY_ITEM_UPDATE_ALL;
	public static int LAZY_ITEM_UPDATE_TIME;
	public static int LAZY_ITEM_UPDATE_ALL_TIME;
	public static boolean PACKET_FLOOD_PROTECTOR;
	public static int USER_INFO_INTERVAL;
	public static int BROADCAST_CHAR_INFO_INTERVAL;
	public static int EFFECT_TASK_MANAGER_COUNT;
	public static int MAXIMUM_ONLINE_USERS;
	public static int MAX_ITEMS;
	public static boolean AUTH_SERVER_GM_ONLY;
	public static boolean AUTH_SERVER_BRACKETS;
	public static boolean AUTH_SERVER_IS_PVP;
	public static boolean AUTH_SERVER_CLOCK;
	public static boolean AUTH_SERVER_TEST;
	public static int AUTH_SERVER_AGE_LIMIT;
	public static int AUTH_SERVER_SERVER_TYPE;

	public static boolean DONTLOADSPAWN;
	public static boolean DONTLOADQUEST;

	public static int SHIFT_BY;
	public static int SHIFT_BY_Z;
	public static int MAP_MIN_Z;
	public static int MAP_MAX_Z;

	public static boolean JAVA_SCRIPTS;
	public static int CHAT_MESSAGE_MAX_LEN;
	public static boolean MAT_BANCHAT;
	public static int[] BAN_CHANNEL_LIST;
	public static boolean MAT_REPLACE;
	public static String MAT_REPLACE_STRING;
	public static int UNCHATBANTIME;
	public static int SHOUT_CHAT_MIN_LVL;
	public static int TRADE_CHAT_MIN_LVL;
	public static int TELL_CHAT_MIN_LVL;
	public static int ALL_CHAT_MIN_LVL;
	public static int TELL_DELAY_LEVEL;
	public static int TELL_DELAY_TIME;
	public static int SHOUT_CHAT_DELAY;
	public static int TRADE_CHAT_DELAY;
	public static int HERO_CHAT_DELAY;
	public static int ALL_CHAT_RANGE;
	public static boolean NO_TS_JAILED;
	public static boolean NO_TELL_JAILED;
	public static Pattern[] MAT_LIST;
	public static boolean MAT_ANNOUNCE;
	public static boolean MAT_ANNOUNCE_NICK;
	public static int GLOBAL_CHAT;
	public static int GLOBAL_TRADE_CHAT;
	public static int SHOUT_CHAT_MODE;
	public static int TRADE_CHAT_MODE;
	public static int CHAT_RANGE_FIRST_MODE;
	public static boolean LOG_CHAT;
	public static boolean SPAM_FILTER;
	public static boolean SPAM_PS_WORK;
	public static int[] SPAM_CHANNELS;
	public static boolean SPAM_SKIP_SYMBOLS;
	public static int SPAM_COUNT;
	public static int SPAM_TIME;
	public static int SPAM_BLOCK_TIME;
	public static boolean SPAM_BAN_HWID;
	public static int SPAM_BAN_HWID_MIN;
	public static int SPAM_BAN_HWID_MAX;
	public static boolean SPAM_MESSAGE;
	public static int SPAM_MESSAGE_COUNT;
	public static int SPAM_MESSAGE_TIME;
	public static int SPAM_MESSAGE_BLOCK_TIME;
	public static boolean SPAM_MESSAGE_SAME;
	public static int[] SPAM_MESSAGE_CHANNELS;
	public static Pattern[] SPAM_LIST;
	public static boolean SAVING_SPS;
	public static boolean MANAHEAL_SPS_BONUS;
	public static int ALT_ADD_RECIPES;
	public static boolean ALT_100_RECIPES_B;
	public static boolean ALT_100_RECIPES_A;
	public static boolean ALT_100_RECIPES_S;
	public static int ALT_MAX_ALLY_SIZE;
	public static int ALT_PARTY_DISTRIBUTION_RANGE;
	public static int ALT_PARTY_LVL_DIFF;
	public static int PARTY_QUEST_ITEMS_RANGE;
	public static int PARTY_QUEST_ITEMS_RANGE_Z;
	public static float[] ALT_PARTY_BONUS;
	public static float[] ALT_PARTY_QUEST_BONUS;
	public static long HoursDissolveClan;
	public static long HoursBeforeCreateClan;
	public static long HoursBeforeJoinAClan;
	public static boolean PENALTY_BY_CLAN_DISMISS;
	public static long HoursBeforeInviteClan;
	public static long HoursBeforeJoinAlly;
	public static long HoursBeforeInviteAlly;
	public static int AltClanMembersForWar;
	public static int AltMinClanLvlForWar;
	public static int AltClanWarMax;
	public static boolean NO_COMBAT_STOP_CLAN_WAR;
	public static long DaysBeforeCreateNewAllyWhenDissolved;
	public static int MinLevelToCreatePledge;
	public static int ADENA_FOR_LEVEL_1;
	public static int ADENA_FOR_LEVEL_2;
	public static int MEMBERS_FOR_LEVEL_6;
	public static int MEMBERS_FOR_LEVEL_7;
	public static int MEMBERS_FOR_LEVEL_8;
	public static int REP_FOR_LEVEL_6;
	public static int REP_FOR_LEVEL_7;
	public static int REP_FOR_LEVEL_8;
	public static int MAX_CLAN_MEMBERS;
	public static int ACADEMY_LEAVE_LVL;
	public static int ROYAL_REP;
	public static int KNIGHT_REP;
	public static boolean DISABLE_ACADEMY;
	public static boolean DISABLE_ROYAL;
	public static boolean DISABLE_KNIGHT;
	public static List<Integer> BlockBuffList;
	public static List<Integer> NoBlockBuffInOly;
	public static boolean KILL_COUNTER;
	public static boolean KILL_COUNTER_PRELOAD;
	public static boolean DROP_COUNTER;
	public static boolean CRAFT_COUNTER;
	public static int SCHEDULED_THREAD_POOL_SIZE;
	public static int EXECUTOR_THREAD_POOL_SIZE;
	public static boolean ENABLE_RUNNABLE_STATS;
	public static SelectorConfig SELECTOR_CONFIG;
	public static boolean AUTO_LOOT_INDIVIDUAL_ADENA;
	public static boolean AUTO_LOOT_INDIVIDUAL_ITEMS;
	public static boolean AUTO_LOOT_INDIVIDUAL_HERBS;
	public static boolean AUTO_LOOT_INDIVIDUAL_LIST;
	public static boolean AUTO_LOOT_ITEMS;
	public static boolean AUTO_LOOT_ADENA;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_FROM_RAIDS;
	public static int[] DROP_ITEMS_GR;
	public static int[] AUTO_LOOT_LIST;
	public static boolean AUTO_LOOT_PK;
	public static String APASSWD_TEMPLATE;
	public static boolean OFF_NAME_LENGTH;
	public static String CNAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static Pattern CNAME_DENY_PATTERN;
	public static int CNAME_MAXLEN;
	public static String CLAN_NAME_TEMPLATE;
	public static String CLAN_TITLE_TEMPLATE;
	public static String ALLY_NAME_TEMPLATE;
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean ALT_GAME_MATHERIALSDROP;
	public static boolean ALT_DOUBLE_SPAWN;
	public static boolean AUGMENT_STATIC_REUSE;
	public static boolean ALT_GAME_UNREGISTER_RECIPE;
	public static int SS_ANNOUNCE_PERIOD;
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ALT_GAME_SHOW_DROPLIST;
	public static boolean ALT_GAME_GEN_DROPLIST_ON_DEMAND;
	public static boolean SHOW_DROPLIST_NPCID;
	public static boolean ALT_FULL_NPC_STATS_PAGE;
	public static boolean ALLOW_NPC_SHIFTCLICK;
	public static boolean ALLOW_VOICED_COMMANDS;
	public static boolean ALLOW_AUTOHEAL_COMMANDS;
	public static boolean SHOW_HTML_WELCOME;
	public static boolean ALT_MAGICFAILURES;
	public static int MAGICFAIL_DIFF;
	public static double MAGICFAIL_MOD;
	public static double MAGICRESIST_MOD;
	public static double MAGIC_DAMAGE;
	public static double CRIT_DAMAGE_MAGIC;
	public static double MCRIT_MOD;
	public static boolean MCRIT_MYINFO;
	public static double SKILLS_CHANCE_CAP;
	public static double SKILLS_CHANCE_MIN;
	public static double SKILLS_MOB_CHANCE;
	public static boolean SKILLS_SHOW_CHANCE;
	public static boolean SKILLS_STOP_ACTOR;
	public static boolean CHARGE_DAM_C4;
	public static boolean CHARGE_DAM_C4_OUTSIDE_OLY;
	public static double CHARGE_DAM_OLY;
	public static double BLOW_DAM_OUTSIDE_OLY;
	public static boolean CONTROL_HEADING;
	public static double CRIT_DAM_BOW_PVP;
	public static int SKILLS_CAST_TIME_MIN;
	public static int CAST_INTERRUPT_TIME_ADD;
	public static boolean ALT_TOGGLE;
	public static int START_FORCE_EFFECT;
	public static int ATTACK_DELAY_MIN;
	public static int ATTACK_DELAY_BOW_MIN;
	public static int SKILL_PACKET_DELAY;
	public static int MAX_SKILL_PACKETS;
	public static int SKILL_USE_DELAY;
	public static int ITEM_PACKET_DELAY;
	public static int MAX_ITEM_PACKETS;
	public static int ITEM_USE_DELAY;
	public static int ATTACK_END_CORRECT;
	public static boolean OFF_AUTOATTACK;
	public static int ATTACK_RANGE_ADD;
	public static int ATTACK_RANGE_ARRIVED_ADD;
	public static int CAST_RANGE_ADD;
	public static int CAST_RANGE_ARRIVED_ADD;
	public static boolean CHECK_EPIC_CAN_DAMAGE;
	public static boolean CAST_CHECK;
	public static boolean NEXT_CAST_CHECK;
	public static int LIM_CP;
	public static int LIM_HP;
	public static int LIM_MP;
	public static int LIM_MATK_SPD;
	public static int LIM_PATK_SPD;
	public static int LIM_CRIT;
	public static int LIM_MCRIT;
	public static short LIM_HENNA_STAT;
	public static double POLE_DAMAGE_MODIFIER;
	public static double POLE_BASE_ANGLE;
	public static double POLE_BASE_TC;
	public static double[] POLE_VAMPIRIC_MOD;
	public static boolean ALT_SHOW_REUSE_MSG;
	public static boolean DEL_AUGMENT_BUFFS;
	public static boolean ALT_SHOW_CRIT_MSG;
	public static boolean ALT_SOCIAL_ACTION_REUSE;
	public static boolean ALT_DISABLE_SPELLBOOKS;
	public static boolean ALT_DISABLE_EGGS;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean ALT_ARENA_EXP;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
	public static int ALT_GAME_SUB_ADD;
	public static boolean ALT_GAME_ANY_SUBCLASS;
	public static boolean ANY_SUBCLASS_MASTER;
	public static boolean NO_HERO_SKILLS_SUB;
	public static int CLASS_CHANGE_DELAY;
	public static boolean ALT_NO_LASTHIT;
	public static boolean DROP_LASTHIT;
	public static String CHAR_TITLE;
	public static boolean NOBLE_KILL_RB;
	public static boolean ALT_SIMPLE_SIGNS;
	public static int ALT_MAMMOTH_EXCHANGE;
	public static int ALT_MAMMOTH_UPGRADE;
	public static int[] ALT_MAMMOTH_HARDCODE;
	public static int ALT_BUFF_LIMIT;
	public static int DEBUFF_LIMIT;
	public static int TRIGGER_LIMIT;
	public static boolean SAVE_EFFECTS_AFTER_DEATH;
	public static int MULTISELL_SIZE;
	public static boolean MULTISELL_PTS;
	public static int MULTISELL_MAX_AMOUNT;
	public static int[] ALT_REF_MULTISELL;
	public static boolean MULTISELL_WARN;
	public static boolean ALLOW_MARKUP;
	public static int DIVIDER_SELL;
	public static int DIVIDER_PRICES;
	public static double HENNA_PRICE_MOD;
	public static double AUGMENT_CANCEL_PRICE_MOD;
	public static boolean SERVICES_CHANGE_NICK_ENABLED;
	public static int SERVICES_CHANGE_NICK_PRICE;
	public static int SERVICES_CHANGE_NICK_ITEM;
	public static String SERVICES_CHANGE_NICK_TEMPLATE;
	public static String SERVICES_CHANGE_NICK_SYMBOLS;
	public static boolean SERVICES_CHANGE_SEX_ENABLED;
	public static int SERVICES_CHANGE_SEX_PRICE;
	public static int SERVICES_CHANGE_SEX_ITEM;
	public static int SERVICES_CHANGE_BASE_PRICE;
	public static int SERVICES_CHANGE_BASE_ITEM;
	public static boolean SERVICES_CHANGE_BASE_HERO;
	public static boolean SERVICES_CHANGE_PET_NAME_ENABLED;
	public static int SERVICES_CHANGE_PET_NAME_PRICE;
	public static int SERVICES_CHANGE_PET_NAME_ITEM;
	public static boolean SERVICES_CHANGE_NICK_COLOR_ENABLED;
	public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
	public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;
	public static int SERVICES_CHANGE_NICK_COLOR_BLACK;
	public static int SERVICES_CHANGE_TITLE_COLOR_PRICE;
	public static boolean SERVICES_CHANGE_TITLE;
	public static int SERVICES_CHANGE_TITLE_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_TITLE_COLOR_LIST;
	public static int SERVICES_CHANGE_TITLE_COLOR_BLACK;
	public static int SERVICES_CLAN_ITEM;
	public static String SERVICES_CLAN_PAGE_PATH;
	public static int SERVICES_CHANGE_CLAN_NAME_PRICE;
	public static int SERVICES_CLAN_SKILLS_PRICE;
	public static int SERVICES_CLAN_REP_COUNT;
	public static int SERVICES_CLAN_REP_PRICE;
	public static int SERVICES_CLAN_LVL1_PRICE;
	public static int SERVICES_CLAN_LVL2_PRICE;
	public static int SERVICES_CLAN_LVL3_PRICE;
	public static int SERVICES_CLAN_LVL4_PRICE;
	public static int SERVICES_CLAN_LVL5_PRICE;
	public static int SERVICES_CLAN_LVL6_PRICE;
	public static int SERVICES_CLAN_LVL7_PRICE;
	public static int SERVICES_CLAN_LVL8_PRICE;
	public static int TRANSFER_AUGMENT_ITEM;
	public static int TRANSFER_AUGMENT_PRICE;

	public static int AUGMENT_SERVICE_COST_ITEM_ID;
	public static long AUGMENT_SERVICE_COST_ITEM_COUNT;
	public static int[] AUGMENT_SERVICE_SKILLS_VARIATIONS_WARRIOR;
	public static int[] AUGMENT_SERVICE_STATS_VARIATIONS_WARRIOR;
	public static int[] AUGMENT_SERVICE_SKILLS_VARIATIONS_MAGE;
	public static int[] AUGMENT_SERVICE_STATS_VARIATIONS_MAGE;

	public static boolean SERVICES_HERO_STATUS_ENABLE;
	public static int[] SERVICES_HERO_STATUS_PRICE;
	public static boolean SERVICES_BASH_ENABLED;
	public static boolean SERVICES_BASH_SKIP_DOWNLOAD;
	public static int SERVICES_BASH_RELOAD_TIME;
	public static int SERVICES_RATE_TYPE;
	public static int[] SERVICES_RATE_BONUS_PRICE;
	public static int[] SERVICES_RATE_BONUS_ITEM;
	public static float[] SERVICES_RATE_BONUS_VALUE;
	public static int[] SERVICES_RATE_BONUS_DAYS;
	public static boolean SERVICES_RATE_BONUS_E_ENABLED;
	public static double SERVICES_RATE_BONUS_E_W;
	public static double SERVICES_RATE_BONUS_E_A;
	public static double SERVICES_RATE_BONUS_E_J;
	public static int SERVICES_RATE_BONUS_AS;
	public static boolean SERVICES_RATE_BONUS_NO_DROP_PK;
	public static boolean SERVICES_NOBLESS_SELL_ENABLED;
	public static int SERVICES_NOBLESS_SELL_PRICE;
	public static int SERVICES_NOBLESS_SELL_ITEM;
	public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
	public static int SERVICES_EXPAND_INVENTORY_PRICE;
	public static int SERVICES_EXPAND_INVENTORY_ITEM;
	public static Integer SERVICES_EXPAND_INVENTORY_MAX;
	public static int SERVICES_WASH_PK_ITEM;
	public static int SERVICES_WASH_PK_PRICE;
	public static int SERVICES_WASH_PK_COUNT;
	public static boolean SERVICES_HOW_TO_GET_COL;
	public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
	public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
	public static boolean SERVICES_ALLOW_OFFLINE_TRADE_NAME_COLOR;
	public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
	public static boolean SERVICES_OFFLINE_TRADE_KICK_NOT_TRADING;
	public static int SERVICES_OFFLINE_TRADE_PRICE;
	public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
	public static long SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
	public static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;

	@VelocityVariable
	public static boolean SERVICES_GIRAN_HARBOR_ENABLED;

	public static boolean SERVICES_CHANGE_PASSWORD;
	public static boolean BLOCK_EXP;
	public static boolean ALLOW_MY_INFO;
	public static boolean SERVICES_HERO_AURA;
	public static boolean SERVICES_LOCK_ACCOUNT_IP;
	public static boolean SERVICES_LOCK_CHAR_HWID;
	public static boolean SERVICES_LOCK_ACC_HWID;
	public static boolean SERVICES_CHAR_KEY;
	public static boolean SERVICES_CHAR_FORCED_KEY;
	public static boolean CHAR_KEY_SAVE_DB;
	public static int CHAR_KEY_FAIL_KICK;
	public static int CHAR_KEY_FAIL_BAN;
	public static boolean GM_CAN_SEE_CHAR_KEY;
	public static int CHAR_KEY_SAVE_DELAY;
	public static boolean CHAR_KEY_BACKUP;
	public static HashMap<Integer, String> CHAR_KEYS;
	public static Map<String, Integer> CHECKS;
	public static boolean SERVICES_ALLOW_ROULETTE;
	public static int SERVICES_ROULETTE_MIN_BET;
	public static int SERVICES_ROULETTE_MAX_BET;
	public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static int CLASS_GAME_MIN;
	public static int NONCLASS_GAME_MIN;
	public static int[] OLY_CLASSED_GAMES_DAYS;
	public static int OLY_MIN_REG_POINTS;
	public static int OLY_COMP_WIN_ANNOUNCE;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int[] OLY_MATCH_REWARD;
	public static int OLY_RETURN_TIME;
	public static boolean OLY_NO_SAME_IP;
	public static boolean OLY_NO_SAME_PC;
	public static int HERO_CLAN_REP;
	public static int[] HERO_ITEMS;
	public static int OLYMPIAD_POINTS_DEFAULT;
	public static int OLYMPIAD_POINTS_WEEKLY;
	public static int OLY_POINTS_HERO;
	public static int OLY_POINTS_MAX;
	public static int OLY_COMP_WIN_HERO;
	public static int OLY_COMP_DONE_HERO;
	public static boolean OLY_RANKING_PAST;
	public static boolean OLY_SORT_LIST;
	public static int OLY_SAVE_DELAY;
	public static int OLY_ZONE_CHECK;
	public static long ALT_OLY_CPERIOD;
	public static int ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static boolean OLY_END_WAIT_COMPS;
	public static boolean OLY_RENEWAL_BEGIN;
	public static boolean OLY_RENEWAL_END;
	public static boolean OLY_BUFFS_PTS;
	public static boolean OLY_RESET_CHARGES;
	public static boolean ALLOW_OLY_HENNA;
	public static int[] OLY_RESTRICTED_ITEMS;
	public static int[] OLY_RESTRICTED_SKILLS;
	public static int[] OLY_RESTRICTED_SUMMONS;
	public static boolean OLY_ENCHANT_LIMIT;
	public static int OLY_ENCHANT_LIMIT_WEAPON;
	public static int OLY_ENCHANT_LIMIT_ARMOR;
	public static boolean ENABLE_OLYMPIAD;
	public static boolean ENABLE_OLYMPIAD_SPECTATING;
	public static SchedulingPattern OLYMIAD_END_PERIOD_TIME;
	public static SchedulingPattern OLYMPIAD_START_TIME;
	public static boolean ENABLE_DECOY;
	public static int ALT_TRUE_CHESTS;
	public static int NONOWNER_ITEM_PICKUP_DELAY;
	public static int NONOWNER_ITEM_PICKUP_DELAY_BOSS;
	public static boolean NONOWNER_ITEM_PICKUP_PET;
	public static boolean LOG_KILLS;
	public static boolean LOG_ITEMS;
	public static HashMap<Integer, PlayerAccess> gmlist;
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_QUESTS_REWARD;
	public static float RATE_QUESTS_DROP;
	public static float RATE_QUESTS_DROP_PROF;
	public static boolean RATE_QUESTS_OCCUPATION_CHANGE;
	public static float RATE_CLAN_REP_SCORE;
	public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
	public static float RATE_DROP_ADENA;
	public static float RATE_DROP_ADENA_STATIC_MOD;
	public static boolean ADENA_SS;
	public static float RATE_DROP_ADENA_MULT_MOD;
	public static float RATE_DROP_ADENA_PARTY;
	public static float RATE_DROP_ITEMS_PARTY;
	public static float RATE_XP_PARTY;
	public static float RATE_SP_PARTY;
	public static float RATE_DROP_ITEMS;
	public static int[] EPICBOSS_IDS;
	public static float RATE_DROP_RAIDBOSS;
	public static float RATE_DROP_EPICBOSS;
	public static float RATE_DROP_BOX;
	public static float RATE_DROP_CHEST;
	public static float RATE_DROP_SPOIL;
	public static int RATE_BREAKPOINT;
	public static int MAX_DROP_ITERATIONS;
	public static boolean INTEGRAL_DROP;
	public static boolean ALT_SINGLE_DROP;
	public static int[] NO_RATE_ITEMS;
	public static boolean NO_RATE_RECIPES;
	public static double RATE_MANOR;
	public static float RATE_FISH_DROP_COUNT;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static double RATE_CLAN_POINTS_ACADEMY1;
	public static double RATE_CLAN_POINTS_ACADEMY2;
	public static int CLAN_WAR_POINTS;
	public static int CLAN_WAR_POINTS_MORE;
	public static int CLAN_WAR_POINTS_MIN;
	public static boolean RATE_PARTY_MIN;
	public static boolean SUMMON_EXP_SP_PARTY;
	public static boolean ALT_RATE_ADENA;
	public static float ALT_RATE_ADENA_NG;
	public static float ALT_RATE_ADENA_D;
	public static float ALT_RATE_ADENA_C;
	public static float ALT_RATE_ADENA_B;
	public static float ALT_RATE_ADENA_A;
	public static float ALT_RATE_ADENA_S;

	public static int RATE_MOB_SPAWN;
	public static int RATE_MOB_SPAWN_MIN_LEVEL;
	public static int RATE_MOB_SPAWN_MAX_LEVEL;

	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_NEEDED_TO_DROP;
	public static int KARMA_DROP_ITEM_LIMIT;
	public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;
	public static double KARMA_DROPCHANCE_BASE;
	public static double KARMA_DROPCHANCE_MOD;
	public static double NORMAL_DROPCHANCE_BASE;
	public static int DROPCHANCE_EQUIPMENT;
	public static int DROPCHANCE_EQUIPPED_WEAPON;
	public static int DROPCHANCE_ITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int DELETE_DAYS;
	public static int PURGE_BYPASS_TASK_FREQUENCY;

	/** Datapack root directory */
	public static File DATAPACK_ROOT;
	public static File GEODATA_ROOT;

	public static boolean WEAR_TEST_ENABLED;
	public static float MAXLOAD_MODIFIER;
	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_DISCARDITEM_GM;
	public static boolean ALLOW_FREIGHT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_PRIVATE_STORE;
	public static boolean ALLOW_MANUFACTURE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_BOAT;
	public static int BOAT_BROADCAST_RADIUS;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean DROP_CURSED_WEAPONS_ON_KICK;
	public static boolean RESET_CURSED_WEAPONS;
	public static boolean ALLOW_NOBLE_TP_TO_ALL;
	public static int SWIMING_SPEED;
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int HTM_CACHE_MODE;
	public static boolean HTM_SHAPE_ARABIC;

	public static boolean USE_CLIENT_LANG;
	public static Language DEFAULT_LANG;
	public static Set<Language> AVAILABLE_LANGUAGES;

	public static String COMMAND_LANG;
	public static int RESTART_TIME;
	public static int RESTART_AT_TIME;
	public static int RESTART_AT_MINS;
	public static int MOVE_PACKET_DELAY;
	public static int ATTACK_PACKET_DELAY;

	public static final String OTHER_CONFIG_FILE = "./config/other.properties";
	public static final String SPOIL_CONFIG_FILE = "./config/spoil.properties";
	public static final String ALT_SETTINGS_FILE = "./config/altsettings.properties";
	public static final String PVP_CONFIG_FILE = "./config/pvp.properties";
	public static final String CONFIGURATION_FILE = "./config/server.properties";
	public static final String RESIDENCE_CONFIG_FILE = "./config/residence.properties";
	public static final String AI_CONFIG_FILE = "./config/ai.properties";
	public static final String EVENTS_CONFIG_FILE = "./config/events.properties";
	public static final String SERVICES_FILE = "./config/services.properties";
	public static final String PVPCB_FILE = "./config/community.properties";
	public static final String ENCHANT_FILE = "./config/enchant.properties";
	public static final String GEODATA_CONFIG_FILE = "./config/geodata.properties";
	public static final String OLYMPIAD = "./config/olympiad.properties";
	public static final String RATES_FILE = "./config/rates.properties";
	public static final String FORMULAS_FILE = "./config/formulas.properties";
	public static final String CHAT_FILE = "./config/chat.properties";
	public static final String BOTS_FILE = "./config/Advanced/bots/bots.properties";

	public static final String TANOS_FILE = "./config/tanos.properties";

	public static final String GM_PERSONAL_ACCESS_FILE = "config/GMAccess.xml";
	public static final String GM_ACCESS_FILES_DIR = "config/GMAccess.d/";

	public static final String MAT_CONFIG_FILE = "./config/Advanced/abusewords.txt";
	public static final String SPAM_CONFIG_FILE = "./config/Advanced/spamwords.txt";

	public static boolean ALLOW_PVPCB_ABNORMAL;
	public static boolean PVPCB_ONLY_PEACE;
	public static boolean ALLOW_PVPCB_BUFFER;
	public static boolean PVPCB_BUFFER_PEACE;
	public static boolean NO_BUFF_EPIC;
	public static boolean ALLOW_PVPCB_SHOP;
	public static boolean ALLOW_PVPCB_SHOP_KARMA;
	public static int[] CB_MULTISELLS;
	public static boolean ALLOW_CB_ENCHANT;
	public static int CB_ENCH_ITEM;
	public static int[] CB_ENCHANT_LVL_WEAPON;

//	public static int[] ID_PREMIUM_BUFF_IN_PROFILES_USE;
	public static List<Integer> ID_PREMIUM_BUFF_IN_PROFILES_USE;
	public static int ITEM_ID_PREMIUM_BUFF_IN_BUFF_PROFILE;
	public static int ITEM_COUNT_PREMIUM_BUFF_IN_BUFF_PROFILE;
	public static int[] CB_ENCHANT_LVL_ARMOR;
	public static int[] CB_ENCHANT_PRICE_WEAPON;
	public static int[] CB_ENCHANT_PRICE_ARMOR;
	public static boolean ALLOW_PVPCB_STAT;
	public static int CB_STAT_LIMIT_TOP_PVP;
	public static int CB_STAT_LIMIT_TOP_PK;
	public static int CB_STAT_LIMIT_TOP_ONLINE;
	public static int CB_STAT_LIMIT_TOP_CLANS;
	public static int[] CB_STAT_CASTLES;
	public static boolean CB_STAT_ONLINE;
	public static int CB_STAT_TABLE_WIDTH;
	public static int CB_STAT_TD_WIDTH;
	public static boolean ALLOW_PVPCB_TELEPORT;
	public static long PVPCB_BUFFER_ALT_TIME;
	public static int PVPCB_BUFFER_PRICE_ITEM;
	public static int PVPCB_BUFFER_PRICE_ONE;
	public static int PVPCB_BUFFER_PRICE_GRP;
	public static int PVPCB_BUFFER_PRICE_GRP_ADENA;
	public static boolean ALLOW_DELUXE_BUFF;
	public static int DELUXE_BUFF_ITEM;
	public static int DELUXE_BUFF_COST;
	public static String DELUXE_BUFF_PAGE_PATH;
	public static int DELUXE_BUFF_PRICE;
	public static boolean DELUXE_BUFF_PREMIUM;
	public static int[] DELUXE_PREMIUM_PRICE;
	public static String DELUXE_PREMIUM_PAGE_PATH;
	public static boolean DELUXE_NPC_PAGE_AFTER_BUY;
	public static String DELUXE_PAGE_AFTER_BUY;
	private static int[] ALLOW_DELUXE_EFFECTS;
	public static boolean ALLOW_PB_COMMAND;
	public static int MAX_BUFF_SCHEM;
	public static String BUFF_SCHEM_NAME;
	private static int[] ALLOW_EFFECTS;
	public static int PVPCB_BUFFER_MIN_LVL;
	public static int PVPCB_BUFFER_MAX_LVL;
	public static boolean PVPCB_BUFFER_ALLOW_EVENT;
	public static boolean PVPCB_BUFFER_ALLOW_SIEGE;
	public static boolean PVPCB_BUFFER_ALLOW_PK;
	public static HashMap<Integer, Integer> CB_BUFFS;
	public static HashMap<Integer, Integer> DELUXE_BUFFS;
	public static int PVP_BBS_TELE_ITEM;
	public static int PVP_BBS_TELE_PRICE;
	public static int PVP_BBS_TELEPORT_ITEM;
	public static int PVP_BBS_TELEPORT_PRICE;
	public static boolean ALLOW_BBS_TELEPORT_SAVE;
	public static String BBS_TELEPORT_SAVE_NAME;
	public static boolean BBS_TELEPORT_PEACE_SAVE;
	public static int PVP_BBS_TELEPORT_SAVE_COUNT;
	public static boolean PVP_BBS_TELEPORT_ADDITIONAL_RULES;
	public static boolean PVP_BBS_TELEPORT_KARMA;
	public static boolean PVP_BBS_TELEPORT_PEACE;
	public static boolean PVP_BBS_TELEPORT_SIEGE;
	public static boolean NO_PVP_BBS_TELEPORT_EPIC;
	public static int PVP_BBS_TELEPORT_LVL;
	public static boolean TELEPORT_FILTER;
	public static List<String> TELEPORT_LIST_FILTER;
	public static boolean ALLOW_MAIL;
	public static int EXPOSTB_COIN;
	public static int EXPOSTB_PRICE;
	public static String EXPOSTB_NAME;
	public static int EXPOSTA_COIN;
	public static int EXPOSTA_PRICE;
	public static String EXPOSTA_NAME;
	public static boolean POST_CHARBRIEF;
	public static String POST_BRIEFTHEME;
	public static String POST_BRIEFTEXT;
	public static int POST_BRIEF_ITEM;
	public static int POST_BRIEF_COUNT;

	public static int REQUEST_ID;
	public static String EXTERNAL_HOSTNAME;
	public static int PORT_GAME;

	public static boolean SPAWN_CLASS_MASTERS;
	public static String ALLOW_CLASS_MASTERS;
	public static int[] CLASS_MASTERS_PRICE_ITEM;
	public static ArrayList<Integer> ALLOW_CLASS_MASTERS_LIST;
	public static int[] CLASS_MASTERS_PRICE_LIST;
	public static int[] CLASS_MASTERS_REWARD;
	public static int COUPONS_COUNT;
	public static boolean GET_CLASS_SOC;
	public static boolean ALLOW_REMOTE_CLASS_MASTERS;
	public static boolean EVER_BASE_CLASS;

	@VelocityVariable
	public static boolean ITEM_BROKER_ITEM_SEARCH;

	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static float BASE_SPOIL_RATE;
	public static float MINIMUM_SPOIL_RATE;
	public static boolean ALT_SPOIL_FORMULA;
	public static double MANOR_SOWING_BASIC_SUCCESS;
	public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
	public static double MANOR_HARVESTING_BASIC_SUCCESS;
	public static double MANOR_DIFF_PLAYER_TARGET;
	public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
	public static double MANOR_DIFF_SEED_TARGET;
	public static double MANOR_DIFF_SEED_TARGET_PENALTY;
	public static int KARMA_MIN_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_LOST_BASE;
	public static int MIN_PK_TO_ITEMS_DROP;
	public static boolean DROP_ITEMS_ON_DIE;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS;
	public static int PVP_TIME;
	public static boolean PVP_IP;
	public static boolean PVP_HWID;
	public static boolean PVP_KILLS;
	public static int[] PVP_KILLS_ZONES;
	public static boolean ALLOW_PVP_REWARD;
	public static int[] PVP_REWARD;
	public static int PVP_REWARD_LVL_DIFF;
	public static long PVP_REWARD_TIME;
	public static boolean ALLOW_PK_REWARD;
	public static int[] PK_REWARD;
	public static int PK_REWARD_LVL_DIFF;
	public static long PK_REWARD_TIME;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static int[] ALT_GAME_KARMA_NPC;
	public static boolean NO_DAMAGE_NPC;
	public static boolean SHOW_NPC_CREST;
	public static long DELAY_SPAWN_NPC;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static double ENCHANT_CHANCE_WEAPON;
	public static double ENCHANT_CHANCE_ARMOR;
	public static double ENCHANT_CHANCE_ACCESSORY;
	public static double ENCHANT_CHANCE_CRYSTAL_WEAPON;
	public static double ENCHANT_CHANCE_CRYSTAL_ARMOR;
	public static double ENCHANT_CHANCE_CRYSTAL_ACCESSORY;
	public static double ENCHANT_CHANCE_LIST;
	public static int[] ENCHANT_LIST;
	public static boolean USE_ALT_ENCHANT;
	public static ArrayList<Double> ALT_ENCHANT_WEAPON;
	public static ArrayList<Double> ALT_ENCHANT_ARMOR;
	public static ArrayList<Double> ALT_ENCHANT_JEWELRY;
	public static ArrayList<Double> ALT_ENCHANT_LIST;
	public static long ENCHANT_MAX_WEAPON;
	public static long ENCHANT_MAX_ACCESSORY;
	public static long ENCHANT_MAX_ARMOR;
	public static boolean SAFE_ENCHANT;
	public static boolean SET_SAFE_ENCHANT;
	public static boolean CRYSTAL_BLESSED;
	public static boolean ENCHANT_HERO_WEAPON;
	public static boolean ENCHANT_BLESSED_HERO_WEAPON;
	public static boolean ALT_AUGMENT;
	public static int[] ALT_AUGMENT_HIGH_SKILLS;
	public static int[] ALT_AUGMENT_TOP_SKILLS;
	public static int AUGMENT_NG_SKILL_CHANCE;
	public static int AUGMENT_MID_SKILL_CHANCE;
	public static int AUGMENT_HIGH_SKILL_CHANCE;
	public static int AUGMENT_TOP_SKILL_CHANCE;
	public static int AUGMENT_CHANCE_STAT;
	public static int AUGMENT_NG_GLOW_CHANCE;
	public static int AUGMENT_MID_GLOW_CHANCE;
	public static int AUGMENT_HIGH_GLOW_CHANCE;
	public static int AUGMENT_TOP_GLOW_CHANCE;
	public static boolean REGEN_SIT_WAIT;
	public static int REGEN_SW_MP;
	public static int REGEN_SW_HP;
	public static float RATE_RAID_REGEN;
	public static int RAID_MAX_LEVEL_DIFF;
	public static boolean PARALIZE_ON_RAID_DIFF;
	public static boolean RAID_ONE_SPAWN;
	public static float ALT_PK_DEATH_RATE;
	public static byte STARTING_LEVEL;
	public static byte SUBCLASS_LEVEL;
	public static int STARTING_SP;
	public static int STARTING_ADENA;
	public static int[] START_QUESTS_COMPLETED;
	public static boolean EVER_NOBL;
	public static boolean BLESS_NOBL;
	public static byte CREATE_CLAN_LVL;
	public static int CREATE_CLAN_REP;
	public static boolean ALLOW_START_ITEMS;
	public static int[] START_ITEMS_MAGE;
	public static int[] START_ITEMS_FIGHTER;
	public static boolean ALLOW_START_ITEMS_ENCHANT;
	public static int START_ITEMS_ENCHANT_ARMOR;
	public static int START_ITEMS_ENCHANT_WEAPON;
	public static boolean ALLOW_START_BUFFS;
	public static int[] START_BUFFS_MAGE;
	public static int[] START_BUFFS_FIGHTER;
	public static int[] START_XYZ;
	public static int START_PA;
	public static float START_RATE_PA;
	public static boolean START_PA_CHECK_HWID;
	public static boolean START_PA_CHECK_IP;
	public static boolean INFINITY_SS;
	public static boolean INFINITY_BEAST_SS;
	public static boolean INFINITY_ARROW;
	public static boolean ALLOW_PVP_ZONES_MOD;
	public static int[] PVP_ZONES_MOD;
	public static boolean ALLOW_WINGS_MOD;
	public static boolean ENGRAVE_SYSTEM;
	public static boolean ATTACK_ANIM_MOD;
	public static int ATTACK_ANIM_DELAY;
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;
	public static boolean UNSTUCK_SKILL;
	public static int UNSTUCK_TOWN;
	public static int TO_TOWN;
	public static boolean NO_TO_TOWN_PK;
	public static boolean NO_SUMMON_KARMA;
	public static boolean ALLOW_RCM;
	public static int DELAY_RCM;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static int MAX_PVTCRAFT_SLOTS;
	public static boolean RWHO_ENABLED;
	public static boolean SHOW_OFFLINE_MODE_IN_ONLINE;
	public static boolean ALLOW_RESIDENCE_DOOR_OPEN_ON_CLICK;
	public static boolean ALT_CH_ALL_BUFFS;
	public static boolean ALT_CH_ALLOW_1H_BUFFS;
	public static boolean ADV_NEWBIE_BUFF;
	public static int[] HS_DISEASE;
	public static boolean ENABLE_FORBIDDEN_BOW_CLASSES;
	public static int[] FORBIDDEN_BOW_CLASSES;
	public static boolean ALT_SALVATION;
	public static boolean USE_BREAK_FAKEDEATH;
	public static boolean GATEKEEPER_TELEPORT_SIEGE;
	public static float GATEKEEPER_MODIFIER;
	public static int GATEKEEPER_FREE;
	public static int CRUMA_GATEKEEPER_LVL;
	public static int TELEPORT_PROTECT;
	public static boolean VALID_TELEPORT;
	public static double CHAMPION_CHANCE1;
	public static double CHAMPION_CHANCE2;
	public static boolean CHAMPION_CAN_BE_AGGRO;
	public static boolean CHAMPION_CAN_BE_SOCIAL;
	public static boolean CHAMPION_FEAR_IMMUNE;
	public static boolean CHAMPION_PARALYZE_IMMUNE;
	public static boolean CHAMPION_DROP_HERBS;
	public static int CHAMPION_TOP_LEVEL;
	public static int CHAMPION_MIN_LEVEL;
	public static boolean CHAMPION_DROP_ONLY_ADENA;
	public static float RATE_CHAMPION_DROP_ADENA;
	public static boolean CHAMPION_REWARD;
	public static int[] CHAMPION_REWARD_LIST1;
	public static int[] CHAMPION_REWARD_LIST2;
	public static int CHAMPION_REWARD_DIFF;
	public static long QUEST_KILL_DELAY;
	public static boolean QUEST_KILL_REWARD_DEAD;
	public static double FIRST_BLOODED_FABRIC;
	public static int RESTORE_CANCEL_BUFFS;
	public static int REVIVE_TIME;
	public static boolean VISIBLE_SIEGE_ICONS;
	public static boolean NO_RES_SIEGE;
	public static boolean ATTACKERS_ALLY_FIRST_STEP_SIEGE;
	public static boolean OSWEOC;
	public static boolean ALLOW_PETS_ACTION_SKILLS;
	public static Map<Integer, Integer> PETS_ACTION_SKILLS;
	public static int CH_AUCTION_MINCLANLEVEL;
	public static int CH_AUCTION_BID_ID;
	public static long CH_AUCTION_MAX_BID;
	public static double RESIDENCE_LEASE_FUNC_MULTIPLIER;
	public static double RESIDENCE_LEASE_MULTIPLIER;
	public static boolean RESIDENCE_BUFFS_COST_MP;
	public static int[] CASTLE_SELECT_HOURS;
	public static boolean CASTLE_GENERATE_TIME_ALTERNATIVE;
	public static long CASTLE_GENERATE_TIME_LOW;
	public static long CASTLE_GENERATE_TIME_HIGH;
	public static int[] CASTLE_HIGH_LIST;
	public static int CASTLE_SIEGE_WEEKS;
	public static int[] GLUDIO_REWARD;
	public static int[] DION_REWARD;
	public static int[] GIRAN_REWARD;
	public static int[] OREN_REWARD;
	public static int[] ADEN_REWARD;
	public static int[] INNADRIL_REWARD;
	public static int[] GODDARD_REWARD;
	public static int[] RUNE_REWARD;
	public static int[] SCHUTTGART_REWARD;
	public static boolean ENABLE_BOTS;
	public static boolean WRITE_BOTS_AI;
	public static boolean BOTS_AI_FILE;
	public static boolean BOTS_AI_LOAD_AGAIN;
	public static int BOTS_AI_REC_TIME;
	public static int BOTS_AI_MAX_AS;
	public static int BOTS_AI_MIN_AS;
	public static long BOTS_AI_MAX_TIME;
	public static int BOTS_MIN_LVL;
	public static int BOTS_MAX_LVL;
	public static boolean BOTS_NO_WRITE_HERO;
	public static boolean BOTS_NO_WRITE_GM;
	public static boolean BOTS_SPAWN;
	public static int BOTS_SPAWN_TYPE;
	public static int BOTS_SPAWN_INTERVAL;
	public static int BOTS_NEXT_SPAWN_INTERVAL;
	public static int BOTS_SPAWN_COUNT;
	public static boolean BOTS_SPAWN_KEEP;
	public static boolean BOTS_START_LOC_RND;
	public static boolean BOTS_SPAWN_AI_RND;
	public static boolean BOTS_DELETE;
	public static boolean BOTS_NAMES_LOAD_AGAIN;
	public static boolean BOTS_NAME_RND;
	public static boolean BOTS_USED_NAMES;
	public static int[] BOTS_RT_ZONES;
	public static int[] BOTS_RT_EQUIP;
	public static int[] BOTS_RT_SKILLS;
	public static int[] BOTS_START_ITEMS;
	public static boolean BOTS_BUFFS;
	public static int[] BOTS_BUFFS_MAGE;
	public static int[] BOTS_BUFFS_FIGHTER;
	public static int BOTS_ENCHANT_MAX;
	public static int BOTS_SP_INT_MIN;
	public static int BOTS_SP_INT_MAX;
	public static int BOTS_UNSP_INT_MIN;
	public static int BOTS_UNSP_INT_MAX;
	public static int BOTS_FA_MIN;
	public static int BOTS_FA_MAX;
	public static int BOTS_LC_MIN;
	public static int BOTS_LC_MAX;
	public static boolean BOTS_NOBLE;
	public static String BOTS_TITLE;
	public static double BOTS_TITLE_CHANCE;
	public static double BOTS_FEMALE;
	public static boolean BOTS_SORT;
	public static boolean BOTS_CAN_JOIN_CLAN;
	public static double BOTS_CHANCE_JOIN_CLAN;
	public static double BOTS_CHANCE_REFUSE_CLAN;
	public static boolean BOTS_CAN_JOIN_PARTY;
	public static double BOTS_CHANCE_JOIN_PARTY;
	public static double BOTS_CHANCE_REFUSE_PARTY;
	public static boolean BOTS_WRITE_ATTACK;
	public static boolean BOTS_STOP_ACT;
	public static boolean BOTS_CAN_SAY;
	public static double BOTS_SAY_CHANCE;
	public static double BOTS_SHOUT_CHANCE;
	public static boolean BOTS_REMOVE_SAY;
	public static boolean BOTS_SAY_RND;
	public static String BOTS_ACC;
	public static boolean L2WALKER_PROTECTION;
	public static int BUGUSER_PUNISH;
	public static int DEFAULT_PUNISH;
	public static int MAX_FAILED_PACKETS;
	public static int MAX_UNKNOWN_PACKETS;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static boolean ADDON;
	public static int MASK_HWID;
	public static boolean NO_PICK_UP_MSG;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ANNOUNCE_RB;
	public static int TAMED_X05;
	public static int TAMED_X1;
	public static int TAMED_X2;
	public static boolean CLASS_EXP;
	public static int SC_AGGRO_RANGE;
	public static int[] SC_RANDOM_LEVELING;
	public static double SC_LEVEL_CHANCE;
	public static long MAX_PLAYER_CONTR;
	public static int GM_NAME_COLOUR;
	public static boolean GM_HERO_AURA;
	public static String InvulEffect;
	public static boolean COMMAND_STATUS_GM;
	public static boolean DON_LOG;
	public static int DON_ITEM_LOG;
	public static int DON_MIN_COUNT_LOG;
	public static int NORMAL_NAME_COLOUR;
	public static int CLANLEADER_NAME_COLOUR;
	public static boolean RND_WALK;
	public static int RND_WALK_RATE;
	public static int RND_ANIMATION_RATE;
	public static int AGGRO_CHECK_INTERVAL;
	public static int AGGRO_CHECK_RADIUS;
	public static int AGGRO_CHECK_HEIGHT;
	public static long GLOBAL_AGGRO;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static long HATE_TIME;
	public static int NPC_SEE_SPELL_RANGE;
	public static int AI_TASK_MANAGER_COUNT;
	public static int AI_TASK_ATTACK_DELAY;
	public static int AI_TASK_ACTIVE_DELAY;
	public static boolean BLOCK_ACTIVE_TASKS;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static int MAX_DRIFT_RANGE;
	public static int MAX_PURSUE_UNDERGROUND_RANGE;
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;
	public static int FACTION_NOTIFY_INTERVAL;
	public static int MAX_ATTACK_TIMEOUT;
	public static int TELEPORT_TIMEOUT;
	public static int MAX_PATHFIND_FAILS;
	public static boolean NO_TELEPORT_TO_TARGET;
	public static boolean GEO_SP_LOC;
	public static int GEO_SP1;
	public static int GEO_SP2;
	public static boolean ALT_AI_KELTIRS;
	public static int MIN_RESP_RAID_FIGHTER;
	public static int MAX_RESP_RAID_FIGHTER;
	public static boolean SEVEN_SIGNS_CHECK;
	public static int BOSS_CAST_MIN_MP;
	public static int SAVE_BOSS_HP;
	public static int LETHAL_IMMUNE_HP;
	public static int[] ZAKEN_DOOR_TIME;
	public static int ZAKEN_TELEPORT_MIN_MP;
	public static boolean ZAKEN_CLEAR_ZONE;
	public static int FWA_FIXINTERVALOFANTHARAS;
	public static int FWA_RANDOMINTERVALOFANTHARAS;
	public static int FWA_APPTIMEOFANTHARAS;
	public static int ANTHARAS_LIMITUNTILSLEEP;
	public static int ANTHARAS_ACTIVITY_TIME;
	public static boolean ANTHARAS_CHECK_ANNIHILATED;
	public static int FWB_FIXINTERVALOFBAIUM;
	public static int FWB_RANDOMINTERVALOFBAIUM;
	public static int BAIUM_LIMITUNTILSLEEP;
	public static int BAIUM_ACTIVITY_TIME;
	public static boolean BAIUM_CHECK_ANNIHILATED;
	public static int BAIUM_STATUE_SPAWN_TIME;
	public static int FWF_FIXINTERVALOFFRINTEZZA;
	public static int FWF_RANDOMINTERVALOFFRINTEZZA;
	public static int FRINTEZZA_ACTIVITY_TIME;
	public static int FWS_FIXINTERVALOFSAILRENSPAWN;
	public static int FWS_RANDOMINTERVALOFSAILRENSPAWN;
	public static int SAILREN_ACTIVITY_TIME;
	public static boolean SAILREN_CHECK_ANNIHILATED;
	public static int FWV_FIXINTERVALOFVALAKAS;
	public static int FWV_RANDOMINTERVALOFVALAKAS;
	public static int FWV_APPTIMEOFVALAKAS;
	public static int VALAKAS_LIMITUNTILSLEEP;
	public static int VALAKAS_ACTIVITY_TIME;
	public static boolean VALAKAS_CHECK_ANNIHILATED;
	public static boolean VALAKAS_WOLVES_KILL;
	public static boolean VALAKAS_IXION_KILL;
	public static int NURSE_ANT_RESP;
	public static SchedulingPattern QUEEN_ANT_FIXRESP;
	public static SchedulingPattern CORE_FIXRESP;
	public static SchedulingPattern ORFEN_FIXRESP;
	public static SchedulingPattern ZAKEN_FIXRESP;
	public static SchedulingPattern BAIUM_FIXRESP;
	public static SchedulingPattern ANTHARAS_FIXRESP;
	public static SchedulingPattern VALAKAS_FIXRESP;
	public static SchedulingPattern FRINTEZZA_FIXRESP;
	public static SchedulingPattern SAILREN_FIXRESP;
	public static int BA_CHANCE;
	public static int BA_MIN;
	public static int BA_MAX;
	public static int FS_PARTY_MEM_COUNT;
	public static int FS_SPAWN;
	public static int SHADOW_SPAWN_DELAY;
	public static int LIT_PARTY_MIN;
	public static int LIT_PARTY_MEM;
	public static boolean MOBSLOOTERS;
	public static boolean ALT_DEATH_PENALTY;
	public static boolean ALLOW_DEATH_PENALTY_C5;
	public static int ALT_DEATH_PENALTY_C5_CHANCE;
	public static boolean ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY;
	public static int ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
	public static int ALT_DEATH_PENALTY_C5_KARMA_PENALTY;
	public static long DEATH_PENALTY_LOW_EXP;
	public static boolean DISABLE_EXPERTISE_PENALTY;
	public static boolean SHOW_GM_LOGIN;
	public static boolean SAVE_GM_EFFECTS;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean ALLOW_COMMUNITYBOARD;
	public static String COMMUNITYBOARD_HTML_ROOT;
	public static String BBS_DEFAULT;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static int SAFE_ENCHANT_COMMON;
	public static int SAFE_ENCHANT_FULL_BODY;
	public static boolean ALT_ALLOW_SHADOW_WEAPONS;
	public static int[] ALT_SHOP_PRICE_LIMITS;
	public static int[] ALT_SHOP_UNALLOWED_ITEMS;
	public static int FESTIVAL_MIN_PARTY_SIZE;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME;
	public static int RIFT_AUTO_JUMPS_TIME_RAND;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static boolean ALLOW_JUMP_BOSS;
	public static int BOSS_ROOM_CHANCE;
	public static boolean ALLOW_TALK_WHILE_SITTING;
	public static boolean PARTY_LEADER_ONLY_CAN_INVITE;
	public static boolean ALLOW_CLANSKILLS;
	public static int REMOVE_SK_ON_DELEVEL;
	public static boolean ALLOW_MANOR;
	public static int MANOR_REFRESH_TIME;
	public static int MANOR_REFRESH_MIN;
	public static int MANOR_APPROVE_TIME;
	public static int MANOR_APPROVE_MIN;
	public static int MANOR_MAINTENANCE_PERIOD;
	public static boolean MANOR_SAVE_ALL_ACTIONS;
	public static int SERVICES_Buffer_Id;
	public static long BUFFER_BUFFS_TIME;
	public static int SERVICES_Min_lvl;
	public static int SERVICES_Max_lvl;
	public static int FREE_BUFFS_MAX_LVL;
	public static int HEAL_COIN;
	public static int HEAL_PRICE;
	public static int BUFF_ITEM_ONE;
	public static int BUFF_PRICE_ONE;
	public static int BUFF_ITEM_GRP;
	public static int BUFF_PICE_GRP;
	public static int BUFF_ITEM_GRP_COIN;
	public static int BUFF_PICE_GRP_COIN;
	public static boolean SERVICES_Buffer_Siege;
	public static boolean BUFFER_ALLOW_PK;
	public static boolean NO_BUFFER_EPIC;
	public static int[][] GROUP_BUFFS;
	private static int[] BUFFER_EFFECTS;
	public static boolean BUFFER_SAVE_RESTOR;
	public static int BUFFER_MAX_SCHEM;
	public static String BUFFER_SCHEM_NAME;
	public static HashMap<Integer, Integer> NPC_BUFFS;
	public static boolean SPAWN_FROM_CONFIG;
	public static int[][] SPAWN_NPC_FROM_CONFIG;
	public static int EVENT_CofferOfShadowsPriceRate;
	public static float EVENT_CofferOfShadowsRewardRate;
	public static boolean EVENT_ClassmastersSellsSS;
	public static boolean EVENT_ClassmastersCoLShop;
	public static int[] LastHero_Reward;
	public static int[] LastHero_RewardFinal;
	public static int LastHero_Time;
	public static int LastHero_Time_Paralyze;
	public static int LastHero_Time_Battle;
	public static boolean LastHero_Rate;
	public static boolean LastHero_IP;
	public static boolean LastHero_HWID;
	public static boolean LastHero_Instance;
	public static int LastHero_MinPlayers;
	public static int LastHero_MaxPlayers;
	public static boolean LastHero_Cancel;
	public static boolean LastHero_SetHero;
	public static int LastHero_HeroTime;
	public static boolean LastHero_RateFinal;
	public static int[] LastHero_ReturnPoint;
	public static boolean LastHero_Allow_Calendar_Day;
	public static int[] LastHero_Time_Start;
	public static int[] LH_RESTRICTED_ITEMS;
	public static int[] LH_RESTRICTED_SKILLS;
	public static int[] LH_BUFFS_FIGHTER;
	public static int[] LH_BUFFS_MAGE;
	public static String LastHero_Zone;
	public static String LastHero_Loc;
	public static String LastHero_ClearLoc;
	public static boolean LastHero_NoHero;
	public static int[] TvT_reward;
	public static int[] TvT_reward_final;
	public static int TvT_MinKills;
	public static boolean TvT_DrawReward;
	public static int[] TvT_reward_losers;
	public static int TvT_LosersMinKills;
	public static boolean TvT_IP;
	public static boolean TvT_HWID;
	public static boolean TvT_Instance;
	public static int TvT_MinPlayers;
	public static int TvT_MaxPlayers;
	public static boolean TvT_CancelAllBuff;
	public static boolean TvT_rate;
	public static int TvT_Time;
	public static int TvTResDelay;
	public static int TvT_Time_Paralyze;
	public static int TvT_Time_Battle;
	public static int[] TvT_ReturnPoint;
	public static int TvT_NonActionDelay;
	public static boolean TvT_ShowKills;
	public static boolean TvT_CustomItems;
	public static int TvT_CustomItemsEnchant;
	public static double TVT_CRIT_DAMAGE_MAGIC;
	public static Map<Integer, List<Integer>> TVT_CUSTOM_ITEMS;
	public static boolean TvT_Allow_Calendar_Day;
	public static int[] TvT_Time_Start;
	public static String TvT_Zone;
	public static String TvT_BlueTeamLoc;
	public static String TvT_RedTeamLoc;
	public static String TvT_BlueTeamResLoc;
	public static String TvT_RedTeamResLoc;
	public static String TvT_ClearLoc;
	public static int[] CtF_reward;
	public static int[] CtF_reward_final;
	public static int CtF_MinKills;
	public static boolean CtF_DrawReward;
	public static int[] CtF_reward_losers;
	public static int CtF_LosersMinKills;
	public static boolean CtF_IP;
	public static boolean CtF_HWID;
	public static int CtF_IP_Max;
	public static int CtF_HWID_Max;
	public static boolean CtF_Instance;
	public static int CtF_MinPlayers;
	public static int CtF_MaxPlayers;
	public static boolean CtF_CancelAllBuff;
	public static boolean CtF_rate;
	public static int CtF_Time;
	public static int CtFResDelay;
	public static int CtF_Flags;
	public static int CtF_Time_Paralyze;
	public static int CtF_Time_Battle;
	public static int[] CtF_ReturnPoint;
	public static boolean CtF_Allow_Calendar_Day;
	public static int[] CtF_Time_Start;
	public static String CtF_Zone;
	public static String CtF_BlueTeamLoc;
	public static String CtF_RedTeamLoc;
	public static String CtF_BlueTeamResLoc;
	public static String CtF_RedTeamResLoc;
	public static String CtF_BlueFlagLoc;
	public static String CtF_RedFlagLoc;
	public static String CtF_ClearLoc;
	public static int EVENTS_TIME_BACK;
	public static int[] EVENT_RESTRICTED_ITEMS;
	public static int[] EVENT_RESTRICTED_SKILLS;
	public static int[] EVENT_RESTRICTED_SUMMONS;
	public static int[] EVENT_BUFFS_FIGHTER;
	public static int[] EVENT_BUFFS_MAGE;
	public static boolean EVENT_NO_ASK;
	public static int GvG_Time_Prepare;
	public static int GvG_Time_Battle;
	public static int GvG_Time_Paralyze;
	public static int GvG_Min_Members;
	public static int GvG_Max_Members;
	public static int[] GvG_ItemIds;
	public static int[] GvG_MinBids;
	public static String GvG_Zone;
	public static String GvG_BlueTeamLoc;
	public static String GvG_RedTeamLoc;
	public static String GvG_ClearLoc;
	public static float TFH_POLLEN_CHANCE;
	public static int GLIT_MEDAL_CHANCE;
	public static int GLIT_GLITTMEDAL_CHANCE;
	public static boolean GLIT_EnableRate;
	public static float EVENT_L2DAY_LETTER_CHANCE;
	public static float EVENT_CHANGE_OF_HEART_CHANCE;
	public static boolean EVENT_BOUNTY_HUNTERS_ENABLED;
	public static boolean HITMAN_ENABLE;
	public static int HITMAN_ORDER_DAYS;
	public static int HITMAN_ITEM_ID;
	public static int HITMAN_MIN_ITEM;
	public static String HITMAN_ITEM_NAME;
	public static int HITMAN_ITEM_ID2;
	public static int HITMAN_MIN_ITEM2;
	public static String HITMAN_ITEM_NAME2;
	public static boolean HITMAN_REVENGE_ENABLE;
	public static int HITMAN_REVENGE_PERCENT;
	public static boolean HITMAN_EXECUTE_PVP;
	public static boolean HITMAN_EXECUTE_CLAN;
	public static int HITMAN_ORDER_LIMIT;
	public static int HITMAN_LOSS_CHANCE;
	public static boolean HITMAN_LOGGING_ENABLE;
	public static boolean HITMAN_ANNOUNCE_ENABLE;
	public static String HITMAN_ANNOUNCE_TEXT;
	public static int HITMAN_ANNOUNCE_TIME;
	public static boolean VIKTORINA_ENABLED;
	public static boolean VIKTORINA_REMOVE_QUESTION;
	public static boolean VIKTORINA_REMOVE_QUESTION_NO_ANSWER;
	public static int VIKTORINA_START_TIME_HOUR;
	public static int VIKTORINA_START_TIME_MIN;
	public static int VIKTORINA_WORK_TIME;
	public static int VIKTORINA_TIME_ANSER;
	public static int VIKTORINA_TIME_PAUSE;
	public static String VIKTORINA_REWARD_FIRST;
	public static String VIKTORINA_REWARD_OTHER;
	public static boolean PCBANG_POINTS_ENABLED;
	public static double PCBANG_POINTS_BONUS_DOUBLE_CHANCE;
	public static int PCBANG_POINTS_BONUS;
	public static int PCBANG_POINTS_DELAY;
	public static int PCBANG_POINTS_MIN_LVL;
	public static boolean FIRST_NOBLESS;
	public static int[] FIRST_NOBLESS_REWARD;
	public static boolean ALLOW_LOTO;
	public static boolean CUSTOM_BOX_DROP;
	public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
	public static float SERVICES_TRADE_TAX;
	public static float SERVICES_OFFSHORE_TRADE_TAX;
	public static boolean SERVICES_OFFSHORE_NO_CASTLE_TAX;
	public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
	public static boolean SERVICES_TRADE_ONLY_FAR;
	public static int SERVICES_TRADE_RADIUS;
	public static int SERVICES_TRADE_RADIUS_NPC;
	public static int TRADE_ONLY_TOWNS;
	public static boolean TRADE_ONLY_GH;
	public static int SERVICES_SP_ITEM;
	public static int SERVICES_LVL_UP_ITEM;
	public static int SERVICES_LVL_DOWN_ITEM;
	public static int SERVICES_SP_PRICE;
	public static int SERVICES_LVL_UP_PRICE;
	public static int SERVICES_LVL_DOWN_PRICE;
	public static boolean SERVICES_LVL_DOWN_CLASS;
	public static int SERVICES_LVL80_ITEM;
	public static int SERVICES_LVL80_PRICE;
	public static String SERVICES_LVL_PAGE_PATH;
	public static int SERVICES_OP_ITEM;
	public static int SERVICES_OP_PRICE;
	public static int ALLOW_ESL;
	public static int SERVICES_ACC_MOVE_ITEM;
	public static int SERVICES_ACC_MOVE_PRICE;
	public static boolean SERVICES_ENABLE_NO_CARRIER;
	public static int SERVICES_NO_CARRIER_DEFAULT_TIME;
	public static int SERVICES_NO_CARRIER_MAX_TIME;
	public static int SERVICES_NO_CARRIER_MIN_TIME;
	public static boolean ALLOW_DONATE_PARSE;
	public static long DONATE_PARSE_DELAY;
	public static boolean ALLOW_ES_BONUS;
	public static int[] ES_BONUS_PRICE;
	public static int ES_BONUS_CHANCE;
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_ITEM;
	public static int WEDDING_PRICE;
	public static int WEDDING_DIVORCE_ITEM;
	public static int WEDDING_DIVORCE_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static boolean WEDDING_CUPID_BOW;
	public static int WEDDING_MALE_COLOR;
	public static int WEDDING_FEMALE_COLOR;
	public static boolean L2TopManagerEnabled;
	public static int L2TopManagerInterval;
	public static String L2TopWebAddress;
	public static String L2TopSmsAddress;
	public static String L2TopServerAddress;
	public static String L2TopPrefix;
	public static int L2TopSaveDays;
	public static int[] L2TopReward;
	public static boolean L2TopAccount;
	public static boolean L2TopHWID;
	public static boolean MMO_TOP_MANAGER_ENABLED;
	public static int MMO_TOP_MANAGER_INTERVAL;
	public static String MMO_TOP_WEB_ADDRESS;
	public static int MMO_TOP_SAVE_DAYS;
	public static int[] MMO_TOP_REWARD;
	public static int VIEW_OFFSET;
	public static int DIV_BY;
	public static int DIV_BY_FOR_Z;
	public static int NPC_SHOW_LIMIT;
	public static String VERTICAL_SPLIT_REGIONS;
	public static int[] VipSkillsList;
	public static int[] RESTART_SKILLS;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static boolean ZONE_EQUIP;
	public static int MOBS_CR_MIN_LVL;
	public static boolean TRADE_LOG_MOD;
	public static boolean BS_MOD;
	private static Map<String, List<GameClient>> clients;

	/** Geodata config */
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static boolean ALLOW_GEODATA;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static boolean ALLOW_KEYBOARD_MOVE;
	public static boolean COMPACT_GEO;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;
	public static int REGION_EDGE_MAX_Z_DIFF;

	/** Geodata (Pathfind) config */
	public static int PATHFIND_BOOST;
	public static int PATHFIND_MAP_MUL;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static long PATHFIND_MAX_TIME;
	public static String PATHFIND_BUFFERS;
	public static int NPC_PATH_FIND_MAX_HEIGHT;
	public static int PLAYABLE_PATH_FIND_MAX_HEIGHT;

	public static boolean ALLOW_SEVEN_SIGNS;
	@VelocityVariable
	public static boolean ALLOW_AUGMENTATION;

	public static int CHECK_BANS_INTERVAL;

	public static boolean ACCESS_WITH_PA_ONLY;

	public static void loadGeodataSettings()
	{
		ExProperties geodataSettings = load(GEODATA_CONFIG_FILE);

		GEO_X_FIRST = geodataSettings.getProperty("GeoFirstX", 15);
		GEO_Y_FIRST = geodataSettings.getProperty("GeoFirstY", 10);
		GEO_X_LAST = geodataSettings.getProperty("GeoLastX", 26);
		GEO_Y_LAST = geodataSettings.getProperty("GeoLastY", 26);

		ALLOW_GEODATA = geodataSettings.getProperty("AllowGeodata", true);

		try
		{
			GEODATA_ROOT = new File(geodataSettings.getProperty("GeodataRoot", "./geodata/")).getCanonicalFile();
		}
		catch(IOException e)
		{
			_log.error("", e);
		}

		ALLOW_FALL_FROM_WALLS = geodataSettings.getProperty("AllowFallFromWalls", false);
		ALLOW_KEYBOARD_MOVE = geodataSettings.getProperty("AllowMoveWithKeyboard", true);
		COMPACT_GEO = geodataSettings.getProperty("CompactGeoData", false);
		PATHFIND_BOOST = geodataSettings.getProperty("PathFindBoost", 2);
		PATHFIND_DIAGONAL = geodataSettings.getProperty("PathFindDiagonal", true);
		PATHFIND_MAP_MUL = geodataSettings.getProperty("PathFindMapMul", 2);
		PATH_CLEAN = geodataSettings.getProperty("PathClean", true);
		PATHFIND_MAX_Z_DIFF = geodataSettings.getProperty("PathFindMaxZDiff", 32);
		MAX_Z_DIFF = geodataSettings.getProperty("MaxZDiff", 64);
		MIN_LAYER_HEIGHT = geodataSettings.getProperty("MinLayerHeight", 64);
		REGION_EDGE_MAX_Z_DIFF = geodataSettings.getProperty("RegionEdgeMaxZDiff", 128);
		PATHFIND_MAX_TIME = geodataSettings.getProperty("PathFindMaxTime", 10000000);
		PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
		NPC_PATH_FIND_MAX_HEIGHT = geodataSettings.getProperty("NPC_PATH_FIND_MAX_HEIGHT", 1024);
		PLAYABLE_PATH_FIND_MAX_HEIGHT = geodataSettings.getProperty("PLAYABLE_PATH_FIND_MAX_HEIGHT", 256);
	}

	public static void load(final boolean reload)
	{
		_log.info("Loading gameserver config");
		loadGeodataSettings();
		try
		{
			final ExProperties serverSettings = load("./config/server.properties");
			DEBUG = serverSettings.getProperty("Debug", false);
			SHOW_GM_LOGIN = serverSettings.getProperty("ShowGMLogin", true);
			SAVE_GM_EFFECTS = serverSettings.getProperty("SaveGMEffects", false);
			ADDON = serverSettings.getProperty("Addon", false);
			MASK_HWID = serverSettings.getProperty("MaskHWID", 31);
			OFF_NAME_LENGTH = serverSettings.getProperty("OffNameLength", true);
			CNAME_DENY_PATTERN = Pattern.compile(serverSettings.getProperty("CnameDenyTemplate", "(adm)|(admin)"), 66);
			CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
			CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
			CLAN_TITLE_TEMPLATE = serverSettings.getProperty("ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{1,16}");
			ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
			APASSWD_TEMPLATE = serverSettings.getProperty("ApasswdTemplate", "[A-Za-z0-9]{4,16}");

			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = serverSettings.getProperty("MAX_CHARACTERS_NUMBER_PER_ACCOUNT", 7);
			CHECK_BANS_INTERVAL = serverSettings.getProperty("CHECK_BANS_INTERVAL", 5);

			EVERYBODY_HAS_ADMIN_RIGHTS = serverSettings.getProperty("EverybodyHasAdminRights", false);
			LOG_KILLS = serverSettings.getProperty("LogKills", false);
			LOG_ITEMS = serverSettings.getProperty("LogItems", true);

			RATE_MOB_SPAWN = serverSettings.getProperty("RateMobSpawn", 1);
			RATE_MOB_SPAWN_MIN_LEVEL = serverSettings.getProperty("RateMobMinLevel", 1);
			RATE_MOB_SPAWN_MAX_LEVEL = serverSettings.getProperty("RateMobMaxLevel", 100);

			RATE_RAID_REGEN = serverSettings.getProperty("RateRaidRegen", 1f);
			RAID_MAX_LEVEL_DIFF = serverSettings.getProperty("RaidMaxLevelDiff", 8);
			PARALIZE_ON_RAID_DIFF = serverSettings.getProperty("ParalizeOnRaidLevelDiff", true);
			RAID_ONE_SPAWN = serverSettings.getProperty("RaidOneSpawn", true);
			AUTODESTROY_ITEM_AFTER = serverSettings.getProperty("AutoDestroyDroppedItemAfter", 0);
			DELETE_DAYS = serverSettings.getProperty("DeleteCharAfterDays", 7);
			PURGE_BYPASS_TASK_FREQUENCY = serverSettings.getProperty("PurgeTaskFrequency", 60);
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
			L2WALKER_PROTECTION = serverSettings.getProperty("L2WalkerProtection", false);
			BUGUSER_PUNISH = serverSettings.getProperty("BugUserPunishment", 2);
			DEFAULT_PUNISH = serverSettings.getProperty("IllegalActionPunishment", 1);
			MAX_FAILED_PACKETS = serverSettings.getProperty("MaxFailedPackets", 10);
			MAX_UNKNOWN_PACKETS = serverSettings.getProperty("MaxUnknownPackets", 10);
			ALLOW_DISCARDITEM = serverSettings.getProperty("AllowDiscardItem", true);
			ALLOW_DISCARDITEM_GM = serverSettings.getProperty("AllowDiscardItemGM", false);
			ALLOW_FREIGHT = serverSettings.getProperty("AllowFreight", true);
			ALLOW_WAREHOUSE = serverSettings.getProperty("AllowWarehouse", true);
			ALLOW_PRIVATE_STORE = serverSettings.getProperty("AllowPrivateStore", true);
			ALLOW_MANUFACTURE = serverSettings.getProperty("AllowManufacture", true);
			ALLOW_WATER = serverSettings.getProperty("AllowWater", true);
			ALLOW_BOAT = serverSettings.getProperty("AllowBoat", false);
			BOAT_BROADCAST_RADIUS = serverSettings.getProperty("BoatBroadcastRadius", 10000);
			ALLOW_CURSED_WEAPONS = serverSettings.getProperty("AllowCursedWeapons", false);
			DROP_CURSED_WEAPONS_ON_KICK = serverSettings.getProperty("DropCursedWeaponsOnKick", false);
			RESET_CURSED_WEAPONS = serverSettings.getProperty("ResetCursedWeapons", false);
			MIN_PROTOCOL_REVISION = serverSettings.getProperty("MinProtocolRevision", 709);
			MAX_PROTOCOL_REVISION = serverSettings.getProperty("MaxProtocolRevision", 746);
			if(MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
				throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
			MIN_NPC_ANIMATION = serverSettings.getProperty("MinNPCAnimation", 5);
			MAX_NPC_ANIMATION = serverSettings.getProperty("MaxNPCAnimation", 90);
			ALLOW_COMMUNITYBOARD = serverSettings.getProperty("AllowCommunityBoard", true);
			BBS_DEFAULT = serverSettings.getProperty("BBSDefault", "_bbshome");
			NAME_PAGE_SIZE_COMMUNITYBOARD = serverSettings.getProperty("NamePageSizeOnCommunityBoard", 50);
			NAME_PER_ROW_COMMUNITYBOARD = serverSettings.getProperty("NamePerRowOnCommunityBoard", 5);
			COMMUNITYBOARD_HTML_ROOT = serverSettings.getProperty("CommunityBoardHtmlRoot", "CommunityBoard/");
			AUTODELETE_INVALID_QUEST_DATA = serverSettings.getProperty("AutoDeleteInvalidQuestData", false);
			MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 3000);
			MAX_ITEMS = serverSettings.getProperty("MaxItems", 150000);

			DATABASE_DRIVER = serverSettings.getProperty("DATABASE_DRIVER", "com.mysql.cj.jdbc.Driver");

			String databaseHost = serverSettings.getProperty("DATABASE_HOST", "localhost");
			int databasePort = serverSettings.getProperty("DATABASE_PORT", 3306);
			String databaseName = serverSettings.getProperty("DATABASE_NAME", "l2game");

			DATABASE_URL = serverSettings.getProperty("DATABASE_URL", "jdbc:mysql://" + databaseHost + ":" + databasePort + "/" + databaseName + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
			DATABASE_LOGIN = serverSettings.getProperty("DATABASE_LOGIN", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("DATABASE_PASSWORD", "");

			DATABASE_AUTOUPDATE = serverSettings.getProperty("DATABASE_AUTOUPDATE", false);

			DATABASE_MAX_CONNECTIONS = serverSettings.getProperty("MaximumDbConnections", 10);
			DATABASE_MAX_IDLE_TIMEOUT = serverSettings.getProperty("MaxIdleConnectionTimeout", 600);
			DATABASE_IDLE_TEST_PERIOD = serverSettings.getProperty("IdleConnectionTestPeriod", 60);

			ACCOUNTS_DRIVER = serverSettings.getProperty("AUTH_DATABASE_DRIVER", "com.mysql.cj.jdbc.Driver");

			String authDatabaseHost = serverSettings.getProperty("AUTH_DATABASE_HOST", "localhost");
			int authDatabasePort = serverSettings.getProperty("AUTH_DATABASE_PORT", 3306);
			String authDatabaseName = serverSettings.getProperty("AUTH_DATABASE_NAME", "l2auth");

			ACCOUNTS_URL = serverSettings.getProperty("AUTH_DATABASE_URL", "jdbc:mysql://" + authDatabaseHost + ":" + authDatabasePort + "/" + authDatabaseName + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
			ACCOUNTS_LOGIN = serverSettings.getProperty("AUTH_DATABASE_LOGIN", "root");
			ACCOUNTS_PASSWORD = serverSettings.getProperty("AUTH_DATABASE_PASSWORD", "");
			ACCOUNTS_DB_MAX_CONNECTIONS = serverSettings.getProperty("AUTH_DATABASE_MAX_CONNECTIONS", 3);

			LAZY_ITEM_UPDATE = serverSettings.getProperty("LazyItemUpdate", false);
			LAZY_ITEM_UPDATE_ALL = serverSettings.getProperty("LazyItemUpdateAll", false);
			LAZY_ITEM_UPDATE_TIME = serverSettings.getProperty("LazyItemUpdateTime", 60000);
			LAZY_ITEM_UPDATE_ALL_TIME = serverSettings.getProperty("LazyItemUpdateAllTime", 60000);
			PACKET_FLOOD_PROTECTOR = serverSettings.getProperty("EnablePacketFloodProtector", false);
			USER_INFO_INTERVAL = serverSettings.getProperty("UserInfoInterval", 100);
			BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100);
			EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);
			if(!isPowerOfTwo(EFFECT_TASK_MANAGER_COUNT))
				throw new RuntimeException("EffectTaskManagers value should be power of 2!");
			AUTH_SERVER_AGE_LIMIT = serverSettings.getProperty("ServerAgeLimit", 0);

			for(String a : serverSettings.getProperty("ServerType", "").split(";"))
			{
				if(a.trim().isEmpty())
					continue;

				ServerType t = ServerType.valueOf(a.toUpperCase());
				AUTH_SERVER_SERVER_TYPE |= t.getMask();
			}

			AUTH_SERVER_GM_ONLY = serverSettings.getProperty("ServerGMOnly", false);
			AUTH_SERVER_BRACKETS = serverSettings.getProperty("ServerBrackets", false);
			AUTH_SERVER_IS_PVP = serverSettings.getProperty("PvPServer", false);
			AUTH_SERVER_CLOCK = serverSettings.getProperty("ServerListClock", false);
			AUTH_SERVER_TEST = serverSettings.getProperty("TestServer", false);
			SCHEDULED_THREAD_POOL_SIZE = Integer.parseInt(serverSettings.getProperty("ScheduledThreadPoolSize", String.valueOf(NCPUS * 4)));
			EXECUTOR_THREAD_POOL_SIZE = Integer.parseInt(serverSettings.getProperty("ExecutorThreadPoolSize", String.valueOf(NCPUS * 2)));
			ENABLE_RUNNABLE_STATS = serverSettings.getProperty("EnableRunnableStats", false);
			SELECTOR_CONFIG.SLEEP_TIME = serverSettings.getProperty("SelectorSleepTime", 10L);
			SELECTOR_CONFIG.INTEREST_DELAY = serverSettings.getProperty("InterestDelay", 30L);
			SELECTOR_CONFIG.MAX_SEND_PER_PASS = serverSettings.getProperty("MaxSendPerPass", 32);
			SELECTOR_CONFIG.READ_BUFFER_SIZE = serverSettings.getProperty("ReadBufferSize", 65536);
			SELECTOR_CONFIG.WRITE_BUFFER_SIZE = serverSettings.getProperty("WriteBufferSize", 131072);
			SELECTOR_CONFIG.HELPER_BUFFER_COUNT = serverSettings.getProperty("BufferPoolSize", 64);
			HTM_CACHE_MODE = serverSettings.getProperty("HtmCacheMode", 1);
			HTM_SHAPE_ARABIC = serverSettings.getProperty("HtmShapeArabic", false);

			USE_CLIENT_LANG = serverSettings.getProperty("UseClientLang", false);
			DEFAULT_LANG = Language.valueOf(serverSettings.getProperty("DefaultLang", "ENGLISH").toUpperCase());

			AVAILABLE_LANGUAGES = new HashSet<Language>();
			AVAILABLE_LANGUAGES.add(Language.ENGLISH);
			AVAILABLE_LANGUAGES.add(Language.RUSSIAN);
			AVAILABLE_LANGUAGES.add(DEFAULT_LANG);

			String[] availableLanguages = serverSettings.getProperty("AVAILABLE_LANGUAGES", "").split(";");
			for(String availableLanguage : availableLanguages)
			{
				if(availableLanguage.isEmpty())
					continue;
				AVAILABLE_LANGUAGES.add(Language.valueOf(availableLanguage.toUpperCase()));
			}

			COMMAND_LANG = serverSettings.getProperty("CommandLang", "lang");
			RESTART_TIME = serverSettings.getProperty("AutoRestart", 0);
			RESTART_AT_TIME = serverSettings.getProperty("AutoRestartAt", 5);
			if(RESTART_AT_TIME > 24)
				RESTART_AT_TIME = 24;
			RESTART_AT_MINS = serverSettings.getProperty("AutoRestartAtMins", 0);
			RESTART_AT_MINS = Math.max(Math.min(RESTART_AT_MINS, 59), 0);
			MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketDelay", 100);
			ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketDelay", 500);
			DONTLOADSPAWN = serverSettings.getProperty("StartWithoutSpawn", false);
			DONTLOADQUEST = serverSettings.getProperty("StartWithoutQuest", false);

			SHIFT_BY = serverSettings.getProperty("HShift", 12);
			SHIFT_BY_Z = serverSettings.getProperty("VShift", 11);
			MAP_MIN_Z = serverSettings.getProperty("MapMinZ", -32768);
			MAP_MAX_Z = serverSettings.getProperty("MapMaxZ", 32767);

			JAVA_SCRIPTS = serverSettings.getProperty("JavaScripts", true);
			VIEW_OFFSET = serverSettings.getProperty("ViewOffset", 1);
			DIV_BY = serverSettings.getProperty("DivBy", 2048);
			DIV_BY_FOR_Z = serverSettings.getProperty("DivByForZ", 1024);
			NPC_SHOW_LIMIT = serverSettings.getProperty("NpcShowLimit", 0);
			VERTICAL_SPLIT_REGIONS = serverSettings.getProperty("VerticalSplitRegions", "23_18");
			VipSkillsList = getIntArray(serverSettings, "VipSkills", new int[0]);

			ACCESS_WITH_PA_ONLY = serverSettings.getProperty("ACCESS_WITH_PA_ONLY", false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load ./config/server.properties File.");
		}
		try
		{
			final ExProperties ratesSettings = load("./config/rates.properties");
			RATE_XP = ratesSettings.getProperty("RateXp", 1f);
			RATE_SP = ratesSettings.getProperty("RateSp", 1f);
			RATE_QUESTS_REWARD = ratesSettings.getProperty("RateQuestsReward", 1f);
			RATE_QUESTS_DROP = ratesSettings.getProperty("RateQuestsDrop", 1f);
			RATE_QUESTS_DROP_PROF = ratesSettings.getProperty("RateQuestsDropProf", 1f);
			RATE_QUESTS_OCCUPATION_CHANGE = ratesSettings.getProperty("RateQuestsRewardOccupationChange", true);
			RATE_CLAN_REP_SCORE = ratesSettings.getProperty("RateClanRepScore", 1f);
			RATE_CLAN_REP_SCORE_MAX_AFFECTED = ratesSettings.getProperty("RateClanRepScoreMaxAffected", 2);
			RATE_DROP_ADENA = ratesSettings.getProperty("RateDropAdena", 1f);
			RATE_DROP_ADENA_MULT_MOD = ratesSettings.getProperty("RateDropAdenaMultMod", 1f);
			RATE_DROP_ADENA_STATIC_MOD = ratesSettings.getProperty("RateDropAdenaStaticMod", 0f);
			ADENA_SS = ratesSettings.getProperty("AdenaSS", true);
			RATE_DROP_ADENA_PARTY = ratesSettings.getProperty("RateDropAdenaParty", 1f);
			RATE_DROP_ITEMS_PARTY = ratesSettings.getProperty("RateDropItemsParty", 1f);
			RATE_XP_PARTY = ratesSettings.getProperty("RateXpParty", 1f);
			RATE_SP_PARTY = ratesSettings.getProperty("RateSpParty", 1f);
			RATE_DROP_ITEMS = ratesSettings.getProperty("RateDropItems", 1f);
			EPICBOSS_IDS = getIntArray(ratesSettings, "EpicBossIds", new int[] {
					29001,
					29006,
					29014,
					29020,
					29022,
					29028,
					29047,
					29066,
					29067,
					29068 });
			RATE_DROP_RAIDBOSS = ratesSettings.getProperty("RateRaidBoss", 1f);
			RATE_DROP_EPICBOSS = ratesSettings.getProperty("RateEpicBoss", 1f);
			RATE_DROP_BOX = ratesSettings.getProperty("RateBox", 1f);
			RATE_DROP_CHEST = ratesSettings.getProperty("RateChest", 1f);
			RATE_DROP_SPOIL = ratesSettings.getProperty("RateDropSpoil", 1f);
			RATE_BREAKPOINT = ratesSettings.getProperty("RateBreakpoint", 15);
			MAX_DROP_ITERATIONS = ratesSettings.getProperty("RateMaxIterations", 30);
			INTEGRAL_DROP = ratesSettings.getProperty("IntegralDrop", false);
			ALT_SINGLE_DROP = ratesSettings.getProperty("AltSingleDrop", false);
			NO_RATE_ITEMS = getIntArray(ratesSettings, "NoRateItemIds", new int[] { 6660, 6662, 6661, 6659, 6656, 6658, 8191, 6657 });
			NO_RATE_RECIPES = ratesSettings.getProperty("NoRateRecipes", false);
			RATE_MANOR = ratesSettings.getProperty("RateManor", 1d);
			RATE_FISH_DROP_COUNT = ratesSettings.getProperty("RateFishDropCount", 1f);
			RATE_SIEGE_GUARDS_PRICE = ratesSettings.getProperty("RateSiegeGuardsPrice", 1f);
			RATE_CLAN_POINTS_ACADEMY1 = ratesSettings.getProperty("RateClanPointsAcademy1", 1d);
			RATE_CLAN_POINTS_ACADEMY2 = ratesSettings.getProperty("RateClanPointsAcademy2", 1d);
			CLAN_WAR_POINTS = ratesSettings.getProperty("ClanWarPoints", 1);
			CLAN_WAR_POINTS_MORE = ratesSettings.getProperty("ClanWarPointsMore", 2);
			CLAN_WAR_POINTS_MIN = ratesSettings.getProperty("ClanWarPointsMin", -1000000000);
			RATE_PARTY_MIN = ratesSettings.getProperty("RatePartyMin", false);
			SUMMON_EXP_SP_PARTY = ratesSettings.getProperty("SummonExpSpParty", false);
			ALT_RATE_ADENA = ratesSettings.getProperty("AltRateAdena", true);
			ALT_RATE_ADENA_NG = ratesSettings.getProperty("AltRateAdenaNG", 1f);
			ALT_RATE_ADENA_D = ratesSettings.getProperty("AltRateAdenaD", 1f);
			ALT_RATE_ADENA_C = ratesSettings.getProperty("AltRateAdenaC", 1f);
			ALT_RATE_ADENA_B = ratesSettings.getProperty("AltRateAdenaB", 1f);
			ALT_RATE_ADENA_A = ratesSettings.getProperty("AltRateAdenaA", 1f);
			ALT_RATE_ADENA_S = ratesSettings.getProperty("AltRateAdenaS", 1f);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load ./config/rates.properties File.");
		}
		try
		{
			final ExProperties formulasSettings = load("./config/formulas.properties");
			ALT_MAGICFAILURES = formulasSettings.getProperty("AltMagicFailures", true);
			MAGICFAIL_DIFF = formulasSettings.getProperty("MagicFailDiff", 15);
			MAGICFAIL_MOD = formulasSettings.getProperty("MagicFailMod", 5d);
			MAGICRESIST_MOD = formulasSettings.getProperty("MagicResistMod", 0.5d);
			MAGIC_DAMAGE = formulasSettings.getProperty("MagicDamage", 1.0d);
			CRIT_DAMAGE_MAGIC = formulasSettings.getProperty("CritDamageMagic", 4.0d);
			MCRIT_MOD = formulasSettings.getProperty("CritChanceMagicMod", 1.0d);
			MCRIT_MYINFO = formulasSettings.getProperty("ShowMcritRate", false);
			SKILLS_CHANCE_CAP = formulasSettings.getProperty("SkillsChanceCap", 95d);
			SKILLS_CHANCE_MIN = formulasSettings.getProperty("SkillsChanceMin", 5d);
			SKILLS_MOB_CHANCE = formulasSettings.getProperty("SkillsMobChance", 1.0d);
			SKILLS_SHOW_CHANCE = formulasSettings.getProperty("SkillsShowChance", false);
			SKILLS_STOP_ACTOR = formulasSettings.getProperty("SkillsStopActor", true);
			CHARGE_DAM_C4 = formulasSettings.getProperty("ChargeDamageC4", false);
			CHARGE_DAM_C4_OUTSIDE_OLY = formulasSettings.getProperty("ChargeDamageC4OutsideOly", false);
			CHARGE_DAM_OLY = formulasSettings.getProperty("ChargeDamageOly", 1.0d);
			BLOW_DAM_OUTSIDE_OLY = formulasSettings.getProperty("BlowDamageOutsideOly", 1.0d);
			CONTROL_HEADING = formulasSettings.getProperty("ControlHeading", false);
			CRIT_DAM_BOW_PVP = formulasSettings.getProperty("CritDamageBowPvP", 1.0d);
			SKILLS_CAST_TIME_MIN = formulasSettings.getProperty("SkillsCastTimeMin", 333);
			CAST_INTERRUPT_TIME_ADD = formulasSettings.getProperty("CastInterruptTimeAdd", 0);
			ALT_TOGGLE = formulasSettings.getProperty("AltToggle", false);
			START_FORCE_EFFECT = formulasSettings.getProperty("StartForceEffect", 3300);
			ATTACK_DELAY_MIN = formulasSettings.getProperty("AttackDelayMin", 200);
			ATTACK_DELAY_BOW_MIN = formulasSettings.getProperty("AttackDelayBowMin", 333);
			SKILL_PACKET_DELAY = formulasSettings.getProperty("SkillPacketDelay", 100);
			MAX_SKILL_PACKETS = formulasSettings.getProperty("MaxSkillPackets", 15);
			SKILL_USE_DELAY = formulasSettings.getProperty("SkillUseDelay", 1000);
			ITEM_PACKET_DELAY = formulasSettings.getProperty("ItemPacketDelay", 100);
			MAX_ITEM_PACKETS = formulasSettings.getProperty("MaxItemPackets", 15);
			ITEM_USE_DELAY = formulasSettings.getProperty("ItemUseDelay", 1000);
			ATTACK_END_CORRECT = formulasSettings.getProperty("AttackEndCorrect", 20);
			OFF_AUTOATTACK = formulasSettings.getProperty("OffAutoattack", false);
			ATTACK_RANGE_ADD = formulasSettings.getProperty("AttackRangeAdd", 0);
			ATTACK_RANGE_ARRIVED_ADD = formulasSettings.getProperty("AttackRangeArrivedAdd", 40);
			CAST_RANGE_ADD = formulasSettings.getProperty("CastRangeAdd", 0);
			CAST_RANGE_ARRIVED_ADD = formulasSettings.getProperty("CastRangeArrivedAdd", 40);
			CHECK_EPIC_CAN_DAMAGE = formulasSettings.getProperty("CheckEpicCanDamage", false);
			CAST_CHECK = formulasSettings.getProperty("CastCheck", true);
			NEXT_CAST_CHECK = formulasSettings.getProperty("NextCastCheck", false);
			LIM_CP = formulasSettings.getProperty("LimitMaxCP", 100000);
			LIM_HP = formulasSettings.getProperty("LimitMaxHP", 40000);
			LIM_MP = formulasSettings.getProperty("LimitMaxMP", 40000);
			LIM_PATK_SPD = formulasSettings.getProperty("LimitPAtkSpd", 0);
			LIM_MATK_SPD = formulasSettings.getProperty("LimitMAtkSpd", 0);
			LIM_CRIT = formulasSettings.getProperty("LimitCritical", 500);
			LIM_MCRIT = formulasSettings.getProperty("LimitMCritical", 80) * 10;
			LIM_HENNA_STAT = (short) formulasSettings.getProperty("LimitHennaStat", 5);
			POLE_DAMAGE_MODIFIER = formulasSettings.getProperty("PoleDamageModifier", 1.0d);
			POLE_BASE_ANGLE = formulasSettings.getProperty("PoleBaseAngle", 90d);
			POLE_BASE_TC = formulasSettings.getProperty("PoleBaseTargetCount", 0d);
			POLE_VAMPIRIC_MOD = getDoubleArray(formulasSettings, "PoleVampiricMod", new double[] { 1.0, 0.9, 0.0, 7.0, 0.2, 0.01 });
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load ./config/formulas.properties File.");
		}
		try
		{
			final ExProperties chatSettings = load("./config/chat.properties");
			CHAT_MESSAGE_MAX_LEN = chatSettings.getProperty("ChatMessageLimit", 120);
			MAT_BANCHAT = chatSettings.getProperty("MAT_BANCHAT", false);
			BAN_CHANNEL_LIST = getIntArray(chatSettings, "MAT_BAN_CHANNEL", new int[] { 0 });
			MAT_REPLACE = chatSettings.getProperty("MAT_REPLACE", false);
			MAT_REPLACE_STRING = chatSettings.getProperty("MAT_REPLACE_STRING", "[censored]");
			MAT_ANNOUNCE = chatSettings.getProperty("MAT_ANNOUNCE", true);
			MAT_ANNOUNCE_NICK = chatSettings.getProperty("MAT_ANNOUNCE_NICK", true);
			UNCHATBANTIME = chatSettings.getProperty("UnBanTimer", 30);
			SHOUT_CHAT_MIN_LVL = chatSettings.getProperty("ShoutChatMinLevel", 1);
			TRADE_CHAT_MIN_LVL = chatSettings.getProperty("TradeChatMinLevel", 1);
			TELL_CHAT_MIN_LVL = chatSettings.getProperty("TellChatMinLevel", 1);
			ALL_CHAT_MIN_LVL = chatSettings.getProperty("AllChatMinLevel", 1);
			TELL_DELAY_LEVEL = chatSettings.getProperty("TellDelayLevel", 20);
			TELL_DELAY_TIME = chatSettings.getProperty("TellDelayTime", 10);
			SHOUT_CHAT_DELAY = chatSettings.getProperty("ShoutChatDelay", 3);
			TRADE_CHAT_DELAY = chatSettings.getProperty("TradeChatDelay", 5);
			HERO_CHAT_DELAY = chatSettings.getProperty("HeroChatDelay", 10);
			ALL_CHAT_RANGE = chatSettings.getProperty("AllChatRange", 1250);
			NO_TS_JAILED = chatSettings.getProperty("NoTradeShoutJailed", false);
			NO_TELL_JAILED = chatSettings.getProperty("NoTellJailed", false);
			GLOBAL_CHAT = chatSettings.getProperty("GlobalChat", 0);
			GLOBAL_TRADE_CHAT = chatSettings.getProperty("GlobalTradeChat", 0);
			SHOUT_CHAT_MODE = chatSettings.getProperty("ShoutChatMode", 1);
			TRADE_CHAT_MODE = chatSettings.getProperty("TradeChatMode", 1);
			CHAT_RANGE_FIRST_MODE = chatSettings.getProperty("ChatRangeFirstMode", 10000);
			LOG_CHAT = chatSettings.getProperty("LogChat", false);
			SPAM_FILTER = chatSettings.getProperty("SpamFilter", false);
			SPAM_PS_WORK = chatSettings.getProperty("SpamPrivateStoreWork", false);
			SPAM_CHANNELS = getIntArray(chatSettings, "SpamChannels", new int[] { 0, 1, 2, 3, 4, 8, 9, 17 });
			SPAM_SKIP_SYMBOLS = chatSettings.getProperty("SpamSkipSymbols", false);
			SPAM_COUNT = chatSettings.getProperty("SpamCount", 3);
			SPAM_TIME = chatSettings.getProperty("SpamTime", 300);
			SPAM_BLOCK_TIME = chatSettings.getProperty("SpamBlockTime", -1);
			SPAM_BAN_HWID = chatSettings.getProperty("SpamBanHwid", false);
			SPAM_BAN_HWID_MIN = chatSettings.getProperty("SpamBanHwidMin", 180);
			SPAM_BAN_HWID_MAX = chatSettings.getProperty("SpamBanHwidMax", 300);
			SPAM_MESSAGE = chatSettings.getProperty("SpamMessage", false);
			SPAM_MESSAGE_COUNT = Math.max(chatSettings.getProperty("SpamMessageCount", 5), 2);
			SPAM_MESSAGE_TIME = chatSettings.getProperty("SpamMessageTime", 10);
			SPAM_MESSAGE_BLOCK_TIME = chatSettings.getProperty("SpamMessageBlockTime", 180);
			SPAM_MESSAGE_SAME = chatSettings.getProperty("SpamMessageSame", true);
			SPAM_MESSAGE_CHANNELS = getIntArray(chatSettings, "SpamMessageChannels", new int[] { 0, 1, 8 });
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load ./config/chat.properties File.");
		}
		try
		{
			final ExProperties residenceSettings = load("./config/residence.properties");
			CH_AUCTION_MINCLANLEVEL = residenceSettings.getProperty("ClanHallAuctionMinClanLevel", 2);
			CH_AUCTION_BID_ID = residenceSettings.getProperty("ClanHallAuctionBidID", 57);
			CH_AUCTION_MAX_BID = residenceSettings.getProperty("ClanHallAuctionMaxBid", 999999999L);
			RESIDENCE_LEASE_FUNC_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseFuncMultiplier", 1d);
			RESIDENCE_LEASE_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseMultiplier", 1d);
			RESIDENCE_BUFFS_COST_MP = residenceSettings.getProperty("ResidenceBuffsCostMP", true);
			CASTLE_GENERATE_TIME_ALTERNATIVE = residenceSettings.getProperty("CastleGenerateAlternativeTime", false);
			CASTLE_GENERATE_TIME_LOW = residenceSettings.getProperty("CastleGenerateTimeLow", 46800000L);
			CASTLE_GENERATE_TIME_HIGH = residenceSettings.getProperty("CastleGenerateTimeHigh", 61200000L);
			CASTLE_HIGH_LIST = getIntArray(residenceSettings, "CastleHighList", new int[] { 3, 4, 6, 7 });
			CASTLE_SELECT_HOURS = getIntArray(residenceSettings, "CastleSelectHours", new int[] { 16, 20 });
			CASTLE_SIEGE_WEEKS = residenceSettings.getProperty("CastleSiegeWeeks", 2);
			GLUDIO_REWARD = getIntArray(residenceSettings, "GludioReward", new int[0]);
			DION_REWARD = getIntArray(residenceSettings, "DionReward", new int[0]);
			GIRAN_REWARD = getIntArray(residenceSettings, "GiranReward", new int[0]);
			OREN_REWARD = getIntArray(residenceSettings, "OrenReward", new int[0]);
			ADEN_REWARD = getIntArray(residenceSettings, "AdenReward", new int[0]);
			INNADRIL_REWARD = getIntArray(residenceSettings, "InnadrilReward", new int[0]);
			GODDARD_REWARD = getIntArray(residenceSettings, "GoddardReward", new int[0]);
			RUNE_REWARD = getIntArray(residenceSettings, "RuneReward", new int[0]);
			SCHUTTGART_REWARD = getIntArray(residenceSettings, "SchuttgartReward", new int[0]);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load ./config/residence.properties File.");
		}
		final File file = new File("./config/Advanced/bots/bots.properties");
		if(file.exists())
			try
			{
				final ExProperties BotsSettings = load("./config/Advanced/bots/bots.properties");
				ENABLE_BOTS = BotsSettings.getProperty("EnableBots", false);
				WRITE_BOTS_AI = BotsSettings.getProperty("WriteBotsAi", false);
				BOTS_AI_FILE = BotsSettings.getProperty("BotsAiFile", false);
				BOTS_AI_LOAD_AGAIN = BotsSettings.getProperty("BotsAiLoadAgain", true);
				BOTS_AI_REC_TIME = BotsSettings.getProperty("BotsAiRecTime", 30);
				BOTS_AI_MAX_AS = BotsSettings.getProperty("BotsAiMaxActions", 100);
				BOTS_AI_MIN_AS = BotsSettings.getProperty("BotsAiMinActions", 10);
				BOTS_AI_MAX_TIME = BotsSettings.getProperty("BotsAiMaxTime", 20000L);
				BOTS_MIN_LVL = BotsSettings.getProperty("BotsMinLevel", 1);
				BOTS_MAX_LVL = BotsSettings.getProperty("BotsMaxLevel", 80);
				BOTS_NO_WRITE_HERO = BotsSettings.getProperty("BotsNoWriteHero", true);
				BOTS_NO_WRITE_GM = BotsSettings.getProperty("BotsNoWriteGM", true);
				BOTS_SPAWN = BotsSettings.getProperty("BotsSpawn", false);
				BOTS_SPAWN_TYPE = BotsSettings.getProperty("BotsSpawnType", 2);
				BOTS_SPAWN_INTERVAL = BotsSettings.getProperty("BotsStartSpawnInterval", 300);
				BOTS_NEXT_SPAWN_INTERVAL = BotsSettings.getProperty("BotsNextSpawnInterval", 300);
				BOTS_SPAWN_COUNT = BotsSettings.getProperty("BotsSpawnCount", 100);
				BOTS_SPAWN_KEEP = BotsSettings.getProperty("BotsSpawnKeep", false);
				BOTS_START_LOC_RND = BotsSettings.getProperty("BotsStartLocRnd", false);
				BOTS_SPAWN_AI_RND = BotsSettings.getProperty("BotsSpawnAiRnd", false);
				BOTS_DELETE = BotsSettings.getProperty("BotsDelete", false);
				BOTS_NAMES_LOAD_AGAIN = BotsSettings.getProperty("BotsNamesLoadAgain", true);
				BOTS_NAME_RND = BotsSettings.getProperty("BotsNameRnd", true);
				BOTS_USED_NAMES = BotsSettings.getProperty("BotsUsedNames", false);
				BOTS_RT_ZONES = getIntArray(BotsSettings, "BotsRestrictZones", new int[0]);
				BOTS_RT_EQUIP = getIntArray(BotsSettings, "BotsRestrictEquipment", new int[0]);
				BOTS_RT_SKILLS = getIntArray(BotsSettings, "BotsRestrictSkills", new int[0]);
				BOTS_START_ITEMS = getIntArray(BotsSettings, "BotsStartItems", new int[0]);
				BOTS_BUFFS = BotsSettings.getProperty("BotsBuffs", true);
				BOTS_BUFFS_MAGE = getIntArray(BotsSettings, "BotsBuffsMage", new int[0]);
				BOTS_BUFFS_FIGHTER = getIntArray(BotsSettings, "BotsBuffsFighter", new int[0]);
				BOTS_ENCHANT_MAX = BotsSettings.getProperty("BotsEnchantMax", 20);
				BOTS_SP_INT_MIN = BotsSettings.getProperty("BotsSpawnIntervalMin", 1);
				BOTS_SP_INT_MAX = BotsSettings.getProperty("BotsSpawnIntervalMax", 5);
				BOTS_UNSP_INT_MIN = BotsSettings.getProperty("BotsUnspawnIntervalMin", 1000);
				BOTS_UNSP_INT_MAX = BotsSettings.getProperty("BotsUnspawnIntervalMax", 5000);
				BOTS_FA_MIN = BotsSettings.getProperty("BotsFirstActionMin", 1000);
				BOTS_FA_MAX = BotsSettings.getProperty("BotsFirstActionMax", 5000);
				BOTS_LC_MIN = BotsSettings.getProperty("BotsLifeCycleMin", 1);
				BOTS_LC_MAX = BotsSettings.getProperty("BotsLifeCycleMax", 5);
				BOTS_NOBLE = BotsSettings.getProperty("BotsNoble", true);
				BOTS_TITLE = BotsSettings.getProperty("BotsTitle", "");
				BOTS_TITLE_CHANCE = BotsSettings.getProperty("BotsTitleChance", 3d);
				BOTS_FEMALE = BotsSettings.getProperty("BotsFemale", 0d);
				BOTS_SORT = BotsSettings.getProperty("BotsSort", false);
				BOTS_CAN_JOIN_CLAN = BotsSettings.getProperty("BotsCanJoinClan", false);
				BOTS_CHANCE_JOIN_CLAN = BotsSettings.getProperty("BotsChanceJoinClan", 5d);
				BOTS_CHANCE_REFUSE_CLAN = BotsSettings.getProperty("BotsChanceRefuseClan", 30d);
				BOTS_CAN_JOIN_PARTY = BotsSettings.getProperty("BotsCanJoinParty", false);
				BOTS_CHANCE_JOIN_PARTY = BotsSettings.getProperty("BotsChanceJoinParty", 10d);
				BOTS_CHANCE_REFUSE_PARTY = BotsSettings.getProperty("BotsChanceRefuseParty", 30d);
				BOTS_WRITE_ATTACK = BotsSettings.getProperty("BotsWriteAttack", false);
				BOTS_STOP_ACT = BotsSettings.getProperty("BotsStopActions", false);
				BOTS_CAN_SAY = BotsSettings.getProperty("BotsCanSay", false);
				BOTS_SAY_CHANCE = BotsSettings.getProperty("BotsSayChance", 0.5d);
				BOTS_SHOUT_CHANCE = BotsSettings.getProperty("BotsShoutChance", 0.3d);
				BOTS_REMOVE_SAY = BotsSettings.getProperty("BotsRemoveSay", false);
				BOTS_SAY_RND = BotsSettings.getProperty("BotsSayRnd", true);
				BOTS_ACC = BotsSettings.getProperty("BotsAccount", "bots_players");
			}
			catch(Exception e2)
			{
				e2.printStackTrace();
				throw new Error("Failed to Load ./config/Advanced/bots/bots.properties File.");
			}
		try
		{
			final ExProperties otherSettings = load("./config/other.properties");
			ALT_AUGMENT = otherSettings.getProperty("AltAugment", false);
			ALT_AUGMENT_HIGH_SKILLS = getIntArray(otherSettings, "AltAugmentHighSkills", new int[0]);
			ALT_AUGMENT_TOP_SKILLS = getIntArray(otherSettings, "AltAugmentTopSkills", new int[0]);
			AUGMENT_NG_SKILL_CHANCE = otherSettings.getProperty("AugmentNGSkillChance", 5);
			AUGMENT_MID_SKILL_CHANCE = otherSettings.getProperty("AugmentMidSkillChance", 10);
			AUGMENT_HIGH_SKILL_CHANCE = otherSettings.getProperty("AugmentHighSkillChance", 20);
			AUGMENT_TOP_SKILL_CHANCE = otherSettings.getProperty("AugmentTopSkillChance", 40);
			AUGMENT_CHANCE_STAT = otherSettings.getProperty("AugmentChanceStat", 1);
			AUGMENT_NG_GLOW_CHANCE = otherSettings.getProperty("AugmentNGGlowChance", 0);
			AUGMENT_MID_GLOW_CHANCE = otherSettings.getProperty("AugmentMidGlowChance", 40);
			AUGMENT_HIGH_GLOW_CHANCE = otherSettings.getProperty("AugmentHighGlowChance", 70);
			AUGMENT_TOP_GLOW_CHANCE = otherSettings.getProperty("AugmentTopGlowChance", 100);
			INFINITY_SS = otherSettings.getProperty("InfinitySS", false);
			INFINITY_BEAST_SS = otherSettings.getProperty("InfinityBeastSS", false);
			INFINITY_ARROW = otherSettings.getProperty("InfinityArrow", false);
			ALLOW_PVP_ZONES_MOD = otherSettings.getProperty("AllowPvPZonesMod", false);
			PVP_ZONES_MOD = getIntArray(otherSettings, "PvPZonesMod", new int[0]);
			ALLOW_WINGS_MOD = otherSettings.getProperty("AllowWingsMod", false);
			ENGRAVE_SYSTEM = otherSettings.getProperty("EngraveSystem", false);
			ATTACK_ANIM_MOD = otherSettings.getProperty("AttackAnimationMod", false);
			ATTACK_ANIM_DELAY = otherSettings.getProperty("AttackAnimationDelay", 200);
			DEEPBLUE_DROP_RULES = otherSettings.getProperty("UseDeepBlueDropRules", true);
			DEEPBLUE_DROP_MAXDIFF = otherSettings.getProperty("DeepBlueDropMaxDiff", 8);
			DEEPBLUE_DROP_RAID_MAXDIFF = otherSettings.getProperty("DeepBlueDropRaidMaxDiff", 2);
			SWIMING_SPEED = otherSettings.getProperty("SwimingSpeedTemplate", 50);
			INVENTORY_MAXIMUM_NO_DWARF = otherSettings.getProperty("MaximumSlotsForNoDwarf", 80);
			INVENTORY_MAXIMUM_DWARF = otherSettings.getProperty("MaximumSlotsForDwarf", 100);
			INVENTORY_MAXIMUM_GM = otherSettings.getProperty("MaximumSlotsForGMPlayer", 250);
			MULTISELL_SIZE = otherSettings.getProperty("MultisellPageSize", 10);
			MULTISELL_PTS = otherSettings.getProperty("MultisellPts", false);
			MULTISELL_MAX_AMOUNT = otherSettings.getProperty("MultisellMaxAmount", 5000);
			ALT_REF_MULTISELL = getIntArray(otherSettings, "ReferenceMultisells", new int[0]);
			MULTISELL_WARN = otherSettings.getProperty("MultisellWarn", true);
			ALLOW_MARKUP = otherSettings.getProperty("AllowMarkup", true);
			DIVIDER_SELL = otherSettings.getProperty("DividerSell", 2);
			DIVIDER_PRICES = otherSettings.getProperty("DividerPrices", 1);
			HENNA_PRICE_MOD = otherSettings.getProperty("HennaPriceMod", 1.0d);
			AUGMENT_CANCEL_PRICE_MOD = otherSettings.getProperty("AugmentCancelPriceMod", 1.0d);
			WAREHOUSE_SLOTS_NO_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForNoDwarf", 100);
			WAREHOUSE_SLOTS_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForDwarf", 120);
			WAREHOUSE_SLOTS_CLAN = otherSettings.getProperty("MaximumWarehouseSlotsForClan", 200);
			REGEN_SIT_WAIT = otherSettings.getProperty("RegenSitWait", false);
			REGEN_SW_MP = otherSettings.getProperty("RegenSitWaitMP", 0);
			REGEN_SW_HP = otherSettings.getProperty("RegenSitWaitHP", 0);
			STARTING_LEVEL = (byte) Math.min(Byte.parseByte(otherSettings.getProperty("StartingLevel", "1")), 80);
			SUBCLASS_LEVEL = (byte) Math.max(Math.min(Byte.parseByte(otherSettings.getProperty("SubClassLevel", "40")), 80), 40);
			STARTING_SP = otherSettings.getProperty("StartingSp", 0);
			STARTING_ADENA = otherSettings.getProperty("StartingAdena", 0);
			START_QUESTS_COMPLETED = getIntArray(otherSettings, "StartQuestsCompleted", new int[0]);
			EVER_NOBL = otherSettings.getProperty("EverNoblesse", false);
			BLESS_NOBL = otherSettings.getProperty("BlessingNoblesse", false);
			CREATE_CLAN_LVL = (byte) Math.min(Byte.parseByte(otherSettings.getProperty("CreateClanLevel", "0")), 8);
			CREATE_CLAN_REP = otherSettings.getProperty("CreateClanRep", 0);
			ALLOW_START_ITEMS = otherSettings.getProperty("AllowStartItems", false);
			START_ITEMS_MAGE = getIntArray(otherSettings, "StartItemsMage", new int[0]);
			START_ITEMS_FIGHTER = getIntArray(otherSettings, "StartItemsFighter", new int[0]);
			ALLOW_START_ITEMS_ENCHANT = otherSettings.getProperty("AllowStartItemsEnchant", false);
			START_ITEMS_ENCHANT_ARMOR = otherSettings.getProperty("StartItemsEnchantArmor", 0);
			START_ITEMS_ENCHANT_WEAPON = otherSettings.getProperty("StartItemsEnchantWeapon", 0);
			ALLOW_START_BUFFS = otherSettings.getProperty("AllowStartBuffs", false);
			START_BUFFS_MAGE = getIntArray(otherSettings, "StartBuffsMage", new int[0]);
			START_BUFFS_FIGHTER = getIntArray(otherSettings, "StartBuffsFighter", new int[0]);
			START_XYZ = getIntArray(otherSettings, "StartXYZ", new int[0]);
			START_PA = otherSettings.getProperty("StartPA", 0);
			START_RATE_PA = otherSettings.getProperty("StartRatePA", 2.0f);
			START_PA_CHECK_HWID = otherSettings.getProperty("StartPAcheckHWID", false);
			START_PA_CHECK_IP = otherSettings.getProperty("StartPAcheckIP", true);
			UNSTUCK_SKILL = otherSettings.getProperty("UnstuckSkill", true);
			UNSTUCK_TOWN = otherSettings.getProperty("UnstuckTown", 0);
			TO_TOWN = otherSettings.getProperty("ToTown", 0);
			NO_TO_TOWN_PK = otherSettings.getProperty("NoToTownPK", false);
			NO_SUMMON_KARMA = otherSettings.getProperty("NoSummonKarma", true);
			ALLOW_RCM = otherSettings.getProperty("AllowRCM", true);
			DELAY_RCM = otherSettings.getProperty("DelayRCM", 300);
			RESPAWN_RESTORE_CP = otherSettings.getProperty("RespawnRestoreCP", -1d) / 100.0;
			RESPAWN_RESTORE_HP = otherSettings.getProperty("RespawnRestoreHP", 65d) / 100.0;
			RESPAWN_RESTORE_MP = otherSettings.getProperty("RespawnRestoreMP", -1d) / 100.0;
			MAX_PVTSTORE_SLOTS_DWARF = otherSettings.getProperty("MaxPvtStoreSlotsDwarf", 5);
			MAX_PVTSTORE_SLOTS_OTHER = otherSettings.getProperty("MaxPvtStoreSlotsOther", 4);
			MAX_PVTCRAFT_SLOTS = otherSettings.getProperty("MaxPvtManufactureSlots", 20);
			SHOW_OFFLINE_MODE_IN_ONLINE = otherSettings.getProperty("ShowOfflineTradeInOnline", false);
			NO_PICK_UP_MSG = otherSettings.getProperty("NoPickUpMsg", false);
			ANNOUNCE_MAMMON_SPAWN = otherSettings.getProperty("AnnounceMammonSpawn", false);
			ANNOUNCE_RB = otherSettings.getProperty("AnnounceRaidStatus", false);
			TAMED_X05 = otherSettings.getProperty("TamedX05", 100);
			TAMED_X1 = otherSettings.getProperty("TamedX1", 40);
			TAMED_X2 = otherSettings.getProperty("TamedX2", 25);
			CLASS_EXP = otherSettings.getProperty("ClassExp", false);
			SC_AGGRO_RANGE = otherSettings.getProperty("SoulCrystalAggroRange", 500);
			SC_RANDOM_LEVELING = getIntArray(otherSettings, "SoulCrystalRandomLeveling", new int[] {
					25163,
					25269,
					25453,
					25328,
					25109,
					25202,
					22215,
					22216,
					22217,
					29065,
					25338,
					25319 });
			SC_LEVEL_CHANCE = otherSettings.getProperty("SoulCrystalLevelChance", 5d);
			MAX_PLAYER_CONTR = otherSettings.getProperty("MaxPlayerContribution", 1000000L);
			GM_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("GMNameColour", "FFFFFF"));
			GM_HERO_AURA = otherSettings.getProperty("GMHeroAura", true);
			InvulEffect = otherSettings.getProperty("InvulEffect", "none");
			COMMAND_STATUS_GM = otherSettings.getProperty("CommandStatusGM", true);
			DON_LOG = otherSettings.getProperty("DonLog", false);
			DON_ITEM_LOG = otherSettings.getProperty("DonItemLog", 4037);
			DON_MIN_COUNT_LOG = otherSettings.getProperty("DonMinCountLog", 300);
			NORMAL_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("NormalNameColour", "FFFFFF"));
			CLANLEADER_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("ClanleaderNameColour", "FFFFFF"));
			SHOW_HTML_WELCOME = otherSettings.getProperty("ShowHTMLWelcome", false);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/other.properties File.");
		}
		try
		{
			final ExProperties otherSettings = load("./config/enchant.properties");
			ENCHANT_CHANCE_WEAPON = otherSettings.getProperty("EnchantChanceWeapon", 66d);
			ENCHANT_CHANCE_ARMOR = Double.parseDouble(otherSettings.getProperty("EnchantChanceArmor", String.valueOf(ENCHANT_CHANCE_WEAPON)));
			ENCHANT_CHANCE_ACCESSORY = Double.parseDouble(otherSettings.getProperty("EnchantChanceAccessory", String.valueOf(ENCHANT_CHANCE_ARMOR)));
			ENCHANT_CHANCE_CRYSTAL_WEAPON = otherSettings.getProperty("EnchantChanceCrystalWeapon", 66d);
			ENCHANT_CHANCE_CRYSTAL_ARMOR = Double.parseDouble(otherSettings.getProperty("EnchantChanceCrystalArmor", String.valueOf(ENCHANT_CHANCE_CRYSTAL_WEAPON)));
			ENCHANT_CHANCE_CRYSTAL_ACCESSORY = Double.parseDouble(otherSettings.getProperty("EnchantChanceCrystalAccessory", String.valueOf(ENCHANT_CHANCE_CRYSTAL_ARMOR)));
			ENCHANT_CHANCE_LIST = otherSettings.getProperty("EnchantChanceList", 66d);
			ENCHANT_LIST = getIntArray(otherSettings, "EnchantList", new int[0]);
			USE_ALT_ENCHANT = otherSettings.getProperty("UseAltEnchant", false);
			ALT_ENCHANT_WEAPON = new ArrayList<Double>();
			for(final String prop : otherSettings.getProperty("AltEnchantWeapon", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
				ALT_ENCHANT_WEAPON.add(Double.parseDouble(prop));
			ALT_ENCHANT_ARMOR = new ArrayList<Double>();
			for(final String prop : otherSettings.getProperty("AltEnchantArmor", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
				ALT_ENCHANT_ARMOR.add(Double.parseDouble(prop));
			ALT_ENCHANT_JEWELRY = new ArrayList<Double>();
			for(final String prop : otherSettings.getProperty("AltEnchantJewelry", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
				ALT_ENCHANT_JEWELRY.add(Double.parseDouble(prop));
			ALT_ENCHANT_LIST = new ArrayList<Double>();
			for(final String prop : otherSettings.getProperty("AltEnchantList", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
				ALT_ENCHANT_LIST.add(Double.parseDouble(prop));
			SAFE_ENCHANT_COMMON = otherSettings.getProperty("SafeEnchantCommon", 3);
			SAFE_ENCHANT_FULL_BODY = otherSettings.getProperty("SafeEnchantFullBody", 4);
			ENCHANT_MAX_WEAPON = Long.parseLong(otherSettings.getProperty("EnchantMaxWeapon", "20"));
			ENCHANT_MAX_ACCESSORY = Long.parseLong(otherSettings.getProperty("EnchantMaxAccessory", "20"));
			ENCHANT_MAX_ARMOR = Long.parseLong(otherSettings.getProperty("EnchantMaxArmor", "20"));
			SAFE_ENCHANT = otherSettings.getProperty("SafeEnchant", false);
			SET_SAFE_ENCHANT = otherSettings.getProperty("SetSafeEnchant", false);
			CRYSTAL_BLESSED = otherSettings.getProperty("CrystalBlessed", false);
			ENCHANT_HERO_WEAPON = otherSettings.getProperty("EnchantHeroWeapon", false);
			ENCHANT_BLESSED_HERO_WEAPON = otherSettings.getProperty("EnchantBlessedHeroWeapon", false);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/enchant.properties File.");
		}
		try
		{
			final ExProperties spoilSettings = load("./config/spoil.properties");
			BASE_SPOIL_RATE = spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", 78f);
			MINIMUM_SPOIL_RATE = spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", 1f);
			ALT_SPOIL_FORMULA = spoilSettings.getProperty("AltFormula", false);
			MANOR_SOWING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingSuccess", 100);
			MANOR_SOWING_ALT_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingAltSuccess", 10);
			MANOR_HARVESTING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfHarvestingSuccess", 90);
			MANOR_DIFF_PLAYER_TARGET = spoilSettings.getProperty("MinDiffPlayerMob", 5);
			MANOR_DIFF_PLAYER_TARGET_PENALTY = spoilSettings.getProperty("DiffPlayerMobPenalty", 5);
			MANOR_DIFF_SEED_TARGET = spoilSettings.getProperty("MinDiffSeedMob", 5);
			MANOR_DIFF_SEED_TARGET_PENALTY = spoilSettings.getProperty("DiffSeedMobPenalty", 5);
			ALLOW_MANOR = spoilSettings.getProperty("AllowManor", true);
			MANOR_REFRESH_TIME = spoilSettings.getProperty("AltManorRefreshTime", 20);
			MANOR_REFRESH_MIN = spoilSettings.getProperty("AltManorRefreshMin", 00);
			MANOR_APPROVE_TIME = spoilSettings.getProperty("AltManorApproveTime", 6);
			MANOR_APPROVE_MIN = spoilSettings.getProperty("AltManorApproveMin", 00);
			MANOR_MAINTENANCE_PERIOD = spoilSettings.getProperty("AltManorMaintenancePeriod", 360000);
			MANOR_SAVE_ALL_ACTIONS = spoilSettings.getProperty("AltManorSaveAllActions", false);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/spoil.properties File.");
		}
		try
		{
			final ExProperties altSettings = load("./config/altsettings.properties");
			ALT_ARENA_EXP = altSettings.getProperty("ArenaExp", true);
			ALT_GAME_DELEVEL = altSettings.getProperty("Delevel", true);
			ALT_SHOW_REUSE_MSG = altSettings.getProperty("AltShowSkillReuseMessage", true);
			DEL_AUGMENT_BUFFS = altSettings.getProperty("DeleteAugmentBuffs", false);
			ALT_SHOW_CRIT_MSG = altSettings.getProperty("AltShowSkillCritMessage", false);
			WEAR_TEST_ENABLED = altSettings.getProperty("WearTestEnabled", false);
			AUTO_LOOT_INDIVIDUAL_ADENA = altSettings.getProperty("AutoLootIndividualAdena", false);
			AUTO_LOOT_INDIVIDUAL_ITEMS = altSettings.getProperty("AutoLootIndividualItems", false);
			AUTO_LOOT_INDIVIDUAL_HERBS = altSettings.getProperty("AutoLootIndividualHerbs", false);
			AUTO_LOOT_INDIVIDUAL_LIST = altSettings.getProperty("AutoLootIndividualList", false);
			AUTO_LOOT_ITEMS = altSettings.getProperty("AutoLootItems", false);
			AUTO_LOOT_ADENA = altSettings.getProperty("AutoLootAdena", false);
			AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs", false);
			AUTO_LOOT_FROM_RAIDS = altSettings.getProperty("AutoLootFromRaids", false);
			DROP_ITEMS_GR = getIntArray(altSettings, "DropItemsGround", new int[0]);
			AUTO_LOOT_LIST = getIntArray(altSettings, "AutoLootList", new int[0]);
			AUTO_LOOT_PK = altSettings.getProperty("AutoLootPK", false);
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = altSettings.getProperty("AltKarmaPlayerCanShop", false);
			ALT_GAME_KARMA_NPC = getIntArray(altSettings, "AltKarmaNpc", new int[] { 32011, 31859, 31759, 29061, 32107, 35506, 29055 });
			ALLOW_AUGMENTATION = altSettings.getProperty("ALLOW_AUGMENTATION", true);
			NO_DAMAGE_NPC = altSettings.getProperty("NoDamageNpc", false);
			SHOW_NPC_CREST = altSettings.getProperty("ShowNpcCrest", false);
			DELAY_SPAWN_NPC = Long.valueOf(altSettings.getProperty("DelaySpawnNpc", "0"));
			SAVING_SPS = altSettings.getProperty("SavingSpS", false);
			MANAHEAL_SPS_BONUS = altSettings.getProperty("ManahealSpSBonus", false);
			KILL_COUNTER = altSettings.getProperty("KillCounter", false);
			KILL_COUNTER_PRELOAD = altSettings.getProperty("KillCounterPreload", true);
			DROP_COUNTER = altSettings.getProperty("DropCounter", true);
			CRAFT_COUNTER = altSettings.getProperty("CraftCounter", true);
			ALT_TRUE_CHESTS = altSettings.getProperty("TrueChests", 33);
			ALT_GAME_MATHERIALSDROP = altSettings.getProperty("AltMatherialsDrop", false);
			ALT_DOUBLE_SPAWN = altSettings.getProperty("DoubleSpawn", false);
			AUGMENT_STATIC_REUSE = altSettings.getProperty("AugmentStaticReuse", true);
			ALT_GAME_UNREGISTER_RECIPE = altSettings.getProperty("AltUnregisterRecipe", true);
			ALT_GAME_SHOW_DROPLIST = altSettings.getProperty("AltShowDroplist", true);
			ALT_GAME_GEN_DROPLIST_ON_DEMAND = altSettings.getProperty("AltGenerateDroplistOnDemand", false);
			SHOW_DROPLIST_NPCID = altSettings.getProperty("ShowDroplistNpcId", true);
			ALLOW_NPC_SHIFTCLICK = altSettings.getProperty("AllowShiftClick", true);
			ALT_FULL_NPC_STATS_PAGE = altSettings.getProperty("AltFullStatsPage", false);
			ALLOW_VOICED_COMMANDS = altSettings.getProperty("AllowVoicedCommands", true);
			ALLOW_AUTOHEAL_COMMANDS = altSettings.getProperty("ALLOW_AUTOHEAL_COMMANDS", false);
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = altSettings.getProperty("AltAllowSubclassWithoutQuest", false);
			ALT_GAME_LEVEL_TO_GET_SUBCLASS = altSettings.getProperty("AltLevelToGetSubclass", 75);
			ALT_GAME_SUB_ADD = altSettings.getProperty("AltSubAdd", 0);
			ALT_GAME_ANY_SUBCLASS = altSettings.getProperty("AltAnySubclass", false);
			ANY_SUBCLASS_MASTER = altSettings.getProperty("AnySubclassMaster", false);
			NO_HERO_SKILLS_SUB = altSettings.getProperty("NoHeroSkillsSub", true);
			CLASS_CHANGE_DELAY = altSettings.getProperty("ClassChangeDelay", 5);
			NOBLE_KILL_RB = altSettings.getProperty("NobleKillRB", false);
			ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = altSettings.getProperty("AltAllowOthersWithdrawFromClanWarehouse", true);
			ALT_GAME_REQUIRE_CLAN_CASTLE = altSettings.getProperty("AltRequireClanCastle", false);
			ALT_GAME_REQUIRE_CASTLE_DAWN = altSettings.getProperty("AltRequireCastleDawn", true);
			ALT_100_RECIPES_B = altSettings.getProperty("Alt100PercentRecipesB", false);
			ALT_100_RECIPES_A = altSettings.getProperty("Alt100PercentRecipesA", false);
			ALT_100_RECIPES_S = altSettings.getProperty("Alt100PercentRecipesS", false);
			ALT_ADD_RECIPES = altSettings.getProperty("AltAddRecipes", 0);
			SS_ANNOUNCE_PERIOD = altSettings.getProperty("SSAnnouncePeriod", 0);
			ALT_FISH_CHAMPIONSHIP_ENABLED = altSettings.getProperty("AltFishChampionshipEnabled", true);
			ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = altSettings.getProperty("AltFishChampionshipRewardItemId", 57);
			ALT_FISH_CHAMPIONSHIP_REWARD_1 = altSettings.getProperty("AltFishChampionshipReward1", 800000);
			ALT_FISH_CHAMPIONSHIP_REWARD_2 = altSettings.getProperty("AltFishChampionshipReward2", 500000);
			ALT_FISH_CHAMPIONSHIP_REWARD_3 = altSettings.getProperty("AltFishChampionshipReward3", 300000);
			ALT_FISH_CHAMPIONSHIP_REWARD_4 = altSettings.getProperty("AltFishChampionshipReward4", 200000);
			ALT_FISH_CHAMPIONSHIP_REWARD_5 = altSettings.getProperty("AltFishChampionshipReward5", 100000);
			PETITIONING_ALLOWED = altSettings.getProperty("PetitioningAllowed", true);
			MAX_PETITIONS_PER_PLAYER = altSettings.getProperty("MaxPetitionsPerPlayer", 5);
			MAX_PETITIONS_PENDING = altSettings.getProperty("MaxPetitionsPending", 25);
			AUTO_LEARN_SKILLS = altSettings.getProperty("AutoLearnSkills", false);
			ALT_SOCIAL_ACTION_REUSE = altSettings.getProperty("AltSocialActionReuse", false);
			ALT_DISABLE_SPELLBOOKS = altSettings.getProperty("AltDisableSpellbooks", false);
			ALT_DISABLE_EGGS = altSettings.getProperty("AltDisableEggs", false);
			ALT_SIMPLE_SIGNS = altSettings.getProperty("PushkinSignsOptions", false);
			ALT_MAMMOTH_UPGRADE = altSettings.getProperty("MammonUpgrade", 2);
			ALT_MAMMOTH_EXCHANGE = altSettings.getProperty("MammonExchange", 2);
			ALT_MAMMOTH_HARDCODE = getIntArray(altSettings, "MammonHardcode", new int[] { 31126 });
			ALT_BUFF_LIMIT = altSettings.getProperty("BuffLimit", 20);
			DEBUFF_LIMIT = altSettings.getProperty("DebuffLimit", 6);
			TRIGGER_LIMIT = altSettings.getProperty("TriggerLimit", 12);
			SAVE_EFFECTS_AFTER_DEATH = altSettings.getProperty("SaveEffectsAfterDeath", false);
			ALT_DEATH_PENALTY = altSettings.getProperty("EnableAltDeathPenalty", false);
			ALLOW_DEATH_PENALTY_C5 = altSettings.getProperty("EnableDeathPenaltyC5", true);
			ALT_DEATH_PENALTY_C5_CHANCE = altSettings.getProperty("DeathPenaltyC5Chance", 10);
			ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY = altSettings.getProperty("ChaoticCanUseScrollOfRecovery", false);
			ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY = altSettings.getProperty("DeathPenaltyC5RateExpPenalty", 1);
			ALT_DEATH_PENALTY_C5_KARMA_PENALTY = altSettings.getProperty("DeathPenaltyC5RateKarma", 1);
			DEATH_PENALTY_LOW_EXP = altSettings.getProperty("DeathPenaltyLowExp", 0L);
			ALT_PK_DEATH_RATE = altSettings.getProperty("AltPKDeathRate", 0f);
			DISABLE_EXPERTISE_PENALTY = altSettings.getProperty("DisableExpertisePenalty", false);
			NONOWNER_ITEM_PICKUP_DELAY = altSettings.getProperty("NonOwnerItemPickupDelay", 15) * 1000;
			NONOWNER_ITEM_PICKUP_DELAY_BOSS = altSettings.getProperty("NonOwnerItemPickupDelayBoss", 300) * 1000;
			NONOWNER_ITEM_PICKUP_PET = altSettings.getProperty("NonOwnerItemPickupPet", false);
			ALT_NO_LASTHIT = altSettings.getProperty("NoLasthitOnRaid", false);
			DROP_LASTHIT = altSettings.getProperty("DropLasthit", false);
			CHAR_TITLE = altSettings.getProperty("CharTitle", "");
			if(CHAR_TITLE.length() > 16)
				CHAR_TITLE = CHAR_TITLE.substring(0, 16);
			ALT_ALLOW_SHADOW_WEAPONS = altSettings.getProperty("AllowShadowWeapons", true);
			ALT_SHOP_PRICE_LIMITS = getIntArray(altSettings, "ShopPriceLimits", new int[0]);
			ALT_SHOP_UNALLOWED_ITEMS = getIntArray(altSettings, "ShopUnallowedItems", new int[0]);
			FESTIVAL_MIN_PARTY_SIZE = altSettings.getProperty("FestivalMinPartySize", 5);
			RIFT_MIN_PARTY_SIZE = altSettings.getProperty("RiftMinPartySize", 5);
			RIFT_SPAWN_DELAY = altSettings.getProperty("RiftSpawnDelay", 17000);
			RIFT_MAX_JUMPS = altSettings.getProperty("MaxRiftJumps", 4);
			RIFT_AUTO_JUMPS_TIME = altSettings.getProperty("AutoJumpsDelay", 8);
			RIFT_AUTO_JUMPS_TIME_RAND = altSettings.getProperty("AutoJumpsDelayRandom", 120000);
			RIFT_ENTER_COST_RECRUIT = altSettings.getProperty("RecruitFC", 18);
			RIFT_ENTER_COST_SOLDIER = altSettings.getProperty("SoldierFC", 21);
			RIFT_ENTER_COST_OFFICER = altSettings.getProperty("OfficerFC", 24);
			RIFT_ENTER_COST_CAPTAIN = altSettings.getProperty("CaptainFC", 27);
			RIFT_ENTER_COST_COMMANDER = altSettings.getProperty("CommanderFC", 30);
			RIFT_ENTER_COST_HERO = altSettings.getProperty("HeroFC", 33);
			RIFT_BOSS_ROOM_TIME_MUTIPLY = altSettings.getProperty("BossRoomTimeMultiply", 1.5f);
			ALLOW_JUMP_BOSS = altSettings.getProperty("AllowJumpBoss", false);
			BOSS_ROOM_CHANCE = altSettings.getProperty("BossRoomChance", 0);
			ALLOW_CLANSKILLS = altSettings.getProperty("AllowClanSkills", true);
			REMOVE_SK_ON_DELEVEL = altSettings.getProperty("RemoveSkillsOnDelevel", 10);
			PARTY_LEADER_ONLY_CAN_INVITE = altSettings.getProperty("PartyLeaderOnlyCanInvite", true);
			ALLOW_TALK_WHILE_SITTING = altSettings.getProperty("AllowTalkWhileSitting", true);
			ALLOW_NOBLE_TP_TO_ALL = altSettings.getProperty("AllowNobleTPToAll", false);
			MAXLOAD_MODIFIER = altSettings.getProperty("MaxLoadModifier", 1.0f);
			ALT_MAX_ALLY_SIZE = altSettings.getProperty("AltMaxAllySize", 3);
			ALT_PARTY_DISTRIBUTION_RANGE = altSettings.getProperty("AltPartyDistributionRange", 1500);
			ALT_PARTY_LVL_DIFF = altSettings.getProperty("AltPartyLevelDiff", 20);
			PARTY_QUEST_ITEMS_RANGE = altSettings.getProperty("PartyQuestItemsRange", 1500);
			PARTY_QUEST_ITEMS_RANGE_Z = altSettings.getProperty("PartyQuestItemsRangeZ", 400);
			ALT_PARTY_BONUS = getFloatArray(altSettings, "AltPartyBonus", new float[] {
					1.0f,
					1.3f,
					1.39f,
					1.5f,
					1.54f,
					1.58f,
					1.63f,
					1.67f,
					1.71f });
			ALT_PARTY_QUEST_BONUS = getFloatArray(altSettings, "AltPartyQuestBonus", new float[] {
					1.0f,
					1.3f,
					1.39f,
					1.5f,
					1.54f,
					1.58f,
					1.63f,
					1.67f,
					1.71f });
			HoursDissolveClan = Math.max(altSettings.getProperty("HoursDissolveClan", 24L), 0L);
			HoursBeforeCreateClan = altSettings.getProperty("HoursBeforeCreateClan", 24L);
			HoursBeforeJoinAClan = altSettings.getProperty("HoursBeforeJoinAClan", 24L);
			PENALTY_BY_CLAN_DISMISS = altSettings.getProperty("PenaltyByClanDismiss", true);
			HoursBeforeInviteClan = altSettings.getProperty("HoursBeforeInviteClan", 24L);
			HoursBeforeJoinAlly = altSettings.getProperty("HoursBeforeJoinAlly", 24L);
			HoursBeforeInviteAlly = altSettings.getProperty("HoursBeforeInviteAlly", 24L);
			AltClanMembersForWar = altSettings.getProperty("AltClanMembersForWar", 15);
			AltMinClanLvlForWar = altSettings.getProperty("AltMinClanLvlForWar", 3);
			AltClanWarMax = altSettings.getProperty("AltClanWarMax", 30);
			NO_COMBAT_STOP_CLAN_WAR = altSettings.getProperty("NoCombatStopClanWar", true);
			DaysBeforeCreateNewAllyWhenDissolved = altSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", 1L);
			MinLevelToCreatePledge = altSettings.getProperty("MinLevelToCreatePledge", 10);
			ADENA_FOR_LEVEL_1 = altSettings.getProperty("AdenaForLevel1", 650000);
			ADENA_FOR_LEVEL_2 = altSettings.getProperty("AdenaForLevel2", 2500000);
			MEMBERS_FOR_LEVEL_6 = altSettings.getProperty("MembersForLevel6", 30);
			MEMBERS_FOR_LEVEL_7 = altSettings.getProperty("MembersForLevel7", 80);
			MEMBERS_FOR_LEVEL_8 = altSettings.getProperty("MembersForLevel8", 120);
			REP_FOR_LEVEL_6 = altSettings.getProperty("RepForLevel6", 10000);
			REP_FOR_LEVEL_7 = altSettings.getProperty("RepForLevel7", 20000);
			REP_FOR_LEVEL_8 = altSettings.getProperty("RepForLevel8", 40000);
			MAX_CLAN_MEMBERS = altSettings.getProperty("MaxClanMembers", 40);
			ACADEMY_LEAVE_LVL = Math.max(altSettings.getProperty("AcademyLeaveLevel", 40), 40);
			ROYAL_REP = altSettings.getProperty("RoyalRep", 5000);
			KNIGHT_REP = altSettings.getProperty("KnightRep", 10000);
			DISABLE_ACADEMY = altSettings.getProperty("DisableAcademy", false);
			DISABLE_ROYAL = altSettings.getProperty("DisableRoyal", false);
			DISABLE_KNIGHT = altSettings.getProperty("DisableKnight", false);
			ALLOW_RESIDENCE_DOOR_OPEN_ON_CLICK = altSettings.getProperty("AllowResidenceDoorOpenOnClick", true);
			ALT_CH_ALL_BUFFS = altSettings.getProperty("AltChAllBuffs", false);
			ALT_CH_ALLOW_1H_BUFFS = altSettings.getProperty("AltChAllowHourBuff", false);
			ADV_NEWBIE_BUFF = altSettings.getProperty("AdvNewbieBuff", false);
			HS_DISEASE = getIntArray(altSettings, "HotSpringsDisease", new int[] {
					21314,
					4554,
					21316,
					4554,
					21317,
					4554,
					21319,
					4554,
					21321,
					4554,
					21322,
					4554,
					21316,
					4552,
					21319,
					4552 });
			ENABLE_FORBIDDEN_BOW_CLASSES = altSettings.getProperty("EnableForbiddenBowClasses", false);
			FORBIDDEN_BOW_CLASSES = getIntArray(altSettings, "ForbiddenBowClasses", new int[0]);
			ALT_SALVATION = altSettings.getProperty("AltSalvation", false);
			USE_BREAK_FAKEDEATH = altSettings.getProperty("UseBreakFakeDeath", false);
			GATEKEEPER_TELEPORT_SIEGE = altSettings.getProperty("GkTeleportSiege", false);
			GATEKEEPER_MODIFIER = altSettings.getProperty("GkCostMultiplier", 1.0f);
			GATEKEEPER_FREE = altSettings.getProperty("GkFree", 0);
			CRUMA_GATEKEEPER_LVL = altSettings.getProperty("GkCruma", 56);
			TELEPORT_PROTECT = altSettings.getProperty("TeleportProtect", 0);
			VALID_TELEPORT = altSettings.getProperty("ValidTeleport", false);
			CHAMPION_CHANCE1 = altSettings.getProperty("ChampionChance1", 0d);
			CHAMPION_CHANCE2 = altSettings.getProperty("ChampionChance2", 0d);
			CHAMPION_CAN_BE_AGGRO = altSettings.getProperty("ChampionAggro", false);
			CHAMPION_CAN_BE_SOCIAL = altSettings.getProperty("ChampionSocial", false);
			CHAMPION_FEAR_IMMUNE = altSettings.getProperty("ChampionFearImmune", true);
			CHAMPION_PARALYZE_IMMUNE = altSettings.getProperty("ChampionParalyzeImmune", true);
			CHAMPION_DROP_HERBS = altSettings.getProperty("ChampionDropHerbs", false);
			CHAMPION_TOP_LEVEL = altSettings.getProperty("ChampionTopLevel", 75);
			CHAMPION_MIN_LEVEL = altSettings.getProperty("ChampionMinLevel", 20);
			CHAMPION_DROP_ONLY_ADENA = altSettings.getProperty("ChampionDropOnlyAdena", false);
			RATE_CHAMPION_DROP_ADENA = altSettings.getProperty("RateChampionDropAdena", 1f);
			CHAMPION_REWARD = altSettings.getProperty("ChampionReward", false);
			CHAMPION_REWARD_LIST1 = getIntArray(altSettings, "ChampionRewardList1", new int[0]);
			CHAMPION_REWARD_LIST2 = getIntArray(altSettings, "ChampionRewardList2", new int[0]);
			CHAMPION_REWARD_DIFF = altSettings.getProperty("ChampionRewardDiff", 10);
			QUEST_KILL_DELAY = altSettings.getProperty("QuestKillDelay", 3000L);
			QUEST_KILL_REWARD_DEAD = altSettings.getProperty("QuestKillRewardDead", false);
			FIRST_BLOODED_FABRIC = altSettings.getProperty("FirstBloodedFabric", 0.1d);
			RESTORE_CANCEL_BUFFS = altSettings.getProperty("RestoreCancelBuffs", 0);
			REVIVE_TIME = Math.max(altSettings.getProperty("ReviveTime", 0), 0);
			VISIBLE_SIEGE_ICONS = altSettings.getProperty("VisibleSiegeIcons", false);
			NO_RES_SIEGE = altSettings.getProperty("NoResSiege", false);
			ATTACKERS_ALLY_FIRST_STEP_SIEGE = altSettings.getProperty("AttackersAllyFirstStepSiege", true);
			OSWEOC = altSettings.getProperty("OffensiveSkillsWithEffectsOnCompanion", true);
			ALLOW_PETS_ACTION_SKILLS = altSettings.getProperty("AllowPetsActionSkills", false);
			if(ALLOW_PETS_ACTION_SKILLS)
			{
				final int[] as = getIntArray(altSettings, "PetsActionSkills", new int[0]);
				PETS_ACTION_SKILLS = new HashMap<Integer, Integer>();
				for(int i = 0; i < as.length; i += 2)
					PETS_ACTION_SKILLS.put(as[i], as[i + 1]);
			}
			String buffs = altSettings.getProperty("BlockBuffList", "");
			BlockBuffList = new ArrayList<Integer>();
			if(!buffs.isEmpty())
				for(final String id : buffs.split(","))
					BlockBuffList.add(Integer.parseInt(id));
			buffs = altSettings.getProperty("NoBlockBuffInOly", "");
			NoBlockBuffInOly = new ArrayList<Integer>();
			if(!buffs.isEmpty())
				for(final String id : buffs.split(","))
					NoBlockBuffInOly.add(Integer.parseInt(id));
			RESTART_SKILLS = getIntArray(altSettings, "RestartSkills", new int[0]);
			ENABLE_MODIFY_SKILL_DURATION = altSettings.getProperty("EnableModifySkillDuration", false);
			if(ENABLE_MODIFY_SKILL_DURATION)
			{
				SKILL_DURATION_LIST = new HashMap<Integer, Integer>();
				final String[] split7;
				final String[] propertySplit = split7 = altSettings.getProperty("SkillDurationList", "").split(";");
				for(final String skill : split7)
				{
					final String[] skillSplit = skill.split(",");
					if(skillSplit.length != 2)
						_log.warn("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
					else
						try
						{
							SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch(NumberFormatException nfe)
						{
							if(!skill.isEmpty())
								_log.warn("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
						}
				}
			}
			ALLOW_SEVEN_SIGNS = altSettings.getProperty("ALLOW_SEVEN_SIGNS", true);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/altsettings.properties File.");
		}
		try
		{
			final ExProperties servicesSettings = load("./config/services.properties");
			SPAWN_CLASS_MASTERS = servicesSettings.getProperty("SpawnClassMasters", false);
			ALLOW_CLASS_MASTERS = servicesSettings.getProperty("AllowClassMasters", "0");
			ALLOW_CLASS_MASTERS_LIST = new ArrayList<Integer>();
			if(ALLOW_CLASS_MASTERS.length() != 0 && !ALLOW_CLASS_MASTERS.equals("0"))
				for(final String id2 : ALLOW_CLASS_MASTERS.split(","))
					ALLOW_CLASS_MASTERS_LIST.add(Integer.parseInt(id2));
			final String price = servicesSettings.getProperty("ClassMastersPrice", "0,0,0");
			CLASS_MASTERS_PRICE_LIST = new int[4];
			if(price.length() >= 5)
			{
				int level = 1;
				for(final String id3 : price.split(","))
				{
					CLASS_MASTERS_PRICE_LIST[level] = Integer.parseInt(id3);
					++level;
				}
			}
			final String items = servicesSettings.getProperty("ClassMastersPriceItem", "57,57,57");
			CLASS_MASTERS_PRICE_ITEM = new int[4];
			if(items.length() >= 5)
			{
				int level2 = 1;
				for(final String id4 : items.split(","))
				{
					CLASS_MASTERS_PRICE_ITEM[level2] = Integer.parseInt(id4);
					++level2;
				}
			}
			CLASS_MASTERS_REWARD = getIntArray(servicesSettings, "ClassMastersReward", new int[] { 6622, 1 });
			COUPONS_COUNT = servicesSettings.getProperty("CouponsCount", 0);
			GET_CLASS_SOC = servicesSettings.getProperty("GetClassSocial", true);
			ALLOW_REMOTE_CLASS_MASTERS = servicesSettings.getProperty("AllowRemoteClassMasters", false);
			EVER_BASE_CLASS = servicesSettings.getProperty("EverBaseClass", false);
			SERVICES_Buffer_Id = servicesSettings.getProperty("BufferId", 70034);
			BUFFER_BUFFS_TIME = servicesSettings.getProperty("BufferBuffsTime", 3600000L);
			SERVICES_Min_lvl = servicesSettings.getProperty("MinLevel", 1);
			SERVICES_Max_lvl = servicesSettings.getProperty("MaxLevel", 80);
			FREE_BUFFS_MAX_LVL = servicesSettings.getProperty("FreeBuffsMaxLevel", 0);
			BUFF_ITEM_ONE = servicesSettings.getProperty("BuffItem", 57);
			BUFF_PRICE_ONE = servicesSettings.getProperty("BuffPrice", 5000);
			HEAL_COIN = servicesSettings.getProperty("BuffItem", 57);
			HEAL_PRICE = servicesSettings.getProperty("HealPrice", 5000);
			BUFF_ITEM_GRP = servicesSettings.getProperty("BuffItemGroupAdena", 57);
			BUFF_PICE_GRP = servicesSettings.getProperty("BuffPiceGroupAdena", 10000);
			BUFF_ITEM_GRP_COIN = servicesSettings.getProperty("BuffItemGroup", 4037);
			BUFF_PICE_GRP_COIN = servicesSettings.getProperty("BuffPiceGroup", 4);
			SERVICES_Buffer_Siege = servicesSettings.getProperty("BufferSiege", false);
			BUFFER_ALLOW_PK = servicesSettings.getProperty("BufferAllowPK", true);
			NO_BUFFER_EPIC = servicesSettings.getProperty("NoBufferEpic", false);
			final String[] grps = servicesSettings.getProperty("GroupBuffs", "1068,3,1;1059,3,2").split(";");
			GROUP_BUFFS = new int[grps.length][3];
			for(int j = 0; j < grps.length; ++j)
			{
				final String[] grp = grps[j].split(",");
				GROUP_BUFFS[j][0] = Integer.parseInt(grp[0]);
				GROUP_BUFFS[j][1] = Integer.parseInt(grp[1]);
				GROUP_BUFFS[j][2] = Integer.parseInt(grp[2]);
			}
			BUFFER_EFFECTS = getIntArray(servicesSettings, "BufferEffects", new int[0]);
			BUFFER_SAVE_RESTOR = servicesSettings.getProperty("BufferSaveRestore", false);
			BUFFER_MAX_SCHEM = servicesSettings.getProperty("BufferMaxSchem", 5);
			BUFFER_SCHEM_NAME = servicesSettings.getProperty("BufferSchemName", "[A-Za-z0-9]{1,10}");
			SERVICES_CHANGE_NICK_ENABLED = servicesSettings.getProperty("NickChangeEnabled", false);
			SERVICES_CHANGE_NICK_PRICE = servicesSettings.getProperty("NickChangePrice", 100);
			SERVICES_CHANGE_NICK_ITEM = servicesSettings.getProperty("NickChangeItem", 4037);
			SERVICES_CHANGE_NICK_TEMPLATE = servicesSettings.getProperty("NickChangeTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{2,16}");
			SERVICES_CHANGE_NICK_SYMBOLS = servicesSettings.getProperty("NickChangeSymbols", "");
			SERVICES_CHANGE_SEX_ENABLED = servicesSettings.getProperty("SexChangeEnabled", false);
			SERVICES_CHANGE_SEX_PRICE = servicesSettings.getProperty("SexChangePrice", 100);
			SERVICES_CHANGE_SEX_ITEM = servicesSettings.getProperty("SexChangeItem", 4037);
			SERVICES_CHANGE_BASE_PRICE = servicesSettings.getProperty("BaseChangePrice", 100);
			SERVICES_CHANGE_BASE_ITEM = servicesSettings.getProperty("BaseChangeItem", 0);
			SERVICES_CHANGE_BASE_HERO = servicesSettings.getProperty("BaseChangeHero", false);
			SERVICES_CHANGE_PET_NAME_ENABLED = servicesSettings.getProperty("PetNameChangeEnabled", false);
			SERVICES_CHANGE_PET_NAME_PRICE = servicesSettings.getProperty("PetNameChangePrice", 100);
			SERVICES_CHANGE_PET_NAME_ITEM = servicesSettings.getProperty("PetNameChangeItem", 4037);
			SERVICES_CHANGE_NICK_COLOR_ENABLED = servicesSettings.getProperty("NickColorChangeEnabled", false);
			SERVICES_CHANGE_NICK_COLOR_PRICE = servicesSettings.getProperty("NickColorChangePrice", 100);
			SERVICES_CHANGE_NICK_COLOR_ITEM = servicesSettings.getProperty("NickColorChangeItem", 4037);
			SERVICES_CHANGE_NICK_COLOR_LIST = servicesSettings.getProperty("NickColorChangeList", "00FF00").split(";");
			SERVICES_CHANGE_NICK_COLOR_BLACK = servicesSettings.getProperty("NickColorBlack", 150);
			SERVICES_CHANGE_TITLE_COLOR_PRICE = servicesSettings.getProperty("TitleColorChangePrice", 100);
			SERVICES_CHANGE_TITLE = servicesSettings.getProperty("TitleColor", false);
			SERVICES_CHANGE_TITLE_COLOR_ITEM = servicesSettings.getProperty("TitleColorChangeItem", 4037);
			SERVICES_CHANGE_TITLE_COLOR_LIST = servicesSettings.getProperty("TitleColorChangeList", "00FF00").split(";");
			SERVICES_CHANGE_TITLE_COLOR_BLACK = servicesSettings.getProperty("TitleColorBlack", 150);
			SERVICES_BASH_ENABLED = servicesSettings.getProperty("BashEnabled", false);
			SERVICES_BASH_SKIP_DOWNLOAD = servicesSettings.getProperty("BashSkipDownload", false);
			SERVICES_BASH_RELOAD_TIME = servicesSettings.getProperty("BashReloadTime", 24);
			SERVICES_RATE_TYPE = servicesSettings.getProperty("RateBonusType", 0);
			SERVICES_RATE_BONUS_PRICE = getIntArray(servicesSettings, "RateBonusPrice", new int[] { 1500 });
			SERVICES_RATE_BONUS_ITEM = getIntArray(servicesSettings, "RateBonusItem", new int[] { 4037 });
			SERVICES_RATE_BONUS_VALUE = getFloatArray(servicesSettings, "RateBonusValue", new float[] { 1.25f });
			SERVICES_RATE_BONUS_DAYS = getIntArray(servicesSettings, "RateBonusTime", new int[] { 30 });
			SERVICES_RATE_BONUS_E_ENABLED = servicesSettings.getProperty("RateBonusEnchantEnabled", false);
			SERVICES_RATE_BONUS_E_W = servicesSettings.getProperty("RateBonusEnchantWeapon", 0d);
			SERVICES_RATE_BONUS_E_A = servicesSettings.getProperty("RateBonusEnchantArmor", 0d);
			SERVICES_RATE_BONUS_E_J = servicesSettings.getProperty("RateBonusEnchantAccessory", 0d);
			SERVICES_RATE_BONUS_AS = servicesSettings.getProperty("RateBonusAugmentSkill", 0);
			SERVICES_RATE_BONUS_NO_DROP_PK = servicesSettings.getProperty("RateBonusNoDropPK", false);
			SERVICES_NOBLESS_SELL_ENABLED = servicesSettings.getProperty("NoblessSellEnabled", false);
			SERVICES_NOBLESS_SELL_PRICE = servicesSettings.getProperty("NoblessSellPrice", 1000);
			SERVICES_NOBLESS_SELL_ITEM = servicesSettings.getProperty("NoblessSellItem", 4037);
			SERVICES_EXPAND_INVENTORY_ENABLED = servicesSettings.getProperty("ExpandInventoryEnabled", false);
			SERVICES_EXPAND_INVENTORY_PRICE = servicesSettings.getProperty("ExpandInventoryPrice", 1000);
			SERVICES_EXPAND_INVENTORY_ITEM = servicesSettings.getProperty("ExpandInventoryItem", 4037);
			SERVICES_EXPAND_INVENTORY_MAX = servicesSettings.getProperty("ExpandInventoryMax", 250);
			SERVICES_WASH_PK_ITEM = servicesSettings.getProperty("WashPkItem", 0);
			SERVICES_WASH_PK_PRICE = servicesSettings.getProperty("WashPkPrice", 10);
			SERVICES_WASH_PK_COUNT = servicesSettings.getProperty("WashPkCount", 1);
			SERVICES_HOW_TO_GET_COL = servicesSettings.getProperty("HowToGetCoL", false);
			SERVICES_OFFLINE_TRADE_ALLOW = servicesSettings.getProperty("AllowOfflineTrade", false);
			SERVICES_OFFLINE_TRADE_MIN_LEVEL = servicesSettings.getProperty("OfflineMinLevel", 0);
			SERVICES_ALLOW_OFFLINE_TRADE_NAME_COLOR = servicesSettings.getProperty("AllowOfflineTradeNameColor", true);
			SERVICES_OFFLINE_TRADE_NAME_COLOR = Integer.decode("0x" + servicesSettings.getProperty("OfflineTradeNameColor", "B0FFFF"));
			SERVICES_OFFLINE_TRADE_KICK_NOT_TRADING = servicesSettings.getProperty("KickOfflineNotTrading", true);
			SERVICES_OFFLINE_TRADE_PRICE_ITEM = servicesSettings.getProperty("OfflineTradePriceItem", 0);
			SERVICES_OFFLINE_TRADE_PRICE = servicesSettings.getProperty("OfflineTradePrice", 0);
			SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = servicesSettings.getProperty("OfflineTradeDaysToKick", 14L) * 60L * 60L * 24L;
			SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = servicesSettings.getProperty("OfflineRestoreAfterRestart", true);
			SERVICES_NO_TRADE_ONLY_OFFLINE = servicesSettings.getProperty("NoTradeOnlyOffline", false);
			SERVICES_TRADE_TAX = servicesSettings.getProperty("TradeTax", 0.0f);
			SERVICES_OFFSHORE_TRADE_TAX = servicesSettings.getProperty("OffshoreTradeTax", 0.0f);
			SERVICES_TRADE_TAX_ONLY_OFFLINE = servicesSettings.getProperty("TradeTaxOnlyOffline", false);
			SERVICES_OFFSHORE_NO_CASTLE_TAX = servicesSettings.getProperty("NoCastleTaxInOffshore", false);
			SERVICES_TRADE_RADIUS = servicesSettings.getProperty("TradeRadius", 30);
			SERVICES_TRADE_RADIUS_NPC = servicesSettings.getProperty("TradeRadiusNpc", 150);
			SERVICES_TRADE_ONLY_FAR = servicesSettings.getProperty("TradeOnlyFar", false);
			TRADE_ONLY_TOWNS = servicesSettings.getProperty("TradeOnlyTown", 0);
			TRADE_ONLY_GH = servicesSettings.getProperty("TradeOnlyGiranHarbor", false);
			SERVICES_GIRAN_HARBOR_ENABLED = servicesSettings.getProperty("GiranHarborZone", false);
			SERVICES_LOCK_ACCOUNT_IP = servicesSettings.getProperty("LockAccountIP", false);
			SERVICES_LOCK_CHAR_HWID = servicesSettings.getProperty("LockCharHWID", false);
			SERVICES_LOCK_ACC_HWID = servicesSettings.getProperty("LockAccountHWID", false);
			SERVICES_CHAR_KEY = servicesSettings.getProperty("CharKey", false);
			SERVICES_CHAR_FORCED_KEY = servicesSettings.getProperty("CharForcedKey", false);
			CHAR_KEY_SAVE_DB = servicesSettings.getProperty("CharKeySaveDB", false);
			CHAR_KEY_FAIL_KICK = servicesSettings.getProperty("CharKeyFailKick", 1);
			CHAR_KEY_FAIL_BAN = servicesSettings.getProperty("CharKeyFailBan", 5);
			GM_CAN_SEE_CHAR_KEY = servicesSettings.getProperty("GMCanSeeCharKey", false);
			CHAR_KEY_SAVE_DELAY = servicesSettings.getProperty("CharKeySaveDelay", 60);
			CHAR_KEY_BACKUP = servicesSettings.getProperty("CharKeyBackup", false);
			SERVICES_CHANGE_PASSWORD = servicesSettings.getProperty("ChangePassword", false);
			BLOCK_EXP = servicesSettings.getProperty("BlockExp", false);
			ALLOW_MY_INFO = servicesSettings.getProperty("AllowMyInfo", false);
			SERVICES_HERO_AURA = servicesSettings.getProperty("HeroAura", false);
			SERVICES_ALLOW_ROULETTE = servicesSettings.getProperty("AllowRoulette", false);
			SERVICES_ROULETTE_MIN_BET = servicesSettings.getProperty("RouletteMinBet", 1000);
			SERVICES_ROULETTE_MAX_BET = Integer.parseInt(servicesSettings.getProperty("RouletteMaxBet", String.valueOf(Integer.MAX_VALUE)));
			SERVICES_SP_ITEM = servicesSettings.getProperty("SpPriceItem", 0);
			SERVICES_LVL_UP_ITEM = servicesSettings.getProperty("LvlUpPriceItem", 0);
			SERVICES_LVL_DOWN_ITEM = servicesSettings.getProperty("LvlDownPriceItem", 0);
			SERVICES_SP_PRICE = servicesSettings.getProperty("SpPrice", 10);
			SERVICES_LVL_UP_PRICE = servicesSettings.getProperty("LvlUpPrice", 10);
			SERVICES_LVL_DOWN_PRICE = servicesSettings.getProperty("LvlDownPrice", 10);
			SERVICES_LVL_DOWN_CLASS = servicesSettings.getProperty("LvlDownClass", false);
			SERVICES_LVL80_ITEM = servicesSettings.getProperty("Lvl80Item", 0);
			SERVICES_LVL80_PRICE = servicesSettings.getProperty("Lvl80Price", 10);
			SERVICES_LVL_PAGE_PATH = servicesSettings.getProperty("LvlPagePath", "");
			SERVICES_OP_ITEM = servicesSettings.getProperty("OlyPointsItem", 0);
			SERVICES_OP_PRICE = servicesSettings.getProperty("OlyPointsPrice", 100);
			ALLOW_ESL = servicesSettings.getProperty("AllowESL", -4);
			SERVICES_ACC_MOVE_ITEM = servicesSettings.getProperty("AccMoveItem", 4037);
			SERVICES_ACC_MOVE_PRICE = servicesSettings.getProperty("AccMovePrice", 1000);
			SERVICES_CLAN_ITEM = servicesSettings.getProperty("ClanItemId", 0);
			SERVICES_CLAN_PAGE_PATH = servicesSettings.getProperty("ClanPagePath", "");
			SERVICES_CHANGE_CLAN_NAME_PRICE = servicesSettings.getProperty("ClanNameChangePrice", 1000);
			SERVICES_CLAN_SKILLS_PRICE = servicesSettings.getProperty("ClanSkillsPrice", 5000);
			SERVICES_CLAN_REP_COUNT = servicesSettings.getProperty("ClanRepCount", 1000);
			SERVICES_CLAN_REP_PRICE = servicesSettings.getProperty("ClanRepPrice", 100);
			SERVICES_CLAN_LVL1_PRICE = servicesSettings.getProperty("ClanLvl1Price", 1500);
			SERVICES_CLAN_LVL2_PRICE = servicesSettings.getProperty("ClanLvl2Price", 1500);
			SERVICES_CLAN_LVL3_PRICE = servicesSettings.getProperty("ClanLvl3Price", 1500);
			SERVICES_CLAN_LVL4_PRICE = servicesSettings.getProperty("ClanLvl4Price", 1500);
			SERVICES_CLAN_LVL5_PRICE = servicesSettings.getProperty("ClanLvl5Price", 1500);
			SERVICES_CLAN_LVL6_PRICE = servicesSettings.getProperty("ClanLvl6Price", 1500);
			SERVICES_CLAN_LVL7_PRICE = servicesSettings.getProperty("ClanLvl7Price", 1500);
			SERVICES_CLAN_LVL8_PRICE = servicesSettings.getProperty("ClanLvl8Price", 1500);
			TRANSFER_AUGMENT_ITEM = servicesSettings.getProperty("TransferAugmentItem", 0);
			TRANSFER_AUGMENT_PRICE = servicesSettings.getProperty("TransferAugmentPrice", 1000);

			// Augment shop service
			AUGMENT_SERVICE_COST_ITEM_ID = servicesSettings.getProperty("AUGMENT_SERVICE_COST_ITEM_ID", 4037);
			AUGMENT_SERVICE_COST_ITEM_COUNT = servicesSettings.getProperty("AUGMENT_SERVICE_COST_ITEM_COUNT", 50);
			AUGMENT_SERVICE_SKILLS_VARIATIONS_WARRIOR = servicesSettings.getProperty("AUGMENT_SERVICE_SKILLS_VARIATIONS_WARRIOR", new int[0], ";");
			AUGMENT_SERVICE_STATS_VARIATIONS_WARRIOR = servicesSettings.getProperty("AUGMENT_SERVICE_STATS_VARIATIONS_WARRIOR", new int[0], ";");
			AUGMENT_SERVICE_SKILLS_VARIATIONS_MAGE = servicesSettings.getProperty("AUGMENT_SERVICE_SKILLS_VARIATIONS_MAGE", new int[0], ";");
			AUGMENT_SERVICE_STATS_VARIATIONS_MAGE = servicesSettings.getProperty("AUGMENT_SERVICE_STATS_VARIATIONS_MAGE", new int[0], ";");

			SERVICES_HERO_STATUS_ENABLE = servicesSettings.getProperty("HeroStatusEnable", false);
			SERVICES_HERO_STATUS_PRICE = getIntArray(servicesSettings, "HeroStatusPrice", new int[] { 4037, 5000, 3 });
			SERVICES_ENABLE_NO_CARRIER = servicesSettings.getProperty("EnableNoCarrier", false);
			SERVICES_NO_CARRIER_MIN_TIME = servicesSettings.getProperty("NoCarrierMinTime", 0);
			SERVICES_NO_CARRIER_MAX_TIME = servicesSettings.getProperty("NoCarrierMaxTime", 90);
			SERVICES_NO_CARRIER_DEFAULT_TIME = servicesSettings.getProperty("NoCarrierDefaultTime", 60);
			ALLOW_DONATE_PARSE = servicesSettings.getProperty("AllowDonateParse", false);
			DONATE_PARSE_DELAY = servicesSettings.getProperty("DonateParseDelay", 300000L);
			ALLOW_ES_BONUS = servicesSettings.getProperty("AllowEnchantSkillBonus", false);
			ES_BONUS_PRICE = getIntArray(servicesSettings, "EnchantSkillBonusPrice", new int[0]);
			ES_BONUS_CHANCE = servicesSettings.getProperty("EnchantSkillBonusChance", 10);
			ALLOW_WEDDING = servicesSettings.getProperty("AllowWedding", false);
			WEDDING_ITEM = servicesSettings.getProperty("WeddingItem", 57);
			WEDDING_PRICE = servicesSettings.getProperty("WeddingPrice", 500000);
			WEDDING_DIVORCE_ITEM = servicesSettings.getProperty("WeddingDivorceItem", 57);
			WEDDING_DIVORCE_PRICE = servicesSettings.getProperty("WeddingDivorcePrice", 100000);
			WEDDING_PUNISH_INFIDELITY = servicesSettings.getProperty("WeddingPunishInfidelity", true);
			WEDDING_TELEPORT = servicesSettings.getProperty("WeddingTeleport", true);
			WEDDING_TELEPORT_PRICE = servicesSettings.getProperty("WeddingTeleportPrice", 500000);
			WEDDING_TELEPORT_INTERVAL = servicesSettings.getProperty("WeddingTeleportInterval", 120);
			WEDDING_SAMESEX = servicesSettings.getProperty("WeddingAllowSameSex", true);
			WEDDING_FORMALWEAR = servicesSettings.getProperty("WeddingFormalWear", true);
			WEDDING_CUPID_BOW = servicesSettings.getProperty("WeddingCupidBow", true);
			WEDDING_MALE_COLOR = Integer.decode("0x" + servicesSettings.getProperty("WeddingMaleColor", "FFFFFF"));
			WEDDING_FEMALE_COLOR = Integer.decode("0x" + servicesSettings.getProperty("WeddingFemaleColor", "FFFFFF"));
			L2TopManagerEnabled = servicesSettings.getProperty("L2TopManagerEnabled", false);
			L2TopManagerInterval = servicesSettings.getProperty("L2TopManagerInterval", 300000);
			L2TopWebAddress = servicesSettings.getProperty("L2TopWebAddress", "");
			L2TopSmsAddress = servicesSettings.getProperty("L2TopSmsAddress", "");
			L2TopServerAddress = servicesSettings.getProperty("L2TopServerAddress", "");
			L2TopPrefix = servicesSettings.getProperty("L2TopPrefix", "");
			L2TopSaveDays = servicesSettings.getProperty("L2TopSaveDays", 30);
			L2TopReward = getIntArray(servicesSettings, "L2TopReward", new int[0]);
			L2TopAccount = servicesSettings.getProperty("L2TopAccount", false);
			L2TopHWID = servicesSettings.getProperty("L2TopHWID", false);
			MMO_TOP_MANAGER_ENABLED = servicesSettings.getProperty("MMOTopEnable", false);
			MMO_TOP_MANAGER_INTERVAL = servicesSettings.getProperty("MMOTopManagerInterval", 300000);
			MMO_TOP_WEB_ADDRESS = servicesSettings.getProperty("MMOTopUrl", "");
			MMO_TOP_SAVE_DAYS = servicesSettings.getProperty("MMOTopSaveDays", 30);
			MMO_TOP_REWARD = getIntArray(servicesSettings, "MMOTopReward", new int[0]);

			ITEM_BROKER_ITEM_SEARCH = servicesSettings.getProperty("UseItemBrokerItemSearch", false);

			// Spawn npc from config
			SPAWN_FROM_CONFIG = servicesSettings.getProperty("SpawnFormConfig", true);
			if(SPAWN_FROM_CONFIG) {
				final String[] spwnList = servicesSettings.getProperty("SpawNpcFromConfig", "0").split(";");
				SPAWN_NPC_FROM_CONFIG = new int[spwnList.length][5];
				for (int j = 0; j < spwnList.length; ++j) {
					final String[] npcs = spwnList[j].split(",");
					SPAWN_NPC_FROM_CONFIG[j][0] = Integer.parseInt(npcs[0]);
					SPAWN_NPC_FROM_CONFIG[j][1] = Integer.parseInt(npcs[1]);
					SPAWN_NPC_FROM_CONFIG[j][2] = Integer.parseInt(npcs[2]);
					SPAWN_NPC_FROM_CONFIG[j][3] = Integer.parseInt(npcs[3]);
					SPAWN_NPC_FROM_CONFIG[j][4] = Integer.parseInt(npcs[4]);
				}
			}
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/services.properties File.");
		}

		try {
			final ExProperties tanosSettings = load("./config/tanos.properties");
			ID_PREMIUM_BUFF_IN_PROFILES_USE = Arrays.stream(tanosSettings.getProperty("IDPremiumBuffProfilBuffs", "4554,4553,4703,4702,4700,4699,4551,4552").split(","))
					.map(Integer::parseInt)
					.collect(Collectors.toList());
			ITEM_ID_PREMIUM_BUFF_IN_BUFF_PROFILE = tanosSettings.getProperty("ItemIDPremiumBuffProfilEachBuff", 4037);
			ITEM_COUNT_PREMIUM_BUFF_IN_BUFF_PROFILE = tanosSettings.getProperty("CostPremiumBuffProfilEachBuff", 1);




		}	catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/services.properties File.");
		}

		try
		{
			final ExProperties communitySettings = load("./config/community.properties");
			ALLOW_PVPCB_ABNORMAL = communitySettings.getProperty("AllowBBSAbnormal", false);
			PVPCB_ONLY_PEACE = communitySettings.getProperty("BBSOnlyPeace", false);
			ALLOW_PVPCB_BUFFER = communitySettings.getProperty("AllowBBSBuffer", true);
			PVPCB_BUFFER_PEACE = communitySettings.getProperty("BBSBufferPeace", false);
			NO_BUFF_EPIC = communitySettings.getProperty("NoBuffEpic", false);
			ALLOW_PVPCB_SHOP = communitySettings.getProperty("AllowBBSShop", true);
			ALLOW_PVPCB_SHOP_KARMA = communitySettings.getProperty("AllowBBSShopKarma", true);
			CB_MULTISELLS = getIntArray(communitySettings, "BBSShopMultisells", new int[0]);
			ALLOW_CB_ENCHANT = communitySettings.getProperty("AllowCBEnchant", true);
			CB_ENCH_ITEM = communitySettings.getProperty("CBEnchantItem", 4356);
			CB_ENCHANT_LVL_WEAPON = getIntArray(communitySettings, "CBEnchantLevelWeapon", new int[0]);


			CB_ENCHANT_LVL_ARMOR = getIntArray(communitySettings, "CBEnchantLevelArmor", new int[0]);
			CB_ENCHANT_PRICE_WEAPON = getIntArray(communitySettings, "CBEnchantPriceWeapon", new int[0]);
			CB_ENCHANT_PRICE_ARMOR = getIntArray(communitySettings, "CBEnchantPriceArmor", new int[0]);
			ALLOW_PVPCB_STAT = communitySettings.getProperty("AllowCBStat", true);
			CB_STAT_LIMIT_TOP_PVP = communitySettings.getProperty("CBStatLimitTopPvP", 10);
			CB_STAT_LIMIT_TOP_PK = communitySettings.getProperty("CBStatLimitTopPK", 10);
			CB_STAT_LIMIT_TOP_ONLINE = communitySettings.getProperty("CBStatLimitTopOnline", 10);
			CB_STAT_LIMIT_TOP_CLANS = communitySettings.getProperty("CBStatLimitTopClans", 10);
			CB_STAT_CASTLES = getIntArray(communitySettings, "CBStatCastles", new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
			CB_STAT_ONLINE = communitySettings.getProperty("CBStatOnline", true);
			CB_STAT_TABLE_WIDTH = communitySettings.getProperty("CBStatTableWidth", 570);
			CB_STAT_TD_WIDTH = communitySettings.getProperty("CBStatTdWidth", 250);
			ALLOW_PVPCB_TELEPORT = communitySettings.getProperty("AllowBBSTeleport", true);
			PVPCB_BUFFER_ALT_TIME = communitySettings.getProperty("AltBBSBufferTime", 3600000L);
			PVPCB_BUFFER_PRICE_ITEM = communitySettings.getProperty("AltBBSBufferPriceItem", 57);
			PVPCB_BUFFER_PRICE_ONE = communitySettings.getProperty("AltBBSBufferPriceOne", 10000);
			PVPCB_BUFFER_PRICE_GRP = Integer.parseInt(communitySettings.getProperty("AltBBSBufferPriceGrp", "100000"));
			PVPCB_BUFFER_PRICE_GRP_ADENA = Integer.parseInt(communitySettings.getProperty("BuffPiceGroupAdena", "100000"));
			ALLOW_DELUXE_BUFF = communitySettings.getProperty("AllowDeluxeBuff", false);
			DELUXE_BUFF_ITEM = communitySettings.getProperty("DeluxeBuffItem", 0);
			DELUXE_BUFF_COST = communitySettings.getProperty("DeluxeBuffCost", 0);
			DELUXE_BUFF_PRICE = communitySettings.getProperty("DeluxeBuffPrice", 10);
			DELUXE_BUFF_PAGE_PATH = communitySettings.getProperty("DeluxeBuffPagePath", "");
			DELUXE_BUFF_PREMIUM = communitySettings.getProperty("DeluxeBuffPremium", false);
			DELUXE_PREMIUM_PRICE = getIntArray(communitySettings, "DeluxePremiumPrice", new int[0]);
			DELUXE_PREMIUM_PAGE_PATH = communitySettings.getProperty("DeluxePremiumPagePath", "");
			DELUXE_NPC_PAGE_AFTER_BUY = communitySettings.getProperty("DeluxeNpcPageAfterBuy", false);
			DELUXE_PAGE_AFTER_BUY = communitySettings.getProperty("DeluxePageAfterBuy", "");
			ALLOW_DELUXE_EFFECTS = getIntArray(communitySettings, "AllowDeluxeEffects", new int[0]);
			ALLOW_PB_COMMAND = communitySettings.getProperty("AllowPremiumBuffCommand", true);
			MAX_BUFF_SCHEM = communitySettings.getProperty("MaxBuffSchem", 5);
			BUFF_SCHEM_NAME = communitySettings.getProperty("BuffSchemName", "[A-Za-z0-9]{1,10}");
			ALLOW_EFFECTS = getIntArray(communitySettings, "AllowEffects", new int[0]);
			PVPCB_BUFFER_MIN_LVL = communitySettings.getProperty("AltBBSBufferMinLvl", 1);
			PVPCB_BUFFER_MAX_LVL = communitySettings.getProperty("AltBBSBufferMaxLvl", 99);
			PVPCB_BUFFER_ALLOW_EVENT = communitySettings.getProperty("AltBBSBufferAllowOnEvent", true);
			PVPCB_BUFFER_ALLOW_SIEGE = communitySettings.getProperty("AltBBSBufferAllowOnSiege", true);
			PVPCB_BUFFER_ALLOW_PK = communitySettings.getProperty("AltBBSBufferAllowOnPk", true);
			PVP_BBS_TELEPORT_ADDITIONAL_RULES = communitySettings.getProperty("BBSTeleportAdditionalRules", false);
			PVP_BBS_TELE_ITEM = communitySettings.getProperty("BBSTeleItem", 57);
			PVP_BBS_TELE_PRICE = communitySettings.getProperty("BBSTelePrice", 5000);
			PVP_BBS_TELEPORT_ITEM = communitySettings.getProperty("BBSTeleSaveItem", 4037);
			PVP_BBS_TELEPORT_PRICE = communitySettings.getProperty("BBSTeleSavePrice", 5000);
			ALLOW_BBS_TELEPORT_SAVE = communitySettings.getProperty("AllowBBSTeleportSave", false);
			BBS_TELEPORT_SAVE_NAME = communitySettings.getProperty("BBSTeleportSaveName", "[A-Za-z0-9]{1,10}");
			BBS_TELEPORT_PEACE_SAVE = communitySettings.getProperty("BBSTeleportPeaceSave", false);
			PVP_BBS_TELEPORT_SAVE_COUNT = communitySettings.getProperty("BBSTeleportPointsCount", 5);
			PVP_BBS_TELEPORT_KARMA = communitySettings.getProperty("AllowBBSTeleportKarma", false);
			PVP_BBS_TELEPORT_PEACE = communitySettings.getProperty("BBSTeleportPeace", false);
			PVP_BBS_TELEPORT_SIEGE = communitySettings.getProperty("BBSTeleportSiege", false);
			NO_PVP_BBS_TELEPORT_EPIC = communitySettings.getProperty("NoBBSTeleportEpic", false);
			PVP_BBS_TELEPORT_LVL = communitySettings.getProperty("BBSTeleportLevel", 1);
			TELEPORT_FILTER = communitySettings.getProperty("TeleportFilter", false);
			if(TELEPORT_FILTER)
			{
				TELEPORT_LIST_FILTER = new ArrayList<String>();
				final String[] split11;
				final String[] param = split11 = communitySettings.getProperty("TeleportListFilter", "").split(";");
				for(final String a : split11)
					if(!a.trim().isEmpty())
						TELEPORT_LIST_FILTER.add(a);
			}
			ALLOW_MAIL = communitySettings.getProperty("AllowMail", false);
			EXPOSTB_COIN = communitySettings.getProperty("EpBriefCoin", 4037);
			EXPOSTB_PRICE = communitySettings.getProperty("EpBriefPrice", 1);
			EXPOSTB_NAME = communitySettings.getProperty("EpBriefCoinName", "Coin Of Luck");
			EXPOSTA_COIN = communitySettings.getProperty("EpItemCoin", 4037);
			EXPOSTA_PRICE = communitySettings.getProperty("EpItemPrice", 5);
			EXPOSTA_NAME = communitySettings.getProperty("EpItemCoinName", "Coin Of Luck");
			POST_CHARBRIEF = communitySettings.getProperty("NewbieBrief", false);
			POST_BRIEFTHEME = communitySettings.getProperty("BriefTheme", "");
			POST_BRIEFTEXT = communitySettings.getProperty("BriefText", "");
			POST_BRIEF_ITEM = communitySettings.getProperty("BriefItem", 57);
			POST_BRIEF_COUNT = communitySettings.getProperty("BriefCount", 1000);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/community.properties File.");
		}
		try
		{
			final ExProperties pvpSettings = load("./config/pvp.properties");
			KARMA_MIN_KARMA = pvpSettings.getProperty("MinKarma", 240);
			KARMA_XP_DIVIDER = pvpSettings.getProperty("XPDivider", 260);
			KARMA_LOST_BASE = pvpSettings.getProperty("BaseKarmaLost", 0);
			KARMA_DROP_GM = pvpSettings.getProperty("CanGMDropEquipment", false);
			KARMA_NEEDED_TO_DROP = pvpSettings.getProperty("KarmaNeededToDrop", true);
			KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,3500,3501,3502,4422,4423,4424,2375,6648,6649,6650,6842,6834,6835,6836,6837,6838,6839,6840,5575,7694,6841,8181");
			KARMA_DROP_ITEM_LIMIT = pvpSettings.getProperty("MaxItemsDroppable", 10);
			MIN_PK_TO_ITEMS_DROP = pvpSettings.getProperty("MinPKToDropItems", 5);
			DROP_ITEMS_ON_DIE = pvpSettings.getProperty("DropOnDie", false);
			KARMA_RANDOM_DROP_LOCATION_LIMIT = pvpSettings.getProperty("MaxDropThrowDistance", 70);
			KARMA_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfPKDropBase", 20d);
			KARMA_DROPCHANCE_MOD = pvpSettings.getProperty("ChanceOfPKDropMod", 1d);
			NORMAL_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfNormalDropBase", 1d);
			DROPCHANCE_EQUIPPED_WEAPON = pvpSettings.getProperty("ChanceOfDropWeapon", 3);
			DROPCHANCE_EQUIPMENT = pvpSettings.getProperty("ChanceOfDropEquipment", 17);
			DROPCHANCE_ITEM = pvpSettings.getProperty("ChanceOfDropOther", 80);
			KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();
			for(final String id2 : KARMA_NONDROPPABLE_ITEMS.split(","))
				KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id2));
			PVP_TIME = pvpSettings.getProperty("PvPTime", 40000);
			PVP_IP = pvpSettings.getProperty("PvPIP", false);
			PVP_HWID = pvpSettings.getProperty("PvPHWID", false);
			PVP_KILLS = pvpSettings.getProperty("PvPKills", false);
			PVP_KILLS_ZONES = getIntArray(pvpSettings, "PvPKillsZones", new int[0]);
			ALLOW_PVP_REWARD = pvpSettings.getProperty("AllowPvPReward", false);
			PVP_REWARD = getIntArray(pvpSettings, "PvPReward", new int[] { 57, 100 });
			PVP_REWARD_LVL_DIFF = pvpSettings.getProperty("PvPRewardLvlDiff", 5);
			PVP_REWARD_TIME = pvpSettings.getProperty("PvPRewardTime", 45L) * 1000L;
			ALLOW_PK_REWARD = pvpSettings.getProperty("AllowPKReward", false);
			PK_REWARD = getIntArray(pvpSettings, "PKReward", new int[] { 57, 100 });
			PK_REWARD_LVL_DIFF = pvpSettings.getProperty("PKRewardLvlDiff", 5);
			PK_REWARD_TIME = pvpSettings.getProperty("PKRewardTime", 45L) * 1000L;
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/pvp.properties File.");
		}
		try
		{
			final ExProperties aiSettings = load("./config/ai.properties");
			AI_TASK_MANAGER_COUNT = aiSettings.getProperty("AiTaskManagers", 1);
			if(!isPowerOfTwo(AI_TASK_MANAGER_COUNT))
				throw new RuntimeException("AiTaskManagers value should be power of 2!");
			AI_TASK_ATTACK_DELAY = aiSettings.getProperty("AiTaskDelay", 1000);
			AI_TASK_ACTIVE_DELAY = aiSettings.getProperty("AiTaskActiveDelay", 1000);
			BLOCK_ACTIVE_TASKS = aiSettings.getProperty("BlockActiveTasks", false);
			ALWAYS_TELEPORT_HOME = aiSettings.getProperty("AlwaysTeleportHome", false);
			MOBSLOOTERS = aiSettings.getProperty("MonstersLooters", false);
			RND_WALK = aiSettings.getProperty("RndWalk", true);
			RND_WALK_RATE = aiSettings.getProperty("RndWalkRate", 1);
			RND_ANIMATION_RATE = aiSettings.getProperty("RndAnimationRate", 2);
			AGGRO_CHECK_INTERVAL = aiSettings.getProperty("AggroCheckInterval", 250);
			AGGRO_CHECK_RADIUS = aiSettings.getProperty("AggroCheckRadius", 2000);
			AGGRO_CHECK_HEIGHT = aiSettings.getProperty("AggroCheckHeight", 400);
			GLOBAL_AGGRO = aiSettings.getProperty("GlobalAggro", 1000L);
			GUARD_ATTACK_AGGRO_MOB = aiSettings.getProperty("GuardAttackAggroMob", true);
			HATE_TIME = aiSettings.getProperty("HateTime", 900L) * 1000L;
			NPC_SEE_SPELL_RANGE = aiSettings.getProperty("NpcSeeSpellRange", 2000);
			MAX_DRIFT_RANGE = aiSettings.getProperty("MaxDriftRange", 300);
			MAX_PURSUE_UNDERGROUND_RANGE = aiSettings.getProperty("MaxPursueUndergroundRange", 4000);
			MAX_PURSUE_RANGE = aiSettings.getProperty("MaxPursueRange", 10000);
			MAX_PURSUE_RANGE_RAID = aiSettings.getProperty("MaxPursueRangeRaid", 5000);
			FACTION_NOTIFY_INTERVAL = aiSettings.getProperty("FactionNotifyInterval", 10000);
			MAX_ATTACK_TIMEOUT = aiSettings.getProperty("MaxAttackTimeout", 15000);
			TELEPORT_TIMEOUT = aiSettings.getProperty("TeleportTimeout", 10000);
			MAX_PATHFIND_FAILS = aiSettings.getProperty("MaxPathfindFails", 3);
			NO_TELEPORT_TO_TARGET = aiSettings.getProperty("NoTeleportToTarget", false);
			GEO_SP_LOC = false;
			final int[] cs = getIntArray(aiSettings, "GeoCorrectSpawnLoc", new int[0]);
			if(cs.length > 1)
			{
				GEO_SP_LOC = true;
				GEO_SP1 = cs[0];
				GEO_SP2 = cs[1];
			}
			ALT_AI_KELTIRS = aiSettings.getProperty("AltAiKeltirs", false);
			MIN_RESP_RAID_FIGHTER = aiSettings.getProperty("MinRespRaidFighter", 240000);
			MAX_RESP_RAID_FIGHTER = aiSettings.getProperty("MaxRespRaidFighter", 360000);
			SEVEN_SIGNS_CHECK = aiSettings.getProperty("SevenSignsCheck", true);
			BOSS_CAST_MIN_MP = aiSettings.getProperty("BossCastMinMP", 0);
			SAVE_BOSS_HP = aiSettings.getProperty("SaveBossHP", 90);
			LETHAL_IMMUNE_HP = aiSettings.getProperty("LethalImmuneHP", 50000);
			ZAKEN_DOOR_TIME = getIntArray(aiSettings, "ZakenDoorTime", new int[] { 120 });
			ZAKEN_TELEPORT_MIN_MP = aiSettings.getProperty("ZakenTeleportMinMP", 0);
			ZAKEN_CLEAR_ZONE = aiSettings.getProperty("ZakenClearZone", false);
			FWA_FIXINTERVALOFANTHARAS = aiSettings.getProperty("AntharasResp", 691200000);
			FWA_RANDOMINTERVALOFANTHARAS = aiSettings.getProperty("AntharasRndResp", 28800000);
			FWA_APPTIMEOFANTHARAS = aiSettings.getProperty("AntharasAppTime", 1200000);
			ANTHARAS_LIMITUNTILSLEEP = aiSettings.getProperty("AntharasUntilSleepTime", 1800000);
			ANTHARAS_ACTIVITY_TIME = aiSettings.getProperty("AntharasActivityTime", 7200000);
			ANTHARAS_CHECK_ANNIHILATED = aiSettings.getProperty("AntharasCheckAnnihilated", false);
			FWB_FIXINTERVALOFBAIUM = aiSettings.getProperty("BaiumResp", 432000000);
			FWB_RANDOMINTERVALOFBAIUM = aiSettings.getProperty("BaiumRndResp", 28800000);
			BAIUM_LIMITUNTILSLEEP = aiSettings.getProperty("BaiumUntilSleepTime", 1800000);
			BAIUM_ACTIVITY_TIME = aiSettings.getProperty("BaiumActivityTime", 7200000);
			BAIUM_CHECK_ANNIHILATED = aiSettings.getProperty("BaiumCheckAnnihilated", false);
			BAIUM_STATUE_SPAWN_TIME = aiSettings.getProperty("BaiumStatueSpawnTime", 0);
			FWF_FIXINTERVALOFFRINTEZZA = aiSettings.getProperty("FrintezzaResp", 172800000);
			FWF_RANDOMINTERVALOFFRINTEZZA = aiSettings.getProperty("FrintezzaRndResp", 3600000);
			FRINTEZZA_ACTIVITY_TIME = aiSettings.getProperty("FrintezzaActivityTime", 3600000);
			FWS_FIXINTERVALOFSAILRENSPAWN = aiSettings.getProperty("SailrenResp", 86400000);
			FWS_RANDOMINTERVALOFSAILRENSPAWN = aiSettings.getProperty("SailrenRndResp", 86400000);
			SAILREN_ACTIVITY_TIME = aiSettings.getProperty("SailrenActivityTime", 7200000);
			SAILREN_CHECK_ANNIHILATED = aiSettings.getProperty("SailrenCheckAnnihilated", false);
			FWV_FIXINTERVALOFVALAKAS = aiSettings.getProperty("ValakasResp", 950400000);
			FWV_RANDOMINTERVALOFVALAKAS = aiSettings.getProperty("ValakasRndResp", 0);
			FWV_APPTIMEOFVALAKAS = aiSettings.getProperty("ValakasAppTime", 1200000);
			VALAKAS_LIMITUNTILSLEEP = aiSettings.getProperty("ValakasUntilSleepTime", 1800000);
			VALAKAS_ACTIVITY_TIME = aiSettings.getProperty("ValakasActivityTime", 7200000);
			VALAKAS_CHECK_ANNIHILATED = aiSettings.getProperty("ValakasCheckAnnihilated", false);
			VALAKAS_WOLVES_KILL = aiSettings.getProperty("ValakasWolvesKill", true);
			VALAKAS_IXION_KILL = aiSettings.getProperty("ValakasIxionKill", true);
			NURSE_ANT_RESP = aiSettings.getProperty("NurseAntRespawn", 10000);

			String respawnPattern = aiSettings.getProperty("QueenAntFixResp", "");
			QUEEN_ANT_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);
			respawnPattern = aiSettings.getProperty("CoreFixResp", "");
			CORE_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);
			respawnPattern = aiSettings.getProperty("OrfenFixResp", "");
			ORFEN_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);
			respawnPattern = aiSettings.getProperty("ZakenFixResp", "");
			ZAKEN_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);
			respawnPattern = aiSettings.getProperty("BaiumFixResp", "");
			BAIUM_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);
			respawnPattern = aiSettings.getProperty("AntharasFixResp", "");
			ANTHARAS_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);
			respawnPattern = aiSettings.getProperty("ValakasFixResp", "");
			VALAKAS_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);
			respawnPattern = aiSettings.getProperty("FrintezzaFixResp", "");
			FRINTEZZA_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);
			respawnPattern = aiSettings.getProperty("SailrenFixResp", "");
			SAILREN_FIXRESP = StringUtils.isEmpty(respawnPattern) ? null : new SchedulingPattern(respawnPattern);

			BA_CHANCE = aiSettings.getProperty("BreakingArrowChance", 80);
			BA_MIN = aiSettings.getProperty("BreakingArrowMin", 1);
			BA_MAX = aiSettings.getProperty("BreakingArrowMax", 3);
			FS_PARTY_MEM_COUNT = aiSettings.getProperty("FourSepulchersEnterMin", 4);
			FS_SPAWN = Math.min(aiSettings.getProperty("FourSepulchersSpawn", -1), 3);
			SHADOW_SPAWN_DELAY = aiSettings.getProperty("ShadowSpawnDelay", 0);
			LIT_PARTY_MIN = aiSettings.getProperty("LastImperialTombPartyMin", 4);
			LIT_PARTY_MEM = aiSettings.getProperty("LastImperialTombPartyMembers", 7);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/ai.properties File.");
		}
		try
		{
			final ExProperties EventSettings = load("./config/events.properties");
			EVENT_CofferOfShadowsPriceRate = EventSettings.getProperty("CofferOfShadowsPriceRate", 1);
			EVENT_CofferOfShadowsRewardRate = EventSettings.getProperty("CofferOfShadowsRewardRate", 1f);
			EVENT_ClassmastersSellsSS = EventSettings.getProperty("CM_SellSS", false);
			EVENT_ClassmastersCoLShop = EventSettings.getProperty("CM_CoLShop", false);
			LastHero_Reward = getIntArray(EventSettings, "LastHero_reward", new int[] { 57, 5000 });
			LastHero_Time = EventSettings.getProperty("LastHero_time", 5);
			LastHero_Time_Paralyze = EventSettings.getProperty("LastHero_Time_Paralyze", 60);
			LastHero_Time_Battle = EventSettings.getProperty("LastHero_Time_Battle", 10);
			LastHero_Rate = EventSettings.getProperty("LastHero_rate", false);
			LastHero_IP = EventSettings.getProperty("LastHero_IP", false);
			LastHero_HWID = EventSettings.getProperty("LastHero_HWID", false);
			LastHero_Instance = EventSettings.getProperty("LastHero_Instance", true);
			LastHero_MinPlayers = EventSettings.getProperty("LastHero_MinPlayers", 4);
			LastHero_MaxPlayers = EventSettings.getProperty("LastHero_MaxPlayers", 70);
			LastHero_Cancel = EventSettings.getProperty("LastHero_CancelAllBuff", false);
			LastHero_SetHero = EventSettings.getProperty("LastHero_SetHero", false);
			LastHero_HeroTime = EventSettings.getProperty("LastHero_HeroTime", 0);
			LastHero_RewardFinal = getIntArray(EventSettings, "LastHero_reward_final", new int[] { 57, 10000 });
			LastHero_RateFinal = EventSettings.getProperty("LastHero_rate_final", false);
			LastHero_ReturnPoint = getIntArray(EventSettings, "LastHero_ReturnPoint", new int[0]);
			LastHero_Allow_Calendar_Day = EventSettings.getProperty("LastHero_Allow_Calendar_Day", false);
			LastHero_Time_Start = getIntArray(EventSettings, "LastHero_Time_Start", new int[] { 17, 30, 6 });
			LH_RESTRICTED_ITEMS = getIntArray(EventSettings, "LastHeroRestrictedItems", new int[0]);
			LH_RESTRICTED_SKILLS = getIntArray(EventSettings, "LastHeroRestrictedSkills", new int[0]);
			LH_BUFFS_FIGHTER = getIntArray(EventSettings, "LastHeroBuffsFighter", new int[0]);
			LH_BUFFS_MAGE = getIntArray(EventSettings, "LastHeroBuffsMage", new int[0]);
			LastHero_Zone = EventSettings.getProperty("LastHero_Zone", "[colosseum_battle]");
			LastHero_Loc = EventSettings.getProperty("LastHero_Loc", "149505,46719,-3410");
			LastHero_ClearLoc = EventSettings.getProperty("LastHero_ClearLoc", "147451,46728,-3410");
			LastHero_NoHero = EventSettings.getProperty("LastHero_NoHero", false);
			TvT_reward = getIntArray(EventSettings, "TvT_reward", new int[] { 57, 10000 });
			TvT_reward_final = getIntArray(EventSettings, "TvT_reward_final", new int[] { 57, 10000 });
			TvT_MinKills = EventSettings.getProperty("TvT_MinKills", 0);
			TvT_DrawReward = EventSettings.getProperty("TvT_DrawReward", false);
			TvT_reward_losers = getIntArray(EventSettings, "TvT_reward_losers", new int[0]);
			TvT_LosersMinKills = EventSettings.getProperty("TvT_LosersMinKills", 0);
			TvT_IP = EventSettings.getProperty("TvT_IP", false);
			TvT_HWID = EventSettings.getProperty("TvT_HWID", false);
			TvT_Instance = EventSettings.getProperty("TvT_Instance", true);
			TvT_MinPlayers = EventSettings.getProperty("TvT_MinPlayers", 4);
			TvT_MaxPlayers = EventSettings.getProperty("TvT_MaxPlayers", 54);
			TvT_CancelAllBuff = EventSettings.getProperty("TvT_CancelAllBuff", false);
			TvT_rate = EventSettings.getProperty("TvT_rate", false);
			TvT_Time = EventSettings.getProperty("TvT_time", 3);
			TvTResDelay = EventSettings.getProperty("TvT_ResDelay", 20);
			TvT_Time_Paralyze = EventSettings.getProperty("TvT_Time_Paralyze", 60);
			TvT_Time_Battle = EventSettings.getProperty("TvT_Time_Battle", 10);
			TvT_ReturnPoint = getIntArray(EventSettings, "TvT_ReturnPoint", new int[0]);
			TvT_Allow_Calendar_Day = EventSettings.getProperty("TvT_Allow_Calendar_Day", false);
			TvT_Time_Start = getIntArray(EventSettings, "TvT_Time_Start", new int[] { 18, 30, 6 });
			TvT_Zone = EventSettings.getProperty("TvT_Zone", "[colosseum_battle]");
			TvT_BlueTeamLoc = EventSettings.getProperty("TvT_BlueTeamLoc", "150545,46734,-3410");
			TvT_RedTeamLoc = EventSettings.getProperty("TvT_RedTeamLoc", "148386,46747,-3410");
			TvT_BlueTeamResLoc = EventSettings.getProperty("TvT_BlueTeamResLoc", "150545,46734,-3410");
			TvT_RedTeamResLoc = EventSettings.getProperty("TvT_RedTeamResLoc", "148386,46747,-3410");
			TvT_ClearLoc = EventSettings.getProperty("TvT_ClearLoc", "147451,46728,-3410");
			TvT_NonActionDelay = EventSettings.getProperty("TvT_NonActionDelay", 0);
			TvT_ShowKills = EventSettings.getProperty("TvT_ShowKills", false);
			TvT_CustomItems = EventSettings.getProperty("TvT_CustomItems", false);
			TvT_CustomItemsEnchant = EventSettings.getProperty("TvT_CustomItemsEnchant", 0);
			TVT_CRIT_DAMAGE_MAGIC = EventSettings.getProperty("TvT_CritDamageMagic", 4.0d);
			CtF_reward = getIntArray(EventSettings, "CtF_reward", new int[] { 57, 10000 });
			CtF_reward_final = getIntArray(EventSettings, "CtF_reward_final", new int[] { 57, 10000 });
			CtF_MinKills = EventSettings.getProperty("CtF_MinKills", 0);
			CtF_DrawReward = EventSettings.getProperty("CtF_DrawReward", false);
			CtF_reward_losers = getIntArray(EventSettings, "CtF_reward_losers", new int[0]);
			CtF_LosersMinKills = EventSettings.getProperty("CtF_LosersMinKills", 0);
			CtF_IP = EventSettings.getProperty("CtF_IP", false);
			CtF_HWID = EventSettings.getProperty("CtF_HWID", false);
			CtF_IP_Max = EventSettings.getProperty("CtF_IP_Max", 1);
			CtF_HWID_Max = EventSettings.getProperty("CtF_HWID_Max", 1);
			CtF_Instance = EventSettings.getProperty("CtF_Instance", false);
			CtF_MinPlayers = EventSettings.getProperty("CtF_MinPlayers", 4);
			CtF_MaxPlayers = EventSettings.getProperty("CtF_MaxPlayers", 54);
			CtF_CancelAllBuff = EventSettings.getProperty("CtF_CancelAllBuff", false);
			CtF_rate = EventSettings.getProperty("CtF_rate", false);
			CtF_Time = EventSettings.getProperty("CtF_time", 3);
			CtFResDelay = EventSettings.getProperty("CtF_ResDelay", 20);
			CtF_Flags = EventSettings.getProperty("CtF_Flags", 5);
			CtF_Time_Paralyze = EventSettings.getProperty("CtF_Time_Paralyze", 60);
			CtF_Time_Battle = EventSettings.getProperty("CtF_Time_Battle", 10);
			CtF_ReturnPoint = getIntArray(EventSettings, "CtF_ReturnPoint", new int[0]);
			CtF_Allow_Calendar_Day = EventSettings.getProperty("CtF_Allow_Calendar_Day", false);
			CtF_Time_Start = getIntArray(EventSettings, "CtF_Time_Start", new int[] { 18, 30, 6 });
			CtF_Zone = EventSettings.getProperty("CtF_Zone", "[colosseum_battle]");
			CtF_BlueTeamLoc = EventSettings.getProperty("CtF_BlueTeamLoc", "150545,46734,-3410");
			CtF_RedTeamLoc = EventSettings.getProperty("CtF_RedTeamLoc", "148386,46747,-3410");
			CtF_BlueTeamResLoc = EventSettings.getProperty("CtF_BlueTeamResLoc", "150545,46734,-3410");
			CtF_RedTeamResLoc = EventSettings.getProperty("CtF_RedTeamResLoc", "148386,46747,-3410");
			CtF_BlueFlagLoc = EventSettings.getProperty("CtF_BlueFlagLoc", "150399,46732,-3390");
			CtF_RedFlagLoc = EventSettings.getProperty("CtF_RedFlagLoc", "148501,46738,-3390");
			CtF_ClearLoc = EventSettings.getProperty("CtF_ClearLoc", "147451,46728,-3410");
			EVENTS_TIME_BACK = EventSettings.getProperty("EventsTimeBack", 30);
			EVENT_RESTRICTED_ITEMS = getIntArray(EventSettings, "EventRestrictedItems", new int[0]);
			EVENT_RESTRICTED_SKILLS = getIntArray(EventSettings, "EventRestrictedSkills", new int[0]);
			EVENT_RESTRICTED_SUMMONS = getIntArray(EventSettings, "EventRestrictedSummons", new int[0]);
			EVENT_BUFFS_FIGHTER = getIntArray(EventSettings, "EventBuffsFighter", new int[0]);
			EVENT_BUFFS_MAGE = getIntArray(EventSettings, "EventBuffsMage", new int[0]);
			EVENT_NO_ASK = EventSettings.getProperty("EventNoAsk", false);
			GvG_Time_Prepare = EventSettings.getProperty("GvG_Time_Prepare", 60);
			GvG_Time_Battle = EventSettings.getProperty("GvG_Time_Battle", 10);
			GvG_Time_Paralyze = EventSettings.getProperty("GvG_Time_Paralyze", 20);
			GvG_Min_Members = EventSettings.getProperty("GvG_Min_Members", 9);
			GvG_Max_Members = EventSettings.getProperty("GvG_Max_Members", 108);
			GvG_ItemIds = getIntArray(EventSettings, "GvG_ItemIds", new int[] { 57 });
			GvG_MinBids = getIntArray(EventSettings, "GvG_MinBids", new int[] { 1 });
			GvG_Zone = EventSettings.getProperty("GvG_Zone", "[gvg_pvp]");
			GvG_BlueTeamLoc = EventSettings.getProperty("GvG_BlueTeamLoc", "99575,-222293,-3751");
			GvG_RedTeamLoc = EventSettings.getProperty("GvG_RedTeamLoc", "99575,-207975,-3751");
			GvG_ClearLoc = EventSettings.getProperty("GvG_ClearLoc", "82840,148600,-3470");
			TFH_POLLEN_CHANCE = EventSettings.getProperty("TFH_POLLEN_CHANCE", 5f);
			GLIT_MEDAL_CHANCE = EventSettings.getProperty("MEDAL_CHANCE", 10);
			GLIT_GLITTMEDAL_CHANCE = EventSettings.getProperty("GLITTMEDAL_CHANCE", 5);
			GLIT_EnableRate = EventSettings.getProperty("GLIT_EnableRate", true);
			EVENT_L2DAY_LETTER_CHANCE = EventSettings.getProperty("L2DAY_LETTER_CHANCE", 1f);
			EVENT_CHANGE_OF_HEART_CHANCE = EventSettings.getProperty("EVENT_CHANGE_OF_HEART_CHANCE", 5f);
			EVENT_BOUNTY_HUNTERS_ENABLED = EventSettings.getProperty("BountyHuntersEnabled", true);
			HITMAN_ENABLE = EventSettings.getProperty("HitmanEnable", false);
			HITMAN_ORDER_DAYS = EventSettings.getProperty("HitmanOrderDays", 7);
			HITMAN_ITEM_ID = EventSettings.getProperty("HitmanItemId", 57);
			HITMAN_MIN_ITEM = EventSettings.getProperty("HitmanItemMin", 1);
			HITMAN_ITEM_NAME = EventSettings.getProperty("HitmanItemName", "Adena");
			HITMAN_ITEM_ID2 = EventSettings.getProperty("HitmanItemId2", 4037);
			HITMAN_MIN_ITEM2 = EventSettings.getProperty("HitmanItemMin2", 1);
			HITMAN_ITEM_NAME2 = EventSettings.getProperty("HitmanItemName2", "CoL");
			HITMAN_REVENGE_ENABLE = EventSettings.getProperty("HitmanRevengeEnable", false);
			HITMAN_REVENGE_PERCENT = EventSettings.getProperty("HitmanRevengePercent", 50);
			HITMAN_EXECUTE_PVP = EventSettings.getProperty("HitmanExecutePvP", true);
			HITMAN_EXECUTE_CLAN = EventSettings.getProperty("HitmanExecuteClan", true);
			HITMAN_ORDER_LIMIT = EventSettings.getProperty("HitmanOrderLimit", 5);
			HITMAN_LOSS_CHANCE = EventSettings.getProperty("HitmanLossChance", 50);
			HITMAN_LOGGING_ENABLE = EventSettings.getProperty("HitmanLoggerEnable", false);
			HITMAN_ANNOUNCE_ENABLE = EventSettings.getProperty("HitmanAnnounceEnable", false);
			HITMAN_ANNOUNCE_TEXT = EventSettings.getProperty("HitmanAnnounceText", "");
			HITMAN_ANNOUNCE_TIME = EventSettings.getProperty("HitmanAnnounceTime", 60);
			HITMAN_REVENGE_PERCENT = Math.max(Math.min(HITMAN_REVENGE_PERCENT, 100), 0);
			HITMAN_MIN_ITEM = Math.max(HITMAN_MIN_ITEM, 1);
			HITMAN_MIN_ITEM2 = Math.max(HITMAN_MIN_ITEM2, 1);
			VIKTORINA_ENABLED = EventSettings.getProperty("Victorina_Enabled", false);
			VIKTORINA_REMOVE_QUESTION = EventSettings.getProperty("Victorina_Remove_Question", false);
			VIKTORINA_REMOVE_QUESTION_NO_ANSWER = EventSettings.getProperty("Victorina_Remove_Question_No_Answer", false);
			VIKTORINA_START_TIME_HOUR = EventSettings.getProperty("Victorina_Start_Time_Hour", 12);
			VIKTORINA_START_TIME_MIN = EventSettings.getProperty("Victorina_Start_Time_Minute", 5);
			VIKTORINA_WORK_TIME = EventSettings.getProperty("Victorina_Work_Time", 2);
			VIKTORINA_TIME_ANSER = EventSettings.getProperty("Victorina_Time_Answer", 60);
			VIKTORINA_TIME_PAUSE = EventSettings.getProperty("Victorina_Time_Pause", 90);
			VIKTORINA_REWARD_FIRST = EventSettings.getProperty("Victorina_Reward_First", "57,10000,100;5575,1000,100");
			VIKTORINA_REWARD_OTHER = EventSettings.getProperty("Victorina_Reward_Other", "57,1000,100;5575,100,100");
			PCBANG_POINTS_ENABLED = EventSettings.getProperty("PcBangPointsEnabled", false);
			PCBANG_POINTS_BONUS_DOUBLE_CHANCE = EventSettings.getProperty("PcBangPointsDoubleChance", 10d);
			PCBANG_POINTS_BONUS = EventSettings.getProperty("PcBangPointsBonus", 0);
			PCBANG_POINTS_DELAY = EventSettings.getProperty("PcBangPointsDelay", 20);
			PCBANG_POINTS_MIN_LVL = EventSettings.getProperty("PcBangPointsMinLvl", 1);
			FIRST_NOBLESS = EventSettings.getProperty("FirstNobless", false);
			FIRST_NOBLESS_REWARD = getIntArray(EventSettings, "FirstNoblessReward", new int[] { 57, 1000 });
			ALLOW_LOTO = EventSettings.getProperty("AllowLoto", false);
			CUSTOM_BOX_DROP = EventSettings.getProperty("CustomBoxDrop", false);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/events.properties File.");
		}
		try
		{
			final ExProperties olympSettings = load("./config/olympiad.properties");
			ENABLE_OLYMPIAD = olympSettings.getProperty("EnableOlympiad", true);
			ENABLE_OLYMPIAD_SPECTATING = olympSettings.getProperty("EnableOlympiadSpectating", true);
			OLYMIAD_END_PERIOD_TIME = new SchedulingPattern(olympSettings.getProperty("OLYMIAD_END_PERIOD_TIME", "00 00 01 * *"));
			OLYMPIAD_START_TIME = new SchedulingPattern(olympSettings.getProperty("OLYMPIAD_START_TIME", "00 18 * *"));
			ENABLE_DECOY = olympSettings.getProperty("EnableDecoy", false);
			CLASS_GAME_MIN = olympSettings.getProperty("ClassGameMin", 5);
			NONCLASS_GAME_MIN = olympSettings.getProperty("NonClassGameMin", 9);
			OLY_CLASSED_GAMES_DAYS = getIntArray(olympSettings, "OLY_CLASSED_GAMES_DAYS", new int[]{ Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY });
			OLY_MIN_REG_POINTS = olympSettings.getProperty("OlyMinRegPoints", 1);
			OLY_COMP_WIN_ANNOUNCE = olympSettings.getProperty("OlyCompWinAnnounce", 0);
			ALT_OLY_CLASSED_RITEM_C = olympSettings.getProperty("AltOlyClassedRewItemCount", 50);
			ALT_OLY_NONCLASSED_RITEM_C = olympSettings.getProperty("AltOlyNonClassedRewItemCount", 30);
			OLY_MATCH_REWARD = getIntArray(olympSettings, "OlyMatchReward", new int[0]);
			OLY_RETURN_TIME = olympSettings.getProperty("OlyReturnTime", 40);
			OLY_NO_SAME_IP = olympSettings.getProperty("OlyNoSameIP", false);
			OLY_NO_SAME_PC = olympSettings.getProperty("OlyNoSamePC", false);
			HERO_CLAN_REP = olympSettings.getProperty("HeroClanRep", 1000);
			HERO_ITEMS = getIntArray(olympSettings, "HeroItems", new int[0]);
			OLYMPIAD_POINTS_DEFAULT = olympSettings.getProperty("OlympiadPointsDefault", 18);
			OLYMPIAD_POINTS_WEEKLY = olympSettings.getProperty("OlympiadPointsWeekly", 3);
			OLY_POINTS_HERO = olympSettings.getProperty("OlympiadPointsHero", 300);
			OLY_POINTS_MAX = olympSettings.getProperty("OlympiadPointsMax", 1000);
			OLY_COMP_WIN_HERO = olympSettings.getProperty("OlympiadCompWinHero", 1);
			OLY_COMP_DONE_HERO = olympSettings.getProperty("OlympiadCompDoneHero", 9);
			OLY_RANKING_PAST = olympSettings.getProperty("OlyRankingPast", true);
			OLY_SORT_LIST = olympSettings.getProperty("OlySortList", false);
			OLY_SAVE_DELAY = olympSettings.getProperty("OlySaveDelay", 10);
			OLY_ZONE_CHECK = olympSettings.getProperty("OlyZoneCheck", 0);
			ALT_OLY_CPERIOD = olympSettings.getProperty("AltOlyCPeriod", 21600000L);
			ALT_OLY_BATTLE = olympSettings.getProperty("AltOlyBattle", 6);
			ALT_OLY_WPERIOD = olympSettings.getProperty("AltOlyWPeriod", 604800000L);
			ALT_OLY_VPERIOD = olympSettings.getProperty("AltOlyVPeriod", 43200000L);
			OLY_END_WAIT_COMPS = olympSettings.getProperty("OlyEndWaitComps", true);
			OLY_RENEWAL_BEGIN = olympSettings.getProperty("OlyRenewalBegin", false);
			OLY_RENEWAL_END = olympSettings.getProperty("OlyRenewalEnd", false);
			OLY_BUFFS_PTS = olympSettings.getProperty("OlyBuffsPTS", false);
			OLY_RESET_CHARGES = olympSettings.getProperty("OlyResetCharges", false);
			ALLOW_OLY_HENNA = olympSettings.getProperty("AllowOlyHenna", false);
			OLY_RESTRICTED_ITEMS = getIntArray(olympSettings, "OlyRestrictedItems", new int[0]);
			OLY_RESTRICTED_SKILLS = getIntArray(olympSettings, "OlyRestrictedSkills", new int[0]);
			OLY_RESTRICTED_SUMMONS = getIntArray(olympSettings, "OlyRestrictedSummons", new int[0]);
			OLY_ENCHANT_LIMIT = olympSettings.getProperty("OlyEnchantLimit", false);
			OLY_ENCHANT_LIMIT_WEAPON = olympSettings.getProperty("OlyEnchantLimitWeapon", 4);
			OLY_ENCHANT_LIMIT_ARMOR = olympSettings.getProperty("OlyEnchantLimitArmor", 3);
		}
		catch(Exception e2)
		{
			e2.printStackTrace();
			throw new Error("Failed to Load ./config/olympiad.properties File.");
		}
		loadBuffs();
		abuseLoad();
		spamLoad();
		loadGMAccess();
        AddonsConfig.load();
	}

	public static void addClient(final String login, final GameClient client)
	{
		List<GameClient> list = clients.get(login);
		if(list == null)
		{
			list = new ArrayList<GameClient>();
			list.add(client);
			clients.put(login, list);
		}
		else
			list.add(client);
	}

	public static void removeClient(final String login)
	{
		final List<GameClient> list = clients.get(login);
		if(list != null)
		{
			for(final GameClient c : list)
				if(c != null)
					c.kick();
			clients.remove(login);
		}
	}
	
	public static void loadGMAccess()
	{
		gmlist.clear();
		loadGMAccess(new File(GM_PERSONAL_ACCESS_FILE));
		File dir = new File(GM_ACCESS_FILES_DIR);
		if(!dir.exists() || !dir.isDirectory())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists.");
			return;
		}
		for(File f : dir.listFiles())
			// hidden файлы НЕ игнорируем
			if(!f.isDirectory() && f.getName().endsWith(".xml"))
				loadGMAccess(f);
	}

	public static void loadGMAccess(File file)
	{
		try
		{
			Field fld;
			//File file = new File(filename);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			Document doc = factory.newDocumentBuilder().parse(file);

			for(Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
				for(Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if(!n.getNodeName().equalsIgnoreCase("char"))
						continue;

					PlayerAccess pa = new PlayerAccess();
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						Class<?> cls = pa.getClass();
						String node = d.getNodeName();

						if(node.equalsIgnoreCase("#text"))
							continue;
						try
						{
							fld = cls.getField(node);
						}
						catch(NoSuchFieldException e)
						{
							_log.info("Not found desclarate ACCESS name: " + node + " in XML Player access Object");
							continue;
						}

						if(fld.getType().getName().equalsIgnoreCase("boolean"))
							fld.setBoolean(pa, Boolean.parseBoolean(d.getAttributes().getNamedItem("set").getNodeValue()));
						else if(fld.getType().getName().equalsIgnoreCase("int"))
							fld.setInt(pa, Integer.valueOf(d.getAttributes().getNamedItem("set").getNodeValue()));
					}
					gmlist.put(pa.PlayerID, pa);
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void abuseLoad()
	{
		final List<Pattern> tmp = new ArrayList<Pattern>();
		LineNumberReader lnr = null;
		try
		{
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream("./config/Advanced/abusewords.txt"), "UTF-8"));
			String line;
			while((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
					tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", 98));
			}
			MAT_LIST = tmp.toArray(new Pattern[tmp.size()]);
			tmp.clear();
			_log.info("Abuse: Loaded " + MAT_LIST.length + " abuse words.");
		}
		catch(IOException e1)
		{
			_log.error("Error reading abuse", e1);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception ex)
			{}
		}
	}

	public static void spamLoad()
	{
		final List<Pattern> tmp = new ArrayList<Pattern>();
		LineNumberReader lnr = null;
		try
		{
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream("./config/Advanced/spamwords.txt"), "UTF-8"));
			String line;
			while((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
					tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", 98));
			}
			SPAM_LIST = tmp.toArray(new Pattern[tmp.size()]);
			tmp.clear();
			_log.info("Spam: Loaded " + SPAM_LIST.length + " spam words.");
		}
		catch(IOException e1)
		{
			_log.error("Error reading spam", e1);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception ex)
			{}
		}
	}

	public static boolean containsAbuseWord(final String s)
	{
		for(final Pattern pattern : MAT_LIST)
			if(pattern.matcher(s).matches())
				return true;
		return false;
	}

	public static boolean containsSpamWord(final String s)
	{
		for(final Pattern pattern : SPAM_LIST)
			if(pattern.matcher(s).matches())
				return true;
		return false;
	}

	private static void loadBuffs()
	{
		if(SERVICES_Buffer_Id > 0)
		{
			NPC_BUFFS = new HashMap<Integer, Integer>();
			for(int i = 0; i < BUFFER_EFFECTS.length; i += 2)
				NPC_BUFFS.put(BUFFER_EFFECTS[i], BUFFER_EFFECTS[i + 1]);
		}
		if(ALLOW_PVPCB_BUFFER)
		{
			CB_BUFFS = new HashMap<Integer, Integer>();
			for(int i = 0; i < ALLOW_EFFECTS.length; i += 2)
				CB_BUFFS.put(ALLOW_EFFECTS[i], ALLOW_EFFECTS[i + 1]);
		}
		if(ALLOW_DELUXE_BUFF)
		{
			DELUXE_BUFFS = new HashMap<Integer, Integer>();
			for(int i = 0; i < ALLOW_DELUXE_EFFECTS.length; i += 2)
				DELUXE_BUFFS.put(ALLOW_DELUXE_EFFECTS[i], ALLOW_DELUXE_EFFECTS[i + 1]);
		}
	}

	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}

	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();

		try
		{
			result.load(file);
		}
		catch(IOException e)
		{
			_log.error("Error loading config : " + file.getName() + "!");
		}

		return result;
	}

	private static boolean isPowerOfTwo(final int n)
	{
		return n != 0 && (n & n - 1) == 0x0;
	}

	private static int[] getIntArray(final ExProperties prop, final String name, final int[] _default)
	{
		final String s = prop.getProperty(name.trim());
		return s == null ? _default : Util.parseCommaSeparatedIntegerArray(s.trim());
	}

	private static float[] getFloatArray(final ExProperties prop, final String name, final float[] _default)
	{
		final String s = prop.getProperty(name.trim());
		return s == null ? _default : Util.parseCommaSeparatedFloatArray(s.trim());
	}

	private static double[] getDoubleArray(final ExProperties prop, final String name, final double[] _default)
	{
		final String s = prop.getProperty(name.trim());
		return s == null ? _default : Util.parseCommaSeparatedDoubleArray(s.trim());
	}

	public static float getRateAdena(final Player activeChar)
	{
		return RATE_DROP_ADENA * (activeChar == null ? 1.0f : activeChar.getRateAdena()) * RATE_DROP_ADENA_MULT_MOD + RATE_DROP_ADENA_STATIC_MOD;
	}

	static
	{
		NCPUS = Runtime.getRuntime().availableProcessors();
		MAT_LIST = new Pattern[0];
		SPAM_LIST = new Pattern[0];
		SELECTOR_CONFIG = new SelectorConfig();
		CNAME_MAXLEN = 32;
		CHAR_KEYS = new HashMap<Integer, String>();
		CHECKS = new HashMap<String, Integer>();
		gmlist = new HashMap<Integer, PlayerAccess>();
		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();
		clients = new HashMap<String, List<GameClient>>();
	}
}
