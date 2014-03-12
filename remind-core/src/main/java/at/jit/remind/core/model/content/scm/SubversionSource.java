package at.jit.remind.core.model.content.scm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.SVNException;

import at.jit.remind.core.connector.scm.SVNConnector;
import at.jit.remind.core.connector.scm.SVNConnector.SvnFileNotFoundException;
import at.jit.remind.core.context.PropertiesProvider;
import at.jit.remind.core.context.RemindCompositeKeyProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.RemindModelFeedback;
import at.jit.remind.core.model.content.Source;
import at.jit.remind.core.model.content.Validate;

public class SubversionSource implements Validate, Source<File>
{
	private static final String repositoryUrlMappedKey = "repositoryUrl";

	private static final String userMappedKey = "user";
	private static final String passwordMappedKey = "password";

	public static final String globalRepositoryUrl = "globalRepository";
	private static final String headRevision = "-1";

	private SVNConnector connector;
	private String repositoryUrl;
	private String path;
	private String revision;
	private String repositoryRevision;

	private String user;

	public static final RemindCompositeKeyProvider subversionKeyProvider = new RemindCompositeKeyProvider(SubversionSource.class.getSimpleName(),
			new String[]{repositoryUrlMappedKey}, new String[]{userMappedKey, passwordMappedKey});

	static
	{
		subversionKeyProvider.markContentAsEncrypted(passwordMappedKey);
	}

	public SubversionSource(String repositoryUrl, String revision, String path)
	{
		this.repositoryUrl = (repositoryUrl != null ? repositoryUrl : "");
		this.repositoryUrl = this.repositoryUrl.replace("\\", "/");

		this.path = (path != null ? path : "");
		this.path = this.path.replace("\\", "/");

		this.revision = (revision != null ? revision : "");
		this.repositoryRevision = this.revision;

		PropertiesProvider propertiesProvider = RemindContext.getInstance().getPropertiesProvider();

		Map<String, String> lookupKeyMapping = new HashMap<String, String>();
		lookupKeyMapping.put(repositoryUrlMappedKey, this.repositoryUrl);
		Properties connectionProperties = propertiesProvider.getPossiblyEmptyProperties(subversionKeyProvider.getLookupId(lookupKeyMapping));

		Map<String, String> globalKeyMapping = new HashMap<String, String>();
		globalKeyMapping.put(repositoryUrlMappedKey, globalRepositoryUrl);
		Properties globalConnectionProperties = propertiesProvider.getPossiblyEmptyProperties(subversionKeyProvider.getLookupId(globalKeyMapping));

		user = connectionProperties.getProperty(userMappedKey, globalConnectionProperties.getProperty(userMappedKey, ""));
		String password = connectionProperties.getProperty(passwordMappedKey, globalConnectionProperties.getProperty(passwordMappedKey, ""));

		connector = new SVNConnector(user, password);
	}

	@Override
	public File retrieve() throws MessageHandlerException
	{
		File tmpFile;
		try
		{
			tmpFile = File.createTempFile(getClass().getSimpleName(), ".tmp");
			tmpFile.deleteOnExit();
		}
		catch (IOException e)
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file").build();
			Feedback feedback = RemindContext.getInstance().getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "Temp file could not be created or found.", e.getMessage(), feedbackContext);

			throw new MessageHandlerException(e, feedback);
		}

		try
		{
			RemindContext
					.getInstance()
					.getMessageHandler()
					.addMessage(
							"Retrieving data from SVN: repositoryUrl=" + repositoryUrl + ", path=" + path + ", revision=" + revision + ", repositoryRevision="
									+ repositoryRevision + ", user=" + user);
			connector.exportToFile(repositoryUrl, path, repositoryRevision, tmpFile);
		}
		catch (SVNException e)
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file").build();
			Feedback feedback = RemindContext
					.getInstance()
					.getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "SVN error occurred\nCheck revision number and if svn:keywords are set",
							e.getMessage(), feedbackContext);

			throw new MessageHandlerException(e, feedback);
		}
		catch (FileNotFoundException e)
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file").build();
			Feedback feedback = RemindContext.getInstance().getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "Temp file could not be created or found.", e.getMessage(), feedbackContext);

			throw new MessageHandlerException(e, feedback);
		}
		RemindContext.getInstance().getMessageHandler().addMessage("Successfully retrieved data from SVN.");

		return tmpFile;
	}

	@Override
	public void validate() throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler()
				.addMessage("Checking SVN connection: repositoryUrl=" + repositoryUrl + ", path=" + path + ", revision=" + revision + ", user=" + user);

		if ("".equals(repositoryUrl) || "".equals(path) || "".equals(revision))
		{
			RemindContext.getInstance().getMessageHandler()
					.addMessage(MessageLevel.ERROR, "Error while checking SVN connection!", "Repository url, path or revision is not set.");

			throw new MessageHandlerException("Repository url, path or revision is not set.");
		}

		try
		{
			retrieveRevision();
		}
		catch (MessageHandlerException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "Error while checking SVN connection!", e.getMessage());

			throw new MessageHandlerException(e);
		}

		RemindContext.getInstance().getMessageHandler().addMessage("SVN connection checked successfully.");
	}

	private void retrieveRevision() throws MessageHandlerException
	{
		try
		{
			repositoryRevision = String.valueOf(connector.retrieveRevision(repositoryUrl, path, revision));
		}
		catch (SVNException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "Error while retrieving file from repository!", e.getMessage());

			throw new MessageHandlerException(e);
		}
		catch (SvnFileNotFoundException e)
		{
			RemindContext.getInstance().getMessageHandler()
					.addMessage(MessageLevel.ERROR, "Error while retrieving file from repository!", "Cannot find specified file!");

			throw new MessageHandlerException("Cannot find specified file!");
		}
	}

	@Override
	public String getName()
	{
		String[] pathSegments = StringUtils.split(path, '/');

		StringBuilder b = new StringBuilder();
		for (int i = Math.max(0, pathSegments.length - 2); i < pathSegments.length; ++i)
		{
			b.append('/').append(pathSegments[i]);
		}

		b.append(" @revision: ").append(repositoryRevision);

		return b.toString();
	}

	@Override
	public String getDetails()
	{
		StringBuilder b = new StringBuilder();
		b.append(subversionKeyProvider.getType());
		b.append("[").append(repositoryUrlMappedKey).append("=").append("'" + repositoryUrl + "' ");
		b.append("path").append("=").append("'" + path + "' ");
		b.append("revision").append("=").append("'" + repositoryRevision + "'").append("]");

		return b.toString();
	}

	// TODO: use other/own interface
	@Override
	public int compareTo(Source<?> o)
	{
		SubversionSource that = (SubversionSource) o;

		if (this.revision.equals(that.revision))
		{
			return 0;
		}

		if (headRevision.equals(that.revision))
		{
			return 1;
		}

		if (headRevision.equals(this.revision))
		{
			return -1;
		}

		return Integer.valueOf(that.repositoryRevision).compareTo(Integer.valueOf((this.repositoryRevision)));
	}

	@Override
	public boolean isAlmostEqual(Source<?> o)
	{
		if (!(o instanceof SubversionSource))
		{
			return false;
		}
			
		SubversionSource that = (SubversionSource) o;

		return this.path.equals(that.path) && this.repositoryUrl.equals(that.repositoryUrl);
	}
}
