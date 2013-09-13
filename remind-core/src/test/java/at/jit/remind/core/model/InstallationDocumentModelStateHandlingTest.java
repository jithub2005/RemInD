package at.jit.remind.core.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.context.reporting.ListBasedDeploymentInformationHandler;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.content.scm.SubversionSourceTest;
import at.jit.remind.core.model.status.State;

public class InstallationDocumentModelStateHandlingTest
{
	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();
	private static UserInput userInput = new UserInput();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
		RemindContext.getInstance().setDeploymentInformationHandler(new ListBasedDeploymentInformationHandler());

		userInput.setEnvironment("DEV");
		userInput.setLowerTestCycleNumber(1);
		userInput.setUpperTestCycleNumber(5);

		String sqlFileName = "InstallationDocumentModelStateHandlingTest.sql";

		String sqlContent = FileUtils.readFileToString(FileUtils.toFile(InstallationDocumentModelStateHandlingTest.class
				.getResource("/database/" + sqlFileName)));

		File file = new File(System.getProperty("java.io.tmpdir") + "/" + sqlFileName);
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(sqlContent);
		out.close();
		file.deleteOnExit();
	}

	@Before
	public void setUp() throws Exception
	{
		listBasedMessageHandler.getMessageList().clear();
	}

	@Test
	public void worstStateMustBeError() throws Exception
	{
		// The changes in this example have the states OK, ERROR, OK, so the worst state must be ERROR
		// because action is set to ABORT.
		// Hint: Usually validate before deploy aborts if there are errors.
		// But validate does only check if the database is reachable. In this test, the statements itself are wrong
		// so validate doesn't abort because it doesn't check the statements, so the handling is up to deploy

		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);

		String installDocXml = FileUtils.readFileToString(FileUtils.toFile(SubversionSourceTest.class
				.getResource("/model/InstallationDocumentModelStateHandlingTest.xml")));

		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();
		installationDocumentModel.update(installDocXml);

		try
		{
			installationDocumentModel.deploy(userInput);
		}
		catch (MessageHandlerException e)
		{
			// do nothing here. Must be handled underneath
		}

		State state = installationDocumentModel.determineState();
		if (State.Error.equals(state))
		{
			assertTrue(true);
		}
		else
		{
			fail("State is " + state.toString());
		}
	}

	@Test
	public void worstStateMustBeOk() throws Exception
	{
		// The changes in this example have the states OK, OK, OK, so the worst state must be OK.
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);

		String installDocXml = FileUtils.readFileToString(FileUtils.toFile(SubversionSourceTest.class
				.getResource("/model/InstallationDocumentModelStateHandlingTest2.xml")));

		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();
		installationDocumentModel.update(installDocXml);
		installationDocumentModel.deploy(userInput);

		State state = installationDocumentModel.determineState();
		if (State.Ok.equals(state))
		{
			assertTrue(true);
		}
		else
		{
			fail("State is " + state.toString());
		}
	}

	@Test
	public void worstStateMustBeWarning() throws Exception
	{
		// The changes in this example have the states OK, ERROR, OK.
		// The worst state must be WARNING because the Action is set to SKIP:

		listBasedMessageHandler.setFeedback(RemindModelFeedback.Skip);

		String installDocXml = FileUtils.readFileToString(FileUtils.toFile(SubversionSourceTest.class
				.getResource("/model/InstallationDocumentModelStateHandlingTest.xml")));

		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();
		installationDocumentModel.update(installDocXml);
		try
		{
			installationDocumentModel.deploy(userInput);
		}
		catch (MessageHandlerException e)
		{
			// do nothing here. Must be handled underneath
		}

		State state = installationDocumentModel.determineState();
		if (State.Warning.equals(state))
		{
			assertTrue(true);
		}
		else
		{
			fail("State is " + state.toString());
		}
	}
}
