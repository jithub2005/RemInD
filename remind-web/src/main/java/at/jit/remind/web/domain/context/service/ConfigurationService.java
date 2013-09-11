package at.jit.remind.web.domain.context.service;

import java.util.List;
import java.util.Properties;

import javax.ejb.ApplicationException;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.jboss.solder.logging.Logger;

import at.jit.remind.web.domain.context.model.Configuration;
import at.jit.remind.web.domain.context.model.Property;

@Named
@Stateful
@SessionScoped
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ConfigurationService implements PropertiesProviderService
{
	@Inject
	private Logger logger;

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	@Override
	public Properties getProperties(String lookupKey)
	{
		logger.info("configurationService.getProperties(" + lookupKey + ") called");
		Configuration configuration = findByLookupKey(lookupKey);

		if (configuration == null)
		{
			throw new MissingConfigurationException(lookupKey);
		}

		return getProperties(configuration);
	}

	@Override
	public Properties getPossiblyEmptyProperties(String lookupKey)
	{
		logger.info("configurationService.getPossiblyEmptyProperties(" + lookupKey + ") called");

		return getProperties(findByLookupKey(lookupKey));
	}

	private Properties getProperties(Configuration configuration)
	{
		Properties properties = new Properties();

		if (configuration != null)
		{
			for (Property property : configuration.getProperties())
			{
				properties.put(property.getKey(), property.getValue());
			}
		}

		return properties;
	}

	@Override
	public Configuration getById(long id)
	{
		logger.info("configurationService.getById(" + id + ")");
		return entityManager.find(Configuration.class, id);
	}

	@Override
	public Configuration findByLookupKey(String lookupKey)
	{
		TypedQuery<Configuration> query = entityManager.createNamedQuery(Configuration.findByLookupKeyQuery, Configuration.class);
		List<Configuration> results = query.setParameter(Configuration.lookupKeyParameter, lookupKey).getResultList();

		// lookup key is unique, hence only one other may exist
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean existsWithLookupKey(String lookupKey)
	{
		return findByLookupKey(lookupKey) != null;
	}

	@Override
	public boolean existsOtherWithLookupKey(Configuration configuration)
	{
		Configuration otherConfiguration = findByLookupKey(configuration.getLookupKey());

		if (otherConfiguration == null)
		{
			return false;
		}

		return !otherConfiguration.getId().equals(configuration.getId());
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void save(Configuration configuration)
	{
		logger.info("configurationService.save(" + configuration.getLookupKey() + ")");
		entityManager.persist(configuration);
	}

	@Override
	public void update(Configuration configuration)
	{
		entityManager.refresh(configuration);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void delete(Configuration configuration)
	{
		entityManager.remove(configuration);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Configuration> getAllByType(String type)
	{
		logger.info("configurationService.getAllByType() called");

		Query query = entityManager.createNamedQuery(Configuration.findByTypeQuery);
		query.setParameter(Configuration.findByTypeParameter, type);

		return query.getResultList();
	}

	public static final class DuplicateConfigurationException extends Exception
	{
		private static final long serialVersionUID = 7410398011433517721L;
	}

	@ApplicationException
	public static final class MissingConfigurationException extends RuntimeException
	{
		private static final long serialVersionUID = -6664695909837574080L;

		private String lookupKey;

		public MissingConfigurationException(String lookupKey)
		{
			this.lookupKey = lookupKey;
		}

		public String getLookupKey()
		{
			return lookupKey;
		}
	}
}
