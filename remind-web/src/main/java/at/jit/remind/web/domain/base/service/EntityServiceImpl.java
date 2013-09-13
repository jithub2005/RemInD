package at.jit.remind.web.domain.base.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import at.jit.remind.web.domain.base.model.CriteriaQueryParameterHolder;
import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.base.model.NamedQueryParameterHolder;

public class EntityServiceImpl<E extends EntityBase> implements EntityService<E>
{
	private static final long serialVersionUID = 816501817946175705L;

	private transient EntityManager entityManager = null;

	private Class<E> entityClazz;

	@Override
	public E create(E entity)
	{
		entityManager.persist(entity);

		return entity;
	}

	@Override
	public E find(long id)
	{
		return entityManager.find(entityClazz, id);
	}

	@Override
	public E update(E entity)
	{
		return entityManager.merge(entity);
	}

	@Override
	public void delete(long id)
	{
		E entity = entityManager.getReference(entityClazz, id);
		entityManager.remove(entity);
	}

	@Override
	public List<E> find(NamedQueryParameterHolder<E> parameterHolder)
	{
		TypedQuery<E> typedQuery = parameterHolder.createNamedQuery(entityManager);
		return typedQuery.getResultList();
	}

	@Override
	public Long count(NamedQueryParameterHolder<Long> parameterHolder)
	{
		TypedQuery<Long> typedQuery = parameterHolder.createNamedQuery(entityManager);
		return typedQuery.getSingleResult();
	}

	@Override
	public List<E> find(CriteriaQueryParameterHolder<E> parameterHolder)
	{
		TypedQuery<E> typedQuery = parameterHolder.createSelectCriteriaQuery(entityManager);
		return typedQuery.getResultList();
	}

	@Override
	public Long count(CriteriaQueryParameterHolder<E> parameterHolder)
	{
		TypedQuery<Long> typedQuery = parameterHolder.createCountCriteriaQuery(entityManager);
		return typedQuery.getSingleResult();
	}

	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	protected void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

	public Class<E> getEntityClazz()
	{
		return entityClazz;
	}

	public void setEntityClazz(Class<E> clazz)
	{
		this.entityClazz = clazz;
	}
}
