package l2s.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.NpcSay;

public class AutoChatHandler implements SpawnListener
{
	protected static Logger _log;
	private static AutoChatHandler _instance;
	private static final long DEFAULT_CHAT_DELAY = 180000L;
	Map<Integer, AutoChatInstance> _registeredChats;

	protected AutoChatHandler()
	{
		_registeredChats = new HashMap<Integer, AutoChatInstance>();
		restoreChatData();
		Spawn.addSpawnListener(this);
	}

	private void restoreChatData()
	{
		int numLoaded = 0;
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		ResultSet rset2 = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM auto_chat ORDER BY groupId ASC");
			statement2 = con.prepareStatement("SELECT * FROM auto_chat_text WHERE groupId=?");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++numLoaded;
				statement2.setInt(1, rset.getInt("groupId"));
				rset2 = statement2.executeQuery();
				final List<String> list = new ArrayList<String>();
				while(rset2.next())
					list.add(rset2.getString("chatText"));
				registerGlobalChat(rset.getInt("npcId"), (String[]) list.toArray((Object[]) new String[0]), rset.getLong("chatDelay") * 1000L);
				DbUtils.close(rset2);
			}
		}
		catch(Exception e)
		{
			AutoChatHandler._log.warn("AutoSpawnHandler: Could not restore chat data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(statement2, rset2);
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static AutoChatHandler getInstance()
	{
		if(AutoChatHandler._instance == null)
			AutoChatHandler._instance = new AutoChatHandler();
		return AutoChatHandler._instance;
	}

	public int size()
	{
		return _registeredChats.size();
	}

	public AutoChatInstance registerGlobalChat(final int npcId, final String[] chatTexts, final long chatDelay)
	{
		return this.registerChat(npcId, null, chatTexts, chatDelay);
	}

	public AutoChatInstance registerChat(final NpcInstance npcInst, final String[] chatTexts, final long chatDelay)
	{
		return this.registerChat(npcInst.getNpcId(), npcInst, chatTexts, chatDelay);
	}

	private AutoChatInstance registerChat(final int npcId, final NpcInstance npcInst, final String[] chatTexts, long chatDelay)
	{
		if(chatDelay < 0L)
			chatDelay = 180000L;
		AutoChatInstance chatInst;
		if(_registeredChats.containsKey(npcId))
			chatInst = _registeredChats.get(npcId);
		else
			chatInst = new AutoChatInstance(npcId, chatTexts, chatDelay, npcInst == null);
		if(npcInst != null)
			chatInst.addChatDefinition(npcInst);
		_registeredChats.put(npcId, chatInst);
		return chatInst;
	}

	public boolean removeChat(final int npcId)
	{
		final AutoChatInstance chatInst = _registeredChats.get(npcId);
		return this.removeChat(chatInst);
	}

	public boolean removeChat(final AutoChatInstance chatInst)
	{
		if(chatInst == null)
			return false;
		_registeredChats.remove(chatInst.getNPCId());
		chatInst.setActive(false);
		return true;
	}

	public AutoChatInstance getAutoChatInstance(final int id, final boolean byObjectId)
	{
		if(!byObjectId)
			return _registeredChats.get(id);
		for(final AutoChatInstance chatInst : _registeredChats.values())
			if(chatInst.getChatDefinition(id) != null)
				return chatInst;
		return null;
	}

	public void setAutoChatActive(final boolean isActive)
	{
		for(final AutoChatInstance chatInst : _registeredChats.values())
			chatInst.setActive(isActive);
	}

	@Override
	public void npcSpawned(final NpcInstance npc)
	{
		synchronized (_registeredChats)
		{
			if(npc == null)
				return;
			final int npcId = npc.getNpcId();
			if(_registeredChats.containsKey(npcId))
			{
				final AutoChatInstance chatInst = _registeredChats.get(npcId);
				if(chatInst != null && chatInst.isGlobal())
					chatInst.addChatDefinition(npc);
			}
		}
	}

	@Override
	public void npcDeSpawned(final NpcInstance npc)
	{}

	static
	{
		AutoChatHandler._log = LoggerFactory.getLogger(AutoChatHandler.class);
	}

	public class AutoChatInstance
	{
		int _npcId;
		private long _defaultDelay;
		private String[] _defaultTexts;
		private boolean _defaultRandom;
		private boolean _globalChat;
		private boolean _isActive;
		private Map<Integer, AutoChatDefinition> _chatDefinitions;
		private ScheduledFuture<?> _chatTask;

		AutoChatInstance(final int npcId, final String[] chatTexts, final long chatDelay, final boolean isGlobal)
		{
			_defaultDelay = 180000L;
			_defaultRandom = false;
			_globalChat = false;
			_chatDefinitions = new HashMap<Integer, AutoChatDefinition>();
			_defaultTexts = chatTexts;
			_npcId = npcId;
			_defaultDelay = chatDelay;
			_globalChat = isGlobal;
			setActive(true);
		}

		AutoChatDefinition getChatDefinition(final int objectId)
		{
			return _chatDefinitions.get(objectId);
		}

		AutoChatDefinition[] getChatDefinitions()
		{
			return _chatDefinitions.values().toArray(new AutoChatDefinition[_chatDefinitions.values().size()]);
		}

		public int addChatDefinition(final NpcInstance npcInst)
		{
			return this.addChatDefinition(npcInst, null, 0L);
		}

		public int addChatDefinition(final NpcInstance npcInst, final String[] chatTexts, final long chatDelay)
		{
			final int objectId = npcInst.getObjectId();
			final AutoChatDefinition chatDef = new AutoChatDefinition(this, npcInst, chatTexts, chatDelay);
			_chatDefinitions.put(objectId, chatDef);
			return objectId;
		}

		public boolean removeChatDefinition(final int objectId)
		{
			if(!_chatDefinitions.containsKey(objectId))
				return false;
			final AutoChatDefinition chatDefinition = _chatDefinitions.get(objectId);
			chatDefinition.setActive(false);
			_chatDefinitions.remove(objectId);
			return true;
		}

		public boolean isActive()
		{
			return _isActive;
		}

		public boolean isGlobal()
		{
			return _globalChat;
		}

		public boolean isDefaultRandom()
		{
			return _defaultRandom;
		}

		public boolean isRandomChat(final int objectId)
		{
			return _chatDefinitions.containsKey(objectId) && _chatDefinitions.get(objectId).isRandomChat();
		}

		public int getNPCId()
		{
			return _npcId;
		}

		public int getDefinitionCount()
		{
			return _chatDefinitions.size();
		}

		public NpcInstance[] getNPCInstanceList()
		{
			final List<NpcInstance> npcInsts = new ArrayList<NpcInstance>();
			for(final AutoChatDefinition chatDefinition : _chatDefinitions.values())
				npcInsts.add(chatDefinition._npcInstance);
			return npcInsts.toArray(new NpcInstance[npcInsts.size()]);
		}

		public long getDefaultDelay()
		{
			return _defaultDelay;
		}

		public String[] getDefaultTexts()
		{
			return _defaultTexts;
		}

		public void setDefaultChatDelay(final long delayValue)
		{
			_defaultDelay = delayValue;
		}

		public void setDefaultChatTexts(final String[] textsValue)
		{
			_defaultTexts = textsValue;
		}

		public void setDefaultRandom(final boolean randValue)
		{
			_defaultRandom = randValue;
		}

		public void setChatDelay(final int objectId, final long delayValue)
		{
			final AutoChatDefinition chatDef = getChatDefinition(objectId);
			if(chatDef != null)
				chatDef.setChatDelay(delayValue);
		}

		public void setChatTexts(final int objectId, final String[] textsValue)
		{
			final AutoChatDefinition chatDef = getChatDefinition(objectId);
			if(chatDef != null)
				chatDef.setChatTexts(textsValue);
		}

		public void setRandomChat(final int objectId, final boolean randValue)
		{
			final AutoChatDefinition chatDef = getChatDefinition(objectId);
			if(chatDef != null)
				chatDef.setRandomChat(randValue);
		}

		public void setActive(final boolean activeValue)
		{
			if(_isActive == activeValue)
				return;
			_isActive = activeValue;
			if(!isGlobal())
			{
				for(final AutoChatDefinition chatDefinition : _chatDefinitions.values())
					chatDefinition.setActive(activeValue);
				return;
			}
			if(isActive())
			{
				final AutoChatRunner acr = new AutoChatRunner(_npcId, -1);
				_chatTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(acr, _defaultDelay, _defaultDelay);
			}
			else
				_chatTask.cancel(false);
		}

		private class AutoChatDefinition
		{
			protected int _chatIndex;
			protected NpcInstance _npcInstance;
			protected AutoChatInstance _chatInstance;
			protected ScheduledFuture<?> _chatTask;
			private long _chatDelay;
			private String[] _chatTexts;
			private boolean _isActive;
			private boolean _randomChat;

			protected AutoChatDefinition(final AutoChatInstance chatInst, final NpcInstance npcInst, final String[] chatTexts, final long chatDelay)
			{
				_chatIndex = 0;
				_chatDelay = 0L;
				_chatTexts = null;
				_npcInstance = npcInst;
				_chatInstance = chatInst;
				_randomChat = chatInst.isDefaultRandom();
				_chatDelay = chatDelay;
				_chatTexts = chatTexts;
				if(!chatInst.isGlobal())
					setActive(true);
			}

			String[] getChatTexts()
			{
				if(_chatTexts != null)
					return _chatTexts;
				return _chatInstance.getDefaultTexts();
			}

			private long getChatDelay()
			{
				if(_chatDelay > 0L)
					return _chatDelay;
				return _chatInstance.getDefaultDelay();
			}

			private boolean isActive()
			{
				return _isActive;
			}

			boolean isRandomChat()
			{
				return _randomChat;
			}

			void setRandomChat(final boolean randValue)
			{
				_randomChat = randValue;
			}

			void setChatDelay(final long delayValue)
			{
				_chatDelay = delayValue;
			}

			void setChatTexts(final String[] textsValue)
			{
				_chatTexts = textsValue;
			}

			void setActive(final boolean activeValue)
			{
				if(isActive() == activeValue)
					return;
				if(activeValue)
				{
					final AutoChatRunner acr = new AutoChatRunner(_npcId, _npcInstance.getObjectId());
					_chatTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(acr, getChatDelay(), getChatDelay());
				}
				else
					_chatTask.cancel(false);
				_isActive = activeValue;
			}
		}

		private class AutoChatRunner implements Runnable
		{
			private int _npcId;
			private int _objectId;

			protected AutoChatRunner(final int npcId, final int objectId)
			{
				_npcId = npcId;
				_objectId = objectId;
			}

			@Override
			public synchronized void run()
			{
				final AutoChatInstance chatInst = _registeredChats.get(_npcId);
				AutoChatDefinition[] chatDefinitions;
				if(chatInst.isGlobal())
					chatDefinitions = chatInst.getChatDefinitions();
				else
				{
					final AutoChatDefinition chatDef = chatInst.getChatDefinition(_objectId);
					if(chatDef == null)
					{
						AutoChatHandler._log.warn("AutoChatHandler: Auto chat definition is NULL for NPC ID " + _npcId + ".");
						return;
					}
					chatDefinitions = new AutoChatDefinition[] { chatDef };
				}
				for(final AutoChatDefinition chatDef2 : chatDefinitions)
				{
					try
					{
						if(chatDef2 != null)
						{
							final int maxIndex = chatDef2.getChatTexts().length;
							int lastIndex = Rnd.get(maxIndex);
							if(!chatDef2.isRandomChat())
							{
								lastIndex = chatDef2._chatIndex;
								if(++lastIndex == maxIndex)
									lastIndex = 0;
								chatDef2._chatIndex = lastIndex;
							}

							String text = chatDef2.getChatTexts()[lastIndex];
							if(text == null || text.equals(""))
								return;

							final NpcInstance chatNpc = chatDef2._npcInstance;
							if(!chatNpc.isVisible())
								return;

							final List<Player> nearbyPlayers = text.startsWith("!") ? World.getAroundPlayers(chatNpc) : World.getAroundPlayers(chatNpc, 1500, 200);
							if(nearbyPlayers == null || nearbyPlayers.isEmpty())
								return;

							final Player randomPlayer = nearbyPlayers.get(Rnd.get(nearbyPlayers.size()));
							if(text.indexOf("%player_random%") > -1)
								text = text.replaceAll("%player_random%", randomPlayer.getName());

							if(Config.ALLOW_SEVEN_SIGNS)
							{
								final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
								int losingCabal = 0;
								if(winningCabal == 2)
									losingCabal = 1;
								else if(winningCabal == 1)
									losingCabal = 2;
								if(text.indexOf("%player_cabal_winner%") > -1)
								{
									boolean playerFound = false;
									for(final Player nearbyPlayer : nearbyPlayers)
									{
										if(nearbyPlayer == null)
											continue;
										if(SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == winningCabal)
										{
											text = text.replaceAll("%player_cabal_winner%", nearbyPlayer.getName());
											playerFound = true;
											break;
										}
									}
									if(!playerFound)
										text = "";
								}
								if(text.indexOf("%player_cabal_loser%") > -1)
								{
									boolean playerFound = false;
									for(final Player nearbyPlayer : nearbyPlayers)
									{
										if(nearbyPlayer == null)
											continue;
										if(SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == losingCabal)
										{
											text = text.replaceAll("%player_cabal_loser%", nearbyPlayer.getName());
											playerFound = true;
											break;
										}
									}
									if(!playerFound)
										text = "";
								}
							}
							if(text.equals(""))
								return;
							final NpcSay cs = new NpcSay(chatNpc, text.startsWith("!") ? 1 : 0, text.startsWith("!") ? text.substring(1, text.length() - 1) : text);
							for(final Player nearbyPlayer : nearbyPlayers)
							{
								if(nearbyPlayer == null)
									continue;
								nearbyPlayer.sendPacket(cs);
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
						return;
					}
				}
			}
		}
	}
}
