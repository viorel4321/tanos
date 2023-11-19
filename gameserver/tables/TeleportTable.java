package l2s.gameserver.tables;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2s.gameserver.Config;
import l2s.gameserver.templates.item.ItemTemplate;

public class TeleportTable
{
	private static Logger _log;
	private static TeleportTable _instance;
	private HashMap<Integer, HashMap<Integer, TeleportLocation[]>> _lists;

	public static TeleportTable getInstance()
	{
		if(TeleportTable._instance == null)
			TeleportTable._instance = new TeleportTable();
		return TeleportTable._instance;
	}

	public static void reload()
	{
		TeleportTable._instance = new TeleportTable();
	}

	private TeleportTable()
	{
		_lists = new HashMap<Integer, HashMap<Integer, TeleportLocation[]>>();
		try
		{
			final File file = new File(Config.DATAPACK_ROOT, "data/teleports.xml");
			final DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			final Document doc1 = factory1.newDocumentBuilder().parse(file);
			int counter = 0;
			for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
				if("list".equalsIgnoreCase(n1.getNodeName()))
					for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
						if("npc".equalsIgnoreCase(d1.getNodeName()))
						{
							final HashMap<Integer, TeleportLocation[]> lists = new HashMap<Integer, TeleportLocation[]>();
							for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling())
								if("sublist".equalsIgnoreCase(s1.getNodeName()))
								{
									final List<TeleportLocation> targets = new ArrayList<TeleportLocation>();
									for(Node t1 = s1.getFirstChild(); t1 != null; t1 = t1.getNextSibling())
										if("target".equalsIgnoreCase(t1.getNodeName()))
										{
											++counter;
											final String target = t1.getAttributes().getNamedItem("loc").getNodeValue();
											final String name = t1.getAttributes().getNamedItem("name").getNodeValue();
											final String text = t1.getAttributes().getNamedItem("text") == null ? name : t1.getAttributes().getNamedItem("text").getNodeValue();
											final int random = t1.getAttributes().getNamedItem("random") == null ? 70 : Integer.parseInt(t1.getAttributes().getNamedItem("random").getNodeValue());
											final int price = Integer.parseInt(t1.getAttributes().getNamedItem("price").getNodeValue());
											final int item = t1.getAttributes().getNamedItem("item") == null ? 57 : Integer.parseInt(t1.getAttributes().getNamedItem("item").getNodeValue());
											final TeleportLocation t2 = new TeleportLocation(target, item, price, name, text, random);
											targets.add(t2);
										}
									if(!targets.isEmpty())
										lists.put(Integer.parseInt(s1.getAttributes().getNamedItem("id").getNodeValue()), targets.toArray(new TeleportLocation[targets.size()]));
								}
							if(!lists.isEmpty())
								_lists.put(Integer.parseInt(d1.getAttributes().getNamedItem("id").getNodeValue()), lists);
						}
			TeleportTable._log.info("TeleportController: Loaded " + counter + " locations.");
		}
		catch(Exception e)
		{
			TeleportTable._log.warn("TeleportTable: Lists could not be initialized.");
			e.printStackTrace();
		}
	}

	public TeleportLocation[] getTeleportLocationList(final int npcId, final int listId)
	{
		if(_lists.get(npcId) == null)
		{
			if(Config.DEBUG)
				TeleportTable._log.warn("Not found teleport location for npcId: " + npcId + ", listId: " + listId);
			return null;
		}
		return _lists.get(npcId).get(listId);
	}

	static
	{
		TeleportTable._log = LoggerFactory.getLogger(TeleportTable.class);
	}

	public class TeleportLocation
	{
		public int _price;
		public ItemTemplate _item;
		public String _name;
		public String _text;
		public String _target;
		public int _random;

		public TeleportLocation(final String target, final int item, final int price, final String name, final String text, final int random)
		{
			_target = target;
			_price = price;
			_name = name;
			_text = text;
			_random = random;
			_item = ItemTable.getInstance().getTemplate(item);
			if(_item == null)
				TeleportTable._log.warn("TeleportTable: Not found itemId: " + item + " name: " + _name);
		}
	}
}
