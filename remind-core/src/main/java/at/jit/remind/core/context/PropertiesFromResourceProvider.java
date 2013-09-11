package at.jit.remind.core.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFromResourceProvider implements PropertiesProvider
{
	@Override
	public Properties getProperties(String key)
	{
		return getPossiblyEmptyProperties(key);
	}

	@Override
	public Properties getPossiblyEmptyProperties(String key)
	{
		String modifiedKey = key.replace("://", "___").replace("/", "_");
		Properties properties = new Properties();

		InputStream inputStream = getClass().getResourceAsStream("/" + modifiedKey);

		if (inputStream != null)
		{
			try
			{
				properties.load(inputStream);
			}
			catch (IOException e) // NOSONAR
			{
				// TODO: add logging
			}
			finally
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e) // NOSONAR
				{

				}
			}
		}

		return properties;
	}
}
