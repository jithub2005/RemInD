package at.jit.remind.core.context.reporting;

public interface DeploymentInformationHandler
{
	void add(DeploymentInformation deploymentInformation);

	DeploymentInformation getLatest(String sourceInfo, String targetInfo);
}
