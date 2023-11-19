package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.WorldRegion;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectInvisible extends Abnormal
{
	public EffectInvisible(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(!_effected.isPlayer())
			return false;
		final Player player = (Player) _effected;
		return !player.isInvisible() && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		final Player player = (Player) _effected;
		player.setInvisible(true);
		player.sendUserInfo(true);
		if(player.getCurrentRegion() != null)
			for(final WorldRegion neighbor : player.getCurrentRegion().getNeighbors())
				if(neighbor != null)
					neighbor.removePlayerFromOtherPlayers(player);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		final Player player = (Player) _effected;
		if(!player.isInvisible())
			return;
		player.setInvisible(false);
		player.broadcastUserInfo(true);
		if(player.getServitor() != null)
			player.getServitor().broadcastCharInfo();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
