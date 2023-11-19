package l2s.gameserver.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.compiler.Compiler;
import l2s.commons.compiler.MemoryClassLoader;
import l2s.gameserver.Config;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.quest.Quest;

public class Scripts
{
	private static final Logger _log = LoggerFactory.getLogger(Scripts.class);

	private static final Scripts _instance = new Scripts();

	public static final Scripts getInstance()
	{
		return _instance;
	}

	public static final Map<Integer, List<ScriptClassAndMethod>> dialogAppends = new HashMap<Integer, List<ScriptClassAndMethod>>();
	public static final Map<String, ScriptClassAndMethod> onAction = new HashMap<String, ScriptClassAndMethod>();
	public static final Map<String, ScriptClassAndMethod> onActionShift = new HashMap<String, ScriptClassAndMethod>();
	public static final List<ScriptClassAndMethod> onPlayerExit = new ArrayList<ScriptClassAndMethod>();
	public static final List<ScriptClassAndMethod> onPlayerEnter = new ArrayList<ScriptClassAndMethod>();
	public static final List<ScriptClassAndMethod> onReloadMultiSell = new ArrayList<ScriptClassAndMethod>();
	public static final List<ScriptClassAndMethod> onDie = new ArrayList<ScriptClassAndMethod>();
	public static final List<ScriptClassAndMethod> onEscape = new ArrayList<ScriptClassAndMethod>();

	private final Compiler compiler = new Compiler();
	private final Map<String, Class<?>> _classes = new TreeMap<String, Class<?>>();

	private Scripts()
	{
		load();
	}

	private void load()
	{
		_log.info("Scripts: Loading...");

		List<Class<?>> classes = new ArrayList<Class<?>>();

		boolean result = false;

		File f = new File("scripts.jar");
		if(f.exists())
		{
			JarInputStream stream = null;
			try
			{
				stream = new JarInputStream(new FileInputStream(f));
				JarEntry entry = null;
				while((entry = stream.getNextJarEntry()) != null)
				{
					//Вложенные класс
					if(entry.getName().contains(ClassUtils.INNER_CLASS_SEPARATOR) || !entry.getName().endsWith(".class"))
						continue;

					String name = entry.getName().replace(".class", "").replace("/", ".");

						Class<?> clazz = Class.forName(name);
						if(Modifier.isAbstract(clazz.getModifiers()))
							continue;
						classes.add(clazz);
				}
				result = true;
			}
			catch (Exception e)
			{
				_log.error("Fail to load scripts.jar!", e);
				classes.clear();
			}
			finally
			{
				IOUtils.closeQuietly(stream);
			}
		}

		if(!result || Config.JAVA_SCRIPTS)
			result = load(classes, "");

		if(!result)
		{
			_log.error("Scripts: Failed loading scripts!");
			Runtime.getRuntime().exit(0);
			return;
		}

		_log.info("Scripts: Loaded " + classes.size() + " classes.");

		Class<?> clazz;
		for(int i = 0; i < classes.size(); i++)
		{
			clazz = classes.get(i);
			_classes.put(clazz.getName(), clazz);
		}
	}

	public void init()
	{
		for(final Class<?> clazz : _classes.values())
		{
			addHandlers(clazz);
			if(Config.DONTLOADQUEST && ClassUtils.isAssignable(clazz, Quest.class))
				continue;
			if(!ClassUtils.isAssignable(clazz, ScriptFile.class))
				continue;
			try
			{
				((ScriptFile) clazz.newInstance()).onLoad();
			}
			catch(Exception e)
			{
				Scripts._log.error("Scripts: Failed running " + clazz.getName() + ".onLoad()", e);
			}
		}
	}

	public boolean reload()
	{
		Scripts._log.info("Scripts: Reloading...");
		return this.reload("");
	}

	public boolean reload(final String target)
	{
		final List<Class<?>> classes = new ArrayList<Class<?>>();
		if(this.load(classes, target))
		{
			Scripts._log.info("Scripts: Reloaded " + classes.size() + " classes.");
			for(int i = 0; i < classes.size(); ++i)
			{
				final Class<?> clazz = classes.get(i);
				final Class<?> prevClazz = _classes.put(clazz.getName(), clazz);
				if(prevClazz != null)
				{
					if(ClassUtils.isAssignable(prevClazz, ScriptFile.class))
						try
						{
							((ScriptFile) prevClazz.newInstance()).onReload();
						}
						catch(Exception e)
						{
							Scripts._log.error("Scripts: Failed running " + prevClazz.getName() + ".onReload()", e);
						}
					removeHandlers(prevClazz);
				}
				if(!Config.DONTLOADQUEST || !ClassUtils.isAssignable(clazz, Quest.class))
				{
					if(ClassUtils.isAssignable(clazz, ScriptFile.class))
						try
						{
							((ScriptFile) clazz.newInstance()).onLoad();
						}
						catch(Exception e)
						{
							Scripts._log.error("Scripts: Failed running " + clazz.getName() + ".onLoad()", e);
						}
					addHandlers(clazz);
				}
			}
			return true;
		}
		Scripts._log.error("Scripts: Failed reloading script(s): " + target + "!");
		return false;
	}

