package l2s.gameserver.skills;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import l2s.gameserver.templates.item.ItemTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.StatsSet;

public class DocumentItem extends DocumentBase
{
	private Document _doc;

	public DocumentItem(final File file)
	{
		super(file);
		Document doc;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch(Exception e)
		{
			DocumentBase._log.error("Error loading file " + file, e);
			return;
		}
		_doc = doc;
		try
		{
			parseDocument(doc);
		}
		catch(Exception e)
		{
			DocumentBase._log.error("Error in file " + file, e);
		}
	}

	@Override
	protected StatsSet getStatsSet()
	{
		return null;
	}

	@Override
	protected Number getTableValue(final String name)
	{
		return null;
	}

	@Override
	protected Number getTableValue(final String name, final int idx)
	{
		return null;
	}

	@Override
	protected void parseDocument(final Document null_doc)
	{
		for(Node n = _doc.getFirstChild(); n != null; n = n.getNextSibling())
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
					if ("item".equalsIgnoreCase(d.getNodeName()) || "armor".equalsIgnoreCase(d.getNodeName()) || "etcitem".equalsIgnoreCase(d.getNodeName()) || "weapon".equalsIgnoreCase(d.getNodeName())) {
						ItemTemplate template = ItemTable.getInstance().getTemplate(Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue()));
						parseTemplate(d, template);
						for(Node t = d.getFirstChild(); t != null; t = t.getNextSibling()) {
							if ("triggers".equalsIgnoreCase(t.getNodeName()))
								parseTriggers(t, template);
						}
					}
				}
			}
			else if("item".equalsIgnoreCase(n.getNodeName()) || "armor".equalsIgnoreCase(n.getNodeName()) || "etcitem".equalsIgnoreCase(n.getNodeName()) || "weapon".equalsIgnoreCase(n.getNodeName())) {
				ItemTemplate template = ItemTable.getInstance().getTemplate(Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue()));
				parseTemplate(n, template);
				for(Node t = n.getFirstChild(); t != null; t = t.getNextSibling()) {
					if ("triggers".equalsIgnoreCase(t.getNodeName()))
						parseTriggers(t, template);
				}
			}
	}
}
