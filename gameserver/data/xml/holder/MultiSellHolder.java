package l2s.gameserver.data.xml.holder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.s2c.MultiSellList;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.XmlUtils;

public class MultiSellHolder
{
	private static final Logger _log = LoggerFactory.getLogger(MultiSellHolder.class);

	private static MultiSellHolder _instance = new MultiSellHolder();

	private static final String NODE_PRODUCTION = "production";
	private static final String NODE_INGRIDIENT = "ingredient";

	private TIntObjectHashMap<MultiSellListContainer> entries;

	public static MultiSellHolder getInstance()
	{
		return _instance;
	}

	public MultiSellListContainer getList(final int id)
	{
		return entries.get(id);
	}

	public MultiSellHolder()
	{
		entries = new TIntObjectHashMap<MultiSellListContainer>();
		parseData();
	}

	public void reload()
	{
		parseData();
	}

	private void parseData()
	{
		entries.clear();
		parse();
	}

	private void hashFiles(final String dirname, final List<File> hash)
	{
		final File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if(!dir.exists())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		final File[] listFiles;
		final File[] files = listFiles = dir.listFiles();
		for(final File f : listFiles)
			if(f.getName().endsWith(".xml"))
				hash.add(f);
			else if(f.isDirectory() && !f.getName().equals(".svn"))
				hashFiles(dirname + "/" + f.getName(), hash);
	}

	public void addMultiSellListContainer(final int id, final MultiSellListContainer list)
	{
		if(entries.containsKey(id))
			_log.warn("MultiSell redefined: " + id);
		list.setListId(id);
		entries.put(id, list);
	}

	public MultiSellListContainer remove(final String s)
	{
		return this.remove(new File(s));
	}

	public MultiSellListContainer remove(final File f)
	{
		return this.remove(Integer.parseInt(f.getName().replaceAll(".xml", "")));
	}

	public MultiSellListContainer remove(final int id)
	{
		return entries.remove(id);
	}

