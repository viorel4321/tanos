package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public final class EffectFear extends Abnormal
{
	public static final int FEAR_RANGE = 500;

	public EffectFear(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isFearImmune())
		{
			getEffector().sendPacket(Msg.TARGET_IS_INCORRECT);
			return false;
		}
		if(_effected.isSummon() && ((Servitor) _effected).isSiegeWeapon())
		{
			getEffector().sendPacket(Msg.TARGET_IS_INCORRECT);
			return false;
		}
		if(_effected.isInZonePeace())
		{
			getEffector().sendPacket(Msg.YOU_CANNOT_ATTACK_IN_THE_PEACE_ZONE);
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startFear();
		fearAction(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopFear();
		_effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public boolean onActionTime()
	{
		fearAction(false);
		return true;
	}

	private void fearAction(final boolean start)
	{
		final double radians = Math.toRadians(start ? Util.calculateAngleFrom(getEffector(), _effected) : Util.convertHeadingToDegree(_effected.getHeading()));
		int posX = (int) (_effected.getX() + 500.0 * Math.cos(radians));
		int posY = (int) (_effected.getY() + 500.0 * Math.sin(radians));
		final int posZ = _effected.getZ();
		if(Config.ALLOW_GEODATA)
		{
			final Location destiny = GeoEngine.moveCheck(_effected.getX(), _effected.getY(), posZ, posX, posY, _effected.getGeoIndex());
			if(destiny == null)
				return;

			posX = destiny.getX();
			posY = destiny.getY();
		}
		_effected.setRunning();
		_effected.moveToLocation(posX, posY, posZ, 0, false);
	}
}
