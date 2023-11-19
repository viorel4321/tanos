package l2s.gameserver.model.entity.residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbcp.DbUtils;
import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CastleDAO;
import l2s.gameserver.dao.CastleHiredGuardDAO;
import l2s.gameserver.dao.ClanDataDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.templates.MerchantGuard;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Log;

public class Castle extends Residence
{
	private static final Logger _log;
	private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?;";
	private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
	private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
	private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
	private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
	private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
	private final IntObjectMap<MerchantGuard> _merchantGuards;
	private List<CastleManorManager.CropProcure> _procure;
	private List<CastleManorManager.SeedProduction> _production;
	private List<CastleManorManager.CropProcure> _procureNext;
	private List<CastleManorManager.SeedProduction> _productionNext;
	private boolean _isNextPeriodApproved;
	private int _TaxPercent;
	private double _TaxRate;
	private int _treasury;
	private int _collectedShops;
	private int _collectedSeed;
	private Set<ItemInstance> _spawnMerchantTickets;
	private List<Integer> _artefacts;

	public Castle(final StatsSet set)
	{
		super(set);
		_merchantGuards = new HashIntObjectMap<MerchantGuard>();
		_spawnMerchantTickets = new CopyOnWriteArraySet<ItemInstance>();
		_artefacts = new ArrayList<Integer>();
	}

	@Override
	public void init()
	{
		super.init();
		initArtefact();
	}

	@Override
	public ResidenceType getType()
	{
		return ResidenceType.Castle;
	}

	@Override
	public void changeOwner(final Clan newOwner)
	{
		if(newOwner != null && newOwner.getHasCastle() != 0)
		{
			final Castle oldCastle = ResidenceHolder.getInstance().getResidence(Castle.class, newOwner.getHasCastle());
			if(oldCastle != null)
				oldCastle.changeOwner(null);
		}
		Clan oldOwner = null;
		if(getOwnerId() > 0 && (newOwner == null || newOwner.getClanId() != getOwnerId()))
		{
			removeSkills();
			this.setTaxPercent(null, 0);
			cancelCycleTask();
			oldOwner = getOwner();
			if(oldOwner != null)
			{
				final int amount = getTreasury();
				if(amount > 0)
				{
					final Warehouse warehouse = oldOwner.getWarehouse();
					if(warehouse != null)
					{
						warehouse.addItem(57, amount);
						addToTreasuryNoTax(-amount, false, false);
						Log.add(getName() + "|" + -amount + "|Castle:changeOwner", "treasury");
					}
				}
				for(final Player clanMember : oldOwner.getOnlineMembers(0))
					if(clanMember != null && clanMember.getInventory() != null)
						clanMember.getInventory().checkAllConditions();
				oldOwner.setHasCastle(0);
			}
		}
		if(newOwner != null)
			newOwner.setHasCastle(getId());
		updateOwnerInDB(newOwner);
		rewardSkills();
		update();
	}

	@Override
	protected void loadData()
	{
		_TaxPercent = 0;
		_TaxRate = 0.0;
		_treasury = 0;
		_procure = new ArrayList<CastleManorManager.CropProcure>();
		_production = new ArrayList<CastleManorManager.SeedProduction>();
		_procureNext = new ArrayList<CastleManorManager.CropProcure>();
		_productionNext = new ArrayList<CastleManorManager.SeedProduction>();
		_isNextPeriodApproved = false;
		_owner = ClanDataDAO.getInstance().getOwner(this);
		CastleDAO.getInstance().select(this);
		CastleHiredGuardDAO.getInstance().load(this);
	}

	public void setTaxPercent(final int p)
	{
		_TaxPercent = Math.min(Math.max(0, p), 100);
		_TaxRate = _TaxPercent / 100.0;
	}

	public void setTreasury(final int t)
	{
		_treasury = t;
	}

