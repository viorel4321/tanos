package l2s.gameserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2s.commons.util.Rnd;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.XMLDocumentFactory;
import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.model.ManufactureItem;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Recipe;
import l2s.gameserver.model.RecipeList;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.RecipeBookItemList;
import l2s.gameserver.network.l2.s2c.RecipeItemMakeInfo;
import l2s.gameserver.network.l2.s2c.RecipeShopItemInfo;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.utils.Stat;

public class RecipeController
{
	protected static Logger _log;
	private static RecipeController _instance;
	private HashMap<Integer, RecipeList> _listByRecipeId;
	private HashMap<Integer, RecipeList> _listByRecipeItem;

	public static RecipeController getInstance()
	{
		if(RecipeController._instance == null)
			RecipeController._instance = new RecipeController();
		return RecipeController._instance;
	}

	public RecipeController()
	{
		try
		{
			_listByRecipeId = new HashMap<Integer, RecipeList>();
			_listByRecipeItem = new HashMap<Integer, RecipeList>();
			final File file = new File(Config.DATAPACK_ROOT, "data/pts/recipes.xml");
			final Document doc = XMLDocumentFactory.getInstance().loadDocument(file);
			final List<Recipe> recipePartList = new ArrayList<Recipe>();
			final Node n = doc.getFirstChild();
			for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				if("item".equalsIgnoreCase(d.getNodeName()))
				{
					recipePartList.clear();
					final NamedNodeMap attrs = d.getAttributes();
					Node att = attrs.getNamedItem("id");
					if(att == null)
						RecipeController._log.warn("Missing id for recipe item, skipping");
					else
					{
						final int id = Integer.parseInt(att.getNodeValue());
						att = attrs.getNamedItem("name");
						if(att == null)
							RecipeController._log.warn("Missing name for recipe item id: " + id + ", skipping");
						else
						{
							final String recipeName = att.getNodeValue();
							int recipeId = -1;
							int level = -1;
							boolean isDwarvenRecipe = true;
							int mpCost = -1;
							int successRate = -1;
							int prodId = -1;
							int count = -1;
							for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
								if("recipe".equalsIgnoreCase(c.getNodeName()))
								{
									recipeId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
									level = Integer.parseInt(c.getAttributes().getNamedItem("level").getNodeValue());
									isDwarvenRecipe = c.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("dwarven");
								}
								else if("mpCost".equalsIgnoreCase(c.getNodeName()))
									mpCost = Integer.parseInt(c.getTextContent());
								else if("successRate".equalsIgnoreCase(c.getNodeName()))
									successRate = Integer.parseInt(c.getTextContent());
								else if("ingredient".equalsIgnoreCase(c.getNodeName()))
								{
									final int ingId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
									final int ingCount = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
									final Recipe rp = new Recipe(ingId, ingCount);
									recipePartList.add(rp);
								}
								else if("production".equalsIgnoreCase(c.getNodeName()))
								{
									prodId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
									count = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
								}
							final RecipeList recipeList = new RecipeList(id, level, recipeId, recipeName, successRate, mpCost, prodId, count, isDwarvenRecipe);
							for(final Recipe recipePart : recipePartList)
								recipeList.addRecipe(recipePart);
							_listByRecipeId.put(id, recipeList);
							_listByRecipeItem.put(recipeId, recipeList);
						}
					}
				}
			RecipeController._log.info("RecipeController: Loaded " + _listByRecipeId.size() + " Recipes.");
		}
		catch(Exception e)
		{
			RecipeController._log.error("RecipeController: Failed loading recipe list!", e);
		}
	}

	public Collection<RecipeList> getRecipes()
	{
		return _listByRecipeId.values();
	}

	public RecipeList getRecipeList(final int listId)
	{
		return _listByRecipeId.get(listId);
	}

	public RecipeList getRecipeByItemId(final int itemId)
	{
		return _listByRecipeItem.get(itemId);
	}

