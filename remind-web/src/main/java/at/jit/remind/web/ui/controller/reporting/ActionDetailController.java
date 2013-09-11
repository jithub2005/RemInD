package at.jit.remind.web.ui.controller.reporting;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.New;
import javax.inject.Inject;
import javax.inject.Named;

import at.jit.remind.web.domain.context.reporting.model.Action;
import at.jit.remind.web.domain.context.reporting.model.PersistedDeploymentInformation;
import at.jit.remind.web.domain.context.reporting.service.ActionLogService;
import at.jit.remind.web.domain.context.reporting.service.ActionService;
import at.jit.remind.web.domain.context.reporting.service.DeploymentInformationService;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.ui.util.Navigator;

@LoggedIn
@Named
@SessionScoped
public class ActionDetailController implements Serializable
{
	private static final long serialVersionUID = 3648881968675462873L;

	@Inject
	private Navigator navigator;

	@EJB
	private ActionService actionService;

	@EJB
	private ActionLogService actionLogService;

	@EJB
	private DeploymentInformationService deploymentService;

	private Action action;

	private PersistedDeploymentInformation persistedDeploymentInformation;

	@Inject
	@New
	private ActionLogListController actionLogListController;

	private boolean persistedDeploymentInformationExists;

	@PostConstruct
	protected void initialize()
	{
		persistedDeploymentInformation = new PersistedDeploymentInformation();
	}

	public String navigateTo(long actionId)
	{
		actionLogListController.manageActionIdFilter(actionId);
		action = actionService.find(actionId);
		persistedDeploymentInformation = deploymentService.getPersistedDeploymentInformationByAction(actionId);

		if (persistedDeploymentInformation == null)
		{
			persistedDeploymentInformationExists = false;
		}
		else
		{
			persistedDeploymentInformationExists = true;
		}

		return navigator.navigateTo("/actionDetail");
	}

	public String navigateFromActionLogTo(long actionLogId)
	{
		return navigateTo(actionLogService.getActionIdForActionLogId(actionLogId));
	}

	public Action getAction()
	{
		return action;
	}

	public void setAction(Action action)
	{
		this.action = action;
	}

	public PersistedDeploymentInformation getPersistedDeploymentInformation()
	{
		return persistedDeploymentInformation;
	}

	public void setPersistedDeploymentInformation(PersistedDeploymentInformation persistedDeploymentInformation)
	{
		this.persistedDeploymentInformation = persistedDeploymentInformation;
	}

	public ActionLogListController getActionLogListController()
	{
		return actionLogListController;
	}

	public boolean isPersistedDeploymentInformationExists()
	{
		return persistedDeploymentInformationExists;
	}

	public void setPersistedDeploymentInformationExists(boolean persistedDeploymentInformationExists)
	{
		this.persistedDeploymentInformationExists = persistedDeploymentInformationExists;
	}
}
