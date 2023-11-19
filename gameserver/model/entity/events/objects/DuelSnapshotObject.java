package l2s.gameserver.model.entity.events.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;
import l2s.gameserver.utils.Location;

public class DuelSnapshotObject implements Serializable
{
	private final int _team;
	private final Player _player;
	private final List<Abnormal> _effects;
	private final Location _returnLoc;
	private final double _currentHp;
	private final double _currentMp;
	private final double _currentCp;
	private boolean _isDead;

	public DuelSnapshotObject(final Player player, final int team)
	{
		_player = player;
		_team = team;
		_returnLoc = player.getLoc();
		_currentCp = player.getCurrentCp();
		_currentHp = player.getCurrentHp();
		_currentMp = player.getCurrentMp();
		final List<Abnormal> effectList = player.getAbnormalList().values();
		_effects = new ArrayList<Abnormal>(effectList.size());
		for(final Abnormal e : effectList)
			if(!e.getSkill().isToggle() && (!Config.DEL_AUGMENT_BUFFS || !e.getSkill().isItemSkill()))
			{
				final Abnormal effect = e.getTemplate().getEffect(new Env(e.getEffector(), e.getEffected(), e.getSkill()));
				effect.setCount(e.getCount());
				effect.setPeriod(e.getCount() == 1 ? e.getPeriod() - e.getTime() : e.getPeriod());
				_effects.add(effect);
			}
	}

	public void restore(final boolean abnormal)
	{
		if(!abnormal && !_player.isInOlympiadMode())
		{
			_player.getAbnormalList().stopAll();
			for(final Abnormal e : _effects)
				_player.getAbnormalList().add(e);
			_player.setCurrentCp(_currentCp);
			_player.setCurrentHpMp(_currentHp, _currentMp, false);
		}
	}

	public void teleport()
	{
		_player._stablePoint = null;
		if(_player.isBlocked())
			_player.unblock();
		ThreadPoolManager.getInstance().schedule(() -> {
			_player.teleToLocation(_returnLoc);
		}, 5000L);
	}

	public Player getPlayer()
	{
		return _player;
	}

	public boolean isDead()
	{
		return _isDead;
	}

	public void setDead()
	{
		_isDead = true;
	}

	public Location getLoc()
	{
		return _returnLoc;
	}

	public int getTeam()
	{
		return _team;
	}
}
