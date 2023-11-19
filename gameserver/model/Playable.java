package l2s.gameserver.model;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.Revive;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;

public abstract class Playable extends Creature
{
	private static final long serialVersionUID = 1L;
	private AtomicState _isSilentMoving;
	private boolean _isPendingRevive;
	private List<QuestState> _NotifyQuestOfDeathList;
	private List<QuestState> _NotifyQuestOfPlayerKillList;
	public boolean PvPFlagDead;
	public boolean _rc;
	public boolean _salva;
	public List<String> _resEffs;

	public Playable(final int objectId, final CreatureTemplate template)
	{
		super(objectId, template);
		_isSilentMoving = new AtomicState();
		_isPendingRevive = false;
		PvPFlagDead = false;
		_rc = false;
		_salva = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<? extends Playable> getRef()
	{
		return (HardReference<Playable>) super.getRef();
	}

	@Override
	public boolean isAttackable(final Creature attacker)
	{
		return isAttackable(attacker, true, false);
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return isAttackable(attacker, false, false);
	}

	public boolean isAttackable(final Creature attacker, final boolean force, final boolean nextAttackCheck)
	{
		final Player player = getPlayer();
		if(attacker == null || player == null || attacker == this || attacker == player && !force || isDead() || attacker.isAlikeDead() || isFakeDeath() && !attacker.isPlayable())
			return false;
		if(isInvisible() || isInVehicle() || getReflectionId() != attacker.getReflectionId())
			return false;
		final Player pcAttacker = attacker.getPlayer();
		if(isPlayer() && pcAttacker == this)
			return false;
		if(pcAttacker == null || pcAttacker == player)
			return true;
		if(pcAttacker.isInVehicle())
			return false;
		if(pcAttacker.isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && pcAttacker.getLevel() < 21)
			return false;
		if(Config.CHECK_EPIC_CAN_DAMAGE && player.isInZone(Zone.ZoneType.epic) != pcAttacker.isInZone(Zone.ZoneType.epic))
			return false;
		if((player.isInOlympiadMode() || pcAttacker.isInOlympiadMode()) && player.getOlympiadGameId() != pcAttacker.getOlympiadGameId())
			return false;
		if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
			return false;
		if(player.isInDuel() && pcAttacker.isInDuel() && player.getEvent(DuelEvent.class) == pcAttacker.getEvent(DuelEvent.class) && player.getTeam() != pcAttacker.getTeam())
			return true;
		if(pcAttacker.getTeam() > 0 && pcAttacker.isChecksForTeam() && player.getTeam() == 0)
			return false;
		if(player.getTeam() > 0 && player.isChecksForTeam() && pcAttacker.getTeam() == 0)
			return false;
		if(player.getTeam() > 0 && player.isChecksForTeam() && pcAttacker.getTeam() > 0 && pcAttacker.isChecksForTeam() && player.getTeam() == pcAttacker.getTeam())
			return false;
		if(!force && (player.isInParty() && (player.getParty() == pcAttacker.getParty() || player.getParty().getCommandChannel() != null && pcAttacker.isInParty() && player.getParty().getCommandChannel() == pcAttacker.getParty().getCommandChannel()) || player.getTeam() == 0 && !player.isInOlympiadMode() && (player.getClanId() != 0 && player.getClanId() == pcAttacker.getClanId() || player.getAllyId() != 0 && pcAttacker.getAllyId() == player.getAllyId())))
			return false;
		for(final GlobalEvent e : attacker.getEvents())
			if(e.checkForAttack(this, attacker, null, force) != null)
				return false;
		if(isInZoneBattle())
			return true;
		if(isInZonePeace())
			return false;
		for(final GlobalEvent e : attacker.getEvents())
			if(e.canAttack(this, attacker, null, force, nextAttackCheck))
				return true;
		if(isInZone(Zone.ZoneType.Siege))
			return true;
		if(pcAttacker.atMutualWarWith(player))
			return true;
		if(player.getKarma() > 0)
			return true;
		if(player.getPvpFlag() > 0)
			return !nextAttackCheck;
		return force;
	}

	public abstract Inventory getInventory();

	@Override
	public boolean checkPvP(final Creature target, final Skill skill)
	{
		final Player player = getPlayer();
		if(isDead() || target == null || player == null || target == this || target == player || target == player.getServitor() || player.getKarma() > 0)
			return false;
		if(skill != null)
		{
			if(skill.getTargetType() == Skill.SkillTargetType.TARGET_UNLOCKABLE)
				return false;
			if(skill.getTargetType() == Skill.SkillTargetType.TARGET_CHEST)
				return false;
		}
		final DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && duelEvent == target.getEvent(DuelEvent.class))
			return false;
		if(isInZonePeace() && target.isInZonePeace())
			return false;
		if(isInZoneBattle() && target.isInZoneBattle())
			return false;
		if(isInOlympiadMode() && target.isInOlympiadMode())
			return false;
		if(skill == null || skill.isOffensive())
		{
			if(target.getKarma() > 0)
				return false;
			if(target.isPlayable())
				return true;
		}
		else if(target.getPvpFlag() > 0 || target.getKarma() > 0 || target.isMonster())
		{
			if(skill != null && skill.getSkillType() == Skill.SkillType.RESURRECT)
				PvPFlagDead = true;
			return true;
		}
		return false;
	}

