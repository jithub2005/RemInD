package at.jit.remind.core.context.messaging;

import java.util.List;

public interface MessageHandler
{
	public enum MessageLevel
	{
		INFO, WARNING, ERROR;
	}

	void addMessage(String message);

	void addMessage(MessageLevel messageLevel, String message, String detail);

	String addAndFormatMessage(MessageLevel messageLevel, String message, String detail);

	Feedback addMessageWithFeedback(MessageLevel messageLevel, String message, String detail, FeedbackContext feedbackContext);

	void clear();

	void startAction(ActionType type);

	void endCurrentAction();

	Long getCurrentActionId();

	Range openRange();

	interface Range
	{
		int getKey();

		void close();

		boolean isClosed();

		List<Message> getMessages();

		boolean containsMessageWithLevel(MessageLevel messageLevel);
	}

	Formatter getFormatter();

	void setFormatter(Formatter formatter);

	FeedbackContext getFeedbackContext();
}
