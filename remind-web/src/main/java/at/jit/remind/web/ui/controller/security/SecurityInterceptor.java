package at.jit.remind.web.ui.controller.security;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import at.jit.remind.web.domain.security.exception.AuthenticationException;
import at.jit.remind.web.domain.security.exception.AuthorizationException;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.domain.security.qualifier.NotReadOnly;

@LoggedIn
@Interceptor
public class SecurityInterceptor implements Serializable
{
	private static final long serialVersionUID = -3864195603004038092L;

	@Inject
	private LoginController loginController;

	public SecurityInterceptor()
	{
	}

	@AroundInvoke
	public Object checkPermissions(InvocationContext context) throws Exception // NOPMD - need to call proceed() throws
																				// Exception
	{
		if (!loginController.isLoggedIn())
		{
			throw new AuthenticationException();
		}

		// TODO: call checkAnnotations
		boolean authorized = checkAnnotations(context, NotReadOnly.class, new SecurityVerifier()
		{
			@Override
			public boolean verify()
			{
				return !loginController.isReadOnly();
			}
		});

		if (!authorized)
		{
			throw new AuthorizationException();
		}

		return context.proceed();
	}

	protected boolean checkAnnotations(InvocationContext context, Class<? extends Annotation> securityClazz, SecurityVerifier checker)
	{
		boolean verified = true;
		if (context.getMethod().getDeclaringClass().isAnnotationPresent(securityClazz))
		{
			verified &= checker.verify();
		}
		if (context.getMethod().getAnnotation(securityClazz) != null)
		{
			verified &= checker.verify();
		}

		return verified;
	}

	// TODO: only for prototype, should be handled in more generic way
	private interface SecurityVerifier
	{
		boolean verify();
	}
}
