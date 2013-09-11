package at.jit.remind.web.domain.base.model;

import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class NamedQueryParameterHolder<E> extends QueryParameterHolder<E, Object>
{
	private static final long serialVersionUID = 4334437706949052908L;

	private String queryName;

	public NamedQueryParameterHolder(Class<E> clazz)
	{
		super(clazz);
	}

	public String getQueryName()
	{
		return queryName;
	}

	public void setQueryName(String queryName)
	{
		this.queryName = queryName;
	}

	public TypedQuery<E> createNamedQuery(EntityManager entityManager)
	{
		TypedQuery<E> typedQuery = entityManager.createNamedQuery(queryName, getClazz());
		for (Entry<String, Object> entry : getParameterMap().entrySet())
		{
			typedQuery.setParameter(entry.getKey(), entry.getValue());
		}

		return setLimits(typedQuery);
	}
}
