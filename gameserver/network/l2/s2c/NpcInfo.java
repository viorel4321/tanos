package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.utils.Location;

public class NpcInfo extends L2GameServerPacket
{
	private boolean _canWrite = false;
	private Creature _cha;
	private int _npcObjId;
	private int _npcId;
	private int incombat;
	private int dead;
	private int team;
	private int _showSpawnAnimation;
	private int _runSpd;
	private int _walkSpd;
	private int _mAtkSpd;
	private int _pAtkSpd;
	private int _rhand;
	private int _lhand;
	private int karma;
	private int pvp_flag;
	private int _abnormalEffect;
	private int clan_crest_id;
	private int ally_crest_id;
	private int _titleColor;
	private int clan_id;
	private int ally_id;
	private double colHeight;
	private double colRadius;
	private float speed_move;
	private boolean _isAttackable;
	private Location _loc;
	private Location decoy_fishLoc;
	private String _name;
	private String _title;
	private Inventory decoy_inv;
	private int decoy_race;
	private int decoy_sex;
	private int decoy_base_class;
	private int decoy_clan_id;
	private int decoy_ally_id;
	private int decoy_noble;
	private int decoy_hair_style;
	private int decoy_hair_color;
	private int decoy_face;
	private int decoy_sitting;
	private int decoy_invis;
	private int decoy_rec_have;
	private int decoy_rec_left;
	private int decoy_class_id;
	private int decoy_maxCp;
	private int decoy_curCP;
	private int decoy_large_clan_crest_id;
	private int decoy_enchant;
	private int decoy_PledgeClass;
	private int decoy_pledge_type;
	private int decoy_NameColor;
	private int decoy_TitleColor;
	private int running;
	private int decoy_hero;
	private int decoy_swimSpd;
	private int decoy_cw_level;
	private byte decoy_mount_type;
	private byte decoy_private_store;
	private byte decoy_fishing;
	private double decoy_move_speed;
	private double decoy_attack_speed;
	private EffectCubic[] decoy_cubics;
	private boolean partyRoom;
	private boolean isFlying;

	public NpcInfo(NpcInstance npc, Creature attacker)
	{
		_name = npc.getVisibleName(attacker.getPlayer());
		_title = npc.getVisibleTitle(attacker.getPlayer());
		isFlying = false;
		if(npc == null)
			return;
		_cha = npc;
		_npcId = npc.getTemplate().displayId != 0 ? npc.getTemplate().displayId : npc.getTemplate().npcId;
		_isAttackable = npc.isAutoAttackable(attacker);
		_rhand = npc.getRightHandItem();
		_lhand = npc.getLeftHandItem();
		_titleColor = npc.isSummon() ? 1 : 0;
		_showSpawnAnimation = npc.getSpawnAnimation();
		common();
	}

	public NpcInfo(Servitor servitor, Creature attacker)
	{
		_name = servitor.getVisibleName(attacker.getPlayer());
		_title = servitor.getVisibleTitle(attacker.getPlayer());
		isFlying = false;
		if(servitor == null)
			return;
		final Player player = servitor.getPlayer();
		if(player != null && player.isInvisible())
			return;
		_cha = servitor;
		_npcId = servitor.getTemplate().npcId;
		_isAttackable = servitor.isAutoAttackable(attacker);
		_rhand = 0;
		_lhand = 0;
		_titleColor = servitor.isSummon() ? 1 : 0;
		_showSpawnAnimation = servitor.getSpawnAnimation();
		common();
	}

	public NpcInfo(Servitor servitor, Creature attacker, int showSpawnAnimation)
	{
		this(servitor, attacker);
		_showSpawnAnimation = showSpawnAnimation;
	}

