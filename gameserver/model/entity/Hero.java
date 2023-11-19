package l2s.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.CharTemplateTable;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.StatsSet;

public class Hero
{
	private static Logger _log;
	private static Hero _instance;
	private static final String GET_HEROES = "SELECT * FROM heroes WHERE played = 1";
	private static final String GET_ALL_HEROES = "SELECT * FROM heroes";
	private static Map<Integer, StatsSet> _heroes;
	private static Map<Integer, StatsSet> _completeHeroes;
	private static Map<Integer, List<HeroDiary>> _herodiary;
	private static Map<Integer, String> _heroMessage;
	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";

	public static Hero getInstance()
	{
		if(Hero._instance == null)
			Hero._instance = new Hero();
		return Hero._instance;
	}

	public Hero()
	{
		init();
	}

	private static void HeroSetClanAndAlly(final int charId, final StatsSet hero)
	{
		Map.Entry<Clan, Alliance> e = ClanTable.getInstance().getClanAndAllianceByCharId(charId);
		hero.set("clan_crest", e.getKey() == null ? 0 : e.getKey().getCrestId());
		hero.set("clan_name", e.getKey() == null ? "" : e.getKey().getName());
		hero.set("ally_crest", e.getValue() == null ? 0 : e.getValue().getAllyCrestId());
		hero.set("ally_name", e.getValue() == null ? "" : e.getValue().getAllyName());
		e = null;
	}

