package l2s.gameserver.skills.skillclasses;

import java.util.List;
import java.util.Set;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.util.Rnd;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Fishing;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.FishTable;
import l2s.gameserver.templates.FishTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public class FishingSkill extends Skill
{
	public FishingSkill(final StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		final Player player = (Player) activeChar;
		if(player.getSkillLevel(1315) == -1)
			return false;
		if(player.isFishing())
		{
			player.stopFishing();
			player.sendPacket(new SystemMessage(1458));
			return false;
		}
		if(player.isInVehicle())
		{
			activeChar.sendPacket(new SystemMessage(1456));
			return false;
		}
		if(player.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(1638));
			return false;
		}
		if(player.getReflectionId() != 0)
		{
			player.sendPacket(new SystemMessage(1457));
			return false;
		}
		final Zone fishingZone = player.getZone(Zone.ZoneType.FISHING);
		if(fishingZone == null)
		{
			player.sendPacket(new SystemMessage(1457));
			return false;
		}
		final WeaponTemplate weaponItem = player.getActiveWeaponItem();
		if(weaponItem == null || weaponItem.getItemType() != WeaponTemplate.WeaponType.ROD)
		{
			player.sendPacket(new SystemMessage(1453));
			return false;
		}
		final ItemInstance lure = player.getInventory().getPaperdollItem(8);
		if(lure == null || lure.getCount() < 1L)
		{
			player.sendPacket(new SystemMessage(1454));
			return false;
		}
		final int rnd = Rnd.get(50) + 150;
		final double angle = Util.convertHeadingToDegree(player.getHeading());
		final double radian = Math.toRadians(angle - 90.0);
		final double sin = Math.sin(radian);
		final double cos = Math.cos(radian);
		final int x1 = -(int) (sin * rnd);
		final int y1 = (int) (cos * rnd);
		final int x2 = player.getX() + x1;
		final int y2 = player.getY() + y1;
		int z = GeoEngine.getLowerHeight(x2, y2, player.getZ(), player.getGeoIndex()) + 1;
		boolean isInWater = fishingZone.getParams().getInteger("fishing_place_type") == 2;
		final LazyArrayList<Zone> zones = LazyArrayList.newInstance();
		World.getZones(zones, new Location(x2, y2, z));
		for(final Zone zone : zones)
			if(zone.getType() == Zone.ZoneType.water)
			{
				z = zone.getLoc().getZmax();
				isInWater = true;
				break;
			}
		LazyArrayList.recycle(zones);
		if(!isInWater)
		{
			player.sendPacket(new SystemMessage(1457));
			return false;
		}
		player.getFishing().setFishLoc(new Location(x2, y2, z));
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature caster, final Set<Creature> targets)
	{
		if(caster == null || !caster.isPlayer())
			return;
		final Player player = (Player) caster;
		final ItemInstance lure = player.getInventory().getPaperdollItem(8);
		if(lure == null || lure.getCount() < 1L)
		{
			player.sendPacket(new SystemMessage(1454));
			return;
		}
		final Zone zone = player.getZone(Zone.ZoneType.FISHING);
		if(zone == null)
			return;
		final int distributionId = zone.getParams().getInteger("distribution_id");
		final int lureId = lure.getItemId();
		final int fishLvl = Fishing.getRandomFishLvl(player);
		final int group = Fishing.getFishGroup(lureId);
		final int type = Fishing.getRandomFishType(lureId, fishLvl, distributionId);
		final List<FishTemplate> fishs = FishTable.getInstance().getFish(group, type, fishLvl);
		if(fishs == null || fishs.size() == 0)
		{
			player.sendPacket(new SystemMessage(399));
			return;
		}
		final ItemInstance lure2 = player.getInventory().destroyItem(player.getInventory().getPaperdollObjectId(8), 1L, false);
		if(lure2 == null || lure2.getCount() == 0L)
		{
			player.sendPacket(new SystemMessage(1459));
			return;
		}
		final int check = Rnd.get(fishs.size());
		final FishTemplate fish = fishs.get(check);
		player.startFishing(fish, lureId);
	}
}
