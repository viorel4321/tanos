package l2s.gameserver.skills.effects;

import java.util.*;
import java.util.concurrent.Future;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.xml.holder.CubicHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.Env;
import l2s.gameserver.templates.CubicTemplate;

public class EffectCubic extends Abnormal
{
	private final CubicTemplate _template;
	private Future<?> _task;
	private long _reuse;

	public EffectCubic(final Env env, final EffectTemplate template)
	{
		super(env, template);
		_task = null;
		_reuse = 0L;
		_template = CubicHolder.getInstance().getTemplate(getTemplate()._cubicId, getTemplate()._cubicLevel);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		final Player player = _effected.getPlayer();
		if(player == null)
			return;
		player.addCubic(this);
		_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(), 1000L, 1000L);
		player.broadcastUserInfo(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		final Player player = _effected.getPlayer();
		if(player == null)
			return;
		player.removeCubic(getId());
		final Future<?> task = _task;
		if(task != null)
			task.cancel(true);
		_task = null;
		player.broadcastUserInfo(true);
	}

	public void doAction(final Player player)
	{
		if(_reuse > System.currentTimeMillis())
			return;
		boolean result = false;
		int chance = Rnd.get(100);
		for(final Map.Entry<Integer, List<CubicTemplate.SkillInfo>> entry : _template.getSkills())
			if((chance -= entry.getKey()) < 0)
			{
				for(final CubicTemplate.SkillInfo skillInfo : entry.getValue())
					switch(skillInfo.getActionType())
					{
						case ATTACK:
						{
							result = doAttack(player, skillInfo);
							continue;
						}
						case DEBUFF:
						{
							result = doDebuff(player, skillInfo);
							continue;
						}
						case HEAL:
						{
							result = doHeal(player, skillInfo);
							continue;
						}
						case CANCEL:
						{
							result = doCancel(player, skillInfo);
							continue;
						}
					}
				break;
			}
		if(result)
			_reuse = System.currentTimeMillis() + _template.getDelay() * 1000L;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	public int getId()
	{
		return _template.getId();
	}

	private static boolean doHeal(final Player player, final CubicTemplate.SkillInfo info)
	{
		final Skill skill = info.getSkill();
		Creature target = null;
		if(player.getParty() == null)
		{
			if(!player.isCurrentHpFull() && !player.isDead())
				target = player;
		}
		else
		{
			double currentHp = 2.147483647E9;
			for(final Player member : player.getParty().getPartyMembers())
			{
				if(member == null)
					continue;
				if(!player.isInRange(member, skill.getCastRange()) || member.isCurrentHpFull() || member.isDead() || member.getCurrentHp() >= currentHp)
					continue;
				currentHp = member.getCurrentHp();
				target = member;
			}
		}
		if(target == null)
			return false;
		final int chance = info.getChance((int) target.getCurrentHpPercents());
		if(!Rnd.chance(chance))
			return false;
		final Creature aimTarget = target;
		player.broadcastPacket(new MagicSkillUse(player, aimTarget, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		ThreadPoolManager.getInstance().schedule(() -> {
			final Set<Creature> targets = new HashSet<Creature>(1);
			targets.add(aimTarget);
			player.broadcastPacket(new MagicSkillLaunched(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets));
			player.callSkill(aimTarget, skill, targets, false);
		}, skill.getHitTime());
		return true;
	}

	private static boolean doAttack(final Player player, final CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
			return false;
		final Creature target = getTarget(player, info);
		if(target == null)
			return false;
		final Creature aimTarget = target;
		final Skill skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		ThreadPoolManager.getInstance().schedule(() -> {
			final Set<Creature> targets = new HashSet<>(1);
			targets.add(aimTarget);
			player.broadcastPacket(new MagicSkillLaunched(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets));
			player.callSkill(aimTarget, skill, targets, false);
			if(aimTarget.isNpc())
				if(aimTarget.paralizeOnAttack(player))
					player.paralizeMe((NpcInstance) aimTarget);
				else
				{
					final int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : (int) skill.getPower();
					aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, skill, damage);
				}
		}, skill.getHitTime());
		return true;
	}

	private static boolean doDebuff(final Player player, final CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
			return false;
		final Creature target = getTarget(player, info);
		if(target == null)
			return false;
		final Creature aimTarget = target;
		final Skill skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		ThreadPoolManager.getInstance().schedule(() -> {
			final Set<Creature> targets = new HashSet<>(1);
			targets.add(aimTarget);
			player.broadcastPacket(new MagicSkillLaunched(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets));
			player.callSkill(aimTarget, skill, targets, false);
		}, skill.getHitTime());
		return true;
	}

	private static boolean doCancel(final Player player, final CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
			return false;
		boolean hasDebuff = false;
		for(final Abnormal e : player.getAbnormalList().values())
			if(e.isOffensive() && e.getSkill().isCancelable() && !e.getTemplate()._applyOnCaster)
			{
				hasDebuff = true;
				break;
			}
		if(!hasDebuff)
			return false;
		final Skill skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, player, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
		ThreadPoolManager.getInstance().schedule(() -> {
			final Set<Creature> targets = new HashSet<>(1);
			targets.add(player);
			player.broadcastPacket(new MagicSkillLaunched(player.getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets));
			player.callSkill(player, skill, targets, false);
		}, skill.getHitTime());
		return true;
	}

	private static final Creature getTarget(final Player owner, final CubicTemplate.SkillInfo info)
	{
		if(!owner.isInCombat())
			return null;
		final GameObject object = owner.getTarget();
		if(object == null || !object.isCreature())
			return null;
		final Creature target = (Creature) object;
		if(target.isDead())
			return null;
		if(target.isDoor() && !info.isCanAttackDoor())
			return null;
		if(!owner.isInRangeZ(target, info.getSkill().getCastRange()))
			return null;
		final Player targetPlayer = target.getPlayer();
		if(targetPlayer != null && !targetPlayer.isInCombat())
			return null;
		if(!target.isAutoAttackable(owner))
			return null;
		return target;
	}

	private class ActionTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isActive())
				return;
			final Player player = EffectCubic.this._effected != null && EffectCubic.this._effected.isPlayer() ? (Player) EffectCubic.this._effected : null;
			if(player == null)
				return;
			doAction(player);
		}
	}
}
