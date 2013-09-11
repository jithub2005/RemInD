package at.jit.remind.core.context;

import java.util.Properties;

public interface PropertiesProvider
{
	Properties getProperties(String key);

	Properties getPossiblyEmptyProperties(String key);
}
