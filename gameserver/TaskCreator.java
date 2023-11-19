package l2s.gameserver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TaskCreator
{
	public static Runnable createTask(final Object obj, final Method method, final Object... args)
	{
		return new Runnable(){
			@Override
			public void run()
			{
				try
				{
					method.invoke(obj, args);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
	}

	public static Runnable createTask(final Object obj, final Class<?> clazz, final String methodName, final Object... args) throws SecurityException, NoSuchMethodException
	{
		final Class<?>[] arg_classes = new Class<?>[args.length];
		for(int i = 0; i < args.length; ++i)
			arg_classes[i] = args[i].getClass();
		final Method method = getMethod(clazz, methodName, arg_classes);
		return createTask(obj, method, args);
	}

	public static Runnable createTask(final Object obj, final String className, final String methodName, final Object... args) throws SecurityException, NoSuchMethodException, ClassNotFoundException
	{
		return createTask(obj, Class.forName(className), methodName, args);
	}

	public static Runnable createTask(final Method method, final Object... args)
	{
		return createTask(null, method, args);
	}

	public static Runnable createTask(final Class<?> clazz, final String methodName, final Object... args) throws SecurityException, NoSuchMethodException
	{
		return createTask(null, clazz, methodName, args);
	}

	public static Runnable createTask(final String className, final String methodName, final Object... args) throws SecurityException, NoSuchMethodException, ClassNotFoundException
	{
		return createTask(null, className, methodName, args);
	}

	public static String argumentTypesToString(final Class<?>[] argTypes)
	{
		final StringBuilder buf = new StringBuilder();
		buf.append("(");
		if(argTypes != null)
			for(int i = 0; i < argTypes.length; ++i)
			{
				if(i > 0)
					buf.append(", ");
				final Class<?> c = argTypes[i];
				buf.append(c == null ? "null" : c.getName());
			}
		buf.append(")");
		return buf.toString();
	}

	private static Class<?> getPrimitiveClass(final Class<?> clazz)
	{
		try
		{
			final Field f = clazz.getField("TYPE");
			if(f.getType() == Class.class)
				return (Class<?>) f.get(null);
		}
		catch(Exception ex)
		{}
		return null;
	}

	private static boolean paramEq(final Class<?> a1, final Class<?> a2)
	{
		if(a1 == a2)
			return true;
		final Object _a1 = getPrimitiveClass(a1);
		if(_a1 != null && _a1 == a2)
			return true;
		final Object _a2 = getPrimitiveClass(a2);
		return _a2 != null && (a1 == _a2 || _a1 == _a2);
	}

	private static boolean paramsEq(final Class<?>[] a1, final Class<?>[] a2)
	{
		if(a1 == null)
			return a2 == null || a2.length == 0;
		if(a2 == null)
			return a1.length == 0;
		if(a1.length != a2.length)
			return false;
		for(int i = 0; i < a1.length; ++i)
			if(!paramEq(a1[i], a2[i]))
				return false;
		return true;
	}

	public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... paramTypes) throws NoSuchMethodException, SecurityException
	{
		final String internedName = name.intern();
		for(final Method method : clazz.getMethods())
			if(method.getName() == internedName && paramsEq(paramTypes, method.getParameterTypes()))
				return method;
		Method res = null;
		if(!clazz.isInterface())
		{
			final Class<?> c = clazz.getSuperclass();
			if(c != null && (res = getMethod(c, name, paramTypes)) != null)
				return res;
		}
		final Class<?>[] interfaces2;
		final Class<?>[] interfaces = interfaces2 = clazz.getInterfaces();
		for(final Class<?> c2 : interfaces2)
			if((res = getMethod(c2, name, paramTypes)) != null)
				return res;
		throw new NoSuchMethodException(clazz.getName() + "." + name + argumentTypesToString(paramTypes));
	}
}
