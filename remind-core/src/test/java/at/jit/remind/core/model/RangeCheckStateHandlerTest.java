package at.jit.remind.core.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.context.reporting.ListBasedDeploymentInformationHandler;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.status.State;

public class RangeCheckStateHandlerTest
{
	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();
	private static UserInput userInput = new UserInput();
	private static File sourceFile;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
		RemindContext.getInstance().setDeploymentInformationHandler(new ListBasedDeploymentInformationHandler());

		userInput.setEnvironment("DEV");
		userInput.setLowerTestCycleNumber(1);
		userInput.setUpperTestCycleNumber(5);

		sourceFile = new File(System.getProperty("java.io.tmpdir") + "/" + "RangeCheckStateHandlerTestSource.txt");
		sourceFile.createNewFile();
		sourceFile.deleteOnExit();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		File targetFile = new File(System.getProperty("java.io.tmpdir") + "/" + "RangeCheckStateHandlerTest.txt");
		targetFile.createNewFile();
		targetFile.deleteOnExit();
	}

	@Before
	public void setUp() throws Exception
	{
		listBasedMessageHandler.getMessageList().clear();
	}

	@Test
	public void wrongFileSystemSourceCausesInstallationBlockOkAndError()
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);
		InstallationDocumentModel installationDocumentModel = null;
		InstallationBlockModel installationBlockModel1 = null;
		InstallationBlockModel installationBlockModel2 = null;

		try
		{
			String installDocXml = FileUtils.readFileToString(FileUtils.toFile(RangeCheckStateHandlerTest.class
					.getResource("/model/RangeCheckStateHandlerTest1.xml")));
			installationDocumentModel = new InstallationDocumentModel();
			installationDocumentModel.update(installDocXml);
			installationDocumentModel.deploy(userInput);
		}
		catch (Exception e) // Catch is awaited,
		{
			if (installationDocumentModel != null)
			{
				at.jit.remind.core.model.InstallationDocumentModel.Iterator iter = installationDocumentModel.iterator();

				installationBlockModel1 = iter.getNext();
				while (iter.hasNext())
				{
					installationBlockModel2 = iter.getNext();
				}

				if (State.Ok.equals(installationBlockModel1.getState()) && State.Error.equals(installationBlockModel2.getState()))
				{
					assertTrue(true);
				}
				else
				{
					fail("State of installation blocks 1 and 2 are " + installationBlockModel1.getState().toString() + " and "
							+ installationBlockModel2.getState().toString());
				}
			}
			else
			{
				fail("Cant execute JUnit test, because installationDocumentModel is NULL.");
			}
		}
	}

	@Test
	public void wrongFilesystemSourceSourceCausesInstallationDocumentError() throws IOException, RemindModelException, MessageHandlerException
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);
		InstallationDocumentModel installationDocumentModel = null;
		try
		{
			String installDocXml = FileUtils.readFileToString(FileUtils.toFile(RangeCheckStateHandlerTest.class
					.getResource("/model/RangeCheckStateHandlerTest1.xml")));
			installationDocumentModel = new InstallationDocumentModel();
			installationDocumentModel.update(installDocXml);
			installationDocumentModel.deploy(userInput);
		}
		catch (Exception e)
		{
			if (installationDocumentModel != null)
			{
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
			else
			{
				fail("Cant execute JUnit test, because installationDocumentModel is NULL.");
			}
		}
	}

	@Test
	public void wrongSubversionSourceCausesInstallationDocumentError() throws IOException, RemindModelException, MessageHandlerException
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);
		InstallationDocumentModel installationDocumentModel = null;
		try
		{
			String installDocXml = FileUtils.readFileToString(FileUtils.toFile(RangeCheckStateHandlerTest.class
					.getResource("/model/RangeCheckStateHandlerTest2.xml")));
			installationDocumentModel = new InstallationDocumentModel();
			installationDocumentModel.update(installDocXml);
			installationDocumentModel.deploy(userInput);
		}
		catch (Exception e)
		{
			if (installationDocumentModel != null)
			{
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
			else
			{
				fail("Cant execute JUnit test, because installationDocumentModel is NULL.");
			}
		}
	}

	@Test
	public void wrongFileSystemTargetCausesInstallationDocumentError() throws IOException, RemindModelException, MessageHandlerException
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);
		InstallationDocumentModel installationDocumentModel = null;
		try
		{
			String installDocXml = FileUtils.readFileToString(FileUtils.toFile(RangeCheckStateHandlerTest.class
					.getResource("/model/RangeCheckStateHandlerTest3.xml")));
			installationDocumentModel = new InstallationDocumentModel();
			installationDocumentModel.update(installDocXml);
			installationDocumentModel.deploy(userInput);
		}
		catch (Exception e)
		{
			if (installationDocumentModel != null)
			{
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
			else
			{
				fail("Cant execute JUnit test, because installationDocumentModel is NULL.");
			}
		}
	}

	@Test
	public void installationDocumentStatusOk() throws IOException, RemindModelException, MessageHandlerException
	{
		InstallationDocumentModel installationDocumentModel = prepareAndExecuteDocument("/model/RangeCheckStateHandlerTest4.xml");
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
	public void wrongDatabaseTargetCausesInstallationDocumentStatusError() throws IOException, RemindModelException
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);

		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());

		String sqlContent = FileUtils.readFileToString(FileUtils.toFile(RangeCheckStateHandlerTest.class.getResource("/RangeCheckStateHandlerTest.sql")));
		generateSqlFile(sqlContent, "RangeCheckStateHandlerTest.sql");

		InstallationDocumentModel installationDocumentModel = prepareAndExecuteDocument("/model/RangeCheckStateHandlerTest5.xml");
		State state = installationDocumentModel.getState();

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
	public void correctDatabaseTargetCausesInstallationDocumentStatusOk() throws IOException, RemindModelException
	{
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());

		listBasedMessageHandler.setFeedback(RemindModelFeedback.Skip);

		String sqlContent = FileUtils.readFileToString(FileUtils.toFile(RangeCheckStateHandlerTest.class.getResource("/RangeCheckStateHandlerTest.sql")));
		generateSqlFile(sqlContent, "RangeCheckStateHandlerTest.sql");

		InstallationDocumentModel installationDocumentModel = prepareAndExecuteDocument("/model/RangeCheckStateHandlerTest6.xml");

		State state = installationDocumentModel.getState();

		if (State.Ok.equals(state))
		{
			assertTrue(true);
		}
		else
		{
			fail("State is " + state.toString());
		}
	}

	private InstallationDocumentModel prepareAndExecuteDocument(String filePath) throws IOException, RemindModelException
	{
		String installDocXml = FileUtils.readFileToString(FileUtils.toFile(RangeCheckStateHandlerTest.class.getResource(filePath)));
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();
		installationDocumentModel.update(installDocXml);

		try
		{
			installationDocumentModel.deploy(userInput);
		}
		catch (MessageHandlerException e)
		{
			// intentionally left blank
		}

		return installationDocumentModel;
	}

	private void generateSqlFile(String sqlContent, String sqlFileName) throws IOException
	{
		String pathname = System.getProperty("java.io.tmpdir") + "/" + sqlFileName;
		File file = new File(pathname);
		file.deleteOnExit();

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(sqlContent);
		out.close();
	}
}
