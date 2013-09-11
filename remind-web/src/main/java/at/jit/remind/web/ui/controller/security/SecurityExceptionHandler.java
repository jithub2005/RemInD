package at.jit.remind.web.ui.controller.security;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import at.jit.remind.web.domain.security.exception.AlreadyLoggedInException;
import at.jit.remind.web.domain.security.exception.AuthenticationException;
import at.jit.remind.web.domain.security.exception.AuthorizationException;

public class SecurityExceptionHandler extends ExceptionHandlerWrapper
{
	private ExceptionHandler wrapped;

	public SecurityExceptionHandler(ExceptionHandler wrapper)
	{
		this.wrapped = wrapper;
	}

	@Override
	public ExceptionHandler getWrapped()
	{
		return wrapped;
	}

	@Override
	public void handle()
	{
		final Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();
		while (iterator.hasNext())
		{
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) iterator.next().getSource();
			Throwable throwable = context.getException();

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();

			try
			{
				if (throwable.getCause() instanceof AlreadyLoggedInException)
				{
					redirectTo(externalContext, "/faces/index.xhtml");
				}
				else if (throwable.getCause() instanceof AuthenticationException || throwable instanceof ViewExpiredException)
				{
					redirectTo(externalContext, "/faces/index.xhtml");
				}
				// TODO: improve check
				else if (throwable.getMessage() != null && throwable.getMessage().contains(AuthorizationException.class.getSimpleName()))
				{
					// TODO: add page
					redirectTo(externalContext, "/faces/unauthorized.jsf");
				}
				else
				{
					// TODO: add dedicated error page
					redirectTo(externalContext, "/faces/index.xhtml");
				}
			}
			finally
			{
				iterator.remove();
			}
		}

		getWrapped().handle();
	}

	private void redirectTo(ExternalContext externalContext, String redirectPage)
	{
		if (!redirectPage.isEmpty())
		{
			try
			{
				externalContext.redirect(externalContext.getRequestContextPath() + redirectPage);
			}
			catch (IOException e)
			{
				throw new FacesException(e);
			}
		}
	}
}
