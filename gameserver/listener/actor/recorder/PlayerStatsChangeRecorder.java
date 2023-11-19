package l2s.gameserver.listener.actor.recorder;

import l2s.commons.collections.CollectionUtils;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCount;
import l2s.gameserver.network.l2.s2c.StatusUpdate;

public final class PlayerStatsChangeRecorder extends CharStatsChangeRecorder<Player>
{
	public static final int BROADCAST_KARMA = 8;
	public static final int SEND_STORAGE_INFO = 16;
	public static final int SEND_MAX_LOAD = 32;
	public static final int SEND_CUR_LOAD = 64;
	public static final int BROADCAST_TITLE = 128;
	public static final int SEND_MAX_CP = 256;
	public static final int SEND_MAX_HP = 512;
	public static final int SEND_MAX_MP = 1024;

	private int _maxCp;
	private int _maxHp;
	private int _maxMp;
	private int _maxLoad;
	private int _curLoad;
	private long _exp;
	private int _sp;
	private int _karma;
	private int _pk;
	private int _pvp;
	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _trade;
	private int _recipeDwarven;
	private int _recipeCommon;
	private int _partyRoom;
	private String _title;
	private int _cubicsHash;

	private int _weaponEnchant;
	private int _weaponVariation1;
	private int _weaponVariation2;

	public PlayerStatsChangeRecorder(final Player activeChar)
	{
		super(activeChar);
		_title = "";
	}

	@Override
	protected void refreshStats()
	{
		_maxCp = set(SEND_MAX_CP, _maxCp, _activeChar.getMaxCp());
		_maxHp = set(SEND_MAX_HP, _maxHp, _activeChar.getMaxHp());
		_maxMp = set(SEND_MAX_MP, _maxMp, _activeChar.getMaxMp());
		
		super.refreshStats();
		
		_maxLoad = set(SEND_MAX_LOAD, _maxLoad, _activeChar.getMaxLoad());
		_curLoad = set(SEND_CUR_LOAD, _curLoad, _activeChar.getCurrentLoad());
		_exp = set(SEND_CHAR_INFO, _exp, _activeChar.getExp());
		_sp = set(SEND_CHAR_INFO, _sp, _activeChar.getSp());
		_pk = set(SEND_CHAR_INFO, _pk, _activeChar.getPkKills());
		_pvp = set(SEND_CHAR_INFO, _pvp, _activeChar.getPvpKills());
		_karma = set(BROADCAST_KARMA, _karma, _activeChar.getKarma());
		_inventory = set(SEND_STORAGE_INFO, _inventory, _activeChar.getInventoryLimit());
		_warehouse = set(SEND_STORAGE_INFO, _warehouse, _activeChar.getWarehouseLimit());
		_clan = set(SEND_STORAGE_INFO, _clan, Config.WAREHOUSE_SLOTS_CLAN);
		_trade = set(SEND_STORAGE_INFO, _trade, _activeChar.getTradeLimit());
		_recipeDwarven = set(SEND_STORAGE_INFO, _recipeDwarven, _activeChar.getDwarvenRecipeLimit());
		_recipeCommon = set(SEND_STORAGE_INFO, _recipeCommon, _activeChar.getCommonRecipeLimit());
		_title = set(BROADCAST_TITLE, _title, _activeChar.getTitle());
		_cubicsHash = set(BROADCAST_CHAR_INFO, _cubicsHash, CollectionUtils.hashCode(_activeChar.getCubics()));
		_partyRoom = set(BROADCAST_CHAR_INFO, _partyRoom, _activeChar.getPartyRoom() != null && _activeChar.getPartyRoom().getLeader() == _activeChar ? _activeChar.getPartyRoom().getId() : 0);

		_weaponEnchant = set(BROADCAST_CHAR_INFO, _weaponEnchant, _activeChar.getEnchantEffect());
		_weaponVariation1 = set(BROADCAST_CHAR_INFO, _weaponVariation1, _activeChar.getVariation1Id());
		_weaponVariation2 = set(BROADCAST_CHAR_INFO, _weaponVariation2, _activeChar.getVariation2Id());
	}

	@Override
	protected void onSendChanges()
	{
		if((_changes & 0x1) == 0x1)
			_activeChar.broadcastUserInfo(false);
		else if((_changes & 0x80) == 0x80)
			_activeChar.broadcastTitleInfo();
		else if((_changes & 0x2) == 0x2)
			_activeChar.sendUserInfo(false);
		else
		{
			final StatusUpdate su = new StatusUpdate(_activeChar.getObjectId());
			if((_changes & 0x100) == 0x100)
				su.addAttribute(34, _activeChar.getMaxCp());
			if((_changes & 0x200) == 0x200)
				su.addAttribute(10, _activeChar.getMaxHp());
			if((_changes & 0x400) == 0x400)
				su.addAttribute(12, _activeChar.getMaxMp());
			if(su.hasAttributes())
			{
				_activeChar.sendPacket(su);
				_activeChar.broadcastStatusUpdate();
			}
		}
		if((_changes & 0x40) == 0x40)
			_activeChar.sendStatusUpdate(false, false, 14);
		if((_changes & 0x20) == 0x20)
			_activeChar.sendStatusUpdate(false, false, 15);
		if((_changes & 0x8) == 0x8)
			_activeChar.sendStatusUpdate(true, false, 27);
		if((_changes & 0x10) == 0x10)
			_activeChar.sendPacket(new ExStorageMaxCount(_activeChar));
	}
}
