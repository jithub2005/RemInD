package at.jit.remind.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.context.reporting.ListBasedDeploymentInformationHandler;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.Environment;
import at.jit.remind.core.xml.InstallationDocument;

public class ChangeFilterTest
{
	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
		RemindContext.getInstance().setDeploymentInformationHandler(new ListBasedDeploymentInformationHandler());
	}

	@After
	public void tearDown() throws Exception
	{
		listBasedMessageHandler.getMessageList().clear();
	}

	@Test
	public void testChangeFilter() throws IOException, RemindModelException
	{
		String xmlData = FileUtils.readFileToString(FileUtils.toFile(InstallationDocumentModelTest.class.getResource("/model/ChangeFilterTest.xml")));
		InstallationDocument input = (InstallationDocument) XmlHelper.unmarshall(XmlHelper.parseDocumentElement(xmlData), InstallationDocument.class)
				.getValue();

		UserInput userInput = new UserInput(1, 2, Environment.PRODUCTION.value());
		InstallationDocument result = new ChangeFilter().on(XmlHelper.parseDocumentElement(xmlData)).with(userInput).apply();

		assertEquals("Element from input file with index 0 is still on position 0 in result file", input.getInstallationBlock().get(0).getChange().get(0)
				.getTestCycleNumber(), BigInteger.valueOf(1));

		assertSame("Second change from input file with index 1 is not any more in result file", result.getInstallationBlock().get(0).getInstallationBlock()
				.get(0).getChange().size(), 0);
	}
	
	//TODO Gleiches Target, verschiedene Sourcen.
	@Test
	public void testChangeWithDifferent() throws IOException, RemindModelException
	{
		
	}

	@Test
	public void testSortWithManyChanges() throws IOException, RemindModelException
	{
		String xmlData = FileUtils
				.readFileToString(FileUtils.toFile(InstallationDocumentModelTest.class.getResource("/model/ChangeFilterMoreChangesTest.xml")));
		InstallationDocument input = (InstallationDocument) XmlHelper.unmarshall(XmlHelper.parseDocumentElement(xmlData), InstallationDocument.class)
				.getValue();

		UserInput userInput = new UserInput(1, 2, Environment.DEV.value());
		InstallationDocument result = new ChangeFilter().on(XmlHelper.parseDocumentElement(xmlData)).with(userInput).apply();

		Change firstChangeFromResult = result.getInstallationBlock().get(0).getChange().get(0);
		Change secondChangeFromInput = input.getInstallationBlock().get(0).getChange().get(1);
		assertTrue(
				"Element from input file with index 1 (revision number 3) is now on position 0 in result file",
				firstChangeFromResult.getSource().getSourceCodeManagement().getSubversion().getRevision()
						.equals(secondChangeFromInput.getSource().getSourceCodeManagement().getSubversion().getRevision()));

	}

	@Test
	public void testSortWithManyChangesAndParentExchange() throws IOException, RemindModelException
	{
		String xmlData = FileUtils.readFileToString(FileUtils.toFile(InstallationDocumentModelTest.class
				.getResource("/model/ChangeFilterParentExchangeTest.xml")));
		InstallationDocument input = (InstallationDocument) XmlHelper.unmarshall(XmlHelper.parseDocumentElement(xmlData), InstallationDocument.class)
				.getValue();

		UserInput userInput = new UserInput(1, 3, Environment.DEV.value());
		InstallationDocument result = new ChangeFilter().on(XmlHelper.parseDocumentElement(xmlData)).with(userInput).apply();

		Change firstChangeFromResult = result.getInstallationBlock().get(0).getChange().get(0);
		Change fourthChangeFromInput = input.getInstallationBlock().get(0).getInstallationBlock().get(0).getInstallationBlock().get(0).getChange().get(0);
		assertTrue(
				"Element from input file with index 3 (revision number 5) is now on position 0 in result file",
				firstChangeFromResult.getSource().getSourceCodeManagement().getSubversion().getRevision()
						.equals(fourthChangeFromInput.getSource().getSourceCodeManagement().getSubversion().getRevision()));

		Change fourthChangeFromResult = result.getInstallationBlock().get(0).getInstallationBlock().get(0).getInstallationBlock().get(0).getChange().get(0);
		Change firstChangeFromInput = input.getInstallationBlock().get(0).getChange().get(0);
		assertTrue(
				"Element from input file with index 0 (revision number 1) is now on position 3 in result file",
				fourthChangeFromResult.getSource().getSourceCodeManagement().getSubversion().getRevision()
						.equals(firstChangeFromInput.getSource().getSourceCodeManagement().getSubversion().getRevision()));
	}

	@Test
	public void testSortWithManyChangesAndParentExchangeWithOutOfScopeMembers1() throws IOException, RemindModelException
	{
		String xmlData = FileUtils.readFileToString(FileUtils.toFile(InstallationDocumentModelTest.class
				.getResource("/model/ChangeFilterParentExchangeTest.xml")));
		InstallationDocument input = (InstallationDocument) XmlHelper.unmarshall(XmlHelper.parseDocumentElement(xmlData), InstallationDocument.class)
				.getValue();

		UserInput userInput = new UserInput(1, 2, Environment.DEV.value());
		InstallationDocument result = new ChangeFilter().on(XmlHelper.parseDocumentElement(xmlData)).with(userInput).apply();

		Change firstChangeFromResult = result.getInstallationBlock().get(0).getChange().get(0);
		Change fourthChangeFromInput = input.getInstallationBlock().get(0).getInstallationBlock().get(0).getChange().get(0);
		assertTrue(
				"Element from input file with index 2 (revision number 4) is now on position 0 in result file",
				firstChangeFromResult.getSource().getSourceCodeManagement().getSubversion().getRevision()
						.equals(fourthChangeFromInput.getSource().getSourceCodeManagement().getSubversion().getRevision()));

	}

	@Test
	public void testSortWithManyChangesAndParentExchangeWithOutOfScopeMembers2() throws IOException, RemindModelException
	{
		String xmlData = FileUtils.readFileToString(FileUtils.toFile(InstallationDocumentModelTest.class
				.getResource("/model/ChangeFilterWithOutOfScopeMembersTest.xml")));
		InstallationDocument input = (InstallationDocument) XmlHelper.unmarshall(XmlHelper.parseDocumentElement(xmlData), InstallationDocument.class)
				.getValue();

		UserInput userInput = new UserInput(1, 2, Environment.DEV.value());
		InstallationDocument result = new ChangeFilter().on(XmlHelper.parseDocumentElement(xmlData)).with(userInput).apply();

		Change firstChangeFromResult = result.getInstallationBlock().get(0).getChange().get(0);
		Change thirdChangeFromInput = input.getInstallationBlock().get(0).getChange().get(2);
		assertTrue(
				"Element from input file with index 2 (revision number 4) is now on position 0 in result file",
				firstChangeFromResult.getSource().getSourceCodeManagement().getSubversion().getRevision()
						.equals(thirdChangeFromInput.getSource().getSourceCodeManagement().getSubversion().getRevision()));

		assertSame("input file have 6 changes", input.getInstallationBlock().get(0).getChange().size(), 6);

		assertSame("because 3 changes from input file are out of scope from user input, we will have only 3 changes in new installation document", result
				.getInstallationBlock().get(0).getChange().size(), 3);

	}

	@Test
	public void test() throws IOException, RemindModelException
	{
		String xmlData = FileUtils.readFileToString(FileUtils.toFile(InstallationDocumentModelTest.class.getResource("/model/ChangeFilterTestFlat.xml")));
		InstallationDocument input = (InstallationDocument) XmlHelper.unmarshall(XmlHelper.parseDocumentElement(xmlData), InstallationDocument.class)
				.getValue();

		UserInput userInput = new UserInput(0, 100, Environment.QM.value());
		InstallationDocument result = new ChangeFilter().on(XmlHelper.parseDocumentElement(xmlData)).with(userInput).apply();

		assertSame("input file have 6 changes", input.getInstallationBlock().get(0).getChange().size(), 4);

		assertSame("because 3 changes from input file are out of scope from user input, we will have only 3 changes in new installation document", result
				.getInstallationBlock().get(0).getChange().size(), 4);
	}

	@Test
	public void testSortWithManyChangesAndParentExchangeWithNegativeRevision() throws IOException, RemindModelException
	{
		String xmlData = FileUtils.readFileToString(FileUtils.toFile(InstallationDocumentModelTest.class
				.getResource("/model/ChangeFilterParentExchangeWithNegativeRevisionTest.xml")));
		InstallationDocument input = (InstallationDocument) XmlHelper.unmarshall(XmlHelper.parseDocumentElement(xmlData), InstallationDocument.class)
				.getValue();

		UserInput userInput = new UserInput(0, 3, Environment.DEV.value());
		InstallationDocument result = new ChangeFilter().on(XmlHelper.parseDocumentElement(xmlData)).with(userInput).apply();

		Change firstChangeFromResult = result.getInstallationBlock().get(0).getChange().get(0);
		Change thirdChangeFromInput = input.getInstallationBlock().get(0).getInstallationBlock().get(0).getChange().get(0);
		assertTrue(
				"Element from input file with index 2 (revision number -1) is now on position 0 in result file",
				firstChangeFromResult.getSource().getSourceCodeManagement().getSubversion().getRevision()
						.equals(thirdChangeFromInput.getSource().getSourceCodeManagement().getSubversion().getRevision()));

		Change thirdChangeFromResult = result.getInstallationBlock().get(0).getInstallationBlock().get(0).getChange().get(0);
		Change fourthChangeFromInput = input.getInstallationBlock().get(0).getInstallationBlock().get(0).getInstallationBlock().get(0).getChange().get(0);
		assertTrue(
				"Element from input file with index 3 (revision number 5) is now on position 2 in result file",
				thirdChangeFromResult.getSource().getSourceCodeManagement().getSubversion().getRevision()
						.equals(fourthChangeFromInput.getSource().getSourceCodeManagement().getSubversion().getRevision()));

		Change fourthChangeFromResult = result.getInstallationBlock().get(0).getInstallationBlock().get(0).getInstallationBlock().get(0).getChange().get(0);
		Change firstChangeFromInput = input.getInstallationBlock().get(0).getChange().get(0);
		assertTrue(
				"Element from input file with index 0 (revision number 1) is now on position 3 in result file",
				fourthChangeFromResult.getSource().getSourceCodeManagement().getSubversion().getRevision()
						.equals(firstChangeFromInput.getSource().getSourceCodeManagement().getSubversion().getRevision()));
	}
}
