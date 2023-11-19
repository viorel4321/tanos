package l2s.gameserver.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.UserInfo;
import l2s.gameserver.utils.AutoBan;
import l2s.gameserver.utils.Location;

public class GameObjectTasks
{
	static final Logger _log;

	static
	{
		_log = LoggerFactory.getLogger(GameObjectTasks.class);
	}

	public static class PvPFlagTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public PvPFlagTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;

			try
			{
				final long diff = Math.abs(System.currentTimeMillis() - player.getlastPvpAttack());
				if(diff > Config.PVP_TIME)
					player.stopPvPFlag();
				else if(diff > Config.PVP_TIME - 5000)
					player.updatePvPFlag(2);
				else
					player.updatePvPFlag(1);
			}
			catch(Exception e)
			{
				_log.error("error in pvp flag task:", e);
			}
		}
	}

	public static class BonusTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public BonusTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null || player.getNetConnection() == null)
				return;

			player.getNetConnection().setBonus(1.0f);
			player.getNetConnection().setBonusExpire(0);

			if(Config.ACCESS_WITH_PA_ONLY)
			{
				player.kick(false);
				return;
			}

			player.restoreBonus();
			if(player.getParty() != null)
				player.getParty().recalculatePartyData();
			final String msg = new CustomMessage("scripts.services.RateBonus.LuckEnded").toString(player);
			player.sendPacket(new ExShowScreenMessage(msg, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
			player.sendMessage(msg);
		}
	}

	public static class WaterTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public WaterTask(final Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;

			if(player.isDead() || !player.isInWater())
			{
				player.stopWaterTask();
				return;
			}
			final double reduceHp = player.getMaxHp() < 100 ? 1.0 : player.getMaxHp() / 100;
			player.reduceCurrentHp(reduceHp, player, null, 0, false, false, false, true, false, false, false, false);
			player.sendPacket(new SystemMessage(297).addNumber(Integer.valueOf((int) reduceHp)));
		}
	}

	public static class KickTask implements Runnable
	{
		private final HardReference<Player> _playerRef;
		private final boolean sc;

		public KickTask(final Player player, final boolean val)
		{
			_playerRef = player.getRef();
			sc = val;
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;

			player.setOfflineMode(false);
			player.kick(sc);
		}
	}

	public static class TeleportTask implements Runnable
	{
		private final HardReference<Player> _playerRef;
		private Location _loc;

		public TeleportTask(final Player player, final Location p)
		{
			_playerRef = player.getRef();
			_loc = p;
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;

			player.teleToLocation(_loc);
			player.unsetVar("jailed");
			player.unsetVar("jailedFrom");
		}
	}

	public static class UserInfoTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public UserInfoTask(final Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;

			player.sendPacket(new UserInfo(player));
			player._userInfoTask = null;
		}
	}

	public static class BroadcastCharInfoTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public BroadcastCharInfoTask(final Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;

			player.broadcastCharInfoImpl();
			player._broadcastCharInfoTask = null;
		}
	}

	public static class EndSitDownTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public EndSitDownTask(final Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;

			player.sittingTaskLaunched = false;
			player.getAI().clearNextAction();
		}
	}

	public static class EndStandUpTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public EndStandUpTask(final Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;

			player.setSitting(player.sittingTaskLaunched = false);
			if(!player.getAI().setNextIntention())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public static class InventoryEnableTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public InventoryEnableTask(final Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			final Player player = _playerRef.get();
			if(player == null)
				return;

			player._inventoryDisable = false;
		}
	}

	public static class AutoUseCP implements Runnable
	{
		private final int playerObjectId;

		public AutoUseCP(final int id)
		{
			playerObjectId = id;
		}

		@Override
		public void run()
		{
			final Player player = GameObjectsStorage.getPlayer(playerObjectId);
			if(player == null || player.isPotionsDisabled())
				return;

			if(player.getCurrentCpPercents() > player.percentCP)
			{
				if(player._useCP != null)
				{
					try
					{
						player._useCP.cancel(false);
					}
					catch(Exception ex)
					{}
					player._useCP = null;
				}
				player.skillCP = null;
				return;
			}
			if(player.inEvent && ArrayUtils.contains(Config.EVENT_RESTRICTED_ITEMS, 5592) || player.inLH && ArrayUtils.contains(Config.LH_RESTRICTED_ITEMS, 5592))
				return;
			if(player.skillCP != null && !player.isSkillDisabled(player.skillCP) && player.getInventory().getItemByItemId(5592) != null && player.skillCP.checkCondition(player, player, false, false, true))
				player.getAI().Cast(player.skillCP, player, false, false);
		}
	}

	public static class AutoUseHP implements Runnable
	{
		private final int playerObjectId;

		public AutoUseHP(final int id)
		{
			playerObjectId = id;
		}

		@Override
		public void run()
		{
			final Player player = GameObjectsStorage.getPlayer(playerObjectId);
			if(player == null || player.isPotionsDisabled())
				return;

			if(player.getCurrentHpPercents() > player.percentHP)
			{
				if(player._useHP != null)
				{
					try
					{
						player._useHP.cancel(false);
					}
					catch(Exception ex)
					{}
					player._useHP = null;
				}
				player.skillHP = null;
				return;
			}
			if(player.inEvent && ArrayUtils.contains(Config.EVENT_RESTRICTED_ITEMS, 1539) || player.inLH && ArrayUtils.contains(Config.LH_RESTRICTED_ITEMS, 1539))
				return;
			if(player.skillHP != null && !player.isSkillDisabled(player.skillHP) && player.getInventory().getItemByItemId(1539) != null && player.skillHP.checkCondition(player, player, false, false, true))
				player.getAI().Cast(player.skillHP, player, false, false);
		}
	}

	public static class AutoUseMP implements Runnable
	{
		private final int playerObjectId;

		public AutoUseMP(final int id)
		{
			playerObjectId = id;
		}

		@Override
		public void run()
		{
			final Player player = GameObjectsStorage.getPlayer(playerObjectId);
			if(player == null || player.isPotionsDisabled())
				return;

			if(player.getCurrentMpPercents() > player.percentMP)
			{
				if(player._useMP != null)
				{
					try
					{
						player._useMP.cancel(false);
					}
					catch(Exception ex)
					{}
					player._useMP = null;
				}
				player.skillMP = null;
				return;
			}
			if(player.inEvent && ArrayUtils.contains(Config.EVENT_RESTRICTED_ITEMS, 728) || player.inLH && ArrayUtils.contains(Config.LH_RESTRICTED_ITEMS, 728))
				return;
			if(player.skillMP != null && !player.isSkillDisabled(player.skillMP) && player.getInventory().getItemByItemId(728) != null && player.skillMP.checkCondition(player, player, false, false, true))
				player.getAI().Cast(player.skillMP, player, false, false);
		}
	}

	public static class AltMagicUseTask implements Runnable
	{
		private final HardReference<? extends Creature> _casterRef;
		private final HardReference<? extends Creature> _targetRef;
		private final Skill _skill;

		public AltMagicUseTask(Creature character, Creature target, Skill skill)
		{
			_casterRef = character.getRef();
			_targetRef = target.getRef();
			_skill = skill;
		}

		@Override
		public void run()
		{
			Creature caster = _casterRef.get();
			if(caster == null)
				return;

			Creature target = _targetRef.get();
			if(target == null)
				return;

			caster.altOnMagicUseTimer(target, _skill);
		}
	}

	/** CastEndTimeTask */
	public static class CastEndTimeTask implements Runnable
	{
		private final HardReference<? extends Creature> _charRef;
		private final HardReference<? extends Creature> _aimingTargetRef;
		private final Set<Creature> _targets;

		public CastEndTimeTask(Creature character, Creature aimingTarget, Set<Creature> targets)
		{
			_charRef = character.getRef();
			_aimingTargetRef = aimingTarget.getRef();
			_targets = targets;
		}

		@Override
		public void run()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			character.onCastEndTime(_aimingTargetRef.get(), _targets, true);
		}
	}

	public static class HitTask implements Runnable
	{
		final boolean _crit;
		final boolean _miss;
		final boolean _shld;
		final boolean _soulshot;
		final boolean _unchargeSS;
		final boolean _notify;
		final int _sAtk;
		final int _damage;
		final int _poleHitCount;
		private final HardReference<? extends Creature> _charRef;
		private final HardReference<? extends Creature> _targetRef;

		public HitTask(final Creature cha, final Creature target, final int damage, final int poleHitCount, final boolean crit, final boolean miss, final boolean soulshot, final boolean shld, final boolean unchargeSS, final boolean notify, final int sAtk)
		{
			_charRef = cha.getRef();
			_targetRef = target.getRef();
			_damage = damage;
			_poleHitCount = poleHitCount;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
			_unchargeSS = unchargeSS;
			_notify = notify;
			_sAtk = sAtk;
		}

		@Override
		public void run()
		{
			final Creature character;
			final Creature target;
			if((character = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;
			if(character.isAttackAborted())
				return;
			character.onHitTimer(target, _damage, _poleHitCount, _crit, _miss, _soulshot, _shld, _unchargeSS);
			if(_notify)
				ThreadPoolManager.getInstance().schedule(new NotifyAITask(character, CtrlEvent.EVT_READY_TO_ACT), _sAtk / 2);
		}
	}

	public static class MagicUseTask implements Runnable
	{
		private final HardReference<? extends Creature> _creatureRef;
		private boolean _forceUse;

		public MagicUseTask(final Creature cha, final boolean forceUse)
		{
			_creatureRef = cha.getRef();
			_forceUse = forceUse;
		}

		@Override
		public void run()
		{
			Creature character = _creatureRef.get();
			if(character == null)
				return;

			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}

			character.onMagicUseTimer(castingTarget, castingSkill, _forceUse);
		}
	}

	public static class MagicCheck implements Runnable
	{
		private final HardReference<? extends Creature> _charRef;
		private final int _range;

		public MagicCheck(final Creature cha, final int range)
		{
			_charRef = cha.getRef();
			_range = range;
		}

		@Override
		public void run()
		{
			final Creature character = _charRef.get();
			if(character == null)
				return;
			final Creature target = character.getCastingTarget();
			if(target == null)
				return;
			boolean abort = false;
			if(!character.isInRange(target, _range))
			{
				character.sendPacket(new SystemMessage(22));
				abort = true;
			}
			else if(!GeoEngine.canSeeTarget(character, target))
			{
				if (character.getDistance(target) > character.getMagicalAttackRange(character.getCastingSkill())) {
					if(!target.isDoor()) {
						character.sendPacket(new SystemMessage(181));
						abort = true;
					}
				}
			}
			if(abort)
				character.abortCast(true, false);
		}
	}

	public static class MagicLaunchedTask implements Runnable
	{
		private final HardReference<? extends Creature> _creatureRef;
		public boolean _forceUse;

		public MagicLaunchedTask(final Creature cha, final boolean forceUse)
		{
			_creatureRef = cha.getRef();
			_forceUse = forceUse;
		}

		@Override
		public void run()
		{
			final Creature character = _creatureRef.get();
			if(character == null)
				return;

			final Skill castingSkill = character.getCastingSkill();
			final Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			final Set<Creature> targets = castingSkill.getTargets(character, castingTarget, _forceUse);
			character.broadcastPacket(new MagicSkillLaunched(character.getObjectId(), castingSkill.getDisplayId(), castingSkill.getDisplayLevel(), targets));
		}
	}

	public static class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;
		private final Object _arg0;
		private final Object _arg1;
		private final Object _arg2;
		private final HardReference<? extends Creature> _charRef;

		public NotifyAITask(final Creature cha, final CtrlEvent evt, final Object arg0, final Object arg1, final Object arg2)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_arg0 = arg0;
			_arg1 = arg1;
			_arg2 = arg2;
		}

		public NotifyAITask(final Creature cha, final CtrlEvent evt, final Object arg0, final Object arg1)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_arg0 = arg0;
			_arg1 = arg1;
			_arg2 = null;
		}

		public NotifyAITask(final Creature cha, final CtrlEvent evt)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_arg0 = null;
			_arg1 = null;
			_arg2 = null;
		}

		@Override
		public void run()
		{
			final Creature character = _charRef.get();
			if(character == null || !character.hasAI())
				return;
			character.getAI().notifyEvent(_evt, _arg0, _arg1, _arg2);
		}
	}

	public static class DeleteTask implements Runnable
	{
		private final HardReference<NpcInstance> _npcRef;

		public DeleteTask(NpcInstance n)
		{
			_npcRef = n.getRef();
		}

		@Override
		public void run()
		{
			NpcInstance n = _npcRef.get();
			if(n != null)
				n.deleteMe();
		}
	}

	public static class RaidCurseTask implements Runnable
	{
		private final HardReference<? extends Playable> _playableRef;
		private final HardReference<NpcInstance> _npcRef;
		private final Skill _skill;

		public RaidCurseTask(Playable p, NpcInstance n, Skill skill)
		{
			_playableRef = p.getRef();
			_npcRef = n.getRef();
			_skill = skill;
		}

		@Override
		public void run()
		{
			Playable pl = _playableRef.get();
			if(pl == null || pl.isDead())
				return;

			NpcInstance npc = _npcRef.get();
			if(npc == null || npc.isDead())
				return;

			try
			{
				if(npc.isInRange(pl.getLoc(), _skill.getEffectRange()))
				{
					npc.broadcastPacket(new MagicSkillUse(npc, pl, _skill.getId(), 1, 500, 0L));
					_skill.getEffects(pl, pl, false, false);
				}
			}
			catch(Exception e)
			{
				_log.error("error in RaidCurseTask for sillId " + _skill.getId() + ":", e);
			}
			pl._rc = false;
		}
	}

	public static class BanHwidTask implements Runnable
	{
		private final int _id;
		private final String _name;
		private final String _hwid;
		private final String _reason;
		private final String _gm;
		private final long _time;

		public BanHwidTask(final int id, final String name, final String hwid, final String reason, final long time, final String gm)
		{
			_id = id;
			_name = name;
			_hwid = hwid;
			_reason = reason;
			_time = time;
			_gm = gm;
		}

		@Override
		public void run()
		{
			AutoBan.addHwidBan(_name, _hwid, _reason, _time, _gm);

			Player player = GameObjectsStorage.getPlayer(_id);
			if(player == null)
				return;

			if(player.isInOfflineMode())
				player.setOfflineMode(false);
			player.kick(true);
		}
	}
}
