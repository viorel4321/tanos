package l2s.gameserver.network.l2.s2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.items.ItemInstance;

public class EquipUpdate extends L2GameServerPacket
{
	private static Logger _log;
	private ItemInstance _item;
	private int _change;

	public EquipUpdate(final ItemInstance item, final int change)
	{
		_item = item;
		_change = change;
	}

	@Override
	protected final void writeImpl()
	{
		int bodypart = 0;
		writeC(75);
		writeD(_change);
		writeD(_item.getObjectId());
		switch(_item.getBodyPart())
		{
			case 4:
			{
				bodypart = 1;
				break;
			}
			case 2:
			{
				bodypart = 2;
				break;
			}
			case 8:
			{
				bodypart = 3;
				break;
			}
			case 16:
			{
				bodypart = 4;
				break;
			}
			case 32:
			{
				bodypart = 5;
				break;
			}
			case 64:
			{
				bodypart = 6;
				break;
			}
			case 128:
			{
				bodypart = 7;
				break;
			}
			case 256:
			{
				bodypart = 8;
				break;
			}
			case 512:
			{
				bodypart = 9;
				break;
			}
			case 1024:
			{
				bodypart = 10;
				break;
			}
			case 2048:
			{
				bodypart = 11;
				break;
			}
			case 4096:
			{
				bodypart = 12;
				break;
			}
			case 8192:
			{
				bodypart = 13;
				break;
			}
			case 16384:
			{
				bodypart = 14;
				break;
			}
			case 65536:
			{
				bodypart = 15;
				break;
			}
		}
		if(Config.DEBUG)
			EquipUpdate._log.info("body:" + bodypart);
		writeD(bodypart);
	}

	static
	{
		EquipUpdate._log = LoggerFactory.getLogger(EquipUpdate.class);
	}
}
