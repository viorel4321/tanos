package l2s.gameserver.model.instances;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.ShowTownMap;
import l2s.gameserver.network.l2.s2c.StaticObject;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class StaticObjectInstance extends NpcInstance
{
	private int _id;
	private int _type;
	private String _filePath;
	private int _mapX;
	private int _mapY;

	public int getUId()
	{
		return _id;
	}

	public void setUId(int id)
	{
		_id = id;
	}

	public void getUId(int id)
	{
		_id = id;
	}

	public StaticObjectInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_type = -1;
	}

	public int getType()
	{
		return _type;
	}

	public void setType(final int type)
	{
		_type = type;
	}

	public void setFilePath(final String path)
	{
		_filePath = path;
	}

	public void setMapX(final int x)
	{
		_mapX = x;
	}

	public void setMapY(final int y)
	{
		_mapY = y;
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			return;
		}
		if(!this.isInRange(player, 150L))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}
		if(player.isMoving)
			player.stopMove();
		player.turn(this, 3000);
		if(_type == 0)
			player.sendPacket(new NpcHtmlMessage(player, this, "newspaper/arena.htm", 0));
		else if(_type == 2)
		{
			player.sendPacket(new ShowTownMap(_filePath, _mapX, _mapY));
			player.sendActionFailed();
		}
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable(final Creature attacker)
	{
		return false;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public int getGeoZ(int x, int y, int z)
	{
		return z;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		return Collections.singletonList(new StaticObject(this));
	}

	@Override
	public Clan getClan()
	{
		return null;
	}

	@Override
	public boolean isStaticObject()
	{
		return true;
	}
}
