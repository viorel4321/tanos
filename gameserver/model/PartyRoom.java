package l2s.gameserver.model;

import java.util.Vector;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.instancemanager.PartyRoomManager;
import l2s.gameserver.listener.actor.OnPlayerPartyInviteListener;
import l2s.gameserver.listener.actor.OnPlayerPartyLeaveListener;
import l2s.gameserver.network.l2.s2c.ExClosePartyRoom;
import l2s.gameserver.network.l2.s2c.ExManagePartyRoomMember;
import l2s.gameserver.network.l2.s2c.ExPartyRoomMember;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.PartyRoomInfo;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class PartyRoom
{
	public static int WAIT_PLAYER;
	public static int ROOM_MASTER;
	public static int PARTY_MEMBER;
	private final int _id;
	private int _minLevel;
	private int _maxLevel;
	private int _maxMemberSize;
	private int _lootType;
	private String _topic;
	private final PartyListenerImpl _listener;
	protected Player _leader;
	private final Vector<Integer> members_list;

	public PartyRoom(final Player leader, final int minLevel, final int maxLevel, final int maxMemberSize, final int lootType, final String topic)
	{
		_listener = new PartyListenerImpl();
		members_list = new Vector<Integer>();
		_leader = leader;
		_id = PartyRoomManager.getInstance().addMatchingRoom(this);
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_maxMemberSize = maxMemberSize;
		_lootType = lootType;
		_topic = topic;
		addMember0(leader, null);
		leader.broadcastUserInfo(false);
	}

	public void changeLeader(final Player newLeader)
	{
		final Player oldLeader = _leader;
		_leader = newLeader;
		broadCast(new ExManagePartyRoomMember(1, this, newLeader), new ExManagePartyRoomMember(1, this, oldLeader), new SystemMessage(1397));
	}

	public boolean addMember(final Player player)
	{
		if(members_list.contains(player.getObjectId()))
			return true;
		if(player.getLevel() < getMinLevel() || player.getLevel() > getMaxLevel() || getPlayers().size() >= getMaxMembersSize())
		{
			player.sendPacket(notValidMessage());
			return false;
		}
		return addMember0(player, enterMessage().addName(player));
	}

	private boolean addMember0(final Player player, final L2GameServerPacket p)
	{
		if(!members_list.isEmpty())
			player.addListener(_listener);
		members_list.add(player.getObjectId());
		player.setPartyRoom(this);
		for(final Integer objectId : members_list)
		{
			final Player $member;
			if(($member = GameObjectsStorage.getPlayer(objectId)) != null && $member != player)
				$member.sendPacket(p, addMemberPacket($member, player));
		}
		PartyRoomManager.getInstance().removeFromWaitingList(player);
		broadCast(new ExManagePartyRoomMember(0, this, player));
		player.sendPacket(infoRoomPacket(), membersPacket(player));
		player.sendChanges();
		return true;
	}

	public void removeMember(final Player member, final boolean oust)
	{
		if(!members_list.contains(member.getObjectId()))
			return;
		members_list.remove(member.getObjectId());
		member.removeListener(_listener);
		member.setPartyRoom(null);
		if(members_list.isEmpty())
			disband();
		else
		{
			final L2GameServerPacket infoPacket = infoRoomPacket();
			final SystemMessage exitMessage0 = exitMessage(true, oust);
			final L2GameServerPacket exitMessage2 = exitMessage0 != null ? exitMessage0.addName(member) : null;
			for(final Integer objectId : members_list)
			{
				final Player player;
				if((player = GameObjectsStorage.getPlayer(objectId)) != null)
					player.sendPacket(infoPacket, removeMemberPacket(player, member), exitMessage2);
			}
		}
		member.sendPacket(ExClosePartyRoom.STATIC, exitMessage(false, oust));
		PartyRoomManager.getInstance().addToWaitingList(member);
		broadCast(new ExManagePartyRoomMember(2, this, member));
		member.sendChanges();
	}

	public void broadcastPlayerUpdate(final Player player)
	{
		for(final Integer objectId : members_list)
		{
			final Player $member;
			if(($member = GameObjectsStorage.getPlayer(objectId)) != null)
				$member.sendPacket(updateMemberPacket($member, player));
		}
	}

	public void disband()
	{
		for(final Integer objectId : members_list)
		{
			final Player player;
			if((player = GameObjectsStorage.getPlayer(objectId)) != null)
			{
				player.removeListener(_listener);
				player.sendPacket(closeRoomMessage());
				player.sendPacket(ExClosePartyRoom.STATIC);
				player.setPartyRoom(null);
				player.sendChanges();
				PartyRoomManager.getInstance().addToWaitingList(player);
			}
		}
		members_list.clear();
		PartyRoomManager.getInstance().removeMatchingRoom(this);
	}

	public SystemMessage notValidMessage()
	{
		return Msg.SINCE_YOU_DO_NOT_MEET_THE_REQUIREMENTS_YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM;
	}

	public SystemMessage enterMessage()
	{
		return Msg.S1_HAS_ENTERED_THE_PARTY_ROOM;
	}

	public SystemMessage exitMessage(final boolean toOthers, final boolean kick)
	{
		if(toOthers)
			return kick ? Msg.S1_HAS_BEEN_OUSTED_FROM_THE_PARTY_ROOM : Msg.S1_HAS_LEFT_THE_PARTY_ROOM;
		return kick ? Msg.YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM : Msg.YOU_HAVE_EXITED_FROM_THE_PARTY_ROOM;
	}

	public SystemMessage closeRoomMessage()
	{
		return Msg.THE_PARTY_ROOM_HAS_BEEN_DISBANDED;
	}

	public L2GameServerPacket infoRoomPacket()
	{
		return new PartyRoomInfo(this);
	}

	public L2GameServerPacket addMemberPacket(final Player $member, final Player active)
	{
		return membersPacket($member);
	}

	public L2GameServerPacket removeMemberPacket(final Player $member, final Player active)
	{
		return membersPacket($member);
	}

	public L2GameServerPacket updateMemberPacket(final Player $member, final Player active)
	{
		return membersPacket($member);
	}

	public L2GameServerPacket membersPacket(final Player active)
	{
		return new ExPartyRoomMember(this, active);
	}

	public int getMemberType(final Player member)
	{
		return member.equals(_leader) ? PartyRoom.ROOM_MASTER : member.getParty() != null && _leader.getParty() == member.getParty() ? PartyRoom.PARTY_MEMBER : PartyRoom.WAIT_PLAYER;
	}

	public void broadCast(final IBroadcastPacket... arg)
	{
		for(final Integer objectId : members_list)
		{
			final Player player;
			if((player = GameObjectsStorage.getPlayer(objectId)) != null)
				player.sendPacket(arg);
		}
	}

	public int getId()
	{
		return _id;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public String getTopic()
	{
		return _topic;
	}

	public int getMaxMembersSize()
	{
		return _maxMemberSize;
	}

	public int getLocationId()
	{
		return PartyRoomManager.getInstance().getLocation(_leader);
	}

	public Player getLeader()
	{
		return _leader;
	}

	public Vector<Integer> getPlayers()
	{
		return members_list;
	}

	public int getLootType()
	{
		return _lootType;
	}

	public void setMinLevel(final int minLevel)
	{
		_minLevel = minLevel;
	}

	public void setMaxLevel(final int maxLevel)
	{
		_maxLevel = maxLevel;
	}

	public void setTopic(final String topic)
	{
		_topic = topic;
	}

	public void setMaxMemberSize(final int maxMemberSize)
	{
		_maxMemberSize = maxMemberSize;
	}

	public void setLootType(final int lootType)
	{
		_lootType = lootType;
	}

	static
	{
		PartyRoom.WAIT_PLAYER = 0;
		PartyRoom.ROOM_MASTER = 1;
		PartyRoom.PARTY_MEMBER = 2;
	}

	private class PartyListenerImpl implements OnPlayerPartyInviteListener, OnPlayerPartyLeaveListener
	{
		@Override
		public void onPartyInvite(final Player player)
		{
			broadcastPlayerUpdate(player);
		}

		@Override
		public void onPartyLeave(final Player player)
		{
			broadcastPlayerUpdate(player);
		}
	}
}
