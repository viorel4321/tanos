package l2s.gameserver.utils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.reward.DropData;
import l2s.gameserver.model.reward.DropGroup;
import l2s.gameserver.templates.npc.NpcTemplate;

public abstract class DropList
{
	private static NumberFormat df = NumberFormat.getPercentInstance();
	static
	{
		df.setMaximumFractionDigits(4);
	}

	public static String generateDroplist(final NpcTemplate template, final MonsterInstance monster, double mod, final Player pl)
	{
		final boolean ru = pl != null && pl.isLangRus();
		final StringBuffer tmp = new StringBuffer();
		tmp.append("<html><body><center><font color=\"LEVEL\">").append(template.name);
		if(Config.SHOW_DROPLIST_NPCID)
			tmp.append(", Id: ").append(template.getId());
		tmp.append("</font>");
		if(template.isDropHerbs)
			tmp.append("<br1><font color=\"00FF00\">" + (ru ? "\u0425\u0435\u0440\u0431\u044b" : "Herbs") + "</font>");
		tmp.append("</center><table><tr><td></td></tr>");
		boolean emptylist = false;
		boolean overlevel = true;
		final boolean icons = pl != null && pl.getVarBoolean("DroplistIcons");
		final double rateAdena = mod * Config.getRateAdena(pl);
		final double mulDrop = monster == null ? 1.0 : monster.isRB() ? Config.RATE_DROP_RAIDBOSS : monster.isEpicBoss() ? Config.RATE_DROP_EPICBOSS : monster.isBox() ? Config.RATE_DROP_BOX : monster.isChest() ? Config.RATE_DROP_CHEST : (double) Config.RATE_DROP_ITEMS;
		double rateDrop = mod * mulDrop * (pl != null ? pl.getRateItems() : 1.0f);
		final double rateSpoil = mod * Config.RATE_DROP_SPOIL * (pl != null ? pl.getRateSpoil() : 1.0f);
		if(template.getDropData() != null)
		{
			if(template.getDropData().getNormal() != null)
				for(final DropGroup g : template.getDropData().getNormal())
				{
					if(g.isAdena() && rateAdena == 0.0)
						continue;
					if(!g.isAdena() && rateDrop == 0.0)
						continue;
					overlevel = false;
					double GCHANCE;
					double chancemult;
					double dropmult;
					List<DropData> items;
					boolean canIterate;
					if(g.notRate())
					{
						mod = Math.min(1.0, mod);
						GCHANCE = g.getChance() * mod;
						chancemult = mod;
						dropmult = 1.0;
						items = g.getDropItems(false);
						canIterate = false;
					}
					else if(g.isAdena())
					{
						if(mod < 10.0)
						{
							GCHANCE = g.getChance();
							chancemult = 1.0;
							dropmult = rateAdena;
						}
						else
						{
							chancemult = 1000000.0 / g.getChance();
							dropmult = rateAdena * g.getChance() / 1000000.0;
							GCHANCE = 1000000.0;
						}
						items = g.getDropItems(false);
						canIterate = false;
					}
					else if(template.isRaid || monster != null && monster.getChampion() > 0 || g.fixedQty() || g.notRate())
					{
						GCHANCE = g.getChance() * rateDrop;
						final Map.Entry<Double, Integer> balanced = DropGroup.balanceChanceAndMult(GCHANCE);
						chancemult = balanced.getKey() / g.getChance();
						GCHANCE = balanced.getKey();
						dropmult = balanced.getValue();
						items = g.getDropItems(false);
						canIterate = true;
					}
					else
					{
						if(rateDrop > Config.RATE_BREAKPOINT)
						{
							canIterate = true;
							dropmult = Math.min(Math.ceil(rateDrop / Config.RATE_BREAKPOINT), Config.MAX_DROP_ITERATIONS);
							rateDrop /= dropmult;
						}
						else
						{
							dropmult = 1.0;
							canIterate = false;
						}
						items = g.getRatedItems(rateDrop);
						chancemult = 1.0;
						GCHANCE = 0.0;
						for(final DropData i : items)
							GCHANCE += i.getChance();
					}
					tmp.append("</table><br><center>" + (ru ? "\u0428\u0430\u043d\u0441 \u0433\u0440\u0443\u043f\u043f\u044b" : "Group chance") + ": ").append(DropList.df.format(GCHANCE / 1000000.0));
					if(dropmult > 1.0 && canIterate)
					{
						tmp.append(" x").append((int) dropmult);
						dropmult = 1.0;
					}
					tmp.append("</center><table width=100%>");
					for(final DropData d : items)
					{
						final String chance = DropList.df.format(d.getChance() * chancemult / 1000000.0);
						if(icons)
						{
							tmp.append("<tr><td width=32><img src=").append(d.getItem().getIcon()).append(" width=32 height=32></td><td width=200>").append(compact(d.getName())).append("<br1>[");
							tmp.append(Math.round(d.getMinDrop() * dropmult)).append("-").append(Math.round(d.getMaxDrop() * dropmult)).append("]");
							tmp.append(chance).append("</td></tr>");
						}
						else
						{
							tmp.append("<tr><td width=80%>").append(compact(d.getName())).append("</td><td width=10%>");
							tmp.append(Math.min(Math.round((d.getMinDrop() + d.getMaxDrop()) * dropmult / 2.0), 9999999L)).append("</td><td width=10%>");
							tmp.append(chance).append("</td></tr>");
						}
					}
				}
			if(template.getDropData().getSpoil() != null && template.getDropData().getSpoil().size() > 0 && rateSpoil > 0.0)
			{
				overlevel = false;
				tmp.append("</table><br><center>" + (ru ? "\u0421\u043f\u043e\u0439\u043b" : "Spoil") + ":</center><table width=100%>");
				for(final DropGroup g : template.getDropData().getSpoil())
					for(final DropData d2 : g.getDropItems(false))
					{
						final Map.Entry<Double, Integer> e = DropGroup.balanceChanceAndMult(d2.getChance() * rateSpoil);
						final double GCHANCE2 = e.getKey() / 1000000.0;
						final int dropmult2 = e.getValue();
						if(icons)
						{
							tmp.append("<tr><td width=32><img src=").append(d2.getItem().getIcon()).append(" width=32 height=32></td><td width=200>").append(compact(d2.getName())).append("<br1>[");
							tmp.append(d2.getMinDrop() * dropmult2).append("-").append(d2.getMaxDrop() * dropmult2).append("]");
							tmp.append(DropList.df.format(GCHANCE2)).append("</td></tr>");
						}
						else
						{
							final float qty = (d2.getMinDrop() + d2.getMaxDrop()) * dropmult2 / 2.0f;
							tmp.append("<tr><td width=80%>").append(compact(d2.getName())).append("</td><td width=10%>");
							tmp.append(Math.round(qty)).append("</td><td width=10%>");
							tmp.append(DropList.df.format(GCHANCE2)).append("</td></tr>");
						}
					}
			}
		}
		else
			emptylist = true;
		tmp.append("</table>");
		if(emptylist)
			tmp.append("<center>Droplist " + (ru ? "\u043f\u0443\u0441\u0442" : "is empty") + "</center>");
		else if(overlevel)
			tmp.append("<center>" + (ru ? "\u042d\u0442\u043e\u0442 \u043c\u043e\u043d\u0441\u0442\u0440 \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u0441\u043b\u0430\u0431 \u0434\u043b\u044f \u0412\u0430\u0441" : "This monster is too weak for you") + "!</center>");
		tmp.append("</body></html>");
		return tmp.toString();
	}

	public static String compact(final String s)
	{
		return s.replaceFirst("Recipe:", "R:").replaceFirst("Scroll: Enchant", "Enchant").replaceFirst("Compressed Package", "CP");
	}
}
