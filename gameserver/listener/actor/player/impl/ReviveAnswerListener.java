package l2s.gameserver.listener.actor.player.impl;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.Config;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.PetInstance;

public class ReviveAnswerListener implements OnAnswerListener
{
	private HardReference<Player> _playerRef;
	private double _power;
	private boolean _forPet;
	private boolean _salva;
	private String _name;
	private long _time;

	public ReviveAnswerListener(final Player player, final double power, final boolean forPet, final boolean salva, final String name)
	{
		_playerRef = player.getRef();
		_forPet = forPet;
		_power = power;
		_salva = salva;
		_name = name;
		_time = System.currentTimeMillis();
	}

	@Override
	public void sayYes()
	{
		final Player player = _playerRef.get();
		if(player == null)
			return;
		if(!player.isDead() && !_forPet || _forPet && player.getServitor() != null && !player.getServitor().isDead())
			return;
		if(Config.REVIVE_TIME > 0 && 1000L * Config.REVIVE_TIME + _time < System.currentTimeMillis())
		{
			player.sendMessage("Time is over.");
			return;
		}
		if(!_forPet)
		{
			player._salva = _salva;
			player.doRevive(_power);
			player.cancelSalva();
		}
		else if(player.getServitor() != null)
		{
			final PetInstance pet = (PetInstance) player.getServitor();
			pet._salva = _salva;
			pet.doRevive(_power);
			pet.cancelSalva();
		}
	}

	@Override
	public void sayNo()
	{}

	public double getPower()
	{
		return _power;
	}

	public boolean isForPet()
	{
		return _forPet;
	}

	public String getRevName()
	{
		return _name;
	}
}
