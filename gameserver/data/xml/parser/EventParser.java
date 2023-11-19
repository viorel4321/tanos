package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.events.actions.ActiveDeactiveAction;
import l2s.gameserver.model.entity.events.actions.AnnounceAction;
import l2s.gameserver.model.entity.events.actions.GiveItemAction;
import l2s.gameserver.model.entity.events.actions.IfElseAction;
import l2s.gameserver.model.entity.events.actions.InitAction;
import l2s.gameserver.model.entity.events.actions.OpenCloseAction;
import l2s.gameserver.model.entity.events.actions.PlaySoundAction;
import l2s.gameserver.model.entity.events.actions.RefreshAction;
import l2s.gameserver.model.entity.events.actions.SayAction;
import l2s.gameserver.model.entity.events.actions.SpawnDespawnAction;
import l2s.gameserver.model.entity.events.actions.StartStopAction;
import l2s.gameserver.model.entity.events.actions.TeleportPlayersAction;
import l2s.gameserver.model.entity.events.objects.BoatPoint;
import l2s.gameserver.model.entity.events.objects.CTBTeamObject;
import l2s.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.utils.Location;

public final class EventParser extends AbstractParser<EventHolder>
{
	private static final EventParser _instance;

	public static EventParser getInstance()
	{
		return EventParser._instance;
	}

