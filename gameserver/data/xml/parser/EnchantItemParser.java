package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EnchantItemHolder;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.EnchantScroll;
import l2s.gameserver.templates.item.support.EnchantType;
import l2s.gameserver.templates.item.support.FailResultType;

/**
 * @author VISTALL
 * @date 3:10/18.06.2011
 */
public class EnchantItemParser extends AbstractParser<EnchantItemHolder>
{
	private static EnchantItemParser _instance = new EnchantItemParser();

	public static EnchantItemParser getInstance()
	{
		return _instance;
	}

	private EnchantItemParser()
	{
		super(EnchantItemHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/enchant_items.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "enchant_items.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		int defaultMaxEnchant = 0;
		int defaultChance = 0;
		int defaultMagicChance = 0;
		int defaultMinEcnhEff = 0;
		int defaultMaxEcnhEff = 0;
		int defaultSafeLevel = 3;
		int defaultSafeLevelFullArmor = 4;
		boolean defaultFailEffect = false;
		boolean defaultAltFormula = false;

		Element defaultElement = rootElement.element("default");
		if(defaultElement != null)
		{
			defaultMaxEnchant = Integer.parseInt(defaultElement.attributeValue("max_enchant"));
			defaultChance = Integer.parseInt(defaultElement.attributeValue("chance"));
			defaultMagicChance = Integer.parseInt(defaultElement.attributeValue("magic_chance"));
			defaultMinEcnhEff = Integer.parseInt(defaultElement.attributeValue("succ_eff_ench_min"));
			defaultMaxEcnhEff = Integer.parseInt(defaultElement.attributeValue("succ_eff_ench_max"));
			defaultFailEffect = Boolean.parseBoolean(defaultElement.attributeValue("show_fail_effect"));
			defaultAltFormula = Boolean.parseBoolean(defaultElement.attributeValue("is_alt_formula"));
			defaultSafeLevel = Integer.parseInt(defaultElement.attributeValue("safe_level"));
			defaultSafeLevelFullArmor = Integer.parseInt(defaultElement.attributeValue("safe_level_full_armor"));

		}

		for(Iterator<Element> iterator = rootElement.elementIterator("enchant_scroll"); iterator.hasNext();)
		{
			Element enchantItemElement = iterator.next();
			int itemId = Integer.parseInt(enchantItemElement.attributeValue("id"));
			int chance = enchantItemElement.attributeValue("chance") == null ? defaultChance : Integer.parseInt(enchantItemElement.attributeValue("chance"));
			int magicChance = enchantItemElement.attributeValue("magic_chance") == null ? defaultMagicChance : Integer.parseInt(enchantItemElement.attributeValue("magic_chance"));
			int maxEnchant = enchantItemElement.attributeValue("max_enchant") == null ? defaultMaxEnchant : Integer.parseInt(enchantItemElement.attributeValue("max_enchant"));
			FailResultType resultType = FailResultType.valueOf(enchantItemElement.attributeValue("on_fail"));
			EnchantType enchantType = enchantItemElement.attributeValue("type") == null ? EnchantType.ALL : EnchantType.valueOf(enchantItemElement.attributeValue("type"));
			ItemGrade grade = enchantItemElement.attributeValue("grade") == null ? ItemGrade.NONE : ItemGrade.valueOf(enchantItemElement.attributeValue("grade"));
			int minEcnhEff = enchantItemElement.attributeValue("succ_eff_ench_min") == null ? defaultMinEcnhEff : Integer.parseInt(enchantItemElement.attributeValue("succ_eff_ench_min"));
			int maxEcnhEff = enchantItemElement.attributeValue("succ_eff_ench_max") == null ? defaultMaxEcnhEff : Integer.parseInt(enchantItemElement.attributeValue("succ_eff_ench_max"));
			boolean failEffect = enchantItemElement.attributeValue("show_fail_effect") == null ? defaultFailEffect : Boolean.parseBoolean(enchantItemElement.attributeValue("show_fail_effect"));
			boolean useAltFormula = enchantItemElement.attributeValue("is_alt_formula") == null ? defaultAltFormula : Boolean.parseBoolean(enchantItemElement.attributeValue("is_alt_formula"));
			int safe_level = enchantItemElement.attributeValue("safe_level") == null ? defaultSafeLevel : Integer.parseInt(enchantItemElement.attributeValue("safe_level"));
			int safe_level_full_armor = enchantItemElement.attributeValue("safe_level_full_armor") == null ? defaultSafeLevelFullArmor : Integer.parseInt(enchantItemElement.attributeValue("safe_level_full_armor"));

			EnchantScroll item = new EnchantScroll(itemId, chance, magicChance, maxEnchant, enchantType, grade, resultType, minEcnhEff, maxEcnhEff, failEffect, useAltFormula, safe_level, safe_level_full_armor);

			for(Iterator<Element> iterator2 = enchantItemElement.elementIterator(); iterator2.hasNext();)
			{
				Element element2 = iterator2.next();
				if(element2.getName().equals("item_list"))
				{
					for(Element e : element2.elements())
						item.addItemId(Integer.parseInt(e.attributeValue("id")));
				}
			}
			getHolder().addEnchantScroll(item);
		}
	}
}
