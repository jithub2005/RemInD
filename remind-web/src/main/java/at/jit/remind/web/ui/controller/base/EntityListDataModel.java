package at.jit.remind.web.ui.controller.base;

import at.jit.remind.web.domain.base.model.CriteriaFilterBase;
import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.base.service.EntityServiceBase;

public interface EntityListDataModel<E extends EntityBase>
{
	CriteriaFilterBase<E, ? extends Object> getParameter(String name);

	void addParameter(String name, CriteriaFilterBase<E, ? extends Object> filter);

	void setEntityService(EntityServiceBase<E> entityService);
}
