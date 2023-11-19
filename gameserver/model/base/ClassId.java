package l2s.gameserver.model.base;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;

public enum ClassId
{
	/*0*/Fighter(ClassType.Fighter, Race.human, null, 1),
	/*1*/Warrior(ClassType.Fighter, Race.human, ClassId.Fighter, 2),
	/*2*/Gladiator(ClassType.Fighter, Race.human, ClassId.Warrior, 3),
	/*3*/Warlord(ClassType.Fighter, Race.human, ClassId.Warrior, 3),
	/*4*/Knight(ClassType.Fighter, Race.human, ClassId.Fighter, 2),
	/*5*/Paladin(ClassType.Fighter, Race.human, ClassId.Knight, 3),
	/*6*/DarkAvenger(ClassType.Fighter, Race.human, ClassId.Knight, 3),
	/*7*/Rogue(ClassType.Fighter, Race.human, ClassId.Fighter, 2),
	/*8*/TreasureHunter(ClassType.Fighter, Race.human, ClassId.Rogue, 3),
	/*9*/Hawkeye(ClassType.Fighter, Race.human, ClassId.Rogue, 3),
	/*10*/Mage(ClassType.Mystic, Race.human, null, 1),
	/*11*/Wizard(ClassType.Mystic, Race.human, ClassId.Mage, 2),
	/*12*/Sorceror(ClassType.Mystic, Race.human, ClassId.Wizard, 3),
	/*13*/Necromancer(ClassType.Mystic, Race.human, ClassId.Wizard, 3),
	/*14*/Warlock(ClassType.Mystic, Race.human, ClassId.Wizard, 3),
	/*15*/Cleric(ClassType.Priest, Race.human, ClassId.Mage, 2),
	/*16*/Bishop(ClassType.Priest, Race.human, ClassId.Cleric, 3),
	/*17*/Prophet(ClassType.Priest, Race.human, ClassId.Cleric, 3),
	/*18*/ElvenFighter(ClassType.Fighter, Race.elf, null, 1),
	/*19*/ElvenKnight(ClassType.Fighter, Race.elf, ClassId.ElvenFighter, 2),
	/*20*/TempleKnight(ClassType.Fighter, Race.elf, ClassId.ElvenKnight, 3),
	/*21*/SwordSinger(ClassType.Fighter, Race.elf, ClassId.ElvenKnight, 3),
	/*22*/ElvenScout(ClassType.Fighter, Race.elf, ClassId.ElvenFighter, 2),
	/*23*/PlainsWalker(ClassType.Fighter, Race.elf, ClassId.ElvenScout, 3),
	/*24*/SilverRanger(ClassType.Fighter, Race.elf, ClassId.ElvenScout, 3),
	/*25*/ElvenMage(ClassType.Mystic, Race.elf, null, 1),
	/*26*/ElvenWizard(ClassType.Mystic, Race.elf, ClassId.ElvenMage, 2),
	/*27*/Spellsinger(ClassType.Mystic, Race.elf, ClassId.ElvenWizard, 3),
	/*28*/ElementalSummoner(ClassType.Mystic, Race.elf, ClassId.ElvenWizard, 3),
	/*29*/Oracle(ClassType.Priest, Race.elf, ClassId.ElvenMage, 2),
	/*30*/Elder(ClassType.Priest, Race.elf, ClassId.Oracle, 3),
	/*31*/DarkFighter(ClassType.Fighter, Race.darkelf, null, 1),
	/*32*/PalusKnight(ClassType.Fighter, Race.darkelf, ClassId.DarkFighter, 2),
	/*33*/ShillienKnight(ClassType.Fighter, Race.darkelf, ClassId.PalusKnight, 3),
	/*34*/Bladedancer(ClassType.Fighter, Race.darkelf, ClassId.PalusKnight, 3),
	/*35*/Assassin(ClassType.Fighter, Race.darkelf, ClassId.DarkFighter, 2),
	/*36*/AbyssWalker(ClassType.Fighter, Race.darkelf, ClassId.Assassin, 3),
	/*37*/PhantomRanger(ClassType.Fighter, Race.darkelf, ClassId.Assassin, 3),
	/*38*/DarkMage(ClassType.Mystic, Race.darkelf, null, 1),
	/*39*/DarkWizard(ClassType.Mystic, Race.darkelf, ClassId.DarkMage, 2),
	/*40*/Spellhowler(ClassType.Mystic, Race.darkelf, ClassId.DarkWizard, 3),
	/*41*/PhantomSummoner(ClassType.Mystic, Race.darkelf, ClassId.DarkWizard, 3),
	/*42*/ShillienOracle(ClassType.Priest, Race.darkelf, ClassId.DarkMage, 2),
	/*43*/ShillienElder(ClassType.Priest, Race.darkelf, ClassId.ShillienOracle, 3),
	/*44*/OrcFighter(ClassType.Fighter, Race.orc, null, 1),
	/*45*/OrcRaider(ClassType.Fighter, Race.orc, ClassId.OrcFighter, 2),
	/*46*/Destroyer(ClassType.Fighter, Race.orc, ClassId.OrcRaider, 3),
	/*47*/OrcMonk(ClassType.Fighter, Race.orc, ClassId.OrcFighter, 2),
	/*48*/Tyrant(ClassType.Fighter, Race.orc, ClassId.OrcMonk, 3),
	/*49*/OrcMage(ClassType.Mystic, Race.orc, null, 1),
	/*50*/OrcShaman(ClassType.Mystic, Race.orc, ClassId.OrcMage, 2),
	/*51*/Overlord(ClassType.Mystic, Race.orc, ClassId.OrcShaman, 3),
	/*52*/Warcryer(ClassType.Mystic, Race.orc, ClassId.OrcShaman, 3),
	/*53*/DwarvenFighter(ClassType.Fighter, Race.dwarf, null, 1),
	/*54*/Scavenger(ClassType.Fighter, Race.dwarf, ClassId.DwarvenFighter, 2),
	/*55*/BountyHunter(ClassType.Fighter, Race.dwarf, ClassId.Scavenger, 3),
	/*56*/Artisan(ClassType.Fighter, Race.dwarf, ClassId.DwarvenFighter, 2),
	/*57*/Warsmith(ClassType.Fighter, Race.dwarf, ClassId.Artisan, 3),
	/*58*/dummyEntry1,
	/*59*/dummyEntry2,
	/*60*/dummyEntry3,
	/*61*/dummyEntry4,
	/*62*/dummyEntry5,
	/*63*/dummyEntry6,
	/*64*/dummyEntry7,
	/*65*/dummyEntry8,
	/*66*/dummyEntry9,
	/*67*/dummyEntry10,
	/*68*/dummyEntry11,
	/*69*/dummyEntry12,
	/*70*/dummyEntry13,
	/*71*/dummyEntry14,
	/*72*/dummyEntry15,
	/*73*/dummyEntry16,
	/*74*/dummyEntry17,
	/*75*/dummyEntry18,
	/*76*/dummyEntry19,
	/*77*/dummyEntry20,
	/*78*/dummyEntry21,
	/*79*/dummyEntry22,
	/*80*/dummyEntry23,
	/*81*/dummyEntry24,
	/*82*/dummyEntry25,
	/*83*/dummyEntry26,
	/*84*/dummyEntry27,
	/*85*/dummyEntry28,
	/*86*/dummyEntry29,
	/*87*/dummyEntry30,
	/*88*/Duelist(ClassType.Fighter, Race.human, ClassId.Gladiator, 4),
	/*89*/Dreadnought(ClassType.Fighter, Race.human, ClassId.Warlord, 4),
	/*90*/PhoenixKnight(ClassType.Fighter, Race.human, ClassId.Paladin, 4),
	/*91*/HellKnight(ClassType.Fighter, Race.human, ClassId.DarkAvenger, 4),
	/*92*/Sagittarius(ClassType.Fighter, Race.human, ClassId.Hawkeye, 4),
	/*93*/Adventurer(ClassType.Fighter, Race.human, ClassId.TreasureHunter, 4),
	/*94*/Archmage(ClassType.Mystic, Race.human, ClassId.Sorceror, 4),
	/*95*/Soultaker(ClassType.Mystic, Race.human, ClassId.Necromancer, 4),
	/*96*/ArcanaLord(ClassType.Mystic, Race.human, ClassId.Warlock, 4),
	/*97*/Cardinal(ClassType.Priest, Race.human, ClassId.Bishop, 4),
	/*98*/Hierophant(ClassType.Priest, Race.human, ClassId.Prophet, 4),
	/*99*/EvaTemplar(ClassType.Fighter, Race.elf, ClassId.TempleKnight, 4),
	/*100*/SwordMuse(ClassType.Fighter, Race.elf, ClassId.SwordSinger, 4),
	/*101*/WindRider(ClassType.Fighter, Race.elf, ClassId.PlainsWalker, 4),
	/*102*/MoonlightSentinel(ClassType.Fighter, Race.elf, ClassId.SilverRanger, 4),
	/*103*/MysticMuse(ClassType.Mystic, Race.elf, ClassId.Spellsinger, 4),
	/*104*/ElementalMaster(ClassType.Mystic, Race.elf, ClassId.ElementalSummoner, 4),
	/*105*/EvaSaint(ClassType.Priest, Race.elf, ClassId.Elder, 4),
	/*106*/ShillienTemplar(ClassType.Fighter, Race.darkelf, ClassId.ShillienKnight, 4),
	/*107*/SpectralDancer(ClassType.Fighter, Race.darkelf, ClassId.Bladedancer, 4),
	/*108*/GhostHunter(ClassType.Fighter, Race.darkelf, ClassId.AbyssWalker, 4),
	/*109*/GhostSentinel(ClassType.Fighter, Race.darkelf, ClassId.PhantomRanger, 4),
	/*110*/StormScreamer(ClassType.Mystic, Race.darkelf, ClassId.Spellhowler, 4),
	/*111*/SpectralMaster(ClassType.Mystic, Race.darkelf, ClassId.PhantomSummoner, 4),
	/*112*/ShillienSaint(ClassType.Priest, Race.darkelf, ClassId.ShillienElder, 4),
	/*113*/Titan(ClassType.Fighter, Race.orc, ClassId.Destroyer, 4),
	/*114*/GrandKhauatari(ClassType.Fighter, Race.orc, ClassId.Tyrant, 4),
	/*115*/Dominator(ClassType.Mystic, Race.orc, ClassId.Overlord, 4),
	/*116*/Doomcryer(ClassType.Mystic, Race.orc, ClassId.Warcryer, 4),
	/*117*/FortuneSeeker(ClassType.Fighter, Race.dwarf, ClassId.BountyHunter, 4),
	/*118*/Maestro(ClassType.Fighter, Race.dwarf, ClassId.Warsmith, 4);

