package at.jit.remind.core.model;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.context.reporting.ListBasedDeploymentInformationHandler;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.status.State;

public class InstallationDocumentModelTest
{
	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();
	private static UserInput userInput = new UserInput();

	private final static String SourceFile = "InstallationDocumentModelTestSource.txt";
	private static String instDoc;

	@Before
	public void setUp() throws IOException
	{
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
		RemindContext.getInstance().setDeploymentInformationHandler(new ListBasedDeploymentInformationHandler());

		userInput.setEnvironment("Production");
		userInput.setLowerTestCycleNumber(1);
		userInput.setUpperTestCycleNumber(5);

		instDoc = FileUtils.readFileToString(FileUtils.toFile(InstallationDocumentModelTest.class.getResource("/model/InstallationDocumentModelTest.xml")));

		String pathname = System.getProperty("java.io.tmpdir") + "/" + SourceFile;
		File file = new File(pathname);
		file.deleteOnExit();

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write("INSERT INTO FOO BAR...\n INSERT INTO FOO BAR 2");
		out.close();

	}

	@After
	public void tearDown() throws IOException
	{
		File targetFile = new File(System.getProperty("java.io.tmpdir") + "/" + "InstallationDocumentModelTestTarget.txt");
		targetFile.createNewFile();
		targetFile.delete();
	}

	@Test
	public void canValidate() throws RemindModelException
	{
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();
		installationDocumentModel.update(instDoc);
		installationDocumentModel.validate();

		State state = installationDocumentModel.determineState();
		assertTrue("State is " + state.name(), State.Ok.equals(state));
	}

	@Test
	public void canDeploy() throws RemindModelException, MessageHandlerException
	{
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();
		installationDocumentModel.update(instDoc);
		installationDocumentModel.deploy(userInput);

		State state = installationDocumentModel.determineState();
		assertTrue("State is " + state.name(), State.Ok.equals(state));
	}

	@Test
	public void canHandleStateOk() throws RemindModelException, MessageHandlerException
	{
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();
		installationDocumentModel.update(instDoc);
		installationDocumentModel.deploy(userInput);

		State state = installationDocumentModel.determineState();
		assertTrue("State is " + state.name(), State.Ok.equals(state));
	}
}
