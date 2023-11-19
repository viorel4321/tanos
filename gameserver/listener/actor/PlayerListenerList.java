package l2s.gameserver.listener.actor;

import l2s.commons.listener.Listener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;

public class PlayerListenerList extends CharListenerList
{
	public PlayerListenerList(final Player actor)
	{
		super(actor);
	}

	@Override
	public Player getActor()
	{
		return (Player) actor;
	}

	public void onEnter()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(final Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
		if(!getListeners().isEmpty())
			for(final Listener<Creature> listener : getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
	}

	public void onExit()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(final Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());
		if(!getListeners().isEmpty())
			for(final Listener<Creature> listener : getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());
	}

	public void onPartyInvite()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(final Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
		if(!getListeners().isEmpty())
			for(final Listener<Creature> listener : getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
	}

	public void onPartyLeave()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(final Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
		if(!getListeners().isEmpty())
			for(final Listener<Creature> listener : getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
	}
}
