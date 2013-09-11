package at.jit.remind.web.domain.base.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import at.jit.remind.web.domain.base.model.EntityBase;

public abstract class EntityServiceBase<E extends EntityBase> extends EntityServiceImpl<E>
{
	private static final long serialVersionUID = -7823902889991376783L;

	@PersistenceContext
	private transient EntityManager entityManager;

	protected void initialize()
	{
		setEntityManager(entityManager);
	}
}
