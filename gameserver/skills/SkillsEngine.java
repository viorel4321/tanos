package l2s.gameserver.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.model.Skill;
import l2s.gameserver.tables.SkillTable;

public class SkillsEngine
{
	private static final Logger _log = LoggerFactory.getLogger(SkillsEngine.class);
	private static List<File> _skillFiles = new ArrayList<File>();

	public static SkillsEngine getInstance()
	{
		return SingletonHolder._instance;
	}

	private SkillsEngine()
	{
		hashFiles("data/stats/skills", _skillFiles);
	}

	private void hashFiles(final String dirname, final List<File> hash)
	{
		final File dir = new File(Config.DATAPACK_ROOT, dirname);
		if(!dir.exists())
		{
			_log.warn("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		final File[] listFiles;
		final File[] files = listFiles = dir.listFiles();
		for(final File f : listFiles)
			if(f.getName().endsWith(".xml"))
				hash.add(f);
	}

	public void loadAllSkills(final TIntObjectHashMap<Skill> allSkills)
	{
		int count = 0;
		for(final File file : _skillFiles)
		{
			final List<Skill> s = loadSkills(file);
			if(s == null)
				continue;
			for(final Skill skill : s)
			{
				allSkills.put(SkillTable.getSkillHashCode(skill), skill);
				++count;
			}
		}
		_log.info("SkillsEngine: Loaded " + count + " Skill templates from XML files.");
	}

	public List<Skill> loadSkills(final File file)
	{
		if(file == null)
		{
			_log.warn("Skill file not found.");
			return null;
		}
		final DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}

	private static class SingletonHolder
	{
		protected static final SkillsEngine _instance = new SkillsEngine();
	}
}
