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
	private static final String FallBackEncoding = "ISO-8859-1";
	private static final String DefaultEncoding = "UTF-8";
	
	@Test
	public void charsetISO88591FallbackToISO88591() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/ISO8859-1.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame(String.format("Charset %s expected but was %s", FallBackEncoding, charset), FallBackEncoding, charset);
	}
	
	@Test
	public void charsetISO88596FallbackToISO88591() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/ISO8859-6.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame(String.format("Charset %s expected but was %s", FallBackEncoding, charset), FallBackEncoding, charset);
	}
	
	@Test
	public void detectCharsetUTF8DefaultUTF8() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/UTF8.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame(String.format("Charset %s expected but was %s", DefaultEncoding, charset), DefaultEncoding, charset);
	}
	
	@Test
	public void charsetWindows1252FallbackToISO88591() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/Windows1252.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame(String.format("Charset %s expected but was %s", FallBackEncoding, charset), FallBackEncoding, charset);
	}
	
	@Test
	public void charsetKOIFallbackToISO88591() throws IOException
	{
		File file = FileUtils.toFile(DatabaseTargetTest.class.getResource("/charset/KOI8R.sql"));
		
		String charset = charsetDetector.detectCharset(file);
		
		assertSame(String.format("Charset %s expected but was %s", FallBackEncoding, charset), FallBackEncoding, charset);
	}
}
