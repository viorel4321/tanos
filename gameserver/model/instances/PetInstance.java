package l2s.gameserver.model.instances;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Future;

import l2s.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.PetData;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.items.PetInventory;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.InventoryUpdate;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.PetInfo;
import l2s.gameserver.network.l2.s2c.PetItemList;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.taskmanager.DecayTaskManager;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;

public class PetInstance extends Servitor
{
	protected static Logger _log = LoggerFactory.getLogger(PetInstance.class);

	private byte _level;
	private int _curFed;
	protected PetInventory _inventory;
	private final int _controlItemId;
	private boolean _respawned;
	private Future<?> _feedTask;
	protected boolean _feedMode;
	protected PetData _data;
	private int lostExp;
	private long getExpForNextLevel;
	private long getExpForThisLevel;

	public static PetInstance spawnPet(final NpcTemplate template, final Player owner, final ItemInstance control)
	{
		final PetInstance result = restore(control, template, owner);
		if(result != null)
			result.InventoryUpdateControlItem();
		return result;
	}

	public PetInstance(final int objectId, final NpcTemplate template, final Player owner, final ItemInstance control, final byte _currentLevel)
	{
		super(objectId, template, owner);
		_controlItemId = control.getObjectId();
		if(template.npcId == 12564)
		{
			_level = (byte) getControlItem().getEnchantLevel();
			if(_level <= 0)
				_level = owner.getLevel();
		}
		else
			_level = _currentLevel == 0 ? template.level : _currentLevel;
		_data = PetDataTable.getInstance().getInfo(template.npcId, _level);
		(_inventory = new PetInventory(this)).restore();
		transferPetItems();
		startFeed(false);
	}