	public void parseFile(final File f)
	{
		int id = 0;
		try
		{
			id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
		}
		catch(Exception e)
		{
			_log.error("Error loading file " + f, e);
			return;
		}
		Document doc = null;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(f);
		}
		catch(Exception e2)
		{
			_log.error("Error loading file " + f, e2);
			return;
		}
		try
		{
			addMultiSellListContainer(id, parseDocument(doc, id));
		}
		catch(Exception e2)
		{
			_log.error("Error in file " + f, e2);
		}
	}

	private void parse()
	{
		final List<File> files = new ArrayList<File>();
		hashFiles("multisell", files);
		for(final File f : files)
			parseFile(f);
	}

	protected MultiSellListContainer parseDocument(final Document doc, final int id)
	{
		final MultiSellListContainer list = new MultiSellListContainer();
		int entId = 1;
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if("list".equalsIgnoreCase(n.getNodeName()))
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if("item".equalsIgnoreCase(d.getNodeName()))
					{
						final MultiSellEntry e = parseEntry(d, id);
						if(e != null)
						{
							e.setEntryId(entId++);
							list.addEntry(e);
						}
					}
					else if("config".equalsIgnoreCase(d.getNodeName()))
					{
						list.setShowAll(XmlUtils.getAttributeBooleanValue(d, "showall", true));
						list.setNoTax(XmlUtils.getAttributeBooleanValue(d, "notax", false));
						list.setKeepEnchant(XmlUtils.getAttributeBooleanValue(d, "keepenchanted", false));
						list.setExtra(XmlUtils.getAttributeBooleanValue(d, "extra", false));
						list.setNoKey(XmlUtils.getAttributeBooleanValue(d, "nokey", false));
						list.setNpcId(XmlUtils.getAttributeIntArrayValue(d, "npcid", new int[0]));
					}
		return list;
	}

	protected MultiSellEntry parseEntry(final Node n, final int multiSellId)
	{
		final MultiSellEntry entry = new MultiSellEntry();
		for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if(NODE_INGRIDIENT.equalsIgnoreCase(d.getNodeName()))
			{
				final int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
				final int count = Integer.parseInt(d.getAttributes().getNamedItem("count").getNodeValue());
				final MultiSellIngredient mi = new MultiSellIngredient(id, count);
				if(d.getAttributes().getNamedItem("enchant") != null)
					mi.setItemEnchant(Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue()));
				if(d.getAttributes().getNamedItem("mantainIngredient") != null)
					mi.setMantainIngredient(Boolean.parseBoolean(d.getAttributes().getNamedItem("mantainIngredient").getNodeValue()));
				entry.addIngredient(mi);
			}
			else if(NODE_PRODUCTION.equalsIgnoreCase(d.getNodeName()))
			{
				final int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
				final int count = Integer.parseInt(d.getAttributes().getNamedItem("count").getNodeValue());
				final MultiSellIngredient mi = new MultiSellIngredient(id, count);
				if(d.getAttributes().getNamedItem("enchant") != null)
					mi.setItemEnchant(Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue()));
				if(!Config.ALT_ALLOW_SHADOW_WEAPONS && id > 0)
				{
					final ItemTemplate item = ItemTable.getInstance().getTemplate(id);
					if(item != null && item.isShadowItem() && item.isWeapon() && !Config.ALT_ALLOW_SHADOW_WEAPONS)
						return null;
				}
				entry.addProduct(mi);
			}
		}

		if(entry.getIngredients().isEmpty() || entry.getProduction().isEmpty())
		{
			_log.warn("MultiSell [" + multiSellId + "] is empty!");
			return null;
		}

		for(MultiSellIngredient ingridient : entry.getIngredients())
		{
			if(ingridient.getItemId() == ItemTemplate.ITEM_ID_ADENA && ingridient.getItemCount() == -1)
			{
				int price = 0;
				for(MultiSellIngredient product : entry.getProduction())
				{
					ItemTemplate item = ItemTable.getInstance().getTemplate(product.getItemId());
					if(item == null)
						continue;

					price += item.getReferencePrice() * product.getItemCount();
				}
				ingridient.setItemCount(price);
			}
			else if(ingridient.getItemCount() <= 0)
			{
				_log.warn("MultiSell [" + multiSellId + "] ingridient ID[" + ingridient.getItemId() + "] has negative item count!");
				return null;
			}
		}

		if(entry.getIngredients().size() == 1 && entry.getProduction().size() == 1 && entry.getIngredients().get(0).getItemId() == 57)
		{
			final ItemTemplate item2 = ItemTable.getInstance().getTemplate(entry.getProduction().get(0).getItemId());
			if(item2 == null)
			{
				_log.warn("MultiSell [" + multiSellId + "] Production [" + entry.getProduction().get(0).getItemId() + "] not found!");
				return null;
			}
			if(Config.DIVIDER_PRICES > 1)
				entry.getIngredients().get(0).setItemCount(Math.max(entry.getIngredients().get(0).getItemCount() / Config.DIVIDER_PRICES, 1));
			for(final int i : Config.ALT_REF_MULTISELL)
				if(i == multiSellId)
					entry.getIngredients().get(0).setItemCount(item2.getReferencePrice());
			if(Config.MULTISELL_WARN && Config.DIVIDER_PRICES !=-1)
			{
				final int cn = item2.getReferencePrice() / Config.DIVIDER_SELL;
				if(cn > entry.getIngredients().get(0).getItemCount())
					_log.warn("MultiSell [" + multiSellId + "] Production '" + item2.getName() + "' [" + entry.getProduction().get(0).getItemId() + "] price is lower than sell price | " + cn + " > " + entry.getIngredients().get(0).getItemCount());
			}
		}
		return entry;
	}

	private static int[] parseItemIdAndCount(final String s)
	{
		if(s == null || s.isEmpty())
			return null;
		final String[] a = s.split(":");
		try
		{
			final int id = Integer.parseInt(a[0]);
			final int count = a.length > 1 ? Integer.parseInt(a[1]) : 1;
			return new int[] { id, count };
		}
		catch(Exception e)
		{
			_log.error("", e);
			return null;
		}
	}

	public static MultiSellEntry parseEntryFromStr(final String s)
	{
		if(s == null || s.isEmpty())
			return null;
		final String[] a = s.split("->");
		if(a.length != 2)
			return null;
		final int[] ingredient;
		final int[] production;
		if((ingredient = parseItemIdAndCount(a[0])) == null || (production = parseItemIdAndCount(a[1])) == null)
			return null;
		final MultiSellEntry entry = new MultiSellEntry();
		entry.addIngredient(new MultiSellIngredient(ingredient[0], ingredient[1]));
		entry.addProduct(new MultiSellIngredient(production[0], production[1]));
		return entry;
	}

	public void SeparateAndSend(final int listId, final Player player, final double taxRate)
	{
		MultiSellListContainer list = getList(listId);
		if(list == null)
		{
			if(listId != 9998 && listId != 9999)
			{
				player.sendMessage("Multisell not exist!");
				player.sendActionFailed();
				return;
			}
			list = new MultiSellListContainer();
			list.setListId(listId);
			list.setNpcId(Config.ALT_MAMMOTH_HARDCODE);
		}
		this.SeparateAndSend(list, player, taxRate);
	}

	public void SeparateAndSend(MultiSellListContainer list, final Player player, final double taxRate)
	{
		final boolean bbs = player.getLastNpcId() == -1;
		final NpcInstance npc = player.getLastNpc();
		if(!bbs && (!NpcInstance.canBypassCheck(player, npc) || !player.checkLastNpc()))
		{
			player.sendMessage(player.isLangRus() ? "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0431\u0435\u0437 \u043f\u0440\u043e\u0434\u0430\u0432\u0446\u0430!" : "Impossible without trader!");
			player.sendActionFailed();
			return;
		}
		if(bbs && !ArrayUtils.contains(Config.CB_MULTISELLS, list.getListId()))
		{
			player.sendMessage(player.isLangRus() ? "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0439 \u043c\u0443\u043b\u044c\u0442\u0438\u0441\u0435\u043b\u043b!" : "Not allowed meltisell!");
			player.sendActionFailed();
			return;
		}
		final int[] ids = list.getNpcId();
		if(ids.length > 0 && !ArrayUtils.contains(ids, player.getLastNpcId()))
		{
			player.sendMessage("Incorrect npc!");
			return;
		}
		list = generateMultiSell(list, player, taxRate);
		MultiSellListContainer temp = new MultiSellListContainer();
		int page = 1;
		temp.setListId(list.getListId());
		player.setMultisell(list);
		for(final MultiSellEntry e : list.getEntries())
		{
			if(temp.getEntries().size() == Config.MULTISELL_SIZE)
			{
				player.sendPacket(new MultiSellList(temp, page, 0));
				++page;
				temp = new MultiSellListContainer();
				temp.setListId(list.getListId());
			}
			temp.addEntry(e);
		}
		player.sendPacket(new MultiSellList(temp, page, 1));
		if(!bbs)
			player.turn(npc, 3000);
	}

	private MultiSellListContainer generateMultiSell(final MultiSellListContainer container, final Player player, final double taxRate)
	{
		final int listId = container.getListId();
		final MultiSellListContainer list = new MultiSellListContainer();
		list.setListId(listId);
		if(listId == 9999)
		{
			list.setShowAll(false);
			list.setKeepEnchant(true);
			list.setNoTax(true);
			list.setNpcId(Config.ALT_MAMMOTH_HARDCODE);
			final Inventory inv = player.getInventory();
			final List<ItemInstance> _items = new ArrayList<ItemInstance>();
			for(final ItemInstance itm : inv.getItems())
				if(itm.getTemplate().getAdditionalName().isEmpty() && !itm.getTemplate().isSa() && itm.canBeTraded(player) && !itm.isStackable() && itm.getTemplate().getType2() == 0 && itm.getTemplate().getItemGrade().ordinal() > 0 && itm.getTemplate().getItemGrade().ordinal() <= Config.ALT_MAMMOTH_EXCHANGE && !itm.isShadowItem() && !itm.isTemporalItem() && !itm.isEquipped() && itm.getTemplate().getCrystalCount() > 0 && itm.getTemplate().isTradeable() && (itm.getCustomFlags() & 0x2) != 0x2)
					_items.add(itm);
			for(final ItemInstance itm2 : _items)
				for(final WeaponTemplate i : ItemTable.getInstance().getAllWeapons())
					if(i.getAdditionalName().isEmpty() && !i.isSa() && !i.isShadowItem() && !i.isTemporal() && i.isTradeable() && i.getItemId() != itm2.getItemId() && i.getType2() == 0 && itm2.getTemplate().getItemGrade().ordinal() > 0 && i.getItemType() == WeaponTemplate.WeaponType.DUAL == (itm2.getTemplate().getItemType() == WeaponTemplate.WeaponType.DUAL) && itm2.getTemplate().getItemGrade().ordinal() == i.getItemGrade().ordinal() && itm2.getTemplate().getCrystalCount() == i.getCrystalCount())
					{
						final int entry = new int[] { itm2.getItemId(), i.getItemId(), itm2.getEnchantLevel() }.hashCode();
						final MultiSellEntry possibleEntry = new MultiSellEntry(entry, i.getItemId(), 1, itm2.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(itm2.getItemId(), 1, itm2.getEnchantLevel()));
						list.entries.add(possibleEntry);
					}
		}
		else if(listId == 9998)
		{
			list.setShowAll(false);
			list.setKeepEnchant(false);
			list.setNoTax(true);
			list.setNpcId(Config.ALT_MAMMOTH_HARDCODE);
			final Inventory inv = player.getInventory();
			final List<ItemInstance> _items = new ArrayList<ItemInstance>();
			for(final ItemInstance itm : inv.getItems())
				if(itm.getTemplate().getAdditionalName().isEmpty() && !itm.getTemplate().isSa() && !itm.getTemplate().isShadowItem() && !itm.isTemporalItem() && !itm.isStackable() && itm.getTemplate().getType2() == 0 && itm.getTemplate().getItemGrade().ordinal() > 0 && itm.getTemplate().getItemGrade().ordinal() <= Config.ALT_MAMMOTH_UPGRADE && itm.getTemplate().getCrystalCount() > 0 && !itm.isEquipped() && itm.getTemplate().isTradeable() && (itm.getCustomFlags() & 0x2) != 0x2)
					_items.add(itm);
			for(final ItemInstance itemtosell : _items)
				for(final WeaponTemplate itemtobuy : ItemTable.getInstance().getAllWeapons())
					if(itemtobuy.getAdditionalName().isEmpty() && !itemtobuy.isSa() && !itemtobuy.isShadowItem() && !itemtobuy.isTemporal() && itemtobuy.isTradeable() && itemtobuy.getType2() == 0 && itemtobuy.getItemType() == WeaponTemplate.WeaponType.DUAL == (itemtosell.getTemplate().getItemType() == WeaponTemplate.WeaponType.DUAL) && itemtobuy.getItemGrade().ordinal() >= itemtosell.getTemplate().getItemGrade().ordinal() && itemtobuy.getItemGrade().ordinal() <= Config.ALT_MAMMOTH_UPGRADE && itemtosell.getTemplate().getReferencePrice() < itemtobuy.getReferencePrice() && itemtosell.getReferencePrice() * 1.7 > itemtobuy.getReferencePrice())
					{
						final int entry = new int[] { itemtosell.getItemId(), itemtobuy.getItemId(), itemtosell.getEnchantLevel() }.hashCode();
						final MultiSellEntry possibleEntry = new MultiSellEntry(entry, itemtobuy.getItemId(), 1, 0);
						possibleEntry.addIngredient(new MultiSellIngredient(itemtosell.getItemId(), 1, itemtosell.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient(5575, (int) ((itemtobuy.getReferencePrice() - itemtosell.getReferencePrice()) * 1.2), 0));
						list.entries.add(possibleEntry);
					}
		}
		else
		{
			final boolean enchant = container.isKeepEnchant();
			final boolean extra = container.isExtra();
			final boolean notax = container.isNoTax();
			final boolean showall = container.isShowAll();
			final boolean nokey = container.isNoKey();
			final int[] ids = container.getNpcId();
			list.setShowAll(showall);
			list.setKeepEnchant(enchant);
			list.setExtra(extra);
			list.setNoTax(notax);
			list.setNoKey(nokey);
			list.setNpcId(ids);
			final ItemInstance[] items = player.getInventory().getItems();
			for(final MultiSellEntry origEntry : container.getEntries())
			{
				final MultiSellEntry ent = origEntry.clone();
				List<MultiSellIngredient> ingridients;
				if(!notax && taxRate > 0.0)
				{
					double tax = 0.0;
					int adena = 0;
					ingridients = new ArrayList<MultiSellIngredient>(ent.getIngredients().size() + 1);
					for(final MultiSellIngredient j : ent.getIngredients())
						if(j.getItemId() == 57)
						{
							adena += j.getItemCount();
							tax += j.getItemCount() * taxRate;
						}
						else
						{
							ingridients.add(j);
							if(j.getItemId() == -200)
								tax += j.getItemCount() / 120 * 1000 * taxRate * 100.0;
							if(j.getItemId() < 1)
								continue;
							final ItemTemplate item = ItemTable.getInstance().getTemplate(j.getItemId());
							if(!item.isStackable())
								continue;
							tax += item.getReferencePrice() * j.getItemCount() * taxRate;
						}
					adena = (int) Math.round(adena + tax);
					if(adena > 0)
						ingridients.add(new MultiSellIngredient(57, adena));
					ent.setTax((int) Math.round(tax));
					ent.getIngredients().clear();
					ent.getIngredients().addAll(ingridients);
				}
				else
					ingridients = ent.getIngredients();
				if(showall)
					list.entries.add(ent);
				else
				{
					final List<Integer> itms = new ArrayList<Integer>();
					for(final MultiSellIngredient ingredient : ingridients)
					{
						final ItemTemplate template = ingredient.getItemId() <= 0 ? null : ItemTable.getInstance().getTemplate(ingredient.getItemId());
						if(ingredient.getItemId() <= 0 || nokey || template.isEquipment())
							if(ingredient.getItemId() == -200)
							{
								if(itms.contains(ingredient.getItemId()) || player.getClan() == null || player.getClan().getReputationScore() < ingredient.getItemCount())
									continue;
								itms.add(ingredient.getItemId());
							}
							else if(ingredient.getItemId() == -100)
							{
								if(itms.contains(ingredient.getItemId()) || player.getPcBangPoints() < ingredient.getItemCount())
									continue;
								itms.add(ingredient.getItemId());
							}
							else
								for(final ItemInstance item2 : items)
									if(item2.getItemId() == ingredient.getItemId() && !item2.isEquipped() && (item2.getCustomFlags() & 0x2) != 0x2)
										if(!itms.contains(enchant ? ingredient.getItemId() + ingredient.getItemEnchant() * 100000L : ingredient.getItemId()))
											if(item2.getEnchantLevel() >= ingredient.getItemEnchant())
											{
												if(item2.isStackable() && item2.getCount() < ingredient.getItemCount())
													break;
												itms.add(enchant ? ingredient.getItemId() + ingredient.getItemEnchant() * 100000 : ingredient.getItemId());
												final MultiSellEntry possibleEntry2 = new MultiSellEntry(enchant ? ent.getEntryId() + item2.getEnchantLevel() * 100000 : ent.getEntryId());
												for(final MultiSellIngredient p : ent.getProduction())
												{
													if(enchant)
														p.setItemEnchant(item2.getEnchantLevel());
													possibleEntry2.addProduct(p);
												}
												for(final MultiSellIngredient ig : ingridients)
												{
													if(enchant && ig.getItemId() > 0 && ItemTable.getInstance().getTemplate(ig.getItemId()).canBeEnchanted())
														ig.setItemEnchant(item2.getEnchantLevel());
													possibleEntry2.addIngredient(ig);
												}
												list.entries.add(possibleEntry2);
												break;
											}
					}
				}
			}
		}
		return list;
	}

	public static class MultiSellListContainer
	{
		private int _listId;
		private boolean _showall;
		private boolean keep_enchanted;
		private boolean extra;
		private boolean is_dutyfree;
		private boolean nokey;
		private int[] npc_id;
		private List<MultiSellEntry> entries;

		public MultiSellListContainer()
		{
			_showall = true;
			keep_enchanted = false;
			extra = false;
			is_dutyfree = false;
			nokey = false;
			npc_id = new int[0];
			entries = new ArrayList<MultiSellEntry>();
		}

		public void setListId(final int listId)
		{
			_listId = listId;
		}

		public int getListId()
		{
			return _listId;
		}

		public void setShowAll(final boolean bool)
		{
			_showall = bool;
		}

		public boolean isShowAll()
		{
			return _showall;
		}

		public void setNoTax(final boolean bool)
		{
			is_dutyfree = bool;
		}

		public boolean isNoTax()
		{
			return is_dutyfree;
		}

		public void setNoKey(final boolean bool)
		{
			nokey = bool;
		}

		public boolean isNoKey()
		{
			return nokey;
		}

		public void setKeepEnchant(final boolean bool)
		{
			keep_enchanted = bool;
		}

		public boolean isKeepEnchant()
		{
			return keep_enchanted;
		}

		public void setExtra(final boolean bool)
		{
			extra = bool;
		}

		public boolean isExtra()
		{
			return extra;
		}

		public void setNpcId(final int[] val)
		{
			npc_id = val;
		}

		public int[] getNpcId()
		{
			return npc_id;
		}

		public void addEntry(final MultiSellEntry e)
		{
			entries.add(e);
		}

		public List<MultiSellEntry> getEntries()
		{
			return entries;
		}

		public boolean isEmpty()
		{
			return entries.isEmpty();
		}
	}
}
