package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.utils.Location;

public class ExShowTrace extends L2GameServerPacket
{
	private final List<Trace> _traces;

	public ExShowTrace()
	{
		_traces = new ArrayList<Trace>();
	}

	public void addTrace(final int x, final int y, final int z, final int time)
	{
		_traces.add(new Trace(x, y, z, time));
	}

	public void addLine(final Location from, final Location to, final int step, final int time)
	{
		this.addLine(from.x, from.y, from.z, to.x, to.y, to.z, step, time);
	}

	public void addLine(final int from_x, final int from_y, final int from_z, final int to_x, final int to_y, final int to_z, final int step, final int time)
	{
		final int x_diff = to_x - from_x;
		final int y_diff = to_y - from_y;
		final int z_diff = to_z - from_z;
		final double xy_dist = Math.sqrt(x_diff * x_diff + y_diff * y_diff);
		final double full_dist = Math.sqrt(xy_dist * xy_dist + z_diff * z_diff);
		final int steps = (int) (full_dist / step);
		this.addTrace(from_x, from_y, from_z, time);
		if(steps > 1)
		{
			final int step_x = x_diff / steps;
			final int step_y = y_diff / steps;
			final int step_z = z_diff / steps;
			for(int i = 1; i < steps; ++i)
				this.addTrace(from_x + step_x * i, from_y + step_y * i, from_z + step_z * i, time);
		}
		this.addTrace(to_x, to_y, to_z, time);
	}

	public void addTrace(final GameObject obj, final int time)
	{
		this.addTrace(obj.getX(), obj.getY(), obj.getZ(), time);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(103);
		writeH(_traces.size());
		for(final Trace t : _traces)
		{
			writeD(t._x);
			writeD(t._y);
			writeD(t._z);
			writeH(t._time);
		}
	}

	static final class Trace
	{
		public final int _x;
		public final int _y;
		public final int _z;
		public final int _time;

		public Trace(final int x, final int y, final int z, final int time)
		{
			_x = x;
			_y = y;
			_z = z;
			_time = time;
		}
	}
}
