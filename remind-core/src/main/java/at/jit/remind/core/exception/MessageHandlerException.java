package at.jit.remind.core.exception;

import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.core.model.RemindModelFeedback;

public class MessageHandlerException extends Exception
{
	private static final long serialVersionUID = 4125555438057919969L;

	private Feedback feedback = RemindModelFeedback.None;

	public MessageHandlerException(String message)
	{
		super(message);
	}

	public MessageHandlerException(Exception cause)
	{
		super(cause);
	}

	public MessageHandlerException(String message, Exception cause)
	{
		super(message, cause);
	}

	public MessageHandlerException(String message, Feedback feedback)
	{
		super(message);
		this.feedback = feedback;
	}

	public MessageHandlerException(Exception cause, Feedback feedback)
	{
		super(cause);
		this.feedback = feedback;
	}

	public Feedback getFeedback()
	{
		return feedback;
	}
}
