package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.TeleportLocation;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.ResidenceFunction;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.MerchantGuard;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public final class ResidenceParser extends AbstractParser<ResidenceHolder>
{
	private static ResidenceParser _instance;

	public static ResidenceParser getInstance()
	{
		return ResidenceParser._instance;
	}

	private ResidenceParser()
	{
		super(ResidenceHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/residences/");
	}

	@Override
	public boolean isIgnored(final File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "residence.dtd";
	}

	@Override
	protected void readData(final Element rootElement) throws Exception
	{
		final String impl = rootElement.attributeValue("impl");
		Class<?> clazz = null;
		final StatsSet set = new StatsSet();
		final Iterator<Attribute> iterator = rootElement.attributeIterator();
		while(iterator.hasNext())
		{
			final Attribute element = iterator.next();
			set.set(element.getName(), element.getValue());
		}
		Residence residence = null;
		try
		{
			clazz = Class.forName("l2s.gameserver.model.entity.residence." + impl);
			final Constructor<?> constructor = clazz.getConstructor(StatsSet.class);
			residence = (Residence) constructor.newInstance(set);
			getHolder().addResidence(residence);
		}
		catch(Exception e)
		{
			this.error("fail to init: " + getCurrentFileName(), e);
			return;
		}
		final Iterator<Element> iterator2 = rootElement.elementIterator();
		while(iterator2.hasNext())
		{
			final Element element2 = iterator2.next();
			final String nodeName = element2.getName();
			final int level = element2.attributeValue("level") == null ? 0 : Integer.valueOf(element2.attributeValue("level"));
			final int lease = (int) ((element2.attributeValue("lease") == null ? 0 : Integer.valueOf(element2.attributeValue("lease"))) * Config.RESIDENCE_LEASE_FUNC_MULTIPLIER);
			final int npcId = element2.attributeValue("npcId") == null ? 0 : Integer.valueOf(element2.attributeValue("npcId"));
			final int listId = element2.attributeValue("listId") == null ? 0 : Integer.valueOf(element2.attributeValue("listId"));
			ResidenceFunction function = null;
			if(nodeName.equalsIgnoreCase("teleport"))
			{
				function = checkAndGetFunction(residence, 1);
				final List<TeleportLocation> targets = new ArrayList<TeleportLocation>();
				final Iterator<Element> it2 = element2.elementIterator();
				while(it2.hasNext())
				{
					final Element teleportElement = it2.next();
					if("target".equalsIgnoreCase(teleportElement.getName()))
					{
						final String locName = teleportElement.attributeValue("name");
						final int price = Integer.parseInt(teleportElement.attributeValue("price"));
						final int itemId = teleportElement.attributeValue("item") == null ? 57 : Integer.parseInt(teleportElement.attributeValue("item"));
						final TeleportLocation loc = new TeleportLocation(itemId, price, locName, 0);
						loc.set(Location.parseLoc(teleportElement.attributeValue("loc")));
						targets.add(loc);
					}
				}
				function.addTeleports(level, targets.toArray(new TeleportLocation[targets.size()]));
			}
			else if(nodeName.equalsIgnoreCase("support"))
			{
				if(level > 9 && !Config.ALT_CH_ALLOW_1H_BUFFS)
					continue;
				function = checkAndGetFunction(residence, 6);
				function.addBuffs(level);
			}
			else if(nodeName.equalsIgnoreCase("item_create"))
			{
				function = checkAndGetFunction(residence, 2);
				function.addBuylist(level, new int[] { npcId, listId });
			}
			else if(nodeName.equalsIgnoreCase("curtain"))
				function = checkAndGetFunction(residence, 7);
			else if(nodeName.equalsIgnoreCase("platform"))
				function = checkAndGetFunction(residence, 8);
			else if(nodeName.equalsIgnoreCase("restore_exp"))
				function = checkAndGetFunction(residence, 5);
			else if(nodeName.equalsIgnoreCase("restore_hp"))
				function = checkAndGetFunction(residence, 3);
			else if(nodeName.equalsIgnoreCase("restore_mp"))
				function = checkAndGetFunction(residence, 4);
			else if(nodeName.equalsIgnoreCase("skills"))
			{
				final Iterator<Element> nextIterator = element2.elementIterator();
				while(nextIterator.hasNext())
				{
					final Element nextElement = nextIterator.next();
					final int id2 = Integer.parseInt(nextElement.attributeValue("id"));
					final int level2 = Integer.parseInt(nextElement.attributeValue("level"));
					final Skill skill = SkillTable.getInstance().getInfo(id2, level2);
					if(skill != null)
						residence.addSkill(skill);
				}
			}
			else if(nodeName.equalsIgnoreCase("banish_points"))
			{
				final Iterator<Element> banishPointsIterator = element2.elementIterator();
				while(banishPointsIterator.hasNext())
				{
					final Location loc2 = Location.parse(banishPointsIterator.next());
					residence.addBanishPoint(loc2);
				}
			}
			else if(nodeName.equalsIgnoreCase("owner_restart_points"))
			{
				final Iterator<Element> ownerRestartPointsIterator = element2.elementIterator();
				while(ownerRestartPointsIterator.hasNext())
				{
					final Location loc2 = Location.parse(ownerRestartPointsIterator.next());
					residence.addOwnerRestartPoint(loc2);
				}
			}
			else if(nodeName.equalsIgnoreCase("other_restart_points"))
			{
				final Iterator<Element> otherRestartPointsIterator = element2.elementIterator();
				while(otherRestartPointsIterator.hasNext())
				{
					final Location loc2 = Location.parse(otherRestartPointsIterator.next());
					residence.addOtherRestartPoint(loc2);
				}
			}
			else if(nodeName.equalsIgnoreCase("chaos_restart_points"))
			{
				final Iterator<Element> chaosRestartPointsIterator = element2.elementIterator();
				while(chaosRestartPointsIterator.hasNext())
				{
					final Location loc2 = Location.parse(chaosRestartPointsIterator.next());
					residence.addChaosRestartPoint(loc2);
				}
			}
			else if(nodeName.equalsIgnoreCase("merchant_guards"))
			{
				final Iterator<Element> subElementIterator = element2.elementIterator();
				while(subElementIterator.hasNext())
				{
					final Element subElement = subElementIterator.next();
					final int itemId2 = Integer.parseInt(subElement.attributeValue("item_id"));
					final int npcId2 = Integer.parseInt(subElement.attributeValue("npc_id"));
					final int maxGuard = Integer.parseInt(subElement.attributeValue("max"));
					final IntSet intSet = new HashIntSet(3);
					final String[] split;
					final String[] ssq = split = subElement.attributeValue("ssq").split(";");
					for(final String q : split)
						if(q.equalsIgnoreCase("cabal_null"))
							intSet.add(0);
						else if(q.equalsIgnoreCase("cabal_dusk"))
							intSet.add(1);
						else if(q.equalsIgnoreCase("cabal_dawn"))
							intSet.add(2);
						else
							this.error("Unknown ssq type: " + q + "; file: " + getCurrentFileName());
					((Castle) residence).addMerchantGuard(new MerchantGuard(itemId2, npcId2, maxGuard, intSet));
				}
			}
			else if(nodeName.equalsIgnoreCase("reputation"))
			{
				residence.setReputation(Integer.valueOf(element2.attributeValue("score")));
				residence.setReputationOwner(Integer.valueOf(element2.attributeValue("scoreOwner")));
				residence.setReputationLoser(Integer.valueOf(element2.attributeValue("scoreLoser")));
			}
			if(function != null)
				function.addLease(level, lease);
		}
	}

	private ResidenceFunction checkAndGetFunction(final Residence residence, final int type)
	{
		ResidenceFunction function = residence.getFunction(type);
		if(function == null)
		{
			function = new ResidenceFunction(residence.getId(), type);
			residence.addFunction(function);
		}
		return function;
	}

	static
	{
		ResidenceParser._instance = new ResidenceParser();
	}
}
