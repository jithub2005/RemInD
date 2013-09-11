package at.jit.remind.web.domain.base.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

public class CriteriaQueryParameterHolder<E extends EntityBase> extends QueryParameterHolder<E, CriteriaFilterBase<E, ? extends Object>>
{
	private static final long serialVersionUID = 4591804638432020828L;

	public CriteriaQueryParameterHolder(Class<E> clazz)
	{
		super(clazz);
	}

	public TypedQuery<Long> createCountCriteriaQuery(EntityManager entityManager)
	{
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<E> root = criteriaQuery.from(getClazz());

		Expression<Boolean> filterCriteria = createFilterCriteria(criteriaBuilder, root);
		if (filterCriteria != null)
		{
			criteriaQuery.where(filterCriteria);
		}

		Expression<Long> count = criteriaBuilder.count(root);
		criteriaQuery.select(count);

		return entityManager.createQuery(criteriaQuery);
	}

	public TypedQuery<E> createSelectCriteriaQuery(EntityManager entityManager)
	{
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(getClazz());
		Root<E> root = criteriaQuery.from(getClazz());

		Expression<Boolean> filterCriteria = createFilterCriteria(criteriaBuilder, root);
		if (filterCriteria != null)
		{
			criteriaQuery.where(filterCriteria);
		}

		List<Order> orderList = createOrderList(criteriaBuilder, root);
		if (!orderList.isEmpty())
		{
			criteriaQuery.orderBy(orderList);
		}

		return setLimits(entityManager.createQuery(criteriaQuery));
	}

	private Expression<Boolean> createFilterCriteria(CriteriaBuilder criteriaBuilder, Root<E> root)
	{
		Expression<Boolean> filterCriteria = null;

		for (String parameterName : getOrderedParameterListReference())
		{
			Expression<Boolean> predicate = getParameter(parameterName).createPredicate(criteriaBuilder, root);

			if (predicate == null)
			{
				continue;
			}

			if (filterCriteria == null)
			{
				filterCriteria = predicate.as(Boolean.class);
			}
			else
			{
				filterCriteria = criteriaBuilder.and(filterCriteria, predicate.as(Boolean.class));
			}
		}

		return filterCriteria;
	}

	private List<Order> createOrderList(CriteriaBuilder criteriaBuilder, Root<E> root)
	{
		List<Order> orderList = new ArrayList<Order>();

		Order order;
		for (String parameterName : getOrderedParameterListReference())
		{
			order = getParameter(parameterName).createOrder(root, criteriaBuilder);
			if (order != null)
			{
				orderList.add(order);
			}
		}

		// TODO: review and find more general solution without hard-coded id
		// Such ordering is needed for pagination as it is not garanteed that database returns data always in the
		// same order.
		orderList.add(criteriaBuilder.asc(root.get("id")));

		return orderList;
	}

	@Override
	public void clear()
	{
		for (CriteriaFilterBase<E, ?> filter : getParameterMap().values())
		{
			filter.clear();
		}
	}
}
