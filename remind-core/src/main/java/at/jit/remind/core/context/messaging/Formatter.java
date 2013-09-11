package at.jit.remind.core.context.messaging;

import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;

public interface Formatter
{
	String format(MessageLevel messageLevel, String message, String detail);

	void setData(String key, String value);

	void clearData();
}
