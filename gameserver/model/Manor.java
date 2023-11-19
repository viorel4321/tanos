package l2s.gameserver.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class Manor
{
	private static Logger _log;
	private static Manor _instance;
	private static Map<Integer, SeedData> _seeds;

	public Manor()
	{
		_seeds = new HashMap<Integer, SeedData>();
		parseData();
	}

	public static Manor getInstance()
	{
		if(_instance == null)
			_instance = new Manor();
		return _instance;
	}

	public List<Integer> getAllCrops()
	{
		final List<Integer> crops = new ArrayList<Integer>();
		for(final SeedData seed : Manor._seeds.values())
			if(!crops.contains(seed.getCrop()) && seed.getCrop() != 0 && !crops.contains(seed.getCrop()))
				crops.add(seed.getCrop());
		return crops;
	}

	public Map<Integer, SeedData> getAllSeeds()
	{
		return Manor._seeds;
	}

	public int getSeedBasicPrice(final int seedId)
	{
		final ItemTemplate seedItem = ItemTable.getInstance().getTemplate(seedId);
		if(seedItem != null)
			return seedItem.getReferencePrice();
		return 0;
	}

	public int getSeedBasicPriceByCrop(final int cropId)
	{
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getCrop() == cropId)
				return getSeedBasicPrice(seed.getId());
		return 0;
	}

	public int getCropBasicPrice(final int cropId)
	{
		final ItemTemplate cropItem = ItemTable.getInstance().getTemplate(cropId);
		if(cropItem != null)
			return cropItem.getReferencePrice();
		return 0;
	}

	public int getMatureCrop(final int cropId)
	{
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getCrop() == cropId)
				return seed.getMature();
		return 0;
	}

	public long getSeedBuyPrice(final int seedId)
	{
		final long buyPrice = getSeedBasicPrice(seedId) / 10;
		return buyPrice >= 0L ? buyPrice : 1L;
	}

	public int getSeedMinLevel(final int seedId)
	{
		final SeedData seed = Manor._seeds.get(seedId);
		if(seed != null)
			return seed.getLevel() - 5;
		return -1;
	}

	public int getSeedMaxLevel(final int seedId)
	{
		final SeedData seed = Manor._seeds.get(seedId);
		if(seed != null)
			return seed.getLevel() + 5;
		return -1;
	}

	public int getSeedLevelByCrop(final int cropId)
	{
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getCrop() == cropId)
				return seed.getLevel();
		return 0;
	}

	public int getSeedLevel(final int seedId)
	{
		final SeedData seed = Manor._seeds.get(seedId);
		if(seed != null)
			return seed.getLevel();
		return -1;
	}

	public boolean isAlternative(final int seedId)
	{
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getId() == seedId)
				return seed.isAlternative();
		return false;
	}

	public int getCropType(final int seedId)
	{
		final SeedData seed = Manor._seeds.get(seedId);
		if(seed != null)
			return seed.getCrop();
		return -1;
	}

	public synchronized int getRewardItem(final int cropId, final int type)
	{
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getCrop() == cropId)
				return seed.getReward(type);
		return -1;
	}

	public synchronized long getRewardAmountPerCrop(final int castle, final int cropId, final int type)
	{
		final CastleManorManager.CropProcure cs = ResidenceHolder.getInstance().getResidence(Castle.class, castle).getCropProcure(0).get(cropId);
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getCrop() == cropId)
				return cs.getPrice() / getCropBasicPrice(seed.getReward(type));
		return -1L;
	}

	public synchronized int getRewardItemBySeed(final int seedId, final int type)
	{
		final SeedData seed = Manor._seeds.get(seedId);
		if(seed != null)
			return seed.getReward(type);
		return 0;
	}

	public List<Integer> getCropsForCastle(final int castleId)
	{
		final List<Integer> crops = new ArrayList<Integer>();
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getManorId() == castleId && !crops.contains(seed.getCrop()))
				crops.add(seed.getCrop());
		return crops;
	}

	public List<Integer> getSeedsForCastle(final int castleId)
	{
		final List<Integer> seedsID = new ArrayList<Integer>();
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getManorId() == castleId && !seedsID.contains(seed.getId()))
				seedsID.add(seed.getId());
		return seedsID;
	}

	public int getCastleIdForSeed(final int seedId)
	{
		final SeedData seed = Manor._seeds.get(seedId);
		if(seed != null)
			return seed.getManorId();
		return 0;
	}

	public long getSeedSaleLimit(final int seedId)
	{
		final SeedData seed = Manor._seeds.get(seedId);
		if(seed != null)
			return seed.getSeedLimit();
		return 0L;
	}

	public long getCropPuchaseLimit(final int cropId)
	{
		for(final SeedData seed : Manor._seeds.values())
			if(seed.getCrop() == cropId)
				return seed.getCropLimit();
		return 0L;
	}

	private void parseData()
	{
		LineNumberReader lnr = null;
		try
		{
			final File seedData = new File(Config.DATAPACK_ROOT, "data/seeds.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(seedData)));
			String line = null;
			while((line = lnr.readLine()) != null)
				if(line.trim().length() != 0)
				{
					if(line.startsWith("#"))
						continue;
					final SeedData seed = parseList(line);
					Manor._seeds.put(seed.getId(), seed);
				}
			Manor._log.info("ManorManager: Loaded " + Manor._seeds.size() + " seeds");
		}
		catch(FileNotFoundException e2)
		{
			Manor._log.info("seeds.csv is missing in data folder");
		}
		catch(Exception e)
		{
			Manor._log.info("error while loading seeds: " + e.getMessage());
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception ex)
			{}
		}
	}

	private SeedData parseList(final String line)
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		final int seedId = Integer.parseInt(st.nextToken());
		final int level = Integer.parseInt(st.nextToken());
		final int cropId = Integer.parseInt(st.nextToken());
		final int matureId = Integer.parseInt(st.nextToken());
		final int type1R = Integer.parseInt(st.nextToken());
		final int type2R = Integer.parseInt(st.nextToken());
		final int manorId = Integer.parseInt(st.nextToken());
		final int isAlt = Integer.parseInt(st.nextToken());
		final long limitSeeds = Math.round(Integer.parseInt(st.nextToken()) * Config.RATE_MANOR);
		final long limitCrops = Math.round(Integer.parseInt(st.nextToken()) * Config.RATE_MANOR);
		final SeedData seed = new SeedData(level, cropId, matureId);
		seed.setData(seedId, type1R, type2R, manorId, isAlt, limitSeeds, limitCrops);
		return seed;
	}

	static
	{
		Manor._log = LoggerFactory.getLogger(Manor.class);
	}

	public class SeedData
	{
		private int _id;
		private int _level;
		private int _crop;
		private int _mature;
		private int _type1;
		private int _type2;
		private int _manorId;
		private int _isAlternative;
		private long _limitSeeds;
		private long _limitCrops;

		public SeedData(final int level, final int crop, final int mature)
		{
			_level = level;
			_crop = crop;
			_mature = mature;
		}

		public void setData(final int id, final int t1, final int t2, final int manorId, final int isAlt, final long lim1, final long lim2)
		{
			_id = id;
			_type1 = t1;
			_type2 = t2;
			_manorId = manorId;
			_isAlternative = isAlt;
			_limitSeeds = lim1;
			_limitCrops = lim2;
		}

		public int getManorId()
		{
			return _manorId;
		}

		public int getId()
		{
			return _id;
		}

		public int getCrop()
		{
			return _crop;
		}

		public int getMature()
		{
			return _mature;
		}

		public int getReward(final int type)
		{
			return type == 1 ? _type1 : _type2;
		}

		public int getLevel()
		{
			return _level;
		}

		public boolean isAlternative()
		{
			return _isAlternative == 1;
		}

		public long getSeedLimit()
		{
			return _limitSeeds;
		}

		public long getCropLimit()
		{
			return _limitCrops;
		}
	}
}
