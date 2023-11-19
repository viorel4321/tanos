package l2s.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import l2s.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.communitybbs.CommunityBoard;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.network.l2.s2c.ShowBoard;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.utils.EffectsComparator;
import l2s.gameserver.utils.Util;

public class BuffBBSManager extends BaseBBSManager {
    private static final Logger _log;
    private static BuffBBSManager _Instance;

    public static BuffBBSManager getInstance() {
        if (BuffBBSManager._Instance == null)
            BuffBBSManager._Instance = new BuffBBSManager();
        return BuffBBSManager._Instance;
    }

    @Override
    public void parsecmd(final String command, final Player player) {
        if (command.equals("_bbsbuff;"))
            showBuffList(player);
        else if (command.startsWith("_bbsbuff;buff;")) {
            final StringTokenizer stBuff = new StringTokenizer(command, ";");
            stBuff.nextToken();
            stBuff.nextToken();
            final String path = stBuff.nextToken();
            final int skill_id = Integer.parseInt(stBuff.nextToken());
            final int skill_lvl = Integer.parseInt(stBuff.nextToken());
            doBuff(skill_id, skill_lvl, player);
            TopBBSManager.getInstance().parsecmd("_bbstop;" + path, player);
        } else if (command.startsWith("_bbsbuff;grp;")) {
            final StringTokenizer stBuffGrp = new StringTokenizer(command, ";");
            stBuffGrp.nextToken();
            stBuffGrp.nextToken();
            final int id_groups = Integer.parseInt(stBuffGrp.nextToken());
            if (stBuffGrp.hasMoreTokens())
                TopBBSManager.getInstance().parsecmd("_bbstop;" + stBuffGrp.nextToken(), player);
            else
                showBuffList(player);
            doBuffGroup(id_groups, player);
        } else if (command.startsWith("_bbsbuff;cancel;")) {
            if (!checkCondition(player))
                return;
            final StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            final String page = st.nextToken();
            if (page.equals("70"))
                showBuffList(player);
            else
                TopBBSManager.getInstance().parsecmd("_bbstop;" + page, player);
            final Servitor pet = player.getServitor();
            if ((!player.getVarBoolean("BuffSummon") || pet == null) && player.getAbnormalList().getEffectsBySkillId(4515) == null && player.getAbnormalList().getEffectsBySkillId(3632) == null)
                player.getAbnormalList().stop();
            else if (pet != null && pet.getAbnormalList().getEffectsBySkillId(4515) == null)
                pet.getAbnormalList().stopAll();
        } else if (command.startsWith("_bbsbuff;heal;")) {
            if (!checkCondition(player))
                return;

            ItemInstance i = player.getInventory().getItemByItemId(Config.HEAL_COIN);
            if (i == null || i.getCount() < Config.HEAL_PRICE) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                return;
            }
            player.sendMessage(Config.HEAL_PRICE + " " + i.getName() + " disappeared");
            player.getInventory().destroyItem(i, Config.HEAL_PRICE, false);

            final StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();

            final String page = st.nextToken();
            if (page.equals("70"))
                showBuffList(player);
            else
                TopBBSManager.getInstance().parsecmd("_bbstop;" + page, player);
            final Servitor pet = player.getServitor();
            if (!player.getVarBoolean("BuffSummon") || pet == null) {
                player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), false);
                player.setCurrentCp(player.getMaxCp());
            } else
                pet.setCurrentHpMp(pet.getMaxHp(), pet.getMaxMp(), false);
        } else if (command.startsWith("_bbsbuff;save;")) {
            if (!Config.BUFFER_SAVE_RESTOR) {
                player.sendMessage(player.isLangRus() ? "\u0412 \u0434\u0430\u043d\u043d\u044b\u0439 \u043c\u043e\u043c\u0435\u043d\u0442 \u044d\u0442\u0430 \u0444\u0443\u043d\u043a\u0446\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "This function disabled.");
                return;
            }
            final StringTokenizer stAdd = new StringTokenizer(command, ";");
            stAdd.nextToken();
            stAdd.nextToken();
            String SchNameAdd = null;
            if (stAdd.hasMoreTokens())
                SchNameAdd = stAdd.nextToken().trim();
            if (SchNameAdd != null && !SchNameAdd.equals(""))
                SAVE(player, SchNameAdd);
            else
                player.sendMessage("\u0412\u044b \u043d\u0435 \u0432\u0432\u0435\u043b\u0438 \u0438\u043c\u044f \u0437\u0430\u043a\u043b\u0430\u0434\u043a\u0438.");
            showBuffList(player);
        } else if (command.startsWith("_bbsbuff;restore;")) {
            if (!Config.BUFFER_SAVE_RESTOR) {
                player.sendMessage(player.isLangRus() ? "\u0412 \u0434\u0430\u043d\u043d\u044b\u0439 \u043c\u043e\u043c\u0435\u043d\u0442 \u044d\u0442\u0430 \u0444\u0443\u043d\u043a\u0446\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "This function disabled.");
                return;
            }
            final StringTokenizer stBuff = new StringTokenizer(command, ";");
            stBuff.nextToken();
            stBuff.nextToken();
            showBuffList(player);
            RESTOR(player, stBuff.nextToken().trim());
        } else if (command.startsWith("_bbsbuff;delete;")) {
            if (!Config.BUFFER_SAVE_RESTOR) {
                player.sendMessage(player.isLangRus() ? "\u0412 \u0434\u0430\u043d\u043d\u044b\u0439 \u043c\u043e\u043c\u0435\u043d\u0442 \u044d\u0442\u0430 \u0444\u0443\u043d\u043a\u0446\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "This function disabled.");
                return;
            }
            final StringTokenizer stBuff = new StringTokenizer(command, ";");
            stBuff.nextToken();
            stBuff.nextToken();
            player.schemesB.remove(stBuff.nextToken().trim());
            showBuffList(player);
        } else if (command.startsWith("_bbsbuff;target;")) {
            final StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            final String page = st.nextToken();
            if (page.equals("70"))
                showBuffList(player);
            else
                TopBBSManager.getInstance().parsecmd("_bbstop;" + page, player);
            if (player.getServitor() == null) {
                if (!player.isLangRus())
                    player.sendMessage("You don't have a summon.");
                else
                    player.sendMessage("\u0423 \u0432\u0430\u0441 \u043d\u0435\u0442 \u0441\u0430\u043c\u043c\u043e\u043d\u0430.");
                return;
            }
            if (!player.getVarBoolean("BuffSummon")) {
                player.setVar("BuffSummon", "1");
                if (!player.isLangRus())
                    player.sendMessage("Target for buffs: Summon");
                else
                    player.sendMessage("\u0426\u0435\u043b\u044c \u0434\u043b\u044f \u0431\u0430\u0444\u0444\u0430: \u0421\u0430\u043c\u043c\u043e\u043d");
            } else {
                player.unsetVar("BuffSummon");
                if (!player.isLangRus())
                    player.sendMessage("Target for buffs: Player");
                else
                    player.sendMessage("\u0426\u0435\u043b\u044c \u0434\u043b\u044f \u0431\u0430\u0444\u0444\u0430: \u041f\u0435\u0440\u0441\u043e\u043d\u0430\u0436");
            }
        } else
            ShowBoard.separateAndSend("<html><body><br><br><center>\u0412 CommunityBuffer \u0444\u0443\u043d\u043a\u0446\u0438\u044f: " + command + " \u043f\u043e\u043a\u0430 \u043d\u0435 \u0440\u0435\u0430\u043b\u0438\u0437\u043e\u0432\u0430\u043d\u0430</center><br><br></body></html>", player);
    }

    public void doBuff(final int skill_id, final int skill_lvl, final Player player) {
        if (!checkCondition(player))
            return;
        if (!Config.CB_BUFFS.containsKey(skill_id) || Config.CB_BUFFS.get(skill_id) != skill_lvl) {
            BuffBBSManager._log.warn("Player " + player.getName() + " [" + player.getObjectId() + "] tried to use illegal buff: " + skill_id);
            GmListTable.broadcastMessageToGMs("Player " + player.getName() + " [" + player.getObjectId() + "] tried to use illegal buff: " + skill_id);
            player.sendMessage("\u0413\u041c \u0438\u043d\u0444\u043e\u0440\u043c\u0438\u0440\u043e\u0432\u0430\u043d. \u041e\u0436\u0438\u0434\u0430\u0439\u0442\u0435 \u0431\u0430\u043d\u0430.");
            return;
        }
        if (Config.PVPCB_BUFFER_PRICE_ONE > 0) {
            ItemInstance i = player.getInventory().getItemByItemId(Config.PVPCB_BUFFER_PRICE_ITEM);
            if (i == null || i.getCount() < Config.PVPCB_BUFFER_PRICE_ONE) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                return;
            }
            player.sendMessage(Config.PVPCB_BUFFER_PRICE_ONE + " " + i.getName() + " disappeared");
            player.getInventory().destroyItem(i, Config.PVPCB_BUFFER_PRICE_ONE, false);
        }
        giveBuff(player, SkillTable.getInstance().getInfo(skill_id, skill_lvl), player.getServitor() != null && player.getVarBoolean("BuffSummon"), 0);
    }

    public void doBuffGroup(final int id_groups, final Player player) {
        if (!checkCondition(player))
            return;
        if (Config.PVPCB_BUFFER_PRICE_GRP > 0) {
            ItemInstance i = player.getInventory().getItemByItemId(Config.PVPCB_BUFFER_PRICE_ITEM);
            if (i == null || i.getCount() < Config.PVPCB_BUFFER_PRICE_GRP) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                return;
            }
            player.sendMessage(Config.PVPCB_BUFFER_PRICE_GRP + " " + i.getName() + " disappeared");
            player.getInventory().destroyItem(i, Config.PVPCB_BUFFER_PRICE_GRP, false);
        }
        final boolean sn = player.getServitor() != null && player.getVarBoolean("BuffSummon");
        for (final int[] buff : Config.GROUP_BUFFS)
            if (buff[2] == id_groups)
                giveBuff(player, SkillTable.getInstance().getInfo(buff[0], buff[1]), sn, 0);
    }

    public void showBuffList(final Player player) {
        final TextBuilder html = new TextBuilder();
        html.append("<table width=120>");
        for (final String i : player.schemesB.keySet()) {
            html.append("<tr>");
            html.append("<td>");
            html.append("<button value=\"" + i + "\" action=\"bypass _bbsbuff;restore;" + i + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            html.append("</td>");
            html.append("<td>");
            html.append("<button value=\"-\" action=\"bypass _bbsbuff;delete;" + i + "\" width=20 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            html.append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        String content = HtmCache.getInstance().getHtml("CommunityBoard/70.htm", player);
        content = content.replace("%sch%", html.toString());
        content = CommunityBoard.htmlAll(content, player);
        separateAndSend(content, player);
    }

    private void SAVE(final Player player, final String SchName) {
        if (!Util.isMatchingRegexp(SchName, Config.BUFF_SCHEM_NAME)) {
            player.sendMessage(player.isLangRus() ? "\u041d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u043e\u0435 \u0438\u043c\u044f \u0441\u0445\u0435\u043c\u044b!" : "Incorrect scheme name!");
            return;
        }
        String allbuff = "";
        final Abnormal[] skill = player.getAbnormalList().getAllFirstEffects();
        Arrays.sort(skill, EffectsComparator.getInstance());
        final String v = player.getVar("PremiumBuff");
        final HashMap<Integer, Integer> aEff = v != null && Long.parseLong(v) > System.currentTimeMillis() || Config.DELUXE_BUFF_PREMIUM && player.isPremium() || Config.DELUXE_BUFF_ITEM > 0 && player.getInventory().getItemByItemId(Config.DELUXE_BUFF_ITEM) != null ? Config.DELUXE_BUFFS : Config.CB_BUFFS;
        for (int i = 0; i < skill.length; ++i) {
            final int sId = skill[i].getSkill().getId();
            final int sLvl = skill[i].getSkill().getLevel();
            if (aEff.containsKey(sId) && aEff.get(sId) == sLvl)
                allbuff += sId + "," + sLvl + ";";
        }
        if (allbuff.equals(""))
            player.sendMessage(player.isLangRus() ? "\u041d\u0430 \u043f\u0435\u0440\u0441\u043e\u043d\u0430\u0436\u0435 \u043d\u0435\u0442 \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0445 \u0431\u0430\u0444\u0444\u043e\u0432 \u0434\u043b\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u044f." : "No valid buffs to save.");
        else if (player.schemesB.size() < Config.MAX_BUFF_SCHEM) {
            if (player.schemesB.containsKey(SchName))
                player.sendMessage(player.isLangRus() ? "\u042d\u0442\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0443\u0436\u0435 \u0437\u0430\u043d\u044f\u0442\u043e." : "This name is already taken.");
            else
                player.schemesB.put(SchName, allbuff);
        } else
            player.sendMessage(player.isLangRus() ? "\u0412\u044b \u0443\u0436\u0435 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u043b\u0438 \u043c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0441\u0445\u0435\u043c." : "You have already saved maximum schemes count.");
    }

    public void RESTOR(final Player player, final String scheme) //use buff profile
    {
        if (!checkCondition(player))
            return;
        if (Config.PVPCB_BUFFER_PRICE_GRP > 0) {
            if (player.getAdena() < Config.PVPCB_BUFFER_PRICE_GRP) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            player.reduceAdena(Config.PVPCB_BUFFER_PRICE_GRP, true);
        }
        try {
            if (!player.schemesB.containsKey(scheme))
                return;
            final String allskills = player.schemesB.get(scheme);
            final StringTokenizer stBuff = new StringTokenizer(allskills, ";");
            String v = player.getVar("PremiumBuff");
            if (v != null && Long.parseLong(v) < System.currentTimeMillis()) {
                player.sendMessage(player.isLangRus() ? "\u0412\u0430\u0448 \u043f\u0440\u0435\u043c\u0438\u0443\u043c \u0434\u043e\u0441\u0442\u0443\u043f \u043a \u0431\u0430\u0444\u0444\u0443 \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0441\u044f." : "Your premium access for buff is over.");
                player.unsetVar("PremiumBuff");
                v = null;
            }
            final HashMap<Integer, Integer> aEff = v != null || Config.DELUXE_BUFF_PREMIUM && player.isPremium() || Config.DELUXE_BUFF_ITEM > 0 && player.getInventory().getItemByItemId(Config.DELUXE_BUFF_ITEM) != null ? Config.DELUXE_BUFFS : Config.CB_BUFFS;
            int i = 0;
            final boolean sn = player.getServitor() != null && player.getVarBoolean("BuffSummon");
            int count_paid_items_for_prem_buff = 0;
            ItemInstance item_pay = null;
            while (stBuff.hasMoreTokens()) {
                final String s = stBuff.nextToken();
                final StringTokenizer sk = new StringTokenizer(s, ",");
                final int id = Integer.parseInt(sk.nextToken());
                final int lvl = Integer.parseInt(sk.nextToken());
                if (aEff.containsKey(id)) {
                    if (aEff.get(id) != lvl)
                        continue;
                    for (int m = 0; m < Config.ID_PREMIUM_BUFF_IN_PROFILES_USE.size(); m++) {
                        if (Config.ID_PREMIUM_BUFF_IN_PROFILES_USE.get(m).equals(id)) {
                            if (player.getInventory().getItemByItemId(Config.ITEM_ID_PREMIUM_BUFF_IN_BUFF_PROFILE).getCount() >= Config.ITEM_COUNT_PREMIUM_BUFF_IN_BUFF_PROFILE) {
                                item_pay = player.getInventory().getItemByItemId(Config.ITEM_ID_PREMIUM_BUFF_IN_BUFF_PROFILE);
                                player.getInventory().destroyItem(item_pay, Config.ITEM_COUNT_PREMIUM_BUFF_IN_BUFF_PROFILE, true);
                                count_paid_items_for_prem_buff++;
                            } else {
                                continue;
                            }
                        }
                    }
                    giveBuff(player, SkillTable.getInstance().getInfo(id, lvl), sn, i);
                    ++i;
                }
            }
            if (count_paid_items_for_prem_buff > 0) {
                player.sendMessage("Paid " + count_paid_items_for_prem_buff + " " + item_pay.getName() + " for premium buffs.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initSchemes(final Player player) {
        if (!Config.BUFFER_SAVE_RESTOR)
            return;
        if (player.schemesB == null)
            player.schemesB = new HashMap<String, String>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT name,skills FROM buffer_skillsave WHERE charId=?");
            statement.setInt(1, player.getObjectId());
            rset = statement.executeQuery();
            while (rset.next())
                player.schemesB.put(rset.getString("name"), rset.getString("skills"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public static void storeSchemes(final Player player) {
        if (!Config.BUFFER_SAVE_RESTOR || player.schemesB == null)
            return;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM buffer_skillsave WHERE charId=?");
            statement.setInt(1, player.getObjectId());
            statement.execute();
            if (player.schemesB != null && !player.schemesB.isEmpty())
                for (final String i : player.schemesB.keySet()) {
                    DbUtils.closeQuietly(statement);
                    statement = con.prepareStatement("REPLACE INTO buffer_skillsave (charId,name,skills) VALUES(?,?,?)");
                    statement.setInt(1, player.getObjectId());
                    statement.setString(2, i);
                    statement.setString(3, player.schemesB.get(i));
                    statement.execute();
                }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        player.schemesB = null;
    }

    public boolean checkCondition(final Player player) {
        if (player == null)
            return false;
        if (!Config.ALLOW_PVPCB_BUFFER) {
            if (!player.isLangRus())
                player.sendMessage("This function disabled.");
            else
                player.sendMessage("\u0424\u0443\u043d\u043a\u0446\u0438\u044f \u0431\u0430\u0444\u0444\u0430 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430.");
            return false;
        }
        if (Config.PVPCB_BUFFER_PEACE && !player.isInZonePeace()) {
            if (!player.isLangRus())
                player.sendMessage("Buff is available only in a peace zone.");
            else
                player.sendMessage("\u0411\u0430\u0444\u0444 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043c\u0438\u0440\u043d\u043e\u0439 \u0437\u043e\u043d\u0435.");
            return false;
        }
        if (player.isInOlympiadMode()) {
            if (!player.isLangRus())
                player.sendMessage("You can not use this function in Olympiad.");
            else
                player.sendMessage("\u0412\u043e \u0432\u0440\u0435\u043c\u044f \u041e\u043b\u0438\u043c\u043f\u0438\u0430\u0434\u044b \u043d\u0435\u043b\u044c\u0437\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0434\u0430\u043d\u043d\u0443\u044e \u0444\u0443\u043d\u043a\u0446\u0438\u044e.");
            return false;
        }
        if (player.isInCombat()) {
            player.sendMessage(player.isLangRus() ? "\u0411\u0430\u0444\u0444 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0432 \u0431\u043e\u044e." : "You can not use buff in combat.");
            return false;
        }
        if (player.getLevel() > Config.PVPCB_BUFFER_MAX_LVL || player.getLevel() < Config.PVPCB_BUFFER_MIN_LVL) {
            if (!player.isLangRus())
                player.sendMessage("Your level does not meet the requirements!");
            else
                player.sendMessage("\u0412\u0430\u0448 \u0443\u0440\u043e\u0432\u0435\u043d\u044c \u043d\u0435 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f\u043c!");
            return false;
        }
        if (!Config.PVPCB_BUFFER_ALLOW_EVENT && player.getTeam() > 0) {
            if (!player.isLangRus())
                player.sendMessage("You can not use buff in event.");
            else
                player.sendMessage("\u041d\u0435\u043b\u044c\u0437\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0431\u0430\u0444\u0444 \u0432 \u044d\u0432\u0435\u043d\u0442\u0430\u0445.");
            return false;
        }
        if (!Config.PVPCB_BUFFER_ALLOW_SIEGE && player.getEvent(SiegeEvent.class) != null) {
            if (!player.isLangRus())
                player.sendMessage("You can not use buff in siege.");
            else
                player.sendMessage("\u041d\u0435\u043b\u044c\u0437\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0431\u0430\u0444\u0444 \u0432\u043e \u0432\u0440\u0435\u043c\u044f \u043e\u0441\u0430\u0434\u044b.");
            return false;
        }
        if (!Config.PVPCB_BUFFER_ALLOW_PK && player.getKarma() > 0) {
            player.sendMessage(player.isLangRus() ? "\u0411\u0430\u0444\u0444 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0432 \u043a\u0430\u0440\u043c\u0435." : "You can not use buff with karma.");
            return false;
        }
        if (Config.NO_BUFF_EPIC && player.isInZone(Zone.ZoneType.epic)) {
            player.sendMessage(player.isLangRus() ? "\u0411\u0430\u0444\u0444 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0432 \u044d\u043f\u0438\u043a \u043b\u043e\u043a\u0430\u0446\u0438\u0438." : "You can not use buff in epic zone.");
            return false;
        }
        return true;
    }

    private void giveBuff(final Player player, final Skill skill, final boolean sn, final int i) {
        for (final EffectTemplate et : skill.getEffectTemplates()) {
            final Env env = new Env(sn ? player.getServitor() : player, sn ? player.getServitor() : player, skill);
            final Abnormal effect = et.getEffect(env);
            if (Config.PVPCB_BUFFER_ALT_TIME > 0L && (effect.getCount() != 1 || effect.getPeriod() != 0L))
                effect.setPeriod(Config.PVPCB_BUFFER_ALT_TIME);
            effect.setStartTime(System.currentTimeMillis() + i);
            if (sn)
                player.getServitor().getAbnormalList().add(effect);
            else
                player.getAbnormalList().add(effect);
        }
    }

    @Override
    public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player player) {
    }

    static {
        _log = LoggerFactory.getLogger(BuffBBSManager.class);
        BuffBBSManager._Instance = null;
    }
}
