package l2s.gameserver.templates.item.support;

import java.util.Collections;
import java.util.Set;

import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

/**
 * @author VISTALL
 * @date 22:40/19.05.2011
 */
public class EnchantItem
{
	private final int _itemId;
	private final int _chance;
	private final int _magicChance;
	private final int _maxEnchant;
	private final EnchantType _type;
	private final ItemGrade _grade;
	private boolean _altFormula;
	private final int _safeLevel;
	private final int _safeLevelFullArmor;

	private IntSet _items = Containers.EMPTY_INT_SET;

	public EnchantItem(int itemId, int chance, int magicChance, int maxEnchant, EnchantType type, ItemGrade grade, boolean altFormula, int safeLevel, int safeLevelFull)
	{
		_itemId = itemId;
		_chance = chance;
		_magicChance = magicChance;
		_maxEnchant = maxEnchant;
		_type = type;
		_grade = grade;
		_altFormula = altFormula;
		_safeLevel = safeLevel;
		_safeLevelFullArmor = safeLevelFull;
	}

	public void addItemId(int id)
	{
		if(_items.isEmpty())
			_items = new HashIntSet();

		_items.add(id);
	}

	public int getItemId()
	{
		return _itemId;
	}
	
	public boolean isAltFormula(boolean forceAll)
	{
		if(forceAll)
			return true;
		return _altFormula;
	}
	
	public int getChance(boolean isMagic)
	{
		return isMagic ? _magicChance : _chance;
	}

	public int getMaxEnchant()
	{
		return _maxEnchant;
	}

	public ItemGrade getGrade()
	{
		return _grade;
	}

	public IntSet getItems()
	{
		return _items;
	}

	public EnchantType getType()
	{
		return _type;
	}
	
	public int getSafeLevel()
	{
		return _safeLevel;
	}

	public int getSafeLevelFullArmor()
	{
		return _safeLevelFullArmor;
	}	
}
