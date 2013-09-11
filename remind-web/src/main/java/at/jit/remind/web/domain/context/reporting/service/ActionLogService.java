package at.jit.remind.web.domain.context.reporting.service;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.web.domain.base.service.EntityServiceBase;
import at.jit.remind.web.domain.context.reporting.model.Action;
import at.jit.remind.web.domain.context.reporting.model.ActionLog;
import at.jit.remind.web.domain.messaging.util.MessageSplitter;

@Stateless
public class ActionLogService extends EntityServiceBase<ActionLog>
{
	private static final long serialVersionUID = 5448031175208259237L;

	private static final int maximumLengthOfMessage = 250;

	@PostConstruct
	@Override
	protected void initialize()
	{
		super.initialize();
		setEntityClazz(ActionLog.class);
	}

	public void storeMessage(Action currentAction, String message, MessageLevel messageLevel)
	{
		if (currentAction != null && message != null)
		{
			// when host OS is Windows, replace LF (\n) with CRLF(\r\n) separator
			if (System.getProperty("os.name").contains("Windows"))
			{
				message = message.replaceAll("(\r\n|\n)", "\r\n");
			}

			String[] messageRows = message.split(System.getProperty("line.separator"));

			for (String row : messageRows)
			{
				if (row.length() > maximumLengthOfMessage)
				{
					for (String rowPart : MessageSplitter.splitMessageByLengthAndSpace(row, maximumLengthOfMessage))
					{
						createActionLog(currentAction, messageLevel, rowPart);
					}
				}
				else
				{
					createActionLog(currentAction, messageLevel, row);
				}
			}
		}
	}

	private void createActionLog(Action currentAction, MessageLevel messageLevel, String rowPart)
	{
		ActionLog actionLog = new ActionLog();
		actionLog.setLogText(rowPart);
		actionLog.setLogLevel(messageLevel.toString());
		actionLog.setAction(currentAction);

		create(actionLog);
	}

	public Long getActionIdForActionLogId(Long actionLogId)
	{
		return find(actionLogId).getAction().getId();
	}
}
