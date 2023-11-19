package l2s.gameserver.data.xml;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.parser.*;

/**
 * @author VISTALL
 * @date  20:55/30.11.2010
 */
public abstract class Parsers
{
	public static void parseAll()
	{
		ThreadPoolManager.getInstance().execute(() -> HtmCache.getInstance().reload());
		StringsHolder.getInstance().load();

		OptionDataParser.getInstance().load();
		VariationDataParser.getInstance().load();
		DoorParser.getInstance().load();
		SpawnParser.getInstance().load();
		ResidenceParser.getInstance().load();
		EventParser.getInstance().load();
		CubicParser.getInstance().load();
		MultiSellHolder.getInstance();
	}
}
