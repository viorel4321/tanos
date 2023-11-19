package l2s.gameserver.tables;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.DocumentItem;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.Item;
import l2s.gameserver.templates.item.ArmorTemplate;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;

public class ItemTable
{
	private final Logger _log;
	private static final HashMap<String, Byte> _materials;
	private static final HashMap<String, Byte> _crystalTypes;
	private static final HashMap<String, WeaponTemplate.WeaponType> _weaponTypes;
	private static final HashMap<String, ArmorTemplate.ArmorType> _armorTypes;
	private static final HashMap<String, Integer> _slots;
	private ItemTemplate[] _allTemplates;
	private final HashMap<Integer, EtcItemTemplate> _etcItems;
	private final HashMap<Integer, ArmorTemplate> _armors;
	private final HashMap<Integer, WeaponTemplate> _weapons;
	private final HashMap<Integer, Item> itemData;
	private final HashMap<Integer, Item> weaponData;
	private final HashMap<Integer, Item> armorData;
	private boolean _initialized;
	private static ItemTable _instance;
	private static final String[] SQL_ITEM_SELECTS;

	public static ItemTable getInstance()
	{
		if(ItemTable._instance == null)
			ItemTable._instance = new ItemTable();
		return ItemTable._instance;
	}

	public Item newItem()
	{
		return new Item();
	}

