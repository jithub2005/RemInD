package at.jit.remind.core.context.reporting;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.InstallationDocumentModel;
import at.jit.remind.core.model.UserInput;
import at.jit.remind.core.model.content.database.DatabaseFeedback;
import at.jit.remind.core.model.content.io.FileSystemLocation;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.Database;
import at.jit.remind.core.xml.DocumentInformation;
import at.jit.remind.core.xml.Environment;
import at.jit.remind.core.xml.FileSystem;
import at.jit.remind.core.xml.InstallationBlock;
import at.jit.remind.core.xml.InstallationDocument;
import at.jit.remind.core.xml.InstallerInformation;
import at.jit.remind.core.xml.Phase;
import at.jit.remind.core.xml.PhaseDescription;
import at.jit.remind.core.xml.Source;
import at.jit.remind.core.xml.SourceCodeManagement;
import at.jit.remind.core.xml.Subversion;
import at.jit.remind.core.xml.Target;

public class DeploymentInformationHandlerTest
{
	private UserInput userInput = new UserInput(1, 5, "DEV");

	private Environment environment = Environment.DEV;
	private static String testCycleNumber = "3";

	private static String fileSystemSourceTempDir = System.getProperty("java.io.tmpdir") + "/DeploymentHistoryHandlerTestSource.txt";
	private static String fileSystemTargetTempDir = System.getProperty("java.io.tmpdir") + "/DeploymentHistoryHandlerTestTarget.txt";

	private static String repositoryUrl = "http://dev.jit.at/svn/remind-testing";
	private static String revision = "-1";
	private static String subversionPath = "trunk/dbFiles/dummy2.sql";

	private static String sid = "REMINDTESTING";
	private static String schema = "remindtest";

