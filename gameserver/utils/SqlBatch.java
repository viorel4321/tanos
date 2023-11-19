package l2s.gameserver.utils;

public class SqlBatch
{
	private String _header;
	private String _tail;
	private StringBuffer _sb;
	private StringBuffer _result;
	private long _limit;
	private boolean isEmpty;

	public SqlBatch(final String header, final String tail)
	{
		_limit = Long.MAX_VALUE;
		isEmpty = true;
		_header = header + "\n";
		_tail = tail != null && tail.length() > 0 ? " " + tail + ";\n" : ";\n";
		_sb = new StringBuffer(_header);
		_result = new StringBuffer();
	}

	public SqlBatch(final String header)
	{
		this(header, null);
	}

	public void writeStructure(final String str)
	{
		_result.append(str);
	}

	public void write(final String str)
	{
		isEmpty = false;
		if(_sb.length() + str.length() < _limit - _tail.length())
			_sb.append(str + ",\n");
		else
		{
			_sb.append(str + _tail);
			_result.append(_sb.toString());
			_sb = new StringBuffer(_header);
		}
	}

	public void writeBuffer()
	{
		final String last = _sb.toString();
		if(last.length() > 0)
			_result.append(last.substring(0, last.length() - 2) + _tail);
		_sb = new StringBuffer(_header);
	}

	public String close()
	{
		if(_sb.length() > _header.length())
			writeBuffer();
		return _result.toString();
	}

	public void setLimit(final long l)
	{
		_limit = l;
	}

	public boolean isEmpty()
	{
		return isEmpty;
	}
}
