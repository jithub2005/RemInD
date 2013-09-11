package at.jit.remind.core.context.messaging;

import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;

public class LineBreakFormatter implements Formatter
{
	@Override
	public String format(MessageLevel messageLevel, String message, String detail)
	{
		return messageLevel + "\n" + message + "\n" + detail;
	}

	@Override
	public void setData(String key, String value)
	{
	}

	@Override
	public void clearData()
	{
	}
}
