package l2s.gameserver.skills;

import l2s.gameserver.model.Skill;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 09.02.2019
 * Developed for L2-Scripts.com
 **/
public interface SkillInfo {
	int getId();

	int getDisplayId();

	int getLevel();

	int getDisplayLevel();

	Skill getTemplate();
}
