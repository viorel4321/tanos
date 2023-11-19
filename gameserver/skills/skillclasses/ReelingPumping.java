package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Fishing;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.WeaponTemplate;

public class ReelingPumping extends Skill
{
	public ReelingPumping(final StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!((Player) activeChar).isFishing())
		{
			if(getSkillType() == SkillType.PUMPING)
				activeChar.sendPacket(new SystemMessage(1462));
			else
				activeChar.sendPacket(new SystemMessage(1463));
			activeChar.sendActionFailed();
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature caster, final Set<Creature> targets)
	{
		if(caster == null || !caster.isPlayer())
			return;
		final Player player = caster.getPlayer();
		final Fishing fishing = player.getFishing();
		if(fishing == null || !fishing.isInCombat())
			return;
		final WeaponTemplate weaponItem = player.getActiveWeaponItem();
		final int SS = player.getChargedFishShot() ? 2 : 1;
		int pen = 0;
		final double gradebonus = 1.0 + weaponItem.getItemGrade().ordinal() * 0.1;
		int dmg = (int) (this.getPower() * gradebonus * SS);
		if(player.getSkillLevel(1315) < getLevel() - 2)
		{
			player.sendPacket(new SystemMessage(1670));
			pen = 50;
			final int penatlydmg = dmg -= pen;
		}
		if(SS == 2)
			player.unChargeFishShot();
		fishing.useFishingSkill(dmg, pen, getSkillType());
	}
}