	public static final ClassId[] VALUES = values();

	private final Race _race;
	private final ClassId _parent;
	private final int _level;
	private final ClassType _type;
	private final boolean _isDummy;

	private ClassId()
	{
		this(null, null, null, 0, true);
	}

	private ClassId(ClassType classType, Race race, ClassId parent, int level)
	{
		this(classType, race, parent, level, false);
	}

	private ClassId(ClassType classType, Race race, ClassId parent, int level, boolean isDummy)
	{
		_type = classType;
		_race = race;
		_parent = parent;
		_level = level;
		_isDummy = isDummy;
	}

	public final int getId()
	{
		return ordinal();
	}

	public final Race getRace()
	{
		return _race;
	}

	public final boolean isOfRace(Race race)
	{
		return _race == race;
	}

	public ClassType getType()
	{
		return _type;
	}

	public final boolean isOfType(ClassType type)
	{
		return _type == type;
	}

	public final boolean childOf(final ClassId cid)
	{
		return _parent != null && (_parent == cid || _parent.childOf(cid));
	}

	public final boolean equalsOrChildOf(final ClassId cid)
	{
		return this == cid || childOf(cid);
	}

	public final byte level()
	{
		if(_parent == null)
			return 0;
		return (byte) (1 + _parent.level());
	}

	public final ClassId getParent()
	{
		return _parent;
	}

	public final int getLevel()
	{
		return _level;
	}

	public final String getName(Player player)
	{
		return new CustomMessage("l2s.gameserver.model.base.ClassId.name." + getId()).toString(player);
	}
}
