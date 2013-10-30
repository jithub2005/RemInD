package at.jit.remind.core.connector.scm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

public class SVNConnector
{
	private Map<String, SVNRepository> connectionMap = new HashMap<String, SVNRepository>();
	private SVNClientManager svnClientManager;

	static
	{
		DAVRepositoryFactory.setup();
	}

	public SVNConnector(String user, String pwd)
	{
		DefaultSVNOptions svnOptions = new DefaultSVNOptions();
		svnClientManager = SVNClientManager.newInstance(svnOptions, user, pwd);
	}

	public String getAsString(String repositoryUrl, String path, String revision) throws SVNException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SVNRepository svnRepository = getRepository(repositoryUrl);
		SVNProperties svnProperties = new SVNProperties();
		svnRepository.getFile(path, Long.parseLong(revision), svnProperties, baos);

		return baos.toString();
	}

	public void exportToFile(String repositoryUrl, String path, String revision, File targetFile) throws SVNException, FileNotFoundException
	{
		SVNRepository svnRepository = getRepository(repositoryUrl);
		SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		updateClient.setExportExpandsKeywords(true);
		updateClient.doExport(svnRepository.getLocation().appendPath(path, false), targetFile, SVNRevision.create(Long.valueOf(-1)),
				SVNRevision.create(Long.valueOf(revision)), null, true, SVNDepth.INFINITY);
	}

	public void closeSVNConnections()
	{
		for (SVNRepository svnRepository : connectionMap.values())
		{
			svnRepository.closeSession();
		}

		connectionMap.clear();

		if (svnClientManager != null)
		{
			svnClientManager.dispose();
		}
	}

	private SVNRepository getRepository(String repositoryUrl) throws SVNException
	{
		String trimmedRepositoryUrl = repositoryUrl.trim();

		if (connectionMap.containsKey(trimmedRepositoryUrl))
		{
			return connectionMap.get(trimmedRepositoryUrl);
		}

		SVNRepository svnRepository = svnClientManager.createRepository(SVNURL.parseURIDecoded(trimmedRepositoryUrl), false);
		connectionMap.put(trimmedRepositoryUrl, svnRepository);

		return svnRepository;
	}

	public long retrieveRevision(String repositoryUrl, String path, String revision) throws SVNException, SvnFileNotFoundException
	{
		SVNRepository svnRepository = getRepository(repositoryUrl);
		SVNDirEntry entry = svnRepository.info(path, Long.parseLong(revision));

		if (entry == null)
		{
			throw new SvnFileNotFoundException();
		}

		return entry.getRevision();
	}

	public static final class SvnFileNotFoundException extends Exception
	{
		private static final long serialVersionUID = 1194869052730167045L;
	}
}
