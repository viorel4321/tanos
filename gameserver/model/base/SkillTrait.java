package l2s.gameserver.model.base;

import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;

public enum SkillTrait
{
	NONE,
	BLEED
	{
		@Override
		public final double calcVuln(final Env env)
		{
			return env.target.calcStat(Stats.BLEED_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(final Env env)
		{
			return env.character.calcStat(Stats.BLEED_POWER, env.target, env.skill);
		}
	},
	BOSS,
	DEATH,
	DERANGEMENT
	{
		@Override
		public final double calcVuln(final Env env)
		{
			return env.target.calcStat(Stats.MENTAL_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(final Env env)
		{
			return env.character.calcStat(Stats.MENTAL_POWER, env.target, env.skill);
		}
	},
	ETC,
	GUST,
	HOLD
	{
		@Override
		public final double calcVuln(final Env env)
		{
			return env.target.calcStat(Stats.ROOT_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(final Env env)
		{
			return env.character.calcStat(Stats.ROOT_POWER, env.target, env.skill);
		}
	},
	PARALYZE
	{
		@Override
		public final double calcVuln(final Env env)
		{
			return env.target.calcStat(Stats.PARALYZE_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(final Env env)
		{
			return env.character.calcStat(Stats.PARALYZE_POWER, env.target, env.skill);
		}
	},
	PHYSICAL_BLOCKADE,
	POISON
	{
		@Override
		public final double calcVuln(final Env env)
		{
			return env.target.calcStat(Stats.POISON_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(final Env env)
		{
			return env.character.calcStat(Stats.POISON_POWER, env.target, env.skill);
		}
	},
	SHOCK
	{
		@Override
		public final double calcVuln(final Env env)
		{
			return env.target.calcStat(Stats.STUN_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(final Env env)
		{
			return env.character.calcStat(Stats.STUN_POWER, env.target, env.skill);
		}
	},
	SLEEP
	{
		@Override
		public final double calcVuln(final Env env)
		{
			return env.target.calcStat(Stats.SLEEP_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(final Env env)
		{
			return env.character.calcStat(Stats.SLEEP_POWER, env.target, env.skill);
		}
	},
	CANCEL
	{
		@Override
		public final double calcVuln(final Env env)
		{
			return env.target.calcStat(Stats.CANCEL_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(final Env env)
		{
			return env.character.calcStat(Stats.CANCEL_POWER, env.target, env.skill);
		}
	},
	VALAKAS;

	public double calcVuln(final Env env)
	{
		return 0.0;
	}

	public double calcProf(final Env env)
	{
		return 0.0;
	}
}
