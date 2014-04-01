package at.jit.remind.core.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import at.jit.remind.core.model.content.database.DatabaseTargetTest;

public class CharsetDetectorTest
{
	private CharsetDetector charsetDetector = new CharsetDetector();
	private static final String FallbackEncoding = "UTF-8";
	
	@Test
	public void charsetISO88591NoFallbackToUTF8() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/ISO8859-1.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertNotSame(FallbackEncoding, charset);
	}
	
	@Test
	public void charsetISO88596FallbackToUTF8() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/ISO8859-6.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame("Fallback UTF-8 must be set, because ISO8859-6 is not recognized due to few characters in file.", FallbackEncoding, charset);
	}
	
	@Test
	public void charsetUTF8() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/UTF8.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame("Charset UTF-8 must be recognized.", FallbackEncoding, charset);
	}
	
	@Test
	public void charsetWindows1255() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/Windows1255.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame("WINDOWS-1255 must be recognized.", "WINDOWS-1255", charset);
	}
	
	@Test
	public void charsetANSI() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/RemInD_create_table4.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame("WINDOWS-1252 must be recognized.", "WINDOWS-1252", charset);	
	}
}
