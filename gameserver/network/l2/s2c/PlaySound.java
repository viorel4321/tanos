package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.utils.Location;

public class PlaySound extends L2GameServerPacket
{
	public static final L2GameServerPacket SIEGE_VICTORY;
	public static final L2GameServerPacket B04_S01;
	public static final L2GameServerPacket HB01;
	private int _type;
	private String _soundFile;
	private int _hasCenterObject;
	private int _objectId;
	private Location _loc;

	public PlaySound(final String soundFile)
	{
		this(0, soundFile, 0, 0, null);
	}

	public PlaySound(final Type type, final String soundFile, final int c, final int objectId, final Location loc)
	{
		this(type.ordinal(), soundFile, c, objectId, loc);
	}

	public PlaySound(final int type, final String soundFile, final int c, final int objectId, final Location loc)
	{
		_loc = new Location();
		_type = type;
		_soundFile = soundFile;
		_hasCenterObject = c;
		_objectId = objectId;
		if(loc != null)
			_loc = loc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(152);
		writeD(_type);
		writeS((CharSequence) _soundFile);
		writeD(_hasCenterObject);
		writeD(_objectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}

	static
	{
		SIEGE_VICTORY = new PlaySound("Siege_Victory");
		B04_S01 = new PlaySound("B04_S01");
		HB01 = new PlaySound(Type.MUSIC, "HB01", 0, 0, null);
	}

	public enum Type
	{
		SOUND,
		MUSIC,
		VOICE;
	}
}
