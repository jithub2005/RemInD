package at.jit.remind.core.model.content.io;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.MessageHandlerException;

public class FileSystemLocationTest
{
	private FileSystemLocation fileSystemLocation;
	private static String sourceFilePath;

	private final static String TargetFile = "FileSystemLocationTestTarget.txt";

	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();

	@BeforeClass
	public static void setUpBeforeClass() throws IOException
	{
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);

		String sourceContent = FileUtils.readFileToString(FileUtils.toFile(FileSystemLocationTest.class.getResource("/io/FileSystemLocationTestSource.txt")));

		File file = File.createTempFile(FileSystemLocationTest.class.getSimpleName(), ".tmp");
		file.deleteOnExit();

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(sourceContent);
		out.close();

		sourceFilePath = file.getAbsolutePath();

	}

	@After
	public void tearDown() throws IOException
	{
		File targetFile = new File(System.getProperty("java.io.tmpdir") + "/" + TargetFile);
		targetFile.createNewFile();
		targetFile.delete();
	}

	@Test(expected = MessageHandlerException.class)
	public void cannotRetrieveInvalidSourceFile() throws MessageHandlerException
	{
		fileSystemLocation = new FileSystemLocation("foobar.txt", true);
		fileSystemLocation.retrieve();
	}

	@Test
	public void canRetrieveSourceFile()
	{
		fileSystemLocation = new FileSystemLocation(sourceFilePath, true);
		try
		{
			File file = fileSystemLocation.retrieve();
			assertTrue(file.length() > 0);
		}
		catch (MessageHandlerException e)
		{
			fail("Retrieve method must not throw a MessageHandlerException.");
		}
	}

	@Test(expected = MessageHandlerException.class)
	public void cannotValidateInvalidFile() throws MessageHandlerException
	{
		fileSystemLocation = new FileSystemLocation("foobar.txt", true);
		fileSystemLocation.validate();
	}

	@Test
	public void canValidateSourceFile() throws MessageHandlerException
	{
		fileSystemLocation = new FileSystemLocation(sourceFilePath, true);
		fileSystemLocation.validate();
	}

	@Test
	public void canValidateTargetFile() throws MessageHandlerException
	{
		fileSystemLocation = new FileSystemLocation(System.getProperty("java.io.tmpdir") + "/" + TargetFile, false);
		fileSystemLocation.validate();
	}

	@Test(expected = MessageHandlerException.class)
	public void cannotValidateIncorrectTargetFile() throws MessageHandlerException
	{
		fileSystemLocation = new FileSystemLocation("/FOOBAR/" + TargetFile, false);
		fileSystemLocation.validate();
	}

	@Test
	public void canDeploy() throws MessageHandlerException
	{
		FileSystemLocation targetFileLocation = new FileSystemLocation(System.getProperty("java.io.tmpdir") + "/" + TargetFile, false);
		targetFileLocation.deploy(new File(sourceFilePath));

		assertTrue(targetFileLocation.retrieve().length() > 0);
	}

	@Test(expected = MessageHandlerException.class)
	public void cannotDeployInvalidSourceFile() throws MessageHandlerException
	{
		FileSystemLocation targetFileLocation = new FileSystemLocation(System.getProperty("java.io.tmpdir") + "/" + TargetFile, false);
		targetFileLocation.deploy(null);
	}
}
