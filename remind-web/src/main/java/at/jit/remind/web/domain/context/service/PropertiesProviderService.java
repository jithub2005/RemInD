package at.jit.remind.web.domain.context.service;

import java.util.List;
import java.util.Properties;

import at.jit.remind.core.context.PropertiesProvider;
import at.jit.remind.web.domain.context.model.Configuration;

public interface PropertiesProviderService extends PropertiesProvider
{
	@Override
	Properties getProperties(String key);

	@Override
	Properties getPossiblyEmptyProperties(String key);

	Configuration getById(long id);

	Configuration findByLookupKey(String lookupKey);

	boolean existsWithLookupKey(String lookupKey);

	boolean existsOtherWithLookupKey(Configuration configuration);

	void save(Configuration configuration);

	void update(Configuration configuration);

	void delete(Configuration configuration);

	List<Configuration> getAllByType(String type);
}
