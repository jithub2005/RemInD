package at.jit.remind.web.rest.dto;

public class Pagination
{
	private String first;
	private String previous;
	private String next;
	private String last;

	public String getFirst()
	{
		return first;
	}

	public void setFirst(String first)
	{
		this.first = first;
	}

	public String getPrevious()
	{
		return previous;
	}

	public void setPrevious(String previous)
	{
		this.previous = previous;
	}

	public String getNext()
	{
		return next;
	}

	public void setNext(String next)
	{
		this.next = next;
	}

	public String getLast()
	{
		return last;
	}

	public void setLast(String last)
	{
		this.last = last;
	}
}
