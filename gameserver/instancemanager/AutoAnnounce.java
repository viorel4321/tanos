package l2s.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2s.gameserver.Announcements;
import l2s.gameserver.model.AutoAnnounces;

public class AutoAnnounce implements Runnable
{
	private static final Logger _log;
	private static AutoAnnounce _instance;
	private static HashMap<Integer, AutoAnnounces> _lists;

	public static AutoAnnounce getInstance()
	{
		if(AutoAnnounce._instance == null)
			AutoAnnounce._instance = new AutoAnnounce();
		return AutoAnnounce._instance;
	}

	public static void reload()
	{
		AutoAnnounce._instance = new AutoAnnounce();
	}

	public AutoAnnounce()
	{
		AutoAnnounce._lists = new HashMap<Integer, AutoAnnounces>();
		AutoAnnounce._log.info("AutoAnnounce: Initializing...");
		load();
		AutoAnnounce._log.info("AutoAnnounce: Loaded " + AutoAnnounce._lists.size() + " announces.");
	}

	private void load()
	{
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			final File file = new File("./config/Advanced/autoannounce.xml");
			if(!file.exists())
			{
				AutoAnnounce._log.warn("AutoAnnounce: NO FILE (./config/Advanced/autoannounce.xml)");
				return;
			}
			final Document doc = factory.newDocumentBuilder().parse(file);
			int counterAnnounce = 0;
			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if("list".equalsIgnoreCase(n.getNodeName()))
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if("announce".equalsIgnoreCase(d.getNodeName()))
						{
							final ArrayList<String> msg = new ArrayList<String>();
							final NamedNodeMap attrs = d.getAttributes();
							final int delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
							final int repeat = Integer.parseInt(attrs.getNamedItem("repeat").getNodeValue());
							final AutoAnnounces aa = new AutoAnnounces(counterAnnounce);
							for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								if("message".equalsIgnoreCase(cd.getNodeName()))
									msg.add(String.valueOf(cd.getAttributes().getNamedItem("text").getNodeValue()));
							aa.setAnnounce(delay, repeat, msg);
							AutoAnnounce._lists.put(counterAnnounce, aa);
							++counterAnnounce;
						}
		}
		catch(Exception e)
		{
			AutoAnnounce._log.warn("AutoAnnounce: Error parsing autoannounce.xml file. " + e);
		}
	}

	@Override
	public void run()
	{
		if(AutoAnnounce._lists.size() <= 0)
			return;
		for(int i = 0; i < AutoAnnounce._lists.size(); ++i)
			if(AutoAnnounce._lists.get(i).canAnnounce())
			{
				final ArrayList<String> msg = AutoAnnounce._lists.get(i).getMessage();
				for(int c = 0; c < msg.size(); ++c)
					Announcements.getInstance().announceToAll(msg.get(c));
				AutoAnnounce._lists.get(i).updateRepeat();
			}
	}

	static
	{
		_log = LoggerFactory.getLogger(AutoAnnounce.class);
	}
}
