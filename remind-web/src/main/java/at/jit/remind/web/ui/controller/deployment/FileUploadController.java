package at.jit.remind.web.ui.controller.deployment;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.event.FileUploadListener;
import org.richfaces.model.UploadedFile;

import at.jit.remind.core.context.messaging.MessageHandler;
import at.jit.remind.web.domain.context.reporting.model.FileInfo;
import at.jit.remind.web.domain.context.reporting.service.FileInfoService;
import at.jit.remind.web.domain.messaging.qualifier.FileUploadStart;
import at.jit.remind.web.domain.security.model.User;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.domain.security.qualifier.SessionUser;

@LoggedIn
@Named
@RequestScoped
public class FileUploadController implements FileUploadListener
{
	@Inject
	private MessageHandler messageHandler;

	@Inject
	@SessionUser
	private User sessionUser;

	@Inject
	private String sessionId;

	@Inject
	private FileInfoService fileInfoService;

	@FileUploadStart
	@Inject
	private Event<FileInfo> fileInfoEvent;

	@Override
	public void processFileUpload(FileUploadEvent event)
	{
		UploadedFile uploadedFile = event.getUploadedFile();

		try
		{
			String content = IOUtils.toString(uploadedFile.getInputStream());

			FileInfo fileInfo = new FileInfo();
			fileInfo.setName(uploadedFile.getName());
			fileInfo.setContent(content);
			fileInfo.setSessionId(sessionId);
			fileInfo.setUser(sessionUser);

			fileInfoEvent.fire(fileInfoService.create(fileInfo));
		}
		catch (IOException e)
		{
			// TODO: proper error handling

			return;
		}
	}
}
