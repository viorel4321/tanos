package l2s.gameserver.templates.item;

import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public final class EtcItemTemplate extends ItemTemplate
{
	public EtcItemTemplate(final EtcItemType type, final StatsSet set)
	{
		super(type, set);
	}

	@Override
	public EtcItemType getItemType()
	{
		return (EtcItemType) super.type;
	}

	@Override
	public long getItemMask()
	{
		return getItemType().mask();
	}

	@Override
	public final boolean isShadowItem()
	{
		return false;
	}

	public enum EtcItemType
	{
		ARROW(1, "Arrow"),
		MATERIAL(2, "Material"),
		PET_COLLAR(3, "PetCollar"),
		POTION(4, "Potion"),
		RECIPE(5, "Recipe"),
		SCROLL(6, "Scroll"),
		QUEST(7, "Quest"),
		MONEY(8, "Money"),
		OTHER(9, "Other"),
		SPELLBOOK(10, "Spellbook"),
		SEED(11, "Seed"),
		BAIT(12, "Bait"),
		SHOT(13, "Shot");

		final int _id;
		final String _name;

		private EtcItemType(final int id, final String name)
		{
			_id = id;
			_name = name;
		}

		public long mask()
		{
			return 1L << _id + WeaponTemplate.WeaponType.values().length + ArmorTemplate.ArmorType.values().length;
		}

		@Override
		public String toString()
		{
			return _name;
		}
	}
}
