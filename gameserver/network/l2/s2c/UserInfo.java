package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.utils.Location;

public class UserInfo extends L2GameServerPacket
{
	private boolean _canWrite = false;
	private boolean partyRoom;
	private int _runSpd;
	private int _walkSpd;
	private int _swimSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private int _relation;
	private float move_speed;
	private float attack_speed;
	private double col_radius;
	private double col_height;
	private PcInventory _inv;
	private Location _loc;
	private Location _fishLoc;
	private int obj_id;
	private int _race;
	private int sex;
	private int base_class;
	private int level;
	private int curCp;
	private int maxCp;
	private int _enchant;
	private int _weaponFlag;
	private long _exp;
	private int curHp;
	private int maxHp;
	private int curMp;
	private int maxMp;
	private int curLoad;
	private int maxLoad;
	private int rec_left;
	private int rec_have;
	private int _str;
	private int _con;
	private int _dex;
	private int _int;
	private int _wit;
	private int _men;
	private int _sp;
	private int ClanPrivs;
	private int InventoryLimit;
	private int _patk;
	private int _patkspd;
	private int _pdef;
	private int evasion;
	private int accuracy;
	private int crit;
	private int _matk;
	private int _matkspd;
	private int _mdef;
	private int pvp_flag;
	private int karma;
	private int hair_style;
	private int hair_color;
	private int face;
	private int gm_commands;
	private int clan_id;
	private int clan_crest_id;
	private int ally_id;
	private int ally_crest_id;
	private int large_clan_crest_id;
	private int private_store;
	private int can_crystalize;
	private int pk_kills;
	private int pvp_kills;
	private int class_id;
	private int team;
	private int AbnormalEffect;
	private int noble;
	private int hero;
	private int fishing;
	private int mount_id;
	private int cw_level;
	private int name_color;
	private int pledge_class;
	private int pledge_type;
	private int title_color;
	private int running;
	private byte mount_type;
	private String _name;
	private String _title;
	private EffectCubic[] cubics;

	public UserInfo(final Player player)
	{
		_name = player.getVisibleName(player);
		name_color = player.getVisibleNameColor(player);
		_title = player.getVisibleTitle(player);
		title_color = player.getVisibleTitleColor(player);

		if(player.isPledgeVisible(player))
		{
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clan_id = clan == null ? 0 : clan.getClanId();
			_relation = player.isClanLeader() ? 64 : 0;
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			//
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}

		cw_level = player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(player.getCursedWeaponEquippedId()) : 0;

		if(player.isMounted())
		{
			_enchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = (byte) player.getMountType();
			_runSpd = player.getMountBaseSpeed();
		}
		else
		{
			_enchant = (byte) player.getEnchantEffect2();
			mount_id = 0;
			mount_type = 0;
			_runSpd = player.getTemplate().baseRunSpd;
		}

		_walkSpd = player.getTemplate().baseWalkSpd;
		move_speed = player.getMovementSpeedMultiplier();
		_weaponFlag = player.getActiveWeaponInstance() == null ? 20 : 40;
		_flRunSpd = 0;
		_flWalkSpd = 0;
		if(player.isFlying())
		{
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		}
		else
		{
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}
		_swimSpd = player.getSwimSpeed();
		_inv = player.getInventory();
		for(final GlobalEvent e : player.getEvents())
			_relation = e.getUserRelation(player, _relation);
		_loc = player.getLoc();
		obj_id = player.getObjectId();
		_race = player.getRace().ordinal();
		sex = player.getSex();
		base_class = player.getBaseClassId();
		level = player.getLevel();
		_exp = player.getExp();
		_str = player.getSTR();
		_dex = player.getDEX();
		_con = player.getCON();
		_int = player.getINT();
		_wit = player.getWIT();
		_men = player.getMEN();
		curHp = (int) player.getCurrentHp();
		maxHp = player.getMaxHp();
		curMp = (int) player.getCurrentMp();
		maxMp = player.getMaxMp();
		curLoad = player.getCurrentLoad();
		maxLoad = player.getMaxLoad();
		_sp = player.getSp();
		_patk = player.getPAtk(null);
		_patkspd = player.getPAtkSpd();
		_pdef = player.getPDef(null);
		evasion = player.getEvasionRate(null);
		accuracy = player.getAccuracy();
		crit = player.getCriticalHit(null, null);
		_matk = player.getMAtk(null, null);
		_matkspd = player.getMAtkSpd();
		_mdef = player.getMDef(null, null);
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();
		attack_speed = player.getAttackSpeedMultiplier();
		col_radius = player.getCollisionRadius();
		col_height = player.getCollisionHeight();
		hair_style = player.getHairStyle();
		hair_color = player.getHairColor();
		face = player.getFace();
		gm_commands = player.isGM() || player.getPlayerAccess().CanUseGMCommand ? 1 : 0;
		
		if(player.isInvisible())
			_title += "[I]";

		ally_id = player.getAllyId();
		private_store = player.getPrivateStoreType();
		can_crystalize = player.getSkillLevel(248) > 0 ? 1 : 0;
		pk_kills = player.getPkKills();
		pvp_kills = player.getPvpKills();
		cubics = player.getCubics().toArray(new EffectCubic[player.getCubics().size()]);
		AbnormalEffect = player.getAbnormalEffect();
		ClanPrivs = player.getClanPrivileges();
		rec_left = player.getRecomLeft();
		rec_have = player.getPlayerAccess().IsGM ? 0 : player.getRecomHave();
		InventoryLimit = player.getInventoryLimit();
		class_id = player.getClassId().getId();
		maxCp = player.getMaxCp();
		curCp = (int) player.getCurrentCp();
		team = player.getTeam();
		noble = player.isNoble() ? 1 : 0;
		hero = !player.noHeroAura && (player.isHero() || player.isGM() && Config.GM_HERO_AURA) ? 1 : 0;
		fishing = player.isFishing() ? 1 : 0;
		_fishLoc = player.getFishLoc();
		running = player.isRunning() ? 1 : 0;
		pledge_class = player.getPledgeClass();
		pledge_type = player.getPledgeType();
		partyRoom = player.getPartyRoom() != null;
		_canWrite = true;
	}