	private static String target = "Maxit DB";
	private static String release = "Release 11.1";
	private static String title = "Installationsanleitung";
	private static String version = "1.0";
	private static String testCycle = "3";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		ListBasedMessageHandler messageHandler = new ListBasedMessageHandler();
		messageHandler.setFeedback(DatabaseFeedback.SkipStatement);
		RemindContext.getInstance().setMessageHandler(messageHandler);
		RemindContext.getInstance().setDeploymentInformationHandler(new ListBasedDeploymentInformationHandler());
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());

		File fsSource = new File(fileSystemSourceTempDir);
		fsSource.createNewFile();
		fsSource.deleteOnExit();

		File fsTarget = new File(fileSystemTargetTempDir);
		fsTarget.createNewFile();
		fsTarget.deleteOnExit();
	}

	@After
	public void tearDown() throws Exception
	{
		ListBasedDeploymentInformationHandler d = (ListBasedDeploymentInformationHandler) RemindContext.getInstance().getDeploymentInformationHandler();
		d.getDeploymentInformationList().clear();
	}

	@Test
	public void canAddFilesystemSourceFilesystemTargetToHistory()
	{
		InstallationDocument installationDocument = createInstallationDocument(createFileSystemChange());
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		ListBasedDeploymentInformationHandler d = (ListBasedDeploymentInformationHandler) RemindContext.getInstance().getDeploymentInformationHandler();

		try
		{
			installationDocumentModel.update(installationDocument);
			installationDocumentModel.deploy(userInput);

			List<DeploymentInformation> dList = d.getDeploymentInformationList();
			DeploymentInformation info = dList.get(0);

			assertTrue("Environment is: " + info.getEnvironment(), environment.name().equals(info.getEnvironment()));
			assertTrue("Testcycle is: " + info.getTestCycleNumber(), testCycle.equals(info.getTestCycle()));

			String fs = FileSystemLocation.class.getSimpleName();
			assertTrue("SourceInfo doesn't contain " + fs, info.getSourceInfo().contains(fs));
			assertTrue("FileSystemLocation is not source", info.getSourceInfo().contains("isSource='true'"));
			assertTrue("FileSystemLocation doesn't contain path " + fileSystemSourceTempDir, info.getSourceInfo().contains(fileSystemSourceTempDir));

			assertTrue("SourceInfo doesn't contain " + fs, info.getTargetInfo().contains(fs));
			assertTrue("FileSystemLocation is not target", info.getTargetInfo().contains("isSource='false'"));
			assertTrue("FileSystemLocation doesn't contain path " + fileSystemTargetTempDir, info.getTargetInfo().contains(fileSystemTargetTempDir));
		}
		catch (RemindModelException e)
		{
			fail(e.getMessage());
		}
		catch (MessageHandlerException e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void canAddSubversionSourceDatabaseTargetToHistory()
	{
		InstallationDocument installationDocument = createInstallationDocument(createSubversionSourceDbTargetChange());
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		ListBasedDeploymentInformationHandler d = (ListBasedDeploymentInformationHandler) RemindContext.getInstance().getDeploymentInformationHandler();

		try
		{
			installationDocumentModel.update(installationDocument);
			installationDocumentModel.deploy(userInput);

			List<DeploymentInformation> dList = d.getDeploymentInformationList();
			DeploymentInformation info = dList.get(0);

			assertTrue("Environment is: " + info.getEnvironment(), environment.name().equals(info.getEnvironment()));
			assertTrue("Testcycle is: " + info.getTestCycleNumber(), testCycle.equals(info.getTestCycle()));

			assertTrue("SourceInfo contains SubversionSource", info.getSourceInfo().contains("SubversionSource"));
			assertTrue("SourceInfo contains contain " + repositoryUrl, info.getSourceInfo().contains(repositoryUrl));
			assertTrue("SourceInfo contains contain " + subversionPath, info.getSourceInfo().contains(subversionPath));
			assertTrue("SourceInfo does not contain " + revision, !info.getSourceInfo().contains(revision));

			assertTrue("TargetInfo contains DatabaseTarget", info.getTargetInfo().contains("DatabaseTarget"));
			assertTrue("DatabaseTarget contains SID " + sid, info.getTargetInfo().contains(sid));
			assertTrue("DatabaseTarget contains Schema " + schema, info.getTargetInfo().contains(schema));
		}
		catch (RemindModelException e)
		{
			fail(e.getMessage());
		}
		catch (MessageHandlerException e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void receivesAllParentData()
	{
		InstallationDocument installationDocument = createInstallationDocument(createSubversionSourceDbTargetChange());
		InstallationDocumentModel installationDocumentModel = new InstallationDocumentModel();

		ListBasedDeploymentInformationHandler d = (ListBasedDeploymentInformationHandler) RemindContext.getInstance().getDeploymentInformationHandler();

		try
		{
			installationDocumentModel.update(installationDocument);
			installationDocumentModel.deploy(userInput);

			List<DeploymentInformation> dList = d.getDeploymentInformationList();
			DeploymentInformation info = dList.get(0);

			assertTrue("Environment is: " + info.getEnvironment(), environment.name().equals(info.getEnvironment()));
			assertTrue("Title is: " + info.getTitle(), title.equals(info.getTitle()));
			assertTrue("Target is: " + info.getTarget(), target.equals(info.getTarget()));
			assertTrue("Release is: " + info.getRelease(), release.equals(info.getRelease()));
			assertTrue("TestCycle is: " + info.getTestCycle(), testCycle.equals(info.getTestCycle()));
			assertTrue("Version is: " + info.getVersion(), version.equals(info.getVersion()));

		}
		catch (RemindModelException e)
		{
			fail(e.getMessage());
		}
		catch (MessageHandlerException e)
		{
			fail(e.getMessage());
		}
	}

	private Change createSubversionSourceDbTargetChange()
	{
		Source source = new Source();
		SourceCodeManagement sourceCodeManagement = new SourceCodeManagement();

		Subversion subversion = new Subversion();
		subversion.setRepositoryUrl(repositoryUrl);
		subversion.setRevision(revision);
		subversion.setPath(subversionPath);

		sourceCodeManagement.setSubversion(subversion);

		source.setSourceCodeManagement(sourceCodeManagement);

		Target target = new Target();
		Database database = new Database();
		database.setSID(sid);
		database.setSchema(schema);

		target.setDatabase(database);

		Change change = new Change();
		change.setDescription("Description");
		change.setDeveloper("Developer");
		change.setOnlyOnce(false);
		change.setPause(false);
		change.setSource(source);
		change.setTarget(target);
		change.setTestCycleNumber(new BigInteger(testCycleNumber));
		change.getEnvironment().add(environment);

		return change;
	}

	private Change createFileSystemChange()
	{
		FileSystem fileSystem = new FileSystem();
		fileSystem.setPath(fileSystemSourceTempDir);

		Source source = new Source();
		source.setFileSystem(fileSystem);

		fileSystem = new FileSystem();
		fileSystem.setPath(fileSystemTargetTempDir);

		Target target = new Target();
		target.setFileSystem(fileSystem);

		Change change = new Change();
		change.setDescription("Description");
		change.setDeveloper("Developer");
		change.setOnlyOnce(false);
		change.setPause(false);
		change.setSource(source);
		change.setTarget(target);
		change.setTestCycleNumber(new BigInteger(testCycleNumber));
		change.getEnvironment().add(environment);

		return change;
	}

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
		documentInformation.setTarget(target);
		documentInformation.setRelease(release);
		documentInformation.setTestCycle(testCycle);
		documentInformation.setTitle(title);
		documentInformation.setVersion(version);

		installationDocument.setDocumentInformation(documentInformation);
		installationDocument.getInstallationBlock().add(installationBlock);

		return installationDocument;
	}
}
