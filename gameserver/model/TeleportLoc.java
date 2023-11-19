package l2s.gameserver.model;

public class TeleportLoc
{
	private int _teleId;
	private int _locX;
	private int _locY;
	private int _locZ;
	private int _price;
	private boolean _forNoble;

	public void setTeleId(final int id)
	{
		_teleId = id;
	}

	public void setLocX(final int locX)
	{
		_locX = locX;
	}

	public void setLocY(final int locY)
	{
		_locY = locY;
	}

	public void setLocZ(final int locZ)
	{
		_locZ = locZ;
	}

	public void setPrice(final int price)
	{
		_price = price;
	}

	public void setIsForNoble(final boolean val)
	{
		_forNoble = val;
	}

	public int getTeleId()
	{
		return _teleId;
	}

	public int getLocX()
	{
		return _locX;
	}

	public int getLocY()
	{
		return _locY;
	}

	public int getLocZ()
	{
		return _locZ;
	}

	public int getPrice()
	{
		return _price;
	}

	public boolean isForNoble()
	{
		return _forNoble;
	}
}
