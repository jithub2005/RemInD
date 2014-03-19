package at.jit.remind.core.model.content.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.RemindModelFeedback;
import at.jit.remind.core.model.content.Source;
import at.jit.remind.core.model.content.Target;
import at.jit.remind.core.util.CharsetDetector;

public class FileSystemLocation implements Source<File>, Target<File, File>
{
//	public static final String charset = "UTF-8";

	private File file;
	private boolean isSource = false;
	private CharsetDetector charsetDetector = new CharsetDetector();

	private Set<String> deploymentDetails = new LinkedHashSet<String>();

	// we need to distinguish between source and target because of "validate".
	public FileSystemLocation(String path, boolean isSource)
	{
		file = new File(path);
		this.isSource = isSource;
	}

	@Override
	public File retrieve() throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().addMessage("Retrieving file: " + file.getName());
		if (file.exists() && file.canRead())
		{
			return file;
		}
		else
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file").build();
			Feedback feedback = RemindContext.getInstance().getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "Error while retrieving file!", "File does not exist or is not readable!", feedbackContext);

			throw new MessageHandlerException("File does not exist or is not readable!", feedback);
		}
	}

	@Override
	public void validate() throws MessageHandlerException
	{
		if (isSource)
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Checking if file " + file.getAbsoluteFile() + " exists and is readable.");
			if (!file.exists() || !file.canRead())
			{
				RemindContext.getInstance().getMessageHandler()
						.addMessage(MessageLevel.ERROR, "Error validating file " + file.getAbsolutePath() + ".", "File does not exist or is not readable!");

				throw new MessageHandlerException("File does not exist or is not readable!");
			}
			RemindContext.getInstance().getMessageHandler().addMessage("File " + file.getAbsoluteFile() + " validated successfully.");
		}
		else
		{
			try
			{
				RemindContext.getInstance().getMessageHandler().addMessage("Validating file " + file.getAbsoluteFile());
				boolean createdSucessfully = file.createNewFile();
				boolean deletedSuccessfully = file.delete();

				if (!createdSucessfully)
				{
					RemindContext
							.getInstance()
							.getMessageHandler()
							.addMessage(MessageLevel.WARNING, "Validation file aready exists.",
									"The validation file already exists. In this case the validation's returned result is not guaranteed to be accurate.");
				}
				if (!deletedSuccessfully)
				{
					RemindContext
							.getInstance()
							.getMessageHandler()
							.addMessage(MessageLevel.WARNING, "Validation file was not able to delete.",
									"The validation file was not able to delete. This could cause further validation results to be not accurate.");
				}
				RemindContext.getInstance().getMessageHandler().addMessage("File " + file.getAbsoluteFile() + " validated successfully.");
			}
			catch (IOException e)
			{
				RemindContext.getInstance().getMessageHandler()
						.addMessage(MessageLevel.ERROR, "Error validating file " + file.getAbsolutePath() + ".", e.getMessage());

				throw new MessageHandlerException("Cannot write into this directory!", e);
			}
		}
	}

	@Override
	public File convert(Source<File> source) throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().addMessage("Converting file: " + file.getName());
		return source.retrieve();
	}

	@Override
	public void deploy(File content) throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().addMessage("Deploying file: " + file.getName());
		if (content == null)
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file").build();
			Feedback feedback = RemindContext.getInstance().getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "Error while reading file!", "File does not exist!", feedbackContext);

			throw new MessageHandlerException("File does not exist!", feedback);
		}
		else
		{
			BufferedReader reader = null;
			BufferedWriter out = null;
			
			try
			{
				String charset = charsetDetector.detectCharset(content);
				
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(content), charset));
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));

				String line = null;
				while ((line = reader.readLine()) != null)
				{
					out.write(line + "\n");
				}
			}
			catch (IOException e)
			{
				FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
						.withFeedback(RemindModelFeedback.Skip, "Skip file").build();
				Feedback feedback = RemindContext.getInstance().getMessageHandler()
						.addMessageWithFeedback(MessageLevel.ERROR, "Error while reading or writing file!", e.getMessage(), feedbackContext);

				throw new MessageHandlerException(e, feedback);
			}
			finally
			{
				try
				{
					if (reader != null)
					{
						reader.close();
					}
				}
				catch (IOException e)
				{
					RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "BufferedReader could not be closed.", e.getMessage());
				}
				try
				{
					if (out != null)
					{
						out.close();
					}
				}
				catch (IOException e)
				{
					RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "BufferedWriter could not be closed.", e.getMessage());
				}
			}
		}

		RemindContext.getInstance().getMessageHandler().addMessage("File " + file.getName() + " deployed successfully.");
	}

	@Override
	public Set<String> getDeploymentDetails()
	{
		return deploymentDetails;
	}

	@Override
	public String getName()
	{
		return file.getAbsolutePath();
	}

	@Override
	public String getDetails()
	{
		return "FileSystemLocation[isSource='" + isSource + "' path='" + file.getAbsolutePath() + "']";
	}

	@Override
	public int hashCode()
	{
		return getDetails().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FileSystemLocation))
		{
			return false;
		}

		return getDetails().equals(((FileSystemLocation) obj).getDetails());
	}

	@Override
	public int compareTo(Source<?> o)
	{
		// there is no revision for file system location, so the file is always the same.
		return 0;
	}

	@Override
	public boolean isAlmostEqual(Source<?> that)
	{
		return this.equals(that);
	}
}
