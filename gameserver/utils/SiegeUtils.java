package l2s.gameserver.utils;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.tables.SkillTable;

public class SiegeUtils
{
	public static void addSiegeSkills(final Player character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
		if(character.isNoble())
			character.addSkill(SkillTable.getInstance().getInfo(326, 1), false);
	}

	public static void removeSiegeSkills(final Player character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1), false);
		character.removeSkill(SkillTable.getInstance().getInfo(326, 1), false);
	}

	public static boolean getCanRide()
	{
		for(final Residence residence : ResidenceHolder.getInstance().getResidences()) {
			if (residence != null) {
				SiegeEvent<?, ?> siegeEvent = residence.getSiegeEvent();
				if(siegeEvent != null && siegeEvent.isInProgress())
					return false;
			}
		}
		return true;
	}
}
