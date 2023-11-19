package l2s.gameserver.listener.actor.ai;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.listener.AiListener;
import l2s.gameserver.model.Creature;

public interface OnAiIntentionListener extends AiListener
{
	void onAiIntention(final Creature p0, final CtrlIntention p1, final Object p2, final Object p3);
}
