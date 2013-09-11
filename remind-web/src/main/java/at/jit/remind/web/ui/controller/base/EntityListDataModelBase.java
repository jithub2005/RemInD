package at.jit.remind.web.ui.controller.base;

import java.util.List;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.richfaces.model.Arrangeable;
import org.richfaces.model.ArrangeableState;

import at.jit.remind.web.domain.base.model.CriteriaFilterBase;
import at.jit.remind.web.domain.base.model.CriteriaQueryParameterHolder;
import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.base.service.EntityServiceBase;

public class EntityListDataModelBase<E extends EntityBase> extends ExtendedDataModel<E> implements Arrangeable, EntityListDataModel<E>
{
	private EntityServiceBase<E> entityService;
	private CriteriaQueryParameterHolder<E> parameterHolder;

	private ArrangeableState arrangeableState;
	private Integer rowKey;
	private List<E> wrappedData;
	private int rowCount = -1;

	protected EntityListDataModelBase(CriteriaQueryParameterHolder<E> parameterHolder)
	{
		this.parameterHolder = parameterHolder;
	}

	@Override
	public void arrange(FacesContext arg0, ArrangeableState arrangeableState)
	{
		this.arrangeableState = arrangeableState;
	}

	@Override
	public Object getRowKey()
	{
		return rowKey;
	}

	@Override
	public void setRowKey(Object rowKey)
	{
		this.rowKey = (Integer) rowKey;
	}

	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument)
	{
		retrieveRowCount();

		SequenceRange sequenceRange = (SequenceRange) range;
		setWrappedData(loadData(sequenceRange.getFirstRow(), sequenceRange.getRows()));

		for (int row = 0; row < getWrappedData().size(); ++row)
		{
			visitor.process(context, row, argument);
		}

	}

	protected List<E> loadData(int firstResult, int maxResults)
	{
		updateLimit(firstResult, maxResults);

		return entityService.find(parameterHolder);
	}

	@Override
	public int getRowCount()
	{
		if (rowCount <= 0)
		{
			retrieveRowCount();
		}

		return rowCount;
	}

	private void retrieveRowCount()
	{
		rowCount = entityService.count(parameterHolder).intValue();
	}

	@Override
	public E getRowData()
	{
		return wrappedData.get(rowKey);
	}

	@Override
	public int getRowIndex()
	{
		return -1;
	}

	@Override
	public boolean isRowAvailable()
	{
		return rowKey != null;
	}

	@Override
	public void setRowIndex(int arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> getWrappedData()
	{
		return wrappedData;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setWrappedData(Object data)
	{
		wrappedData = (List<E>) data;
	}

	private void updateLimit(int firstResult, int maxResults)
	{
		if (getRowCount() == 0)
		{
			return;
		}

		if (getRowCount() > firstResult)
		{
			parameterHolder.limit(firstResult, maxResults);
			return;
		}

		parameterHolder.limit(Math.max(firstResult - maxResults, 0), maxResults);
	}

	protected CriteriaQueryParameterHolder<E> getParameterHolder()
	{
		return parameterHolder;
	}

	@Override
	public CriteriaFilterBase<E, ? extends Object> getParameter(String name)
	{
		return parameterHolder.getParameter(name);
	}

	@Override
	public void addParameter(String name, CriteriaFilterBase<E, ? extends Object> filter)
	{
		parameterHolder.setParameter(name, filter);
	}

	@Override
	public void setEntityService(EntityServiceBase<E> entityService)
	{
		this.entityService = entityService;
	}
}
