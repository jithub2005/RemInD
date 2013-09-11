package at.jit.remind.core.context.messaging;

import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;

public class FeedbackFormatter extends KeyValueBasedFormatter
{
	private static final int messageLength = 100;

	@Override
	public String format(MessageLevel messageLevel, String message, String detail)
	{
		String sourcePath = "Source";
		String developer = "Unknown Developer";

		if (getValueMap().containsKey("SourcePath"))
		{
			sourcePath = getValueMap().get("SourcePath");
		}

		if (getValueMap().containsKey("Developer"))
		{
			developer = getValueMap().get("Developer");
		}

		String shortMessage;
		if (message.length() < messageLength)
		{
			shortMessage = message.substring(0, message.length());
		}
		else
		{
			shortMessage = message.substring(0, messageLength) + " [...]";
		}

		return sourcePath + " (" + developer + ")\n" + "++++++++++++++++++++++++++++++++++++++++++++++++\n" + shortMessage + "\n"
				+ "++++++++++++++++++++++++++++++++++++++++++++++++\n" + detail + "\n";
	}
}