	private void updateOwnerInDB(final Clan clan)
	{
		_owner = clan;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=? LIMIT 1");
			statement.setInt(1, getId());
			statement.execute();
			DbUtils.close(statement);
			if(clan != null)
			{
				statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=? LIMIT 1");
				statement.setInt(1, getId());
				statement.setInt(2, getOwnerId());
				statement.execute();
				clan.broadcastClanStatus(true, false, false);
			}
		}
		catch(Exception e)
		{
			Castle._log.warn("Exception: updateOwnerInDB(): " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int getTaxPercent()
	{
		if(Config.ALLOW_SEVEN_SIGNS && _TaxPercent > 5 && SevenSigns.getInstance().getSealOwner(3) == 1)
			_TaxPercent = 5;
		return _TaxPercent;
	}

	public int getTaxPercent0()
	{
		return _TaxPercent;
	}

	public int getCollectedShops()
	{
		return _collectedShops;
	}

	public int getCollectedSeed()
	{
		return _collectedSeed;
	}

	public void setCollectedShops(final int value)
	{
		_collectedShops = value;
	}

	public void setCollectedSeed(final int value)
	{
		_collectedSeed = value;
	}

	public void addToTreasury(int amount, final boolean shop, final boolean seed)
	{
		if(getOwnerId() <= 0)
			return;
		if(amount == 0)
			return;
		if(amount > 1 && _id != 5 && _id != 8)
		{
			final Castle royal = ResidenceHolder.getInstance().getResidence(Castle.class, _id >= 7 ? 8 : 5);
			if(royal != null)
			{
				final int royalTax = (int) (amount * royal.getTaxRate());
				if(royal.getOwnerId() > 0)
				{
					royal.addToTreasury(royalTax, shop, seed);
					if(_id == 5)
						Log.add("Aden|" + royalTax + "|Castle:adenTax", "treasury");
					else if(_id == 8)
						Log.add("Rune|" + royalTax + "|Castle:runeTax", "treasury");
				}
				amount -= royalTax;
			}
		}
		addToTreasuryNoTax(amount, shop, seed);
	}

	public void addToTreasuryNoTax(final int amount, final boolean shop, final boolean seed)
	{
		if(getOwnerId() <= 0)
			return;
		if(amount == 0)
			return;
		_treasury = SafeMath.addAndLimit(_treasury, amount);
		if(shop)
			_collectedShops += amount;
		if(seed)
			_collectedSeed += amount;
		setJdbcState(JdbcEntityState.UPDATED);
		update();
	}

	public int getCropRewardType(final int crop)
	{
		int rw = 0;
		for(final CastleManorManager.CropProcure cp : _procure)
			if(cp.getId() == crop)
				rw = cp.getReward();
		return rw;
	}

	public void setTaxPercent(final Player activeChar, final int taxPercent)
	{
		this.setTaxPercent(taxPercent);
		setJdbcState(JdbcEntityState.UPDATED);
		update();
		if(activeChar != null)
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.model.entity.Castle.OutOfControl.CastleTaxChangetTo").addString(getName()).addNumber(taxPercent));
	}

	public double getTaxRate()
	{
		if(Config.ALLOW_SEVEN_SIGNS && _TaxRate > 0.05 && SevenSigns.getInstance().getSealOwner(3) == 1)
			_TaxRate = 0.05;
		return _TaxRate;
	}

	public int getTreasury()
	{
		return _treasury;
	}

	public List<CastleManorManager.SeedProduction> getSeedProduction(final int period)
	{
		return period == 0 ? _production : _productionNext;
	}

	public List<CastleManorManager.CropProcure> getCropProcure(final int period)
	{
		return period == 0 ? _procure : _procureNext;
	}

	public void setSeedProduction(final List<CastleManorManager.SeedProduction> seed, final int period)
	{
		if(period == 0)
			_production = seed;
		else
			_productionNext = seed;
	}

	public void setCropProcure(final List<CastleManorManager.CropProcure> crop, final int period)
	{
		if(period == 0)
			_procure = crop;
		else
			_procureNext = crop;
	}

	public synchronized CastleManorManager.SeedProduction getSeed(final int seedId, final int period)
	{
		for(final CastleManorManager.SeedProduction seed : getSeedProduction(period))
			if(seed.getId() == seedId)
				return seed;
		return null;
	}

	public synchronized CastleManorManager.CropProcure getCrop(final int cropId, final int period)
	{
		for(final CastleManorManager.CropProcure crop : getCropProcure(period))
			if(crop.getId() == cropId)
				return crop;
		return null;
	}

	public int getManorCost(final int period)
	{
		List<CastleManorManager.CropProcure> procure;
		List<CastleManorManager.SeedProduction> production;
		if(period == 0)
		{
			procure = _procure;
			production = _production;
		}
		else
		{
			procure = _procureNext;
			production = _productionNext;
		}
		int total = 0;
		if(production != null)
			for(final CastleManorManager.SeedProduction seed : production)
				total += (int) (Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce());
		if(procure != null)
			for(final CastleManorManager.CropProcure crop : procure)
				total += crop.getPrice() * crop.getStartAmount();
		return total;
	}

	public void saveSeedData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
			statement.setInt(1, getId());
			statement.execute();
			DbUtils.close(statement);
			if(_production != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String[] values = new String[_production.size()];
				for(final CastleManorManager.SeedProduction s : _production)
				{
					values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 0 + ")";
					++count;
				}
				if(values.length > 0)
				{
					query += values[0];
					for(int i = 1; i < values.length; ++i)
						query = query + "," + values[i];
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
			if(_productionNext != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String[] values = new String[_productionNext.size()];
				for(final CastleManorManager.SeedProduction s : _productionNext)
				{
					values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 1 + ")";
					++count;
				}
				if(values.length > 0)
				{
					query += values[0];
					for(int i = 1; i < values.length; ++i)
						query = query + "," + values[i];
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
		}
		catch(Exception e)
		{
			Castle._log.warn("Error adding seed production data for castle " + getName() + "! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void saveSeedData(final int period)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;");
			statement.setInt(1, getId());
			statement.setInt(2, period);
			statement.execute();
			DbUtils.close(statement);
			List<CastleManorManager.SeedProduction> prod = null;
			prod = getSeedProduction(period);
			if(prod != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String[] values = new String[prod.size()];
				for(final CastleManorManager.SeedProduction s : prod)
				{
					values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")";
					++count;
				}
				if(values.length > 0)
				{
					query += values[0];
					for(int i = 1; i < values.length; ++i)
						query = query + "," + values[i];
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
		}
		catch(Exception e)
		{
			Castle._log.warn("Error2 adding seed production data for castle " + getName() + "! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void saveCropData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
			statement.setInt(1, getId());
			statement.execute();
			DbUtils.close(statement);
			if(_procure != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String[] values = new String[_procure.size()];
				for(final CastleManorManager.CropProcure cp : _procure)
				{
					values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 0 + ")";
					++count;
				}
				if(values.length > 0)
				{
					query += values[0];
					for(int i = 1; i < values.length; ++i)
						query = query + "," + values[i];
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
			if(_procureNext != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String[] values = new String[_procureNext.size()];
				for(final CastleManorManager.CropProcure cp : _procureNext)
				{
					values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 1 + ")";
					++count;
				}
				if(values.length > 0)
				{
					query += values[0];
					for(int i = 1; i < values.length; ++i)
						query = query + "," + values[i];
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
		}
		catch(Exception e)
		{
			Castle._log.warn("Error adding crop data for castle " + getName() + "! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void saveCropData(final int period)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;");
			statement.setInt(1, getId());
			statement.setInt(2, period);
			statement.execute();
			DbUtils.close(statement);
			List<CastleManorManager.CropProcure> proc = null;
			proc = getCropProcure(period);
			if(proc != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String[] values = new String[proc.size()];
				for(final CastleManorManager.CropProcure cp : proc)
				{
					values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")";
					++count;
				}
				if(values.length > 0)
				{
					query += values[0];
					for(int i = 1; i < values.length; ++i)
						query = query + "," + values[i];
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
		}
		catch(Exception e)
		{
			Castle._log.warn("Error2 adding crop data for castle " + getName() + "! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void updateCrop(final int cropId, final int amount, final int period)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?");
			statement.setInt(1, amount);
			statement.setInt(2, cropId);
			statement.setInt(3, getId());
			statement.setInt(4, period);
			statement.execute();
		}
		catch(Exception e)
		{
			Castle._log.warn("Error updating crop data for castle " + getName() + "! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void updateSeed(final int seedId, final int amount, final int period)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?");
			statement.setInt(1, amount);
			statement.setInt(2, seedId);
			statement.setInt(3, getId());
			statement.setInt(4, period);
			statement.execute();
		}
		catch(Exception e)
		{
			Castle._log.warn("Error updating seed production data for castle " + getName() + "! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isNextPeriodApproved()
	{
		return _isNextPeriodApproved;
	}

	public void setNextPeriodApproved(final boolean val)
	{
		_isNextPeriodApproved = val;
	}

	@Override
	public void update()
	{
		CastleDAO.getInstance().update(this);
	}

	public void addMerchantGuard(final MerchantGuard merchantGuard)
	{
		_merchantGuards.put(merchantGuard.getItemId(), merchantGuard);
	}

	public MerchantGuard getMerchantGuard(final int itemId)
	{
		return _merchantGuards.get(itemId);
	}

	public IntObjectMap<MerchantGuard> getMerchantGuards()
	{
		return _merchantGuards;
	}

	public Set<ItemInstance> getSpawnMerchantTickets()
	{
		return _spawnMerchantTickets;
	}

	@Override
	public void startCycleTask()
	{}

	private void initArtefact()
	{
		switch(getId())
		{
			case 1:
			{
				_artefacts.add(35063);
				break;
			}
			case 2:
			{
				_artefacts.add(35105);
				break;
			}
			case 3:
			{
				_artefacts.add(35147);
				break;
			}
			case 4:
			{
				_artefacts.add(35189);
				break;
			}
			case 5:
			{
				_artefacts.add(35233);
				break;
			}
			case 6:
			{
				_artefacts.add(35279);
				break;
			}
			case 7:
			{
				_artefacts.add(35322);
				_artefacts.add(35323);
				break;
			}
			case 8:
			{
				_artefacts.add(35469);
				break;
			}
			case 9:
			{
				_artefacts.add(35514);
				_artefacts.add(35515);
				break;
			}
		}
	}

	public List<Integer> getArtefacts()
	{
		return _artefacts;
	}

	static
	{
		_log = LoggerFactory.getLogger(Castle.class);
	}
}
