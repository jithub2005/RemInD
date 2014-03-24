package at.jit.remind.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.mozilla.universalchardet.UniversalDetector;

/*
 * Detects character set of read file. If character set is not detected, fallback is set to UTF-8
 */
public class CharsetDetector
{
	private static final String FallbackEncoding = "UTF-8";
	
	private UniversalDetector universalDetector;
	private Logger logger = Logger.getLogger("CharsetDetectorLogger");
	private ConsoleHandler handler = new ConsoleHandler();
	
	public CharsetDetector()
	{
		logger.setLevel(Level.ALL);
		handler.setLevel(Level.ALL);
		handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
	}

	public String detectCharset(File file) throws IOException
	{
		byte[] buf = new byte[4096];

		FileInputStream fis = new FileInputStream(file);
		universalDetector = new UniversalDetector(null);

		int nread;
		try 
		{
			while ((nread = fis.read(buf)) > 0 && !universalDetector.isDone())
			{
				universalDetector.handleData(buf, 0, nread);
			}
		}
		finally
		{
			fis.close();
			universalDetector.dataEnd();
		}

		String encoding = universalDetector.getDetectedCharset();
		
		if (encoding == null)
		{
			logger.warning(String.format("Set default encoding %s", FallbackEncoding));
			
			encoding = FallbackEncoding;
		}

		universalDetector.reset();
		
		return encoding;
	}
}
