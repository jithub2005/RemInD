package at.jit.remind.core.context.messaging;

import java.util.HashMap;

public abstract class KeyValueBasedFormatter implements Formatter
{
	private HashMap<String, String> valueMap = new HashMap<String, String>();

	@Override
	public void setData(String key, String value)
	{
		valueMap.put(key, value);
	}

	@Override
	public void clearData()
	{
		valueMap.clear();
	}

	protected HashMap<String, String> getValueMap()
	{
		return valueMap;
	}
}