	public void shutdown()
	{
		for(final Class<?> clazz : _classes.values())
		{
			if(ClassUtils.isAssignable(clazz, Quest.class))
				continue;
			if(!ClassUtils.isAssignable(clazz, ScriptFile.class))
				continue;
			try
			{
				((ScriptFile) clazz.newInstance()).onShutdown();
			}
			catch(Exception e)
			{
				Scripts._log.error("Scripts: Failed running " + clazz.getName() + ".onShutdown()", e);
			}
		}
	}

	private boolean load(final List<Class<?>> classes, final String target)
	{
		Collection<File> scriptFiles = Collections.emptyList();
		File file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target.replace(".", "/") + ".java");
		if(file.isFile())
		{
			scriptFiles = new ArrayList<File>(1);
			scriptFiles.add(file);
		}
		else
		{
			file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target);
			if(file.isDirectory())
				scriptFiles = FileUtils.listFiles(file, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
		}
		if(scriptFiles.isEmpty())
			return true;
		boolean success;
		if(success = compiler.compile(scriptFiles))
		{
			final MemoryClassLoader classLoader = compiler.getClassLoader();
			for(final String name : classLoader.getLoadedClasses())
				if(!name.contains(ClassUtils.INNER_CLASS_SEPARATOR))
					try
					{
						final Class<?> clazz = classLoader.loadClass(name);
						if(!Modifier.isAbstract(clazz.getModifiers()))
							classes.add(clazz);
					}
					catch(ClassNotFoundException e)
					{
						success = false;
						Scripts._log.error("Scripts: Can't load script class: " + name, e);
					}
			classLoader.clear();
		}
		return success;
	}

	private void addHandlers(Class<?> clazz)
	{
		//TODO: Избавиться от этого и применить слушатели.
		try
		{
			for(Method method : clazz.getMethods())
			{
				if(method.getName().contains("DialogAppend_"))
				{
					Integer id = Integer.parseInt(method.getName().substring(13));
					List<ScriptClassAndMethod> handlers = Scripts.dialogAppends.get(id);
					if(handlers == null)
					{
						handlers = new ArrayList<ScriptClassAndMethod>();
						Scripts.dialogAppends.put(id, handlers);
					}
					handlers.add(new ScriptClassAndMethod(clazz.getName(), method.getName()));
				}
				else if(method.getName().contains("OnAction_"))
				{
					String name = method.getName().substring(9);
					Scripts.onAction.put(name, new ScriptClassAndMethod(clazz.getName(), method.getName()));
				}
				else if(method.getName().contains("OnActionShift_"))
				{
					String name = method.getName().substring(14);
					Scripts.onActionShift.put(name, new ScriptClassAndMethod(clazz.getName(), method.getName()));
				}
				else if(method.getName().equals("OnPlayerExit"))
					Scripts.onPlayerExit.add(new ScriptClassAndMethod(clazz.getName(), method.getName()));
				else if(method.getName().equals("OnPlayerEnter"))
					Scripts.onPlayerEnter.add(new ScriptClassAndMethod(clazz.getName(), method.getName()));
				else if(method.getName().equals("OnReloadMultiSell"))
					Scripts.onReloadMultiSell.add(new ScriptClassAndMethod(clazz.getName(), method.getName()));
				else if(method.getName().equals("OnDie"))
					Scripts.onDie.add(new ScriptClassAndMethod(clazz.getName(), method.getName()));
				else if(method.getName().equals("OnEscape"))
					Scripts.onEscape.add(new ScriptClassAndMethod(clazz.getName(), method.getName()));
			}
		}
		catch(Exception e)
		{
			Scripts._log.error("", e);
		}
	}

	private void removeHandlers(Class<?> script)
	{
		try
		{
			for(List<ScriptClassAndMethod> entry : Scripts.dialogAppends.values())
			{
				List<ScriptClassAndMethod> toRemove = new ArrayList<ScriptClassAndMethod>();
				for(ScriptClassAndMethod sc : entry)
				{
					if(sc.className.equals(script.getName()))
						toRemove.add(sc);
				}
				for(ScriptClassAndMethod sc : toRemove)
					entry.remove(sc);
			}

			List<String> toRemove2 = new ArrayList<String>();
			for(Map.Entry<String, ScriptClassAndMethod> entry2 : Scripts.onAction.entrySet())
			{
				if(entry2.getValue().className.equals(script.getName()))
					toRemove2.add(entry2.getKey());
			}

			for(final String key : toRemove2)
				Scripts.onAction.remove(key);

			toRemove2 = new ArrayList<String>();
			for(Map.Entry<String, ScriptClassAndMethod> entry2 : Scripts.onActionShift.entrySet())
			{
				if(entry2.getValue().className.equals(script.getName()))
					toRemove2.add(entry2.getKey());
			}

			for(String key : toRemove2)
				Scripts.onActionShift.remove(key);

			List<ScriptClassAndMethod> toRemove3 = new ArrayList<ScriptClassAndMethod>();
			for(ScriptClassAndMethod sc2 : Scripts.onPlayerExit)
			{
				if(sc2.className.equals(script.getName()))
					toRemove3.add(sc2);
			}

			for(ScriptClassAndMethod sc2 : toRemove3)
				Scripts.onPlayerExit.remove(sc2);

			toRemove3 = new ArrayList<ScriptClassAndMethod>();
			for(ScriptClassAndMethod sc2 : Scripts.onPlayerEnter)
			{
				if(sc2.className.equals(script.getName()))
					toRemove3.add(sc2);
			}

			for(ScriptClassAndMethod sc2 : toRemove3)
				Scripts.onPlayerEnter.remove(sc2);

			toRemove3 = new ArrayList<ScriptClassAndMethod>();
			for(ScriptClassAndMethod sc2 : Scripts.onReloadMultiSell)
			{
				if(sc2.className.equals(script.getName()))
					toRemove3.add(sc2);
			}

			for(ScriptClassAndMethod sc2 : toRemove3)
				Scripts.onReloadMultiSell.remove(sc2);

			toRemove3 = new ArrayList<ScriptClassAndMethod>();
			for(ScriptClassAndMethod sc2 : Scripts.onDie)
			{
				if(sc2.className.equals(script.getName()))
					toRemove3.add(sc2);
			}

			for(ScriptClassAndMethod sc2 : toRemove3)
				Scripts.onDie.remove(sc2);

			toRemove3 = new ArrayList<ScriptClassAndMethod>();
			for(ScriptClassAndMethod sc2 : Scripts.onEscape)
			{
				if(sc2.className.equals(script.getName()))
					toRemove3.add(sc2);
			}

			for(ScriptClassAndMethod sc2 : toRemove3)
				Scripts.onEscape.remove(sc2);
		}
		catch(Exception e)
		{
			Scripts._log.error("", e);
		}
	}

	public Object callScripts(final String className, final String methodName)
	{
		return this.callScripts(null, className, methodName, null, null);
	}

	public Object callScripts(final String className, final String methodName, final Object[] args)
	{
		return this.callScripts(null, className, methodName, args, null);
	}

	public Object callScripts(final String className, final String methodName, final Map<String, Object> variables)
	{
		return this.callScripts(null, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, variables);
	}

	public Object callScripts(final String className, final String methodName, final Object[] args, final Map<String, Object> variables)
	{
		return this.callScripts(null, className, methodName, args, variables);
	}

	public Object callScripts(final GameObject caller, final String className, final String methodName)
	{
		return this.callScripts(caller, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
	}

	public Object callScripts(final GameObject caller, final String className, final String methodName, final Object[] args)
	{
		return this.callScripts(caller, className, methodName, args, null);
	}

	public Object callScripts(final GameObject caller, final String className, final String methodName, final Map<String, Object> variables)
	{
		return this.callScripts(caller, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, variables);
	}

	public Object callScripts(final GameObject caller, final String className, final String methodName, final Object[] args, final Map<String, Object> variables)
	{
		final Class<?> clazz = _classes.get(className);
		if(clazz == null)
		{
			Scripts._log.error("Script class " + className + " not found!");
			return null;
		}
		Object o;
		try
		{
			o = clazz.newInstance();
		}
		catch(Exception e)
		{
			Scripts._log.error("Scripts: Failed creating instance of " + clazz.getName(), e);
			return null;
		}
		if(variables != null && !variables.isEmpty())
			for(final Map.Entry<String, Object> param : variables.entrySet())
				try
				{
					FieldUtils.writeField(o, param.getKey(), param.getValue());
				}
				catch(Exception e2)
				{
					Scripts._log.error("Scripts: Failed setting fields for " + clazz.getName(), e2);
				}
		if(caller != null)
			try
			{
				Field field = null;
				if((field = FieldUtils.getField(clazz, "self")) != null)
					FieldUtils.writeField(field, o, caller.getRef());
			}
			catch(Exception e)
			{
				Scripts._log.error("Scripts: Failed setting field for " + clazz.getName(), e);
			}
		Object ret = null;
		try
		{
			final Class<?>[] parameterTypes = new Class<?>[args.length];
			for(int i = 0; i < args.length; ++i)
				parameterTypes[i] = args[i] != null ? args[i].getClass() : null;
			ret = MethodUtils.invokeMethod(o, methodName, args, parameterTypes);
		}
		catch(NoSuchMethodException ex)
		{}
		catch(InvocationTargetException ite)
		{
			Scripts._log.error("Scripts: Error while calling " + clazz.getName() + "." + methodName + "()", ite.getTargetException());
		}
		catch(Exception e3)
		{
			Scripts._log.error("Scripts: Failed calling " + clazz.getName() + "." + methodName + "()", e3);
		}
		return ret;
	}

	public Map<String, Class<?>> getClasses()
	{
		return _classes;
	}

	public static class ScriptClassAndMethod
	{
		public final String className;
		public final String methodName;

		public ScriptClassAndMethod(final String className, final String methodName)
		{
			this.className = className;
			this.methodName = methodName;
		}
	}
}
