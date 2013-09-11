package at.jit.remind.core.model.content.scm;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.RemindModelFeedback;

public class SubversionSourceMessageHandlerTest
{
	private static final String repositoryUrl = "http://dev.jit.at/svn/remind-testing";
	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();
	private static SubversionSource subversionSource1;
	private static SubversionSource subversionSource2;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());

		subversionSource1 = new SubversionSource(repositoryUrl, "1000", "trunk/dbFiles/dummy.sql");
		subversionSource2 = new SubversionSource(repositoryUrl, "-1", "trunk/dbFiles/dummy2.sql");
	}

	@Before
	public void setUp() throws Exception
	{
		listBasedMessageHandler.getMessageList().clear();
	}

	@Test
	public void retrieveWithAbort()
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);
		// targetfile1 is needed for throwing the exception.
		// If the ABORT mechanism works properly, file 2 must be null.
		@SuppressWarnings("unused")
		File targetfile1 = null;
		File targetfile2 = null;
		try
		{
			targetfile1 = subversionSource1.retrieve();
			targetfile2 = subversionSource2.retrieve();
		}
		catch (MessageHandlerException e)
		{
			if (!RemindModelFeedback.Abort.equals(e.getFeedback()))
			{
				try
				{
					targetfile2 = subversionSource2.retrieve();
				}
				catch (MessageHandlerException e1)
				{
					assertTrue(false);
				}
			}
		}
		assertTrue(targetfile2 == null);
	}

	@Test
	public void retrieveWithSkip()
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Skip);
		// targetfile1 is needed for throwing the exception.
		// If the SKIP mechanism works properly, file 2 must NOT be null.
		@SuppressWarnings("unused")
		File targetfile1 = null;
		File targetfile2 = null;
		try
		{
			targetfile1 = subversionSource1.retrieve();
		}
		catch (MessageHandlerException e)
		{
			if (RemindModelFeedback.Skip.equals(e.getFeedback()))
			{
				try
				{
					targetfile2 = subversionSource2.retrieve();
				}
				catch (MessageHandlerException e1)
				{
					assertTrue(false);
				}
			}
		}

		assertTrue(targetfile2 != null);
	}

	@Test
	public void validateWithSuccess() throws MessageHandlerException
	{
		subversionSource2.validate();
	}

	@Test(expected = MessageHandlerException.class)
	public void validateWithWrongRevision() throws MessageHandlerException
	{
		subversionSource1.validate();
	}

	@Test(expected = MessageHandlerException.class)
	public void validateWithWrongPath() throws MessageHandlerException
	{
		SubversionSource s = new SubversionSource(repositoryUrl, "-1", null);
		s.validate();
	}

	@Test(expected = MessageHandlerException.class)
	public void validateWithWrongRepositoryUrl() throws MessageHandlerException
	{
		SubversionSource s = new SubversionSource(null, "-1", "trunk/dbFiles/dummy2.sql");
		s.validate();
	}
}
