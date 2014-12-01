package at.jit.remind.web.ui.controller.security;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import at.jit.remind.web.domain.security.qualifier.LoggedIn;

@LoggedIn
@Named
@SessionScoped
public class ChangePasswordController implements Serializable
{
	private static final long serialVersionUID = 6241753114226470571L;

	@Inject
	private LoginController loginController;

	public LoginController getLoginController()
	{
		return loginController;
	}
}
