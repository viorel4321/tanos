package l2s.gameserver.network.l2.s2c;

public class ExShowScreenMessage extends L2GameServerPacket
{
	public enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT;
	}

	private int _type;
	private int _sysMessageId;
	private boolean _big_font;
	private boolean _effect;
	private ScreenMessageAlign _text_align;
	private int _time;
	private String _text;
	private int _unk1;
	private int _unk2;
	private int _unk3;
	private int _unk4;

	public ExShowScreenMessage(final String text, final int time, final ScreenMessageAlign text_align, final boolean big_font)
	{
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 0;
		_unk4 = 1;
		_type = 1;
		_sysMessageId = -1;
		_text = text;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = false;
	}

	public ExShowScreenMessage(final String text, final int time, final ScreenMessageAlign text_align)
	{
		this(text, time, text_align, true);
	}

	public ExShowScreenMessage(final String text, final int time)
	{
		this(text, time, ScreenMessageAlign.MIDDLE_CENTER);
	}

	public ExShowScreenMessage(final String text, final int time, final ScreenMessageAlign text_align, final boolean big_font, final int type, final int messageId, final boolean showEffect)
	{
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 0;
		_unk4 = 1;
		_type = type;
		_sysMessageId = messageId;
		_text = text;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
	}

	public ExShowScreenMessage(final String text, final int time, final ScreenMessageAlign text_align, final boolean big_font, final int type, final int messageId, final boolean showEffect, final int unk1, final int unk2, final int unk3, final int unk4)
	{
		this(text, time, text_align, big_font, type, messageId, showEffect);
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_unk4 = unk4;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(56);
		writeD(_type);
		writeD(_sysMessageId);
		writeD(_text_align.ordinal() + 1);
		writeD(_unk1);
		writeD(_big_font ? 0 : 1);
		writeD(_unk2);
		writeD(_unk3);
		writeD(_effect ? 1 : 0);
		writeD(_time);
		writeD(_unk4);
		writeS(_text);
	}
}
