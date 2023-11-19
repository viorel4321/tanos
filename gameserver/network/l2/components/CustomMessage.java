package l2s.gameserver.network.l2.components;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Language;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @reworked by Bonux
 * Даный класс является обработчиком серверных интернациональных сообщений.
 */
public class CustomMessage implements IBroadcastPacket
{
	private static interface IArgument
	{
		public String toString(Language lang);
	}

	private static class StringArgument implements IArgument
	{
		private final String _text;

		public StringArgument(String text)
		{
			_text = text;
		}

		@Override
		public String toString(Language lang)
		{
			return _text;
		}
	}

	private static class CustomMessageArgument implements IArgument
	{
		private final CustomMessage _customMessage;

		public CustomMessageArgument(CustomMessage customMessage)
		{
			_customMessage = customMessage;
		}

		@Override
		public String toString(Language lang)
		{
			return _customMessage.toString(lang);
		}
	}

	private static class ItemNameArgument implements IArgument
	{
		private final int _itemId;

		public ItemNameArgument(int itemId)
		{
			_itemId = itemId;
		}

		@Override
		public String toString(Language lang)
		{
			ItemTemplate item = ItemTable.getInstance().getTemplate(_itemId);
			if(item == null)
				return "null";
			return item.getName();
		}
	}

	private static class SkillNameArgument implements IArgument
	{
		private final int _skillId;
		private final int _skillLvl;

		public SkillNameArgument(int skillId, int skillLvl)
		{
			_skillId = skillId;
			_skillLvl = skillLvl;
		}

		@Override
		public String toString(Language lang)
		{
			Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
			if(skill == null)
				return "null";
			return skill.getName();
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(CustomMessage.class);

	private static final String[] PARAMS = {"{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}"};

	private String _address;
	private List<IArgument> _args;

	public CustomMessage(String address)
	{
		_address = address;
	}

	public CustomMessage addString(String text)
	{
		if(_args == null)
			_args = new ArrayList<IArgument>();
		_args.add(new StringArgument(text));
		return this;
	}

	public CustomMessage addNumber(int i)
	{
		return addString(String.valueOf(i));
	}

	public CustomMessage addNumber(long l)
	{
		return addString(String.valueOf(l));
	}

	public CustomMessage addCustomMessage(CustomMessage msg)
	{
		if(msg != this) // Иначе будет рекурсия.
		{
			if(_args == null)
				_args = new ArrayList<IArgument>();
			_args.add(new CustomMessageArgument(msg));
		}
		return this;
	}

	public CustomMessage addItemName(int itemId)
	{
		if(_args == null)
			_args = new ArrayList<IArgument>();
		_args.add(new ItemNameArgument(itemId));
		return this;
	}

	public CustomMessage addSkillName(int skillId, int skillLvl)
	{
		if(_args == null)
			_args = new ArrayList<IArgument>();
		_args.add(new SkillNameArgument(skillId, skillLvl));
		return this;
	}
	
	public String toString(Player player)
	{
		return toString(player.getLanguage());
	}

	public String toString(Language lang)
	{
		StrBuilder msg = null;

		String text = StringsHolder.getInstance().getString(_address, lang);
		if(text != null)
		{
			msg = new StrBuilder(text);

			if(_args != null)
			{
				for(int i = 0; i < _args.size(); i++)
				{
					msg.replaceFirst(PARAMS[i], _args.get(i).toString(lang));
				}
			}
		}

		if(StringUtils.isEmpty(msg))
		{
			_log.warn("CustomMessage: string: " + _address + " not found for lang: " + lang + "!");
			return StringUtils.EMPTY;
		}

		return msg.toString();
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		return new SystemMessage(SystemMessage.S1).addString(toString(player));
	}
}