	private void init()
	{
		Hero._heroes = new ConcurrentHashMap<Integer, StatsSet>();
		Hero._completeHeroes = new ConcurrentHashMap<Integer, StatsSet>();
		Hero._herodiary = new ConcurrentHashMap<Integer, List<HeroDiary>>();
		Hero._heroMessage = new ConcurrentHashMap<Integer, String>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM heroes WHERE played = 1");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final StatsSet hero = new StatsSet();
				final int charId = rset.getInt("char_id");
				hero.set("char_name", rset.getString("char_name"));
				hero.set("class_id", rset.getInt("class_id"));
				hero.set("count", rset.getInt("count"));
				hero.set("played", rset.getInt("played"));
				HeroSetClanAndAlly(charId, hero);
				loadDiary(charId);
				loadMessage(charId);
				Hero._heroes.put(charId, hero);
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT * FROM heroes");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final StatsSet hero = new StatsSet();
				final int charId = rset.getInt("char_id");
				hero.set("char_name", rset.getString("char_name"));
				hero.set("class_id", rset.getInt("class_id"));
				hero.set("count", rset.getInt("count"));
				hero.set("played", rset.getInt("played"));
				HeroSetClanAndAlly(charId, hero);
				Hero._completeHeroes.put(charId, hero);
			}
		}
		catch(SQLException e)
		{
			Hero._log.warn("Hero System: Couldn't load Heroes " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		Hero._log.info("Hero System: Loaded " + Hero._heroes.size() + " Heroes.");
		Hero._log.info("Hero System: Loaded " + Hero._completeHeroes.size() + " all time Heroes.");
	}

	public Map<Integer, StatsSet> getHeroes()
	{
		return Hero._heroes;
	}

	public synchronized void clearHeroes()
	{
		mysql.set("UPDATE heroes SET played = 0");
		if(!Hero._completeHeroes.isEmpty())
			for(final Integer heroId : Hero._completeHeroes.keySet())
				Hero._completeHeroes.get(heroId).set("played", 0);
		if(!Hero._heroes.isEmpty())
			for(final StatsSet hero : Hero._heroes.values())
			{
				final String name = hero.getString("char_name");
				final Player player = World.getPlayer(name);
				if(player != null)
				{
					player.setHero(false);
					removeSkills(player);
					for(final ItemInstance item : player.getInventory().getItems())
						if(item != null && item.isHeroWeapon())
							player.getInventory().destroyItem(item);
					player.updatePledgeClass();
					player.broadcastUserInfo(true);
				}
			}
		Hero._heroes.clear();
		Hero._herodiary.clear();
	}

	public synchronized boolean computeNewHeroes(final List<StatsSet> newHeroes)
	{
		if(newHeroes.isEmpty())
			return true;
		final List<Integer> heroes = new ArrayList<Integer>();
		for(final StatsSet hero : newHeroes)
		{
			final int charId = hero.getInteger("char_id");
			if(Hero._completeHeroes.containsKey(charId))
			{
				final StatsSet oldHero = Hero._completeHeroes.get(charId);
				final int count = oldHero.getInteger("count");
				oldHero.set("count", count + 1);
				oldHero.set("played", -1);
				Hero._completeHeroes.remove(charId);
				Hero._completeHeroes.put(charId, oldHero);
			}
			else
			{
				final StatsSet newHero = new StatsSet();
				newHero.set("char_name", hero.getString("char_name"));
				newHero.set("class_id", hero.getInteger("class_id"));
				newHero.set("count", 1);
				newHero.set("played", -1);
				HeroSetClanAndAlly(charId, newHero);
				Hero._completeHeroes.put(charId, newHero);
			}
			addHeroDiary(charId, 2, 0);
			loadDiary(charId);
			heroes.add(charId);
		}
		for(final int id : heroes)
			updateHero(id);
		return false;
	}

	private void updateHero(final int id)
	{
		if(!Hero._completeHeroes.containsKey(id))
			return;
		final StatsSet hero = Hero._completeHeroes.get(id);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO heroes (char_id, char_name, class_id, count, played) VALUES (?,?,?,?,?)");
			statement.setInt(1, id);
			statement.setString(2, hero.getString("char_name"));
			statement.setInt(3, hero.getInteger("class_id"));
			statement.setInt(4, hero.getInteger("count"));
			statement.setInt(5, hero.getInteger("played"));
			statement.execute();
		}
		catch(SQLException e)
		{
			Hero._log.error("Hero System: Couldn't update Heroes", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void becomeHero(final Player player)
	{
		final int objId = player.getObjectId();
		final StatsSet hero = Hero._completeHeroes.get(objId);
		hero.set("played", 1);
		HeroSetClanAndAlly(objId, hero);
		Hero._heroes.remove(objId);
		Hero._heroes.put(objId, hero);
		Hero._completeHeroes.remove(objId);
		Hero._completeHeroes.put(objId, hero);
		player.setHero(true);
		addSkills(player);
		player.sendPacket(new SkillList(player));
		player.updatePledgeClass();
		player.broadcastPacket(new SocialAction(objId, 16));
		updateHero(objId);
		Announcements.getInstance().announceToAll(player.getName() + " has become a " + CharTemplateTable.getClassNameById(player.getBaseClassId()) + " hero. Congratulations.");
		final Clan clan = player.getClan();
		if(clan != null && clan.getLevel() >= 5)
		{
			clan.incReputation(Config.HERO_CLAN_REP, false, "Hero");
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			clan.broadcastToOtherOnlineMembers(new SystemMessage(1776).addString(player.getName()).addNumber(Integer.valueOf(Config.HERO_CLAN_REP)), player);
		}
		if(Config.HERO_ITEMS.length > 1)
		{
			for(int i = 0; i < Config.HERO_ITEMS.length; i += 2)
			{
				player.getInventory().addItem(Config.HERO_ITEMS[i], Config.HERO_ITEMS[i + 1]);
				player.sendPacket(SystemMessage.obtainItems(Config.HERO_ITEMS[i], Config.HERO_ITEMS[i + 1], 0));
			}
			player.sendPacket(new ItemList(player, false));
		}
		player.broadcastUserInfo(true);
	}

	public static boolean isNotActiveHero(final int id)
	{
		return !Hero._completeHeroes.isEmpty() && Hero._completeHeroes.containsKey(id) && Hero._completeHeroes.get(id).getInteger("played") < 0;
	}

	public static boolean isPlayedHero(final int id)
	{
		return !Hero._completeHeroes.isEmpty() && Hero._completeHeroes.containsKey(id) && Hero._completeHeroes.get(id).getInteger("played") > 0;
	}

	public static void addSkills(final Player player)
	{
		player.addSkill(SkillTable.getInstance().getInfo(395, 1));
		player.addSkill(SkillTable.getInstance().getInfo(396, 1));
		player.addSkill(SkillTable.getInstance().getInfo(1374, 1));
		player.addSkill(SkillTable.getInstance().getInfo(1375, 1));
		player.addSkill(SkillTable.getInstance().getInfo(1376, 1));
		if(Config.NO_HERO_SKILLS_SUB)
			if(player.isSubClassActive())
				unActivateHeroSkills(player);
			else
				activateHeroSkills(player);
	}

	public static void removeSkills(final Player player)
	{
		player.removeSkillById(395);
		player.removeSkillById(396);
		player.removeSkillById(1374);
		player.removeSkillById(1375);
		player.removeSkillById(1376);
	}

	public static void activateHeroSkills(final Player player)
	{
		if(!player.isHero())
			return;
		player.removeUnActiveSkill(SkillTable.getInstance().getInfo(395, 1));
		player.removeUnActiveSkill(SkillTable.getInstance().getInfo(396, 1));
		player.removeUnActiveSkill(SkillTable.getInstance().getInfo(1374, 1));
		player.removeUnActiveSkill(SkillTable.getInstance().getInfo(1375, 1));
		player.removeUnActiveSkill(SkillTable.getInstance().getInfo(1376, 1));
	}

	public static void unActivateHeroSkills(final Player player)
	{
		if(!player.isHero())
			return;
		player.addUnActiveSkill(SkillTable.getInstance().getInfo(395, 1));
		player.addUnActiveSkill(SkillTable.getInstance().getInfo(396, 1));
		player.addUnActiveSkill(SkillTable.getInstance().getInfo(1374, 1));
		player.addUnActiveSkill(SkillTable.getInstance().getInfo(1375, 1));
		player.addUnActiveSkill(SkillTable.getInstance().getInfo(1376, 1));
	}

	public void loadDiary(final int charId)
	{
		final List<HeroDiary> diary = new ArrayList<HeroDiary>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
			statement.setInt(1, charId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				final long time = rset.getLong("time");
				final int action = rset.getInt("action");
				final int param = rset.getInt("param");
				final HeroDiary d = new HeroDiary(action, time, param);
				diary.add(d);
			}
			Hero._herodiary.put(charId, diary);
			if(Config.DEBUG)
				Hero._log.info("Hero System: Loaded " + diary.size() + " diary entries for Hero(object id: #" + charId + ")");
		}
		catch(SQLException e)
		{
			Hero._log.warn("Hero System: Couldn't load Hero Diary for CharId: " + charId);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void showHeroDiary(final Player activeChar, final int heroclass, final int charid, final int page)
	{
		final int perpage = 10;
		final List<HeroDiary> mainlist = Hero._herodiary.get(charid);
		if(mainlist != null)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(activeChar, null);
			String t;
			if(activeChar.isLangRus())
				t = "\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0413\u0435\u0440\u043e\u044f %heroname%: %message%";
			else
				t = "Hero %heroname%'s Message: %message%";
			html.setFile("olympiad/monument_hero_info.htm");
			html.replace("%title%", t);
			html.replace("%heroname%", Olympiad.getNobleName(charid));
			html.replace("%message%", Hero._heroMessage.get(charid) != null ? Hero._heroMessage.get(charid) : "");
			final List<HeroDiary> list = new ArrayList<HeroDiary>(mainlist);
			Collections.reverse(list);
			boolean color = true;
			final StringBuilder fList = new StringBuilder(500);
			int counter = 0;
			int breakat = 0;
			for(int i = (page - 1) * 10; i < list.size(); ++i)
			{
				breakat = i;
				final HeroDiary diary = list.get(i);
				final Map.Entry<String, String> entry = diary.toString(activeChar);
				fList.append("<tr><td>");
				if(color)
					fList.append("<table width=270 bgcolor=\"131210\">");
				else
					fList.append("<table width=270>");
				fList.append("<tr><td width=270><font color=\"LEVEL\">" + entry.getKey() + "</font></td></tr>");
				fList.append("<tr><td width=270>" + entry.getValue() + "</td></tr>");
				fList.append("<tr><td>&nbsp;</td></tr></table>");
				fList.append("</td></tr>");
				color = !color;
				if(++counter >= 10)
					break;
			}
			if(breakat < list.size() - 1)
			{
				html.replace("%buttprev%", "<button value=\"<<\" action=\"bypass %prev_bypass%\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">");
				html.replace("%prev_bypass%", "_diary?class=" + heroclass + "&page=" + (page + 1));
			}
			else
				html.replace("%buttprev%", "");
			if(page > 1)
			{
				html.replace("%buttnext%", "<button value=\">>\" action=\"bypass %next_bypass%\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">");
				html.replace("%next_bypass%", "_diary?class=" + heroclass + "&page=" + (page - 1));
			}
			else
				html.replace("%buttnext%", "");
			html.replace("%list%", fList.toString());
			activeChar.sendPacket(html);
		}
	}

	public void addHeroDiary(final int playerId, final int id, final int param)
	{
		insertHeroDiary(playerId, id, param);
		final List<HeroDiary> list = Hero._herodiary.get(playerId);
		if(list != null)
			list.add(new HeroDiary(id, System.currentTimeMillis(), param));
	}

	private void insertHeroDiary(final int charId, final int action, final int param)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
			statement.setInt(1, charId);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, action);
			statement.setInt(4, param);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			Hero._log.warn("SQL exception while saving DiaryData.");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void loadMessage(final int charId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			String message = null;
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT message FROM heroes WHERE char_id=?");
			statement.setInt(1, charId);
			rset = statement.executeQuery();
			rset.next();
			message = rset.getString("message");
			Hero._heroMessage.put(charId, message);
		}
		catch(SQLException e)
		{
			Hero._log.warn("Hero System: Couldn't load Hero Message for CharId: " + charId);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void setHeroMessage(final int charId, final String message)
	{
		Hero._heroMessage.put(charId, message);
	}

	public void saveHeroMessage(final int charId)
	{
		if(Hero._heroMessage.get(charId) == null)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE heroes SET message=? WHERE char_id=?;");
			statement.setString(1, Hero._heroMessage.get(charId));
			statement.setInt(2, charId);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			Hero._log.warn("SQL exception while saving HeroMessage.");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void shutdown()
	{
		for(final int charId : Hero._heroMessage.keySet())
			saveHeroMessage(charId);
	}

	public int getHeroByClass(final int classid)
	{
		if(!Hero._heroes.isEmpty())
			for(final Integer heroId : Hero._heroes.keySet())
			{
				final StatsSet hero = Hero._heroes.get(heroId);
				if(hero.getInteger("class_id") == classid)
					return heroId;
			}
		return 0;
	}

	public static void changeHeroName(final int id, final String n)
	{
		boolean upd = false;
		if(Hero._completeHeroes.containsKey(id))
		{
			final StatsSet hero = Hero._completeHeroes.get(id);
			hero.set("char_name", n);
			Hero._completeHeroes.remove(id);
			Hero._completeHeroes.put(id, hero);
			upd = true;
		}
		if(Hero._heroes.containsKey(id))
		{
			final StatsSet hero = Hero._heroes.get(id);
			hero.set("char_name", n);
			Hero._heroes.remove(id);
			Hero._heroes.put(id, hero);
			upd = true;
		}
		if(upd)
			mysql.set("UPDATE heroes SET char_name=? WHERE char_id=? LIMIT 1", n, id);
	}

	public static void deleteHero(final int id)
	{
		if(Hero._completeHeroes.containsKey(id))
			Hero._completeHeroes.remove(id);
		if(Hero._heroes.containsKey(id))
			Hero._heroes.remove(id);
	}

	public static void deleteDiary(final int id)
	{
		if(Hero._herodiary.containsKey(id))
			Hero._herodiary.remove(id);
		if(Hero._heroMessage.containsKey(id))
			Hero._heroMessage.remove(id);
	}

	static
	{
		Hero._log = LoggerFactory.getLogger(Hero.class);
	}
}
