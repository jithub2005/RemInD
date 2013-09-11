package at.jit.remind.web.ui.controller.base;

import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.base.service.EntityServiceBase;

public abstract class EntityListControllerBase<E extends EntityBase> implements EntityListController<E>
{
	private static final long serialVersionUID = -5219883923969394314L;

	private EntityServiceBase<E> entityService;

	private int page = 1;

	protected EntityServiceBase<E> getEntityService()
	{
		return entityService;
	}

	protected void setEntityService(EntityServiceBase<E> entityService)
	{
		this.entityService = entityService;
	}

	public abstract EntityListDataModel<E> getDataModel();

	public int getPage()
	{
		return page;
	}

	public void setPage(int page)
	{
		this.page = page;
	}
}
