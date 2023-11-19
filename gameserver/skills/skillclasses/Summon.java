package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.funcs.FuncAdd;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class Summon extends Skill
{
	private final SummonType _summonType;
	private final float _expPenalty;
	private final int _itemConsumeIdInTime;
	private final int _itemConsumeCountInTime;
	private final int _itemConsumeDelay;
	private final int _lifeTime;

	public Summon(final StatsSet set)
	{
		super(set);
		_summonType = Enum.valueOf(SummonType.class, set.getString("summonType", "PET").toUpperCase());
		_expPenalty = set.getFloat("expPenalty", 0.0f);
		_itemConsumeIdInTime = set.getInteger("itemConsumeIdInTime", 0);
		_itemConsumeCountInTime = set.getInteger("itemConsumeCountInTime", 0);
		_itemConsumeDelay = set.getInteger("itemConsumeDelay", 250) * 1000;
		_lifeTime = set.getInteger("lifeTime", 1200) * 1000;
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		final Player player = activeChar.getPlayer();
		if(player == null)
			return false;
		switch(_summonType)
		{
			case PET:
			case SIEGE_SUMMON:
			{
				if(player.getServitor() != null || player.isMounted())
				{
					player.sendPacket(new SystemMessage(543));
					return false;
				}
				break;
			}
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature caster, final Set<Creature> targets)
	{
		final Player activeChar = caster.getPlayer();
		if(getNpcId() == 0)
		{
			caster.sendMessage("Summon skill " + getId() + " not described yet");
			return;
		}
		switch(_summonType)
		{
			case PET:
			case SIEGE_SUMMON:
			{
				Location loc = null;
				if(_targetType == SkillTargetType.TARGET_CORPSE)
					for(final Creature target : targets)
						if(target != null && target.isDead() && target.isNpc())
						{
							((NpcInstance) target).endDecayTask();
							loc = target.getLoc();
						}
				if(activeChar.getServitor() != null || activeChar.isMounted())
					return;
				final NpcTemplate summonTemplate = NpcTable.getTemplate(getNpcId());
				final SummonInstance summon = new SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay);
				summon.setTitle(Servitor.TITLE_BY_OWNER_NAME);
				summon.setExpPenalty(_expPenalty);
				summon.setExp(Experience.LEVEL[summon.getLevel()]);
				summon.setCurrentHp(summon.getMaxHp(), false);
				summon.setCurrentMp(summon.getMaxMp());
				summon.setHeading(activeChar.getHeading());
				summon.setRunning();
				activeChar.setServitor(summon);
				summon.setReflectionId(activeChar.getReflectionId());
				summon.setIsInOlympiadMode(activeChar.isInOlympiadMode());
				summon.spawnMe(loc == null ? Location.findAroundPosition(activeChar.getLoc(), 100, 150, activeChar.getGeoIndex()) : loc);
				if(summon.getSkillLevel(4140) > 0)
					summon.altUseSkill(SkillTable.getInstance().getInfo(4140, summon.getSkillLevel(4140)), activeChar);
				if(summon.getName().equalsIgnoreCase("Shadow"))
					summon.addStatFunc(new FuncAdd(Stats.ABSORB_DAMAGE_PERCENT, 64, this, 15.0));
				summon.setFollowStatus(true, true);
				summon.broadcastPetInfo();
				if(_summonType == SummonType.SIEGE_SUMMON)
				{
					final SiegeEvent<?, ?> siegeEvent = activeChar.getEvent(SiegeEvent.class);
					siegeEvent.addSiegeSummon(summon);
					break;
				}
				break;
			}
		}
		if(isSSPossible())
			caster.unChargeShots(isMagic());
	}

	@Override
	public boolean isOffensive()
	{
		return _targetType == SkillTargetType.TARGET_CORPSE;
	}

	private enum SummonType
	{
		PET,
		SIEGE_SUMMON;
	}
}
