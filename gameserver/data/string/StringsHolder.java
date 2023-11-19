package l2s.gameserver.data.string;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Language;

/**
 * Author: VISTALL
 * Date:  19:27/29.12.2010
 */
public final class StringsHolder extends AbstractHolder
{
	private static final StringsHolder _instance = new StringsHolder();

	private final Map<Language, Map<String, String>> _strings = new HashMap<Language, Map<String, String>>();

	public static StringsHolder getInstance()
	{
		return _instance;
	}

	private StringsHolder()
	{
		//
	}

	public String getString(String name, Player player)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getString(name, lang);
	}
	
	public String getString(Player player, String name)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getString(name, lang);
	}

	public String getString(String address, Language lang)
	{
		Map<String, String> strings = _strings.get(lang);
		String value = strings.get(address);
		if(value == null)
		{
			Language secondLang = lang;
			do
			{
				if(secondLang == secondLang.getSecondLanguage())
					break;

				secondLang = secondLang.getSecondLanguage();
				strings = _strings.get(secondLang);
				value = strings.get(address);
			}
			while(value == null);

			if(value == null)
			{
				for(Language l : Language.VALUES)
				{
					strings = _strings.get(secondLang);
					if((value = strings.get(address)) != null)
						break;
				}
			}
		}
		return value;
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_strings.put(lang, new HashMap<String, String>());

			if(!Config.AVAILABLE_LANGUAGES.contains(lang))
				continue;

			File file = new File(Config.DATAPACK_ROOT, "data/string/" + lang.getShortName() + ".properties");
			if(!file.exists())
				warn("Not find file: " + file.getAbsolutePath());
			else
				loadFile(file, lang);

			file = new File(Config.DATAPACK_ROOT, "data/string/" + lang.getShortName() + "/");
			if(file.exists() && file.isDirectory())
			{
				for(File f : file.listFiles())
				{
					if(f.getName().matches("^(.+)\\.properties$"))
						loadFile(f, lang);
				}
			}
		}
		log();
	}

	private void loadFile(File file, Language lang)
	{
		LineNumberReader reader = null;
		try
		{
			reader = new LineNumberReader(new FileReader(file));
			String line = null;
			while((line = reader.readLine()) != null)
			{
				if(line.startsWith("#"))
					continue;

				StringTokenizer token = new StringTokenizer(line, "=");
				if(token.countTokens() < 2)
				{
					error("Error on line: " + line + "; file: " + file.getName());
					continue;
				}

				String name = token.nextToken();
				String value = token.nextToken();
				while(token.hasMoreTokens())
					value += "=" + token.nextToken();

				_strings.get(lang).put(name, value);
			}
		}
		catch(Exception e)
		{
			error("Exception: " + e, e);
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch(Exception e)
			{
				//
			}
		}
	}

	public void reload()
	{
		clear();
		load();
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, Map<String, String>> entry : _strings.entrySet())
		{
			if(!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
			info("load strings: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size()
	{
		return _strings.size();
	}

	@Override
	public void clear()
	{
		_strings.clear();
	}
}
