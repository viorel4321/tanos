package l2s.gameserver.listener.actor;

import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.listener.actor.ai.OnAiEventListener;
import l2s.gameserver.listener.actor.ai.OnAiIntentionListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

public class CharListenerList extends ListenerList<Creature>
{
	static final ListenerList<Creature> global = new ListenerList<Creature>();
	protected final Creature actor;

	public CharListenerList(final Creature actor)
	{
		this.actor = actor;
	}

	public Creature getActor()
	{
		return actor;
	}

	public static final boolean addGlobal(final Listener<Creature> listener)
	{
		return global.add(listener);
	}

	public static final boolean removeGlobal(final Listener<Creature> listener)
	{
		return global.remove(listener);
	}

	public void onAiIntention(final CtrlIntention intention, final Object arg0, final Object arg1)
	{
		if(!getListeners().isEmpty())
			for(final Listener<Creature> listener : getListeners())
				if(OnAiIntentionListener.class.isInstance(listener))
					((OnAiIntentionListener) listener).onAiIntention(getActor(), intention, arg0, arg1);
	}

	public void onAiEvent(final CtrlEvent evt, final Object[] args)
	{
		if(!getListeners().isEmpty())
			for(final Listener<Creature> listener : getListeners())
				if(OnAiEventListener.class.isInstance(listener))
					((OnAiEventListener) listener).onAiEvent(getActor(), evt, args);
	}

	public void onDeath(final Creature killer)
	{
		if(!global.getListeners().isEmpty())
			for(final Listener<Creature> listener : global.getListeners())
				if(OnDeathListener.class.isInstance(listener))
					((OnDeathListener) listener).onDeath(getActor(), killer);

		if(!getListeners().isEmpty())
			for(final Listener<Creature> listener : getListeners())
				if(OnDeathListener.class.isInstance(listener))
					((OnDeathListener) listener).onDeath(getActor(), killer);
	}

	public void onCurrentHpDamage(final double damage, final Creature attacker, final Skill skill)
	{
		if(!global.getListeners().isEmpty())
			for(final Listener<Creature> listener : global.getListeners())
				if(OnCurrentHpDamageListener.class.isInstance(listener))
					((OnCurrentHpDamageListener) listener).onCurrentHpDamage(getActor(), damage, attacker, skill);

		if(!getListeners().isEmpty())
			for(final Listener<Creature> listener : getListeners())
				if(OnCurrentHpDamageListener.class.isInstance(listener))
					((OnCurrentHpDamageListener) listener).onCurrentHpDamage(getActor(), damage, attacker, skill);
	}

	public void onChangeCurrentCp(double oldCp, double newCp)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnChangeCurrentCpListener.class.isInstance(listener))
					((OnChangeCurrentCpListener) listener).onChangeCurrentCp(getActor(), oldCp, newCp);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnChangeCurrentCpListener.class.isInstance(listener))
					((OnChangeCurrentCpListener) listener).onChangeCurrentCp(getActor(), oldCp, newCp);
	}

	public void onChangeCurrentHp(double oldHp, double newHp)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnChangeCurrentHpListener.class.isInstance(listener))
					((OnChangeCurrentHpListener) listener).onChangeCurrentHp(getActor(), oldHp, newHp);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnChangeCurrentHpListener.class.isInstance(listener))
					((OnChangeCurrentHpListener) listener).onChangeCurrentHp(getActor(), oldHp, newHp);
	}

	public void onChangeCurrentMp(double oldMp, double newMp)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnChangeCurrentMpListener.class.isInstance(listener))
					((OnChangeCurrentMpListener) listener).onChangeCurrentMp(getActor(), oldMp, newMp);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnChangeCurrentMpListener.class.isInstance(listener))
					((OnChangeCurrentMpListener) listener).onChangeCurrentMp(getActor(), oldMp, newMp);
	}
}