	public boolean checkAttack(final Creature target)
	{
		final Player player = getPlayer();
		if(player == null)
			return false;
		if(target == null || target.isDead())
		{
			player.sendPacket(Msg.INCORRECT_TARGET);
			return false;
		}
		if(!isInRange(target, 2000L))
		{
			player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		if(target.isDoor() && !((DoorInstance) target).isAttackable(this))
		{
			player.sendPacket(Msg.INCORRECT_TARGET);
			return false;
		}
		if(target.isInvisible() || getReflectionId() != target.getReflectionId() || !GeoEngine.canSeeTarget(this, target))
		{
			if (!target.isDoor()) {
				player.sendPacket(Msg.CANNOT_SEE_TARGET);
				return false;
			}
		}
		if(Config.CHECK_EPIC_CAN_DAMAGE && player.isInZone(Zone.ZoneType.epic) != target.isInZone(Zone.ZoneType.epic))
		{
			player.sendPacket(Msg.INCORRECT_TARGET);
			return false;
		}
		if(Config.ALLOW_WINGS_MOD && target.isPlayable() && !target.isAutoAttackable(player))
		{
			final ItemInstance item = player.getInventory().getPaperdollItem(16);
			if(item != null && item.getItemId() == 9585)
			{
				player.sendPacket(Msg.INCORRECT_TARGET);
				return false;
			}
		}
		if(target.isNpc())
		{
			if(((NpcInstance) target).getFactionId().equalsIgnoreCase("varka_silenos_clan") && player.getVarka() > 0 || ((NpcInstance) target).getFactionId().equalsIgnoreCase("ketra_orc_clan") && player.getKetra() > 0)
			{
				final Skill revengeSkill = SkillTable.getInstance().getInfo(4578, 1);
				revengeSkill.getEffects(target, this, false, false);
				return false;
			}
			if(Config.NO_DAMAGE_NPC && !target.isDmg())
				return false;
		}
		if(target.isPlayable())
		{
			if(target.isPlayer() && target.getPlayer().inObserverMode())
				return false;
			if(player.isInOlympiadMode())
			{
				if(target.getPlayer() == null)
					return false;
				if(!target.getPlayer().isInOlympiadMode())
					return false;
				if(player.getOlympiadGameId() != target.getPlayer().getOlympiadGameId())
					return false;
			}
			if(isInZonePeace() || target.isInZonePeace())
			{
				player.sendPacket(Msg.YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE);
				return false;
			}
		}
		return true;
	}

	@Override
	public void doAttack(final Creature target)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		if(isAttackingNow())
		{
			player.sendActionFailed();
			return;
		}
		if(player.inObserverMode())
		{
			player.sendMessage(new CustomMessage("l2s.gameserver.model.Playable.OutOfControl.ObserverNoAttack"));
			return;
		}
		if(!checkAttack(target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			player.sendActionFailed();
			return;
		}
		final DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
			duelEvent.abortDuel(getPlayer());
		final WeaponTemplate weaponItem = getActiveWeaponItem();
		if(weaponItem != null && weaponItem.getItemType() == WeaponTemplate.WeaponType.BOW)
		{
			double bowMpConsume = weaponItem.getMpConsume();
			if(bowMpConsume > 0.0)
			{
				final double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0.0, target, null);
				if(chance > 0.0 && Rnd.chance(chance))
					bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);
				if(_currentMp < bowMpConsume)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					player.sendPacket(Msg.NOT_ENOUGH_MP);
					player.sendActionFailed();
					return;
				}
				reduceCurrentMp(bowMpConsume, null);
			}
			if(!player.checkAndEquipArrows())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				player.sendPacket(Msg.NOT_ENOUGH_ARROWS);
				player.sendActionFailed();
				return;
			}
		}
		super.doAttack(target);
	}

	public void addNotifyQuestOfDeath(final QuestState qs)
	{
		if(qs == null || _NotifyQuestOfDeathList != null && _NotifyQuestOfDeathList.contains(qs))
			return;
		if(_NotifyQuestOfDeathList == null)
			_NotifyQuestOfDeathList = new CopyOnWriteArrayList<QuestState>();
		_NotifyQuestOfDeathList.add(qs);
	}

	public void addNotifyOfPlayerKill(final QuestState qs)
	{
		if(qs == null || _NotifyQuestOfPlayerKillList != null && _NotifyQuestOfPlayerKillList.contains(qs))
			return;
		if(_NotifyQuestOfPlayerKillList == null)
			_NotifyQuestOfPlayerKillList = new CopyOnWriteArrayList<QuestState>();
		_NotifyQuestOfPlayerKillList.add(qs);
	}

	public void removeNotifyOfPlayerKill(final QuestState qs)
	{
		if(qs == null || _NotifyQuestOfPlayerKillList == null)
			return;
		_NotifyQuestOfPlayerKillList.remove(qs);
		if(_NotifyQuestOfPlayerKillList.isEmpty())
			_NotifyQuestOfPlayerKillList = null;
	}

	public List<QuestState> getNotifyOfPlayerKillList()
	{
		return _NotifyQuestOfPlayerKillList;
	}

	@Override
	protected void onDeath(final Creature killer)
	{
		super.onDeath(killer);
		if(killer != null)
		{
			final Player pk = killer.getPlayer();
			final Player player = getPlayer();
			if(pk != null && player != null)
			{
				final Party party = pk.getParty();
				if(party == null)
				{
					final List<QuestState> killList = pk.getNotifyOfPlayerKillList();
					if(killList != null)
						for(final QuestState qs : killList)
							qs.getQuest().notifyPlayerKill(player, qs);
				}
				else
					for(final Player member : party.getPartyMembers())
						if(member != null && member.isInRange(pk, 2000L))
						{
							final List<QuestState> killList2 = member.getNotifyOfPlayerKillList();
							if(killList2 == null)
								continue;
							for(final QuestState qs2 : killList2)
								qs2.getQuest().notifyPlayerKill(player, qs2);
						}
			}
		}
		if(_NotifyQuestOfDeathList != null)
		{
			for(final QuestState qs3 : _NotifyQuestOfDeathList)
				qs3.getQuest().notifyDeath(killer, this, qs3);
			_NotifyQuestOfDeathList = null;
		}
	}

	@Override
	public int getPAtkSpd()
	{
		int val = Math.max((int) calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, getTemplate().basePAtkSpd, null, null), null, null), 1);
		if(Config.LIM_PATK_SPD != 0 && val > Config.LIM_PATK_SPD)
			val = Config.LIM_PATK_SPD;
		return val;
	}

	@Override
	public int getKarma()
	{
		final Player player = getPlayer();
		return player == null ? 0 : player.getKarma();
	}

	@Override
	public void callSkill(Creature aimingTarget, Skill skill, Set<Creature> targets, boolean useActionSkills)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		if(useActionSkills && !skill.altUse() && !skill.isToggle() && !skill.getSkillType().equals(Skill.SkillType.BEAST_FEED))
			for(final Creature target : targets)
			{
				if(target.isNpc())
				{
					if(skill.isOffensive())
					{
						if(((NpcInstance) target).getFactionId().equalsIgnoreCase("varka_silenos_clan") && player.getVarka() > 0 || ((NpcInstance) target).getFactionId().equalsIgnoreCase("ketra_orc_clan") && player.getKetra() > 0)
						{
							final Skill revengeSkill = SkillTable.getInstance().getInfo(4578, 1);
							revengeSkill.getEffects(target, this, false, false);
							return;
						}
						if(target.paralizeOnAttack(player))
							paralizeMe((NpcInstance) target);
						else if(!skill.isAI() && (!target.isBox() || !skill.isBox()))
						{
							final int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : 1;
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, skill, damage);
						}
					}
					target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
				}
				else if(target.isPlayable() && target != getServitor() && (!isSummon() || target != player))
				{
					final DuelEvent duelEvent = target.getEvent(DuelEvent.class);
					if(duelEvent != null && getEvent(DuelEvent.class) != duelEvent)
						duelEvent.abortDuel(target.getPlayer());
					final int aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());
					final List<NpcInstance> npcs = World.getAroundNpc(target);
					for(final NpcInstance npc : npcs)
						if(!npc.isDead())
						{
							if(!npc.isInRange(this, Config.NPC_SEE_SPELL_RANGE))
								continue;
							npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
							final AggroList.AggroInfo ai = npc.getAggroList().get(target);
							if(ai == null)
								continue;
							if(!skill.isHandler() && npc.paralizeOnAttack(player))
								muteMe(npc);
							if(ai.hate < 100)
								continue;
							if(!GeoEngine.canSeeTarget(npc, target))
								continue;
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, ai.damage == 0 ? aggro / 2 : aggro);
						}
				}
				if(checkPvP(target, skill))
					startPvPFlag(target);
			}
		super.callSkill(aimingTarget, skill, targets, useActionSkills);
	}

	public void broadcastPickUpMsg(final ItemInstance item)
	{
		if(Config.NO_PICK_UP_MSG)
			return;
		final Player player = getPlayer();
		if(item == null || player == null || player.isInvisible())
			return;
		if(item.isEquipable() && !(item.getTemplate() instanceof EtcItemTemplate) && !item.isCursed())
		{
			SystemMessage msg = null;
			final String player_name = player.getName();
			if(item.getEnchantLevel() > 0)
			{
				final int msg_id = isPlayer() ? 1534 : 1536;
				msg = new SystemMessage(msg_id).addString(player_name).addNumber(Integer.valueOf(item.getEnchantLevel())).addItemName(Integer.valueOf(item.getItemId()));
			}
			else
			{
				final int msg_id = isPlayer() ? 1533 : 1536;
				msg = new SystemMessage(msg_id).addString(player_name).addItemName(Integer.valueOf(item.getItemId()));
			}
			player.broadcastPacket(msg);
		}
	}

	public void paralizeMe(final NpcInstance effector)
	{
		if(_rc)
			return;
		_rc = true;
		final Skill sk = SkillTable.getInstance().getInfo(4515, 1);
		if(effector.isInRange(this, sk.getCastRange()))
		{
			if(effector.getAggroList().getHateList(1500).isEmpty())
				effector.turn(this, 10000);
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.RaidCurseTask(this, effector, sk), 500L);
		}
		else
			_rc = false;
	}

	public void muteMe(final NpcInstance effector)
	{
		if(_rc)
			return;
		_rc = true;
		final Skill sk = SkillTable.getInstance().getInfo(4215, 1);
		if(effector.isInRange(this, sk.getCastRange()))
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.RaidCurseTask(this, effector, sk), 1500L);
		else
			_rc = false;
	}

	public final void setPendingRevive(final boolean value)
	{
		_isPendingRevive = value;
	}

	@Override
	public boolean isPendingRevive()
	{
		return _isPendingRevive;
	}

	public void cancelSalva()
	{
		if(_salva && Config.ALT_SALVATION)
			for(final Abnormal e : getAbnormalList().values())
				if(e.getSkill().isSalvation())
					e.exit();
		_salva = false;
	}

	public void doRevive(final boolean absolute)
	{
		if(!isTeleporting())
		{
			setPendingRevive(false);
			if(absolute)
				broadcastPacket(new Revive(this));
			if(isSalvation())
			{
				if(!Config.ALT_SALVATION || isSummon())
					for(final Abnormal e : getAbnormalList().values())
						if(e.getSkill().isSalvation())
							e.exit();
				if(_salva || isSummon() || isPlayer() && ((Player) this).inEvent)
				{
					setCurrentHp(getMaxHp(), true);
					setCurrentMp(getMaxMp());
					if(isPlayer())
						setCurrentCp(getMaxCp());
				}
				else
				{
					setCurrentHp(Math.max(1.0, getMaxHp() * Config.RESPAWN_RESTORE_HP), true);
					if(Config.RESPAWN_RESTORE_MP >= 0.0)
						setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
					if(isPlayer() && Config.RESPAWN_RESTORE_CP >= 0.0)
						setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				}
			}
			else if(isPlayer() && ((Player) this).inEvent)
			{
				setCurrentHp(getMaxHp(), true);
				setCurrentMp(getMaxMp());
				setCurrentCp(getMaxCp());
			}
			else
			{
				setCurrentHp(Math.max(1.0, getMaxHp() * Config.RESPAWN_RESTORE_HP), false);
				if(Config.RESPAWN_RESTORE_MP >= 0.0)
					setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
				if(isPlayer() && Config.RESPAWN_RESTORE_CP >= 0.0)
					setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
			}
		}
		else
			setPendingRevive(true);
	}

	public void sitDown(final int throne)
	{}

	public void standUp()
	{}

	public boolean startSilentMoving()
	{
		return _isSilentMoving.getAndSet(true);
	}

	public boolean stopSilentMoving()
	{
		return _isSilentMoving.setAndGet(false);
	}

	public boolean isSilentMoving()
	{
		return _isSilentMoving.get();
	}

	@Override
	public boolean isSalvation()
	{
		return calcStat(Stats.SALVATION, 0.0) > 0.0;
	}

	@Override
	public boolean isBlessedByNoblesse()
	{
		return calcStat(Stats.BLESS_NOBLESSE, 0.0) > 0.0 || Config.SAVE_EFFECTS_AFTER_DEATH || Config.ALLOW_PVP_ZONES_MOD && ArrayUtils.contains(Config.PVP_ZONES_MOD, getZoneIndex(Zone.ZoneType.battle_zone));
	}

	@Override
	public boolean isPlayable()
	{
		return true;
	}

	@Override
	public boolean isDmg()
	{
		return true;
	}

	public boolean useItem(ItemInstance item, boolean ctrl, boolean sendMsg)
	{
		return false;
	}
}
