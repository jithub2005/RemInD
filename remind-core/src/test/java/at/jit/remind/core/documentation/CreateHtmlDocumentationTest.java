package at.jit.remind.core.documentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.Database;
import at.jit.remind.core.xml.DocumentInformation;
import at.jit.remind.core.xml.Environment;
import at.jit.remind.core.xml.InstallationBlock;
import at.jit.remind.core.xml.InstallationDocument;
import at.jit.remind.core.xml.Phase;
import at.jit.remind.core.xml.PhaseDescription;
import at.jit.remind.core.xml.Schema;
import at.jit.remind.core.xml.Source;
import at.jit.remind.core.xml.SourceCodeManagement;
import at.jit.remind.core.xml.Subversion;
import at.jit.remind.core.xml.Target;

public class CreateHtmlDocumentationTest
{
	private CreateHtmlDocumentation htmlExporter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
		RemindContext.getInstance().setMessageHandler(new ListBasedMessageHandler());

		copyTestFile("velocity.properties");
		copyTestFile("velocity.vm");
		copyTestFile("version.properties");
	}

	@Before
	public void setUp() throws Exception
	{
		htmlExporter = new CreateHtmlDocumentation("Velocity");
	}

	@Test
	public void testGeneratedHtmlFilePath() throws IOException, RemindModelException
	{
		htmlExporter.generateVelocityContext(generateSchema());
		String generatedHtmlFileName = htmlExporter.getGeneratedHtmlFileName();

		assertFalse("".equals(generatedHtmlFileName));
		assertFalse(generatedHtmlFileName.contains(" "));
	}

	@Test
	public void canInterceptHtmlFileNameGenerationError()
	{
		String generatedHtmlFileName = htmlExporter.getGeneratedHtmlFileName();

		assertTrue("".equals(generatedHtmlFileName));
	}

	@Test
	public void testGenerateVelocityContext() throws RemindModelException, IOException
	{
		String htmlContent = htmlExporter.generateVelocityContext(generateSchema());

		assertFalse("".equals(htmlContent));
		assertTrue(htmlContent.contains("<html>")); // html translation works.
		assertTrue(htmlContent.contains("http://subversion.foo/")); // now we know that changes will be appended
	}

	@Test
	public void canWriteHtmlFiles() throws RemindModelException
	{
		htmlExporter.generateVelocityContext(generateSchema());
		htmlExporter.writeHtmlFile();

		File file = new File(System.getProperty("java.io.tmpdir") + "/" + htmlExporter.getGeneratedHtmlFileName());
		assertTrue(file.exists());

		if (file.exists())
		{
			file.delete();
		}
	}

	@Test
	public void canInterceptErrorsWhileWritingHtmlFile() throws RemindModelException
	{
		String absolutFilePath = htmlExporter.writeHtmlFile();

		assertTrue((System.getProperty("java.io.tmpdir") + "/").equals(absolutFilePath));
	}

	@Test
	public void canReceiveDataAsOutputStream() throws RemindModelException
	{
		htmlExporter.generateVelocityContext(generateSchema());
		OutputStream outputStream = htmlExporter.getHtmlContentAsOutputStream();
		String htmlContent = outputStream.toString();

		assertTrue(htmlContent.length() > 0);
	}

	@Test(expected = RemindModelException.class)
	public void canHandleInvalidPropertyFile() throws RemindModelException
	{
		htmlExporter = new CreateHtmlDocumentation("FooBar");
	}

	private static void copyTestFile(String fileName) throws IOException
	{
		InputStream is = CreateHtmlDocumentationTest.class.getResourceAsStream("/work/systemSettings/resources/" + fileName);
		String fileContent = IOUtils.toString(is);

		File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
		file.deleteOnExit();

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(fileContent);
		out.close();
	}

	private Schema generateSchema()
	{
		Source source = new Source();

		Subversion subversion = new Subversion();
		subversion.setPath("repository.foo.path");
		subversion.setRevision("-1");
		subversion.setRepositoryUrl("http://subversion.foo/");

		SourceCodeManagement sourceCodeManagement = new SourceCodeManagement();
		sourceCodeManagement.setSubversion(subversion);

		source.setSourceCodeManagement(sourceCodeManagement);

		Database database = new Database();
		database.setSchema("DBSchema");
		database.setSID("MAXIT");

		Target target = new Target();
		target.setDatabase(database);

		Change change = new Change();
		change.setDescription("Description");
		change.setDeveloper("Developer");
		change.setOnlyOnce(false);
		change.setPause(false);
		change.setSource(source);
		change.setTarget(target);
		change.setTestCycleNumber(new BigInteger("3"));
		change.getEnvironment().add(Environment.QM);

		InstallationBlock installationBlock = new InstallationBlock();
		installationBlock.getChange().add(change);

		PhaseDescription phaseDescription = new PhaseDescription();
		phaseDescription.setPhase(Phase.OTHERS);

		installationBlock.setPhaseDescription(phaseDescription);

		InstallationDocument installationDocument = new InstallationDocument();

		DocumentInformation documentInformation = new DocumentInformation();
		documentInformation.setTarget("This is the target");
		documentInformation.setRelease("Release 1");
		documentInformation.setTestCycle("TC 01");
		documentInformation.setTitle("this is the title");
		documentInformation.setVersion("01");

		installationDocument.setDocumentInformation(documentInformation);
		installationDocument.getInstallationBlock().add(installationBlock);

		Schema schema = new Schema();
		schema.setInstallationDocument(installationDocument);

		return schema;
	}
}
