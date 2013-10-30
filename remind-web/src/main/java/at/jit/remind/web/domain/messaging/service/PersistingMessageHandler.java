package at.jit.remind.web.domain.messaging.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.solder.logging.Logger;

import at.jit.remind.core.context.RemindCompositeKeyProvider;
import at.jit.remind.core.context.messaging.ActionType;
import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.context.messaging.MessageHandler;
import at.jit.remind.core.model.RemindModelFeedback;
import at.jit.remind.web.domain.context.model.Configuration;
import at.jit.remind.web.domain.context.reporting.model.Action;
import at.jit.remind.web.domain.context.reporting.model.FileInfo;
import at.jit.remind.web.domain.context.reporting.model.WebActionType;
import at.jit.remind.web.domain.context.reporting.service.ActionLogService;
import at.jit.remind.web.domain.context.reporting.service.ActionService;
import at.jit.remind.web.domain.context.service.ConfigurationService.DuplicateConfigurationException;
import at.jit.remind.web.domain.context.service.PropertiesProviderService;
import at.jit.remind.web.domain.messaging.qualifier.FeedbackReceived;
import at.jit.remind.web.domain.messaging.qualifier.FileUploadEnd;
import at.jit.remind.web.domain.messaging.qualifier.FileUploadStart;
import at.jit.remind.web.domain.messaging.qualifier.Identifier;
import at.jit.remind.web.domain.messaging.qualifier.Message;
import at.jit.remind.web.domain.messaging.qualifier.RequestFeedback;
import at.jit.remind.web.domain.security.model.User;
import at.jit.remind.web.domain.security.qualifier.Logout;
import at.jit.remind.web.domain.security.qualifier.SessionUser;

@Named("messageHandler")
@SessionScoped
public class PersistingMessageHandler extends ListBasedMessageHandler implements Serializable
{
	private static final long serialVersionUID = -7038042617575475797L;

	private static final String Delay = "delay";
	private static long DefaultDelay = 100;

	private static final RemindCompositeKeyProvider messageHandlerKeyProvider = new RemindCompositeKeyProvider(MessageHandler.class.getSimpleName(),
			new String[]{}, new String[]{Delay});

	@Inject
	private Logger logger;

	@Inject
	@SessionUser
	private User sessionUser;

	@Inject
	private String sessionId;

	@FileUploadEnd
	@Inject
	private Event<FileInfo> fileInfoEvent;

	@Inject
	@Message
	private Event<String> messageEvent;

	@Inject
	@RequestFeedback
	private Event<String> requestFeedbackEvent;

	@Inject
	private FeedbackHandler feedbackHandler;

	@Inject
	private PropertiesProviderService configurationService;

	@Inject
	private ActionService actionService;

	@Inject
	private ActionLogService actionLogService;

	private String identifier;

	private long delay;

	private Stack<Action> actionStack;

	private FileInfo currentFileInfo;

	private CountDownLatch latch;

	@PostConstruct
	@SuppressWarnings("unused")
	private void initialize() throws DuplicateConfigurationException
	{
		String lookupKey = messageHandlerKeyProvider.getLookupId(new HashMap<String, String>());
		actionStack = new Stack<Action>();
		if (configurationService.existsWithLookupKey(lookupKey))
		{
			Properties properties = configurationService.getProperties(lookupKey);
			delay = Integer.valueOf(properties.getProperty(Delay, String.valueOf(DefaultDelay)));
		}
		else
		{
			delay = DefaultDelay;
			Configuration configuration = new Configuration.Builder().withLookupKey(lookupKey).withProperty(Delay, String.valueOf(delay)).build();
			configurationService.save(configuration);
		}
	}

