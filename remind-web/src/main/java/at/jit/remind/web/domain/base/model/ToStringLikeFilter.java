package at.jit.remind.web.domain.base.model;

import java.util.Locale;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

public class ToStringLikeFilter<E extends EntityBase> extends EqualFilter<E, Object>
{
	public ToStringLikeFilter(String property)
	{
		super(property, Object.class);
	}

	@Override
	public Expression<Boolean> createPredicate(CriteriaBuilder criteriaBuilder, Root<E> root)
	{
		if (isReset())
		{
			return null;
		}

		Expression<Integer> locator = criteriaBuilder.locate(criteriaBuilder.lower(getExpression(root)), getValue().toString().toLowerCase(Locale.ENGLISH), 1);
		return criteriaBuilder.gt(locator, 0);
	}
}
