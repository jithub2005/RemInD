package at.jit.remind.core.context.messaging;

import java.util.ArrayList;
import java.util.List;

import at.jit.remind.core.model.RemindModelFeedback;

public class ListBasedMessageHandler implements MessageHandler
{
	private FeedbackContext currentFeedbackContext;
	private Formatter formatter = new LineBreakFormatter();

	private Feedback feedback = RemindModelFeedback.None;

	private List<Message> messageList = new ArrayList<Message>();

	@Override
	public void addMessage(String message)
	{
		addMessage(MessageLevel.INFO, message, "");
	}

	@Override
	public void addMessage(MessageLevel messageLevel, String message, String detail)
	{
		messageList.add(new Message(messageLevel, message, detail));
	}

	@Override
	public String addAndFormatMessage(MessageLevel messageLevel, String message, String detail)
	{
		addMessage(messageLevel, message, detail);

		return formatter.format(messageLevel, message, detail);
	}

	@Override
	public Feedback addMessageWithFeedback(MessageLevel messageLevel, String message, String detail, FeedbackContext feedbackContext)
	{
		addMessage(messageLevel, message, detail);

		return feedback;
	}

	@Override
	public void clear()
	{
		messageList.clear();
	}

	@Override
	public void startAction(ActionType type)
	{
	}

	@Override
	public void endCurrentAction()
	{
	}

	@Override
	public Long getCurrentActionId()
	{
		return null;
	}

	public List<Message> getMessageList()
	{
		return messageList;
	}

	public Feedback getFeedback()
	{
		return feedback;
	}

	public void setFeedback(Feedback feedback)
	{
		this.feedback = feedback;
	}

	@Override
	public Range openRange()
	{
		return new RangeImpl(messageList.size());
	}

	private class RangeImpl implements Range
	{
		private int startPosition = -1;
		private int endPosition = -1;

		public RangeImpl(int startPosition)
		{
			this.startPosition = startPosition;
		}

		@Override
		public int getKey()
		{
			// TODO Auto-generated method stub
			// Maybe for "history", but maybe not needed, TBD
			return 0;
		}

		@Override
		public void close()
		{
			endPosition = messageList.size();
		}

		@Override
		public boolean isClosed()
		{
			return endPosition != -1;
		}

		@Override
		public List<Message> getMessages()
		{
			return messageList.subList(startPosition, endPosition);
		}

		@Override
		public boolean containsMessageWithLevel(MessageLevel messageLevel)
		{
			for (Message m : getMessages())
			{
				if (messageLevel.equals(m.getLevel()))
				{
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public Formatter getFormatter()
	{
		return formatter;
	}

	@Override
	public void setFormatter(Formatter formatter)
	{
		this.formatter = formatter;
	}

	@Override
	public FeedbackContext getFeedbackContext()
	{
		return currentFeedbackContext;
	}

	protected void setCurrentFeedbackContext(FeedbackContext currentFeedbackContext)
	{
		this.currentFeedbackContext = currentFeedbackContext;
	}
}
