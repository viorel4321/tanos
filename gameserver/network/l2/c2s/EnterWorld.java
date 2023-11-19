package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.stats.triggers.TriggerType;
import org.apache.commons.lang3.tuple.Pair;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.communitybbs.Manager.BuffBBSManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.WorldRegion;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.ChangeWaitType;
import l2s.gameserver.network.l2.s2c.ClientSetTime;
import l2s.gameserver.network.l2.s2c.ConfirmDlg;
import l2s.gameserver.network.l2.s2c.Die;
import l2s.gameserver.network.l2.s2c.EtcStatusUpdate;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.ExPCCafePointInfo;
import l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCount;
import l2s.gameserver.network.l2.s2c.FriendList;
import l2s.gameserver.network.l2.s2c.FriendStatus;
import l2s.gameserver.network.l2.s2c.HennaInfo;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.PartySmallWindowAll;
import l2s.gameserver.network.l2.s2c.PartySpelled;
import l2s.gameserver.network.l2.s2c.PetInfo;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListAll;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListUpdate;
import l2s.gameserver.network.l2.s2c.PledgeSkillList;
import l2s.gameserver.network.l2.s2c.PrivateStoreMsgBuy;
import l2s.gameserver.network.l2.s2c.PrivateStoreMsgSell;
import l2s.gameserver.network.l2.s2c.QuestList;
import l2s.gameserver.network.l2.s2c.RecipeShopMsg;
import l2s.gameserver.network.l2.s2c.RelationChanged;
import l2s.gameserver.network.l2.s2c.Ride;
import l2s.gameserver.network.l2.s2c.SSQInfo;
import l2s.gameserver.network.l2.s2c.ShortCutInit;
import l2s.gameserver.network.l2.s2c.SkillCoolTime;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.tables.FriendsTable;
import l2s.gameserver.tables.SkillTable;

