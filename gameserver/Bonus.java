package l2s.gameserver;

import l2s.gameserver.dao.AccountBonusDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.BonusRequest;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.SocialAction;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Util;

public class Bonus
{
	public float RATE_XP;
	public float RATE_SP;
	public float RATE_PARTY_XP;
	public float RATE_PARTY_SP;
	public float RATE_QUESTS_REWARD;
	public float RATE_QUESTS_DROP;
	public float RATE_DROP_ADENA;
	public float RATE_DROP_ITEMS;
	public float RATE_DROP_SPOIL;
	public float LUCK_MULTIPLIER;

	public Bonus()
	{
		RATE_XP = 1.0f;
		RATE_SP = 1.0f;
		RATE_PARTY_XP = 1.0f;
		RATE_PARTY_SP = 1.0f;
		RATE_QUESTS_REWARD = 1.0f;
		RATE_QUESTS_DROP = 1.0f;
		RATE_DROP_ADENA = 1.0f;
		RATE_DROP_ITEMS = 1.0f;
		RATE_DROP_SPOIL = 1.0f;
		LUCK_MULTIPLIER = 1.0f;
	}

	public void restore(final Player player)
	{
		if(player == null || player.getNetConnection() == null)
			return;
		if(Config.SERVICES_RATE_TYPE > 0)
		{
			if(player.getNetConnection() == null)
				return;
			RATE_XP = 1.0f;
			RATE_SP = 1.0f;
			RATE_DROP_ADENA = 1.0f;
			RATE_DROP_ITEMS = 1.0f;
			RATE_DROP_SPOIL = 1.0f;
			LUCK_MULTIPLIER = 1.0f;
			LUCK_MULTIPLIER *= player.getNetConnection().getBonus();
			if(LUCK_MULTIPLIER == 0.0f)
				LUCK_MULTIPLIER = 1.0f;
			RATE_XP *= LUCK_MULTIPLIER;
			RATE_SP *= LUCK_MULTIPLIER;
			RATE_DROP_ADENA *= LUCK_MULTIPLIER;
			RATE_DROP_ITEMS *= LUCK_MULTIPLIER;
			RATE_DROP_SPOIL *= LUCK_MULTIPLIER;
			player.startBonusTask();
		}
	}

	public static void newbieBonus(final int objId, final float bonus, final int hours, final GameClient client)
	{
		if(mysql.simple_get_int("obj_Id", "char_bonus", "account='" + client.getLogin() + "'" + (Config.START_PA_CHECK_IP ? " OR ip='" + client.getIpAddr() + "'" : "") + (Config.START_PA_CHECK_HWID ? " OR hwid='" + client.getHWID() + "'" : "")) > 0)
			return;
		mysql.set("REPLACE INTO `char_bonus` (obj_Id, account, ip, hwid) values(" + objId + ",'" + client.getLogin() + "','" + client.getIpAddr() + "','" + client.getHWID() + "')");
		final int bonusExpire = (int) (System.currentTimeMillis() / 1000L) + hours * 3600;
		if(Config.SERVICES_RATE_TYPE > 1)
		{
			if(mysql.simple_get_int("bonus_expire", "account_bonus", "account='" + client.getLogin() + "'") > bonusExpire)
				return;
			AccountBonusDAO.getInstance().insert(Config.SERVICES_RATE_TYPE == 3 ? String.valueOf(objId) : client.getLogin(), bonus, bonusExpire);
		}
		//else TODO: Перенести ПА систему.
			//AuthServerCommunication.getInstance().sendPacket(new BonusRequest(client.getLogin(), bonus, bonusExpire));
		client.setBonus(bonus);
		client.setBonusExpire(bonusExpire);
	}

	public static void giveBonus(final Player player, final float bonus, final int hours)
	{
		final int bonusExpire = (int) (System.currentTimeMillis() / 1000L) + hours * 3600;
		if(Config.SERVICES_RATE_TYPE > 1)
			AccountBonusDAO.getInstance().insert(Config.SERVICES_RATE_TYPE == 3 ? String.valueOf(player.getObjectId()) : player.getAccountName(), bonus, bonusExpire);
		else
			AuthServerCommunication.getInstance().sendPacket(new BonusRequest(player.getAccountName(), bonus, bonusExpire));
		player.getNetConnection().setBonus(bonus);
		player.getNetConnection().setBonusExpire(bonusExpire);
		player.restoreBonus();
		if(player.getParty() != null)
			player.getParty().recalculatePartyData();
		Functions.show(HtmCache.getInstance().getHtml("scripts/services/RateBonusGet.htm", player), player);
	}

	public static void givePB(final Player player, final long hours)
	{
		final String v = player.getVar("PremiumBuff");
		if(v != null && Long.parseLong(v) > System.currentTimeMillis())
		{
			player.setVar("PremiumBuff", String.valueOf(Long.parseLong(v) + 3600000L * hours));
			if(player.isLangRus())
				player.sendMessage("\u041f\u0440\u0435\u043c\u0438\u0443\u043c \u0434\u043e\u0441\u0442\u0443\u043f \u043a \u0431\u0430\u0444\u0444\u0443 \u043f\u0440\u043e\u0434\u043b\u0435\u043d \u043d\u0430 " + hours + " " + Util.hourFormat(true, String.valueOf(hours)) + ".");
			else
				player.sendMessage("Extended premium access to buff on " + hours + " " + Util.hourFormat(false, String.valueOf(hours)) + ".");
		}
		else
		{
			player.setVar("PremiumBuff", String.valueOf(System.currentTimeMillis() + 3600000L * hours));
			if(player.isLangRus())
				player.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043f\u0440\u0435\u043c\u0438\u0443\u043c \u0434\u043e\u0441\u0442\u0443\u043f \u043a \u0431\u0430\u0444\u0444\u0443 \u043d\u0430 " + hours + " " + Util.hourFormat(true, String.valueOf(hours)) + ".");
			else
				player.sendMessage("You have received premium access to buff on " + hours + " " + Util.hourFormat(false, String.valueOf(hours)) + ".");
		}
	}

	public static void giveHS(final Player player, final long hours)
	{
		final String v = player.getVar("HeroStatus");
		if(v != null && Long.parseLong(v) > System.currentTimeMillis())
		{
			player.setVar("HeroStatus", String.valueOf(Long.parseLong(v) + 3600000L * hours));
			if(player.isLangRus())
				player.sendMessage("\u0421\u0442\u0430\u0442\u0443\u0441 \u0413\u0435\u0440\u043e\u044f \u043f\u0440\u043e\u0434\u043b\u0435\u043d \u043d\u0430 " + hours + " " + Util.hourFormat(true, String.valueOf(hours)) + ".");
			else
				player.sendMessage("Extended Hero status on " + hours + " " + Util.hourFormat(false, String.valueOf(hours)) + ".");
		}
		else
		{
			player.setVar("HeroStatus", String.valueOf(System.currentTimeMillis() + 3600000L * hours));
			if(player.isLangRus())
				player.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u0441\u0442\u0430\u0442\u0443\u0441 \u0413\u0435\u0440\u043e\u044f \u043d\u0430 " + hours + " " + Util.hourFormat(true, String.valueOf(hours)) + ".");
			else
				player.sendMessage("You have received Hero status on " + hours + " " + Util.hourFormat(false, String.valueOf(hours)) + ".");
			if(!player.isHero())
			{
				player.setHero(true);
				Hero.addSkills(player);
				player.updatePledgeClass();
				player.sendPacket(new SkillList(player));
				player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
				player.broadcastUserInfo(true);
			}
		}
	}
}
