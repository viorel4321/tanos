package l2s.gameserver.model.instances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.MinionList;

public class RaidBossInstance extends MonsterInstance
{
	protected static Logger _log;
	private ScheduledFuture<?> minionMaintainTask;
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 1500;
	private static final int MINION_UNSPAWN_INTERVAL = 3000;
	private RaidBossSpawnManager.Status _raidStatus;

	public RaidBossInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isRaid()
	{
		return true;
	}

	@Override
	public boolean isRB()
	{
		return !isEpicBoss();
	}

	@Override
	public boolean isMuted(final Skill skill)
	{
		return isRB() && getCurrentMp() < Config.BOSS_CAST_MIN_MP || super.isMuted(skill);
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return 1500;
	}

	protected int getMinionUnspawnInterval()
	{
		return 3000;
	}

	protected int getKilledInterval(final MonsterInstance minion)
	{
		return Rnd.get(Config.MIN_RESP_RAID_FIGHTER, Config.MAX_RESP_RAID_FIGHTER);
	}

	@Override
	public void notifyMinionDied(final MonsterInstance minion)
	{
		minionMaintainTask = ThreadPoolManager.getInstance().schedule(new maintainKilledMinion(minion.getNpcId()), getKilledInterval(minion));
		super.notifyMinionDied(minion);
	}

	@Override
	public void onDeath(final Creature killer)
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(false);
			minionMaintainTask = null;
		}
		final int points = RaidBossSpawnManager.getInstance().getPointsForRaid(getNpcId());
		if(points > 0)
			calcRaidPointsReward(points);
		if(killer != null && killer.isPlayable())
		{
			final Player player = killer.getPlayer();
			if(player != null)
			{
				if(player.isInParty())
				{
					for(final Player member : player.getParty().getPartyMembers())
						if(member.isNoble())
							Hero.getInstance().addHeroDiary(member.getObjectId(), 1, getNpcId());
					player.getParty().broadCast(new SystemMessage(1209));
				}
				else
				{
					if(player.isNoble())
						Hero.getInstance().addHeroDiary(player.getObjectId(), 1, getNpcId());
					player.sendPacket(new SystemMessage(1209));
				}
				if(player.getClan() != null && player.getClan().getLeader().isOnline() && player.getClan().getLeader().getPlayer().getQuestState(508) != null)
				{
					final QuestState st = player.getClan().getLeader().getPlayer().getQuestState(508);
					st.getQuest().onKill(this, st);
				}
			}
		}
		unspawnMinions();
		int boxId = 0;
		switch(getNpcId())
		{
			case 25035:
			{
				boxId = 31027;
				break;
			}
			case 25054:
			{
				boxId = 31028;
				break;
			}
			case 25126:
			{
				boxId = Config.ADDON ? 0 : 31029;
				break;
			}
			case 25220:
			{
				boxId = 31030;
				break;
			}
		}
		if(boxId != 0)
		{
			final NpcTemplate boxTemplate = NpcTable.getTemplate(boxId);
			if(boxTemplate != null)
			{
				final NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate);
				box.spawnMe(getLoc());
				box.setSpawnedLoc(getLoc());
				ThreadPoolManager.getInstance().schedule(new Runnable(){
					@Override
					public void run()
					{
						box.deleteMe();
					}
				}, 60000L);
			}
		}
		super.onDeath(killer);
		setRaidStatus(RaidBossSpawnManager.Status.DEAD);
		if(Config.ANNOUNCE_RB)
			Announcements.getInstance().announceByCustomMessage("l2s.BossDead", new String[] { getName() });
	}

	private class GroupInfo
	{
		public HashSet<Player> players;
		public long reward;

		public GroupInfo()
		{
			this.players = new HashSet<Player>();
			this.reward = 0;
		}
	}

	private void calcRaidPointsReward(final int totalPoints)
	{
		final HashMap<Object, GroupInfo> participants = new HashMap<Object, GroupInfo>();
		final double totalHp = getMaxHp();
		for(final AggroList.HateInfo ai : getAggroList().getPlayableMap().values())
		{
			final Player player = ai.attacker.getPlayer();
			final Object key = player.getParty() != null ? player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty() : player.getPlayer();
			GroupInfo info = participants.get(key);
			if(info == null)
			{
				info = new GroupInfo();
				participants.put(key, info);
			}
			if(key instanceof CommandChannel)
			{
				for(final Player p : ((CommandChannel) key).getMembers())
					if(p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						info.players.add(p);
			}
			else if(key instanceof Party)
			{
				for(final Player p : ((Party) key).getPartyMembers())
					if(p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						info.players.add(p);
			}
			else
				info.players.add(player);
			info.reward += ai.damage;
		}
		for(final GroupInfo groupInfo : participants.values())
		{
			final HashSet<Player> players = groupInfo.players;
			final int perPlayer = (int) Math.round(totalPoints * groupInfo.reward / (totalHp * players.size()));
			for(final Player player2 : players)
			{
				int playerReward = perPlayer;
				playerReward = (int) Math.round(playerReward * Experience.penaltyModifier(calculateLevelDiffForDrop(player2.getLevel()), 9.0));
				if(playerReward == 0)
					continue;
				player2.sendPacket(new SystemMessage(1725).addNumber(Integer.valueOf(playerReward)));
				RaidBossSpawnManager.getInstance().addPoints(player2.getObjectId(), getNpcId(), playerReward);
			}
		}
		RaidBossSpawnManager.getInstance().updatePointsDb();
		RaidBossSpawnManager.getInstance().calculateRanking();
	}

	@Override
	public void onDecay()
	{
		super.onDecay();
		RaidBossSpawnManager.getInstance().onBossDespawned(this);
	}

	public void unspawnMinions()
	{
		if(hasMinions())
			ThreadPoolManager.getInstance().schedule(new Runnable(){
				@Override
				public void run()
				{
					try
					{
						RaidBossInstance.this.removeMinions();
					}
					catch(Throwable e)
					{
						RaidBossInstance._log.error("", e);
						e.printStackTrace();
					}
				}
			}, getMinionUnspawnInterval());
	}

	@Override
	public void onSpawn()
	{
		addSkill(SkillTable.getInstance().getInfo(4045, 1));
		super.onSpawn();
		RaidBossSpawnManager.getInstance().onBossSpawned(this);
	}

	public void setRaidStatus(final RaidBossSpawnManager.Status status)
	{
		_raidStatus = status;
	}

	public RaidBossSpawnManager.Status getRaidStatus()
	{
		return _raidStatus;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}

	static
	{
		RaidBossInstance._log = LoggerFactory.getLogger(RaidBossInstance.class);
	}

	private class maintainKilledMinion implements Runnable
	{
		private int _minion;

		public maintainKilledMinion(final int minion)
		{
			_minion = minion;
		}

		@Override
		public void run()
		{
			try
			{
				if(!isDead())
				{
					final MinionList list = getMinionList();
					if(list != null)
						list.spawnSingleMinionSync(_minion);
				}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
}
