package at.jit.remind.web.rest;

public class PaginationHelper
{
	private int normalizedIndex;
	private int previousIndex;
	private int nextIndex;
	private int lastIndex;

	public PaginationHelper(int index, int count, int maxOnPage)
	{
		if (index < 0)
		{
			throw new IllegalArgumentException("index must not be negative");
		}

		if (count <= 0 || maxOnPage <= 0)
		{
			throw new IllegalArgumentException("count and maxOnPage must be positive");
		}

		normalizedIndex = (Math.min(index, count) / maxOnPage) * maxOnPage;
		previousIndex = Math.max(normalizedIndex - maxOnPage, 0);
		lastIndex = (count / maxOnPage) * maxOnPage;
		nextIndex = Math.min(normalizedIndex + maxOnPage, lastIndex);
	}

	public int getFirstIndex()
	{
		return 0;
	}

	public int getNormalizedIndex()
	{
		return normalizedIndex;
	}

	public int getPreviousIndex()
	{
		return previousIndex;
	}

	public int getNextIndex()
	{
		return nextIndex;
	}

	public int getLastIndex()
	{
		return lastIndex;
	}
}
