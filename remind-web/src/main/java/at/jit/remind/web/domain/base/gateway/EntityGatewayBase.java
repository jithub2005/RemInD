package at.jit.remind.web.domain.base.gateway;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.base.service.EntityServiceImpl;

public class EntityGatewayBase<E extends EntityBase> extends EntityServiceImpl<E> implements EntityGateway<E>
{
	private static final long serialVersionUID = 2536918805622596875L;

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	private transient EntityManager entityManager;

	protected void initialize()
	{
		setEntityManager(entityManager);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void save()
	{
		// all changes to managed entities are automatically flushed to database at transaction commit
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public E refresh(E entity)
	{
		entityManager.refresh(entity);
		return entity;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public E create(E entity)
	{
		return super.create(entity);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public E find(long id)
	{
		return super.find(id);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public E update(E entity)
	{
		return super.update(entity);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void delete(long id)
	{
		super.delete(id);
	}
}
