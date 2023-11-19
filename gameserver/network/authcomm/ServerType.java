package l2s.gameserver.network.authcomm;

/**
 * @author VISTALL
 * @date 21:12/28.06.2011
 */
public enum ServerType
{
	/*0*/NORMAL,
	/*1*/RELAX,
	/*2*/TEST,
	/*3*/NO_LABEL,
	/*4*/RESTRICTED,
	/*5*/EVENT,
	/*6*/FREE,
	/*7*/UNK_7,
	/*8*/UNK_8,
	/*9*/UNK_9;

	private int _mask;

	ServerType()
	{
		_mask = 1 << ordinal();
	}

	public int getMask()
	{
		return _mask;
	}
}
