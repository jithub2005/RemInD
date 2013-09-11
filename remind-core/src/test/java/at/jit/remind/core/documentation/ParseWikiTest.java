package at.jit.remind.core.documentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.content.io.FileSystemLocationTest;

public class ParseWikiTest
{
	private static File wikiFile;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setMessageHandler(new ListBasedMessageHandler());
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());

		String parsedWikiFileContent = FileUtils
				.readFileToString(FileUtils.toFile(FileSystemLocationTest.class.getResource("/documentation/ParseWikiTest.txt")));

		wikiFile = File.createTempFile(ParseWikiTest.class.getSimpleName(), ".tmp");
		wikiFile.deleteOnExit();

		BufferedWriter out = new BufferedWriter(new FileWriter(wikiFile));
		out.write(parsedWikiFileContent);
		out.close();
	}

	@Test
	public void canExtractDataFromWikiDocumentProperly() throws Exception
	{
		// Result must consist of 4 Changes because there are 4 lines.

		// Change 2 must contain the following attributes and values:
		// an sql string.
		// onlyOnce = false
		// environment QM = there must not be an entry
		// environment Production = there must be an entry.
		// Developer = TG
		// SVNRepo + URL
		// target database with SID = MAXIT and Schema = maxwell

		ParseWiki parseWiki = new ParseWiki("Syntax");
		parseWiki.setSID("MAXIT");
		String parsedFileContent = parseWiki.wikiFileToXml(wikiFile);

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		InputStream xmlInput = new ByteArrayInputStream(parsedFileContent.getBytes());
		Element documentRoot = builder.parse(xmlInput).getDocumentElement();

		// The nodes must consist of 4x "change"
		NodeList changes = documentRoot.getElementsByTagName("change");
		Node change2 = changes.item(1);
		Element change2Element = (Element) change2;

		String developer = getTagValue(change2Element, "developer");
		String environmentProduction = getTagValue(change2Element, "environment");
		String onlyOnce = getTagValue(change2Element, "onlyOnce");
		String sid = getTagValue(change2Element, "SID");
		String schema = getTagValue(change2Element, "schema");

		String repositoryUrl = getTagValue(change2Element, "repositoryUrl");
		String path = getTagValue(change2Element, "path");

		assertTrue(changes.getLength() == 4);

		assertTrue("Developer is: " + developer, "TG".equals(developer));
		assertTrue("Production is: " + environmentProduction, "Production".equals(environmentProduction));
		assertTrue("OnlyOnce is: " + onlyOnce, "false".equals(onlyOnce));
		assertTrue("Sid is: " + sid, "MAXIT".equals(sid));
		assertTrue("Schema is: " + schema, "maxwell".equals(schema));
		assertTrue("RepositoryUrl is: ", !"".equals(repositoryUrl));
		assertTrue("Path is: ", !"".equals(path));
	}

	@Test
	public void recognizesMaintenanceMode() throws Exception
	{
		ParseWiki parseWiki = new ParseWiki("Syntax");
		String maintenanceXml = parseWiki.wikiFileToXml(wikiFile);

		String parsedWikiFileNoMaintenance = FileUtils.readFileToString(FileUtils.toFile(FileSystemLocationTest.class
				.getResource("/documentation/ParseWikiTest2.txt")));

		File noMaintenanceFile = File.createTempFile(ParseWikiTest.class.getSimpleName(), ".tmp");

		BufferedWriter out = new BufferedWriter(new FileWriter(noMaintenanceFile));
		out.write(parsedWikiFileNoMaintenance);
		out.close();

		String noMaintenanceXml = parseWiki.wikiFileToXml(noMaintenanceFile);
		noMaintenanceFile.delete();

		assertTrue(maintenanceXml.contains("duringMaintenance"));
		assertTrue(maintenanceXml.contains("installationblock"));

		assertFalse(noMaintenanceXml.contains("duringMaintenance"));
		assertFalse(noMaintenanceXml.contains("installationblock"));
	}

	@Test
	public void canHandleInvalidPropertyName()
	{
		ParseWiki parseWiki = new ParseWiki("FooBar");
		try
		{
			parseWiki.wikiFileToXml(wikiFile);
			assertTrue("Wrong properties file is not recognized", false);
		}
		catch (RemindModelException e)
		{
			assertTrue("No properties loaded!".equals(e.getMessage()));
		}
	}

	private String getTagValue(Element change, String tag)
	{
		NodeList attributeList = change.getElementsByTagName(tag);
		Element attributeElement = (Element) attributeList.item(0);
		NodeList attributeElementList = attributeElement.getChildNodes();
		String attributeValue = attributeElementList.item(0).getNodeValue();

		return attributeValue;
	}
}
