package at.jit.remind.web.domain.base.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

public class QueryParameterHolder<E, P> implements Serializable
{
	private static final long serialVersionUID = -2927353569446145949L;

	private Class<E> clazz;

	private Map<String, P> parameterMap = new HashMap<String, P>();
	private List<String> orderedParameterList = new ArrayList<String>();

	private int firstResult;
	private int maxResults;

	protected QueryParameterHolder(Class<E> clazz)
	{
		this.clazz = clazz;
	}

	protected Class<E> getClazz()
	{
		return clazz;
	}

	protected Map<String, P> getParameterMap()
	{
		return parameterMap;
	}

	public List<String> getOrderedParameterList()
	{
		return new ArrayList<String>(getOrderedParameterListReference());
	}

	protected List<String> getOrderedParameterListReference()
	{
		return orderedParameterList;
	}

	public P getParameter(String name)
	{
		return parameterMap.get(name);
	}

	public void setParameter(String name, P parameter)
	{
		if (!parameterMap.containsKey(name))
		{
			orderedParameterList.add(name);
		}

		parameterMap.put(name, parameter);
	}

	public void clear()
	{
		parameterMap.clear();
		orderedParameterList.clear();
		firstResult = -1;
		maxResults = -1;
	}

	public void limit(int firstResult, int maxResults)
	{
		this.firstResult = firstResult;
		this.maxResults = maxResults;
	}

	public boolean isLimited()
	{
		return firstResult >= 0 && maxResults > 0;
	}

	protected TypedQuery<E> setLimits(TypedQuery<E> typedQuery)
	{
		return isLimited() ? typedQuery.setFirstResult(firstResult).setMaxResults(maxResults) : typedQuery;
	}
}
