package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.Earthquake;
import l2s.gameserver.network.l2.s2c.ExRedSky;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.utils.Location;

public class CursedWeapon
{
	private final String _name;
	private final int _itemId;
	private final int _skillMaxLevel;
	private final Integer _skillId;
	private int _dropRate;
	private int _disapearChance;
	private int _durationMin;
	private int _durationMax;
	private int _durationLost;
	private int _stageKills;
	private int _nbKills;
	private int _playerKarma;
	private int _playerPkKills;
	private CursedWeaponState _state;
	private Location _loc;
	private long _endTime;
	private int _owner;
	private ItemInstance _item;

	public CursedWeapon(final int itemId, final Integer skillId, final String name)
	{
		_nbKills = 0;
		_playerKarma = 0;
		_playerPkKills = 0;
		_state = CursedWeaponState.NONE;
		_loc = null;
		_endTime = 0L;
		_owner = 0;
		_item = null;
		_name = name;
		_itemId = itemId;
		_skillId = skillId;
		_skillMaxLevel = SkillTable.getInstance().getMaxLevel(_skillId);
	}

	public void initWeapon()
	{
		zeroOwner();
		setState(CursedWeaponState.NONE);
		_endTime = 0L;
		_item = null;
		_nbKills = 0;
	}

	public void create(final NpcInstance attackable, final Player killer, final boolean force)
	{
		if(force || Rnd.get(100000000) <= _dropRate)
		{
			_item = ItemTable.getInstance().createItem(_itemId);
			if(_item != null)
			{
				zeroOwner();
				setState(CursedWeaponState.DROPPED);
				if(_endTime == 0L)
					_endTime = System.currentTimeMillis() + getRndDuration() * 60000;
				_item.dropToTheGround(killer, attackable);
				_loc = _item.getLoc();
				_item.setDropTime(0L);
				final L2GameServerPacket redSky = new ExRedSky(10);
				final L2GameServerPacket eq = new Earthquake(killer.getLoc(), 30, 12);
				for(final Player aPlayer : GameObjectsStorage.getPlayers())
					aPlayer.sendPacket(redSky, eq);
			}
		}
	}

	public boolean dropIt(final Player owner)
	{
		if(Rnd.chance(_disapearChance))
			return false;
		Player player = getOnlineOwner();
		if(player == null)
		{
			if(owner == null)
				return false;
			player = owner;
		}
		ItemInstance oldItem = player.getInventory().getItemByItemId(_itemId);
		if(oldItem == null)
			return false;
		final long oldCount = oldItem.getCount();
		if((oldItem = player.getInventory().dropItem(oldItem, oldCount, true)) == null)
			return false;
		player.setKarma(_playerKarma);
		player.setPkKills(_playerPkKills);
		player.setCursedWeaponEquippedId(0);
		final Skill skill = SkillTable.getInstance().getInfo(_skillId, player.getSkillLevel(_skillId));
		if(skill != null)
			for(final Skill.AddedSkill s : skill.getAddedSkills())
				player.removeSkillById(s.id);
		player.removeSkillById(_skillId);
		player.abortAttack(true, false);
		zeroOwner();
		setState(CursedWeaponState.DROPPED);
		oldItem.dropToTheGround(player, (NpcInstance) null);
		_loc = oldItem.getLoc();
		oldItem.setDropTime(0L);
		_item = oldItem;
		player.sendPacket(new SystemMessage(298).addItemName(Integer.valueOf(oldItem.getItemId())));
		player.refreshExpertisePenalty();
		player.broadcastUserInfo(true);
		player.broadcastPacket(new Earthquake(player.getLoc(), 30, 12));
		return true;
	}

	private void giveSkill(final Player player)
	{
		for(final Skill s : getSkills(player))
			player.addSkill(s, false);
		player.sendPacket(new SkillList(player));
	}

	private Collection<Skill> getSkills(final Player player)
	{
		int level = 1 + _nbKills / _stageKills;
		if(level > _skillMaxLevel)
			level = _skillMaxLevel;
		final Skill skill = SkillTable.getInstance().getInfo(_skillId, level);
		final List<Skill> ret = new ArrayList<Skill>();
		ret.add(skill);
		for(final Skill.AddedSkill s : skill.getAddedSkills())
			ret.add(SkillTable.getInstance().getInfo(s.id, s.level));
		return ret;
	}

	public boolean reActivate()
	{
		if(getTimeLeft() <= 0L)
		{
			if(getPlayerId() != 0)
				setState(CursedWeaponState.ACTIVATED);
			return false;
		}
		if(getPlayerId() == 0)
		{
			if(_loc == null || (_item = ItemTable.getInstance().createItem(_itemId)) == null)
				return false;
			_item.dropMe(null, _loc);
			_item.setDropTime(0L);
			setState(CursedWeaponState.DROPPED);
		}
		else
			setState(CursedWeaponState.ACTIVATED);
		return true;
	}

