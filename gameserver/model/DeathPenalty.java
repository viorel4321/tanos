package l2s.gameserver.model;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.SkillTable;

public class DeathPenalty
{
	private static final int _skillId = 5076;
	private static final int _fortuneOfNobleseSkillId = 1325;
	private static final int _charmOfLuckSkillId = 2168;
	private HardReference<Player> _playerRef;
	private byte _level;
	private boolean _hasCharmOfLuck;

	public DeathPenalty(final Player player, final byte level)
	{
		_playerRef = player.getRef();
		_level = player.isGM() ? 0 : level;
	}

	public Player getPlayer()
	{
		return _playerRef.get();
	}

	public int getLevel()
	{
		if(_level > 15)
			_level = 15;
		if(_level < 0)
			_level = 0;
		return Config.ALLOW_DEATH_PENALTY_C5 ? _level : 0;
	}

	public int getLevelOnSaveDB()
	{
		if(_level > 15)
			_level = 15;
		if(_level < 0)
			_level = 0;
		return _level;
	}

	public void notifyDead(final Creature killer)
	{
		if(!Config.ALLOW_DEATH_PENALTY_C5)
			return;
		if(_hasCharmOfLuck)
		{
			_hasCharmOfLuck = false;
			return;
		}
		final Player player = getPlayer();
		if(player == null || player.getLevel() <= 9)
			return;
		int karmaBonus = player.getKarma() / Config.ALT_DEATH_PENALTY_C5_KARMA_PENALTY;
		if(karmaBonus < 0)
			karmaBonus = 0;
		if(Rnd.chance(Config.ALT_DEATH_PENALTY_C5_CHANCE + karmaBonus) && !killer.isPlayable())
			addLevel();
	}

	public void restore()
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		final Skill remove = getCurrentSkill();
		if(remove != null)
			player.removeSkill(remove, true);
		if(!Config.ALLOW_DEATH_PENALTY_C5)
			return;
		if(getLevel() > 0)
		{
			player.addSkill(SkillTable.getInstance().getInfo(5076, getLevel()), false);
			player.sendPacket(new SystemMessage(1916).addNumber(Integer.valueOf(getLevel())));
		}
		player.sendEtcStatusUpdate();
		player.updateStats();
	}

	public void addLevel()
	{
		final Player player = getPlayer();
		if(player == null || getLevel() >= 15 || player.isGM())
			return;
		if(getLevel() != 0)
		{
			final Skill remove = getCurrentSkill();
			if(remove != null)
				player.removeSkill(remove, true);
		}
		++_level;
		player.addSkill(SkillTable.getInstance().getInfo(5076, getLevel()), false);
		player.sendPacket(new SystemMessage(1916).addNumber(Integer.valueOf(getLevel())));
		player.sendEtcStatusUpdate();
		player.updateStats();
	}

	public void reduceLevel()
	{
		final Player player = getPlayer();
		if(player == null || getLevel() <= 0)
			return;
		final Skill remove = getCurrentSkill();
		if(remove != null)
			player.removeSkill(remove, true);
		--_level;
		if(getLevel() > 0)
		{
			player.addSkill(SkillTable.getInstance().getInfo(5076, getLevel()), false);
			player.sendPacket(new SystemMessage(1916).addNumber(Integer.valueOf(getLevel())));
		}
		else
			player.sendPacket(new SystemMessage(1917));
		player.sendEtcStatusUpdate();
		player.updateStats();
	}

	public Skill getCurrentSkill()
	{
		final Player player = getPlayer();
		if(player != null)
			for(final Skill s : player.getAllSkills())
				if(s.getId() == 5076)
					return s;
		return null;
	}

	public void checkCharmOfLuck()
	{
		final Player player = getPlayer();
		if(player != null)
			for(final Abnormal e : player.getAbnormalList().values())
				if(e.getSkill().getId() == 2168 || e.getSkill().getId() == 1325)
				{
					_hasCharmOfLuck = true;
					return;
				}
		_hasCharmOfLuck = false;
	}
}
