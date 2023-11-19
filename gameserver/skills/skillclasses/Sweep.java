package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class Sweep extends Skill
{
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(isNotTargetAoE())
			return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
		if(target == null)
			return false;
		if(!target.isMonster() || !((MonsterInstance) target).isDead() || ((MonsterInstance) target).isDying())
		{
			activeChar.sendPacket(Msg.INCORRECT_TARGET);
			return false;
		}
		if(!((MonsterInstance) target).isSpoiled())
		{
			activeChar.sendPacket(new SystemMessage(343));
			return false;
		}
		if(!((MonsterInstance) target).isSpoiled((Player) activeChar))
		{
			activeChar.sendPacket(Msg.THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	public Sweep(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;
		final Player player = (Player) activeChar;
		for(final Creature targ : targets)
			if(targ != null && targ.isMonster() && targ.isDead())
			{
				MonsterInstance target = (MonsterInstance) targ;
				//if(target.isSweeped() || !target.isSpoiled())
				//{
				//	_log.info("sweeped but not spoiled");
				//	continue;
				//}

				if(!target.isSpoiled(player))
					activeChar.sendPacket(Msg.THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER);
				else
				{
					final ItemInstance[] items = target.takeSweep();
					if(items == null)
					{
						activeChar.getAI().setAttackTarget(null);
						target.endDecayTask();
					}
					else
					{
						target.setSweeped(true);
						target.setSpoiled(false, null);
						for(final ItemInstance item : items)
							if(player.isInParty() && player.getParty().isDistributeSpoilLoot())
								player.getParty().distributeItem(player, item);
							else
							{
								final int itemId = item.getItemId();
								if(player.getInventoryLimit() <= player.getInventory().getSize() && (!item.isStackable() || player.getInventory().getItemByItemId(itemId) == null))
									item.dropToTheGround(player, target);
								else
								{
									final long itemCount = item.getCount();
									player.getInventory().addItem(item);
									if(itemCount == 1L)
									{
										final SystemMessage smsg = new SystemMessage(30);
										smsg.addItemName(Integer.valueOf(itemId));
										player.sendPacket(smsg);
									}
									else
									{
										final SystemMessage smsg = new SystemMessage(29);
										smsg.addItemName(Integer.valueOf(itemId));
										smsg.addNumber(Long.valueOf(itemCount));
										player.sendPacket(smsg);
									}
									if(player.isInParty())
										if(itemCount == 1L)
										{
											final SystemMessage smsg = new SystemMessage(609);
											smsg.addString(player.getName());
											smsg.addItemName(Integer.valueOf(itemId));
											player.getParty().broadcastToPartyMembers(player, smsg);
										}
										else
										{
											final SystemMessage smsg = new SystemMessage(608);
											smsg.addString(player.getName());
											smsg.addItemName(Integer.valueOf(itemId));
											smsg.addNumber(Long.valueOf(itemCount));
											player.getParty().broadcastToPartyMembers(player, smsg);
										}
								}
							}
						activeChar.getAI().setAttackTarget(null);
						target.endDecayTask();
					}
				}
			}
	}

	@Override
	public void onFinishCast(Creature aimingTarget, Creature activeChar, Set<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(!target.isMonster() || !target.isDead())
				continue;

			final MonsterInstance monter = (MonsterInstance) target;
			if(!monter.isSweeped())
				continue;

			monter.endDecayTask();
		}
	}
}
