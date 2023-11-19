package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CastleManorManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;

public class RequestSetCrop extends L2GameClientPacket
{
	private int _size;
	private int _manorId;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_size = readD();
		if(_size * 13 > _buf.remaining() || _size > 32767 || _size <= 0)
		{
			_size = 0;
			return;
		}
		_items = new int[_size * 4];
		for(int i = 0; i < _size; ++i)
		{
			final int itemId = readD();
			_items[i * 4 + 0] = itemId;
			final int sales = readD();
			_items[i * 4 + 1] = sales;
			final int price = readD();
			_items[i * 4 + 2] = price;
			final int type = readC();
			_items[i * 4 + 3] = type;
		}
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _size < 1)
			return;
		if(activeChar.getClan() == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		final Castle caslte = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(caslte == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		final List<CastleManorManager.CropProcure> crops = new ArrayList<CastleManorManager.CropProcure>();
		for(int i = 0; i < _size; ++i)
		{
			final int id = _items[i * 4 + 0];
			final int sales = _items[i * 4 + 1];
			final int price = _items[i * 4 + 2];
			final int type = _items[i * 4 + 3];
			if(id > 0)
			{
				final CastleManorManager.CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
				crops.add(s);
			}
		}
		caslte.setCropProcure(crops, 1);
		if(Config.MANOR_SAVE_ALL_ACTIONS)
			caslte.saveCropData(1);
	}
}
