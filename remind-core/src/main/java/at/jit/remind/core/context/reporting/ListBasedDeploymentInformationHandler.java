package at.jit.remind.core.context.reporting;

import java.util.LinkedList;
import java.util.List;

public class ListBasedDeploymentInformationHandler implements DeploymentInformationHandler
{
	private List<DeploymentInformation> deploymentUnitList = new LinkedList<DeploymentInformation>();

	@Override
	public void add(DeploymentInformation deploymentUnit)
	{
		deploymentUnitList.add(deploymentUnit);
	}

	public List<DeploymentInformation> getDeploymentInformationList()
	{
		return deploymentUnitList;
	}

	@Override
	public DeploymentInformation getLatest(String sourceInfo, String targetInfo)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
