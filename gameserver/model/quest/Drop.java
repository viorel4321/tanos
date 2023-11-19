package l2s.gameserver.model.quest;

import java.util.ArrayList;
import java.util.List;

public class Drop
{
	public int condition;
	public int maxcount;
	public int chance;
	public List<Short> itemList;

	public Drop(final Integer _condition, final Integer _maxcount, final Integer _chance)
	{
		itemList = new ArrayList<Short>();
		condition = _condition;
		maxcount = _maxcount;
		chance = _chance;
	}

	public Drop addItem(final Short item)
	{
		itemList.add(item);
		return this;
	}
}
