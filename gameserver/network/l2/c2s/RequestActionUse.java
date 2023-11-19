package l2s.gameserver.network.l2.c2s;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.ManufactureList;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.SiegeHeadquarterInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.network.l2.s2c.RecipeShopManageList;

public class RequestActionUse extends L2GameClientPacket
{
	private static Logger _log;
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	public void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		boolean usePet = false;
		switch(_actionId)
		{
			case 16:
			case 17:
			case 19:
			case 22:
			case 23:
			case 32:
			case 36:
			case 39:
			case 41:
			case 42:
			case 43:
			case 44:
			case 45:
			case 46:
			case 47:
			case 48:
			case 52:
			case 53:
			case 54:
			case 1004:
			case 1005:
			case 1006:
			case 1007:
			case 1008:
			case 1009:
			case 1010:
			case 1011:
			case 1012:
			case 1013:
			case 1014:
			case 1015:
			case 1016:
			case 1017:
			case 1041:
			case 1042:
			{
				usePet = true;
				break;
			}
			default:
			{
				usePet = false;
				break;
			}
		}
		if(!usePet && (activeChar.isOutOfControl() || activeChar.isActionsDisabled()) && (!activeChar.isFakeDeath() || _actionId != 0))
		{
			activeChar.sendActionFailed();
			return;
		}
		final GameObject target = activeChar.getTarget();
		final Servitor pet = activeChar.getServitor();
		if(usePet && (pet == null || pet.isOutOfControl()))
		{
			activeChar.sendActionFailed();
			return;
		}
		switch(_actionId)
		{
			case 0:
			{
				if(activeChar.isMounted())
				{
					activeChar.sendActionFailed();
					break;
				}
				if(activeChar.isFakeDeath())
				{
					activeChar.breakFakeDeath();
					activeChar.updateEffectIcons();
					break;
				}
				if(activeChar.isSitting())
				{
					activeChar.standUp();
					break;
				}
				if(target != null && target instanceof StaticObjectInstance && ((StaticObjectInstance) target).getType() == 1 && activeChar.getDistance3D(target) <= 150.0)
				{
					activeChar.sitDown(((StaticObjectInstance) target).getUId());
					break;
				}
				activeChar.sitDown(0);
				break;
			}
			case 1:
			{
				if(activeChar.isRunning())
				{
					activeChar.setWalking();
					break;
				}
				activeChar.setRunning();
				break;
			}
			case 10:
			{
				activeChar.tryOpenPrivateStore(true, false);
				break;
			}
			case 28:
			{
				activeChar.tryOpenPrivateStore(false, false);
				break;
			}
			case 15:
			case 21:
			{
				if(pet == null)
					break;
				if(pet.isDepressed())
				{
					activeChar.sendPacket(Msg.THE_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
					break;
				}
				pet.setFollowTarget(pet.getPlayer());
				pet.setFollowStatus(!pet.isFollow(), true);
				break;
			}
			case 16:
			case 22:
			{
				if(target == null || pet == target || pet.isDead())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInOlympiadMode() && !activeChar.isOlympiadCompStart())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(pet.getTemplate().getId() == 12564)
					return;
				if(!_ctrlPressed && !target.isAutoAttackable(pet))
				{
					pet.setFollowTarget((Creature) target);
					pet.setFollowStatus(true, true);
					return;
				}
				if(activeChar.getLevel() + 20 <= pet.getLevel())
				{
					activeChar.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
					return;
				}
				if(!target.isDoor() && pet.isSiegeWeapon())
				{
					activeChar.sendPacket(Msg.INCORRECT_TARGET);
					return;
				}
				if(pet.isPet() && activeChar.getDistance(activeChar.getServitor()) > 1500.0)
					return;
				pet.getAI().Attack(target, _ctrlPressed, _shiftPressed);
				break;
			}
			case 17:
			case 23:
			{
				pet.setFollowTarget(pet.getPlayer());
				pet.setFollowStatus(false, true);
				break;
			}
			case 19:
			{
				if(pet.isDead())
				{
					activeChar.sendPacket(Msg.A_DEAD_PET_CANNOT_BE_SENT_BACK, Msg.ActionFail);
					return;
				}
				if(pet.isInCombat())
				{
					activeChar.sendPacket(Msg.A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE, Msg.ActionFail);
					break;
				}
				if(pet.isPet() && pet.getCurrentFed() < 0.55 * pet.getMaxFed())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_RESTORE_HUNGRY_PETS, Msg.ActionFail);
					break;
				}
				pet.unSummon();
				break;
			}
			case 38:
			{
				mount(activeChar, pet);
				break;
			}
			case 32:
			{
				UseSkill(4230, target);
				break;
			}
			case 36:
			{
				UseSkill(4259, target);
				break;
			}
			case 37:
			{
				if(activeChar.isInTransaction())
					activeChar.getTransaction().cancel();
				if(activeChar.getCreateList() == null)
					activeChar.setCreateList(new ManufactureList());
				activeChar.setPrivateStoreType((short) 0);
				activeChar.standUp();
				activeChar.broadcastUserInfo(true);
				if(!activeChar.checksForShop(true))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			}
			case 39:
			{
				UseSkill(4138, target);
				break;
			}
			case 41:
			{
				if(target.isDoor())
				{
					UseSkill(4230, target);
					break;
				}
				activeChar.sendPacket(Msg.INCORRECT_TARGET);
				break;
			}
			case 42:
			{
				UseSkill(4378, activeChar);
				break;
			}
			case 43:
			{
				UseSkill(4137, target);
				break;
			}
			case 44:
			{
				UseSkill(4139, target);
				break;
			}
			case 45:
			{
				UseSkill(4025, activeChar);
				break;
			}
			case 46:
			{
				UseSkill(4261, target);
				break;
			}
			case 47:
			{
				UseSkill(4260, target);
				break;
			}
			case 48:
			{
				UseSkill(4068, target);
				break;
			}
			case 51:
			{
				if(!activeChar.checksForShop(true))
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getCreateList() == null)
					activeChar.setCreateList(new ManufactureList());
				activeChar.setPrivateStoreType((short) 0);
				activeChar.standUp();
				activeChar.broadcastUserInfo(true);
				activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			}
			case 52:
			{
				if(pet.isInCombat())
				{
					activeChar.sendPacket(Msg.A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE);
					activeChar.sendActionFailed();
					break;
				}
				pet.unSummon();
				break;
			}
			case 53:
			case 54:
			{
				if(target != null && pet != target && !pet.isMovementDisabled())
				{
					pet.setFollowStatus(false, true);
					pet.moveToLocation(target.getLoc(), 100, true);
					break;
				}
				break;
			}
			case 61:
			{
				activeChar.tryOpenPrivateStore(true, true);
				break;
			}
			case 1000:
			{
				if(target.isDoor())
				{
					UseSkill(4079, target);
					break;
				}
				activeChar.sendPacket(Msg.INCORRECT_TARGET);
				break;
			}
			case 1001:
			{
				break;
			}
			case 1003:
			{
				UseSkill(4710, target);
				break;
			}
			case 1004:
			{
				UseSkill(4711, activeChar);
				break;
			}
			case 1005:
			{
				UseSkill(4712, target);
				break;
			}
			case 1006:
			{
				UseSkill(4713, activeChar);
				break;
			}
			case 1007:
			{
				UseSkill(4699, activeChar);
				break;
			}
			case 1008:
			{
				UseSkill(4700, activeChar);
				break;
			}
			case 1009:
			{
				UseSkill(4701, target);
				break;
			}
			case 1010:
			{
				UseSkill(4702, activeChar);
				break;
			}
			case 1011:
			{
				UseSkill(4703, activeChar);
				break;
			}
			case 1012:
			{
				UseSkill(4704, target);
				break;
			}
			case 1013:
			{
				UseSkill(4705, target);
				break;
			}
			case 1014:
			{
				UseSkill(4706, activeChar);
				break;
			}
			case 1015:
			{
				UseSkill(4707, target);
				break;
			}
			case 1016:
			{
				UseSkill(4709, target);
				break;
			}
			case 1017:
			{
				UseSkill(4708, target);
				break;
			}
			case 1031:
			{
				UseSkill(5135, target);
				break;
			}
			case 1032:
			{
				UseSkill(5136, target);
				break;
			}
			case 1033:
			{
				UseSkill(5137, target);
				break;
			}
			case 1034:
			{
				UseSkill(5138, target);
				break;
			}
			case 1035:
			{
				UseSkill(5139, target);
				break;
			}
			case 1036:
			{
				UseSkill(5142, target);
				break;
			}
			case 1037:
			{
				UseSkill(5141, target);
				break;
			}
			case 1038:
			{
				UseSkill(5140, target);
				break;
			}
			case 1039:
			{
				if(!target.isDoor() && !(target instanceof SiegeHeadquarterInstance))
				{
					UseSkill(5110, target);
					break;
				}
				activeChar.sendPacket(Msg.INCORRECT_TARGET);
				break;
			}
			case 1040:
			{
				if(!target.isDoor() && !(target instanceof SiegeHeadquarterInstance))
				{
					UseSkill(5111, target);
					break;
				}
				activeChar.sendPacket(Msg.INCORRECT_TARGET);
				break;
			}
			default:
			{
				if(Config.ALLOW_PETS_ACTION_SKILLS && Config.PETS_ACTION_SKILLS.containsKey(_actionId))
				{
					final int id = Config.PETS_ACTION_SKILLS.get(_actionId);
					UseSkill(id, pet.getTemplate().getSkills().get(id).isOffensive() ? target : activeChar);
					break;
				}
				RequestActionUse._log.warn(activeChar.toString() + " unhandled action type " + _actionId);
				activeChar.kick(true);
				break;
			}
		}
	}

	public static void mount(final Player activeChar, final Servitor pet)
	{
		if(pet != null && pet.isMountable() && !activeChar.isMounted())
		{
			if(activeChar.isDead())
				activeChar.sendPacket(Msg.A_STRIDER_CANNOT_BE_RIDDEN_WHEN_DEAD);
			else if(pet.isDead())
				activeChar.sendPacket(Msg.A_DEAD_STRIDER_CANNOT_BE_RIDDEN);
			else if(activeChar.isInDuel() || activeChar.isInVehicle())
				activeChar.sendMessage("You can't mount because you don't meet the requirements.");
			else if(pet.isInCombat())
				activeChar.sendPacket(Msg.A_STRIDER_IN_BATTLE_CANNOT_BE_RIDDEN);
			else if(activeChar.isInCombat())
				activeChar.sendPacket(Msg.A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE);
			else if(activeChar.isSitting() || activeChar.isMoving)
				activeChar.sendPacket(Msg.A_STRIDER_CAN_BE_RIDDEN_ONLY_WHEN_STANDING);
			else if(activeChar.isFishing())
				activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			else if(activeChar.isCursedWeaponEquipped() || activeChar.isFlagEquipped())
				activeChar.sendMessage("You can't mount because you don't meet the requirements.");
			else if(activeChar.isCastingNow())
				activeChar.sendMessage("You can't mount because you don't meet the requirements.");
			else if(activeChar.isParalyzed())
				activeChar.sendMessage("You can't mount because you don't meet the requirements.");
			else if(activeChar.getDistance(pet) > 200.0)
				activeChar.sendMessage("You are too far away from your pet.");
			else if(!pet.isDead() && !activeChar.isMounted())
			{
				activeChar.setMount(pet.getTemplate().npcId, pet.getObjectId(), pet.getLevel());
				pet.unSummon();
			}
		}
		else if(activeChar.isMounted())
		{
			if(activeChar.isFlying() && !activeChar.checkLandingState())
			{
				activeChar.sendPacket(Msg.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION, Msg.ActionFail);
				return;
			}
			for(final Creature cha : World.getAroundCharacters(activeChar, 400, 400))
				if(cha.isDoor() && activeChar.getDistance(cha) < 200.0)
				{
					activeChar.sendMessage("You can't dismount near doors.");
					activeChar.sendActionFailed();
					return;
				}
			activeChar.setMount(0, 0, 0);
		}
	}

	private void UseSkill(final int skillId, final GameObject target)
	{
		final Player activeChar = getClient().getActiveChar();
		final Servitor pet = activeChar.getServitor();
		if(target == null || !target.isCreature() || pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		final HashMap<Integer, Skill> _skills = pet.getTemplate().getSkills();
		if(_skills.size() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		final Skill skill = _skills.get(skillId);
		if(skill == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getLevel() + 20 <= pet.getLevel())
		{
			activeChar.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			return;
		}
		if(skill.isOffensive() && skill.getTargetType() != Skill.SkillTargetType.TARGET_AURA && skill.getTargetType() != Skill.SkillTargetType.TARGET_MULTIFACE_AURA && (target == activeChar || target == pet))
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}
		pet.setTarget(target);
		final Creature aimingTarget = skill.getAimingTarget(pet, target);
		if(skill.checkCondition(pet, aimingTarget, _ctrlPressed, _shiftPressed, true))
			pet.getAI().Cast(skill, aimingTarget, _ctrlPressed, _shiftPressed);
		else
			activeChar.sendActionFailed();
	}

	static
	{
		RequestActionUse._log = LoggerFactory.getLogger(RequestActionUse.class);
	}
}
