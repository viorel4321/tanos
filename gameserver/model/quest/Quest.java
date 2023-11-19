package l2s.gameserver.model.quest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.QuestManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;

public class Quest
{
	public static String SOUND_ITEMGET;
	public static String SOUND_ACCEPT;
	public static String SOUND_MIDDLE;
	public static String SOUND_FINISH;
	public static String SOUND_GIVEUP;
	public static String SOUND_TUTORIAL;
	public static String SOUND_JACKPOT;
	public static String SOUND_HORROR2;
	public static String SOUND_BEFORE_BATTLE;
	public static String SOUND_FANFARE_MIDDLE;
	public static String SOUND_FANFARE2;
	public static String SOUND_BROKEN_KEY;
	public static String SOUND_ENCHANT_SUCESS;
	public static String SOUND_ENCHANT_FAILED;
	public static String SOUND_ED_CHIMES05;
	public static String SOUND_ARMOR_WOOD_3;
	public static String SOUND_ITEM_DROP_EQUIP_ARMOR_CLOTH;
	public static final int ADENA_ID = 57;
	public static final int PARTY_NONE = 0;
	public static final int PARTY_ONE = 1;
	public static final int PARTY_ALL = 2;
	protected static Logger _log;
	private static Map<String, List<QuestTimer>> _allEventTimers;
	private static Map<String, List<QuestTimer>> _allPausedEventTimers;
	private List<Integer> _questitems;
	protected String _descr;
	protected final String _name;
	protected final int _party;
	protected final int _questId;
	public static final int CREATED = 1;
	public static final int STARTED = 2;
	public static final int COMPLETED = 3;

	public void addQuestItem(final int... ids)
	{
		for(final int id : ids)
			if(id != 0)
			{
				ItemTemplate i = null;
				try
				{
					i = ItemTable.getInstance().getTemplate(id);
				}
				catch(Exception e)
				{
					System.out.println("Warning: unknown item " + i + " (" + id + ") in quest drop in " + getName());
				}
				_questitems.add(id);
			}
	}

	public List<Integer> getItems()
	{
		return _questitems;
	}

	public static void updateQuestInDb(final QuestState qs)
	{
		updateQuestVarInDb(qs, "<state>", qs.getStateName());
	}

