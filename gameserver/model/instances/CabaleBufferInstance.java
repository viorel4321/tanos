package l2s.gameserver.model.instances;

import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class CabaleBufferInstance extends NpcInstance
{
	ScheduledFuture<?> aiTask;

	public CabaleBufferInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);

		if(Config.ALLOW_SEVEN_SIGNS)
			aiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CabalaAI(this), 3000L, 3000L);
	}

	@Override
	public void deleteMe()
	{
		if(aiTask != null)
			aiTask.cancel(true);
		super.deleteMe();
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	class CabalaAI implements Runnable
	{
		CabaleBufferInstance _caster;

		CabalaAI(final CabaleBufferInstance caster)
		{
			_caster = caster;
		}

		@Override
		public void run()
		{
			if(!Config.ALLOW_SEVEN_SIGNS)
				return;

			final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
			if(winningCabal == 0)
				return;
			int losingCabal = 0;
			if(winningCabal == 2)
				losingCabal = 1;
			else if(winningCabal == 1)
				losingCabal = 2;
			for(final Player player : World.getAroundPlayers(CabaleBufferInstance.this, 900, 200))
			{
				if(player == null)
					continue;
				final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				if(playerCabal == winningCabal && _caster.getNpcId() == 31094)
				{
					if(player.isMageClass())
						handleCast(player, 4365);
					else
						handleCast(player, 4364);
				}
				else
				{
					if(playerCabal != losingCabal || _caster.getNpcId() != 31093)
						continue;
					if(player.isMageClass())
						handleCast(player, 4362);
					else
						handleCast(player, 4361);
				}
			}
		}

		private void handleCast(final Player player, final int skillId)
		{
			final Skill skill = SkillTable.getInstance().getInfo(skillId, 2);
			if(player.getAbnormalList().getEffectsBySkill(skill) == null && GeoEngine.canSeeTarget(_caster, player))
			{
				skill.getEffects(_caster, player, false, false);
				CabaleBufferInstance.this.broadcastPacket(new MagicSkillUse(_caster, player, skill.getId(), 2, skill.getHitTime(), 0L));
			}
			final Servitor summon = player.getServitor();
			if(summon != null && summon.getAbnormalList().getEffectsBySkill(skill) == null && summon.isInRangeZ(_caster, 900L) && GeoEngine.canSeeTarget(_caster, summon))
			{
				skill.getEffects(_caster, summon, false, false);
				CabaleBufferInstance.this.broadcastPacket(new MagicSkillUse(_caster, summon, skill.getId(), 2, skill.getHitTime(), 0L));
			}
		}
	}
}
