package at.jit.remind.web.domain.context.reporting.service;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import at.jit.remind.web.domain.base.service.EntityServiceBase;
import at.jit.remind.web.domain.context.reporting.model.PersistedDeploymentInformation;

@Stateless
public class PersistedDeploymentService extends EntityServiceBase<PersistedDeploymentInformation>
{
	private static final long serialVersionUID = 5015728655679148568L;

	@PostConstruct
	@Override
	protected void initialize()
	{
		super.initialize();
		setEntityClazz(PersistedDeploymentInformation.class);
	}
}
