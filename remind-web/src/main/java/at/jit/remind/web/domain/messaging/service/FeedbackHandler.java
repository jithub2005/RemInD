package at.jit.remind.web.domain.messaging.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.solder.logging.Logger;

import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.core.model.RemindModelFeedback;
import at.jit.remind.core.model.content.database.DatabaseFeedback;
import at.jit.remind.web.domain.messaging.qualifier.FeedbackReceived;

@Named
@ApplicationScoped
public class FeedbackHandler implements Serializable
{
	private static final long serialVersionUID = -6068594204408384983L;

	@Inject
	private Logger logger;

	private Map<String, FeedbackContext> feedbackContextMap;

	@Inject
	@FeedbackReceived
	private Event<Feedback> feedbackReceivedEvent;

	@PostConstruct
	@SuppressWarnings("unused")
	private void initialize()
	{
		logger.info("feedbackHandler.initialize() called: " + hashCode());
		feedbackContextMap = new HashMap<String, FeedbackContext>();
	}

	private void handleFeedback(String identifier, Feedback feedback)
	{
		if (!feedbackContextMap.containsKey(identifier))
		{
			return;
		}

		feedbackContextMap.remove(identifier);
		feedbackReceivedEvent.fire(feedback);
	}

	public Collection<FeedbackContext> getAllFeedbackContexts()
	{
		return feedbackContextMap.values();
	}

	public FeedbackContext getFeedbackContext(String identifier)
	{
		return feedbackContextMap.get(identifier);
	}

	public void addContext(String identifier, FeedbackContext feedbackContext)
	{
		feedbackContextMap.put(identifier, feedbackContext);
	}

	public void abort(String identifier)
	{
		logger.info("feedbackHandler.abort() called");

		handleFeedback(identifier, RemindModelFeedback.Abort);
	}

	public void skip(String identifier)
	{
		logger.info("feedbackHandler.skip() called");

		handleFeedback(identifier, RemindModelFeedback.Skip);
	}

	public void skipStatement(String identifier)
	{
		logger.info("feedbackHandler.skipStatement() called");

		handleFeedback(identifier, DatabaseFeedback.SkipStatement);
	}
}
