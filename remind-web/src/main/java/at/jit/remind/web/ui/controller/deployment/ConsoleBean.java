package at.jit.remind.web.ui.controller.deployment;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.solder.logging.Logger;
import org.richfaces.cdi.push.Push;

import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.web.domain.messaging.qualifier.FeedbackReceived;
import at.jit.remind.web.domain.messaging.qualifier.Message;
import at.jit.remind.web.domain.messaging.qualifier.RequestFeedback;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;

@LoggedIn
@Named
@SessionScoped
public class ConsoleBean implements Serializable
{
	private static final long serialVersionUID = 2341078825434018583L;

	private static final String topicName = "remindTopic";

	@Inject
	private Logger logger;

	@Inject
	@Push(topic = topicName, subtopic = "#{consoleBean.identifier}")
	private Event<String> pushEvent;

	@Inject
	@Push(topic = topicName, subtopic = "#{consoleBean.identifierForFeedback}")
	private Event<String> pushFeedbackEvent;

	private String identifier;
	private String feedbackUrl;

	private boolean feedbackPending = false;

	private List<String> lines = new ArrayList<String>();

	@PostConstruct
	protected void initialize()
	{
		identifier = UUID.randomUUID().toString().replace("-", "");
		logger.info("consoleBean[" + identifier + "].initialize() called: topicName=" + topicName);

		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

		feedbackUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/remind-web/rest/feedback";
		logger.info("consoleBean[" + identifier + "].initialize(): feedbackUrl=" + feedbackUrl);
	}

	public void handleMessageEvent(@Observes @Message String message)
	{
		logger.debug("consoleBean[" + identifier + "].handleMessageEvent() called: " + message);
		addLine(message);
	}

	public void forwardRequestFeedbackEvent(@Observes @RequestFeedback String message)
	{
		logger.info("consoleBean[" + identifier + "].forwardRequestFeedbackEvent() called: " + message);
		addLine("Requesting user feedback with message: " + message);
		logger.info("consoleBean[" + identifier + "].forwardRequestFeedbackEvent(): pushing feedback event...");
		pushFeedbackEvent.fire(message);
		feedbackPending = true;
		logger.info("consoleBean[" + identifier + "].forwardRequestFeedbackEvent(): feedback event pushed");
	}

	public void handleFeedbackReceivedEvent(@Observes @FeedbackReceived Feedback feedback)
	{
		logger.info("consoleBean[" + identifier + "].feedbackReceivedListener(): " + feedback.name());
		feedbackPending = false;
	}

	public void clearConsole()
	{
		lines.clear();
	}

	public void exportConsoleAsFile()
	{
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) faces.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		response.setContentType("application/x-download");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-disposition", "attachment; filename=\"Console_Export-" + new Date() + ".txt\"");

		try
		{
			ServletOutputStream os = response.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);

			boolean isUnix = !request.getHeader("User-Agent").contains("Windows");

			for (String line : lines)
			{
				if (isUnix)
				{
					line = String.format(line + "%n");
				}
				else
				{
					if (line.contains("\n"))
					{
						line = line.replace("\n", "\r\n");
					}
					else
					{
						line += "\r\n";
					}
				}

				osw.write(line);
			}
			osw.flush();
		}
		catch (IOException e)
		{
			logger.error("consoleBean[" + identifier + "].exportConsoleAsFile(): could not export textual file with console logs.");
		}

		faces.responseComplete();
	}

	public List<String> getLines()
	{
		return lines;
	}

	private void addLine(String line)
	{
		logger.debug("consoleBean[" + identifier + "].addLine() called: " + line);
		lines.add(line);
		logger.debug("consoleBean[" + identifier + "].addLine(): pushing console event...");
		pushEvent.fire(line);
		logger.debug("consoleBean[" + identifier + "].addLine(): console event pushed");
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getIdentifierForFeedback()
	{
		return identifier + "Feedback";
	}

	public String getFeedbackUrl()
	{
		return feedbackUrl;
	}

	public String getConsolePushAddress()
	{
		return getIdentifier() + "@" + topicName;
	}

	public String getFeedbackPushAddress()
	{
		return getIdentifierForFeedback() + "@" + topicName;
	}

	public boolean isFeedbackPending()
	{
		return feedbackPending;
	}
}
