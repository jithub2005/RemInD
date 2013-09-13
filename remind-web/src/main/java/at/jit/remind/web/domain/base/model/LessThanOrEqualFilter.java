package at.jit.remind.web.domain.base.model;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

public class LessThanOrEqualFilter<E extends EntityBase, V extends Comparable<V>> extends CriteriaFilterBase<E, V>
{
	public LessThanOrEqualFilter(String property, Class<V> valueClazz)
	{
		super(property, valueClazz);
	}

	@Override
	public Expression<Boolean> createPredicate(CriteriaBuilder criteriaBuilder, Root<E> root)
	{
		if (isReset())
		{
			return null;
		}

		return criteriaBuilder.lessThanOrEqualTo(getExpression(root).as(getValueClazz()), getValue());
	}
}
