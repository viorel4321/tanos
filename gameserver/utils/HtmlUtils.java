package l2s.gameserver.utils;

import org.apache.commons.lang3.StringUtils;

public class HtmlUtils
{
	public static final String PREV_BUTTON = "<button value=\"<<\" action=\"bypass %prev_bypass%\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">";
	public static final String NEXT_BUTTON = "<button value=\">>\" action=\"bypass %next_bypass%\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">";

	public static String htmlResidenceName(final int id)
	{
		return "&%" + id + ";";
	}

	public static String htmlNpcName(final int npcId)
	{
		return "&@" + npcId + ";";
	}

	public static String htmlSysString(final int id)
	{
		return "&$" + id + ";";
	}

	public static String htmlItemName(final int itemId)
	{
		return "&#" + itemId + ";";
	}

	public static String htmlClassName(final int classId)
	{
		return "<ClassId>" + classId + "</ClassId>";
	}

	public static String htmlNpcString(final int id, final Object... params)
	{
		String replace = "<fstring";
		if(params.length > 0)
			for(int i = 0; i < params.length; ++i)
				replace = replace + " p" + (i + 1) + "=\"" + String.valueOf(params[i]) + "\"";
		replace = replace + ">" + id + "</fstring>";
		return replace;
	}

	public static String htmlButton(final String value, final String action, final int width)
	{
		return htmlButton(value, action, width, 22);
	}

	public static String htmlButton(final String value, final String action, final int width, final int height)
	{
		return String.format("<button value=\"%s\" action=\"%s\" back=\"sek.cbui94\" width=%d height=%d fore=\"sek.cbui92\">", value, action, width, height);
	}

	public static String bbParse(String s)
	{
		if(s == null)
			return null;
		s = s.replace("\r", "");
		s = StringUtils.replaceAll(s, "<!--((?!TEMPLATE).*?)-->", "");
		s = s.replaceAll("(\\s|\"|'|\\(|^|\n)\\*(.*?)\\*(\\s|\"|'|\\)|\\?|\\.|!|:|;|,|$|\n)", "$1<font color=\"LEVEL\">$2</font>$3");
		s = Strings.replace(s, "^!(.*?)$", 8, "<font color=\"LEVEL\">$1</font>\n\n");
		s = s.replaceAll("%%\\s*\n", "<br1>");
		s = s.replaceAll("\n\n+", "<br>");
		s = Strings.replace(s, "\\[([^\\]\\|]*?)\\|([^\\]]*?)\\]", 32, "<br1><a action=\"bypass -h $1\">$2</a>");
		s = s.replaceAll(" @", "\" msg=\"");
		return s;
	}
}
