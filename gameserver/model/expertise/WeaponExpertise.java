package l2s.gameserver.model.expertise;

import l2s.gameserver.model.Player;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.funcs.FuncMul;
import l2s.gameserver.skills.funcs.FuncSub;

public class WeaponExpertise
{
	private Player _player;
	public static double[] acc;
	public static double[] crit_rate;
	public static double[] crit_dam;
	public static double[] power;
	public static double[] magic;
	public static boolean run_dec;
	public static double[] run;

	public WeaponExpertise(final Player player)
	{
		_player = player;
	}

	public void add(final int level)
	{
		_player.addStatFunc(new FuncSub(Stats.ACCURACY_COMBAT, 128, this, WeaponExpertise.acc[level]));
		_player.addStatFunc(new FuncMul(Stats.CRITICAL_RATE, 112, this, WeaponExpertise.crit_rate[level]));
		_player.addStatFunc(new FuncMul(Stats.CRITICAL_DAMAGE, 112, this, WeaponExpertise.crit_dam[level]));
		_player.addStatFunc(new FuncMul(Stats.POWER_ATTACK, 112, this, WeaponExpertise.power[level]));
		_player.addStatFunc(new FuncMul(Stats.MAGIC_ATTACK, 112, this, WeaponExpertise.magic[level]));
		if(WeaponExpertise.run_dec)
			_player.addStatFunc(new FuncMul(Stats.RUN_SPEED, 112, this, WeaponExpertise.run[level]));
	}

	public void remove()
	{
		_player.removeStatsOwner(this);
	}

	static
	{
		WeaponExpertise.acc = new double[] { 0.0, 16.0, 16.0, 16.0, 16.0 };
		WeaponExpertise.crit_rate = new double[] { 0.0, 0.9, 0.8, 0.7, 0.6 };
		WeaponExpertise.crit_dam = new double[] { 0.0, 0.9, 0.8, 0.7, 0.6 };
		WeaponExpertise.power = new double[] { 0.0, 0.9, 0.8, 0.7, 0.6 };
		WeaponExpertise.magic = new double[] { 0.0, 0.9, 0.8, 0.7, 0.6 };
		WeaponExpertise.run_dec = false;
		WeaponExpertise.run = new double[] { 0.0, 0.8, 0.7, 0.6, 0.5 };
	}
}
