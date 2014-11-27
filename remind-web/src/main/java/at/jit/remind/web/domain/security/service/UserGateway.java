package at.jit.remind.web.domain.security.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.jboss.solder.logging.Logger;

import at.jit.remind.web.domain.base.gateway.EntityGatewayBase;
import at.jit.remind.web.domain.security.model.User;

@Named
@Stateful
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UserGateway extends EntityGatewayBase<User>
{
	private static final long serialVersionUID = -7388966178024190105L;

	@Inject
	private Logger logger;

	@PostConstruct
	protected void initialize()
	{
		super.initialize();
		setEntityClazz(User.class);
	}

	public User findByUsername(String username) throws InvalidUsernameOrPasswordException
	{
		TypedQuery<User> query = null;

		query = getEntityManager().createNamedQuery(User.findByUsernameQuery, User.class);
		query.setParameter(User.usernameParameter, username);

		List<User> userList = query.getResultList();
		if (userList.size() != 1)
		{
			throw new InvalidUsernameOrPasswordException();
		}

		return userList.get(0);
	}

	public boolean existsByUsername(String username)
	{
		TypedQuery<Long> query = getEntityManager().createNamedQuery(User.existsByUsernameQuery, Long.class);
		query.setParameter(User.usernameParameter, username);

		return query.getSingleResult() == 1;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int setReadOnlyToFalseWhereNull()
	{
		Query query = getEntityManager().createNamedQuery(User.setReadOnlyToFalseIfNull);
		
		return query.executeUpdate();
	}

	public static final class InvalidUsernameOrPasswordException extends Exception
	{
		private static final long serialVersionUID = 5412430010629597847L;
	}
}