	public static void updateQuestVarInDb(final QuestState qs, final String var, final String value)
	{
		final Player player = qs.getPlayer();
		if(player == null)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_quests (char_id,id,var,value) VALUES (?,?,?,?)");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setInt(2, qs.getQuest().getId());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			Quest._log.error("could not insert char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static void deleteQuestInDb(final QuestState qs)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND id=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setInt(2, qs.getQuest().getId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			Quest._log.error("could not delete char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static void deleteQuestVarInDb(final QuestState qs, final String var)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND id=? AND var=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setInt(2, qs.getQuest().getId());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			Quest._log.error("could not delete char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static void playerEnter(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement invalidQuestData = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND id=?");
			statement = con.prepareStatement("SELECT id,value FROM character_quests WHERE char_id=? AND var=?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int questId = rset.getInt("id");
				final String state = rset.getString("value");
				if(state.equalsIgnoreCase("Start"))
				{
					invalidQuestData.setInt(1, player.getObjectId());
					invalidQuestData.setInt(2, questId);
					invalidQuestData.executeUpdate();
				}
				else
				{
					final Quest q = QuestManager.getQuest(questId);
					if(q == null)
					{
						if(Config.DONTLOADQUEST)
							continue;
						Quest._log.warn("Unknown quest ID[" + questId + "] for player " + player.getName());
					}
					else
						new QuestState(q, player, getStateId(state));
				}
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT id,var,value FROM character_quests WHERE char_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int questId = rset.getInt("id");
				final String var = rset.getString("var");
				String value = rset.getString("value");
				final QuestState qs = player.getQuestState(questId);
				if(qs == null)
					continue;
				if(var.equals("cond"))
					if(value.equals("null") || value.equals("Completed"))
						value = "0";
					else if(Integer.parseInt(value) < 0)
						value = String.valueOf(Integer.parseInt(value) | 0x1);
				qs.set(var, value, false);
			}
		}
		catch(Exception e)
		{
			Quest._log.error("could not insert char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(invalidQuestData);
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static String getStateName(final int state)
	{
		switch(state)
		{
			case 1:
			{
				return "Start";
			}
			case 2:
			{
				return "Started";
			}
			case 3:
			{
				return "Completed";
			}
			default:
			{
				return "Start";
			}
		}
	}

	public static int getStateId(final String state)
	{
		if(state.equalsIgnoreCase("Start"))
			return 1;
		if(state.equalsIgnoreCase("Started"))
			return 2;
		if(state.equalsIgnoreCase("Completed"))
			return 3;
		return 1;
	}

	public Quest(final boolean party)
	{
		this(null, party ? 1 : 0);
	}

	public Quest(final String descr, final boolean party)
	{
		this(descr, party ? 1 : 0);
	}

	public Quest(final int party)
	{
		this(null, party);
	}

	public Quest(final String descr, final int party)
	{
		_questitems = new ArrayList<Integer>();
		_name = getClass().getSimpleName();
		_questId = Integer.parseInt(_name.split("_")[1]);
		_descr = descr;
		if(_descr == null)
			_descr = getDescr();
		_party = party;
		QuestManager.addQuest(this);
	}

	public Quest(final String descr, final int party, final int questId)
	{
		_questitems = new ArrayList<Integer>();
		_name = getClass().getSimpleName();
		_questId = questId;
		_descr = descr;
		if(_descr == null)
			_descr = getDescr();
		_party = party;
		QuestManager.addQuest(this);
	}

	public void addAttackId(final int... attackIds)
	{
		for(final int attackId : attackIds)
			addEventId(attackId, QuestEventType.MOBGOTATTACKED);
	}

	public NpcTemplate addEventId(final int npcId, final QuestEventType eventType)
	{
		try
		{
			final NpcTemplate t = NpcTable.getTemplate(npcId);
			if(t != null)
				t.addQuestEvent(eventType, this);
			return t;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void addKillId(final int... killIds)
	{
		for(final int killid : killIds)
			addEventId(killid, QuestEventType.MOBKILLED);
	}

	public void addKillId(final Collection<Integer> killIds)
	{
		for(final int killid : killIds)
			addKillId(killid);
	}

	public NpcTemplate addSkillUseId(final int npcId)
	{
		return addEventId(npcId, QuestEventType.MOB_TARGETED_BY_SKILL);
	}

	public void addStartNpc(final int... npcIds)
	{
		for(final int talkId : npcIds)
			addStartNpc(talkId);
	}

	public NpcTemplate addStartNpc(final int npcId)
	{
		addTalkId(npcId);
		return addEventId(npcId, QuestEventType.QUEST_START);
	}

	public void addFirstTalkId(final int... npcIds)
	{
		for(final int npcId : npcIds)
			addEventId(npcId, QuestEventType.NPC_FIRST_TALK);
	}

	public void addTalkId(final int... talkIds)
	{
		for(final int talkId : talkIds)
			addEventId(talkId, QuestEventType.QUEST_TALK);
	}

	public void addTalkId(final Collection<Integer> talkIds)
	{
		for(final int talkId : talkIds)
			addTalkId(talkId);
	}

	public String getDescr(final Player player)
	{
		if(_descr == null)
			return new CustomMessage("q." + _questId).toString(player);
		return _descr;
	}

	public String getDescr()
	{
		if(_descr == null)
			return new CustomMessage("q." + _questId).toString(Language.ENGLISH);
		return _descr;
	}

	public String getName()
	{
		return _name;
	}

	public int getId()
	{
		return _questId;
	}

	public int getParty()
	{
		return _party;
	}

	public QuestState newQuestState(final Player player, final int state)
	{
		final QuestState qs = new QuestState(this, player, state);
		updateQuestInDb(qs);
		return qs;
	}

	public void notifyAttack(final NpcInstance npc, final QuestState qs)
	{
		String res = null;
		try
		{
			res = onAttack(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(npc, qs.getPlayer(), res);
	}

	public void notifyDeath(final Creature killer, final Playable victim, final QuestState qs)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(null, qs.getPlayer(), res);
	}

	public void notifyEvent(final String event, final QuestState qs, final NpcInstance npc)
	{
		String res = null;
		try
		{
			res = onEvent(event, qs, npc);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(npc, qs.getPlayer(), res);
	}

	public void notifyKill(int npcObjectId, int pObjectId)
	{
		ThreadPoolManager.getInstance().schedule(new RewardTask(npcObjectId, pObjectId), Config.QUEST_KILL_DELAY);
	}

	public void notifyPlayerKill(final Player player, final QuestState qs)
	{
		String res = null;
		try
		{
			res = onPlayerKill(player, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(null, qs.getPlayer(), res);
	}

	public final boolean notifyFirstTalk(final NpcInstance npc, final Player player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch(Exception e)
		{
			showError(player, e);
			return true;
		}
		return showResult(npc, player, res);
	}

	public boolean notifyTalk(final NpcInstance npc, final QuestState qs)
	{
		String res = null;
		try
		{
			res = onTalk(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		return showResult(npc, qs.getPlayer(), res);
	}

	public boolean notifySkillUse(final NpcInstance npc, final Skill skill, final QuestState qs)
	{
		String res = null;
		try
		{
			res = onSkillUse(npc, skill, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		return showResult(npc, qs.getPlayer(), res);
	}

	public void notifyPlayerEnter(final QuestState qs)
	{
		try
		{
			onPlayerEnter(qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
		}
	}

	public void onPlayerEnter(final QuestState qs)
	{}

	public String onAttack(final NpcInstance npc, final QuestState qs)
	{
		return null;
	}

	public String onDeath(final Creature killer, final Playable victim, final QuestState qs)
	{
		return null;
	}

	public String onEvent(final String event, final QuestState qs, final NpcInstance npc)
	{
		return null;
	}

	public String onKill(final NpcInstance npc, final QuestState qs)
	{
		return null;
	}

	public String onPlayerKill(final Player killed, final QuestState st)
	{
		return null;
	}

	public String onFirstTalk(final NpcInstance npc, final Player player)
	{
		return null;
	}

	public String onTalk(final NpcInstance npc, final QuestState qs)
	{
		return null;
	}

	public String onSkillUse(final NpcInstance npc, final Skill skill, final QuestState qs)
	{
		return null;
	}

	public void onAbort(final QuestState qs)
	{}

	private void showError(final Player player, final Throwable t)
	{
		Quest._log.error("", t);
		if(player != null && player.isGM())
		{
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			final String res = "<html><body><title>Script error</title>" + sw.toString() + "</body></html>";
			showResult(null, player, res);
		}
	}

	public void showHtmlFile(final Player player, final String fileName)
	{
		showHtmlFile(player, fileName, null, (String[]) null);
	}

	public void showHtmlFile(final Player player, final String fileName, final String toReplace, final String replaceWith)
	{
		showHtmlFile(player, fileName, new String[] { toReplace }, new String[] { replaceWith });
	}

	public void showHtmlFile(final Player player, final String fileName, final String[] toReplace, final String[] replaceWith)
	{
		if(player == null)
			return;

		final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
		npcReply.setFile("quests/" + getClass().getSimpleName() + "/" + fileName);
		npcReply.replace("<?quest_id?>", String.valueOf(getId()));

		if(player.getTarget() != null)
			npcReply.replace("%objectId%", String.valueOf(player.getTarget().getObjectId()));

		if(toReplace != null && replaceWith != null && toReplace.length == replaceWith.length)
			for(int i = 0; i < toReplace.length; ++i)
				npcReply.replace(toReplace[i], replaceWith[i]);

		player.sendPacket(npcReply);
	}

	private void showSimpleHtmFile(final Player player, final String fileName)
	{
		if(player == null)
			return;
		final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
		npcReply.setFile(fileName);
		player.sendPacket(npcReply);
	}

	private boolean showResult(final NpcInstance npc, final Player player, final String res)
	{
		if(res == null)
			return true;
		if(res.isEmpty())
			return false;
		if(res.startsWith("no_quest") || res.equalsIgnoreCase("noquest") || res.equalsIgnoreCase("no-quest"))
			showSimpleHtmFile(player, "no-quest.htm");
		else if(res.equalsIgnoreCase("completed"))
			showSimpleHtmFile(player, "completed-quest.htm");
		else if(res.endsWith(".htm"))
			showHtmlFile(player, res);
		else
		{
			final NpcHtmlMessage npcReply = npc == null ? new NpcHtmlMessage(5) : new NpcHtmlMessage(player, npc);
			npcReply.setHtml(res);
			player.sendPacket(npcReply);
		}
		return true;
	}

	public void removeQuestTimer(final QuestTimer timer)
	{
		if(timer == null)
			return;
		final List<QuestTimer> timers = getQuestTimers(timer.getName());
		if(timers == null)
			return;
		timers.remove(timer);
	}

	public static synchronized void pauseQuestTimes(final Player player)
	{
		final List<QuestTimer> toSleep = new ArrayList<QuestTimer>();
		for(final List<QuestTimer> timers : Quest._allEventTimers.values())
			for(final QuestTimer timer : timers)
				if(timer != null && timer.getPlayer() == player)
					toSleep.add(timer);
		for(final QuestTimer timer2 : toSleep)
		{
			timer2.cancel();
			List<QuestTimer> temp = Quest._allPausedEventTimers.get(timer2.getName());
			if(temp == null)
			{
				temp = new ArrayList<QuestTimer>();
				Quest._allPausedEventTimers.put(timer2.getName(), temp);
			}
			temp.add(timer2);
		}
	}

	public static synchronized void resumeQuestTimers(final Player player)
	{
		final List<QuestTimer> toWakeUp = new ArrayList<QuestTimer>();
		for(final List<QuestTimer> timers : Quest._allPausedEventTimers.values())
			for(final QuestTimer timer : timers)
				if(timer != null && timer.getPlayer() == player)
					toWakeUp.add(timer);
		for(final QuestTimer timer2 : toWakeUp)
		{
			final List<QuestTimer> timers2 = Quest._allPausedEventTimers.get(timer2.getName());
			if(timers2 != null)
				timers2.remove(timer2);
			startQuestTimer(timer2.getQuest(), timer2.getName(), timer2.getTime(), timer2.getNpc(), player);
		}
	}

	public static synchronized QuestTimer getQuestTimer(final Quest quest, final String name, final Player player)
	{
		if(Quest._allEventTimers.get(name) == null)
			return null;
		for(final QuestTimer timer : Quest._allEventTimers.get(name))
			if(timer.isMatch(quest, name, player))
				return timer;
		return null;
	}

	public static List<QuestTimer> getQuestTimers(final String name)
	{
		return Quest._allEventTimers.get(name);
	}

	public void startQuestTimer(final String name, final long time, final NpcInstance npc, final Player player)
	{
		startQuestTimer(this, name, time, npc, player);
	}

	public static synchronized void startQuestTimer(final Quest quest, final String name, final long time, final NpcInstance npc, final Player player)
	{
		List<QuestTimer> timers = getQuestTimers(name);
		if(timers == null)
		{
			timers = new ArrayList<QuestTimer>();
			timers.add(new QuestTimer(quest, name, time, npc, player));
			Quest._allEventTimers.put(name, timers);
		}
		else if(getQuestTimer(quest, name, player) == null)
			timers.add(new QuestTimer(quest, name, time, npc, player));
	}

	public void cancelQuestTimer(final String name, final Player player)
	{
		cancelQuestTimer(this, name, player);
	}

	public static synchronized void cancelQuestTimer(final Quest quest, final String name, final Player player)
	{
		final List<QuestTimer> toRemove = new ArrayList<QuestTimer>();
		for(final List<QuestTimer> timers : Quest._allEventTimers.values())
			for(final QuestTimer timer : timers)
				if(timer.isMatch(quest, name, player))
					toRemove.add(timer);
		for(final QuestTimer timer2 : toRemove)
			timer2.cancel();
	}

	public static synchronized QuestTimer stopQuestTimers(final Player player)
	{
		final List<QuestTimer> toRemove = new ArrayList<QuestTimer>();
		for(final List<QuestTimer> timers : Quest._allEventTimers.values())
			for(final QuestTimer timer : timers)
			{
				final Player pl = timer.getPlayer();
				if(pl == null || pl == player)
					toRemove.add(timer);
			}
		for(final QuestTimer timer2 : toRemove)
			timer2.cancel();
		return null;
	}

	protected String str(final long i)
	{
		return String.valueOf(i);
	}

	public NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int heading, final int randomOffset, final int despawnDelay)
	{
		return addSpawn(npcId, new Location(x, y, z, heading), randomOffset, despawnDelay);
	}

	public NpcInstance addSpawn(final int npcId, final Location loc, final int randomOffset, final int despawnDelay)
	{
		final NpcInstance result = Functions.spawn(randomOffset > 50 ? loc.rnd(50, randomOffset, false) : loc, npcId);
		if(despawnDelay > 0 && result != null)
			ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(result), despawnDelay);
		return result;
	}

	public boolean isVisible(Player player)
	{
		return true;
	}

	public static NpcInstance addSpawnToInstance(final int npcId, final Location loc, final int randomOffset, final int instanceId)
	{
		try
		{
			final NpcTemplate template = NpcTable.getTemplate(npcId);
			if(template != null)
			{
				final NpcInstance npc = NpcTable.getTemplate(npcId).getNewInstance();
				npc.setReflectionId(instanceId);
				npc.setSpawnedLoc(randomOffset > 50 ? loc.rnd(50, randomOffset, false) : loc);
				npc.spawnMe(npc.getSpawnedLoc());
				return npc;
			}
		}
		catch(Exception e1)
		{
			Quest._log.warn("Could not spawn Npc " + npcId);
		}
		return null;
	}

	protected boolean contains(final int[] array, final int id)
	{
		for(final int i : array)
			if(i == id)
				return true;
		return false;
	}

	static
	{
		Quest.SOUND_ITEMGET = "ItemSound.quest_itemget";
		Quest.SOUND_ACCEPT = "ItemSound.quest_accept";
		Quest.SOUND_MIDDLE = "ItemSound.quest_middle";
		Quest.SOUND_FINISH = "ItemSound.quest_finish";
		Quest.SOUND_GIVEUP = "ItemSound.quest_giveup";
		Quest.SOUND_TUTORIAL = "ItemSound.quest_tutorial";
		Quest.SOUND_JACKPOT = "ItemSound.quest_jackpot";
		Quest.SOUND_HORROR2 = "SkillSound5.horror_02";
		Quest.SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
		Quest.SOUND_FANFARE_MIDDLE = "ItemSound.quest_fanfare_middle";
		Quest.SOUND_FANFARE2 = "ItemSound.quest_fanfare_2";
		Quest.SOUND_BROKEN_KEY = "ItemSound2.broken_key";
		Quest.SOUND_ENCHANT_SUCESS = "ItemSound3.sys_enchant_sucess";
		Quest.SOUND_ENCHANT_FAILED = "ItemSound3.sys_enchant_failed";
		Quest.SOUND_ED_CHIMES05 = "AmdSound.ed_chimes_05";
		Quest.SOUND_ARMOR_WOOD_3 = "ItemSound.armor_wood_3";
		Quest.SOUND_ITEM_DROP_EQUIP_ARMOR_CLOTH = "ItemSound.item_drop_equip_armor_cloth";
		Quest._log = LoggerFactory.getLogger(Quest.class);
		Quest._allEventTimers = new HashMap<String, List<QuestTimer>>();
		Quest._allPausedEventTimers = new HashMap<String, List<QuestTimer>>();
	}

	private class RewardTask implements Runnable
	{
		private final int _npcObjectId;
		private final int _playerObjectId;

		public RewardTask(int npcObjectId, int playerObjectId)
		{
			_npcObjectId = npcObjectId;
			_playerObjectId = playerObjectId;
		}

		@Override
		public void run()
		{
			final NpcInstance npc = GameObjectsStorage.getNpc(_npcObjectId);
			final Player p = GameObjectsStorage.getPlayer(_playerObjectId);
			if(npc == null || p == null)
				return;
			final QuestState qs = p.getQuestState(getId());
			if(qs == null || qs.isCompleted())
				return;
			String res = null;
			try
			{
				res = onKill(npc, qs);
			}
			catch(Exception e)
			{
				showError(qs.getPlayer(), e);
				return;
			}
			showResult(npc, qs.getPlayer(), res);
		}
	}

	public class DeSpawnScheduleTimerTask implements Runnable
	{
		NpcInstance _npc;

		public DeSpawnScheduleTimerTask(final NpcInstance npc)
		{
			_npc = null;
			_npc = npc;
		}

		@Override
		public void run()
		{
			if(_npc != null)
				if(_npc.getSpawn() != null)
					_npc.getSpawn().despawnAll();
				else
					_npc.deleteMe();
		}
	}
}
