package at.jit.remind.web.domain.security.service;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jboss.solder.logging.Logger;

import at.jit.remind.web.domain.security.model.User;

@Singleton
@Startup
public class AdminUserCreator
{
	public static final String adminUsername = "admin";

	@Inject
	private Logger logger;

	@Inject
	private UserGateway userGateway;

	@SuppressWarnings("unused")
	@PostConstruct
	private void initialize()
	{
		logger.info("adminService.initialize(): checking for admin user");

		if (userGateway.existsByUsername(adminUsername))
		{
			logger.info("adminService.initialize(): admin user found");

			return;
		}

		User user = new User();
		user.setUsername(adminUsername);
		user.setPassword(adminUsername);
		user.setVerified(true);

		userGateway.create(user);
		userGateway.save();

		logger.info("adminService.initialize(): admin user successfully created");
	}
}
