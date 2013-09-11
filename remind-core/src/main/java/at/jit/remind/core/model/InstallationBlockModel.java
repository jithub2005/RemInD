package at.jit.remind.core.model;

import java.util.ArrayList;
import java.util.List;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.reporting.DeploymentInformation;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.status.State;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.InstallationBlock;

@SuppressWarnings("rawtypes")
public class InstallationBlockModel extends RemindModelBase<InstallationBlock, RemindModelBase>
{
	private List<InstallationBlockModel> installationBlockModels = new ArrayList<InstallationBlockModel>();
	private List<ChangeModel> changeModels = new ArrayList<ChangeModel>();

	public InstallationBlockModel()
	{
		super(InstallationBlock.class);
	}

	@Override
	public void update(InstallationBlock element) throws RemindModelException
	{
		setElement(element);

		setName(element.getPhaseDescription().getPhase().name());

		removeAllChilds();
		for (InstallationBlock childInstallationBlock : element.getInstallationBlock())
		{
			InstallationBlockModel childInstallationBlockModel = new InstallationBlockModel();
			addChild(childInstallationBlockModel);

			childInstallationBlockModel.update(childInstallationBlock);
		}

		for (Change change : element.getChange())
		{
			ChangeModel changeModel = new ChangeModel();
			addChild(changeModel);

			changeModel.update(change);
		}
	}

	private void addChild(InstallationBlockModel installationBlockModel)
	{
		installationBlockModel.setParent(this);
		installationBlockModels.add(installationBlockModel);
	}

	private void addChild(ChangeModel changeModel)
	{
		changeModel.setParent(this);
		changeModels.add(changeModel);
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
		changeModels.clear();
	}

	@Override
	public void validate()
	{
		RemindContext.getInstance().getMessageHandler().addMessage("Validating InstallationBlock for phase " + getName());
		Iterator iterator = iterator();
		while (iterator.hasNext())
		{
			iterator.getNext().validate();
		}

		setState(determineState());
		RemindContext.getInstance().getMessageHandler().addMessage("Validating of " + getName() + " finished with state=" + getState());
	}

	// This validate method is used if and only if deploy has been executed
	@Override
	protected void validate(UserInput userInput) throws MessageHandlerException
	{
		setRange(RemindContext.getInstance().getMessageHandler().openRange());
		RemindContext.getInstance().getMessageHandler().addMessage("Validating InstallationBlock for phase " + getName());
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
			getRange().close();
			setState(determineState());
			if (State.Ok.equals(getState()))
			{
				RemindContext.getInstance().getMessageHandler().addMessage("Validating of " + getName() + " successfully finished with state=" + getState());
			}
			else
			{
				RemindContext.getInstance().getMessageHandler().addMessage("Validating of " + getName() + " ended with state=" + getState());
			}
		}
	}

	@Override
	public void deploy(UserInput userInput) throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler().addMessage("Deploying InstallationBlock for phase " + getName());
		setRange(RemindContext.getInstance().getMessageHandler().openRange());
		try
		{
			Iterator iterator = iterator();
			while (iterator.hasNext())
			{
				iterator.getNext().deploy(userInput);
			}
		}
		finally
		{
			getRange().close();
			setState(determineState());
			if (State.Ok.equals(getState()))
			{
				RemindContext.getInstance().getMessageHandler().addMessage("Deployment of " + getName() + " successfully finished with state=" + getState());
			}
			else
			{
				RemindContext.getInstance().getMessageHandler().addMessage("Deployment of " + getName() + " ended with state=" + getState());
			}
		}
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

		for (ChangeModel changeModel : changeModels)
		{
			State currentState = changeModel.getState();
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
		getParent().fill(deploymentInformation);
	}

	protected class Iterator implements RemindModel.Iterator<RemindModelBase>
	{
		private int installationBlockModelPosition = 0;
		private int changeModelPosition = 0;

		@Override
		public boolean hasNext()
		{
			return hasNextInstallationBlockModel() || hasNextChangeModel();
		}

		private boolean hasNextInstallationBlockModel()
		{
			return installationBlockModels.size() > installationBlockModelPosition;
		}

		private boolean hasNextChangeModel()
		{
			return changeModels.size() > changeModelPosition;
		}

		@Override
		public RemindModelBase getNext()
		{
			if (hasNextInstallationBlockModel())
			{
				return installationBlockModels.get(installationBlockModelPosition++);
			}

			return changeModels.get(changeModelPosition++);
		}
	}
}
