package l2s.gameserver.model.base;

import java.util.ArrayList;
import java.util.List;

public class MultiSellEntry
{
	private int _entryId;
	private List<MultiSellIngredient> _ingredients;
	private List<MultiSellIngredient> _production;
	private int _tax;

	public MultiSellEntry()
	{
		_ingredients = new ArrayList<MultiSellIngredient>();
		_production = new ArrayList<MultiSellIngredient>();
	}

	public MultiSellEntry(final int id)
	{
		_ingredients = new ArrayList<MultiSellIngredient>();
		_production = new ArrayList<MultiSellIngredient>();
		_entryId = id;
	}

	public MultiSellEntry(final int id, final int product, final int prod_count, final int enchant)
	{
		_ingredients = new ArrayList<MultiSellIngredient>();
		_production = new ArrayList<MultiSellIngredient>();
		_entryId = id;
		addProduct(new MultiSellIngredient(product, prod_count, enchant));
	}

	public void setEntryId(final int entryId)
	{
		_entryId = entryId;
	}

	public int getEntryId()
	{
		return _entryId;
	}

	public void addIngredient(final MultiSellIngredient ingredient)
	{
		if(ingredient.getItemCount() > 0)
			_ingredients.add(ingredient);
	}

	public List<MultiSellIngredient> getIngredients()
	{
		return _ingredients;
	}

	public void addProduct(final MultiSellIngredient ingredient)
	{
		_production.add(ingredient);
	}

	public List<MultiSellIngredient> getProduction()
	{
		return _production;
	}

	public int getTax()
	{
		return _tax;
	}

	public void setTax(final int tax)
	{
		_tax = tax;
	}

	@Override
	public int hashCode()
	{
		return _entryId;
	}

	@Override
	public MultiSellEntry clone()
	{
		final MultiSellEntry ret = new MultiSellEntry(_entryId);
		for(final MultiSellIngredient i : _ingredients)
			ret.addIngredient(i.clone());
		for(final MultiSellIngredient i : _production)
			ret.addProduct(i.clone());
		return ret;
	}
}