	protected EventParser()
	{
		super(EventHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/events/");
	}

	@Override
	public boolean isIgnored(final File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "events.dtd";
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void readData(final Element rootElement) throws Exception
	{
		final Iterator<?> iterator = rootElement.elementIterator("event");
		while(iterator.hasNext())
		{
			final Element eventElement = (Element) iterator.next();
			final int id = Integer.parseInt(eventElement.attributeValue("id"));
			final String name = eventElement.attributeValue("name");
			final String impl = eventElement.attributeValue("impl");
			final EventType type = EventType.valueOf(eventElement.attributeValue("type"));
			Class<GlobalEvent> eventClass = null;
			try
			{
				eventClass = (Class<GlobalEvent>) Class.forName("l2s.gameserver.model.entity.events.impl." + impl + "Event");
			}
			catch(ClassNotFoundException e)
			{
				eventClass = (Class<GlobalEvent>) Scripts.getInstance().getClasses().get("events." + impl + "Event");
			}

			if(eventClass == null)
			{
				info("Not found impl class: " + impl + "; File: " + getCurrentFileName());
				continue;
			}

			final Constructor<GlobalEvent> constructor = eventClass.getConstructor(MultiValueSet.class);
			final MultiValueSet<String> set = new MultiValueSet<String>();
			set.set("id", id);
			set.set("name", name);
			final Iterator<?> parameterIterator = eventElement.elementIterator("parameter");
			while(parameterIterator.hasNext())
			{
				final Element parameterElement = (Element) parameterIterator.next();
				set.set(parameterElement.attributeValue("name"), parameterElement.attributeValue("value"));
			}
			final GlobalEvent event = constructor.newInstance(set);
			event.addOnStartActions(parseActions(eventElement.element("on_start"), Integer.MAX_VALUE));
			event.addOnStopActions(parseActions(eventElement.element("on_stop"), Integer.MAX_VALUE));
			event.addOnInitActions(parseActions(eventElement.element("on_init"), Integer.MAX_VALUE));
			final Element onTime = eventElement.element("on_time");
			if(onTime != null)
			{
				final Iterator<?> onTimeIterator = onTime.elementIterator("on");
				while(onTimeIterator.hasNext())
				{
					final Element on = (Element) onTimeIterator.next();
					final int time = Integer.parseInt(on.attributeValue("time"));
					final List<EventAction> actions = parseActions(on, time);
					event.addOnTimeActions(time, actions);
				}
			}
			final Iterator<?> objectIterator = eventElement.elementIterator("objects");
			while(objectIterator.hasNext())
			{
				final Element objectElement = (Element) objectIterator.next();
				final String objectsName = objectElement.attributeValue("name");
				final List<Serializable> objects = parseObjects(objectElement);
				event.addObjects(objectsName, objects);
			}
			getHolder().addEvent(type, event);
		}
	}

	private List<Serializable> parseObjects(final Element element)
	{
		if(element == null)
			return Collections.emptyList();
		final List<Serializable> objects = new ArrayList<Serializable>(2);
		final Iterator<?> objectsIterator = element.elementIterator();
		while(objectsIterator.hasNext())
		{
			final Element objectsElement = (Element) objectsIterator.next();
			final String nodeName = objectsElement.getName();
			if(nodeName.equalsIgnoreCase("boat_point"))
				objects.add(BoatPoint.parse(objectsElement));
			else if(nodeName.equalsIgnoreCase("point"))
				objects.add(Location.parse(objectsElement));
			else if(nodeName.equalsIgnoreCase("spawn_ex"))
				objects.add(new SpawnExObject(objectsElement.attributeValue("name")));
			else if(nodeName.equalsIgnoreCase("door"))
				objects.add(new DoorObject(Integer.parseInt(objectsElement.attributeValue("id"))));
			else if(nodeName.equalsIgnoreCase("siege_toggle_npc"))
			{
				final int id = Integer.parseInt(objectsElement.attributeValue("id"));
				final int fakeId = Integer.parseInt(objectsElement.attributeValue("fake_id"));
				final int x = Integer.parseInt(objectsElement.attributeValue("x"));
				final int y = Integer.parseInt(objectsElement.attributeValue("y"));
				final int z = Integer.parseInt(objectsElement.attributeValue("z"));
				final int hp = Integer.parseInt(objectsElement.attributeValue("hp"));
				Set<String> set = Collections.emptySet();
				final Iterator<?> oIterator = objectsElement.elementIterator();
				while(oIterator.hasNext())
				{
					final Element sub = (Element) oIterator.next();
					if(set.isEmpty())
						set = new HashSet<String>();
					set.add(sub.attributeValue("name"));
				}
				objects.add(new SiegeToggleNpcObject(id, fakeId, new Location(x, y, z), hp, set));
			}
			else if(nodeName.equalsIgnoreCase("castle_zone"))
			{
				final long price = Long.parseLong(objectsElement.attributeValue("price"));
				objects.add(new CastleDamageZoneObject(objectsElement.attributeValue("name"), price));
			}
			else if(nodeName.equalsIgnoreCase("zone"))
				objects.add(new ZoneObject(objectsElement.attributeValue("name")));
			else
			{
				if(!nodeName.equalsIgnoreCase("ctb_team"))
					continue;
				final int mobId = Integer.parseInt(objectsElement.attributeValue("mob_id"));
				final int flagId = Integer.parseInt(objectsElement.attributeValue("id"));
				final Location loc = Location.parse(objectsElement);
				objects.add(new CTBTeamObject(mobId, flagId, loc));
			}
		}
		return objects;
	}

	private List<EventAction> parseActions(final Element element, final int time)
	{
		if(element == null)
			return Collections.emptyList();
		IfElseAction lastIf = null;
		final List<EventAction> actions = new ArrayList<EventAction>(0);
		final Iterator<?> iterator = element.elementIterator();
		while(iterator.hasNext())
		{
			final Element actionElement = (Element) iterator.next();
			if(actionElement.getName().equalsIgnoreCase("start"))
			{
				final String name = actionElement.attributeValue("name");
				final StartStopAction startStopAction = new StartStopAction(name, true);
				actions.add(startStopAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("stop"))
			{
				final String name = actionElement.attributeValue("name");
				final StartStopAction startStopAction = new StartStopAction(name, false);
				actions.add(startStopAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("spawn"))
			{
				final String name = actionElement.attributeValue("name");
				final SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, true);
				actions.add(spawnDespawnAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("despawn"))
			{
				final String name = actionElement.attributeValue("name");
				final SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, false);
				actions.add(spawnDespawnAction);
			}
			else if(actionElement.getName().equalsIgnoreCase("open"))
			{
				final String name = actionElement.attributeValue("name");
				final OpenCloseAction a = new OpenCloseAction(true, name);
				actions.add(a);
			}
			else if(actionElement.getName().equalsIgnoreCase("close"))
			{
				final String name = actionElement.attributeValue("name");
				final OpenCloseAction a = new OpenCloseAction(false, name);
				actions.add(a);
			}
			else if(actionElement.getName().equalsIgnoreCase("active"))
			{
				final String name = actionElement.attributeValue("name");
				final ActiveDeactiveAction a2 = new ActiveDeactiveAction(true, name);
				actions.add(a2);
			}
			else if(actionElement.getName().equalsIgnoreCase("deactive"))
			{
				final String name = actionElement.attributeValue("name");
				final ActiveDeactiveAction a2 = new ActiveDeactiveAction(false, name);
				actions.add(a2);
			}
			else if(actionElement.getName().equalsIgnoreCase("refresh"))
			{
				final String name = actionElement.attributeValue("name");
				final RefreshAction a3 = new RefreshAction(name);
				actions.add(a3);
			}
			else if(actionElement.getName().equalsIgnoreCase("init"))
			{
				final String name = actionElement.attributeValue("name");
				final InitAction a4 = new InitAction(name);
				actions.add(a4);
			}
			else if(actionElement.getName().equalsIgnoreCase("play_sound"))
			{
				final int range = Integer.parseInt(actionElement.attributeValue("range"));
				final String sound = actionElement.attributeValue("sound");
				final PlaySound.Type type = PlaySound.Type.valueOf(actionElement.attributeValue("type"));
				final PlaySoundAction action = new PlaySoundAction(range, sound, type);
				actions.add(action);
			}
			else if(actionElement.getName().equalsIgnoreCase("give_item"))
			{
				final int itemId = Integer.parseInt(actionElement.attributeValue("id"));
				final long count = Integer.parseInt(actionElement.attributeValue("count"));
				final GiveItemAction action2 = new GiveItemAction(itemId, count);
				actions.add(action2);
			}
			else if(actionElement.getName().equalsIgnoreCase("announce"))
			{
				final String val = actionElement.attributeValue("val");
				if(val == null && time == Integer.MAX_VALUE)
					this.info("Can't get announce time." + getCurrentFileName());
				else
				{
					final int val2 = val == null ? time : Integer.parseInt(val);
					final EventAction action3 = new AnnounceAction(val2);
					actions.add(action3);
				}
			}
			else if(actionElement.getName().equalsIgnoreCase("if"))
			{
				final String name = actionElement.attributeValue("name");
				final IfElseAction action4 = new IfElseAction(name, false);
				action4.setIfList(parseActions(actionElement, time));
				actions.add(action4);
				lastIf = action4;
			}
			else if(actionElement.getName().equalsIgnoreCase("ifnot"))
			{
				final String name = actionElement.attributeValue("name");
				final IfElseAction action4 = new IfElseAction(name, true);
				action4.setIfList(parseActions(actionElement, time));
				actions.add(action4);
				lastIf = action4;
			}
			else if(actionElement.getName().equalsIgnoreCase("else"))
			{
				if(lastIf == null)
					this.info("Not find <if> for <else> tag");
				else
					lastIf.setElseList(parseActions(actionElement, time));
			}
			else if(actionElement.getName().equalsIgnoreCase("say"))
			{
				final int chat = Integer.parseInt(actionElement.attributeValue("chat"));
				final int range2 = Integer.parseInt(actionElement.attributeValue("range"));
				final int how = Integer.parseInt(actionElement.attributeValue("how"));
				final int msg = Integer.parseInt(actionElement.attributeValue("msg"));
				actions.add(new SayAction(range2, chat, how, msg));
			}
			else
			{
				if(!actionElement.getName().equalsIgnoreCase("teleport_players"))
					continue;
				final String name = actionElement.attributeValue("id");
				final TeleportPlayersAction a5 = new TeleportPlayersAction(name);
				actions.add(a5);
			}
		}
		return actions.isEmpty() ? Collections.emptyList() : actions;
	}

	static
	{
		_instance = new EventParser();
	}
}
