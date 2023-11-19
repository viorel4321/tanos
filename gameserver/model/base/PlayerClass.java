package l2s.gameserver.model.base;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import l2s.gameserver.Config;

public enum PlayerClass
{
	HumanFighter(Race.human, ClassType.Fighter, ClassLevel.First),
	Warrior(Race.human, ClassType.Fighter, ClassLevel.Second),
	Gladiator(Race.human, ClassType.Fighter, ClassLevel.Third),
	Warlord(Race.human, ClassType.Fighter, ClassLevel.Third),
	HumanKnight(Race.human, ClassType.Fighter, ClassLevel.Second),
	Paladin(Race.human, ClassType.Fighter, ClassLevel.Third),
	DarkAvenger(Race.human, ClassType.Fighter, ClassLevel.Third),
	Rogue(Race.human, ClassType.Fighter, ClassLevel.Second),
	TreasureHunter(Race.human, ClassType.Fighter, ClassLevel.Third),
	Hawkeye(Race.human, ClassType.Fighter, ClassLevel.Third),
	HumanMystic(Race.human, ClassType.Mystic, ClassLevel.First),
	HumanWizard(Race.human, ClassType.Mystic, ClassLevel.Second),
	Sorceror(Race.human, ClassType.Mystic, ClassLevel.Third),
	Necromancer(Race.human, ClassType.Mystic, ClassLevel.Third),
	Warlock(Race.human, ClassType.Mystic, ClassLevel.Third),
	Cleric(Race.human, ClassType.Priest, ClassLevel.Second),
	Bishop(Race.human, ClassType.Priest, ClassLevel.Third),
	Prophet(Race.human, ClassType.Priest, ClassLevel.Third),
	ElvenFighter(Race.elf, ClassType.Fighter, ClassLevel.First),
	ElvenKnight(Race.elf, ClassType.Fighter, ClassLevel.Second),
	TempleKnight(Race.elf, ClassType.Fighter, ClassLevel.Third),
	Swordsinger(Race.elf, ClassType.Fighter, ClassLevel.Third),
	ElvenScout(Race.elf, ClassType.Fighter, ClassLevel.Second),
	Plainswalker(Race.elf, ClassType.Fighter, ClassLevel.Third),
	SilverRanger(Race.elf, ClassType.Fighter, ClassLevel.Third),
	ElvenMystic(Race.elf, ClassType.Mystic, ClassLevel.First),
	ElvenWizard(Race.elf, ClassType.Mystic, ClassLevel.Second),
	Spellsinger(Race.elf, ClassType.Mystic, ClassLevel.Third),
	ElementalSummoner(Race.elf, ClassType.Mystic, ClassLevel.Third),
	ElvenOracle(Race.elf, ClassType.Priest, ClassLevel.Second),
	ElvenElder(Race.elf, ClassType.Priest, ClassLevel.Third),
	DarkElvenFighter(Race.darkelf, ClassType.Fighter, ClassLevel.First),
	PalusKnight(Race.darkelf, ClassType.Fighter, ClassLevel.Second),
	ShillienKnight(Race.darkelf, ClassType.Fighter, ClassLevel.Third),
	Bladedancer(Race.darkelf, ClassType.Fighter, ClassLevel.Third),
	Assassin(Race.darkelf, ClassType.Fighter, ClassLevel.Second),
	AbyssWalker(Race.darkelf, ClassType.Fighter, ClassLevel.Third),
	PhantomRanger(Race.darkelf, ClassType.Fighter, ClassLevel.Third),
	DarkElvenMystic(Race.darkelf, ClassType.Mystic, ClassLevel.First),
	DarkElvenWizard(Race.darkelf, ClassType.Mystic, ClassLevel.Second),
	Spellhowler(Race.darkelf, ClassType.Mystic, ClassLevel.Third),
	PhantomSummoner(Race.darkelf, ClassType.Mystic, ClassLevel.Third),
	ShillienOracle(Race.darkelf, ClassType.Priest, ClassLevel.Second),
	ShillienElder(Race.darkelf, ClassType.Priest, ClassLevel.Third),
	OrcFighter(Race.orc, ClassType.Fighter, ClassLevel.First),
	OrcRaider(Race.orc, ClassType.Fighter, ClassLevel.Second),
	Destroyer(Race.orc, ClassType.Fighter, ClassLevel.Third),
	OrcMonk(Race.orc, ClassType.Fighter, ClassLevel.Second),
	Tyrant(Race.orc, ClassType.Fighter, ClassLevel.Third),
	OrcMystic(Race.orc, ClassType.Mystic, ClassLevel.First),
	OrcShaman(Race.orc, ClassType.Mystic, ClassLevel.Second),
	Overlord(Race.orc, ClassType.Mystic, ClassLevel.Third),
	Warcryer(Race.orc, ClassType.Mystic, ClassLevel.Third),
	DwarvenFighter(Race.dwarf, ClassType.Fighter, ClassLevel.First),
	DwarvenScavenger(Race.dwarf, ClassType.Fighter, ClassLevel.Second),
	BountyHunter(Race.dwarf, ClassType.Fighter, ClassLevel.Third),
	DwarvenArtisan(Race.dwarf, ClassType.Fighter, ClassLevel.Second),
	Warsmith(Race.dwarf, ClassType.Fighter, ClassLevel.Third),
	DummyEntry1((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry2((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry3((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry4((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry5((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry6((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry7((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry8((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry9((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry10((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry11((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry12((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry13((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry14((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry15((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry16((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry17((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry18((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry19((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry20((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry21((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry22((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry23((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry24((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry25((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry26((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry27((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry28((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry29((Race) null, (ClassType) null, (ClassLevel) null),
	DummyEntry30((Race) null, (ClassType) null, (ClassLevel) null),
	Duelist(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	Dreadnought(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	PhoenixKnight(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	HellKnight(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	Sagittarius(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	Adventurer(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	Archmage(Race.human, ClassType.Mystic, ClassLevel.Fourth),
	Soultaker(Race.human, ClassType.Mystic, ClassLevel.Fourth),
	ArcanaLord(Race.human, ClassType.Mystic, ClassLevel.Fourth),
	Cardinal(Race.human, ClassType.Priest, ClassLevel.Fourth),
	Hierophant(Race.human, ClassType.Priest, ClassLevel.Fourth),
	EvaTemplar(Race.elf, ClassType.Fighter, ClassLevel.Fourth),
	SwordMuse(Race.elf, ClassType.Fighter, ClassLevel.Fourth),
	WindRider(Race.elf, ClassType.Fighter, ClassLevel.Fourth),
	MoonlightSentinel(Race.elf, ClassType.Fighter, ClassLevel.Fourth),
	MysticMuse(Race.elf, ClassType.Mystic, ClassLevel.Fourth),
	ElementalMaster(Race.elf, ClassType.Mystic, ClassLevel.Fourth),
	EvaSaint(Race.elf, ClassType.Priest, ClassLevel.Fourth),
	ShillienTemplar(Race.darkelf, ClassType.Fighter, ClassLevel.Fourth),
	SpectralDancer(Race.darkelf, ClassType.Fighter, ClassLevel.Fourth),
	GhostHunter(Race.darkelf, ClassType.Fighter, ClassLevel.Fourth),
	GhostSentinel(Race.darkelf, ClassType.Fighter, ClassLevel.Fourth),
	StormScreamer(Race.darkelf, ClassType.Mystic, ClassLevel.Fourth),
	SpectralMaster(Race.darkelf, ClassType.Mystic, ClassLevel.Fourth),
	ShillienSaint(Race.darkelf, ClassType.Priest, ClassLevel.Fourth),
	Titan(Race.orc, ClassType.Fighter, ClassLevel.Fourth),
	GrandKhauatari(Race.orc, ClassType.Fighter, ClassLevel.Fourth),
	Dominator(Race.orc, ClassType.Mystic, ClassLevel.Fourth),
	Doomcryer(Race.orc, ClassType.Mystic, ClassLevel.Fourth),
	FortuneSeeker(Race.dwarf, ClassType.Fighter, ClassLevel.Fourth),
	Maestro(Race.dwarf, ClassType.Fighter, ClassLevel.Fourth);

	private Race _race;
	private ClassLevel _level;
	private ClassType _type;
	private static final Set<PlayerClass> mainSubclassSet;
	private static final Set<PlayerClass> neverSubclassed;
	private static final Set<PlayerClass> subclasseSet1;
	private static final Set<PlayerClass> subclasseSet2;
	private static final Set<PlayerClass> subclasseSet3;
	private static final Set<PlayerClass> subclasseSet4;
	private static final Set<PlayerClass> subclasseSet5;
	private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap;

	private PlayerClass(final Race race, final ClassType type, final ClassLevel level)
	{
		_race = race;
		_level = level;
		_type = type;
	}

	public static EnumSet<PlayerClass> getSet(final Race race, final ClassLevel level)
	{
		final EnumSet<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);
		for(final PlayerClass playerClass : EnumSet.allOf(PlayerClass.class))
			if((race == null || playerClass.isOfRace(race)) && (level == null || playerClass.isOfLevel(level)))
				allOf.add(playerClass);
		return allOf;
	}

	public final boolean isOfRace(final Race race)
	{
		return _race == race;
	}

	public final boolean isOfType(final ClassType type)
	{
		return _type == type;
	}

	public final boolean isOfLevel(final ClassLevel level)
	{
		return _level == level;
	}

	public final Set<PlayerClass> getAvailableSubclasses()
	{
		Set<PlayerClass> subclasses = null;
		if(_level == ClassLevel.Third || _level == ClassLevel.Fourth)
			if(Config.ALT_GAME_ANY_SUBCLASS)
			{
				subclasses = EnumSet.copyOf(getSet(null, ClassLevel.Third));
				subclasses.remove(this);
			}
			else
			{
				subclasses = EnumSet.copyOf(PlayerClass.mainSubclassSet);
				subclasses.removeAll(PlayerClass.neverSubclassed);
				subclasses.remove(this);
				switch(_race)
				{
					case elf:
					{
						subclasses.removeAll(getSet(Race.darkelf, ClassLevel.Third));
						break;
					}
					case darkelf:
					{
						subclasses.removeAll(getSet(Race.elf, ClassLevel.Third));
						break;
					}
				}
				final Set<PlayerClass> unavaliableClasses = PlayerClass.subclassSetMap.get(this);
				if(unavaliableClasses != null)
					subclasses.removeAll(unavaliableClasses);
			}
		return subclasses;
	}

	static
	{
		neverSubclassed = EnumSet.of(PlayerClass.Overlord, PlayerClass.Warsmith);
		subclasseSet1 = EnumSet.of(PlayerClass.DarkAvenger, PlayerClass.Paladin, PlayerClass.TempleKnight, PlayerClass.ShillienKnight);
		subclasseSet2 = EnumSet.of(PlayerClass.TreasureHunter, PlayerClass.AbyssWalker, PlayerClass.Plainswalker);
		subclasseSet3 = EnumSet.of(PlayerClass.Hawkeye, PlayerClass.SilverRanger, PlayerClass.PhantomRanger);
		subclasseSet4 = EnumSet.of(PlayerClass.Warlock, PlayerClass.ElementalSummoner, PlayerClass.PhantomSummoner);
		subclasseSet5 = EnumSet.of(PlayerClass.Sorceror, PlayerClass.Spellsinger, PlayerClass.Spellhowler);
		subclassSetMap = new EnumMap<PlayerClass, Set<PlayerClass>>(PlayerClass.class);
		final Set<PlayerClass> subclasses = getSet(null, ClassLevel.Third);
		subclasses.removeAll(PlayerClass.neverSubclassed);
		mainSubclassSet = subclasses;
		PlayerClass.subclassSetMap.put(PlayerClass.DarkAvenger, PlayerClass.subclasseSet1);
		PlayerClass.subclassSetMap.put(PlayerClass.HellKnight, PlayerClass.subclasseSet1);
		PlayerClass.subclassSetMap.put(PlayerClass.Paladin, PlayerClass.subclasseSet1);
		PlayerClass.subclassSetMap.put(PlayerClass.PhoenixKnight, PlayerClass.subclasseSet1);
		PlayerClass.subclassSetMap.put(PlayerClass.TempleKnight, PlayerClass.subclasseSet1);
		PlayerClass.subclassSetMap.put(PlayerClass.EvaTemplar, PlayerClass.subclasseSet1);
		PlayerClass.subclassSetMap.put(PlayerClass.ShillienKnight, PlayerClass.subclasseSet1);
		PlayerClass.subclassSetMap.put(PlayerClass.ShillienTemplar, PlayerClass.subclasseSet1);
		PlayerClass.subclassSetMap.put(PlayerClass.TreasureHunter, PlayerClass.subclasseSet2);
		PlayerClass.subclassSetMap.put(PlayerClass.Adventurer, PlayerClass.subclasseSet2);
		PlayerClass.subclassSetMap.put(PlayerClass.AbyssWalker, PlayerClass.subclasseSet2);
		PlayerClass.subclassSetMap.put(PlayerClass.GhostHunter, PlayerClass.subclasseSet2);
		PlayerClass.subclassSetMap.put(PlayerClass.Plainswalker, PlayerClass.subclasseSet2);
		PlayerClass.subclassSetMap.put(PlayerClass.WindRider, PlayerClass.subclasseSet2);
		PlayerClass.subclassSetMap.put(PlayerClass.Hawkeye, PlayerClass.subclasseSet3);
		PlayerClass.subclassSetMap.put(PlayerClass.Sagittarius, PlayerClass.subclasseSet3);
		PlayerClass.subclassSetMap.put(PlayerClass.SilverRanger, PlayerClass.subclasseSet3);
		PlayerClass.subclassSetMap.put(PlayerClass.MoonlightSentinel, PlayerClass.subclasseSet3);
		PlayerClass.subclassSetMap.put(PlayerClass.PhantomRanger, PlayerClass.subclasseSet3);
		PlayerClass.subclassSetMap.put(PlayerClass.GhostSentinel, PlayerClass.subclasseSet3);
		PlayerClass.subclassSetMap.put(PlayerClass.Warlock, PlayerClass.subclasseSet4);
		PlayerClass.subclassSetMap.put(PlayerClass.ArcanaLord, PlayerClass.subclasseSet4);
		PlayerClass.subclassSetMap.put(PlayerClass.ElementalSummoner, PlayerClass.subclasseSet4);
		PlayerClass.subclassSetMap.put(PlayerClass.ElementalMaster, PlayerClass.subclasseSet4);
		PlayerClass.subclassSetMap.put(PlayerClass.PhantomSummoner, PlayerClass.subclasseSet4);
		PlayerClass.subclassSetMap.put(PlayerClass.SpectralMaster, PlayerClass.subclasseSet4);
		PlayerClass.subclassSetMap.put(PlayerClass.Sorceror, PlayerClass.subclasseSet5);
		PlayerClass.subclassSetMap.put(PlayerClass.Archmage, PlayerClass.subclasseSet5);
		PlayerClass.subclassSetMap.put(PlayerClass.Spellsinger, PlayerClass.subclasseSet5);
		PlayerClass.subclassSetMap.put(PlayerClass.MysticMuse, PlayerClass.subclasseSet5);
		PlayerClass.subclassSetMap.put(PlayerClass.Spellhowler, PlayerClass.subclasseSet5);
		PlayerClass.subclassSetMap.put(PlayerClass.StormScreamer, PlayerClass.subclasseSet5);
		PlayerClass.subclassSetMap.put(PlayerClass.Duelist, EnumSet.of(PlayerClass.Gladiator));
		PlayerClass.subclassSetMap.put(PlayerClass.Dreadnought, EnumSet.of(PlayerClass.Warlord));
		PlayerClass.subclassSetMap.put(PlayerClass.Soultaker, EnumSet.of(PlayerClass.Necromancer));
		PlayerClass.subclassSetMap.put(PlayerClass.Cardinal, EnumSet.of(PlayerClass.Bishop));
		PlayerClass.subclassSetMap.put(PlayerClass.Hierophant, EnumSet.of(PlayerClass.Prophet));
		PlayerClass.subclassSetMap.put(PlayerClass.SwordMuse, EnumSet.of(PlayerClass.Swordsinger));
		PlayerClass.subclassSetMap.put(PlayerClass.EvaSaint, EnumSet.of(PlayerClass.ElvenElder));
		PlayerClass.subclassSetMap.put(PlayerClass.SpectralDancer, EnumSet.of(PlayerClass.Bladedancer));
		PlayerClass.subclassSetMap.put(PlayerClass.Titan, EnumSet.of(PlayerClass.Destroyer));
		PlayerClass.subclassSetMap.put(PlayerClass.GrandKhauatari, EnumSet.of(PlayerClass.Tyrant));
		PlayerClass.subclassSetMap.put(PlayerClass.Dominator, EnumSet.of(PlayerClass.Overlord));
		PlayerClass.subclassSetMap.put(PlayerClass.Doomcryer, EnumSet.of(PlayerClass.Warcryer));
	}
}
