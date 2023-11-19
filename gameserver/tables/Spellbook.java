package l2s.gameserver.tables;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import gnu.trove.map.hash.TIntIntHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.DocumentFactory;

public class Spellbook
{
	private static Logger _log;
	private static TIntIntHashMap _skillSpellbooks;

	public static Spellbook getInstance()
	{
		return SingletonHolder._instance;
	}

	private Spellbook()
	{
		_skillSpellbooks = new TIntIntHashMap();
		try
		{
			final File f = new File(Config.DATAPACK_ROOT, "data/pts/skillstrees/spellbooks.xml");
			final Document doc = DocumentFactory.getInstance().loadDocument(f);
			final Node n = doc.getFirstChild();
			for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				if(d.getNodeName().equalsIgnoreCase("book"))
					_skillSpellbooks.put(Integer.valueOf(d.getAttributes().getNamedItem("skill_id").getNodeValue()), Integer.valueOf(d.getAttributes().getNamedItem("item_id").getNodeValue()));
		}
		catch(Exception e)
		{
			Spellbook._log.warn("Error while loading spellbook data: " + e.getMessage());
		}
		Spellbook._log.info("SpellbookTable: Loaded " + _skillSpellbooks.size() + " spellbooks.");
	}

	public int getBookForSkill(final int skillId, final int level)
	{
		if(skillId == 1405 && level != -1)
			switch(level)
			{
				case 1:
				{
					return 8618;
				}
				case 2:
				{
					return 8619;
				}
				case 3:
				{
					return 8620;
				}
				case 4:
				{
					return 8621;
				}
				default:
				{
					return -1;
				}
			}
		else
		{
			if(!Spellbook._skillSpellbooks.containsKey(skillId))
				return -1;
			return Spellbook._skillSpellbooks.get(skillId);
		}
	}

	static
	{
		Spellbook._log = LoggerFactory.getLogger(Spellbook.class);
	}

	private static class SingletonHolder
	{
		protected static final Spellbook _instance = new Spellbook();
	}
}
