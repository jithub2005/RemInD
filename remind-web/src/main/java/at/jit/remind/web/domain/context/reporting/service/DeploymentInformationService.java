package at.jit.remind.web.domain.context.reporting.service;

import at.jit.remind.core.context.reporting.DeploymentInformation;
import at.jit.remind.core.context.reporting.DeploymentInformationHandler;
import at.jit.remind.web.domain.context.reporting.model.PersistedDeploymentInformation;

public interface DeploymentInformationService extends DeploymentInformationHandler
{
	void add(DeploymentInformation deploymentInformation);

	DeploymentInformation getLatest(String sourceInfo, String targetInfo);

	PersistedDeploymentInformation getPersistedDeploymentInformationByAction(long actionId);
}
