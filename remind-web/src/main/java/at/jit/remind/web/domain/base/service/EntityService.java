package at.jit.remind.web.domain.base.service;

import java.io.Serializable;
import java.util.List;

import at.jit.remind.web.domain.base.model.CriteriaQueryParameterHolder;
import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.base.model.NamedQueryParameterHolder;

public interface EntityService<E extends EntityBase> extends Serializable
{
	E create(E entity);

	E find(long id);

	E update(E entity);

	void delete(long id);

	List<E> find(NamedQueryParameterHolder<E> parameterHolder);

	Long count(NamedQueryParameterHolder<Long> parameterHolder);

	List<E> find(CriteriaQueryParameterHolder<E> parameterHolder);

	Long count(CriteriaQueryParameterHolder<E> parameterHolder);

	Class<E> getEntityClazz();
}
