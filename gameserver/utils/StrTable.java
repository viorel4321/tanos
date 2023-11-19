package l2s.gameserver.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class StrTable
{
	private final HashMap<Integer, HashMap<String, String>> rows;
	private final LinkedHashMap<String, Integer> columns;
	private final List<String> titles;

	public StrTable(final String title)
	{
		rows = new HashMap<Integer, HashMap<String, String>>();
		columns = new LinkedHashMap<String, Integer>();
		titles = new ArrayList<String>();
		if(title != null)
			titles.add(title);
	}

	public StrTable()
	{
		this(null);
	}

	public StrTable set(final int rowIndex, final String colName, final Object value)
	{
		final String val = value.toString();
		HashMap<String, String> row;
		synchronized (rows)
		{
			if(rows.containsKey(rowIndex))
				row = rows.get(rowIndex);
			else
			{
				row = new HashMap<String, String>();
				rows.put(rowIndex, row);
			}
		}
		synchronized (row)
		{
			row.put(colName, val);
		}
		synchronized (columns)
		{
			int columnSize;
			if(!columns.containsKey(colName))
				columnSize = Math.max(colName.length(), val.length());
			else if(columns.get(colName) >= (columnSize = val.length()))
				return this;
			columns.put(colName, columnSize);
		}
		return this;
	}

	public StrTable addTitle(final String s)
	{
		synchronized (rows)
		{
			titles.add(s);
		}
		return this;
	}

	public static String pad_right(final String s, int sz)
	{
		String result = s;
		if((sz -= s.length()) > 0)
			result += repeat(" ", sz);
		return result;
	}

	public static String pad_left(final String s, int sz)
	{
		String result = s;
		if((sz -= s.length()) > 0)
			result = repeat(" ", sz) + result;
		return result;
	}

	public static String pad_center(final String s, final int sz)
	{
		String result = s;
		int i;
		while((i = sz - result.length()) > 0)
			if(i == 1)
				result += " ";
			else
				result = " " + result + " ";
		return result;
	}

	public static String repeat(final String s, final int sz)
	{
		String result = "";
		for(int i = 0; i < sz; ++i)
			result += s;
		return result;
	}

	@Override
	public String toString()
	{
		final String[] result;
		synchronized (rows)
		{
			if(columns.isEmpty())
				return "";
			String header = "|";
			String line = "|";
			for(final String c : columns.keySet())
			{
				header = header + pad_center(c, columns.get(c) + 2) + "|";
				line = line + repeat("-", columns.get(c) + 2) + "|";
			}
			result = new String[rows.size() + 4 + (titles.isEmpty() ? 0 : titles.size() + 1)];
			int i = 0;
			if(!titles.isEmpty())
			{
				result[i++] = " " + repeat("-", header.length() - 2) + " ";
				for(final String title : titles)
					result[i++] = "| " + pad_right(title, header.length() - 3) + "|";
			}
			result[i++] = result[result.length - 1] = " " + repeat("-", header.length() - 2) + " ";
			result[i++] = header;
			result[i++] = line;
			for(final HashMap<String, String> row : rows.values())
			{
				line = "|";
				for(final String c2 : columns.keySet())
					line = line + pad_center(row.containsKey(c2) ? row.get(c2) : "-", columns.get(c2) + 2) + "|";
				result[i++] = line;
			}
		}
		return Strings.joinStrings("\r\n", result);
	}

	public String toL2Html()
	{
		return toString().replaceAll("\r\n", "<br1>");
	}
}
