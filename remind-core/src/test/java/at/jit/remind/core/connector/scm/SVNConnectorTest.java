package at.jit.remind.core.connector.scm;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import at.jit.remind.core.connector.scm.SVNConnector.SvnFileNotFoundException;

public class SVNConnectorTest
{
	private static final String repositoryUrl = "http://dev.jit.at/svn/remind-testing";

	private Map<String, SVNRepository> connectionMap;
	private String user = "remind-test";
	private String pwd = "pwd";

	private SVNConnector svnConnector;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception
	{
		svnConnector = new SVNConnector(user, pwd);
		Field connectionMapField = SVNConnector.class.getDeclaredField("connectionMap");
		connectionMapField.setAccessible(true);
		connectionMap = (Map<String, SVNRepository>) connectionMapField.get(svnConnector);
	}

	@After
	public void tearDown() throws Exception
	{
		svnConnector.closeSVNConnections();
	}

	@Test
	public void connectionMap() throws SVNException
	{
		assertSame("ConnectionMap must be empty", connectionMap.size(), 0);

		svnConnector.getAsString(repositoryUrl, "trunk/dbFiles/dummy.sql", "10");
		assertTrue("ConnectionMap contains the new url", connectionMap.containsKey(repositoryUrl));
		assertSame("Size must be 1", connectionMap.size(), 1);

		svnConnector.getAsString(repositoryUrl, "trunk/dbFiles/dummy2.sql", "10");
		assertSame("Size must still be 1", connectionMap.size(), 1);

		svnConnector.getAsString(repositoryUrl + "/trunk", "dbFiles/dummy2.sql", "10");
		assertTrue("ConnectionMap contains new url", connectionMap.containsKey(repositoryUrl + "/trunk"));
		assertSame("Size must now be 2", connectionMap.size(), 2);

		svnConnector.closeSVNConnections();
		assertTrue("ConnectionMap must now be empty", connectionMap.isEmpty());
	}

	@Test(expected = SVNException.class)
	public void fileOrRevisionNotFound() throws SVNException
	{
		svnConnector.getAsString(repositoryUrl, "trunk/dbFiles/dummy10.sql", "10");
	}

	@Test
	public void testSvnKeywords() throws IOException, SVNException
	{
		File tempFile = File.createTempFile("JUnit", getClass().getSimpleName() + ".testSvnKeywords");
		tempFile.deleteOnExit();

		svnConnector.exportToFile(repositoryUrl, "trunk/test.txt", "30", tempFile);

		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(tempFile));
			String line = reader.readLine();
			assertTrue("$Rev:$ is expanded to $Rev: 30 $", line.contains("$Rev: 30 $"));
		}
		finally
		{
			reader.close();
		}
	}

	@Test
	public void testRetrieveRevision() throws IOException, SVNException, SvnFileNotFoundException
	{
		long revision = svnConnector.retrieveRevision(repositoryUrl, "trunk/test.txt", "31");
		assertSame("Revision 31 of file trunk/test exists: retrieved revision is 31", revision, Long.valueOf(31));

		revision = svnConnector.retrieveRevision(repositoryUrl, "trunk/test.txt", "32");
		assertSame("Revision 32 of file trunk/test does not exist: retrieved revision is 31", revision, Long.valueOf(31));
	}

	@Test(expected = SvnFileNotFoundException.class)
	public void testRetrieveRevisionWithWrongPath() throws IOException, SVNException, SvnFileNotFoundException
	{
		svnConnector.retrieveRevision(repositoryUrl, "tunk/test.txt", "-1");
	}
}
