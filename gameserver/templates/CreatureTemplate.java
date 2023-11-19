package l2s.gameserver.templates;

public class CreatureTemplate
{
	private StatsSet _set;
	public final int baseSTR;
	public final int baseCON;
	public final int baseDEX;
	public final int baseINT;
	public final int baseWIT;
	public final int baseMEN;
	public final double baseHpMax;
	public final double baseMpMax;
	public double baseHpReg;
	public double baseMpReg;
	public final int basePAtk;
	public final int baseMAtk;
	public final int basePDef;
	public final int baseMDef;
	public final int basePAtkSpd;
	public final int baseMAtkSpd;
	public final double baseMReuseRate;
	public final int baseShldDef;
	public final int baseAtkRange;
	public final int baseShldRate;
	public final int baseCritRate;
	public final int baseRunSpd;
	public final int baseWalkSpd;
	public final boolean immobilized;
	public float collisionRadius;
	public float collisionHeight;

	public CreatureTemplate(final StatsSet set)
	{
		_set = set;
		baseSTR = set.getInteger("str", 40);
		baseCON = set.getInteger("con", 21);
		baseDEX = set.getInteger("dex", 30);
		baseINT = set.getInteger("int", 20);
		baseWIT = set.getInteger("wit", 43);
		baseMEN = set.getInteger("men", 20);
		baseHpMax = set.getDouble("hp", 0.0);
		baseMpMax = set.getDouble("mp", 0.0);
		baseHpReg = set.getDouble("hpRegen", 1.5);
		baseMpReg = set.getDouble("mpRegen", 0.9);
		basePAtk = set.getInteger("pAtk", 0);
		baseMAtk = set.getInteger("mAtk", 0);
		basePDef = set.getInteger("pDef", 0);
		baseMDef = set.getInteger("mDef", 0);
		basePAtkSpd = set.getInteger("atkSpd", 300);
		baseMAtkSpd = set.getInteger("baseMAtkSpd", 333);
		baseMReuseRate = set.getDouble("baseMReuseDelay", 1.0);
		baseShldDef = set.getInteger("baseShldDef", 0);
		baseAtkRange = set.getInteger("attackRange", 40);
		baseShldRate = set.getInteger("baseShldRate", 0);
		baseCritRate = set.getInteger("crit", 4);
		final int run = set.getInteger("runSpd", 0);
		baseRunSpd = Math.max(run, 1);
		baseWalkSpd = Math.max(set.getInteger("walkSpd", 80), 1);
		immobilized = run < 1;
		collisionRadius = (float) set.getDouble("radius", 5.0);
		collisionHeight = (float) set.getDouble("height", 5.0);
	}

	public int getId()
	{
		return 0;
	}

	public StatsSet getSet()
	{
		return _set;
	}

	public void setSet(final StatsSet set)
	{
		_set = set;
	}

	public static StatsSet getEmptyStatsSet()
	{
		final StatsSet npcDat = new StatsSet();
		npcDat.set("str", 0);
		npcDat.set("con", 0);
		npcDat.set("dex", 0);
		npcDat.set("int", 0);
		npcDat.set("wit", 0);
		npcDat.set("men", 0);
		npcDat.set("hp", 0);
		npcDat.set("mp", 0);
		npcDat.set("hpRegen", 0.003000000026077032);
		npcDat.set("mpRegen", 0.003000000026077032);
		npcDat.set("pAtk", 0);
		npcDat.set("mAtk", 0);
		npcDat.set("pDef", 100);
		npcDat.set("mDef", 100);
		npcDat.set("atkSpd", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("attackRange", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("crit", 0);
		npcDat.set("runSpd", 0);
		npcDat.set("walkSpd", 0);
		return npcDat;
	}
}
