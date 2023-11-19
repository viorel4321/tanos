package l2s.gameserver.tables;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.XMLDocumentFactory;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.player.PlayerTemplate;

public class CharTemplateTable
{
	private static final Logger _log;
	private static final Map<Integer, PlayerTemplate> _templates;

	public static CharTemplateTable getInstance()
	{
		return SingletonHolder._instance;
	}

	protected CharTemplateTable()
	{
		final File mainDir = new File(Config.DATAPACK_ROOT, "data/pts/classes");
		if(!mainDir.isDirectory())
		{
			CharTemplateTable._log.warn("CharTemplateTable: Main dir " + mainDir.getAbsolutePath() + " hasn't been found.");
			return;
		}
		for(final File file : mainDir.listFiles())
			if(file.isFile() && file.getName().endsWith(".xml"))
				loadFileClass(file);
		CharTemplateTable._log.info("CharTemplateTable: Loaded " + CharTemplateTable._templates.size() + " character templates.");
		CharTemplateTable._log.info("CharTemplateTable: Loaded " + SkillTree.getSkillTrees().size() + " classes skills trees.");
	}

	private void loadFileClass(final File f)
	{
		try
		{
			final Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			final Node n = doc.getFirstChild();
			for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				if("class".equalsIgnoreCase(d.getNodeName()))
				{
					NamedNodeMap attrs = d.getAttributes();
					final StatsSet set = new StatsSet();
					final int classId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					final int parentId = Integer.parseInt(attrs.getNamedItem("parentId").getNodeValue());
					String items = null;
					set.set("classId", classId);
					set.set("hpRegen", 0.01);
					set.set("mpRegen", 0.01);
					set.set("baseShldDef", 0);
					set.set("baseShldRate", 0);
					for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						if("set".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							final String name = attrs.getNamedItem("name").getNodeValue().trim();
							final String value = attrs.getNamedItem("val").getNodeValue().trim();
							set.set(name, value);
						}
						else if("skillTrees".equalsIgnoreCase(cd.getNodeName()))
						{
							final List<SkillLearn> skills = new ArrayList<SkillLearn>();
							for(Node cb = cd.getFirstChild(); cb != null; cb = cb.getNextSibling())
							{
								SkillLearn skillLearn = null;
								if("skill".equalsIgnoreCase(cb.getNodeName()))
								{
									attrs = cb.getAttributes();
									final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
									final int lvl = Integer.parseInt(attrs.getNamedItem("lvl").getNodeValue());
									final int minLvl = Integer.parseInt(attrs.getNamedItem("minLvl").getNodeValue());
									final int cost = Integer.parseInt(attrs.getNamedItem("sp").getNodeValue());
									skillLearn = new SkillLearn(id, lvl, minLvl, cost, 0, 0);
									skills.add(skillLearn);
								}
							}
							SkillTree.getInstance().addSkillsToSkillTrees(skills, classId, parentId);
						}
						else if("items".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							items = attrs.getNamedItem("val").getNodeValue().trim();
						}
					set.set("isMale", true);
					PlayerTemplate ct = new PlayerTemplate(set);
					if(items != null)
					{
						final String[] split;
						final String[] itemsSplit = split = items.split(";");
						for(final String element : split)
							ct.addItem(Integer.parseInt(element));
					}
					CharTemplateTable._templates.put(ct.classId.getId(), ct);
					set.set("isMale", false);
					ct = new PlayerTemplate(set);
					if(items != null)
					{
						final String[] split2;
						final String[] itemsSplit = split2 = items.split(";");
						for(final String element : split2)
							ct.addItem(Integer.parseInt(element));
					}
					CharTemplateTable._templates.put(ct.classId.getId() | 0x100, ct);
				}
		}
		catch(Exception e)
		{
			CharTemplateTable._log.error("CharTemplateTable: Error loading from file: " + f.getName(), e);
		}
	}

	public PlayerTemplate getTemplate(final ClassId classId, final boolean female)
	{
		return this.getTemplate(classId.getId(), female);
	}

	public PlayerTemplate getTemplate(final int classId, final boolean female)
	{
		int key = classId;
		if(female)
			key |= 0x100;
		return CharTemplateTable._templates.get(key);
	}

	public static final String getClassNameById(final int classId)
	{
		final PlayerTemplate pcTemplate = CharTemplateTable._templates.get(classId);
		if(pcTemplate == null)
			throw new IllegalArgumentException("No template for classId: " + classId);
		return pcTemplate.className;
	}

	static
	{
		_log = LoggerFactory.getLogger(CharTemplateTable.class);
		_templates = new HashMap<Integer, PlayerTemplate>();
	}

	private static class SingletonHolder
	{
		protected static final CharTemplateTable _instance;

		static
		{
			_instance = new CharTemplateTable();
		}
	}
}