	private ItemTable()
	{
		_log = LoggerFactory.getLogger(ItemTable.class);
		_etcItems = new HashMap<Integer, EtcItemTemplate>();
		_armors = new HashMap<Integer, ArmorTemplate>();
		_weapons = new HashMap<Integer, WeaponTemplate>();
		itemData = new HashMap<Integer, Item>();
		weaponData = new HashMap<Integer, Item>();
		armorData = new HashMap<Integer, Item>();
		_initialized = true;
		Connection con = null;
		Statement st = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(final String selectQuery : ItemTable.SQL_ITEM_SELECTS)
			{
				statement = con.prepareStatement(selectQuery);
				rset = statement.executeQuery();
				while(rset.next())
				{
					if(selectQuery.endsWith("etcitem"))
					{
						final Item newItem = readItem(rset);
						itemData.put(newItem.id, newItem);
					}
					else if(selectQuery.endsWith("armor"))
					{
						final Item newItem = readArmor(rset);
						armorData.put(newItem.id, newItem);
					}
					else
					{
						if(!selectQuery.endsWith("weapon"))
							continue;
						final Item newItem = readWeapon(rset);
						weaponData.put(newItem.id, newItem);
					}
				}
				DbUtils.closeQuietly(statement, rset);
			}
		}
		catch(Exception e)
		{
			_log.error("data error on item: ", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		for(final Map.Entry<Integer, Item> e2 : armorData.entrySet())
			_armors.put(e2.getKey(), new ArmorTemplate((ArmorTemplate.ArmorType) e2.getValue().type, e2.getValue().set));
		_log.info("ItemTable: Loaded " + _armors.size() + " Armors.");
		for(final Map.Entry<Integer, Item> e2 : itemData.entrySet())
			_etcItems.put(e2.getKey(), new EtcItemTemplate((EtcItemTemplate.EtcItemType) e2.getValue().type, e2.getValue().set));
		_log.info("ItemTable: Loaded " + _etcItems.size() + " Items.");
		for(final Map.Entry<Integer, Item> e2 : weaponData.entrySet())
			_weapons.put(e2.getKey(), new WeaponTemplate((WeaponTemplate.WeaponType) e2.getValue().type, e2.getValue().set));
		_log.info("ItemTable: Loaded " + _weapons.size() + " Weapons.");
		buildFastLookupTable();
		new Thread(new Runnable(){
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(1000L);
				}
				catch(InterruptedException ex)
				{}
				for(final File f : new File(Config.DATAPACK_ROOT, "data/stats/items/").listFiles())
					if(!f.isDirectory())
						new DocumentItem(f);
			}
		}).start();
	}

	private Item readWeapon(final ResultSet rset) throws SQLException
	{
		final Item item = new Item();
		item.set = new StatsSet();
		item.id = rset.getInt("item_id");
		item.type = ItemTable._weaponTypes.get(rset.getString("weaponType"));
		if(item.type == null)
			System.out.println("Error in weapons table: unknown weapon type " + rset.getString("weaponType") + " for item " + item.id);
		item.name = rset.getString("name");
		item.set.set("class", "WEAPON");
		item.set.set("item_id", item.id);
		item.set.set("name", item.name);
		item.set.set("additional_name", rset.getString("additional_name"));
		if(item.type == WeaponTemplate.WeaponType.NONE)
		{
			item.set.set("type1", 1);
			item.set.set("type2", 1);
		}
		else
		{
			item.set.set("type1", 0);
			item.set.set("type2", 0);
		}
		item.set.set("bodypart", ItemTable._slots.get(rset.getString("bodypart")));
		item.set.set("material", ItemTable._materials.get(rset.getString("material")));
		item.set.set("crystal_type", ItemTable._crystalTypes.get(rset.getString("crystal_type")));
		item.set.set("crystallizable", (boolean) Boolean.valueOf(rset.getString("crystallizable")));
		item.set.set("weight", rset.getInt("weight"));
		item.set.set("soulshots", rset.getInt("soulshots"));
		item.set.set("spiritshots", rset.getInt("spiritshots"));
		item.set.set("p_dam", rset.getInt("p_dam"));
		item.set.set("rnd_dam", rset.getInt("rnd_dam"));
		item.set.set("critical", rset.getInt("critical"));
		item.set.set("hit_modify", rset.getDouble("hit_modify"));
		item.set.set("avoid_modify", rset.getInt("avoid_modify"));
		item.set.set("shield_def", rset.getInt("shield_def"));
		item.set.set("shield_def_rate", rset.getInt("shield_def_rate"));
		item.set.set("atk_speed", rset.getInt("atk_speed"));
		item.set.set("mp_consume", rset.getInt("mp_consume"));
		item.set.set("m_dam", rset.getInt("m_dam"));
		item.set.set("durability", rset.getInt("durability"));
		item.set.set("price", rset.getInt("price"));
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("tradeable", rset.getInt("tradeable") > 0);
		item.set.set("dropable", rset.getInt("dropable") > 0);
		item.set.set("destroyable", rset.getInt("destroyable") > 0);
		item.set.set("temporal", rset.getInt("temporal") > 0);
		item.set.set("skill_id", rset.getString("skill_id"));
		item.set.set("skill_level", rset.getString("skill_level"));
		item.set.set("enchant4_skill_id", rset.getInt("enchant4_skill_id"));
		item.set.set("enchant4_skill_lvl", rset.getInt("enchant4_skill_lvl"));
		item.set.set("variation_group_id", rset.getInt("variation_group_id"));
		item.set.set("is_magic_weapon", rset.getInt("is_magic_weapon") > 0);
		item.set.set("icon", rset.getString("icon"));
		if(item.type == WeaponTemplate.WeaponType.PET)
		{
			item.set.set("type1", 0);
			if(item.set.getInteger("bodypart") == -100)
				item.set.set("type2", 6);
			else if(item.set.getInteger("bodypart") == -101)
				item.set.set("type2", 7);
			else
				item.set.set("type2", 8);
			item.set.set("bodypart", 128);
		}
		return item;
	}

	private Item readArmor(final ResultSet rset) throws SQLException
	{
		final Item item = new Item();
		item.set = new StatsSet();
		item.type = ItemTable._armorTypes.get(rset.getString("armor_type"));
		item.id = rset.getInt("item_id");
		item.name = rset.getString("name");
		item.set.set("item_id", item.id);
		item.set.set("name", item.name);
		final int bodypart = ItemTable._slots.get(rset.getString("bodypart"));
		item.set.set("bodypart", bodypart);
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		if(bodypart == ItemTemplate.SLOT_NECK || (bodypart & ItemTemplate.SLOT_L_EAR) != 0 || (bodypart & ItemTemplate.SLOT_L_FINGER) != 0)
		{
			item.set.set("type1", ItemTemplate.TYPE1_WEAPON_RING_EARRING_NECKLACE);
			item.set.set("type2", ItemTemplate.TYPE2_ACCESSORY);
			item.set.set("class", "JEWELRY");
		}
		else if(bodypart == ItemTemplate.SLOT_HAIR || bodypart == ItemTemplate.SLOT_DHAIR || bodypart == ItemTemplate.SLOT_HAIRALL)
		{
			item.set.set("type1", ItemTemplate.TYPE1_OTHER);
			item.set.set("type2", ItemTemplate.TYPE2_OTHER);
			item.set.set("class", "ACCESSORY");
		}
		else
		{
			item.set.set("type1", ItemTemplate.TYPE1_SHIELD_ARMOR);
			item.set.set("type2", ItemTemplate.TYPE2_SHIELD_ARMOR);
			item.set.set("class", "ARMOR");
		}
		item.set.set("weight", rset.getInt("weight"));
		item.set.set("material", ItemTable._materials.get(rset.getString("material")));
		item.set.set("crystal_type", ItemTable._crystalTypes.get(rset.getString("crystal_type")));
		item.set.set("avoid_modify", rset.getInt("avoid_modify"));
		item.set.set("durability", rset.getInt("durability"));
		item.set.set("p_def", rset.getInt("p_def"));
		item.set.set("m_def", rset.getInt("m_def"));
		item.set.set("mp_bonus", rset.getInt("mp_bonus"));
		item.set.set("price", rset.getInt("price"));
		item.set.set("tradeable", rset.getInt("tradeable") > 0);
		item.set.set("dropable", rset.getInt("dropable") > 0);
		item.set.set("destroyable", rset.getInt("destroyable") > 0);
		item.set.set("temporal", rset.getInt("temporal") > 0);
		item.set.set("skill_id", rset.getString("skill_id"));
		item.set.set("skill_level", rset.getString("skill_level"));
		item.set.set("icon", rset.getString("icon"));
		if(item.type == ArmorTemplate.ArmorType.PET)
		{
			item.set.set("type1", 1);
			if(item.set.getInteger("bodypart") == -100)
				item.set.set("type2", 6);
			else if(item.set.getInteger("bodypart") == -101)
				item.set.set("type2", 7);
			else
				item.set.set("type2", 8);
			item.set.set("bodypart", 1024);
		}
		return item;
	}

	private Item readItem(final ResultSet rset) throws SQLException
	{
		final Item item = new Item();
		item.set = new StatsSet();
		item.id = rset.getInt("item_id");
		item.set.set("item_id", item.id);
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
		item.set.set("type1", 4);
		item.set.set("type2", 5);
		item.set.set("bodypart", 0);
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("temporal", rset.getInt("temporal") > 0);
		item.set.set("icon", rset.getString("icon"));
		item.set.set("class", rset.getString("class"));
		final String itemType = rset.getString("item_type");
		if(itemType.equals("none"))
			item.type = EtcItemTemplate.EtcItemType.OTHER;
		else if(itemType.equals("mticket"))
			item.type = EtcItemTemplate.EtcItemType.SCROLL;
		else if(itemType.equals("material"))
			item.type = EtcItemTemplate.EtcItemType.MATERIAL;
		else if(itemType.equals("pet_collar"))
			item.type = EtcItemTemplate.EtcItemType.PET_COLLAR;
		else if(itemType.equals("potion"))
			item.type = EtcItemTemplate.EtcItemType.POTION;
		else if(itemType.equals("recipe"))
			item.type = EtcItemTemplate.EtcItemType.RECIPE;
		else if(itemType.equals("scroll"))
			item.type = EtcItemTemplate.EtcItemType.SCROLL;
		else if(itemType.equals("seed"))
			item.type = EtcItemTemplate.EtcItemType.SEED;
		else if(itemType.equals("spellbook"))
			item.type = EtcItemTemplate.EtcItemType.SPELLBOOK;
		else if(itemType.equals("shot"))
			item.type = EtcItemTemplate.EtcItemType.SHOT;
		else if(itemType.equals("arrow"))
		{
			item.type = EtcItemTemplate.EtcItemType.ARROW;
			item.set.set("bodypart", 256);
		}
		else if(itemType.equals("bait"))
		{
			item.type = EtcItemTemplate.EtcItemType.BAIT;
			item.set.set("bodypart", 256);
		}
		else if(itemType.equals("quest"))
		{
			item.type = EtcItemTemplate.EtcItemType.QUEST;
			item.set.set("type2", 3);
		}
		else
		{
			_log.warn("unknown etcitem type:" + itemType);
			item.type = EtcItemTemplate.EtcItemType.OTHER;
		}
		final String consume = rset.getString("consume_type");
		if(consume.equals("asset"))
		{
			item.type = EtcItemTemplate.EtcItemType.MONEY;
			item.set.set("stackable", true);
			item.set.set("type2", 4);
		}
		else if(consume.equals("stackable"))
			item.set.set("stackable", true);
		else
			item.set.set("stackable", false);
		final int material = ItemTable._materials.get(rset.getString("material"));
		item.set.set("material", material);
		final int crystal = ItemTable._crystalTypes.get(rset.getString("crystal_type"));
		item.set.set("crystal_type", crystal);
		final int weight = rset.getInt("weight");
		item.set.set("weight", weight);
		item.name = rset.getString("name");
		item.set.set("name", item.name);
		item.set.set("durability", rset.getInt("durability"));
		item.set.set("price", rset.getInt("price"));
		item.set.set("skill_id", rset.getString("skill_id"));
		item.set.set("skill_level", rset.getString("skill_level"));
		item.set.set("tradeable", rset.getInt("tradeable") > 0);
		item.set.set("dropable", rset.getInt("dropable") > 0);
		item.set.set("destroyable", rset.getInt("destroyable") > 0);
		return item;
	}

	private void buildFastLookupTable()
	{
		int highestId = 0;
		for(final Integer id : _armors.keySet())
			if(id > highestId)
				highestId = id;
		for(final Integer id : _weapons.keySet())
			if(id > highestId)
				highestId = id;
		for(final Integer id : _etcItems.keySet())
			if(id > highestId)
				highestId = id;
		_allTemplates = new ItemTemplate[highestId + 1];
		for(final Integer id : _armors.keySet())
		{
			final ArmorTemplate item = _armors.get(id);
			assert _allTemplates[id] == null;
			_allTemplates[id] = item;
		}
		for(final Integer id : _weapons.keySet())
		{
			final WeaponTemplate item2 = _weapons.get(id);
			assert _allTemplates[id] == null;
			_allTemplates[id] = item2;
		}
		for(final Integer id : _etcItems.keySet())
		{
			final EtcItemTemplate item3 = _etcItems.get(id);
			assert _allTemplates[id] == null;
			_allTemplates[id] = item3;
		}
	}

	public ItemInstance createItem(final int itemId)
	{
		return new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
	}

	public ItemTemplate getTemplate(final int id)
	{
		if(id >= _allTemplates.length || id < 0)
		{
			_log.warn("ItemTable[604]: Not defined item_id=" + id + "; out of range");
			Thread.dumpStack();
			return null;
		}
		return _allTemplates[id];
	}

	public ItemTemplate[] getAllTemplates()
	{
		return _allTemplates;
	}

	public Collection<WeaponTemplate> getAllWeapons()
	{
		return _weapons.values();
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	static
	{
		_materials = new HashMap<String, Byte>();
		_crystalTypes = new HashMap<String, Byte>();
		_weaponTypes = new HashMap<String, WeaponTemplate.WeaponType>();
		_armorTypes = new HashMap<String, ArmorTemplate.ArmorType>();
		_slots = new HashMap<String, Integer>();
		ItemTable._materials.put("paper", (byte) 8);
		ItemTable._materials.put("wood", (byte) 9);
		ItemTable._materials.put("liquid", (byte) 18);
		ItemTable._materials.put("cloth", (byte) 10);
		ItemTable._materials.put("leather", (byte) 11);
		ItemTable._materials.put("horn", (byte) 13);
		ItemTable._materials.put("bone", (byte) 12);
		ItemTable._materials.put("bronze", (byte) 3);
		ItemTable._materials.put("fine_steel", (byte) 1);
		ItemTable._materials.put("cotton", (byte) 1);
		ItemTable._materials.put("mithril", (byte) 6);
		ItemTable._materials.put("silver", (byte) 4);
		ItemTable._materials.put("gold", (byte) 5);
		ItemTable._materials.put("adamantaite", (byte) 15);
		ItemTable._materials.put("steel", (byte) 0);
		ItemTable._materials.put("oriharukon", (byte) 7);
		ItemTable._materials.put("blood_steel", (byte) 2);
		ItemTable._materials.put("crystal", (byte) 17);
		ItemTable._materials.put("damascus", (byte) 14);
		ItemTable._materials.put("chrysolite", (byte) 16);
		ItemTable._materials.put("scale_of_dragon", (byte) 19);
		ItemTable._materials.put("dyestuff", (byte) 20);
		ItemTable._materials.put("cobweb", (byte) 21);
		ItemTable._materials.put("seed", (byte) 22);
		ItemTable._materials.put("fish", (byte) 23);
		ItemTable._crystalTypes.put("s", (byte) 5);
		ItemTable._crystalTypes.put("a", (byte) 4);
		ItemTable._crystalTypes.put("b", (byte) 3);
		ItemTable._crystalTypes.put("c", (byte) 2);
		ItemTable._crystalTypes.put("d", (byte) 1);
		ItemTable._crystalTypes.put("none", (byte) 0);
		ItemTable._weaponTypes.put("blunt", WeaponTemplate.WeaponType.BLUNT);
		ItemTable._weaponTypes.put("bigblunt", WeaponTemplate.WeaponType.BIGBLUNT);
		ItemTable._weaponTypes.put("bow", WeaponTemplate.WeaponType.BOW);
		ItemTable._weaponTypes.put("dagger", WeaponTemplate.WeaponType.DAGGER);
		ItemTable._weaponTypes.put("dual", WeaponTemplate.WeaponType.DUAL);
		ItemTable._weaponTypes.put("dualfist", WeaponTemplate.WeaponType.DUALFIST);
		ItemTable._weaponTypes.put("etc", WeaponTemplate.WeaponType.ETC);
		ItemTable._weaponTypes.put("fist", WeaponTemplate.WeaponType.FIST);
		ItemTable._weaponTypes.put("none", WeaponTemplate.WeaponType.NONE);
		ItemTable._weaponTypes.put("pole", WeaponTemplate.WeaponType.POLE);
		ItemTable._weaponTypes.put("sword", WeaponTemplate.WeaponType.SWORD);
		ItemTable._weaponTypes.put("bigsword", WeaponTemplate.WeaponType.BIGSWORD);
		ItemTable._weaponTypes.put("pet", WeaponTemplate.WeaponType.PET);
		ItemTable._weaponTypes.put("rod", WeaponTemplate.WeaponType.ROD);
		ItemTable._armorTypes.put("none", ArmorTemplate.ArmorType.NONE);
		ItemTable._armorTypes.put("light", ArmorTemplate.ArmorType.LIGHT);
		ItemTable._armorTypes.put("heavy", ArmorTemplate.ArmorType.HEAVY);
		ItemTable._armorTypes.put("magic", ArmorTemplate.ArmorType.MAGIC);
		ItemTable._armorTypes.put("pet", ArmorTemplate.ArmorType.PET);
		ItemTable._slots.put("chest", 1024);
		ItemTable._slots.put("fullarmor", 32768);
		ItemTable._slots.put("head", 64);
		ItemTable._slots.put("hair", 65536);
		ItemTable._slots.put("face", 262144);
		ItemTable._slots.put("dhair", 524288);
		ItemTable._slots.put("underwear", 1);
		ItemTable._slots.put("back", 8192);
		ItemTable._slots.put("neck", 8);
		ItemTable._slots.put("legs", 2048);
		ItemTable._slots.put("feet", 4096);
		ItemTable._slots.put("gloves", 512);
		ItemTable._slots.put("chest,legs", 3072);
		ItemTable._slots.put("rhand", 128);
		ItemTable._slots.put("lhand", 256);
		ItemTable._slots.put("lrhand", 16384);
		ItemTable._slots.put("rear,lear", 6);
		ItemTable._slots.put("rfinger,lfinger", 48);
		ItemTable._slots.put("none", 0);
		ItemTable._slots.put("wolf", -100);
		ItemTable._slots.put("hatchling", -101);
		ItemTable._slots.put("strider", -102);
		ItemTable._slots.put("formalwear", 131072);
		SQL_ITEM_SELECTS = new String[] {
				"SELECT item_id, name, class, icon, crystallizable, item_type, weight, consume_type, material, crystal_type, durability, price, crystal_count, sellable, skill_id, skill_level, tradeable, dropable, destroyable, temporal FROM etcitem",
				"SELECT item_id, name, `additional_name`, icon, bodypart, crystallizable, armor_type, weight, material, crystal_type, avoid_modify, durability, p_def, m_def, mp_bonus, price, crystal_count, sellable, tradeable, dropable, destroyable, skill_id, skill_level, temporal FROM armor",
				"SELECT item_id, name, `additional_name`, icon, bodypart, crystallizable, weight, soulshots, spiritshots, material, crystal_type, p_dam, rnd_dam, weaponType, critical, hit_modify, avoid_modify, shield_def, shield_def_rate, atk_speed, mp_consume, m_dam, durability, price, crystal_count, sellable, tradeable, dropable, destroyable, skill_id, skill_level, enchant4_skill_id, enchant4_skill_lvl, variation_group_id, is_magic_weapon, temporal FROM weapon" };
	}
}