	@PreDestroy
	protected void preDestroy()
	{
		if (latch != null)
		{
			latch.countDown();
		}
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(@Observes @Identifier String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public void startAction(ActionType type)
	{
		logger.info("messageHandler[identifier=" + identifier + "].startAction(): " + type);

		Action action = new Action();
		action.setType(type);
		action.setUser(sessionUser);
		action.setSessionId(sessionId);
		action.setFileInfo(currentFileInfo);

		action = actionService.create(action);

		if (!actionStack.isEmpty())
		{
			Action parentAction = actionStack.pop();
			parentAction.addChild(action);
			actionStack.push(actionService.addChild(parentAction, action));
		}

		actionStack.push(action);

		logger.info("messageHandler[identifier=" + identifier + "].startAction() finished: " + action.getId());
	}

	@Override
	public void endCurrentAction()
	{
		if (actionStack.isEmpty())
		{
			logger.info("messageHandler[identifier=" + identifier + "].endCurrentAction(): null");

			return;
		}

		Action currentAction = actionStack.peek();

		logger.info("messageHandler[identifier=" + identifier + "].endCurrentAction(): " + currentAction.getId());

		super.endCurrentAction();

		currentAction.setEnded(new Date());
		actionService.update(currentAction);

		actionStack.pop();

		logger.info("messageHandler[identifier=" + identifier + "].endCurrentAction() finished: new currentActionId="
				+ (currentAction != null ? currentAction.getId() : null));
	}

	@Override
	public Long getCurrentActionId()
	{
		return actionStack.isEmpty() ? null : actionStack.peek().getId();
	}

	public void handleFileUpload(@Observes @FileUploadStart FileInfo fileInfo)
	{
		logger.info("messageHandler[identifier=" + identifier + "].handleFileUpload(): " + fileInfo.getId());

		currentFileInfo = fileInfo;
		startAction(WebActionType.FileUpload);

		addMessage("Loading file " + fileInfo.getName());
		addMessage("Content: ");
		addMessage(fileInfo.getContent());

		fileInfoEvent.fire(fileInfo);
	}

	@Override
	public void addMessage(MessageLevel messageLevel, String message, String detail)
	{
		logger.info("messageHandler[identifier=" + identifier + "].addMessage(): " + message);
		super.addMessage(messageLevel, message, detail);

		// Save current action logs
		actionLogService.storeMessage(actionStack.isEmpty() ? null : actionStack.peek(), message, messageLevel);

		fireMessage(message);
	}

	@Override
	public Feedback addMessageWithFeedback(MessageLevel messageLevel, String message, String detail, FeedbackContext feedbackContext)
	{
		setCurrentFeedbackContext(feedbackContext);

		String formattedMessage = addAndFormatMessage(messageLevel, message, detail);

		requestFeedbackEvent.fire(formattedMessage);

		feedbackContext.getDataMap().put(FeedbackContext.errorMessageKey, formattedMessage);
		feedbackHandler.addContext(identifier, feedbackContext);

		latch = new CountDownLatch(1);
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			logger.error("messageHandler[identifier=" + identifier + "].addMessageWithFeedback(): " + e.getMessage());
			setFeedback(RemindModelFeedback.Abort);
		}
		latch = null;

		logger.info("messageHandler[identifier=" + identifier + "].addMessageWithFeedback(): received feedback " + getFeedback());

		return getFeedback();
	}

	public void handleFeedbackReceivedEvent(@Observes @FeedbackReceived Feedback feedback)
	{
		logger.info("messageHandler[identifier=" + identifier + "].handleFeedbackReceivedEvent(): " + feedback.name());
		setFeedback(feedback);
		latch.countDown();
		logger.info("messageHandler[identifier=" + identifier + "].handleFeedbackReceivedEvent() finished");
	}

	public void handleLogout(@Observes @Logout User user)
	{
		logger.info("messageHandler[identifier=" + identifier + "].handleLogout(): " + user.getUsername());
		feedbackHandler.abort(identifier);
		logger.info("messageHandler[identifier=" + identifier + "].handleLogout() finished");
	}

	private void fireMessage(String message)
	{
		logger.debug("messageHandler[identifier=" + identifier + "].fireMessage(): firing message event...");
		messageEvent.fire(message);

		try
		{
			Thread.sleep(delay);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.debug("messageHandler[identifier=" + identifier + "].fireMessage(): message event fired");
	}
}
