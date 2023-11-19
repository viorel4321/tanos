package l2s.gameserver.skills;

import java.util.NoSuchElementException;

public enum AbnormalEffect
{
	NULL("null", 0),
	BLEEDING("bleeding", 1),
	POISON("poison", 2),
	REDCIRCLE("redcircle", 4),
	ICE("ice", 8),
	AFFRAID("affraid", 16),
	CONFUSED("confused", 32),
	STUN("stun", 64),
	SLEEP("sleep", 128),
	MUTED("muted", 256),
	ROOT("root", 512),
	HOLD_1("hold1", 1024),
	HOLD_2("hold2", 2048),
	UNKNOWN_13("unk13", 4096),
	BIG_HEAD("bighead", 8192),
	FLAME("flame", 16384),
	UNKNOWN_16("unk16", 32768),
	GROW("grow", 65536),
	FLOATING_ROOT("floatroot", 131072),
	DANCE_STUNNED("dancestun", 262144),
	FIREROOT_STUN("firerootstun", 524288),
	STEALTH("shadow", 1048576),
	IMPRISIONING_1("imprison1", 2097152),
	IMPRISIONING_2("imprison2", 4194304),
	MAGIC_CIRCLE("magiccircle", 8388608),
	ICE2("ice2", 16777216),
	EARTHQUAKE("earthquake", 33554432),
	UNKNOWN_27("unk27", 67108864),
	INVULNERABLE("invul", 134217728);

	private final int _mask;
	private final String _name;

	private AbnormalEffect(final String name, final int mask)
	{
		_name = name;
		_mask = mask;
	}

	public final int getMask()
	{
		return _mask;
	}

	public final String getName()
	{
		return _name;
	}

	public static AbnormalEffect getByName(final String name)
	{
		for(final AbnormalEffect eff : values())
			if(eff.getName().equals(name))
				return eff;
		throw new NoSuchElementException("AbnormalEffect not found for name: '" + name + "'.\n Please check " + AbnormalEffect.class.getCanonicalName());
	}
}
