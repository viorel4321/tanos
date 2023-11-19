package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class FeedableBeastInstance extends MonsterInstance
{
	private static Logger _log = LoggerFactory.getLogger(FeedableBeastInstance.class);

	public Map<Integer, growthInfo> growthCapableMobs;
	List<Integer> tamedBeasts;
	List<Integer> feedableBeasts;
	public static Map<Integer, Integer> feedInfo;
	private static int GOLDEN_SPICE;
	private static int CRYSTAL_SPICE;
	private static int SKILL_GOLDEN_SPICE;
	private static int SKILL_CRYSTAL_SPICE;
	private static String[][] text;
	private static String[] mytext;

	public FeedableBeastInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		growthCapableMobs = new HashMap<Integer, growthInfo>();
		tamedBeasts = new ArrayList<Integer>();
		feedableBeasts = new ArrayList<Integer>();
		growthCapableMobs.put(21451, new growthInfo(0, new int[][][] {
				{ { 21452, 21453, 21454, 21455 } },
				{ { 21456, 21457, 21458, 21459 } } }, Config.TAMED_X05));
		growthCapableMobs.put(21452, new growthInfo(1, new int[][][] { { { 21460, 21462 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21453, new growthInfo(1, new int[][][] { { { 21461, 21463 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21454, new growthInfo(1, new int[][][] { { { 21460, 21462 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21455, new growthInfo(1, new int[][][] { { { 21461, 21463 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21456, new growthInfo(1, new int[][][] { new int[0][], { { 21464, 21466 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21457, new growthInfo(1, new int[][][] { new int[0][], { { 21465, 21467 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21458, new growthInfo(1, new int[][][] { new int[0][], { { 21464, 21466 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21459, new growthInfo(1, new int[][][] { new int[0][], { { 21465, 21467 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21460, new growthInfo(2, new int[][][] {
				{ { 21468, 21824 }, { 16017, 16018 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21461, new growthInfo(2, new int[][][] {
				{ { 21469, 21825 }, { 16017, 16018 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21462, new growthInfo(2, new int[][][] {
				{ { 21468, 21824 }, { 16017, 16018 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21463, new growthInfo(2, new int[][][] {
				{ { 21469, 21825 }, { 16017, 16018 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21464, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21468, 21824 }, { 16017, 16018 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21465, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21469, 21825 }, { 16017, 16018 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21466, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21468, 21824 }, { 16017, 16018 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21467, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21469, 21825 }, { 16017, 16018 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21470, new growthInfo(0, new int[][][] {
				{ { 21471, 21472, 21473, 21474 } },
				{ { 21475, 21476, 21477, 21478 } } }, Config.TAMED_X05));
		growthCapableMobs.put(21471, new growthInfo(1, new int[][][] { { { 21479, 21481 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21472, new growthInfo(1, new int[][][] { { { 21480, 21482 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21473, new growthInfo(1, new int[][][] { { { 21479, 21481 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21474, new growthInfo(1, new int[][][] { { { 21480, 21482 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21475, new growthInfo(1, new int[][][] { new int[0][], { { 21483, 21485 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21476, new growthInfo(1, new int[][][] { new int[0][], { { 21484, 21486 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21477, new growthInfo(1, new int[][][] { new int[0][], { { 21483, 21485 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21478, new growthInfo(1, new int[][][] { new int[0][], { { 21484, 21486 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21479, new growthInfo(2, new int[][][] {
				{ { 21487, 21826 }, { 16013, 16014 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21480, new growthInfo(2, new int[][][] {
				{ { 21488, 21827 }, { 16013, 16014 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21481, new growthInfo(2, new int[][][] {
				{ { 21487, 21826 }, { 16013, 16014 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21482, new growthInfo(2, new int[][][] {
				{ { 21488, 21827 }, { 16013, 16014 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21483, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21487, 21826 }, { 16013, 16014 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21484, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21488, 21827 }, { 16013, 16014 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21485, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21487, 21826 }, { 16013, 16014 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21486, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21488, 21827 }, { 16013, 16014 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21489, new growthInfo(0, new int[][][] {
				{ { 21490, 21491, 21492, 21493 } },
				{ { 21494, 21495, 21496, 21497 } } }, Config.TAMED_X05));
		growthCapableMobs.put(21490, new growthInfo(1, new int[][][] { { { 21498, 21500 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21491, new growthInfo(1, new int[][][] { { { 21499, 21501 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21492, new growthInfo(1, new int[][][] { { { 21498, 21500 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21493, new growthInfo(1, new int[][][] { { { 21499, 21501 } }, new int[0][] }, Config.TAMED_X1));
		growthCapableMobs.put(21494, new growthInfo(1, new int[][][] { new int[0][], { { 21502, 21504 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21495, new growthInfo(1, new int[][][] { new int[0][], { { 21503, 21505 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21496, new growthInfo(1, new int[][][] { new int[0][], { { 21502, 21504 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21497, new growthInfo(1, new int[][][] { new int[0][], { { 21503, 21505 } } }, Config.TAMED_X1));
		growthCapableMobs.put(21498, new growthInfo(2, new int[][][] {
				{ { 21506, 21828 }, { 16015, 16016 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21499, new growthInfo(2, new int[][][] {
				{ { 21507, 21829 }, { 16015, 16016 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21500, new growthInfo(2, new int[][][] {
				{ { 21506, 21828 }, { 16015, 16016 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21501, new growthInfo(2, new int[][][] {
				{ { 21507, 21829 }, { 16015, 16016 } },
				new int[0][] }, Config.TAMED_X2));
		growthCapableMobs.put(21502, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21506, 21828 }, { 16015, 16016 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21503, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21507, 21829 }, { 16015, 16016 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21504, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21506, 21828 }, { 16015, 16016 } } }, Config.TAMED_X2));
		growthCapableMobs.put(21505, new growthInfo(2, new int[][][] {
				new int[0][],
				{ { 21507, 21829 }, { 16015, 16016 } } }, Config.TAMED_X2));
		for(Integer i = 16013; i <= 16018; ++i)
			tamedBeasts.add(i);
		for(Integer i = 16013; i <= 16019; ++i)
			feedableBeasts.add(i);
		for(Integer i = 21451; i <= 21507; ++i)
			feedableBeasts.add(i);
		for(Integer i = 21824; i <= 21829; ++i)
			feedableBeasts.add(i);
	}

	private void spawnNext(final Player player, final int growthLevel, final int food)
	{
		final int npcId = getNpcId();
		int nextNpcId = 0;
		if(growthLevel == 2)
		{
			if(Rnd.chance(50))
			{
				if(player.isMageClass())
					nextNpcId = growthCapableMobs.get(npcId).spice[food][1][1];
				else
					nextNpcId = growthCapableMobs.get(npcId).spice[food][1][0];
			}
			else if(player.isMageClass())
				nextNpcId = growthCapableMobs.get(npcId).spice[food][0][1];
			else
				nextNpcId = growthCapableMobs.get(npcId).spice[food][0][0];
		}
		else
			nextNpcId = growthCapableMobs.get(npcId).spice[food][0][Rnd.get(growthCapableMobs.get(npcId).spice[food][0].length)];
		FeedableBeastInstance.feedInfo.remove(getObjectId());
		if(growthCapableMobs.get(npcId).growth_level == 0)
			onDecay();
		else
			deleteMe();
		if(tamedBeasts.contains(nextNpcId))
		{
			final TamedBeastInstance oldTrained = player.getTrainedBeast();
			if(oldTrained != null)
				oldTrained.doDespawn();
			final NpcTemplate template = NpcTable.getTemplate(nextNpcId);
			final TamedBeastInstance nextNpc = new TamedBeastInstance(IdFactory.getInstance().getNextId(), template, player, food == 0 ? FeedableBeastInstance.SKILL_GOLDEN_SPICE : FeedableBeastInstance.SKILL_CRYSTAL_SPICE, getLoc());
			QuestState st = player.getQuestState(20);
			if(st != null && !st.isCompleted() && Rnd.chance(5) && st.getQuestItemsCount(7185) == 0L)
			{
				st.giveItems(7185, 1L);
				st.set("cond", "2");
			}
			st = player.getQuestState(655);
			if(st != null && !st.isCompleted() && st.getCond() == 1 && st.getQuestItemsCount(8084) < 10L)
				st.giveItems(8084, 1L);
			final int rand = Rnd.get(10);
			if(rand <= 4)
				Functions.npcSayCustomMessage(nextNpc, "l2s.gameserver.model.instances.FeedableBeastInstance.4." + (rand + 1), player.getName());
		}
		else
		{
			final MonsterInstance nextNpc2 = spawn(nextNpcId, getX(), getY(), getZ());
			feedInfo.put(nextNpc2.getObjectId(), player.getObjectId());
			Functions.npcSayCustomMessage(nextNpc2, FeedableBeastInstance.text[growthLevel][Rnd.get(FeedableBeastInstance.text[growthLevel].length)], new Object[0]);
			nextNpc2.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 99999);
		}
	}

	@Override
	public void onDeath(final Creature killer)
	{
		FeedableBeastInstance.feedInfo.remove(getObjectId());
		super.onDeath(killer);
	}

	public MonsterInstance spawn(final int npcId, final int x, final int y, final int z)
	{
		try
		{
			final MonsterInstance monster = (MonsterInstance) NpcTable.getTemplate(npcId).getInstanceConstructor().newInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(npcId));
			monster.setSpawnedLoc(new Location(x, y, z));
			monster.spawnMe(monster.getSpawnedLoc());
			return monster;
		}
		catch(Exception e)
		{
			_log.error("Could not spawn Npc " + npcId, e);
			return null;
		}
	}

	public void onSkillUse(final Player player, final int skill_id)
	{
		final int npcId = getNpcId();
		if(!feedableBeasts.contains(npcId))
			return;
		if(skill_id != FeedableBeastInstance.SKILL_GOLDEN_SPICE && skill_id != FeedableBeastInstance.SKILL_CRYSTAL_SPICE)
			return;
		int food = FeedableBeastInstance.GOLDEN_SPICE;
		if(skill_id == FeedableBeastInstance.SKILL_CRYSTAL_SPICE)
			food = FeedableBeastInstance.CRYSTAL_SPICE;
		final int objectId = getObjectId();
		this.broadcastPacket(new SocialAction(objectId, 2));
		if(growthCapableMobs.containsKey(npcId))
		{
			if(growthCapableMobs.get(npcId).spice[food].length == 0)
				return;
			final int growthLevel = growthCapableMobs.get(npcId).growth_level;
			if(growthLevel > 0 && FeedableBeastInstance.feedInfo.get(objectId) != null && FeedableBeastInstance.feedInfo.get(objectId) != player.getObjectId())
				return;
			if(Rnd.chance(growthCapableMobs.get(npcId).growth_chance))
				spawnNext(player, growthLevel, food);
		}
		else if(tamedBeasts.contains(npcId) && skill_id == ((TamedBeastInstance) this).getFoodType())
		{
			((TamedBeastInstance) this).onReceiveFood();
			Functions.npcSayCustomMessage(this, FeedableBeastInstance.mytext[Rnd.get(FeedableBeastInstance.mytext.length)], new Object[0]);
		}
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}

	static
	{
		FeedableBeastInstance.feedInfo = new HashMap<Integer, Integer>();
		FeedableBeastInstance.GOLDEN_SPICE = 0;
		FeedableBeastInstance.CRYSTAL_SPICE = 1;
		FeedableBeastInstance.SKILL_GOLDEN_SPICE = 2188;
		FeedableBeastInstance.SKILL_CRYSTAL_SPICE = 2189;
		FeedableBeastInstance.text = new String[][] {
				{
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.1",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.2",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.3",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.4",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.5",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.6",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.7",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.8",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.9",
						"l2s.gameserver.model.instances.FeedableBeastInstance.1.10" },
				{
						"l2s.gameserver.model.instances.FeedableBeastInstance.2.1",
						"l2s.gameserver.model.instances.FeedableBeastInstance.2.2",
						"l2s.gameserver.model.instances.FeedableBeastInstance.2.3",
						"l2s.gameserver.model.instances.FeedableBeastInstance.2.4",
						"l2s.gameserver.model.instances.FeedableBeastInstance.2.5" },
				{
						"l2s.gameserver.model.instances.FeedableBeastInstance.3.1",
						"l2s.gameserver.model.instances.FeedableBeastInstance.3.2",
						"l2s.gameserver.model.instances.FeedableBeastInstance.3.3",
						"l2s.gameserver.model.instances.FeedableBeastInstance.3.4",
						"l2s.gameserver.model.instances.FeedableBeastInstance.3.5" } };
		FeedableBeastInstance.mytext = new String[] {
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.1",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.2",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.3",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.4",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.5",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.6",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.7",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.8",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.9",
				"l2s.gameserver.model.instances.FeedableBeastInstance.5.10" };
	}

	private class growthInfo
	{
		public int growth_level;
		public int growth_chance;
		public int[][][] spice;

		public growthInfo(final int level, final int[][][] sp, final int chance)
		{
			growth_level = level;
			spice = sp;
			growth_chance = chance;
		}
	}
}
