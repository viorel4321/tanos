package l2s.gameserver.model.quest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ExShowQuestMark;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.QuestList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.TutorialEnableClientEvent;
import l2s.gameserver.network.l2.s2c.TutorialShowHtml;
import l2s.gameserver.network.l2.s2c.TutorialShowQuestionMark;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.SpawnTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.AddonsConfig;
import l2s.gameserver.utils.Log;

public final class QuestState
{
	protected static Logger _log;
	private int ownerObjectId;
	private Quest _quest;
	private int _state;
	private ConcurrentHashMap<String, String> _vars;

	public QuestState(Quest quest, Player player, int state)
	{
		_quest = quest;
		ownerObjectId = player.getObjectId();
		player.setQuestState(this);
		_state = state;
		quest.notifyPlayerEnter(this);
	}

	public void addExpAndSp(final long exp, final long sp)
	{
		this.addExpAndSp(exp, sp, false);
	}

	public void addExpAndSp(final long exp, final long sp, final boolean prof)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		if(!prof || prof && Config.RATE_QUESTS_OCCUPATION_CHANGE)
			player.addExpAndSp((long) (exp * getRateQuestsReward()), (long) (sp * getRateQuestsReward()), false, false);
		else
			player.addExpAndSp(exp, sp, false, false);
	}

	public void addNotifyOfDeath(final Playable playable)
	{
		if(playable != null)
			playable.addNotifyQuestOfDeath(this);
	}

	public void addNotifyOfPlayerKill()
	{
		final Player player = getPlayer();
		if(player != null)
			player.addNotifyOfPlayerKill(this);
	}

	public void removeNotifyOfPlayerKill()
	{
		final Player player = getPlayer();
		if(player != null)
			player.removeNotifyOfPlayerKill(this);
	}

	public void addRadar(final int x, final int y, final int z)
	{
		final Player player = getPlayer();
		if(player != null)
			player.radar.addMarker(x, y, z);
	}

	public void clearRadar()
	{
		final Player player = getPlayer();
		if(player != null)
			player.radar.removeAllMarkers();
	}

	public QuestState exitCurrentQuest(final boolean repeatable)
	{
		final Player player = getPlayer();
		if(player == null)
			return this;
		if(_quest.getItems() != null)
			for(final Integer itemId : _quest.getItems())
			{
				final ItemInstance item = player.getInventory().getItemByItemId(itemId);
				if(item != null)
				{
					if(itemId == 57)
						continue;
					final long count = item.getCount();
					player.getInventory().destroyItemByItemId(itemId, count, true);
					if(!Config.ALLOW_WAREHOUSE)
						continue;
					player.getWarehouse().destroyItem(itemId, count);
				}
			}
		if(repeatable)
		{
			player.delQuestState(_quest.getId());
			Quest.deleteQuestInDb(this);
			_vars = null;
		}
		else
		{
			if(_vars != null && !_vars.isEmpty())
				for(final String var : _vars.keySet())
					if(var != null)
						unset(var);
			setState(3);
			Quest.updateQuestInDb(this);
		}
		player.sendPacket(new QuestList(player));
		return this;
	}

	public void abortQuest()
	{
		_quest.onAbort(this);
		exitCurrentQuest(true);
	}

	public String get(final String var)
	{
		if(_vars == null)
			return null;
		return _vars.get(var);
	}

	public ConcurrentHashMap<String, String> getVars()
	{
		final ConcurrentHashMap<String, String> result = new ConcurrentHashMap<String, String>();
		if(_vars != null)
			result.putAll(_vars);
		return result;
	}

	public int getInt(final String var)
	{
		if(var.equalsIgnoreCase("cond"))
			return getCond();
		return getRawInt(var);
	}

	public int getRawInt(final String var)
	{
		int varint = 0;
		try
		{
			final String val = get(var);
			if(val == null)
				return 0;
			varint = Integer.parseInt(val);
		}
		catch(Exception e)
		{
			QuestState._log.error(getPlayer().getName() + ": variable " + var + " isn't an integer: " + varint, e);
		}
		return varint;
	}

	public int getItemEquipped(final int loc)
	{
		return getPlayer().getInventory().getPaperdollItemId(loc);
	}

	public Player getPlayer()
	{
		return GameObjectsStorage.getPlayer(ownerObjectId);
	}

	public Quest getQuest()
	{
		return _quest;
	}

	public boolean checkQuestItemsCount(final int... itemIds)
	{
		final Player player = getPlayer();
		if(player == null)
			return false;
		for(final int itemId : itemIds)
			if(player.getInventory().getCountOf(itemId) <= 0L)
				return false;
		return true;
	}

	public long getSumQuestItemsCount(final int... itemIds)
	{
		final Player player = getPlayer();
		if(player == null)
			return 0L;
		long count = 0L;
		for(final int itemId : itemIds)
			count += player.getInventory().getCountOf(itemId);
		return count;
	}

	public long getQuestItemsCount(final int itemId)
	{
		final Player player = getPlayer();
		return player == null ? 0L : player.getInventory().getCountOf(itemId);
	}

	public long getQuestItemsCount(final int... itemsIds)
	{
		long result = 0L;
		for(final int id : itemsIds)
			result += this.getQuestItemsCount(id);
		return result;
	}

	public final QuestTimer getQuestTimer(final String name)
	{
		return Quest.getQuestTimer(getQuest(), name, getPlayer());
	}

	public int getState()
	{
		return _state;
	}

	public String getStateName()
	{
		return Quest.getStateName(_state);
	}

	public ItemInstance giveItems(final int itemId, final long count)
	{
		if(itemId == 57)
			return this.giveItems(itemId, count, 0, true);
		return this.giveItems(itemId, count, 0, false);
	}

	public ItemInstance giveItems(final int itemId, final long count, final boolean rate)
	{
		return this.giveItems(itemId, count, 0, rate);
	}

	public ItemInstance giveItems(final int itemId, long count, final int enchantlevel, final boolean rate)
	{
		final Player player = getPlayer();
		if(player == null)
			return null;
		if(count <= 0L)
			count = 1L;
		if(rate)
			if(itemId == 57)
				count *= (long) (getRateQuestsReward() * Config.RATE_DROP_ADENA_MULT_MOD + Config.RATE_DROP_ADENA_STATIC_MOD);
			else
				count *= (long) getRateQuestsReward();
		if(itemId == 57)
			Log.add("Quest|" + getQuest().getId() + "|" + count + "|" + player.getName(), "adena");
		final ItemTemplate template = ItemTable.getInstance().getTemplate(itemId);
		if(template == null)
			return null;
		ItemInstance ret = null;
		if(template.isStackable())
		{
			final ItemInstance item = ItemTable.getInstance().createItem(itemId);
			item.setCount(count);
			ret = player.getInventory().addItem(item);
			if(enchantlevel > 0)
				item.setEnchantLevel(enchantlevel);
		}
		else
			for(int i = 0; i < count; ++i)
			{
				final ItemInstance item2 = ItemTable.getInstance().createItem(itemId);
				ret = player.getInventory().addItem(item2);
				if(enchantlevel > 0)
					item2.setEnchantLevel(enchantlevel);
			}
		player.sendPacket(SystemMessage.obtainItems(template.getItemId(), count, 0));
		player.sendChanges();
		return ret;
	}

	public int rollDrop(final int count, final double calcChance, final boolean prof)
	{
		if(calcChance <= 0.0 || count <= 0)
			return 0;
		return this.rollDrop(count, count, calcChance, prof);
	}

	public int rollDrop(final int min, final int max, double calcChance, final boolean prof)
	{
		if(calcChance <= 0.0 || min <= 0 || max <= 0)
			return 0;
		int dropmult = 1;
		calcChance *= getRateQuestsDrop(prof);
		if(getQuest().getParty() > 0)
		{
			final Player player = getPlayer();
			if(player.getParty() != null)
				calcChance *= Config.ALT_PARTY_QUEST_BONUS[player.getParty().getMemberCountInRange(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) - 1];
		}
		if(calcChance > 100.0)
		{
			if((int) Math.ceil(calcChance / 100.0) <= calcChance / 100.0)
				calcChance = Math.nextUp(calcChance);
			dropmult = (int) Math.ceil(calcChance / 100.0);
			calcChance /= dropmult;
		}
		return Rnd.chance(calcChance) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}

	public float getRateQuestsDrop(final boolean prof)
	{
		final Player player = getPlayer();
		final float Bonus = player == null ? 1.0f : player.getBonus().RATE_QUESTS_DROP;
		return (prof ? Config.RATE_QUESTS_DROP_PROF : Config.RATE_QUESTS_DROP) * Bonus * AddonsConfig.getQuestDropRates(getQuest());
	}

	public float getRateQuestsReward()
	{
		final Player player = getPlayer();
		final float Bonus = player == null ? 1.0f : player.getBonus().RATE_QUESTS_REWARD;
		return Config.RATE_QUESTS_REWARD * Bonus * AddonsConfig.getQuestRewardRates(getQuest());
	}

	public boolean rollAndGive(final int itemId, final int min, final int max, final int limit, final double calcChance)
	{
		return calcChance > 0.0 && min > 0 && max > 0 && limit > 0 && itemId > 0 && this.rollAndGive(itemId, min, max, limit, calcChance, false);
	}

	public boolean rollAndGive(final int itemId, final int min, final int max, final int limit, final double calcChance, final boolean prof)
	{
		if(calcChance <= 0.0 || min <= 0 || max <= 0 || limit <= 0 || itemId <= 0)
			return false;
		long count = this.rollDrop(min, max, calcChance, prof);
		if(count > 0L)
		{
			final long alreadyCount = this.getQuestItemsCount(itemId);
			if(alreadyCount + count > limit)
				count = limit - alreadyCount;
			if(count > 0L)
			{
				this.giveItems(itemId, count, false);
				if(count + alreadyCount >= limit)
				{
					playSound(Quest.SOUND_MIDDLE);
					return true;
				}
				playSound(Quest.SOUND_ITEMGET);
			}
		}
		return false;
	}

	public boolean dropItems(final int itemId, final int min, final int max, final long limit, final double calcChance, final boolean rate)
	{
		if(calcChance <= 0.0 || min <= 0 || max <= 0 || limit <= 0L || itemId <= 0 || !Rnd.chance(rate ? calcChance * AddonsConfig.getQuestDropRates(getQuest()) : calcChance))
			return false;
		long count = Rnd.get(min, max);
		if(rate)
			count *= (long) AddonsConfig.getQuestRewardRates(getQuest());
		if(count > 0L)
		{
			final long alreadyCount = this.getQuestItemsCount(itemId);
			if(alreadyCount >= limit)
				return false;
			if(count + alreadyCount > limit)
				count = limit - alreadyCount;
			this.giveItems(itemId, count, false);
			if(count + alreadyCount >= limit)
			{
				playSound(Quest.SOUND_MIDDLE);
				return true;
			}
			playSound(Quest.SOUND_ITEMGET);
		}
		return false;
	}

	public void rollAndGive(final int itemId, final int min, final int max, final double calcChance)
	{
		if(calcChance <= 0.0 || min <= 0 || max <= 0 || itemId <= 0)
			return;
		this.rollAndGive(itemId, min, max, calcChance, false);
	}

	public void rollAndGive(final int itemId, final int min, final int max, final double calcChance, final boolean prof)
	{
		if(calcChance <= 0.0 || min <= 0 || max <= 0 || itemId <= 0)
			return;
		final int count = this.rollDrop(min, max, calcChance, prof);
		if(count > 0)
		{
			this.giveItems(itemId, count, false);
			playSound(Quest.SOUND_ITEMGET);
		}
	}

	public boolean rollAndGive(final int itemId, final int count, final double calcChance)
	{
		return calcChance > 0.0 && count > 0 && itemId > 0 && this.rollAndGive(itemId, count, calcChance, false);
	}

	public boolean rollAndGive(final int itemId, final int count, final double calcChance, final boolean prof)
	{
		if(calcChance <= 0.0 || count <= 0 || itemId <= 0)
			return false;
		final int countToDrop = this.rollDrop(count, calcChance, prof);
		if(countToDrop > 0)
		{
			this.giveItems(itemId, countToDrop, false);
			playSound(Quest.SOUND_ITEMGET);
			return true;
		}
		return false;
	}

	public boolean isCompleted()
	{
		return getState() == 3;
	}

	public boolean isStarted()
	{
		return getState() != 1 && getState() != 3;
	}

	public void killNpcByObjectId(final int _objId)
	{
		final NpcInstance npc = GameObjectsStorage.getNpc(_objId);
		if(npc != null)
			npc.doDie(null);
		else
			QuestState._log.warn("Attemp to kill object that is not npc in quest " + getQuest().getId());
	}

	public String set(final String var, final String val)
	{
		if(var.equalsIgnoreCase("cond"))
			return setCond(Integer.parseInt(val));
		return this.set(var, val, true);
	}

	public String set(final String var, final int intval)
	{
		if(var.equalsIgnoreCase("cond"))
			return setCond(intval);
		return this.set(var, String.valueOf(intval), true);
	}

	public String set(final String var, String val, final boolean store)
	{
		if(_vars == null)
			_vars = new ConcurrentHashMap<String, String>();
		if(val == null)
			val = "";
		_vars.put(var, val);
		if(store)
		{
			final Player player = getPlayer();
			if(player == null)
				return null;
			Quest.updateQuestVarInDb(this, var, val);
			if(var.equalsIgnoreCase("cond"))
			{
				player.sendPacket(new QuestList(player));
				if(!val.equals("0") && getQuest().isVisible(player) && isStarted())
					player.sendPacket(new ExShowQuestMark(getQuest().getId()));
			}
		}
		return val;
	}

	public Object setState(final int state)
	{
		final Player player = getPlayer();
		if(player == null)
			return null;
		_state = state;
		if(getQuest().isVisible(player) && isStarted())
			player.sendPacket(new ExShowQuestMark(getQuest().getId()));
		Quest.updateQuestInDb(this);
		player.sendPacket(new QuestList(player));
		return state;
	}

	public void removeRadar(final int x, final int y, final int z)
	{
		final Player player = getPlayer();
		if(player != null)
			player.radar.removeMarker(x, y, z);
	}

	public void playSound(final String sound)
	{
		final Player player = getPlayer();
		if(player != null)
			player.sendPacket(new PlaySound(sound));
	}

	public void playTutorialVoice(final String voice)
	{
		final Player player = getPlayer();
		if(player != null)
			player.sendPacket(new PlaySound(2, voice, 0, 0, player.getLoc()));
	}

	public void onTutorialClientEvent(final int number)
	{
		final Player player = getPlayer();
		if(player != null)
			player.sendPacket(new TutorialEnableClientEvent(number));
	}

	public void showQuestionMark(final int number)
	{
		final Player player = getPlayer();
		if(player != null)
			player.sendPacket(new TutorialShowQuestionMark(number));
	}

	public void showTutorialHTML(final String html)
	{
		final Player player = getPlayer();
		if(player == null)
			return;
		final String text = HtmCache.getInstance().getHtml("quests/_255_Tutorial/" + html, player);
		player.sendPacket(new TutorialShowHtml(text));
	}

	public void startQuestTimer(final String name, final long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer());
	}

	public long takeItems(final int itemId, long count)
	{
		final Player player = getPlayer();
		if(player == null)
			return 0L;
		final ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if(item == null)
			return 0L;
		if(count < 0L || count > item.getCount())
			count = item.getCount();
		player.getInventory().destroyItemByItemId(itemId, count, true);
		player.sendPacket(SystemMessage.removeItems(itemId, count));
		return count;
	}

	public long takeAllItems(final int itemId)
	{
		return takeItems(itemId, -1L);
	}

	public long takeAllItems(final int... itemsIds)
	{
		long result = 0L;
		for(final int id : itemsIds)
			result += this.takeAllItems(id);
		return result;
	}

	public long takeAllItems(final short... itemsIds)
	{
		long result = 0L;
		for(final int id : itemsIds)
			result += this.takeAllItems(id);
		return result;
	}

	public long takeAllItems(final Collection<Integer> itemsIds)
	{
		long result = 0L;
		for(final int id : itemsIds)
			result += this.takeAllItems(id);
		return result;
	}

	public String unset(final String var)
	{
		if(_vars == null || var == null)
			return null;
		final String old = _vars.remove(var);
		if(old != null)
			Quest.deleteQuestVarInDb(this, var);
		return old;
	}

	private boolean checkPartyMember(final Player member, final int state, final int maxrange, final GameObject rangefrom)
	{
		if(member == null)
			return false;
		if(rangefrom != null && maxrange > 0 && !member.isInRange(rangefrom, maxrange))
			return false;
		final QuestState qs = member.getQuestState(getQuest().getId());
		return qs != null && qs.getState() == state;
	}

	public List<Player> getPartyMembers(final int state, final int maxrange, final GameObject rangefrom)
	{
		final List<Player> result = new ArrayList<Player>();
		final Party party = getPlayer().getParty();
		if(party == null)
		{
			if(checkPartyMember(getPlayer(), state, maxrange, rangefrom))
				result.add(getPlayer());
			return result;
		}
		for(final Player _member : party.getPartyMembers())
			if(checkPartyMember(_member, state, maxrange, rangefrom))
				result.add(getPlayer());
		return result;
	}

	public Player getRandomPartyMember(final int state, final int maxrangefromplayer)
	{
		return this.getRandomPartyMember(state, maxrangefromplayer, getPlayer());
	}

	public Player getRandomPartyMember(final int state, final int maxrange, final GameObject rangefrom)
	{
		final List<Player> list = getPartyMembers(state, maxrange, rangefrom);
		if(list.size() == 0)
			return null;
		return list.get(Rnd.get(list.size()));
	}

	public NpcInstance addSpawn(final int npcId)
	{
		return this.addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, 0, 0);
	}

	public NpcInstance addSpawn(final int npcId, final int despawnDelay)
	{
		return this.addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, 0, despawnDelay);
	}

	public NpcInstance addSpawn(final int npcId, final int x, final int y, final int z)
	{
		return this.addSpawn(npcId, x, y, z, 0, 0, 0);
	}

	public NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int despawnDelay)
	{
		return this.addSpawn(npcId, x, y, z, 0, 0, despawnDelay);
	}

	public NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int heading, final int randomOffset, final int despawnDelay)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
	}

	public NpcInstance findTemplate(final int npcId)
	{
		for(final Spawn spawn : SpawnTable.getInstance().getSpawnTable())
			if(spawn != null && spawn.getNpcId() == npcId)
				return spawn.getLastSpawn();
		return null;
	}

	public int calculateLevelDiffForDrop(final int mobLevel, final int player)
	{
		if(!Config.DEEPBLUE_DROP_RULES)
			return 0;
		return Math.max(player - mobLevel - Config.DEEPBLUE_DROP_MAXDIFF, 0);
	}

	public int getCond()
	{
		final int value = getRawInt("cond");
		if(value < 0)
			return bitToInt(value);
		return value;
	}

	private int bitToInt(final int value)
	{
		for(int i = 30; i >= 0; --i)
			if((value & 1 << i) > 0)
				return i + 1;
		return 0;
	}

	public String setCond(final int newCond)
	{
		int oldCond = getRawInt("cond");
		if(oldCond < 0)
		{
			final int oldCondStd = bitToInt(oldCond);
			if(newCond < oldCondStd)
			{
				for(int i = oldCondStd - 1; i >= newCond; --i)
					if((oldCond & 1 << i) > 0)
						oldCond ^= 1 << i;
				return this.set("cond", String.valueOf(oldCond), true);
			}
			return this.set("cond", String.valueOf(oldCond | 1 << newCond - 1), true);
		}
		else
		{
			if(oldCond >= newCond - 1)
				return this.set("cond", String.valueOf(newCond), true);
			return this.set("cond", String.valueOf((1 << oldCond) - 1 | 1 << newCond - 1 | 0x80000001), true);
		}
	}

	static
	{
		QuestState._log = LoggerFactory.getLogger(Quest.class);
	}
}
