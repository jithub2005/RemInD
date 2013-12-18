package at.jit.remind.web.rest.dto;

import java.util.List;

public class DtoListBase<T>
{
	private Pagination pagination = new Pagination();

	private List<T> data;

	public Pagination getPagination()
	{
		return pagination;
	}

	public List<T> getData()
	{
		return data;
	}

	protected void setData(List<T> data)
	{
		this.data = data;
	}
}
