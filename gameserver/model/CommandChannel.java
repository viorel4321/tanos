package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.s2c.ExMPCCUpdate;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class CommandChannel
{
	private List<Party> _commandChannelParties;
	private Player _commandChannelLeader;
	private int _commandChannelLvl;
	public static final int STRATEGY_GUIDE_ID = 8871;
	public static final int CLAN_IMPERIUM_ID = 391;

	public CommandChannel(final Player leader)
	{
		_commandChannelLeader = leader;
		(_commandChannelParties = new ArrayList<Party>()).add(leader.getParty());
		_commandChannelLvl = leader.getParty().getLevel();
		leader.getParty().setCommandChannel(this);
		broadcastToChannelMembers(Msg.ExMPCCOpen);
	}

	public void addParty(final Party party)
	{
		broadcastToChannelMembers(new ExMPCCUpdate(party, 1));
		_commandChannelParties.add(party);
		refreshLevel();
		party.setCommandChannel(this);
		party.broadCast(Msg.ExMPCCOpen);
	}

	public void removeParty(final Party party)
	{
		_commandChannelParties.remove(party);
		refreshLevel();
		party.setCommandChannel(null);
		party.broadCast(Msg.ExMPCCClose);
		if(_commandChannelParties.size() < 2)
			disbandChannel();
		else
			broadcastToChannelMembers(new ExMPCCUpdate(party, 0));
	}

	public void disbandChannel()
	{
		broadcastToChannelMembers(Msg.THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED);
		for(final Party party : _commandChannelParties)
			if(party != null)
			{
				party.setCommandChannel(null);
				party.broadCast(Msg.ExMPCCClose);
			}
		_commandChannelParties = null;
		_commandChannelLeader = null;
	}

	public int getMemberCount()
	{
		int count = 0;
		for(final Party party : _commandChannelParties)
			if(party != null)
				count += party.getMemberCount();
		return count;
	}

	public void broadcastToChannelMembers(final L2GameServerPacket gsp)
	{
		if(_commandChannelParties != null && !_commandChannelParties.isEmpty())
			for(final Party party : _commandChannelParties)
				if(party != null)
					party.broadCast(gsp);
	}

	public void broadcastToChannelPartyLeaders(final L2GameServerPacket gsp)
	{
		if(_commandChannelParties != null && !_commandChannelParties.isEmpty())
			for(final Party party : _commandChannelParties)
				if(party != null)
				{
					final Player leader = party.getPartyLeader();
					if(leader == null)
						continue;
					leader.sendPacket(gsp);
				}
	}

	public List<Party> getParties()
	{
		return _commandChannelParties;
	}

	public List<Player> getMembers()
	{
		final List<Player> members = new ArrayList<Player>();
		for(final Party party : getParties())
			members.addAll(party.getPartyMembers());
		return members;
	}

	public int getLevel()
	{
		return _commandChannelLvl;
	}

	public void setChannelLeader(final Player newLeader)
	{
		_commandChannelLeader = newLeader;
		broadcastToChannelMembers(new SystemMessage(1589).addString(newLeader.getName()));
	}

	public Player getChannelLeader()
	{
		return _commandChannelLeader;
	}

	public boolean meetRaidWarCondition(final GameObject obj)
	{
		if(!obj.isRaid())
			return false;
		final int npcId = ((MonsterInstance) obj).getNpcId();
		switch(npcId)
		{
			case 29001:
			case 29006:
			case 29014:
			case 29022:
			{
				return getMemberCount() > 36;
			}
			case 29020:
			{
				return getMemberCount() > 56;
			}
			case 29019:
			{
				return getMemberCount() > 225;
			}
			case 29028:
			{
				return getMemberCount() > 99;
			}
			default:
			{
				return getMemberCount() > 18;
			}
		}
	}

	private void refreshLevel()
	{
		_commandChannelLvl = 0;
		for(final Party pty : _commandChannelParties)
			if(pty.getLevel() > _commandChannelLvl)
				_commandChannelLvl = pty.getLevel();
	}

	public static boolean checkAuthority(final Player creator)
	{
		if(creator.getClan() == null || !creator.isInParty() || !creator.getParty().isLeader(creator) || creator.getPledgeClass() < 5)
		{
			creator.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
			return false;
		}
		final boolean haveSkill = creator.getSkillLevel(391) > 0;
		final boolean haveItem = creator.getInventory().getItemByItemId(8871) != null;
		if(!haveSkill && !haveItem)
		{
			creator.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
			return false;
		}
		return true;
	}
}
