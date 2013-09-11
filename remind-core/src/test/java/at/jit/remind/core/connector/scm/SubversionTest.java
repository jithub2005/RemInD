package at.jit.remind.core.connector.scm;

import java.io.ByteArrayOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;

public class SubversionTest
{
	private static final String TestFilePath = "trunk/test.txt";

	private static final String TestFileExpectedFirstLineRev5 = "First line";
	private static final String TestFileExpectedSecondLineRev5 = "Second line";

	private SVNRepository svnRepository;
	private SVNClientManager svnClientManager;

	@Before
	public void setUp() throws Exception
	{
		DAVRepositoryFactory.setup();

		DefaultSVNOptions svnOptions = new DefaultSVNOptions();
		svnClientManager = SVNClientManager.newInstance(svnOptions, "remind-test", "pwd");
		svnRepository = svnClientManager.createRepository(SVNURL.parseURIDecoded("http://dev.jit.at/svn/remind-testing"), true);
	}

	@After
	public void tearDown() throws Exception
	{
		svnRepository.closeSession();
		svnClientManager.dispose();
	}

	@Test
	public void test() throws SVNException
	{
		// check existence
		svnRepository.checkPath(TestFilePath, 4);

		// checkout file: revision 4 should be empty
		SVNProperties svnProperties = new SVNProperties();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		svnRepository.getFile(TestFilePath, 4, svnProperties, baos);
		Assert.assertTrue(baos.size() == 0);

		// revision 5 should contain lines Subversion.TestFileExpectedFirstLineRev5 and
		// Subversion.TestFileExpectedSecondLineRev5
		svnRepository.getFile(TestFilePath, 5, svnProperties, baos);
		Assert.assertTrue(baos.size() != 0);
		String contentRev5 = baos.toString();
		Assert.assertTrue((TestFileExpectedFirstLineRev5 + "\n" + TestFileExpectedSecondLineRev5).equals(contentRev5));

		// newest revision ends with "newest"
		svnRepository.getFile(TestFilePath, -1, svnProperties, baos);
		Assert.assertTrue(baos.size() != 0);
		Assert.assertTrue(baos.toString().endsWith("newest"));
	}
}
