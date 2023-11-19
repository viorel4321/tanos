package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.utils.Location;

public class PetInfo extends L2GameServerPacket
{
	private int _runSpd;
	private int _walkSpd;
	private int MAtkSpd;
	private int PAtkSpd;
	private int pvp_flag;
	private int karma;
	private int _type;
	private int obj_id;
	private int npc_id;
	private int runing;
	private int incombat;
	private int dead;
	private int _sp;
	private int level;
	private int _abnormalEffect;
	private int curFed;
	private int maxFed;
	private int curHp;
	private int maxHp;
	private int curMp;
	private int maxMp;
	private int curLoad;
	private int maxLoad;
	private int PAtk;
	private int PDef;
	private int MAtk;
	private int MDef;
	private int Accuracy;
	private int Evasion;
	private int Crit;
	private int team;
	private int sps;
	private int ss;
	private int _spawnAnimation;
	private Location _loc;
	private double col_redius;
	private double col_height;
	private float speed_move;
	private long exp;
	private long exp_this_lvl;
	private long exp_next_lvl;
	private String _name;
	private String title;
	private boolean owner;
	private boolean rideable;

	public PetInfo(final Servitor summon)
	{
		_type = summon.getSummonType();
		obj_id = summon.getObjectId();
		npc_id = summon.getTemplate().npcId;
		_loc = summon.getLoc();
		MAtkSpd = summon.getMAtkSpd();
		PAtkSpd = summon.getPAtkSpd();
		speed_move = summon.getMovementSpeedMultiplier();
		_runSpd = summon.getBaseRunSpd();
		_walkSpd = summon.getTemplate().baseWalkSpd;
		col_redius = summon.getCollisionRadius();
		col_height = summon.getCollisionHeight();
		owner = summon.getPlayer() != null;
		runing = summon.isRunning() ? 1 : 0;
		incombat = summon.isInCombat() ? 1 : 0;
		dead = summon.isAlikeDead() ? 1 : 0;
		_name = summon.getVisibleName(summon.getPlayer());
		title = summon.getVisibleTitle(summon.getPlayer());
		pvp_flag = summon.getPvpFlag();
		karma = summon.getKarma();
		curFed = summon.getCurrentFed();
		maxFed = summon.getMaxFed();
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		_sp = summon.getSp();
		level = summon.getLevel();
		exp = summon.getExp();
		exp_this_lvl = summon.getExpForThisLevel();
		exp_next_lvl = summon.getExpForNextLevel();
		curLoad = summon.isPet() ? summon.getInventory().getTotalWeight() : 0;
		maxLoad = summon.getMaxLoad();
		PAtk = summon.getPAtk(null);
		PDef = summon.getPDef(null);
		MAtk = summon.getMAtk(null, null);
		MDef = summon.getMDef(null, null);
		Accuracy = summon.getAccuracy();
		Evasion = summon.getEvasionRate(null);
		Crit = summon.getCriticalHit(null, null);
		_abnormalEffect = summon.getAbnormalEffect();
		rideable = summon.isMountable();
		team = summon.getTeam();
		ss = summon.getSoulshotConsumeCount();
		sps = summon.getSpiritshotConsumeCount();
		_spawnAnimation = summon.getSpawnAnimation();
	}

	public PetInfo(final Servitor summon, final int animation)
	{
		this(summon);
		_spawnAnimation = animation;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(177);
		writeD(_type);
		writeD(obj_id);
		writeD(npc_id + 1000000);
		writeD(0);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_loc.h);
		writeD(0);
		writeD(MAtkSpd);
		writeD(PAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(speed_move);
		writeF(1.0);
		writeF(col_redius);
		writeF(col_height);
		writeD(0);
		writeD(0);
		writeD(0);
		writeC(owner ? 1 : 0);
		writeC(runing);
		writeC(incombat);
		writeC(dead);
		writeC(_spawnAnimation);
		writeS(_name);
		writeS(title);
		writeD(1);
		writeD(pvp_flag);
		writeD(karma);
		writeD(curFed);
		writeD(maxFed);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(_sp);
		writeD(level);
		writeQ(exp);
		writeQ(exp_this_lvl);
		writeQ(exp_next_lvl);
		writeD(curLoad);
		writeD(maxLoad);
		writeD(PAtk);
		writeD(PDef);
		writeD(MAtk);
		writeD(MDef);
		writeD(Accuracy);
		writeD(Evasion);
		writeD(Crit);
		writeD(_runSpd);
		writeD(PAtkSpd);
		writeD(MAtkSpd);
		writeD(_abnormalEffect);
		writeH(rideable ? 1 : 0);
		writeC(0);
		writeH(0);
		writeC(team);
		writeD(ss);
		writeD(sps);
	}
}
