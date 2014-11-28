package at.jit.remind.web.ui.controller.security;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.AssertTrue;

import org.jboss.solder.logging.Logger;

import at.jit.remind.web.domain.security.model.User;
import at.jit.remind.web.domain.security.qualifier.Admin;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.domain.security.service.AdminUserCreator;
import at.jit.remind.web.domain.security.service.UserGateway;
import at.jit.remind.web.ui.controller.base.EntityControllerBase;

@Admin
@LoggedIn
@Named
@SessionScoped
public class UserController extends EntityControllerBase<User>
{
	private static final long serialVersionUID = 246994052569690579L;

	@Inject
	private Logger logger;

	@Inject
	private UserGateway userGateway;

	private boolean enterValuesPanelExpanded;

	private PasswordValidator passwordValidator;
	private UsernameAvailableValidator userAvailableValidator;

	@PostConstruct
	protected void initialize()
	{
		logger.info("userController.initialize() called");

		setEntityGateway(userGateway);
		reset();
	}

	public boolean isUsernameEditable()
	{
		return !isManaged();
	}

	public boolean isEnterValuesPanelExpanded()
	{
		return enterValuesPanelExpanded;
	}

	public void setEnterValuesPanelExpanded(boolean enterValuesPanelExpanded)
	{
		this.enterValuesPanelExpanded = enterValuesPanelExpanded;
	}

	@Override
	public User create()
	{
		return new User();
	}

	@Override
	public void setId(long id)
	{
		// discard previous unsaved changes if any
		cancel();

		super.setId(id);
		setEnterValuesPanelExpanded(true);

		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		ViewHandler viewHandler = application.getViewHandler();
		UIViewRoot viewRoot = viewHandler.createView(context, context.getViewRoot().getViewId());
		context.setViewRoot(viewRoot);
		context.renderResponse();
	}

	@Override
	public String save()
	{
		logger.info("userController.save() called");

		return super.save();
	}

	@Override
	public void delete(long id)
	{
		logger.info("userController.delete() called");

		User user = userGateway.find(id);
		if (!AdminUserCreator.adminUsername.equals(user.getUsername()))
		{
			super.delete(id);
		}

		reset();
	}

	public void reset()
	{
		logger.info("userController.reset() called");

		enterValuesPanelExpanded = false;
		passwordValidator = new PasswordValidator();
		userAvailableValidator = new UsernameAvailableValidator();
	}

	@Override
	public String cancel()
	{
		reset();

		return super.cancel();
	}

	public PasswordValidator getPasswordValidator()
	{
		return passwordValidator;
	}

	public UsernameAvailableValidator getUserAvailableValidator()
	{
		return userAvailableValidator;
	}

	public class PasswordValidator implements Cloneable
	{
		private String confirmation;

		@Override
		public Object clone() throws CloneNotSupportedException
		{
			return super.clone();
		}

		public String getConfirmation()
		{
			return confirmation;
		}

		public void setConfirmation(String confirmation)
		{
			this.confirmation = confirmation;
		}

		@AssertTrue(message = "Passwords do not match")
		boolean isConfirmed()
		{
			logger.info("userController[" + hashCode() + "].isConfirmed() called: " + getEntity().getPassword() + " " + confirmation);

			return getEntity().validatePassword(confirmation);
		}
	}

	public class UsernameAvailableValidator implements Cloneable
	{
		@Override
		public Object clone() throws CloneNotSupportedException
		{
			return super.clone();
		}

		@AssertTrue(message = "Username already taken")
		boolean isUsernameAvailable()
		{
			logger.info("userController[" + hashCode() + "].isUsernameAvailable() called: " + getEntity().getUsername());

			if (isManaged())
			{
				return true;
			}

			return !userGateway.existsByUsername(getEntity().getUsername());
		}
	}
}
