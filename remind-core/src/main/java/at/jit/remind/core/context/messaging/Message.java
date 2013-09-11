package at.jit.remind.core.context.messaging;

import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;

public class Message
{
	private final MessageLevel level;
	private final String message;
	private final String detail;

	public Message(MessageLevel level, String message, String detail)
	{
		this.level = level;
		this.message = message;
		this.detail = detail;
	}

	public String getMessage()
	{
		return message;
	}

	public MessageLevel getLevel()
	{
		return level;
	}

	public String getDetail()
	{
		return detail;
	}
}
