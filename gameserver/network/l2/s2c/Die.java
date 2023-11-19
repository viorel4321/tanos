package l2s.gameserver.network.l2.s2c;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.pledge.Clan;

public class Die extends L2GameServerPacket
{
	private int _objectId;
	private boolean _fake;
	private boolean _sweepable;
	private Map<RestartType, Boolean> _types;

	public Die(final Creature cha)
	{
		_types = new HashMap<RestartType, Boolean>(RestartType.VALUES.length);
		_objectId = cha.getObjectId();
		_fake = !cha.isDead();
		if(cha.isMonster())
			_sweepable = ((MonsterInstance) cha).isSweepActive();
		else if(cha.isPlayer())
		{
			final Player player = (Player) cha;
			put(RestartType.FIXED, player.getPlayerAccess().ResurectFixed);
			put(RestartType.TO_VILLAGE, true);
			Clan clan = null;
			if(get(RestartType.TO_VILLAGE))
				clan = player.getClan();
			if(clan != null)
			{
				put(RestartType.TO_CLANHALL, clan.getHasHideout() > 0);
				put(RestartType.TO_CASTLE, clan.getHasCastle() > 0);
			}
			for(final GlobalEvent e : cha.getEvents())
				e.checkRestartLocs(player, _types);
			if(Config.ALLOW_PVP_ZONES_MOD && ArrayUtils.contains(Config.PVP_ZONES_MOD, player.getZoneIndex(Zone.ZoneType.battle_zone)))
				put(RestartType.TO_FLAG, true);
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(_fake)
			return;
		writeC(6);
		writeD(_objectId);
		writeD(1);
		writeD(get(RestartType.TO_CLANHALL) ? 1 : 0);
		writeD(get(RestartType.TO_CASTLE) ? 1 : 0);
		writeD(get(RestartType.TO_FLAG) ? 1 : 0);
		writeD(_sweepable ? 1 : 0);
		writeD(get(RestartType.FIXED) ? 1 : 0);
	}

	private void put(final RestartType t, final boolean b)
	{
		_types.put(t, b);
	}

	private boolean get(final RestartType t)
	{
		final Boolean b = _types.get(t);
		return b != null && b;
	}
}
