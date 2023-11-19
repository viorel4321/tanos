package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.Player;

public class ShowBoard extends L2GameServerPacket
{
	private static Logger _log;
	private String _htmlCode;
	private String _id;
	private List<String> _arg;
	static final ShowBoard CACHE_NULL_102;
	static final ShowBoard CACHE_NULL_103;

	public static void separateAndSend(String html, final Player activeChar)
	{
		activeChar.cleanBypasses(true);
		html = activeChar.encodeBypasses(html, true);
		if(html.length() < 4090)
		{
			activeChar.sendPacket(new ShowBoard(html, "101"));
			activeChar.sendPacket(ShowBoard.CACHE_NULL_102);
			activeChar.sendPacket(ShowBoard.CACHE_NULL_103);
		}
		else if(html.length() < 8180)
		{
			activeChar.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			activeChar.sendPacket(new ShowBoard(html.substring(4090, html.length()), "102"));
			activeChar.sendPacket(ShowBoard.CACHE_NULL_103);
		}
		else if(html.length() < 12270)
		{
			activeChar.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			activeChar.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
			activeChar.sendPacket(new ShowBoard(html.substring(8180, html.length()), "103"));
		}
	}

	public static void send1001(final String html, final Player activeChar)
	{
		if(html.length() < 8180)
			activeChar.sendPacket(new ShowBoard(html, "1001"));
	}

	public static void send1002(final Player activeChar, final String string, final String string2, final String string3)
	{
		final List<String> _arg = new ArrayList<String>();
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add(activeChar.getName());
		_arg.add(Integer.toString(activeChar.getObjectId()));
		_arg.add(activeChar.getAccountName());
		_arg.add("9");
		_arg.add(string2);
		_arg.add(string2);
		_arg.add(string);
		_arg.add(string3);
		_arg.add(string3);
		_arg.add("0");
		_arg.add("0");
		activeChar.sendPacket(new ShowBoard(_arg));
	}

	public ShowBoard(final String htmlCode, final String id)
	{
		if(htmlCode != null && htmlCode.length() > 8192)
		{
			ShowBoard._log.warn("Html '" + htmlCode + "' is too long! this will crash the client!");
			_htmlCode = "<html><body>Html was too long</body></html>";
			return;
		}
		_htmlCode = htmlCode;
		_id = id;
	}

	public ShowBoard(final List<String> arg)
	{
		_id = "1002";
		_htmlCode = null;
		_arg = arg;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(110);
		writeC(1);
		writeS("bypass _bbstop");
		writeS("bypass _bbsgetfav");
		writeS("bypass _bbsregion");
		writeS("bypass _bbsclan");
		writeS("bypass _bbsmemo");
		writeS("bypass _bbsmail");
		writeS("bypass _bbsfriends");
		writeS("bypass bbs_addfav");
		String str = _id + "\b";
		if(_id.equals("1002"))
			for(final String arg : _arg)
				str = str + arg + " \b";
		else if(_htmlCode != null)
			str += _htmlCode;
		writeS(str);
	}

	static
	{
		ShowBoard._log = LoggerFactory.getLogger(ShowBoard.class);
		CACHE_NULL_102 = new ShowBoard(null, "102");
		CACHE_NULL_103 = new ShowBoard(null, "103");
	}
}
