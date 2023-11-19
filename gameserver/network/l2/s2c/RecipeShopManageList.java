package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l2s.gameserver.model.ManufactureItem;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.RecipeList;

public class RecipeShopManageList extends L2GameServerPacket
{
	private List<CreateItemInfo> infos;
	private List<RecipeInfo> recipes;
	private int seller_id;
	private int seller_adena;
	private boolean _isDwarven;

	public RecipeShopManageList(final Player seller, final boolean isDvarvenCraft)
	{
		infos = new ArrayList<CreateItemInfo>();
		recipes = new ArrayList<RecipeInfo>();
		seller_id = seller.getObjectId();
		seller_adena = seller.getAdena();
		_isDwarven = isDvarvenCraft;
		Collection<RecipeList> _recipes;
		if(_isDwarven)
			_recipes = seller.getDwarvenRecipeBook();
		else
			_recipes = seller.getCommonRecipeBook();
		int i = 1;
		for(final RecipeList r : _recipes)
			recipes.add(new RecipeInfo(r.getId(), i++));
		if(seller.getCreateList() != null)
			for(final ManufactureItem item : seller.getCreateList().getList())
				infos.add(new CreateItemInfo(item.getRecipeId(), 0, item.getCost()));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(216);
		writeD(seller_id);
		writeD(seller_adena);
		writeD(_isDwarven ? 0 : 1);
		writeD(recipes.size());
		for(final RecipeInfo _recipe : recipes)
		{
			writeD(_recipe._id);
			writeD(_recipe.n);
		}
		recipes.clear();
		writeD(infos.size());
		for(final CreateItemInfo _info : infos)
		{
			writeD(_info._id);
			writeD(_info.unk1);
			writeD(_info.cost);
		}
		infos.clear();
	}

	static class RecipeInfo
	{
		public int _id;
		public int n;

		public RecipeInfo(final int __id, final int _n)
		{
			_id = __id;
			n = _n;
		}
	}

	static class CreateItemInfo
	{
		public int _id;
		public int unk1;
		public int cost;

		public CreateItemInfo(final int __id, final int _unk1, final int _cost)
		{
			_id = __id;
			unk1 = _unk1;
			cost = _cost;
		}
	}
}