	private void transferPetItems()
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		boolean transferred = false;
		for(ItemInstance item : owner.getInventory().getItemsList())
			if(!item.isEquipped() && (item.getCustomFlags() & 0x80) == 0x80)
				if(_inventory.getTotalWeight() + item.getTemplate().getWeight() * item.getCount() > getMaxLoad())
					owner.sendPacket(new SystemMessage(546));
				else
				{
					if(!item.canBeDropped(owner))
						continue;
					item = owner.getInventory().dropItem(item, item.getCount());
					item.setCustomFlags(item.getCustomFlags() | 0x80, true);
					_inventory.addItem(item);
					tryEquipItem(item, false);
					transferred = true;
				}
		if(transferred)
		{
			owner.sendPacket(new PetItemList(this));
			broadcastCharInfo();
			owner.sendPacket(new ItemList(owner, false));
		}
	}

	public boolean tryEquipItem(ItemInstance item, boolean broadcast)
	{
		if(!item.isEquipable())
			return false;

		int petId = getTemplate().getId();
		if(petId == 12077 && item.getTemplate().isForWolf() || petId >= 12311 && petId <= 12313 && item.getTemplate().isForHatchling() || petId >= 12526 && petId <= 12528 && item.getTemplate().isForStrider())
		{
			if(item.isEquipped())
				_inventory.unEquipItem(item);
			else
				_inventory.equipItem(item, true);
			if(broadcast)
			{
				Player owner = getPlayer();
				if(owner != null)
					owner.sendPacket(new PetItemList(this));
				broadcastCharInfo();
			}
			return true;
		}
		return false;
	}

	public PetInstance(final int objectId, final NpcTemplate template, final Player owner, final ItemInstance control)
	{
		super(objectId, template, owner);
		_controlItemId = control.getObjectId();
		_inventory = new PetInventory(this);
		if(template.npcId == 12564)
		{
			_level = (byte) getControlItem().getEnchantLevel();
			if(_level <= 0)
				_level = owner.getLevel();
		}
		else
			_level = template.level;
		_data = PetDataTable.getInstance().getInfo(template.npcId, _level);
		startFeed(false);
	}

	@Override
	public final byte getLevel()
	{
		return _level;
	}

	public boolean isRespawned()
	{
		return _respawned;
	}

	@Override
	public int getSummonType()
	{
		return 2;
	}

	@Override
	public int getControlItemId()
	{
		return _controlItemId;
	}

	public ItemInstance getControlItem()
	{
		final Player owner = getPlayer();
		if(owner == null)
			return null;
		final int item_id = getControlItemId();
		if(item_id == 0)
			return null;
		return owner.getInventory().getItemByObjectId(item_id);
	}

	public void InventoryUpdateControlItem()
	{
		final ItemInstance controlItem = getControlItem();
		if(controlItem == null)
			return;
		controlItem.setEnchantLevel(_level);
		controlItem.setCustomType2(getName() != null ? 1 : 0);
		final Player owner = getPlayer();
		if(owner != null)
			owner.sendPacket(new InventoryUpdate().addModifiedItem(controlItem));
	}

	@Override
	public int getMaxHp()
	{
		return (int) this.calcStat(Stats.MAX_HP, _data.getHP(), null, null);
	}

	@Override
	public int getMaxMp()
	{
		return (int) this.calcStat(Stats.MAX_MP, _data.getMP(), null, null);
	}

	@Override
	public int getMaxFed()
	{
		return _data.getFeedMax();
	}

	@Override
	public int getMAtk(final Creature target, final Skill skill)
	{
		return (int) this.calcStat(Stats.MAGIC_ATTACK, _data.getMAtk(), target, skill);
	}

	@Override
	public int getMDef(final Creature target, final Skill skill)
	{
		return (int) this.calcStat(Stats.MAGIC_DEFENCE, _data.getMDef(), target, skill);
	}

	@Override
	public int getPAtk(final Creature target)
	{
		return (int) this.calcStat(Stats.POWER_ATTACK, _data.getPAtk(), target, null);
	}

	@Override
	public int getPDef(final Creature target)
	{
		return (int) this.calcStat(Stats.POWER_DEFENCE, _data.getPDef(), target, null);
	}

	@Override
	public int getAccuracy()
	{
		return (int) this.calcStat(Stats.ACCURACY_COMBAT, _data.getAccuracy(), null, null);
	}

	@Override
	public int getEvasionRate(final Creature target)
	{
		return (int) this.calcStat(Stats.EVASION_RATE, _data.getEvasion(), target, null);
	}

	@Override
	public int getCriticalHit(final Creature target, final Skill skill)
	{
		return (int) this.calcStat(Stats.CRITICAL_BASE, _data.getCritical(), target, skill);
	}

	@Override
	public int getRunSpeed()
	{
		if(isInWater())
			return getSwimSpeed();
		return (int) this.calcStat(Stats.RUN_SPEED, _data.getSpeed(), null, null);
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if(isRunning())
			return getRunSpeed() * 1.0f / _data.getSpeed();
		return getWalkSpeed() * 1.0f / getTemplate().baseWalkSpd;
	}

	@Override
	public int getBaseRunSpd()
	{
		return _data.getSpeed();
	}

	@Override
	public int getPAtkSpd()
	{
		return (int) this.calcStat(Stats.POWER_ATTACK_SPEED, this.calcStat(Stats.ATK_BASE, _data.getAtkSpeed(), null, null), null, null);
	}

	@Override
	public int getMAtkSpd()
	{
		return (int) this.calcStat(Stats.MAGIC_ATTACK_SPEED, _data.getCastSpeed(), null, null);
	}

	@Override
	public int getMaxLoad()
	{
		return (int) this.calcStat(Stats.MAX_LOAD, _data.getMaxLoad(), null, null);
	}

	@Override
	public int getCurrentFed()
	{
		return _curFed;
	}

	public void setCurrentFed(final int num)
	{
		_curFed = num;
	}

	@Override
	public void setSp(final int sp)
	{
		_sp = sp;
	}

	public void setLevel(final byte level)
	{
		_level = level;
	}

	public void setRespawned(final boolean respawned)
	{
		_respawned = respawned;
	}

	@Override
	public void addExpAndSp(final long addToExp, final long addToSp)
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		_exp += addToExp;
		_sp += (int) addToSp;
		if(addToExp > 0L)
			owner.sendPacket(new SystemMessage(1014).addNumber(Integer.valueOf((int) addToExp)));
		final int old_level = _level;
		while(_exp >= getExpForNextLevel() && _level < 80)
			++_level;
		if(old_level != _level)
		{
			InventoryUpdateControlItem();
			_data = PetDataTable.getInstance().getInfo(getTemplate().npcId, _level);
		}
		boolean needStatusUpdate = true;
		if(old_level < _level)
		{
			owner.sendMessage(new CustomMessage("l2s.gameserver.model.instances.PetInstance.PetLevelUp").addNumber(_level));
			this.broadcastPacket(new SocialAction(getObjectId(), 15));
			setCurrentHpMp(getMaxHp(), getMaxMp(), false);
			needStatusUpdate = false;
		}
		if(needStatusUpdate && (addToExp > 0L || addToSp > 0L))
			broadcastStatusUpdate();
	}

	@Override
	public long getExpForThisLevel()
	{
		return getExpForThisLevel = PetDataTable.getInstance().getInfo(getNpcId(), _level).getExp();
	}

	@Override
	public long getExpForNextLevel()
	{
		return getExpForNextLevel = PetDataTable.getInstance().getInfo(getNpcId(), (byte) (_level + 1)).getExp();
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public PetInventory getInventory()
	{
		return _inventory;
	}

	public void removeItemFromInventory(final ItemInstance item, final int count, final boolean toLog)
	{
		synchronized (_inventory)
		{
			_inventory.destroyItem(item.getObjectId(), count, toLog);
		}
	}

	@Override
	public void doPickupItem(final GameObject object)
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		this.stopMove();
		if(!object.isItem())
		{
			owner.sendActionFailed();
			return;
		}
		final ItemInstance target = (ItemInstance) object;
		if(target.isCursed() || target.isMercTicket())
		{
			owner.sendPacket(new SystemMessage(56).addItemName(Integer.valueOf(target.getItemId())));
			return;
		}
		synchronized (target)
		{
			if(Config.NONOWNER_ITEM_PICKUP_PET && target.getDropTimeOwner() > 0L && target.getItemDropOwner() != null && target.getDropTimeOwner() > System.currentTimeMillis() && owner != target.getItemDropOwner() && (!owner.isInParty() || owner.isInParty() && target.getItemDropOwner().isInParty() && owner.getParty() != target.getItemDropOwner().getParty()))
			{
				SystemMessage sm;
				if(target.getItemId() == 57)
				{
					sm = new SystemMessage(55);
					sm.addNumber(Long.valueOf(target.getCount()));
				}
				else
				{
					sm = new SystemMessage(56);
					sm.addItemName(Integer.valueOf(target.getItemId()));
				}
				owner.sendPacket(sm);
				return;
			}
			if(!target.isVisible())
			{
				owner.sendActionFailed();
				return;
			}
			if(getInventory().getTotalWeight() + target.getTemplate().getWeight() * target.getCount() > getMaxLoad())
			{
				owner.sendPacket(new SystemMessage(546));
				return;
			}
			if(target.isHerb())
			{
				final Skill[] skills = target.getTemplate().getAttachedSkills();
				if(skills != null && skills.length > 0)
					for(final Skill skill : skills)
						altUseSkill(skill, this);
				target.deleteMe();
				return;
			}
			target.pickupMe(this);
		}
		if(owner.getParty() == null || owner.getParty().getLootDistribution() == 0)
		{
			owner.sendPacket(SystemMessage.obtainItemsBy(target, "Your pet"));
			target.setCustomFlags(target.getCustomFlags() | 0x80, true);
			synchronized (_inventory)
			{
				_inventory.addItem(target);
			}
			owner.sendPacket(new PetItemList(this));
			owner.sendPacket(new PetInfo(this, 1));
		}
		else
			owner.getParty().distributeItem(owner, target);
		broadcastPickUpMsg(target);
	}

	@Override
	public void deleteMe()
	{
		giveAllToOwner();
		destroyControlItem();
		stopFeed();
		super.deleteMe();
	}

	@Override
	public synchronized void onDeath(final Creature killer)
	{
		super.onDeath(killer);
		final Player owner = getPlayer();
		if(owner == null)
		{
			onDecay();
			return;
		}
		stopFeed();
		deathPenalty();
		owner.sendPacket(new SystemMessage(1519));
		DecayTaskManager.getInstance().addDecayTask(this, 1200000L);
	}

	@Override
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	private synchronized void giveAllToOwner()
	{
		final Player owner = getPlayer();
		synchronized (_inventory)
		{
			for(final ItemInstance i : _inventory.getItems())
			{
				final ItemInstance item = _inventory.dropItem(i, i.getCount());
				if(owner != null && owner.getInventory().validateWeight(i) && owner.getInventory().validateCapacity(i))
					owner.getInventory().addItem(item);
				else
					item.dropMe(this, getLoc().changeZ(25));
			}
			_inventory.getItemsList().clear();
		}
	}

	public void destroyControlItem()
	{
		final Player owner = getPlayer();
		if(owner == null || getControlItemId() == 0)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, getControlItemId());
			statement.execute();
		}
		catch(Exception e)
		{
			PetInstance._log.warn("could not delete pet:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		try
		{
			owner.getInventory().destroyItem(getControlItemId(), 1L, true);
		}
		catch(Exception e)
		{
			PetInstance._log.warn("Error while destroying control item: " + e);
		}
	}

	@Override
	public boolean isMountable()
	{
		switch(getTemplate().getId())
		{
			case 12526:
			case 12527:
			case 12528:
			case 12621:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	private static PetInstance restore(final ItemInstance control, final NpcTemplate template, final Player owner)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT objId, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?");
			statement.setInt(1, control.getObjectId());
			rset = statement.executeQuery();
			if(!rset.next())
				return new PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			final PetInstance pet = new PetInstance(rset.getInt("objId"), template, owner, control, rset.getByte("level"));
			pet.setRespawned(true);
			pet.setName(rset.getString("name"));
			pet.setCurrentHpMp(rset.getDouble("curHp"), rset.getInt("curMp"), true);
			pet.setCurrentCp(pet.getMaxCp());
			pet.setExp(rset.getLong("exp"));
			pet.setSp(rset.getInt("sp"));
			pet.setCurrentFed(rset.getInt("fed"));
			return pet;
		}
		catch(Exception e)
		{
			PetInstance._log.error("could not restore Pet data: ", e);
			return null;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void store()
	{
		if(getControlItemId() == 0)
			return;
		String req;
		if(!isRespawned())
			req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,objId,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?)";
		else
			req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=?,objId=? WHERE item_obj_id = ?";
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(req);
			statement.setString(1, _name);
			statement.setInt(2, _level);
			statement.setDouble(3, getCurrentHp());
			statement.setDouble(4, getCurrentMp());
			statement.setLong(5, _exp);
			statement.setLong(6, _sp);
			statement.setInt(7, _curFed);
			statement.setInt(8, _objectId);
			statement.setInt(9, _controlItemId);
			statement.executeUpdate();
			_respawned = true;
		}
		catch(Exception e)
		{
			PetInstance._log.warn("could not store pet data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	protected synchronized void stopFeed()
	{
		if(_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
		}
	}

	public synchronized void startFeed(final boolean battleFeed)
	{
		if(_feedTask != null)
			stopFeed();
		if(_feedTask == null && !isDead())
		{
			int _feedTime;
			if(battleFeed)
			{
				_feedMode = true;
				_feedTime = _data.getFeedBattle();
			}
			else
			{
				_feedMode = false;
				_feedTime = _data.getFeedNormal();
			}
			if(_feedTime <= 0)
				_feedTime = 1;
			_feedTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FeedTask(), 60000 / _feedTime, 60000 / _feedTime);
		}
	}

	@Override
	public void unSummon()
	{
		stopFeed();
		giveAllToOwner();
		super.deleteMe();
		store();
	}

	@Override
	public void displayGiveDamageMessage(final Creature target, final boolean crit, final boolean miss, final boolean magic)
	{
		final Player owner = getPlayer();
		if(owner == null)
			return;
		if(crit)
			owner.sendPacket(Msg.PETS_CRITICAL_HIT);
		if(miss)
			owner.sendPacket(new SystemMessage(1999).addName(target));
	}

	@Override
	public void displayReceiveDamageMessage(final Creature attacker, final int damage)
	{
		final Player owner = getPlayer();
		if(!isDead() && attacker != null && owner != null)
		{
			final SystemMessage sm = new SystemMessage(1016);
			String name = attacker.getVisibleName(owner);
			if(attacker.isNpc() && name.isEmpty())
				sm.addNpcName(((NpcInstance) attacker).getTemplate().npcId);
			else
				sm.addString(name);
			sm.addNumber(Integer.valueOf(damage));
			owner.sendPacket(sm);
		}
	}

	private void deathPenalty()
	{
		final int lvl = getLevel();
		final double percentLost = -0.07 * lvl + 6.5;
		lostExp = (int) Math.round((getExpForNextLevel() - getExpForThisLevel()) * percentLost / 100.0);
		_exp -= lostExp;
		if(_exp < getExpForThisLevel())
		{
			if(_level == 1)
				return;
			--_level;
			_data = PetDataTable.getInstance().getInfo(getTemplate().npcId, _level);
			InventoryUpdateControlItem();
			setCurrentHpMp(getMaxHp(), getMaxMp(), false);
			broadcastStatusUpdate();
		}
	}

	public void restoreExp()
	{
		this.restoreExp(100.0);
	}

	public void restoreExp(final double percent)
	{
		if(lostExp != 0)
		{
			addExpAndSp((long) (lostExp * percent / 100.0), 0L);
			lostExp = 0;
		}
		sendChanges();
	}

	public void doRevive(final double percent)
	{
		setCurrentHpMp(0.6 * getMaxHp(), 0.6 * getMaxMp(), true);
		this.restoreExp(percent);
		this.doRevive(true);
	}

	@Override
	public void doRevive(final boolean absolute)
	{
		stopDecay();
		super.doRevive(absolute);
		setCurrentFed(getMaxFed() / 10);
		startFeed(false);
		setRunning();
		setFollowStatus(true, true);
	}

	public int getSkillLevel(final int skillId)
	{
		if(_skills == null || _skills.get(skillId) == null)
			return -1;
		final int lvl = getLevel();
		return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
	}

	@Override
	public boolean consumeItem(final int itemConsumeId, final int itemCount)
	{
		final ItemInstance item = getInventory().findItemByItemId(itemConsumeId);
		return item != null && item.getCount() >= itemCount && getInventory().destroyItem(item, itemCount, false) != null;
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!isVisible())
			return;
		sendStUpdate();
	}

	@Override
	public boolean isPet()
	{
		return true;
	}

	@Override
	public boolean useItem(ItemInstance item, boolean ctrl, boolean sendMsg)
	{
		int itemId = item.getItemId();
		if(getPlayer().isAlikeDead() || isDead() || isOutOfControl())
		{
			if(sendMsg)
				getPlayer().sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(Integer.valueOf(itemId)));
			return false;
		}

		if(tryEquipItem(item, true))
			return true;

		int petId = getNpcId();
		if(petId == 12077 && itemId == 2515 || petId == 12564 && itemId == 2515 || petId >= 12311 && petId <= 12313 && itemId == 4038 || petId >= 12526 && petId <= 12528 && itemId == 5168 || petId >= 12780 && petId <= 12782 && itemId == 7582)
		{
			if(getCurrentFed() >= getMaxFed())
			{
				if(sendMsg)
					getPlayer().sendActionFailed();
				return false;
			}
			removeItemFromInventory(item, 1, true);
			setCurrentFed(getCurrentFed() + (int) (getMaxFed() * 0.12));
			if(getCurrentFed() > getMaxFed())
				setCurrentFed(getMaxFed());
			broadcastStatusUpdate();
			return true;
		}

		if(sendMsg)
			getPlayer().sendPacket(new SystemMessage(SystemMessage.ITEM_NOT_AVAILABLE_FOR_PETS));

		return false;
	}

	class FeedTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				final Player owner = getPlayer();
				if(owner == null)
				{
					stopFeed();
					unSummon();
					return;
				}
				if(isAttackingNow())
					if(!_feedMode)
						startFeed(true);
					else
						startFeed(false);
				if(getCurrentFed() > 0)
					setCurrentFed(getCurrentFed() - 1);
				else
				{
					setCurrentFed(0);
					stopFeed();
					owner.sendMessage(new CustomMessage("l2s.gameserver.model.instances.PetInstance.UnSummonHungryPet"));
					unSummon();
				}
				ItemInstance food = null;
				switch(getTemplate().npcId)
				{
					case 12077:
					case 12564:
					{
						food = getInventory().findItemByItemId(2515);
						break;
					}
					case 12311:
					case 12312:
					case 12313:
					{
						food = getInventory().findItemByItemId(4038);
						break;
					}
					case 12526:
					case 12527:
					case 12528:
					{
						food = getInventory().findItemByItemId(5168);
						break;
					}
					case 12780:
					case 12781:
					case 12782:
					{
						food = getInventory().findItemByItemId(7582);
						break;
					}
					default:
					{
						return;
					}
				}
				if(food != null && getCurrentFed() < 0.55 * getMaxFed())
				{
					setCurrentFed(getCurrentFed() + (int) (getMaxFed() * 0.12));
					removeItemFromInventory(food, 1, false);
					if(getCurrentFed() > getMaxFed())
						setCurrentFed(getMaxFed());
				}
				broadcastStatusUpdate();
			}
			catch(Throwable e)
			{
				PetInstance._log.error("", e);
			}
		}
	}
}
