package at.jit.remind.web.domain.base.model;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

public class InCollectionFilter<E extends EntityBase, V> extends CriteriaFilterBase<E, Collection<V>>
{
	public InCollectionFilter(String property)
	{
		super(property, null);

		super.setValue(new HashSet<V>());
	}

	@Override
	public boolean isReset()
	{
		return getValue().isEmpty();
	}

	@Override
	public Expression<Boolean> createPredicate(CriteriaBuilder criteriaBuilder, Root<E> root)
	{
		return getValue().size() > 0 ? getExpression(root).in(getValue()) : null;
	}

	@Override
	public void setValue(Collection<V> value)
	{
		getValue().clear();

		if (value == null)
		{
			return;
		}

		getValue().addAll(value);
	}

	public void add(V value)
	{
		getValue().add(value);
	}
}
