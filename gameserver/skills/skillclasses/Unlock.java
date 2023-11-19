package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.BoxInstance;
import l2s.gameserver.model.instances.ChestInstance;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class Unlock extends Skill
{
	private static final boolean chestTrap(final Creature chest)
	{
		if(chest.getLevel() > 60)
			return Rnd.get(100) < 80;
		if(chest.getLevel() > 40)
			return Rnd.get(100) < 50;
		if(chest.getLevel() > 30)
			return Rnd.get(100) < 30;
		return Rnd.get(100) < 10;
	}

	private static final boolean chestUnlock(final Skill skill, final Creature chest, final Creature cha, final boolean key)
	{
		int chance = 0;
		if(key)
		{
			if(chest.getLevel() > 1 && chest.getLevel() <= 19)
				switch(skill.getLevel())
				{
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					{
						return true;
					}
				}
			else if(chest.getLevel() >= 20 && chest.getLevel() <= 29)
				switch(skill.getLevel())
				{
					case 1:
					{
						chance = 90 - (chest.getLevel() - skill.getLevel() * 10);
						break;
					}
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					{
						return true;
					}
				}
			else if(chest.getLevel() >= 30 && chest.getLevel() <= 39)
				switch(skill.getLevel())
				{
					case 1:
					{
						chance = 3;
						break;
					}
					case 2:
					{
						chance = 90 - (chest.getLevel() - skill.getLevel() * 10);
						break;
					}
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					{
						return true;
					}
				}
			else if(chest.getLevel() >= 40 && chest.getLevel() <= 49)
				switch(skill.getLevel())
				{
					case 1:
					case 2:
					{
						chance = 3;
						break;
					}
					case 3:
					{
						chance = 90 - (chest.getLevel() - skill.getLevel() * 10);
						break;
					}
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					{
						return true;
					}
				}
			else if(chest.getLevel() >= 50 && chest.getLevel() <= 59)
				switch(skill.getLevel())
				{
					case 1:
					case 2:
					case 3:
					{
						chance = 3;
						break;
					}
					case 4:
					{
						chance = 90 - (chest.getLevel() - skill.getLevel() * 10);
						break;
					}
					case 5:
					case 6:
					case 7:
					case 8:
					{
						return true;
					}
				}
			else if(chest.getLevel() >= 60 && chest.getLevel() <= 69)
				switch(skill.getLevel())
				{
					case 1:
					case 2:
					case 3:
					case 4:
					{
						chance = 3;
						break;
					}
					case 5:
					{
						chance = 90 - (chest.getLevel() - skill.getLevel() * 10);
						break;
					}
					case 6:
					case 7:
					case 8:
					{
						return true;
					}
				}
			else if(chest.getLevel() >= 70 && chest.getLevel() <= 79)
				switch(skill.getLevel())
				{
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					{
						chance = 3;
						break;
					}
					case 6:
					{
						chance = 90 - (chest.getLevel() - skill.getLevel() * 10);
						break;
					}
					case 7:
					case 8:
					{
						return true;
					}
				}
			else if(chest.getLevel() >= 80 && chest.getLevel() <= 89)
				switch(skill.getLevel())
				{
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					{
						chance = 3;
						break;
					}
					case 7:
					{
						chance = 90 - (chest.getLevel() - skill.getLevel() * 10);
						break;
					}
					case 8:
					{
						return true;
					}
				}
		}
		else
		{
			if(chest.getLevel() > 60)
			{
				if(skill.getLevel() < 10)
					return false;
				chance = (skill.getLevel() - 10) * 5 + 30;
			}
			else if(chest.getLevel() > 40)
			{
				if(skill.getLevel() < 6)
					return false;
				chance = (skill.getLevel() - 6) * 5 + 10;
			}
			else if(chest.getLevel() > 30)
			{
				if(skill.getLevel() < 3)
					return false;
				if(skill.getLevel() > 12)
					return true;
				chance = (skill.getLevel() - 3) * 5 + 30;
			}
			else
			{
				if(skill.getLevel() > 10)
					return true;
				chance = skill.getLevel() * 5 + 35;
			}
			if(cha.getLevel() >= 78)
			{
				if(cha.getLevel() - chest.getLevel() >= 5)
					chance /= 2;
			}
			else if(cha.getLevel() <= 77 && cha.getLevel() - chest.getLevel() >= 6)
				chance /= 2;
			chance = Math.min(chance, 70);
		}
		return Rnd.get(100) <= chance;
	}

	private static final boolean doorUnlock(final Skill skill)
	{
		switch(skill.getLevel())
		{
			case 0:
			{
				return false;
			}
			case 1:
			{
				return Rnd.get(120) < 30;
			}
			case 2:
			{
				return Rnd.get(120) < 50;
			}
			case 3:
			{
				return Rnd.get(120) < 75;
			}
			default:
			{
				return Rnd.get(120) < 100;
			}
		}
	}

	public Unlock(final StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(target == null || (target.isChest() || target.isBox()) && target.isDead())
		{
			activeChar.sendPacket(new SystemMessage(109));
			return false;
		}
		if((target.isChest() || target.isBox()) && activeChar.isPlayer())
			return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
		if(!target.isDoor())
		{
			activeChar.sendPacket(new SystemMessage(109));
			return false;
		}
		final DoorInstance door = (DoorInstance) target;
		if(door.isOpen())
		{
			activeChar.sendPacket(new SystemMessage(321));
			return false;
		}
		if(!door.isUnlockable())
		{
			activeChar.sendPacket(new SystemMessage(319));
			return false;
		}
		if(door.getKey() > 0)
		{
			activeChar.sendPacket(new SystemMessage(319));
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature targ : targets)
			if(targ != null)
			{
				if(targ.isDoor())
				{
					final DoorInstance target = (DoorInstance) targ;
					if(target.isOpen())
						activeChar.sendPacket(new SystemMessage(321));
					else if(doorUnlock(this))
					{
						target.openMe(activeChar.getPlayer(), true);
					}
					else
					{
						activeChar.sendPacket(new PlaySound("interfacesound.system_close_01"));
						activeChar.sendPacket(new SystemMessage(320));
					}
					return;
				}
				if(targ.isChest())
				{
					final ChestInstance chest = (ChestInstance) targ;
					final Player player = (Player) activeChar;
					if(chest.isDead())
						return;
					chest.onOpen((Player) activeChar);
					if(chestTrap(chest))
						chest.chestTrap(player);
					else
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.ChestInstance.Fake"));
				}
				else
				{
					if(!targ.isBox())
						continue;
					final BoxInstance box = (BoxInstance) targ;
					if(box.isDead())
						return;
					if(chestUnlock(this, box, activeChar, isHandler()))
						box.onOpen((Player) activeChar);
					else
					{
						activeChar.sendPacket(new PlaySound("interfacesound.system_close_01"));
						activeChar.sendPacket(new SystemMessage(1597).addString(getName()));
						box.doDie(null);
					}
				}
			}
	}
}
