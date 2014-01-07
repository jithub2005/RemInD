package at.jit.remind.web.domain.context.reporting.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import at.jit.remind.core.context.reporting.DeploymentInformation;
import at.jit.remind.web.domain.context.reporting.model.PersistedDeploymentInformation;

@Stateless
public class DeploymentService implements DeploymentInformationService
{
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void add(DeploymentInformation deploymentInformation)
	{
		PersistedDeploymentInformation persistedDeploymentInformation = new PersistedDeploymentInformation();

		persistedDeploymentInformation.setEnvironment(deploymentInformation.getEnvironment());
		persistedDeploymentInformation.setRelease(deploymentInformation.getRelease());
		persistedDeploymentInformation.setSourceInfo(deploymentInformation.getSourceInfo());
		persistedDeploymentInformation.setTarget(deploymentInformation.getTarget());
		persistedDeploymentInformation.setTargetInfo(deploymentInformation.getTargetInfo());
		persistedDeploymentInformation.setTestCycle(deploymentInformation.getTestCycle());
		persistedDeploymentInformation.setTestCycleNumber(deploymentInformation.getTestCycleNumber());
		persistedDeploymentInformation.setTitle(deploymentInformation.getTitle());
		persistedDeploymentInformation.setDeploymentVersion(deploymentInformation.getVersion());
		persistedDeploymentInformation.setDeveloper(deploymentInformation.getDeveloper());
		persistedDeploymentInformation.setActionId(deploymentInformation.getActionId());
		persistedDeploymentInformation.setStatus(deploymentInformation.getStatus());
		persistedDeploymentInformation.setStatusDetails(deploymentInformation.getStatusDetails());

		entityManager.persist(persistedDeploymentInformation);
	}

	@Override
	public DeploymentInformation getLatest(String sourceInfo, String targetInfo)
	{
		TypedQuery<PersistedDeploymentInformation> query = entityManager.createNamedQuery(PersistedDeploymentInformation.FindSourceAndTarget,
				PersistedDeploymentInformation.class);
		query.setParameter(PersistedDeploymentInformation.SourceInfo, sourceInfo);
		query.setParameter(PersistedDeploymentInformation.TargetInfo, targetInfo);

		List<PersistedDeploymentInformation> results = query.getResultList();

		if (results.isEmpty())
		{
			return null;
		}

		PersistedDeploymentInformation persistedDeploymentInformation = results.get(0);
		DeploymentInformation deploymentInformation = new DeploymentInformation();
		deploymentInformation.setEnvironment(persistedDeploymentInformation.getEnvironment());
		deploymentInformation.setRelease(persistedDeploymentInformation.getRelease());
		deploymentInformation.setSourceInfo(persistedDeploymentInformation.getSourceInfo());
		deploymentInformation.setTarget(persistedDeploymentInformation.getTarget());
		deploymentInformation.setTargetInfo(persistedDeploymentInformation.getTargetInfo());
		deploymentInformation.setTestCycle(persistedDeploymentInformation.getTestCycle());
		deploymentInformation.setTestCycleNumber(persistedDeploymentInformation.getTestCycleNumber());
		deploymentInformation.setTitle(persistedDeploymentInformation.getTitle());
		deploymentInformation.setVersion(persistedDeploymentInformation.getDeploymentVersion());
		deploymentInformation.setDeveloper(persistedDeploymentInformation.getDeveloper());
		deploymentInformation.setActionId(persistedDeploymentInformation.getActionId());
		deploymentInformation.setStatus(persistedDeploymentInformation.getStatus());
		deploymentInformation.setStatusDetails(persistedDeploymentInformation.getStatusDetails());

		return deploymentInformation;
	}

	@Override
	public PersistedDeploymentInformation getPersistedDeploymentInformationByAction(long actionId)
	{
		try
		{
			return entityManager.createNamedQuery(PersistedDeploymentInformation.FindDeploymentByAction, PersistedDeploymentInformation.class)
					.setParameter("actionId", actionId).getSingleResult();
		}
		catch (NoResultException e)
		{
			return null;
		}
	}
}
