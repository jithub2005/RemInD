package at.jit.remind.web.domain.security.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import at.jit.remind.web.domain.security.model.User;

@Named
@Stateless
public class UserListService implements Serializable
{
	private static final long serialVersionUID = -6886431294310722511L;

	@PersistenceContext
	private EntityManager entityManager;

	public List<User> findAll()
	{
		TypedQuery<User> query = entityManager.createNamedQuery(User.findAllQuery, User.class);

		return query.getResultList();
	}

	public List<User> searchByUsername(String username)
	{
		TypedQuery<User> query = entityManager.createNamedQuery(User.searchByUsernameQuery, User.class);
		query.setParameter(User.usernameParameter, "%" + username + "%");

		return query.getResultList();
	}
}
