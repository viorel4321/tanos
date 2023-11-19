package l2s.gameserver.handler;

import l2s.commons.lang.StatsUtils;
import l2s.gameserver.Config;
import l2s.gameserver.Shutdown;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Functions;

public class Status extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList;
	public static boolean started;

	public Status()
	{
		_commandList = new String[] { "status", "test", "showlic", "test2" };
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(final String command, final Player activeChar, final String target)
	{
		if(command.equals("status"))
		{
			if(Config.COMMAND_STATUS_GM && (!activeChar.isGM() || !activeChar.getPlayerAccess().CanUseStatus))
				return true;
			final StringBuilder ret = new StringBuilder();
			final boolean en = !activeChar.isLangRus();

			int online = 0;
			for(Player player : GameObjectsStorage.getPlayers())
			{
				if(!player.isInOfflineMode())
					online++;
			}

			int max_online = ServerVariables.getInt("RecordOnline", 0);
			if(online > max_online)
			{
				max_online = online;
				ServerVariables.set("RecordOnline", online);
			}
			if(en)
			{
				ret.append("<center><font color=\"LEVEL\">Server status:</font></center>");
				ret.append("<br>Total online:  " + online);
				ret.append("<br1>Record online:  " + max_online);
			}
			else
			{
				ret.append("<center><font color=\"LEVEL\">\u0421\u0442\u0430\u0442\u0443\u0441 \u0441\u0435\u0440\u0432\u0435\u0440\u0430:</font></center>");
				ret.append("<br>\u041e\u043d\u043b\u0430\u0439\u043d \u0441\u0435\u0440\u0432\u0435\u0440\u0430:  " + online);
				ret.append("<br1>\u0420\u0435\u043a\u043e\u0440\u0434 \u043e\u043d\u043b\u0430\u0439\u043d\u0430:  " + max_online);
			}
			if(activeChar.getPlayerAccess().CanRestart)
				ret.append("<br1>" + (en ? "Memory usage" : "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442\u0441\u044f \u043f\u0430\u043c\u044f\u0442\u044c") + ":  ").append(StatsUtils.getMemUsedPerc());
			int mtc = Shutdown.getInstance().getSeconds();
			if(mtc > 0)
			{
				if(en)
					ret.append("<br1>Time to restart: ");
				else
					ret.append("<br1>\u0414\u043e \u0440\u0435\u0441\u0442\u0430\u0440\u0442\u0430: ");
				final int numDays = mtc / 86400;
				mtc -= numDays * 86400;
				final int numHours = mtc / 3600;
				mtc -= numHours * 3600;
				final int numMins = mtc / 60;
				final int numSeconds;
				mtc = numSeconds = mtc - numMins * 60;
				if(numDays > 0)
					ret.append(numDays + "d ");
				if(numHours > 0)
					ret.append(numHours + "h ");
				if(numMins > 0)
					ret.append(numMins + "m ");
				if(numSeconds > 0)
					ret.append(numSeconds + "s");
			}
			else
				ret.append("<br1>" + (en ? "Restart task not launched" : "\u0410\u0432\u0442\u043e\u0440\u0435\u0441\u0442\u0430\u0440\u0442 \u043d\u0435 \u0437\u0430\u043f\u0443\u0449\u0435\u043d") + ".");
			ret.append("<br><center><button value=\"");
			ret.append(en ? "Refresh" : "\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c");
			ret.append("\" action=\"bypass -h user_status\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\" /></center>");
			Functions.show(ret.toString(), activeChar);
			return true;
		}
		else
		{
			if(command.equals("showlic"))
			{
				activeChar.sendMessage(String.valueOf(55501730));
				return true;
			}
			return command.equals("test") || command.equals("test2");
		}
	}
}
