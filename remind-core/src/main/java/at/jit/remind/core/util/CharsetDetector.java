package at.jit.remind.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.mozilla.universalchardet.UniversalDetector;

/*
 * Detects character set of read file. If character set is not detected, fallback is set to UTF-8
 */
public class CharsetDetector
{
	private static final int byteBufferSize = 4096;
    private static final String fallbackEncoding = "UTF-8";

	public String detectCharset(File file) throws IOException
	{
		byte[] buf = new byte[byteBufferSize];

		FileInputStream fis = new FileInputStream(file);
		UniversalDetector universalDetector = new UniversalDetector(null);

		int nread;
		try 
		{
			while ((nread = fis.read(buf)) > 0 && !universalDetector.isDone())  //NOSONAR
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
			encoding = fallbackEncoding;
		}

		universalDetector.reset();
		
		return encoding;
	}
}
