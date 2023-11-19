package l2s.gameserver.network.l2.s2c;

import java.util.Vector;

public class ConfirmDlg extends L2GameServerPacket
{
	private int _messageId;
	private int _Time;
	private int _requestId;
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private Vector<Integer> _types;
	private Vector<Object> _values;

	public ConfirmDlg(final int messageId, final int time)
	{
		_types = new Vector<Integer>();
		_values = new Vector<Object>();
		_messageId = messageId;
		_Time = time;
	}

	public ConfirmDlg addString(final String text)
	{
		_types.add(0);
		_values.add(text);
		return this;
	}

	public ConfirmDlg addNumber(final Integer number)
	{
		_types.add(1);
		_values.add(number);
		return this;
	}

	public ConfirmDlg addNumber(final Short number)
	{
		_types.add(1);
		_values.add(new Integer(number));
		return this;
	}

	public ConfirmDlg addNumber(final Byte number)
	{
		_types.add(1);
		_values.add(new Integer(number));
		return this;
	}

	public ConfirmDlg addNpcName(final int id)
	{
		_types.add(2);
		_values.add(new Integer(1000000 + id));
		return this;
	}

	public ConfirmDlg addItemName(final Short id)
	{
		_types.add(3);
		_values.add(new Integer(id));
		return this;
	}

	public ConfirmDlg addItemName(final Integer id)
	{
		_types.add(3);
		_values.add(id);
		return this;
	}

	public ConfirmDlg addZoneName(final int x, final int y, final int z)
	{
		_types.add(new Integer(7));
		final int[] coord = { x, y, z };
		_values.add(coord);
		return this;
	}

	public ConfirmDlg addSkillName(final Short id, final Short level)
	{
		_types.add(4);
		final int[] skill = { id, level };
		_values.add(skill);
		return this;
	}

	public void setRequestId(final int requestId)
	{
		_requestId = requestId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(237);
		writeD(_messageId);
		writeD(_types.size());
		for(int i = 0; i < _types.size(); ++i)
		{
			final int t = _types.get(i);
			writeD(t);
			switch(t)
			{
				case 0:
				{
					if(_values.size() >= i)
					{
						writeS((CharSequence) _values.get(i));
						break;
					}
					break;
				}
				case 1:
				case 2:
				case 3:
				{
					if(_values.size() < i)
						break;
					final int t2 = ((Integer) _values.get(i)).intValue();
					writeD(t2);
					break;
				}
				case 4:
				{
					if(_values.size() < i)
						break;
					final int[] skill = (int[]) _values.get(i);
					writeD(skill[0]);
					writeD(skill[1]);
					break;
				}
				case 7:
				{
					final int[] coord = (int[]) _values.get(i);
					writeD(coord[0]);
					writeD(coord[1]);
					writeD(coord[2]);
					break;
				}
			}
		}
		writeD(_Time);
		writeD(_requestId);
	}
}
