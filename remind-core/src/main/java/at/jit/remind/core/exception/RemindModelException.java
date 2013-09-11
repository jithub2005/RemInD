package at.jit.remind.core.exception;

public class RemindModelException extends Exception
{
	private static final long serialVersionUID = -5817890606354466262L;

	public RemindModelException(String message)
	{
		super(message);
	}

	public RemindModelException(Exception cause)
	{
		super(cause);
	}
}
