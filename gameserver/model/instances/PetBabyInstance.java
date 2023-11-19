package l2s.gameserver.model.instances;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Future;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PetInventory;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class PetBabyInstance extends PetInstance
{
	private Future<?> _healTask;
	float _expPenalty;
	boolean _thinking;

	public PetBabyInstance(final int objectId, final NpcTemplate template, final Player owner, final ItemInstance control)
	{
		super(objectId, template, owner, control);
		_thinking = false;
	}

	public synchronized void stopHealTask()
	{
		if(_healTask != null)
		{
			_healTask.cancel(false);
			_healTask = null;
			if(Config.DEBUG)
				PetInstance._log.warn("Pet [#" + getObjectId() + "] Heal task stop");
		}
	}

	public synchronized void startHealTask()
	{
		if(_healTask != null)
			stopHealTask();
		if(_healTask == null && !isDead())
			_healTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HealTask(this), 3000L, 1000L);
	}

	public static PetBabyInstance spawnPet(final NpcTemplate template, final Player owner, final ItemInstance control)
	{
		return restore(control, template, owner);
	}

	@Override
	public synchronized void onDeath(final Creature killer)
	{
		stopHealTask();
		super.onDeath(killer);
	}

	@Override
	public void doRevive(final boolean absolute)
	{
		super.doRevive(absolute);
		startHealTask();
	}

	private static PetBabyInstance restore(final ItemInstance control, final NpcTemplate template, final Player owner)
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
				return new PetBabyInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			final PetBabyInstance pet = new PetBabyInstance(rset.getInt("objId"), template, owner, control);
			pet.setExpPenalty(0.1f);
			pet.setRespawned(true);
			pet.setName(rset.getString("name"));
			pet.setLevel(rset.getByte("level"));
			pet.setCurrentHpMp(rset.getDouble("curHp"), rset.getInt("curMp"), true);
			pet.setCurrentCp(pet.getMaxCp());
			pet.setExp(rset.getInt("exp"));
			pet.setSp(rset.getInt("sp"));
			pet.setCurrentFed(rset.getInt("fed"));
			pet._data = PetDataTable.getInstance().getInfo(pet.getTemplate().npcId, pet.getLevel());
			pet._inventory = new PetInventory(pet);
			return pet;
		}
		catch(Exception e)
		{
			PetInstance._log.error("could not restore PetBaby data: ", e);
			return null;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void setExpPenalty(final float expPenalty)
	{
		_expPenalty = expPenalty;
	}

	public float getExpPenalty()
	{
		return _expPenalty;
	}

	@Override
	public synchronized void unSummon()
	{
		stopHealTask();
		super.unSummon();
	}

	public int getSkillLevel()
	{
		final int lvl = getLevel();
		return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
	}

	class HealTask implements Runnable
	{
		private PetBabyInstance _pet;

		HealTask(final PetBabyInstance pet)
		{
			_pet = pet;
		}

		@Override
		public void run()
		{
			final Player owner = _pet.getPlayer();
			if(owner == null || _thinking)
				return;
			_thinking = true;
			final int maxHp = owner.getMaxHp();
			final double curHP = owner.getCurrentHp();
			Skill skill = null;
			try
			{
				if(!owner.isDead() && owner != null)
				{
					if(Rnd.chance(25))
					{
						if(curHP <= maxHp * 0.95 && curHP >= maxHp * 0.2)
							skill = SkillTable.getInstance().getInfo(4717, PetBabyInstance.this.getSkillLevel());
					}
					else if(curHP < maxHp * 0.2)
						skill = SkillTable.getInstance().getInfo(4718, PetBabyInstance.this.getSkillLevel());
					if(skill != null && skill.checkCondition(_pet, owner, false, !isFollow(), true))
					{
						_pet.setTarget(owner);
						_pet.getAI().Cast(skill, owner, false, !_pet.isFollow());
					}
				}
			}
			catch(Throwable e)
			{
				if(Config.DEBUG)
					PetInstance._log.error("Pet [#" + getObjectId() + "] a heal task error has occurred: ", e);
			}
			_thinking = false;
		}
	}
}
