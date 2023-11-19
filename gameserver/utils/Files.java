package l2s.gameserver.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class Files
{
	private static final Logger _log;

	public static void writeFile(final String path, final String string)
	{
		try
		{
			FileUtils.writeStringToFile(new File(path), string, "UTF-8");
		}
		catch(IOException e)
		{
			Files._log.error("Error while saving file : " + path, e);
		}
	}

	public static boolean copyFile(final String srcFile, final String destFile)
	{
		try
		{
			FileUtils.copyFile(new File(srcFile), new File(destFile), false);
			return true;
		}
		catch(IOException e)
		{
			Files._log.error("Error while copying file : " + srcFile + " to " + destFile, e);
			return false;
		}
	}

	static
	{
		_log = Logger.getLogger(Files.class);
	}
}
