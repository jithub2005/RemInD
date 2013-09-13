package at.jit.remind.web.domain.base.model;

public enum SortOrder
{
	Unsorted, Ascending, Descending;

	public SortOrder next()
	{
		return SortOrder.values()[(this.ordinal() + 1) % SortOrder.values().length];
	}

	public SortOrder reset()
	{
		return SortOrder.Unsorted;
	}
}
