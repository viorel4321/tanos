package l2s.gameserver.model.base;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;

public enum ClassType
{
	Fighter,
	Mystic,
	Priest;

	public static final ClassType[] VALUES = values();
	public static final ClassType[] MAIN_TYPES = getMainTypes();

	public static ClassType[] getMainTypes()
	{
		return new ClassType[]{ Fighter, Mystic };
	}

	public ClassType getMainType()
	{
		if(this == Priest)
			return Mystic;
		return this;
	}

	public boolean isMagician()
	{
		return this != Fighter;
	}
	
	public boolean isHealer()
	{
		return this == Priest;
	}

	public final String getName(Player player)
	{
		return new CustomMessage("l2s.gameserver.model.base.ClassType.name." + getMainType().ordinal()).toString(player);
	}
}