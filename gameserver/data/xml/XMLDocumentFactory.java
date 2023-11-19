package l2s.gameserver.data.xml;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public final class XMLDocumentFactory
{
	private final DocumentBuilder _builder;
	private final Transformer _transformer;

	public static final XMLDocumentFactory getInstance()
	{
		return SingletonHolder._instance;
	}

	protected XMLDocumentFactory() throws Exception
	{
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			_builder = factory.newDocumentBuilder();
			_transformer = TransformerFactory.newInstance().newTransformer();
		}
		catch(Exception e)
		{
			throw new Exception("Failed initializing", e);
		}
	}

	public final Document loadDocument(final String filePath) throws Exception
	{
		return this.loadDocument(new File(filePath));
	}

	public final Document loadDocument(final File file) throws Exception
	{
		if(!checkFile(file))
			throw new Exception("File: " + file.getAbsolutePath() + " doesn't exist and/or is not a file.");
		return _builder.parse(file);
	}

	public final void writeDocument(final String filePath, final Document doc) throws Exception
	{
		final File file = new File(filePath);
		_transformer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(file)));
	}

	public final Document newDocument()
	{
		return _builder.newDocument();
	}

	private static final boolean checkFile(final File file)
	{
		return file.exists() && file.isFile();
	}

	private static class SingletonHolder
	{
		protected static final XMLDocumentFactory _instance;

		static
		{
			try
			{
				_instance = new XMLDocumentFactory();
			}
			catch(Exception e)
			{
				throw new ExceptionInInitializerError(e);
			}
		}
	}
}
