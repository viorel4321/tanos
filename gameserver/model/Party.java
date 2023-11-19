package l2s.gameserver.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.DimensionalRift;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.ExManagePartyRoomMember;
import l2s.gameserver.network.l2.s2c.PartyMemberPosition;
import l2s.gameserver.network.l2.s2c.PartySmallWindowAdd;
import l2s.gameserver.network.l2.s2c.PartySmallWindowAll;
import l2s.gameserver.network.l2.s2c.PartySmallWindowDelete;
import l2s.gameserver.network.l2.s2c.PartySmallWindowDeleteAll;
import l2s.gameserver.network.l2.s2c.PartySmallWindowUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;

public class Party
{
	public static final int MAX_SIZE = 9; // TODO: Config.MAXIMUM_MEMBERS_IN_PARTY;

	private final List<Player> _members = new CopyOnWriteArrayList<Player>();

	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemOrder = 0;
	private DimensionalRift _dr;
	private CommandChannel _commandChannel;
	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;
	public float _rateDrop;
	public float _rateAdena;
	public float _rateSpoil;
	private final UpdatePositionTask posTask = new UpdatePositionTask(this);
	private ScheduledFuture<?> posTaskThread;
	private boolean _rAdena = true;

	public Party(final Player leader, final int itemDistribution)
	{
		_itemDistribution = itemDistribution;
		_members.add(leader);
		_partyLvl = leader.getLevel();
		posTaskThread = ThreadPoolManager.getInstance().schedule(posTask, 11000L);
		_rateAdena = leader.getBonus().RATE_DROP_ADENA;
		_rateDrop = leader.getBonus().RATE_DROP_ITEMS;
		_rateSpoil = leader.getBonus().RATE_DROP_SPOIL;
	}

	public int getMemberCount()
	{
		return _members.size();
	}

	public int getMemberCountInRange(Player player, int range)
	{
		int count = 0;
		for(Player member : _members)
			if(member == player || member.isInRangeZ(player, range))
				count++;
		return count;
	}

	public List<Player> getPartyMembers()
	{
		return _members;
	}

	public List<Playable> getPartyMembersWithPets()
	{
		List<Playable> result = new ArrayList<Playable>();
		for(Player member : _members)
		{
			result.add(member);
			Servitor servitor = member.getServitor();
			if(servitor != null)
				result.add(servitor);
		}
		return result;
	}

	public Player getRandomMember()
	{
		return Rnd.get(_members);
	}

	private Player getRandomMemberInRange(final Player player, final ItemInstance item, final int range)
	{
		final List<Player> ret = new ArrayList<Player>();
		for(Player member : _members) {
			if (member == player || member.isInRangeZ(player, range))
				ret.add(member);
		}
		return Rnd.get(ret);
	}

	private Player getNextLooterInRange(Player player, ItemInstance item, int range)
	{
		synchronized (_members)
		{
			int antiloop = _members.size();
			while(--antiloop > 0)
			{
				int looter = _itemOrder;
				_itemOrder++;
				if(_itemOrder > _members.size() - 1)
					_itemOrder = 0;

				Player ret = looter < _members.size() ? _members.get(looter) : player;

				if(ret != null && !ret.isDead() && ret.isInRangeZ(player, range) && ret.getInventory().validateCapacity(item) && ret.getInventory().validateWeight(item))
					return ret;
			}
		}
		return player;
	}

	/**
	 * true if player is party leader
	 */
	public boolean isLeader(Player player)
	{
		return getPartyLeader() == player;
	}

	/**
	 * Возвращает лидера партии
	 * @return L2Player Лидер партии
	 */
	public Player getPartyLeader()
	{
		synchronized (_members)
		{
			if(_members.size() == 0)
				return null;
			return _members.get(0);
		}
	}

	/**
	 * Broadcasts packet to every party member
	 * @param msg packet to broadcast
	 */
	public void broadCast(IBroadcastPacket... msg)
	{
		for(Player member : _members)
			member.sendPacket(msg);
	}

