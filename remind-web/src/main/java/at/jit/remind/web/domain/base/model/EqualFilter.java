package at.jit.remind.web.domain.base.model;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

public class EqualFilter<E extends EntityBase, V extends Object> extends CriteriaFilterBase<E, V>
{
	public EqualFilter(String property, Class<V> valueClazz)
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

		return criteriaBuilder.equal(getExpression(root).as(getValueClazz()), getValue());
	}
}
