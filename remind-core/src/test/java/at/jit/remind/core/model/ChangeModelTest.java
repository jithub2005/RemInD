package at.jit.remind.core.model;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.context.reporting.ListBasedDeploymentInformationHandler;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.status.State;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.DocumentInformation;
import at.jit.remind.core.xml.Environment;
import at.jit.remind.core.xml.FileSystem;
import at.jit.remind.core.xml.InstallationBlock;
import at.jit.remind.core.xml.InstallationDocument;
import at.jit.remind.core.xml.InstallerInformation;
import at.jit.remind.core.xml.Phase;
import at.jit.remind.core.xml.PhaseDescription;
import at.jit.remind.core.xml.Source;
import at.jit.remind.core.xml.Target;

public class ChangeModelTest
{
	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();
	private static UserInput userInput = new UserInput();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
		RemindContext.getInstance().setDeploymentInformationHandler(new ListBasedDeploymentInformationHandler());
	}

	@Before
	public void setUp()
	{
		userInput.setEnvironment("DEV");
		userInput.setLowerTestCycleNumber(1);
		userInput.setUpperTestCycleNumber(5);
	}

	@After
	public void tearDown() throws Exception
	{
		listBasedMessageHandler.getMessageList().clear();
	}

	@Test
	public void canHandleSourceAndTargetUpdateProperly() throws IOException, RemindModelException
	{
		Change change = generateFilesystemTargetChange("FooBarSource.txt", "FooBarTarget.txt", Environment.DEV);

		InstallationDocument installationDocument = createInstallationDocument(change);
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		installationDocumentModel.update(installationDocument);

		assertTrue("FooBarSource.txt".equals(installationDocumentModel.getElement().getInstallationBlock().get(0).getChange().get(0).getSource()
				.getFileSystem().getPath()));
		assertTrue("FooBarTarget.txt".equals(installationDocumentModel.getElement().getInstallationBlock().get(0).getChange().get(0).getTarget()
				.getFileSystem().getPath()));
	}

	@Test
	public void canValidate() throws IOException, RemindModelException
	{
		File sourceTmpFile = createTempFile();
		String targetFilePath = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID() + ".tmp";
		Change change = generateFilesystemTargetChange(sourceTmpFile.getAbsolutePath(), targetFilePath, Environment.DEV);

		InstallationDocument installationDocument = createInstallationDocument(change);
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		BufferedWriter out = new BufferedWriter(new FileWriter(sourceTmpFile));
		out.write("SELECT * FROM FOOBAR;");
		out.close();

		installationDocumentModel.update(installationDocument);
		installationDocumentModel.validate();

		assertTrue(State.Ok.equals(installationDocumentModel.getState()));

	}

	@Test
	public void cannotValidateInvalidSourceOrTarget() throws IOException, RemindModelException
	{
		String sourcePath = "FooBar.txt";
		File targetTmpFile = createTempFile();

		Change change = generateFilesystemTargetChange(sourcePath, targetTmpFile.getAbsolutePath(), Environment.DEV);

		InstallationDocument installationDocument = createInstallationDocument(change);
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		installationDocumentModel.update(installationDocument);
		installationDocumentModel.validate();

		assertTrue(State.Error.equals(installationDocumentModel.getState()));
	}

	@Test
	public void deployReturnsStateOk() throws IOException, MessageHandlerException, RemindModelException
	{
		File sourceTmpFile = createTempFile();
		File targetTmpFile = createTempFile();
		Change change = generateFilesystemTargetChange(sourceTmpFile.getAbsolutePath(), targetTmpFile.getAbsolutePath(), Environment.DEV);

		InstallationDocument installationDocument = createInstallationDocument(change);
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		BufferedWriter out = new BufferedWriter(new FileWriter(sourceTmpFile));
		out.write("SELECT * FROM FOOBAR;");
		out.close();

		installationDocumentModel.update(installationDocument);
		installationDocumentModel.deploy(userInput);

		assertTrue("State is " + installationDocumentModel.getState(), State.Ok.equals(installationDocumentModel.getState()));
	}

	@Test
	public void deployReturnsStateError() throws IOException, RemindModelException
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);

		String sourcePath = "FooBar.txt";
		String targetPath = createTempFile().getAbsolutePath();
		Change change = generateFilesystemTargetChange(sourcePath, targetPath, Environment.DEV);

		InstallationDocument installationDocument = createInstallationDocument(change);
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		installationDocumentModel.update(installationDocument);
		try
		{
			installationDocumentModel.deploy(userInput);
		}
		catch (MessageHandlerException e)
		{
			// we do not handle catch in this test because we need to know what is the State.
		}

		assertTrue("State is " + installationDocumentModel.getState(), State.Error.equals(installationDocumentModel.getState()));
	}

	@Test
	public void deployReturnsStateUnknown() throws IOException, MessageHandlerException, RemindModelException
	{
		// State is unknown because due to wrong user input, the deployment will never be executed.
		File sourceTmpFile = createTempFile();
		File targetTmpFile = createTempFile();
		Change change = generateFilesystemTargetChange(sourceTmpFile.getAbsolutePath(), targetTmpFile.getAbsolutePath(), Environment.DEV);

		BufferedWriter out = new BufferedWriter(new FileWriter(sourceTmpFile));
		out.write("SELECT * FROM FOOBAR;");
		out.close();

		InstallationDocument installationDocument = createInstallationDocument(change);
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		userInput.setEnvironment("QM");

		installationDocumentModel.update(installationDocument);
		installationDocumentModel.deploy(userInput);

		assertTrue("State is " + installationDocumentModel.getState(), State.Unknown.equals(installationDocumentModel.getState()));
	}

	private Change generateFilesystemTargetChange(String sourcePath, String targetPath, Environment... environments)
	{
		FileSystem fileSystem = new FileSystem();
		fileSystem.setPath(sourcePath);

		Source source = new Source();
		source.setFileSystem(fileSystem);

		fileSystem = new FileSystem();
		fileSystem.setPath(targetPath);

		Target target = new Target();
		target.setFileSystem(fileSystem);

		Change change = new Change();
		change.setDescription("Description");
		change.setDeveloper("Developer");
		change.setOnlyOnce(false);
		change.setPause(false);
		change.setSource(source);
		change.setTarget(target);
		change.setTestCycleNumber(new BigInteger("3"));
		for (Environment environment : environments)
		{
			change.getEnvironment().add(environment);
		}

		return change;
	}

	// we have to create an installation document too because the new fill method is null without it.
	private InstallationDocument createInstallationDocument(Change change)
	{
		InstallationBlock installationBlock = new InstallationBlock();
		installationBlock.getChange().add(change);

		PhaseDescription phaseDescription = new PhaseDescription();
		phaseDescription.setPhase(Phase.OTHERS);

		installationBlock.setPhaseDescription(phaseDescription);

		InstallationDocument installationDocument = new InstallationDocument();

		InstallerInformation installerInformation = new InstallerInformation();
		installerInformation.setVersion("1.0");

		installationDocument.setInstallerInformation(installerInformation);

		DocumentInformation documentInformation = new DocumentInformation();
		documentInformation.setTarget("Maxit DB");
		documentInformation.setRelease("Release 11.1");
		documentInformation.setTestCycle("3");
		documentInformation.setTitle("Installationsanleitung");
		documentInformation.setVersion("0.2");

		installationDocument.setDocumentInformation(documentInformation);
		installationDocument.getInstallationBlock().add(installationBlock);

		return installationDocument;
	}

	private File createTempFile() throws IOException
	{
		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".tmp");
		tmpFile.deleteOnExit();

		return tmpFile;
	}
}
