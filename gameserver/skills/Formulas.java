package l2s.gameserver.skills;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import l2s.gameserver.utils.PositionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.SkillTrait;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.conditions.ConditionPlayerState;
import l2s.gameserver.skills.funcs.Func;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Util;

public class Formulas
{
	protected static final Logger _log;
	public static int MAX_STAT_VALUE;
	public static final double[] WITbonus;
	public static final double[] MENbonus;
	public static final double[] INTbonus;
	public static final double[] STRbonus;
	public static final double[] DEXbonus;
	public static final double[] CONbonus;

	public static void addFuncsToNewCharacter(final Creature cha)
	{
		if(cha.isPlayer())
		{
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMaxCpMul.func);
			cha.addStatFunc(FuncMaxHpMul.func);
			cha.addStatFunc(FuncMaxMpMul.func);
			cha.addStatFunc(FuncAttackRange.func);
			cha.addStatFunc(FuncMoveSpeed.func);
			cha.addStatFunc(FuncHennaSTR.func);
			cha.addStatFunc(FuncHennaDEX.func);
			cha.addStatFunc(FuncHennaINT.func);
			cha.addStatFunc(FuncHennaMEN.func);
			cha.addStatFunc(FuncHennaCON.func);
			cha.addStatFunc(FuncHennaWIT.func);
			cha.addStatFunc(FuncInventory.func);
			cha.addStatFunc(FuncWarehouse.func);
			cha.addStatFunc(FuncTradeLimit.func);
			cha.addStatFunc(FuncSDefPlayers.func);
			cha.addStatFunc(FuncMaxHpLimit.func);
			cha.addStatFunc(FuncMaxMpLimit.func);
			cha.addStatFunc(FuncMaxCpLimit.func);
			cha.addStatFunc(FuncPAtkMul.func);
			cha.addStatFunc(FuncMAtkMul.func);
			cha.addStatFunc(FuncPDefMul.func);
			cha.addStatFunc(FuncMDefMul.func);
			cha.addStatFunc(FuncPAtkSpeedMul.func);
			cha.addStatFunc(FuncMAtkSpeedMul.func);
		}
		if(!cha.isSummon())
		{
			cha.addStatFunc(FuncSDefInit.func);
			cha.addStatFunc(FuncSDefAll.func);
		}
		cha.addStatFunc(FuncCritLimit.func);
		cha.addStatFunc(FuncPCriticalRateMul.func);
		cha.addStatFunc(FuncMCriticalRateMul.func);
		cha.addStatFunc(FuncAccuracyAdd.func);
		cha.addStatFunc(FuncEvasionAdd.func);
		cha.addStatFunc(FuncPDamageResists.func);
		cha.addStatFunc(FuncMDamageResists.func);
	}

	public static double calcHpRegen(final Creature cha)
	{
		double init;
		if(cha.isPlayer())
			init = (cha.getLevel() <= 10 ? 1.95 + cha.getLevel() / 20.0 : 1.4 + cha.getLevel() / 10.0) * cha.getLevelMod() * Formulas.CONbonus[cha.getCON()];
		else
			init = cha.getTemplate().baseHpReg * Formulas.CONbonus[cha.getCON()];
		if(cha.isPlayable())
		{
			final Player player = cha.getPlayer();
			if(player != null && player.getClan() != null && player.getInResidence() != ResidenceType.None)
				switch(player.getInResidence())
				{
					case ClanHall:
					{
						final int clanHallIndex = player.getClan().getHasHideout();
						if(clanHallIndex > 0)
						{
							final ClanHall clansHall = ResidenceHolder.getInstance().getResidence(ClanHall.class, clanHallIndex);
							if(clansHall != null && clansHall.isFunctionActive(3))
								init *= 1.0 + clansHall.getFunction(3).getLevel() / 100.0;
							break;
						}
						break;
					}
					case Castle:
					{
						final int caslteIndex = player.getClan().getHasCastle();
						if(caslteIndex <= 0)
							break;
						final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, caslteIndex);
						if(castle != null && castle.isFunctionActive(3))
						{
							init *= 1.0 + castle.getFunction(3).getLevel() / 100.0;
							break;
						}
						break;
					}
				}
		}
		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}

	public static double calcMpRegen(final Creature cha)
	{
		double init;
		if(cha.isPlayer())
			init = (0.87 + cha.getLevel() * 0.03) * cha.getLevelMod() * Formulas.MENbonus[cha.getMEN()];
		else
			init = cha.getTemplate().baseMpReg * Formulas.MENbonus[cha.getMEN()];
		if(cha.isSummon())
			init *= 2.0;
		if(cha.isPlayable())
		{
			final Player player = cha.getPlayer();
			if(player != null)
			{
				final Clan clan = player.getClan();
				if(clan != null)
					switch(player.getInResidence())
					{
						case ClanHall:
						{
							final int clanHallIndex = clan.getHasHideout();
							if(clanHallIndex > 0)
							{
								final ClanHall clansHall = ResidenceHolder.getInstance().getResidence(ClanHall.class, clanHallIndex);
								if(clansHall != null && clansHall.isFunctionActive(4))
									init *= 1.0 + clansHall.getFunction(4).getLevel() / 100.0;
								break;
							}
							break;
						}
						case Castle:
						{
							final int caslteIndex = clan.getHasCastle();
							if(caslteIndex <= 0)
								break;
							final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, caslteIndex);
							if(castle != null && castle.isFunctionActive(4))
							{
								init *= 1.0 + castle.getFunction(4).getLevel() / 100.0;
								break;
							}
							break;
						}
					}
			}
		}
		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
	}

	public static double calcCpRegen(final Creature cha)
	{
		final double init = (1.5 + cha.getLevel() / 10) * cha.getLevelMod() * Formulas.CONbonus[cha.getCON()];
		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null);
	}

	public static double calcPhysDam(final Creature attacker, final Creature target, final Skill skill, final boolean shld, boolean crit, final boolean dual, final boolean ss)
	{
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		if(shld)
			defence += target.getShldDef();
		if(defence == 0.0)
			defence = 1.0;
		if(skill != null)
			damage += skill.getPower(target);
		if(skill == null || !skill.isChargeBoost())
			damage *= 1.0 + (Rnd.get() * attacker.getRandomDamage() * 2.0 - attacker.getRandomDamage()) / 100.0;
		if(skill == null && dual)
			damage /= 1.5;
		if(ss)
			damage *= 2.0;
		if(skill != null)
		{
			if(skill.isChargeBoost())
			{
				if(Config.CHARGE_DAM_C4 && (!Config.CHARGE_DAM_C4_OUTSIDE_OLY || !attacker.isInOlympiadMode()))
					damage *= 0.7 + 0.3 * attacker.getIncreasedForce();
				else
					damage *= 0.8 + 0.2 * attacker.getIncreasedForce();
				if(attacker.isInOlympiadMode())
					damage *= Config.CHARGE_DAM_OLY;
			}
			if(Rnd.chance(skill.getCriticalRate() * Formulas.STRbonus[attacker.getSTR()]))
			{
				if(Config.ALT_SHOW_CRIT_MSG)
					attacker.sendPacket(Msg.CRITICAL_HIT);
				damage *= 2.0;
				crit = true;
			}
		}
		else if(crit)
		{
			damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
			damage *= 2.0;
			if(attacker.isPlayer() && target.isPlayable())
			{
				final WeaponTemplate weapon = attacker.getActiveWeaponItem();
				if(weapon != null && weapon.getItemType() == WeaponTemplate.WeaponType.BOW)
					damage *= Config.CRIT_DAM_BOW_PVP;
			}
			damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
		}
		damage *= 70.0 / defence;
		damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, damage, target, skill);
		if(skill != null)
			damage = attacker.calcStat(Stats.PHYSICAL_SKILL_DAMAGE, damage, target, skill);
		if(attacker.isNoble() && target.isPlayable())
			damage *= 1.04;
		final boolean esd = shld && Rnd.chance(5);
		if(esd)
			damage = 1.0;
		if(skill != null)
		{
			if(shld)
				if(esd)
					target.sendPacket(Msg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				else
					target.sendPacket(Msg.SHIELD_DEFENSE_HAS_SUCCEEDED);
			if(damage > 1.0 && skill.isDeathlink())
				damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
			if(calcCastBreak(target, crit))
				target.abortCast(false, true);
		}
		return damage;
	}

	public static double calcBlowDamage(final Creature attacker, final Creature target, final Skill skill, final boolean shld, final boolean ss)
	{
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		if(shld)
			defence += target.getShldDef();
		if(defence == 0.0)
			defence = 1.0;
		if(!skill.isBehind() && ss)
			damage *= 2.04;
		damage += skill.getPower(target);
		damage *= 1.0 + (Rnd.get() * attacker.getRandomDamage() * 2.0 - attacker.getRandomDamage()) / 100.0;
		if(skill.isBehind() && ss)
			damage *= 1.5;
		damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
		damage += 6.2 * attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
		damage *= 70.0 / defence;
		damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, damage, target, skill);
		damage = attacker.calcStat(Stats.PHYSICAL_SKILL_DAMAGE, damage, target, skill);
		if(attacker.isNoble() && target.isPlayable())
			damage *= 1.04;
		final double sc = skill.getCriticalRate() * Formulas.STRbonus[attacker.getSTR()];
		final boolean crit = sc > 0.0 && Rnd.chance(sc);
		if(crit)
			damage *= 2.0;
		if(!attacker.isInOlympiadMode())
			damage *= Config.BLOW_DAM_OUTSIDE_OLY;
		if(shld)
			if(Rnd.chance(5))
			{
				damage = 1.0;
				target.sendPacket(Msg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
			}
			else
				target.sendPacket(Msg.SHIELD_DEFENSE_HAS_SUCCEEDED);
		if(calcCastBreak(target, crit))
			target.abortCast(false, true);
		return Math.max(1.0, damage);
	}

	public static double calcMagicDam(final Creature attacker, final Creature target, final Skill skill, final int sps)
	{
		double mAtk = attacker.getMAtk(target, skill);
		if(sps == 2)
			mAtk *= 4.0;
		else if(sps == 1)
			mAtk *= 2.0;
		double mdef = target.getMDef(null, skill);
		if(mdef == 0.0)
			mdef = 1.0;
		double damage = 91.0 * skill.getPower(target) * Math.sqrt(mAtk) / mdef;
		damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);
		if(attacker.isNoble() && target.isPlayable())
			damage *= 1.04;
		if(Config.ALT_MAGICFAILURES)
		{
			final int mLevel = skill.getMagicLevel() <= 0 ? attacker.getLevel() : skill.getMagicLevel();
			final int levelDiff = target.getLevel() - mLevel;
			if(levelDiff > Config.MAGICFAIL_DIFF)
			{
				final double diff = Math.max(1, levelDiff);
				if(Rnd.chance(Config.MAGICRESIST_MOD * diff))
				{
					damage = 1.0;
					attacker.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(skill.getId(), skill.getDisplayLevel()));
					if(skill.getSkillType() == Skill.SkillType.DRAIN)
						target.sendPacket(new SystemMessage(157).addName(attacker));
					else
						target.sendPacket(new SystemMessage(159).addSkillName(skill.getId(), skill.getDisplayLevel()));
				}
				else if(Rnd.chance(Config.MAGICFAIL_MOD * diff))
				{
					damage /= 2.0;
					if(skill.getSkillType() == Skill.SkillType.DRAIN)
					{
						attacker.sendPacket(new SystemMessage(156));
						target.sendPacket(new SystemMessage(157).addName(attacker));
					}
					else
					{
						attacker.sendPacket(Msg.ATTACK_FAILED);
						target.sendPacket(new SystemMessage(159).addSkillName(skill.getId(), skill.getDisplayLevel()));
					}
				}
			}
		}
		if(damage > 1.0)
		{
			if(skill.isDeathlink())
				damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
			if(skill.isBasedOnTargetDebuff())
				damage *= 1.0 + 0.05 * target.getAbnormalList().values().size();
			damage *= Config.MAGIC_DAMAGE;
		}
		final boolean crit = calcMCrit(attacker.getCriticalMagic(target, skill));
		if(crit)
			damage *= attacker.calcStat(Stats.MCRITICAL_DAMAGE, attacker.isPlayer() && attacker.getPlayer().inTvT ? Config.TVT_CRIT_DAMAGE_MAGIC : Config.CRIT_DAMAGE_MAGIC, target, skill);
		if(damage > 0.0)
			attacker.displayGiveDamageMessage(target, crit, false, true);
		if(calcCastBreak(target, crit))
			target.abortCast(false, true);
		return damage;
	}

	public static double calcManaDam(final Creature attacker, final Creature target, final Skill skill, final int sps)
	{
		double mAtk = attacker.getMAtk(target, skill);
		final double mDef = target.getMDef(attacker, skill);
		final double mp = target.getMaxMp();
		if(sps == 2)
			mAtk *= 4.0;
		else if(sps == 1)
			mAtk *= 2.0;
		double damage = Math.sqrt(mAtk) * skill.getPower(target) * (mp / 97.0) / mDef;
		damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);
		damage = damage > target.getCurrentMp() ? target.getCurrentMp() : damage;
		return damage;
	}

	public static boolean calcBlow(final Creature activeChar, final Creature target, final Skill skill)
	{
		double chance = 30.0;
		switch(Util.getDirectionTo(target, activeChar))
		{
			case BEHIND:
			{
				chance = skill.isBehind() ? 90.0 : 70.0;
				break;
			}
			case SIDE:
			{
				chance = skill.isBehind() ? 10.0 : 50.0;
				break;
			}
			case FRONT:
			{
				if(skill.isBehind())
					return false;
				break;
			}
		}
		if(!target.isInCombat())
			chance *= 1.1;
		chance = Math.max(Math.min(activeChar.calcStat(Stats.FATALBLOW_RATE, chance * (1.0 + (activeChar.getDEX() - 20) / 100), target, null), 100.0), 0.0);
		if((activeChar.isGM() || Config.SKILLS_SHOW_CHANCE && activeChar.isPlayer()) && ((Player) activeChar).getVarBoolean("SkillsChance"))
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.Formulas.Chance").addString("Blow").addNumber((int) chance));
		return Rnd.chance(chance);
	}

	public static boolean calcCrit(final Creature attacker, final Creature target, final double rate)
	{
		if(attacker.isPlayer() && attacker.getActiveWeaponItem() == null)
			return false;
		final double chance = rate / 10.0;
		return Rnd.chance(chance);
	}

	public static boolean calcMCrit(final double mRate)
	{
		return Rnd.get() * 1000.0 <= mRate;
	}

	public static boolean calcCastBreak(final Creature target, final boolean crit)
	{
		if(target == null || target.isInvul() || target.isRaid() || !target.isCastingNow())
			return false;
		final Skill skill = target.getCastingSkill();
		return (skill == null || skill.getSkillType() != Skill.SkillType.TAKECASTLE) && Rnd.chance(target.calcStat(Stats.CAST_INTERRUPT, crit ? 75.0 : 10.0, null, skill));
	}

	public static int calcPAtkSpd(final double rate)
	{
		return (int) (500000.0 / rate);
	}

	public static int calcMAtkSpd(final Creature attacker, final Skill skill, final double skillTime)
	{
		if(skill.isMagic())
			return (int) (skillTime * 333.0 / Math.max(attacker.getMAtkSpd(), 1));
		return (int) (skillTime * 333.0 / Math.max(attacker.getPAtkSpd(), 1));
	}

	public static long calcSkillReuseDelay(final Creature actor, final Skill skill)
	{
		long reuseDelay = skill.getReuseDelay();
		if(actor.isMonster())
			reuseDelay = skill.getReuseForMonsters();
		if(skill.isReuseDelayPermanent() || skill.isHandler() || Config.AUGMENT_STATIC_REUSE && skill.isItemSkill())
			return reuseDelay;
		if(actor.getSkillMastery(skill.getId()) == 1)
		{
			actor.removeSkillMastery(skill.getId());
			return 0L;
		}
		if(skill.isMusic())
			return (long) actor.calcStat(Stats.MUSIC_REUSE_RATE, reuseDelay * 333L / Math.max(actor.getPAtkSpd(), 1), null, skill);
		if(skill.isMagic())
			return (long) (reuseDelay * actor.getMReuseRate(skill) * 333.0 / Math.max(actor.getMAtkSpd(), 1));
		return (long) actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay * 333L / Math.max(actor.getPAtkSpd(), 1), null, skill);
	}

	public static boolean calcHitMiss(final Creature attacker, final Creature target)
	{
		int chanceToHit = 88 + 2 * (attacker.getAccuracy() - target.getEvasionRate(attacker));
		chanceToHit = Math.max(chanceToHit, 28);
		chanceToHit = Math.min(chanceToHit, 98);
		final PositionUtils.TargetDirection direction = Util.getDirectionTo(attacker, target);
		switch(direction)
		{
			case BEHIND:
			{
				chanceToHit *= (int) 1.2;
				break;
			}
			case SIDE:
			{
				chanceToHit *= (int) 1.1;
				break;
			}
		}
		return !Rnd.chance(chanceToHit);
	}

	public static boolean calcShldUse(final Creature attacker, final Creature target)
	{
		final WeaponTemplate weapon = target.getSecondaryWeaponItem();
		if(weapon == null || weapon.getItemType() != WeaponTemplate.WeaponType.NONE)
			return false;
		final int angle = (int) target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
		return (angle >= 360 || Util.isFacing(target, attacker, angle)) && Rnd.chance((int) target.calcStat(Stats.SHIELD_RATE, attacker, null));
	}

	public static double calcSavevsDependence(final int save, final Creature cha)
	{
		double bonus = 1.0;
		try
		{
			switch(save)
			{
				case 1:
				{
					bonus = Formulas.INTbonus[cha.getINT()];
					break;
				}
				case 2:
				{
					bonus = Formulas.WITbonus[cha.getWIT()];
					break;
				}
				case 3:
				{
					bonus = Formulas.MENbonus[cha.getMEN()];
					break;
				}
				case 4:
				{
					bonus = Formulas.CONbonus[cha.getCON()];
					break;
				}
				case 5:
				{
					bonus = Formulas.DEXbonus[cha.getDEX()];
					break;
				}
				case 6:
				{
					return Math.min(2.0 - Math.sqrt(Formulas.STRbonus[cha.getSTR()]), 1.0);
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			Formulas._log.warn("Failed calc savevs on char " + cha + " with save-stat " + save);
			e.printStackTrace();
		}
		return 2.0 - Math.sqrt(bonus);
	}

	public static boolean calcSkillSuccess(final Env env, final int spiritshot, final Stats resistType, final Stats attributeType, final boolean effect, final boolean ignoreResists)
	{
		if(env.target.isDoor())
			return false;
		if(env.value == -1.0)
			return true;
		env.value = Math.max(Math.min(env.value, 100.0), 1.0);
		final double base = env.value;
		final Skill skill = env.skill;
		if(!skill.isOffensive())
			return Rnd.chance(env.value);
		final Creature caster = env.character;
		final Creature target = env.target;
		final double mLevel = skill.getMagicLevel() <= 0 ? caster.getLevel() : (double) skill.getMagicLevel();
		env.value += Math.min((mLevel - target.getLevel() + 3.0) * skill.getLevelModifier(), 0.0);
		if(skill.getSavevs() > 0)
			env.value *= calcSavevsDependence(skill.getSavevs(), target);
		env.value = Math.max(env.value, 1.0);
		if(skill.isMagic())
		{
			final int mdef = Math.max(1, target.getMDef(target, skill));
			double matk = caster.getMAtk(target, skill);
			if(skill.isSSPossible() && spiritshot > 0)
				matk *= spiritshot < 2 ? 1.41 : spiritshot;
			env.value *= Math.pow(matk, 0.35) * Math.pow(Math.log1p(matk), 2.0) / mdef;
		}
		if(!skill.isIgnoreResists() && !ignoreResists)
		{
			double res = 0.0;
			if(effect)
			{
				if(resistType != null)
					res += target.calcStat(resistType, caster, skill);
				if(attributeType != null)
					res -= caster.calcStat(attributeType, target, skill);
			}
			else
			{
				final SkillTrait trait = skill.getTraitType();
				if(trait != null)
				{
					res += trait.calcVuln(env);
					res -= trait.calcProf(env);
				}
			}
			res += target.calcStat(Stats.DEBUFF_RESIST, caster, skill);
			if(res == Double.NEGATIVE_INFINITY)
				return true;
			if(res == Double.POSITIVE_INFINITY)
				return false;
			res *= 0.01 * env.value;
			env.value -= res;
		}
		if(caster.isMonster())
			env.value *= Config.SKILLS_MOB_CHANCE;
		env.value = caster.calcStat(Stats.ACTIVATE_RATE, env.value, target, skill);
		env.value = Math.max(env.value, Math.min(base, Config.SKILLS_CHANCE_MIN));
		env.value = Math.max(Math.min(env.value, Config.SKILLS_CHANCE_CAP), 0.0);
		if(Config.SKILLS_SHOW_CHANCE && caster.isPlayer() && ((Player) caster).getVarBoolean("SkillsChance"))
			caster.sendMessage(new CustomMessage("l2s.gameserver.skills.Formulas.Chance").addString(skill.getName()).addNumber((int) env.value));
		return Rnd.chance((int) env.value);
	}

	public static boolean calcSkillSuccess(final Creature player, final Creature target, final Skill skill, final int activateRate)
	{
		final Env env = new Env();
		env.character = player;
		env.target = target;
		env.skill = skill;
		env.value = activateRate;
		return calcSkillSuccess(env, player.getChargedSpiritShot(), null, null, false, false);
	}

	public static double cancelChance(final Creature character, final Creature targ, final Skill sk, final int rate)
	{
		if(rate == -1)
			return 100.0;
		final double mLevel = sk.getMagicLevel() <= 0 ? character.getLevel() : (double) sk.getMagicLevel();
		double value = Math.min((mLevel - targ.getLevel() + 3.0) * sk.getLevelModifier(), 0.0) + rate;
		if(sk.getSavevs() > 0)
			value *= calcSavevsDependence(sk.getSavevs(), targ);
		value = Math.max(value, 1.0);
		if(sk.isMagic())
		{
			final int mdef = Math.max(1, targ.getMDef(targ, sk));
			double matk = character.getMAtk(targ, sk);
			final int spiritshot = character.getChargedSpiritShot();
			if(sk.isSSPossible() && spiritshot > 0)
				matk *= spiritshot < 2 ? 1.41 : spiritshot;
			value *= Math.pow(matk, 0.35) * Math.pow(Math.log1p(matk), 2.0) / mdef;
		}
		if(!sk.isIgnoreResists())
		{
			final double res = (targ.calcStat(Stats.CANCEL_RESIST, character, sk) - character.calcStat(Stats.CANCEL_POWER, targ, sk)) * 0.01 * value;
			value -= res;
		}
		if(character.isMonster())
			value *= Config.SKILLS_MOB_CHANCE;
		return value;
	}

	public static void calcSkillMastery(final Skill skill, final Creature activeChar)
	{
		if(skill.isHandler() || !activeChar.isPlayer())
			return;
		if(activeChar.getSkillLevel(331) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getINT(), null, skill) >= Rnd.get(1000) || activeChar.getSkillLevel(330) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getSTR(), null, skill) >= Rnd.get(1000))
		{
			final Skill.SkillType type = skill.getSkillType();
			byte masteryLevel;
			if(skill.isMusic() || type == Skill.SkillType.BUFF || type == Skill.SkillType.HOT || type == Skill.SkillType.HEAL_PERCENT || type == Skill.SkillType.SEED)
				masteryLevel = 2;
			else
				masteryLevel = 1;
			activeChar.setSkillMastery(skill.getId(), masteryLevel);
		}
	}

	public static double calcDamageResists(final Skill skill, final Creature attacker, final Creature defender, final double value)
	{
		final int fire_attack = (int) attacker.calcStat(Stats.ATTACK_ELEMENT_FIRE, 0.0, null, null);
		final int water_attack = (int) attacker.calcStat(Stats.ATTACK_ELEMENT_WATER, 0.0, null, null);
		final int wind_attack = (int) attacker.calcStat(Stats.ATTACK_ELEMENT_WIND, 0.0, null, null);
		final int earth_attack = (int) attacker.calcStat(Stats.ATTACK_ELEMENT_EARTH, 0.0, null, null);
		final int sacred_attack = (int) attacker.calcStat(Stats.ATTACK_ELEMENT_SACRED, 0.0, null, null);
		final int unholy_attack = (int) attacker.calcStat(Stats.ATTACK_ELEMENT_UNHOLY, 0.0, null, null);
		if(skill != null && skill.getElement() != Skill.Element.NONE)
			switch(skill.getElement())
			{
				case FIRE:
				{
					return applyDefense(defender, Stats.FIRE_RECEPTIVE, fire_attack, value);
				}
				case WATER:
				{
					return applyDefense(defender, Stats.WATER_RECEPTIVE, water_attack, value);
				}
				case WIND:
				{
					return applyDefense(defender, Stats.WIND_RECEPTIVE, wind_attack, value);
				}
				case EARTH:
				{
					return applyDefense(defender, Stats.EARTH_RECEPTIVE, earth_attack, value);
				}
				case SACRED:
				{
					return applyDefense(defender, Stats.SACRED_RECEPTIVE, sacred_attack, value);
				}
				case UNHOLY:
				{
					return applyDefense(defender, Stats.UNHOLY_RECEPTIVE, unholy_attack, value);
				}
			}
		if(fire_attack == 0 && water_attack == 0 && earth_attack == 0 && wind_attack == 0 && unholy_attack == 0 && sacred_attack == 0)
			return value;
		final TreeMap<Integer, Stats> sort_attibutes = new TreeMap<Integer, Stats>();
		sort_attibutes.put(fire_attack, Stats.FIRE_RECEPTIVE);
		sort_attibutes.put(water_attack, Stats.WATER_RECEPTIVE);
		sort_attibutes.put(wind_attack, Stats.WIND_RECEPTIVE);
		sort_attibutes.put(earth_attack, Stats.EARTH_RECEPTIVE);
		sort_attibutes.put(sacred_attack, Stats.SACRED_RECEPTIVE);
		sort_attibutes.put(unholy_attack, Stats.UNHOLY_RECEPTIVE);
		final int attack = sort_attibutes.lastEntry().getKey();
		final Stats defence_type = sort_attibutes.lastEntry().getValue();
		return applyDefense(defender, defence_type, attack, value);
	}

	public static double calcMagicDamageResists(final Skill skill, final Creature defender, final double value)
	{
		if(skill != null && skill.getElement() != Skill.Element.NONE)
			switch(skill.getElement())
			{
				case FIRE:
				{
					return applyDefense(defender, Stats.FIRE_RECEPTIVE, 0, value);
				}
				case WATER:
				{
					return applyDefense(defender, Stats.WATER_RECEPTIVE, 0, value);
				}
				case WIND:
				{
					return applyDefense(defender, Stats.WIND_RECEPTIVE, 0, value);
				}
				case EARTH:
				{
					return applyDefense(defender, Stats.EARTH_RECEPTIVE, 0, value);
				}
				case SACRED:
				{
					return applyDefense(defender, Stats.SACRED_RECEPTIVE, 0, value);
				}
				case UNHOLY:
				{
					return applyDefense(defender, Stats.UNHOLY_RECEPTIVE, 0, value);
				}
			}
		return value;
	}

	public static double applyDefense(final Creature defender, final Stats defence_type, final int attack, double value)
	{
		final int defense = 100 - (int) defender.calcStat(defence_type, 100.0, null, null);
		int diff = defense - attack;
		diff = Math.min(100, diff);
		diff = Math.max(-100, diff);
		if(diff < 0)
			value *= 1.0 - diff / 133.0;
		else if(diff > 0)
			value /= 1.0 + diff / 100.0;
		return value;
	}

	static
	{
		_log = LoggerFactory.getLogger(Creature.class);
		Formulas.MAX_STAT_VALUE = 100;
		WITbonus = new double[Formulas.MAX_STAT_VALUE];
		MENbonus = new double[Formulas.MAX_STAT_VALUE];
		INTbonus = new double[Formulas.MAX_STAT_VALUE];
		STRbonus = new double[Formulas.MAX_STAT_VALUE];
		DEXbonus = new double[Formulas.MAX_STAT_VALUE];
		CONbonus = new double[Formulas.MAX_STAT_VALUE];
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		final File file = new File(Config.DATAPACK_ROOT, "data/attribute_bonus.xml");
		Document doc = null;
		try
		{
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch(SAXException e)
		{
			e.printStackTrace();
		}
		catch(IOException e2)
		{
			e2.printStackTrace();
		}
		catch(ParserConfigurationException e3)
		{
			e3.printStackTrace();
		}
		if(doc != null)
			for(Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
				for(Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if(n.getNodeName().equalsIgnoreCase("str_bonus"))
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							final String node = d.getNodeName();
							if(node.equalsIgnoreCase("set"))
							{
								final int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								final double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								Formulas.STRbonus[i] = (100.0 + val) / 100.0;
							}
						}
					if(n.getNodeName().equalsIgnoreCase("int_bonus"))
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							final String node = d.getNodeName();
							if(node.equalsIgnoreCase("set"))
							{
								final int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								final double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								Formulas.INTbonus[i] = (100.0 + val) / 100.0;
							}
						}
					if(n.getNodeName().equalsIgnoreCase("con_bonus"))
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							final String node = d.getNodeName();
							if(node.equalsIgnoreCase("set"))
							{
								final int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								final double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								Formulas.CONbonus[i] = (100.0 + val) / 100.0;
							}
						}
					if(n.getNodeName().equalsIgnoreCase("men_bonus"))
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							final String node = d.getNodeName();
							if(node.equalsIgnoreCase("set"))
							{
								final int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								final double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								Formulas.MENbonus[i] = (100.0 + val) / 100.0;
							}
						}
					if(n.getNodeName().equalsIgnoreCase("dex_bonus"))
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							final String node = d.getNodeName();
							if(node.equalsIgnoreCase("set"))
							{
								final int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								final double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								Formulas.DEXbonus[i] = (100.0 + val) / 100.0;
							}
						}
					if(n.getNodeName().equalsIgnoreCase("wit_bonus"))
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							final String node = d.getNodeName();
							if(node.equalsIgnoreCase("set"))
							{
								final int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								final double val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								Formulas.WITbonus[i] = (100.0 + val) / 100.0;
							}
						}
				}
	}

	private static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] func;

		static Func getFunc(final Stats stat)
		{
			final int pos = stat.ordinal();
			if(FuncMultRegenResting.func[pos] == null)
				FuncMultRegenResting.func[pos] = new FuncMultRegenResting(stat);
			return FuncMultRegenResting.func[pos];
		}

		private FuncMultRegenResting(final Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, true));
		}

		@Override
		public void calc(final Env env)
		{
			if(!cond.test(env))
				return;
			if(env.character.isPlayer() && env.character.getLevel() <= 40 && ((Player) env.character).getClassId().getLevel() < 3)
				env.value *= 6.0;
			else
				env.value *= 1.5;
		}

		static
		{
			func = new FuncMultRegenResting[Stats.NUM_STATS];
		}
	}

	private static class FuncMultRegenStanding extends Func
	{
		static final FuncMultRegenStanding[] func;

		static Func getFunc(final Stats stat)
		{
			final int pos = stat.ordinal();
			if(FuncMultRegenStanding.func[pos] == null)
				FuncMultRegenStanding.func[pos] = new FuncMultRegenStanding(stat);
			return FuncMultRegenStanding.func[pos];
		}

		private FuncMultRegenStanding(final Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.STANDING, true));
		}

		@Override
		public void calc(final Env env)
		{
			if(!cond.test(env))
				return;
			env.value *= 1.1;
		}

		static
		{
			func = new FuncMultRegenStanding[Stats.NUM_STATS];
		}
	}

	private static class FuncMultRegenRunning extends Func
	{
		static final FuncMultRegenRunning[] func;

		static Func getFunc(final Stats stat)
		{
			final int pos = stat.ordinal();
			if(FuncMultRegenRunning.func[pos] == null)
				FuncMultRegenRunning.func[pos] = new FuncMultRegenRunning(stat);
			return FuncMultRegenRunning.func[pos];
		}

		private FuncMultRegenRunning(final Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, true));
		}

		@Override
		public void calc(final Env env)
		{
			if(!cond.test(env))
				return;
			env.value *= 0.7;
		}

		static
		{
			func = new FuncMultRegenRunning[Stats.NUM_STATS];
		}
	}

	private static class FuncPAtkMul extends Func
	{
		static final FuncPAtkMul func;

		private FuncPAtkMul()
		{
			super(Stats.POWER_ATTACK, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= Formulas.STRbonus[env.character.getSTR()] * env.character.getLevelMod();
		}

		static
		{
			func = new FuncPAtkMul();
		}
	}

	private static class FuncMAtkMul extends Func
	{
		static final FuncMAtkMul func;

		private FuncMAtkMul()
		{
			super(Stats.MAGIC_ATTACK, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			final double ib = Formulas.INTbonus[env.character.getINT()];
			final double lvlb = env.character.getLevelMod();
			env.value *= lvlb * lvlb * ib * ib;
		}

		static
		{
			func = new FuncMAtkMul();
		}
	}

	private static class FuncPDefMul extends Func
	{
		static final FuncPDefMul func;

		private FuncPDefMul()
		{
			super(Stats.POWER_DEFENCE, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= env.character.getLevelMod();
		}

		static
		{
			func = new FuncPDefMul();
		}
	}

	private static class FuncMDefMul extends Func
	{
		static final FuncMDefMul func;

		private FuncMDefMul()
		{
			super(Stats.MAGIC_DEFENCE, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= Formulas.MENbonus[env.character.getMEN()] * env.character.getLevelMod();
		}

		static
		{
			func = new FuncMDefMul();
		}
	}

	private static class FuncAttackRange extends Func
	{
		static final FuncAttackRange func;

		private FuncAttackRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			final WeaponTemplate weapon = env.character.getActiveWeaponItem();
			if(weapon != null)
				env.value += weapon.getAttackRange();
		}

		static
		{
			func = new FuncAttackRange();
		}
	}

	private static class FuncAccuracyAdd extends Func
	{
		static final FuncAccuracyAdd func;

		private FuncAccuracyAdd()
		{
			super(Stats.ACCURACY_COMBAT, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value += Math.sqrt(env.character.getDEX()) * 6.0 + env.character.getLevel();
			if(env.character.isNpc() || env.character.isSummon())
				env.value += ((NpcTemplate) env.character.getTemplate()).physHitMod;
		}

		static
		{
			func = new FuncAccuracyAdd();
		}
	}

	private static class FuncEvasionAdd extends Func
	{
		static final FuncEvasionAdd func;

		private FuncEvasionAdd()
		{
			super(Stats.EVASION_RATE, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value += Math.sqrt(env.character.getDEX()) * 6.0 + env.character.getLevel();
			if(env.character.isNpc() || env.character.isSummon())
				env.value += ((NpcTemplate) env.character.getTemplate()).physAvoidMod;
		}

		static
		{
			func = new FuncEvasionAdd();
		}
	}

	private static class FuncPCriticalRateMul extends Func
	{
		static final FuncPCriticalRateMul func;

		private FuncPCriticalRateMul()
		{
			super(Stats.CRITICAL_BASE, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			if(!env.character.isSummon())
				env.value *= Formulas.DEXbonus[env.character.getDEX()];
			env.value *= 0.01 * env.character.calcStat(Stats.CRITICAL_RATE, env.target, env.skill);
		}

		static
		{
			func = new FuncPCriticalRateMul();
		}
	}

	private static class FuncMCriticalRateMul extends Func
	{
		static final FuncMCriticalRateMul func;

		private FuncMCriticalRateMul()
		{
			super(Stats.MCRITICAL_RATE, 48, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= Formulas.WITbonus[env.character.getWIT()];
		}

		static
		{
			func = new FuncMCriticalRateMul();
		}
	}

	private static class FuncCritLimit extends Func
	{
		static final FuncCritLimit func;

		private FuncCritLimit()
		{
			super(Stats.CRITICAL_BASE, 256, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value = Math.min(Config.LIM_CRIT, env.value);
		}

		static
		{
			func = new FuncCritLimit();
		}
	}

	private static class FuncMoveSpeed extends Func
	{
		static final FuncMoveSpeed func;

		private FuncMoveSpeed()
		{
			super(Stats.RUN_SPEED, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= Formulas.DEXbonus[env.character.getDEX()];
		}

		static
		{
			func = new FuncMoveSpeed();
		}
	}

	private static class FuncPAtkSpeedMul extends Func
	{
		static final FuncPAtkSpeedMul func;

		private FuncPAtkSpeedMul()
		{
			super(Stats.POWER_ATTACK_SPEED, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= Formulas.DEXbonus[env.character.getDEX()];
		}

		static
		{
			func = new FuncPAtkSpeedMul();
		}
	}

	private static class FuncMAtkSpeedMul extends Func
	{
		static final FuncMAtkSpeedMul func;

		private FuncMAtkSpeedMul()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= Formulas.WITbonus[env.character.getWIT()];
		}

		static
		{
			func = new FuncMAtkSpeedMul();
		}
	}

	private static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR func;

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1.0, env.value + pc.getHennaStatSTR());
		}

		static
		{
			func = new FuncHennaSTR();
		}
	}

	private static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX func;

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1.0, env.value + pc.getHennaStatDEX());
		}

		static
		{
			func = new FuncHennaDEX();
		}
	}

	private static class FuncHennaINT extends Func
	{
		static final FuncHennaINT func;

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1.0, env.value + pc.getHennaStatINT());
		}

		static
		{
			func = new FuncHennaINT();
		}
	}

	private static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN func;

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1.0, env.value + pc.getHennaStatMEN());
		}

		static
		{
			func = new FuncHennaMEN();
		}
	}

	private static class FuncHennaCON extends Func
	{
		static final FuncHennaCON func;

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1.0, env.value + pc.getHennaStatCON());
		}

		static
		{
			func = new FuncHennaCON();
		}
	}

	private static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT func;

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 16, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player pc = (Player) env.character;
			if(pc != null)
				env.value = Math.max(1.0, env.value + pc.getHennaStatWIT());
		}

		static
		{
			func = new FuncHennaWIT();
		}
	}

	private static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul func;

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= Formulas.CONbonus[env.character.getCON()];
		}

		static
		{
			func = new FuncMaxHpMul();
		}
	}

	private static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul func = new FuncMaxCpMul();

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 32, null);
		}

		@Override
		public void calc(Env env)
		{
			double cpSSmod = 1.0;
			if(Config.ALLOW_SEVEN_SIGNS)
			{
				int sealOwnedBy = SevenSigns.getInstance().getSealOwner(3);
				int playerCabal = SevenSigns.getInstance().getPlayerCabal((Player) env.character);
				if(sealOwnedBy != 0)
				{
					if(playerCabal == sealOwnedBy)
						cpSSmod = 1.1;
					else
						cpSSmod = 0.9;
				}
			}
			env.value *= Formulas.CONbonus[env.character.getCON()] * cpSSmod;
		}
	}

	private static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul func = new FuncMaxMpMul();

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= Formulas.MENbonus[env.character.getMEN()];
		}
	}

	private static class FuncPDamageResists extends Func
	{
		private static final FuncPDamageResists func = new FuncPDamageResists();

		private FuncPDamageResists()
		{
			super(Stats.PHYSICAL_DAMAGE, 48, null);
		}

		@Override
		public void calc(final Env env)
		{
			final WeaponTemplate weapon = env.character.getActiveWeaponItem();
			if(weapon != null && weapon.getItemType().getDefence() != null)
				env.value *= 0.01 * env.target.calcStat(weapon.getItemType().getDefence(), 100.0, null, null);
			env.value = Formulas.calcDamageResists(env.skill, env.character, env.target, env.value);
		}
	}

	private static class FuncMDamageResists extends Func
	{
		private static final FuncMDamageResists func = new FuncMDamageResists();

		private FuncMDamageResists()
		{
			super(Stats.MAGIC_DAMAGE, 48, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value = Formulas.calcMagicDamageResists(env.skill, env.target, env.value);
		}
	}

	private static class FuncInventory extends Func
	{
		private static final FuncInventory func = new FuncInventory();

		private FuncInventory()
		{
			super(Stats.INVENTORY_LIMIT, 1, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player _cha = (Player) env.character;
			if(_cha.isGM())
				env.value = Config.INVENTORY_MAXIMUM_GM;
			else if(_cha.getTemplate().race == Race.dwarf)
				env.value = Config.INVENTORY_MAXIMUM_DWARF;
			else
				env.value = Config.INVENTORY_MAXIMUM_NO_DWARF;
			env.value += _cha.getExpandInventory();
		}
	}

	private static class FuncWarehouse extends Func
	{
		private static final FuncWarehouse func = new FuncWarehouse();

		private FuncWarehouse()
		{
			super(Stats.STORAGE_LIMIT, 1, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player _cha = (Player) env.character;
			if(_cha.getTemplate().race == Race.dwarf)
				env.value = Config.WAREHOUSE_SLOTS_DWARF;
			else
				env.value = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}
	}

	private static class FuncTradeLimit extends Func
	{
		private static final FuncTradeLimit func;

		private FuncTradeLimit()
		{
			super(Stats.TRADE_LIMIT, 1, null);
		}

		@Override
		public void calc(final Env env)
		{
			final Player _cha = (Player) env.character;
			if(_cha.getRace() == Race.dwarf)
				env.value = Config.MAX_PVTSTORE_SLOTS_DWARF;
			else
				env.value = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}

		static
		{
			func = new FuncTradeLimit();
		}
	}

	private static class FuncSDefInit extends Func
	{
		static final Func func;

		private FuncSDefInit()
		{
			super(Stats.SHIELD_RATE, 1, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value = env.character.getTemplate().baseShldRate;
		}

		static
		{
			func = new FuncSDefInit();
		}
	}

	private static class FuncSDefAll extends Func
	{
		static final FuncSDefAll func;

		private FuncSDefAll()
		{
			super(Stats.SHIELD_RATE, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			if(env.value == 0.0)
				return;
			final Creature target = env.target;
			if(target != null)
			{
				final WeaponTemplate weapon = target.getActiveWeaponItem();
				if(weapon != null)
					switch(weapon.getItemType())
					{
						case BOW:
						{
							env.value += 30.0;
							break;
						}
						case DAGGER:
						{
							env.value += 12.0;
							break;
						}
					}
			}
		}

		static
		{
			func = new FuncSDefAll();
		}
	}

	private static class FuncSDefPlayers extends Func
	{
		static final FuncSDefPlayers func;

		private FuncSDefPlayers()
		{
			super(Stats.SHIELD_RATE, 32, null);
		}

		@Override
		public void calc(final Env env)
		{
			if(env.value == 0.0)
				return;
			final Creature cha = env.character;
			final ItemInstance shld = ((Player) cha).getInventory().getPaperdollItem(8);
			if(shld == null || shld.getItemType() != WeaponTemplate.WeaponType.NONE)
				return;
			env.value *= Formulas.DEXbonus[cha.getDEX()];
		}

		static
		{
			func = new FuncSDefPlayers();
		}
	}

	private static class FuncMaxHpLimit extends Func
	{
		static final Func func;

		private FuncMaxHpLimit()
		{
			super(Stats.MAX_HP, 256, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value = Math.min(Config.LIM_HP, env.value);
		}

		static
		{
			func = new FuncMaxHpLimit();
		}
	}

	private static class FuncMaxMpLimit extends Func
	{
		static final Func func;

		private FuncMaxMpLimit()
		{
			super(Stats.MAX_MP, 256, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value = Math.min(Config.LIM_MP, env.value);
		}

		static
		{
			func = new FuncMaxMpLimit();
		}
	}

	private static class FuncMaxCpLimit extends Func
	{
		static final Func func;

		private FuncMaxCpLimit()
		{
			super(Stats.MAX_CP, 256, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value = Math.min(Config.LIM_CP, env.value);
		}

		static
		{
			func = new FuncMaxCpLimit();
		}
	}
}
