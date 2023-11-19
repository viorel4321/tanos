package l2s.gameserver.model.expertise;

import l2s.gameserver.model.Player;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.funcs.FuncMul;
import l2s.gameserver.skills.funcs.FuncSub;

public class ArmorExpertise
{
	private Player _player;
	public static double[] evas;
	public static double[] power;
	public static double[] magic;
	public static double[] run;

	public ArmorExpertise(final Player player)
	{
		_player = player;
	}

	public void add(final int level)
	{
		_player.addStatFunc(new FuncSub(Stats.EVASION_RATE, 128, this, ArmorExpertise.evas[level]));
		_player.addStatFunc(new FuncMul(Stats.POWER_ATTACK_SPEED, 112, this, ArmorExpertise.power[level]));
		_player.addStatFunc(new FuncMul(Stats.MAGIC_ATTACK_SPEED, 112, this, ArmorExpertise.magic[level]));
		_player.addStatFunc(new FuncMul(Stats.RUN_SPEED, 112, this, ArmorExpertise.run[level]));
	}

	public void remove()
	{
		_player.removeStatsOwner(this);
	}

	static
	{
		ArmorExpertise.evas = new double[] { 0.0, 2.5, 5.0, 7.0, 10.0 };
		ArmorExpertise.power = new double[] { 0.0, 0.8, 0.7, 0.6, 0.5 };
		ArmorExpertise.magic = new double[] { 0.0, 0.8, 0.7, 0.6, 0.5 };
		ArmorExpertise.run = new double[] { 0.0, 0.8, 0.7, 0.6, 0.5 };
	}
}