	public void activate(final Player player, final ItemInstance item)
	{
		if(isDropped() || getPlayerId() != player.getObjectId())
		{
			_playerKarma = player.getKarma();
			_playerPkKills = player.getPkKills();
			if(Config.RESET_CURSED_WEAPONS)
				_nbKills = 0;
		}
		if(player.recording)
			player.writeBot(false);
		setPlayer(player);
		setState(CursedWeaponState.ACTIVATED);
		if(player.isInParty())
			player.getParty().oustPartyMember(player);
		if(player.isMounted())
			player.setMount(0, 0, 0);
		_item = item;
		player.getInventory().equipItem(_item, false);
		player.sendPacket(new SystemMessage(49).addItemName(Integer.valueOf(_item.getItemId())));
		player.setCursedWeaponEquippedId(_itemId);
		player.setKarma(9999999);
		player.setPkKills(_nbKills);
		if(_endTime == 0L)
			_endTime = System.currentTimeMillis() + getRndDuration() * 60000;
		giveSkill(player);
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), false);
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}

	public void increaseKills()
	{
		final Player player = getOnlineOwner();
		if(player == null)
			return;
		player.setPkKills(++_nbKills);
		player.broadcastUserInfo(true);
		if(_nbKills % _stageKills == 0 && _nbKills <= _stageKills * (_skillMaxLevel - 1))
			giveSkill(player);
		_endTime = System.currentTimeMillis() + getRndDuration() * 60000L;
	}

	public void setDisapearChance(final int disapearChance)
	{
		_disapearChance = disapearChance;
	}

	public void setDropRate(final int dropRate)
	{
		_dropRate = dropRate;
	}

	public void setDurationMin(final int duration)
	{
		_durationMin = duration;
	}

	public void setDurationMax(final int duration)
	{
		_durationMax = duration;
	}

	public void setDurationLost(final int durationLost)
	{
		_durationLost = durationLost;
	}

	public void setStageKills(final int stageKills)
	{
		_stageKills = stageKills;
	}

	public void setNbKills(final int nbKills)
	{
		_nbKills = nbKills;
	}

	public void setPlayerId(int playerId)
	{
		_owner = playerId;
	}

	public void setPlayerKarma(final int playerKarma)
	{
		_playerKarma = playerKarma;
	}

	public void setPlayerPkKills(final int playerPkKills)
	{
		_playerPkKills = playerPkKills;
	}

	public void setState(final CursedWeaponState state)
	{
		_state = state;
	}

	public void setEndTime(final long endTime)
	{
		_endTime = endTime;
	}

	public void setPlayer(final Player player)
	{
		if(player != null)
			_owner = player.getObjectId();
		else if(_owner != 0)
			setPlayerId(getPlayerId());
	}

	private void zeroOwner()
	{
		_owner = 0;
		_playerKarma = 0;
		_playerPkKills = 0;
	}

	public void setItem(final ItemInstance item)
	{
		_item = item;
	}

	public void setLoc(final Location loc)
	{
		_loc = loc;
	}

	public CursedWeaponState getState()
	{
		return _state;
	}

	public boolean isActivated()
	{
		return getState() == CursedWeaponState.ACTIVATED;
	}

	public boolean isDropped()
	{
		return getState() == CursedWeaponState.DROPPED;
	}

	public long getEndTime()
	{
		return _endTime;
	}

	public String getName()
	{
		return _name;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public ItemInstance getItem()
	{
		return _item;
	}

	public Integer getSkillId()
	{
		return _skillId;
	}

	public int getPlayerId()
	{
		return _owner;
	}

	public Player getPlayer()
	{
		return _owner == 0 ? null : GameObjectsStorage.getPlayer(_owner);
	}

	public int getPlayerKarma()
	{
		return _playerKarma;
	}

	public int getPlayerPkKills()
	{
		return _playerPkKills;
	}

	public int getNbKills()
	{
		return _nbKills;
	}

	public int getStageKills()
	{
		return _stageKills;
	}

	public Location getLoc()
	{
		return _loc;
	}

	public int getRndDuration()
	{
		if(_durationMin > _durationMax)
			_durationMax = 2 * _durationMin;
		return Rnd.get(_durationMin, _durationMax);
	}

	public boolean isActive()
	{
		return isActivated() || isDropped();
	}

	public int getLevel()
	{
		return Math.min(1 + _nbKills / _stageKills, _skillMaxLevel);
	}

	public long getTimeLeft()
	{
		return _endTime - System.currentTimeMillis();
	}

	public Location getWorldPosition()
	{
		if(isActivated())
		{
			final Player player = getOnlineOwner();
			if(player != null)
				return player.getLoc();
		}
		else if(isDropped() && _item != null)
			return _item.getLoc();
		return null;
	}

	public Player getOnlineOwner()
	{
		final Player player = getPlayer();
		return player != null && player.isOnline() ? player : null;
	}

	public boolean isOwned()
	{
		return _owner != 0;
	}

	public enum CursedWeaponState
	{
		NONE,
		ACTIVATED,
		DROPPED;
	}
}