	private void common()
	{
		colHeight = _cha.getCollisionHeight();
		colRadius = _cha.getCollisionRadius();
		_npcObjId = _cha.getObjectId();
		_loc = _cha.getLoc();
		_mAtkSpd = _cha.getMAtkSpd();
		if(Config.SHOW_NPC_CREST)
		{
			final Clan clan = _cha.getClan();
			final Alliance alliance = clan == null ? null : clan.getAlliance();
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}
		if(_cha.isDecoy())
			runImpl_Decoy();
		else
		{
			speed_move = _cha.getMovementSpeedMultiplier();
			_runSpd = _cha.getTemplate().baseRunSpd;
			_walkSpd = _cha.getTemplate().baseWalkSpd;
			karma = _cha.getKarma();
			pvp_flag = _cha.getPvpFlag();
			_pAtkSpd = _cha.getPAtkSpd();
			running = _cha.isRunning() ? 1 : 0;
			incombat = _cha.isInCombat() ? 1 : 0;
			dead = _cha.isAlikeDead() ? 1 : 0;
			_abnormalEffect = _cha.getAbnormalEffect();
			team = _cha.getTeam();
			isFlying = _cha.isFlying() && !_cha.isSummon();
		}
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
		if(_cha.isDecoy())
		{
			writeImpl_Decoy();
			return;
		}

		writeC(22);
		writeD(_npcObjId);
		writeD(_npcId + 1000000);
		writeD(_isAttackable ? 1 : 0);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_loc.h);
		writeD(0);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(speed_move);
		writeF(_pAtkSpd / 277.47833f);
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rhand);
		writeD(0);
		writeD(_lhand);
		writeC(1);
		writeC(running);
		writeC(incombat);
		writeC(dead);
		writeC(_showSpawnAnimation);
		writeS(_name);
		writeS(_title);
		writeD(_titleColor);
		writeD(pvp_flag);
		writeD(karma);
		writeD(_abnormalEffect);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeC(isFlying ? 2 : 0);
		writeC(team);
		writeF(colRadius);
		writeF(colHeight);
		writeD(0);
		writeD(isFlying ? 1 : 0);
	}

	private void runImpl_Decoy()
	{
		final Player cha_owner = _cha.getPlayer();
		_runSpd = cha_owner.getTemplate().baseRunSpd;
		_walkSpd = cha_owner.getTemplate().baseWalkSpd;
		karma = cha_owner.getKarma();
		pvp_flag = cha_owner.getPvpFlag();
		_pAtkSpd = cha_owner.getPAtkSpd();
		running = cha_owner.isRunning() ? 1 : 0;
		incombat = cha_owner.isInCombat() ? 1 : 0;
		dead = cha_owner.isAlikeDead() ? 1 : 0;
		_abnormalEffect = cha_owner.getAbnormalEffect();
		team = cha_owner.getTeam();
		if(cha_owner.isCursedWeaponEquipped())
		{
			_name = cha_owner.getName();
			_title = "";
			clan_crest_id = 0;
			ally_crest_id = 0;
			decoy_clan_id = 0;
			decoy_ally_id = 0;
			decoy_large_clan_crest_id = 0;
			decoy_cw_level = CursedWeaponsManager.getInstance().getLevel(cha_owner.getCursedWeaponEquippedId());
		}
		else
		{
			_name = cha_owner.getName();
			_title = cha_owner.getTitle();
			clan_crest_id = cha_owner.getClanCrestId();
			ally_crest_id = cha_owner.getAllyCrestId();
			decoy_clan_id = cha_owner.getClanId();
			decoy_ally_id = cha_owner.getAllyId();
			decoy_large_clan_crest_id = cha_owner.getClanCrestLargeId();
			decoy_cw_level = 0;
		}
		if(cha_owner.isMounted())
		{
			decoy_enchant = 0;
			decoy_mount_type = (byte) cha_owner.getMountType();
		}
		else
		{
			decoy_enchant = (byte) cha_owner.getEnchantEffect();
			decoy_mount_type = 0;
		}
		decoy_fishing = (byte) (cha_owner.isFishing() ? 1 : 0);
		decoy_fishLoc = cha_owner.getFishLoc();
		decoy_swimSpd = cha_owner.getSwimSpeed();
		decoy_private_store = (byte) cha_owner.getPrivateStoreType();
		decoy_inv = cha_owner.getInventory();
		decoy_race = cha_owner.getBaseTemplate().race.ordinal();
		decoy_sex = cha_owner.getSex();
		decoy_base_class = cha_owner.getBaseClassId();
		decoy_move_speed = cha_owner.getMovementSpeedMultiplier();
		decoy_attack_speed = cha_owner.getAttackSpeedMultiplier();
		decoy_hair_style = cha_owner.getHairStyle();
		decoy_hair_color = cha_owner.getHairColor();
		decoy_face = cha_owner.getFace();
		decoy_sitting = _cha.isSitting() ? 0 : 1;
		decoy_invis = 0;
		decoy_cubics = cha_owner.getCubics().toArray(new EffectCubic[cha_owner.getCubics().size()]);
		decoy_rec_left = cha_owner.getRecomLeft();
		decoy_rec_have = cha_owner.getPlayerAccess().IsGM ? 0 : cha_owner.getRecomHave();
		decoy_class_id = cha_owner.getClassId().getId();
		decoy_maxCp = cha_owner.getMaxCp();
		decoy_curCP = (int) cha_owner.getCurrentCp();
		decoy_noble = cha_owner.isNoble() ? 1 : 0;
		decoy_hero = cha_owner.isHero() || cha_owner.isGM() && Config.GM_HERO_AURA ? 1 : 0;
		decoy_NameColor = cha_owner.getNameColor();
		decoy_PledgeClass = cha_owner.getPledgeClass();
		decoy_pledge_type = cha_owner.getPledgeType();
		decoy_TitleColor = cha_owner.getTitleColor();
		partyRoom = cha_owner.getPartyRoom() != null;
	}

	private void writeImpl_Decoy()
	{
		final Player activeChar = (getClient()).getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.noHeroAura)
			decoy_hero = 0;
		writeC(3);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_loc.h);
		writeD(_npcObjId);
		writeS(_name);
		writeD(decoy_race);
		writeD(decoy_sex);
		writeD(decoy_base_class);
		writeD(decoy_inv.getPaperdollItemId(0));
		writeD(decoy_inv.getPaperdollItemId(6));
		writeD(decoy_inv.getPaperdollItemId(7));
		writeD(decoy_inv.getPaperdollItemId(8));
		writeD(decoy_inv.getPaperdollItemId(9));
		writeD(decoy_inv.getPaperdollItemId(10));
		writeD(decoy_inv.getPaperdollItemId(11));
		writeD(decoy_inv.getPaperdollItemId(12));
		writeD(decoy_inv.getPaperdollItemId(13));
		writeD(decoy_inv.getPaperdollItemId(7));
		writeD(decoy_inv.getPaperdollItemId(15));
		writeD(decoy_inv.getPaperdollItemId(16));
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(decoy_inv.getPaperdollVariation1Id(7));
		writeH(decoy_inv.getPaperdollVariation2Id(7));
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
		writeH(decoy_inv.getPaperdollVariation1Id(8));
		writeH(decoy_inv.getPaperdollVariation2Id(8));
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
		writeD(decoy_swimSpd);
		writeD(decoy_swimSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(decoy_move_speed);
		writeF(decoy_attack_speed);
		writeF(colRadius);
		writeF(colHeight);
		writeD(decoy_hair_style);
		writeD(decoy_hair_color);
		writeD(decoy_face);
		writeS(_title);
		writeD(decoy_clan_id);
		writeD(clan_crest_id);
		writeD(decoy_ally_id);
		writeD(ally_crest_id);
		writeD(0);
		writeC(decoy_sitting);
		writeC(running);
		writeC(incombat);
		writeC(dead);
		writeC(decoy_invis);
		writeC(decoy_mount_type);
		writeC(decoy_private_store);
		writeH(decoy_cubics.length);
		for(final EffectCubic decoy_cubic : decoy_cubics)
			writeH(decoy_cubic == null ? 0 : decoy_cubic.getId());
		writeC(partyRoom ? 1 : 0);
		writeD(_abnormalEffect);
		writeC(decoy_rec_left);
		writeH(decoy_rec_have);
		writeD(decoy_class_id);
		writeD(decoy_maxCp);
		writeD(decoy_curCP);
		writeC(decoy_enchant);
		writeC(team);
		writeD(decoy_large_clan_crest_id);
		writeC(decoy_noble);
		writeC(decoy_hero);
		writeC(decoy_fishing);
		writeD(decoy_fishLoc.getX());
		writeD(decoy_fishLoc.getY());
		writeD(decoy_fishLoc.getZ());
		writeD(decoy_NameColor);
		writeD(_loc.h);
		writeD(decoy_PledgeClass);
		writeD(decoy_pledge_type);
		writeD(decoy_TitleColor);
		writeD(decoy_cw_level);
	}

	@Override
	public String getType()
	{
		return super.getType() + (_cha != null ? " about " + _cha : "");
	}
}