public class EnterWorld extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		final GameClient client = getClient();
		final Player activeChar = client.getActiveChar();
		if(activeChar == null)
		{
			client.closeNow(false);
			return;
		}
		final boolean first = activeChar.entering;
		if(first)
		{
			activeChar.setUptime(System.currentTimeMillis());
			if(activeChar.getPlayerAccess().GodMode && !Config.SHOW_GM_LOGIN && !Config.EVERYBODY_HAS_ADMIN_RIGHTS)
				activeChar.setInvisible(true);
			activeChar.spawnMe();
			if(activeChar.isInStoreMode() && !activeChar.checksForShop(activeChar.getPrivateStoreType() == 5))
			{
				activeChar.setPrivateStoreType((short) 0);
				activeChar.standUp();
				activeChar.broadcastUserInfo(false);
			}
			activeChar.setRunning();
			activeChar.standUp();
			BuffBBSManager.initSchemes(activeChar);
			activeChar.startAutoSaveTask();
			activeChar.startPcBangPointsTask();
		}
		else if(activeChar.isTeleporting())
			activeChar.onTeleported();
		if(client.getState() == GameClient.GameClientState.ENTER_GAME)
			client.setState(GameClient.GameClientState.IN_GAME);
		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new SSQInfo(), new HennaInfo(activeChar));
		activeChar.sendPacket(new SkillList(activeChar));
		activeChar.sendPacket(new SystemMessage(34));
		Announcements.getInstance().showAnnouncements(activeChar);
		activeChar.classWindow();
		if(first)
		{
			final Object[] script_args = { activeChar };
			for(final Scripts.ScriptClassAndMethod handler : Scripts.onPlayerEnter)
				Scripts.getInstance().callScripts(activeChar, handler.className, handler.methodName, script_args);
			activeChar.getListeners().onEnter();
		}
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		if(Config.SHOW_HTML_WELCOME && activeChar.getClan() == null)
		{
			final String text = HtmCache.getInstance().getIfExists("welcome.htm", activeChar);
			if(text != null)
				this.sendPacket(new NpcHtmlMessage(5).setHtml(text));
		}
		if(activeChar.getClan() != null)
		{
			notifyClanMembers(activeChar);
			this.sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar), new PledgeShowInfoUpdate(activeChar.getClan()), new PledgeSkillList(activeChar.getClan()));
		}
		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}
		if(first)
		{
			notifyFriends(activeChar, true);
			loadTutorial(activeChar);
			activeChar.restoreDisableSkills(false);
			activeChar.sendPacket(new SkillCoolTime(activeChar));
		}
		else
			this.sendPacket(new FriendList(activeChar, false));
		this.sendPacket(new ExStorageMaxCount(activeChar), new QuestList(activeChar), new EtcStatusUpdate(activeChar));
		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();
		if(Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
		if(!first)
		{
			if(activeChar.isCastingNow())
				activeChar.abortCast(true, false);
			if(activeChar.isInVehicle())
				this.sendPacket(activeChar.getVehicle().getOnPacket(activeChar, activeChar.getInVehiclePosition()));
			if(activeChar.isMoving || activeChar.isFollow)
				this.sendPacket(activeChar.movePacket());
			if(activeChar.getMountNpcId() != 0)
				this.sendPacket(new Ride(activeChar));
			if(activeChar.isFishing())
				activeChar.stopFishing();
			activeChar.stopDeleteTask();
		}
		activeChar.entering = false;
		activeChar.sendUserInfo(true);
		if(activeChar.isSitting())
			activeChar.sendPacket(new ChangeWaitType(activeChar, 0));
		if(activeChar.getPrivateStoreType() != 0)
			if(activeChar.getPrivateStoreType() == 3)
				this.sendPacket(new PrivateStoreMsgBuy(activeChar, false));
			else if(activeChar.getPrivateStoreType() == 1 || activeChar.getPrivateStoreType() == 8)
				this.sendPacket(new PrivateStoreMsgSell(activeChar, false));
			else if(activeChar.getPrivateStoreType() == 5)
				this.sendPacket(new RecipeShopMsg(activeChar, false));
		if(activeChar.isDead())
			this.sendPacket(new Die(activeChar));
		activeChar.unsetVar("offline");
		activeChar.sendActionFailed();
		if(first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand)
		{
			if(activeChar.getVarBoolean("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(new SystemMessage(177));
			}
			if(activeChar.getVarBoolean("gm_invul"))
			{
				activeChar.setIsInvul(true);
				activeChar.sendMessage(activeChar.getName() + " is now immortal.");
			}
			try
			{
				final int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
				if(var_gmspeed >= 1 && var_gmspeed <= 4)
					activeChar.doCast(SkillTable.getInstance().getInfo(7029, var_gmspeed), activeChar, true);
			}
			catch(Exception ex)
			{}
		}
		PlayerMessageStack.getInstance().CheckMessages(activeChar);
		this.sendPacket(new ClientSetTime(), new ExSetCompassZoneCode(activeChar));
		final Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(false);
		if(entry != null && entry.getValue() instanceof ReviveAnswerListener)
		{
			final ConfirmDlg cd = new ConfirmDlg(1510, 0).addString(activeChar.isLangRus() ? "\u041a\u0442\u043e-\u0442\u043e" : "Somebody");
			cd.setRequestId(entry.getKey());
			this.sendPacket(cd);
		}
		if(activeChar.isCursedWeaponEquipped())
			CursedWeaponsManager.getInstance().showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());
		if(!first)
		{
			if(activeChar.inObserverMode())
			{
				if(activeChar.getOlympiadObserveId() == -1)
					activeChar.leaveObserverMode();
				else
					activeChar.leaveOlympiadObserverMode();
			}
			else if(activeChar.getCurrentRegion() != null)
				for(final WorldRegion neighbor : activeChar.getCurrentRegion().getNeighbors())
					neighbor.showObjectsToPlayer(activeChar);
			if(activeChar.getServitor() != null)
				this.sendPacket(new PetInfo(activeChar.getServitor()));
			if(activeChar.isInParty())
			{
				this.sendPacket(new PartySmallWindowAll(activeChar.getParty(), activeChar.getObjectId()));
				for(final Player member : activeChar.getParty().getPartyMembers())
					if(member != activeChar)
					{
						this.sendPacket(new PartySpelled(member, true));
						final Servitor member_pet;
						if((member_pet = member.getServitor()) != null)
							this.sendPacket(new PartySpelled(member_pet, true));
						this.sendPacket(RelationChanged.update(activeChar, member, activeChar));
					}
				if(activeChar.getParty().isInCommandChannel())
					this.sendPacket(Msg.ExMPCCOpen);
			}
			for(final Abnormal e : activeChar.getAbnormalList().getAllFirstEffects())
				if(e.getSkill().isToggle())
					this.sendPacket(new MagicSkillLaunched(activeChar.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), activeChar));
			activeChar.broadcastUserInfo(false);
		}
		else
			activeChar.sendUserInfo(false);
		activeChar.sendPacket(new ItemList(activeChar, false));
		activeChar.sendPacket(new ShortCutInit(activeChar));
		if(!first)
			for(final int shotId : activeChar.getAutoSoulShot())
				this.sendPacket(new ExAutoSoulShot(shotId, true));
		activeChar.updateEffectIcons();
		activeChar.updateStats();
		if(Config.PCBANG_POINTS_ENABLED)
			activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));
		if(Config.SERVICES_CHAR_KEY && activeChar.isKeyBlocked())
			activeChar.sendMessage(new CustomMessage("l2s.KeyFrozen"));

		if(first)
		{
			activeChar.useTriggers(activeChar, TriggerType.ON_ENTER_WORLD, null, null, 0);
		}
	}

	public static void notifyFriends(final Player cha, final boolean login)
	{
		if(login)
			cha.sendPacket(new FriendList(cha, false));
		try
		{
			for(final Integer friend_id : FriendsTable.getInstance().getFriendsList(cha.getObjectId()))
			{
				final Player friend = GameObjectsStorage.getPlayer(friend_id);
				if(friend != null)
					if(login)
						friend.sendPacket(new SystemMessage(503).addString(cha.getName()), new FriendStatus(cha, true));
					else
						friend.sendPacket(new FriendStatus(cha, false));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(Config.ALLOW_MAIL)
			FriendsTable.checkMail(cha);
	}

	private static void notifyClanMembers(final Player activeChar)
	{
		final Clan clan = activeChar.getClan();
		if(clan == null || clan.getClanMember(Integer.valueOf(activeChar.getObjectId())) == null)
			return;
		clan.getClanMember(Integer.valueOf(activeChar.getObjectId())).setPlayerInstance(activeChar);
		final int sponsor = activeChar.getSponsor();
		final int apprentice = activeChar.getApprentice();
		final SystemMessage msg = new SystemMessage(304).addString(activeChar.getName());
		final PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(activeChar);
		for(final Player clanMember : clan.getOnlineMembers(activeChar.getObjectId()))
		{
			clanMember.sendPacket(memberUpdate);
			if(clanMember.getObjectId() == sponsor)
				clanMember.sendPacket(new SystemMessage(1756).addString(activeChar.getName()));
			else if(clanMember.getObjectId() == apprentice)
				clanMember.sendPacket(new SystemMessage(1758).addString(activeChar.getName()));
			else
				clanMember.sendPacket(msg);
		}
		if(clan.isNoticeEnabled() && clan.getNotice() != "")
		{
			final NpcHtmlMessage notice = new NpcHtmlMessage(5);
			notice.setHtml("<html><body><center><font color=\"LEVEL\">" + activeChar.getClan().getName() + " Clan Notice</font></center><br>" + activeChar.getClan().getNotice() + "</body></html>");
			activeChar.sendPacket(notice);
		}
		if(!activeChar.isClanLeader())
			return;
		final ClanHall clanHall = clan.getHasHideout() > 0 ? ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
		if(clanHall == null || clanHall.getAuctionLength() != 0)
			return;

		SiegeEvent<?, ?> siegeEvent = clanHall.getSiegeEvent();
		if(siegeEvent == null || siegeEvent.getClass() != ClanHallAuctionEvent.class)
			return;
		if(clan.getWarehouse().getAdenaCount() < clanHall.getRentalFee())
			activeChar.sendPacket(new SystemMessage(1051).addNumber(Long.valueOf(clanHall.getRentalFee())));
	}

	private void loadTutorial(final Player player)
	{
		player.processQuestEvent(255, "UC", null);
	}
}
