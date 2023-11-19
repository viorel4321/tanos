package l2s.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

public class PlayerMessageStack
{
	private static PlayerMessageStack _instance;
	private final Map<Integer, List<L2GameServerPacket>> _stack;

	public static PlayerMessageStack getInstance()
	{
		if(PlayerMessageStack._instance == null)
			PlayerMessageStack._instance = new PlayerMessageStack();
		return PlayerMessageStack._instance;
	}

	public PlayerMessageStack()
	{
		_stack = new HashMap<Integer, List<L2GameServerPacket>>();
	}

	public void mailto(final int char_obj_id, final L2GameServerPacket message)
	{
		final Player cha = GameObjectsStorage.getPlayer(char_obj_id);
		if(cha != null)
		{
			cha.sendPacket(message);
			return;
		}
		synchronized (_stack)
		{
			List<L2GameServerPacket> messages;
			if(_stack.containsKey(char_obj_id))
				messages = _stack.remove(char_obj_id);
			else
				messages = new ArrayList<L2GameServerPacket>();
			messages.add(message);
			_stack.put(char_obj_id, messages);
		}
	}

	public void CheckMessages(final Player cha)
	{
		List<L2GameServerPacket> messages = null;
		synchronized (_stack)
		{
			if(!_stack.containsKey(cha.getObjectId()))
				return;
			messages = _stack.remove(cha.getObjectId());
		}
		if(messages == null || messages.size() == 0)
			return;
		for(final L2GameServerPacket message : messages)
			cha.sendPacket(message);
	}
}
