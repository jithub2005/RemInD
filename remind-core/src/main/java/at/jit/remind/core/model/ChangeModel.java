package at.jit.remind.core.model;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.CoreActionType;
import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.context.reporting.DeploymentInformation;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.content.Source;
import at.jit.remind.core.model.content.Target;
import at.jit.remind.core.model.content.database.DatabaseTarget;
import at.jit.remind.core.model.content.io.FileSystemLocation;
import at.jit.remind.core.model.content.local.ExecutorTarget;
import at.jit.remind.core.model.content.scm.SubversionSource;
import at.jit.remind.core.model.status.State;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.Environment;
import at.jit.remind.core.xml.Executor;
import at.jit.remind.core.xml.SourceCodeManagement;
import at.jit.remind.core.xml.Subversion;

public class ChangeModel extends RemindModelBase<Change, ChangeModel>
{
	@SuppressWarnings("rawtypes")
	private Source source;
	@SuppressWarnings("rawtypes")
	private Map<String, Target> envTargetMap;

	@SuppressWarnings("rawtypes")
	public ChangeModel()
	{
		super(Change.class);

		source = null;
		envTargetMap = new LinkedHashMap<String, Target>();
	}

	@Override
	public void update(Change element) throws RemindModelException
	{
		setElement(element);

		// TODO Use a factory
		if (element.getSource().getFileSystem() != null)
		{
			source = new FileSystemLocation(element.getSource().getFileSystem().getPath(), true);
		}
		// TODO close SVNConnector connections.
		SourceCodeManagement sourceCodeManagement = element.getSource().getSourceCodeManagement();
		if (sourceCodeManagement != null && sourceCodeManagement.getSubversion() != null)
		{
			Subversion s = sourceCodeManagement.getSubversion();
			source = new SubversionSource(s.getRepositoryUrl(), s.getRevision(), s.getPath());
		}

		for (Environment environment : element.getEnvironment())
		{
			// Obviously the unmarshaller inserts null values if the environment values doesn't match
			// with the proper diction, so the foreach loop below iterates over null values! That's why there is a
			// null check now.
			if (environment == null)
			{
				throw new RemindModelException("Environment doesn't match. Is the spelling correct?");
			}

			if (element.getTarget().getDatabase() != null)
			{
				envTargetMap.put(environment.value(), new DatabaseTarget(environment.value(), element.getTarget().getDatabase().getSID(), element.getTarget()
						.getDatabase().getSchema()));

				continue;
			}

			if (element.getTarget().getFileSystem() != null)
			{
				envTargetMap.put(environment.value(), new FileSystemLocation(element.getTarget().getFileSystem().getPath(), false));

				continue;
			}

			if (element.getTarget().getExecutor() != null)
			{
				Executor executor = element.getTarget().getExecutor();
				envTargetMap.put(environment.value(), new ExecutorTarget(environment.value(), executor.getWorkingPath(), executor.getCommand()));
			}
		}

		setName("TC " + element.getTestCycleNumber() + ": S: " + source.getName());
		// TODO: set identifier

		getDeploymentInformation().setTestCycleNumber(getElement().getTestCycleNumber());
		getDeploymentInformation().setSourceInfo(source.getDetails());
		getDeploymentInformation().setDeveloper(getElement().getDeveloper());
		fill(getDeploymentInformation());
	}

