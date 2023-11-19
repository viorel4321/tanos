package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.WorldRegion;

public class Action extends L2GameClientPacket
{
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _actionId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC();
	}

	@Override
	public void runImpl()
	{
		try
		{
			final Player activeChar = getClient().getActiveChar();
			if(activeChar == null)
				return;
			if(activeChar.isOutOfControl())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.inObserverMode() && activeChar.getObservNeighbor() != null)
				for(final WorldRegion region : activeChar.getObservNeighbor().getNeighbors())
					for(final GameObject obj : region.getObjectsList(new ArrayList<GameObject>(region.getObjectsSize()), activeChar.getObjectId(), activeChar.getReflectionId()))
						if(obj != null && obj.getObjectId() == _objectId && activeChar.getTarget() != obj)
						{
							obj.onAction(activeChar, false);
							return;
						}
			if(activeChar.getPrivateStoreType() != 0)
			{
				activeChar.sendActionFailed();
				return;
			}
			GameObject obj2 = activeChar.getVisibleObject(_objectId);
			if(obj2 == null && ((obj2 = GameObjectsStorage.getItem(_objectId)) == null || !activeChar.isInRange(obj2, 1000L)))
			{
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.getAggressionTarget() != null && activeChar.getAggressionTarget() != obj2)
			{
				activeChar.sendActionFailed();
				return;
			}
			obj2.onAction(activeChar, _actionId == 1);
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
	}
}
