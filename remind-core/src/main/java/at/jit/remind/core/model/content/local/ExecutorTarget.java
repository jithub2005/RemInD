package at.jit.remind.core.model.content.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.RemindModelFeedback;
import at.jit.remind.core.model.content.Source;
import at.jit.remind.core.model.content.Target;

public class ExecutorTarget implements Target<File, File>
{
	public static final String charset = "UTF-8";

	private String environment;

	private String workingPath;
	private String[] splitCommand;
	private String executable;

	private Set<String> deploymentDetails = new LinkedHashSet<String>();

	public ExecutorTarget(String environment, String workingPath, String command)
	{
		this.workingPath = workingPath;
		this.environment = environment;

		splitCommand = command.split(" ");
		executable = splitCommand.length > 0 ? splitCommand[splitCommand.length - 1] : "";
	}

	@Override
	public File convert(Source<File> file) throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().addMessage("Converting file: " + file.getName());

		return file.retrieve();
	}

	@Override
	public String getName()
	{
		return environment + ":" + workingPath + ":" + executable;
	}

	@Override
	public String getDetails()
	{
		StringBuffer b = new StringBuffer();
		b.append(ExecutorTarget.class.getSimpleName());
		b.append("[environment='").append(environment).append("' ");
		b.append("workingPath='").append(workingPath).append("' ");
		b.append("executable='").append(executable).append("']");

		return b.toString();
	}

	@Override
	public void deploy(File content) throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().addMessage("Deploying file: " + content.getName());

		try
		{
			String[] splitCommandWithArgument = Arrays.copyOf(splitCommand, splitCommand.length + 2);
			splitCommandWithArgument[splitCommand.length] = environment;
			splitCommandWithArgument[splitCommand.length + 1] = content.getAbsolutePath();

			File workingDirectory = new File(workingPath);
			RemindContext
					.getInstance()
					.getMessageHandler()
					.addMessage(
							"Starting process with command array " + Arrays.toString(splitCommandWithArgument) + " in directory "
									+ workingDirectory.getAbsolutePath());

			ProcessBuilder builder = new ProcessBuilder(splitCommandWithArgument).directory(workingDirectory).redirectErrorStream(true);
			final Process process = builder.start();
			final CountDownLatch latch = new CountDownLatch(1);

			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						process.waitFor();
						latch.countDown();
					}
					catch (InterruptedException e)
					{
						// TODO: handle properly
					}
				}
			}).start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			try
			{
				while ((line = reader.readLine()) != null) // NOSONAR
				{
					RemindContext.getInstance().getMessageHandler().addMessage(line);
				}
			}
			catch (IOException e)
			{
				FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
						.withFeedback(RemindModelFeedback.Skip, "Skip file").build();
				Feedback feedback = RemindContext
						.getInstance()
						.getMessageHandler()
						.addMessageWithFeedback(MessageLevel.ERROR, "Access to output stream of " + executable + " aborted unexpectedly", e.getMessage(),
								feedbackContext);

				throw new MessageHandlerException(e, feedback);
			}
			finally
			{
				reader.close();
			}

			try
			{
				latch.await();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			RemindContext.getInstance().getMessageHandler().addMessage("Process " + executable + " returned " + process.exitValue());
		}
		catch (IOException e)
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file").build();
			Feedback feedback = RemindContext.getInstance().getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "Failed to start process " + executable, e.getMessage(), feedbackContext);

			throw new MessageHandlerException(e, feedback);
		}
	}

	@Override
	public Set<String> getDeploymentDetails()
	{
		return deploymentDetails;
	}

	@Override
	public void validate() throws MessageHandlerException
	{
		File workingDirectory = new File(workingPath);

		RemindContext.getInstance().getMessageHandler().addMessage("Checking if directory " + workingDirectory.getAbsolutePath() + " is accessible.");
		if (!workingDirectory.exists() || !workingDirectory.canRead())
		{
			RemindContext
					.getInstance()
					.getMessageHandler()
					.addMessage(MessageLevel.ERROR, "Error validating directory " + workingDirectory.getAbsolutePath() + ".",
							"Directory does not exist or is not readable!");

			throw new MessageHandlerException("Directory does not exist or is not readable!");
		}

		File executableFile = new File(workingPath + "/" + executable);
		RemindContext.getInstance().getMessageHandler().addMessage("Checking if file " + executableFile.getAbsolutePath() + " is executable.");
		if (!executableFile.exists() || !executableFile.canExecute())
		{
			RemindContext
					.getInstance()
					.getMessageHandler()
					.addMessage(MessageLevel.ERROR, "Error validating executable " + executableFile.getAbsolutePath() + ".",
							"File does not exist or is not executable!");

			throw new MessageHandlerException("File does not exist or is not executable!");
		}

		RemindContext.getInstance().getMessageHandler().addMessage("File " + executableFile.getAbsoluteFile() + " validated successfully.");
	}
}
