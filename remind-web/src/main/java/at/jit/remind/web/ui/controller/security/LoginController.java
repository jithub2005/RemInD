package at.jit.remind.web.ui.controller.security;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.jboss.solder.logging.Logger;

import at.jit.remind.core.context.messaging.MessageHandler;
import at.jit.remind.web.domain.context.reporting.model.WebActionType;
import at.jit.remind.web.domain.security.model.User;
import at.jit.remind.web.domain.security.qualifier.Logout;
import at.jit.remind.web.domain.security.qualifier.SessionUser;
import at.jit.remind.web.domain.security.service.AdminUserCreator;
import at.jit.remind.web.domain.security.service.UserGateway;
import at.jit.remind.web.domain.security.service.UserGateway.InvalidUsernameOrPasswordException;
import at.jit.remind.web.ui.controller.deployment.TreeBean;

@Named
@SessionScoped
public class LoginController implements Serializable
{
	private static final long serialVersionUID = -5093263946177052005L;

	@Inject
	private Logger logger;

	@Inject
	@Logout
	private Event<User> logoutEvent;

	@Inject
	private MessageHandler messageHandler;

	@Inject
	private UserGateway userGateway;

	@Inject
	private TreeBean treeBean;

	private User sessionUser;
	private String sessionId;

	private String username;
	private String password;

	public String getUserName()
	{
		return username;
	}

	public void setUserName(String userName)
	{
		this.username = userName;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean isLoggedIn()
	{
		return sessionUser != null;
	}

	@SessionUser
	@Produces
	@Named
	public User getSessionUser()
	{
		return sessionUser;
	}

	@Produces
	@Named
	public String getSessionId()
	{
		return sessionId;
	}

	public void login()
	{
		logger.info("loginController.login(): attempt to login user " + username);

		try
		{
			User user = userGateway.findByUsername(username);

			if (!user.validatePassword(password))
			{
				FacesContext.getCurrentInstance().addMessage("login", new FacesMessage("Wrong username or password"));

				return;
			}

			logger.info("loginController.login(): user " + user.getUsername() + " successfully logged it");
			sessionUser = user;
			sessionId = UUID.randomUUID().toString();

			messageHandler.startAction(WebActionType.Login);
			messageHandler.addMessage("User " + user.getUsername() + " successfully logged in");
			messageHandler.endCurrentAction();
		}
		catch (InvalidUsernameOrPasswordException e)
		{
			logger.info("loginController.login(): failed to login user " + username);

			FacesContext.getCurrentInstance().addMessage("login", new FacesMessage("Wrong username or password"));
		}
	}

	public void reset()
	{
		sessionUser = null;
		sessionId = null;

		username = "";
		password = "";
	}

	public void logout()
	{
		if (messageHandler.getCurrentActionId() != null)
		{
			logger.info("loginController.logout(): actions pending");

			FacesContext.getCurrentInstance().addMessage("logout", new FacesMessage("You cannot logout while actions are still pending"));

			return;
		}

		logoutEvent.fire(sessionUser);

		messageHandler.startAction(WebActionType.Logout);

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
		httpSession.invalidate();

		logger.info("loginController.logout(): user " + sessionUser.getUsername() + " successfully logged out");
		messageHandler.addMessage("User " + sessionUser.getUsername() + " successfully logged out");
		messageHandler.endCurrentAction();

		reset();

		try
		{
			ExternalContext externalContext = facesContext.getExternalContext();
			externalContext.redirect(externalContext.getRequestContextPath() + "/faces/index.xhtml");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isAdmin()
	{
		if (!isLoggedIn())
		{
			return false;
		}

		return AdminUserCreator.adminUsername.equals(sessionUser.getUsername());
	}
}
