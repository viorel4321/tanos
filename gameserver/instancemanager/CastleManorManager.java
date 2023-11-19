package l2s.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.tables.ClanTable;

public class CastleManorManager
{
	protected static Logger _log;
	private static CastleManorManager _instance;
	public static final int PERIOD_CURRENT = 0;
	public static final int PERIOD_NEXT = 1;
	protected static final String var_name = "ManorApproved";
	private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
	private static final String CASTLE_MANOR_LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
	private static final int NEXT_PERIOD_APPROVE;
	private static final int NEXT_PERIOD_APPROVE_MIN;
	private static final int MANOR_REFRESH;
	private static final int MANOR_REFRESH_MIN;
	protected static final long MAINTENANCE_PERIOD;
	private boolean _underMaintenance;
	private boolean _disabled;

	public static CastleManorManager getInstance()
	{
		if(CastleManorManager._instance == null)
		{
			CastleManorManager._log.info("Manor System: Initializing...");
			CastleManorManager._instance = new CastleManorManager();
		}
		return CastleManorManager._instance;
	}

	private CastleManorManager()
	{
		load();
		init();
		_underMaintenance = false;
		_disabled = !Config.ALLOW_MANOR;
		for(final Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			c.setNextPeriodApproved(ServerVariables.getBool("ManorApproved"));
	}

	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(final Castle castle : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			{
				final List<SeedProduction> production = new ArrayList<SeedProduction>();
				final List<SeedProduction> productionNext = new ArrayList<SeedProduction>();
				final List<CropProcure> procure = new ArrayList<CropProcure>();
				final List<CropProcure> procureNext = new ArrayList<CropProcure>();
				statement = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castle_id=?");
				statement.setInt(1, castle.getId());
				rs = statement.executeQuery();
				while(rs.next())
				{
					final int seedId = rs.getInt("seed_id");
					final int canProduce = rs.getInt("can_produce");
					final int startProduce = rs.getInt("start_produce");
					final int price = rs.getInt("seed_price");
					final int period = rs.getInt("period");
					if(period == 0)
						production.add(new SeedProduction(seedId, canProduce, price, startProduce));
					else
						productionNext.add(new SeedProduction(seedId, canProduce, price, startProduce));
				}
				DbUtils.closeQuietly(statement, rs);
				castle.setSeedProduction(production, 0);
				castle.setSeedProduction(productionNext, 1);
				statement = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castle_id=?");
				statement.setInt(1, castle.getId());
				rs = statement.executeQuery();
				while(rs.next())
				{
					final int cropId = rs.getInt("crop_id");
					final int canBuy = rs.getInt("can_buy");
					final int startBuy = rs.getInt("start_buy");
					final int rewardType = rs.getInt("reward_type");
					final int price2 = rs.getInt("price");
					final int period2 = rs.getInt("period");
					if(period2 == 0)
						procure.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price2));
					else
						procureNext.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price2));
				}
				castle.setCropProcure(procure, 0);
				castle.setCropProcure(procureNext, 1);
				if(!procure.isEmpty() || !procureNext.isEmpty() || !production.isEmpty() || !productionNext.isEmpty())
					CastleManorManager._log.info("Manor System: Loaded data for " + castle.getName() + " castle");
				DbUtils.closeQuietly(statement, rs);
			}
			DbUtils.closeQuietly(con, statement, rs);
		}
		catch(Exception e)
		{
			CastleManorManager._log.error("Manor System: Error restoring manor data: " + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	protected void init()
	{
		if(ServerVariables.getString("ManorApproved", "").isEmpty())
		{
			final Calendar manorRefresh = Calendar.getInstance();
			manorRefresh.set(11, CastleManorManager.MANOR_REFRESH);
			manorRefresh.set(12, CastleManorManager.MANOR_REFRESH_MIN);
			manorRefresh.set(13, 0);
			manorRefresh.set(14, 0);
			final Calendar periodApprove = Calendar.getInstance();
			periodApprove.set(11, CastleManorManager.NEXT_PERIOD_APPROVE);
			periodApprove.set(12, CastleManorManager.NEXT_PERIOD_APPROVE_MIN);
			periodApprove.set(13, 0);
			periodApprove.set(14, 0);
			final boolean isApproved = periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();
			ServerVariables.set("ManorApproved", isApproved);
		}
		final Calendar FirstDelay = Calendar.getInstance();
		FirstDelay.set(13, 0);
		FirstDelay.set(14, 0);
		FirstDelay.add(12, 1);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new ManorTask(), FirstDelay.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), 60000L);
	}

	public void setNextPeriod()
	{
		for(final Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
		{
			if(c.getOwnerId() <= 0)
				continue;
			final Clan clan = ClanTable.getInstance().getClan(c.getOwnerId());
			if(clan == null)
				continue;
			final Warehouse cwh = clan.getWarehouse();
			for(final CropProcure crop : c.getCropProcure(0))
			{
				if(crop.getStartAmount() == 0)
					continue;
				if(crop.getStartAmount() > crop.getAmount())
				{
					int count = crop.getStartAmount() - crop.getAmount();
					count = count * 90 / 100;
					if(count < 1 && Rnd.get(99) < 90)
						count = 1;
					if(count >= 1)
					{
						final int id = Manor.getInstance().getMatureCrop(crop.getId());
						cwh.addItem(id, count);
					}
				}
				if(crop.getAmount() > 0)
					c.addToTreasuryNoTax(crop.getAmount() * crop.getPrice(), false, false);
				c.setCollectedShops(0);
				c.setCollectedSeed(0);
			}
			c.setSeedProduction(c.getSeedProduction(1), 0);
			c.setCropProcure(c.getCropProcure(1), 0);
			final int manor_cost = c.getManorCost(0);
			if(c.getTreasury() < manor_cost)
			{
				c.setSeedProduction(getNewSeedsList(c.getId()), 1);
				c.setCropProcure(getNewCropsList(c.getId()), 1);
			}
			else
			{
				final List<SeedProduction> production = new ArrayList<SeedProduction>();
				final List<CropProcure> procure = new ArrayList<CropProcure>();
				for(final SeedProduction s : c.getSeedProduction(0))
				{
					s.setCanProduce(s.getStartProduce());
					production.add(s);
				}
				for(final CropProcure cr : c.getCropProcure(0))
				{
					cr.setAmount(cr.getStartAmount());
					procure.add(cr);
				}
				c.setSeedProduction(production, 1);
				c.setCropProcure(procure, 1);
			}
			c.saveCropData();
			c.saveSeedData();
			PlayerMessageStack.getInstance().mailto(clan.getLeaderId(), Msg.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
			c.setNextPeriodApproved(false);
		}
	}

	public void approveNextPeriod()
	{
		for(final Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
		{
			if(c.getOwnerId() <= 0)
				continue;
			int manor_cost = c.getManorCost(1);
			if(c.getTreasury() < manor_cost)
			{
				c.setSeedProduction(getNewSeedsList(c.getId()), 1);
				c.setCropProcure(getNewCropsList(c.getId()), 1);
				manor_cost = c.getManorCost(1);
				final Clan clan = ClanTable.getInstance().getClan(c.getOwnerId());
				PlayerMessageStack.getInstance().mailto(clan.getLeaderId(), Msg.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
			}
			else
				c.addToTreasuryNoTax(-manor_cost, false, false);
			c.setNextPeriodApproved(true);
		}
	}

	private List<SeedProduction> getNewSeedsList(final int castleId)
	{
		final List<SeedProduction> seeds = new ArrayList<SeedProduction>();
		final List<Integer> seedsIds = Manor.getInstance().getSeedsForCastle(castleId);
		for(final int sd : seedsIds)
			seeds.add(new SeedProduction(sd));
		return seeds;
	}

	private List<CropProcure> getNewCropsList(final int castleId)
	{
		final List<CropProcure> crops = new ArrayList<CropProcure>();
		final List<Integer> cropsIds = Manor.getInstance().getCropsForCastle(castleId);
		for(final int cr : cropsIds)
			crops.add(new CropProcure(cr));
		return crops;
	}

	public boolean isUnderMaintenance()
	{
		return _underMaintenance;
	}

	public void setUnderMaintenance(final boolean mode)
	{
		_underMaintenance = mode;
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(final boolean mode)
	{
		_disabled = mode;
	}

	public SeedProduction getNewSeedProduction(final int id, final int amount, final int price, final int sales)
	{
		return new SeedProduction(id, amount, price, sales);
	}

	public CropProcure getNewCropProcure(final int id, final int amount, final int type, final int price, final int buy)
	{
		return new CropProcure(id, amount, type, buy, price);
	}

	public void save()
	{
		for(final Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
		{
			c.saveSeedData();
			c.saveCropData();
		}
	}

	public String getOwner(final int castleId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id FROM clan_data WHERE hasCastle = ? LIMIT 1");
			statement.setInt(1, castleId);
			rs = statement.executeQuery();
			if(rs.next())
				return ClanTable.getInstance().getClan(rs.getInt("clan_id")).toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
		return null;
	}

	static
	{
		CastleManorManager._log = LoggerFactory.getLogger(CastleManorManager.class);
		NEXT_PERIOD_APPROVE = Config.MANOR_APPROVE_TIME;
		NEXT_PERIOD_APPROVE_MIN = Config.MANOR_APPROVE_MIN;
		MANOR_REFRESH = Config.MANOR_REFRESH_TIME;
		MANOR_REFRESH_MIN = Config.MANOR_REFRESH_MIN;
		MAINTENANCE_PERIOD = Config.MANOR_MAINTENANCE_PERIOD / 60000;
	}

	public class CropProcure
	{
		int _rewardType;
		int _cropId;
		int _buyResidual;
		int _buy;
		int _price;

		public CropProcure(final int id)
		{
			_cropId = id;
			_buyResidual = 0;
			_rewardType = 0;
			_buy = 0;
			_price = 0;
		}

		public CropProcure(final int id, final int amount, final int type, final int buy, final int price)
		{
			_cropId = id;
			_buyResidual = amount;
			_rewardType = type;
			_buy = buy;
			_price = price;
			if(_price < 0)
			{
				_price = 0;
				_log.warn("CropProcure price = " + price);
				Thread.dumpStack();
			}
		}

		public int getReward()
		{
			return _rewardType;
		}

		public int getId()
		{
			return _cropId;
		}

		public int getAmount()
		{
			return _buyResidual;
		}

		public int getStartAmount()
		{
			return _buy;
		}

		public int getPrice()
		{
			return _price;
		}

		public void setAmount(final int amount)
		{
			_buyResidual = amount;
		}
	}

	public class SeedProduction
	{
		int _seedId;
		int _residual;
		int _price;
		int _sales;

		public SeedProduction(final int id)
		{
			_seedId = id;
			_sales = 0;
			_price = 0;
			_sales = 0;
		}

		public SeedProduction(final int id, final int amount, final int price, final int sales)
		{
			_seedId = id;
			_residual = amount;
			_price = price;
			_sales = sales;
		}

		public int getId()
		{
			return _seedId;
		}

		public int getCanProduce()
		{
			return _residual;
		}

		public int getPrice()
		{
			return _price;
		}

		public int getStartProduce()
		{
			return _sales;
		}

		public void setCanProduce(final int amount)
		{
			_residual = amount;
		}
	}

	private class ManorTask implements Runnable
	{
		@Override
		public void run()
		{
			final int H = Calendar.getInstance().get(11);
			final int M = Calendar.getInstance().get(12);
			if(ServerVariables.getBool("ManorApproved"))
			{
				if(H < CastleManorManager.NEXT_PERIOD_APPROVE || H > CastleManorManager.MANOR_REFRESH || H == CastleManorManager.MANOR_REFRESH && M >= CastleManorManager.MANOR_REFRESH_MIN)
				{
					ServerVariables.set("ManorApproved", false);
					setUnderMaintenance(true);
				}
			}
			else if(isUnderMaintenance())
			{
				if(H != CastleManorManager.MANOR_REFRESH || M >= CastleManorManager.MANOR_REFRESH_MIN + CastleManorManager.MAINTENANCE_PERIOD)
				{
					setUnderMaintenance(false);
					if(isDisabled())
						return;
					setNextPeriod();
					try
					{
						save();
					}
					catch(Exception e)
					{
						CastleManorManager._log.error("Manor System: Failed to save manor data: " + e);
					}
				}
			}
			else if(H > CastleManorManager.NEXT_PERIOD_APPROVE && H < CastleManorManager.MANOR_REFRESH || H == CastleManorManager.NEXT_PERIOD_APPROVE && M >= CastleManorManager.NEXT_PERIOD_APPROVE_MIN)
			{
				ServerVariables.set("ManorApproved", true);
				if(isDisabled())
					return;
				approveNextPeriod();
			}
		}
	}
}
