package l2s.gameserver.network.l2.s2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.utils.Location;

public class CharInfo extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharInfo.class);
	private static final Location _fLoc = new Location();

	private boolean _canWrite = false;
	private Inventory _inv;
	private int _mAtkSpd;
	private int _pAtkSpd;
	private int _runSpd;
	private int _walkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private int _swimSpd;
	private Location _loc;
	private Location _fishLoc;
	private String _name;
	private String _title;
	private int _objId;
	private int _race;
	private int _sex;
	private int base_class;
	private int pvp_flag;
	private int karma;
	private int rec_have;
	private int rec_left;
	private float speed_move;
	private float speed_atack;
	private double col_radius;
	private double col_height;
	private int hair_style;
	private int hair_color;
	private int face;
	private int abnormal_effect;
	private int clan_id;
	private int clan_crest_id;
	private int large_clan_crest_id;
	private int ally_id;
	private int ally_crest_id;
	private int class_id;
	private int maxCp;
	private int curCP;
	private byte _sit;
	private byte _run;
	private byte _combat;
	private byte _dead;
	private byte _invis;
	private byte private_store;
	private byte _enchant;
	private byte _team;
	private byte _noble;
	private boolean _hero;
	private byte _fishing;
	private byte mount_type;
	private int plg_class;
	private int pledge_type;
	private int cw_level;
	private int _nameColor;
	private int _title_color;
	private EffectCubic[] cubics;
	private boolean partyRoom;

	public CharInfo(Player player, Player receiver)
	{
		if(player == null)
		{
			System.out.println("CharInfo: player is null!");
			Thread.dumpStack();
			return;
		}

		if(receiver == null)
			return;

		if(player.isInvisible())
			return;

		if(player.isDeleting())
			return;

		_objId = player.getObjectId();
		if(_objId == 0)
			return;

		if(receiver.getObjectId() == _objId)
		{
			_log.error("You cant send CharInfo about his character to active user!!!");
			return;
		}

		_name = player.getVisibleName(receiver);
		_nameColor = player.getVisibleNameColor(receiver);

		if(player.isConnected() || player.isFashion || player.isInOfflineMode())//если чар в офлайн торговле
		{
			_title = player.getVisibleTitle(receiver);
			_title_color = player.getVisibleTitleColor(receiver);
		}
		else
		{
			_title = "NO CARRIER";
			_title_color = 255;
		}

		if(player.isPledgeVisible(receiver))
		{
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			//
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}

		cw_level = player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(player.getCursedWeaponEquippedId()) : 0;

		_inv = player.getInventory();
		if(_inv == null)
			return;

		if(player.isMounted())
		{
			_enchant = 0;
			mount_type = (byte) player.getMountType();
			_runSpd = player.getMountBaseSpeed();
		}
		else
		{
			_enchant = (byte) player.getEnchantEffect2();
			mount_type = 0;
			_runSpd = player.getTemplate().baseRunSpd;
		}

		_walkSpd = player.getTemplate().baseWalkSpd;
		speed_move = player.getMovementSpeedMultiplier();
		if(player.isInVehicle())
			_loc = player.getInVehiclePosition();
		if(_loc == null)
			_loc = player.getLoc();
		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = player.getPAtkSpd();
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
		_race = player.getBaseTemplate().race.ordinal();
		_sex = player.getSex();
		base_class = player.getBaseClassId();
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();
		speed_atack = player.getAttackSpeedMultiplier();
		col_radius = player.getCollisionRadius();
		col_height = player.getCollisionHeight();
		hair_style = player.getHairStyle();
		hair_color = player.getHairColor();
		face = player.getFace();
		_sit = (byte) (player.isSitting() ? 0 : 1);
		_run = (byte) (player.isRunning() ? 1 : 0);
		_combat = (byte) (player.isInCombat() ? 1 : 0);
		_dead = (byte) (player.isAlikeDead() && !player.isPendingRevive() ? 1 : 0);
		_invis = (byte) (player.isInvisible() ? 1 : 0);
		private_store = (byte) player.getPrivateStoreType();
		cubics = player.getCubics().toArray(new EffectCubic[player.getCubics().size()]);
		abnormal_effect = player.getAbnormalEffect();
		rec_left = player.getRecomLeft();
		rec_have = player.isGM() ? 0 : player.getRecomHave();
		class_id = player.getClassId().getId();
		maxCp = player.getMaxCp();
		curCP = (int) player.getCurrentCp();
		_team = (byte) player.getTeam();
		_noble = (byte) (player.isNoble() ? 1 : 0);
		_hero = !receiver.noHeroAura && (player.isHero() || player.isGM() && Config.GM_HERO_AURA);
		_fishing = (byte) (player.isFishing() ? 1 : 0);
		_fishLoc = player.getFishing() != null ? player.getFishLoc() : CharInfo._fLoc;
		_nameColor = player.getNameColor();
		plg_class = player.getPledgeClass();
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
		writeC(3);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(_objId);
		writeS(_name);
		writeD(_race);
		writeD(_sex);
		writeD(base_class);
		writeD(_inv.getPaperdollItemId(0));
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
		writeD(pvp_flag);
		writeD(karma);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
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
		writeF(speed_move);
		writeF(speed_atack);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeD(0);
		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(_invis);
		writeC(mount_type);
		writeC(private_store);
		writeH(cubics.length);
		for(final EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(partyRoom ? 1 : 0);
		writeD(abnormal_effect);
		writeC(rec_left);
		writeH(rec_have);
		writeD(class_id);
		writeD(maxCp);
		writeD(curCP);
		writeC(_enchant);
		writeC(_team);
		writeD(large_clan_crest_id);
		writeC(_noble);
		writeC(_hero);
		writeC(_fishing);
		writeD(_fishLoc.getX());
		writeD(_fishLoc.getY());
		writeD(_fishLoc.getZ());
		writeD(_nameColor);
		writeD(_loc.h);
		writeD(plg_class);
		writeD(pledge_type);
		writeD(_title_color);
		writeD(cw_level);
	}
}
