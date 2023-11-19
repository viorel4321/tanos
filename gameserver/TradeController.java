package l2s.gameserver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2s.gameserver.model.TradeItem;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class TradeController
{
	private static Logger _log = LoggerFactory.getLogger(TradeController.class);;
	private static TradeController _instance;
	private HashMap<Integer, NpcTradeList> _lists;

	public static TradeController getInstance()
	{
		if(TradeController._instance == null)
			TradeController._instance = new TradeController();
		return TradeController._instance;
	}

	public static void reload()
	{
		TradeController._instance = new TradeController();
	}

	private TradeController()
	{
		_lists = new HashMap<Integer, NpcTradeList>();
		try
		{
			final File filelists = new File(Config.DATAPACK_ROOT, "data/merchant_filelists.xml");
			final DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			final Document doc1 = factory1.newDocumentBuilder().parse(filelists);
			int counterFiles = 0;
			int counterItems = 0;
			for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
				if("list".equalsIgnoreCase(n1.getNodeName()))
					for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
						if("file".equalsIgnoreCase(d1.getNodeName()))
						{
							final String filename = d1.getAttributes().getNamedItem("name").getNodeValue();
							final File file = new File(Config.DATAPACK_ROOT, "data/" + filename);
							final DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
							factory2.setValidating(false);
							factory2.setIgnoringComments(true);
							final Document doc2 = factory2.newDocumentBuilder().parse(file);
							++counterFiles;
							for(Node n2 = doc2.getFirstChild(); n2 != null; n2 = n2.getNextSibling())
								if("list".equalsIgnoreCase(n2.getNodeName()))
									for(Node d2 = n2.getFirstChild(); d2 != null; d2 = d2.getNextSibling())
										if("tradelist".equalsIgnoreCase(d2.getNodeName()))
										{
											final int shop_id = Integer.parseInt(d2.getAttributes().getNamedItem("shop").getNodeValue());
											final int npc_id = Integer.parseInt(d2.getAttributes().getNamedItem("npc").getNodeValue());
											final float markup = npc_id > 0 ? Config.ALLOW_MARKUP ? 1.0f + Float.parseFloat(d2.getAttributes().getNamedItem("markup").getNodeValue()) / 100.0f : 1.0f : 0.0f;
											final NpcTradeList tl = new NpcTradeList(shop_id);
											tl.setNpcId(npc_id);
											for(Node i = d2.getFirstChild(); i != null; i = i.getNextSibling())
												if("item".equalsIgnoreCase(i.getNodeName()))
												{
													final int itemId = Integer.parseInt(i.getAttributes().getNamedItem("id").getNodeValue());
													final ItemTemplate template = ItemTable.getInstance().getTemplate(itemId);
													if(template == null)
														TradeController._log.warn("Template not found for itemId: " + itemId + " for shop " + shop_id);
													else if(checkItem(template))
													{
														++counterItems;
														final int price = i.getAttributes().getNamedItem("price") != null ? Integer.parseInt(i.getAttributes().getNamedItem("price").getNodeValue()) : Math.round(template.getReferencePrice() * markup);
														final TradeItem item = new TradeItem();
														item.setItemId(itemId);
														final int itemCount = i.getAttributes().getNamedItem("count") != null ? Integer.parseInt(i.getAttributes().getNamedItem("count").getNodeValue()) : 0;
														final int itemRechargeTime = i.getAttributes().getNamedItem("time") != null ? Integer.parseInt(i.getAttributes().getNamedItem("time").getNodeValue()) : 0;
														item.setOwnersPrice(price);
														item.setCount(itemCount);
														item.setCurrentValue(itemCount);
														item.setLastRechargeTime((int) (System.currentTimeMillis() / 60000L));
														item.setRechargeTime(itemRechargeTime);
														tl.addItem(item);
													}
												}
											_lists.put(shop_id, tl);
										}
						}
			TradeController._log.info("TradeController: Loaded " + counterFiles + " file(s).");
			TradeController._log.info("TradeController: Loaded " + counterItems + " Items.");
			TradeController._log.info("TradeController: Loaded " + _lists.size() + " Buylists.");
		}
		catch(Exception e)
		{
			TradeController._log.error("TradeController: Buylists could not be initialized.", e);
		}
	}

	public boolean checkItem(final ItemTemplate template)
	{
		if(template.isEquipment() && !template.isForPet() && Config.ALT_SHOP_PRICE_LIMITS.length > 0)
		{
			int i = 0;
			while(i < Config.ALT_SHOP_PRICE_LIMITS.length)
				if(template.getBodyPart() == Config.ALT_SHOP_PRICE_LIMITS[i])
				{
					if(template.getReferencePrice() > Config.ALT_SHOP_PRICE_LIMITS[i + 1])
						return false;
					break;
				}
				else
					i += 2;
		}
		if(Config.ALT_SHOP_UNALLOWED_ITEMS.length > 0)
			for(final int j : Config.ALT_SHOP_UNALLOWED_ITEMS)
				if(template.getItemId() == j)
					return false;
		return true;
	}

	public NpcTradeList getBuyList(final int listId)
	{
		return _lists.get(listId);
	}

	public void addToBuyList(final int listId, final NpcTradeList list)
	{
		_lists.put(listId, list);
	}

	public static class NpcTradeList
	{
		private static final List<TradeItem> emptyList = new ArrayList<TradeItem>(0);;
		private List<TradeItem> tradeList;
		private int _id;
		private int _npcId;

		public NpcTradeList(final int id)
		{
			_id = id;
		}

		public int getListId()
		{
			return _id;
		}

		public void setNpcId(final int id)
		{
			_npcId = id;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public void addItem(final TradeItem ti)
		{
			if(tradeList == null)
				tradeList = new ArrayList<TradeItem>();
			tradeList.add(ti);
		}

		public List<TradeItem> getItems()
		{
			return tradeList == null ? NpcTradeList.emptyList : tradeList;
		}
	}
}
