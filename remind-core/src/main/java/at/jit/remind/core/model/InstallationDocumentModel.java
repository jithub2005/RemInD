package at.jit.remind.core.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.CoreActionType;
import at.jit.remind.core.context.messaging.FeedbackFormatter;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.context.reporting.DeploymentInformation;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.status.State;
import at.jit.remind.core.xml.DocumentInformation;
import at.jit.remind.core.xml.InstallationBlock;
import at.jit.remind.core.xml.InstallationDocument;

public class InstallationDocumentModel extends RemindModelBase<InstallationDocument, InstallationBlockModel>
{
	private List<InstallationBlockModel> installationBlockModels = new ArrayList<InstallationBlockModel>();

	public InstallationDocumentModel()
	{
		super(InstallationDocument.class);
	}

	@Override
	public void update(InstallationDocument element) throws RemindModelException
	{
		setElement(element);

		DocumentInformation documentInformation = element.getDocumentInformation();

		setName(documentInformation.getTarget() + " - " + documentInformation.getRelease() + " - " + documentInformation.getTestCycle() + " - "
				+ documentInformation.getVersion());

		removeAllChilds();

		DeploymentInformation deploymentInformation = getDeploymentInformation();
		deploymentInformation.setTitle(documentInformation.getTitle());
		deploymentInformation.setTarget(documentInformation.getTarget());
		deploymentInformation.setRelease(documentInformation.getRelease());
		deploymentInformation.setTestCycle(documentInformation.getTestCycle());
		deploymentInformation.setVersion(documentInformation.getVersion());

		for (InstallationBlock installationBlock : element.getInstallationBlock())
		{
			InstallationBlockModel installationBlockModel = new InstallationBlockModel();
			addChild(installationBlockModel);

			installationBlockModel.update(installationBlock);
		}
	}

	private void addChild(InstallationBlockModel installationBlockModel)
	{
		installationBlockModel.setParent(this);
		installationBlockModels.add(installationBlockModel);
	}

	@Override
	protected void removeAllChilds()
	{
		Iterator iterator = iterator();
		while (iterator.hasNext())
		{
			iterator.getNext().setParent(null);
		}

		installationBlockModels.clear();
	}

	@Override
	public void validate()
	{
		RemindContext.getInstance().getMessageHandler().startAction(CoreActionType.DocumentValidation);
		RemindContext.getInstance().getMessageHandler().addMessage("\nValidating installation document " + getName());

		resetState();

		Iterator iterator = iterator();
		while (iterator.hasNext())
		{
			iterator.getNext().validate();
		}

		setState(determineState());

		RemindContext.getInstance().getMessageHandler().addMessage("Validation of installation document " + getName() + " finished with state=" + getState());
		RemindContext.getInstance().getMessageHandler().endCurrentAction();
	}

	// This validate method is used if and only if deploy has been executed
	@Override
	protected void validate(UserInput userInput) throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().addMessage("Validating installation document " + getName());

		try
		{
			Iterator iterator = iterator();
			while (iterator.hasNext())
			{
				iterator.getNext().validate(userInput);
			}
		}
		finally
		{
			setState(determineState());
			if (State.Ok.equals(getState()))
			{
				RemindContext.getInstance().getMessageHandler()
						.addMessage("Validation of installation document " + getName() + " + successfully finished with state=" + getState());
			}
			else
			{
				RemindContext.getInstance().getMessageHandler().addMessage("Validation of installation document ended with state=" + getState());
			}
		}
	}

	@Override
	public void deploy(UserInput userInput) throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().startAction(CoreActionType.DocumentDeployment);

		resetState();

		RemindContext.getInstance().getMessageHandler().setFormatter(new FeedbackFormatter());

		RemindContext.getInstance().getMessageHandler().addMessage("\nStart deployment of installation document " + getName());
		RemindContext
				.getInstance()
				.getMessageHandler()
				.addMessage(
						"UserInput: environment=" + userInput.getEnvironment() + ", lowerTestCycle=" + userInput.getLowerTestCycleNumber()
								+ ", upperTestCycle=" + userInput.getUpperTestCycleNumber());
		try
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Effective installation document is as follows:");
			RemindContext.getInstance().getMessageHandler().addMessage(getXmlData());

			RemindContext.getInstance().getMessageHandler().addMessage("Start validation process before deployment...");
			validate(userInput);
			RemindContext.getInstance().getMessageHandler().addMessage("Validation process finished.");

			Iterator iterator = iterator();
			while (iterator.hasNext())
			{
				iterator.getNext().deploy(userInput);
			}
		}
		catch (RemindModelException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "Failed to start deployment", e.getMessage());
			setState(State.Error);
		}
		finally
		{
			setState(determineState());
			if (State.Ok.equals(getState()))
			{
				RemindContext.getInstance().getMessageHandler()
						.addMessage("Deployment of installation document " + getName() + " successfully finished with state=" + getState().toString());
			}
			else
			{
				RemindContext.getInstance().getMessageHandler()
						.addMessage("Deployment of installation document " + getName() + " ended with state=" + getState().toString());
			}

			RemindContext.getInstance().getMessageHandler().endCurrentAction();
		}
	}

	public InstallationDocumentModel getResultingModel(UserInput userInput) throws RemindModelException
	{
		Element element = XmlHelper.parseDocumentElement(getXmlData());
		InstallationDocumentModel resultingModel = new InstallationDocumentModel();
		resultingModel.update(new ChangeFilter().on(element).with(userInput).apply());

		return resultingModel;
	}

	@Override
	public Iterator iterator()
	{
		return new Iterator();
	}

	@Override
	protected State determineState()
	{
		State worstState = State.Unknown;

		for (InstallationBlockModel installationBlockModel : installationBlockModels)
		{
			State currentState = installationBlockModel.getState();
			worstState = currentState.retrieveWorstState(worstState);
		}

		return worstState;
	}

	@Override
	protected void resetState()
	{
		setState(State.Unknown);

		Iterator iterator = iterator();
		while (iterator.hasNext())
		{
			iterator.getNext().resetState();
		}
	}

	@Override
	protected void fill(DeploymentInformation deploymentInformation)
	{
		DeploymentInformation installationDocumentInformation = getDeploymentInformation();
		deploymentInformation.setTitle(installationDocumentInformation.getTitle());
		deploymentInformation.setTarget(installationDocumentInformation.getTarget());
		deploymentInformation.setRelease(installationDocumentInformation.getRelease());
		deploymentInformation.setTestCycle(installationDocumentInformation.getTestCycle());
		deploymentInformation.setVersion(installationDocumentInformation.getVersion());
	}

	protected class Iterator implements RemindModel.Iterator<InstallationBlockModel>
	{
		private int position = 0;

		@Override
		public boolean hasNext()
		{
			return installationBlockModels.size() > position;
		}

		@Override
		public InstallationBlockModel getNext()
		{
			return installationBlockModels.get(position++);
		}
	}
}
