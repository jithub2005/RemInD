package at.jit.remind.web.ui.controller.security;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class SecurityExceptionHandlerFactory extends ExceptionHandlerFactory
{
	private ExceptionHandlerFactory parent;

	public SecurityExceptionHandlerFactory(ExceptionHandlerFactory parent)
	{
		this.parent = parent;
	}

	@Override
	public ExceptionHandler getExceptionHandler()
	{
		return new SecurityExceptionHandler(parent.getExceptionHandler());
	}
}
