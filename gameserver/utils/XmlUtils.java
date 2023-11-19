package l2s.gameserver.utils;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Node;

public class XmlUtils
{
	public static SAXReader createReader()
	{
		final SAXReader reader = new SAXReader();
		reader.setValidation(false);
		reader.setIgnoreComments(true);
		return reader;
	}

	@Deprecated
	public static SAXReader createReader(final boolean ignoreComments)
	{
		final SAXReader reader = new SAXReader();
		reader.setValidation(false);
		reader.setIgnoreComments(ignoreComments);
		return reader;
	}

	public static Document readFile(final File file) throws DocumentException
	{
		return createReader().read(file);
	}

	@Deprecated
	public static Document readFile(final File file, final boolean ignoreComments) throws DocumentException
	{
		return createReader(ignoreComments).read(file);
	}

	public static int getIntValue(final Element e, final String name, final int def)
	{
		try
		{
			return Integer.parseInt(e.attributeValue(name));
		}
		catch(NumberFormatException nfe)
		{
			return def;
		}
	}

	public static long getLongValue(final Element e, final String name, final long def)
	{
		try
		{
			return Long.parseLong(e.attributeValue(name));
		}
		catch(NumberFormatException nfe)
		{
			return def;
		}
	}

	public static boolean getBooleanValue(final Element e, final String name, final boolean def)
	{
		if(name == null || e.attributeValue(name) == null)
			return def;
		return Boolean.parseBoolean(e.attributeValue(name));
	}

	public static int[] getIntArray(final Element e, final String name, final String delimeter, final int[] def)
	{
		if(name == null || e.attributeValue(name) == null)
			return def;
		final int[] args = new int[e.attributeValue(name).split(delimeter).length];
		try
		{
			int i = 0;
			for(final String s : e.attributeValue(name).split(delimeter))
				args[i++] = Integer.parseInt(s);
		}
		catch(NumberFormatException nfe)
		{
			nfe.printStackTrace();
			return def;
		}
		return args;
	}

	public static boolean getAttributeBooleanValue(final Node n, final String item, final boolean dflt)
	{
		final Node d = n.getAttributes().getNamedItem(item);
		if(d == null)
			return dflt;
		final String val = d.getNodeValue();
		if(val == null)
			return dflt;
		return Boolean.parseBoolean(val);
	}

	public static int[] getAttributeIntArrayValue(final Node n, final String item, final int[] dflt)
	{
		final Node d = n.getAttributes().getNamedItem(item);
		if(d == null)
			return dflt;
		final String val = d.getNodeValue();
		if(val == null)
			return dflt;
		return Util.parseCommaSeparatedIntegerArray(val);
	}
}