	@Override
	protected boolean canWrite()
	{
		return _canWrite;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(4);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_loc.h);
		writeD(obj_id);
		writeS(_name);
		writeD(_race);
		writeD(sex);
		writeD(base_class);
		writeD(level);
		writeQ(_exp);
		writeD(_str);
		writeD(_dex);
		writeD(_con);
		writeD(_int);
		writeD(_wit);
		writeD(_men);
		writeD(maxHp);
		writeD(curHp);
		writeD(maxMp);
		writeD(curMp);
		writeD(_sp);
		writeD(curLoad);
		writeD(maxLoad);
		writeD(_weaponFlag);
		writeD(_inv.getPaperdollObjectId(0));
		writeD(_inv.getPaperdollObjectId(2));
		writeD(_inv.getPaperdollObjectId(1));
		writeD(_inv.getPaperdollObjectId(3));
		writeD(_inv.getPaperdollObjectId(5));
		writeD(_inv.getPaperdollObjectId(4));
		writeD(_inv.getPaperdollObjectId(6));
		writeD(_inv.getPaperdollObjectId(7));
		writeD(_inv.getPaperdollObjectId(8));
		writeD(_inv.getPaperdollObjectId(9));
		writeD(_inv.getPaperdollObjectId(10));
		writeD(_inv.getPaperdollObjectId(11));
		writeD(_inv.getPaperdollObjectId(12));
		writeD(_inv.getPaperdollObjectId(13));
		writeD(_inv.getPaperdollObjectId(7));
		writeD(_inv.getPaperdollObjectId(15));
		writeD(_inv.getPaperdollObjectId(16));
		writeD(_inv.getPaperdollItemId(0));
		writeD(_inv.getPaperdollItemId(2));
		writeD(_inv.getPaperdollItemId(1));
		writeD(_inv.getPaperdollItemId(3));
		writeD(_inv.getPaperdollItemId(5));
		writeD(_inv.getPaperdollItemId(4));
		writeD(_inv.getPaperdollItemId(6));
		writeD(_inv.getPaperdollItemId(7));
		writeD(_inv.getPaperdollItemId(8));
		writeD(_inv.getPaperdollItemId(9));
		writeD(_inv.getPaperdollItemId(10));
		writeD(_inv.getPaperdollItemId(11));
		writeD(_inv.getPaperdollItemId(12));
		writeD(_inv.getPaperdollItemId(13));
		writeD(_inv.getPaperdollItemId(7));
		writeD(_inv.getPaperdollItemId(15));
		writeD(_inv.getPaperdollItemId(16));
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(_inv.getPaperdollVariation1Id(7));
		writeH(_inv.getPaperdollVariation2Id(7));
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(_inv.getPaperdollVariation1Id(8));
		writeH(_inv.getPaperdollVariation2Id(8));
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeD(_patk);
		writeD(_patkspd);
		writeD(_pdef);
		writeD(evasion);
		writeD(accuracy);
		writeD(crit);
		writeD(_matk);
		writeD(_matkspd);
		writeD(_patkspd);
		writeD(_mdef);
		writeD(pvp_flag);
		writeD(karma);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimSpd);
		writeD(_swimSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(move_speed);
		writeF(attack_speed);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeD(gm_commands);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeD(_relation);
		writeC(mount_type);
		writeC(private_store);
		writeC(can_crystalize);
		writeD(pk_kills);
		writeD(pvp_kills);
		writeH(cubics.length);
		for(final EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(partyRoom ? 1 : 0);
		writeD(AbnormalEffect);
		writeC(0);
		writeD(ClanPrivs);
		writeH(rec_left);
		writeH(rec_have);
		writeD(mount_id);
		writeH(InventoryLimit);
		writeD(class_id);
		writeD(0);
		writeD(maxCp);
		writeD(curCp);
		writeC(_enchant);
		writeC(team);
		writeD(large_clan_crest_id);
		writeC(noble);
		writeC(hero);
		writeC(fishing);
		writeD(_fishLoc.getX());
		writeD(_fishLoc.getY());
		writeD(_fishLoc.getZ());
		writeD(name_color);
		writeC(running);
		writeD(pledge_class);
		writeD(pledge_type);
		writeD(title_color);
		writeD(cw_level);
	}
}