	/**
	 * Рассылает текстовое сообщение всем членам группы
	 * @param msg сообщение
	 */
	public void broadcastMessageToPartyMembers(String msg)
	{
		this.broadCast(new SystemMessage(msg));
	}

	public void broadcastCustomMessageToPartyMembers(String address, String... replacements)
	{
		for(Player member : _members)
		{
			CustomMessage cm = new CustomMessage(address);
			for(String s : replacements)
				cm.addString(s);
			member.sendMessage(cm);
		}
	}

	/**
	 * Рассылает пакет всем членам группы исключая указанного персонажа<BR><BR>
	 */
	public void broadcastToPartyMembers(Player exclude, IBroadcastPacket msg)
	{
		for(Player member : _members)
			if(exclude != member)
				member.sendPacket(msg);
	}

	public void broadcastToPartyMembersInRange(Player player, IBroadcastPacket msg, int range)
	{
		for(Player member : _members)
			if(player.isInRangeZ(member, range))
				member.sendPacket(msg);
	}

	public boolean containsMember(Player player)
	{
		return _members.contains(player);
	}

	public int indexOf(Player player)
	{
		return _members.indexOf(player);
	}

	public boolean addPartyMember(Player player, boolean force)
	{
		final Player leader = getPartyLeader();
		if(leader == null)
			return false;

		synchronized (_members)
		{
			if(_members.isEmpty())
				return false;
			if(_members.contains(player))
				return false;
			if(!force && _members.size() == MAX_SIZE)
				return false;
			_members.add(player);
		}

		player.sendPacket(new PartySmallWindowAll(this, player.getObjectId()));
		if(!force) {
			player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_JOINED_S1S_PARTY).addString(leader.getName()));
			broadcastToPartyMembers(player, new SystemMessage(SystemMessage.S1_HAS_JOINED_THE_PARTY).addString(player.getName()));
		}
		broadcastToPartyMembers(player, new PartySmallWindowAdd(player, leader.getObjectId(), getLootDistribution()));

		player.setParty(this);
		player.getListeners().onPartyInvite();
		recalculatePartyData();
		player.updateEffectIcons();
		if(player.getServitor() != null)
			player.getServitor().updateEffectIcons();
		if(isInCommandChannel())
			player.sendPacket(Msg.ExMPCCOpen);
		if(isInDimensionalRift())
			_dr.partyMemberInvited();
		final PartyRoom room = leader.getPartyRoom();
		if(room != null)
			room.broadCast(new ExManagePartyRoomMember(1, room, player));
		return true;
	}

	public void dissolveParty()
	{
		synchronized (_members)
		{
			for(final Player p : getPartyMembers())
				p.setParty(null);
			_members.clear();
		}
		setDimensionalRift(null);
		_commandChannel = null;
		posTaskThread.cancel(false);
	}

	public void removePartyMember(final Player player, final boolean kick)
	{
		if(player == null || !_members.contains(player))
			return;
		synchronized (_members)
		{
			_members.remove(player);
			posTask.remove(player);
		}
		player.getListeners().onPartyLeave();
		player.setParty(null);
		recalculatePartyData();
		if(player.isFestivalParticipant())
			SevenSignsFestival.getInstance().updateParticipants(player, this);
		if(isInCommandChannel())
			player.sendPacket(Msg.ExMPCCClose);
		if(kick)
			player.sendPacket(new SystemMessage(202));
		else
			player.sendPacket(new SystemMessage(200));
		player.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
		if(kick)
			broadCast(new SystemMessage(201).addString(player.getName()));
		else
			broadCast(new SystemMessage(108).addString(player.getName()));
		broadCast(new PartySmallWindowDelete(player));
		if(isInDimensionalRift())
			_dr.partyMemberExited(player);
		final Player leader = getPartyLeader();
		if(_members.size() == 1 || leader == null)
		{
			if(isInCommandChannel())
				_commandChannel.removeParty(this);
			if(leader != null)
				leader.setParty(null);
			dissolveParty();
		}
		else if(isInCommandChannel() && _commandChannel.getChannelLeader() == player)
			_commandChannel.setChannelLeader(leader);
	}

	public void changePartyLeader(final String name)
	{
		final Player new_leader = getPlayerByName(name);
		final Player current_leader = getPartyLeader();
		if(new_leader == null || current_leader == null || new_leader.isInDuel())
			return;
		if(current_leader.getObjectId() == new_leader.getObjectId())
		{
			current_leader.sendPacket(new SystemMessage(1401));
			return;
		}

		if(!_members.contains(new_leader))
		{
			current_leader.sendPacket(new SystemMessage(1402));
			return;
		}

		// Меняем местами нового и текущего лидера
		synchronized (_members)
		{
			int index = _members.indexOf(new_leader);
			if(index == -1)
				return;
			_members.set(0, new_leader);
			_members.set(index, current_leader);
		}

		final SystemMessage msg = new SystemMessage(1384);
		msg.addString(name);
		for(Player member : _members)
		{
			member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
			member.sendPacket(new PartySmallWindowAll(this, member.getObjectId()));
			member.sendPacket(msg);
		}

		posTask.lastpositions.clear();

		if(isInCommandChannel() && _commandChannel.getChannelLeader() == current_leader)
			_commandChannel.setChannelLeader(new_leader);

		PartyRoom room = new_leader.getPartyRoom();
		if(room != null)
			room.changeLeader(new_leader);
	}

	private Player getPlayerByName(final String name)
	{
		synchronized (_members)
		{
			for(Player member : _members)
			{
				if(name.equalsIgnoreCase(member.getName()))
					return member;
			}
		}
		return null;
	}

	public void oustPartyMember(final Player player)
	{
		synchronized (_members)
		{
			if(player == null || !_members.contains(player))
				return;
		}
		if(isLeader(player))
		{
			removePartyMember(player, false);
			if(_members.size() > 0)
			{
				final Player leader = getPartyLeader();
				final SystemMessage msg = new SystemMessage(1384);
				msg.addString(leader.getName());
				broadCast(msg);
				broadCast(new PartySmallWindowUpdate(leader));
			}
			if(_members.size() > 1)
				posTask.lastpositions.clear();
		}
		else
			removePartyMember(player, false);
	}

	public void oustPartyMember(final String name)
	{
		oustPartyMember(getPlayerByName(name));
	}

	public void distributeItem(final Player player, final ItemInstance item)
	{
		distributeItem(player, item, null);
	}

	public void distributeItem(final Player player, final ItemInstance item, final NpcInstance fromNpc)
	{
		Player target = null;
		switch(_itemDistribution)
		{
			case 1:
			case 2:
			{
				target = getRandomMemberInRange(player, item, Config.ALT_PARTY_DISTRIBUTION_RANGE);
				break;
			}
			case 3:
			case 4:
			{
				target = getNextLooterInRange(player, item, Config.ALT_PARTY_DISTRIBUTION_RANGE);
				break;
			}
			default:
			{
				target = player;
				break;
			}
		}
		if(target == null)
		{
			item.dropToTheGround(player, fromNpc);
			return;
		}
		if(!target.getInventory().validateWeight(item))
		{
			target.sendActionFailed();
			target.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			item.dropToTheGround(target, fromNpc);
			return;
		}
		if(!target.getInventory().validateCapacity(item))
		{
			target.sendActionFailed();
			target.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			item.dropToTheGround(player, fromNpc);
			return;
		}
		item.pickupMe(target);
		if(item.getCount() == 1L)
		{
			SystemMessage smsg;
			if(item.getEnchantLevel() > 0)
			{
				smsg = new SystemMessage(369);
				smsg.addNumber(Integer.valueOf(item.getEnchantLevel()));
				smsg.addItemName(Integer.valueOf(item.getItemId()));
			}
			else
			{
				smsg = new SystemMessage(30);
				smsg.addItemName(Integer.valueOf(item.getItemId()));
			}
			target.sendPacket(smsg);
			if(item.getEnchantLevel() > 0)
			{
				smsg = new SystemMessage(376);
				smsg.addString(target.getName());
				smsg.addNumber(Integer.valueOf(item.getEnchantLevel()));
				smsg.addItemName(Integer.valueOf(item.getItemId()));
			}
			else
			{
				smsg = new SystemMessage(300);
				smsg.addString(target.getName());
				smsg.addItemName(Integer.valueOf(item.getItemId()));
			}
			broadcastToPartyMembers(target, smsg);
		}
		else
		{
			SystemMessage smsg = new SystemMessage(29);
			smsg.addItemName(Integer.valueOf(item.getItemId()));
			smsg.addNumber(Long.valueOf(item.getCount()));
			target.sendPacket(smsg);
			smsg = new SystemMessage(299);
			smsg.addString(target.getName());
			smsg.addItemName(Integer.valueOf(item.getItemId()));
			smsg.addNumber(Long.valueOf(item.getCount()));
			broadcastToPartyMembers(target, smsg);
		}
		final ItemInstance item2 = target.getInventory().addItem(item);
		Log.LogItem(target, "PartyPickup", item2);
		target.sendChanges();
	}

	public void distributeAdena(final ItemInstance adena, final NpcInstance fromNpc, final Player player)
	{
		if(player == null)
			return;
		final List<Player> _membersInRange = new ArrayList<Player>();
		synchronized (_members)
		{
			if(adena.getCount() < _members.size())
				_membersInRange.add(player);
			else
				for(Player p : _members)
				{
					if(p.equals(player) || player.isInRange(p, Config.ALT_PARTY_DISTRIBUTION_RANGE) && !p.isDead())
						_membersInRange.add(p);
				}
		}
		final long totalAdena = adena.getCount();
		final long amount = totalAdena / _membersInRange.size();
		final long ost = totalAdena % _membersInRange.size();
		for(final Player member : _membersInRange)
		{
			final int a = member.getInventory().getAdena();
			if(a >= Integer.MAX_VALUE)
				continue;
			long count = member.equals(player) ? amount + ost : amount;
			final ItemInstance newAdena = ItemTable.getInstance().createItem(57);
			if(count + a > Integer.MAX_VALUE)
				count = Integer.MAX_VALUE - a;
			newAdena.setCount(count);
			final ItemInstance item2 = member.getInventory().addItem(newAdena);
			Log.LogItem(member, "PartyPickup", item2);
			member.sendPacket(new SystemMessage(28).addNumber(Long.valueOf(count)));
		}
	}

	public void distributeXpAndSp(final double xpReward, final double spReward, final List<Player> rewardedMembers, final Creature lastAttacker, final MonsterInstance monster)
	{
		recalculatePartyData();
		final List<Player> mtr = new ArrayList<Player>();
		int minPartyLevel = lastAttacker.getLevel();
		int maxPartyLevel = lastAttacker.getLevel();
		double partyLvlSum = 0.0;
		for(final Player member : rewardedMembers)
		{
			if(!lastAttacker.isInRange(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			minPartyLevel = Math.min(minPartyLevel, member.getLevel());
			maxPartyLevel = Math.max(maxPartyLevel, member.getLevel());
		}
		_rAdena = true;
		for(final Player member : rewardedMembers)
		{
			if(!lastAttacker.isInRange(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			if(member.getLevel() < maxPartyLevel - Config.ALT_PARTY_LVL_DIFF)
				_rAdena = false;
			else
			{
				partyLvlSum += member.getLevel();
				mtr.add(member);
			}
		}
		if(mtr.size() == 0)
		{
			recalculatePartyData();
			return;
		}
		final double bonus = Config.ALT_PARTY_BONUS[mtr.size() - 1];
		final double XP = xpReward * bonus;
		final double SP = spReward * bonus;
		for(final Player member2 : mtr)
		{
			final double lvlPenalty = Experience.penaltyModifier(monster.calculateLevelDiffForDrop(member2.getLevel()), 9.0);
			double memberXp = XP * lvlPenalty * member2.getLevel() / partyLvlSum;
			double memberSp = SP * lvlPenalty * member2.getLevel() / partyLvlSum;
			memberXp = Math.min(memberXp, xpReward);
			memberSp = Math.min(memberSp, spReward);
			if(member2.getServitor() != null && member2.getServitor().isSummon())
			{
				final float penalty = 1.0f - ((SummonInstance) member2.getServitor()).getExpPenalty();
				memberXp *= penalty;
				memberSp *= penalty;
			}
			member2.addExpAndSp(member2.getVarBoolean("NoExp") ? 0L : (long) (int) memberXp, (int) memberSp, true, true);
		}
		recalculatePartyData();
	}

	public void recalculatePartyData()
	{
		_partyLvl = 0;
		float rateDrop = 0.0f;
		float rateAdena = 0.0f;
		float rateSpoil = 0.0f;
		float minRateDrop = Float.MAX_VALUE;
		float minRateAdena = Float.MAX_VALUE;
		float minRateSpoil = Float.MAX_VALUE;
		byte count = 0;
		synchronized (_members)
		{
			for(Player member : _members)
			{
				final int level = member.getLevel();
				_partyLvl = Math.max(_partyLvl, level);
				++count;
				rateDrop += member.getBonus().RATE_DROP_ITEMS;
				rateAdena += member.getBonus().RATE_DROP_ADENA * rAdena(member.getLevel());
				rateSpoil += member.getBonus().RATE_DROP_SPOIL;
				minRateDrop = Math.min(minRateDrop, member.getBonus().RATE_DROP_ITEMS);
				minRateAdena = Math.min(minRateAdena, member.getBonus().RATE_DROP_ADENA * rAdena(member.getLevel()));
				minRateSpoil = Math.min(minRateSpoil, member.getBonus().RATE_DROP_SPOIL);
			}
		}
		_rateDrop = Config.RATE_PARTY_MIN ? minRateDrop : rateDrop / count;
		_rateAdena = Config.RATE_PARTY_MIN ? minRateAdena : rateAdena / count;
		_rateSpoil = Config.RATE_PARTY_MIN ? minRateSpoil : rateSpoil / count;
	}

	private float rAdena(final int lvl)
	{
		float rate = 1.0f;
		if(Config.ALT_RATE_ADENA && _rAdena)
			if(lvl > 75)
				rate *= Config.ALT_RATE_ADENA_S;
			else if(lvl >= 61)
				rate *= Config.ALT_RATE_ADENA_A;
			else if(lvl >= 52)
				rate *= Config.ALT_RATE_ADENA_B;
			else if(lvl >= 40)
				rate *= Config.ALT_RATE_ADENA_C;
			else if(lvl >= 20)
				rate *= Config.ALT_RATE_ADENA_D;
			else
				rate *= Config.ALT_RATE_ADENA_NG;
		return rate;
	}

	public int getLevel()
	{
		return _partyLvl;
	}

	public int getLootDistribution()
	{
		return _itemDistribution;
	}

	public boolean isDistributeSpoilLoot()
	{
		boolean rv = false;
		if(_itemDistribution == 2 || _itemDistribution == 4)
			rv = true;
		return rv;
	}

	public boolean isInDimensionalRift()
	{
		return _dr != null;
	}

	public void setDimensionalRift(final DimensionalRift dr)
	{
		_dr = dr;
	}

	public DimensionalRift getDimensionalRift()
	{
		return _dr;
	}

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public void setCommandChannel(final CommandChannel channel)
	{
		_commandChannel = channel;
	}

	public void Teleport(final int x, final int y, final int z)
	{
		TeleportParty(getPartyMembers(), new Location(x, y, z));
	}

	public void Teleport(final Location dest)
	{
		TeleportParty(getPartyMembers(), dest);
	}

	public void Teleport(final Territory territory)
	{
		RandomTeleportParty(getPartyMembers(), territory);
	}

	public void Teleport(final Territory territory, final Location dest)
	{
		TeleportParty(getPartyMembers(), territory, dest);
	}

	public static void TeleportParty(final List<Player> members, final Location dest)
	{
		for(final Player _member : members)
		{
			if(_member == null)
				continue;
			_member.teleToLocation(dest);
		}
	}

	public static void TeleportParty(final List<Player> members, final Territory territory, final Location dest)
	{
		if(!territory.isInside(dest.x, dest.y))
		{
			Log.add("TeleportParty: dest is out of territory", "errors");
			Thread.dumpStack();
			return;
		}
		final int base_x = members.get(0).getX();
		final int base_y = members.get(0).getY();
		for(final Player _member : members)
		{
			if(_member == null)
				continue;
			int diff_x = _member.getX() - base_x;
			int diff_y = _member.getY() - base_y;
			final Location loc = new Location(dest.x + diff_x, dest.y + diff_y, dest.z);
			while(!territory.isInside(loc.x, loc.y))
			{
				diff_x = loc.x - dest.x;
				diff_y = loc.y - dest.y;
				if(diff_x != 0)
				{
					final Location location = loc;
					location.x -= diff_x / Math.abs(diff_x);
				}
				if(diff_y != 0)
				{
					final Location location2 = loc;
					location2.y -= diff_y / Math.abs(diff_y);
				}
			}
			_member.teleToLocation(loc);
		}
	}

	public static void RandomTeleportParty(final List<Player> members, final Territory territory)
	{
		for(final Player _member : members)
		{
			final Location _loc = territory.getRandomLoc(_member.getGeoIndex());
			if(_member != null)
			{
				if(_loc == null)
					continue;
				_member.teleToLocation(_loc);
			}
		}
	}

	private class UpdatePositionTask implements Runnable
	{
		private final WeakReference<Party> party_ref;
		private final HashMap<Integer, int[]> lastpositions;

		public UpdatePositionTask(final Party party)
		{
			lastpositions = new HashMap<Integer, int[]>();
			party_ref = new WeakReference<Party>(party);
		}

		public void remove(final Player player)
		{
			synchronized (lastpositions)
			{
				lastpositions.remove(new Integer(player.getObjectId()));
			}
		}

		@Override
		public void run()
		{
			final Party party = party_ref.get();
			if(party == null || party.getMemberCount() < 2)
			{
				synchronized (lastpositions)
				{
					lastpositions.clear();
				}
				party_ref.clear();
				dissolveParty();
				return;
			}
			try
			{
				final List<Player> full_updated = new ArrayList<Player>();
				final List<Player> members = party.getPartyMembers();
				PartyMemberPosition just_updated = new PartyMemberPosition();
				for(final Player member : members)
				{
					if(member == null)
						continue;
					synchronized (lastpositions)
					{
						final int[] lastpos = lastpositions.get(new Integer(member.getObjectId()));
						if(lastpos == null)
						{
							just_updated.add(member);
							full_updated.add(member);
							lastpositions.put(member.getObjectId(), new int[] { member.getX(), member.getY(), member.getZ() });
						}
						else
						{
							if(member.getDistance(lastpos[0], lastpos[1], lastpos[2]) <= 256.0)
								continue;
							just_updated.add(member);
							lastpos[0] = member.getX();
							lastpos[1] = member.getY();
							lastpos[2] = member.getZ();
						}
					}
				}
				if(just_updated.size() > 0)
					for(final Player member : members)
						if(!full_updated.contains(member))
							member.sendPacket(just_updated);
				if(full_updated.size() > 0)
				{
					just_updated = new PartyMemberPosition().add(members);
					for(final Player member : full_updated)
						member.sendPacket(just_updated);
					full_updated.clear();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				posTaskThread = ThreadPoolManager.getInstance().schedule(this, 1000L);
			}
		}
	}
}
