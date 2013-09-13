package at.jit.remind.web.domain.base.model;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;

public abstract class CriteriaFilterBase<E extends EntityBase, V extends Object>
{
	private String property;

	private V value;
	private Class<V> valueClazz;
	private SortOrder sortOrder = SortOrder.Unsorted;

	protected CriteriaFilterBase(String property, Class<V> valueClazz)
	{
		this.property = property;
		this.valueClazz = valueClazz;
	}

	public String getProperty()
	{
		return property;
	}

	public V getValue()
	{
		return value;
	}

	public void setValue(V value)
	{
		this.value = value;
	}

	protected Class<V> getValueClazz()
	{
		return valueClazz;
	}

	public SortOrder getSortOrder()
	{
		return sortOrder;
	}

	public final void switchSortOrder()
	{
		sortOrder = sortOrder.next();
	}

	public void clear()
	{
		setValue(null);
		sortOrder = sortOrder.reset();
	}

	public boolean isReset()
	{
		return getValue() == null || getValue().toString().isEmpty();
	}

	protected Path<String> getExpression(Root<E> root)
	{
		String[] paths = StringUtils.split(getProperty(), '.');

		Path<String> partialExpression = root.get(paths[0]);
		for (int i = 1; i < paths.length; ++i)
		{
			partialExpression = partialExpression.get(paths[i]);
		}

		return partialExpression;
	}

	public abstract Expression<Boolean> createPredicate(CriteriaBuilder criteriaBuilder, Root<E> root);

	public Order createOrder(Root<E> root, CriteriaBuilder criteriaBuilder)
	{
		if (sortOrder == SortOrder.Ascending)
		{
			return criteriaBuilder.asc(getExpression(root));
		}

		if (sortOrder == SortOrder.Descending)
		{
			return criteriaBuilder.desc(getExpression(root));
		}

		return null;
	}
}
