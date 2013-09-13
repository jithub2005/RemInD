package at.jit.remind.core.model.content.scm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.MessageHandlerException;

public class SubversionSourceTest
{
	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();

	@BeforeClass
	public static void setUpBeforeClass() throws IOException
	{
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
	}

	@Before
	public void setUp() throws Exception
	{
		listBasedMessageHandler.getMessageList().clear();
	}

	@Test(expected = MessageHandlerException.class)
	public void canHandleInvalidSubversionSourceSettings() throws MessageHandlerException
	{
		SubversionSource subversionSource = new SubversionSource(null, null, null);
		subversionSource.validate();
	}

	@Test
	public void canRetrieveFileFromSVN() throws MessageHandlerException
	{
		SubversionSource subversionSource = new SubversionSource("http://dev.jit.at/svn/remind-testing", "-1", "/trunk/dbFiles/dummy.sql");
		File file;
		try
		{
			file = subversionSource.retrieve();
			assertTrue("File retrieved successfully", file != null && file.length() > 0);
		}
		catch (MessageHandlerException e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void canValidate() throws MessageHandlerException
	{
		SubversionSource subversionSource = new SubversionSource("http://dev.jit.at/svn/remind-testing", "-1", "/trunk/dbFiles/dummy.sql");
		try
		{
			subversionSource.retrieve();
			subversionSource.validate();

			assertTrue("Source successfully validated", true);
		}
		catch (MessageHandlerException e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void canHandleBackslashes() throws MessageHandlerException
	{
		SubversionSource subversionSource = new SubversionSource("http://dev.jit.at\\svn\\remind-testing", "-1", "\\trunk/dbFiles\\dummy.sql");

		try
		{
			subversionSource.validate();
			subversionSource.retrieve();

			assertTrue("Source successfully validated", true);
		}
		catch (MessageHandlerException e)
		{
			fail(e.getMessage());
		}
	}
}
