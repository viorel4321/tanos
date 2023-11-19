package l2s.gameserver.network.l2.s2c;

import java.util.Map;

import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.templates.StatsSet;

public class ExHeroList extends L2GameServerPacket
{
	private Map<Integer, StatsSet> _heroList;

	public ExHeroList()
	{
		_heroList = Hero.getInstance().getHeroes();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(35);
		writeD(_heroList.size());
		for(final StatsSet hero : _heroList.values())
		{
			writeS((CharSequence) hero.getString("char_name"));
			writeD(hero.getInteger("class_id"));
			writeS((CharSequence) hero.getString("clan_name", ""));
			writeD(hero.getInteger("clan_crest", 0));
			writeS((CharSequence) hero.getString("ally_name", ""));
			writeD(hero.getInteger("ally_crest", 0));
			writeD(hero.getInteger("count"));
		}
	}
}
