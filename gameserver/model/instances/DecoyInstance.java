package l2s.gameserver.model.instances;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ChangeWaitType;
import l2s.gameserver.templates.npc.NpcTemplate;

public class DecoyInstance extends NpcInstance
{
	private int ownerObjectId;
	private boolean _isSit;

	public DecoyInstance(final int objectId, final NpcTemplate template, final Player owner)
	{
		super(objectId, template);
		ownerObjectId = owner.getObjectId();
	}

	@Override
	public Player getPlayer()
	{
		return GameObjectsStorage.getPlayer(ownerObjectId);
	}

	@Override
	public boolean isAttackable(final Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return false;
	}

	@Override
	public void deleteMe()
	{
		final Player owner = getPlayer();
		if(owner != null)
			owner.setDecoy(null);
		super.deleteMe();
	}

	@Override
	public void onAction(final Player player, final boolean shift)
	{
		if(player.getTarget() != this)
			player.setTarget(this);
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else
			player.sendActionFailed();
	}

	@Override
	public double getCollisionRadius()
	{
		final Player player = getPlayer();
		if(player == null)
			return 0.;
		return player.getBaseTemplate().collisionRadius;
	}

	@Override
	public double getCollisionHeight()
	{
		final Player player = getPlayer();
		if(player == null)
			return 0.;
		return player.getBaseTemplate().collisionHeight;
	}

	@Override
	public void onRandomAnimation()
	{}

	public void sitDown()
	{
		this.broadcastPacket(new ChangeWaitType(this, 0));
		_isSit = true;
	}

	public void standUp()
	{
		this.broadcastPacket(new ChangeWaitType(this, 1));
		_isSit = false;
	}

	@Override
	public boolean isSitting()
	{
		return _isSit;
	}

	@Override
	public boolean isDecoy()
	{
		return true;
	}
}
