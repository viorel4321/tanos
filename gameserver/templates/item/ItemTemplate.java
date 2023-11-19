package l2s.gameserver.templates.item;

import java.util.regex.Pattern;

import l2s.gameserver.Config;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ItemTemplate extends StatTemplate
{
	public static enum ItemClass
	{
		ALL,
		WEAPON,
		ARMOR,
		JEWELRY,
		ACCESSORY,
		/** Soul/Spiritshot, Potions, Scrolls */
		CONSUMABLE,
		/** Common craft matherials */
		MATHERIALS,
		/** Special (item specific) craft matherials */
		PIECES,
		/** Crafting recipies */
		RECIPIES,
		/** Skill learn books */
		SPELLBOOKS,
		/** Dyes, lifestones */
		MISC,
		/** Item, why contains capsuled items **/
		EXTRACTABLE,
		/** All other */
		OTHER
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemTemplate.class);

	public static final int ITEM_ID_PC_BANG_POINTS = -100;
	public static final int ITEM_ID_CLAN_REPUTATION_SCORE = -200;
	public static final int ITEM_ID_ADENA = 57;

	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_OTHER = 2;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;

	public static final byte TYPE2_WEAPON = 0;
	public static final byte TYPE2_SHIELD_ARMOR = 1;
	public static final byte TYPE2_ACCESSORY = 2;
	public static final byte TYPE2_QUEST = 3;
	public static final byte TYPE2_MONEY = 4;
	public static final byte TYPE2_OTHER = 5;
	public static final byte TYPE2_PET_WOLF = 6;
	public static final byte TYPE2_PET_HATCHLING = 7;
	public static final byte TYPE2_PET_STRIDER = 8;
	public static final byte TYPE2_NODROP = 9;

	public static final int SLOT_NONE = 0;
	public static final int SLOT_UNDERWEAR = 1;
	public static final int SLOT_R_EAR = 2;
	public static final int SLOT_L_EAR = 4;
	public static final int SLOT_NECK = 8;
	public static final int SLOT_R_FINGER = 16;
	public static final int SLOT_L_FINGER = 32;
	public static final int SLOT_HEAD = 64;
	public static final int SLOT_R_HAND = 128;
	public static final int SLOT_L_HAND = 256;
	public static final int SLOT_GLOVES = 512;
	public static final int SLOT_CHEST = 1024;
	public static final int SLOT_LEGS = 2048;
	public static final int SLOT_FEET = 4096;
	public static final int SLOT_BACK = 8192;
	public static final int SLOT_LR_HAND = 16384;
	public static final int SLOT_FULL_ARMOR = 32768;
	public static final int SLOT_HAIR = 65536;
	public static final int SLOT_FORMAL_WEAR = 131072;
	public static final int SLOT_DHAIR = 262144;
	public static final int SLOT_HAIRALL = 524288;
	public static final int SLOT_WOLF = -100;
	public static final int SLOT_HATCHLING = -101;
	public static final int SLOT_STRIDER = -102;
	public static final int SLOT_BABYPET = -103;

	public static final byte MATERIAL_STEEL = 0;
	public static final byte MATERIAL_FINE_STEEL = 1;
	public static final byte MATERIAL_BLOOD_STEEL = 2;
	public static final byte MATERIAL_BRONZE = 3;
	public static final byte MATERIAL_SILVER = 4;
	public static final byte MATERIAL_GOLD = 5;
	public static final byte MATERIAL_MITHRIL = 6;
	public static final byte MATERIAL_ORIHARUKON = 7;
	public static final byte MATERIAL_PAPER = 8;
	public static final byte MATERIAL_WOOD = 9;
	public static final byte MATERIAL_CLOTH = 10;
	public static final byte MATERIAL_LEATHER = 11;
	public static final byte MATERIAL_BONE = 12;
	public static final byte MATERIAL_HORN = 13;
	public static final byte MATERIAL_DAMASCUS = 14;
	public static final byte MATERIAL_ADAMANTAITE = 15;
	public static final byte MATERIAL_CHRYSOLITE = 16;
	public static final byte MATERIAL_CRYSTAL = 17;
	public static final byte MATERIAL_LIQUID = 18;
	public static final byte MATERIAL_SCALE_OF_DRAGON = 19;
	public static final byte MATERIAL_DYESTUFF = 20;
	public static final byte MATERIAL_COBWEB = 21;
	public static final byte MATERIAL_SEED = 22;
	public static final byte MATERIAL_FISH = 23;

	public static final int CRYSTAL_NONE = 0;
	public static final int CRYSTAL_D = 1458;
	public static final int CRYSTAL_C = 1459;
	public static final int CRYSTAL_B = 1460;
	public static final int CRYSTAL_A = 1461;
	public static final int CRYSTAL_S = 1462;

	private final short _itemId;
	private final ItemClass _class;
	protected final String _name;
	protected final String _addname;
	protected final String _icon;
	private final int _type1;
	private final int _type2;
	private final int _weight;
	private final boolean _crystallizable;
	private final boolean _stackable;
	private final ItemGrade _itemGrade;
	private final int _durability;
	private final boolean _temporal;
	private final int _bodyPart;
	private final int _referencePrice;
	private final short _crystalCount;
	private final boolean _sellable;
	private final boolean _dropable;
	private final boolean _tradeable;
	private final boolean _destroyable;
	private boolean _isMercTicket;
	private Skill[] _skills;
	public final Enum<?> type;
	private static final Pattern _noskill = Pattern.compile("(^0$)|(^-1$)");

	private IItemHandler _handler = IItemHandler.NULL;

	private final int _variationGroupId;

	protected ItemTemplate(final Enum<?> type, final StatsSet set)
	{
		this.type = type;
		_itemId = set.getShort("item_id");
		_class = ItemClass.valueOf(set.getString("class"));
		_name = set.getString("name");
		_addname = set.getString("additional_name", "");
		final String ic = set.getString("icon");
		_icon = ic.isEmpty() ? "icon.skill4416_etc" : ic;
		_type1 = set.getInteger("type1");
		_type2 = set.getInteger("type2");
		_weight = set.getInteger("weight");
		_crystallizable = set.getBool("crystallizable");
		_stackable = set.getBool("stackable", false);
		_itemGrade = ItemGrade.VALUES[set.getInteger("crystal_type", 0)];
		_durability = set.getInteger("durability", -1);
		_temporal = set.getBool("temporal", false);
		_bodyPart = set.getInteger("bodypart");
		if(Config.DIVIDER_PRICES > 1)
		{
			final int price = set.getInteger("price");
			if(price > 0)
			{
				if(price <= Config.DIVIDER_PRICES)
					_referencePrice = 1;
				else
					_referencePrice = price / Config.DIVIDER_PRICES;
			}
			else
				_referencePrice = 0;
		}
		else
			_referencePrice = set.getInteger("price");
		_crystalCount = set.getShort("crystal_count", (byte) 0);

		_variationGroupId = set.getInteger("variation_group_id", 0);

		_sellable = set.getBool("sellable", true);
		_dropable = set.getBool("dropable", true);
		_destroyable = set.getBool("destroyable", true);
		_tradeable = set.getBool("tradeable", true);
		final String[] skills = set.getString("skill_id", "0").split(";");
		final String[] skilllevels = set.getString("skill_level", "1").split(";");
		try
		{
			for(int i = 0; i < skills.length; ++i)
				if(!ItemTemplate._noskill.matcher(skills[i]).matches() && !ItemTemplate._noskill.matcher(skilllevels[i]).matches())
				{
					final Skill skill = SkillTable.getInstance().getInfo(Integer.parseInt(skills[i]), Integer.parseInt(skilllevels[i]));
					if(skill != null)
					{
						if(skill.getSkillType() == Skill.SkillType.NOTDONE)
							LOGGER.warn(getClass().getSimpleName() + ": item " + _itemId + " action attached skill not done: " + skill);
						attachSkill(skill);
					}
					else
						LOGGER.warn(getClass().getSimpleName() + ": item " + _itemId + " attached skill not exist: " + skills[i] + " " + skilllevels[i]);
				}
		}
		catch(Exception e)
		{
			LOGGER.error(getClass().getSimpleName() + ": Skill: " + set.getString("skill_id", "0") + " Level: " + set.getString("skill_level", "1") + ": " + e, e);
		}
	}

	public Enum<?> getItemType()
	{
		return type;
	}

	public final int getDurability()
	{
		return _durability;
	}

	public final boolean isTemporal()
	{
		return _temporal;
	}

	public final int getItemId()
	{
		return _itemId;
	}

	public abstract long getItemMask();

	public final int getType2()
	{
		return _type2;
	}

	public final int getType2ForPackets()
	{
		int type2 = _type2;
		switch(_type2)
		{
			case 6:
			case 7:
			case 8:
			{
				if(_bodyPart == 1024)
				{
					type2 = 1;
					break;
				}
				type2 = 0;
				break;
			}
		}
		return type2;
	}

	public final int getWeight()
	{
		return _weight;
	}

	public final boolean isCrystallizable()
	{
		return _crystallizable && !isStackable() && getItemGrade() != ItemGrade.NONE && getCrystalCount() > 0;
	}

	public final ItemGrade getItemGrade()
	{
		return _itemGrade;
	}

	public final int getCrystalCount()
	{
		return _crystalCount;
	}

	public final String getName()
	{
		return _name;
	}

	public final String getAdditionalName()
	{
		return _addname;
	}

	public final int getBodyPart()
	{
		return _bodyPart;
	}

	public final int getType1()
	{
		return _type1;
	}

	public final boolean isStackable()
	{
		return _stackable;
	}

	public final int getReferencePrice()
	{
		return _referencePrice;
	}

	public final boolean isSellable()
	{
		return _sellable;
	}

	public boolean isForHatchling()
	{
		return _type2 == 7;
	}

	public boolean isForStrider()
	{
		return _type2 == 8;
	}

	public boolean isForWolf()
	{
		return _type2 == 6;
	}

	public boolean isAugmentable()
	{
		return getVariationGroupId() > 0;
	}

	public boolean isTradeable()
	{
		return _tradeable;
	}

	public boolean isDestroyable()
	{
		return _destroyable;
	}

	public boolean isDropable()
	{
		return _dropable;
	}

	public boolean isForPet()
	{
		return _type2 == 7 || _type2 == 6 || _type2 == 8;
	}

	public void attachSkill(final Skill skill)
	{
		if(_skills == null)
			_skills = new Skill[] { skill };
		else
		{
			final int len = _skills.length;
			final Skill[] tmp = new Skill[len + 1];
			System.arraycopy(_skills, 0, tmp, 0, len);
			tmp[len] = skill;
			_skills = tmp;
		}
	}

	public Skill[] getAttachedSkills()
	{
		return _skills;
	}

	public Skill getFirstSkill()
	{
		if(_skills != null && _skills.length > 0)
			return _skills[0];
		return null;
	}

	@Override
	public String toString()
	{
		return _name;
	}

	public boolean isShadowItem()
	{
		return _durability > 0 && !isTemporal();
	}

	public boolean isSa()
	{
		return false;
	}

	public boolean isAltSeed()
	{
		return _name.contains("Alternative");
	}

	public ItemClass getItemClass()
	{
		return _class;
	}

	public boolean isAdena()
	{
		return Config.ADENA_SS ? _itemId == 57 || _itemId == 6360 || _itemId == 6361 || _itemId == 6362 : _itemId == 57;
	}

	public boolean isEnchantScroll()
	{
		return _itemId >= 6569 && _itemId <= 6578 || _itemId >= 949 && _itemId <= 962 || _itemId >= 729 && _itemId <= 732;
	}

	public boolean isEquipment()
	{
		return _bodyPart != 0;
	}

	public boolean isKeyMatherial()
	{
		return _class == ItemClass.PIECES;
	}

	public boolean isRaidAccessory()
	{
		return _itemId == 6661 || _itemId == 6659 || _itemId == 6656 || _itemId == 6660 || _itemId == 6662 || _itemId == 6658 || _itemId == 8191 || _itemId == 6657;
	}

	public boolean isRecipe()
	{
		return _class == ItemClass.RECIPIES;
	}

	public boolean isArrow()
	{
		return type == EtcItemTemplate.EtcItemType.ARROW;
	}

	public boolean isHerb()
	{
		return _itemId >= 8600 && _itemId <= 8614;
	}

	public boolean isHeroItem()
	{
		return _itemId >= 6611 && _itemId <= 6621 || _itemId == 6842;
	}

	public boolean isHeroWeapon()
	{
		return _itemId >= 6611 && _itemId <= 6621;
	}

	public boolean isCursed()
	{
		return CursedWeaponsManager.getInstance().isCursed(_itemId);
	}

	public boolean isFlag()
	{
		return _itemId == 6718;
	}

	public boolean isRod()
	{
		return getItemType() == WeaponTemplate.WeaponType.ROD;
	}

	public boolean isWeapon()
	{
		return getType2() == 0;
	}

	public boolean isArmor()
	{
		return getType2() == 1;
	}

	public boolean isAccessory()
	{
		return getType2() == 2;
	}

	public boolean isQuest()
	{
		return getType2() == 3;
	}

	public boolean isMercTicket()
	{
		return _isMercTicket;
	}

	public void setIsMercTicket(final boolean val)
	{
		_isMercTicket = val;
	}

	public boolean canBeEnchanted()
	{
		if(isHeroWeapon())
			return Config.ENCHANT_HERO_WEAPON;
		return !isShadowItem() && !isTemporal() && !isRod() && !isCursed() && !isQuest() && !isStackable() && getItemGrade() != ItemGrade.NONE;
	}

	public boolean isEquipable()
	{
		return getItemType() == EtcItemTemplate.EtcItemType.BAIT || getItemType() == EtcItemTemplate.EtcItemType.ARROW || getBodyPart() != 0 && !(this instanceof EtcItemTemplate);
	}

	public String getIcon()
	{
		return _icon;
	}

	public IItemHandler getHandler()
	{
		return _handler;
	}

	public void setHandler(IItemHandler handler)
	{
		_handler = handler;
	}

	public boolean useItem(Playable playable, ItemInstance item, boolean ctrlPressed)
	{
		if(playable == null || item == null || playable.getObjectId() != item.getOwnerId() || item.getTemplate() != this)
			return false;

		if(playable.isPlayer())
		{
			if(item.getLocation() != ItemInstance.ItemLocation.INVENTORY && item.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
				return false;
		}
		else if(playable.isPet())
		{
			if(item.getLocation() != ItemInstance.ItemLocation.INVENTORY && item.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
				return false;
		}
		else
			return false;

		IItemHandler handler = getHandler();
		if(handler == null)
			return false;

		return handler.useItem(playable, item, ctrlPressed);
	}

	public int getVariationGroupId()
	{
		return _variationGroupId;
	}

	public WeaponFightType getWeaponFightType()
	{
		return WeaponFightType.WARRIOR;
	}
}
