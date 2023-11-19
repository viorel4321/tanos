package l2s.gameserver.skills.effects;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.TerrainObjectInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2s.gameserver.skills.Env;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public final class EffectSymbol extends Abnormal
{
	private static final Logger _log;
	private RoundTerritoryWithSkill _territory;
	private TerrainObjectInstance _symbol;

	public EffectSymbol(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getSkill().getTargetType() != Skill.SkillTargetType.TARGET_SELF)
		{
			EffectSymbol._log.error("Symbol skill with target != self, id = " + getSkill().getId());
			return false;
		}
		final Skill skill = getSkill().getFirstAddedSkill();
		if(skill == null)
		{
			EffectSymbol._log.error("Not implemented symbol skill, id = " + getSkill().getId());
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Skill skill = getSkill().getFirstAddedSkill();
		skill.setMagicType(getSkill().getMagicType());

		Location loc = _effected.getLoc();
		if(_effected.isPlayer())
		{
			Player player = _effected.getPlayer();
			if(player.getGroundSkillLoc() != null) {
				loc = player.getGroundSkillLoc();
				player.setGroundSkillLoc(null);
			}
		}

		World.addTerritory(_territory = new RoundTerritoryWithSkill(_effected.getObjectId(), loc.x, loc.y, _skill.getSkillRadius(), loc.z - 200, loc.z + 200, _effector, skill));

		_symbol = new TerrainObjectInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(_skill.getSymbolId()));
		_symbol.setReflectionId(_effected.getReflectionId());
		_symbol.spawnMe(loc);

		for(final Creature cha : World.getAroundCharacters(_symbol, _skill.getSkillRadius() + 200, 400))
			cha.updateTerritories();
	}

	@Override
	public void onExit()
	{
		super.onExit();

		World.removeTerritory(_territory);

		if(_symbol == null)
			return;

		for(Creature cha : World.getAroundCharacters(_symbol, _skill.getSkillRadius() + 200, 400))
			cha.updateTerritories();

		_symbol.deleteMe();
		_symbol = null;
	}

	@Override
	public boolean onActionTime()
	{
		if(getTemplate()._counter <= 1)
			return false;

		Creature effector = getEffector();
		Skill skill = getSkill().getFirstAddedSkill();
		TerrainObjectInstance symbol = _symbol;
		double mpConsume = getSkill().getMpConsume();
		if(effector == null || skill == null || symbol == null)
			return false;

		if(mpConsume > effector.getCurrentMp())
		{
			effector.sendPacket(Msg.NOT_ENOUGH_MP);
			return false;
		}

		effector.reduceCurrentMp(mpConsume, effector);
		for(Creature cha : World.getAroundCharacters(symbol, getSkill().getSkillRadius(), 200)) {
			if (!cha.isDoor() && skill.checkTarget(effector, cha, null, false, false, false) == null) {
				if (skill.isOffensive() && !GeoEngine.canSeeTarget(symbol, cha))
					continue;

				Set<Creature> targets = new HashSet<>(1);
				targets.add(cha);
				effector.callSkill(cha, skill, targets, true);
				effector.broadcastPacket(new MagicSkillLaunched(effector.getObjectId(), getSkill().getDisplayId(), getSkill().getDisplayLevel(), cha));
			}
		}
		return true;
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	static
	{
		_log = LoggerFactory.getLogger(EffectSymbol.class);
	}
}
