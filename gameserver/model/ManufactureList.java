package l2s.gameserver.model;

import java.util.ArrayList;

import l2s.gameserver.Config;

public class ManufactureList
{
	private ArrayList<ManufactureItem> _list;
	private boolean _confirmed;
	private boolean _spam;
	private String _manufactureStoreName;

	public ManufactureList()
	{
		_list = new ArrayList<ManufactureItem>();
		_confirmed = false;
		_spam = false;
	}

	public int size()
	{
		return _list.size();
	}

	public void setConfirmedTrade(final boolean x)
	{
		_confirmed = x;
	}

	public boolean hasConfirmed()
	{
		return _confirmed;
	}

	public boolean isSpam()
	{
		return _spam;
	}

	public void setStoreName(final String manufactureStoreName)
	{
		_manufactureStoreName = manufactureStoreName;
		_spam = Config.SPAM_PS_WORK && manufactureStoreName != null && Config.containsSpamWord(Config.SPAM_SKIP_SYMBOLS ? manufactureStoreName.replaceAll("[^0-9a-zA-Z\u0410-\u042f\u0430-\u044f]", "") : manufactureStoreName);
	}

	public String getStoreName()
	{
		return _manufactureStoreName;
	}

	public void add(final ManufactureItem item)
	{
		_list.add(item);
	}

	public ArrayList<ManufactureItem> getList()
	{
		return _list;
	}

	public void setList(final ArrayList<ManufactureItem> list)
	{
		_list = list;
	}
}
