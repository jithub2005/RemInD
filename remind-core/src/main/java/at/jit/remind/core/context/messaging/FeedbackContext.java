package at.jit.remind.core.context.messaging;

import java.util.HashMap;
import java.util.Map;

public class FeedbackContext
{
	public static final String soureFilePathDataKey = "source.file.path";
	public static final String errorCauseDataKey = "error.cause";
	public static final String errorMessageKey = "error.message";
	public static final String containsFixFeedbackKey = "contains.fixfeedback";

	private Map<Feedback, String> feedbackLabelMap = new HashMap<Feedback, String>();
	private Map<String, String> dataMap = new HashMap<String, String>();

	private FeedbackContext()
	{
		/*Per default FeedbackContext does not contain a FixStatementFeedback*/
		dataMap.put(containsFixFeedbackKey, "false");
	}

	public Feedback[] getAvailableFeedbacks()
	{
		return feedbackLabelMap.keySet().toArray(new Feedback[]{});
	}

	public Map<Feedback, String> getFeedbackLabelMap()
	{
		return feedbackLabelMap;
	}

	public Map<String, String> getDataMap()
	{
		return dataMap;
	}

	public String getFeedbackLabel(Feedback feedback)
	{
		return feedbackLabelMap.get(feedback);
	}

	public String getData(String key)
	{
		return dataMap.get(key);
	}

	public static final class Builder
	{
		private FeedbackContext feedbackContext = new FeedbackContext();

		public Builder withFeedback(Feedback feedback)
		{
			feedbackContext.feedbackLabelMap.put(feedback, feedback.name());

			return this;
		}

		public Builder withFeedback(Feedback feedback, String label)
		{
			feedbackContext.feedbackLabelMap.put(feedback, label);

			return this;
		}

		public Builder withData(String key, String value)
		{
			feedbackContext.dataMap.put(key, value);

			return this;
		}

		public FeedbackContext build()
		{
			return feedbackContext;
		}
	}
}
