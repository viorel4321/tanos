package l2s.gameserver.ai;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.Vehicle;

public class BoatAI extends CharacterAI
{
	public BoatAI(final Creature actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtArrived()
	{
		final Vehicle actor = (Vehicle) getActor();
		if(actor == null)
			return;
		actor.onEvtArrived();
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}