	public void requestBookOpen(final Player player, final boolean isDwarvenCraft)
	{
		final RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, (int) player.getCurrentMp());
		if(isDwarvenCraft)
			response.setRecipes(player.getDwarvenRecipeBook());
		else
			response.setRecipes(player.getCommonRecipeBook());
		player.sendPacket(response);
	}

	public void requestMakeItem(final Player player, final int recipeListId)
	{
		final RecipeList recipeList = getRecipeList(recipeListId);
		if(recipeList == null || recipeList.getRecipes().length == 0)
		{
			player.sendPacket(Msg.THE_RECIPE_IS_INCORRECT);
			return;
		}
		synchronized (player)
		{
			if(player.getCurrentMp() < recipeList.getMpCost())
			{
				player.sendPacket(Msg.NOT_ENOUGH_MP);
				player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(), player, 0));
				return;
			}
			if(!player.findRecipe(recipeListId))
			{
				player.sendPacket(Msg.PLEASE_REGISTER_A_RECIPE);
				player.sendActionFailed();
				return;
			}
			if(player.isAlikeDead())
			{
				player.sendMessage("You can't do it now.");
				player.sendActionFailed();
				return;
			}
		}
		synchronized (player.getInventory())
		{
			final Recipe[] recipes = recipeList.getRecipes();
			final Inventory inventory = player.getInventory();
			for(final Recipe recipe : recipes)
				if(recipe.getQuantity() != 0)
					if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemTable.getInstance().getTemplate(recipe.getItemId()).getItemType() == EtcItemTemplate.EtcItemType.RECIPE)
					{
						final RecipeList rp = getInstance().getRecipeByItemId(recipe.getItemId());
						if(!player.findRecipe(rp))
						{
							player.sendPacket(Msg.NOT_ENOUGH_MATERIALS);
							player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(), player, 0));
							return;
						}
					}
					else
					{
						final ItemInstance invItem = inventory.getItemByItemId(recipe.getItemId());
						if(invItem == null || recipe.getQuantity() > invItem.getIntegerLimitedCount())
						{
							player.sendPacket(Msg.NOT_ENOUGH_MATERIALS);
							player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(), player, 0));
							return;
						}
					}
			player.reduceCurrentMp(recipeList.getMpCost(), null);
			for(final Recipe recipe : recipes)
				if(recipe.getQuantity() != 0)
				{
					final ItemInstance invItem = inventory.getItemByItemId(recipe.getItemId());
					if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemTable.getInstance().getTemplate(recipe.getItemId()).getItemType() == EtcItemTemplate.EtcItemType.RECIPE)
						player.unregisterRecipe(getInstance().getRecipeByItemId(recipe.getItemId()).getId());
					else
						inventory.destroyItem(invItem, recipe.getQuantity(), false);
				}
		}
		final ItemInstance createdItem = ItemTable.getInstance().createItem(recipeList.getItemId());
		createdItem.setCount(recipeList.getCount());
		int success = 0;
		if(!Rnd.chance(recipeList.getSuccessRate()))
			player.sendPacket(new SystemMessage(960).addItemName(Integer.valueOf(recipeList.getItemId())));
		else
		{
			if(Config.CRAFT_COUNTER)
				player.incrementCraftCounter(createdItem.getItemId(), new Integer(recipeList.getCount()));
			player.getInventory().addItem(createdItem);
			player.sendPacket(SystemMessage.obtainItems(createdItem));
			++success;
		}
		player.sendPacket(new StatusUpdate(player.getObjectId()).addAttribute(14, player.getCurrentLoad()).addAttribute(11, (int) player.getCurrentMp()));
		player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(), player, success));
	}

	public void requestManufactureItem(final Player player, final Player employer, final int recipeListId)
	{
		final RecipeList recipeList = getRecipeList(recipeListId);
		if(recipeList == null)
			return;
		int success = 0;
		player.sendMessage(new CustomMessage("l2s.gameserver.RecipeController.GotOrder").addString(recipeList.getRecipeName()));
		if(recipeList.getRecipes().length == 0)
		{
			player.sendMessage(new CustomMessage("l2s.gameserver.RecipeController.NoRecipe").addString(recipeList.getRecipeName()));
			employer.sendMessage(new CustomMessage("l2s.gameserver.RecipeController.NoRecipe").addString(recipeList.getRecipeName()));
			return;
		}
		synchronized (player)
		{
			if(player.getCurrentMp() < recipeList.getMpCost())
			{
				player.sendPacket(Msg.NOT_ENOUGH_MP);
				employer.sendPacket(Msg.NOT_ENOUGH_MP);
				employer.sendPacket(new RecipeShopItemInfo(player.getObjectId(), recipeListId, success, employer));
				return;
			}
			if(!player.findRecipe(recipeListId))
			{
				player.sendPacket(Msg.PLEASE_REGISTER_A_RECIPE);
				player.sendActionFailed();
				return;
			}
			if(player.isAlikeDead())
			{
				player.sendMessage("You can't do it now.");
				player.sendActionFailed();
				return;
			}
		}
		int price = 0;
		for(final ManufactureItem temp : player.getCreateList().getList())
			if(temp.getRecipeId() == recipeList.getId())
			{
				price = temp.getCost();
				break;
			}
		if(employer.getAdena() < price)
		{
			employer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			employer.sendPacket(new RecipeShopItemInfo(player.getObjectId(), recipeListId, success, employer));
			return;
		}
		synchronized (employer.getInventory())
		{
			final Recipe[] recipes = recipeList.getRecipes();
			final Inventory inventory = employer.getInventory();
			for(final Recipe recipe : recipes)
				if(recipe.getQuantity() != 0)
				{
					final ItemInstance invItem = inventory.getItemByItemId(recipe.getItemId());
					if(invItem == null || recipe.getQuantity() > invItem.getIntegerLimitedCount())
					{
						employer.sendPacket(Msg.NOT_ENOUGH_MATERIALS);
						employer.sendPacket(new RecipeShopItemInfo(player.getObjectId(), recipeListId, success, employer));
						return;
					}
				}
			player.reduceCurrentMp(recipeList.getMpCost(), null);
			for(final Recipe recipe : recipes)
				if(recipe.getQuantity() != 0)
				{
					final ItemInstance invItem = inventory.getItemByItemId(recipe.getItemId());
					inventory.destroyItem(invItem, recipe.getQuantity(), false);
				}
		}
		if(price > 0)
		{
			employer.reduceAdena(price, false);
			player.addAdena(price);
			int tax = (int) (price * Config.SERVICES_TRADE_TAX / 100.0f);
			if(ZoneManager.getInstance().checkIfInZone(Zone.ZoneType.offshore, player.getX(), player.getY()))
				tax = (int) (price * Config.SERVICES_OFFSHORE_TRADE_TAX / 100.0f);
			if(Config.SERVICES_TRADE_TAX_ONLY_OFFLINE && !player.isInOfflineMode())
				tax = 0;
			if(tax > 0)
			{
				player.reduceAdena(tax, false);
				Stat.addTax(tax);
				player.sendMessage(new CustomMessage("trade.HavePaidTax").addNumber(tax));
			}
		}
		final ItemInstance createdItem = ItemTable.getInstance().createItem(recipeList.getItemId());
		createdItem.setCount(recipeList.getCount());
		SystemMessage msgtoemployer;
		SystemMessage msgtomaster;
		if(!Rnd.chance(recipeList.getSuccessRate()))
		{
			msgtoemployer = new SystemMessage(1150);
			msgtoemployer.addString(player.getName());
			msgtoemployer.addItemName(Integer.valueOf(createdItem.getItemId()));
			msgtoemployer.addNumber(Integer.valueOf(price));
			msgtomaster = new SystemMessage(1149);
			msgtomaster.addString(employer.getName());
			msgtomaster.addItemName(Integer.valueOf(createdItem.getItemId()));
			msgtomaster.addNumber(Integer.valueOf(price));
		}
		else
		{
			if(Config.CRAFT_COUNTER)
				player.incrementCraftCounter(createdItem.getItemId(), new Integer(recipeList.getCount()));
			employer.getInventory().addItem(createdItem);
			if(recipeList.getCount() > 1)
			{
				msgtoemployer = new SystemMessage(1148);
				msgtoemployer.addString(player.getName());
				msgtoemployer.addNumber(Integer.valueOf(recipeList.getCount()));
				msgtoemployer.addItemName(Integer.valueOf(createdItem.getItemId()));
				msgtoemployer.addNumber(Integer.valueOf(price));
				msgtomaster = new SystemMessage(1152);
				msgtomaster.addString(employer.getName());
				msgtomaster.addNumber(Integer.valueOf(recipeList.getCount()));
				msgtomaster.addItemName(Integer.valueOf(createdItem.getItemId()));
				msgtomaster.addNumber(Integer.valueOf(price));
			}
			else
			{
				msgtoemployer = new SystemMessage(1146);
				msgtoemployer.addString(player.getName());
				msgtoemployer.addItemName(Integer.valueOf(createdItem.getItemId()));
				msgtoemployer.addNumber(Integer.valueOf(price));
				msgtomaster = new SystemMessage(1151);
				msgtomaster.addString(employer.getName());
				msgtomaster.addItemName(Integer.valueOf(createdItem.getItemId()));
				msgtomaster.addNumber(Integer.valueOf(price));
			}
			++success;
		}
		player.sendPacket(new StatusUpdate(player.getObjectId()).addAttribute(14, player.getCurrentLoad()).addAttribute(11, (int) player.getCurrentMp()));
		player.sendPacket(msgtomaster);
		employer.sendPacket(msgtoemployer);
		employer.sendChanges();
		employer.sendPacket(new StatusUpdate(employer.getObjectId()).addAttribute(14, employer.getCurrentLoad()));
		employer.sendPacket(new RecipeShopItemInfo(player.getObjectId(), recipeListId, success, employer));
	}

	static
	{
		RecipeController._log = LoggerFactory.getLogger(RecipeController.class);
	}
}
