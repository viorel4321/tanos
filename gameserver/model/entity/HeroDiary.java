package l2s.gameserver.model.entity;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Map;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.HtmlUtils;

public class HeroDiary
{
	private static final SimpleDateFormat SIMPLE_FORMAT;
	public static final int ACTION_RAID_KILLED = 1;
	public static final int ACTION_HERO_GAINED = 2;
	public static final int ACTION_CASTLE_TAKEN = 3;
	private int _id;
	private long _time;
	private int _param;

	public HeroDiary(final int id, final long time, final int param)
	{
		_id = id;
		_time = time;
		_param = param;
	}

	public Map.Entry<String, String> toString(final Player player)
	{
		CustomMessage message = null;
		switch(_id)
		{
			case 1:
			{
				message = new CustomMessage("l2s.gameserver.model.entity.Hero.RaidBossKilled").addString(HtmlUtils.htmlNpcName(_param));
				break;
			}
			case 2:
			{
				message = new CustomMessage("l2s.gameserver.model.entity.Hero.HeroGained");
				break;
			}
			case 3:
			{
				message = new CustomMessage("l2s.gameserver.model.entity.Hero.CastleTaken").addString(HtmlUtils.htmlResidenceName(_param));
				break;
			}
			default:
			{
				return null;
			}
		}
		return new AbstractMap.SimpleEntry<String, String>(HeroDiary.SIMPLE_FORMAT.format(_time), message.toString(player));
	}

	static
	{
		SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");
	}
}
