package l2s.gameserver.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.quest.Quest;

public class AddonsConfig
{
	private static final Logger _log;
	private static ConcurrentHashMap<Integer, Float> questRewardRates;
	private static ConcurrentHashMap<Integer, Float> questDropRates;

	public static void load()
	{
		File file = new File("./config/Advanced/quest_reward_rates.properties");
		if(!file.exists())
			AddonsConfig._log.warn("config/Advanced/quest_reward_rates.properties not exists! Config not loaded!");
		else
			parseFile(file);
		file = new File("./config/Advanced/quest_drop_rates.properties");
		if(!file.exists())
			AddonsConfig._log.warn("config/Advanced/quest_drop_rates.properties not exists! Config not loaded!");
		else
			parseFile(file);
	}

	public static void reload()
	{
		synchronized (AddonsConfig.questRewardRates)
		{
			synchronized (AddonsConfig.questDropRates)
			{
				AddonsConfig.questRewardRates = new ConcurrentHashMap<Integer, Float>();
				AddonsConfig.questDropRates = new ConcurrentHashMap<Integer, Float>();
				load();
			}
		}
	}

	private static void parseFile(final File f)
	{
		if(f.getName().startsWith("quest_reward_rates"))
			try
			{
				final InputStream is = new FileInputStream(f);
				final Properties p = new Properties();
				p.load(is);
				loadQuestRewardRates(p);
				is.close();
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch(IOException e2)
			{
				e2.printStackTrace();
			}
		else if(f.getName().startsWith("quest_drop_rates"))
			try
			{
				final InputStream is = new FileInputStream(f);
				final Properties p = new Properties();
				p.load(is);
				loadQuestDropRates(p);
				is.close();
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch(IOException e2)
			{
				e2.printStackTrace();
			}
	}

	private static void loadQuestRewardRates(final Properties p)
	{
		for(final String name : p.stringPropertyNames())
		{
			int id;
			try
			{
				id = Integer.parseInt(name);
			}
			catch(NumberFormatException nfe)
			{
				continue;
			}
			if(AddonsConfig.questRewardRates.get(id) != null)
				AddonsConfig.questRewardRates.replace(id, Float.parseFloat(p.getProperty(name).trim()));
			else if(p.getProperty(name) == null)
				AddonsConfig._log.info("Null property for quest id " + name);
			else
				AddonsConfig.questRewardRates.put(id, Float.parseFloat(p.getProperty(name).trim()));
		}
		p.clear();
	}

	private static void loadQuestDropRates(final Properties p)
	{
		for(final String name : p.stringPropertyNames())
		{
			int id;
			try
			{
				id = Integer.parseInt(name);
			}
			catch(NumberFormatException nfe)
			{
				continue;
			}
			if(AddonsConfig.questDropRates.get(id) != null)
				AddonsConfig.questDropRates.replace(id, Float.parseFloat(p.getProperty(name).trim()));
			else if(p.getProperty(name) == null)
				AddonsConfig._log.info("Null property for quest id " + name);
			else
				AddonsConfig.questDropRates.put(id, Float.parseFloat(p.getProperty(name).trim()));
		}
		p.clear();
	}

	public static float getQuestRewardRates(final Quest q)
	{
		return AddonsConfig.questRewardRates.containsKey(q.getId()) ? AddonsConfig.questRewardRates.get(q.getId()) : 1.0f;
	}

	public static float getQuestDropRates(final Quest q)
	{
		return AddonsConfig.questDropRates.containsKey(q.getId()) ? AddonsConfig.questDropRates.get(q.getId()) : 1.0f;
	}

	static
	{
		_log = LoggerFactory.getLogger(AddonsConfig.class);
		AddonsConfig.questRewardRates = new ConcurrentHashMap<Integer, Float>();
		AddonsConfig.questDropRates = new ConcurrentHashMap<Integer, Float>();
	}
}
