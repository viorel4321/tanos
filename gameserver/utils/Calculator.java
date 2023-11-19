package l2s.gameserver.utils;

import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Calculator
{
	private static final ScriptEngine javaScriptEngine;
	private static final Pattern PATTERN;

	public static double eval(final String expr)
	{
		if(!isMatchingRexEg(expr))
			return Double.NaN;
		Object result = null;
		try
		{
			result = Calculator.javaScriptEngine.eval(expr);
		}
		catch(ScriptException ex)
		{}
		if(result instanceof Double)
			return (double) result;
		return Double.NaN;
	}

	private static boolean isMatchingRexEg(final String expression)
	{
		return Calculator.PATTERN.matcher(expression).matches();
	}

	static
	{
		javaScriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		PATTERN = Pattern.compile("[0-9+-\\\\* \\\\\\\\()\\\\.]{1,}");
	}
}
