package l2s.gameserver.network.l2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketFloodProtector
{
	private static PacketFloodProtector _instance;
	private static Map<Integer, PacketData> _packetList;
	private static Logger _log;

	public static PacketFloodProtector getInstance()
	{
		if(PacketFloodProtector._instance == null)
			PacketFloodProtector._instance = new PacketFloodProtector();
		return PacketFloodProtector._instance;
	}

	public PacketFloodProtector()
	{
		load();
	}

	public void load()
	{
		if(PacketFloodProtector._packetList == null)
			PacketFloodProtector._packetList = new HashMap<Integer, PacketData>();
		LineNumberReader lnr = null;
		try
		{
			final File dataFile = new File("./config/floodprotect.properties");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(dataFile)));
			PacketFloodProtector._log.info("PacketFloodProtector: initializing...");
			String line;
			while((line = lnr.readLine()) != null)
				if(line.trim().length() != 0)
				{
					if(line.startsWith("#"))
						continue;
					final PacketData pd = parseList(line);
					if(pd == null)
						continue;
					PacketFloodProtector._packetList.put(pd.getPacketId(), pd);
				}
			PacketFloodProtector._log.info("PacketFloodProtector: Loaded " + PacketFloodProtector._packetList.size() + " packets.");
		}
		catch(FileNotFoundException e2)
		{
			PacketFloodProtector._log.warn("PacketFloodProtector: config/floodprotect.properties is missing");
		}
		catch(IOException e)
		{
			PacketFloodProtector._log.error("PacketFloodProtector: error while creating packet flood table.", e);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch(Exception ex)
			{}
		}
	}

	public void reload()
	{
		PacketFloodProtector._packetList = null;
		load();
	}

	private PacketData parseList(final String line)
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		try
		{
			final int packetId = Integer.decode(st.nextToken());
			final int delay = Integer.parseInt(st.nextToken());
			final ActionType action = ActionType.valueOf(st.nextToken());
			return new PacketData(packetId, delay, action);
		}
		catch(Exception e)
		{
			PacketFloodProtector._log.error("FP: parse error: '" + line + "'.", e);
			return null;
		}
	}

	public PacketData getDataByPacketId(final int packetId)
	{
		if(PacketFloodProtector._packetList == null || PacketFloodProtector._packetList.size() == 0)
			return null;
		return PacketFloodProtector._packetList.get(packetId);
	}

	static
	{
		PacketFloodProtector._log = LoggerFactory.getLogger(PacketFloodProtector.class);
	}

	public enum ActionType
	{
		log,
		drop_log,
		kick_log,
		drop,
		none;
	}

	public class PacketData
	{
		private int _packetId;
		private int _delay;
		private ActionType _action;

		public PacketData(final int packetId, final int delay, final ActionType action)
		{
			_packetId = packetId;
			_delay = delay;
			_action = action;
		}

		public int getDelay()
		{
			return _delay;
		}

		public ActionType getAction()
		{
			return _action;
		}

		public int getPacketId()
		{
			return _packetId;
		}
	}
}