	@Override
	public void validate()
	{
		try
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Validating " + getName());
			RemindContext.getInstance().getMessageHandler().addMessage("Validating source " + source.getName());
			source.validate();
			RemindContext.getInstance().getMessageHandler().addMessage("Source " + source.getName() + " validated successfully.");

			for (Environment environment : getElement().getEnvironment())
			{
				@SuppressWarnings("rawtypes")
				Target target = envTargetMap.get(environment.value());
				if (target == null)
				{
					throw new MessageHandlerException("Environment " + environment.value() + " does not exist as property!");
				}
				else
				{
					RemindContext.getInstance().getMessageHandler().addMessage("Validating target " + target.getName());
					target.validate();
					RemindContext.getInstance().getMessageHandler().addMessage("Target " + target.getName() + " validated successfully.");
				}
			}
			setState(State.Ok);
			RemindContext.getInstance().getMessageHandler().addMessage(getName() + " validated successfully.");
		}
		catch (MessageHandlerException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Error while validating " + getName());
			setState(State.Error);
		}
	}

	// This validate method is used if and only if deploy has been executed
	@Override
	protected void validate(UserInput userInput) throws MessageHandlerException
	{
		if (!appliesFor(userInput))
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Skipping validation of " + getName() + " as it does not apply to user input.");
			return;
		}

		try
		{
			setRange(RemindContext.getInstance().getMessageHandler().openRange());

			RemindContext.getInstance().getMessageHandler().addMessage("Validating " + getName());
			RemindContext.getInstance().getMessageHandler().addMessage("Validating Source " + source.getName());
			source.validate();
			RemindContext.getInstance().getMessageHandler().addMessage("Source " + source.getName() + " validated successfully.");

			@SuppressWarnings("rawtypes")
			Target target = envTargetMap.get(userInput.getEnvironment());
			if (target == null)
			{
				throw new MessageHandlerException("Environment " + userInput.getEnvironment() + " does not exist as property!");
			}
			else
			{
				RemindContext.getInstance().getMessageHandler().addMessage("Validating target " + target.getName());
				target.validate();
				RemindContext.getInstance().getMessageHandler().addMessage("Target " + target.getName() + " validated successfully.");
			}

			setState(State.Ok);
		}
		catch (MessageHandlerException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Error while validating " + getName());
			setState(State.Error);

			throw new MessageHandlerException(e);
		}
		finally
		{
			getRange().close();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deploy(UserInput userInput) throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().getFormatter().setData("Developer", getElement().getDeveloper());
		RemindContext.getInstance().getMessageHandler().getFormatter().setData("SourcePath", source.getName());

		if (!appliesFor(userInput))
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Skipping deployment of " + getName() + " as it does not apply to user input.");

			return;
		}

		DeploymentInformation changeDeploymentInformation = getDeploymentInformation();
		changeDeploymentInformation.setEnvironment(userInput.getEnvironment());
		changeDeploymentInformation.setSourceInfo(source.getDetails());

		@SuppressWarnings("rawtypes")
		Target target = envTargetMap.get(userInput.getEnvironment());
		if (target == null)
		{
			throw new MessageHandlerException("Target for environment " + userInput.getEnvironment() + " cannot be found!");
		}

		target.getDeploymentDetails().clear();
		changeDeploymentInformation.setTargetInfo(target.getDetails());

		// A deployment information must be stored only after specific outcomes in the code below.
		// The flag is then used in the finally block to decide whether to persist a deployment information or not.
		boolean persistDeploymentInformation = true;

		try
		{
			RemindContext.getInstance().getMessageHandler().startAction(CoreActionType.ChangeDeployment);
			changeDeploymentInformation.setActionId(RemindContext.getInstance().getMessageHandler().getCurrentActionId());

			setRange(RemindContext.getInstance().getMessageHandler().openRange());

			if (getElement().isPause())
			{
				FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
						.withFeedback(RemindModelFeedback.Skip, "Continue deployment").build();

				Feedback feedback = RemindContext.getInstance().getMessageHandler()
						.addMessageWithFeedback(MessageLevel.INFO, "Change is paused.", "Waiting for your input...", feedbackContext);

				if (RemindModelFeedback.Abort.equals(feedback))
				{
					setState(State.Error);
					persistDeploymentInformation = false;

					throw new MessageHandlerException("Deployment process aborted.");
				}
			}

			if (getElement().isOnlyOnce())
			{
				RemindContext.getInstance().getMessageHandler()
						.addMessage("Change " + getName() + " is declared as onlyOnce=true...\nChecking if change has already been deployed...");

				// check if change has already been deployed
				DeploymentInformation previousDeploymentInformation = RemindContext.getInstance().getDeploymentInformationHandler()
						.getLatest(source.getDetails(), target.getDetails());
				if (previousDeploymentInformation != null)
				{
					RemindContext
							.getInstance()
							.getMessageHandler()
							.addMessage(
									"Change " + getName() + " has already been deployed: status=" + previousDeploymentInformation.getStatus() + " feedback="
											+ previousDeploymentInformation.getStatusDetails() + "\nSkipping Change...");
					persistDeploymentInformation = false;

					return;
				}
				else
				{
					RemindContext.getInstance().getMessageHandler().addMessage("Change " + getName() + " has not been deployed yet.");
				}

				// save deployment information without deployment execution
				if (Boolean.TRUE.equals(getElement().isOverridden()))
				{
					RemindContext.getInstance().getMessageHandler().addMessage("Change " + getName() + " is skipped because it is marked as overridden.");

					changeDeploymentInformation.setStatusDetails("Overridden by higher revision");

					return;
				}
			}

			RemindContext.getInstance().getMessageHandler().addMessage("Deploying Change " + getName());
			target.deploy(target.convert(source));
			RemindContext.getInstance().getMessageHandler().addMessage(getName() + " deployed successfully.");

			setState(State.Ok);
		}
		catch (MessageHandlerException e)
		{
			if (RemindModelFeedback.Skip.equals(e.getFeedback()))
			{
				RemindContext.getInstance().getMessageHandler().addMessage(getName() + " manually skipped.");
				setState(State.Warning);

				target.getDeploymentDetails().add("Skipped");
			}
			else
			{
				setState(State.Error);
				RemindContext.getInstance().getMessageHandler().addMessage("Error while deploying " + getName());

				target.getDeploymentDetails().add("Aborted");

				throw new MessageHandlerException(e);
			}
		}
		finally
		{
			getRange().close();

			if (getState() == State.Ok && getRange().containsMessageWithLevel(MessageLevel.WARNING))
			{
				setState(State.Warning);
			}

			if (persistDeploymentInformation)
			{
				changeDeploymentInformation.setStatus(getState().name());
				changeDeploymentInformation.addStatusDetails(target.getDeploymentDetails());
				RemindContext.getInstance().getDeploymentInformationHandler().add(changeDeploymentInformation);
			}

			RemindContext.getInstance().getMessageHandler().endCurrentAction();
		}
	}

	@Override
	public boolean appliesFor(UserInput userInput)
	{
		BigInteger testCycleNumber = getElement().getTestCycleNumber();
		if (testCycleNumber.intValue() < userInput.getLowerTestCycleNumber() || testCycleNumber.intValue() > userInput.getUpperTestCycleNumber())
		{
			return false;
		}

		if (userInput.getEnvironment().equals(Environment.ALL.value()))
		{
			return true;
		}

		for (Environment environment : getElement().getEnvironment())
		{
			if (userInput.getEnvironment().trim().equals(environment.value().trim()))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public Iterator iterator()
	{
		return new Iterator();
	}

	@Override
	protected State determineState()
	{
		return getState();
	}

	@Override
	protected void resetState()
	{
		setState(State.Unknown);
	}

	@Override
	protected void fill(DeploymentInformation deploymentInformation)
	{
		// TODO: check
		if (getParent() != null)
		{
			getParent().fill(deploymentInformation);
		}
	}

	public Source<?> getSource()
	{
		return source;
	}

	public Target<?, ?> getTarget(String environment)
	{
		return envTargetMap.get(environment);
	}

	protected static class Iterator implements RemindModel.Iterator<ChangeModel>
	{
		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public ChangeModel getNext()
		{
			return null;
		}
	}
}
